package com.zhiitek.liftcontroller.views.wheelpicker.view;



import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.Scroller;

/**
 * 滚轮方向抽象接口
 * Interface of WheelView's direction
 */
interface IWheelDirection {
    int measureWidth(int sizeA, int sizeB);

    int measureHeight(int sizeA, int sizeB);

    int getCurrentPoint(MotionEvent event);

    boolean isValidArea(MotionEvent event, int width, int height);

    void startScroll(Scroller scroller, int start, int distance, int duration);

    void startFling(Scroller scroller, VelocityTracker tracker, int start, int disMin, int disMax);

    int getFinal(Scroller scroller);

    int getCurrent(Scroller scroller);

    int computeStraightWidth(int count, int space, int width, int height);

    int computeStraightHeight(int count, int space, int width, int height);

    int computeCurvedWidth(int count, int space, int width, int height);

    int computeCurvedHeight(int count, int space, int width, int height);

    int computeUnitStraight(int width, int height, int count);

    int computeRadiusCurved(int width, int height);

    int getUnitDisplayRule(int width, int height);
}