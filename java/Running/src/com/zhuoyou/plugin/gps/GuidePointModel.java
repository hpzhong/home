package com.zhuoyou.plugin.gps;

import java.io.Serializable;

public class GuidePointModel implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4710693202712413234L;
	/**
	 * 
	 */
	private long guideId;//数据库ID
	private double latitude;//经度
	private double longitude;//纬度
	private String address;//用户的详细地址
	private float accuracy;//定位精度
	private String provider;//定位提供者，判断是网络定位还是Gps定位
	private long time;//取得定位点的详细时间
	private float speed;//获取速度（只有Gps定位时才会有此速度）
	private double altitude;//获取定位的海拔
	private int gpsStatus;//获得Gps卫星数量  次数据暂时无用
	private long sysTime;//获取系统时间
	private int pointState;//判断当前点的状态，每次点击操作之后第一条数据为状态值，其余点为0
	private int syncState; //云同步的处理

	public GuidePointModel(){
		
	}
	
	public GuidePointModel(double latitude, double longitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public float getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public int getGpsStatus() {
		return gpsStatus;
	}

	public void setGpsStatus(int gpsStatus) {
		this.gpsStatus = gpsStatus;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public long getSysTime() {
		return sysTime;
	}

	public void setSysTime(long sysTime) {
		this.sysTime = sysTime;
	}

	public int getPointState() {
		return pointState;
	}

	public void setPointState(int pointState) {
		this.pointState = pointState;
	}

	public long getGuideId() {
		return guideId;
	}

	public void setGuideId(long guideId) {
		this.guideId = guideId;
	}

	public int getSyncState() {
		return syncState;
	}

	public void setSyncState(int syncState) {
		this.syncState = syncState;
	}
}
