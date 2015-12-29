package com.zhiitek.liftcontroller.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 
 * ViewPager禁止左右滑动
 * 
 * @author ZhaoFan
 *
 */
public class BlackBoxViewPager extends ViewPager {

	/** 默认ViewPager不可左右滑动 */
	private boolean isScrolling = false;

	public BlackBoxViewPager(Context context) {
		this(context, null);
	}

	public BlackBoxViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if (isScrolling) {
			return super.onTouchEvent(event);
		}

		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		
		if (isScrolling) {
			return super.onInterceptTouchEvent(event);
		}

		return false;
	}

	public boolean isScrolling() {
		return isScrolling;
	}

	public void setScrolling(boolean isScrolling) {
		this.isScrolling = isScrolling;
	}
	
}
