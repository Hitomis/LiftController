package com.zhiitek.liftcontroller.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.zhiitek.liftcontroller.utils.DensityUtil;

public class CircleTextView extends View {
	
	private final float displayRatio = 1.0f / 40.0f, textRatio = 40.0f / 60.0f;
	
	private Paint textPaint, backgroudPaint;

	private String text;

	private int radiu;

	private int measureDiameter, measureTextSize;
	
	public void setText(String text) {
		this.text = text;
		int width = (int) textPaint.measureText(text) + getPaddingLeft() + getPaddingRight();
		int height = (int) (Math.abs(textPaint.ascent() - textPaint.descent()) + getPaddingBottom() + getPaddingTop());
		radiu = Math.max(Math.max(width, height), measureDiameter) / 2;
	}

	public CircleTextView(Context context) {
		this(context, null);
	}
	
	public CircleTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CircleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {

		textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		textPaint.setColor(Color.WHITE);
		textPaint.setTextAlign(Align.CENTER);

		backgroudPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		backgroudPaint.setColor(Color.RED);
		
		int screenWidth = DensityUtil.getScreenWidth(getContext());
		int screenHeight = DensityUtil.getScreenHeight(getContext());
		
		float ratiow = screenWidth * displayRatio;
		float ratioh = screenHeight * displayRatio;
		
		measureDiameter = (int) Math.max(ratiow, ratioh);
		measureTextSize = (int) (textRatio * measureDiameter);
		textPaint.setTextSize(measureTextSize);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(measureDiameter, measureDiameter);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawCircle(radiu, radiu, radiu, backgroudPaint);
		canvas.save();
		int baseY = (int) (radiu - ((textPaint.descent() + textPaint.ascent()) / 2));  
		canvas.drawText(text, radiu, baseY, textPaint);
		canvas.restore();
	}
}
