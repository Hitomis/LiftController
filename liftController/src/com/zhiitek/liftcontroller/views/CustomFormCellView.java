package com.zhiitek.liftcontroller.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.utils.DensityUtil;

public class CustomFormCellView extends LinearLayout{
	
	private Context context;
	/**
	 * 默认padding
	 */
	private static final int DEFAULT_PADDING = 10;
	/**
	 * 最小padding
	 */
	private static final int MIN_PADDING = 5;
	/**
	 * 默认高度
	 */
	private static final int DEFAULT_HEIGHT = 45;
	/**
	 * 提示view默认weight
	 */
	private static final float DEFAULT_HEADERVIEW_WEIGHT = 2.5f;
	/**
	 * infoView默认weight
	 */
	private static final float DEFAULT_INFOVIEW_WEIGHT = 7.5f;
	
	//表格单元头信息
	private TextView headerTextView;
	//表格单元显示信息
	private WrapTextView infoTextView;
	//表格单元输入信息
	private EditText infoEditText;
	private ToggleButton infoToggleButton;
	
	/**
	 * infoView控件为textview类型
	 */
	public final static int TYPE_TEXTVIEW = 0;
	/**
	 * infoView控件为edittext类型
	 */
	public final static int TYPE_EDITTEXT = 1;
	/**
	 * infoView控件为ToggleButton类型
	 */
	public final static int TYPE_TOGGLEBUTTON = 2;
	/**
	 * infoView控件默认类型
	 */
	private final static int TYPE_DEFAULT = TYPE_TEXTVIEW;
	
	private int infoViewType;

	public CustomFormCellView(Context context) {
		this(context, null);
	}

