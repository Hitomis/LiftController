package com.zhiitek.liftcontroller.activity;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.components.SocketConnection;
import com.zhiitek.liftcontroller.components.SocketConnection.SocketDataCallback;
import com.zhiitek.liftcontroller.components.net.NetWorkHelper;
import com.zhiitek.liftcontroller.utils.AppConstant;
import com.zhiitek.liftcontroller.utils.DensityUtil;
import com.zhiitek.liftcontroller.utils.LiftActivityManager;
import com.zhiitek.liftcontroller.views.wheelpicker.view.SimpleWheelChangeListener;
import com.zhiitek.liftcontroller.views.wheelpicker.widget.WheelDatePicker;

public abstract class BaseActivity extends FragmentActivity {
	
	public static final int TYPE_USER_ADMINISTRATOR = 0; //管理员身份角色
	public static final int TYPE_USER_INSPECT = 1; //特检身份角色
	public static final int TYPE_USER_SUPERVISE = 2; //监督身份角色
	public static final int TYPE_USER_MAINTENANCE = 3; //维保身份角色
	public static final int TYPE_USER_PROPERTY = 4; //物业身份角色
	
	protected final static int FLAG_LOGIN_SUCCESS = 0;
	
	protected final static int FLAG_LOGIN_FAILURE = -1;
	
	protected NetWorkHelper netWorkHelper = NetWorkHelper.getInstance();
	
	protected SharedPreferences sharedPreferences;
	
	private TextView tvTitle;
	
