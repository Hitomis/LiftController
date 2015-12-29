package com.zhiitek.liftcontroller.components;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

/**
 * <p>
 * 2015-11-01
 * </p>
 * 
 * <p>
 * 用于与Android设备交互的 UPD Socket通信组件
 * </p>
 * 
 * @author ZhaoFan
 *
 */
public class UdpSocketConnection {

	/**
	 * 手机本地socket服务器端口
	 */
	private final int localServerSocketPort = 9998;

	/**
	 * 默认Android设备Socket服务器端口
	 */
	private final int socketPort = 9999;
	
	/**
	 * 另一端地址
	 */
	private String ipAddress;

	/***what flag****/
	private final static int WAHT_CONN_SUCCESS = 100;

	private final static int WHAT_CONN_FAILURE = -100;

	/***obj flag****/
	/**连接超时*/
	public final static int FLAG_CONNECT_TIMEOUT = -101;

	/**wifi未启动*/
	public final static int FLAG_CONNECT_NOT_ENABLE_WIFI = -102;
	
	/**创建 UDP 服务端错误*/
	public final static int FLAG_CREATE_SERVER_ERROR = -103;
	
	/**发送数据错误*/
	public final static int FLAG_POST_DATA_ERROR = -104;
	
	/**当前通信未结束*/
	public final static int FLAG_COMMU_DATA_ING = -105;
	
	private DatagramSocket datagramSocket;

	private WifiManager wifiMan;

	private SocketDataCallback callbackListener;
	
	/** 是否在通信, 用于解决：第一次发送数据后还没有接收到返回的消息,当前端又发送了数据的情形*/
	private boolean isCommunicate = false;
	
	/**
	 * 是否退出调试设备
	 */
	private boolean exitDebug = false;

	/**
	 * 与android设备终端通信使用
	 * @param context
	 *            使用ApplicationContext
	 */
	public UdpSocketConnection(Context context) {
		wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}
	
	/**
	 * 与网口通信的时候使用
	 * @param ipAddress 通信的另外一端地址
	 */
	public UdpSocketConnection(Context context, final String ipAddress) {
		wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (ipAddress == null || "".equals(ipAddress)){
			throw new IllegalArgumentException("ipAddress not be null or empty string");
		}
		this.ipAddress = ipAddress;
	}

	/**
	 * 将int类型的ip地址转换成字符类型的ip地址
	 * 
	 * @param i
	 * @return
	 */
	private String int2Ip(int i) {
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + ((i >> 24) & 0xFF);
	}

	/**
	 * 开启本地服务端，用于接收另外一端回复的信息
	 * @return
	 */
	private boolean open(boolean isDebug) {
		if (isCommunicate) {
			mHandler.sendMessage(mHandler.obtainMessage(WHAT_CONN_FAILURE, FLAG_COMMU_DATA_ING));
			return false;
		}
		if (!checkWifiIsEnabled()) {
			mHandler.sendMessage(mHandler.obtainMessage(WHAT_CONN_FAILURE, FLAG_CONNECT_NOT_ENABLE_WIFI));
			return false;
		}
		if (datagramSocket == null) {
			try {
				datagramSocket = new DatagramSocket(null);
				datagramSocket.setReuseAddress(true);
				datagramSocket.bind(new InetSocketAddress(localServerSocketPort));
				datagramSocket.setSoTimeout(5000);// 设置5s超时时间
			} catch (IOException e) {
				mHandler.sendMessage(mHandler.obtainMessage(WHAT_CONN_FAILURE, FLAG_CREATE_SERVER_ERROR));
				return false;
			}
		}
		if (isDebug) {
			new Thread(new DebugReceiveTask()).start();
		} else {
			new Thread(new ReceiveTask()).start();
		}
		return true;
	}

	/**
	 * 检查Wifi是否启用
	 * 
	 * @return
	 */
	private boolean checkWifiIsEnabled() {
		return null != wifiMan && wifiMan.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
	}

	/**
	 * 断开连接,释放资源,让GC回收堆栈
	 * 
	 */
	public void shutdown() {
		isCommunicate = false;
		releaseSeverSocket();
	}

