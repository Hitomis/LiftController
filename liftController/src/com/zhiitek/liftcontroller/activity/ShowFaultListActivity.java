package com.zhiitek.liftcontroller.activity;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.adapter.BaseAdapterHelper;
import com.zhiitek.liftcontroller.adapter.ViewHolder;
import com.zhiitek.liftcontroller.components.net.NetWorkCons;
import com.zhiitek.liftcontroller.components.net.NetWorkHelper;
import com.zhiitek.liftcontroller.components.net.client.NetCallback;
import com.zhiitek.liftcontroller.model.AlarmInfo;
import com.zhiitek.liftcontroller.model.FaultInfo;
import com.zhiitek.liftcontroller.model.TaskInfo;
import com.zhiitek.liftcontroller.views.CustomFormCellView;
import com.zhiitek.liftcontroller.views.SwipeFinishLayout;

public class ShowFaultListActivity extends BaseActivity{

	private ListView mFaultListView;
	private BaseAdapterHelper<FaultInfo> mFaultAdapter;
	private ArrayList<FaultInfo> mFaultInfos;
	private TaskInfo taskInfo;

	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_show_fault_list);
	}

	@Override
	protected void findViewById() {
		taskInfo = (TaskInfo) getIntent().getSerializableExtra("taskInfo");
		mFaultInfos = taskInfo.getTaskFault();
		mFaultListView = (ListView) findViewById(R.id.fault_list);
		((CustomFormCellView) findViewById(R.id.task_title)).setInfoText(taskInfo.getTaskName());
		((CustomFormCellView) findViewById(R.id.task_time)).setInfoText(taskInfo.getTaskTime());
		((CustomFormCellView) findViewById(R.id.task_adress)).setInfoText(taskInfo.getAddress());
		((CustomFormCellView) findViewById(R.id.task_memo)).setInfoText(taskInfo.getTaskMemo());
	}

	@Override
	protected void setListener() {
		setTitleBar("告警任务详情", null);
		mFaultListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				downloadAlarmInfo(mFaultAdapter.getItem(position));
			}
		});
	}

	private void showAlarmDetails(AlarmInfo alarmInfo) {
		//点击一条告警数据时,跳转到AlarmDetailsActivity并将该告警数据传过去
		Intent intent = new Intent(this, AlarmDetailsActivity.class);
		intent.putExtra("alarmInfo", alarmInfo);
		startActivity(intent);
	}

	private AlarmInfo downloadAlarmInfo(FaultInfo item) {
		try {
			JSONObject jsonParams = netWorkHelper.initJsonParameters(NetWorkCons.CMD_HTTP_GET_ONE_ALARM_DETAILS);
			netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_FAULTID, item.getFaultId(), jsonParams);
			netWorkHelper.execHttpNet(this, NetWorkCons.downloadOneAlarmDetailsUrl, jsonParams, new NetCallback() {
				@Override
				public void callback(JSONObject resultJson) {
					if (resultJson != null) {
						try {
							AlarmInfo alarmInfo = new AlarmInfo(resultJson.getString(NetWorkCons.JSON_KEY_LIFTNO), resultJson.getString(NetWorkCons.JSON_KEY_LIFTNAME), resultJson.getString(NetWorkCons.JSON_KEY_BLOCKNAME),
									resultJson.getString(NetWorkCons.JSON_KEY_LIFTADD), resultJson.getString(NetWorkCons.JSON_KEY_FAULTLEVEL), resultJson.getString(NetWorkCons.JSON_KEY_FAULTNAME),
									resultJson.getString(NetWorkCons.JSON_KEY_FAULTNO), resultJson.getString(NetWorkCons.JSON_KEY_FAULTTIME), resultJson.getString(NetWorkCons.JSON_KEY_FAULTDATA),
									resultJson.getString(NetWorkCons.JSON_KEY_FAULTPHOTO), resultJson.getString(NetWorkCons.JSON_KEY_FAULTCOUNT));
							showAlarmDetails(alarmInfo);
						} catch (JSONException e) {
							showToast("网络数据错误, 请联系我们");
						}
					}
				}
			});
		} catch (JSONException e) {
			showToast("网络数据错误, 请联系我们");
		}
		return null;
	}

	@Override
	protected void dealProcessLogic() {
		SwipeFinishLayout.attachToActivity(this);
		if (mFaultInfos != null) {
			mFaultAdapter = new BaseAdapterHelper<FaultInfo>(this, mFaultInfos, R.layout.item_show_fault_list) {
				@Override
				public void convert(ViewHolder viewHolder, FaultInfo item) {
					
					viewHolder.setText(R.id.fault_num, item.getFaultNo());
					viewHolder.setText(R.id.fault_data, item.getFaultData());
					viewHolder.setText(R.id.fault_time, item.getFaultTime());
				}
			};
			mFaultListView.setAdapter(mFaultAdapter);
		}
	}
}
