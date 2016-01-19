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

	//xml中读取的头信息的相关参数
	String headerText;
	float headerMarginLeft;
	float headerWeight;
	int headerTextColor;
	float headerTextSize;
	//xml中读取的显示信息的相关参数
	String infoText;
	float infoMarginLeft;
	float infoWeight;
	int infoTextColor;
	float infoTextSize;
	Drawable infoBackground = null;


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
		headerText = a.getString(R.styleable.CustomFormCellView_header_text);
		headerMarginLeft = a.getDimension(R.styleable.CustomFormCellView_header_layout_marginLeft, 0);
		headerWeight = a.getFloat(R.styleable.CustomFormCellView_header_layout_weight, DEFAULT_HEADERVIEW_WEIGHT);
		headerTextColor = a.getInt(R.styleable.CustomFormCellView_header_text_color, R.color.text_color);
		headerTextSize = a.getDimension(R.styleable.CustomFormCellView_header_text_size, DensityUtil.sp2px(context, 16));
		infoText = a.getString(R.styleable.CustomFormCellView_info_text);
		infoMarginLeft = a.getDimension(R.styleable.CustomFormCellView_info_layout_marginLeft, 0);
		infoWeight = a.getFloat(R.styleable.CustomFormCellView_info_layout_weight, DEFAULT_INFOVIEW_WEIGHT);
		infoTextColor = a.getInt(R.styleable.CustomFormCellView_info_text_color, Color.BLACK);
		infoTextSize = a.getDimension(R.styleable.CustomFormCellView_info_text_size, DensityUtil.sp2px(context, 17));
		infoViewType = a.getInt(R.styleable.CustomFormCellView_info_view_type, TYPE_DEFAULT);
		infoBackground = a.getDrawable(R.styleable.CustomFormCellView_info_background);
		a.recycle();
		
		setOrientation(LinearLayout.HORIZONTAL);
		int lp = getPaddingLeft() == 0 ? DensityUtil.dip2Px(context, DEFAULT_PADDING) : getPaddingLeft();
		int rp = getPaddingRight() == 0 ? DensityUtil.dip2Px(context, DEFAULT_PADDING) : getPaddingRight();
		int tp = getPaddingTop() == 0 ? DensityUtil.dip2Px(context, DEFAULT_PADDING) : getPaddingTop();
		int bp = getPaddingBottom() == 0 ? DensityUtil.dip2Px(context, DEFAULT_PADDING) : getPaddingBottom();
		setPadding(lp, tp, rp, bp);

		addViews();
	}

	private void addViews() {
		removeAllViews();
		setTextViewAttributes(context, headerMarginLeft, headerWeight, headerText, headerTextColor, headerTextSize);
		if (infoViewType == TYPE_EDITTEXT) {
			 setEdittextAttributes(context, infoMarginLeft, DEFAULT_INFOVIEW_WEIGHT, infoText, Color.BLACK, DensityUtil.sp2px(context, 17), infoBackground);
		 } else if (infoViewType == TYPE_TEXTVIEW) {
			 setWrapTextViewAttributes(context, infoMarginLeft, DEFAULT_INFOVIEW_WEIGHT, infoText, Color.BLACK, DensityUtil.sp2px(context, 17));
		 } else if (infoViewType == TYPE_TOGGLEBUTTON) {
			 setToggleButtonAttributes(context, infoMarginLeft, 1.875f);
		 }
	}

	public void setTextViewAttributes(Context context, float marginLeft,
			Float weight, String text, int textColor, float textSize) {
		if (headerTextView == null) {
			headerTextView = new TextView(context);
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
		addView(headerTextView);
	}

	public void setWrapTextViewAttributes(Context context, float marginLeft,
			Float weight, String text, int textColor, float textSize) {
		if (infoTextView == null) {
			infoTextView = new WrapTextView(context);
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
		addView(infoTextView);
	}

	@SuppressLint("NewApi")
	public void setEdittextAttributes(Context context, float marginLeft,
			Float weight, String text, int textColor, float textSize,
			Drawable drawable) {
		if (infoEditText == null) {
			infoEditText = new EditText(context);
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
		addView(infoEditText);
	}
	
	public void setToggleButtonAttributes(Context context, float marginLeft, Float weight) {
		if (infoToggleButton == null) {
			infoToggleButton = new ToggleButton(context);
		}
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
		layoutParams.setMargins((int) marginLeft, 0, 0, 0);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		infoEditText.setLayoutParams(layoutParams);
		infoEditText.setGravity(Gravity.CENTER_VERTICAL);
		infoEditText.setPadding(0, 0, 0, 0);
		addView(infoToggleButton);
	}
	 
	 @Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(DensityUtil.dip2Px(context, DEFAULT_HEIGHT), MeasureSpec.EXACTLY);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		 // 在onlayout中设置padding无效
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
		 addViews();
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
		this.headerText = headerText;
		headerTextView.setText(headerText);
	}
	
	public void setInfoText(String text) {
		infoText = text;
		infoTextView.setWrapText(text);
	}
}
