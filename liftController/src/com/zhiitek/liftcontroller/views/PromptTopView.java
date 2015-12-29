package com.zhiitek.liftcontroller.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.utils.DensityUtil;

/**
 * 当 Activity already running 可用 
 */
public class PromptTopView extends PopupWindow {
	
	private Context context;
	
	private TextView promptText;
	
	private View parent;
	
	private int gravity = Gravity.TOP;
	
	private int duration;
	
	public static final int LENGTH_SHORT = 2000;
	
	public static final int LENGTH_LONG = 3000;
	
	private PromptTopView(Context context, View parent) {
		super(context);
		
		this.parent = parent;
		this.context = context;
		
		init();
    }
	
	private void init(){
		View popouView = View.inflate(context, R.layout.popup_prompt, null);
		promptText = (TextView) popouView.findViewById(R.id.tv_prompt);
		setContentView(popouView);
		
		//设置PromptTopView宽度、高度
		setWidth(DensityUtil.getScreenWidth(context));
        setHeight(DensityUtil.getScreenHeight(context) / 8);
        //设置点击PromptTopView以外的区域不隐藏
        setFocusable(false);
        //设置背景色
		setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1adbdb")));
		// 设置PopupWindow显示和隐藏动画
		setAnimationStyle(R.style.top_prompt_anim);
	}
	
	/**
	 * 
	 * @param context
	 * @param parent 当前Activity的任意一个非空View,用于指定覆盖在该View所在Activity顶部
	 * @param text
	 * @param duration
	 * @return
	 */
    public static PromptTopView makeText(Context context, View parent, CharSequence text, int duration) {
    	PromptTopView promptView = new PromptTopView(context, parent);
    	promptView.duration = duration;
    	promptView.promptText.setText(text);
        return promptView;
    }
	
	private Handler promptHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			dismiss();
		};
	};
	
	public void setText(String promptStr){
		promptText.setText(promptStr);
	}
	
	public void setGravity(int gravity){
		this.gravity = gravity;
	}
	
	/**
	 * 默认在屏幕顶部显示PromptView
	 */
	public void show(){
		showAtLocation(parent, gravity, 0, 0);
		promptHandler.sendEmptyMessageDelayed(0, duration);
	}
	
}
