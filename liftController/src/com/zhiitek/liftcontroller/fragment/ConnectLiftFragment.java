package com.zhiitek.liftcontroller.fragment;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.zbar.lib.CaptureActivity;
import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.activity.BlackBoxMainActivity;
import com.zhiitek.liftcontroller.activity.ConfigLiftActivity;
import com.zhiitek.liftcontroller.activity.DeviceTestActivity;
import com.zhiitek.liftcontroller.activity.LiftSettingActivity;
import com.zhiitek.liftcontroller.db.LiftControllerSqlHelper;
import com.zhiitek.liftcontroller.service.MonitorConnectTimeService;
import com.zhiitek.liftcontroller.service.MonitorConnectTimeService.OnTimeOutListener;
import com.zhiitek.liftcontroller.utils.AppUtil;
import com.zhiitek.liftcontroller.utils.LiftActivityManager;
import com.zhiitek.liftcontroller.utils.WifiUtil;
import com.zhiitek.liftcontroller.utils.WifiUtil.ConnectWifiCallback;

public class ConnectLiftFragment extends BaseFragment {

	private ImageView mScanningImg, mVerificationImg;
	
	public static final int TYPE_NO_PASSWD = 0x11;
	public static final int TYPE_WEP = 0x12;
	public static final int TYPE_WPA = 0x13;
	
	private LinearLayout llContrent, llConfig, llSetting, llTest;
	
	private WifiUtil wifiUtil;
	
	private String ssid = "";
	private String password = "";
	
	private WifiManager wifiMan;
	
	private MonitorConnectTimeService timeService;
	
