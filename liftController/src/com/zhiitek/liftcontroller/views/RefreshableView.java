package com.zhiitek.liftcontroller.views;

import java.lang.reflect.Field;

import com.zhiitek.liftcontroller.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnTouchListener;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 可进行下拉刷新的自定义控件。
 * 
 * @author guolin
 * 
 */
public class RefreshableView extends LinearLayout implements OnTouchListener {

	/**
	 * 下拉状态
	 */
	public static final int STATUS_PULL_TO_REFRESH = 0;

	/**
	 * 释放立即刷新状态
	 */
	public static final int STATUS_RELEASE_TO_REFRESH = 1;

	/**
	 * 正在刷新状态
	 */
	public static final int STATUS_REFRESHING = 2;

	/**
	 * 刷新完成或未刷新状态
	 */
	public static final int STATUS_REFRESH_FINISHED = 3;
	
	/**
	 * 下拉状态
	 */
	public static final int STATUS_PULL_TO_LOAD = 10;

	/**
	 * 释放立即刷新状态
	 */
	public static final int STATUS_RELEASE_TO_LOAD = 11;

	/**
	 * 正在刷新状态
	 */
	public static final int STATUS_LOADING = 12;

	/**
	 * 刷新完成或未刷新状态
	 */
	public static final int STATUS_LOAD_FINISHED = 13;

	/**
	 * 下拉头部回滚的速度
	 */
	public static final int SCROLL_SPEED = -20;

	/**
	 * 一分钟的毫秒值，用于判断上次的更新时间
	 */
	public static final long ONE_MINUTE = 60 * 1000;

	/**
	 * 一小时的毫秒值，用于判断上次的更新时间
	 */
	public static final long ONE_HOUR = 60 * ONE_MINUTE;

	/**
	 * 一天的毫秒值，用于判断上次的更新时间
	 */
	public static final long ONE_DAY = 24 * ONE_HOUR;

	/**
	 * 一月的毫秒值，用于判断上次的更新时间
	 */
	public static final long ONE_MONTH = 30 * ONE_DAY;

	/**
	 * 一年的毫秒值，用于判断上次的更新时间
	 */
	public static final long ONE_YEAR = 12 * ONE_MONTH;

	/**
	 * 上次更新时间的字符串常量，用于作为SharedPreferences的键值
	 */
	private static final String UPDATED_AT = "updated_at";

	/**
	 * 下拉刷新的回调接口
	 */
	private PullToRefreshListener mRefreshListener;
	
	/**
	 * 上拉加载更多的回调接口
	 */
	private PullToLoadListener mLoadListener;

	/**
	 * 用于存储上次更新时间
	 */
	private SharedPreferences preferences;

	/**
	 * 下拉头的View
	 */
	private View header;

	/**
	 * 上拉底部的View
	 */
	private View footer;
	
	/**
	 * 需要去下拉刷新的ListView
	 */
	private ListView listView;
	
	/**
	 * 刷新时显示的进度条
	 */
	private ProgressBar headerProgressBar;

	/**
	 * 指示下拉和释放的箭头
	 */
	private ImageView headerArrow;

	/**
	 * 指示下拉和释放的文字描述
	 */
	private TextView headerDescription;

	/**
	 * 上次更新时间的文字描述
	 */
	private TextView headerUpdateAt;

	/**
	 * 下拉头的布局参数
	 */
	private MarginLayoutParams headerLayoutParams;
	
	/**
	 * 上拉底部的布局参数
	 */
	private MarginLayoutParams footerLayoutParams;
	
	/**
	 * listview的布局参数
	 */
	private MarginLayoutParams listviewLayoutParams;
	
	/**
	 * 上次更新时间的毫秒值
	 */
	private long lastUpdateTime;

	/**
	 * 为了防止不同界面的下拉刷新在上次更新时间上互相有冲突，使用id来做区分
	 */
	private int mId = -1;

	/**
	 * 下拉头的高度
	 */
	private int hideHeaderHeight;

	/**
	 * 当前处理什么状态，可选值有STATUS_PULL_TO_REFRESH, STATUS_RELEASE_TO_REFRESH,
	 * STATUS_REFRESHING 和 STATUS_REFRESH_FINISHED
	 */
	private int currentStatus = STATUS_REFRESH_FINISHED;;

	/**
	 * 记录上一次的状态是什么，避免进行重复操作
	 */
	private int lastStatus = currentStatus;

