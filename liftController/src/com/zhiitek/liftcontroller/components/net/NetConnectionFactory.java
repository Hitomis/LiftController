package com.zhiitek.liftcontroller.components.net;

import com.zhiitek.liftcontroller.components.net.behavior.DialogPromptBehavior;
import com.zhiitek.liftcontroller.components.net.behavior.NonePromptBehavior;
import com.zhiitek.liftcontroller.components.net.behavior.PromptBehavior;

/**
 * 
 * 网络任务工厂类
 * 
 * @author ZhaoFan
 *
 */
final class NetConnectionFactory {
	
    static enum NetType {
    	/** http普通通信协议 */
        HTTP("http"),
        /** http加密通信协议 */
        HTTPS("https"),
        /** socket tcp 通信协议*/
        TCP("tcp"),
        /** socket udp 通信协议*/
        UDP("udp");
        
        private String netType = "";

        private NetType(String type) {
        	netType = type;
        }

        @Override
        public String toString() {
            return netType;
        }
    }
	
    /**
     * NetWork 组件工厂方法
     * @param type
     * @return
     */
	static AbsNetConnection createNetConnection(NetType type) {
		AbsNetConnection absNetConn = null;
		PromptBehavior prompt = null;
		switch (type) {
		case HTTP:
			absNetConn = new NetHttpConnection();
			prompt = new DialogPromptBehavior();
			break;
		case HTTPS:
			break;
		case TCP:
			break;
		case UDP:
			break;
		default:
			absNetConn = new NetNoneConnection();
			prompt = new NonePromptBehavior();
			break;
		}
		absNetConn.setPromptBehaviro(prompt);
		return absNetConn;
	}
	
}
