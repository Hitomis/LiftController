package com.zhiitek.liftcontroller.activity;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.components.ImageHelper;
import com.zhiitek.liftcontroller.components.net.NetWorkCons;
import com.zhiitek.liftcontroller.model.AlarmInfo;
import com.zhiitek.liftcontroller.utils.AppUtil;
import com.zhiitek.liftcontroller.utils.SoundService;
import com.zhiitek.liftcontroller.views.StateChangeButton;
import com.zhiitek.liftcontroller.views.SwipeFinishLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;

public class AlarmDetailsActivity extends BaseActivity{
	
	private ImageView ivShowPhoto;
	
	private ImageHelper imageHelper;

	private StateChangeButton scbDownAndPlayRecording;

	private AlarmInfo alarmInfo;

	/** 录音文件是否已下载 */
	private boolean isRecordingDownloaded = false;

	private SoundService soundService = SoundService.getInstance();
	
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

	/** 告警的录音文件 */
	private File recordingFile;

	/** 保存录音文件夹路径 */
	private String SAVE_RECORDING_PATH = String.format("%s/%s", Environment.getExternalStorageDirectory().getAbsolutePath(), "LiftController/Recording");

	/** 保存录音文件成功 */
	private static final int SAVE_RECORDING_SUCCESS = 0;
	/** 保存录音文件失败 */
	private static final int SAVE_RECORDING_FAILURE = 1;
	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_alarm_details);
	}

	@Override
	protected void findViewById() {
		setTitleBar("告警详情", null);
		ivShowPhoto = (ImageView) findViewById(R.id.iv_show_alarm_photo);
		scbDownAndPlayRecording = (StateChangeButton) findViewById(R.id.scb_download_and_play_recording);
	}

	@Override
	protected void setListener() {

		scbDownAndPlayRecording.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isRecordingDownloaded) {
					scbDownAndPlayRecording.startCircling();
					String url = String.format(NetWorkCons.downloadAlarmRecordingUrl, getUserId(), alarmInfo.getLiftNo(), sdf.format(AppUtil.stringToTime(alarmInfo.getAlarmTime())));
					String path = String.format("%s/%s_%s.mp3", SAVE_RECORDING_PATH, alarmInfo.getLiftNo(), sdf.format(AppUtil.stringToTime(alarmInfo.getAlarmTime())));
					downloadRecording(url, path);
				} else {
					scbDownAndPlayRecording.startCircling();
					try {
						soundService.playVoice(recordingFile.getAbsolutePath(), new MediaPlayer.OnCompletionListener() {
							@Override
							public void onCompletion(MediaPlayer mp) {
								scbDownAndPlayRecording.reset();
								scbDownAndPlayRecording.setText("播放");
							}
						});
					} catch (Exception e) {
						showToast("录音文件已损坏,无法播放!");
						scbDownAndPlayRecording.reset();
						scbDownAndPlayRecording.setText("播放");
					}
				}
			}
		});
	}

	@Override
	protected void dealProcessLogic() {
		SwipeFinishLayout.attachToActivity(this);
		imageHelper = ImageHelper.getInstance();
		imageHelper.init(this);
		soundService.init(this);
		alarmInfo = (AlarmInfo) getIntent().getSerializableExtra("alarmInfo");
		if (alarmInfo != null) {
			setContent(alarmInfo);
		}
	}
	
	private void setContent(AlarmInfo alarmInfo) {
		((TextView)findViewById(R.id.tv_lift_no)).setText(alarmInfo.getLiftNo());
		((TextView)findViewById(R.id.tv_lift_name)).setText(alarmInfo.getLiftName());
		((TextView)findViewById(R.id.tv_lift_community)).setText(alarmInfo.getLiftCommunity());
		if (!alarmInfo.getLiftAddress().equals("null")) {
			((TextView)findViewById(R.id.tv_lift_address)).setText(alarmInfo.getLiftAddress());
		}
		((TextView)findViewById(R.id.tv_alarm_time)).setText(alarmInfo.getAlarmTime());
		((TextView)findViewById(R.id.tv_alarm_name)).setText(alarmInfo.getAlarmName());
		TextView floorView = ((TextView)findViewById(R.id.tv_alarm_floor));
		try {
			JSONObject jsonObject = new JSONObject(alarmInfo.getAlarmData());
			if (!jsonObject.isNull("floor")) {
				floorView.setText(jsonObject.getString("floor"));
			} else {
				floorView.setText("未知");
			}
		} catch (JSONException e) {
			// Special [faultdata 的值可能是json字符串，也有可能是普通字符串]
			floorView.setText("未知");
		}
		File dir = new File(SAVE_RECORDING_PATH);
		if (!dir.exists()) {
			dir.mkdir();
		}
		if ("1".equals(alarmInfo.getAlarmAudio())) { // 有录音文件
			scbDownAndPlayRecording.setVisibility(View.VISIBLE);
			String path = String.format("%s/%s_%s.mp3", SAVE_RECORDING_PATH, alarmInfo.getLiftNo(), sdf.format(AppUtil.stringToTime(alarmInfo.getAlarmTime())));
			File file = new File(path);
			if (file.exists()) { // 录音文件已经下载过
				isRecordingDownloaded = true;
				scbDownAndPlayRecording.setText("播放");
				recordingFile = file;
			} else {
				scbDownAndPlayRecording.setText("下载");
			}
		}
		if ("1".equals(alarmInfo.getAlarmPhoto())) { // 有告警图片
			String uri = String.format(NetWorkCons.downloadAlarmPhotoUrl,
					getUserId(), 
					alarmInfo.getLiftNo(), 
					sdf.format(AppUtil.stringToTime(alarmInfo.getAlarmTime())), 
					3);
			imageHelper.displayImage(uri, ivShowPhoto);
		}
	}

	/**
	 * 下载录音文件
	 * @param path
	 */
	private void downloadRecording(final String url, final String path) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				File saveFile = AppUtil.download(url, path);
				Message msg = Message.obtain();
				if (saveFile != null) {
					msg.what = SAVE_RECORDING_SUCCESS;
					recordingFile = saveFile;
				} else {
					msg.what = SAVE_RECORDING_FAILURE;
				}
				handler.sendMessage(msg);
			}
		}).start();
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			scbDownAndPlayRecording.reset();
			switch (msg.what) {
				case SAVE_RECORDING_SUCCESS:
					isRecordingDownloaded = true;
					scbDownAndPlayRecording.setText("播放");
					break;
				case SAVE_RECORDING_FAILURE:
					showToast("录音文件下载缺失!");
					scbDownAndPlayRecording.setText("下载");
					break;
			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		soundService.release();
	}
}
