package com.zhiitek.liftcontroller.views;

import com.zhiitek.liftcontroller.adapter.BaseAdapterHelper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;

public class SpinnerEditText extends EditText {

	private Context mContext;
	
	private PopupWindow popupWindow;
	
	private ListView listView;
	
	private Drawable mSpinnerDrawable;
	
	private BaseAdapterHelper<String> baseAdapter;
	
	private PopupItemClickListenner listenner;

	public SpinnerEditText(Context context) {
		this(context, null);
	}
	
	public SpinnerEditText(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.editTextStyle);
	}
	
	public SpinnerEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
		listView = new ListView(mContext);
		listView.setBackgroundColor(Color.LTGRAY);
		mSpinnerDrawable = getCompoundDrawables()[2];
		mSpinnerDrawable.setBounds(0, 0, mSpinnerDrawable.getIntrinsicWidth(), mSpinnerDrawable.getIntrinsicHeight());
	}
	
	public void setAdapter(BaseAdapterHelper<String> adapter) {
		baseAdapter = adapter;
	}

	@Override   
    public boolean onTouchEvent(MotionEvent event) {  
        if (event.getAction() == MotionEvent.ACTION_UP) {  
            if (getCompoundDrawables()[2] != null && this.isEnabled()) {  
  
                boolean touchable = event.getX() > (getWidth() - getTotalPaddingRight())  
                        && (event.getX() < ((getWidth() - getPaddingRight())));  
                  
                if (touchable) {  
                    showSpinnerItems();
                    return true;
                }  
            } else {
            	showSpinnerItems();
            	return true;
            }
        }  
  
        return super.onTouchEvent(event);  
    }  

	private void showSpinnerItems() {
		if(popupWindow == null){
			// 创建PopupWindow
			popupWindow = new PopupWindow(listView, this.getWidth() - 4, LayoutParams.WRAP_CONTENT, true);
			// PopupWindow需要一个背景来实现点击其它部位关闭
			popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}
		if(baseAdapter != null) {
			listView.setAdapter(baseAdapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					setText(baseAdapter.getItem(position));
					if (listenner != null) {
						listenner.onClick(position);
					}
					popupWindow.dismiss();
				}
			});
		}
		popupWindow.showAsDropDown(this, 0, 0);
	}
	
	public interface PopupItemClickListenner {
		public void onClick(int position);
	}
	
	public void setOnClickItemListenner(PopupItemClickListenner listenner) {
		this.listenner = listenner;
	}
}
