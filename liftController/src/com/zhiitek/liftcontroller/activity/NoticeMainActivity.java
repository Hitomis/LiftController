package com.zhiitek.liftcontroller.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.adapter.BaseAdapterHelper;
import com.zhiitek.liftcontroller.adapter.ViewHolder;
import com.zhiitek.liftcontroller.components.net.NetWorkCons;
import com.zhiitek.liftcontroller.components.net.client.NetCallback;
import com.zhiitek.liftcontroller.model.NoticeInfo;
import com.zhiitek.liftcontroller.utils.AppConstant;
import com.zhiitek.liftcontroller.views.RefreshableView;
import com.zhiitek.liftcontroller.views.RefreshableView.PullToLoadListener;
import com.zhiitek.liftcontroller.views.RefreshableView.PullToRefreshListener;
import com.zhiitek.liftcontroller.views.SwipeFinishLayout;

/**
 * 
 * 通告管理页面  </br>
 * 
 * 我的设备 -通告管理
 * 
 * @author ZhaoFan
 *
 */
public class NoticeMainActivity extends BaseActivity {
	
	private RefreshableView refreshView;
	
	private ListView lvNotices;
	
	private List<NoticeInfo> noticeList = new ArrayList<NoticeInfo>();
	
	private int currentIndexPage = 1;
	
	private int totalNoticeCount;
	
	private NetNoticeCallback netNoticeCallback;
	
