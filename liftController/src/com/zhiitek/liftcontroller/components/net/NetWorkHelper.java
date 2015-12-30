package com.zhiitek.liftcontroller.components.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.zhiitek.liftcontroller.components.net.NetConnectionFactory.NetType;
import com.zhiitek.liftcontroller.components.net.client.NetCallback;
import com.zhiitek.liftcontroller.components.net.client.RequestConfig.HttpMethod;
import com.zhiitek.liftcontroller.components.net.client.ZTRequest;
import com.zhiitek.liftcontroller.components.net.client.ZTRequest.Builder;
import com.zhiitek.liftcontroller.components.net.client.ZTResponse;

/**
 * 
 * 网络请求任务帮助类, 用来简化网络请求任务的使用
 * 
 * @author ZhaoFan
 *
 */
public class NetWorkHelper {
	
	private Context context;
	
	private static class SingletonHolder {
		public final static NetWorkHelper instance = new NetWorkHelper();
	}

	public static NetWorkHelper getInstance() {
		return SingletonHolder.instance;
	}
	
	public void init(Context context) {
		this.context = context;
	} 
	
	private ZTRequest.Builder getDefaultBuilderWithPrompt(Context context, String url) {
		Builder builder = new ZTRequest.Builder();
		builder.setContext(context)
			   .setUrl(url)
			   .setParamHeader("PDU")//ZHIITEK 接口参数默认需要带上该参数头部字符
			   .setConnTimeOut(6000)
			   .setSoTimeOut(6000)
			   .setHttpMethod(HttpMethod.POST)
			   .setShowPrompt(true);
		return builder;
	}
	
	private ZTRequest.Builder getDefaultBuilderWithoutPrompt(Context context, String url) {
		Builder builder = new ZTRequest.Builder();
		builder.setContext(context)
			   .setUrl(url)
			   .setParamHeader("PDU")//ZHIITEK 接口参数默认需要带上该参数头部字符
			   .setConnTimeOut(6000)
			   .setSoTimeOut(6000)
			   .setHttpMethod(HttpMethod.POST)
			   .setShowPrompt(false);
		return builder;
	}
	
	private ZTRequest.Builder getGetBuilderWithoutPrompt(Context context, String url) {
		Builder builder = new ZTRequest.Builder();
		builder.setContext(context)
			   .setUrl(url)
			   .setConnTimeOut(6000)
			   .setSoTimeOut(6000)
			   .setHttpMethod(HttpMethod.GET)
			   .setShowPrompt(false);
		return builder;
	}
	
	private void doExec(ZTRequest request) {
		AbsNetConnection absNetConn = NetConnectionFactory.createNetConnection(NetType.HTTP);
		absNetConn.execute(request);
	}
	
	public void execHttpNetGet(String url, NetCallback callback) {
		ZTRequest request = getGetBuilderWithoutPrompt(context, url)
				.setParamters(null)
				.setNetCallback(callback)
				.create();
		doExec(request);
	}
	
	public void execHttpNet(String url, JSONObject jsonParamters) {
		this.execHttpNet(url, jsonParamters, null);
	}
	
	public void execHttpNet(String url, NetCallback callback) {
		this.execHttpNet(url, null, callback);
	}
	
	public void execHttpNet(ZTRequest request) {
		doExec(request);
	}
	
	public void execHttpNet(String url, JSONObject jsonParamters, NetCallback callback) {
		ZTRequest request = getDefaultBuilderWithPrompt(context, url)
								.setParamters(jsonParamters)
								.setNetCallback(callback)
								.create();
		doExec(request);
	}

	public void execHttpNet(Context context, String url, JSONObject jsonParamters, NetCallback callback) {
		ZTRequest request = getDefaultBuilderWithPrompt(context, url)
				.setParamters(jsonParamters)
				.setNetCallback(callback)
				.create();
		doExec(request);
	}
	
	public void execHttpNetWithoutPrompt(String url, JSONObject jsonParamters, NetCallback callback) {
		ZTRequest request = getDefaultBuilderWithoutPrompt(context, url)
								.setParamters(jsonParamters)
								.setNetCallback(callback)
								.create();
		doExec(request);
	}
	
	/**
	 * 初始化用于网络请求的JSONObject参数
	 * @param cmd 接口命令码 【ZHIITEK 自定义】
	 * @return
	 */
	public JSONObject initJsonParameters(int cmd) throws JSONException{
		JSONObject jsonResult = new JSONObject();
		JSONObject jsonData = new JSONObject();
		jsonResult.put(ZTResponse.NET_KEY_CMD, cmd);
		jsonResult.put(ZTResponse.NET_KEY_DATA, jsonData);
		return jsonResult;
	}
	
	/**
	 * <p>因为 响应JSON格式为：{"cmd":"int", "data":{}}</p>
	 * 故本方法是将数据封装在待响应JSONObject中的data里面
	 * @param key 
	 * @param value
	 * @param jsonParams 调用{@link NetWorkHelper#initJsonParameters(int)}之后获取的JSONObject
	 * @throws JSONException
	 */
	public void setDataInResponseJson(String key, Object value, JSONObject jsonParams) throws JSONException {
		jsonParams.getJSONObject(ZTResponse.NET_KEY_DATA).put(key, value);
	}
}
