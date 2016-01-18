package com.zhiitek.liftcontroller.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.views.CustomPromptDialog;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class AppUtil {

	public static final String defaultFormat = new String("yyyy-MM-dd HH:mm:ss");
	public static final String dateFormat = new String("yyyy-MM-dd");
	
	//扫描连接设备的有效期
	public static final int TIME_OUT_MINUTES = 15;
	//监测设备有效期是否过期的间隔秒数
	public static final int CHECK_TIME_OUT_SECONDS = 30;

	/**
	 * 获得网络连接是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean hasNetwork(Context context) {
		ConnectivityManager con = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo workinfo = con.getActiveNetworkInfo();
		if (workinfo == null || !workinfo.isAvailable()) {
			return false;
		}
		return true;
	}
	
	/**
	 * 获取服务器文件的名称
	 * @param apkUrl
	 * @return
	 */
	public static String getServerFileName(String serverPath) {
		String fileName = null;
		if(serverPath != null && !TextUtils.isEmpty(serverPath)){
			int index = serverPath.lastIndexOf("/");
			if(index != -1){
				fileName = serverPath.substring(index + 1);
			}
			
		}
		return fileName;
	}

	/**
	 * 从服务器上下载最新的APK
	 * @param serverPath 服务器路径
	 * @param savedPath  文件在SDcard上保存的路径
	 * @param pd 进度条
	 * @return 返回null代表下载失败
	 */
	public static File download(String serverPath, String savedPath,
			ProgressDialog pd) {
		try {
			URL url = new URL(serverPath);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);
			int code = conn.getResponseCode();
			if(code == 200){//TODO:请求成功
				pd.setMax(conn.getContentLength());
				InputStream is = conn.getInputStream();
				File file = new File(savedPath);
				FileOutputStream fos = new FileOutputStream(file);
				byte[] buffer = new byte[1024];
				int len = 0;
				int total = 0;
				while((len = is.read(buffer)) != -1){
					fos.write(buffer, 0, len);
					total += len;
					pd.setProgress(total);
					Thread.sleep(5);//TODO:为了下载的时可以显示出进度条跑动的效果暂时加上
				}
				fos.flush();
				fos.close();
				is.close();
				return file;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 检查Wifi是否启用
	 * @param context
	 * @return
	 */
	public static boolean isEnabledWifi(Context context) {
		WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		return null != wifiMan && wifiMan.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
	}

	/**
	 * 从文件输入流中读取字符串
	 * 
	 * @param is
	 * @return
	 */
	public static String readInputStream(InputStream is) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int len = 0;
			byte[] buffer = new byte[1024];
			while ((len = is.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}
			is.close();
			baos.close();
			byte[] result = baos.toByteArray();
			// 解析result里面的字符串
			return new String(result);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 获取当前时间字符串
	 * 
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static String getCurrentTimeStr() {
		return new SimpleDateFormat(defaultFormat).format(new Date());
	}

	/**
	 * 获取当前日期字符串
	 * 
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static String getCurrentDateStr() {
		return new SimpleDateFormat(dateFormat).format(new Date());
	}

	/*
	 * 时间转字符串
	 */
	// synchronized
	public static String timeToString(Date time) {
		if (time == null) {
			time = new Date();
		}
		return new SimpleDateFormat(defaultFormat).format(time);
	}

	/*
	 * 字符串转时间
	 */
	// synchronized
	public static Date stringToTime(String timeStr) {
		try {
			return new SimpleDateFormat(defaultFormat).parse(timeStr);
		} catch (Exception e) {
		}

		return null;
	}

	public static Date getTopTime() {
		return new Date(2000, 1, 1, 0, 0, 0);
	}

	public static Date getBottomTime() {
		return new Date(0);
	}
	
	public static void showTimeOutDialog(final Context context, DialogInterface.OnClickListener listener) {
		CustomPromptDialog.Builder customBuilder = new CustomPromptDialog.Builder(context);
            customBuilder.setTitle("连接超时")
                .setMessage(String.format("%d分钟有效期已到，如需要继续维护设备，请重新验证", TIME_OUT_MINUTES))
                .setButton("好的", listener);
        CustomPromptDialog dialog = customBuilder.create();
        dialog.show();
	}
	
	public static boolean isConnectValid(Context context) {
		boolean flag = false;
		String lastConnectTime = context.getSharedPreferences(context.getApplicationInfo().packageName, Context.MODE_PRIVATE).getString("connecttime", "");
		if (!lastConnectTime.isEmpty()) {
			Date now = new Date();
			Date before = AppUtil.stringToTime(lastConnectTime);
			long milliseconds = now.getTime() - before.getTime();
			if (milliseconds <= (TIME_OUT_MINUTES * 60 * 1000))
				flag = true;
		}
		return flag;
	}
	
	/**
	 * 绘制divier线
	 * @param context
	 * @return
	 */
	public static View createDivider(Context context) {
		View view = new View(context);
		view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dip2Px(context, 1)));
		view.setBackgroundResource(R.color.dividing_deep_line_color);
		return view;
	}
	
	/**
	 * 打开参数查询fragment时是否需要查询配置
	 * @return
	 */
	public static boolean isNeedQueryConfig(Context context) {
		return context.getSharedPreferences(context.getApplicationInfo().packageName, Context.MODE_PRIVATE)
				.getBoolean(AppConstant.KEY_BLACKBOX_NEED_QUERY_CONFIG_FLAG, false);
	}
	
	/**
	 * 保存打开参数查询fragment时是否查询配置的flag
	 * @param context
	 * @param flag
	 */
	public static void saveQueryConfigFlag(Context context, boolean flag) {
		SharedPreferences sp = context.getSharedPreferences(context.getApplicationInfo().packageName, Context.MODE_PRIVATE);
		sp.edit().putBoolean(AppConstant.KEY_BLACKBOX_NEED_QUERY_CONFIG_FLAG, flag).commit();
	}
	
	/**
	 * 获取服务器url
	 * @return
	 */
	public static String getBaseUrl(Context context) {
		String url = "www.zhiitek.com:8081";
		SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstant.KEY_SERVICE_URL, Context.MODE_PRIVATE);
		if (sharedPreferences.contains(AppConstant.KEY_SERVICE_URL)) {
			url = sharedPreferences.getString(AppConstant.KEY_SERVICE_URL, "");
		}
	    return String.format("%s%s", "http://", url);
	}

	/**
	 * Map a value within a given range to another range.
	 * @param value the value to map
	 * @param fromLow the low end of the range the value is within
	 * @param fromHigh the high end of the range the value is within
	 * @param toLow the low end of the range to map to
	 * @param toHigh the high end of the range to map to
	 * @return the mapped value
	 */
	public static double mapValueFromRangeToRange(
			double value,
			double fromLow,
			double fromHigh,
			double toLow,
			double toHigh) {
		double fromRangeSize = fromHigh - fromLow;
		double toRangeSize = toHigh - toLow;
		double valueScale = (value - fromLow) / fromRangeSize;
		return toLow + (valueScale * toRangeSize);
	}
	
}
