package com.zhuoyou.plugin.running;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import com.zhuoyou.plugin.database.DataBaseContants;

public class MotionDataCenter {

	private Context mContext;
	private List<String> steps;
	private List<String> calories;
	private List<String> kilometers;
	private PersonalConfig mPersonalConfig;
	private PersonalGoal mPersonalGoal;
	private int caloriesAddSport;
	
	public MotionDataCenter(Context context) {
		mContext = context;
		steps = new ArrayList<String>();
		calories = new ArrayList<String>();
		kilometers = new ArrayList<String>();
		mPersonalConfig = Tools.getPersonalConfig();
		mPersonalGoal = Tools.getPersonalGoal();
		String enter_day = Tools.getFirstEnterDay(mContext);
		String today = Tools.getDate(0);
		int count = Tools.getDayCount(enter_day, today);
		ContentResolver cr = mContext.getContentResolver();
		for (int i = 0; i < count; i++) {
			String day = Tools.getDate(enter_day, 0 - i);
			initAddSport(day);
			Cursor c = cr.query(DataBaseContants.CONTENT_URI, new String[] { "_id", "steps", "calories", "kilometer" }, DataBaseContants.DATE + "  = ? AND " + DataBaseContants.STATISTICS + " = ? ", 
					new String[] { day, "1" }, null);
			c.moveToFirst();
			if (c.getCount() > 0 && c.moveToFirst() && c.getLong(c.getColumnIndex(DataBaseContants.ID)) > 0) {
				int step = c.getInt(c.getColumnIndex(DataBaseContants.STEPS));
				steps.add(String.valueOf(step));
				int cal = c.getInt(c.getColumnIndex(DataBaseContants.CALORIES)) + caloriesAddSport;
				calories.add(String.valueOf(cal));
				int kilometer = c.getInt(c.getColumnIndex(DataBaseContants.KILOMETER));
				kilometers.add(String.valueOf(kilometer));
			} else {
				steps.add(String.valueOf(0));
				calories.add(String.valueOf(caloriesAddSport));
				kilometers.add(String.valueOf(0));
			}
			c.close();
			c = null;
		}
	}
	
	void initAddSport(String day) {
		caloriesAddSport = 0;
		int calories = 0;
		ContentResolver cr = mContext.getContentResolver();
		Cursor cAddSport = cr.query(DataBaseContants.CONTENT_URI, new String[] { "_id", "calories" , "sports_type" , "type"},  DataBaseContants.DATE + "  = ? AND " + DataBaseContants.STATISTICS + " = ?",
				new String[] { day , "0" }, null);
		cAddSport.moveToFirst();
		if(cAddSport.getCount() > 0 ){
			for(int y=0;y<cAddSport.getCount();y++){
				if(cAddSport.getInt(cAddSport.getColumnIndex(DataBaseContants.TYPE))==2){	
					if(cAddSport.getInt(cAddSport.getColumnIndex(DataBaseContants.SPORTS_TYPE))!=0){
						calories=calories+cAddSport.getInt(cAddSport.getColumnIndex(DataBaseContants.CALORIES));
						caloriesAddSport=calories;
					}
				}
				cAddSport.moveToNext();
			}
		}
		cAddSport.close();
		cAddSport=null;
	}

	public int getBestSteps() {
		int best_value = 0;
		if (steps != null && steps.size() > 0) {
			for (int i = 0; i < steps.size(); i++) {
				int step = Integer.parseInt(steps.get(i));
				if (step > best_value)
					best_value = step;
			}
		}
		return best_value;
	}
	
	public int getBestCalories() {
		int best_value = 0;
		if (calories != null && calories.size() > 0) {
			for (int i = 0; i < calories.size(); i++) {
				int cal = Integer.parseInt(calories.get(i));
				if (cal > best_value)
					best_value = cal;
			}
		}
		return best_value;
	}
	
	public int getAvgSteps() {
		int avg_value = 0;
		int total = 0;
		if (steps != null && steps.size() > 0) {
			for (int i = 0; i < steps.size(); i++) {
				int step = Integer.parseInt(steps.get(i));
				total = total + step;
			}
			avg_value = total / steps.size();
		}
		return avg_value;
	}
	
	public int getAvgCalories() {
		int avg_value = 0;
		int total = 0;
		if (calories != null && calories.size() > 0) {
			for (int i = 0; i < calories.size(); i++) {
				int cal = Integer.parseInt(calories.get(i));
				total = total + cal;
			}
			avg_value = total / calories.size();
		}
		return avg_value;
	}

	public double getTotalKM() {
		double d = 0.0D;
		int total = 0;
		if (kilometers != null && kilometers.size() > 0) {
			for (int i = 0; i < kilometers.size(); i++) {
				int meter = Integer.parseInt(kilometers.get(i));
				total = total + meter;
			}
			d = (double)total / 1000D;
		}
		return d;
	}
	
	public int getTotalCalories() {
		int total = 0;
		if (calories != null && calories.size() > 0) {
			for (int i = 0; i < calories.size(); i++) {
				int cal = Integer.parseInt(calories.get(i));
				total = total + cal;
			}
		}
		return total;
	}
	
	public String getGoal() {
		int day = 0;
		String percent = "0%";
		int goalcals = mPersonalGoal.mGoalCalories;
		if (calories != null && calories.size() > 0) {
			for (int i = 0; i < calories.size(); i++) {
				int cal = Integer.parseInt(calories.get(i));
				if (cal >= goalcals) {
					day++;
				}
			}
			double d = (double)day / (double)steps.size();
			NumberFormat numberformat = NumberFormat.getPercentInstance();
			numberformat.setMinimumFractionDigits(1);
			percent = numberformat.format(d);
		}		
		return String.valueOf(day) + "," + percent;
	}
	
	public double getBMI() {
		double bmi = 0.0D;
		int weight = mPersonalConfig.getWeight();
		int height = mPersonalConfig.getHeight();
		bmi = (double) (weight * 10000D) / (height * height);
		return bmi;
	}
	
	public int getBMR() {
		double bmr = 0.0D;
		int weight = mPersonalConfig.getWeight();
		int height = mPersonalConfig.getHeight();
		int age = Calendar.getInstance().get(Calendar.YEAR) - mPersonalConfig.getYear();
		if (mPersonalConfig.getSex() == PersonalConfig.SEX_MAN) {
			bmr = (13.7 * weight) + (5.0 * height) - (6.8 * age) + 66;
		} else if (mPersonalConfig.getSex() == PersonalConfig.SEX_WOMAN) {
			bmr = (9.6 * weight) + (1.8 * height) - (4.7 * age) + 655;
		}
		return (int) bmr;
	}
}
