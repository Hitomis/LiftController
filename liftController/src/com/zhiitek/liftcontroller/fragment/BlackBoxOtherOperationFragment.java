package com.zhiitek.liftcontroller.fragment;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.activity.BlackBoxOperatingActivity;
import com.zhiitek.liftcontroller.components.UdpSocketConnection;
import com.zhiitek.liftcontroller.components.UdpSocketConnection.SocketDataCallback;
import com.zhiitek.liftcontroller.utils.BlackBoxUtil;
import com.zhiitek.liftcontroller.utils.DialogUtil;
import com.zhiitek.liftcontroller.views.CustomFormCellView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class BlackBoxOtherOperationFragment extends BaseFragment {
	
	private View parentView;
	
	private CustomFormCellView cellView;
	
	private UdpSocketConnection udpConn;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		parentView = inflater.inflate(R.layout.fragment_blackbox_other, null);
		return parentView;
	}

	@Override
	protected void findViewById() {
		cellView = (CustomFormCellView) parentView.findViewById(R.id.black_box_reboot);
	}

	@Override
	protected void setListener() {
		cellView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showRebootDlg();
			}
		});
	}

	@Override
	protected void dealProcessLogic() {
		udpConn = new UdpSocketConnection(getActivity());
	}
	
	public static BlackBoxOtherOperationFragment newInstance() {
		return new BlackBoxOtherOperationFragment();
	}
	
	/**
	 * 弹出重启设备确认对话框
	 */
	private void showRebootDlg() {
		DialogUtil.showConfirmDialog(getActivity(), null, "请确认是否要重启设备？", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				rebootDevice();
				dialog.cancel();
			}
		}, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
	}

	/**
	 * 重启设备
	 */
	protected void rebootDevice() {
		//重启命令，无任何返回值
		udpConn.post(BlackBoxUtil.makeSendCommand(0xED), null);
		showToast("设备重启中...");
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		udpConn.shutdown();
	}

}
