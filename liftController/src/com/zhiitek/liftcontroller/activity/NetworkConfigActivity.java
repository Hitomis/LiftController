package com.zhiitek.liftcontroller.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.components.SocketConnection;
import com.zhiitek.liftcontroller.components.SocketConnection.SocketDataCallback;
import com.zhiitek.liftcontroller.utils.AppConstant;
import com.zhiitek.liftcontroller.views.CustomProgressDialog;
import com.zhiitek.liftcontroller.views.ToggleButton;
import com.zhiitek.liftcontroller.views.ToggleButton.OnToggleChanged;

public class NetworkConfigActivity extends BaseActivity{

	private ToggleButton tbNetworkEnable, tbDHCP;
	private EditText etIP, etSubnetMask, etDefaultGateway, etDNSServer;
	private Button btnSaveConfig;
	private LinearLayout llNetInfo;
	private LinearLayout llDHCP;
	
	private SocketConnection conn;
	
	private CustomProgressDialog mProgressDialog;
	
	private static final int FLAG_CONFIGURE_NET_COMPLETE = 100;
	private static final int FLAG_CONFIGURE_NET_FAILURE = 101;
	
	private static final int FLAG_QUERY_NET_CONFIG_COMPLETE = 102;
	private static final int FLAG_QUERY_NET_CONFIG_FAILURE = 103;
	
	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_network_config);
	}

	@Override
	protected void findViewById() {
		tbNetworkEnable = (ToggleButton) findViewById(R.id.tb_network_enable);
		tbDHCP = (ToggleButton) findViewById(R.id.tb_dhcp);
		etIP = (EditText) findViewById(R.id.et_ip);
		etSubnetMask = (EditText) findViewById(R.id.et_subnet_mask);
		etDefaultGateway = (EditText) findViewById(R.id.et_default_gateway);
		etDNSServer = (EditText) findViewById(R.id.et_dns_server);
		btnSaveConfig = (Button) findViewById(R.id.btn_save_network);
		llNetInfo = (LinearLayout) findViewById(R.id.ll_network_info);
		llDHCP = (LinearLayout) findViewById(R.id.ll_dhcp);
	}

	@Override
	protected void setListener() {
		tbNetworkEnable.setOnToggleChanged(new OnToggleChanged() {
			@Override
			public void onToggle(boolean on) {
				tbDHCP.setEnabled(on);
				if (on) {
					llDHCP.setAlpha(1.0f);
				} else {
					llDHCP.setAlpha(0.5f);
				}
			}
		});
		tbDHCP.setOnToggleChanged(new OnToggleChanged() {
			@Override
			public void onToggle(boolean on) {
				if (on) {
					llNetInfo.setAlpha(0.5f);
					setEditTextEnable(false);
				} else {
					llNetInfo.setAlpha(1);
					setEditTextEnable(true);
				}
			}
		});
		btnSaveConfig.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				saveConfig();
			}
		});
	}

	@Override
	protected void dealProcessLogic() {
		setTitleBar("网络管理", null);
		conn = new SocketConnection(this.getApplicationContext());
		queryConfig();
	}
	
	private void saveConfig() {
		mProgressDialog = new CustomProgressDialog(this, R.style.loading_dialog);
		mProgressDialog.show();
		conn.post(createSaveConfigJson(), new SocketDataCallback() {
			@Override
			public void onSuccess(String result) {
				resolveSuccessResult(result);
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
	
	/**
	 * 保存信息的Json串
	 * @return
	 */
	private JSONObject createSaveConfigJson() {
		JSONObject jsonParam = new JSONObject();
		JSONObject jsonData = new JSONObject();
		try {
			jsonParam.put(AppConstant.KEY_SOCKET_CMD, 9);
			jsonData.put(AppConstant.KEY_SOCKET_CHECKCODE, sharedPreferences.getString(AppConstant.DEV_SERIALS, "zhiitek"));
			jsonData.put(AppConstant.KEY_USER_ID, getUserId());
			if(tbNetworkEnable.getToggleState()) {
				jsonData.put("network_enable", "true");
				if (tbDHCP.getToggleState()) {
					jsonData.put("dhcp", "true");
				} else {
					jsonData.put("dhcp", "false");
					jsonData.put("ip", etIP.getText().toString().trim());
					jsonData.put("subnet_mask", etSubnetMask.getText().toString().trim());
					jsonData.put("gateway", etDefaultGateway.getText().toString().trim());
					jsonData.put("dns", etDNSServer.getText().toString().trim());
				}
			} else {
				jsonData.put("network_enable", "false");
			}
			jsonParam.put(AppConstant.KEY_SOCKET_DATA, jsonData);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonParam;
	}
	
	/**
	 * 解析保存信息的结果
	 * @param result
	 */
	protected void resolveSuccessResult(String result) {
		Message msg = mHandler.obtainMessage();
		if (!TextUtils.isEmpty(result)) { 
			JSONObject resultJson = null;
			try {
				resultJson = new JSONObject(result);
				if(resultJson != null && !resultJson.isNull(AppConstant.KEY_SOCKET_CMD)){
					if(checkCmd(resultJson.getInt(AppConstant.KEY_SOCKET_CMD))){
						msg.what = FLAG_CONFIGURE_NET_COMPLETE;
					}else {
						msg.what = FLAG_CONFIGURE_NET_FAILURE;
					}
				}
			} catch (JSONException e) {
				msg.what = FLAG_CONFIGURE_NET_FAILURE;
			}
		} else {
			msg.what = FLAG_CONFIGURE_NET_FAILURE;
		}
		msg.sendToTarget();
	}

	private void queryConfig() {
		mProgressDialog = new CustomProgressDialog(this, R.style.loading_dialog);
		mProgressDialog.show();
		conn.post(createQueryConfigJson(), new SocketDataCallback() {
			@Override
			public void onSuccess(String result) {
				resolveQueryConfigResult(result);
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
	
	/**
	 * 查询信息的Json串
	 * @return
	 */
	private JSONObject createQueryConfigJson() {
		JSONObject jsonParam = new JSONObject();
		JSONObject jsonData = new JSONObject();
		try {
			jsonParam.put(AppConstant.KEY_SOCKET_CMD, 8);
			jsonData.put(AppConstant.KEY_SOCKET_CHECKCODE, sharedPreferences.getString(AppConstant.DEV_SERIALS, "zhiitek"));
			jsonData.put(AppConstant.KEY_USER_ID, getUserId());
			jsonParam.put(AppConstant.KEY_SOCKET_DATA, jsonData);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonParam;
	}
	
	/**
	 * 解析查询信息的结果
	 * @param result
	 */
	protected void resolveQueryConfigResult(String result) {
		Message msg = mHandler.obtainMessage();
		if (!TextUtils.isEmpty(result)) {
			JSONObject resultJson = null;
			try {
				resultJson = new JSONObject(result);
				if(resultJson != null && !resultJson.isNull(AppConstant.KEY_SOCKET_CMD)){
					if(checkCmd(resultJson.getInt(AppConstant.KEY_SOCKET_CMD))){
						msg.what = FLAG_QUERY_NET_CONFIG_COMPLETE;//设备查询成功
						msg.obj = resultJson.getJSONObject(AppConstant.KEY_SOCKET_DATA);
					}else {
						msg.what = FLAG_QUERY_NET_CONFIG_FAILURE;
					}
				}
			} catch (JSONException e) {
				msg.what = FLAG_QUERY_NET_CONFIG_FAILURE;
			}
		} else {
			msg.what = FLAG_QUERY_NET_CONFIG_FAILURE;
		}
		msg.sendToTarget();
	}
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case FLAG_QUERY_NET_CONFIG_COMPLETE:
				JSONObject dataJson = (JSONObject) msg.obj;
				try {
					setConfig2View(dataJson);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				dismissDialog(mProgressDialog);
				break;
			case FLAG_QUERY_NET_CONFIG_FAILURE:
				showToast("查询以太网信息失败");
				dismissDialog(mProgressDialog);
				break;
			case FLAG_CONFIGURE_NET_COMPLETE:
				showToast("配置以太网信息成功");
				dismissDialog(mProgressDialog);
				break;
			case FLAG_CONFIGURE_NET_FAILURE:
				showToast("配置以太网信息失败");
				dismissDialog(mProgressDialog);
				break;
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

	/**
	 * 显示查询到的网络信息
	 * @param dataJson
	 * @throws JSONException
	 */
	private void setConfig2View(JSONObject dataJson) throws JSONException {
		if (dataJson.has("network_enable")) {
			if (dataJson.getString("network_enable").equals("true")) {
				tbNetworkEnable.setToggleOn();
			} else {
				tbNetworkEnable.setToggleOff();
				tbDHCP.setEnabled(false);
				setEditTextEnable(false);
				llNetInfo.setAlpha(0.5f);
			}
		}
		if (dataJson.has("dhcp")) {
			if (dataJson.getString("dhcp").equals("true")) {
				tbDHCP.setToggleOn();
				setEditTextEnable(false);
				llNetInfo.setAlpha(0.5f);
			} else {
				tbDHCP.setToggleOff();
				setEditTextEnable(true);
				llNetInfo.setAlpha(1);
			}
		}
		if (dataJson.has("ip")) {
			etIP.setText(dataJson.getString("ip"));
		}
		if (dataJson.has("subnet_mask")) {
			etSubnetMask.setText(dataJson.getString("subnet_mask"));
		}
		if (dataJson.has("gateway")) {
			etDefaultGateway.setText(dataJson.getString("gateway"));
		}
		if (dataJson.has("dns")) {
			etDNSServer.setText(dataJson.getString("dns"));
		}
	}
	
	private void setEditTextEnable(boolean enabled) {
		etIP.setEnabled(enabled);
		etSubnetMask.setEnabled(enabled);
		etDefaultGateway.setEnabled(enabled);
		etDNSServer.setEnabled(enabled);
	}
	
}
