package com.zhiitek.liftcontroller.utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;

public class DialogUtil {
	
	/**
	 * 定义一个显示消息的对话框
	 * @param ctx
	 * @param msg 显示消息
	 */
	public static void showDialog(final Context ctx, String msg) {
		// 创建一个AlertDialog.Builder对象
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx,  AlertDialog.THEME_DEVICE_DEFAULT_LIGHT).setMessage(
				msg).setCancelable(false);

		builder.setPositiveButton("确定", null);
		builder.create().show();
	}

	
	/**
	 * 定义一个显示指定组件的对话框
	 * @param ctx
	 * @param view 指定组件
	 */
	public static AlertDialog showDialog(Context ctx, View view) {
		AlertDialog.Builder builder = new Builder(ctx);
		AlertDialog dialog = builder.create();
		dialog.setView(view, 0, 0, 0, 0);
		dialog.show();
		return dialog;
	}
	
	/**
	 * 
	 * @param context
	 * @param title 标题
	 * @param message 提示语
	 * @param positiveStr 确定按钮字符： 如果为null就默认为“确定”
	 * @param negativeStr 取消按钮字符： 如果为null就默认为“取消”
	 * @param onPositiveClickListener  确定按钮的点击事件
	 * @param onNegativeClickListener  取消按钮的点击事件
	 */
	private static void dialogTemplet(Context context, String title, String message, String positiveStr, String negativeStr,
			DialogInterface.OnClickListener onPositiveClickListener, DialogInterface.OnClickListener onNegativeClickListener){
		AlertDialog.Builder localBuilder = new AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
		if(!TextUtils.isEmpty(title)) localBuilder.setTitle(title);
		localBuilder.setMessage(message);
		localBuilder.setCancelable(false);
		if (onPositiveClickListener != null) {
			localBuilder.setPositiveButton(positiveStr == null ? "确定" : positiveStr, onPositiveClickListener);
		}
		if(onNegativeClickListener != null){
			localBuilder.setNegativeButton(negativeStr == null ? "取消" : negativeStr, onNegativeClickListener);
		}
		localBuilder.show();
	}
	
	/**
	 * 消息提示框
	 * @param context
	 * @param title   标题
	 * @param message 提示语
	 * @param onPositiveClickListener  确定按钮监听事件
	 */
	public static void showInfoDialog(Context context, String title, String message, DialogInterface.OnClickListener onPositiveClickListener){
		dialogTemplet(context, title, message, null, null, onPositiveClickListener, null);
	}
	
	public static void showConfirmDialog(Context context, String title, String message, DialogInterface.OnClickListener onPositiveClickListener, DialogInterface.OnClickListener onNegativeClickListener){
		dialogTemplet(context, title, message, null, null, onPositiveClickListener, onNegativeClickListener);
	}
	
	

}
