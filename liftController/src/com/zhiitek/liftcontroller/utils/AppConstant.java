package com.zhiitek.liftcontroller.utils;

public class AppConstant {
	/** intent传递电梯信息列表的key */
	public static final String INTENT_KEY_LIFTINFOLIST = "infolist";
	/** 服务器返回数据的头字段 */
	public static final String PREFIX = "PDU";
	/** 设备号 */
	public static final String DEV_SERIALS = "devSerial";
	/** 电梯编号 */
	public static final String LIFT_NUMBER = "liftNo";
	/** 功能码 */
	public static final String KEY_SOCKET_CMD = "cmd";
	/** socket通信数据的key */
	public static final String KEY_SOCKET_DATA = "data";
	/** socket通信时checkcode的key */
	public static final String KEY_SOCKET_CHECKCODE = "chkCode";
	
	public static final String KEY_SOCKET_ENO = "eno";
	/** socket通信返回的message的key */
	public static final String KEY_SOCKET_EMESSAGE = "emsg";
	/** 用户名的key */
	public static final String KEY_USER_ID = "username";
	/** 用户密码的key */
	public static final String KEY_PASSWORD = "password";
	/** 用户类型的key */
	public static final String KEY_USER_TYPE = "userType";
	/** 服务器url的key */
	public static final String KEY_SERVICE_URL = "serviceUrl";
	/** 文件不存在 */
	public static final String FILE_NOT_EXISTS = "file not exists";
	/** 进入硬件测试中的参数查询界面，是否需要查询数据的key */
	public static final String KEY_BLACKBOX_NEED_QUERY_CONFIG_FLAG = "blackbox_queryconfig";
	
	/** 上次执行清理程序的时间 */
	public static final String KEY_LAST_CLEAR_DATE = "last_clear_date";
	/** 保存用户名密码的shareprefrence的key */
	/** 是否设置过极光推送的别名 */
	public static final String KEY_JPUSH_SET_ALIAS = "key_jpush_set_alias";
	
	public static final String KEY_SP_USER = "user";
	
	/** 告警数目的key */
	public static final String KEY_ALARM_COUNT = "alarmCount";
	/** 任务数目的key */
	public static final String KEY_TASK_COUNT = "taskCount";
	/** 进去MainActivity时,没有网络 */
	public static final String INTENT_KEY_ENTER_MAINACTIVITY_WITH_NO_NET = "enter_mainactivity_with_no_net";
	
	/** ListView 中每页列表条数 */
	public static final int PAGE_COUNT = 20;
}
