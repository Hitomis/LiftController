package com.zhiitek.liftcontroller.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.activity.BaseActivity;
import com.zhiitek.liftcontroller.activity.NoticeMainActivity;
import com.zhiitek.liftcontroller.adapter.BaseAdapterHelper;
import com.zhiitek.liftcontroller.adapter.ViewHolder;
import com.zhiitek.liftcontroller.components.net.NetWorkCons;
import com.zhiitek.liftcontroller.components.net.client.NetCallback;
import com.zhiitek.liftcontroller.model.DevicesInfo;
import com.zhiitek.liftcontroller.utils.AppConstant;
import com.zhiitek.liftcontroller.utils.DialogUtil;
import com.zhiitek.liftcontroller.views.DevicesInfoSearchDialog;
import com.zhiitek.liftcontroller.views.WaterStretchListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
/**
 *  我的设备页面
 */
public class MyDevicesFragment extends BaseFragment implements OnClickListener, WaterStretchListView.WaterStretchListener{
	
	private RelativeLayout relaySearch;
	
	private TextView tvNoticeManager;
	
	private View view, parentView, titleView;
	
	private LinearLayout linlayNoticeManager;
	
	/** 显示设备数据的listview */
	private WaterStretchListView waterStretchListView;
	/** 下载的设备数据 */
	public List<DevicesInfo> devicesInfoList;
	
	BaseAdapterHelper<DevicesInfo> mDvicesInfoAdapter;
	/** 搜索功能对话框 */
	private DevicesInfoSearchDialog searchDialog;
	
	/** 下载设备数据成功 */
	protected final static int FLAG_GET_DEVICES_SUCCESS = 0;
	/** 下载设备数据失败 */
	protected final static int FLAG_GET_DEVICES_FAILURE = -1;

	/** 更新设备的页码 */
	private int currentIndexPage = 1;
	
