package com.zhiitek.liftcontroller.model;

import android.os.Parcel;
import android.os.Parcelable;

public class LiftInfo implements Parcelable{
	
	private String devSerial;
	
	private String liftNo;
	
	private String checkCode;
	
	public LiftInfo() {
		super();
	}

	public LiftInfo(String devSerial, String liftNo, String checkCode) {
		super();
		this.devSerial = devSerial;
		this.liftNo = liftNo;
		this.checkCode = checkCode;
	}

	public String getDevSerial() {
		return devSerial;
	}

	public void setDevSerial(String devSerial) {
		this.devSerial = devSerial;
	}

	public String getLiftNo() {
		return liftNo;
	}

	public void setLiftNo(String liftNo) {
		this.liftNo = liftNo;
	}

	public String getCheckCode() {
		return checkCode;
	}

	public void setCheckCode(String checkCode) {
		this.checkCode = checkCode;
	}

	@Override
	public int describeContents() {
		return 0;
	}
	
    public static final Parcelable.Creator<LiftInfo> CREATOR = new Creator<LiftInfo>() {

		@Override
		public LiftInfo createFromParcel(Parcel source) {
			return new LiftInfo(source.readString(), source.readString(), source.readString());
		}

		@Override
		public LiftInfo[] newArray(int size) {
			return new LiftInfo[size];
		}
		
    }; 

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(devSerial);
		dest.writeString(liftNo);
		dest.writeString(checkCode);
	}
	
}