	private void releaseSeverSocket() {
		if (datagramSocket != null && !datagramSocket.isClosed()) {
			datagramSocket.close();
			datagramSocket = null;
		}
	}

	/**
	 * 发送数据到设备终端
	 * 
	 * @param jsonParam
	 *            提交给设备终端的json类型的数据
	 * @param callbackListener
	 *            终端设备返回数据时候的回调接口
	 */
	public synchronized void post(byte[] buffer, SocketDataCallback callbackListener) {
		this.callbackListener = callbackListener;
		if (!open(false)) return;
		new Thread(new PostServerTask(buffer)).start();
	}
	
	/**
	 * 发送数据到设备终端
	 * @param buffer
	 * @param isDebug 是否在debug状态
	 * @param exitDebug 是否退出debug模式
	 * @param callbackListener
	 */
	public synchronized void post(byte[] buffer, boolean isDebug, boolean exitDebug, SocketDataCallback callbackListener) {
		this.callbackListener = callbackListener;
		if (!open(isDebug)) return;
		this.exitDebug = exitDebug;
		new Thread(new PostServerTask(buffer)).start();
	}

	/**
	 * 
	 * 与Android设备通信后回调接口
	 * 
	 * @author ZhaoFan
	 *
	 */
	public interface SocketDataCallback {

		public void onSuccess(byte[] result);

		/**
		 * 
		 * @param errorCode
		 *            [-101:服务器超时,-102:wifi未开启]
		 */
		public void onFailure(int errorCode);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case WAHT_CONN_SUCCESS:
				if (msg.obj != null && callbackListener != null) {
					callbackListener.onSuccess((byte[]) msg.obj);
				}
				break;
			case WHAT_CONN_FAILURE:
				if (callbackListener != null)
					callbackListener.onFailure(Integer.parseInt(msg.obj.toString()));
				break;
			}
//			shutdown();
		};
	};

	/**
	 * 接受来自设备终端响应的数据
	 */
	private class ReceiveTask implements Runnable {
		@Override
		public void run() {
			Message msg = mHandler.obtainMessage();
			byte[] buffer = new byte[1024];
			DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
			try {
				datagramSocket.receive(datagramPacket);
				msg.what = WAHT_CONN_SUCCESS;
				msg.obj = datagramPacket.getData();
			} catch (IOException e) {
				msg.what = WHAT_CONN_FAILURE;
				msg.obj = FLAG_CONNECT_TIMEOUT;
			} finally {
				msg.sendToTarget();
			}
		}
	}
	
	/**
	 * debug模式下接受来自设备终端响应的数据
	 */
	private class DebugReceiveTask implements Runnable {
		@Override
		public void run() {
			while (datagramSocket != null) {
				Message msg = mHandler.obtainMessage();
				byte[] buffer = new byte[1024];
				DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
				try {
					datagramSocket.receive(datagramPacket);
					msg.what = WAHT_CONN_SUCCESS;
					msg.obj = datagramPacket.getData();
				} catch (IOException e) {
					msg.what = WHAT_CONN_FAILURE;
					msg.obj = FLAG_CONNECT_TIMEOUT;
				} finally {
					msg.sendToTarget();
				}
				if (exitDebug) break;
			}
		}
	}

	/**
	 * 发送数据到设备终端
	 */
	private class PostServerTask implements Runnable {
		private byte[] bytes;

		public PostServerTask(byte[] bytes) {
			super();
			this.bytes = bytes;
		}

		@Override
		public void run() {
			ipAddress = int2Ip(wifiMan.getDhcpInfo().serverAddress);
			try {
				DatagramPacket dp = new DatagramPacket(bytes, bytes.length,
						InetAddress.getByName(ipAddress),
						socketPort);
				datagramSocket.send(dp);
			} catch (IOException e) {
				mHandler.obtainMessage(WHAT_CONN_FAILURE, FLAG_POST_DATA_ERROR).sendToTarget();
			}
		}
	}

	public void setIpAddress(String ipAddress) {
		if (ipAddress == null || "".equals(ipAddress)){
			throw new IllegalArgumentException("ipAddress not be null or empty string");
		}
		this.ipAddress = ipAddress;
	}
	
}
