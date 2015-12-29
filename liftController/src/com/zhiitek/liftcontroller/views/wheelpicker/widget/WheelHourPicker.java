package com.zhiitek.liftcontroller.views.wheelpicker.widget;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;

import com.zhiitek.liftcontroller.views.wheelpicker.view.WheelPicker;


class WheelHourPicker extends WheelPicker{

    private static final List<String> HOURS = new ArrayList<String>();
    private static final int FROM = 0, TO = 23;

    static {
        for (int i = FROM; i <= TO; i++) HOURS.add(String.valueOf(i));
    }

    private List<String> hours = HOURS;

    private int from = FROM, to = TO;

    public WheelHourPicker(Context context) {
        this(context, null);
    }

    public WheelHourPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WheelHourPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setData(hours);
        setCurrentHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
    }

    @Override
    public void setData(List<String> data) {
        throw new RuntimeException("Set data will not allow here!");
    }

    public void setCurrentHour(int hour) {
    	hour = Math.max(hour, from);
    	hour = Math.min(hour, to);
        int d = hour - from;
        setItemIndex(d);
    }
}