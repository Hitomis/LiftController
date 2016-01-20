package com.zhiitek.liftcontroller.views;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Scroller;

public class WaterStretchListView extends ListView implements OnScrollListener, WaterStretchListViewHeader.StateChangedListener {

	/** 最后触发事件event对象的getY值 */
	private float mLastY = -1;

	/** 是否允许下拉刷新 */
	private boolean mEnablePullRefresh = true;

	/** 是否允许上推加载更多 */
	private boolean mEnablePushLoad;
	
	/** 是否正在刷新 */
	private boolean mPullLoading;
	
	/** FooterView是否是Ready状态 */
	private boolean mIsFooterReady = false;

	/** 当前条目的总条数 */
	private int mTotalItemCount;

	private ScrollStatus mScrollStatus;
	/** 手指是否触摸屏幕 */
	private boolean isTouchingScreen = false;

	/** HeaderView或者FooterVie回滚动画的时长 */
	private final static int SCROLL_DURATION = 400;
	
	/** 当上推PULL_LOAD_MORE_DELTA个像素值, 触发加载更多事件 */
	private final static int PULL_LOAD_MORE_DELTA = 50;
	
	/** 降低当手指移动对应控件滚动的速率, 起粘着效果 */
	private final static float OFFSET_RADIO = 1.5f;
	
	/** 用来让下拉的头部回滚回去 */
	private Scroller mScroller;
	
	private OnScrollListener mScrollListener;
	
	private WaterStretchListener mListViewListener;
	
	private WaterStretchListViewHeader mHeaderView;
	
	private WaterStretchListViewFooter mFooterView;

	private enum ScrollStatus{
		header,
		footer
	}

	/**
	 * @param context
	 */
	public WaterStretchListView(Context context) {
		this(context, null);
	}

