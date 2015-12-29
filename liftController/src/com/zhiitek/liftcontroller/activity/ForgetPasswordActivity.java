package com.zhiitek.liftcontroller.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.components.net.NetWorkCons;
import com.zhiitek.liftcontroller.components.net.NetWorkHelper;
import com.zhiitek.liftcontroller.components.net.client.NetCallback;

public class ForgetPasswordActivity extends BaseActivity{

	private EditText etUserId, etUserPhone, etPhoneCode, etNewPsw1, etNewPsw2;
	private Button btnResetPsw, btnGetCode;
	
	/** 成功 */
	protected final static int FLAG_SUCCESS = 0;
	/** 失败 */
	protected final static int FLAG_FAILURE = 1;

	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_forget_password);
	}

	@Override
	protected void findViewById() {
		etUserId = (EditText) findViewById(R.id.et_user_id);
		etUserPhone = (EditText) findViewById(R.id.et_user_phone);
		etPhoneCode = (EditText) findViewById(R.id.et_user_code);
		etNewPsw1 = (EditText)findViewById(R.id.et_new_psw);
		etNewPsw2 = (EditText)findViewById(R.id.et_confirm_new_psw);
		btnResetPsw = (Button)findViewById(R.id.btn_reset_psw);
		btnGetCode = (Button) findViewById(R.id.btn_get_code);
	}

	@Override
	protected void setListener() {
		btnResetPsw.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (confirmPsw()) {
					resetPsw();
				}
			}
		});
		btnGetCode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getCode();
			}
		});
	}

	@Override
	protected void dealProcessLogic() {
		setTitleBar("重置密码", null);
	}
	
	private void getCode() {
		if (etUserId.getText().toString().trim().length() > 0 && etUserPhone.getText().toString().trim().length() == 11) {
			getVerificationCode();
		} else {
			showToast("请检查用户名和手机号码格式！");
		}
	}
	
	/**
	 * 检查密码格式
	 * @return
	 */
	private boolean confirmPsw() {
		if (etPhoneCode.getText().toString().trim().length() == 6) {
			if (etNewPsw1.getText().toString().trim().equals(etNewPsw2.getText().toString().trim())){
				if(checkPswFormat(etNewPsw1.getText().toString().trim())) {
					return true;
				} else {
					showToast("请检查新密码格式！");
					return false;
				}
			} else {
				showToast("两次新密码输入不一致");
				return false;
			}
		} else {
			showToast("请检查验证码！");
			return false;
		}
	}
	
	/**
	 * 重置密码
	 */
	private void resetPsw() {
		try{
			netWorkHelper.execHttpNet(NetWorkCons.modifyPasswordUrl, initResetPswJsonParameter(), new NetCallback() {
				@Override
				public void callback(JSONObject resultJson) {
					if (resultJson != null) {
						try{
							switch (resultJson.getInt(NetWorkCons.JSON_KEY_SUCCESS)) {
							case FLAG_SUCCESS:
								showToast("重置密码成功，请重新登录！");
								finish();
								break;
							case FLAG_FAILURE:
								showToast(resultJson.getString(NetWorkCons.JSON_KEY_MESSAGE));
								break;
							}
						} catch (JSONException e) {
							showToast("网络数据错误, 请联系我们");
						}
					}
				}
			});
		} catch (JSONException e) {
			showToast("网络数据错误, 请联系我们");
		}
	}
	
	private JSONObject initResetPswJsonParameter() throws JSONException {
		JSONObject jsonParams = netWorkHelper.initJsonParameters(NetWorkCons.CMD_HTTP_MODIFY_LOGIN_PASSWORD);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_USERID, etUserId.getText().toString().trim(), jsonParams);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_CURRENTPWD, etUserPhone.getText().toString().trim(), jsonParams);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_NEWPWD, etNewPsw1.getText().toString().trim(), jsonParams);
		return jsonParams;
	}

	/**
	 * 获取验证码
	 */
	private void getVerificationCode() {
		try {
			netWorkHelper.execHttpNet(NetWorkCons.getSecurityCodeUrl, initGetCodeJsonParameter(), new NetCallback() {
				@Override
				public void callback(JSONObject resultJson) {
					if (resultJson != null) {
						try {
							switch (resultJson.getInt(NetWorkCons.JSON_KEY_SUCCESS)) {
							case FLAG_SUCCESS:
								showToast("请输入稍后收到的验证码！");
								btnResetPsw.setEnabled(true);
								break;
							case FLAG_FAILURE:
								showToast(resultJson.getString(NetWorkCons.JSON_KEY_MESSAGE));
								break;
							}
						} catch (JSONException e) {
							showToast("网络数据错误, 请联系我们");
						}
						
					}
				}
			});
		} catch (JSONException e) {
			showToast("网络数据错误, 请联系我们");
		}
	}
	
	private JSONObject initGetCodeJsonParameter() throws JSONException {
		JSONObject jsonParams = netWorkHelper.initJsonParameters(NetWorkCons.CMD_HTTP_GET_SECURITY_CODE);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_USERID, etUserId.getText().toString().trim(), jsonParams);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_USERPHONE, etUserPhone.getText().toString().trim(), jsonParams);
		return jsonParams;
	}
}
