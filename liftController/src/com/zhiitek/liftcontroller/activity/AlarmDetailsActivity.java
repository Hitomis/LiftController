package com.zhiitek.liftcontroller.activity;

import android.annotation.SuppressLint;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.components.ImageHelper;
import com.zhiitek.liftcontroller.components.net.NetWorkCons;
import com.zhiitek.liftcontroller.model.AlarmInfo;
import com.zhiitek.liftcontroller.utils.AppUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

public class AlarmDetailsActivity extends BaseActivity{
	
	private ImageView ivShowPhoto;
	
	private ImageHelper imageHelper;
	
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_alarm_details);
	}

	@Override
	protected void findViewById() {
		setTitleBar("告警详情", null);
		ivShowPhoto = (ImageView) findViewById(R.id.iv_show_alarm_photo);
	}

	@Override
	protected void setListener() {
	}

	@Override
	protected void dealProcessLogic() {
		imageHelper = ImageHelper.getInstance();
		imageHelper.init(this);
		AlarmInfo alarmInfo = (AlarmInfo) getIntent().getSerializableExtra("alarmInfo");
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
		if (!alarmInfo.getAlarmPhoto().equals("0")) { // 有告警图片
			String uri = String.format(NetWorkCons.downloadAlarmPhotoUrl,
					getUserId(), 
					alarmInfo.getLiftNo(), 
					sdf.format(AppUtil.stringToTime(alarmInfo.getAlarmTime())), 
					3);
			imageHelper.displayImage(uri, ivShowPhoto);
		}
	}
}
