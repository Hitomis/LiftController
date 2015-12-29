package com.zhiitek.liftcontroller.application;

import android.app.Application;
import cn.jpush.android.api.JPushInterface;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MyApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();

		initImageLoader();

		initJPush();

	}

	private void initJPush() {
		JPushInterface.setDebugMode(true);
		JPushInterface.init(this);
	}

	private void initImageLoader() {
		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this).writeDebugLogs().build();
		ImageLoader.getInstance().init(configuration);
	}
	
}
