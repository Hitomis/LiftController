package com.zhiitek.liftcontroller.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhiitek.liftcontroller.R;

public class WaterStretchListViewFooter extends LinearLayout {

	private Context mContext;

	private View mContentView;

	private SpinFadeLoaderView mLoaderView;

	private TextView mHintView;

	private TextView txt_progresstext;

	private LinearLayout layout_progress;

	public enum STATE {
		normal, ready, loading
	}

	public WaterStretchListViewFooter(Context context) {
		this(context, null);
	}

	public WaterStretchListViewFooter(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public void setState(STATE state) {
		mHintView.setVisibility(View.INVISIBLE);
		mLoaderView.setVisibility(View.INVISIBLE);
		mHintView.setVisibility(View.INVISIBLE);
		txt_progresstext.setVisibility(View.INVISIBLE);
		layout_progress.setVisibility(View.INVISIBLE);
		if (state == STATE.ready) {
			mHintView.setVisibility(View.VISIBLE);
			mHintView.setText("松开载入更多");
		} else if (state == STATE.loading) {
			mLoaderView.setVisibility(View.VISIBLE);
			layout_progress.setVisibility(View.VISIBLE);
			txt_progresstext.setVisibility(View.VISIBLE);
		} else {
			mHintView.setVisibility(View.VISIBLE);
			mHintView.setText("查看更多");
		}
	}

	public void setBottomMargin(int height) {
		if (height < 0)
			return;
		LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
		lp.bottomMargin = height;
		mContentView.setLayoutParams(lp);
	}

	public int getBottomMargin() {
		LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
		return lp.bottomMargin;
	}

	/**
	 * normal status
	 */
	public void normal() {
		mHintView.setVisibility(View.VISIBLE);
		mLoaderView.setVisibility(View.GONE);
		layout_progress.setVisibility(View.GONE);
		txt_progresstext.setVisibility(View.GONE);
	}

	/**
	 * loading status
	 */
	public void loading() {
		mHintView.setVisibility(View.GONE);
		mLoaderView.setVisibility(View.VISIBLE);
		layout_progress.setVisibility(View.VISIBLE);
		txt_progresstext.setVisibility(View.VISIBLE);
	}

	/**
	 * hide footer when disable pull load more
	 */
	public void hide() {
		LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
		lp.height = 0;
//		mContentView.setVisibility(View.GONE);
		mContentView.setLayoutParams(lp);
	}

	/**
	 * show footer
	 */
	public void show() {
		LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
		lp.height = LayoutParams.WRAP_CONTENT;
//		mContentView.setVisibility(View.VISIBLE);
		mContentView.setLayoutParams(lp);
	}

	private void initView(Context context) {
		mContext = context;
		LinearLayout moreView = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.layout_waterstretchlistview_footer, null);
		addView(moreView);
		moreView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		mContentView = moreView.findViewById(R.id.waterstretchlistview_footer_content);
		mLoaderView = (SpinFadeLoaderView) moreView.findViewById(R.id.waterstretchlistview_footer_progressbar);
		mHintView = (TextView) moreView.findViewById(R.id.waterstretchlistview_footer_hint_textview);
		layout_progress = (LinearLayout) moreView.findViewById(R.id.progresslayout);
		txt_progresstext = (TextView) moreView.findViewById(R.id.txt_progresstext);
	}

}
