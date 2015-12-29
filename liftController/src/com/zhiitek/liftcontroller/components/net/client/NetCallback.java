package com.zhiitek.liftcontroller.components.net.client;

import org.json.JSONObject;

public interface NetCallback {
	
	/**
	 * @param resultJson 可能为null
	 */
	void callback(JSONObject resultJson);

}
