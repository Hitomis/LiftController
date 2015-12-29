package com.zhiitek.liftcontroller.components.net.behavior;

import android.content.Context;

/**
 * 
 * PromptBehavior空实现, 用来避免空指针
 * 
 * @author ZhaoFan
 *
 */
public class NonePromptBehavior implements PromptBehavior{

	@Override
	public void showPrompt(Context context) {}

	@Override
	public void hidePrompt() {}

}
