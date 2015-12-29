package com.zhiitek.liftcontroller.views;
 

import com.zhiitek.liftcontroller.R;

import android.R.string;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
 


public class CustomEditDialog extends Dialog {
 
    public CustomEditDialog(Context context, int theme) {
        super(context, theme);
    }
 
    public CustomEditDialog(Context context) {
        super(context);
    }
 
    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {
 
        private Context context;
        private String title;
        private String content;
        private String buttonText;
        private View contentView;
 
//        private DialogInterface.OnClickListener buttonClickListener;
        
        private OnClick onClick;
 
        public Builder(Context context) {
            this.context = context;
        }
 
        /**
         * Set the Dialog message from String
         * @param title
         * @return
         */
        public Builder setContent(String content) {
            this.content = content;
            return this;
        }
 
        /**
         * Set the Dialog message from resource
         * @param title
         * @return
         */
        public Builder setContent(int content) {
            this.content = (String) context.getText(content);
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
                OnClick listener) {
            this.buttonText = (String) context
                    .getText(positiveButtonText);
            this.onClick = listener;
            return this;
        }
 
        /**
         * Set the positive button text and it's listener
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setButton(String positiveButtonText,
        		OnClick listener) {
            this.buttonText = positiveButtonText;
            this.onClick = listener;
            return this;
        }
 
        /**
         * Create the custom dialog
         */
        public CustomEditDialog create() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            final CustomEditDialog dialog = new CustomEditDialog(context, 
            		R.style.prompt_dialog);
//            dialog.setCanceledOnTouchOutside(false);
            final View layout = inflater.inflate(R.layout.custom_edit_dialog_layout, null);
            dialog.addContentView(layout, new LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            // set the dialog title
            ((TextView) layout.findViewById(R.id.title)).setText(title);
            
			if (buttonText != null) {
				((TextView) layout.findViewById(R.id.btn)).setText(buttonText);
				if (onClick != null) {
					((TextView) layout.findViewById(R.id.btn))
							.setOnClickListener(new View.OnClickListener() {
								public void onClick(View v) {
									onClick.onClick(dialog, ((EditText) layout.findViewById(R.id.et_content)).getText().toString());
								}
							});
				}
			}
            // set the content message
            ((EditText) layout.findViewById(R.id.et_content)).setText(content);
            dialog.setContentView(layout);
            return dialog;
        }
        
        public interface OnClick {
        	public void onClick(DialogInterface dialog, String text);
        }
 
    }
 
}