	/** 设备总数 */
	private int totalDeviceCounts;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_my_deivces, container, false);
		return view;
	}

	@Override
	protected void findViewById() {
		relaySearch = (RelativeLayout) view.findViewById(R.id.relay_search);
		waterStretchListView = (WaterStretchListView) view.findViewById(R.id.wsl_devices_data);
		waterStretchListView.setWaterStretchListViewListener(this);
		waterStretchListView.setPushLoadEnable(false);
		parentView = getActivity().findViewById(R.id.linlay_conents);
		titleView = getActivity().findViewById(R.id.relay_titles);
		linlayNoticeManager = (LinearLayout) view.findViewById(R.id.linlay_notice_manager);
		tvNoticeManager = (TextView) view.findViewById(R.id.tv_notice_manager);
	}

	@Override
	protected void setListener() {
		waterStretchListView.setOnItemClickListener(fragmentItemClickListener);
		relaySearch.setOnClickListener(this);
		linlayNoticeManager.setOnClickListener(this);
	}
	
	/**
	 * 获取设备列表的callback
	 */
	NetCallback netCallback = new NetCallback() {
		@Override
		public void callback(JSONObject resultJson) {
			if (resultJson != null) {
				try {
					switch (resultJson.getInt("result")) {
					case FLAG_GET_DEVICES_SUCCESS:// success
						getDevicesListSucess(resultJson);
						break;
					case FLAG_GET_DEVICES_FAILURE:// failure 用户名密码错误
						getDevicesListFailed();
						showToast("用户名密码错误!");
						break;
					}
				} catch (JSONException e) {
					getDevicesListFailed();
				}
			} else {
				getDevicesListFailed();
			}
		}
	};
	
	/**
	 * 获取设备列表
	 */
	private void getDevicesDataList() {
		currentIndexPage = 1;
		try {
			netWorkHelper.execHttpNet(NetWorkCons.loginUrl, initGetDevicesJsonParameter(), netCallback);
		} catch (JSONException e) {
			getDevicesListFailed();
		}
	}
	
	/**
	 * 获取设备列表,没有loading对话框
	 */
	private void getDevicesDataListWithoutPrompt() {
		try {
			netWorkHelper.execHttpNetWithoutPrompt(NetWorkCons.loginUrl, initGetDevicesJsonParameter(), netCallback);
		} catch (JSONException e) {
			getDevicesListFailed();
		}
	}

	/**
	 * 构造查询设备列表的JSON参数串
	 * @return
	 * @throws JSONException
	 */
	private JSONObject initGetDevicesJsonParameter() throws JSONException {
		JSONObject jsonParams = netWorkHelper.initJsonParameters(NetWorkCons.CMD_HTTP_LOGIN_AND_GET_LIFTINFOS);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_USERID, getUserID(), jsonParams);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_USERPWD, sharedPreferences.getString(AppConstant.KEY_PASSWORD, ""), jsonParams);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_PAGE, currentIndexPage, jsonParams);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_ROWS, AppConstant.PAGE_COUNT, jsonParams);
		return jsonParams;
	}
	
	/**
	 * 获取设备列表成功
	 * @param resultJson
	 * @throws JSONException
	 */
	private void getDevicesListSucess(JSONObject resultJson) throws JSONException {
		totalDeviceCounts = resultJson.getInt("total");//获取数据总条数
		((TextView)(getActivity().findViewById(R.id.title_name))).setText(String.format("我的设备(共%d台)", totalDeviceCounts));// 更新title
		if (currentIndexPage == 0) {
			devicesInfoList.addAll(0, convertLiftInfo(resultJson.getJSONArray("infoList")));
			waterStretchListView.stopRefresh(true);
		} else if (currentIndexPage == 1) {
			devicesInfoList.clear();//更新数据时显示第一页数据
			devicesInfoList.addAll(convertLiftInfo(resultJson.getJSONArray("infoList")));
//			waterStretchListView.stopRefresh(true);
		} else if (currentIndexPage > 1){//每次加载更多,直接添加数据
			devicesInfoList.addAll(convertLiftInfo(resultJson.getJSONArray("infoList")));
			waterStretchListView.stopLoadMore();
		}
		mDvicesInfoAdapter.notifyDataSetChanged();
		showBlank(devicesInfoList);
		if (mDvicesInfoAdapter.getCount() >= totalDeviceCounts) {//加载的数据比服务器总数据还多时,不再显示加载更多
			waterStretchListView.setPushLoadEnable(false);
		} else {
			waterStretchListView.setPushLoadEnable(true);
		}
	}

	/**
	 * 获取设备列表失败
	 */
	private void getDevicesListFailed() {
		if (currentIndexPage == 0) {
			waterStretchListView.stopRefresh(false);
		} else if (currentIndexPage > 1){
			currentIndexPage--;
			waterStretchListView.stopLoadMore();
		}
		showBlank(devicesInfoList);
	}
	
	/**
	 * 解析从服务器down下来的电梯数据，并转换为list集合
	 * @param jsonArray
	 * @return
	 * @throws JSONException 
	 */
	private ArrayList<DevicesInfo> convertLiftInfo(JSONArray jsonArray) throws JSONException {
		ArrayList<DevicesInfo> infoList = new ArrayList<DevicesInfo>();
		DevicesInfo info = null;
		JSONObject jobject = null;
		for(int i = 0; jsonArray != null && i < jsonArray.length(); i++){
			jobject = jsonArray.getJSONObject(i);
			info = new DevicesInfo(jobject.getString("liftNo"), jobject.getString("liftName"), jobject.getString("liftStatus"));
			infoList.add(info);
		}
		return infoList;
	}
	
	/**
	 * 更新设备状态
	 * @param liftNo
	 * @param liftStatus
	 */
	private void updateDeviceStatus(final String liftNo, final String liftStatus) {
		try {
			netWorkHelper.execHttpNet(NetWorkCons.updateLiftStatusUrl, initUpdateDeviceStatusJsonParameter(liftNo, liftStatus), new NetCallback() {
				@Override
				public void callback(JSONObject resultJson) {
					if (resultJson != null) {
						changeStatus(liftNo, liftStatus);
						popup.dismiss();
					}
				}
			});
		} catch (JSONException e) {
			showToast("网络数据错误, 请联系我们");
		}
	}
	
	private JSONObject initUpdateDeviceStatusJsonParameter(String liftNo, String liftStatus) throws JSONException {
		JSONObject jsonParams = netWorkHelper.initJsonParameters(NetWorkCons.CMD_HTTP_UPDATE_LIFT_STATUS);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_USERID, getUserID(), jsonParams);
		JSONArray jsonArray = new JSONArray();
		JSONObject liftData = new JSONObject();
		liftData.put("liftNo", liftNo);
		liftData.put("liftSatus", liftStatus);
		jsonArray.put(liftData);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_LIFTINFOLIST, jsonArray, jsonParams);
		return jsonParams;
	}

	private OnItemClickListener fragmentItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			View popouView = View.inflate(getActivity(), R.layout.popup_devices_status, null);
			addClickListener(popouView, view);
			popup = new PopupWindow(popouView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			// 设置PopupWindow可以获取焦点[在失去焦点的时候,会自动关闭PopupWindow]
			popup.setFocusable(true);
			popup.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#52C2FC")));
			// 设置PopupWindow显示的位置
			popup.showAsDropDown(view, 0, -view.getHeight());
		}
	};
	
	private OnItemClickListener searchDialogItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			
			View popouView = View.inflate(getActivity(), R.layout.popup_devices_status, null);
			addClickListener(popouView, view);
			popup = new PopupWindow(popouView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			// 设置PopupWindow可以获取焦点[在失去焦点的时候,会自动关闭PopupWindow]
			popup.setFocusable(true);
			popup.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#52C2FC")));
			// 设置PopupWindow显示的位置
			popup.showAsDropDown(view, 0, -view.getHeight()); 
		}
	};

	@Override
	protected void dealProcessLogic() {
		displayNoticeManager();
		devicesInfoList = new ArrayList<DevicesInfo>();
		initAdapter();
		getDevicesDataList();
	}

	/**
	 * 管理员和物业角色的用户显示通告发布按钮
	 */
	private void displayNoticeManager() {
		int userType = getUserType();
		if(userType == BaseActivity.TYPE_USER_ADMINISTRATOR || userType == BaseActivity.TYPE_USER_PROPERTY) {
			tvNoticeManager.setVisibility(View.VISIBLE);
		}
	}
	
	private void initAdapter() {
		mDvicesInfoAdapter = new BaseAdapterHelper<DevicesInfo>(getActivity(), devicesInfoList, R.layout.item_my_devices) {
			@Override
			public void convert(ViewHolder viewHolder, DevicesInfo item) {
				viewHolder.setText(R.id.tv_lift_name, item.getLiftName());
				viewHolder.setText(R.id.tv_lift_no, item.getLiftNo());
				viewHolder.setText(R.id.tv_lift_status, item.getRunningStatus());
			}
		};
		waterStretchListView.setAdapter(mDvicesInfoAdapter);
	}

	private PopupWindow popup;
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.relay_search:
			prepareSearchAnimation();
			break;
		case R.id.linlay_notice_manager:
			startActivity(new Intent(getActivity(), NoticeMainActivity.class));
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
				searchDialog = new DevicesInfoSearchDialog(getActivity(), devicesInfoList);
				searchDialog.setItemClickListener(searchDialogItemClickListener);
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

	/**
	 * 添加点击事件
	 * @param popouView
	 * @param itemView
	 */
	private void addClickListener(View popouView, View itemView) {
		PopouViewClickListener popouViewClickListener = new PopouViewClickListener(itemView);
		popouView.findViewById(R.id.tv_running).setOnClickListener(popouViewClickListener);
		popouView.findViewById(R.id.tv_stop).setOnClickListener(popouViewClickListener);
		popouView.findViewById(R.id.tv_fault).setOnClickListener(popouViewClickListener);
		popouView.findViewById(R.id.tv_check).setOnClickListener(popouViewClickListener);
		popouView.findViewById(R.id.tv_scrap).setOnClickListener(popouViewClickListener);
		popouView.findViewById(R.id.tv_reboot).setOnClickListener(popouViewClickListener);
	}

	@Override
	public void onRefresh() {
		currentIndexPage = 0;//刷新数据时,更新第一页内容
		getDevicesDataListWithoutPrompt();
	}

	@Override
	public void onLoadMore() {
		currentIndexPage++;//每次加载更多,都刷新下一页内容
		getDevicesDataListWithoutPrompt();
	}

	/**
	 * 自定义点击事件
	 * @author Administrator
	 *
	 */
	private class PopouViewClickListener implements OnClickListener {
		
		private View itemView;
		
		public PopouViewClickListener(View itemView) {
			super();
			this.itemView = itemView;
		}
		
		@Override
		public void onClick(View v) {
			TextView tvLiftNo =  (TextView) itemView.findViewById(R.id.tv_lift_no);
			final String liftNo = tvLiftNo.getText().toString();
			if (v.getId() == R.id.tv_reboot) { // 重启设备操作
				
				DialogUtil.showConfirmDialog(getActivity(), null, "是否确认重启该设备?", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						netWorkHelper.execHttpNetGet(NetWorkCons.getRebootUrl(liftNo), null);
					}

				}, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						popup.dismiss();
					}
				});
				
			} else {
				if (v instanceof TextView) {
					final String clickStr = ((TextView)v).getText().toString();
					String message = String.format("是否确认更改电梯编号 “%s” 的电梯状态为 “%s” 状态?", liftNo, clickStr);
					DialogUtil.showConfirmDialog(getActivity(), null, message, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//发送网络请求修改状态,状态修改成功后再修改对应数据显示
							updateDeviceStatus(liftNo, DevicesInfo.getStatusByFont(clickStr));
						}

					}, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							popup.dismiss();
						}
					});
				}
			}
		}
	} 
	
	/**
	 * 服务器修改状态成功后,在界面上修改对应状态
	 * @param liftNo
	 * @param status
	 */
	@SuppressWarnings("unchecked")
	private void changeStatus(String liftNo, String status) {
		if (searchDialog != null) {
			searchDialog.dismiss();
		}
		if (liftNo != null && status != null) {
			int changeItemPosition = findItemPositionByLiftNoAndChangeStatus(liftNo, status);
			if (changeItemPosition >= 0) {
				((BaseAdapterHelper<DevicesInfo>)((HeaderViewListAdapter)waterStretchListView.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
				waterStretchListView.setSelection(changeItemPosition);
			}
		}
	}

	/**
	 * 通过电梯编号,查找到对应的item,并修改该item的设备状态
	 * @param liftNo
	 * @param status
	 * @return
	 */
	private int findItemPositionByLiftNoAndChangeStatus(String liftNo, String status) {
		int i = -1;
		for(DevicesInfo devicesInfo : devicesInfoList) {
			if (liftNo.equals(devicesInfo.getLiftNo())){
				devicesInfo.setRunningStatus(status);
				i = devicesInfoList.indexOf(devicesInfo);
				break;
			}
		}
		return i;
	}

}
