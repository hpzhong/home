package com.zhuoyou.plugin.running;

import java.util.ArrayList;
import java.util.List;

import com.zhuoyou.plugin.database.DataBaseContants;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

public class DataStatsCenter {

	private Context mContext;
	private List<StatsItem> dailyStatses;
	private List<StatsItem> weeklyStatses;
	private List<StatsItem> monthlyStatses;
	private int caloriesAddSport;

	public DataStatsCenter(Context context) {
		mContext = context;
		dailyStatses = new ArrayList<StatsItem>();		
		String enter_day = Tools.getFirstEnterDay(mContext);
		String today = Tools.getDate(0);
		int count = Tools.getDayCount(enter_day, today);
		ContentResolver cr = mContext.getContentResolver();
		for (int i = 0; i < count; i++) {
			String day = Tools.getDate(enter_day, 0 - i);
			initAddSport(day);
			StatsItem statsdate = new StatsItem();
			Cursor c = cr.query(DataBaseContants.CONTENT_URI, new String[] { "_id", "steps", "calories", "kilometer" }, DataBaseContants.DATE + "  = ? AND " + DataBaseContants.STATISTICS + " = ? ", 
					new String[] { day, "1" }, null);
			c.moveToFirst();
			if (c.getCount() > 0 && c.moveToFirst() && c.getLong(c.getColumnIndex(DataBaseContants.ID)) > 0) {
				statsdate.setDate(c.getString(c.getColumnIndex(DataBaseContants.DATE)));
				statsdate.setCalories(c.getInt(c.getColumnIndex(DataBaseContants.CALORIES))+caloriesAddSport);
				statsdate.setSteps(c.getInt(c.getColumnIndex(DataBaseContants.STEPS)));
				statsdate.setMeter(c.getInt(c.getColumnIndex(DataBaseContants.KILOMETER)));
			} else {
				statsdate.setDate(day);
				statsdate.setCalories(caloriesAddSport);
				statsdate.setSteps(0);
				statsdate.setMeter(0);
			}
			c.close();
			c = null;
			dailyStatses.add(statsdate);
		}
		getWeeklyStats();
		getMonthlyStats();
	}
	
	private void initAddSport(String day) {
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

    public StatsItem getDailyStatses(int paramInt) {
        return dailyStatses.get(paramInt);
      }

      public int getDailyStatsesCount() {
        return dailyStatses.size();
      }

      public List<StatsItem> getDailyStatsesList() {
        return dailyStatses;
      }
	
	private void getWeeklyStats() {
		weeklyStatses = new ArrayList<StatsItem>();
		String date = "";
		int step = 0;
		int cal = 0;
		int meter = 0;
		if (dailyStatses != null && dailyStatses.size() > 0) {
			for (int i = 0; i < dailyStatses.size(); i++) {
				if (i == 0) {
					date = dailyStatses.get(0).getDate();
					step = dailyStatses.get(0).getSteps();
					cal = dailyStatses.get(0).getCalories();
					meter = dailyStatses.get(0).getMeter();
				} else {
					String date1 = dailyStatses.get(i - 1).getDate();
					String date2 = dailyStatses.get(i).getDate();
					if (Tools.isSameWeek(date1, date2)) {
						step = step + dailyStatses.get(i).getSteps();
						cal = cal + dailyStatses.get(i).getCalories();
						meter = meter + dailyStatses.get(i).getMeter();
					} else {
						date = date + "|" + date1;
						StatsItem statsdate = new StatsItem();
						statsdate.setDate(date);
						statsdate.setCalories(cal);
						statsdate.setSteps(step);
						statsdate.setMeter(meter);
						weeklyStatses.add(statsdate);
						date = date2;
						step = dailyStatses.get(i).getSteps();
						cal = dailyStatses.get(i).getCalories();
						meter = dailyStatses.get(i).getMeter();
					}
				}
				if (dailyStatses.size() == i + 1) {
					StatsItem statsdate = new StatsItem();
					statsdate.setDate(date + "|" + dailyStatses.get(i).getDate());
					statsdate.setCalories(cal);
					statsdate.setSteps(step);
					statsdate.setMeter(meter);
					weeklyStatses.add(statsdate);
				}
			}
		}
	}
	
    public StatsItem getWeeklyStatses(int paramInt) {
      return weeklyStatses.get(paramInt);
    }

    public int getWeeklyStatsesCount() {
      return weeklyStatses.size();
    }

    public List<StatsItem> getWeeklyStatsesList() {
      return weeklyStatses;
    }

	private void getMonthlyStats() {
		monthlyStatses = new ArrayList<StatsItem>();
		String date = "";
		int step = 0;
		int cal = 0;
		int meter = 0;
		if (dailyStatses != null && dailyStatses.size() > 0) {
			for (int i = 0; i < dailyStatses.size(); i++) {
				if (i == 0) {
					date = dailyStatses.get(0).getDate();
					step = dailyStatses.get(0).getSteps();
					cal = dailyStatses.get(0).getCalories();
					meter = dailyStatses.get(0).getMeter();
				} else {
					String date1 = dailyStatses.get(i - 1).getDate();
					String date2 = dailyStatses.get(i).getDate();
					if (Tools.isSameMonth(date1, date2)) {
						step = step + dailyStatses.get(i).getSteps();
						cal = cal + dailyStatses.get(i).getCalories();
						meter = meter + dailyStatses.get(i).getMeter();
					} else {
						date = date + "|" + date1;
						StatsItem statsdate = new StatsItem();
						statsdate.setDate(date);
						statsdate.setCalories(cal);
						statsdate.setSteps(step);
						statsdate.setMeter(meter);
						monthlyStatses.add(statsdate);
						date = date2;
						step = dailyStatses.get(i).getSteps();
						cal = dailyStatses.get(i).getCalories();
						meter = dailyStatses.get(i).getMeter();
					}
				}
				if (dailyStatses.size() == i + 1) {
					StatsItem statsdate = new StatsItem();
					statsdate.setDate(date + "|" + dailyStatses.get(i).getDate());
					statsdate.setCalories(cal);
					statsdate.setSteps(step);
					statsdate.setMeter(meter);
					monthlyStatses.add(statsdate);
				}
			}
		}
	}

    public StatsItem getMonthlyStatses(int paramInt) {
        return monthlyStatses.get(paramInt);
      }

      public int getMonthlyStatsesCount() {
        return monthlyStatses.size();
      }

      public List<StatsItem> getMonthlyStatsesList() {
        return monthlyStatses;
      }
	
}
