package com.zhuoyou.plugin.running;

import java.math.BigDecimal;

import android.content.ContentValues;

import com.zhuoyou.plugin.database.DataBaseContants;

public class RunningItem {
	private long id = 0;
	private String mDate = "";
	private String mDuration = "";
	private String mStartTime;
	private String mEndTime;
	private int mPm25 = 0;
	private int mSteps = 0;
	private int mKilometer = 0;
	private int mCalories = 0;
	private String mWeight;
	private String mBmi;
	private String mImgUri;
	private String mExplain;
	private int mSportsType;
	private int mType;
	private String img_cloud;
	private int isComplete;
	private boolean isStatistics = false;

	public RunningItem() {

	}

	public RunningItem(long id, String date, String duration, String startTime,
			String endTime, int steps, int kilometer, int calories,
			String weight, String bmi, String img_uri, String explain,
			int sportsType, int type, String img_cloud, int complete,
			boolean statistics) {
		this.id=id;
		this.mDate=date;
		this.mDuration=duration;
		this.mStartTime=startTime;
		this.mEndTime=endTime;
		this.mSteps=steps;
		this.mKilometer=kilometer;
		this.mCalories=calories;
		this.mWeight=weight;
		this.mBmi=bmi;
		this.mImgUri=img_uri;
		this.mExplain=explain;
		this.mSportsType=sportsType;
		this.mType=type;
		this.img_cloud=img_cloud;
		this.isComplete=complete;
		this.isStatistics=statistics;
	}

	public long getID() {
		return id;
	}

	public String getDate() {
		return mDate;
	}

	public String getDuration() {
		if (mDuration == null) {
			mDuration = "";
		}

		return mDuration;
	}

	public String getStartTime() {
		return mStartTime;
	}

	public String getEndTime() {
		return mEndTime;
	}

	public int getPm25() {
		return mPm25;
	}

	public int getSteps() {
		return mSteps;
	}

	public float getKilometer() {
		float f = mKilometer;
		f /= 1000;
		int scale = 2;
		int roundingMode = 4;
		BigDecimal bd = new BigDecimal((double) f);
		bd = bd.setScale(scale, roundingMode);
		f = bd.floatValue();
		return f;
	}

	public int getMeter() {
		return mKilometer;
	}

	public int getCalories() {
		return mCalories;
	}

	public void setID(long id) {
		this.id = id;
	}

	public void setDate(String mDate) {
		this.mDate = mDate;
	}

	public void setStartTime(String mStartTime) {
		this.mStartTime = mStartTime;
	}

	public void setEndTime(String mEndTime) {
		this.mEndTime = mEndTime;
	}

	public void setPm25(int mPm25) {
		this.mPm25 = mPm25;
	}

	public void setSteps(int mSteps) {
		this.mSteps = mSteps;
	}

	public void setKilometer(int mKilometer) {
		this.mKilometer = mKilometer;
	}

	public void setCalories(int mCalories) {
		this.mCalories = mCalories;
	}

	public void setDuration(String mDuration) {
		this.mDuration = mDuration;
	}

	public void setisComplete(int is) {
		isComplete = is;
	}

	public void setisStatistics(boolean is) {
		isStatistics = is;
	}

	public int getIsComplete() {
		return isComplete;
	}

	public boolean getIsStatistics() {
		return isStatistics;
	}

	public String getmWeight() {
		return mWeight;
	}

	public void setmWeight(String mWeight) {
		this.mWeight = mWeight;
	}

	public String getmBmi() {
		return mBmi;
	}

	public void setmBmi(String mBmi) {
		this.mBmi = mBmi;
	}

	public String getmImgUri() {
		return mImgUri;
	}

	public void setmImgUri(String mImgUri) {
		this.mImgUri = mImgUri;
	}

	public String getmExplain() {
		return mExplain;
	}

	public void setmExplain(String mExplain) {
		this.mExplain = mExplain;
	}

	public int getSportsType() {
		return mSportsType;
	}

	public void setSportsType(int sportsType) {
		this.mSportsType = sportsType;
	}

	public int getmType() {
		return mType;
	}

	public void setmType(int mType) {
		this.mType = mType;
	}

	public String getImg_cloud() {
		return img_cloud;
	}

	public void setImg_cloud(String img_cloud) {
		this.img_cloud = img_cloud;
	}

	public ContentValues toContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(DataBaseContants.DATE, mDate);
		cv.put(DataBaseContants.TIME_DURATION, mDuration);
		cv.put(DataBaseContants.TIME_START, mStartTime);
		cv.put(DataBaseContants.TIME_END, mEndTime);
		cv.put(DataBaseContants.STEPS, mSteps);
		cv.put(DataBaseContants.KILOMETER, mKilometer);
		cv.put(DataBaseContants.CALORIES, mCalories);
		cv.put(DataBaseContants.SPORTS_TYPE, mSportsType);
		cv.put(DataBaseContants.TYPE, mType);
		cv.put(DataBaseContants.COMPLETE, isComplete);
		cv.put(DataBaseContants.STATISTICS, isStatistics);
		return cv;
	}
	
	public String toString(){
		String ret = "";
		
		ret += "mSteps = " ;
		ret += mSteps;
		ret += " | ";
		
		ret += "mDuration = " ;
		ret += mDuration;
		ret += " | ";
		
		ret += "mStartTime = " ;
		ret += mStartTime;
		ret += " | ";
		
		ret += "mEndTime = " ;
		ret += mEndTime;
		ret += " | ";
		
		ret += "mPm25 = " ;
		ret += mPm25;
		ret += " | ";
		
		ret += "mSteps = " ;
		ret += mSteps;
		ret += " | ";
		
		ret += "mKilometer = " ;
		ret += mKilometer;
		ret += " | ";
		
		ret += "mCalories = " ;
		ret += mCalories;
		ret += " | ";
		
		ret += "isStatistics = ";
		ret += isStatistics;
		ret += " | ";
		
		return ret;
	}
}
