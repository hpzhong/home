package com.zhuoyou.plugin.running;

public class StatsItem {

	private String mDate = "";
	private int mSteps = 0;
	private int mCalories = 0;
	private int mMeter = 0;

	public String getDate() {
		return mDate;
	}
	
	public void setDate(String mDate) {
		this.mDate = mDate;
	}

	public int getSteps() {
		return mSteps;
	}

	public void setSteps(int mSteps) {
		this.mSteps = mSteps;
	}

	public int getCalories() {
		return mCalories;
	}

	public void setCalories(int mCalories) {
		this.mCalories = mCalories;
	}

	public int getMeter() {
		return mMeter;
	}

	public void setMeter(int mMeter) {
		this.mMeter = mMeter;
	}

}
