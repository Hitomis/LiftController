package com.zhiitek.liftcontroller.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.components.SocketConnection;
import com.zhiitek.liftcontroller.components.SocketConnection.SocketDataCallback;
import com.zhiitek.liftcontroller.utils.AppConstant;
import com.zhiitek.liftcontroller.utils.DensityUtil;
import com.zhiitek.liftcontroller.utils.DialogUtil;
import com.zhiitek.liftcontroller.views.CustomProgressDialog;
import com.zhiitek.liftcontroller.views.StateChangeButton;
import com.zhiitek.liftcontroller.views.SwipeFinishLayout;
import com.zhiitek.liftcontroller.views.ToggleButton;
import com.zhiitek.liftcontroller.views.ToggleButton.OnToggleChanged;
import com.zhiitek.liftcontroller.views.VoiceSeekBar;
import com.zhiitek.liftcontroller.views.VoiceSeekBar.OnSeekBarChangeListener;

/**
 * 设备测试页面
 * 
 * @author ZhaoFan
 *
 */
public class DeviceTestActivity extends BaseActivity implements OnClickListener{
	
	private TextView tvAutoCheck;
	
	private ToggleButton tbScreen, tbBluetooth;
	
	private StateChangeButton scbSpeaker, scbMic, scbWebcam, scbWatchdog;
	
	private SocketConnection conn;
	
	//测试的时候从设备终端获取的文件存放的路径
	private static final String FILE_DIR = "LiftController/DevicesTest";

