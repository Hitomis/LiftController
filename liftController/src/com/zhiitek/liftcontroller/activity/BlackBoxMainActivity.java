package com.zhiitek.liftcontroller.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;

import com.zbar.lib.CaptureActivity;
import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.fragment.BlackBoxDevicesDockingFragment;
import com.zhiitek.liftcontroller.fragment.BlackBoxEthernetPortFragment;
import com.zhiitek.liftcontroller.utils.AppUtil;
import com.zhiitek.liftcontroller.views.SwipeFinishLayout;
import com.zhiitek.liftcontroller.views.ViewPagerIndicator;
import com.zhiitek.liftcontroller.views.ViewPagerIndicator.PageChangeListener;

/**
 * 硬件调试主页面
 * 
 * @author ZhaoFan
 *
 */
public class BlackBoxMainActivity extends BaseActivity implements
		OnClickListener {

	private ViewPagerIndicator viewPagerIndicator;

	private ViewPager viewPager;

	private FragmentPagerAdapter mAdapter;
	
	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_blackbox_main);
	}

	@Override
	protected void findViewById() {
		viewPagerIndicator = (ViewPagerIndicator) findViewById(R.id.id_indicator);
		viewPager = (ViewPager) findViewById(R.id.id_vp);
	}

	@Override
	protected void setListener() {
		setTitleBar("硬件调试", this);
	}
	
	@Override
	protected int editRightImageResource() {
		return R.drawable.right_scan;
	}

	@Override
	protected void dealProcessLogic() {
		SwipeFinishLayout.attachToActivity(this);
		initDatas();
		// 设置Tab上的标题
		viewPagerIndicator.setTabItemTitles(createTabItemTitles());
		viewPager.setAdapter(mAdapter);
		// 设置关联的ViewPager
		viewPagerIndicator.setViewPager(viewPager, 0);
		viewPagerIndicator.setOnPageChangeListener(new PageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				if (position == 0) {
					changeRightImageResource(R.drawable.right_scan);
				} else if (position == 1) {
					changeRightImageResource(0);
				}
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
			
			@Override
			public void onPageScrollStateChanged(int state) {}
		});
		
	}

	private void initDatas() {
		final List<Fragment> mTabContents = new ArrayList<Fragment>();
		mTabContents.add(BlackBoxDevicesDockingFragment.newInstance());
		mTabContents.add(BlackBoxEthernetPortFragment.newInstance());

		mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
			@Override
			public int getCount() {
				return mTabContents.size();
			}

			@Override
			public Fragment getItem(int position) {
				return mTabContents.get(position);
			}
		};
	}

	private List<String> createTabItemTitles() {
		final List<String> mTabItemTitles = new ArrayList<String>();
		mTabItemTitles.add("设备对接");
		mTabItemTitles.add("网口对接");
		return mTabItemTitles;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_right:// titlebar 右边按钮点击事件
			if (!AppUtil.isEnabledWifi(this)) {
				showToast("请设置wifi为启动状态后再进行扫描操作");
			} else {
				mAdapter.getItem(0).startActivityForResult(new Intent(this, CaptureActivity.class), 0);
			}
			break;

		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
