package com.zhiitek.liftcontroller.activity;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.adapter.BaseAdapterHelper;
import com.zhiitek.liftcontroller.adapter.ViewHolder;
import com.zhiitek.liftcontroller.components.net.NetWorkCons;
import com.zhiitek.liftcontroller.components.net.NetWorkHelper;
import com.zhiitek.liftcontroller.components.net.client.NetCallback;
import com.zhiitek.liftcontroller.model.InspectInfo;
import com.zhiitek.liftcontroller.model.TaskInfo;
import com.zhiitek.liftcontroller.utils.AppConstant;
import com.zhiitek.liftcontroller.views.SpinnerEditText;
import com.zhiitek.liftcontroller.views.SpinnerEditText.PopupItemClickListenner;
import com.zhiitek.liftcontroller.views.SwipeFinishLayout;

/**
 * 解决检验任务
 * @author Administrator
 *
 */
public class ResolveInspTaskActivity extends BaseActivity{
	
	private TaskInfo inspTaskInfo;
	
	private TextView mLiftNoTv, mLiftNameTv;
	private SpinnerEditText mInspConclutionEt;
	private EditText mInspNumberEt, mInspRemarkEt;
	
	private String[] inspConclutionStrings;
	
	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_resolve_insptask);
	}

	@Override
	protected void findViewById() {
		mLiftNoTv = (TextView) findViewById(R.id.tv_lift_number);
		mInspNumberEt = (EditText) findViewById(R.id.et_insp_number);
		mInspConclutionEt = (SpinnerEditText) findViewById(R.id.et_insp_conclution);
		mInspRemarkEt = (EditText) findViewById(R.id.et_insp_remark);
		mLiftNameTv = (TextView) findViewById(R.id.tv_lift_name);
	}

	@Override
	protected void setListener() {
	}

	@Override
	protected void dealProcessLogic() {
		SwipeFinishLayout.attachToActivity(this);
		inspTaskInfo = (TaskInfo) getIntent().getSerializableExtra("taskInfo");
		String title;
		if(inspTaskInfo.getTaskType().equals(TaskInfo.BAK_INSPECT_TASK_TYPE)) {
			title = "年检报告";
		} else if (inspTaskInfo.getTaskType().equals(TaskInfo.SITE_INSPECT_TASK_TYPE)) {
			title = "巡检报告";
		} else {
			title = "检验报告";
		}
		setTitleBar(title, null);
		mLiftNameTv.setText(inspTaskInfo.getTaskName());
		mLiftNoTv.setText(inspTaskInfo.getLiftNo());
		InspectInfo info = inspTaskInfo.getInspect();
		if(info != null && (InspectInfo.INSP_NOT_PASS.equals(info.getStatus())
				|| InspectInfo.FIELD_INSP_NOT_PASS.equals(info.getStatus()) || InspectInfo.REINSP_NOT_PASS.equals(info.getStatus()))) {
			inspConclutionStrings = getResources().getStringArray(R.array.reinsp_conclution);
		} else {
			inspConclutionStrings = getResources().getStringArray(R.array.insp_conclution);
		}
		mInspConclutionEt.setText(inspConclutionStrings[0]);
		mInspConclutionEt.setAdapter(new BaseAdapterHelper<String>(this, Arrays.asList(inspConclutionStrings), R.layout.popup_spinner_item) {
			@Override
			public void convert(ViewHolder viewHolder, String item) {
				viewHolder.setText(R.id.tv_item_name, item);				
			}
		});
		mInspConclutionEt.setOnClickItemListenner(new PopupItemClickListenner() {
			@Override
			public void onClick(int position) {
				if(position == 0) {
					mInspRemarkEt.setVisibility(View.INVISIBLE);
				} else if (position == 1){
					mInspRemarkEt.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	/**
	 * 提交检验信息
	 * @param v
	 */
	public void submitInspInfo(View v) {
		resolveTask(inspTaskInfo);
	}
	
	private void resolveTask(TaskInfo taskInfo) {
		try {
			netWorkHelper.execHttpNet(NetWorkCons.resolveTaskUrl, initModifyPswJsonParameter(taskInfo), new NetCallback() {
				@Override
				public void callback(JSONObject resultJson) {
					if (resultJson != null) {
						try {
							showToast(resultJson.getString(NetWorkCons.JSON_KEY_EMSG));
							setResult(100, getIntent());
							finish();
						} catch (JSONException e) {
							showToast("网络数据错误, 请联系我们");
						}
					} else {
						showToast("网络数据错误, 请联系我们");
					}
				}
			});
		} catch (JSONException e) {
			showToast("网络数据错误, 请联系我们");
		}
	}
	
	private JSONObject initModifyPswJsonParameter(TaskInfo taskInfo) throws JSONException {
		JSONObject jsonParams = netWorkHelper.initJsonParameters(NetWorkCons.CMD_HTTP_MODIFY_LOGIN_PASSWORD);
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(NetWorkCons.JSON_KEY_TASKID, taskInfo.getTaskID());
		jsonObject.put(NetWorkCons.JSON_KEY_TASKTYPE, taskInfo.getTaskType());
		jsonObject.put(NetWorkCons.JSON_KEY_LIFTNO, taskInfo.getLiftNo());
		jsonObject.put(NetWorkCons.JSON_KEY_USERID, getUserId());
		if (taskInfo.getTaskType().equals(TaskInfo.SITE_INSPECT_TASK_TYPE)) {
			jsonObject.put(NetWorkCons.JSON_KEY_INSPTYPE, InspectInfo.SITE_INSPECTION);
		} else if (taskInfo.getTaskType().equals(TaskInfo.BAK_INSPECT_TASK_TYPE)) {
			jsonObject.put(NetWorkCons.JSON_KEY_INSPTYPE, InspectInfo.ANNUAL_INSPECTION);
		}
		if(mInspConclutionEt.getText().toString().equals("现场检验合格")){
			jsonObject.put(NetWorkCons.JSON_KEY_INSPCONCLUDE, InspectInfo.FIELD_INSP_PASS);
		} else if (mInspConclutionEt.getText().toString().equals("现场检验不合格")){
			jsonObject.put(NetWorkCons.JSON_KEY_INSPCONCLUDE, InspectInfo.FIELD_INSP_NOT_PASS);
			jsonObject.put(NetWorkCons.JSON_KEY_INSPREMARK, mInspRemarkEt.getText().toString().trim());
		} else if (mInspConclutionEt.getText().toString().equals("复检合格")){
			jsonObject.put(NetWorkCons.JSON_KEY_INSPCONCLUDE, InspectInfo.REINSP_PASS);
		} else if (mInspConclutionEt.getText().toString().equals("复检不合格")){
			jsonObject.put(NetWorkCons.JSON_KEY_INSPCONCLUDE, InspectInfo.REINSP_NOT_PASS);
			jsonObject.put(NetWorkCons.JSON_KEY_INSPREMARK, mInspRemarkEt.getText().toString().trim());
		}
		jsonObject.put(NetWorkCons.JSON_KEY_INSPREPORTNO, mInspNumberEt.getText().toString().trim());
		jsonArray.put(jsonObject);
		netWorkHelper.setDataInResponseJson("taskList", jsonArray, jsonParams);
		return jsonParams;
	}
	
}