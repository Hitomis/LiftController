package com.zhiitek.liftcontroller.components.net.behavior;

import android.app.Dialog;
import android.content.Context;

import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.views.CustomProgressDialog;

public class DialogPromptBehavior implements PromptBehavior {
	private Dialog dialog; 
	
	@Override
	public void showPrompt(Context context) {
		dialog = new CustomProgressDialog(context, R.style.loading_dialog);
		dialog.show();
	}

	@Override
	public void hidePrompt() {
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}

}
