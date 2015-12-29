package com.zhiitek.liftcontroller.components;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONObject;

import com.zhiitek.liftcontroller.utils.AppConstant;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

/**
 * <p>
 * 2015-02-26
 * </p>
 * 
 * <p>
 * 用于与Android设备交互的Socket通信组件
 * </p>
 * 
 * @author ZhaoFan
 *
 */
public class SocketConnection {

	/**
	 * 手机本地socket服务器端口
	 */
	private final int localServerSocketPort = 8686;

	/**
	 * 默认Android设备Socket服务器端口
	 */
	private final int socketPort = 8585;

	private final static int WHAT_CONN_SUCCESS = 100;

	private final static int WHAT_CONN_FAILURE = -100;

	public final static int FLAG_CONNECT_TIMEOUT = -101;

	public final static int FLAG_CONNECT_NOT_ENABLE_WIFI = -102;
	
	public final static int FLAG_CREATE_SERVER_ERROR = -103;
	
	public final static int FLAG_POST_DATA_ERROR = -104;
	
	public final static int FLAG_COMMU_DATA_ING = -105;

	private WifiManager wifiMan;

	private ServerSocket serverSocket;

	private SocketDataCallback callbackListener;

	private String address;
	
	private String fileDirectory;
	
	/**
	 * 
	 * @param context
	 *            使用ApplicationContext
	 */
	public SocketConnection(Context context) {
		wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		fileDirectory = "LiftController";
	}

	/**
	 * 将int类型的ip地址转换成字符类型的ip地址
	 * 
	 * @param i
	 * @return
	 */
	private String int2Ip(int i) {
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
	}

	/**
	 * 开启本地服务端，用于接收另外一端回复的信息
	 * @return
	 */
	private boolean open() {
		if (!checkWifiIsEnabled()) {
			mHandler.sendMessage(mHandler.obtainMessage(WHAT_CONN_FAILURE, FLAG_CONNECT_NOT_ENABLE_WIFI));
			return false;
		}
		address = int2Ip(wifiMan.getDhcpInfo().serverAddress);
		if (serverSocket == null) {
			try {
				serverSocket = new ServerSocket(localServerSocketPort);
				serverSocket.setSoTimeout(5000);// 设置5s超时时间
			} catch (IOException e) {
				mHandler.sendMessage(mHandler.obtainMessage(WHAT_CONN_FAILURE, FLAG_CREATE_SERVER_ERROR));
				return false;
			}
		}
		new Thread(new ReceiveTask()).start();
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
		releaseSeverSocket();
	}

	private void releaseSeverSocket() {
		if (serverSocket != null && !serverSocket.isClosed()) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				serverSocket = null;
			}
		}
	}

	private void releaseSocket(Socket socket) {
		if (socket != null && !socket.isClosed()) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
	public synchronized void post(JSONObject jsonParam, SocketDataCallback callbackListener) {
		this.callbackListener = callbackListener;
		if (jsonParam == null)
			return;
		if (!open())
			return;
		String paramStr = null;
		try {
			paramStr = new String(jsonParam.toString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (!TextUtils.isEmpty(paramStr)) {
			new Thread(new PostServerTask(paramStr)).start();
		}
	}

	/**
	 * 
	 * 与Android设备通信后回调接口
	 * 
	 * @author ZhaoFan
	 *
	 */
	public interface SocketDataCallback {

		public void onSuccess(String result);

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
			case WHAT_CONN_SUCCESS:
				if (msg.obj != null && callbackListener != null) {
					callbackListener.onSuccess(msg.obj.toString());
				}
				break;
			case WHAT_CONN_FAILURE:
				if (callbackListener != null)
				callbackListener.onFailure(Integer.parseInt(msg.obj.toString()));
				break;
			}
			shutdown();
		};
	};

	/**
	 * 接受来自设备终端响应的数据
	 */
	private class ReceiveTask implements Runnable {

		@Override
		public void run() {
			Socket acceptSocket = null;
			String result = null;
			Message msg = mHandler.obtainMessage();
			try {
				acceptSocket = serverSocket.accept();
				DataInputStream inputStream = new DataInputStream(new BufferedInputStream(acceptSocket.getInputStream()));
				String type = inputStream.readUTF();
				if (type.equals("file")) {
					try {
						boolean fileExists = inputStream.readBoolean();
						if (!fileExists) {
							result = AppConstant.FILE_NOT_EXISTS;
						} else {
							byte[] buffer = new byte[2 * 1024];
							int len = -1;
							FileOutputStream fos = null;
							String fileName = inputStream.readUTF();
							long fileLength = inputStream.readLong();
							File dir = new File(String.format("%s/%s", Environment.getExternalStorageDirectory().getAbsolutePath(), fileDirectory));
							if (!dir.exists()){
								dir.mkdirs();
							}
							File file = new File(dir, fileName);
							if (!file.exists()) {
								file.createNewFile();
							} else {
								file.delete();
								file.createNewFile();
							}
							fos = new FileOutputStream(file);
							while (fileLength > 0) {
								len = inputStream.read(buffer);
								fos.write(buffer, 0, len);
								if (len != -1) {
									fileLength -= len;
								}
							}
							result = fileName;
							fos.flush();
							fos.close();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					result = inputStream.readUTF();
				}
				msg.what = WHAT_CONN_SUCCESS;
				msg.obj = result;
			} catch (IOException e) {
				msg.what = WHAT_CONN_FAILURE;
				msg.obj = FLAG_CONNECT_TIMEOUT;
				e.printStackTrace();
			} finally {
				releaseSocket(acceptSocket);
				msg.sendToTarget();
			}
		}
	}

	/**
	 * 发送数据到设备终端
	 */
	private class PostServerTask implements Runnable {
		private String paramStr;

		public PostServerTask(String paramStr) {
			super();
			this.paramStr = paramStr;
		}

		@Override
		public void run() {
			Socket clientSocket = null;
			BufferedWriter bw = null;
			try {
				clientSocket = new Socket(address, socketPort);
				bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
				bw.write(paramStr);
				bw.flush();// 刷新输出流，使Server马上收到该字符串
			} catch (IOException e) {
				mHandler.obtainMessage(WHAT_CONN_FAILURE, FLAG_POST_DATA_ERROR).sendToTarget();
			} finally {
				releaseSocket(clientSocket);
				if (bw != null) {
					try {
						bw.close();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						bw = null;
					}
				}
			}
		}
	}

	public String getFileDirectory() {
		return fileDirectory;
	}

	public void setFileDirectory(String fileDirectory) {
		this.fileDirectory = fileDirectory;
	}
	
}
