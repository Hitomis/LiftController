package com.zhiitek.liftcontroller.components.net;


public class NetWorkCons {
	/*
	 * HTTP请求的接口命令码*************************************************************************
	 */
	/** 登陆命接口令码，登陆成功返回该用户管理的所有电梯信息 */
	public static final int CMD_HTTP_LOGIN_AND_GET_LIFTINFOS = 5;
	/** 下载任务列表命接口令码 */
	public static final int CMD_HTTP_GET_TASKLIST = 6;
	/** 解决任务命接口令码 */
	public static final int CMD_HTTP_RESOLVE_TASK = 7;
	/** 下载告警主要信息列表接口命令码【事件码、原始数据、发生时间】 */
	public static final int CMD_HTTP_GET_ALARMLIST = 8;
	/** 更新电梯状态接口命令码 */
	public static final int CMD_HTTP_UPDATE_LIFT_STATUS = 9;
	/** 获取重置密码的验证码的接口命令码 */
	public static final int CMD_HTTP_GET_SECURITY_CODE = 10;
	/** 修改登录密码接口命令码 */
	public static final int CMD_HTTP_MODIFY_LOGIN_PASSWORD = 11;
	/** 下载告警详细信息列表接口命令码 */
	public static final int CMD_HTTP_GET_DETAILS_ALARMLIST = 12;
	/** 下载告警总数和任务总数接口命令码 */
	public static final int CMD_HTTP_GET_ALARM_AND_TASK_COUNT = 13;
	/** 下载一条告警详细信息接口命令码 */
	public static final int CMD_HTTP_GET_ONE_ALARM_DETAILS = 14;
	/** 通告列表接口命令码 */
	public static final int CMD_HTTP_GET_NOTICE_LIST = 15;
	/** 行政区域和小区信息接口命令码 */
	public static final int CMD_HTTP_GET_COMMUNITY_LIST = 16;
	/** 发布通告接口命令码 */
	public static final int CMD_HTTP_POST_NOTICE = 17;
	//******************************************************************************************
	
	/*
	 * SOCKET请求的接口命令码***********************************************************************
	 */
	/** 获取电梯编号的接口命令码 */
	public static final int CMD_SOCKET_GET_LIFT_NUMBER = 1;
	/** 获取电梯配置信息的接口命令码 */
	public static final int CMD_SOCKET_GET_LIFT_CONFIG_INFO = 3;
	/** 设置电梯配置信息的接口命令码 */
	public static final int CMD_SOCKET_SETUP_LIFT_CONFIG = 4;
	/** 下载电梯告警列表的接口命令码 */
	public static final int CMD_SOCKET_GET_LIFT_ALARMLIST = 5;
	/** 解决告警的接口命令码 */
	public static final int CMD_SOCKET_RESOLVE_LIFT_ALARMS = 6;
	/** 提交电梯检验结论的接口命令码 */
	public static final int CMD_SOCKET_SUBMIT_INSPECT_RESULT = 7;
	/** 读取电梯以太网信息的接口命令码 */
	public static final int CMD_SOCKET_GET_LIFT_ETHERNET_INFO = 8;
	/** 配置电梯以太网信息的接口命令码 */
	public static final int CMD_SOCKET_SETUP_LIFT_ETHERNET = 9;
	/** 查询蓝牙和音量信息的接口命令码 */
	public static final int CMD_SOCKET_GET_LIFT_BT_AND_VOLUME = 10;
	/** 设置蓝牙开关的接口命令码 */
	public static final int CMD_SOCKET_SET_BT_ENABLE = 11;
	/** 设置音量的接口命令码 */
	public static final int CMD_SOCKET_SET_VOLUME = 12;
	/** 获取电梯日志文件的接口命令码 */
	public static final int CMD_SOCKET_GET_LIFT_LOG_FILE = 13;
	/** 重启设备的接口命令码 */
	public static final int CMD_SOCKET_REBOOT_LIFT = 14;
	/** 设备自动化测试的接口命令码 */
	public static final int CMD_SOCKET_LIFT_AUTOMATED_TEST = 15;
	/** 测试显示屏的接口命令码 */
	public static final int CMD_SOCKET_TEST_DISPLAY = 16;
	/** 测试麦克风的接口命令码 */
	public static final int CMD_SOCKET_TEST_MIKE = 17;
	/** 测试摄像头的接口命令码 */
	public static final int CMD_SOCKET_TEST_CAMERA = 18;
	/** 关闭测试音乐的接口命令码 */
	public static final int CMD_SOCKET_CLOSE_TEST_MUSIC = 19;
	/** 测试看门狗的接口命令码 */
	public static final int CMD_SOCKET_TEST_WATCHDOG = 20;
	//******************************************************************************************
	
