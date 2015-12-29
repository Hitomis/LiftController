package com.zhiitek.liftcontroller.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.util.Xml;

import com.zhiitek.liftcontroller.model.UpdateInfo;

/**
 * 解析类
 * @author ZhaoF
 *
 * @param <T>
 */
public class BaseParser{
	
//	/**
//	 * 解析分页参数
//	 * @param paramString
//	 * @return
//	 * @throws JSONException
//	 */
//	public static Map<String, Integer> parserJSONPage(String paramString) throws JSONException{
//		JSONObject jsonObject = new JSONObject(paramString);
//		JSONArray  jsonArray = jsonObject.getJSONArray(FinalDictionary.APPDATA);
//		if(null !=jsonArray && jsonArray.length() > 0){
//			WeakHashMap<String, Integer> pageMap = new WeakHashMap<String, Integer>();
//			int tempTotalPage = 0; 
//			int tempCurrentPage = 0;
//			for(int i = 0; i<jsonArray.length(); i++){
//				if(!jsonArray.getJSONObject(i).isNull("totalpage")){
//					tempTotalPage = jsonArray.getJSONObject(i).getInt("totalpage");
//					pageMap.put("totalPage", tempTotalPage);
//					continue;
//				}
//				if(!jsonArray.getJSONObject(i).isNull("currentpage")){
//					tempCurrentPage = jsonArray.getJSONObject(i).getInt("currentpage");
//					pageMap.put("currentPage", tempCurrentPage);
//					continue;
//				}
//			}
//			return pageMap;
//		}
//		return null;
//	}
//	
//	/**
//	 * 解析选择框中所用的用户数据
//	 * @param paramString
//	 * @return
//	 * @throws JSONException
//	 */
//	public static List<SpinnerItem> parserJSONUser(String paramString) throws JSONException{
//		JSONObject jsonObject = new JSONObject(paramString);
//		JSONArray  jsonArray = jsonObject.getJSONArray(FinalDictionary.APPDATA);
//		List<SpinnerItem> itemList = null;
//		if(jsonArray != null && jsonArray.length() > 0){
//			JSONObject jobj = null;
//			JSONArray jArray = null;
//			for(int i=0; i<jsonArray.length(); i++){
//				jobj = jsonArray.getJSONObject(i);
//				if(!jobj.isNull(FinalDictionary.Users)){
//					jArray = jobj.getJSONArray(FinalDictionary.Users);
//					break;
//				}
//			}
//			
//			if(jArray != null && jArray.length() >0){
//				itemList = new ArrayList<SpinnerItem>();
//				SpinnerItem item = null;
//				for(int i=0; i<jArray.length(); i++){
//					jobj = jArray.getJSONObject(i);
//					item = new SpinnerItem(jobj.getString("id"), jobj.getString("name"));
//					itemList.add(item);
//				}
//			}
//		}
//		return itemList;
//	}
//	
//	/**
//	 * 解析详细页面底部信息
//	 * @param paramString
//	 * @return
//	 * @throws JSONException
//	 */
//	public static Map<String, Integer> parserJSONSub(String paramString) throws JSONException{
//		Map<String, Integer> subMap = null;
//		JSONObject jsonObject = new JSONObject(paramString);
//		JSONArray jsonArray = jsonObject.getJSONArray(FinalDictionary.APPDATA);
//		if(jsonArray != null && jsonArray.length() > 0){
//			JSONObject jobj = null;
//			subMap = new HashMap<String, Integer>();
//			int temptNum = -1;
//			for(int i=0; i<jsonArray.length(); i++){
//				jobj = jsonArray.getJSONObject(i);
//				if(!jobj.isNull("hasactivity")){
//					temptNum = jobj.getInt("hasactivity");
//					subMap.put("hasactivity", temptNum);
//				}else if(!jobj.isNull("taskundone")){
//					temptNum = jobj.getInt("taskundone");
//					subMap.put("taskundone", temptNum);
//				}else if(!jobj.isNull("taskdone")){
//					temptNum = jobj.getInt("taskdone");
//					subMap.put("taskdone", temptNum);
//				}else if(!jobj.isNull("eventundone")){
//					temptNum = jobj.getInt("eventundone");
//					subMap.put("eventundone", temptNum);
//				}else if(!jobj.isNull("eventdone")){
//					temptNum = jobj.getInt("eventdone");
//					subMap.put("eventdone", temptNum);
//				}
//			}
//		}
//		return subMap;
//	}
	
