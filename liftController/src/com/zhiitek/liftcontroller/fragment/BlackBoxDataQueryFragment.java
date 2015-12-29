package com.zhiitek.liftcontroller.fragment;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.components.UdpSocketConnection;
import com.zhiitek.liftcontroller.components.UdpSocketConnection.SocketDataCallback;
import com.zhiitek.liftcontroller.model.BlackBoxCommand;
import com.zhiitek.liftcontroller.utils.AppUtil;
import com.zhiitek.liftcontroller.utils.BlackBoxUtil;
import com.zhiitek.liftcontroller.utils.DensityUtil;
import com.zhiitek.liftcontroller.views.CustomFormCellView;
import com.zhiitek.liftcontroller.views.SpinFadeLoaderView;

public class BlackBoxDataQueryFragment extends BaseFragment {
	
	private View parentView;
	
	private LinearLayout llContentLayout;
	
	private UdpSocketConnection udpConn;
	
	/**
	 * 统计发送查询命令数量
	 */
	int counts;
	
	/**
	 * 所有需要查询的配置命令
	 */
	private ArrayList<BlackBoxCommand> commands = new ArrayList<BlackBoxCommand>();
	
	/**
	 * 查询到的结果集合,查询失败的配置项,结果则为null
	 */
	private ArrayList<byte[]> results = new ArrayList<byte[]>();
	
	/**
	 * fragment对于用户是否可见
	 */
	private boolean isFragmentVisibleToUser = false;
	
