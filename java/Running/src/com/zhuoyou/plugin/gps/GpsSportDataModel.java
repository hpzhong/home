package com.zhuoyou.plugin.gps;

import java.io.Serializable;

public class GpsSportDataModel implements Serializable {

	private static final long serialVersionUID = 7172694132214059534L;
	
	private long starttime;			//取得Gps运动的开始时间
	private long endtime;			//Gps运动的结束时间
	private long starSysttime;		//取得Gps运动的开始时间(系统时间)
	private long endSystime;		//Gps运动的结束时间(系统时间)
	private long durationtime;		//Gps运动的持续时间
	private double avespeed;		//整个运动的平均速度
	private double totalDistance;	//整个运动的总距离
	private int steps;				//整个运动过程运动步数
	private double calorie;			//整个运动过程中小号的卡路里
	private long gpsId;				//插入数据时的ID
	private String startAddress;	//插入数据时的开始位置
	private String endAddress;		//插入数据时的结束位置
	private int syncState;          //云同步状态

	public GpsSportDataModel(){
		
	}

	
	public long getGpsId() {
		return gpsId;
	}


	public void setGpsId(long gpsId) {
		this.gpsId = gpsId;
	}


	public long getStarttime() {
		return starttime;
	}

	public void setStarttime(long starttime) {
		this.starttime = starttime;
	}

	public long getEndtime() {
		return endtime;
	}

	public void setEndtime(long endtime) {
		this.endtime = endtime;
	}

	public long getDurationtime() {
		return durationtime;
	}

	public void setDurationtime(long durationtime) {
		this.durationtime = durationtime;
	}

	public double getAvespeed() {
		return avespeed;
	}

	public void setAvespeed(double avespeed) {
		this.avespeed = avespeed;
	}

	public double getTotalDistance() {
		return totalDistance;
	}

	public void setTotalDistance(double totalDistance) {
		this.totalDistance = totalDistance;
	}

	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	public double getCalorie() {
		return calorie;
	}

	public void setCalorie(double calorie) {
		this.calorie = calorie;
	}

	public long getStarSysttime() {
		return starSysttime;
	}

	public void setStarSysttime(long starSysttime) {
		this.starSysttime = starSysttime;
	}

	public long getEndSystime() {
		return endSystime;
	}

	public void setEndSystime(long endSystime) {
		this.endSystime = endSystime;
	}


	public String getStartAddress() {
		return startAddress;
	}


	public void setStartAddress(String startAddress) {
		this.startAddress = startAddress;
	}


	public String getEndAddress() {
		return endAddress;
	}


	public void setEndAddress(String endAddress) {
		this.endAddress = endAddress;
	}
	
	public int getSyncState() {
		return syncState;
	}


	public void setSyncState(int syncState) {
		this.syncState = syncState;
	}


	public void clearData(){
		starttime=0;
		endtime=0;			
		starSysttime=0;		
		endSystime=0;		
		durationtime=0;		
		avespeed=0;		
		totalDistance=0;	
		steps=0;				
		calorie=0;			
		gpsId=0;				
		startAddress="";	
		endAddress="";	
		syncState=0;
	}
	
}
