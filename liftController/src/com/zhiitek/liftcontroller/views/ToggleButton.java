package com.zhiitek.liftcontroller.views;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.OvershootInterpolator;

/**
 * 仿iPhone ToggleButton
 * 
 * 2015-7-13
 * 
 * @author ZhaoFan
 *
 */
public class ToggleButton extends View{
	private float radius;
	// 开启颜色
	private int onColor;
	// 关闭颜色
	private int offColor;
	// 灰色带颜色
	private int offBorderColor;
	// 手柄颜色
	private int spotColor;
	// 边框颜色
	private int borderColor;
	// 画笔
	private Paint paint ;
	// 开关状态
	private boolean toggleOn = false;
	// 边框大小 默认为2px
	private int borderWidth = 2;
	// 垂直中心
	private float centerY;
	// 按钮的开始和结束位置
	private float startX, endX;
	// 手柄X位置的最小和最大值
	private float spotMinX, spotMaxX;
	// 手柄大小
	private int spotSize ;
	///  手柄X位置
	private float spotX;
	// 关闭时内部灰色带高度
	private float offLineWidth;
	
	private RectF rect = new RectF();
	
	//开关切换监听器
	private OnToggleChanged listener;
	
	//属性动画
	private ValueAnimator animation;
	
	public ToggleButton(Context context) {
		this(context, null);
	}
	
