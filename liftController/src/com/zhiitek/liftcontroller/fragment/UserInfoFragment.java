package com.zhiitek.liftcontroller.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.activity.ModifyPasswordActivity;

public class UserInfoFragment extends BaseFragment{
	
	private LinearLayout ll_modify_psw;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_user_info, container, false);
		return mView;
	}

	@Override
	protected void findViewById() {
		ll_modify_psw = (LinearLayout) mView.findViewById(R.id.ll_modify_psw);
	}

	@Override
	protected void setListener() {
		ll_modify_psw.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(mContext, ModifyPasswordActivity.class));
			}
		});
	}

	@Override
	protected void dealProcessLogic() {
		
	}

}
