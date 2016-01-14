package com.zhiitek.liftcontroller.activity;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.components.SocketConnection;
import com.zhiitek.liftcontroller.components.SocketConnection.SocketDataCallback;
import com.zhiitek.liftcontroller.utils.AppConstant;
import com.zhiitek.liftcontroller.utils.DialogUtil;
import com.zhiitek.liftcontroller.views.CustomEditDialog;
import com.zhiitek.liftcontroller.views.CustomEditDialog.Builder.OnClick;
import com.zhiitek.liftcontroller.views.CustomProgressDialog;
import com.zhiitek.liftcontroller.views.SwipeFinishLayout;


public class ConfigLiftActivity extends BaseActivity implements OnTouchListener{

	private EditText mEditLiftNum, mEditLiftPort, mEditServerPort;
	private EditText mEditServerUrl;
	private Button mSubmitBtn;
	private TextView mTvDisplayVersion;
	
	private SocketConnection conn;
	
	private CustomProgressDialog mProgressDialog;
	
	//是否是第一次配置
	private boolean isLiftFirstConfig;
	
	private float startX, startY;
	
	private static final int FLAG_CONFIGURE_LIFT_COMPLETE = 100;
	private static final int FLAG_CONFIGURE_LIFT_FAILURE = 101;
	
	private static final int FLAG_QUERY_LIFT_CONFIG_COMPLETE = 102;
	private static final int FLAG_QUERY_LIFT_CONFIG_FAILURE = 103;
	
