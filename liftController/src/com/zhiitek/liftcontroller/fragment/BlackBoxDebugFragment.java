package com.zhiitek.liftcontroller.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.GridView;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.adapter.BaseAdapterHelper;
import com.zhiitek.liftcontroller.adapter.ViewHolder;
import com.zhiitek.liftcontroller.components.UdpSocketConnection;
import com.zhiitek.liftcontroller.components.UdpSocketConnection.SocketDataCallback;
import com.zhiitek.liftcontroller.utils.BlackBoxUtil;
import com.zhiitek.liftcontroller.views.WrapTextView;

public class BlackBoxDebugFragment extends BaseFragment {
	
	private View parentView;
	
	private UdpSocketConnection udpConn;
	
	private Button btnDebug;
	
	/**	 显示调式结果	 */
	private GridView gridView;
	/**	 添加到adapter的数据集合	 */
	private ArrayList<String> gridAdapterData = new ArrayList<String>();
	/**	 调试结果集合	 */
	private ArrayList<String> debugReciveData = new ArrayList<String>();
	
	/**  显示调试结果的gridview的adapter	 */
	private BaseAdapterHelper<String> mAdapterHelper;

	/**	  调试接口	 */
	private DebugCallBack debugCallBack;
	/**	  是否正在调试	 */
	private boolean isDebuging = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		parentView = inflater.inflate(R.layout.fragment_blackbox_debug, null);
		return parentView;
	}

	@Override
	protected void findViewById() {
		btnDebug = (Button) parentView.findViewById(R.id.btn_blackbox_debug);
		gridView = (GridView) parentView.findViewById(R.id.gv_blackbox_debug_print);
	}

	@Override
	protected void setListener() {
		btnDebug.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isDebuging) {
					beginDebug();
				} else {
					endDebug();
				}
			}
		});
		ViewParent parent = gridView.getParent();
		if (parent instanceof ViewGroup) {
			final ViewGroup vGroup = (ViewGroup) parent;
			gridView.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					gridView.requestDisallowInterceptTouchEvent(false);
					return true;
				}
			});
		}
		
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		debugCallBack = (DebugCallBack) activity;
	}
	
	/**
	 * 初始化Adapter
	 */
	private void initAdapter() {
		mAdapterHelper = new BaseAdapterHelper<String>(mContext, gridAdapterData, R.layout.item_blackbox_debug) {
			@Override
			public void convert(ViewHolder viewHolder, String item) {
				((WrapTextView)viewHolder.getView(R.id.item_blackbox_debug_text)).setWrapText(item);
			}
		};
	}
	
	/**
	 * 初始化adapter的数据
	 */
	private void initAdapterData() {
		gridAdapterData.clear();
		debugReciveData.clear();
		gridAdapterData.add("类型");
		gridAdapterData.add("数据1");
		gridAdapterData.add("数据2");
		gridAdapterData.add("数据3");
	}
	
	/**
	 * 解析调式结果
	 * @param data
	 */
	private void resolveData(String data) {
		if (data.startsWith("SIG_SSR") || data.startsWith("POS") || data.startsWith("TIM_TAG")
				|| data.startsWith("EVN")) {//格式满足才进行解析和显示
			debugReciveData.add(data);
			//对所有调试结果排序
			Collections.sort(debugReciveData, new SortComparator());
			gridAdapterData.clear();
			gridAdapterData.add("类型");
			gridAdapterData.add("数据1");
			gridAdapterData.add("数据2");
			gridAdapterData.add("数据3");
			for (int i = 0; i < debugReciveData.size(); i++) {//将每条调试结果分成四部分添加到adapter的数据中
				//调试结果格式:"SIG_SSR/0/0/09CB0053/"
				String[] dataStrings = debugReciveData.get(i).split("\\/");
				if (dataStrings.length == 5) {
					String[] datas = Arrays.copyOfRange(dataStrings, 0, 4);
					gridAdapterData.addAll(Arrays.asList(datas));
				}
			}
			mAdapterHelper.notifyDataSetChanged();
		}
	}
	
	/**
	 * 开始调式
	 */
	private void beginDebug() {
		initAdapterData();
		udpConn.post(BlackBoxUtil.makeSendCommand(0x0EB, new byte[] {(byte) 1, (byte) 0}, 2), true, false, new SocketDataCallback() {
			@Override
			public void onSuccess(byte[] result) {
				if (checkCmd(result, 0x0EB)) {
					if (!isDebuging) {
						isDebuging = true;
						debugCallBack.beginDebug();
						btnDebug.setText("结束调式");
					}
					//解析数据
					resolveData(new String(BlackBoxUtil.getResponseData(result, 4, BlackBoxUtil.getResponseDataLength(result))));
				}
			}
			
			@Override
			public void onFailure(int errorCode) {
			}
		});
	}
	
	/**
	 * 结束调试
	 */
	private void endDebug() {
		udpConn.post(BlackBoxUtil.makeSendCommand(0x0EB, new byte[] {(byte) 1, (byte) 9}, 2), true, true, new SocketDataCallback() {
			@Override
			public void onSuccess(byte[] result) {
				if (checkCmd(result, 0x0EB)) {
					if (BlackBoxUtil.getResponseCode(result) == BlackBoxUtil.SSE_SUCCESS) {
						isDebuging = false;
						debugCallBack.endDebug();
						btnDebug.setText("开始调式");
					} else {
						showToast("关闭调试模式失败");
					}
				}
			}
			
			@Override
			public void onFailure(int errorCode) {

			}
		});
	}

	@Override
	protected void dealProcessLogic() {
		udpConn = new UdpSocketConnection(getActivity());
		initAdapterData();
		initAdapter();
		gridView.setAdapter(mAdapterHelper);
	}
	
	public static BlackBoxDebugFragment newInstance() {
		return new BlackBoxDebugFragment();
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		udpConn.shutdown();
	}
	
	/**
	 * 调试接口
	 * @author 
	 *
	 */
	public interface DebugCallBack {
		/**
		 * 开始调试
		 */
		public void beginDebug();
		/**
		 * 结束调试
		 */
		public void endDebug();
	}
	
	/**
	 * 为数据排序
	 * @author Administrator
	 *
	 */
	private class SortComparator implements Comparator<String> {

		@Override
		public int compare(String lhs, String rhs) {
			return getLevel(lhs) - getLevel(rhs);
			
		}
		
		private int getLevel(String data) {
			if (data != null) {
				if (data.startsWith("SIG_SSR")) {
					return 1;
				} else if (data.startsWith("POS")){
					return 2;
				} else if (data.startsWith("TIM_TAG")){
					return 3;
				} else if (data.startsWith("EVN")){
					return 4;
				}
			}
			return -1;
		}
	}

}
