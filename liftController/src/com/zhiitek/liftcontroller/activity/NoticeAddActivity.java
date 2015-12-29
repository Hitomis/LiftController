package com.zhiitek.liftcontroller.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.adapter.BaseAdapterHelper;
import com.zhiitek.liftcontroller.adapter.ViewHolder;
import com.zhiitek.liftcontroller.components.net.NetWorkCons;
import com.zhiitek.liftcontroller.components.net.client.NetCallback;
import com.zhiitek.liftcontroller.model.CommunityInfo;
import com.zhiitek.liftcontroller.model.LocalInfo;
import com.zhiitek.liftcontroller.utils.AppUtil;
import com.zhiitek.liftcontroller.utils.DensityUtil;
import com.zhiitek.liftcontroller.views.SpinnerEditText;
import com.zhiitek.liftcontroller.views.SpinnerEditText.PopupItemClickListenner;
import com.zhiitek.liftcontroller.views.wheelpicker.view.SimpleWheelChangeListener;
import com.zhiitek.liftcontroller.views.wheelpicker.view.WheelPicker;
import com.zhiitek.liftcontroller.views.wheelpicker.widget.WheelLocalPicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 
 * 通告发布页面  </br>
 * 
 * 我的设备 - 通告管理 - 通告发布
 * 
 * @author ZhaoFan
 *
 */
public class NoticeAddActivity extends BaseActivity implements OnClickListener {
	
	static final String DEFAULTINPUTSTR = "请选择";
	
	/** 通告播放的最短时间限度  */
	static final long MIN_PLAY_DIFF_TIME = 1000 * 60 * 60;
	
	/** 通告播放的起始时间不能早于当前时间的差额 */
	static final long MIN_START_DIFF_TIME = 1000 * 60 * 3;
	
	/** 片区通告类型  */
	static final int NOTICE_TYPE_AREA = 0;
	
	/** 小区通告类型  */
	static final int NOTICE_TYPE_COMMUNITY = 1;

	static final String BROADCAST_LOCAL = "key_local_receiver";
	
	/** 省市区数据集合  */
	private ArrayList<LocalInfo> localInfos;
	
	/** 小区数据集合  */
	private ArrayList<CommunityInfo> commuInfos;
	
	/** 通告类型[0 or 1] */
	private int noticeType;
	
	private final String[] noticeTypeArray = new String[]{"片区通知", "小区通知"};
	
	private TextView tvSendTarget, tvStartTime, tvEndTime;
	
	private EditText etContent;
	
	private Button btnSend;
	
	private LinearLayout linlayNotceType;
	
	private SpinnerEditText setNoticeType;

