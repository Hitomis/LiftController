package com.zhiitek.liftcontroller.views.wheelpicker.widget;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;

import com.zhiitek.liftcontroller.views.wheelpicker.view.WheelPicker;

public class WheelMinutePicker extends WheelPicker {

	private static final List<String> MINUTES = new ArrayList<String>();
	private static final int FROM = 0, TO = 59;

	static {
		for (int i = FROM; i <= TO; i++)
			MINUTES.add(String.valueOf(i));
	}

	private List<String> minutes = MINUTES;

	private int from = FROM, to = TO;	

	public WheelMinutePicker(Context context) {
		this(context, null);
	}

	public WheelMinutePicker(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WheelMinutePicker(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		super.setData(minutes);
		setCurrentMinute(Calendar.getInstance().get(Calendar.MINUTE));
	}

	@Override
	public void setData(List<String> data) {
		throw new RuntimeException("Set data will not allow here!");
	}

	public void setCurrentMinute(int minute) {
		minute = Math.max(minute, from);
		minute = Math.min(minute, to);
		int d = minute - from;
		setItemIndex(d);
	}

}
