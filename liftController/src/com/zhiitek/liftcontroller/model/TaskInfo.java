package com.zhiitek.liftcontroller.model;

import java.io.Serializable;
import java.util.ArrayList;

public class TaskInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String taskID;
	private String taskName;
	private String taskType;
	private String liftNo;
	private String address;
	private String taskTime;
	private String taskMemo;
	private ArrayList<FaultInfo> faultList;
	private InspectInfo inspect;
	
	/**
	 * 年检任务
	 */
	public static final String BAK_INSPECT_TASK_TYPE = "0";
	/**
	 * 告警任务
	 */
	public static final String FAULT_TASK_TYPE = "1";
	/**
	 * 巡检任务
	 */
	public static final String SITE_INSPECT_TASK_TYPE = "2";
	/**
	 * 监督检查
	 */
	public static final String REPAIR_TASK_TYPE_STRING = "3";
	
	public TaskInfo() {
		
	}

	public TaskInfo(String taskID, String taskName, String taskType, String liftNo, String address, String taskTime, String taskMemo) {
		this.taskID = taskID;
		this.taskName = taskName;
		this.taskType = taskType;
		this.liftNo = liftNo;
		this.address = address;
		this.taskTime = taskTime;
		this.taskMemo = taskMemo;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getTaskTime() {
		return taskTime;
	}

	public void setTaskTime(String taskTime) {
		this.taskTime = taskTime;
	}

	public String getTaskMemo() {
		return taskMemo;
	}

	public void setTaskMemo(String taskMemo) {
		this.taskMemo = taskMemo;
	}

	public ArrayList<FaultInfo> getTaskFault() {
		return faultList;
	}

	public void setTaskFault(ArrayList<FaultInfo> faultList) {
		this.faultList = faultList;
	}

	public String getTaskID() {
		return taskID;
	}

	public void setTaskID(String taskID) {
		this.taskID = taskID;
	}

	public String getTaskType() {
		return taskType;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}

	public String getLiftNo() {
		return liftNo;
	}

	public void setLiftNo(String liftNo) {
		this.liftNo = liftNo;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public InspectInfo getInspect() {
		return inspect;
	}

	public void setInspect(InspectInfo inspect) {
		this.inspect = inspect;
	}
}
