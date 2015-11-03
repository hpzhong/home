package com.zhuoyou.plugin.running;

import java.util.Calendar;

public class PersonalConfig {
	public static final int SEX_MAN = 0;
	public static final int SEX_WOMAN = 1;

	public float id;

	/**
	 * @sex @ man = 1 @ woman = 2;
	 */
	private int mSex;

	/**
	 * @weight @ kg
	 */
	private int mWeight;

	/**
	 * @height @ m
	 */
	private int mHeight;

	/**
	 * @year
	 */
	private int mYear;

	public PersonalConfig() {

	}

	public PersonalConfig(int sex, int w, int h, int year) {
		mSex = sex;
		mWeight = w;
		mHeight = h;
		mYear = year;
	}

	public int getSex() {
		return mSex;
	}

	public void setSex(int Sex) {
		this.mSex = Sex;
	}

	public int getWeight() {
		return mWeight;
	}

	public void setWeight(int Weight) {
		this.mWeight = Weight;
	}

	public int getHeight() {
		return mHeight;
	}

	public void setHeight(int Height) {
		this.mHeight = Height;
	}

	public int getYear() {
		return mYear;
	}

	public void setYear(int Year) {
		this.mYear = Year;
	}

	public boolean isEquals(PersonalConfig config) {
		return (mSex == config.mSex) && (mWeight == config.mWeight) && (mHeight == config.mHeight) && (mYear == config.mYear);
	}

	public String toString(){
		int sex = 0;
		if(mSex == SEX_MAN){
			sex =1 ;
		}else{
			sex = 0;
		}
		int age = Calendar.getInstance().get(Calendar.YEAR) - mYear;
		return sex + "|" + mHeight + "|" + mWeight + "|" + age + "|";
	}
}
