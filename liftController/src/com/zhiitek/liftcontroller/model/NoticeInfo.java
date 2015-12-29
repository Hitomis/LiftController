package com.zhiitek.liftcontroller.model;

import java.io.Serializable;

public class NoticeInfo implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;
	
	private String content;
	
	private String startTime;
	
	private String endTime;
	
	/** 1 - 有效，0 – 撤消，-2超时失效 */
	private String status;
	
	private String type;
	
	private String target;
	
	private String targetName;
	
	private String updateTime;
	
	private String sendUserID;
	
	private String sendUserName;
	
	private String cancelUserId;
	
	//0:有效 , 1:撤销, 2:超时
	/** 撤销 */
	public static final String STATUS_REVOCATION = "0";
	/** 有效 */
	public static final String STATUS_EFFECTIVE = "1";
	/** 超时 */
	public static final String STATUS_TIMEOUT = "2";
	
	//通告类型：0 - 片区通知（主管单位发），1 - 小区通知（小区物业发）
	/** 片区通知 */
	public static final String type_Area = "0";
	
	/** 小区通知 */
	public static final String type_Community = "1";

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getSendUserID() {
		return sendUserID;
	}

	public void setSendUserID(String sendUserID) {
		this.sendUserID = sendUserID;
	}

	public String getSendUserName() {
		return sendUserName;
	}

	public void setSendUserName(String sendUserName) {
		this.sendUserName = sendUserName;
	}

	public String getCancelUserId() {
		return cancelUserId;
	}

	public void setCancelUserId(String cancelUserId) {
		this.cancelUserId = cancelUserId;
	}
	
	public String displayStatusText() {
		String text;
		if (STATUS_EFFECTIVE.equals(status)) {
			text = "有效";
		} else if (STATUS_REVOCATION.equals(status)) {
			text = "撤销";
		} else {
			text = "超时";
		}
		return text;
	}
	
	public String displayStatusColor() {
		String color;
		if (STATUS_EFFECTIVE.equals(status)) {
			color = "#339933";
		} else if (STATUS_REVOCATION.equals(status)) {
			color = "#CCCC66";
		} else {
			color = "#FF6666";
		}
		return color;
	}
	
	public String displayTypeText() {
		String text;
		if (type_Area.equals(type)) {
			text = "片区通知";
		} else {
			text = "小区通知";
		}
		return text;
	}
	
}
