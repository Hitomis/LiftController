package com.zhiitek.liftcontroller.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.zhiitek.liftcontroller.R;

/**
 * 仿iPhone VoiceSeekBar
 * 
 * 2015-7-16
 * 
 * @author ZhaoFan
 *
 */
@SuppressLint("NewApi")
public class VoiceSeekBar extends View {
	
	/**
	 * 手柄半径比率
	 */
	private static final float SPOTRADIUSRATE = 13.f/40.f;
	
	/**
	 * 横条默认高度
	 */
	private static final int BAR_HEIGHT = 2;
	
	/**
	 * 默认间隙宽度
	 */
	private static final int SPACE = 30;
	
	/**
	 * 最高级别[级别从0开始,并且级别的段数则为：MAXLEVEL + 1]
	 */
	private static int MAXLEVEL;
	
	/**
	 * 条形棒宽度
	 */
	private int barWidth;
	
	/**
	 * 手柄的区域
	 */
	private Region spotRegion;
	
	/**
	 * 绘制条形棒的矩形
	 */
	private Rect rect;
	
	// 左边条形棒的颜色,右边条形棒的颜色,手柄的颜色
	private int leftBarColor, rightBarColor, spotColor;
	
	// 手指按下的时候x坐标
	private float pressX;
	
	// 条形棒矩形的左上角定点
	private int barLeft ,barTop;
	
	// 左侧符号的X坐标,Y坐标
	private int signX,signY;
	
	// 手柄半径
	private float radius;
	
	private Paint paint, spotPaint;
	
	private Bitmap signLeft, signRigh;
	
	private int currentLevel;
	
	private OnSeekBarChangeListener onSeekBarChangeListener;
	
    public interface OnSeekBarChangeListener {
        
        void onProgressChanged(VoiceSeekBar seekBar, int currentLevel);
    
        void onStartTrackingTouch(VoiceSeekBar seekBar);
        
        void onStopTrackingTouch(VoiceSeekBar seekBar);
        
    }
	
	public VoiceSeekBar(Context context) {
		this(context, null);
	}
	
