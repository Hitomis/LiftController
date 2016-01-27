package com.zhiitek.liftcontroller.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.activity.ResolveInspTaskActivity;
import com.zhiitek.liftcontroller.activity.ShowFaultListActivity;
import com.zhiitek.liftcontroller.adapter.BaseAdapterHelper;
import com.zhiitek.liftcontroller.adapter.ViewHolder;
import com.zhiitek.liftcontroller.components.net.NetWorkCons;
import com.zhiitek.liftcontroller.components.net.client.NetCallback;
import com.zhiitek.liftcontroller.model.FaultInfo;
import com.zhiitek.liftcontroller.model.InspectInfo;
import com.zhiitek.liftcontroller.model.TaskInfo;
import com.zhiitek.liftcontroller.service.task.UpdateCountsTask;
import com.zhiitek.liftcontroller.views.WaterStretchListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyTaskFragment extends BaseFragment implements WaterStretchListView.WaterStretchListener {

	private Button mResolveTaskBtn, mChooseTaskBtn;
	private BaseAdapterHelper<TaskInfo> mAdapterHelper;
	private WaterStretchListView waterStretchListView;

	/** 下载的所有任务数据 */
	private ArrayList<TaskInfo> mTaskInfos;
	
	/** 是否处于选择告警任务状态 */
	private boolean isResolvingTask = false;
	/** 任务列表中是否含有告警任务 */
	private boolean hasFaultTask = false;

	private boolean isPullToRefresh = false;
	
	/** 准备解决的任务列表 */
	private ArrayList<TaskInfo> mPrepareResolveTaskInfos;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_my_task, container, false);
		return mView;
	}

	@Override
	protected void findViewById() {
		mResolveTaskBtn = (Button) mView.findViewById(R.id.btn_resolve_task);
		waterStretchListView = (WaterStretchListView) mView.findViewById(R.id.wsl_my_task);
		waterStretchListView.setWaterStretchListViewListener(this);
		waterStretchListView.setPushLoadEnable(false);
		mChooseTaskBtn = (Button) mView.findViewById(R.id.btn_choose_task);
	}


	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	protected void setListener() {
		mResolveTaskBtn.setOnClickListener(btnResolveListener);
		waterStretchListView.setOnItemClickListener(taskListItemClick);
		mChooseTaskBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isResolvingTask = !isResolvingTask;
				mAdapterHelper.notifyDataSetChanged();
				if (isResolvingTask) {
					((Button)v).setText("取消选择");
				} else {
					((Button)v).setText("选择告警任务");
					mResolveTaskBtn.setEnabled(false);
					mPrepareResolveTaskInfos.clear();
				}
			}
		});
	}
	
	NetCallback netCallback = new NetCallback() {
		@Override
		public void callback(JSONObject resultJson) {
			if (resultJson != null) {
				try {
					if (isPullToRefresh) waterStretchListView.stopRefresh(true);
					mTaskInfos.clear();
					mTaskInfos.addAll(convertTaskInfo(resultJson.getJSONArray("taskList")));
					resetBtnStatus();
					showBlank(mTaskInfos);
					mAdapterHelper.notifyDataSetChanged();
					// 更新title
					((TextView)(getActivity().findViewById(R.id.title_name))).setText(String.format("我的任务(共%d条)", mTaskInfos.size()));
				} catch (Exception e) {
					if (isPullToRefresh) waterStretchListView.stopRefresh(false);
					showToast("网络数据错误, 请联系我们");
					showBlank(mTaskInfos);
				}
			} else {
				if (isPullToRefresh) waterStretchListView.stopRefresh(false);
				showBlank(mTaskInfos);
			}
		}
	};
	
	/**
	 * 刷新任务列表
	 */
	private void refreshTasklist() {
		try {
			isPullToRefresh = false;
			JSONObject jsonParams = initGetTaskListJsonParameter();
			netWorkHelper.execHttpNet(NetWorkCons.getTaskUrl, jsonParams, netCallback);
		} catch (JSONException e) {
			showBlank(mTaskInfos);
		}
	}
	
	/**
	 * 下拉刷新任务列表
	 */
	private void pullToRefreshTasklist() {
		try {
			isPullToRefresh = true;
			JSONObject jsonParams = initGetTaskListJsonParameter();
			netWorkHelper.execHttpNetWithoutPrompt(NetWorkCons.getTaskUrl, jsonParams, netCallback);
		} catch (JSONException e) {
			waterStretchListView.stopRefresh(false);
			showBlank(mTaskInfos);
		}
	}

	private JSONObject initGetTaskListJsonParameter() throws JSONException {
		JSONObject jsonParams = netWorkHelper.initJsonParameters(NetWorkCons.CMD_HTTP_GET_TASKLIST);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_USERID, getUserID(), jsonParams);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_PAGE, 1, jsonParams);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_ROWS, 1000, jsonParams);
		return jsonParams;
	}
	
	/**
	 * 解析从服务器down下来的任务列表，并转换为list集合
	 * @param jsonArray
	 * @return
	 */
	private ArrayList<TaskInfo> convertTaskInfo(JSONArray jsonArray) throws JSONException {
		ArrayList<TaskInfo> taskInfos = new ArrayList<TaskInfo>();
		ArrayList<FaultInfo> faultInfos;
		TaskInfo taskInfo = null;
		FaultInfo faultInfo = null;
		InspectInfo inspectInfo = null;
		JSONObject jtaskobject = null;
		JSONObject jfaultobject = null;
		JSONObject jinspobject = null;
		for(int i = 0; jsonArray != null && i < jsonArray.length(); i++){
			jtaskobject = jsonArray.getJSONObject(i);
			String taskType = jtaskobject.getString(NetWorkCons.JSON_KEY_TASKTYPE);
			taskInfo = new TaskInfo(jtaskobject.getString(NetWorkCons.JSON_KEY_TASKID),
					jtaskobject.getString(NetWorkCons.JSON_KEY_TASKNAME),
					taskType,
					jtaskobject.getString(NetWorkCons.JSON_KEY_LIFTNO),
					jtaskobject.getString(NetWorkCons.JSON_KEY_ADDRESS),
					jtaskobject.getString(NetWorkCons.JSON_KEY_CREATETIME),
					jtaskobject.getString(NetWorkCons.JSON_KEY_MEMO));
			if(taskType.equals(TaskInfo.FAULT_TASK_TYPE)) {//告警任务
				hasFaultTask = true;
				JSONArray jsonFaultArray = jtaskobject.getJSONArray(NetWorkCons.JSON_KEY_FAULTLIST);
				faultInfos = new ArrayList<FaultInfo>();
				for(int j = 0; jsonFaultArray != null && j < jsonFaultArray.length(); j++) {
					jfaultobject = jsonFaultArray.getJSONObject(j);
					faultInfo = new FaultInfo(jfaultobject.getString(NetWorkCons.JSON_KEY_FAULTNO),
							jfaultobject.getString(NetWorkCons.JSON_KEY_FAULTID), 
							jfaultobject.getString(NetWorkCons.JSON_KEY_FAULTDATA), 
							jfaultobject.getString(NetWorkCons.JSON_KEY_FAULTTIME));
					faultInfos.add(faultInfo);
				}
				taskInfo.setTaskFault(faultInfos);
			} else {//巡检任务
				jinspobject = jtaskobject.getJSONObject(NetWorkCons.JSON_KEY_INSPECT);
				if (jinspobject.isNull(NetWorkCons.JSON_KEY_STATUS)) {
					inspectInfo = new InspectInfo();
				} else {
					inspectInfo = new InspectInfo(jinspobject.getString(NetWorkCons.JSON_KEY_NEXTINSPECTDATE),
							jinspobject.getString(NetWorkCons.JSON_KEY_STATUS),
							jinspobject.getString(NetWorkCons.JSON_KEY_INSPTYPE));
				}
				taskInfo.setInspect(inspectInfo);
			}
			taskInfos.add(taskInfo);
		}
		return taskInfos;
	}
	
	/**
	 * 构造解决告警任务的jason串
	 * @return
	 * @throws JSONException
	 */
	private JSONArray createSolveFaultTaskJson() throws JSONException{
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject;
		for (int i = 0; i < mPrepareResolveTaskInfos.size(); i++) {
			TaskInfo taskInfo = mPrepareResolveTaskInfos.get(i);
			jsonObject = new JSONObject();
			jsonObject.put(NetWorkCons.JSON_KEY_TASKID, taskInfo.getTaskID());
			jsonObject.put(NetWorkCons.JSON_KEY_TASKTYPE, taskInfo.getTaskType());
			jsonObject.put(NetWorkCons.JSON_KEY_LIFTNO, taskInfo.getLiftNo());
			if (taskInfo.getTaskType().equals(TaskInfo.FAULT_TASK_TYPE)) {// 解决告警任务,部分字段无意义,设为空值
				jsonObject.put(NetWorkCons.JSON_KEY_INSPTYPE, "");
				jsonObject.put(NetWorkCons.JSON_KEY_INSPCONCLUDE, "");
				jsonObject.put(NetWorkCons.JSON_KEY_INSPNO, "");
				jsonObject.put(NetWorkCons.JSON_KEY_INSPMEMO, "");
			}
			jsonObject.put(NetWorkCons.JSON_KEY_USERID, getUserID());
			jsonArray.put(i, jsonObject);
		}
		return jsonArray;
	}
	
	/**
	 * 解决告警任务
	 */
	private void solveFaultTask() {
		try {
			JSONObject jsonParams = netWorkHelper.initJsonParameters(NetWorkCons.CMD_HTTP_RESOLVE_TASK);
			netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_TASKLIST, createSolveFaultTaskJson(), jsonParams);
			netWorkHelper.execHttpNetWithoutPrompt(NetWorkCons.resolveTaskUrl, jsonParams, new NetCallback() {
				@Override
				public void callback(JSONObject resultJson) {
					if (resultJson != null) {
						try {
							showToast(resultJson.getString(NetWorkCons.JSON_KEY_EMSG));
							for (TaskInfo taskInfo : mPrepareResolveTaskInfos) {
								mTaskInfos.remove(taskInfo);
							}
							for (TaskInfo taskInfo2: mTaskInfos) {
								if (TaskInfo.FAULT_TASK_TYPE.equals(taskInfo2.getTaskType())) {
									hasFaultTask = true;
									break;
								}
							}
							new UpdateCountsTask(getActivity()).updateCount();
							resetBtnStatus();
							mAdapterHelper.notifyDataSetChanged();
							showBlank(mTaskInfos);
						} catch (Exception e){
							showToast("网络数据错误, 请联系我们");
						}
					}
				}
			});
		} catch (JSONException e) {
			showToast("网络数据错误, 请联系我们");
		}
	}

	private OnItemClickListener taskListItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			TaskInfo info = (TaskInfo)waterStretchListView.getAdapter().getItem(position);
			if(info.getTaskType().equals(TaskInfo.FAULT_TASK_TYPE)){//告警任务,显示任务详情
				Intent intent = new Intent(mContext, ShowFaultListActivity.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("taskInfo", info);
				intent.putExtras(bundle);
				startActivity(intent);
			} else {//巡检任务
				Intent taskIntent = new Intent(mContext, ResolveInspTaskActivity.class);
				Bundle bundle = new Bundle();
				bundle.putInt("position", position);
				bundle.putSerializable("taskInfo", info);
				taskIntent.putExtras(bundle);
				startActivityForResult(taskIntent, 0);
			}
		}
	};
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 100) {//成功解决巡检任务后,返回时移除已解决的任务,刷新列表
			mTaskInfos.remove(data.getIntExtra("position", 0));
		}
		mAdapterHelper.notifyDataSetChanged();
		showBlank(mTaskInfos);
	};

	/**
	 * 解决告警任务
	 */
	private OnClickListener btnResolveListener= new OnClickListener() {
		@Override
		public void onClick(View v) {
			solveFaultTask();
		}
	};

	@Override
	protected void dealProcessLogic() {
		mTaskInfos = new ArrayList<TaskInfo>();
		mPrepareResolveTaskInfos = new ArrayList<TaskInfo>();
		initTaskAdapter(mTaskInfos);
		waterStretchListView.setAdapter(mAdapterHelper);
		refreshTasklist();
	}
	
	/**
	 * 重置btn状态
	 */
	private void resetBtnStatus() {
		mChooseTaskBtn.setEnabled(hasFaultTask);
		mResolveTaskBtn.setEnabled(false);
		mChooseTaskBtn.setText("选择告警任务");
		mPrepareResolveTaskInfos.clear();
		isResolvingTask = false;
		hasFaultTask = false;
	}

	/**
	 * 创建adapter
	 * @param taskInfos
	 */
	private void initTaskAdapter(ArrayList<TaskInfo> taskInfos) {
		mAdapterHelper = new BaseAdapterHelper<TaskInfo>(mContext, taskInfos, R.layout.item_task) {
			@Override
			public void convert(ViewHolder viewHolder, final TaskInfo item) {
				//如果备注为空，隐藏备注栏
				if (item.getTaskMemo() == null || item.getTaskMemo().equals("")) {
					viewHolder.getView(R.id.taskmemo).setVisibility(View.GONE);
				}
				viewHolder.setText(R.id.tasknametv, item.getTaskID() + " " + item.getTaskName());
				viewHolder.setText(R.id.tasktimetv, item.getTaskTime());
				viewHolder.setText(R.id.taskaddresstv, item.getAddress());
				viewHolder.setText(R.id.taskmemotv, item.getTaskMemo());
				if(item.getTaskType().equals(TaskInfo.FAULT_TASK_TYPE)){//告警任务加载的特殊布局
					viewHolder.getView(R.id.faultnumberstv).setVisibility(View.VISIBLE);
					viewHolder.setCircleTextView(R.id.faultnumberstv, String.valueOf(item.getTaskFault().size()));
					if (isResolvingTask) {//处于选择告警任务状态中,将checkbox显示出来,隐藏右箭头图标
						updateViewInResolvingTask(viewHolder, item);
					} else {//没有处于选择告警任务状态中,将checkbox隐藏,显示右箭头图标
						viewHolder.getView(R.id.img_go_details).setVisibility(View.VISIBLE);
						viewHolder.getView(R.id.chk_is_resolve).setVisibility(View.GONE);
					}
				} else {//检验任务加载的特殊布局
					viewHolder.getView(R.id.faultnumberstv).setVisibility(View.INVISIBLE);
					viewHolder.getView(R.id.img_go_details).setVisibility(View.VISIBLE);
					viewHolder.getView(R.id.chk_is_resolve).setVisibility(View.GONE);
				}
			}

			/**
			 * 当处于选择告警状态时,更新界面
			 * @param viewHolder
			 * @param item
			 */
			private void updateViewInResolvingTask(ViewHolder viewHolder, final TaskInfo item) {
				viewHolder.getView(R.id.img_go_details).setVisibility(View.GONE);
				viewHolder.getView(R.id.chk_is_resolve).setVisibility(View.VISIBLE);
				//为每个告警任务item的checkbox设置单独的点击事件
				viewHolder.getView(R.id.chk_is_resolve).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						CheckBox cb = (CheckBox) v;
						if (cb.isChecked()) {
							//勾选checkbox,将此条告警任务添加进待解决的告警任务列表
							mPrepareResolveTaskInfos.add(item);
							mResolveTaskBtn.setEnabled(true);
						} else {
							//取消勾选checkbox,将此条告警任务从待解决的告警任务列表中移除
							if (mPrepareResolveTaskInfos.contains(item)){
								mPrepareResolveTaskInfos.remove(item);
							}
							//待解决的告警任务列表为空时,解决任务按钮置灰
							if (mPrepareResolveTaskInfos.size() == 0) {
								mResolveTaskBtn.setEnabled(false);
							}
						}
					}
				});
				//设置每个告警任务item的checkbox状态,防止拉动列表时,checkbox状态显示错误
				if (mPrepareResolveTaskInfos.contains(item)) {
					((CheckBox)viewHolder.getView(R.id.chk_is_resolve)).setChecked(true);
				} else {
					((CheckBox)viewHolder.getView(R.id.chk_is_resolve)).setChecked(false);
				}
			}
		};
	}

	@Override
	public void onRefresh() {
		pullToRefreshTasklist();
	}

	@Override
	public void onLoadMore() {

	}
}
