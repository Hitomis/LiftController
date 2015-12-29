package com.zhiitek.liftcontroller.views.wheelpicker.view;


/**
 * 滚轮构建工厂
 * Factory to create WheelView's objects
 *
 * 修改方法入参
 */
final class WheelFactory {
    static AbstractWheelStyle createWheelStyle(int style, WheelView view) {
        AbstractWheelStyle wheel = null;
        switch (style) {
            case WheelPicker.STRAIGHT:
                wheel = new WheelStyleStraight(view);
                break;
            case WheelPicker.CURVED:
                wheel = new WheelStyleCurved(view);
                break;
        }
        return wheel;
    }

    static IWheelDirection createWheelDirection(int direction) {
        IWheelDirection wheel = null;
        switch (direction) {
            case WheelPicker.HORIZONTAL:
                wheel = new WheelDirectHor();
                break;
            case WheelPicker.VERTICAL:
                wheel = new WheelDirectVer();
                break;
        }
        return wheel;
    }
}