package com.zhiitek.liftcontroller.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

/**
 * 自定义textview,修正换行问题
 */
public class CustomTextView extends TextView {

	private String text;
	private float textShowWidth;

	public CustomTextView(Context context) {
		this(context, null);
	}

	public CustomTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		TextPaint paint = getPaint();
		textShowWidth = this.getMeasuredWidth() - getCompoundPaddingLeft() - getCompoundPaddingRight();
		int lineCount = 0;
		text = this.getText().toString();
		if (text == null) return;
		char[] textCharArray = text.toCharArray();
		float drawedWidth = 0;
		float charWidth;
		Paint.FontMetrics fm = paint.getFontMetrics();
		int textHeight = (int) (Math.ceil(fm.descent - fm.ascent));
		float firstLinebaseY = (int) ((textHeight - (paint.descent() + paint.ascent())) / 2) + getCompoundPaddingTop();
		float realX = getCompoundPaddingLeft();
		float lineMaxWidth = 0;
		for (int i = 0; i < textCharArray.length; i++) {// 计算内容的长度及行数
			charWidth = paint.measureText(textCharArray, i, 1);
			if (textCharArray[i] == '\n') {
				drawedWidth = 0;
				lineCount++;
				continue;
			}
			if (textShowWidth - drawedWidth < charWidth) {
				drawedWidth = 0;
				lineCount++;
			}
			drawedWidth += charWidth;
			lineMaxWidth = lineMaxWidth < drawedWidth ? drawedWidth : lineMaxWidth;
		}
		int gravity = getGravity();
		if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL) {
			realX += (getWidth() - lineMaxWidth)/2;
		}
		if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.CENTER_VERTICAL) {
			firstLinebaseY = ((getMeasuredHeight() - (lineCount + 1) * (fm.bottom - fm.top)) / 2 - fm.top);
		}
		drawedWidth = 0;
		lineCount = 0;
		for (int i = 0; i < textCharArray.length; i++) {
			charWidth = paint.measureText(textCharArray, i, 1);
			if (textCharArray[i] == '\n') {
				lineCount++;
				drawedWidth = 0;
				continue;
			}
			if (textShowWidth - drawedWidth < charWidth) {
				lineCount++;
				drawedWidth = 0;
			}
			canvas.drawText(textCharArray, i, 1, realX + drawedWidth,
					firstLinebaseY + lineCount * textHeight, paint);
			drawedWidth += charWidth;
		}
	}
}
