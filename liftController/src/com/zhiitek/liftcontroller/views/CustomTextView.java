package com.zhiitek.liftcontroller.views;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

public class CustomTextView extends TextView {

	private ArrayList<String> contentList = new ArrayList<String>();
	
	private int lineMaxWidth;

	public CustomTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int contentHeight = measureHeight(widthSize);
		switch (heightMode)
		{
		case MeasureSpec.EXACTLY:
			break;
		case MeasureSpec.AT_MOST:
			heightSize = contentHeight;
			break;
		case MeasureSpec.UNSPECIFIED:
			heightSize = contentHeight;
			break;
		default:
			break;
		}
		setMeasuredDimension(widthSize, heightSize);
	}

	private int measureHeight(int viewWidth) {
		TextPaint paint = getPaint();
		String text = getText().toString();
		Paint.FontMetrics fm = paint.getFontMetrics();
		int textHeight = (int) (Math.ceil(fm.descent - fm.ascent));
		int line=0;  
		int istart=0;
        int lineWidth=getCompoundPaddingLeft() + getCompoundPaddingRight();  
        contentList.clear();
        for (int i = 0; i < text.length(); i++)  
        {  
            char ch = text.charAt(i);  
            String srt = String.valueOf(ch);  
            float width = paint.measureText(srt);
  
            if (ch == 10){  
                line++;
                contentList.add(text.substring(istart, i));
                istart = i + 1;
                lineWidth = 0;
            }else{  
            	lineWidth += (int) width;
                if (lineWidth > viewWidth){  
                    line++;
                    contentList.add(text.substring(istart, i));
                    istart = i;
                    i--;
                    lineWidth = 0;
                }else{
                	if (lineWidth > lineMaxWidth) {
                		lineMaxWidth = lineWidth;
                	}
                    if (i == (text.length() - 1)){  
                        line++;
                        contentList.add(text.substring(istart));
                    }  
                }  
            }  
        }
		return (line)*textHeight + getCompoundPaddingTop() + getCompoundPaddingBottom();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (contentList.size() > 0) {
			TextPaint paint = getPaint();
			Paint.FontMetrics fm = paint.getFontMetrics();
			int textHeight = (int) (Math.ceil(fm.descent - fm.ascent));
			int firstLinebaseY = (int) ((textHeight - (paint.descent() + paint.ascent())) / 2) + getCompoundPaddingTop();
			float realX = getGravity() == Gravity.CENTER_HORIZONTAL ? (getWidth() - getCompoundPaddingLeft() - getCompoundPaddingRight() - lineMaxWidth)/2 : 0 + getCompoundPaddingLeft();
			for (int i = 0, j = 0; i < contentList.size(); i++, j++)  
	        {
	            canvas.drawText(contentList.get(i), realX,  firstLinebaseY + (textHeight * j), getPaint());
	        }
		}
	}
}
