package com.zhiitek.liftcontroller.activity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.adapter.BaseAdapterHelper;
import com.zhiitek.liftcontroller.adapter.ViewHolder;
import com.zhiitek.liftcontroller.components.UdpSocketConnection;
import com.zhiitek.liftcontroller.fragment.BlackBoxDataQueryFragment;
import com.zhiitek.liftcontroller.fragment.BlackBoxDataSetupFragment;
import com.zhiitek.liftcontroller.fragment.BlackBoxDebugFragment;
import com.zhiitek.liftcontroller.fragment.BlackBoxDebugFragment.DebugCallBack;
import com.zhiitek.liftcontroller.fragment.BlackBoxOtherOperationFragment;
import com.zhiitek.liftcontroller.fragment.BlackBoxPasswordSetupFragment;
import com.zhiitek.liftcontroller.utils.BlackBoxUtil;
import com.zhiitek.liftcontroller.views.BlackBoxViewPager;

/**
 * 硬件调试具体页面
 * 
 * @author ZhaoFan
 *
 */
public class BlackBoxOperatingActivity extends BaseActivity implements DebugCallBack{
	
	private ListView lvMenu;
	
	private BlackBoxViewPager vpContent;
	
	private FragmentPagerAdapter fragPagerAdapter;
	
	private Map<String, Fragment> fragMap;
	
	{
		fragMap = new LinkedHashMap<String, Fragment>();
		fragMap.put("参数查询", BlackBoxDataQueryFragment.newInstance());
		fragMap.put("参数配置", BlackBoxDataSetupFragment.newInstance());
		fragMap.put("设备调试", BlackBoxDebugFragment.newInstance());
		fragMap.put("口令设置", BlackBoxPasswordSetupFragment.newInstance());
//		fragMap.put("网络设置", BlackBoxConnUrlSetupFragment.newInstance());
		fragMap.put("其它操作", BlackBoxOtherOperationFragment.newInstance());
	}

	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_blackbox_operating);
	}

	@Override
	protected void findViewById() {
		lvMenu = (ListView) findViewById(R.id.lv_menu);
		vpContent = (BlackBoxViewPager) findViewById(R.id.id_vp);
	}

	@Override
	protected void setListener() {
		lvMenu.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				exceptIndicator(position, view);
				vpContent.setCurrentItem(position);
				if (position == 0) {
					((BlackBoxDataQueryFragment)((FragmentPagerAdapter)vpContent.getAdapter()).getItem(0)).queryData();
				}
			}

		});
		
		vpContent.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {}
		});
	}
	
	/**
	 * 选择后Item UI显示[显示蓝色条并且背景变色]
	 * @param currClickPostion
	 * @param view
	 */
	private void exceptIndicator(int currClickPostion, View view) {
		setSelctionUI(view);
		for (int i = 0; i < lvMenu.getChildCount(); i++) {
			View child = lvMenu.getChildAt(i);
			if (i != currClickPostion) {
				child.setBackgroundResource(0);
				child.findViewById(R.id.id_indicator).setVisibility(View.INVISIBLE);
			}
		}
	}

	/**
	 * 处理ListView中选中的条目UI样式
	 * @param view
	 */
	private void setSelctionUI(View view) {
		view.findViewById(R.id.id_indicator).setVisibility(View.VISIBLE);
		view.setBackgroundResource(R.color.module_backgroud_color);
	}

	@Override
	protected void dealProcessLogic() {
		setTitleBar("轿顶黑盒操作", null);
		
		initMenu();
		
		initFragment();
		
		lvMenu.post(new Runnable() {
			
			@Override
			public void run() {
				View child = lvMenu.getChildAt(0);
				if (child != null) {
					setSelctionUI(child);
				}
			}
		});
	}

	private void initMenu() {
		List<String> menuList = createMenuItemData();
		BaseAdapterHelper<String> menuAdapterHelper = new BaseAdapterHelper<String>(BlackBoxOperatingActivity.this, menuList, R.layout.item_blackbox_operating_menu) {
			
			@Override
			public void convert(ViewHolder viewHolder, String item) {
				viewHolder.setText(R.id.tv_item_name, item);
			}
		};
		lvMenu.setAdapter(menuAdapterHelper);
	}
	
	private void initFragment() {
		final List<Fragment> fragmentList = createFragmentInstance();

		fragPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
			@Override
			public int getCount() {
				return fragmentList.size();
			}

			@Override
			public Fragment getItem(int position) {
				return fragmentList.get(position);
			}
		};
		vpContent.setAdapter(fragPagerAdapter);
	}

	/**
	 * 创建菜单对应的fragment实例
	 * @return
	 */
	private List<Fragment> createFragmentInstance() {
		return new ArrayList<Fragment>(fragMap.values());
	}

	/**
	 * 创建菜单项
	 * @return
	 */
	private List<String> createMenuItemData() {
		return new ArrayList<String>(fragMap.keySet());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		disConnect();
	}

	private void disConnect() {
		UdpSocketConnection udp = new UdpSocketConnection(this);
		udp.post(BlackBoxUtil.makeSendCommand(0x0EF), null);
		udp.shutdown();
	}

	/**
	 * 开启调式模式，禁用其他操作，直至手动关闭调试模式
	 */
	@Override
	public void beginDebug() {
		ArrayList<String> menutitle = (ArrayList<String>) createMenuItemData();
		lvMenu.setEnabled(false);
		for (int i = 0; i < lvMenu.getChildCount(); i++) {
			View child = lvMenu.getChildAt(i);
			if (i != menutitle.indexOf("设备调试")) {
				((TextView)child.findViewById(R.id.tv_item_name)).setTextColor(Color.LTGRAY);
			}
		}
	}

	/**
	 * 关闭调试模式
	 */
	@Override
	public void endDebug() {
		lvMenu.setEnabled(true);
		for (int i = 0; i < lvMenu.getChildCount(); i++) {
			View child = lvMenu.getChildAt(i);
			((TextView)child.findViewById(R.id.tv_item_name)).setTextColor(getResources().getColor(R.color.text_color));
		}
	}
	
}