	/**
	 * 手指按下时的屏幕纵坐标
	 */
	private float yDown;

	/**
	 * 在被判定为滚动之前用户手指可以移动的最大值。
	 */
	private int touchSlop;

	/**
	 * 是否已加载过一次layout，这里onLayout中的初始化只需加载一次
	 */
	private boolean loadOnce;

	/**
	 * 当前是否可以下拉，只有ListView滚动到头的时候才允许下拉
	 */
	private boolean ableToPull;
	
	/**
	 * 当前是否可以上拉,只有ListView滚动到头的时候才允许上拉
	 */
	private boolean ableToLoad;
	
	/**
	 * 上拉底部view是否还需要显示，只有数据未加载完成时才需要显示底部view
	 */
	private boolean isFooterViewShow;

	/**
	 * 下拉刷新控件的构造函数，会在运行时动态添加一个下拉头的布局。
	 * 
	 * @param context
	 * @param attrs
	 */
	public RefreshableView(Context context, AttributeSet attrs) {
		super(context, attrs);
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		header = LayoutInflater.from(context).inflate(R.layout.top_pull_to_refresh, null, true);
		footer = LayoutInflater.from(context).inflate(R.layout.bottom_pull_to_load_more, null, true);
		headerProgressBar = (ProgressBar) header.findViewById(R.id.progress_bar);
		headerArrow = (ImageView) header.findViewById(R.id.arrow);
		headerDescription = (TextView) header.findViewById(R.id.description);
		headerUpdateAt = (TextView) header.findViewById(R.id.updated_at);
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		refreshUpdatedAtValue();
		setOrientation(VERTICAL);
		addView(header);
	}

	/**
	 * 进行一些关键性的初始化操作，比如：将下拉头向上偏移进行隐藏，给ListView注册touch事件。
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed && !loadOnce) {
			hideHeaderHeight = -header.getHeight();
			header.layout(0, hideHeaderHeight, r, 0);
			footer.layout(0, b, r, b - hideHeaderHeight);
			headerLayoutParams = (MarginLayoutParams) header.getLayoutParams();
			headerLayoutParams.topMargin = hideHeaderHeight;
			header.setLayoutParams(headerLayoutParams);
			footerLayoutParams = (MarginLayoutParams) footer.getLayoutParams();
			if (footerLayoutParams != null) {
				footerLayoutParams.topMargin = 0;
				footer.setLayoutParams(footerLayoutParams);
			}
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				View view = getChildAt(i);
				if (view instanceof ListView) {
					listView = (ListView) view;
					break;
				}
			}
			footer.setVisibility(View.INVISIBLE);
			listView.setOnTouchListener(this);
			loadOnce = true;
		}
	}
	
	/**
	 * 设置底部view
	 */
	public void setFooterView() {
		addView(footer);
	}
	
	/**
	 * 设置底部view是否显示
	 * @param isFooterViewShow
	 */
	public void setFooterViewShow(boolean isFooterViewShow) {
		this.isFooterViewShow = isFooterViewShow;
	}

