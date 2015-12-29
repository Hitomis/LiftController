package com.zhiitek.liftcontroller.model;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class FaultInfo implements Parcelable, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String faultNo;
	
	private String faultId;
	
	private String faultData;
	
	private String faultTime;

	public FaultInfo() {
	}

	public FaultInfo(String faultNo, String faultId, String faultData, String faultTime) {
		this.faultNo = faultNo;
		this.faultId = faultId;
		this.faultData = faultData;
		this.faultTime = faultTime;
	}

	public String getFaultNo() {
		return faultNo;
	}

	public void setFaultNo(String faultNo) {
		this.faultNo = faultNo;
	}

	public String getFaultData() {
		return faultData;
	}

	public void setFaultData(String faulData) {
		this.faultData = faulData;
	}

	public String getFaultTime() {
		return faultTime;
	}

	public void setFaultTime(String faultTime) {
		this.faultTime = faultTime;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<FaultInfo> CREATOR = new Creator<FaultInfo>() {

		@Override
		public FaultInfo createFromParcel(Parcel source) {
			return new FaultInfo(source.readString(), source.readString(), source.readString(), source.readString());
		}

		@Override
		public FaultInfo[] newArray(int size) {
			return new FaultInfo[size];
		}
		
    }; 

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(faultNo);
		dest.writeString(faultData);
		dest.writeString(faultTime);
	}

	public String getFaultId() {
		return faultId;
	}

	public void setFaultId(String faultId) {
		this.faultId = faultId;
	}
}