	public VoiceSeekBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public VoiceSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// 关闭硬件加速
		setLayerType(LAYER_TYPE_SOFTWARE, null);  
		init();
		
	}
	
	private void init() {
		//初始化当前级别
		currentLevel = 0;
		
		// 初始化加载左边与右边的两张符号图标
		signLeft = BitmapFactory.decodeResource(getResources(), R.drawable.minus);
		signRigh = BitmapFactory.decodeResource(getResources(), R.drawable.plus);
		
		// 手柄左边/右边的条形棒颜色
		leftBarColor = Color.parseColor("#4ebb7f");
		rightBarColor = Color.parseColor("#dadbda");
		// 手柄颜色
		spotColor = rightBarColor;
		
		paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		paint.setStyle(Paint.Style.FILL);
		paint.setStrokeCap(Paint.Cap.ROUND);
		
		spotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		spotPaint.setColor(spotColor);
		spotPaint.setStyle(Paint.Style.FILL);
		// 为手柄添加阴影效果
		spotPaint.setShadowLayer(10, 0, 1, Color.DKGRAY);
		
		// 手柄所在的区域
		spotRegion = new Region();
		rect = new Rect();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		Resources r = Resources.getSystem();
		//测量高度,高度默认为35
		int heigthSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, r.getDisplayMetrics());
		heightMeasureSpec = MeasureSpec.makeMeasureSpec(heigthSize, MeasureSpec.EXACTLY);
		
		//测量宽度
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int minWidth = (int) (signLeft.getWidth() + signRigh.getWidth() + SPACE * 4 +  heigthSize * SPOTRADIUSRATE);
		widthSize = Math.max(minWidth, widthSize);
		widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		final int width = getWidth();
		final int height = getHeight();
		
		//手柄的半径
		radius = height * SPOTRADIUSRATE;
		
		//条形棒宽度
		barWidth = (int) (width - (signLeft.getWidth() + signRigh.getWidth() + SPACE * 4 + radius));
		
		//计算条形棒开始绘制的左上角顶点位置
		barLeft = (int) Math.ceil((width - barWidth) / 2.f);
		barTop = (int) Math.ceil((height - BAR_HEIGHT) / 2.f);
		
		//计算左边符号绘制的位置
		signX = (int) (barLeft - signLeft.getWidth() - SPACE);
		signY = (height - signLeft.getHeight()) / 2;
		
		calcCurrentPressX();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		//0.绘制左右两边的符号
		canvas.drawBitmap(signLeft, signX, signY, paint);
		canvas.drawBitmap(signRigh, barWidth + barLeft + SPACE, signY, paint);
		
		//1.绘制圆形手柄
		//计算手柄圆点坐标x值
		float cx = 0;
		float cy = barTop + BAR_HEIGHT / 2; 
		if(pressX <= radius + barLeft){//手指触摸的范围在：从最左侧到横条开始的一个手柄半径距离范围
			cx = barLeft + radius;
		}else if (pressX > barLeft + radius && pressX <= barWidth + barLeft - radius){//手指触摸点在 横条最左侧开始加上一个手柄半径距离到横条最右侧减去一个手柄半径距离范围之内
			cx = pressX;
		} else {//手指触摸点超过横条最右侧减去一个手柄半径距离的范围
			cx = barLeft + barWidth - radius;
		}
		canvas.drawCircle(cx, cy, radius, spotPaint);
		
		//2.绘制左边的条形棒
		int leftBarRight = (int) (cx - radius);
		int barBottom = barTop + BAR_HEIGHT;
		rect.set(barLeft, barTop, leftBarRight, barBottom);
		paint.setColor(leftBarColor);
		canvas.drawRect(rect, paint);
		
		//3.绘制右边的条形棒
		int rightBarLeft = (int) (leftBarRight + radius * 2);
		rect.set(rightBarLeft, barTop, barWidth + barLeft, barBottom);
		paint.setColor(rightBarColor);
		canvas.drawRect(rect, paint);
		
		//4.记录当前手柄所在的区域(区域的范围扩大一个半径范围)
		int regionLeft = (int)(cx - 2 * radius);
		spotRegion.set(regionLeft, (int) -radius, (int)(radius * 4) + regionLeft, (int)(radius * 4));
		
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		getParent().requestDisallowInterceptTouchEvent(true);
		return super.dispatchTouchEvent(ev);
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
 			pressX = event.getX();
 			float pressY = event.getY();
 			if(!spotRegion.contains((int)pressX, (int)pressY)) {
 				return false;
 			}
 			if(onSeekBarChangeListener != null) 
 			onSeekBarChangeListener.onStartTrackingTouch(this);
 			break;
		case MotionEvent.ACTION_MOVE:
			pressX = event.getX();
			int level = CalcCurrentLevel();
			if(onSeekBarChangeListener != null && currentLevel != level) {
				currentLevel = level;
				onSeekBarChangeListener.onProgressChanged(this, level);
			}
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			if(onSeekBarChangeListener != null) 
			onSeekBarChangeListener.onStopTrackingTouch(this);
			break;
		}
		return true;
	}
	
	/**
	 * 设置当前级别
	 * @param currentLevel
	 */
	public void setCurrentLevel(int currentLevel) {
		if(currentLevel < 0){
			this.currentLevel = 0;
		} else {
			this.currentLevel = currentLevel >= MAXLEVEL ? MAXLEVEL : currentLevel;
		}
	}
	
	/**
	 * 获取当前级别
	 * @return
	 */
	public int getCurrentLevel() {
		return currentLevel;
	}

	/**
	 * 根据当前级别计算当前pressX值(pressX用来计算手柄所在位置)
	 */
	private void calcCurrentPressX() {
		pressX = barWidth * ((float)currentLevel / MAXLEVEL) + barLeft;
	}

	/**
	 *  计算当前的级别
	 * @return
	 */
	private int CalcCurrentLevel() {
		int level ;
		if(pressX <= radius + barLeft){//手指触摸的范围在：从最左侧到横条开始的一个手柄半径距离范围
			level = 0;
		}else if (pressX > barLeft + radius && pressX <= barWidth + barLeft - radius){//手指触摸点在 横条最左侧开始加上一个手柄半径距离到横条最右侧减去一个手柄半径距离范围之内
			level = (int) (((pressX - barLeft) * MAXLEVEL) / barWidth);
		} else {//手指触摸点超过横条最右侧减去一个手柄半径距离的范围
			level = MAXLEVEL;
		}
		return level;
	}

	public void setOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener) {
		this.onSeekBarChangeListener = onSeekBarChangeListener;
	}
	
	public void setMaxLevel(int max) {
		MAXLEVEL = max;
	}

}
