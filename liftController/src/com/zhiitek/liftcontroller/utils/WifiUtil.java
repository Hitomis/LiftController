package com.zhiitek.liftcontroller.utils;

import java.util.List;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.views.CustomProgressDialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

public class WifiUtil {
	
	public static final int TYPE_NO_PASSWD = 0x11;
	public static final int TYPE_WEP = 0x12;
	public static final int TYPE_WPA = 0x13;
	
	private Context mContext;
	
	private WifiManager wifiMan;
	
	private ConnectWifiCallback connectWifiCallback;
	
	private WifiConnectChangeReceiver wifiConnectChangeReceiver;
	
	private CustomProgressDialog mCustomProgressDialog;
	
	private String ssid;
	
	/**
	 * 是否是用户手动连接wifi
	 */
	private boolean isUserConnectWifi = false;
	
	public WifiUtil(Context context) {
		mContext = context;
		wifiMan = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
	}
	
	public void setConnectWifiCallback(ConnectWifiCallback connectWifiCallback) {
		this.connectWifiCallback = connectWifiCallback;
	}
	
	/**
	 * 注册接受Wifi切换事件的广播接收者
	 */
	public void registerWifiConnChangeReceiver() {
		IntentFilter ifilter = new IntentFilter();
		ifilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		ifilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		ifilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		ifilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		ifilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		wifiConnectChangeReceiver = new WifiConnectChangeReceiver();
		mContext.registerReceiver(wifiConnectChangeReceiver, ifilter);
	}
	
	/**
	 * 解除Wifi切换事件的广播接收者
	 */
	public void unregisterWifiConnChangeListener() {
		if (wifiConnectChangeReceiver != null) {
			mContext.unregisterReceiver(wifiConnectChangeReceiver);
			wifiConnectChangeReceiver = null;
		}
	}
	
	/**
	 * 监听Wifi连接状态的广播接收者
	 */
	private class WifiConnectChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
				NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
				if (ConnectivityManager.TYPE_WIFI == info.getType() && NetworkInfo.State.CONNECTED == info.getState()) {// WLAN连接成功状态
					if (isUserConnectWifi) {
						dismissDlg();
						connectWifiCallback.connectedWifi(wifiMan.getConnectionInfo().getSSID());
						isUserConnectWifi = false;
					}
				} else if (ConnectivityManager.TYPE_WIFI == info.getType() && NetworkInfo.State.DISCONNECTED == info.getState()) {
					connectWifiCallback.disConnectWifi(wifiMan.getConnectionInfo().getSSID());
				}
			} else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent.getAction())) {
				if (WifiManager.ERROR_AUTHENTICATING == intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, Integer.MIN_VALUE)) {// 密码错误
					dismissDlg();
					connectWifiCallback.errorAuthenticating();
				}
			} else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
				if (isUserConnectWifi && !isSSIDAvailable(ssid)) {
					dismissDlg();
					isUserConnectWifi = false;
					connectWifiCallback.apNotAvailable();
				}
			}
		}
	}
	
	/**
	 * 扫描得到的AP是否可用
	 * @param ssid
	 * @return
	 */
	public Boolean isSSIDAvailable(String ssid) {
		Boolean flag = false;
		List<ScanResult> strongScanResult = wifiMan.getScanResults();
		for (ScanResult scanResult : strongScanResult) {
			if (scanResult.SSID.equals(ssid)) {
				flag = true;
				break;
			}
		}
		return flag;
	}
	
	/**
	 * 根据SSID和密码连接到AP
	 * @param ssid
	 * @param password
	 */
	public void connectWifiAp(String ssid, String password) {
		showLoadingDlg();
		this.ssid = ssid;
		isUserConnectWifi = true;
		WifiConfiguration wifiConfig = isSSIDExsits("\"" + ssid + "\"");
		if(wifiConfig != null) {
			clearWifiConfiguration(wifiConfig.SSID);
		}
		wifiMan.setWifiEnabled(true);
		WifiConfiguration createWifiInfo = createWifiConfiguration(ssid, password, TYPE_WPA);
		int netWorkID = wifiMan.addNetwork(createWifiInfo);
		wifiMan.enableNetwork(netWorkID, true);
	}
	
	private void clearWifiConfiguration(String SSID) {
		WifiConfiguration errorConfig = isSSIDExsits(SSID);
		if (errorConfig != null) {
			wifiMan.removeNetwork(errorConfig.networkId);
		}
	}
	
	/**
	 * 检测当前SSID是否以前连接过（如果连接过的wifi,那么配置信息会保存在手机中，通过getConfiguredNetworks() 可以拿到）
	 * 
	 * @param SSID
	 * @return
	 */
	private WifiConfiguration isSSIDExsits(String SSID) {
		List<WifiConfiguration> existingConfigs = wifiMan.getConfiguredNetworks();
		if (existingConfigs != null) {
			for (WifiConfiguration existingConfig : existingConfigs) {
				if (existingConfig.SSID.equals(SSID)) {
					return existingConfig;
				}
			}
		}
		return null;
	}
	
	/**
	 * 创建Wifi连接的配置对象
	 * 
	 * @param SSID
	 * @param password
	 * @param type
	 * @return
	 */
	private WifiConfiguration createWifiConfiguration(String SSID, String password, int type) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";

		// 分为三种情况：1没有密码2用wep加密3用wpa加密
		if (type == TYPE_NO_PASSWD) {// WIFICIPHER_NOPASS
			config.wepKeys[0] = "";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;

		} else if (type == TYPE_WEP) { // WIFICIPHER_WEP
			config.hiddenSSID = true;
			config.wepKeys[0] = "\"" + password + "\"";
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		} else if (type == TYPE_WPA) { // WIFICIPHER_WPA
			config.preSharedKey = "\"" + password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;
		}
		return config;
	}
	
	private void showLoadingDlg() {
		mCustomProgressDialog = new CustomProgressDialog(mContext, R.style.loading_dialog);
		mCustomProgressDialog.show();
	}
	
	private void dismissDlg() {
		if (mCustomProgressDialog != null) {
			mCustomProgressDialog.dismiss();
		}
	}
	
	/**
	 * wifi连接的回调接口
	 * @author Administrator
	 *
	 */
	public interface ConnectWifiCallback {
		/**
		 * 成功连接到connectedSSID的AP
		 * @param connectedSSID 当前连接的AP的ssid
		 */
		public void connectedWifi(String connectedSSID);
		/**
		 * 断开wifi连接
		 * @param connectedSSID 当前连接的AP的ssid
		 */
		public void disConnectWifi(String connectedSSID);
		/**
		 * 验证错误
		 */
		public void errorAuthenticating();
		/**
		 * AP不可用
		 */
		public void apNotAvailable();
	}

}
