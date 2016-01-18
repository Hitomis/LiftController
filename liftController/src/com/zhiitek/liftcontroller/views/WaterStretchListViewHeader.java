package com.zhiitek.liftcontroller.views;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.utils.AppUtil;


/**
 * 下拉刷新头部控件
 *
 * @author ZhaoFan
 */
public class WaterStretchListViewHeader extends FrameLayout {

    private static final int DISTANCE_BETWEEN_STRETCH_READY = 150;

    /**
     * 上下内边距
     */
    private static final int PADDING_SPACE = 10;

    /**
     * WaterStretchView拉伸的高度
     */
    private int stretchHeight;

    /**
     * WaterStretchView可拉伸的最大高度
     */
    private int readyHeight;

    private LinearLayout mContainer;

    private SpinFadeLoaderView mLoaderView;

    private WaterStretchView mWaterStretchView;

    private TextView mTextveiw;

    private STATE mState = STATE.normal;

    private StateChangedListener mStateChangedListener;

    public enum STATE {
        normal,//正常
        stretch,//准备进行拉伸
        ready,//拉伸到最大位置
        refreshing,//刷新
        ok,//刷新成功
        end//刷新结束，回滚
    }

    public WaterStretchListViewHeader(Context context) {
        this(context, null);
    }

    /**
     * @param context
     * @param attrs
     */
    public WaterStretchListViewHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        mContainer = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.layout_waterstretchlistview_header, null);
        mLoaderView = (SpinFadeLoaderView) mContainer.findViewById(R.id.waterstretchlist_loaderbar);
        mWaterStretchView = (WaterStretchView) mContainer.findViewById(R.id.waterstreteclist_waterstretch);
        mTextveiw = (TextView) mContainer.findViewById(R.id.waterstretchlist_textveiw);
        // 初始情况，设置下拉刷新view高度为0
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        addView(mContainer, lp);
        initHeight();
    }

    private void initHeight() {

        mContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                stretchHeight = mWaterStretchView.getHeight() + PADDING_SPACE * 2;
                readyHeight = stretchHeight + DISTANCE_BETWEEN_STRETCH_READY;
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

    }

    /**
     * 修改状态。注：状态的改变与前一个状态以及下拉头高度有关
     *
     * @param state
     */
    public void updateState(STATE state) {
        if (state == mState) return;
        STATE oldState = mState;
        mState = state;
        if (mStateChangedListener != null) {
            mStateChangedListener.notifyStateChanged(oldState, mState);
        }

        switch (mState) {
            case normal:
                handleStateNormal();
                break;
            case stretch:
                handleStateStretch();
                break;
            case ready:
                handleStateReady();
                break;
            case refreshing:
                handleStateRefreshing();
                break;
            case ok:
                handleOk();
            case end:
                handleStateEnd();
                break;
            default:
        }
    }

    /**
     * 处理处于normal状态的值
     */
    private void handleStateNormal() {
        mContainer.setPadding(0, 0, 0, PADDING_SPACE);
        mContainer.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        mWaterStretchView.setVisibility(View.VISIBLE);
        mTextveiw.setVisibility(View.GONE);
        mLoaderView.setVisibility(View.GONE);
    }

    /**
     * 处理水滴拉伸状态
     */
    private void handleStateStretch() {
        mContainer.setPadding(0, PADDING_SPACE, 0, PADDING_SPACE);
        mContainer.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        mWaterStretchView.setVisibility(View.VISIBLE);
        mLoaderView.setVisibility(View.GONE);
        mTextveiw.setVisibility(View.GONE);
    }

    /**
     * 处理水滴ready状态，回弹效果
     */
    @SuppressLint("NewApi")
    private void handleStateReady() {
        mContainer.setPadding(0, PADDING_SPACE, 0, PADDING_SPACE);
        mWaterStretchView.setVisibility(View.VISIBLE);
        mLoaderView.setVisibility(View.GONE);
        mTextveiw.setVisibility(View.GONE);
        Animator shrinkAnimator = mWaterStretchView.createAnimator();
        shrinkAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //回弹结束后即进入refreshing状态
                updateState(STATE.refreshing);
            }
        });
        shrinkAnimator.start();//开始回弹
    }

    /**
     * 处理正在进行刷新状态
     */
    private void handleStateRefreshing() {
        mContainer.setPadding(0, PADDING_SPACE, 0, PADDING_SPACE);
        mWaterStretchView.setVisibility(View.GONE);
        mTextveiw.setVisibility(View.GONE);
        mLoaderView.setVisibility(View.VISIBLE);
    }

    /**
     * 处理刷新成功状态
     */
    private void handleOk() {
        mContainer.setPadding(0, PADDING_SPACE, 0, PADDING_SPACE);
        mContainer.setGravity(Gravity.CENTER);
        mWaterStretchView.setVisibility(View.GONE);
        mLoaderView.setVisibility(View.GONE);
        mTextveiw.setVisibility(View.VISIBLE);
    }

    /**
     * 处理刷新完毕状态
     */
    private void handleStateEnd() {
        mContainer.setPadding(0, PADDING_SPACE, 0, PADDING_SPACE);
        mWaterStretchView.setVisibility(View.GONE);
        mLoaderView.setVisibility(View.GONE);
    }

    /**
     * 设置当前下拉刷新控件可见高度
     *
     * @param height
     */
    public void setVisiableHeight(int height) {
        if (height < 0) height = 0;
        LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
        lp.height = height;
        mContainer.setLayoutParams(lp);
        //通知水滴进行更新
        if (mState == STATE.stretch) {
            float pullOffset = (float) AppUtil.mapValueFromRangeToRange(height, stretchHeight, readyHeight, 0, 1);
            mWaterStretchView.updateComleteState(pullOffset);
        }

    }

    public int getVisiableHeight() {
        return mContainer.getHeight();
    }

    public STATE getCurrentState() {
        return mState;
    }

    public int getStretchHeight() {
        return stretchHeight;
    }

    public int getReadyHeight() {
        return readyHeight;
    }

    public void setStateChangedListener(StateChangedListener l) {
        mStateChangedListener = l;
    }

    public interface StateChangedListener {
        public void notifyStateChanged(STATE oldState, STATE newState);
    }
}