	/**
	 * 当ListView被触摸时调用，其中处理了各种下拉刷新的具体逻辑。
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		setIsAbleToPull(event);
		setIsAbleToLoad(event);
		if (ableToPull) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				yDown = event.getRawY();
				break;
			case MotionEvent.ACTION_MOVE:
				float yMove = event.getRawY();
				int distance = (int) (yMove - yDown);
				// 如果手指是下滑状态，并且下拉头是完全隐藏的，就屏蔽下拉事件
				if (distance <= 0 && headerLayoutParams.topMargin <= hideHeaderHeight) {
					return false;
				}
				if (distance < touchSlop) {
					return false;
				} else {
					clearContentViewEvents();
				}
				if (currentStatus != STATUS_REFRESHING) {
					if (headerLayoutParams.topMargin > 0) {
						currentStatus = STATUS_RELEASE_TO_REFRESH; 
					} else {
						currentStatus = STATUS_PULL_TO_REFRESH;
					}
					// 通过偏移下拉头的topMargin值，来实现下拉效果
					headerLayoutParams.topMargin = (distance / 2) + hideHeaderHeight;
					header.setLayoutParams(headerLayoutParams);
				}
				break;
			case MotionEvent.ACTION_UP:
			default:
				if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
					// 松手时如果是释放立即刷新状态，就去调用正在刷新的任务
					new RefreshingTask().execute();
				} else if (currentStatus == STATUS_PULL_TO_REFRESH) {
					// 松手时如果是下拉状态，就去调用隐藏下拉头的任务
					new HideHeaderTask().execute();
				}
				break;
			}
			// 时刻记得更新下拉头中的信息
			if (currentStatus == STATUS_PULL_TO_REFRESH
					|| currentStatus == STATUS_RELEASE_TO_REFRESH) {
				updateHeaderView();
				// 当前正处于下拉或释放状态，要让ListView失去焦点，否则被点击的那一项会一直处于选中状态
				listView.setPressed(false);
				listView.setFocusable(false);
				listView.setFocusableInTouchMode(false);
				lastStatus = currentStatus;
				// 当前正处于下拉或释放状态，通过返回true屏蔽掉ListView的滚动事件
				return true;
			}
		}
		if (ableToLoad) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				yDown = event.getRawY();
				break;
			case MotionEvent.ACTION_MOVE:
				float yMove = event.getRawY();
				int distance = (int) (yMove - yDown);
				if (distance > -touchSlop) {
					return false;
				} else {
					clearContentViewEvents();
				}
				if (currentStatus != STATUS_LOADING) {
					currentStatus = STATUS_RELEASE_TO_LOAD;
					footer.setVisibility(View.VISIBLE);
					footerLayoutParams.topMargin = hideHeaderHeight;
					footer.setLayoutParams(footerLayoutParams);
					listviewLayoutParams = (MarginLayoutParams)listView.getLayoutParams();
					listviewLayoutParams.bottomMargin = -hideHeaderHeight;
					listviewLayoutParams.topMargin = hideHeaderHeight;
					listView.setLayoutParams(listviewLayoutParams);
				}
				break;
			case MotionEvent.ACTION_UP:
			default:
				if (currentStatus == STATUS_RELEASE_TO_LOAD) {
					startLoading();
				}
				break;
			}
			if (currentStatus == STATUS_RELEASE_TO_LOAD) {
				// 当前正处于上拉状态，要让ListView失去焦点，否则被点击的那一项会一直处于选中状态
				listView.setPressed(false);
				listView.setFocusable(false);
				listView.setFocusableInTouchMode(false);
				lastStatus = currentStatus;
				// 当前正处于上拉状态，通过返回true屏蔽掉ListView的滚动事件
				return true;
			}
		}
		return false;
	}

	/**
	 * 通过反射修改字段去掉长按事件和点击事件
	 */
	private void clearContentViewEvents()
	{
		try
		{
			Field[] fields = AbsListView.class.getDeclaredFields();
			for (int i = 0; i < fields.length; i++)
				if (fields[i].getName().equals("mPendingCheckForLongPress"))
				{
					// mPendingCheckForLongPress是AbsListView中的字段，通过反射获取并从消息列表删除，去掉长按事件
					fields[i].setAccessible(true);
					listView.getHandler().removeCallbacks((Runnable) fields[i].get(listView));
				} else if (fields[i].getName().equals("mTouchMode"))
				{
					// TOUCH_MODE_REST = -1， 这个可以去除点击事件
					fields[i].setAccessible(true);
					fields[i].set(listView, -1);
				}
			// 去掉焦点
			((AbsListView) listView).getSelector().setState(new int[]
			{ 0 });
		} catch (Exception e)
		{
		}
	}

	/**
	 * 给下拉刷新控件注册一个监听器。
	 * 
	 * @param listener
	 *            监听器的实现。
	 * @param id
	 *            为了防止不同界面的下拉刷新在上次更新时间上互相有冲突， 请不同界面在注册下拉刷新监听器时一定要传入不同的id。
	 */
	public void setOnRefreshListener(PullToRefreshListener listener, int id) {
		mRefreshListener = listener;
		mId = id;
	}
	
	/**
	 * 给上拉加载更多控件注册一个监听器
	 * 
	 * @param listener
	 */
	public void setOnLoadListener(PullToLoadListener listener) {
		mLoadListener = listener;
	}

	/**
	 * 当所有的刷新逻辑完成后，记录调用一下，否则你的ListView将一直处于正在刷新状态。
	 */
	public void finishRefreshing() {
		currentStatus = STATUS_REFRESH_FINISHED;
		preferences.edit().putLong(UPDATED_AT + mId, System.currentTimeMillis()).commit();
		new HideHeaderTask().execute();
	}

	/**
	 * 开始刷新
	 */
	public void startRefreshing() {
		new RefreshingTask().execute();
	}
	
