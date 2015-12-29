package com.zhiitek.liftcontroller.views;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.utils.DensityUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CustomView extends View{

	private Paint paint;
	private Paint textPaint;
	private Path path;
	private OnShapeClickListener mOnShapeClickListener;
	private Region mRegion;

	public CustomView(Context context) {
		super(context);
        init(context);
	}

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

	private void init(Context context) {
		paint = new Paint();
		path = new Path();
		mRegion = new Region();
		textPaint = new Paint();
	}

    @Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		RectF r=new RectF();
		path.computeBounds(r, true);
		mRegion.setPath(path, new Region((int)r.left,(int)r.top,(int)r.right,(int)r.bottom));
		canvas.drawPath(path, paint);
		textPaint.setTextSize(DensityUtil.dip2Px(getContext(), 40));
		textPaint.setDither(true);
		textPaint.setAntiAlias(true);
		textPaint.setColor(getContext().getResources().getColor(R.color.cpb_blue_dark));
		canvas.drawText("不合格", getWidth()/2 - DensityUtil.dip2Px(getContext(), 40)*3/2, getHeight()*3/4, textPaint);
		textPaint.setColor(getContext().getResources().getColor(R.color.cpb_white));
		canvas.drawText("合格", getWidth()/2 - DensityUtil.dip2Px(getContext(), 40), getHeight()/4, textPaint);
		invalidate();
	}

    @Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		int x = (int)event.getX();
		int y = (int)event.getY();
		if (mOnShapeClickListener != null && event.getAction() == MotionEvent.ACTION_UP) {
			if (mRegion.contains(x, y)) {
				mOnShapeClickListener.onUpShapeClick(this);
			} else {
				mOnShapeClickListener.onDowmShapeClick(this);
			}
		}
		return true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		int harfHeight = h/2;
		int eighthHeight = h/8;
		int width = w;
		path.reset();
		path.moveTo(0, 0);
		path.lineTo(0, harfHeight);
		for(int i=1; i<5; i++) {
		path.lineTo(i * width/5 ,
				i % 2 == 1 ? (harfHeight + ((float) (1000 * Math.random()) % eighthHeight))
						: (harfHeight - ((float) (1000 * Math.random()) % eighthHeight)));
	    }
		path.lineTo(width, harfHeight);
		path.lineTo(width, 0);
		path.close();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(getContext().getResources().getColor(R.color.light_blue));
		paint.setAntiAlias(true);
		paint.setDither(true);
		
	}

    public void setOnShapeClickListrener(OnShapeClickListener onShapeClickListener) {
    	this.mOnShapeClickListener = onShapeClickListener;
    }

    public interface OnShapeClickListener {
    	public void onUpShapeClick(View v);
    	public void onDowmShapeClick(View v);
    }
}
