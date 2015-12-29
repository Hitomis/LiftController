package com.zhiitek.liftcontroller.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.components.net.NetWorkCons;
import com.zhiitek.liftcontroller.components.net.NetWorkHelper;
import com.zhiitek.liftcontroller.components.net.client.NetCallback;
import com.zhiitek.liftcontroller.utils.AppConstant;
import com.zhiitek.liftcontroller.utils.LiftActivityManager;

public class ModifyPasswordActivity extends BaseActivity{
	
	private EditText etOldPsw, etNewPsw1, etNewPsw2;
	private Button btnModifyPsw;
	
	/**
	 * 成功
	 */
	protected final static int FLAG_SUCCESS = 0;
	/**
	 * 失败
	 */
	protected final static int FLAG_FAILURE = 1;

	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_modify_password);
	}

	@Override
	protected void findViewById() {
		etOldPsw = (EditText)findViewById(R.id.et_old_psw);
		etNewPsw1 = (EditText)findViewById(R.id.et_new_psw);
		etNewPsw2 = (EditText)findViewById(R.id.et_confirm_new_psw);
		btnModifyPsw = (Button)findViewById(R.id.btn_modify_psw);
	}

	@Override
	protected void setListener() {
		btnModifyPsw.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (confirmPsw()) {
					modifyPassword();
				}
			}
		});
	}

	@Override
	protected void dealProcessLogic() {
		setTitleBar("修改密码", null);
	}
	
	/**
	 * 检查密码格式
	 * @return
	 */
	private boolean confirmPsw() {
		if (checkPswFormat(etOldPsw.getText().toString().trim())) {
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
			showToast("请检查原密码格式！");
			return false;
		}
	}
	
	/**
	 * 修改密码
	 */
	private void modifyPassword() {
		try{
			netWorkHelper.execHttpNet(NetWorkCons.modifyPasswordUrl, initModifyPswJsonParameter(), new NetCallback() {
				@Override
				public void callback(JSONObject resultJson) {
					if (resultJson != null) {
						try{
							switch (resultJson.getInt(NetWorkCons.JSON_KEY_SUCCESS)) {
							case FLAG_SUCCESS:
								showToast("修改密码成功，请重新登录！");
								LiftActivityManager.getInstance().finishAllExcept(ModifyPasswordActivity.this);
								startActivity(new Intent(ModifyPasswordActivity.this, LoginActivity.class));
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
	
	private JSONObject initModifyPswJsonParameter() throws JSONException {
		JSONObject jsonParams = netWorkHelper.initJsonParameters(NetWorkCons.CMD_HTTP_MODIFY_LOGIN_PASSWORD);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_USERID, getUserId(), jsonParams);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_CURRENTPWD, etOldPsw.getText().toString().trim(), jsonParams);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_NEWPWD, etNewPsw1.getText().toString().trim(), jsonParams);
		return jsonParams;
	}
}