	/*
	 * JSON数据中的KEY值**************************************************************************
	 */
	/** 用户名 */
	public static final String JSON_KEY_USERID = "userId";
	/** 用户密码 */
	public static final String JSON_KEY_USERPWD = "userPwd";
	/** 用户类型 */
	public static final String JSON_KEY_USERTYPE = "userType";
	/** 登陆标识【0-成功，1-失败】 */
	public static final String JSON_KEY_RESULT = "result";
	/** 用户当前密码 */
	public static final String JSON_KEY_CURRENTPWD = "currentPwd";
	/** 用户新密码 */
	public static final String JSON_KEY_NEWPWD = "newPwd";
	/** 密码相关接口标识【0-成功，1-失败】 */
	public static final String JSON_KEY_SUCCESS = "Success";
	/** 密码相关接口失败返回的msg */
	public static final String JSON_KEY_MESSAGE = "Message";
	/** 用户手机号 */
	public static final String JSON_KEY_USERPHONE = "userPhone";
	/** 页码 */
	public static final String JSON_KEY_PAGE = "page";
	/** 每页数据条数 */
	public static final String JSON_KEY_ROWS = "rows";
	/** 数据总数 */
	public static final String JSON_KEY_TOTAL = "total";
	/** 电梯信息列表 */
	public static final String JSON_KEY_INFOLIST = "infoList";
	/** 电梯编号 */
	public static final String JSON_KEY_LIFTNO = "liftNo";
	/** 电梯名称 */
	public static final String JSON_KEY_LIFTNAME = "liftName";
	/** 电梯状态 */
	public static final String JSON_KEY_LIFTSTATUS = "liftStatus";
	/** 电梯信息列表 */
	public static final String JSON_KEY_LIFTINFOLIST = "liftInfoList";
	/** 电梯所在小区名称 */
	public static final String JSON_KEY_BLOCKNAME = "blockName";
	/** 电梯所在地 */
	public static final String JSON_KEY_LIFTADD = "liftAdd";
	/** 电梯详细地址 */
	public static final String JSON_KEY_ADDRESS = "address";
	/** 告警级别 */
	public static final String JSON_KEY_FAULTLEVEL = "faultLevel";
	/** 告警事件名称 */
	public static final String JSON_KEY_FAULTNAME = "faultName";
	/** 告警照片 */
	public static final String JSON_KEY_FAULTPHOTO = "faultPhoto";
	/** 返回字符串信息 */
	public static final String JSON_KEY_EMSG = "emsg";
	/** 告警总数 */
	public static final String JSON_KEY_FAULTCOUNT = "faultCount";
	/** 告警录音 */
	public static final String JSON_KEY_FAULTAUDIO = "faultAudio";
	/** 任务总数 */
	public static final String JSON_KEY_TASKCOUNT = "taskCount";
	/** 任务列表 */
	public static final String JSON_KEY_TASKLIST = "taskList";
	/** 任务编号 */
	public static final String JSON_KEY_TASKID = "taskID";
	/** 任务名称 */
	public static final String JSON_KEY_TASKNAME = "taskName";
	/** 任务类型 */
	public static final String JSON_KEY_TASKTYPE = "taskType";
	/** 任务创建时间 */
	public static final String JSON_KEY_CREATETIME = "createTime";
	/** 任务备注 */
	public static final String JSON_KEY_MEMO = "memo";
	/** 告警列表 */
	public static final String JSON_KEY_FAULTLIST = "faultList";
	/** 告警事件码 */
	public static final String JSON_KEY_FAULTNO = "faultNo";
	/** 告警事件ID */
	public static final String JSON_KEY_FAULTID = "faultId";
	/** 原始数据 */
	public static final String JSON_KEY_FAULTDATA = "faultData";
	/** 告警时间 */
	public static final String JSON_KEY_FAULTTIME = "faultTime";
	/** 超期检验列表 */
	public static final String JSON_KEY_INSPECT = "inspect";
	/** 检验超期日 */
	public static final String JSON_KEY_NEXTINSPECTDATE = "nextInspectDate";
	/** 上次检验状态 */
	public static final String JSON_KEY_STATUS = "status";
	/** 检验类型 */
	public static final String JSON_KEY_INSPTYPE = "inspType";
	/** 检验结论 */
	public static final String JSON_KEY_INSPCONCLUDE = "inspConclude";
	/** 检验编号 */
	public static final String JSON_KEY_INSPNO = "inspNo";
	/** 检验备注 */
	public static final String JSON_KEY_INSPMEMO = "inspMemo";
	/** 检验不合格的用原因 */
	public static final String JSON_KEY_INSPREMARK = "inspRemark";
	/** 检验报告编号 */
	public static final String JSON_KEY_INSPREPORTNO = "inspReportNo";
	/** 通告发布开始日期 */
	public static final String JSON_KEY_NOTICE_POSTTIME = "postTime";
	/** 通告发布 戒指日期*/
	public static final String JSON_KEY_NOTICE_LIFETIME = "lifeTime";
	/** 通告发布的类型*/
	public static final String JSON_KEY_NOTICE_TYPE = "type";
	/** 通告发布的目标 */
	public static final String JSON_KEY_NOTICE_TARGET = "target";
	/** 通告发布的内容 */
	public static final String JSON_KEY_NOTICE_CONTENT = "content";
	