	/**
	 * 是否正在查询，防止多次重复查询
	 */
	private boolean isQuerying = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		parentView = inflater.inflate(R.layout.fragment_blackbox_dataquery, container, false);
		return parentView;
	}

	@Override
	
	
	protected void findViewById() {
		llContentLayout = (LinearLayout) parentView.findViewById(R.id.ll_query_info_content);
	}

	@Override
	protected void setListener() {
	}

	@Override
	protected void dealProcessLogic() {
		AppUtil.saveQueryConfigFlag(getActivity(), true);
		queryData();
	}

	/**
	 * 初始化所有查询命令
	 */
	public void initData() {
		commands.clear();
		commands.add(new BlackBoxCommand(1, 32, "电梯编号", BlackBoxCommand.DATA_TYPE_ASCII));
		commands.add(new BlackBoxCommand(10, 1, "物理顶层", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(11, 1, "校准层", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(33, 1, "电梯自动返回楼层", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(12, 2, "正常速度(cm/s)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(13, 1, "平层正常通过时间(ms)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(14, 2, "正常抖动周期(ms)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(15, 1, "人体传感延时(s)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(17, 1, "平层停止判断时间(s)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(18, 1, "层间停止判断时间(s)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(19, 1, "电梯空闲判断时间(min)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(20, 1, "电梯停用判定时间(hour)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(30, 1, "关门故障判定时间(min)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(31, 1, "开门故障判定时间(s)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(32, 1, "电梯困人判定时间(min)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(40, 1, "同一过性事件过滤去重复时间(min)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(41, 1, "同一状态性事件报告间隔时间(min)", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(201, 4, "网络地址", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(202, 2, "网络端口", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(203, 4, "子网掩码", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(204, 4, "互连网关", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(205, 4, "域名服务器地址", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(206, 96, "远程管理服务器ip/域名", BlackBoxCommand.DATA_TYPE_ASCII));
		commands.add(new BlackBoxCommand(207, 2, "远程管理服务器端口", BlackBoxCommand.DATA_TYPE_INT));
		commands.add(new BlackBoxCommand(208, 2, "远程管理在线心跳间"
				+ "隔(s)", BlackBoxCommand.DATA_TYPE_INT));
		
		
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		isFragmentVisibleToUser = isVisibleToUser;
	}

	/**
	 * 查询数据相关操作
	 */
	public void queryData() {
		if (!isQuerying && isFragmentVisibleToUser && AppUtil.isNeedQueryConfig(getActivity())) {//fragment对用户可见时，才进行查询数据操作
			isQuerying = true;
			udpConn = new UdpSocketConnection(getActivity());
			initData();
			counts = 0;
			results.clear();
			llContentLayout.removeAllViews();
			((SpinFadeLoaderView)parentView.findViewById(R.id.loader_view)).setVisibility(View.VISIBLE);
			postCommand(commands.get(counts));
		}
	}
	
	/**
	 * 发送一个查询命令
	 * 
	 * 查询成功则向results中添加结果,查询失败则添加null
	 * 
	 * 一条查询命令得到回应后,才能开启下一条查询命令
	 * 
	 * @param blackBoxCommand
	 */
	private void postCommand(BlackBoxCommand blackBoxCommand) {
		udpConn.post(BlackBoxUtil.makeSendCommand(0x01, BlackBoxUtil.int2byteArray(blackBoxCommand.getNumber(), 2), 2), new SocketDataCallback() {
			@Override
			public void onSuccess(byte[] result) {
				if ( 0x01 == BlackBoxUtil.getResponseCmd(result)) {
					if (BlackBoxUtil.SSE_SUCCESS == BlackBoxUtil.getResponseCode(result)) {
						results.add(result);
					} else {
						results.add(null);
					}
				} else {
					results.add(null);
				}
				nextQueryPost();
			}
			
			@Override
			public void onFailure(int errorCode) {
				results.add(null);
				nextQueryPost();
			} 
		});
	}
	
	/**
	 * 发送下一条查询命令
	 */
	private void nextQueryPost() {
		counts++;
		if (counts == commands.size()) {//所有查询命令都发送完成时
			isQuerying = false;
			AppUtil.saveQueryConfigFlag(getActivity(), false);
			showResultView();
			udpConn.shutdown();
			//隐藏loading画面
			((SpinFadeLoaderView)parentView.findViewById(R.id.loader_view)).setVisibility(View.GONE);
		} else if (counts < commands.size()){
			postCommand(commands.get(counts));
		}
	}
	
	/**
	 * 显示所有查询结果
	 */
	private void showResultView() {
		for ( int i = 0; i < commands.size(); i ++) {
			String resultString = "";
			if (results.get(i) != null) {//查询配置结果不为空,才处理结果;结果为空,直接显示为""
				if (BlackBoxUtil.isIpCommand(commands.get(i))) {
					//ip地址以*.*.*.*形式显示
					resultString = String.format("%d.%d.%d.%d", BlackBoxUtil.responseData2Int(results.get(i), 4, 1),
							BlackBoxUtil.responseData2Int(results.get(i), 5, 1), BlackBoxUtil.responseData2Int(results.get(i), 6, 1), BlackBoxUtil.responseData2Int(results.get(i), 7, 1));
				} else {
					switch (commands.get(i).getDataType()) {
					case BlackBoxCommand.DATA_TYPE_INT:
						//返回数据int型转换成string显示
						resultString = String.format("%d", BlackBoxUtil.responseData2Int(results.get(i), 4, commands.get(i).getLength()));
						break;
					case BlackBoxCommand.DATA_TYPE_ASCII:
						//返回数据为ascii串转换成string显示
						try {
							resultString = new String(BlackBoxUtil.getResponseData(results.get(i), 4, BlackBoxUtil.getResponseDataLength(results.get(i))), "US-ASCII");
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						break;
					}
				}
			}
			//添加查询结果view
			llContentLayout.addView(createCellView(commands.get(i).getName(), resultString));
			//添加分隔线
			llContentLayout.addView(AppUtil.createDivider(getActivity()));
		}
	}
	
	/**
	 * 创建显示查询结果的view
	 * @param header
	 * @param info
	 * @return
	 */
	private CustomFormCellView createCellView(String header, String info) {
		CustomFormCellView cellView = new CustomFormCellView(getActivity());
		cellView.setTextViewAttributes(getActivity(), 0f, 5f, header, getResources().getColor(R.color.text_color), DensityUtil.sp2px(getActivity(), 12f));
		cellView.setWrapTextViewAttributes(getActivity(), 0f, 5f, info, Color.BLACK, DensityUtil.sp2px(getActivity(), 12f));
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, DensityUtil.dip2Px(getActivity(), 40));
		cellView.setLayoutParams(layoutParams);
		return cellView;
	}

	public static BlackBoxDataQueryFragment newInstance() {
		return new BlackBoxDataQueryFragment();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		results.clear();
		commands.clear();
	}
}