	private LocalInfoReceiver localInfoReceiver;
	
	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_notice_add);
	}

	@Override
	protected void findViewById() {
		tvSendTarget = (TextView) findViewById(R.id.tv_send_target);
		tvStartTime = (TextView) findViewById(R.id.tv_start_time);
		tvEndTime = (TextView) findViewById(R.id.tv_end_time);
		etContent = (EditText) findViewById(R.id.et_content);
		btnSend = (Button) findViewById(R.id.btn_sned);
		linlayNotceType = (LinearLayout) findViewById(R.id.linlay_notice_type);
		setNoticeType = (SpinnerEditText) findViewById(R.id.set_notice_type);
	}

	@Override
	protected void setListener() {
		tvSendTarget.setOnClickListener(this);
		tvStartTime.setOnClickListener(this);
		tvEndTime.setOnClickListener(this);
		btnSend.setOnClickListener(this);
		setNoticeType.setOnClickItemListenner(new PopupItemClickListenner() {

			@Override
			public void onClick(int position) {
				noticeType = position;
				resetSendTarget();
			}
		});
	}

	@Override
	protected void dealProcessLogic() {
		setTitleBar("通告发布", null);
		
		initNoticeTypeCheck();
		
		displayNoticeType();
		
		downloadGisInfo();

		registerLocalBroadcast();
	}

	private void registerLocalBroadcast() {
		localInfoReceiver = new LocalInfoReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(getPackageName() + BROADCAST_LOCAL);
		registerReceiver(localInfoReceiver, intentFilter);
	}

	/**
	 * 重置发送目标字段
	 */
	private void resetSendTarget() {
		tvSendTarget.setTag(null);
		tvSendTarget.setText(DEFAULTINPUTSTR);
	}

	private void initNoticeTypeCheck() {
		setNoticeType.setAdapter(new BaseAdapterHelper<String>(this, Arrays.asList(noticeTypeArray), R.layout.popup_spinner_item) {

			@Override
			public void convert(ViewHolder viewHolder, String item) {
				viewHolder.setText(R.id.tv_item_name, item);
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (localInfoReceiver != null) {
			unregisterReceiver(localInfoReceiver);
		}
	}

	/**
	 * 从服务区获取省市区和小区地理信息
	 */
	private void downloadGisInfo() {
		try {
			JSONObject jsonParams = netWorkHelper.initJsonParameters(NetWorkCons.CMD_HTTP_GET_COMMUNITY_LIST);
			netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_USERID, getUserId(), jsonParams);
			netWorkHelper.execHttpNet(NetWorkCons.downloadCommunityUrl, jsonParams, new NetCallback() {
				
				@Override
				public void callback(JSONObject resultJson) {
					if (resultJson != null) {
						localInfos = parseLocalInfos(resultJson);
						commuInfos = paresCommuInfos(resultJson);
					}
				}

			});
		} catch (JSONException e) {
		}
	}

	/**
	 * 管理员角色可以发片区和小区两种类型通告->将类型切换操作栏显示出来</br>
	 * 否则就是物业角色->物业只能发送小区类型通告
	 */
	private void displayNoticeType() {
		if (TYPE_USER_ADMINISTRATOR == getUserType()) { // 管理员 [可以发布片区和小区两种类型通告]
			linlayNotceType.setVisibility(View.VISIBLE);
			noticeType = NOTICE_TYPE_AREA;
			setNoticeType.setText(noticeTypeArray[0]);//默认选中片区类型
		} else { // [只能发送小区类型通告]
			noticeType = NOTICE_TYPE_COMMUNITY;
			setNoticeType.setText(noticeTypeArray[1]);//默认选中小区类型
		}
	}
	
	/**
	 * 解析小区gis信息
	 * @param resultJson
	 * @return
	 */
	private ArrayList<CommunityInfo> paresCommuInfos(JSONObject resultJson) {
		ArrayList<CommunityInfo> commuInfoList = new ArrayList<CommunityInfo>();
		try {
			JSONArray jsonArray = resultJson.getJSONArray("gisList");
			CommunityInfo commuInfo;
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				commuInfo = new CommunityInfo();
				commuInfo.setCommuNo(jsonObject.getString("blockNo"));
				commuInfo.setCommuName(jsonObject.getString("blockName"));
				commuInfo.setCommuCode(jsonObject.getString("areaCode"));
				commuInfo.setCommuAddress(jsonObject.getString("address"));
				commuInfoList.add(commuInfo);
			}
		} catch (JSONException e) {
		}
		return commuInfoList;
	}
	
	/**
	 * 解析省市区gis信息
	 * @param resultJson
	 * @return
	 */
	private ArrayList<LocalInfo> parseLocalInfos(JSONObject resultJson) {
		ArrayList<LocalInfo> localInfoList = new ArrayList<LocalInfo>();
		try {
			JSONArray jsonArray;
			if (resultJson.optJSONArray("areaList") != null) {
				jsonArray = resultJson.getJSONArray("areaList");
			} else {
				jsonArray = resultJson.getJSONArray("children");
			}
			LocalInfo localInfo;
			for (int i = 0; i < jsonArray.length(); i++) {
				localInfo = new LocalInfo();
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				localInfo.setCode(jsonObject.getString("code"));
				localInfo.setName(jsonObject.getString("name"));
				localInfo.setParent(jsonObject.getString("parent"));
				int layer = jsonObject.getInt("layer");
				localInfo.setLayer(jsonObject.getInt("layer"));
				if (layer < 3) {
					localInfo.setChildren(parseLocalInfos(jsonObject));
				}
				localInfoList.add(localInfo);
			}
		} catch (JSONException e) {
		}
		return localInfoList;
	}

	private class LocalInfoReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String localStr = intent.getStringExtra(NoticeLocalActivity.CURR_CHOOSE_LOCAL_STR);
			String localCode = intent.getStringExtra(NoticeLocalActivity.CURR_CHOOSE_LOCAL_CODE);
			tvSendTarget.setText(localStr);
			tvSendTarget.setTag(localCode);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_send_target:
			if (noticeType == NOTICE_TYPE_AREA) {
//				showLocalPickerPopup();
				Intent intent = new Intent(this, NoticeLocalActivity.class);
				intent.putExtra(NoticeMainActivity.INTENT_EXTRA_LOCALINFO_LIFT, (Serializable) localInfos);
				startActivity(intent);
			} else {
				showCommuPickerPopup();
			}
			break;
		case R.id.tv_start_time:
			showDatePickerPopup(tvStartTime, "请选择");
			break;
		case R.id.tv_end_time:
			showDatePickerPopup(tvEndTime,"请选择");
			break;
		case R.id.btn_sned:
			prepareNoticeData();
			break;
		}
	}

	/**
	 * 验证通告发布字段的正确性,通告则发送通告,否则提示相应Toast
	 */
	private void prepareNoticeData() {
		Object sendTarget = tvSendTarget.getTag();
		if (sendTarget == null || TextUtils.isEmpty(sendTarget.toString())) {
			showToast("请选择发送目标");
			return ;
		}
		
		String startTime = tvStartTime.getText().toString();
		if (DEFAULTINPUTSTR.equals(startTime)) {
			showToast("请选择发布时间");
			return;
		}
		
		long startDiffTime = new Date().getTime() - AppUtil.stringToTime(startTime).getTime();
		if (startDiffTime > MIN_START_DIFF_TIME) {
			showToast("通告发布时间不能早于当前时间");
			return;
		}
		
		
		String endTime = tvEndTime.getText().toString();
		if (DEFAULTINPUTSTR.equals(endTime)) {
			showToast("请选择截止时间");
			return ;
		}
		
		long diffTime = AppUtil.stringToTime(endTime).getTime() - AppUtil.stringToTime(startTime).getTime();
		if (diffTime < MIN_PLAY_DIFF_TIME) {//通告发布播放时间不能少于min_diff_time
			showToast("通告发布时间不能少于1小时");
			return;
		}
		
		String content = etContent.getText().toString().trim();
		if (TextUtils.isEmpty(content)) {
			showToast("通告内容不能为空");
			return ;
		}
		//与服务器通信提交表单发送通告
		sendNotice();
	}
	
	/**
	 * 发送通告
	 */
	private void sendNotice() {
		try {
			JSONObject jsonParams = netWorkHelper.initJsonParameters(NetWorkCons.CMD_HTTP_POST_NOTICE);
			netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_USERID, getUserId(), jsonParams);
			netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_NOTICE_POSTTIME, tvStartTime.getText().toString(), jsonParams);
			netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_NOTICE_LIFETIME, tvEndTime.getText().toString(), jsonParams);
			netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_NOTICE_TYPE, noticeType, jsonParams);
			netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_NOTICE_TARGET, tvSendTarget.getTag().toString(), jsonParams);
			netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_NOTICE_CONTENT, etContent.getText().toString(), jsonParams);
			//解决选择省市区信息后context被修改的bug
			netWorkHelper.execHttpNet(this, NetWorkCons.postNoticeUrl, jsonParams, new NetCallback() {
				
				@Override
				public void callback(JSONObject resultJson) {
					try {
						if (resultJson != null) {
							showToast(resultJson.getString(NetWorkCons.JSON_KEY_EMSG));
							finish();
						}
					} catch (JSONException e) {
					}
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}		
	}

	private List<String> getCommuNameList() {
		List<String> commuNames = new ArrayList<String>();
		for (CommunityInfo commuInfo : commuInfos) {
			commuNames.add(commuInfo.getCommuName());
		}
		return commuNames;
	}
	
	private String getTargetNoByName(String name) {
		String targetNo = null;
		for (CommunityInfo commInfo : commuInfos) {
			if (commInfo.getCommuName().equals(name)) {
				targetNo = commInfo.getCommuNo();
				break;
			}
		}
		return targetNo;
	}
	
	private void showCommuPickerPopup() {
		View popouView = View.inflate(this, R.layout.popup_community_select, null);
		TextView tvCancel = (TextView) popouView.findViewById(R.id.tv_cancel);
		TextView tvConfirm = (TextView) popouView.findViewById(R.id.tv_confirm);
		final WheelPicker commuPicker = (WheelPicker) popouView.findViewById(R.id.wp_community_picker);
		commuPicker.setData(getCommuNameList());
		commuPicker.setTextColor(0xFF3F96C3);
		commuPicker.setOnWheelChangeListener(new SimpleWheelChangeListener() {
			@Override
			public void onWheelSelected(int index, String data) {
				tvSendTarget.setTag(getTargetNoByName(data));
				tvSendTarget.setText(data);
			}
		});
		
		final PopupWindow popup = new PopupWindow(popouView, 
				DensityUtil.getScreenWidth(this), 
				DensityUtil.getScreenHeight(this) / 3);
		// 设置PopupWindow可以获取焦点[在失去焦点的时候,会自动关闭PopupWindow]
		popup.setFocusable(true);
		popup.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#dfdfdf")));
		// 设置PopupWindow显示和隐藏动画
		popup.setAnimationStyle(R.style.volume_seekbar_dlg_anim);
		// 设置PopupWindow显示的位置
		popup.showAtLocation(tvSendTarget, Gravity.BOTTOM, 0, 0);
		tvCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				popup.dismiss();
			}
		});
		tvConfirm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				popup.dismiss();
			}
		});
	}

	private void showLocalPickerPopup() {
		View popouView = View.inflate(this, R.layout.popup_local_select, null);
		TextView tvCancel = (TextView) popouView.findViewById(R.id.tv_cancel);
		TextView tvConfirm = (TextView) popouView.findViewById(R.id.tv_confirm);
		final WheelLocalPicker localPicker = (WheelLocalPicker) popouView.findViewById(R.id.wdp_localpicker);
		localPicker.setLocalData(localInfos);
		localPicker.setTextColor(0xFF3F96C3);
		localPicker.setOnWheelChangeListener(new SimpleWheelChangeListener() {
			@Override
			public void onWheelSelected(int index, String data) {}
		});
		
//		String currData = tvSendTarget.getText().toString().trim();
//		if (defaultStr != null && !currData.equals(defaultStr)) {
//			localPicker.setCurrentLocal(localProvince, localCity, localDistrict);
//		}
		
		final PopupWindow popup = new PopupWindow(popouView, 
				DensityUtil.getScreenWidth(this), 
				DensityUtil.getScreenHeight(this) / 3);
		// 设置PopupWindow可以获取焦点[在失去焦点的时候,会自动关闭PopupWindow]
		popup.setFocusable(true);
		popup.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#dfdfdf")));
		// 设置PopupWindow显示和隐藏动画
		popup.setAnimationStyle(R.style.volume_seekbar_dlg_anim);
		// 设置PopupWindow显示的位置
		popup.showAtLocation(tvSendTarget, Gravity.BOTTOM, 0, 0);
		tvCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				popup.dismiss();
			}
		});
		tvConfirm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Map<String, String> checkLocalMap = localPicker.getData();
				Set<Entry<String, String>> entrySet = checkLocalMap.entrySet();
				for (Entry<String, String> entry : entrySet){
					tvSendTarget.setTag(entry.getKey());
					tvSendTarget.setText(entry.getValue());
				}
				popup.dismiss();
			}
		});
	} 
	
//	private String localProvince, localCity, localDistrict;
/*	private String splitLocalInfo(String localStr) {
		String[] localArray = localStr.split("_");
		if (localArray.length == 3) {
			localProvince = localArray[0];
			localCity = localArray[1];
			localDistrict = localArray[2];
		}
		return localStr.replace("_", ""); 
				
	}*/
	
}
