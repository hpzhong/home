package com.zhuoyou.plugin.running;

import java.io.Serializable;

public class SedentaryDeviceItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String DeviceName;
	public String StartTime;
	public String EndTime;
	public int TimeLag;
	public Boolean State;
	public Boolean isSync;
	public SedentaryDeviceItem(String DeviceName,String StartTime,String EndTime,int TimeLag,Boolean State){
		this.DeviceName=DeviceName;
		this.StartTime=StartTime;
		this.EndTime=EndTime;
		this.TimeLag=TimeLag;
		this.State=State;
		
	}
	public SedentaryDeviceItem(){
		
	}
	public String getDeviceName() {
		return DeviceName;
	}
	public void setDeviceName(String deviceName) {
		DeviceName = deviceName;
	}
	public String getStartTime() {
		return StartTime;
	}
	public void setStartTime(String startTime) {
		StartTime = startTime;
	}
	public String getEndTime() {
		return EndTime;
	}
	public void setEndTime(String endTime) {
		EndTime = endTime;
	}
	public int getTimeLag() {
		return TimeLag;
	}
	public void setTimeLag(int timeLag) {
		TimeLag = timeLag;
	}
	public Boolean getState() {
		return State;
	}
	public void setState(Boolean state) {
		State = state;
	}
	public Boolean getIsSync() {
		return isSync;
	}
	public void setIsSync(Boolean isSync) {
		this.isSync = isSync;
	}
	
	@Override
	public String toString() {
		return StartTime+"|"+EndTime+"|"+TimeLag+"|"+State;
	}

	
}
