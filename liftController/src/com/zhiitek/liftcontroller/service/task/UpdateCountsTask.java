package com.zhiitek.liftcontroller.service.task;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.zhiitek.liftcontroller.components.net.NetWorkCons;
import com.zhiitek.liftcontroller.components.net.NetWorkHelper;
import com.zhiitek.liftcontroller.components.net.client.NetCallback;
import com.zhiitek.liftcontroller.model.MenuItemInfo;
import com.zhiitek.liftcontroller.utils.AppConstant;

import de.greenrobot.event.EventBus;

public class UpdateCountsTask{

	private Context context;

	public UpdateCountsTask(Context context) {
		this.context = context;
	}
	
	/**
	 * 更新任务总数和告警总数
	 */
	public void updateCount() {
		NetWorkHelper netHelper = NetWorkHelper.getInstance();
		netHelper.init(context);
		JSONObject jsonParams = null;
		SharedPreferences sharedPreferences = context.getSharedPreferences(context.getApplicationInfo().packageName, Context.MODE_PRIVATE);
		try {
			jsonParams = netHelper.initJsonParameters(NetWorkCons.CMD_HTTP_GET_ALARM_AND_TASK_COUNT);
			netHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_USERID, sharedPreferences.getString(AppConstant.KEY_USER_ID, ""), jsonParams);
			netHelper.execHttpNetWithoutPrompt(NetWorkCons.getTaskAndAlarmCountUrl, jsonParams, new NetCallback() {
				
				@Override
				public void callback(JSONObject resultJson) {
					try {
						if (resultJson != null) {
							int alarmCount = resultJson.getInt(NetWorkCons.JSON_KEY_FAULTCOUNT);
							int taskCount = resultJson.getInt(NetWorkCons.JSON_KEY_TASKCOUNT);
							saveCount(context, alarmCount, taskCount);
						}
					} catch (JSONException e) {
					}
				}
			});
				
		} catch (JSONException e) {
		}
	}
	
	/**
	 * 在SharedPreferences中存放告警数量和任务数量,并发送通知
	 * @param context
	 * @param alarmCount
	 * @param taskCount
	 */
	private void saveCount(Context context, int alarmCount, int taskCount) {
		SharedPreferences sharedPreference = context.getSharedPreferences(context.getApplicationInfo().packageName, Context.MODE_PRIVATE);
		Editor edit = sharedPreference.edit();
		edit.putInt(AppConstant.KEY_ALARM_COUNT, alarmCount);
		edit.putInt(AppConstant.KEY_TASK_COUNT, taskCount);
		edit.commit();
		EventBus.getDefault().post(new MenuItemInfo());
	}

}