	public static final String INTENT_EXTRA_NOTICEINFO = "noticeInfo";
	public static final String INTENT_EXTRA_LOCALINFO_LIFT = "localInfo_list";
	
	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_notice_main);
	}

	@Override
	protected void findViewById() {
		refreshView = (RefreshableView) findViewById(R.id.rfv_notice);
		refreshView.setFooterView();
		lvNotices = (ListView) findViewById(R.id.lv_notice_data);
	}

	@Override
	protected void setListener() {
		lvNotices.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(NoticeMainActivity.this, NoticeDetailesActivity.class);
				intent.putExtra(INTENT_EXTRA_NOTICEINFO, noticeList.get(position));
				startActivity(intent);
			}
		});
		
		refreshView.setOnRefreshListener(new PullToRefreshListener() {

			@Override
			public void onRefresh() {
				currentIndexPage = 0;//刷新数据时,更新第一页内容
				getNoticeDataListWithoutPrompt();
			}
		}, NetWorkCons.CMD_HTTP_GET_NOTICE_LIST);
		
		refreshView.setOnLoadListener(new PullToLoadListener() {
			
			@Override
			public void onLoad() {
				currentIndexPage++;//每次加载更多,都刷新下一页内容
				getNoticeDataListWithoutPrompt();
			}
		});
	}
	
	@Override
	protected void dealProcessLogic() {
		SwipeFinishLayout.attachToActivity(this);
		setTitleBar("通告管理", new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(NoticeMainActivity.this, NoticeAddActivity.class));
			}
		});
		
		try {
			JSONObject jsonParms = netWorkHelper.initJsonParameters(NetWorkCons.CMD_HTTP_GET_NOTICE_LIST);
			netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_USERID, getUserId(), jsonParms);
			netNoticeCallback = new NetNoticeCallback();
			netWorkHelper.execHttpNet(NetWorkCons.downloadNoticeListUrl, jsonParms, netNoticeCallback);
		} catch (JSONException e) {
		}

	}
	
	
	/**
	 * 获取通告信息列表,没有loading
	 */
	private void getNoticeDataListWithoutPrompt() {
		netWorkHelper.execHttpNetWithoutPrompt(NetWorkCons.downloadNoticeListUrl, createGetNoticesJsonParams(), netNoticeCallback);
	}
	
	private JSONObject createGetNoticesJsonParams() {
		JSONObject jsonParms = null;
		try {
			jsonParms = netWorkHelper.initJsonParameters(NetWorkCons.CMD_HTTP_GET_NOTICE_LIST);
			netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_USERID, getUserId(), jsonParms);
			netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_PAGE, currentIndexPage, jsonParms);
			netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_ROWS, AppConstant.PAGE_COUNT, jsonParms);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonParms;
	}
	
	private class NetNoticeCallback implements NetCallback {

		@Override
		public void callback(JSONObject resultJson) {
			if (resultJson != null) {
				try {
					inflateListViewData(resultJson);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private BaseAdapterHelper<NoticeInfo> noticeAdapterHelper = new BaseAdapterHelper<NoticeInfo>(this, noticeList, R.layout.item_notice) {
		
		@Override
		public void convert(ViewHolder viewHolder, NoticeInfo noticeInfo) {
			TextView tvType = viewHolder.getView(R.id.tv_type);
			TextView tvStatus = viewHolder.getView(R.id.tv_status);
			TextView tvContent = viewHolder.getView(R.id.tv_content);
			TextView tvSendUsername = viewHolder.getView(R.id.tv_send_username);
			TextView tvEndTime = viewHolder.getView(R.id.tv_end_time);
			
			tvType.setText(noticeInfo.displayTypeText());
			tvStatus.setText(noticeInfo.displayStatusText());
			tvStatus.setTextColor(Color.parseColor(noticeInfo.displayStatusColor()));
			tvContent.setText(noticeInfo.getContent());
			tvSendUsername.setText(String.format("发送自:%s", noticeInfo.getSendUserName()));
			tvEndTime.setText(String.format("截止于:%s", noticeInfo.getEndTime()));
		}
	};

	private void inflateListViewData(JSONObject resultJson) throws JSONException {
		totalNoticeCount = resultJson.getInt("total");//获取数据总条数	
		if (currentIndexPage == 0) {
			noticeList.addAll(0, parseNoticeData(resultJson));
			refreshView.finishRefreshing();
			noticeAdapterHelper.notifyDataSetChanged();
		} else if (currentIndexPage == 1) {
			noticeList.clear();
			noticeList.addAll(parseNoticeData(resultJson));
			refreshView.finishRefreshing();
			lvNotices.setAdapter(noticeAdapterHelper);
		} else if (currentIndexPage > 1){//每次加载更多,直接添加数据
			noticeList.addAll(parseNoticeData(resultJson));
			refreshView.finishLoading();
			noticeAdapterHelper.notifyDataSetChanged();
		}
		showBlank(noticeList);
		if (noticeAdapterHelper.getCount() >= totalNoticeCount) {//加载的数据比服务器总数据还多时,不再显示加载更多
			refreshView.setFooterViewShow(false);
		} else {
			refreshView.setFooterViewShow(true);
		}
		
	}
	
	private List<NoticeInfo> parseNoticeData(JSONObject resultJson) {
		List<NoticeInfo> noticeList = new ArrayList<NoticeInfo>();
		try {
			JSONArray jsonArray = resultJson.getJSONArray("noticeList");
			NoticeInfo noticeInfo;
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				noticeInfo = new NoticeInfo();
				noticeInfo.setId(jsonObject.getString("id"));
				noticeInfo.setContent(jsonObject.getString("content"));
				noticeInfo.setEndTime(jsonObject.getString("life"));
				noticeInfo.setStatus(jsonObject.getString("status"));
				noticeInfo.setType(jsonObject.getString("type"));
				noticeInfo.setTarget(jsonObject.getString("target"));
				noticeInfo.setTargetName(jsonObject.getString("targetName"));
				noticeInfo.setStartTime(jsonObject.getString("postTime"));
				noticeInfo.setUpdateTime(jsonObject.getString("updateTime"));
				noticeInfo.setSendUserID(jsonObject.getString("postUser"));
				noticeInfo.setSendUserName(jsonObject.getString("postUserName"));
				noticeInfo.setCancelUserId(jsonObject.getString("cancelUser"));
				noticeList.add(noticeInfo);
			}
		} catch (JSONException e) {
		}
		return noticeList;
	}

	@Override
	protected int editRightImageResource() {
		return R.drawable.icon_common_add;
	}

}
