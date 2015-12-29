package com.zhiitek.liftcontroller.components;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.zhiitek.liftcontroller.R;
import com.zhiitek.liftcontroller.views.CustomProgressDialog;

public class ImageHelper {
	
	private ImageLoader imageLoader;
	
	private DisplayImageOptions options;
	
	private Dialog dialog;
	
	private Context mContext;
	
	private OnLoadCompleteListener loadCompleteListener;
	
	private interface OnLoadCompleteListener {
		public void loadCompleteListener(Bitmap bitmap);
	} 
	
	private static class SingletonHolder {
		public final static ImageHelper instance = new ImageHelper();
	}

	public static ImageHelper getInstance() {
		return SingletonHolder.instance;
	}
	
	/**
	 * @param context 可以为null, 为null的情况下不会显示加载中的progress dialog
	 */
	public void init(Context context) {
		imageLoader = ImageLoader.getInstance();
		
		options = new DisplayImageOptions.Builder()
									.cacheInMemory(false)
							        .cacheOnDisk(true)
							        .showImageForEmptyUri(R.drawable.icon_blank)  // 设置图片Uri为空或是错误的时候显示的图片  
							        .showImageOnFail(R.drawable.icon_blank)       // 设置图片加载或解码过程中发生错误显示的图片    
									.bitmapConfig(Bitmap.Config.RGB_565)
									.imageScaleType(ImageScaleType.EXACTLY)
									.build();
		if (context != null) {
			mContext = context;
			dialog = new CustomProgressDialog(mContext, R.style.loading_dialog);
		}
	}
	
	private class ImageLoadCompleteListener implements ImageLoadingListener {
		
		@Override
		public void onLoadingStarted(String arg0, View arg1) {
		}

		@Override
		public void onLoadingComplete(String arg0, View arg1, Bitmap loadedImage) {
			dismissDialog();
			if (loadCompleteListener != null)
			loadCompleteListener.loadCompleteListener(loadedImage);
		}

		@Override
		public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
			dismissDialog();
		}
		
		@Override
		public void onLoadingCancelled(String arg0, View arg1) {
		}
		
	}
	
	/**
	 * @param uri 图片url
	 * @param completetListener 图片加载完毕后回调接口
	 */
	public void displayImage(String uri, final OnLoadCompleteListener completetListener) {
		showDialog();
		loadCompleteListener = completetListener;
		imageLoader.loadImage(uri, options, new ImageLoadCompleteListener());
	}

	/**
	 * @param uri 图片uri
	 * @param imageView 显示图片的ImageView控件
	 */
	public void displayImage(String uri ,final ImageView imageView) {
		showDialog();
		imageLoader.displayImage(uri, imageView, options, new ImageLoadCompleteListener());
	}
	
	/**
	 * @param uri 图片uri
	 * @param width 图片宽度
	 * @param height 图片高度
	 * @param completetListener 图片加载完毕后回调接口
	 */
	public void displayImage(String uri, int width, int height, final OnLoadCompleteListener completetListener) {
		showDialog();
		loadCompleteListener = completetListener;
		ImageSize imageSize = new ImageSize(width, height);
		imageLoader.loadImage(uri, imageSize, options, new ImageLoadCompleteListener());
	}
	
	/**
	 * 同步加载
	 * @param uri 图片uri
	 * @return 图片的bitmap
	 */
	public Bitmap displayImage(String uri) {
		return imageLoader.loadImageSync(uri, options);
	}
	
	/**
	 *  同步加载
	 * @param uri 图片uri
	 * @param width 图片宽
	 * @param height 图片高
	 * @return 图片的bitmap
	 */
	public Bitmap displayImage(String uri, int width, int height) {
		ImageSize imageSize = new ImageSize(width, height);
		return imageLoader.loadImageSync(uri, imageSize, options);
	}
	
	/**
	 * 清除图片的缓存
	 */
	public void clearCache() {
		if (imageLoader == null) {
			imageLoader = ImageLoader.getInstance();
		}
		imageLoader.clearMemoryCache();
		imageLoader.clearDiskCache();
	}
	
	private void showDialog() {
		if (dialog != null) {
			dialog.show();
		}
	}
	
	private void dismissDialog() {
		if (dialog != null) {
			dialog.dismiss();
		}
	}
	
	
}
