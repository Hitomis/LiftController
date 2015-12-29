 package com.zhiitek.liftcontroller.components.net.client;

import org.json.JSONObject;

import android.content.Context;


/**
 * 
 * 网络请求参数的配置信息对象
 * 
 * @author ZhaoFan
 *
 */
public class RequestConfig {
	
	/**
	 * HTTP 网络请求的网络交互类型 现在支持 POST 和 GET 类型
	 */
    public static enum HttpMethod {
        GET("GET"),
        POST("POST");
        
        /** HTTP request type */
        private String mHttpMethod;

        private HttpMethod(String method) {
            mHttpMethod = method;
        }

        @Override
        public String toString() {
            return mHttpMethod;
        }
    }
	
    public Context context;
    public int soTimeOut;
    public int connTimeOut;
	public String url;
	public boolean showPrompt;
	public String header;
	public JSONObject paramters;
	public HttpMethod method;
	public NetCallback callback;
	
    static class ConfigParams {
    	
    	Context context;
		String url;
		boolean showPrompt;
		String header;
		JSONObject paramters;
		int soTimeOut = 6000;
	    int connTimeOut = 6000;
	    HttpMethod method = HttpMethod.POST;
	    NetCallback callback;
		
	    /**
	     * 将ConfigParams中属性值赋值给参数config
	     * @param config
	     */
		public void apply(RequestConfig config) {
			config.context = context;
			config.url = url;
			config.showPrompt = showPrompt;
			config.header = header;
			config.paramters = paramters;
			config.soTimeOut = soTimeOut;
			config.connTimeOut = connTimeOut;
			config.method = method;
			config.callback = callback;
		}
	}
	
}
