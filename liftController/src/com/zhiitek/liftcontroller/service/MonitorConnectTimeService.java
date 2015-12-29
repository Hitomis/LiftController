package com.zhiitek.liftcontroller.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.zhiitek.liftcontroller.utils.AppUtil;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * 监测设备维护中，wifi连接时间
 * @author Administrator
 *
 */
public class MonitorConnectTimeService extends Service {

	ScheduledExecutorService  newScheduled;
	
	//超时回调接口
	public OnTimeOutListener onTimeOutListener;

	@Override
	public IBinder onBind(Intent intent) {
		return new MonitorConnectTimeBinder();
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		return true;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		startTimeOutSchedule();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		shutdownTimeOutSchedule();
	}
	
	public void setOnTimeOutListener(OnTimeOutListener listener) {
		this.onTimeOutListener = listener;
	}
	
	/**
	 * 每隔规定时间，检测连接是否超时
	 */
	private void startTimeOutSchedule() {
		if (newScheduled != null) {
			newScheduled.shutdownNow();
		}
		newScheduled = Executors.newSingleThreadScheduledExecutor();
		newScheduled.scheduleAtFixedRate(new CheckIsTimeOutTask(), 5, AppUtil.CHECK_TIME_OUT_SECONDS, TimeUnit.SECONDS);
	}
	
	private void shutdownTimeOutSchedule() {
		if (newScheduled != null) {
			newScheduled.shutdownNow();
		}
	}
	
	public class CheckIsTimeOutTask implements Runnable{
		@Override
		public void run() {
			if (!AppUtil.isConnectValid(getApplicationContext())) {
				if (onTimeOutListener != null) {
					onTimeOutListener.onTimeOut();
				}
			}
		}
	}
	
	public interface OnTimeOutListener {
		/**
		 * 检测到超时的回调接口
		 */
		public void onTimeOut();
	}
	
	public class MonitorConnectTimeBinder extends Binder{  
        /** 
         * 获取当前Service的实例 
         * @return 
         */  
        public MonitorConnectTimeService getService(){  
            return MonitorConnectTimeService.this;  
        }  
    }

}