	public ToggleButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public ToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	
	public void init() {
		initPaint();
		initColor();
		initAnimation();
		this.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				toggle();
			}
		});
	}
	
	private void initPaint() {
		//初始化画笔(抗抖动)
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		//绘制风格为填充
		paint.setStyle(Style.FILL);
		//笔触风格为圆角
		paint.setStrokeCap(Cap.ROUND);
	}
	
	private void initColor() {
		onColor = Color.parseColor("#4ebb7f");
		offColor = Color.parseColor("#dadbda");
		offBorderColor = Color.parseColor("#ffffff");
		spotColor = Color.parseColor("#ffffff");
		//因为开始为关闭状态，所以这里边框背景色初始化为关闭状态颜色
		borderColor = offColor;
	}
	
	@SuppressLint("NewApi")
	private void initAnimation() {
		animation = new ValueAnimator();
		animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				calculateToggleEffect(Double.parseDouble(animation.getAnimatedValue().toString()));
			}
		});
		animation.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				setButtonClickable(true);
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
			}
		});
		//OvershootInterpolator ： 结束时会超过给定数值,然后返回设定的最大值
		animation.setInterpolator(new OvershootInterpolator(1.2f)); 
		animation.setDuration(500);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		Resources r = Resources.getSystem();
		if(widthMode == MeasureSpec.UNSPECIFIED || widthMode == MeasureSpec.AT_MOST){
			widthSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 55, r.getDisplayMetrics());
			widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
		}
		
		if(heightMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.AT_MOST){
			heightSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, r.getDisplayMetrics());
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
	}
	
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		final int width = getWidth();
		final int height = getHeight();
		//由宽高计算圆角的半径
		radius = Math.min(width, height) * 0.5f;
		centerY = radius;
		startX = radius;
		endX = width - radius;
		spotMinX = startX + borderWidth;
		spotMaxX = endX - borderWidth;
		spotSize = height - 4 * borderWidth;
		spotX = toggleOn ? spotMaxX : spotMinX;
		offLineWidth = 0;
	}
	
	private int clamp(int value, int low, int high) {
		return Math.min(Math.max(value, low), high);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		//1.绘制最外层边框背景-圆角矩形
		//绘制圆角矩形背景大小为测量的宽高
		rect.set(0, 0, getWidth(), getHeight());
		paint.setColor(borderColor);
		canvas.drawRoundRect(rect, radius, radius, paint);
		
		if(offLineWidth > 0){
			//1.1绘制整个开关区域中除手柄外的灰色区域带
			final float cy = offLineWidth * 0.5f;
			rect.set(spotX - cy, centerY - cy, endX + cy, centerY + cy);
			paint.setColor(offBorderColor);
			canvas.drawRoundRect(rect, cy, cy, paint);
		}
		
		//2.绘制圆形手柄边框
		rect.set(spotX - 1 - radius, centerY - radius, spotX + 1.1f + radius, centerY + radius);
		paint.setColor(borderColor);
		canvas.drawRoundRect(rect, radius, radius, paint);
		
		//3.绘制圆形手柄
		//圆形手柄的半径大小(不包含边框)
		final float spotR = spotSize * 0.5f;
		rect.set(spotX - spotR, centerY - spotR, spotX + spotR, centerY + spotR);
		paint.setColor(spotColor);
		canvas.drawRoundRect(rect, spotR, spotR, paint);
	}
	
	private double mapValueFromRangeToRange(double value, double fromLow,
			double fromHigh, double toLow, double toHigh) {
		double fromRangeSize = fromHigh - fromLow;
		double toRangeSize = toHigh - toLow;
		double valueScale = (value - fromLow) / fromRangeSize;
		return toLow + (valueScale * toRangeSize);
	}
	
	/**
	 * @param value
	 */
	private void calculateToggleEffect(final double value) {
		final float mapToggleX = (float) mapValueFromRangeToRange(value, 0, 1, spotMinX, spotMaxX);
		spotX = mapToggleX;
		
		float mapOffLineWidth = (float) mapValueFromRangeToRange(1 - value, 0, 1, 10, spotSize);
		
		offLineWidth = mapOffLineWidth;
		
		//开启时候的背景色
		final int fr = Color.red(onColor);
		final int fg = Color.green(onColor);
		final int fb = Color.blue(onColor);
		
		//关闭后的背景色
		final int tr = Color.red(offColor);
		final int tg = Color.green(offColor);
		final int tb = Color.blue(offColor);
		
		//border颜色渐变
		int sr = (int) mapValueFromRangeToRange(1 - value, 0, 1, fr, tr);
		int sg = (int) mapValueFromRangeToRange(1 - value, 0, 1, fg, tg);
		int sb = (int) mapValueFromRangeToRange(1 - value, 0, 1, fb, tb);
		
		sr = clamp(sr, 0, 255);
		sb = clamp(sb, 0, 255);
		sg = clamp(sg, 0, 255);
		
		borderColor = Color.rgb(sr, sg, sb);
		//重绘
		if (Looper.myLooper() == Looper.getMainLooper()) {
			invalidate();
		} else {
			postInvalidate();
		}
	}
	
	@SuppressLint("NewApi")
	private void takeToggleAction(boolean isOn){
		if(isOn) {
			animation.setFloatValues(0.f, 1.f);
		} else {
			animation.setFloatValues(1.f, 0.f);
		}
		animation.start();
	}
	
	/**
	 * 切换开关
	 */
	public void toggle() {
		setButtonClickable(false);
		toggleOn = !toggleOn;
		takeToggleAction(toggleOn);
		if(listener != null){//触发toggle事件
			listener.onToggle(toggleOn);
		}
	}
	
	/**
	 * 设置开关是否可点击
	 * @param click
	 */
	public void setButtonClickable(boolean click) {
		this.setEnabled(click);
	}
	
	/**
	 * 切换为打开状态
	 */
	public void toggleOn() {
		toggleOn = true;
		takeToggleAction(toggleOn);
		if(listener != null){//触发toggle事件
			listener.onToggle(toggleOn);
		}
	}
	
	/**
	 * 切换为关闭状态
	 */
	public void toggleOff() {
		toggleOn = false;
		takeToggleAction(toggleOn);
		if(listener != null){//触发toggle事件
			listener.onToggle(toggleOn);
		}
	}
	
	/**
	 * 设置显示成打开样式，不会触发toggle事件
	 */
	public void setToggleOn(){
		toggleOn = true;
		calculateToggleEffect(1.0f);
	}
	
	/**
	 * 设置显示成关闭样式，不会触发toggle事件
	 */
	public void setToggleOff() {
		toggleOn = false;
		calculateToggleEffect(0.0f);
	}
	
	/**
	 * 状态切换监听器
	 */
	public interface OnToggleChanged{
		public void onToggle(boolean on);
	}

	public void setOnToggleChanged(OnToggleChanged onToggleChanged) {
		listener = onToggleChanged;
	}
	
	public boolean getToggleState() {
		return toggleOn;
	}
	
}
