package com.zhiitek.liftcontroller.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;

import com.zhiitek.liftcontroller.components.ImageHelper;
import com.zhiitek.liftcontroller.utils.AppConstant;
import com.zhiitek.liftcontroller.utils.AppUtil;

import java.io.File;
import java.util.Date;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

public class ControllerService extends Service {
	
	private SharedPreferences sharedPref;
	
	/** 设置别名 */
	private static final int MSG_SET_ALIAS = 1001;
	
	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
	@Override
	    public void handleMessage(android.os.Message msg) {
	        super.handleMessage(msg);
	        switch (msg.what) {
	            case MSG_SET_ALIAS:
	            // 调用 JPush 接口来设置别名。
				JPushInterface.setAliasAndTags(getApplicationContext(), (String) msg.obj, null, mAliasCallback);
	            break;
	        }
	    }   
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public void onCreate() {
		sharedPref = getSharedPreferences(getApplicationInfo().packageName, Context.MODE_PRIVATE);
		startBackgroundTask();
		setJPushAlias();
	}

	/**
	 * 设置极光推送别名
	 */
	private void setJPushAlias() {
		//查询是否已经设置过别名
		boolean isSet = sharedPref.getBoolean(AppConstant.KEY_JPUSH_SET_ALIAS, false);
		if (!isSet) { //没有设置
			String username = sharedPref.getString(AppConstant.KEY_USER_ID, null);
			if (username != null) {
				mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS, username));
			}
		}
	}

	private void startBackgroundTask() {
		new Thread(new ImageClearTask()).start();
	}
	
	/**
	 * 设置别名后用于接收返回状态的Callback
	 */
	private final TagAliasCallback mAliasCallback = new TagAliasCallback() {
	    @Override
	    public void gotResult(int code, String alias, Set<String> tags) {
	        switch (code) {
	        case 0:
	            // 建议这里往 SharePreference 里写一个成功设置的状态。成功设置一次后，以后不必再次设置了。
	        	Editor editor = sharedPref.edit();
	        	editor.putBoolean(AppConstant.KEY_JPUSH_SET_ALIAS, true);
	        	editor.commit();
	            break;
	        case 6002:
	            // 延迟 60 秒来调用 Handler 设置别名
	            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
	            break;
	        }
	    }
	};

	/**
	 * 清理缓存图片
	 */
	public class ImageClearTask implements Runnable {
		public void run() {
			String lastClearTime = sharedPref.getString(AppConstant.KEY_LAST_CLEAR_DATE, AppUtil.getCurrentTimeStr());
			float days = (new Date().getTime() - AppUtil.stringToTime(lastClearTime).getTime()) / (1000.f * 3600 * 24);
			if (days > 7) {// 超过一周执行图片缓存清理和录音文件清理
				ImageHelper.getInstance().clearCache();
				clearRecordingFiles();
				Editor editor = sharedPref.edit();
				editor.putString(AppConstant.KEY_LAST_CLEAR_DATE, AppUtil.getCurrentTimeStr());
				editor.commit();
			}
		};
	}

	/**
	 * 清理录音文件
	 */
	private void clearRecordingFiles() {
		String SAVE_RECORDING_PATH = String.format("%s/%s", Environment.getExternalStorageDirectory().getAbsolutePath(), "LiftController/Recording");
		File dir = new File(SAVE_RECORDING_PATH);
		if (dir.exists()) {
			File[] fileList = dir.listFiles();
			if (fileList != null && fileList.length > 0) {
				for(File f : fileList) {
					f.delete();
				}
			}
		}
	}

}