	public CustomFormCellView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CustomFormCellView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context = context;
		initView(context, attrs);
	}
	
	private void initView(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomFormCellView);
		String headerText = a.getString(R.styleable.CustomFormCellView_header_text);
		float headerMarginLeft = a.getDimension(R.styleable.CustomFormCellView_header_layout_marginLeft, 0);
		float headerWeight = a.getFloat(R.styleable.CustomFormCellView_header_layout_weight, DEFAULT_HEADERVIEW_WEIGHT);
		int headerTextColor = a.getInt(R.styleable.CustomFormCellView_header_text_color, R.color.text_color);
		float headerTextSize = a.getDimension(R.styleable.CustomFormCellView_header_text_size, DensityUtil.sp2px(context, 16));
		String infoText = a.getString(R.styleable.CustomFormCellView_info_text);
		float infoMarginLeft = a.getDimension(R.styleable.CustomFormCellView_info_layout_marginLeft, 0);
		float infoWeight = a.getFloat(R.styleable.CustomFormCellView_info_layout_weight, DEFAULT_INFOVIEW_WEIGHT);
		int infoTextColor = a.getInt(R.styleable.CustomFormCellView_info_text_color, Color.BLACK);
		float infoTextSize = a.getDimension(R.styleable.CustomFormCellView_info_text_size, DensityUtil.sp2px(context, 17));
		infoViewType = a.getInt(R.styleable.CustomFormCellView_info_view_type, TYPE_DEFAULT);
		Drawable infoBackground = a.getDrawable(R.styleable.CustomFormCellView_info_background);
		a.recycle();
		
		setOrientation(LinearLayout.HORIZONTAL);
		int lp = getPaddingLeft() == 0 ? DensityUtil.dip2Px(context, DEFAULT_PADDING) : getPaddingLeft();
		int rp = getPaddingRight() == 0 ? DensityUtil.dip2Px(context, DEFAULT_PADDING) : getPaddingRight();
		int tp = getPaddingTop() == 0 ? DensityUtil.dip2Px(context, DEFAULT_PADDING) : getPaddingTop();
		int bp = getPaddingBottom() == 0 ? DensityUtil.dip2Px(context, DEFAULT_PADDING) : getPaddingBottom();
		setPadding(lp, tp, rp, bp);
		
		if (infoViewType == TYPE_TEXTVIEW) {
			setTextViewAttributes(context, headerMarginLeft, headerWeight, headerText, headerTextColor, headerTextSize);
			setWrapTextViewAttributes(context, infoMarginLeft, infoWeight, infoText, infoTextColor, infoTextSize);
		} else if (infoViewType == TYPE_EDITTEXT) {
			setTextViewAttributes(context, headerMarginLeft, headerWeight, headerText, headerTextColor, headerTextSize);
			setEdittextAttributes(context, infoMarginLeft, infoWeight, infoText, infoTextColor, infoTextSize, infoBackground);
		} else if (infoViewType == TYPE_TOGGLEBUTTON) {
			setTextViewAttributes(context, headerMarginLeft, headerWeight, headerText, headerTextColor, headerTextSize);
			setToggleButtonAttributes(context, 0, 1.875f);
		}
	}
	
	public void setTextViewAttributes(Context context, float marginLeft,
			Float weight, String text, int textColor, float textSize) {
		if (headerTextView == null) {
			headerTextView = new TextView(context);
			addView(headerTextView);
		}
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
		layoutParams.setMargins((int) marginLeft, 0, 0, 0);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		headerTextView.setGravity(Gravity.CENTER_VERTICAL);
		headerTextView.setLayoutParams(layoutParams);
		headerTextView.setTextColor(textColor);
		headerTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		headerTextView.setPadding(0, 0, 0, 0);
		headerTextView.setText(text);
	}

	public void setWrapTextViewAttributes(Context context, float marginLeft,
			Float weight, String text, int textColor, float textSize) {
		if (infoTextView == null) {
			infoTextView = new WrapTextView(context);
			addView(infoTextView);
		}
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
		layoutParams.setMargins((int) marginLeft, 0, 0, 0);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		infoTextView.setLayoutParams(layoutParams);
		infoTextView.setTextColor(textColor);
		infoTextView.setGravity(Gravity.CENTER);
		infoTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		infoTextView.setPadding(0, 0, 0, 0);
		if (text != null) {
			infoTextView.setWrapText(text);
		}
	}

	@SuppressLint("NewApi")
	public void setEdittextAttributes(Context context, float marginLeft,
			Float weight, String text, int textColor, float textSize,
			Drawable drawable) {
		if (infoEditText == null) {
			infoEditText = new EditText(context);
			addView(infoEditText);
		}
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
		layoutParams.setMargins((int) marginLeft, 0, 0, 0);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		infoEditText.setBackground(drawable);
		infoEditText.setLayoutParams(layoutParams);
		infoEditText.setTextColor(textColor);
		infoEditText.setGravity(Gravity.CENTER_VERTICAL);
		infoEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		infoEditText.setText(text);
		infoEditText.setPadding(0, 0, 0, 0);
	}
	
	public void setToggleButtonAttributes(Context context, float marginLeft, Float weight) {
		if (infoToggleButton == null) {
			infoToggleButton = new ToggleButton(context);
			addView(infoToggleButton);
		}
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
		layoutParams.setMargins((int) marginLeft, 0, 0, 0);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		infoEditText.setLayoutParams(layoutParams);
		infoEditText.setGravity(Gravity.CENTER_VERTICAL);
		infoEditText.setPadding(0, 0, 0, 0);
	}
	 
	 @Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(DensityUtil.dip2Px(context, DEFAULT_HEIGHT), MeasureSpec.EXACTLY);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int count = getChildCount();
		int defalutPadding2x = DensityUtil.dip2Px(context, DEFAULT_PADDING * 2);
		for(int i = 0; i < count; i++) {
			View view = getChildAt(i);
			if (getHeight() - view.getHeight() < defalutPadding2x) {
				setPadding(DensityUtil.dip2Px(context, DEFAULT_PADDING), DensityUtil.dip2Px(context, MIN_PADDING), DensityUtil.dip2Px(context, DEFAULT_PADDING), DensityUtil.dip2Px(context, MIN_PADDING));
				break;
			}
		}
	}
	 
	 @Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		 super.onLayout(changed, l, t, r, b);
	}
	 
	 public void setInfoViewType(int type) {
		 infoViewType = type;
		 if (infoViewType == TYPE_EDITTEXT) {
			 removeViewAt(1);
			 setEdittextAttributes(context, 0, DEFAULT_INFOVIEW_WEIGHT, "", Color.BLACK, DensityUtil.sp2px(context, 17), null);
		 } else if (infoViewType == TYPE_TEXTVIEW) {
			 removeViewAt(1);
			 setWrapTextViewAttributes(context, 0, DEFAULT_INFOVIEW_WEIGHT, "", Color.BLACK, DensityUtil.sp2px(context, 17));
		 } else if (infoViewType == TYPE_TOGGLEBUTTON) {
			 removeViewAt(1);
			 setToggleButtonAttributes(context, 0, 1.875f);
		 }
	 }
	
	public TextView getHeaderTextView() {
		return headerTextView;
	}

	public WrapTextView getInfoTextView() {
		return infoTextView;
	}
	
	public EditText getInfoEditText() {
		return infoEditText;
	}
	
	public String getTextFormEditText() {
		return infoEditText.getText().toString();
	}

	public void setHeaderText(String headerText) {
		headerTextView.setText(headerText);
	}
	
	public void setInfoText(String text) {
		infoTextView.setWrapText(text);
	}
}
