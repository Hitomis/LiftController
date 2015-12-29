package com.zhiitek.liftcontroller.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.components.SocketConnection;
import com.zhiitek.liftcontroller.components.SocketConnection.SocketDataCallback;
import com.zhiitek.liftcontroller.utils.AppConstant;
import com.zhiitek.liftcontroller.utils.DensityUtil;
import com.zhiitek.liftcontroller.utils.DialogUtil;
import com.zhiitek.liftcontroller.views.CustomProgressDialog;
import com.zhiitek.liftcontroller.views.ToggleButton;
import com.zhiitek.liftcontroller.views.ToggleButton.OnToggleChanged;
import com.zhiitek.liftcontroller.views.VoiceSeekBar;
import com.zhiitek.liftcontroller.views.VoiceSeekBar.OnSeekBarChangeListener;

public class LiftSettingActivity extends BaseActivity implements OnClickListener{

	private LinearLayout llNetwork, llSound, llLogManage, llReboot;
	private ToggleButton tbBluttooth;
	private CustomProgressDialog mProgressDialog;
	private SocketConnection conn;
	
	//设备最大音量
	private int MAX_VOLUME;
	//设备当前音量
	private int current_volume;
	
	private static final int FLAG_QUERY_CONFIG_COMPLETE = 100;
	private static final int FLAG_QUERY_CONFIG_FAILURE = 101;
	
	private static final int FLAG_CONFIGURE_BT_COMPLETE = 102;
	private static final int FLAG_CONFIGURE_BT_FAILURE = 103;
	
	private static final int FLAG_CONFIGURE_VOLUME_COMPLETE = 104;
	private static final int FLAG_CONFIGURE_VOLUME_FAILURE = 105;
	
