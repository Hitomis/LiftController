package com.zhiitek.liftcontroller.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.activity.AlarmDetailsActivity;
import com.zhiitek.liftcontroller.adapter.BaseAdapterHelper;
import com.zhiitek.liftcontroller.adapter.ViewHolder;
import com.zhiitek.liftcontroller.components.net.NetWorkCons;
import com.zhiitek.liftcontroller.components.net.client.NetCallback;
import com.zhiitek.liftcontroller.model.AlarmInfo;
import com.zhiitek.liftcontroller.service.task.UpdateCountsTask;
import com.zhiitek.liftcontroller.utils.AppConstant;
import com.zhiitek.liftcontroller.views.AlarmsInfoSearchDialog;
import com.zhiitek.liftcontroller.views.WaterStretchListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyAlarmFragment extends BaseFragment implements OnClickListener, WaterStretchListView.WaterStretchListener{
	
	private RelativeLayout relaySearch;
	
	private View view, parentView, titleView;
	
	/** 显示告警数据的listview */
	private WaterStretchListView waterStretchListView;
	/** 服务器下载的告警数据 */
	public List<AlarmInfo> alarmInfoList;
	
	BaseAdapterHelper<AlarmInfo> mAlarmsInfoAdapter;
	/** 搜索功能对话框 */
	private AlarmsInfoSearchDialog searchDialog;
	
	/** 更新告警的页码 */
	private int currentUpdatePage = 1;

	/** 当前最后一页数据的页码 */
	private int currentLastPage = 1;

	/** 告警总数 */
	private int totalAlarmCounts;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_alarm, container, false);
		return view;
	}

	@Override
	protected void findViewById() {
		relaySearch = (RelativeLayout) view.findViewById(R.id.relay_search);
		waterStretchListView = (WaterStretchListView) view.findViewById(R.id.wsl_alarms_data);
		parentView = getActivity().findViewById(R.id.linlay_conents);
		titleView = getActivity().findViewById(R.id.relay_titles);
	}

	@Override
	protected void setListener() {
		waterStretchListView.setOnItemClickListener(itemClickListener);
		waterStretchListView.setWaterStretchListViewListener(this);
		waterStretchListView.setPushLoadEnable(false);
		relaySearch.setOnClickListener(this);
	}
	
	private OnItemClickListener itemClickListener = new OnItemClickListener() {
		@SuppressWarnings("unchecked")
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			//点击一条告警数据时,跳转到AlarmDetailsActivity并将该告警数据传过去
			Intent intent = new Intent(mContext, AlarmDetailsActivity.class);
			intent.putExtra("alarmInfo", (AlarmInfo)waterStretchListView.getAdapter().getItem(position));
			startActivity(intent);
		}
	};

	/**
	 * 获取告警列表
	 */
	private void getAlarmList() {
		try {
			currentUpdatePage = 1;
			currentLastPage = 1;
			new UpdateCountsTask(getActivity()).updateCount();
			netWorkHelper.execHttpNet(NetWorkCons.getAlarmUrl, initGetAlarmsJsonParameter(), netCallback);
		} catch (JSONException e) {
			getAlarmListFailed();
		}
	}
	
	/**
	 * 获取告警列表,没有loading对话框
	 */
	private void getAlarmListWithoutPrompt() {
		try {
			new UpdateCountsTask(getActivity()).updateCount();
			netWorkHelper.execHttpNetWithoutPrompt(NetWorkCons.getAlarmUrl, initGetAlarmsJsonParameter(), netCallback);
		} catch (JSONException e) {
			getAlarmListFailed();
		}
	}
	
	NetCallback netCallback = new NetCallback() {
		@Override
		public void callback(JSONObject resultJson) {
			if (resultJson != null) {
				try {
					getAlarmListSuccess(resultJson);
				} catch (JSONException e) {
					getAlarmListFailed();
				}
			} else {
				getAlarmListFailed();
			}
		}
	};
	
	private JSONObject initGetAlarmsJsonParameter() throws JSONException {
		JSONObject jsonParams = netWorkHelper.initJsonParameters(NetWorkCons.CMD_HTTP_GET_DETAILS_ALARMLIST);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_USERID, getUserID(), jsonParams);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_PAGE, currentUpdatePage, jsonParams);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_ROWS, AppConstant.PAGE_COUNT, jsonParams);
		return jsonParams;
	}
	
	/**
	 * 获取告警列表成功
	 * @param resultJson
	 * @throws JSONException
	 */
	private void getAlarmListSuccess(JSONObject resultJson)	throws JSONException {
		totalAlarmCounts = resultJson.getInt(NetWorkCons.JSON_KEY_TOTAL);
		((TextView)(getActivity().findViewById(R.id.title_name))).setText(String.format("我的告警(共%d条)", totalAlarmCounts));// 更新title
		if (currentUpdatePage == 0) {
			if ("true".equals(resultJson.getString("flag"))) { // 此处服务器由于无法判断告警数据变化,因此会一直返回flag=true
				// flag=true时,传过来的数据是第一页数据,重新加载;否则不进行数据操作
				alarmInfoList.clear();
				alarmInfoList.addAll(convertLiftInfo(resultJson.getJSONArray(NetWorkCons.JSON_KEY_FAULTLIST)));
				currentLastPage = 1;
			}
			waterStretchListView.stopRefresh(true);
		} else if (currentUpdatePage == 1) {
			alarmInfoList.clear();
			alarmInfoList.addAll(convertLiftInfo(resultJson.getJSONArray(NetWorkCons.JSON_KEY_FAULTLIST)));
		} else if (currentUpdatePage > 1) {
			waterStretchListView.stopLoadMore();
			alarmInfoList.addAll(convertLiftInfo(resultJson.getJSONArray(NetWorkCons.JSON_KEY_FAULTLIST)));
		}
		mAlarmsInfoAdapter.notifyDataSetChanged();
		showBlank(alarmInfoList);
		if (mAlarmsInfoAdapter.getCount() >= totalAlarmCounts) {
			waterStretchListView.setPushLoadEnable(false);
		} else {
			waterStretchListView.setPushLoadEnable(true);
		}
	}
	
	/**
	 * 获取告警列表失败
	 */
	private void getAlarmListFailed() {
		if (currentUpdatePage == 0) {
			waterStretchListView.stopRefresh(false);
		} else if (currentUpdatePage > 1){
			currentLastPage--;
			waterStretchListView.stopLoadMore();
		}
		showBlank(alarmInfoList);
	}

	/**
	 * 解析从服务器down下来的电梯数据，并转换为list集合
	 * @param jsonArray
	 * @return
	 * @throws JSONException 
	 */
	private ArrayList<AlarmInfo> convertLiftInfo(JSONArray jsonArray) throws JSONException {
		ArrayList<AlarmInfo> infoList = new ArrayList<AlarmInfo>();
		AlarmInfo info = null;
		JSONObject jobject = null;
		for(int i = 0; jsonArray != null && i < jsonArray.length(); i++){
			jobject = jsonArray.getJSONObject(i);
			info = new AlarmInfo(jobject.getString(NetWorkCons.JSON_KEY_LIFTNO), jobject.getString(NetWorkCons.JSON_KEY_LIFTNAME), jobject.getString(NetWorkCons.JSON_KEY_BLOCKNAME),
					jobject.getString(NetWorkCons.JSON_KEY_LIFTADD), jobject.getString(NetWorkCons.JSON_KEY_FAULTLEVEL), jobject.getString(NetWorkCons.JSON_KEY_FAULTNAME),
					jobject.getString(NetWorkCons.JSON_KEY_FAULTNO), jobject.getString(NetWorkCons.JSON_KEY_FAULTTIME), jobject.getString(NetWorkCons.JSON_KEY_FAULTDATA),
					jobject.getString(NetWorkCons.JSON_KEY_FAULTPHOTO), jobject.getString(NetWorkCons.JSON_KEY_FAULTCOUNT), jobject.getString(NetWorkCons.JSON_KEY_FAULTAUDIO));
			infoList.add(info);
		}
		return infoList;
	}

	@Override
	protected void dealProcessLogic() {
		alarmInfoList = new ArrayList<AlarmInfo>();
		initListAdapter();
		getAlarmList();
	}
	
	/**
	 * 初始化adapter
	 */
	private void initListAdapter() {
		mAlarmsInfoAdapter = new BaseAdapterHelper<AlarmInfo>(getActivity(), alarmInfoList, R.layout.item_alarm) {
				@Override
				public void convert(ViewHolder viewHolder, AlarmInfo item) {
					ImageView ivtab = viewHolder.getView(R.id.iv_tab);
					//根据不同告警级别,显示不同的icon
					if (item.getAlarmLevel().equals("9")) {
						ivtab.setImageResource(R.drawable.icon_tab_one);
					} else if (item.getAlarmLevel().equals("8")) {
						ivtab.setImageResource(R.drawable.icon_tab_two);
					} else {
						ivtab.setImageResource(R.drawable.icon_tab_three);
					}
					viewHolder.setText(R.id.tv_lift_no, item.getLiftNo());
					viewHolder.setText(R.id.tv_lift_name, item.getLiftName());
					viewHolder.setText(R.id.tv_alarm_name, item.getAlarmName());
					viewHolder.setText(R.id.tv_alarm_time, item.getAlarmTime());
				}
			};
		waterStretchListView.setAdapter(mAlarmsInfoAdapter);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.relay_search:
			prepareSearchAnimation();
			break;
		}
	}

	/**
	 * 搜索开始动画
	 */
	private void prepareSearchAnimation() {
		ObjectAnimator animator = ObjectAnimator.ofFloat(parentView, "translationY", parentView.getTranslationY(), -titleView.getHeight());
		animator.setDuration(300);
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				searchDialog = new AlarmsInfoSearchDialog(getActivity(), alarmInfoList);
				searchDialog.setOnDismissListener(new OnDismissListener() {
					
					@Override
					public void onDismiss(DialogInterface dialog) {
						finishSearchAnimation();
						
					}
				});
				searchDialog.show();
			}
		});
		animator.start();
	}
	
	/**
	 * 搜索结束动画
	 */
	private void finishSearchAnimation() {
		final int offsetValue = 10;
		ObjectAnimator animator = ObjectAnimator.ofFloat(parentView, "translationY", parentView.getTranslationY()- offsetValue, 0);
		animator.setDuration(300);
		animator.start();
	}

	@Override
	public void onRefresh() {
		currentUpdatePage = 0;//刷新数据时,更新最新的内容
		getAlarmListWithoutPrompt();
	}

	@Override
	public void onLoadMore() {
		currentLastPage++;//每次加载更多,都刷新下一页内容
		currentUpdatePage = currentLastPage;
		getAlarmListWithoutPrompt();
	}
}