	//开始录音or停止录音并播放录音
	private boolean isStartTestMic = false;
	
	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_devicetest);
	}

	@Override
	protected void findViewById() {
		tvAutoCheck = (TextView) findViewById(R.id.tv_autocheck);
		tbScreen = (ToggleButton) findViewById(R.id.tb_screen);
		tbBluetooth = (ToggleButton) findViewById(R.id.tb_bluetooth);
		scbSpeaker = (StateChangeButton) findViewById(R.id.scb_speaker);
		scbMic = (StateChangeButton) findViewById(R.id.scb_mic);
		scbWebcam = (StateChangeButton) findViewById(R.id.scb_webcam);
		scbWatchdog = (StateChangeButton) findViewById(R.id.scb_watchdog);
	}
	
	@Override
	protected void setListener() {
		tvAutoCheck.setOnClickListener(this);
		scbSpeaker.setOnClickListener(this);
		scbMic.setOnClickListener(this);
		scbWebcam.setOnClickListener(this);
		scbWatchdog.setOnClickListener(this);
		tbScreen.setOnToggleChanged(new OnToggleChanged() {
			
			@Override
			public void onToggle(boolean on) {
				transportTestScreen(on);
			}
			
		});
		tbBluetooth.setOnToggleChanged(new OnToggleChanged() {
			
			@Override
			public void onToggle(boolean on) {
				transportTestBlueTooth(on);
			}

		});
	}
	
	@Override
	protected void dealProcessLogic() {
		setTitleBar("设备测试", null);
		SwipeFinishLayout.attachToActivity(this);
		conn = new SocketConnection(this);
		
		queryDevicesInfo();
		tbScreen.setToggleOn();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		conn.shutdown();
	}
	
	/**
	 * 查询设备的信息【目前暂且只查询蓝牙是否开启的信息】
	 */
	private void queryDevicesInfo() {
		conn.post(createSocketJsonParameters(10, null), new SocketDataCallback() {
			
			@Override
			public void onSuccess(String result) {
				resolverQueryDevicesInfo(result);
			}

			@Override
			public void onFailure(int errorCode) {
				showToast("设备信息查询失败");
			}
		});
	}
	
	/**
	 * 解析查询设备信息操作时返回的JSON字符串,并根据字符串修改UI
	 * @param result
	 */
	private void resolverQueryDevicesInfo(String result) {
		if (!TextUtils.isEmpty(result)) {
			JSONObject resultJson = null;
			try {
				resultJson = new JSONObject(result);
				if(resultJson != null && !resultJson.isNull(AppConstant.KEY_SOCKET_CMD) && checkCmd(resultJson.getInt(AppConstant.KEY_SOCKET_CMD))){
					JSONObject dataJson = resultJson.getJSONObject(AppConstant.KEY_SOCKET_DATA);
					if(dataJson.getString("bluetooth_enable").equals("true")) {
						tbBluetooth.setToggleOn();
					} else {
						tbBluetooth.setToggleOff();
					}
					return ;
				}
				showToast("设备信息查询失败");
			} catch (JSONException e) {
				showToast("设备信息查询失败");
			}
			return ;
		} 
		showToast("设备信息查询失败");
	}

	/**
	 * 解析JSON字符串 返回一个存储终端设备扬声器的最大声音和当前声音
	 * @param result
	 * @return
	 */
	private int[] resolverJsonResult(String result) {
		int[] voiceArray = null;
		try {
			JSONObject resultJson = new JSONObject(result);
			if(resultJson != null && !resultJson.isNull(AppConstant.KEY_SOCKET_CMD) && checkCmd(resultJson.getInt(AppConstant.KEY_SOCKET_CMD))){
				JSONObject jsonResult = resultJson.getJSONObject(AppConstant.KEY_SOCKET_DATA);
				//存放设备扬声器的最大声音和当前声音
				voiceArray = new int[2];
				voiceArray[0] = Integer.parseInt(jsonResult.getString("max_volume"));
				voiceArray[1] = Integer.parseInt(jsonResult.getString("current_volume"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return voiceArray;
	}
	
	/**
	 * 显示音量设置的PopupWindow
	 * @throws JSONException 
	 * @throws NumberFormatException 
	 * 
	 */
	private void showVolumePopup(int[] voiceArray) {
		View popouView = View.inflate(this, R.layout.popup_test_volume, null);
		final VoiceSeekBar vsVolume = (VoiceSeekBar) popouView.findViewById(R.id.vs_volume);
		vsVolume.setMaxLevel(voiceArray[0]);
		vsVolume.setCurrentLevel(voiceArray[1]);
		vsVolume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(VoiceSeekBar seekBar) {
				try {
					JSONObject jsonData = new JSONObject();
					jsonData.put("current_volume", seekBar.getCurrentLevel());
					conn.post(createSocketJsonParameters(12, jsonData), null);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onStartTrackingTouch(VoiceSeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(VoiceSeekBar seekBar, int currentLevel) {}
		});
		final PopupWindow popup = new PopupWindow(popouView, DensityUtil.getScreenWidth(this), DensityUtil.getScreenHeight(this) / 4);

		// 设置PopupWindow可以获取焦点[在失去焦点的时候,会自动关闭PopupWindow]
		popup.setFocusable(true);
		popup.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#dfdfdf")));
		// 设置PopupWindow显示和隐藏动画
		popup.setAnimationStyle(R.style.volume_seekbar_dlg_anim);
		// 设置PopupWindow显示的位置
		popup.showAtLocation(scbSpeaker, Gravity.BOTTOM, 0, 0);
		popup.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss() {
				scbSpeaker.reset();//还原按钮的状态
				conn.post(createSocketJsonParameters(19, null), null);//关闭设备终端的声音播放
			}
		});
	}
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_autocheck:
			transportTestAutoCheck();
			break;
		case R.id.scb_speaker:
			transportTestSpeaker();
			break;
		case R.id.scb_mic:
			transportTestMic();
			break;
		case R.id.scb_webcam:
			transportTestWebcam();
			break;
		case R.id.scb_watchdog:
			transportTestWatchdog();
			break;
		}
	}
	
	/**
	 *  与终端设备通信,测试看门狗程序是否正常
	 */
	private void transportTestWatchdog() {
		DialogUtil.showConfirmDialog(this, "注意", "看门狗测试必须在开机启动5分钟后.测试时,会重启设备且返回到上一页并断开与设备的连接,请确认为最后测试项!", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				conn.post(createSocketJsonParameters(20, null), null);
				clearConnectDevicesTime();
				backActivity();
				showToast("请在设备");
			}
		}, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
	}

	/**
	 * 与终端设备通信,测试蓝牙是否能够正常开启与关闭
	 * @param on
	 */
	private void transportTestBlueTooth(boolean on) {
		try {
			JSONObject jsonData = new JSONObject();
			jsonData.put("bluetooth_enable", on);
			conn.post(createSocketJsonParameters(11, jsonData), null);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 与终端设备通信,测试屏幕背光
	 * @param on
	 */
	private void transportTestScreen(boolean on) {
		try {
			JSONObject jsonData = new JSONObject();
			jsonData.put("isEnabled", on);
			conn.post(createSocketJsonParameters(16, jsonData), null);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 麦克风测试
	 */
	private void transportTestMic() {
		JSONObject jsonData = new JSONObject();
		try {
			if (isStartTestMic = !isStartTestMic) {
				scbMic.setText("请说话");
				jsonData.put("startOrStop", "start");
			} else {
				scbMic.reset();
				jsonData.put("startOrStop", "stop");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		conn.post(createSocketJsonParameters(17, jsonData), null);
		
	}

	/**
	 * 摄像头测试
	 */
	private void transportTestWebcam() {
		scbWebcam.setText("加载中");
		final CustomProgressDialog progressDialog = new CustomProgressDialog(this, R.style.loading_dialog);
		conn.setFileDirectory(FILE_DIR);
		progressDialog.show();
		conn.post(createSocketJsonParameters(18, null), new SocketDataCallback() {
			
			@Override
			public void onSuccess(String result) {
				scbWebcam.reset();
				String photoFilePath = String.format("%s/%s/%s", Environment.getExternalStorageDirectory().getAbsolutePath(), FILE_DIR, result);
				progressDialog.setImage(BitmapFactory.decodeFile(photoFilePath));
			}
			
			@Override
			public void onFailure(int errorCode) {
				scbWebcam.reset();
				progressDialog.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.icon_no_image));
			}
		});
	}

	/**
	 * 扬声器测试
	 */
	private void transportTestSpeaker() {
		scbSpeaker.startCircling();
		JSONObject jsonData = new JSONObject();
		try {
			jsonData.put("isPlayMusic", true);
			conn.post(createSocketJsonParameters(10, jsonData), new SocketDataCallback() {
				
				@Override
				public void onSuccess(String result) {
					int[] voiceArray = resolverJsonResult(result);
					if (voiceArray != null) {
						showVolumePopup(voiceArray);
					}
				}
				@Override
				public void onFailure(int errorCode) {}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 自动检测测试
	 */
	private void transportTestAutoCheck() {
		final CustomProgressDialog progressDialog = new CustomProgressDialog(this, R.style.loading_dialog);
		progressDialog.show();
		conn.post(createSocketJsonParameters(15, null), new SocketDataCallback() {
			
			@Override
			public void onSuccess(String result) {
				progressDialog.dismiss();
				showDialog(resolverTestAutoCheckJson(result));
			}

			@Override
			public void onFailure(int errorCode) {
				progressDialog.dismiss();
				showDialog("与设备终端通信异常");
			}
		});
	}
	
	private void showDialog(String msg) {
		DialogUtil.showDialog(this, msg);
	}
	
	private String resolverTestAutoCheckJson(String result) {
		String message = null;
		if (!TextUtils.isEmpty(result)) {
			try {
				JSONObject resultJson = new JSONObject(result);
				JSONObject dataJson = resultJson.getJSONObject(AppConstant.KEY_SOCKET_DATA);
				message = dataJson.getString(AppConstant.KEY_SOCKET_EMESSAGE);
			} catch (JSONException e) {
				message = "未知错误";
			}
		} else {
			message = "与设备终端通信异常";
		}
		return message;
	}
	
	

}
