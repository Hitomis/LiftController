package com.zhiitek.liftcontroller.components.net.behavior;

import android.content.Context;

public interface PromptBehavior {
	
	/** 显示prompt */
	void showPrompt(Context context);
	
	/** 关闭prompt */
	void hidePrompt();
	
}
