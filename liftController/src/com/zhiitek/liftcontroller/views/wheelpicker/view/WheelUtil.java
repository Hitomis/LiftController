package com.zhiitek.liftcontroller.views.wheelpicker.view;


import java.util.HashMap;

/**
 * 滚轮工具类
 * Utils of WheelView
 *
 * 增加数据缓存提升效率
 */
final class WheelUtil {
    private static final HashMap<Float, Integer> SPACES = new HashMap<Float, Integer>();
    private static final HashMap<Float, Integer> DEPTH = new HashMap<Float, Integer>();

    private static float radius;

    static int calculateRadius(int count, float length) {
        return (int) ((count + 1) * length / Math.PI);
    }

    static int calculateSpace(float degree, float radius) {
        if (radius != WheelUtil.radius) {
            WheelUtil.radius = radius;
            SPACES.clear();
        }
        int space;
        if (SPACES.containsKey(degree)) {
            space = SPACES.get(degree);
        } else {
            space = (int) (Math.sin(Math.toRadians(degree)) * radius);
            SPACES.put(degree, space);
        }
        return space;
    }

    static int calculateDegree(int count) {
        return (int) (180 * 1.0 / (count + 1));
    }

    static int calculateDegree(float dis, float radius) {
        return (int) Math.toDegrees(Math.asin(dis * 1.0 / radius));
    }

    static int calculateDepth(float degree, float radius) {
        if (radius != WheelUtil.radius) {
            WheelUtil.radius = radius;
            DEPTH.clear();
        }
        int depth;
        if (DEPTH.containsKey(degree)) {
            depth = DEPTH.get(degree);
        } else {
            depth = (int) (radius - Math.cos(Math.toRadians(degree)) * radius);
            DEPTH.put(degree, depth);
        }
        return depth;
    }
}