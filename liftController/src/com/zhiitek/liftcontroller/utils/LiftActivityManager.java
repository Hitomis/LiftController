package com.zhiitek.liftcontroller.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

/**
 * <p>activity管理器</p>
 * 
 * <font color = 'red'>此Activity管理器保证：凡是被纳入到管理器中的Activity实例有且仅有一个</font>
 * 
 * <p>单例模式</p>
 * 
 * @author ZhaoFan
 *
 */
public class LiftActivityManager {

	private final ConcurrentHashMap<String, Activity> activityMap = new ConcurrentHashMap<String, Activity>();

	private static class SingletonHolder {
		public final static LiftActivityManager instance = new LiftActivityManager();
	}

	public static LiftActivityManager getInstance() {
		return SingletonHolder.instance;
	}

	public Map<String, Activity> getActivityMap(){
		return activityMap;
	}

	/**
	 * 添加Activity实例到Activity管理器当中
	 * 
	 * @param activity
	 */
	public void addActivity(Activity activity) {
		String actName = activity.getClass().getSimpleName();
		if(activityMap.containsKey(actName)){
			activityMap.get(actName).finish();
		}
		activityMap.put(actName, activity);
	}

	/**
	 * 从Activity管理器当中移除该activity
	 * 
	 * @param actName
	 */
	public void removeActivity(Activity activity) {
		activityMap.remove(activity.getClass().getSimpleName());
	}
	
	/**
	 * 管理器中是否存在simpleActivityName的activity实例
	 * @param simpleActivityName
	 * @return
	 */
	public boolean isContainsActivity(String simpleActivityName){
		return activityMap.containsKey(simpleActivityName);
	}
	
	/**
	 * activity是否正在显示运行
	 * @param activity
	 */
	public boolean isActivityRunning(Activity activity) {
		boolean isRunning = false;
		Activity runningActivity = getRunningActivity(activity);
		if(runningActivity == activity){
			isRunning = true;
		}
		return isRunning;
	}
	
	/**
	 * 获取任务栈顶的Activity（当前正在显示的Activity）
	 * 
	 * @param context
	 * @return
	 */
	public Activity getRunningActivity(Context context) {
		Activity activity = null;
		ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		String qualifiedName = actManager.getRunningTasks(1).get(0).topActivity.getClassName();
		if(qualifiedName != null && qualifiedName.indexOf(".") != -1){
			String[] strs = qualifiedName.split("\\.");
			String simpleActivityName = strs[strs.length - 1];
			activity = activityMap.get(simpleActivityName);
		}
		return activity;
	}
	
	/**
	 * finish掉除exceptAct之外的所有Activity
	 * 
	 * @param exceptAct
	 */
	public void finishAllExcept(Activity exceptAct) {
		if (exceptAct != null) {
			List<String> actNameList = new ArrayList<String>();
			Set<String> actNameSet = activityMap.keySet();
			for(String actName : actNameSet){
				if(!actName.equals(exceptAct.getClass().getSimpleName())){
					activityMap.get(actName).finish();
					actNameList.add(actName);
				}
			}
			for(String actName : actNameList){
				activityMap.remove(actName);
			}
		}
	}
	
	/**
	 * finish掉所有Activity
	 */
	public void finishAll() {
		Set<String> actNameSet = activityMap.keySet();
		for (String actName : actNameSet) {
			activityMap.get(actName).finish();
		}
		activityMap.clear();
	}

}
