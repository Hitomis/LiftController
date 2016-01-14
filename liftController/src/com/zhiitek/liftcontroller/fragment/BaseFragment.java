package com.zhiitek.liftcontroller.fragment;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.components.UdpSocketConnection;
import com.zhiitek.liftcontroller.components.net.NetWorkHelper;
import com.zhiitek.liftcontroller.utils.AppConstant;
import com.zhiitek.liftcontroller.utils.BlackBoxUtil;
import com.zhiitek.liftcontroller.views.CustomProgressDialog;

public abstract class BaseFragment extends Fragment{

	protected View mView;
	
	protected Context mContext;
	
	/** 旋转的loading对话框	 */
	protected CustomProgressDialog mProgressDialog;
	
	protected ImageView ivPrompt;
	
	protected NetWorkHelper netWorkHelper = NetWorkHelper.getInstance();
	
	protected SharedPreferences sharedPreferences;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContext = getActivity();
		netWorkHelper.init(mContext);
		if (sharedPreferences == null) {
			sharedPreferences = getActivity().getSharedPreferences(getActivity().getApplicationInfo().packageName, Context.MODE_PRIVATE);
		}
		initView();
	}

	/**
	 * 模板方法
	 */
	private final void initView() {
		findViewById();
		setListener();
		dealProcessLogic();
	}

	/**
	 * 布局文件中控件初始化
	 */
	protected abstract void findViewById();

	/**
	 * 设置监听器
	 */
	protected abstract void setListener();

	/**
	 * 处理该页面中的业务逻辑
	 */
	protected abstract void dealProcessLogic();
	
	/**
	 * 获取用户名
	 * @return
	 */
	protected String getUserID() {
		return sharedPreferences.getString(AppConstant.KEY_USER_ID, "");
	}
	
	protected void showToast(String text) {
		Toast.makeText(mContext.getApplicationContext(), text, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * 如果列表没有数据,则显示当前没有数据的图片,反之隐藏
	 * @param list
	 */
	protected void showBlank(List<?> list) {
		FragmentActivity activity = getActivity();
		if (activity != null) {
			ivPrompt = (ImageView) getActivity().findViewById(R.id.iv_prompt);
			if (list.isEmpty() && ivPrompt!= null) {
				ivPrompt.setVisibility(View.VISIBLE);
			}else {
				ivPrompt.setVisibility(View.GONE);
			}
		}
	}
	
	/**
	 * 检查命令码
	 * @param reciveCmd
	 * @return
	 */
	protected static boolean checkCmd(int reciveCmd) {
		if ((reciveCmd & 0x080) == 0) {
			return true;
		}
		return false;
	}
	
	protected void dismissDialog(Dialog dlg) {
		if (dlg != null) {
			dlg.dismiss();
		}
	}

	/**
	 * 硬件测试，socket通信返回错误码时，提示用户
	 * @param text
	 * @param errorCode
	 */
	protected void promptBlackBoxSocketonFailure(String text, int errorCode) {
		switch (errorCode) {
		case UdpSocketConnection.FLAG_CONNECT_TIMEOUT:
			showToast(String.format("%s,连接超时", text));
			break;
		case UdpSocketConnection.FLAG_CONNECT_NOT_ENABLE_WIFI:
			showToast(String.format("%s,wifi未启动", text));
			break;
		case UdpSocketConnection.FLAG_CREATE_SERVER_ERROR:
			showToast(String.format("%s,创建 UDP 服务端错误", text));
			break;
		case UdpSocketConnection.FLAG_POST_DATA_ERROR:
			showToast(String.format("%s,发送数据错误", text));
			break;
		case UdpSocketConnection.FLAG_COMMU_DATA_ING:
			showToast(String.format("%s,当前通信未结束", text));
			break;
		}
	}
	
	/**
	 * 硬件测试，检查返回数据的功能码
	 * @param result
	 * @param cmd
	 */
	protected boolean checkCmd(byte[] result, int cmd) {
		if (BlackBoxUtil.getResponseCmd(result) == cmd) {
			return true;
		} else {
			showToast("请求和应答的功能码不一致");
			return false;
		}
	}
	
	/**
	 * 检查密码格式，1到9位数字
	 * @param psw
	 * @return
	 */
	public boolean checkPswFormat(String psw) {
		boolean flag = false;
		if (psw.matches("^(?=.*[0-9].*).{1,9}$")) {
			flag = true;
		} else {
			flag = false;
		}
		return flag;
	}
	
	public void showLoadingDlg() {
		mProgressDialog = new CustomProgressDialog(getActivity(), R.style.loading_dialog);
		mProgressDialog.show();
	}
	
	/**
	 * 获取用户id即用户名
	 * @return
	 */
	protected String getUserId() {
		return sharedPreferences.getString(AppConstant.KEY_USER_ID, "");
	}
	
	/**
	 * 获取用户角色类型
	 * @return
	 */
	protected int getUserType() {
		int userType = Integer.valueOf(sharedPreferences.getString(AppConstant.KEY_USER_TYPE, "-1"));
		return userType;
	}

}
