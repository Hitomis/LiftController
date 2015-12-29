package com.zhiitek.liftcontroller.activity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import cn.jpush.android.api.JPushInterface;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.components.net.NetWorkCons;
import com.zhiitek.liftcontroller.components.net.NetWorkHelper;
import com.zhiitek.liftcontroller.components.net.client.NetCallback;
import com.zhiitek.liftcontroller.model.UpdateInfo;
import com.zhiitek.liftcontroller.service.task.UpdateCountsTask;
import com.zhiitek.liftcontroller.utils.AppConstant;
import com.zhiitek.liftcontroller.utils.AppUtil;
import com.zhiitek.liftcontroller.utils.BaseParser;
import com.zhiitek.liftcontroller.utils.DialogUtil;

/**
 * @author ZhaoF
 */
public class SplashActivity extends BaseActivity {
	private static final String TAG = "SplashActivity";
	private TextView tvVersion;
	private UpdateInfo updateInfo;
	private ProgressDialog pd;// 进度框
	public final static long NORMAL_RESPONSE_TIME = 1000;

	private final static int PARSER_INFO_ERROR = -100;
	private final static int SERVER_ERROR = -101;
	private final static int URL_ERROR = -102;
	private final static int NET_FAILED = -103;
	private final static int PARSER_INFO_SUCCESS = 104;
	private final static int DOWNLOAD_ERROR = -105;
	private final static int DOWNLOAD_SUCCESS = 106;
	
	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_splash);
	}

	@Override
	protected void findViewById() {
		// TODO:设置版本字符串
		tvVersion = (TextView) findViewById(R.id.splash_version);
		tvVersion.setText("version:" + getAppVersion());
	}

	@Override
	protected void setListener() {
	}

	@Override
	protected void dealProcessLogic() {
		new Thread(new CheckVersionTask()).start();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		JPushInterface.onResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		JPushInterface.onPause(this);
	}

	private void confirmUser(final String username, final String password) {
		try {
			netWorkHelper.execHttpNetWithoutPrompt(NetWorkCons.loginUrl, initLoginJsonParameter(username, password), new NetCallback() {
				@Override
				public void callback(JSONObject resultJson) {
					if (resultJson != null) {
						try {
							switch (resultJson.getInt(NetWorkCons.JSON_KEY_RESULT)) {
							case FLAG_LOGIN_SUCCESS:// success
								Intent intent = new Intent();
								intent.setClass(SplashActivity.this, MainActivity.class);
								startActivity(intent);
								// 该界面只做进入应用的时候做展示用，所有需要关闭
								finish();
								break;
							case FLAG_LOGIN_FAILURE:// failure 用户名密码错误
								showToast("用户名或者密码错误,请重新登录");
								loadLoginUI();
								break;
							}
						} catch (JSONException e) {
							showToast("网络数据错误, 请联系我们");
						}
					} else {
						loadLoginUI();
					}
				}
			});
		} catch (JSONException e) {
			showToast("网络数据错误, 请联系我们");
		}
	}
	
	private JSONObject initLoginJsonParameter(String username, String password) throws JSONException {
		JSONObject jsonParams = netWorkHelper.initJsonParameters(NetWorkCons.CMD_HTTP_LOGIN_AND_GET_LIFTINFOS);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_USERID, username, jsonParams);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_USERPWD, password, jsonParams);
		return jsonParams;
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case PARSER_INFO_ERROR:
				dealBackgoundLogin();
				break;
			case SERVER_ERROR:
				dealBackgoundLogin();
				break;
			case URL_ERROR:
				dealBackgoundLogin();
				break;
			case NET_FAILED:
				dealBackgoundLogin();
				break;
			case PARSER_INFO_SUCCESS:
				Float oldVersion = Float.parseFloat(getAppVersion());
				Float newVersion = Float.parseFloat(updateInfo.getVersion());
				if (newVersion > oldVersion) {// TODO:有新的版本就弹出更新提示框，下载最新的APK
					showUpdateAppDialog();
				} else {
					dealBackgoundLogin();
				}
				break;
			case DOWNLOAD_ERROR:
				dealBackgoundLogin();
				break;
			case DOWNLOAD_SUCCESS:
				File apkFile = (File) msg.obj;
				installApk(apkFile);
				finish();
				break;
			}
		}
	};
	

	/**
	 * 检查当前版本
	 * 
	 * @author ZhaoF
	 *
	 */
	private class CheckVersionTask implements Runnable {
		@Override
		public void run() {
			SharedPreferences sp = getSharedPreferences("versionConfig",
					MODE_PRIVATE);
			boolean isUpdate = sp.getBoolean("update", true);
			if (!isUpdate) {// 如果用户关闭了自动更新则不检查版本
				try {
					Thread.sleep(800);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				dealBackgoundLogin();
			}

			long startTime = System.currentTimeMillis();
			Message msg = Message.obtain();
			try {
				URL url = new URL(NetWorkCons.updateVersionUrl);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(5000);
				conn.setReadTimeout(5000);
				int code = conn.getResponseCode();
				if (code == 200) {
					InputStream is = conn.getInputStream();
					updateInfo = BaseParser.parserUpdateInfo(is);
					if (updateInfo == null) {
						// TODO:解析服务器信息失败
						msg.what = PARSER_INFO_ERROR;
					} else {
						// TODO:解析成功
						msg.what = PARSER_INFO_SUCCESS;
					}
				} else {
					// TODO:服务器错误
					msg.what = SERVER_ERROR;
				}
			} catch (MalformedURLException e) {
				msg.what = URL_ERROR;
				e.printStackTrace();
			} catch (IOException e) {
				msg.what = NET_FAILED;
				e.printStackTrace();
			} finally {
				long endTime = System.currentTimeMillis();
				long deffTime = endTime - startTime;
				if (deffTime < NORMAL_RESPONSE_TIME) {
					try {
						Thread.sleep(NORMAL_RESPONSE_TIME - deffTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				// TODO: 子线程发送消息告诉主线程，根据消息状态进行相应操作
				handler.sendMessage(msg);
			}
		}
	}

	/**
	 * 显示更新应用程序的弹出框
	 */
	private void showUpdateAppDialog() {
		DialogUtil.showConfirmDialog(SplashActivity.this, "程序升级",
				updateInfo.getDescription(),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String apkUrl = updateInfo.getApkUrl();
						pd = new ProgressDialog(SplashActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
						pd.setTitle("更新进度");
						pd.setMessage("下载中...");
						pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						pd.setCancelable(false);
						pd.show();

						if (Environment.getExternalStorageState().equals(
								Environment.MEDIA_MOUNTED)) {// TODO:判断SD卡是否可用,只有可用状态下,才去下载最新的APK
							final File apkFile = new File(Environment.getExternalStorageDirectory(),
									AppUtil.getServerFileName(apkUrl));
							new Thread() {
								@Override
								public void run() {
									File saveFile = AppUtil.download(
											updateInfo.getApkUrl(),
											apkFile.getAbsolutePath(), pd);
									Message msg = Message.obtain();
									if (saveFile == null) {
										msg.what = DOWNLOAD_ERROR;
									} else {
										msg.what = DOWNLOAD_SUCCESS;
										msg.obj = saveFile;
									}
									handler.sendMessage(msg);
									pd.dismiss();
								}
							}.start();
						} else {// TODO:SDcard不可用
							DialogUtil.showInfoDialog(SplashActivity.this,
									"提示", "SD卡不可用,无法进行程序升级",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											loadLoginUI();
										}

									});
						}
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						loadLoginUI();
					}
				});
	}

	/**
	 * 更新安装Apk
	 * 
	 * @param apkFile
	 */
	private void installApk(File apkFile) {
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setDataAndType(Uri.fromFile(apkFile),
				"application/vnd.android.package-archive");
		startActivity(intent);
	}

	private void dealBackgoundLogin() {
		if (sharedPreferences.contains(AppConstant.KEY_USER_ID) && sharedPreferences.contains(AppConstant.KEY_PASSWORD)) {
			if (AppUtil.hasNetwork(this)) {
				confirmUser(getUserId(), getUserPassword());
			} else {
				Intent intent = new Intent(this, MainActivity.class);
				intent.putExtra(AppConstant.INTENT_KEY_ENTER_MAINACTIVITY_WITH_NO_NET, true);
				startActivity(intent);
				finish();
			}
		} else {
			loadLoginUI();
		}
	}

	/**
	 * 进入应用程序登录界面
	 */
	private void loadLoginUI() {
		Intent intent = new Intent();
		intent.setClass(this, LoginActivity.class);
		startActivity(intent);
		// 该界面只做进入应用的时候做展示用，所有需要关闭
		finish();
	}

	/**
	 * 获取当前应用程序的版本号
	 * 
	 * @return
	 */
	private String getAppVersion() {
		PackageManager pm = getPackageManager();
		try {
			PackageInfo packInfo = pm.getPackageInfo(getPackageName(), 0);
			return packInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}
}
