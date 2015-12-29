package com.zhiitek.liftcontroller.model;

public class DevicesInfo {

	private String liftNo;

	private String liftName;

	private String runningStatus;
	//运行，停止，检修，故障，报废
	public static final String STATUS_RUNNING = "1";
	public static final String STATUS_STOP = "2";
	public static final String STATUS_CHECK = "3";
	public static final String STATUS_FAULT = "4";
	public static final String STATUS_SCRAP= "5";

	public DevicesInfo(String liftNo, String liftName, String runningStatus) {
		super();
		this.liftNo = liftNo;
		this.liftName = liftName;
		this.runningStatus = runningStatus;
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

	public String getRunningStatus() {
		return displayFontByStatus();
	}

	public void setRunningStatus(String runningStatus) {
		this.runningStatus = runningStatus;
	}
	
	private String displayFontByStatus() {
		String text = null;
		if (DevicesInfo.STATUS_RUNNING.equals(runningStatus)) {
			text = "运行";
		} else if (DevicesInfo.STATUS_STOP.equals(runningStatus)) {
			text = "停止";
		} else if (DevicesInfo.STATUS_CHECK.equals(runningStatus)) {
			text = "检修";
		} else if (DevicesInfo.STATUS_FAULT.equals(runningStatus)) {
			text = "故障";
		} else if (DevicesInfo.STATUS_SCRAP.equals(runningStatus)) {
			text = "报废";
		}
		return text;
	}
	
	public static String getStatusByFont(String font) {
		String runningStatus = null;
		if ("运行".equals(font)) {
			runningStatus = STATUS_RUNNING;
		} else if ("停止".equals(font)) {
			runningStatus = STATUS_STOP;
		} else if ("检修".equals(font)) {
			runningStatus = STATUS_CHECK;
		} else if ("故障".equals(font)) {
			runningStatus = STATUS_FAULT;
		} else if ("报废".equals(font)) {
			runningStatus = STATUS_SCRAP;
		}
		return runningStatus;
	}

}