	//******************************************************************************************

	/*
	 * 网络请求URL相关*****************************************************************************
	 */
	/** 测试环境的Host类型 */
	private static final int HOST_ENVIRTYPE_TEST = 1;
	/** 云环境的Host类型 */
	private static final int HOST_ENVIRTYPE_CLOUD = 2;
	/** 自定义的Host类型 */
	private static final int HOST_ENVIRTYPE_MINE = 3;
	/** 当前的Host类型 */
	private static final int HOST_ENVIRTYPE_CURRENT = HOST_ENVIRTYPE_TEST;
	/** URL前缀 */
	private static final String URL_PREFIX = "http://";
	/** Action前缀 */
	private static final String ACTION_PREFIX = "/liftman/liftman/appAction!doNotNeedSession_";
	/** 登陆接口的URL */
	private static final String URL_LOGIN_DOMAIN = "/liftman/liftman/appAction!doNotNeedSession_downCorpLifts.action";
	/** 获取任务列表接口的URL */
	private static final String URL_GET_TASK_DOMAIN = "/liftman/liftman/appAction!doNotNeedSession_downLoadTask.action";
	/** 更新版本的URL */
	private static final String URL_UPDATE_VERSION_DOMAIN = "/liftman/resource/tm/version/xml/controllerupgrade.xml";
	/** 解决任务接口的URL */
	private static final String URL_RESOLVE_TASK_DOMAIN = "/liftman/liftman/appAction!doNotNeedSession_handleTask.action";
	/** 修改登陆密码接口的URL */
	private static final String URL_MODIFY_LOGIN_PASSWORD_DOMAIN = "/liftman/liftman/appAction!doNotNeedSession_tokenPwd.action";
	/** 重置登陆密码时获取验证码接口的URL */
	private static final String URL_GET_SECURITY_CODE_DOMAIN = "/liftman/liftman/appAction!doNotNeedSession_updatePwd.action";
	/** 更新电梯状态接口的URL */
	private static final String URL_UPDATE_LIFT_STATUS_DOMAIN = "/liftman/liftman/appAction!doNotNeedSession_updateLiftState.action";
	/** 获取告警列表接口的URL */
	private static final String URL_GET_ALARM_DOMAIN = "/liftman/liftman/appAction!doNotNeedSession_downLoadUserEmerg.action";
	/** 获取任务和告警的数量接口的URL */
	private static final String URL_GET_TASK_AND_ALARM_COUNT_DOMAIN = "/liftman/liftman/appAction!doNotNeedSession_countEmergAndTask.action";
	/** 下载告警截图接口的URL */
	private static final String URL_DOWNLOAD_ALARM_PHOTO_DOMAIN = "/liftman/liftman/terminaAction!doNotNeedSession_TemergencyImage.action?username=%s&liftId=%s&faultImage=%s&page=%d";
	/** 下载告警录音接口的URL */
	private static final String URL_DOWNLOAD_ALARM_RECORDING_DOMAIN = "/liftman/liftman/terminaAction!doNotNeedSession_TemergencySound.action?username=%s&liftId=%s&faultImage=%s";
	/** 下载一个告警的详细信息的URL */
	private static final String URL_DOWNLOAD_ONE_ALARM_DETAILS_DOMAIN = "/liftman/liftman/appAction!doNotNeedSession_downLoadFault.action";
	/** 通告列表接口URL*/
	private static final String URL_DOWNLOAD_NOTICE_LIST = "/liftman/liftman/appAction!doNotNeedSession_downLoadTnotices.action";
	/** 通告模块中获取行政小区和小区信息URL */
	private static final String URL_DOWNLOAD_COMMUNITY_LIST = "/liftman/liftman/appAction!doNotNeedSession_areaAndGis.action";
	/** 通告模块中发布通告URL */
	private static final String URL_POST_NOTICE = "/liftman/liftman/appAction!doNotNeedSession_addTnotice.action";
	