	/** service检测到连接超时的时候是否需要进行提示 */
	private boolean isNeedToPromptTimeOut = false;
	/** 是否成功绑定上MonitorConnectTimeService */
	private boolean hasAlreadyBindServiceSuccess = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_lift_running_config, container, false);
		return mView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
 		if (!AppUtil.isConnectValid(mContext)) {
			setBarEnable(false);
			isNeedToPromptTimeOut = false;
		} else {
			//重新进入ConnectLiftFragment,若连接有效,超时时需要提示
			isNeedToPromptTimeOut = true;
		}
	}
	
	@Override
	protected void findViewById() {
		mScanningImg = (ImageView) mView.findViewById(R.id.img_scanning);
		wifiMan = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		llContrent = (LinearLayout) mView.findViewById(R.id.ll_conntent);
		llConfig = (LinearLayout) mView.findViewById(R.id.ll_config);
		llSetting = (LinearLayout) mView.findViewById(R.id.ll_setting);
		llTest = (LinearLayout) mView.findViewById(R.id.ll_test);
		mVerificationImg = (ImageView) mView.findViewById(R.id.img_verification);
	}

	@Override
	protected void setListener() {
		mScanningImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!AppUtil.isEnabledWifi(getActivity())) {
					showToast("请设置wifi为启动状态后再进行扫描操作");
				} else {
					Intent intent = new Intent(mContext, CaptureActivity.class);
					startActivityForResult(intent, 0);
				}
			}
		});
		llConfig.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(mContext, ConfigLiftActivity.class), 1);
			}
		});
		llSetting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(mContext, LiftSettingActivity.class), 2);
			}
		});
		mVerificationImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				verify();
			}
		});
		llTest.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startDeviceTestActivity();
			}
		});
		
		mView.findViewById(R.id.ll_temp).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), BlackBoxMainActivity.class));
			}
		});
	}
	
	/**
	 * 跳转到设备测试界面
	 */
	private void startDeviceTestActivity() {
		Intent intent = new Intent(getActivity(), DeviceTestActivity.class);
		startActivity(intent);
	}
	
	/**
	 * 验证
	 */
	private void verify() {
		if (AppUtil.isConnectValid(mContext)) {//连接依然有效
			showToast("连接仍然有效，无需重新连接");
		} else {
			ssid = sharedPreferences.getString("ssid", "");
			if (ssid != null) {
				if ((("\"" + ssid + "\"").equals(wifiMan.getConnectionInfo().getSSID()) || ssid.equals(wifiMan.getConnectionInfo().getSSID()))) {
					setBarEnable(true);
					showToast("重连成功");
					//重连成功,重置连接时间,超时时需要提示
					saveValue2SharedPreference("connecttime", AppUtil.getCurrentTimeStr());
					isNeedToPromptTimeOut = true;
				} else {
					showToast("请先扫描二维码连接设备");
					setBarEnable(false);
				}
			}
		}
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
						password = result.substring(41);
						wifiUtil.connectWifiAp(ssid, password);
					} else {
						showToast("请确认扫描的是AP二维码");
					}
				} else {
					showToast("请确认扫描的是AP二维码");
				}
			}
		} else if (resultCode == 1 || requestCode == 2) {
			//配置设备成功后自动重启或手动重启设备,无需进行超时提示
			isNeedToPromptTimeOut = false;
		}
	}
	
	private void saveValue2SharedPreference(String key, String value) {
		Editor editor = sharedPreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	/**
	 * 保存ssid到数据库
	 * @param ssid
	 */
	private void saveSsid2Database(String ssid) {
		LiftControllerSqlHelper database = new LiftControllerSqlHelper(mContext);
		SQLiteDatabase db = database.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("ssid", ssid);
		db.insert("apListTable",null,cv);
		db.close();
	}

	@Override
	protected void dealProcessLogic() {
		wifiUtil = new WifiUtil(getActivity());
		wifiUtil.registerWifiConnChangeReceiver();
		wifiUtil.setConnectWifiCallback(new ConnectWifiCallback() {
			@Override
			public void errorAuthenticating() {
				showToast("密码错误,未能连接热点");
			}

			@Override
			public void disConnectWifi(String connectedSSID) {
				String lastConnectedSSID = sharedPreferences.getString("ssid", "");
				if (AppUtil.isConnectValid(mContext) && !(("\"" + lastConnectedSSID + "\"").equals(connectedSSID) || lastConnectedSSID.equals(connectedSSID))) {
					isNeedToPromptTimeOut = false;
					sharedPreferences.edit().remove("connecttime").commit();
					setBarEnable(false);
				}
			}
			
			@Override
			public void connectedWifi(String connectedSSID) {
				if (ssid !=null && (("\"" + ssid + "\"").equals(connectedSSID) || ssid.equals(connectedSSID))) {
					saveValue2SharedPreference("connecttime", AppUtil.getCurrentTimeStr());
					saveSsid2Database("\"" + ssid + "\"");
					saveValue2SharedPreference("ssid", ssid);
					showToast(String.format("连接有效期为%d分钟，失效后请重新验证", AppUtil.TIME_OUT_MINUTES));
					setBarEnable(true);
					//连接成功,超时时需要提示
					isNeedToPromptTimeOut = true;
				} else {
					showToast("连接设备Wifi失败，请重新扫描连接");
				}
			}
			
			@Override
			public void apNotAvailable() {
				showToast("设备热点不可用！");
			}
		});
		if (!hasAlreadyBindServiceSuccess) {
			hasAlreadyBindServiceSuccess = mContext.bindService(new Intent(mContext, MonitorConnectTimeService.class), conn, Context.BIND_AUTO_CREATE);
		}
	}
	
	public void unbindTheService() {
		mContext.unbindService(conn);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		wifiUtil.unregisterWifiConnChangeListener();
	}
	
	OnTimeOutListener onTimeOutListener = new OnTimeOutListener() {
		@Override
		public void onTimeOut() {
			//检测到连接超时,只有需要进行提示的情况才进行相关操作,否则跳过
			if (isNeedToPromptTimeOut) {
				handler.sendEmptyMessage(0);
				LiftActivityManager.getInstance().finishAllExcept(getActivity());
				isNeedToPromptTimeOut = false;
			}
		}
	};

	ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			timeService = ((MonitorConnectTimeService.MonitorConnectTimeBinder) service).getService();
			timeService.setOnTimeOutListener(onTimeOutListener);
		}
	};
   
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) {
				AppUtil.showTimeOutDialog(mContext, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				setBarEnable(false);
			}
		}
	};
	
	/**
	 * 设置按钮的可用性
	 * @param isEnable
	 */
	private void setBarEnable(boolean isEnable) {
		llConfig.setEnabled(isEnable);
		llSetting.setEnabled(isEnable);
		llTest.setEnabled(isEnable);
		if (isEnable) {
			llContrent.setAlpha(1.0f);
		} else {
			llContrent.setAlpha(0.5f);
		}
	}

}
