package com.zhiitek.liftcontroller.components.net;

import com.zhiitek.liftcontroller.components.net.client.RequestConfig;
import com.zhiitek.liftcontroller.components.net.client.ZTResponse;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * 
 * 遵守HTTP协议的网络请求组件类
 * 
 * @author ZhaoFan
 *
 */
class NetHttpConnection extends AbsNetConnection {
	
    /**
     * Default Encoding format
     */
    static final String DEFAULT_PARAMS_ENCODING = "UTF-8";
    /**
     * Default Content-type
     */
    final static String HEADER_CONTENT_TYPE = "Content-Type";
    
    final static String BODY_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=" + DEFAULT_PARAMS_ENCODING;
    
	@Override
    public ZTResponse performRequest(RequestConfig config) {
        HttpURLConnection urlConnection = null;
        try {
        	// 构建HttpURLConnection
            urlConnection = createUrlConnection(config);
            // 设置Body参数
            setRequestParams(urlConnection, config);
            // 执行网络请求 ,并封装请求结果
            return fetchResponse(urlConnection);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
		return new ZTResponse(ZTResponse.RESP_TIMOUT);
    }
    
    private HttpURLConnection createUrlConnection(RequestConfig config) throws IOException {
        URL newURL = new URL(config.url);
        URLConnection urlConnection = newURL.openConnection();
        urlConnection.setConnectTimeout(config.connTimeOut);
        urlConnection.setReadTimeout(config.soTimeOut);
        urlConnection.setUseCaches(false);
        return (HttpURLConnection) urlConnection;
    }
    
    protected void setRequestParams(HttpURLConnection connection, RequestConfig config)
            throws ProtocolException, IOException {
        connection.setRequestMethod(config.method.toString());
        // add parameters
        if (config.paramters != null) { 
            // enable output & input
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(true);
            // set content type
            connection.setRequestProperty(HEADER_CONTENT_TYPE, BODY_CONTENT_TYPE);
            connection.connect();
            // write parameters data to connection
            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.writeBytes(wrapperParamters(config));
            dataOutputStream.flush();   
            dataOutputStream.close();
        }
    }
    
    private String wrapperParamters(RequestConfig config) throws UnsupportedEncodingException {
    	String paramters = null;
    	if (config.header != null && !"".equals(config.header)) {
    		paramters = String.format("%s=%s", config.header, URLEncoder.encode(config.paramters.toString(), DEFAULT_PARAMS_ENCODING));
    	} else {
    		paramters = URLEncoder.encode(config.paramters.toString(), DEFAULT_PARAMS_ENCODING);
    	}
		return paramters;
    }
    
	private ZTResponse fetchResponse(HttpURLConnection connection) {
		ZTResponse response = null;
		String resultStr = null;
		int responseCode = 0;
		try {
			if (connection.getResponseCode() == 200) {
				responseCode = ZTResponse.RESP_SUCCESS;
				String result = readInputStream(connection.getInputStream());
				resultStr = URLDecoder.decode(result, DEFAULT_PARAMS_ENCODING);
			}
		} catch (Exception e) {
			responseCode = ZTResponse.RESP_FAILURE;
		} finally {
			// 构建response
			response = new ZTResponse(responseCode);
			if (resultStr != null) {
				response.setEntity(resultStr);
			}
		}
		return response;
	}
    
	/**
	 * 从输入流中读取字符串
	 * 
	 * @param is
	 * @return
	 */
	private String readInputStream(InputStream is) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int len = 0;
			byte[] buffer = new byte[1024];
			while ((len = is.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}
			is.close();
			baos.close();
			byte[] result = baos.toByteArray();
			// 解析result里面的字符串
			return new String(result);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
    
}
