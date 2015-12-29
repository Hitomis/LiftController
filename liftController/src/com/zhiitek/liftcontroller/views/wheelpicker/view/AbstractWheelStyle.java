package com.zhiitek.liftcontroller.views.wheelpicker.view;



import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

/**
 * 抽象滚轮样式
 * Abstract of WheelView's style
 */
abstract class AbstractWheelStyle {
    WheelView view;

    IWheelDirection direction;

    Scroller scroller;
    VelocityTracker tracker;
    Paint paint;
    Paint paintDecor;
    Rect rectDecorCurrentBG = new Rect();
    Rect rectDecorCurrentFG = new Rect();

    int centerX, centerY;
    int centerTextY;
    int lastPoint;
    int distanceSingleMove;
    int maxTextWidth, maxTextHeight;

    int unit;
    int radius;
    int width, height;
    int unitMoveMin, unitMoveMax;
    int finalUnit = -1;
    int unitDisplayMin, unitDisplayMax;
    int unitTotalMove;

    boolean isScrollingTerminal;
    boolean isStateLooped;

    String textCurrentItem;

    AbstractWheelStyle(WheelView view) {
        this.view = view;

        direction = WheelFactory.createWheelDirection(view.direction);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            scroller = new Scroller(view.getContext());
        } else {
            scroller = new Scroller(view.getContext(), null, true);
            scroller.setFriction(ViewConfiguration.getScrollFriction() / 50);
        }
        paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(view.textColor);
        paint.setTextSize(view.textSize);

        paintDecor = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
        paintDecor.setTextAlign(Paint.Align.CENTER);

