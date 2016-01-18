package com.zhiitek.liftcontroller.views;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.zhiitek.liftcontroller.R;


/**
 * 
 * 可拉伸的水滴状控件
 * 
 * @author ZhaoFan
 *
 */
public class WaterStretchView extends View {

	private Circle topCircle;
	
	private Circle bottomCircle;

	private Paint mFillPaint, mStrokePaint;
	
	private Path mFillPath, mStrokePath;
	
	private float mMaxCircleRadius;// 圆半径最大值
	
	private float mMinCircleRaidus;// 圆半径最小值
	
	private Bitmap arrowBitmap;// 箭头
	
	private final static int BACK_ANIM_DURATION = 180;
	
	private final static float STROKE_WIDTH = 1;// 边线宽度
	
	private final static float DEFAULT_RADIO = .5f;
	
	private final static float OFFSET_STROKE = STROKE_WIDTH * DEFAULT_RADIO;

	class Circle {
		private float x;// 圆x坐标
		private float y;// 圆y坐标
		private float radius;// 圆半径
		private int color;// 圆的颜色

		public float getX() {
			return x;
		}

		public void setX(float x) {
			this.x = x;
		}

		public float getY() {
			return y;
		}

		public void setY(float y) {
			this.y = y;
		}

		public float getRadius() {
			return radius;
		}

		public void setRadius(float radius) {
			this.radius = radius;
		}

		public int getColor() {
			return color;
		}

		public void setColor(int color) {
			this.color = color;
		}
	}

	public WaterStretchView(Context context) {
		this(context, null);
	}

	public WaterStretchView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WaterStretchView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		topCircle = new Circle();
		bottomCircle = new Circle();
		
		mFillPath = new Path();
		mStrokePath = new Path();
		
		mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mFillPaint.setColor(Color.GRAY);
		mFillPaint.setStyle(Paint.Style.FILL);
		
		
		mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mStrokePaint.setColor(Color.parseColor("#929292"));
		mStrokePaint.setStyle(Paint.Style.STROKE);
		mStrokePaint.setStrokeWidth(STROKE_WIDTH);
		