	/**
	 * 开始加载更多
	 */
	public void startLoading() {
		mLoadListener.onLoad();
		currentStatus = STATUS_LOADING;
	}
	
	/**
	 * 结束加载更多
	 */
	public void finishLoading() {
		currentStatus = STATUS_LOAD_FINISHED;
		footer.setVisibility(View.INVISIBLE);
		footerLayoutParams.topMargin = 0;
		footer.setLayoutParams(footerLayoutParams);
		listviewLayoutParams = (MarginLayoutParams)listView.getLayoutParams();
		listviewLayoutParams.bottomMargin = 0;
		listviewLayoutParams.topMargin = 0;
		listView.setLayoutParams(listviewLayoutParams);
	}

	/**
	 * 只有listview滚动到最后一项，且isFooterViewShow为true时，才显示底部加载更多的view
	 * @param event
	 */
	private void setIsAbleToLoad(MotionEvent event) {
		final Adapter adapter = listView.getAdapter();
		ableToLoad = false;
		if (null != adapter && !adapter.isEmpty()) {
			final int lastItemPosition = listView.getCount() - 1;
			final int lastVisiblePosition = listView.getLastVisiblePosition();
			if (lastVisiblePosition == lastItemPosition) {
				final int childIndex = lastVisiblePosition - listView.getFirstVisiblePosition();
				final View lastVisibleChild = listView.getChildAt(childIndex);
				if (lastVisibleChild != null) {
					if ((lastVisibleChild.getBottom() == (listView.getBottom() - listView.getTop())) && isFooterViewShow) {
						ableToLoad = true;
					}
				}
			}
		}
	}
	/**
	 * 根据当前ListView的滚动状态来设定 {@link #ableToPull}
	 * 的值，每次都需要在onTouch中第一个执行，这样可以判断出当前应该是滚动ListView，还是应该进行下拉。
	 * 
	 * @param event
	 */
	private void setIsAbleToPull(MotionEvent event) {
		View firstChild = listView.getChildAt(0);
		if (firstChild != null) {
			int firstVisiblePos = listView.getFirstVisiblePosition();
			if (firstVisiblePos == 0 && firstChild.getTop() == 0) {
				if (!ableToPull) {
					yDown = event.getRawY();
				}
				// 如果首个元素的上边缘，距离父布局值为0，就说明ListView滚动到了最顶部，此时应该允许下拉刷新
				ableToPull = true;
			} else {
				if (headerLayoutParams.topMargin != hideHeaderHeight) {
					headerLayoutParams.topMargin = hideHeaderHeight;
					header.setLayoutParams(headerLayoutParams);
				}
				ableToPull = false;
			}
		} else {
			// 如果ListView中没有元素，也应该允许下拉刷新
			ableToPull = true;
		}
	}

