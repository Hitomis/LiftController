package com.zhiitek.liftcontroller.model;

import java.io.Serializable;

public class InspectInfo implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//检验结果:1现场检验合格，2复检合格,3合格,4现场检验不合格,5复检不合格,6不合格
	public static final String FIELD_INSP_PASS = "1";
	public static final String FIELD_INSP_NOT_PASS = "4";
	public static final String REINSP_PASS = "2";
	public static final String REINSP_NOT_PASS = "5";
	public static final String INSP_PASS = "3";
	public static final String INSP_NOT_PASS = "6";
	//下次检验日期
	private String nextInspectDate;
	//上次检验结果
	private String status;
	//检验类型
	private String inspType;
	
	/**
	 * 年度检查
	 */
	public static final String ANNUAL_INSPECTION = "1";
	/**
	 * 监督检查
	 */
	public static final String SUPERVISION_INSPECTION = "2";
	/**
	 * 巡检
	 */
	public static final String SITE_INSPECTION = "3";
	
	public InspectInfo() {
		
	}

	public InspectInfo(String nextInspectDate, String status, String inspType) {
		this.nextInspectDate = nextInspectDate;
		this.status = status;
		this.inspType = inspType;
	}

	public String getNextInspectDate() {
		return nextInspectDate;
	}
	public void setNextInspectDate(String nextInspectDate) {
		this.nextInspectDate = nextInspectDate;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getInspType() {
		return inspType;
	}
	public void setInspType(String inspType) {
		this.inspType = inspType;
	}
}
