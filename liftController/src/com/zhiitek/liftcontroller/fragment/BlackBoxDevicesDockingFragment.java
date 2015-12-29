package com.zhiitek.liftcontroller.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.activity.BlackBoxOperatingActivity;
import com.zhiitek.liftcontroller.components.UdpSocketConnection;
import com.zhiitek.liftcontroller.components.UdpSocketConnection.SocketDataCallback;
import com.zhiitek.liftcontroller.utils.BlackBoxUtil;
import com.zhiitek.liftcontroller.utils.WifiUtil;
import com.zhiitek.liftcontroller.utils.WifiUtil.ConnectWifiCallback;
import com.zhiitek.liftcontroller.views.CustomFormCellView;
import com.zhiitek.liftcontroller.views.ToggleButton;
import com.zhiitek.liftcontroller.views.ToggleButton.OnToggleChanged;

/**
 * 硬件调试-设备对接
 * 
 * @author ZhaoFan
 *
 */
public class BlackBoxDevicesDockingFragment extends BaseFragment {
	
	private UdpSocketConnection udpConn;
	
	private View view;
	
	private Button btnConnect;
	
	private EditText etCustomNumber, etPassword, etSerialPortUrl;
	/** 设备已绑定，开关设备中TaskLiftListen进程 */
	private ToggleButton tbDeviceAutoQuery;
	
	private CustomFormCellView llDeviceNotBind;
	private LinearLayout llDeviceBinded;
	
	/** 设备是否绑定 */
	private boolean isDeviceBinded = true;
	