	public WaterStretchListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WaterStretchListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setFooterDividersEnabled(false);
		initWithContext(context);
	}

	private void initWithContext(Context context) {

		mScroller = new Scroller(context, new DecelerateInterpolator());
		super.setOnScrollListener(this);

		// init header view
		mHeaderView = new WaterStretchListViewHeader(context);
		mHeaderView.setStateChangedListener(this);
		addHeaderView(mHeaderView);

		// init footer view
		mFooterView = new WaterStretchListViewFooter(context);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (mIsFooterReady == false) {
			mIsFooterReady = true;
			addFooterView(mFooterView);
		}
		super.setAdapter(adapter);
	}

	/**
	 * 设置是否允许上推加载更多
	 * @param enable
	 */
	public void setPushLoadEnable(boolean enable) {
		mEnablePushLoad = enable;
		if (!mEnablePushLoad) {
			mFooterView.hide();
			mFooterView.setOnClickListener(null);
		} else {
			mPullLoading = false;
			mFooterView.show();
			mFooterView.setState(WaterStretchListViewFooter.STATE.normal);
			mFooterView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mFooterView.setEnabled(false);
					startLoadMore();
				}
			});
		}
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mHeaderView.updateState(WaterStretchListViewHeader.STATE.end);
			if(!isTouchingScreen){
				resetHeaderHeight();
			}
		}
	};

	/**
	 * 停止刷新, 并回滚HeaderView
	 */
	public void stopRefresh(boolean isSuccess) {
		if (mHeaderView.getCurrentState() == WaterStretchListViewHeader.STATE.refreshing) {
			if (isSuccess) {
				mHeaderView.updateState(WaterStretchListViewHeader.STATE.ok);
				handler.sendEmptyMessageDelayed(0, 500);
			} else {
				handler.sendEmptyMessage(0);
			}
		}else{
			throw  new IllegalStateException("can not stop refresh while it is not refreshing!");
		}
	}

	/**
	 * 停止加载更多, 并回滚FooterView
	 */
	public void stopLoadMore() {
		if (mPullLoading == true) {
			mPullLoading = false;
			mFooterView.setState(WaterStretchListViewFooter.STATE.normal);
		}
		mFooterView.setEnabled(true);
	}


	private void invokeOnScrolling() {
		if (mScrollListener instanceof OnWaterStretchScrollListener) {
			OnWaterStretchScrollListener l = (OnWaterStretchScrollListener) mScrollListener;
			l.onScrolling(this);
		}
	}

	private void updateHeaderHeight(int height){
		if (mEnablePullRefresh) {
			if (mHeaderView.getCurrentState() == WaterStretchListViewHeader.STATE.normal && height >= mHeaderView.getStretchHeight()) {
				//由normal变成stretch的逻辑：1、当前状态是normal；2、下拉头达到了stretchheight的高度
				mHeaderView.updateState(WaterStretchListViewHeader.STATE.stretch);
			} else if(mHeaderView.getCurrentState() == WaterStretchListViewHeader.STATE.stretch && height >= mHeaderView.getReadyHeight()){
				//由stretch变成ready的逻辑：1、当前状态是stretch；2、下拉头达到了readyheight的高度
				mHeaderView.updateState(WaterStretchListViewHeader.STATE.ready);
			} else if (mHeaderView.getCurrentState() == WaterStretchListViewHeader.STATE.stretch && height < mHeaderView.getStretchHeight() && !isTouchingScreen){
				// 添加最后一个条件是为了解决:直接从stretch状态(这里的stretch状态之前的并不是normal状态)到ready状态的时候，resetHeaderHeight执行错误的bug。
				// 由stretch变成normal的逻辑：1、当前状态是stretch；2、下拉头高度小于stretchheight的高度
				mHeaderView.updateState(WaterStretchListViewHeader.STATE.normal);
			} else if(mHeaderView.getCurrentState() == WaterStretchListViewHeader.STATE.end && height < 2){
				//由end变成normal的逻辑：1、当前状态是end；2、下拉头高度小于一个极小值
				mHeaderView.updateState(WaterStretchListViewHeader.STATE.normal);
			}
		}
		mHeaderView.setVisiableHeight(height);//动态设置HeaderView的高度
	}

	private void updateHeaderHeight(float delta) {
//		System.out.println("delta:" + delta + "vis:" + mHeaderView.getVisiableHeight());
		int newHeight = (int) delta + mHeaderView.getVisiableHeight();
		updateHeaderHeight(newHeight);
	}

	/**
	 * reset header view's height.
	 * 重置headerheight的高度
	 * 逻辑：1、如果状态处于非refreshing，则回滚到height=0状态2；2、如果状态处于refreshing，则回滚到stretchheight高度
	 */
	private void resetHeaderHeight() {
		int height = mHeaderView.getVisiableHeight();
		if (height == 0) return;
		// 当前HeaderView的状态是正在刷新状态或者显示的高度小于headerView的伸展高度则不做处理
		if (mHeaderView.getCurrentState() == WaterStretchListViewHeader.STATE.refreshing 
				&& height <= mHeaderView.getStretchHeight()) 
			return;

		int finalHeight = 0; // default: scroll back to dismiss header.
		// is refreshing, just scroll back to show all the header.
		if ((mHeaderView.getCurrentState() == WaterStretchListViewHeader.STATE.ready ||mHeaderView.getCurrentState() == WaterStretchListViewHeader.STATE.refreshing)&& height > mHeaderView.getStretchHeight()) {
			finalHeight = mHeaderView.getStretchHeight();
		}

		mScrollStatus = ScrollStatus.header;
		if (finalHeight ==0) {
			System.out.println("current:" + mHeaderView.getCurrentState());
		}
		
		mScroller.startScroll(0, height, 0, finalHeight - height, SCROLL_DURATION);
		// 触发 computeScroll()方法
		invalidate();
	}

	private void updateFooterHeight(float delta) {

		int height = mFooterView.getBottomMargin() + (int) delta;
		if (mEnablePushLoad && !mPullLoading) {
			if (height > PULL_LOAD_MORE_DELTA) { // height enough to invoke load more.
				mFooterView.setState(WaterStretchListViewFooter.STATE.ready);
			} else {
				mFooterView.setState(WaterStretchListViewFooter.STATE.normal);
			}
		}
		mFooterView.setBottomMargin(height);
	}


	private void resetFooterHeight() {
		int bottomMargin = mFooterView.getBottomMargin();
		if (bottomMargin > 0) {
			mScrollStatus = ScrollStatus.footer;
			mScroller.startScroll(0, bottomMargin, 0, -bottomMargin, SCROLL_DURATION);
			invalidate();
		}
	}

	private void startLoadMore() {
		mPullLoading = true;
		mFooterView.setState(WaterStretchListViewFooter.STATE.loading);
		if (mListViewListener != null) {
			mListViewListener.onLoadMore();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mLastY == -1) {
			mLastY = ev.getRawY();
		}
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastY = ev.getRawY();
			isTouchingScreen = true;
			break;
		case MotionEvent.ACTION_MOVE:
			final float deltaY = ev.getRawY() - mLastY;
			mLastY = ev.getRawY();
			if (getFirstVisiblePosition() == 0 && (mHeaderView.getVisiableHeight() > 0 || deltaY > 0)) {
				// the first item is showing, header has shown or pull down.
				updateHeaderHeight(deltaY / OFFSET_RADIO);
				invokeOnScrolling();
			} else if (getLastVisiblePosition() == mTotalItemCount - 1 && (mFooterView.getBottomMargin() > 0 || deltaY < 0)) {
				// last item, already pulled up or want to pull up.
				updateFooterHeight(-deltaY / OFFSET_RADIO);
			}
			break;
		default:
			mLastY = -1; // reset
			isTouchingScreen = false;
			//TODO 存在bug：当两个if的条件都满足的时候，只能滚动一个，所以在reSetHeader的时候就不起作用了，一般就只会reSetFooter
			if (getFirstVisiblePosition() == 0) {
				resetHeaderHeight();
			}
			
			if (getLastVisiblePosition() == mTotalItemCount - 1) {
				// invoke load more.
				if (mEnablePushLoad && mFooterView.getBottomMargin() > PULL_LOAD_MORE_DELTA) {
					startLoadMore();
				}
				resetFooterHeight();
			}
			break;
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			if (mScrollStatus == ScrollStatus.header) {
				updateHeaderHeight(mScroller.getCurrY());
				if(mScroller.getCurrY() < 2 && mHeaderView.getCurrentState() == WaterStretchListViewHeader.STATE.end){
					//停止滚动了
					//逻辑：如果header范围进入了一个极小值内，且当前的状态是end，就把状态置成normal
					mHeaderView.updateState(WaterStretchListViewHeader.STATE.normal);
				}
			} else {
				mFooterView.setBottomMargin(mScroller.getCurrY());
			}
			postInvalidate();
			invokeOnScrolling();
		}
		super.computeScroll();
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		mScrollListener = l;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mScrollListener != null) {
			mScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// send to user's listener
		mTotalItemCount = totalItemCount;
		if (mScrollListener != null) {
			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
	}
	
	@Override
	public void notifyStateChanged(WaterStretchListViewHeader.STATE oldState, WaterStretchListViewHeader.STATE newState) {
		if(newState == WaterStretchListViewHeader.STATE.refreshing){
			if(mListViewListener != null){
				mListViewListener.onRefresh();
			}
		}
	}
	
	public void setWaterStretchListViewListener(WaterStretchListener l) {
		mListViewListener = l;
	}

	/**
	 * 实现该接口可以完成当正在回滚HeaderView或者FooterView的时候回调onScrolling方法
	 */
	public interface OnWaterStretchScrollListener extends OnScrollListener {
		public void onScrolling(View view);
	}

	/**
	 * 实现该接口可以回调刷新和加载更多方法
	 */
	public interface WaterStretchListener {
		public void onRefresh();

		public void onLoadMore();
	}
}
