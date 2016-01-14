package com.zhiitek.liftcontroller.activity;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.adapter.BaseAdapterHelper;
import com.zhiitek.liftcontroller.adapter.ViewHolder;
import com.zhiitek.liftcontroller.components.net.NetWorkCons;
import com.zhiitek.liftcontroller.components.net.client.NetCallback;
import com.zhiitek.liftcontroller.db.LiftControllerSqlHelper;
import com.zhiitek.liftcontroller.fragment.ConnectLiftFragment;
import com.zhiitek.liftcontroller.fragment.MyAlarmFragment;
import com.zhiitek.liftcontroller.fragment.MyDevicesFragment;
import com.zhiitek.liftcontroller.fragment.MyTaskFragment;
import com.zhiitek.liftcontroller.fragment.UserInfoFragment;
import com.zhiitek.liftcontroller.model.MenuItemInfo;
import com.zhiitek.liftcontroller.service.ControllerService;
import com.zhiitek.liftcontroller.service.task.UpdateCountsTask;
import com.zhiitek.liftcontroller.utils.AppConstant;
import com.zhiitek.liftcontroller.utils.AppUtil;
import com.zhiitek.liftcontroller.utils.DialogUtil;
import com.zhiitek.liftcontroller.views.CircleTextView;
import com.zhiitek.liftcontroller.views.PromptTopView;
import com.zhiitek.liftcontroller.views.SimpleSlidingMenu;
import com.zhiitek.liftcontroller.views.SimpleSlidingMenu.MenuItemHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import cn.jpush.android.api.JPushInterface;
import de.greenrobot.event.EventBus;

public class MainActivity extends BaseActivity {

	private long mExitTime;

	private WifiManager wifiMan;

    private SimpleSlidingMenu slidingMenu;
	
	private ConnectLiftFragment mConfigureLiftFragment;
	private MyTaskFragment myTaskFragment;
	private MyDevicesFragment mDevicesFragment;
	private MyAlarmFragment alarmFragment;
	private UserInfoFragment userInfoFragment;

	private TextView mTitleTextView, mUserNameTv, mUserTypeTv;
	
	private Button btnExitLogin;
	
	private BaseAdapterHelper<MenuItemInfo> menuItemHelper;
	
	private List<MenuItemInfo> menuItemList;
	
	private boolean isNeedCheckUser = false;
	
