package com.zhiitek.liftcontroller.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;
import android.widget.TextView;


public class CircleView extends TextView {

	private Paint mBgPaint = new Paint();

	PaintFlagsDrawFilter pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG); 

	public CircleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public CircleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mBgPaint.setColor(Color.RED);
		mBgPaint.setAntiAlias(true);
	}

	public CircleView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mBgPaint.setColor(Color.RED);
		mBgPaint.setAntiAlias(true);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
		int measuredWidth = getMeasuredWidth();
		int measuredHeight = getMeasuredHeight();
		int max = Math.max(measuredWidth, measuredHeight);
		setMeasuredDimension(max, max);
	}

	private int measureWidth(int measureSpec) {  
        int result = 0;  
        int specMode = MeasureSpec.getMode(measureSpec);  
        int specSize = MeasureSpec.getSize(measureSpec);  
  
        if (specMode == MeasureSpec.EXACTLY) {  
            // We were told how big to be  
            result = specSize;  
        } else {  
            // Measure the text  
            result = (int) mBgPaint.measureText(getText().toString()) + getPaddingLeft() + getPaddingRight();  
            if (specMode == MeasureSpec.AT_MOST) {  
                // Respect AT_MOST value if that was what is called for by  
                // measureSpec  
                result = Math.min(result, specSize);// 60,480  
            }  
        }  
  
        return result;  
    }  
  
    private int measureHeight(int measureSpec) {  
        int result = 0;  
        int specMode = MeasureSpec.getMode(measureSpec);  
        int specSize = MeasureSpec.getSize(measureSpec);  
  
        int mAscent = (int) mBgPaint.ascent();  
        if (specMode == MeasureSpec.EXACTLY) {  
            // We were told how big to be  
            result = specSize;  
        } else {  
            // Measure the text (beware: ascent is a negative number)  
            result = (int) (-mAscent + mBgPaint.descent()) + getPaddingTop() + getPaddingBottom();  
            if (specMode == MeasureSpec.AT_MOST) {  
                // Respect AT_MOST value if that was what is called for by  
                // measureSpec  
                result = Math.min(result, specSize);  
            }  
        }  
        return result;  
    }  

	@Override
	public void setBackgroundColor(int color) {
		// TODO Auto-generated method stub
		mBgPaint.setColor(color);
	}

	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		canvas.setDrawFilter(pfd);
		canvas.drawCircle(getWidth()/2, getHeight()/2, Math.max(getWidth(), getHeight())/2, mBgPaint);
		super.draw(canvas);
	}
}
