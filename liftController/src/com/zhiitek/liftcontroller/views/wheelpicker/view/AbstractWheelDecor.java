package com.zhiitek.liftcontroller.views.wheelpicker.view;



import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * 抽象滚轮装饰物
 * 滚轮装饰物功能将在下一个版本中启用
 * Abstract of WheelView's decoration
 * This function will be available in next version
 */
public abstract class AbstractWheelDecor {
    public abstract void drawDecor(Canvas canvas, Rect rect, Paint paint);
}