	private static final int FLAG_LIFT_UNINIT = 104;
	
	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_config_lift);
	}

	@Override
	protected void findViewById() {
		mEditLiftNum = (EditText) findViewById(R.id.edit_lift_num);
		mEditLiftPort = (EditText) findViewById(R.id.edit_lift_port);
		mEditServerPort = (EditText) findViewById(R.id.et_server_port);
		mEditServerUrl = (EditText) findViewById(R.id.et_server_url);
		mSubmitBtn = (Button) findViewById(R.id.btn_submit_config);
		mTvDisplayVersion = (TextView) findViewById(R.id.tv_display_version);
	}

	@Override
	protected void setListener() {
		mSubmitBtn.setOnClickListener(btnSubmitListener);
		mEditLiftPort.setOnTouchListener(this);
//		mEditServerUrl.setOnTouchListener(this);
//		mEditLiftNum.setOnTouchListener(this);
	}

	private OnClickListener btnSubmitListener= new OnClickListener() {
		@Override
		public void onClick(View v) {
			submitLiftConfig();
		}
	};

	@Override
	protected void dealProcessLogic() {
		setTitleBar("设备配置", null);
		SwipeFinishLayout.attachToActivity(this);
		conn = new SocketConnection(this.getApplicationContext());
		queryConfig();
	}
	
	/**
	 * 查询配置信息
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
	 * 查询信息的Json类
	 * @return
	 */
	private JSONObject createQueryConfigJson() {
		JSONObject jsonParam = new JSONObject();
		JSONObject jsonData = new JSONObject();
		try {
			jsonParam.put(AppConstant.KEY_SOCKET_CMD, 3);
			jsonData.put(AppConstant.KEY_SOCKET_CHECKCODE, sharedPreferences.getString(AppConstant.DEV_SERIALS, "zhiitek"));
			jsonData.put(AppConstant.KEY_USER_ID, getUserId());
			jsonParam.put(AppConstant.KEY_SOCKET_DATA, jsonData);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonParam;
	}

	/**
	 * 配置信息的Json类
	 * @return
	 */
	private JSONObject createSaveConfigJson() {
		JSONObject jsonParam = new JSONObject();
		JSONObject jsonData = new JSONObject();
		try {
			jsonParam.put(AppConstant.KEY_SOCKET_CMD, 4);
			if (isLiftFirstConfig) {
				jsonData.put(AppConstant.KEY_SOCKET_CHECKCODE, "zhiitek");
			} else {
				jsonData.put(AppConstant.KEY_SOCKET_CHECKCODE, sharedPreferences.getString(AppConstant.DEV_SERIALS, "zhiitek"));	
			}
			String serverUrl = mEditServerUrl.getText().toString().trim();
			String serverPort = mEditServerPort.getText().toString().trim();
			String completeUrl = String.format("http://%s:%s/liftman/liftman", serverUrl, serverPort);
			jsonData.put("liftNo", mEditLiftNum.getText().toString().trim());
			jsonData.put("liftPort", mEditLiftPort.getText().toString().trim());
			jsonData.put("centerPort", completeUrl);
//			jsonData.put("mediaUrl", mEditMediaUrl.getText().toString().trim());
			jsonParam.put(AppConstant.KEY_SOCKET_DATA, jsonData);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonParam;
	}

	/**
	 * 提交配置信息
	 */
	public void submitLiftConfig() {
		if (!validateBindingInfo()) return ;
		DialogUtil.showConfirmDialog(this, null, "绑定操作后设备会重启,是否继续",  new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(final DialogInterface dialog, int which) {
				mProgressDialog = new CustomProgressDialog(ConfigLiftActivity.this, R.style.loading_dialog);
				mProgressDialog.show();
				conn.post(createSaveConfigJson(), new SocketDataCallback() {
					@Override
					public void onSuccess(String result) {
 						resolveSuccessResult(result);
						dialog.dismiss();
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
						dialog.dismiss();
					}
				});
			}
		},  new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
	}

	/**
	 * 绑定电梯表单验证
	 * @return
	 */
	public boolean validateBindingInfo() {
		String liftNo = mEditLiftNum.getText().toString().trim();
		String serverUrl = mEditServerUrl.getText().toString().trim();
		String serverPort = mEditServerPort.getText().toString().trim();
		String patternUrl = "^[w]{3}[.][0-9A-Za-z]+[.][a-z]{2,3}?$";
		String patternIp = "^[0-9]{1,3}[.][0-9]{1,3}[.][0-9]{1,3}[.][0-9]{1,3}";
		boolean result = true;
		if (TextUtils.isEmpty(liftNo)) {
			mEditLiftNum.setError(Html.fromHtml("<font color=#E10979>请输入电梯编号</font>"));
			result = false;
		} else if (liftNo.length() != 20) {
			mEditLiftNum.setError(Html.fromHtml("<font color=#E10979>电梯编号长度为20位有效数字</font>"));
			result = false;
		} else if (TextUtils.isEmpty(serverUrl)) {
			mEditServerUrl.setError(Html.fromHtml("<font color=#E10979>请输入服务器地址</font>"));
			result = false;
		} else if (TextUtils.isEmpty(serverPort)) {
			mEditServerPort.setError(Html.fromHtml("<font color=#E10979>请输入服务器端口号</font>"));
			result = false;
		} else if (!serverUrl.matches(patternUrl) && !serverUrl.matches(patternIp)) {
			mEditServerUrl.setError(Html.fromHtml("<font color=#E10979>服务器地址格式错误</font>"));
			result = false;
		}
		return result;
	}
	
	/**
	 * 解析配置设备信息的结果
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
						msg.what = FLAG_CONFIGURE_LIFT_COMPLETE;//设备配置成功
						if(!resultJson.isNull(AppConstant.KEY_SOCKET_DATA)) {
							JSONObject jData = resultJson.getJSONObject(AppConstant.KEY_SOCKET_DATA);
							msg.obj = jData.getString(AppConstant.KEY_SOCKET_EMESSAGE);
						}
					}else {
						if(!resultJson.isNull(AppConstant.KEY_SOCKET_DATA)) {
							JSONObject jData = resultJson.getJSONObject(AppConstant.KEY_SOCKET_DATA);
							if (!jData.isNull(AppConstant.KEY_SOCKET_EMESSAGE)) {
								msg.obj = jData.getString(AppConstant.KEY_SOCKET_EMESSAGE);
							}
						}
						msg.what = FLAG_CONFIGURE_LIFT_FAILURE;
					}
				}
			} catch (JSONException e) {
				msg.what = FLAG_CONFIGURE_LIFT_FAILURE;
			}
		} else {
			msg.what = FLAG_CONFIGURE_LIFT_FAILURE;
		}
		msg.sendToTarget();
	}
	
	/**
	 * 解析查询设备配置信息的结果
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
						if (resultJson.getJSONObject(AppConstant.KEY_SOCKET_DATA).isNull(AppConstant.LIFT_NUMBER)){
							msg.what = FLAG_LIFT_UNINIT;
						} else {
							msg.what = FLAG_QUERY_LIFT_CONFIG_COMPLETE;//设备查询成功
							msg.obj = resultJson.getJSONObject(AppConstant.KEY_SOCKET_DATA);
						}
					}else {
						msg.what = FLAG_QUERY_LIFT_CONFIG_FAILURE;
					}
				}
			} catch (JSONException e) {
				msg.what = FLAG_QUERY_LIFT_CONFIG_FAILURE;
			}
		} else {
			msg.what = FLAG_QUERY_LIFT_CONFIG_FAILURE;
		}
		msg.sendToTarget();
	}
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case FLAG_CONFIGURE_LIFT_COMPLETE:
				dismissDialog(mProgressDialog);
				if(msg.obj != null) {
					showToast(msg.obj.toString() + ",自动重启中");
				} else {
					showToast("设备绑定成功,自动重启中");
				}
				isLiftFirstConfig = false;
				rebootDevice(conn, mHandler, 1);
				break;
			case FLAG_CONFIGURE_LIFT_FAILURE:
				if(msg.obj != null) {
					showToast(msg.obj.toString());
				} else {
					showToast("设备绑定失败");
				}
				dismissDialog(mProgressDialog);
				break;
			case FLAG_QUERY_LIFT_CONFIG_COMPLETE:
				JSONObject dataJson = (JSONObject) msg.obj;
				try {
					setConfig2Edittext(dataJson);
				} catch (Exception e) {
					e.printStackTrace();
				}
//				showToast("请修改设备配置信息！");
				dismissDialog(mProgressDialog);
				break;
			case FLAG_LIFT_UNINIT:
				showToast("电梯设备未进行初始化，请直接配置相关信息！");
				isLiftFirstConfig = true;
				dismissDialog(mProgressDialog);
				break;
			case FLAG_QUERY_LIFT_CONFIG_FAILURE:
				showToast("查询设备配置信息失败");
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
	 * 弹出输入地址的对话框
	 * @param editText
	 * @param title
	 * @param content
	 */
	private void showInputUrlDialog(final EditText editText, String title, String content) {
		CustomEditDialog.Builder customBuilder = new CustomEditDialog.Builder(this);
		customBuilder.setTitle(title).setContent(content)
				.setButton("保存", new OnClick() {
					@Override
					public void onClick(DialogInterface dialog, String text) {
						dialog.cancel();
						editText.setText(text);
					}

				});
		CustomEditDialog dialog = customBuilder.create();
		dialog.show();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			startX = event.getX();
			startY = event.getY();
			break;

		case MotionEvent.ACTION_UP:
			if(Math.abs(event.getX() - startX) < 5 && Math.abs(event.getY() - startY) < 5) {
				switch(v.getId()){
				case R.id.edit_lift_port:
					showInputUrlDialog(mEditLiftPort, "通信地址", mEditLiftPort.getText().toString());
					break;
//				case R.id.et_server_url:
//					showInputUrlDialog(mEditServerUrl, "服务器地址", mEditServerUrl.getText().toString());
//					break;
//				case R.id.edit_lift_num:
//					showInputUrlDialog(mEditLiftNum, "电梯编号", mEditLiftNum.getText().toString());
//					break;
				}
			}
			break;
		}
		return false;
	}
	
	/**
	 * 显示从设备获取的配置信息
	 * @param object
	 * @throws JSONException
	 * @throws MalformedURLException
	 */
	private void setConfig2Edittext(JSONObject object) throws JSONException, MalformedURLException {
		if (object.has(AppConstant.LIFT_NUMBER)) {
			mEditLiftNum.setText(object.getString(AppConstant.LIFT_NUMBER));
		}
		if (object.has("liftPort")) {
			mEditLiftPort.setText(object.getString("liftPort"));
		}
		if (object.has("centerPort")) {
			URL serverUrl = new URL(object.getString("centerPort"));
			mEditServerUrl.setText(serverUrl.getHost());
			mEditServerPort.setText(String.valueOf(serverUrl.getPort()));
		}
		if (object.has("displayVersion")) {
			mTvDisplayVersion.setText(object.getString("displayVersion"));
		}
//		if (object.has("mediaUrl")) {
//			mEditMediaUrl.setText(object.getString("mediaUrl"));
//		}
	}

}
