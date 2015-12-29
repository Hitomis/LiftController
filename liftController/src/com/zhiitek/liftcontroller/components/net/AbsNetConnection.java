package com.zhiitek.liftcontroller.components.net;

import android.content.Context;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.SystemClock;

import com.zhiitek.liftcontroller.components.net.behavior.PromptBehavior;
import com.zhiitek.liftcontroller.components.net.client.RequestConfig;
import com.zhiitek.liftcontroller.components.net.client.ZTRequest;
import com.zhiitek.liftcontroller.components.net.client.ZTResponse;

/**
 * 
 * 网络请求的抽象类,扩展其他网络请求方式,请继承该类
 * 
 * @author ZhaoFan
 *
 */
abstract class AbsNetConnection {
	
	/** 网络请求前的用户体验  */
	private PromptBehavior promptBehaviro;
	
	/**
	 * 执行网络请求
	 * @param config 网络请求需要用到的配置对象
	 * @return ZTResponse
	 */
    protected abstract ZTResponse performRequest(RequestConfig config);
    
    /** prompt显示的最短时间  */
    private static final long MIN_SHOWTIME = 500;
    
    /**
     * 显示 prompt
     * @param context
     */
	private void showPrompt(Context context) {
    	promptBehaviro.showPrompt(context);
    }
    
    /**
     * 关闭 prompt
     * @param context
     */
	private void hidePrompt() {
    	promptBehaviro.hidePrompt();
    }
    
    public void setPromptBehaviro(PromptBehavior promptBehaviro) {
		this.promptBehaviro = promptBehaviro;
	}

    /**
     * 执行网络请求
     * @param request ZTRequest
     */
    void execute(ZTRequest request) {
    	if (request != null) {
    		final RequestConfig config = request.getConfig();
    		
    		if (config == null) return ;
    		
    		new AsyncTask<Void, Void, ZTResponse>() {
    			
				@Override
				protected void onPreExecute() {
		    		if (config.showPrompt && config.context != null) {
		    			// step1. 是否要显示网络请求前的prompt
		    			showPrompt(config.context);
		    		} 
				}

				@Override
				protected ZTResponse doInBackground(Void... params) {
					long startTimes = System.currentTimeMillis();
					
					// step2. 执行网络请求
					ZTResponse response;
					if (hasNetwork(config.context)) {
						 response = performRequest(config);
					} else {
						response = new ZTResponse(ZTResponse.RESP_NO_NETWORK);
					}
					
					long diffTimes = System.currentTimeMillis() - startTimes;
					if (diffTimes < MIN_SHOWTIME) {
						SystemClock.sleep(MIN_SHOWTIME - diffTimes);
					}
					
					return response;
				}

				@Override
				protected void onPostExecute(ZTResponse response) {
					// step3. 是否要关闭网络请求时的prompt
					hidePrompt();
					// step4. 处理网络请求后的结果
					response.handleOnMainThread(config);
				}
    		}.execute();
    	}
    }
    
	/**
	 * 网络连接是否可用
	 * @param context
	 * @return
	 */
	protected boolean hasNetwork(Context context){
		ConnectivityManager con = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo workinfo = con.getActiveNetworkInfo();
		if(workinfo == null || !workinfo.isAvailable()) {
			return false;
		}
		return true;
	}

}
