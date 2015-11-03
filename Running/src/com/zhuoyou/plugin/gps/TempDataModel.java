package com.zhuoyou.plugin.gps;

/**
 * 该类是存储倒计时开始时的一些临时数据
 * 注释：所有数据均是存储在data表中的
 */
public class TempDataModel {

	private long tempId;   //该条记录ID
	private String tempDate;   //运动时间开始的日期
	private String tempStaTime;  //运动的开始时间
	private String tempDuration;	//运动的持续时间
	private String tempEndTime;		//运动的结束时间
	private int tempCalories;		//运动消耗的卡路里
	private int tempStep;			//运动的步数
	private double tempDistance;		//运动的距离
	private int tempType;			//运动类型
	private int tempStatistics;		//未知，data表中需要的数据
	private int tempState;			//数据状态,判断云同步用
	private String tempStaAddress;	//运动开始的地理位置
	private String tempEndAddress;	//运动结束的地理位置
	private String tempImageUrl;	//存储GPS截图的路径
	private long tempGpsId;		//GPS存储的ID
	
	public long getTempId() {
		return tempId;
	}
	
	public void setTempId(long tempId) {
		this.tempId = tempId;
	}
	
	public String getTempDate() {
		return tempDate;
	}
	
	public void setTempDate(String tempDate) {
		this.tempDate = tempDate;
	}
	
	public String getTempStaTime() {
		return tempStaTime;
	}
	
	public void setTempStaTime(String tempStaTime) {
		this.tempStaTime = tempStaTime;
	}
	
	public String getTempDuration() {
		return tempDuration;
	}
	
	public void setTempDuration(String tempDuration) {
		this.tempDuration = tempDuration;
	}
	
	public String getTempEndTime() {
		return tempEndTime;
	}
	
	public void setTempEndTime(String tempEndTime) {
		this.tempEndTime = tempEndTime;
	}
	
	public int getTempCalories() {
		return tempCalories;
	}
	
	public void setTempCalories(int tempCalories) {
		this.tempCalories = tempCalories;
	}
	
	public int getTempStep() {
		return tempStep;
	}
	
	public void setTempStep(int tempStep) {
		this.tempStep = tempStep;
	}
	
	public double getTempDistance() {
		return tempDistance;
	}
	
	public void setTempDistance(double tempDistance) {
		this.tempDistance = tempDistance;
	}
	
	public int getTempType() {
		return tempType;
	}
	
	public void setTempType(int tempType) {
		this.tempType = tempType;
	}
	
	public int getTempStatistics() {
		return tempStatistics;
	}
	
	public void setTempStatistics(int tempStatistics) {
		this.tempStatistics = tempStatistics;
	}
	
	public int getTempState() {
		return tempState;
	}
	
	public void setTempState(int tempState) {
		this.tempState = tempState;
	}
	
	public String getTempStaAddress() {
		return tempStaAddress;
	}
	
	public void setTempStaAddress(String tempStaAddress) {
		this.tempStaAddress = tempStaAddress;
	}
	
	public String getTempEndAddress() {
		return tempEndAddress;
	}
	
	public void setTempEndAddress(String tempEndAddress) {
		this.tempEndAddress = tempEndAddress;
	}
	
	public String getTempImageUrl() {
		return tempImageUrl;
	}
	
	public void setTempImageUrl(String tempImageUrl) {
		this.tempImageUrl = tempImageUrl;
	}
	
	public long getTempGpsId() {
		return tempGpsId;
	}
	
	public void setTempGpsId(long tempGpsId) {
		this.tempGpsId = tempGpsId;
	}
	
	public void clearData(){
		tempId=0;
		tempDate="";
		tempStaTime="";
		tempDuration="";
		tempEndTime="";
		tempCalories=0;
		tempStep=0;
		tempDistance=0;
		tempType=0;
		tempStatistics=0;
		tempState=0;
		tempStaAddress="";
		tempEndAddress="";
		tempImageUrl="";
		tempGpsId=0;
	}
	
}