	private static final int TYPE_SAVE_BT_CONIFG = 1;
	private static final int TYPE_SAVE_VOLUME_CONIFG = 2;
	
	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_lift_setting);
	}

	@Override
	protected void findViewById() {
		llNetwork = (LinearLayout) findViewById(R.id.ll_network);
		llSound = (LinearLayout) findViewById(R.id.ll_sound);
		tbBluttooth = (ToggleButton) findViewById(R.id.tb_bluetooth);
		llLogManage = (LinearLayout) findViewById(R.id.ll_log_manage);
		llReboot = (LinearLayout) findViewById(R.id.ll_reboot);
	}

	@Override
	protected void setListener() {
		llNetwork.setOnClickListener(this);
		llNetwork.setVisibility(View.GONE);
		llSound.setOnClickListener(this);
		tbBluttooth.setOnToggleChanged(new OnToggleChanged() {
			@Override
			public void onToggle(boolean on) {
				saveConfig(TYPE_SAVE_BT_CONIFG);
			}
		});
		llLogManage.setOnClickListener(this);
		llReboot.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_network:
			startActivity(new Intent(LiftSettingActivity.this, NetworkConfigActivity.class));
			break;
		case R.id.ll_sound:
			showVolumePopup();
			break;
		case R.id.ll_log_manage:
			startActivity(new Intent(LiftSettingActivity.this, LogManageActivity.class));
			break;
		case R.id.ll_reboot:
			showRebootDlg();
			break;
		}
	}
	
	@Override
	protected void dealProcessLogic() {
		setTitleBar("设备设置", null);
		conn = new SocketConnection(this.getApplicationContext());
		queryConfig();
	}
	
	/**
	 * 查询设备设置信息(音量,蓝牙状态)
	 */
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
			jsonParam.put(AppConstant.KEY_SOCKET_CMD, 10);
			jsonData.put(AppConstant.KEY_SOCKET_CHECKCODE, sharedPreferences.getString(AppConstant.DEV_SERIALS, "zhiitek"));
			jsonData.put(AppConstant.KEY_USER_ID, getUserId());
			jsonParam.put(AppConstant.KEY_SOCKET_DATA, jsonData);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonParam;
	}
	
	/**
	 * 解析查询结果
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
						msg.what = FLAG_QUERY_CONFIG_COMPLETE;
						msg.obj = resultJson.getJSONObject(AppConstant.KEY_SOCKET_DATA);
					}else {
						msg.what = FLAG_QUERY_CONFIG_FAILURE;
					}
				}
			} catch (JSONException e) {
				msg.what = FLAG_QUERY_CONFIG_FAILURE;
			}
		} else {
			msg.what = FLAG_QUERY_CONFIG_FAILURE;
		}
		msg.sendToTarget();
	}
	
	/**
	 * 保存信息(音量,蓝牙)
	 * @param type
	 */
	private void saveConfig(final int type) {
		mProgressDialog = new CustomProgressDialog(this, R.style.loading_dialog);
		mProgressDialog.show();
		conn.post(createSaveConfigJson(type), new SocketDataCallback() {
			@Override
			public void onSuccess(String result) {
				resolveSaveResult(result, type);
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
	 * @param type
	 * @return
	 */
	private JSONObject createSaveConfigJson(int type) {
		JSONObject jsonParam = new JSONObject();
		JSONObject jsonData = new JSONObject();
		try {
			if (type == TYPE_SAVE_BT_CONIFG) {
				jsonParam.put(AppConstant.KEY_SOCKET_CMD, 11);
				jsonData.put(AppConstant.KEY_SOCKET_CHECKCODE, sharedPreferences.getString(AppConstant.DEV_SERIALS, "zhiitek"));
				jsonData.put("bluetooth_enable", tbBluttooth.getToggleState() ? "true":"false");
			} else if (type == TYPE_SAVE_VOLUME_CONIFG) {
				jsonParam.put(AppConstant.KEY_SOCKET_CMD, 12);
				jsonData.put(AppConstant.KEY_SOCKET_CHECKCODE, sharedPreferences.getString(AppConstant.DEV_SERIALS, "zhiitek"));
				jsonData.put("current_volume", String.valueOf(current_volume));
			}
			jsonParam.put(AppConstant.KEY_SOCKET_DATA, jsonData);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonParam;
	}
	
	/**
	 * 解析保存返回的结果
	 * @param result
	 * @param type
	 */
	protected void resolveSaveResult(String result, int type) {
		Message msg = mHandler.obtainMessage();
		if (!TextUtils.isEmpty(result)) { 
			JSONObject resultJson = null;
			try {
				resultJson = new JSONObject(result);
				if(resultJson != null && !resultJson.isNull(AppConstant.KEY_SOCKET_CMD)){
					if(checkCmd(resultJson.getInt(AppConstant.KEY_SOCKET_CMD))){
						if (type == TYPE_SAVE_BT_CONIFG) {
							msg.what = FLAG_CONFIGURE_BT_COMPLETE;
						} else if (type == TYPE_SAVE_VOLUME_CONIFG) {
							msg.what = FLAG_CONFIGURE_VOLUME_COMPLETE;
						}
					}else {
						if (type == TYPE_SAVE_BT_CONIFG) {
							msg.what = FLAG_CONFIGURE_BT_FAILURE;
						} else if (type == TYPE_SAVE_VOLUME_CONIFG) {
							msg.what = FLAG_CONFIGURE_VOLUME_FAILURE;
						}
					}
				}
			} catch (JSONException e) {
				if (type == TYPE_SAVE_BT_CONIFG) {
					msg.what = FLAG_CONFIGURE_BT_FAILURE;
				} else if (type == TYPE_SAVE_VOLUME_CONIFG) {
					msg.what = FLAG_CONFIGURE_VOLUME_FAILURE;
				}
			}
		} else {
			if (type == TYPE_SAVE_BT_CONIFG) {
				msg.what = FLAG_CONFIGURE_BT_FAILURE;
			} else if (type == TYPE_SAVE_VOLUME_CONIFG) {
				msg.what = FLAG_CONFIGURE_VOLUME_FAILURE;
			}
		}
		msg.sendToTarget();
	}
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case FLAG_QUERY_CONFIG_COMPLETE:
				JSONObject dataJson = (JSONObject) msg.obj;
				try {
					MAX_VOLUME = Integer.parseInt(dataJson.getString("max_volume"));
					current_volume = Integer.parseInt(dataJson.getString("current_volume"));
					if(dataJson.getString("bluetooth_enable").equals("true")) {
						tbBluttooth.setToggleOn();
					} else {
						tbBluttooth.setToggleOff();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				dismissDialog(mProgressDialog);
				break;
			case FLAG_QUERY_CONFIG_FAILURE:
				showToast("查询配置信息失败");
				dismissDialog(mProgressDialog);
				break;
			case FLAG_CONFIGURE_BT_COMPLETE:
				if (tbBluttooth.getToggleState()) {
					showToast("蓝牙启用成功");
				} else {
					showToast("蓝牙关闭成功");
				}
				dismissDialog(mProgressDialog);
				break;
			case FLAG_CONFIGURE_BT_FAILURE:
				if (tbBluttooth.getToggleState()) {
					showToast("蓝牙启用失败");
					tbBluttooth.setToggleOff();
				} else {
					showToast("蓝牙关闭失败");
					tbBluttooth.setToggleOn();
				}
				dismissDialog(mProgressDialog);
				break;
			case FLAG_CONFIGURE_VOLUME_COMPLETE:
				showToast("修改音量成功");
				dismissDialog(mProgressDialog);
				break;
			case FLAG_CONFIGURE_VOLUME_FAILURE:
				showToast("修改音量失败");
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
	 * 显示音量设置的PopupWindow
	 * 
	 */
	private void showVolumePopup() {
		View popouView = View.inflate(this, R.layout.popup_test_volume, null);

		final VoiceSeekBar vsVolume = (VoiceSeekBar) popouView.findViewById(R.id.vs_volume);
		vsVolume.setMaxLevel(MAX_VOLUME);
		vsVolume.setCurrentLevel(current_volume);
		vsVolume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(VoiceSeekBar seekBar) {
				saveConfig(TYPE_SAVE_VOLUME_CONIFG);
			}
			
			@Override
			public void onStartTrackingTouch(VoiceSeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(VoiceSeekBar seekBar, int progress) {
				current_volume = progress;
			}
		});
		final PopupWindow popup = new PopupWindow(popouView, DensityUtil.getScreenWidth(this), DensityUtil.getScreenHeight(this) / 4);

		// 设置PopupWindow可以获取焦点
		popup.setFocusable(true);
		popup.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#dfdfdf")));
		// 设置PopupWindow显示和隐藏动画
		popup.setAnimationStyle(R.style.volume_seekbar_dlg_anim);
		// 设置PopupWindow显示的位置
		popup.showAtLocation(llSound, Gravity.BOTTOM, 0, 0);
	}
	
	/**
	 * 弹出重启设备确认对话框
	 */
	private void showRebootDlg() {
		DialogUtil.showConfirmDialog(this, null, "请确认是否要重启设备？", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				rebootDevice(conn, mHandler, 2);
				dialog.cancel();
			}
		}, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
	}

}
