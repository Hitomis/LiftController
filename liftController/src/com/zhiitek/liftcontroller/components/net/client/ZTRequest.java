package com.zhiitek.liftcontroller.components.net.client;

import org.json.JSONObject;

import com.zhiitek.liftcontroller.components.net.client.RequestConfig.HttpMethod;

import android.content.Context;

/**
 * 
 * 用于ZHIITEK公司Controller项目的网络请求参数构造任务
 * 
 * @author ZhaoFan
 *
 */
public class ZTRequest {
	
	private RequestConfig config;
	
	public ZTRequest() {
		super();
		config = new RequestConfig();
	}
	
	public static class Builder {
		
		private RequestConfig.ConfigParams configParams;
		
		public Builder() {
			configParams = new RequestConfig.ConfigParams();
		}
		
		public Builder setContext(Context context) {
			configParams.context = context;
			return this;
		}
		
		/** 如果是Http网络请求,设置请求类型 详见：{@link RequestConfig.HttpMethod} */
		public Builder setHttpMethod(HttpMethod method) {
			configParams.method = method;
			return this;
		}
		
		/** 构造网络请求的url */
		public Builder setUrl(String url) {
			configParams.url = url;
			return this;
		}
		
		/** 构造网络请求的时候是否需要显示prompt */
		public Builder setShowPrompt(boolean showPrompt) {
			configParams.showPrompt = showPrompt;
			return this;
		}
		
		/** 构造网络请求的时候需要传递的参数 */
		public Builder setParamters(JSONObject paramters) {
			configParams.paramters = paramters;
			return this;
		}
		
		/** 构造网络请求的时候需要传递的参数头 */
		public Builder setParamHeader(String header) {
			configParams.header = header;
			return this;
		}
		
		/** 构造网络请求连接超时时长 */
		public Builder setConnTimeOut(int connTimeOut) {
			configParams.connTimeOut = connTimeOut;
			return this;
		}
		
		/** 构造网络请求时读取服务器返回输入流时长 */
		public Builder setSoTimeOut(int soTimeOut) {
			configParams.soTimeOut = soTimeOut;
			return this;
		}
		
		/** 构造网络请求后回调接口 */
		public Builder setNetCallback(NetCallback callback) {
			configParams.callback = callback;
			return this;
		}
		
		
		public ZTRequest create() {
			final ZTRequest request = new ZTRequest();
			configParams.apply(request.config);
			return request;
		}

	}

	public RequestConfig getConfig() {
		return config;
	}

}
