package com.zhiitek.liftcontroller.components.net;

import com.zhiitek.liftcontroller.components.net.client.RequestConfig;
import com.zhiitek.liftcontroller.components.net.client.ZTResponse;

/**
 * AbsNetConnection空实现 用来避免空指针
 * 
 * @author ZhaoFan
 *
 */
class NetNoneConnection extends AbsNetConnection{

	@Override
	protected ZTResponse performRequest(RequestConfig config) {
		return new ZTResponse(-10000);
	}

}
