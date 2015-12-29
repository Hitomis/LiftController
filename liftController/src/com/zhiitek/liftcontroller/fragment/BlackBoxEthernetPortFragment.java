package com.zhiitek.liftcontroller.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.components.UdpSocketConnection;

/**
 * 硬件调试-网口对接
 * 
 * @author ZhaoFan
 *
 */
public class BlackBoxEthernetPortFragment extends BaseFragment {
	
	private UdpSocketConnection udpConn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_blackbox_ethernet_port, null);

		return view;
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

	public static BlackBoxEthernetPortFragment newInstance() {
		return new BlackBoxEthernetPortFragment();
	}

}