		Drawable drawable = getResources().getDrawable(R.drawable.refresh_arrow);
		arrowBitmap = drawableToBitmap(drawable);
		parseAttrs(context, attrs);
	}

	private Bitmap drawableToBitmap(Drawable drawable) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;

	}

	private void parseAttrs(Context context, AttributeSet attrs) {
		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WaterStretchView, 0, 0);
			try {
				int waterStretchFillColor = a.getColor(R.styleable.WaterStretchView_waterstretch_fill_color, Color.GRAY);
				mFillPaint.setColor(waterStretchFillColor);
				
				int waterStretchStrokeColor = a.getColor(R.styleable.WaterStretchView_waterstretch_stroke_color, Color.BLACK);
				mStrokePaint.setColor(waterStretchStrokeColor);

				mMaxCircleRadius = a.getDimensionPixelSize(R.styleable.WaterStretchView_max_circle_radius, 0);

				topCircle.setRadius(mMaxCircleRadius);
				bottomCircle.setRadius(mMaxCircleRadius);

				topCircle.setX(STROKE_WIDTH + mMaxCircleRadius);
				topCircle.setY(STROKE_WIDTH + mMaxCircleRadius);

				bottomCircle.setX(STROKE_WIDTH + mMaxCircleRadius);
				bottomCircle.setY(STROKE_WIDTH + mMaxCircleRadius);

				mMinCircleRaidus = a.getDimensionPixelSize(R.styleable.WaterStretchView_min_circle_radius, 0);
				if (mMinCircleRaidus > mMaxCircleRadius) {
					throw new IllegalStateException("Circle's MinRaidus should be equal or lesser than the MaxRadius");
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				a.recycle();
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// 宽度：上圆和下圆的最大直径
		int width = (int) ((mMaxCircleRadius + STROKE_WIDTH) * 2);
		// 高度：(上圆半径 + 圆心距)bottomCircle.getY() + 下圆半径
		int height = (int) Math.ceil(bottomCircle.getY() + bottomCircle.getRadius() + STROKE_WIDTH * 2);
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		makeBezierPath(mStrokePath, topCircle.getRadius() + OFFSET_STROKE + .2f, bottomCircle.getRadius() + OFFSET_STROKE + + .2f);
		canvas.drawPath(mStrokePath, mStrokePaint);
		canvas.drawCircle(topCircle.getX(), topCircle.getY(), topCircle.getRadius() + STROKE_WIDTH / 2.f, mStrokePaint);
		canvas.drawCircle(bottomCircle.getX(), bottomCircle.getY(), bottomCircle.getRadius() + STROKE_WIDTH / 2.f, mStrokePaint);
		
		makeBezierPath(mFillPath, topCircle.getRadius(), bottomCircle.getRadius());
		canvas.drawPath(mFillPath, mFillPaint);
		canvas.drawCircle(topCircle.getX(), topCircle.getY(), topCircle.getRadius(), mFillPaint);
		canvas.drawCircle(bottomCircle.getX(), bottomCircle.getY(), bottomCircle.getRadius(), mFillPaint);
		
		RectF bitmapArea = new RectF(topCircle.getX() - DEFAULT_RADIO * topCircle.getRadius(), 
									 topCircle.getY() - DEFAULT_RADIO * topCircle.getRadius(), 
									 topCircle.getX() + DEFAULT_RADIO * topCircle.getRadius(), 
									 topCircle.getY() + DEFAULT_RADIO * topCircle.getRadius());
		canvas.drawBitmap(arrowBitmap, null, bitmapArea, mFillPaint);
		super.onDraw(canvas);
	}
	
	
	private void makeBezierPath(Path path, float topCircleRadius, float bottomCircleRadius) {
		path.reset();
		// 获取两圆的两个切线形成的四个切点
		double angle = getAngle();
		float topX1 = (float) (topCircle.getX() - topCircleRadius * Math.cos(angle));
		float topY1 = (float) (topCircle.getY() + topCircleRadius * Math.sin(angle));

		float topX2 = (float) (topCircle.getX() + topCircleRadius * Math.cos(angle));
		float topY2 = topY1;

		float bottomX1 = (float) (bottomCircle.getX() - bottomCircleRadius * Math.cos(angle));
		float bottomY1 = (float) (bottomCircle.getY() + bottomCircleRadius * Math.sin(angle));

		float bottomX2 = (float) (bottomCircle.getX() + bottomCircleRadius * Math.cos(angle));
		float bottomY2 = bottomY1;

		path.moveTo(topCircle.getX(), topCircle.getY());
		
		// topX1, topY1为二次贝塞尔曲线起点坐标
		path.lineTo(topX1, topY1);
		// 参数一为二次贝塞尔曲线锚点坐标, 参数二为二次贝塞尔曲线终点坐标
		path.quadTo((bottomCircle.getX() - bottomCircleRadius), (bottomCircle.getY() + topCircle.getY()) / 2, bottomX1, bottomY1);
		
		path.lineTo(bottomX2, bottomY2);
		path.quadTo((bottomCircle.getX() + bottomCircleRadius), (bottomCircle.getY() + topY2) / 2, topX2, topY2);
		
		path.close();
	}

	/**
	 * 获得两个圆切线与圆心连线的夹角
	 *
	 * @return
	 */
	private double getAngle() {
		if (bottomCircle.getRadius() > topCircle.getRadius()) {
			throw new IllegalStateException("bottomCircle's radius must be less than the topCircle's");
		}
		return Math.asin((topCircle.getRadius() - bottomCircle.getRadius()) / (bottomCircle.getY() - topCircle.getY()));
	}

	/**
	 * 创建回弹动画 上圆半径减速恢复至最大半径 下圆半径减速恢复至最大半径 圆心距减速从最大值减到0(下圆Y从当前位置移动到上圆Y)。
	 *
	 * @return
	 */
	@SuppressLint("NewApi")
	public Animator createAnimator() {
		ValueAnimator valueAnimator = ValueAnimator.ofFloat(1, 0).setDuration(BACK_ANIM_DURATION);
		valueAnimator.setInterpolator(new DecelerateInterpolator());
		valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				updateComleteState(Float.parseFloat(valueAnimator.getAnimatedValue().toString()));
			}
		});
		return valueAnimator;
	}

	/**
	 * 完成的百分比
	 *
	 * @param percent
	 *            between[0,1]
	 */
	public void updateComleteState(float percent) {
		if (percent < 0) {
			percent = 0;
		}
		if (percent > 1) {
			percent = 1;
		}
		float topR = (float) (mMaxCircleRadius - 0.28 * percent * mMaxCircleRadius);
		float bottomR = (mMinCircleRaidus - mMaxCircleRadius) * percent + mMaxCircleRadius;
		float bottomCricleOffset = 5.f * percent * mMaxCircleRadius;
		topCircle.setRadius(topR);
		bottomCircle.setRadius(bottomR);
		bottomCircle.setY(topCircle.getY() + bottomCricleOffset);
		requestLayout();
		postInvalidate();
	}

	public Circle getTopCircle() {
		return topCircle;
	}

	public Circle getBottomCircle() {
		return bottomCircle;
	}

	public void setIndicatorColor(int color) {
		mFillPaint.setColor(color);
	}

	public int getIndicatorColor() {
		return mFillPaint.getColor();
	}
}
