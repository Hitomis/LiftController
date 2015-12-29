package com.zhiitek.liftcontroller.views;
 

import com.zhiitek.liftcontroller.R;

import android.R.integer;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
 


public class CustomPromptDialog extends Dialog {
 
    public CustomPromptDialog(Context context, int theme) {
        super(context, theme);
    }
 
    public CustomPromptDialog(Context context) {
        super(context);
    }
 
    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {
 
        private Context context;
        private String title;
        private String message;
        private String buttonText;
        private View contentView;
        private int contentViewHeight;
        private boolean isCanceledOnTouchOutside;
        private boolean isCancelable;
 
        private DialogInterface.OnClickListener buttonClickListener;
 
        public Builder(Context context) {
            this.context = context;
        }
 
        /**
         * Set the Dialog message from String
         * @param title
         * @return
         */
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }
 
        /**
         * Set the Dialog message from resource
         * @param title
         * @return
         */
        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }
 
        /**
         * Set the Dialog title from resource
         * @param title
         * @return
         */
        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }
 
        /**
         * Set the Dialog title from String
         * @param title
         * @return
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }
 
        /**
         * Set a custom content view for the Dialog.
         * If a message is set, the contentView is not
         * added to the Dialog...
         * @param v
         * @return
         */
        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }
 
        /**
         * Set the positive button resource and it's listener
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setButton(int positiveButtonText,
                DialogInterface.OnClickListener listener) {
            this.buttonText = (String) context
                    .getText(positiveButtonText);
            this.buttonClickListener = listener;
            return this;
        }
 
        /**
         * Set the positive button text and it's listener
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setButton(String positiveButtonText,
                DialogInterface.OnClickListener listener) {
            this.buttonText = positiveButtonText;
            this.buttonClickListener = listener;
            return this;
        }
        
        public Builder setContentViewHeight(int height) {
        	this.contentViewHeight = height;
        	return this;
        }
        
        public Builder setCanceledOnTouchOutside(boolean canceld) {
        	this.isCanceledOnTouchOutside = canceld;
        	return this;
        }
 
        public Builder setCancelable(boolean canceld) {
        	this.isCancelable = canceld;
        	return this;
        }
        /**
         * Create the custom dialog
         */
        public CustomPromptDialog create() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            final CustomPromptDialog dialog = new CustomPromptDialog(context, 
            		R.style.prompt_dialog);
            dialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside);
            dialog.setCancelable(isCancelable);
            View layout = inflater.inflate(R.layout.custom_dialog_layout, null);
            dialog.addContentView(layout, new LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            // set the dialog title
            ((TextView) layout.findViewById(R.id.title)).setText(title);
            
			if (buttonText != null) {
				((TextView) layout.findViewById(R.id.btn)).setText(buttonText);
				if (buttonClickListener != null) {
					((TextView) layout.findViewById(R.id.btn))
							.setOnClickListener(new View.OnClickListener() {
								public void onClick(View v) {
									buttonClickListener.onClick(dialog,
											DialogInterface.BUTTON_POSITIVE);
								}
							});
				}
			}
            // set the content message
            if (message != null) {
                ((TextView) layout.findViewById(
                		R.id.message)).setText(message);
            } else if (contentView != null) {
                // if no message set
                // add the contentView to the dialog body
                ((LinearLayout) layout.findViewById(R.id.content))
                        .removeAllViews();
                ((LinearLayout) layout.findViewById(R.id.content))
                        .addView(contentView, 
                                new LayoutParams(
                                        LayoutParams.WRAP_CONTENT, 
                                        contentViewHeight));
            }
            dialog.setContentView(layout);
            return dialog;
        }
 
    }
 
}