	private WifiUtil wifiUtil;
	/** 扫描设备二维码得到的，wifiAP的ssid和密码 */
	private String ssid, pwd;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_blackbox_devices_docking, null);
		return view;
	}

	@Override
	protected void findViewById() {
		btnConnect = (Button) view.findViewById(R.id.btn_connect);
		etCustomNumber = (EditText) view.findViewById(R.id.et_docking_custom_number);
		etPassword = (EditText) view.findViewById(R.id.et_docking_password);
		llDeviceNotBind = (CustomFormCellView) view.findViewById(R.id.blackbox_docking_set_serialport_url);
		etSerialPortUrl = llDeviceNotBind.getInfoEditText();
		etSerialPortUrl.setText("com:///dev/ttyS3:115200");
		tbDeviceAutoQuery = (ToggleButton) view.findViewById(R.id.tb_devices_serialport_auto_query);
		tbDeviceAutoQuery.setToggleOn();
		llDeviceBinded = (LinearLayout) view.findViewById(R.id.blackbox_docking_set_serialport_auto_query);
	}

	@Override
	protected void setListener() {
		btnConnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openSerialPort();
			}
		});
		tbDeviceAutoQuery.setOnToggleChanged(new OnToggleChanged() {
			@Override
			public void onToggle(boolean on) {
				setDeviceAutoQuery(on);
			}
		});

		llDeviceBinded.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("NewApi")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (!isDeviceBinded && event.getAction() == MotionEvent.ACTION_DOWN) {
					touchBind();
				}
				return !isDeviceBinded;
			}
		});
		tbDeviceAutoQuery.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("NewApi")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (!isDeviceBinded && event.getAction() == MotionEvent.ACTION_DOWN) {
					touchBind();
				}
				return !isDeviceBinded;
			}
		});
		llDeviceNotBind.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("NewApi")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (isDeviceBinded && event.getAction() == MotionEvent.ACTION_DOWN) {
					touchNotBind();
				}
				return isDeviceBinded;
			}
		});
		etSerialPortUrl.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("NewApi")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (isDeviceBinded && event.getAction() == MotionEvent.ACTION_DOWN) {
					touchNotBind();
				}
				return isDeviceBinded;
			}
		});
	}
	
	/**
	 * 触摸设备已绑定操作区域
	 */
	@SuppressLint("NewApi")
	private void touchBind() {
		showToast("设备已绑定，请选择是否关闭TaskLiftListen");
		isDeviceBinded = true;
		llDeviceBinded.setBackgroundResource(R.drawable.bg_choosed_item);
		llDeviceNotBind.setBackground(null);
		etSerialPortUrl.clearFocus();
	}
	
	/**
	 * 触摸设备未绑定操作区域
	 */
	@SuppressLint("NewApi")
	private void touchNotBind() {
		showToast("设备未绑定，请输入串口地址");
		isDeviceBinded = false;
		llDeviceBinded.setBackground(null);
		llDeviceNotBind.setBackgroundResource(R.drawable.bg_choosed_item);
	}
	
	/**
	 * 设置界面上控件可用性
	 * @param enabled
	 */
	private void setBarEnabled(boolean enabled) {
		btnConnect.setEnabled(enabled);
		etCustomNumber.setEnabled(enabled);
		etPassword.setEnabled(enabled);
		etSerialPortUrl.setEnabled(enabled);
		tbDeviceAutoQuery.setEnabled(enabled);
	}
	
	/**
	 * 开关设备中TaskLiftListen进程
	 * @param on true：打开TaskLiftListen进程 ； false： 关闭TaskLiftListen进程 
	 */
	protected void setDeviceAutoQuery(final boolean on) {
		udpConn.post(new byte[] {(byte) 0xff, (byte) 0, (byte) (on ? 1:0)}, new SocketDataCallback() {
			@Override
			public void onSuccess(byte[] result) {
				if (on) {
					showToast("设备正常监听电梯状态");
				} else {
					showToast("设备调试结束后请打开该开关");
				}
			}
			
			@Override 
			public void onFailure(int errorCode) {
				if (on) {
					promptBlackBoxSocketonFailure("开启TaskLiftListen进程，", errorCode);
					tbDeviceAutoQuery.setToggleOff();
				} else {
					promptBlackBoxSocketonFailure("关闭TaskLiftListen进程，", errorCode);
					tbDeviceAutoQuery.setToggleOn();
				}
			}
		});
	}

	/**
	 * 打开设备的串口通信
	 */
	private void openSerialPort() {
		String serialPortUrl = etSerialPortUrl.getText().toString().trim();
		if (!isDeviceBinded) {//设备未绑定时,先将填写的串口地址发送给设备,请求打开串口通信
			if (serialPortUrl.length() == 0) {
				showToast("串口地址不能为空");
				return;
			} else {
				showLoadingDlg();
				udpConn.post(BlackBoxUtil.byteMerger(new byte[] {(byte) 0xee, 0}, serialPortUrl.getBytes()), new SocketDataCallback() {
					@Override
					public void onSuccess(byte[] result) {
						//打开串口成功后,才可以连接设备
						connectDevices();
					}
					@Override
					public void onFailure(int errorCode) {
						showToast("打开设备串口失败，请重试");
						dismissDialog(mProgressDialog);
					}
				});
			}
		} else {//设备已绑定时,直接连接设备
			showLoadingDlg();
			connectDevices();
		}
	}

	/**
	 * 连接设备
	 */
	private void connectDevices() {
		String custom = etCustomNumber.getText().toString().trim();
		String pwd = etPassword.getText().toString().trim();
		if (checkPswFormat(custom) && checkPswFormat(pwd)) {//检查口令和定制号的格式
			int customNo = Integer.valueOf(custom);
			int password = Integer.valueOf(pwd);
			udpConn.post(BlackBoxUtil.makeSendCommand(0x00, BlackBoxUtil.byteMerger(BlackBoxUtil.int2byteArray(password, 4), BlackBoxUtil.int2byteArray(customNo, 4)), 8), new SocketDataCallback() {
				@Override
				public void onSuccess(byte[] result) {
					dismissDialog(mProgressDialog);
					if (checkCmd(result, 0x00)) {
						if (BlackBoxUtil.SSE_SUCCESS == BlackBoxUtil.getResponseCode(result)) {//连接成功
							udpConn.shutdown();
							startActivity(new Intent(getActivity(), BlackBoxOperatingActivity.class));
						} else {
							showToast("连接失败，请检查定制号或者口令");
						}
					}
				}
				
				@Override
				public void onFailure(int errorCode) {
					dismissDialog(mProgressDialog);
					promptBlackBoxSocketonFailure("对接设备", errorCode);
				}
			});
		} else {
			dismissDialog(mProgressDialog);
			showToast("请检查定制号或者口令格式");
		}
	}

	@Override
	protected void dealProcessLogic() {
		udpConn = new UdpSocketConnection(getActivity());
		wifiUtil = new WifiUtil(getActivity());
		wifiUtil.registerWifiConnChangeReceiver();
		wifiUtil.setConnectWifiCallback(new ConnectWifiCallback() {
			@Override
			public void errorAuthenticating() {
				showToast("密码错误,未能连接热点");
			}
			
			@Override
			public void disConnectWifi(String connectedSSID) {
			}
			
			@Override
			public void connectedWifi(String connectedSSID) {
				if (ssid !=null && (("\"" + ssid + "\"").equals(connectedSSID) || ssid.equals(connectedSSID))) {
					showToast("连接设备Wifi成功");
				} else {
					showToast("连接设备Wifi失败，请重新扫描连接");
				}
			}
			
			@Override
			public void apNotAvailable() {
				showToast("设备热点不可用！");
			}
		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 0) {
			//处理扫描二维码返回的数据
			if (data != null) {
				String result = data.getStringExtra("result");
				if (result.length() == 49) {
					if (result.substring(0, 7).equals("zhiitek")) {
						ssid = result.substring(8, 40);
						pwd = result.substring(41);
						wifiUtil.connectWifiAp(ssid, pwd);
					} else {
						showToast("请确认扫描的是AP二维码");
					}
				} else {
					showToast("请确认扫描的是AP二维码");
				}
			}
		}
	}
	
	public static BlackBoxDevicesDockingFragment newInstance() {
		return new BlackBoxDevicesDockingFragment();
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		udpConn.shutdown();
		wifiUtil.unregisterWifiConnChangeListener();
	}
}
