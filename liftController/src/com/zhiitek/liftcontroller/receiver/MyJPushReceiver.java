package com.zhiitek.liftcontroller.receiver;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import cn.jpush.android.api.JPushInterface;

import com.zhiitek.liftcontroller.activity.AlarmDetailsActivity;
import com.zhiitek.liftcontroller.model.AlarmInfo;

public class MyJPushReceiver extends BroadcastReceiver {


	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(action)) { // action:用户打开自定义通知栏
			Bundle bundle = intent.getExtras();
			String extraJson = bundle.getString(JPushInterface.EXTRA_EXTRA);
			Intent i = new Intent(context, AlarmDetailsActivity.class);
			//使用extraJson 构造一个AlarmInfo对象 传递到告警详情页面
			i.putExtra("alarmInfo", parseAlarmJson(extraJson));
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
	}

	private AlarmInfo parseAlarmJson(String resultStr) {
		final String key = "fault";
		resultStr = result2JSONStr(resultStr, key);
		AlarmInfo alarmInfo = new AlarmInfo();
		try {
			JSONObject dataJson = new JSONObject(resultStr);
			alarmInfo.setLiftNo(dataJson.getString("liftNo"));
			alarmInfo.setLiftName(dataJson.getString("liftName"));
			alarmInfo.setLiftAddress(dataJson.getString("liftAdd"));
			alarmInfo.setLiftCommunity(dataJson.getString("blockName"));
			alarmInfo.setAlarmCode(dataJson.getString("faultNo"));
			alarmInfo.setAlarmData(dataJson.getString("faultData"));
			alarmInfo.setAlarmCount(dataJson.getString("faultCount"));
			alarmInfo.setAlarmTime(dataJson.getString("faultTime"));
			alarmInfo.setAlarmLevel(dataJson.getString("faultLevel"));
			alarmInfo.setAlarmName(dataJson.getString("faultName"));
			alarmInfo.setAlarmPhoto(dataJson.getString("faultPhoto"));
		} catch (JSONException e) {
			alarmInfo = null;
			e.printStackTrace();
		}
		return alarmInfo;
	}

	private String result2JSONStr(String resultStr, String prefix) {
		int index = resultStr.indexOf(prefix);
		String JsonStr = null;
		if (index != -1) {
			JsonStr = resultStr.substring(index + prefix.length() + 3, resultStr.length() - 2);
			JsonStr = JsonStr.replace("\\", "");
		}
		return JsonStr;
	}

}
