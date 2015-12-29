package com.zhiitek.liftcontroller.activity;


import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.components.net.NetWorkCons;
import com.zhiitek.liftcontroller.components.net.NetWorkHelper;
import com.zhiitek.liftcontroller.components.net.client.NetCallback;
import com.zhiitek.liftcontroller.service.task.UpdateCountsTask;
import com.zhiitek.liftcontroller.utils.AppConstant;
import com.zhiitek.liftcontroller.views.cpb.CircularProgressButton;

public class LoginActivity extends BaseActivity {

	private EditText etUsername, etPassword;

	private CircularProgressButton cpbLogin;
	
	private ImageView imgUrlSetting;
	
	private String username;

	private String password;
	
	private TextView tvForgetPsw;
	
	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_login);
	}

	@Override
	protected void findViewById() {
		etUsername = (EditText) findViewById(R.id.et_name);
		etPassword = (EditText) findViewById(R.id.et_password);

		cpbLogin = (CircularProgressButton) findViewById(R.id.cpb_login);
		
		imgUrlSetting = (ImageView) findViewById(R.id.url_setting_img);
		tvForgetPsw = (TextView) findViewById(R.id.tv_forget_psw);
	}

	@Override
	protected void setListener() {
		cpbLogin.setIndeterminateProgressMode(true);// 使用不确定进程样式
		imgUrlSetting.setOnClickListener(urlSettingListener);
		tvForgetPsw.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class));
			}
		});
	}

	private OnClickListener urlSettingListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(LoginActivity.this, UrlSettingActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.tran_fade_in_activity, 0);
		}
	};

	@Override
	protected void dealProcessLogic() {
		echoUserInfo();
	}

	/**
	 * 回显示上次登陆的用户名和密码
	 */
	private void echoUserInfo() {
		SharedPreferences sharedPreference = getSharedPreferences(AppConstant.KEY_SP_USER, Context.MODE_PRIVATE);
		etUsername.setText(sharedPreference.getString(AppConstant.KEY_USER_ID, ""));
		etPassword.setText(sharedPreference.getString(AppConstant.KEY_PASSWORD, ""));
	}

	/**
	 * 将登陆成功的用户信息记录下来,以便下次回显
	 */
	private void saveUserInfo() {
		SharedPreferences sharedPreference = getSharedPreferences(AppConstant.KEY_SP_USER, Context.MODE_PRIVATE);
		Editor edit = sharedPreference.edit();
		edit.putString(AppConstant.KEY_USER_ID, username);
		edit.putString(AppConstant.KEY_PASSWORD, password);
		edit.commit();
	}

	public void login(View view) {
		username = etUsername.getText().toString().trim();
		password = etPassword.getText().toString().trim();

		if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
			showToast("用户名或者密码不能为空");
			return;
		}

		if (cpbLogin.getProgress() == 0) {
			 cpbLogin.setProgress(50);
			 cpbLogin.setClickable(false);
			 login(username, password);
		} else {
			cpbLogin.setProgress(0);
		}
	}
	
	private void login(final String username, final String password) {
		try {
			netWorkHelper.execHttpNetWithoutPrompt(NetWorkCons.loginUrl, initLoginJsonParameter(username, password), new NetCallback() {
				@Override
				public void callback(JSONObject resultJson) {
					if (resultJson != null) {
						try {
							switch (resultJson.getInt(NetWorkCons.JSON_KEY_RESULT)) {
							case FLAG_LOGIN_SUCCESS:// success
								sharedPreferences.edit().putString(AppConstant.KEY_USER_TYPE, resultJson.getString(NetWorkCons.JSON_KEY_USERTYPE)).commit();
								sharedPreferences.edit().putString(AppConstant.KEY_USER_ID, username).commit();
								sharedPreferences.edit().putString(AppConstant.KEY_PASSWORD, password).commit();
								saveUserInfo();
								Intent intent = new Intent(LoginActivity.this, MainActivity.class);
								startActivity(intent);
								finish();
								break;
							case FLAG_LOGIN_FAILURE:// failure 用户名密码错误
								showToast("用户名或者密码错误");
								resetLoginBtnError();
								break;
							}
						} catch (JSONException e) {
							showToast("网络数据错误, 请联系我们");
						}
					} else {
						resetLoginBtnError();
					}
				}
			});
		} catch (JSONException e) {
			showToast("网络数据错误, 请联系我们");
		}
	}
	
	private JSONObject initLoginJsonParameter(String username, String password) throws JSONException {
		JSONObject jsonParams = netWorkHelper.initJsonParameters(NetWorkCons.CMD_HTTP_LOGIN_AND_GET_LIFTINFOS);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_USERID, username, jsonParams);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_USERPWD, password, jsonParams);
		return jsonParams;
	}

	private void resetLoginBtnError() {
		cpbLogin.setClickable(true);
		cpbLogin.setProgress(-1);
	}

}
