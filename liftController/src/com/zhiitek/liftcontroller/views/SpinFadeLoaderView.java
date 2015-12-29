package com.zhiitek.liftcontroller.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * 
 * 旋转 缩放 加载控件
 * 
 * @author ZhaoFan
 *
 */
public class SpinFadeLoaderView extends View {

	/** 默认直径 */
	private static final int DEFAULT_DIAMETER_SIZE = 55;
	
	/** 小圆半径 */
	private float radius;

	private Paint paint;

	public static final float SCALE = 1.0f;

	public static final int ALPHA = 255;

	private float[] scaleFloats = new float[] { SCALE, SCALE, SCALE, SCALE, SCALE, SCALE, SCALE, SCALE };

	private int[] alphas = new int[] { ALPHA, ALPHA, ALPHA, ALPHA, ALPHA, ALPHA, ALPHA, ALPHA };

	private final class Point {
		public float x;
		public float y;

		public Point(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}

	public SpinFadeLoaderView(Context context) {
		this(context, null);
	}

	public SpinFadeLoaderView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SpinFadeLoaderView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initRes();
	}

	private void initRes() {
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.parseColor("#6699CC"));
	}

	/**
	 * 圆O的圆心为(a,b),半径为R,点A与到X轴的为角α. 则点A的坐标为(a+R*cosα,b+R*sinα)
	 * 
	 * @param width
	 * @param height
	 * @param radius
	 * @param angle
	 * @return
	 */
	Point circleAt(int width, int height, float radius, double angle) {
		float x = (float) (width / 2 + radius * (Math.cos(angle)));
		float y = (float) (height / 2 + radius * (Math.sin(angle)));
		return new Point(x, y);
	}

	/**
	 * 初始化动画驱动
	 */
	private void initDrive() {
		//单个动画启动延迟(ms)
		int[] delays = { 0, 120, 240, 360, 480, 600, 720, 780, 840 };
		for (int i = 0; i < 8; i++) {
			final int index = i;
			ValueAnimator scaleAnim = ValueAnimator.ofFloat(1, 0.4f, 1);//缩放动画
			scaleAnim.setDuration(1000);
			scaleAnim.setRepeatCount(-1);
			scaleAnim.setStartDelay(delays[i]);
			scaleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					scaleFloats[index] = Float.parseFloat(animation.getAnimatedValue().toString());
					postInvalidate();
				}
			});
			scaleAnim.start();

			ValueAnimator alphaAnim = ValueAnimator.ofInt(255, 77, 255);//透明度动画
			alphaAnim.setDuration(1000);
			alphaAnim.setRepeatCount(-1);
			alphaAnim.setStartDelay(delays[i]);
			alphaAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					alphas[index] = Integer.parseInt(animation.getAnimatedValue().toString());
					postInvalidate();
				}
			});
			alphaAnim.start();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		Resources r = Resources.getSystem();
		if (widthMode == MeasureSpec.UNSPECIFIED || widthMode == MeasureSpec.AT_MOST) {
			widthSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_DIAMETER_SIZE,
					r.getDisplayMetrics());
			widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
		}

		if (heightMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.AT_MOST) {
			heightSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_DIAMETER_SIZE,
					r.getDisplayMetrics());
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		initDrive();
		
		radius = Math.min(getWidth(), getHeight()) / 10.f;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for (int i = 0; i < 8; i++) {
			canvas.save();
			Point point = circleAt(getWidth(), getHeight(), getWidth() / 2 - radius, i * (Math.PI / 4));
			canvas.translate(point.x, point.y);
			canvas.scale(scaleFloats[i], scaleFloats[i]);
			paint.setAlpha(alphas[i]);
			canvas.drawCircle(0, 0, radius, paint);
			canvas.restore();
		}
	}
	
}