	/**
	 * (先暂时用于解析服务器放置的应用更新的XML文档)
	 * 解析服务器返回的更新信息
	 * @param is
	 * @return 如果返回null表示解析失败
	 */
	public static UpdateInfo parserUpdateInfo(InputStream is) {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(is, "utf-8");
			int type = parser.getEventType();
			UpdateInfo info = null;
			while(type!=XmlPullParser.END_DOCUMENT){
				switch (type) {
				case XmlPullParser.START_TAG:
					if("upgradeinfo".equals(parser.getName())){
						info = new UpdateInfo();
					}else if("version".equals(parser.getName())){
						info.setVersion(parser.nextText());
					}else if("description".equals(parser.getName())){
						info.setDescription(parser.nextText());
					}else if("apkurl".equals(parser.getName())){
						info.setApkUrl(parser.nextText());
					}
					break;
				}
				type = parser.next();
			}
			return info;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
//	/**
//	 * 解析搜索主页各模块的搜索到的数量数据
//	 * @param context
//	 * @param paramString
//	 * @return
//	 * @throws JSONException
//	 */
//	public static List<SearchHomeInfo> parserJSONSearchCount(Context context,String paramString) throws JSONException{
//		JSONObject jsonObject = new JSONObject(paramString);
//		JSONArray  jsonArray = jsonObject.getJSONArray(FinalDictionary.APPDATA);
//		List<SearchHomeInfo> countList = null;
//		if(jsonArray != null && jsonArray.length() > 0){
//			JSONObject jobj = null;
//			WeakHashMap<String, String> tempMap = null;
//			for(int i=0; i<jsonArray.length(); i++){
//				if(!jsonArray.getJSONObject(i).isNull(FinalDictionary.ALLCOUNT)){
//					jobj = jsonArray.getJSONObject(i).getJSONObject(FinalDictionary.ALLCOUNT);
//					break;
//				}
//			}
//			
//			if(jobj != null){
//				tempMap = new WeakHashMap<String, String>();
//				Iterator iter = jobj.keys();
//				String key = null;
//				while(iter.hasNext()){
//					key = iter.next().toString();
//					tempMap.put(key, "(" + jobj.getString(key) + ")");
//				}
//				if(!tempMap.isEmpty()){
//					countList = new ArrayList<SearchHomeInfo>();
//					SearchHomeInfo count = new SearchHomeInfo("saleslead", SharedPrefUtil.getShareMsgByName(context, SharedPrefConstant.MODUL_NAME, SharedPrefConstant.SALESLEAD_NAME), tempMap.get("salsearchcount"));
//					countList.add(count);
//					
//					count = new SearchHomeInfo("customer", SharedPrefUtil.getShareMsgByName(context, SharedPrefConstant.MODUL_NAME, SharedPrefConstant.CUSTOMER_NAME), tempMap.get("cussearchcount"));
//					countList.add(count);
//					
//					count = new SearchHomeInfo("linkman", SharedPrefUtil.getShareMsgByName(context, SharedPrefConstant.MODUL_NAME, SharedPrefConstant.LINKMAN_NAME), tempMap.get("linksearchcount"));
//					countList.add(count);
//					
//					count = new SearchHomeInfo("business", SharedPrefUtil.getShareMsgByName(context, SharedPrefConstant.MODUL_NAME, SharedPrefConstant.BUSINESS_NAME), tempMap.get("bussearchcount"));
//					countList.add(count);
//					
//					count = new SearchHomeInfo("task", SharedPrefUtil.getShareMsgByName(context, SharedPrefConstant.MODUL_NAME, SharedPrefConstant.ACTIVITYTASK_NAME), tempMap.get("tasksearchcount"));
//					countList.add(count);
//					
//					count = new SearchHomeInfo("event", SharedPrefUtil.getShareMsgByName(context, SharedPrefConstant.MODUL_NAME, SharedPrefConstant.ACTIVITYEVENT_NAME), tempMap.get("eventsearchcount"));
//					countList.add(count);
//					
//					count = new SearchHomeInfo("order", SharedPrefUtil.getShareMsgByName(context, SharedPrefConstant.MODUL_NAME, SharedPrefConstant.ORDER_NAME), tempMap.get("ordersearchcount"));
//					countList.add(count);
//				}
//			}
//		}
//		return countList;
//	}
//	
//	/**
//	 * 解析列表数据
//	 * @param paramString
//	 * @return
//	 * @throws JSONException
//	 */
//	public static List<List<Map<String, Object>>> parserJSONList(String paramString)
//			throws JSONException {
//		JSONObject jsonObject = new JSONObject(paramString);
//		JSONArray jsonArray = jsonObject.getJSONArray(FinalDictionary.APPDATA);
//		if (null != jsonArray && jsonArray.length() > 0) {
//			JSONArray resultArray = null;
//			JSONArray tempJsonArray = null;
//			for (int i = 0; i < jsonArray.length(); i++) {
//				tempJsonArray = jsonArray.getJSONObject(i).optJSONArray(
//						"mbllist");
//
//				if (null != tempJsonArray && tempJsonArray.length() > 0) {
//					resultArray = tempJsonArray;
//					break;
//				}
//			}
//
//			String modelID = null;
//			JSONArray jArray = null;
//			JSONObject jObject = null;
//			List<Object> cellList = null;
//			Map<String, Object> rowMap = null;
//			Map<String, Object> midMap = null;
//			List<Map<String, Object>> itemList = null;
//			List<List<Map<String, Object>>> resultList = null;
//			if(resultArray != null && resultArray.length() > 0){
//				resultList = new ArrayList<List<Map<String, Object>>>();
//				for (int i = 0; i < resultArray.length(); i++) {
//					modelID = resultArray.getJSONObject(i).optString("ID");
//					tempJsonArray = resultArray.getJSONObject(i).optJSONArray(
//							"valuelist");
//					midMap = new HashMap<String, Object>();
//					midMap.put("ID", modelID);
//					if (null != tempJsonArray && tempJsonArray.length() > 0) {
//						itemList = new ArrayList<Map<String, Object>>();
//						itemList.add(midMap);
//						for (int j = 0; j < tempJsonArray.length(); j++) {
//							rowMap = new HashMap<String, Object>();
//							jArray = tempJsonArray.getJSONArray(j);
//							for (int k = 0; null != jArray && k < jArray.length(); k++) {
//								cellList = new ArrayList<Object>();
//								jObject = jArray.getJSONObject(k);
//								cellList.add(jObject.optString("isescape", ""));
//								cellList.add(jObject.optString("width", ""));
//								cellList.add(jObject.optString("value", ""));
//								rowMap.put(j + "" + k + "", cellList);
//							}
//							itemList.add(rowMap);
//						}
//					}
//					resultList.add(itemList);
//				}
//			}
//			return resultList;
//		}
//		return null;
//	}
//	
//	/**
//	 * 解析详细Activity数据
//	 * @param paramString
//	 * @return
//	 * @throws JSONException
//	 */
//	public static JSONArray parserJSONView(String paramString) throws JSONException {
//		JSONArray jsonArray = new JSONObject(paramString).getJSONArray(FinalDictionary.APPDATA);
//		if (jsonArray != null && jsonArray.length() > 0) {
//			JSONObject jobj = null;
//			for (int i = 0; i < jsonArray.length(); i++) {
//				jobj = jsonArray.getJSONObject(i);
//				if (!jobj.isNull("mblviewlist")) {
//					return jobj.getJSONArray("mblviewlist");
//				}
//			}
//		}
//		return null;
//	}
//
//	/**
//	 * 解析添加Activity数据
//	 * @param paramString
//	 * @return
//	 * @throws JSONException
//	 */
//	public static JSONArray parserJSONAdd(String paramString) throws JSONException{
//		JSONObject jsonObject = new JSONObject(paramString);
//		JSONArray ja = new JSONArray();
//		JSONArray resultJsonArray = new JSONArray();
//		Map<String, Object> map = new HashMap<String, Object>();
//		List<Map<String, Object>> resultlist;
//		ja = jsonObject.getJSONArray(FinalDictionary.APPDATA);
//		for (int i = 0; i < ja.length(); i++) {
//			JSONObject resultlistJson = ja.getJSONObject(i);
//			if (!resultlistJson.isNull("resultlist")) {
//				resultJsonArray = (JSONArray) resultlistJson.get("resultlist");
//				break;
//			}
//		}
//		return resultJsonArray;
//	};
//	
//	public static Map<String, Object> parserJsonDataMap(String paramString) throws JSONException{
//		Map<String, Object> dataMap = new HashMap<String, Object>();
//		JSONObject jsonObject = new JSONObject(paramString);
//		JSONArray ja = new JSONArray();
//		JSONObject datajson = new JSONObject();
//		ja = jsonObject.getJSONArray(FinalDictionary.APPDATA);
//		for (int i = 0; i < ja.length(); i++) {
//			JSONObject resultlistJson = ja.getJSONObject(i);
//			if (!resultlistJson.isNull("jsonvalue")) {
//				datajson = resultlistJson.getJSONObject("jsonvalue");
//				 Iterator<String> iterator = datajson.keys();
//			        while (iterator.hasNext())
//			        {
//			        	String key = "";
//				        String value = "";
//			            key = iterator.next();
//			            if (!datajson.isNull(key)) {
//			            	value = datajson.getString(key);	
//						}
//			            dataMap.put(key, value);
//			        }
//			}
//		}
//		return dataMap;
//	}
//	
//	public static String parserQuickAdd(String paramString) throws JSONException{
//		String id = "";
//		JSONObject jsonObject = new JSONObject(paramString);
//		JSONArray ja = new JSONArray();
//		ja = jsonObject.getJSONArray(FinalDictionary.APPDATA);
//		for (int i = 0; i < ja.length(); i++) {
//			JSONObject resultlistJson = ja.getJSONObject(i);
//			if (!resultlistJson.isNull("custId")) {
//				id = (String) resultlistJson.get("custId");
//			}			
//		}
//		return id;
//	}
//	
//	/**
//	 * 解析管理表各模块统计数据
//	 * @param paramString
//	 * @return
//	 * @throws JSONException
//	 */
//	public static Map<String, String> parserJSONManagerCount(String paramString) throws JSONException{
//		JSONObject jsonObject = new JSONObject(paramString);
//		JSONArray  jsonArray = jsonObject.getJSONArray(FinalDictionary.APPDATA);
//		WeakHashMap<String, String> manCountMap = null;
//		if(null !=jsonArray && jsonArray.length() > 0){
//			manCountMap = new WeakHashMap<String, String>();
//			jsonArray = jsonArray.getJSONObject(0).getJSONArray("statlist");
//			JSONObject jobj = null;
//			for(int i = 0; i<jsonArray.length(); i++){
//				jobj = jsonArray.getJSONObject(i);
//				manCountMap.put(jobj.getString("displayname"), jobj.getString("countvalue"));
//			}
//		}
//		
//		return manCountMap;
//	}
//	
//	public static List<SpinnerItem> parserCuslist(String paramString) throws JSONException{
//		JSONObject jsonObject = new JSONObject(paramString);
//		JSONArray ja = new JSONArray();
//		List<SpinnerItem> itemlist = new ArrayList<SpinnerItem>();
//		ja = jsonObject.getJSONArray(FinalDictionary.APPDATA);
//		for (int i = 0; i < ja.length(); i++) {
//			JSONObject resultlistJson = ja.getJSONObject(i);
//			if (!resultlistJson.isNull("meslist")) {
//				JSONArray mesJa = (JSONArray) resultlistJson.get("meslist");
//				String name = "";
//				String ID = "";
//				for (int j = 0; j < mesJa.length(); j++) {
//					JSONObject user = mesJa.getJSONObject(j);
//					SpinnerItem spinnerItem = new SpinnerItem();
//					if (!user.isNull("name")) {
//						name = user.getString("name");
//					}
//					if (!user.isNull("ID")) {
//						ID = user.getString("ID");
//					}
//					spinnerItem.setValue(ID);
//					spinnerItem.setText(name);
//					itemlist.add(spinnerItem);
//				}
//			}
//		}
//		return itemlist;
//	}
//	
//	public static int parserCusPage(String paramString) throws JSONException{
//		JSONObject jsonObject = new JSONObject(paramString);
//		JSONArray ja = new JSONArray();
//		int totalPage = 1;
//		List<SpinnerItem> itemlist = new ArrayList<SpinnerItem>();
//		ja = jsonObject.getJSONArray(FinalDictionary.APPDATA);
//		for (int i = 0; i < ja.length(); i++) {
//			JSONObject resultlistJson = ja.getJSONObject(i);
//			if (!resultlistJson.isNull("totalPage")) {
//				totalPage = (Integer) resultlistJson.get("totalPage");
//			}
//		}
//		return totalPage;
//	}
//	
//	/**
//	 * 工作报告列表数据解析
//	 * @param paramString
//	 * @return
//	 * @throws JSONException
//	 */
//	public static List<Workreport> parserJSONWorkreportList(String paramString) throws JSONException{
//		JSONObject jsonObject = new JSONObject(paramString);
//		JSONArray jsonArray = jsonObject.getJSONArray(FinalDictionary.APPDATA);
//		List<Workreport> workreportList = new ArrayList<Workreport>();
//		Workreport workreport = null;
//		JSONArray jArray = null;
//		JSONObject jObject = null;
//		for (int i = 0; i < jsonArray.length(); i++) {
//			jArray = jsonArray.getJSONObject(i).optJSONArray(
//					"mbllist");
//
//			if (null != jArray && jArray.length() > 0) {
//				break;
//			}
//		}
//		
//		for(int i = 0; i < jArray.length(); i++){
//			jObject = jArray.getJSONObject(i);
//			workreport = new Workreport(
//					jObject.getString("reportid"), 
//					jObject.getString("reportsummary"), 
//					jObject.getString("reportusername"), 
//					jObject.getString("reporttype"), 
//					jObject.getString("reportdate"));
//			workreportList.add(workreport);
//		}
//		return workreportList;
//	}
//	
//	/**
//	 * 解析工作报表明细数据
//	 * @param paramString
//	 * @return
//	 * @throws JSONException
//	 */
//	public static Map<String, String> parserJSONWorkreportView(String paramString) throws JSONException{
//		WeakHashMap<String, String> resultMap = null;
//		JSONObject jsonObject = new JSONObject(paramString);
//		JSONArray jsonArray = jsonObject.getJSONArray(FinalDictionary.APPDATA);
//		JSONObject jObj ;
//		if(jsonArray != null && jsonArray.length() > 0){
//			resultMap = new WeakHashMap<String, String>();
//			for(int i=0; i<jsonArray.length(); i++){
//				jObj = jsonArray.getJSONObject(i);
//				if(!jObj.isNull("reportDate")){
//					resultMap.put("reportDate", jObj.getString("reportDate"));
//				}else if(!jObj.isNull("summary")){
//					resultMap.put("summary", jObj.getString("summary"));
//				}else if(!jObj.isNull("reportusername")){
//					resultMap.put("reportusername", jObj.getString("reportusername"));
//				}
//			}
//		}
//		return resultMap;
//	}
	
}
