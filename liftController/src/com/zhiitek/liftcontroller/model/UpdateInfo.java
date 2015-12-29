package com.zhiitek.liftcontroller.model;


public class UpdateInfo {
	
	private String version;
	
	private String apkUrl;
	
	private String description;
	
	private String fileName;

	public UpdateInfo() {
		super();
	}

	public UpdateInfo(String version, String apkUrl, String description) {
		super();
		this.version = version;
		this.apkUrl = apkUrl;
		this.description = description;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getApkUrl() {
		return apkUrl;
	}

	public void setApkUrl(String apkUrl) {
		this.apkUrl = apkUrl;
		String[] strs = apkUrl.split("/");
		setFileName(strs[strs.length - 1]);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