	protected ImageView ivPrompt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (sharedPreferences == null) {
			sharedPreferences = getSharedPreferences(this.getApplicationInfo().packageName, Context.MODE_PRIVATE);
		}
		LiftActivityManager.getInstance().addActivity(this);
		netWorkHelper.init(this);
		initView();
	}

	/**
	 * 模板方法
	 */
	private final void initView() {
		loadViewLayout();
		findViewById();
		setListener();
		dealProcessLogic();
	}

	/**
	 * 获取用户id即用户名
	 * @return
	 */
	protected String getUserId() {
		return sharedPreferences.getString(AppConstant.KEY_USER_ID, "");
	}
	
	/**
	 * 获取用户密码
	 * @return
	 */
	protected String getUserPassword() {
		return sharedPreferences.getString(AppConstant.KEY_PASSWORD, "");
	}
	
	/**
	 * 获取用户角色类型
	 * @return
	 */
	protected int getUserType() {
		int userType = Integer.valueOf(sharedPreferences.getString(AppConstant.KEY_USER_TYPE, "-1"));
		return userType;
	}
	
	/**
	 * 设置标题栏 （默认左边点击返回到上一页） 
	 * @param title 标题文字
	 * @param rightButtonClickListener 右边按钮点击事件 
	 * rightButtonClickListener为null 表示右边按钮不显示,如果不为null，且想修改按钮图案,请重写
	 * {@link #editRightImageResource()}
	 */
	protected void setTitleBar(String title, OnClickListener rightButtonClickListener) {
		tvTitle = ((TextView) findViewById(R.id.tv_title));
		tvTitle.setText(title);
		findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				backActivity();
			}
		});
		if (rightButtonClickListener != null) {
			ImageView ivRight = (ImageView) findViewById(R.id.iv_right);
			ivRight.setVisibility(View.VISIBLE);
			ivRight.setOnClickListener(rightButtonClickListener);
			ivRight.setImageResource(editRightImageResource());
		}
	}
	
	/**
	 * 更换右侧按钮图片
	 * @param resID
	 */
	protected void changeRightImageResource(int resID){
		ImageView ivRight = (ImageView) findViewById(R.id.iv_right);
		if (resID == 0) {
			ivRight.setVisibility(View.GONE);
		} else {
			ivRight.setImageResource(resID);
			ivRight.setVisibility(View.VISIBLE);
		}
			
	}
	
	/**
	 * 修改titlebar 右边按钮背景图片
	 * @return 图片资源id
	 */
	protected int editRightImageResource() {
		return R.drawable.menu;
	}

	/**
	 * 加载布局xml文件
	 */
	protected abstract void loadViewLayout();

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
	
	protected void showToast(String text) {
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LiftActivityManager.getInstance().removeActivity(this);
	}
	
	/**
	 * 清除当前与终端设备连接的有效时间->【使得app断开与设备终端的连接,用户需要重新激活或者扫描】
	 */
	protected void clearConnectDevicesTime() {
		sharedPreferences.edit().remove("connecttime").commit();
	}
	
	/**
	 * 发送重启命令给终端设备，让设备重启
	 * @param conn
	 * @param mHandler
	 * @param resultCode
	 */
	protected void rebootDevice(SocketConnection conn, final Handler mHandler, final int resultCode) {
		conn.post(createRebootJson(), new SocketDataCallback() {
			@Override
			public void onSuccess(String result) {
				clearConnectDevicesTime();
				setResult(resultCode);
				finish();
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
	
	protected JSONObject createSocketJsonParameters(int cmd, JSONObject jsonData) {
		JSONObject jsonParam = new JSONObject();
		try {
			jsonParam.put(AppConstant.KEY_SOCKET_CMD, cmd);
			if (jsonData == null) {
				jsonData = new JSONObject();
			}
			jsonData.put(AppConstant.KEY_SOCKET_CHECKCODE, sharedPreferences.getString(AppConstant.DEV_SERIALS, "zhiitek"));
			jsonParam.put(AppConstant.KEY_SOCKET_DATA, jsonData);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonParam;
	}
	
	/**
	 * 包装重启设备的命令Json数据
	 * @return
	 */
	private JSONObject createRebootJson() {
		JSONObject jsonParam = new JSONObject();
		JSONObject jsonData = new JSONObject();
		try {
			jsonParam.put(AppConstant.KEY_SOCKET_CMD, 14);
			jsonData.put(AppConstant.KEY_SOCKET_CHECKCODE, sharedPreferences.getString(AppConstant.DEV_SERIALS, "zhiitek"));
			jsonParam.put(AppConstant.KEY_SOCKET_DATA, jsonData);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonParam;
	}
	
	/**
	 * 回退到上一个Activity
	 */
	public void backActivity() {
		finish();
	}
	
	/**
	 * 检查密码格式,最少六位,同时包含数字和字母
	 * @param psw
	 * @return
	 */
	public boolean checkPswFormat(String psw) {
		boolean flag = false;
		if (psw.length() >= 6 && psw.matches("^(?=.*[0-9].*)(?=.*[a-zA-Z].*).{6,}$")) {
			flag = true;
		} else {
			flag = false;
		}
		return flag;
	}
	
	/**
	 * 从屏幕底部显示出DatePicker控件
	 */
	protected void showDatePickerPopup(final TextView tv, String defaultStr) {
		View popouView = View.inflate(this, R.layout.popup_time_select, null);
		
		TextView tvCancel = (TextView) popouView.findViewById(R.id.tv_cancel);
		TextView tvConfirm = (TextView) popouView.findViewById(R.id.tv_confirm);
		final WheelDatePicker dataPicker = (WheelDatePicker) popouView.findViewById(R.id.wdp_datepicker);
		dataPicker.setTextColor(0xFF3F96C3);
		dataPicker.setLabelColor(0xFF94A0B6);
		
		dataPicker.setOnWheelChangeListener(new SimpleWheelChangeListener() {
			@Override
			public void onWheelSelected(int index, String data) {}
		});
		
		//时间控件回显之前选择的时间
		String currData = tv.getText().toString().trim();
		if (defaultStr != null && !currData.equals(defaultStr)) {
			dataPicker.setCurrentDate(currData);
		}
		
		final PopupWindow popup = new PopupWindow(popouView, 
				DensityUtil.getScreenWidth(this), 
				DensityUtil.getScreenHeight(this) / 3);
		// 设置PopupWindow可以获取焦点[在失去焦点的时候,会自动关闭PopupWindow]
		popup.setFocusable(true);
		popup.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#dfdfdf")));
		// 设置PopupWindow显示和隐藏动画
		popup.setAnimationStyle(R.style.volume_seekbar_dlg_anim);
		// 设置PopupWindow显示的位置
		popup.showAtLocation(tvTitle, Gravity.BOTTOM, 0, 0);
		tvCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				popup.dismiss();
			}
		});
		tvConfirm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tv.setText(dataPicker.getData());
				popup.dismiss();
			}
		});
		
	}
	
	/**
	 * 如果列表没有数据,则显示当前没有数据的图片,反之隐藏
	 * @param list
	 */
	protected void showBlank(List<?> list) {
		ivPrompt = (ImageView) findViewById(R.id.iv_prompt);
		if (list.isEmpty() && ivPrompt!= null) {
			ivPrompt.setVisibility(View.VISIBLE);
		}else {
			ivPrompt.setVisibility(View.GONE);
		}
	}
	
}
