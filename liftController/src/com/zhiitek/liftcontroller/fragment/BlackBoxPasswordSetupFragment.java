package com.zhiitek.liftcontroller.fragment;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.components.UdpSocketConnection;
import com.zhiitek.liftcontroller.components.UdpSocketConnection.SocketDataCallback;
import com.zhiitek.liftcontroller.utils.BlackBoxUtil;
import com.zhiitek.liftcontroller.views.CustomFormCellView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

/**
 * 本功能暂不使用
 * @author 
 *
 */
public class BlackBoxPasswordSetupFragment extends BaseFragment {
	
	private View parentView;  
	
	private EditText oldPwdEditText, newPwd1EditText, newPwd2EditText;
	
	private Button btnChangePwd;
	
	private UdpSocketConnection udp;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		parentView = inflater.inflate(R.layout.fragment_blackbox_passwordsetup, null);
		return parentView;
	}

	@Override
	protected void findViewById() {
		oldPwdEditText = ((CustomFormCellView) parentView.findViewById(R.id.black_box_old_pwd)).getInfoEditText();
		newPwd1EditText = ((CustomFormCellView) parentView.findViewById(R.id.black_box_new_pwd1)).getInfoEditText();
		newPwd2EditText = ((CustomFormCellView) parentView.findViewById(R.id.black_box_new_pwd2)).getInfoEditText();
		oldPwdEditText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
		newPwd1EditText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
		newPwd2EditText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
		btnChangePwd = (Button) parentView.findViewById(R.id.btn_change_black_box_pwd);
	}

	@Override
	protected void setListener() {
		btnChangePwd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				changePassword();
			}
		});
	}

	protected void changePassword() {
		if (confirmPwdFormat()) {
			showLoadingDlg();
			int oldPwd = Integer.valueOf(oldPwdEditText.getText().toString().trim());
			int newPwd = Integer.valueOf(newPwd1EditText.getText().toString().trim());
			udp.post(BlackBoxUtil.makeSendCommand(0xEE, BlackBoxUtil.byteMerger(BlackBoxUtil.int2byteArray(oldPwd, 4), BlackBoxUtil.int2byteArray(newPwd, 4)), 8), new SocketDataCallback() {
				@Override
				public void onSuccess(byte[] result) {
					dismissDialog(mProgressDialog);
					if ( 0xEE == BlackBoxUtil.getResponseCmd(result)) {
						if (BlackBoxUtil.SSE_SUCCESS == BlackBoxUtil.getResponseCode(result)) {
							showToast("修改连接口令成功！");
						} else {
							showToast("修改连接口令失败！");
						}
					} else {
						showToast("修改口令时,功能码错误");
					}
				}
				
				@Override
				public void onFailure(int errorCode) {
					dismissDialog(mProgressDialog);
					promptBlackBoxSocketonFailure("修改连接口令", errorCode);
				}
			});
		}
	}

	private boolean confirmPwdFormat() {
		if (checkPswFormat(oldPwdEditText.getText().toString().trim())) {
			if (newPwd1EditText.getText().toString().trim().equals(newPwd2EditText.getText().toString().trim())){
				if(checkPswFormat(newPwd1EditText.getText().toString().trim())) {
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

	@Override
	protected void dealProcessLogic() {
		udp = new UdpSocketConnection(getActivity());
	}
	
	public static BlackBoxPasswordSetupFragment newInstance() {
		return new BlackBoxPasswordSetupFragment();
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		udp.shutdown();
	}

}
