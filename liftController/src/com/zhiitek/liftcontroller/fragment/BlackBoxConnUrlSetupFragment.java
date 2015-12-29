package com.zhiitek.liftcontroller.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.components.UdpSocketConnection;

public class BlackBoxConnUrlSetupFragment extends BaseFragment {
	
	private View parentView;
	
	private UdpSocketConnection udpConn;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		parentView = inflater.inflate(R.layout.fragment_blackbox_connurl_setup, null);
		return parentView;
	}

	@Override
	protected void findViewById() {

	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void dealProcessLogic() {
		udpConn = new UdpSocketConnection(getActivity());
	}
	
	public static BlackBoxConnUrlSetupFragment newInstance() {
		return new BlackBoxConnUrlSetupFragment();
	}
	
	private void queryUrlSetup() {
//		udpConn.post(BlackBoxUtil.makeSendCommand(0xFE, BlackBoxUtil.int2byteArray(blackBoxCommand.getNumber(), 2), 2), new SocketDataCallback() {
//			@Override
//			public void onSuccess(byte[] result) {
//				if ( 0xFE == BlackBoxUtil.getResponseCmd(result)) {
//					if (BlackBoxUtil.SSE_SUCCESS == BlackBoxUtil.getResponseCode(result)) {
//						String resultString = "";
//						switch (blackBoxCommand.getDataType()) {
//						case BlackBoxCommand.DATA_TYPE_INT:
//							resultString = String.format("%d", BlackBoxUtil.responseData2Int(result, 5, blackBoxCommand.getLength()));
//							break;
//						case BlackBoxCommand.DATA_TYPE_INT_ARRAY:
//							for (int i = 0; i <16; i++) {
//								resultString += String.format("%d,", BlackBoxUtil.responseData2Int(result, 5 + 4 * i, 4));
//							}
//							resultString = resultString.substring(0, resultString.length() - 1);
//							break;
//						case BlackBoxCommand.DATA_TYPE_ASCII:
//							resultString = new String(BlackBoxUtil.getResponseData(result, 5, blackBoxCommand.getLength()));
//							break;
//						}
//						llContentLayout.addView(createCellView(blackBoxCommand.getName(), resultString));
//						llContentLayout.addView(createDivider());
//					} else {
//						showToast(String.format("查询%s失败", blackBoxCommand.getName()));
//					}
//				} else {
//					showToast(String.format("查询%s时，功能码错误", blackBoxCommand.getName()));
//				}
//				isCurrentPostFinished = true;
//			}
//			
//			@Override
//			public void onFailure(int errorCode) {
//				isCurrentPostFinished = true;
//				promptBlackBoxSocketonFailure(blackBoxCommand.getName(), errorCode);
//			}
//		});
	}

}
