package com.zhiitek.liftcontroller.views.wheelpicker.widget;



import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;

import com.zhiitek.liftcontroller.views.wheelpicker.view.WheelPicker;

/**
 * 基于WheelPicker的年份选择控件
 * YearPicker base on WheelPicker
 */
public class WheelYearPicker extends WheelPicker {
    private static final List<String> YEARS = new ArrayList<String>();
    private static final int FROM = 1900, TO = 2100;

    static {
        for (int i = 1900; i <= 2100; i++) YEARS.add(String.valueOf(i));
    }

    private List<String> years = YEARS;

    private int from = FROM, to = TO;
    private int year;

    public WheelYearPicker(Context context) {
        this(context, null);
    }

    public WheelYearPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WheelYearPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setData(years);
        setCurrentYear(Calendar.getInstance().get(Calendar.YEAR));
    }

    @Override
    public void setData(List<String> data) {
        throw new RuntimeException("Set data will not allow here!");
    }

    public void setYearRange(int yearFrom, int yearTo) {
        from = yearFrom;
        to = yearTo;
        years.clear();
        for (int i = yearFrom; i <= yearTo; i++) years.add(String.valueOf(i));
        super.setData(years);
    }

    public void setCurrentYear(int year) {
        year = Math.max(year, from);
        year = Math.min(year, to);
        this.year = year;
        int d = year - from;
        setItemIndex(d);
    }
}