package com.zhiitek.liftcontroller.views;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.zhiitek.liftcontroller.R;


public class SwipeFinishLayout extends FrameLayout {

    private static final int MIN_FLING_VELOCITY = 400;

    private static final int FULL_ALPHA = 255;

    private static final int OVERSCROLL_DISTANCE = 10;

    private float mScrollThreshold = .3f;

    private int scrimColor = 0x99000000;

    private Activity activity;

    private View contentView;

    private ViewDragHelper vDragHelper;

    private float scrollPercent;

    private int contentLeft;

    private Drawable shadowLeft;

    private float scrimOpacity;

    private Rect tmpRect = new Rect();


    public SwipeFinishLayout(Context context) {
        this(context, null);
    }

    public SwipeFinishLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeFinishLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        vDragHelper = ViewDragHelper.create(this, new ViewDragCallback());
        vDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);

        float density = getResources().getDisplayMetrics().density;
        float minVel = MIN_FLING_VELOCITY * density;

        vDragHelper.setMinVelocity(minVel);

        shadowLeft = getResources().getDrawable(R.drawable.shadow_left);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return vDragHelper.shouldInterceptTouchEvent(ev);
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        vDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (contentView != null) {
            contentView.layout(contentLeft, 0,
                    contentLeft + contentView.getMeasuredWidth(),
                    contentView.getMeasuredHeight());
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        final boolean isdrawContent = child == contentView;
        boolean ret = super.drawChild(canvas, child, drawingTime);
        if (scrimOpacity > 0 && isdrawContent && vDragHelper.getViewDragState() != ViewDragHelper.STATE_IDLE) {
            drawShadow(canvas, child);
            drawShadowScrim(canvas, child);
        }
        return ret;
    }

    /**
     * 绘制Activity左侧的阴影片区
     *
     * @param canvas
     * @param child
     */
    private void drawShadowScrim(Canvas canvas, View child) {
        final int baseAlpha = (scrimColor & 0xff000000) >>> 24;
        final int alpha = (int) (baseAlpha * scrimOpacity);
        final int color = alpha << 24 | (scrimColor & 0xffffff);

        canvas.clipRect(0, 0, child.getLeft(), getHeight());
        canvas.drawColor(color);
    }

    /**
     * 绘制紧挨Activity左侧的影子
     *
     * @param canvas
     * @param child
     */
    private void drawShadow(Canvas canvas, View child) {
        final Rect childRect = tmpRect;
        child.getHitRect(childRect);
        shadowLeft.setBounds(childRect.left - shadowLeft.getIntrinsicWidth(), childRect.top, childRect.left, childRect.bottom);
        shadowLeft.setAlpha((int) (scrimOpacity * FULL_ALPHA));
        shadowLeft.draw(canvas);
    }

    public void replaceDecorChild(Activity act) {
        activity = act;

        act.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        act.getWindow().getDecorView().setBackgroundDrawable(null);

        TypedArray typedArray = act.getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.windowBackground
        });
        int background = typedArray.getResourceId(0, 0);
        typedArray.recycle();

        ViewGroup vgDecor = (ViewGroup) act.getWindow().getDecorView();//DecorView
        ViewGroup decorChild = (ViewGroup) vgDecor.getChildAt(0);//LinearLayout

        /*
         *  一个窗口的布局容器层次为 {PhoneWindow{DecorView{LinearLayout{FrameLayout{···用户自己的ViewGroup}}}}}
         *  这里的“父容器”LinearLayout这一层
         */
        // 从decor中移除系统的“父容器”
        vgDecor.removeView(decorChild);
        // 设置“父容器”背景为透明
        decorChild.setBackgroundResource(background);
        // 将已经设置为透明背景的“父容器”放置到我们的SwipeFinishLayout容器中
        addView(decorChild);
        // 保存父容器对象值
        contentView = decorChild;
        //将我们的SwipeFinishLayout容器作为“父容器”
        vgDecor.addView(this);
    }

    @Override
    public void computeScroll() {
        scrimOpacity = 1 - scrollPercent;
        if (vDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View view, int i) {
            boolean isEdgeTouched = vDragHelper.isEdgeTouched(ViewDragHelper.EDGE_LEFT);
            boolean directionCheck = !vDragHelper.checkTouchSlop(ViewDragHelper.DIRECTION_VERTICAL, i);
            return isEdgeTouched & directionCheck;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return ViewDragHelper.EDGE_LEFT;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return 0;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            scrollPercent = Math.abs((float) left / (contentView.getWidth() + shadowLeft.getIntrinsicWidth()));

            contentLeft = left;
            invalidate();

            if (scrollPercent >= 1 && !activity.isFinishing()) {
                activity.finish();
                activity.overridePendingTransition(0, 0);
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            int childWidth = releasedChild.getWidth();
            int left = xvel > 0 || xvel == 0 && scrollPercent > mScrollThreshold
                    ? childWidth + shadowLeft.getIntrinsicWidth() + OVERSCROLL_DISTANCE : 0;
            vDragHelper.settleCapturedViewAt(left, 0);
            invalidate();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return Math.min(child.getWidth(), Math.max(left, 0));
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return 0;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
        }

    }

    /**
     * 使activity附加“从左边界向右滑动执行Finish”的功能
     * @param activity
     */
    public static void attachToActivity(Activity activity) {
        SwipeFinishLayout mSwipeFinishLayout = new SwipeFinishLayout(activity);
        mSwipeFinishLayout.replaceDecorChild(activity);
    }

}