	/**
	 * 更新下拉头中的信息。
	 */
	private void updateHeaderView() {
		if (lastStatus != currentStatus) {
			if (currentStatus == STATUS_PULL_TO_REFRESH) {
				headerDescription.setText(getResources().getString(R.string.pull_to_refresh));
				headerArrow.setVisibility(View.VISIBLE);
				headerProgressBar.setVisibility(View.GONE);
				rotateArrow();
			} else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
				headerDescription.setText(getResources().getString(R.string.release_to_refresh));
				headerArrow.setVisibility(View.VISIBLE);
				headerProgressBar.setVisibility(View.GONE);
				rotateArrow();
			} else if (currentStatus == STATUS_REFRESHING) {
				headerDescription.setText(getResources().getString(R.string.refreshing));
				headerProgressBar.setVisibility(View.VISIBLE);
				headerArrow.clearAnimation();
				headerArrow.setVisibility(View.GONE);
			}
			refreshUpdatedAtValue();
		}
	}

	/**
	 * 根据当前的状态来旋转箭头。
	 */
	private void rotateArrow() {
		float pivotX = headerArrow.getWidth() / 2f;
		float pivotY = headerArrow.getHeight() / 2f;
		float fromDegrees = 0f;
		float toDegrees = 0f;
		if (currentStatus == STATUS_PULL_TO_REFRESH) {
			fromDegrees = 180f;
			toDegrees = 360f;
		} else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
			fromDegrees = 0f;
			toDegrees = 180f;
		}
		RotateAnimation animation = new RotateAnimation(fromDegrees, toDegrees, pivotX, pivotY);
		animation.setDuration(100);
		animation.setFillAfter(true);
		headerArrow.startAnimation(animation);
	}

	/**
	 * 刷新下拉头中上次更新时间的文字描述。
	 */
	private void refreshUpdatedAtValue() {
		lastUpdateTime = preferences.getLong(UPDATED_AT + mId, -1);
		long currentTime = System.currentTimeMillis();
		long timePassed = currentTime - lastUpdateTime;
		long timeIntoFormat;
		String updateAtValue;
		if (lastUpdateTime == -1) {
			updateAtValue = getResources().getString(R.string.not_updated_yet);
		} else if (timePassed < 0) {
			updateAtValue = getResources().getString(R.string.time_error);
		} else if (timePassed < ONE_MINUTE) {
			updateAtValue = getResources().getString(R.string.updated_just_now);
		} else if (timePassed < ONE_HOUR) {
			timeIntoFormat = timePassed / ONE_MINUTE;
			String value = timeIntoFormat + "分钟";
			updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
		} else if (timePassed < ONE_DAY) {
			timeIntoFormat = timePassed / ONE_HOUR;
			String value = timeIntoFormat + "小时";
			updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
		} else if (timePassed < ONE_MONTH) {
			timeIntoFormat = timePassed / ONE_DAY;
			String value = timeIntoFormat + "天";
			updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
		} else if (timePassed < ONE_YEAR) {
			timeIntoFormat = timePassed / ONE_MONTH;
			String value = timeIntoFormat + "个月";
			updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
		} else {
			timeIntoFormat = timePassed / ONE_YEAR;
			String value = timeIntoFormat + "年";
			updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
		}
		headerUpdateAt.setText(updateAtValue);
	}

	/**
	 * 正在刷新的任务，在此任务中会去回调注册进来的下拉刷新监听器。
	 * 
	 * @author guolin
	 */
	class RefreshingTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			int topMargin = headerLayoutParams.topMargin;
			while (true) {
				topMargin = topMargin + SCROLL_SPEED;
				if (topMargin <= 0) {
					topMargin = 0;
					break;
				}
				publishProgress(topMargin);
				SystemClock.sleep(10);
			}
			currentStatus = STATUS_REFRESHING;
			publishProgress(0);
			if (mRefreshListener != null) {
				mRefreshListener.onRefresh();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... topMargin) {
			updateHeaderView();
			headerLayoutParams.topMargin = topMargin[0];
			header.setLayoutParams(headerLayoutParams);
		}

	}

	/**
	 * 隐藏下拉头的任务，当未进行下拉刷新或下拉刷新完成后，此任务将会使下拉头重新隐藏。
	 * 
	 * @author guolin
	 */
	class HideHeaderTask extends AsyncTask<Void, Integer, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			int topMargin = headerLayoutParams.topMargin;
			while (true) {
				topMargin = topMargin + SCROLL_SPEED;
				if (topMargin <= hideHeaderHeight) {
					topMargin = hideHeaderHeight;
					break;
				}
				publishProgress(topMargin);
				SystemClock.sleep(10);
			}
			return topMargin;
		}

		@Override
		protected void onProgressUpdate(Integer... topMargin) {
			headerLayoutParams.topMargin = topMargin[0];
			header.setLayoutParams(headerLayoutParams);
		}

		@Override
		protected void onPostExecute(Integer topMargin) {
			headerLayoutParams.topMargin = topMargin;
			header.setLayoutParams(headerLayoutParams);
			currentStatus = STATUS_REFRESH_FINISHED;
		}
	}
	
	/**
	 * 使当前线程睡眠指定的毫秒数。
	 * 
	 * @param time
	 *            指定当前线程睡眠多久，以毫秒为单位
	 */
	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 下拉刷新的监听器，使用下拉刷新的地方应该注册此监听器来获取刷新回调。
	 * 
	 * @author guolin
	 */
	public interface PullToRefreshListener {

		/**
		 * 刷新时会去回调此方法，在方法内编写具体的刷新逻辑。注意此方法是在子线程中调用的， 你可以不必另开线程来进行耗时操作。
		 */
		void onRefresh();

	}
	
	/**
	 * 上拉加载更多的监听器
	 * @author Administrator
	 *
	 */
	public interface PullToLoadListener {
		/**
		 * 加载更多时调用此方法,此方法是主线程调用,耗时操作请另开线程执行
		 */
		void onLoad();
	}
	
}