        computeTextSize();
        computeWheel();
        computeUnitArea();
    }

    void computeUnitArea() {
        unitMoveMin = -unit * (view.data.size() - view.itemIndex - 1);
        unitMoveMax = unit * view.itemIndex;
    }

    void computeTextSize() {
        Rect textBounds = new Rect();
        maxTextWidth = 0;
        maxTextHeight = 0;
        if (view.hasSameSize && !view.data.isEmpty()) { // list<String>集合中第一个字符串的宽度
            String tmp = view.data.get(0);
            paint.getTextBounds(tmp, 0, tmp.length(), textBounds);
            maxTextWidth = textBounds.width();
            maxTextHeight = textBounds.height();
        } else if (!TextUtils.isEmpty(view.textWidthMaximum) && !TextUtils.isEmpty(view.textHeightMaximum)) { //指定字符串的宽度、高度
            paint.getTextBounds(view.textWidthMaximum, 0, view.textWidthMaximum.length(),
                    textBounds);
            maxTextWidth = textBounds.width();
            paint.getTextBounds(view.textHeightMaximum, 0, view.textHeightMaximum.length(),
                    textBounds);
            maxTextHeight = textBounds.height();
        } else if (view.itemIndexWidthMaximum != -1 && view.itemIndexHeightMaximum != -1 && !view.data.isEmpty()) { //list<String>集合下标index的字符宽度
            String tmp = view.data.get(view.itemIndexWidthMaximum);
            paint.getTextBounds(tmp, 0, tmp.length(), textBounds);
            maxTextWidth = textBounds.width();
            tmp = view.data.get(view.itemIndexHeightMaximum);
            paint.getTextBounds(tmp, 0, tmp.length(), textBounds);
            maxTextHeight = textBounds.height();
        } else {
            for (String tmp : view.data) {
                paint.getTextBounds(tmp, 0, tmp.length(), textBounds);
                maxTextWidth = Math.max(maxTextWidth, textBounds.width());
                maxTextHeight = Math.max(maxTextHeight, textBounds.height());
            }
        }
    }

    int measureSize(int mode, int sizeExpect, int sizeActual) {
        int realSize;
        if (mode == View.MeasureSpec.EXACTLY) {
            realSize = sizeExpect;
        } else {
            realSize = sizeActual;
            if (mode == View.MeasureSpec.AT_MOST) {
                realSize = Math.min(realSize, sizeExpect);
            }
        }
        return realSize;
    }

    abstract void computeWheel();

    void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int modeWidth = View.MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = View.MeasureSpec.getMode(heightMeasureSpec);

        int sizeWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = View.MeasureSpec.getSize(heightMeasureSpec);

        int width = direction.measureWidth(maxTextWidth, this.width);
        int height = direction.measureHeight(maxTextHeight, this.height);

        width = measureSize(modeWidth, sizeWidth, width);
        height = measureSize(modeHeight, sizeHeight, height);

        width += (view.getPaddingLeft() + view.getPaddingRight());
        height += (view.getPaddingTop() + view.getPaddingBottom());

        view.setWheelSize(width, height);
    }

    void onSizeChanged(int width, int height) {
        centerX = (int) (width / 2.0F);
        centerY = (int) (height / 2.0F);
        centerTextY = (int) (height / 2.0F - (paint.ascent() + paint.descent()) / 2.0F);

        textCurrentItem = view.data.get(view.itemIndex);
        if (null != view.listener) {
            view.listener.onWheelSelected(view.itemIndex, textCurrentItem);
            view.listener.onWheelScrollStateChanged(view.state);
        }

        computeCurrentDecorArea();
    }

    void computeCurrentDecorArea() {
        int tmp = maxTextHeight / 2 + view.itemSpace / 2;

        int left = 0;
        int top = centerY - tmp;
        int right = width + view.getPaddingRight() * 2;
        int bottom = centerY + tmp;
        if (!view.ignorePaddingDecorBG) {
            left = view.getPaddingLeft();
            right = width + view.getPaddingRight();
        }
        rectDecorCurrentBG.set(left, top, right, bottom);

        left = 0;
        top = centerY - tmp;
        right = width + view.getPaddingRight() * 2;
        bottom = centerY + tmp;
        if (!view.ignorePaddingDecorFG) {
            left = view.getPaddingLeft();
            right = width + view.getPaddingRight();
        }
        rectDecorCurrentFG.set(left, top, right, bottom);
    }

    void onDraw(Canvas canvas) {
        canvas.save();
        canvas.clipRect(rectDecorCurrentBG);
        if (null != view.decorBg)
            view.decorBg.drawDecor(canvas, rectDecorCurrentBG, paintDecor);
        canvas.restore();

        drawItems(canvas);

        canvas.save();
        canvas.clipRect(rectDecorCurrentFG);
        if (null != view.decorFg)
            view.decorFg.drawDecor(canvas, rectDecorCurrentFG, paintDecor);
        canvas.restore();
    }

    abstract void drawItems(Canvas canvas);

    void computeCurrentVelocity() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            tracker.computeCurrentVelocity(WheelCons.VELOCITY_TRACKER_UNITS_GEAR_SECOND);
        else
            tracker.computeCurrentVelocity(WheelCons.VELOCITY_TRACKER_UNITS_GEAR_FIRST);
    }

    void onTouchEvent(MotionEvent event) {
        if (null == tracker) tracker = VelocityTracker.obtain();
        tracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                view.interceptTouchEvent(true);
                isStateLooped = true;
                if (!scroller.isFinished()) scroller.abortAnimation();
                lastPoint = direction.getCurrentPoint(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (direction.isValidArea(event, view.getWidth(), view.getHeight())) break;
                if (null != view.listener && isStateLooped) {
                    if (view.state != WheelPicker.SCROLL_STATE_IDLE)
                        view.listener.onWheelScrolling();
                    int state = WheelPicker.SCROLL_STATE_DRAGGING;
                    if (state != view.state) {
                        view.state = state;
                        view.listener.onWheelScrollStateChanged(state);
                    }
                }
                onTouchEventMove(event);
                break;
            case MotionEvent.ACTION_UP:
                view.interceptTouchEvent(false);
                onTouchEventUp(event);
                tracker.recycle();
                tracker = null;
                break;
        }
        view.invalidate();
    }

    boolean checkScrollState() {
        if (unitTotalMove > unitMoveMax) {
            textCurrentItem = "";
            direction.startScroll(scroller, unitTotalMove, unitMoveMax - unitTotalMove, 600);
            return true;
        }
        if (unitTotalMove < unitMoveMin) {
            textCurrentItem = "";
            direction.startScroll(scroller, unitTotalMove, unitMoveMin - unitTotalMove, 600);
            return true;
        }
        return false;
    }

    abstract void onTouchEventMove(MotionEvent event);

    abstract void onTouchEventUp(MotionEvent event);

    void computeScroll() {
        if (scroller.computeScrollOffset()) {
            if (null != view.listener && isStateLooped) {
                view.listener.onWheelScrolling();
                int state = WheelPicker.SCROLL_STATE_SCROLLING;
                if (state != view.state) {
                    view.state = state;
                    view.listener.onWheelScrollStateChanged(state);
                }
            }
            unitTotalMove = direction.getCurrent(scroller);
            view.invalidate();
        }
        if (unitTotalMove == finalUnit && isScrollingTerminal) {
            isScrollingTerminal = false;
            if (unitTotalMove != 0) correctDegree();
        }
        if (unitTotalMove == finalUnit && unitTotalMove % unit == 0) {
            int tmpIndex = view.itemIndex - unitTotalMove / unit;
            if (tmpIndex < 0) tmpIndex = 0;
            if (tmpIndex >= view.data.size()) tmpIndex = view.data.size() - 1;
            String tmpData = view.data.get(tmpIndex);
            if (!tmpData.equals(textCurrentItem)) {
                textCurrentItem = tmpData;
                if (null != view.listener && isStateLooped) {
                    view.listener.onWheelSelected(tmpIndex, textCurrentItem);
                    int state = WheelPicker.SCROLL_STATE_IDLE;
                    if (state != view.state) {
                        isStateLooped = false;
                        view.state = state;
                        view.listener.onWheelScrollStateChanged(state);
                    }
                }
            }
        }
    }

    private void correctDegree() {
        int degreeRemainder = Math.abs(unitTotalMove % unit);
        if (degreeRemainder != 0) {
            if (degreeRemainder >= unit / 2.0F)
                correctScroll(degreeRemainder - unit, unit - degreeRemainder);
            else correctScroll(degreeRemainder, -degreeRemainder);
            view.invalidate();
        }
    }

    private void correctScroll(int endBack, int endForward) {
        if (unitTotalMove < 0) direction.startScroll(scroller, unitTotalMove, endBack, 300);
        else direction.startScroll(scroller, unitTotalMove, endForward, 300);
        finalUnit = direction.getFinal(scroller);
    }
}