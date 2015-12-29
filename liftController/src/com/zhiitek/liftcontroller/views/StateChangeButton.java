package com.zhiitek.liftcontroller.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class StateChangeButton extends View {
	
	private final static int  SPACE = 2;
	
	public final static int TYPE_SAMPLE = 1, TYPE_CIRCLING = 2;
	
	private Paint paint;
	
	private TextPaint textPaint;
	
	private RectF rect;
	
	private float corner;
	
	private int commonColor;
	
	private String text;
	
	private int textX, textY;
	
	private int type;

	private ValueAnimator animation;
	
	private int angle;
	
	private boolean isCircling;
	
	public StateChangeButton(Context context) {
		this(context, null);
	}
	
	public StateChangeButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public StateChangeButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initRes();
	}

	/**
	 * 初始化资源
	 */
	private void initRes() {
		text = "测试";
		commonColor = Color.parseColor("#4ebb7f");
		setType(TYPE_SAMPLE);
		
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(commonColor);
		paint.setStyle(Paint.Style.STROKE);
		
		textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(commonColor);
		textPaint.setTextSize(25);
		
		rect = new RectF();
		
		initDrive();
	}
	
	/**
	 * 初始化旋转动画驱动
	 */
	private void initDrive() {
		animation = new ValueAnimator();
		animation.setFloatValues(1, 360);
		animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				angle = (int) Float.parseFloat(animation.getAnimatedValue().toString());
				invalidate();
			}

		});
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(1200);
		animation.setRepeatCount(Integer.MAX_VALUE);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		Resources r = Resources.getSystem();
		widthSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 55, r.getDisplayMetrics());
		widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
		
		if(heightMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.AT_MOST){
			heightSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, r.getDisplayMetrics());
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		measureTextPosition();
		
	}

	private void measureTextPosition() {
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		
		corner = Math.min(width, height) * 0.2f;
		textX = (int) ((getWidth() - textPaint.measureText(text) + SPACE) / 2.f);
		textY = (int) ((height + (Math.abs(textPaint.ascent()) - textPaint.descent()) + SPACE) / 2.f);
	}

	 @Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		switch (type) {
		case TYPE_SAMPLE:
			drawSampleButton(canvas);
			break;
		case TYPE_CIRCLING:
			drawCirclingButton(canvas);
			break;
		}
	}

	/**
	 * 绘制旋转的圆环样式
	 * @param canvas
	 */
	private void drawCirclingButton(Canvas canvas) {
		canvas.drawArc(rect, angle, 345, false, paint);
	}

	/**
	 * 绘制简单的样式
	 * @param canvas
	 */
	private void drawSampleButton(Canvas canvas) {
		//1.绘制外边框
		rect.set(SPACE, SPACE, getWidth() - SPACE, getHeight() - SPACE);
		canvas.drawRoundRect(rect, corner, corner, paint);
		
		//2.绘制文字
		canvas.drawText(text, textX, textY, textPaint);;
	}
	
	/**
	 * 开启旋转样式
	 */
	public void startCircling() {
		if (!isCircling) {
			isCircling = true;
			setType(TYPE_CIRCLING);
			int minSize = Math.min(getWidth(), getHeight());
			float rLeft = (getWidth() - minSize) / 2.f + SPACE;
			float rTop = (getHeight() - minSize) / 2.f + SPACE;
			float rRight = rLeft + minSize - SPACE;
			float rBottom = rTop + minSize - SPACE;
			rect.set(rLeft, rTop, rRight, rBottom);
			animation.start();
		}
	}
	
	/**
	 * 修改边框中的文字并更新UI
	 * @param text
	 */
	public void setText(String text) {
		if(text != null) {
			if (text.length() > 3) {
				this.text = text.substring(0,3) + "...";
			} else {
				this.text = text;
			}
			measureTextPosition();
			invalidate();
		}
	}
	
	/**
	 * 还原为初始化默认样式
	 */
	public void reset() {
		setType(TYPE_SAMPLE);
		setText("测试");
		if (isCircling) {
			isCircling = false;
			animation.end();
		} 
		invalidate();
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getText() {
		return text;
	}
	 
}
