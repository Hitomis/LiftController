package com.zhiitek.liftcontroller.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.utils.DensityUtil;


public class SimpleSlidingMenu extends HorizontalScrollView {

	/**
	 * 屏幕宽度
	 */
	private int screenWidth;

	/**
	 * 菜单打开后留有的内容区域宽度
	 */
	private final static int RIGHT_PADDING = 100;
	
	private int menuRightPadding;

	/**
	 * 菜单宽度
	 */
	private int menuWidth;
	
	private int halfMenuWidth;

	private boolean isFirst;

	private boolean isShowMenu = true;
	/**
	 * 菜单是否打开
	 */
	private boolean isOpen;

	/**
	 * 菜单区域
	 */
	private ListView menu;
	
	/**
	 * 内容区域
	 */
	private View content;
	
	private LinearLayout llMenuIconLayout;
	
	private MenuItemHandler menuItemhandler;
	
	private Context mContext;
	
	private int startX;
	
	private int startY;
	
	private VelocityTracker vTracker;
	
	private static final int LIMIT_POINT_VALUE = 800;
	
	/** 自定义属性：背景图片Drawable */
	private Drawable bgDrawable;
	
	public SimpleSlidingMenu(Context context) {
		this(context, null);
	}

	public SimpleSlidingMenu(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public SimpleSlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
		//从TypedArray中取出对应的自定义属性值
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SimpleSlidingMenu);
		bgDrawable = typedArray.getDrawable(R.styleable.SimpleSlidingMenu_menu_background);
		typedArray.recycle();
	}
	
	private void init(Context context) {
		screenWidth = DensityUtil.getScreenWidth(context);
		mContext = context;
		
		
	}
	
	public interface MenuItemHandler{
		public void postMenuItem(ListView menu);
	}
	
	public void setMenuItemHandler(MenuItemHandler handler){
		menuItemhandler = handler;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (!isFirst) {
			LinearLayout wrapper = (LinearLayout) getChildAt(0);
			menu = (ListView)((ViewGroup) wrapper.getChildAt(0)).getChildAt(2);//菜单
			
			content = (View) wrapper.getChildAt(1);//内容显示区域
			llMenuIconLayout = (LinearLayout)((ViewGroup)((ViewGroup) wrapper.getChildAt(1)).getChildAt(0)).getChildAt(1);
			llMenuIconLayout.setOnClickListener(onClickListener);
			menuItemhandler.postMenuItem(menu);//将菜单传递出去
			
			// dp 2 px
			menuRightPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, RIGHT_PADDING, content.getResources()
					.getDisplayMetrics());
			menuWidth = screenWidth - menuRightPadding;
			halfMenuWidth = menuWidth / 2;
			//设置菜单与内容区域的宽度
			menu.getLayoutParams().width = menuWidth;
			content.getLayoutParams().width = screenWidth;
			
			applyBackground(wrapper);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);  
	}

	/**
	 * 适应菜单栏的背景图片
	 * @param wrapper
	 */
	@SuppressLint({ "DrawAllocation", "NewApi" })
	private void applyBackground(LinearLayout wrapper) {
		RelativeLayout relayMenu = (RelativeLayout) wrapper.getChildAt(0);
		Bitmap bgBitmap = ((BitmapDrawable) bgDrawable).getBitmap();
		Bitmap background = Bitmap.createBitmap(bgBitmap, 0, 0, menuWidth, bgBitmap.getHeight());
		relayMenu.setBackground(new BitmapDrawable(getResources(), background));
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			startX = (int) ev.getX();
			startY = (int) ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			int newX = (int) ev.getX();
			int newY = (int) ev.getY();
			int diffY = Math.abs(newY - startY);
			if(diffY > 20){
				return false;
			}
			if(newX - startX > 40 ){
				return true;
			}
		default:
			break;
		}
		return super.onInterceptTouchEvent(ev);
	}

	private OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (isShowMenu) {
				toggleMenu();
			} else {
				Toast.makeText(mContext, "初始化设备时菜单不可用", Toast.LENGTH_LONG).show();
			}
		}
	};

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		this.scrollTo(menuWidth, 0);
		if(changed) 
			isFirst = true;
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(ev.getAction() == MotionEvent.ACTION_DOWN){
			if(vTracker == null){    
                vTracker = VelocityTracker.obtain();    
            }else{    
                vTracker.clear();    
            }
			vTracker.addMovement(ev); 
		}
		return super.dispatchTouchEvent(ev);
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
			vTracker.addMovement(ev);
			vTracker.computeCurrentVelocity(1000);
			break;
		case MotionEvent.ACTION_UP://手指抬起的时候,菜单view的的位置相对于屏幕左边的位置（scrollX） 大于 菜单view一半大小 ->关闭左侧菜单
			if(vTracker.getXVelocity() >= LIMIT_POINT_VALUE){
				openMenu();
			}else if(vTracker.getXVelocity() < -LIMIT_POINT_VALUE){
				closeMenu();
			}else {
				//getScrollX == 横向滚动条相对有屏幕左侧的距离
				if (getScrollX() > halfMenuWidth) {//关闭
					smoothScrollTo(menuWidth, 0);
					isOpen = false;
				} else {//打开
					smoothScrollTo(0, 0);
					isOpen = true;
				}
			}
			return true;
		case MotionEvent.ACTION_CANCEL: 
			vTracker.recycle();
			break;
		}
		return super.onTouchEvent(ev);
	}
	
	/**
	 * 打开菜单
	 */
	public void openMenu(){
		if(isOpen) return;
		this.smoothScrollTo(0, 0);
		isOpen = true;
	}
	
	/**
	 * 关闭菜单
	 */
	public void closeMenu(){
		if(isOpen){
			this.smoothScrollTo(menuWidth, 0);
			isOpen = false;
		}
	}
	
	/**
	 * 切换菜单
	 */
	public void toggleMenu(){
		if(isOpen){
			closeMenu();
		}else{
			openMenu();
		}
	}
	
	/**
	 * 是否显示menu
	 * @param flag
	 */
	public void setShowMenu(boolean flag) {
		isShowMenu = flag;
	}
	
	/**
	 * 返回menu的listview
	 * @return
	 */
	public ListView getMenuListView() {
		return menu;
	}
	
}
