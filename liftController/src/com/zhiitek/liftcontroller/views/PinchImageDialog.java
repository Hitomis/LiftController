package com.zhiitek.liftcontroller.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.zhiitek.liftcontroller.R;

public class PinchImageDialog extends Dialog implements View.OnClickListener{

    Context context;

    private PinchImageView pinchImageView;

    public PinchImageDialog(Context context) {
        this(context, R.style.common_full_dialog);
    }

    public PinchImageDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RelativeLayout contentLayout = new RelativeLayout(context);
        pinchImageView = new PinchImageView(context);
        pinchImageView.setOnClickListener(this);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentLayout.addView(pinchImageView, layoutParams);

        setCancelable(true);// 可以使用返回键取消dialog

        setContentView(contentLayout, new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局

        WindowManager windowManager = ((Activity)context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);// 获取屏幕宽高
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.width = point.x; //设置Dialog宽度为屏幕宽度
        this.getWindow().setAttributes(lp);
    }

    public void setPinchImageView(Bitmap bitmap) {
        pinchImageView.setImageBitmap(bitmap);
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }
}