	public static String host(int envirType) {
		String host = null;
		switch (envirType) {
		case HOST_ENVIRTYPE_TEST:
			host = "136.158.27.254:8086";
			break;
		case HOST_ENVIRTYPE_CLOUD:
			host = "www.zhiitek.com:8081";
			break;
		case HOST_ENVIRTYPE_MINE:
			host= "136.158.27.183:8080";
			break;
		default:
			break;
		}
		return host;
	}

	/** 登陆接口URL */
	public final static String loginUrl = String.format("%s%s%s", URL_PREFIX, host(HOST_ENVIRTYPE_CURRENT), URL_LOGIN_DOMAIN);
	
	/** 获取任务URL */
	public final static String getTaskUrl = String.format("%s%s%s", URL_PREFIX, host(HOST_ENVIRTYPE_CURRENT), URL_GET_TASK_DOMAIN);
	
	/**
	 * 更新版本URL
	 */
	public final static String updateVersionUrl = String.format("%s%s%s", URL_PREFIX, host(HOST_ENVIRTYPE_CURRENT), URL_UPDATE_VERSION_DOMAIN);
	
	/**
	 * 解决任务URL
	 */
	public final static String resolveTaskUrl = String.format("%s%s%s", URL_PREFIX, host(HOST_ENVIRTYPE_CURRENT), URL_RESOLVE_TASK_DOMAIN);
	
	/**
	 * 修改密码URL
	 */
	public final static String modifyPasswordUrl = String.format("%s%s%s", URL_PREFIX, host(HOST_ENVIRTYPE_CURRENT), URL_MODIFY_LOGIN_PASSWORD_DOMAIN);
	
	/**
	 * 重置密码时,获取验证码URL
	 */
	public static String getSecurityCodeUrl = String.format("%s%s%s", URL_PREFIX, host(HOST_ENVIRTYPE_CURRENT), URL_GET_SECURITY_CODE_DOMAIN);
	
	/**
	 * 更新电梯状态URL
	 */
	public static String updateLiftStatusUrl = String.format("%s%s%s", URL_PREFIX, host(HOST_ENVIRTYPE_CURRENT), URL_UPDATE_LIFT_STATUS_DOMAIN);
	
	/**
	 * 获取告警URL
	 */
	public static String getAlarmUrl = String.format("%s%s%s", URL_PREFIX, host(HOST_ENVIRTYPE_CURRENT), URL_GET_ALARM_DOMAIN);
	
	/**
	 * 获取任务和告警数量URL
	 */
	public static String getTaskAndAlarmCountUrl = String.format("%s%s%s", URL_PREFIX, host(HOST_ENVIRTYPE_CURRENT), URL_GET_TASK_AND_ALARM_COUNT_DOMAIN);
	
	/**
	 * 下载告警截图的URL
	 */
	public static String downloadAlarmPhotoUrl = String.format("%s%s%s", URL_PREFIX, host(HOST_ENVIRTYPE_CURRENT), URL_DOWNLOAD_ALARM_PHOTO_DOMAIN);

	/**
	 * 下载告警录音的URL
	 */
	public static String downloadAlarmRecordingUrl = String.format("%s%s%s", URL_PREFIX, host(HOST_ENVIRTYPE_CURRENT), URL_DOWNLOAD_ALARM_RECORDING_DOMAIN);
	
	/**
	 * 获取重启设备URL
	 * @param liftno
	 * @return
	 */
	public static String getRebootUrl(String liftno) {
		return String.format("http://www.zhiitek.com:8082/operation_liftman.do?action=rebootDevices&liftno=%s", liftno);
	}
	
	/**
	 * 下载一条告警的详细信息的URL
	 */
	public static String downloadOneAlarmDetailsUrl = String.format("%s%s%s", URL_PREFIX, host(HOST_ENVIRTYPE_CURRENT), URL_DOWNLOAD_ONE_ALARM_DETAILS_DOMAIN);
	
	/**
	 * 下载通告列表的URL
	 */
	public static String downloadNoticeListUrl = String.format("%s%s%s", URL_PREFIX, host(HOST_ENVIRTYPE_CURRENT), URL_DOWNLOAD_NOTICE_LIST);
	
	/**
	 * 下载行政小区和小区信息的URL
	 */
	public static String downloadCommunityUrl = String.format("%s%s%s", URL_PREFIX, host(HOST_ENVIRTYPE_CURRENT), URL_DOWNLOAD_COMMUNITY_LIST);
	
	/**
	 * 通告模块中发布通告URL
	 */
	public static String postNoticeUrl = String.format("%s%s%s", URL_PREFIX, host(HOST_ENVIRTYPE_CURRENT), URL_POST_NOTICE);
	//******************************************************************************************
}
