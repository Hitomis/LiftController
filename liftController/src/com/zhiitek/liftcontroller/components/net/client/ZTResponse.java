package com.zhiitek.liftcontroller.components.net.client;

import android.content.Context;
import android.widget.Toast;

import com.zhiitek.liftcontroller.utils.AppConstant;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * 用于ZHIITEK公司Controller项目的网络请求后的结果处理任务
 * 
 * @author ZhaoFan
 *
 */
public class ZTResponse {
	
	/** 网络请求成功 */
	public final static int RESP_SUCCESS = 200;
	/** 网络请求错误 */
	public final static int RESP_FAILURE = -200;
	/** 网络请求超时 */
	public final static int RESP_TIMOUT = -201;
	/** JSON解析错误 */
	public final static int RESP_JSON_ERROR = -202;
	/** 网络不可用 */
	public static final int RESP_NO_NETWORK = -203;
	/** 网络接口调用不通过 */
	public static final int RESP_NOT_COMPLY = -204;
	
	public static final String NET_KEY_CMD = "cmd";
	
	public static final String NET_KEY_DATA = "data";
	
	public static final String NET_KEY_EMSG = "emsg";
	
	private Context context;
	
	private int responseCode;
	
	private String entity;
	
	private String eMsg;
	
	public ZTResponse(int responseCode) {
		this.responseCode = responseCode;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public void handleOnMainThread(RequestConfig config) {
		this.context = config.context;
		JSONObject dataJson = null;
		if (entity != null && !"".equals(entity)) {
			dataJson = parseEntity();
		}
		if (RESP_SUCCESS != responseCode) {
			errorHandling();
		}
		if (config.callback != null) {
			config.callback.callback(dataJson);
		}
	}

	private void errorHandling() {
		switch (responseCode) {
		case RESP_FAILURE:
			errorPrompt("网络请求异常, 请联系我们", Toast.LENGTH_LONG);
			break;
		case RESP_JSON_ERROR:
			errorPrompt("网络数据错误, 请联系我们", Toast.LENGTH_LONG);
			break;
		case RESP_TIMOUT:
			errorPrompt("网络请求超时", Toast.LENGTH_SHORT);
			break;
		case RESP_NO_NETWORK:
			errorPrompt("网络不可用", Toast.LENGTH_SHORT);
			break;
		case RESP_NOT_COMPLY:
			errorPrompt(eMsg, Toast.LENGTH_SHORT);
			break;
		default:
			errorPrompt("未知错误, 请联系我们", Toast.LENGTH_SHORT);
			break;	
		}
	}
	
	/**
	 * 服务器返还数据的预解析
	 * 可能改变responseCode的值
	 * @return 服务器返回JSON字符串中data的JsonObject
	 */
	private JSONObject parseEntity() {
		JSONObject dataJson = null;
		if (entity.startsWith("PDU")) {
			try {
				JSONObject resultJson = new JSONObject(entity.substring(AppConstant.PREFIX.length() + 1));
				dataJson = resultJson.getJSONObject(NET_KEY_DATA);
				if (!checkCmd(resultJson.getInt(NET_KEY_CMD))) { // 不通过
					responseCode = RESP_NOT_COMPLY;
					eMsg = dataJson.getString(NET_KEY_EMSG);
				}
			} catch (JSONException e) {
				responseCode = RESP_JSON_ERROR;
			}
		} else if (entity.contains("code")){// 重启设备时返回的数据格式{"code":*}

		} else {
			responseCode = RESP_JSON_ERROR;
		}
		return dataJson;
	}
	
	private void errorPrompt(String text, int duration) {
		Toast.makeText(context, text, duration).show();
	}
	
	/**
	 * 检查命令码
	 * @param reciveCmd
	 * @return
	 */
	protected static boolean checkCmd(int reciveCmd) {
		if ((reciveCmd & 0x080) == 0) {
			return true;
		}
		return false;
	}
}
