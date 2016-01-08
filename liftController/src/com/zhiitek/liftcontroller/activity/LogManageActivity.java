package com.zhiitek.liftcontroller.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.components.SocketConnection;
import com.zhiitek.liftcontroller.components.SocketConnection.SocketDataCallback;
import com.zhiitek.liftcontroller.utils.AppConstant;
import com.zhiitek.liftcontroller.views.CustomProgressDialog;
import com.zhiitek.liftcontroller.views.SwipeFinishLayout;

public class LogManageActivity extends BaseActivity implements OnClickListener{
	
	private LinearLayout llDisplayLog, llUpgradeLog, llDeviceLog;
	
	private static final int TYPE_DISPLAY_LOG_FILE = 0;
	private static final int TYPE_UPGRADE_LOG_FILE = 1;
	private static final int TYPE_DEVICE_LOG_FILE = 2;
	
	private SocketConnection conn;
	private CustomProgressDialog mProgressDialog;
	
	//日志文件存放的文件夹
	private static final String FILE_DIR = "LiftController/LiftLog";

	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_log_manage);
	}

	@Override
	protected void findViewById() {
		llDisplayLog = (LinearLayout) findViewById(R.id.ll_display_log);
		llUpgradeLog = (LinearLayout) findViewById(R.id.ll_upgrade_log);
		llDeviceLog = (LinearLayout) findViewById(R.id.ll_device_log);
	}

	@Override
	protected void setListener() {
		llDeviceLog.setOnClickListener(this);
		llDisplayLog.setOnClickListener(this);
		llUpgradeLog.setOnClickListener(this);
		llDeviceLog.setVisibility(View.GONE);
	}

	@Override
	protected void dealProcessLogic() {
		setTitleBar("日志管理", null);
		SwipeFinishLayout.attachToActivity(this);
		conn = new SocketConnection(this.getApplicationContext());
		conn.setFileDirectory(FILE_DIR);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_display_log:
			getLogFile(TYPE_DISPLAY_LOG_FILE);
			break;
		case R.id.ll_upgrade_log:
			getLogFile(TYPE_UPGRADE_LOG_FILE);
			break;
		case R.id.ll_device_log:
			getLogFile(TYPE_DEVICE_LOG_FILE);
			break;
		}
	}
	
	/**
	 * 获取日志文件
	 * @param type
	 */
	private void getLogFile(int type) {
		mProgressDialog = new CustomProgressDialog(this, R.style.loading_dialog);
		mProgressDialog.show();
		conn.post(createGetLogFileJson(type), new SocketDataCallback() {
			@Override
			public void onSuccess(String result) {
				if (result.equals(AppConstant.FILE_NOT_EXISTS)) {
					showToast("日志文件不存在，请检查设备！");
				} else {
					showToast(result + "已经保存到SD卡的" + FILE_DIR + "目录下");
				}
				dismissDialog(mProgressDialog);
			}
			@Override
			public void onFailure(int errorCode) {
				Message msg = mHandler.obtainMessage();
				if (errorCode == SocketConnection.FLAG_CONNECT_NOT_ENABLE_WIFI) {
					msg.what = SocketConnection.FLAG_CONNECT_NOT_ENABLE_WIFI;
				} else if (errorCode == SocketConnection.FLAG_CONNECT_TIMEOUT) {
					msg.what = SocketConnection.FLAG_CONNECT_TIMEOUT;
				}
				msg.sendToTarget();
			}
		});
	}
	
	private JSONObject createGetLogFileJson(int type) {
		JSONObject jsonParam = new JSONObject();
		JSONObject jsonData = new JSONObject();
		try {
			jsonParam.put(AppConstant.KEY_SOCKET_CMD, 13);
			jsonData.put(AppConstant.KEY_SOCKET_CHECKCODE, sharedPreferences.getString(AppConstant.DEV_SERIALS, "zhiitek"));
			jsonData.put("type", String.valueOf(type));
			jsonParam.put(AppConstant.KEY_SOCKET_DATA, jsonData);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonParam;
	}
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SocketConnection.FLAG_CONNECT_TIMEOUT:
				showToast("服务器响应超时!");
				dismissDialog(mProgressDialog);
				break;
			case SocketConnection.FLAG_CONNECT_NOT_ENABLE_WIFI:
				showToast("请设置wifi为启动状态!");
				dismissDialog(mProgressDialog);
				break;
			}
		};
	};

}