	private NetConnectChangeReceiver netConnectChangeReceiver;
	
	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_main);
	}

	private class MenuItemClickListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			switch(position){
			case 0://我的任务
				if (myTaskFragment == null) {
					myTaskFragment = new MyTaskFragment();
				}
				changeFragment(myTaskFragment);
				break;
			case 1://我的告警
				if (alarmFragment == null) {
					alarmFragment = new MyAlarmFragment();
				}
				changeFragment(alarmFragment);
				break;
			case 2://我的设备
				if (mDevicesFragment == null) {
					mDevicesFragment = new MyDevicesFragment();
				}
				changeFragment(mDevicesFragment);
				break;
			case 3:
				if (userInfoFragment == null) {
					userInfoFragment = new UserInfoFragment();
				}
				changeFragment(userInfoFragment);
				break;
			case 4://设备维护
				if (mConfigureLiftFragment == null) {
					mConfigureLiftFragment = new ConnectLiftFragment();
				}
				changeFragment(mConfigureLiftFragment);
				break;
			}
			slidingMenu.closeMenu();
		}
	}
	
	/**
	 * 替换显示fragment
	 * @param targetFragment
	 */
    private void changeFragment(Fragment targetFragment){
    	if (targetFragment == null) return;
    	if (targetFragment instanceof ConnectLiftFragment) {
    		changeTitle("设备维护");
    	} else if (targetFragment instanceof MyTaskFragment) {
    		changeTitle("我的任务");
    	} else if (targetFragment instanceof MyDevicesFragment) {
    		changeTitle("我的设备");
    	} else if (targetFragment instanceof MyAlarmFragment) {
    		changeTitle("我的告警");
    	} else if (targetFragment instanceof UserInfoFragment) {
    		changeTitle("我的信息");
    	}
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.relay_content, targetFragment, "fragment")
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }
    
    private void changeTitle(String text) {
    	if (mTitleTextView != null) {
    		mTitleTextView.setText(text);
    	}
    }

	@Override
	protected void findViewById() {
		slidingMenu = (SimpleSlidingMenu) findViewById(R.id.simple_menu);
		mTitleTextView = (TextView) findViewById(R.id.title_name);
		mUserNameTv = (TextView) findViewById(R.id.tv_user_login_name);
		mUserTypeTv = (TextView) findViewById(R.id.tv_user_type);
		btnExitLogin = (Button) findViewById(R.id.btn_exit_login);
	}

	@Override
	protected void setListener() {
		slidingMenu.setMenuItemHandler(new MenuItemHandler() {
			@Override
			public void postMenuItem(ListView menu) {
				String userType = sharedPreferences.getString(AppConstant.KEY_USER_TYPE, "-1");
				menuItemList = groupMenuItem(userType);
				menuItemHelper = new BaseAdapterHelper<MenuItemInfo>(MainActivity.this, menuItemList, R.layout.item_slidemenu) {
					
					@Override
					public void convert(ViewHolder viewHolder, MenuItemInfo item) {
						TextView tvMeunItemName = viewHolder.getView(R.id.tv_item_name);
						ImageView icMenuIcon = viewHolder.getView(R.id.iv_menu_icon);
						CircleTextView circleTextView = viewHolder.getView(R.id.item_data_counts);
						tvMeunItemName.setText(item.getMenuTitle());
						icMenuIcon.setImageResource(item.getIconID());
						if (item.getCount() == 0) {
							circleTextView.setVisibility(View.INVISIBLE);
						} else if (item.getCount() > 0 && item.getCount() < 100) {
							circleTextView.setVisibility(View.VISIBLE);
							circleTextView.setText(String.valueOf(item.getCount()));
						} else if (item.getCount() >= 100) {
							circleTextView.setVisibility(View.VISIBLE);
							circleTextView.setText("99+");
						}
					}
				};
				menu.setAdapter(menuItemHelper);
				menu.setOnItemClickListener(new MenuItemClickListener());
			}

		});
		btnExitLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sharedPreferences.edit().clear().commit();
				clearConnectedAp();
				clearJPushAlias();
				stopService(new Intent(MainActivity.this, ControllerService.class));
				startActivity(new Intent(MainActivity.this, LoginActivity.class));
				finish();
			}

		});
	}
	
	/**
	 * 清理 “是否设置过极光推送的别名”的标识 并停止推送服务
	 */
	private void clearJPushAlias() {
		Editor editor = sharedPreferences.edit();
		editor.remove(AppConstant.KEY_JPUSH_SET_ALIAS);
		editor.commit();
		JPushInterface.stopPush(getApplicationContext());
	}
	
	/**
	 * 根据userType 生成不同的目录组
	 * @param userType
	 * @return
	 */
	private List<MenuItemInfo> groupMenuItem(String userType) {
		List<MenuItemInfo> menuNameList;
		MenuItemInfo[] items = null;
		switch (Integer.valueOf(userType)) {
		case TYPE_USER_ADMINISTRATOR:
			items = new MenuItemInfo[]{new MenuItemInfo(R.drawable.menu_my_task, "我的任务", 0),
					new MenuItemInfo(R.drawable.menu_my_notify, "我的告警", 0),
					new MenuItemInfo(R.drawable.menu_my_dev, "我的设备", 0),
					new MenuItemInfo(R.drawable.menu_my_info, "我的信息", 0),
					new MenuItemInfo(R.drawable.menu_dev_pro, "设备维护", 0),};
			break;
		case TYPE_USER_INSPECT:
		case TYPE_USER_MAINTENANCE:
		case TYPE_USER_PROPERTY:
		case TYPE_USER_SUPERVISE:
			items = new MenuItemInfo[]{new MenuItemInfo(R.drawable.menu_my_task, "我的任务", 0),
					new MenuItemInfo(R.drawable.menu_my_notify, "我的告警", 0),
					new MenuItemInfo(R.drawable.menu_my_dev, "我的设备", 0),
					new MenuItemInfo(R.drawable.menu_my_info, "我的信息", 0),};
			break;
		default :
			items = new MenuItemInfo[]{new MenuItemInfo(R.drawable.menu_my_task, "我的任务", 0),
					new MenuItemInfo(R.drawable.menu_my_notify, "我的告警", 0),
					new MenuItemInfo(R.drawable.menu_my_dev, "我的设备", 0),
					new MenuItemInfo(R.drawable.menu_my_info, "我的信息", 0),
					new MenuItemInfo(R.drawable.menu_dev_pro, "设备维护", 0),};
			break;
		}
		menuNameList = Arrays.asList(items);
		return menuNameList;
	}

	@Override
	protected void dealProcessLogic() {
		if (JPushInterface.isPushStopped(getApplicationContext())) {
			JPushInterface.resumePush(getApplicationContext());
		}
		startService(new Intent(this, ControllerService.class));
		EventBus.getDefault().register(this);
		String userType = sharedPreferences.getString(AppConstant.KEY_USER_TYPE, "-1");
		if(Integer.valueOf(userType) == (TYPE_USER_ADMINISTRATOR)){//如果当前用户是管理员身份,进入设备配置fragment模块
			mConfigureLiftFragment = new ConnectLiftFragment();
			changeFragment(mConfigureLiftFragment);
		}else {
			myTaskFragment = new MyTaskFragment();
			changeFragment(myTaskFragment);
		}
		
		isNeedCheckUser = getIntent().getBooleanExtra(AppConstant.INTENT_KEY_ENTER_MAINACTIVITY_WITH_NO_NET, false);
		if (isNeedCheckUser) {
			registerNetReceiver();
		}
		
		wifiMan = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		setUserName();
		
		setUserType();
		
	}
	
	protected void onResume() {
		super.onResume();
		if (!AppUtil.hasNetwork(this)) {
			handler.sendEmptyMessageDelayed(0, 50);
		} else {
			if (mConfigureLiftFragment != null && mConfigureLiftFragment.isVisible()) { //当前加载显示的是设备维护Fragment
				if (!mConfigureLiftFragment.isConnectDevicesWifi) { // 当前不是扫描二维码动作
					new UpdateCountsTask(getApplicationContext()).updateCount();
				}
				mConfigureLiftFragment.isConnectDevicesWifi = false;
			} else { // 第一次进入设备维护Fragment或者其余Fragment默认执行告警数量的更新Task
				new UpdateCountsTask(getApplicationContext()).updateCount();
			}
		}
	};
	
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			PromptTopView.makeText(MainActivity.this, mTitleTextView, "世界上最遥远的距离就是没有网络,请检查设置!", PromptTopView.LENGTH_LONG).show();
		};
	};

	private void registerNetReceiver() {
		IntentFilter ifilter = new IntentFilter();
		ifilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		ifilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		netConnectChangeReceiver = new NetConnectChangeReceiver();
		registerReceiver(netConnectChangeReceiver, ifilter);
	} 
	
	private void unRegisterNetReceiver() {
		if (netConnectChangeReceiver != null) {
			unregisterReceiver(netConnectChangeReceiver);
		}
	}
	
	/**
	 * 监听网络变化
	 * @author Administrator
	 *
	 */
	private class NetConnectChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
				NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
				if (info.getState() == NetworkInfo.State.CONNECTED && isNeedCheckUser) {
					new UpdateCountsTask(getApplicationContext()).updateCount();
					confirmUser(getUserId(), getUserPassword());
				}
			}
		}
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
								unRegisterNetReceiver();
								break;
							case FLAG_LOGIN_FAILURE:// failure 用户名密码错误
								DialogUtil.showInfoDialog(MainActivity.this, "密码错误", "该用户密码已修改,请重新登录", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.cancel();
										startActivity(new Intent(MainActivity.this, LoginActivity.class));
										finish();
									}
								});
								break;
							}
						} catch (JSONException e) {
						}
					} else {
					}
				}
			});
		} catch (JSONException e) {
		}
	}
	
	private JSONObject initLoginJsonParameter(String username, String password) throws JSONException {
		JSONObject jsonParams = netWorkHelper.initJsonParameters(NetWorkCons.CMD_HTTP_LOGIN_AND_GET_LIFTINFOS);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_USERID, username, jsonParams);
		netWorkHelper.setDataInResponseJson(NetWorkCons.JSON_KEY_USERPWD, password, jsonParams);
		return jsonParams;
	}
	
	/**
	 * 订阅者模式
	 * @param menuItemInfo
	 */
	public void onEventMainThread(MenuItemInfo menuItemInfo) {
		setAlarmCount();
	}
	
	public void setAlarmCount() {
		int alarmCount = sharedPreferences.getInt(AppConstant.KEY_ALARM_COUNT, 0);
		menuItemList.set(1, new MenuItemInfo(R.drawable.menu_my_notify, "我的告警", alarmCount));
		menuItemHelper.notifyDataSetChanged();
	}

	/**
	 * 清除保存的SSID的信息
	 * @param SSID
	 */
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
		
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mConfigureLiftFragment != null) {
			mConfigureLiftFragment.unbindTheService();
		}
		unRegisterNetReceiver();
		EventBus.getDefault().unregister(this);
	}

	private void setUserName() {
		if (sharedPreferences.contains(AppConstant.KEY_USER_ID)) {
			mUserNameTv.setVisibility(View.VISIBLE);
			mUserNameTv.setText(getUserId());
		}
	}
	
	private void setUserType() {
		if (getUserType() != -1) {
			mUserTypeTv.setVisibility(View.VISIBLE);
			mUserTypeTv.setText(getResources().getStringArray(R.array.user_type)[getUserType()]);
		}
	}

	/**
	 * 清除连接过的AP
	 */
	private void clearConnectedAp() {
		LiftControllerSqlHelper database = new LiftControllerSqlHelper(this);
		SQLiteDatabase db = database.getWritableDatabase();
		Cursor c = db.query("apListTable",null,null,null,null,null,null);//查询并获得游标
		if(c.moveToFirst()){//判断游标是否为空
			String ssid = c.getString(c.getColumnIndex("ssid"));
	        clearWifiConfiguration(ssid);
		    while(c.moveToNext()) {
		        ssid = c.getString(c.getColumnIndex("ssid"));
		        clearWifiConfiguration(ssid);
		    }
		    c.close();
		}
		db.execSQL("delete from apListTable");
		db.close();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();
			} else {
				clearConnectDevicesTime();
				clearConnectedAp();
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
