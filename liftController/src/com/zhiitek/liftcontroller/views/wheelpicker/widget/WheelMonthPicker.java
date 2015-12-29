package com.zhiitek.liftcontroller.views.wheelpicker.widget;



import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;

import com.zhiitek.liftcontroller.views.wheelpicker.view.WheelPicker;

/**
 * 基于WheelPicker的月份选择控件
 * MonthPicker base on WheelPicker
 */
public class WheelMonthPicker extends WheelPicker {
    private static final List<String> MONTHS = new ArrayList<String>();

    static {
        for (int i = 1; i <= 12; i++) MONTHS.add(String.valueOf(i));
    }

    private List<String> months = MONTHS;

    private int month;

    public WheelMonthPicker(Context context) {
        this(context, null);
    }

    public WheelMonthPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WheelMonthPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setData(months);
        setCurrentMonth(Calendar.getInstance().get(Calendar.MONTH) + 1);
    }

    @Override
    public void setData(List<String> data) {
        throw new RuntimeException("Set data will not allow here!");
    }

    public void setCurrentMonth(int month) {
        month = Math.max(month, 1);
        month = Math.min(month, 12);
        this.month = month;
        setItemIndex(month - 1);
    }
}