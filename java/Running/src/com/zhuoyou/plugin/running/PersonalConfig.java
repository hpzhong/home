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
	private String mWeight;

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

	public PersonalConfig(int sex, String w, int h, int year) {
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

	public String getWeight() {
		return mWeight;
	}

	public float getWeightNum() {
		return Float.valueOf(mWeight);
	}
	
	public void setWeight(String Weight) {
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
		return (mSex == config.mSex) && (mWeight.split("\\.")[0].equals(config.mWeight.split("\\.")[0])) && (mHeight == config.mHeight) && (mYear == config.mYear);
	}

	public String toString(){
		int sex = 0;
		if(mSex == SEX_MAN){
			sex =1 ;
		}else{
			sex = 0;
		}
		int age = Calendar.getInstance().get(Calendar.YEAR) - mYear;
		return sex + "|" + mHeight + "|" + mWeight.split("\\.")[0] + "|" + age + "|";
	}
}
