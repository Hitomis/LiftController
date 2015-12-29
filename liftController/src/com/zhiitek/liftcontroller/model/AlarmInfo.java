package com.zhiitek.liftcontroller.model;

import java.io.Serializable;

public class AlarmInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String liftNo;
	
	private String liftName;
	
	private String liftCommunity;
	
	private String liftAddress;
	
	private String alarmLevel;
	
	private String alarmName;
	
	private String alarmCode;
	
	private String alarmTime;
	
	private String alarmData;
	
	private String alarmPhoto;
	
	private String alarmCount;
	
	public AlarmInfo(String liftNo, String liftName, String liftCommunity, String liftAddress, 
			String alarmLevel, String alarmName, String alarmCode, String alarmTime, String alarmData, String alarmPhoto,
			String alarmCount) {
		super();
		this.liftNo = liftNo;
		this.liftName = liftName;
		this.liftCommunity = liftCommunity;
		this.liftAddress = liftAddress;
		this.alarmLevel = alarmLevel;
		this.alarmName = alarmName;
		this.alarmCode = alarmCode;
		this.alarmTime = alarmTime;
		this.alarmData = alarmData;
		this.alarmPhoto = alarmPhoto;
		this.alarmCount = alarmCount;
	}
	
	public AlarmInfo() {
		super();
	}



	public String getLiftNo() {
		return liftNo;
	}

	public void setLiftNo(String liftNo) {
		this.liftNo = liftNo;
	}

	public String getLiftName() {
		return liftName;
	}

	public void setLiftName(String liftName) {
		this.liftName = liftName;
	}

	public String getLiftCommunity() {
		return liftCommunity;
	}

	public void setLiftCommunity(String liftCommunity) {
		this.liftCommunity = liftCommunity;
	}

	public String getLiftAddress() {
		return liftAddress;
	}

	public void setLiftAddress(String liftAddress) {
		this.liftAddress = liftAddress;
	}

	public String getAlarmLevel() {
		return alarmLevel;
	}

	public void setAlarmLevel(String alarmLevel) {
		this.alarmLevel = alarmLevel;
	}

	public String getAlarmCode() {
		return alarmCode;
	}

	public void setAlarmCode(String alarmCode) {
		this.alarmCode = alarmCode;
	}

	public String getAlarmTime() {
		return alarmTime;
	}

	public void setAlarmTime(String alarmTime) {
		this.alarmTime = alarmTime;
	}

	public String getAlarmName() {
		return alarmName;
	}

	public void setAlarmName(String alarmName) {
		this.alarmName = alarmName;
	}

	public String getAlarmData() {
		return alarmData;
	}

	public void setAlarmData(String alarmData) {
		this.alarmData = alarmData;
	}

	public String getAlarmPhoto() {
		return alarmPhoto;
	}

	public void setAlarmPhoto(String alarmPhoto) {
		this.alarmPhoto = alarmPhoto;
	}

	public String getAlarmCount() {
		return alarmCount;
	}

	public void setAlarmCount(String alarmCount) {
		this.alarmCount = alarmCount;
	}
	
}
