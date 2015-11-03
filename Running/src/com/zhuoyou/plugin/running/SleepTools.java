package com.zhuoyou.plugin.running;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.Duration;

import com.zhuoyou.plugin.database.DataBaseUtil;

import android.annotation.SuppressLint;
import android.content.ClipData.Item;
import android.util.Log;

public class SleepTools {

	
	public static int getDurationTime(long startTime,long endTime){
		Calendar startCal = getCalendar(startTime);
		Calendar endCal = getCalendar(endTime);
		long duration = endCal.getTimeInMillis() - startCal.getTimeInMillis();
		Log.i("hepenghui", "duration="+ endCal.getTimeInMillis());	
		Log.i("hepenghui", "duration="+ startCal.getTimeInMillis());	

		return 	(int) (duration/1000);
	}

	public static Calendar getCalendar(long time){
		Calendar mCal = Calendar.getInstance();
		
		int year =(int) (time/10000000000L);
		if(year<2015 || year > 2029){ return mCal; }
		
		int month=(int)((time%10000000000L) / 100000000);
		int day = (int)((time%100000000) / 1000000);
		int hour =(int)((time%1000000) / 10000);
		int min = (int)((time%10000) / 100);
		int sec = (int) (time%100);
		mCal.set(year, month-1, day, hour, min, sec);
		return mCal;
	}
	
	public static List<Integer> getData(String turnData,int index) throws NumberFormatException{
		List<Integer> turnList = new ArrayList<Integer>();
		String[] turnArray = turnData.split("\\|");
		int maxData = turnArray.length < index ? turnArray.length :index;
		for(int i = 0;i< maxData ;i++){
			turnList.add(Integer.valueOf(turnArray[i]));
		}
		return turnList;
	}
	public static int getDeepSleep(List<Integer> turnList){
		int deepSleep = 0;
		for(int i = 0 ;i < turnList.size();i ++){
			if(turnList.get(i)<= 34 && turnList.get(i) >= 0 ){
				deepSleep = deepSleep + 30*60; //数据每30分钟记录一次
			}
		}
		return deepSleep;
	}

	@SuppressLint("SimpleDateFormat")
	private static String getDate(int prev_index) {
		Calendar c = Calendar.getInstance();
		int theYear = c.get(Calendar.YEAR);
		int theMonth = c.get(Calendar.MONTH);
		int theDay = c.get(Calendar.DAY_OF_MONTH) - prev_index;
		c.set(theYear, theMonth, theDay);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		Date currentDate = c.getTime();
		String curTime = formatter.format(currentDate);
		return curTime;
	}

	public static int getIntervalTime(String start, String end) {
		int interval = 0;
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");

		try {
			Date d1 = df.parse(start);
			Date d2 = df.parse(end);
			long diff = d2.getTime() - d1.getTime();// 这样得到的差值是微秒级别

			long days = diff / (1000 * 60 * 60 * 24);
			long hours = (diff - days * (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
			long minutes = (diff - days * (1000 * 60 * 60 * 24) - hours * (1000 * 60 * 60)) / (1000 * 60);
			interval = (int)(hours * 60 + minutes) * 60;
		} catch (Exception e) {
		}
		return interval;
	}
	
	public static int getDeepSleep2(String[] details){
		int length = details.length;
		int deepSleep = 0;
		for(int i = 0; i < details.length / 2; i++) {
			if(details[i * 2].equals("2") && (i * 2 + 3) < length){
				String start = getDate(0) + details[i * 2 + 1];
				String end = "";
				int time = Integer.parseInt(details[i * 2 + 3]);
				if (time >= 1200)
					end = getDate(0) + details[i * 2 + 3];
				else
					end = getDate(-1) + details[i * 2 + 3];
				
				deepSleep = deepSleep + getIntervalTime(start, end);
			}
		}
		return deepSleep;
	}

	public static int getlightSleep2(String[] details){
		int length = details.length;
		int lightSleep = 0;
		for(int i = 0; i < details.length / 2; i++) {
			if(details[i * 2].equals("1") && (i * 2 + 3) < length){
				String start = getDate(0) + details[i * 2 + 1];
				String end = "";
				int time = Integer.parseInt(details[i * 2 + 3]);
				if (time >= 1200)
					end = getDate(0) + details[i * 2 + 3];
				else
					end = getDate(-1) + details[i * 2 + 3];
				
				lightSleep = lightSleep + getIntervalTime(start, end);
			}
		}
		return lightSleep;
	}
	
	public static ArrayList<SleepBean> getSleepBean(Calendar startTime,Calendar endTime,List<Integer> turnList){
		Calendar startCal = (Calendar) startTime.clone();
		ArrayList<SleepBean> beansArray = new ArrayList<SleepBean>();
		if(turnList == null ||turnList.size() == 0) return beansArray;
		
		
		final int rollOver = 22;
		boolean isDeepSleep = false;
		DecimalFormat decFormat = new DecimalFormat("#00");
		
		SleepBean bean = new SleepBean();
		bean.setDeep(isDeepSleep);
		bean.setStartTime(decFormat.format(startCal.get(Calendar.HOUR_OF_DAY))+":"+decFormat.format(startCal.get(Calendar.MINUTE)));
		
		for(int i=0; i<turnList.size(); i++){
			boolean temp;
			if(turnList.get(i) > rollOver){
				temp = false;
			}else{
				temp = true;				
			}
			startCal.add(Calendar.MINUTE, 30);
			
			// 封装数据
			if(isDeepSleep != temp){
				isDeepSleep = temp;
				if( bean != null ){
					bean.setEndTime(decFormat.format(startCal.get(Calendar.HOUR_OF_DAY))+":"+decFormat.format(startCal.get(Calendar.MINUTE)));
					beansArray.add(bean);
				}	
				bean = new SleepBean();
				bean.setDeep(isDeepSleep);
				bean.setStartTime(decFormat.format(startCal.get(Calendar.HOUR_OF_DAY))+":"+decFormat.format(startCal.get(Calendar.MINUTE)));
			}
			// 封装最后一组数据
			if( i == (turnList.size()-1)){
				isDeepSleep = temp;
				if( bean != null ){
					bean.setEndTime(decFormat.format(endTime.get(Calendar.HOUR_OF_DAY))+":"+decFormat.format(endTime.get(Calendar.MINUTE)));
					beansArray.add(bean);
				}	
			}
		}
		return beansArray;
	}
	
	public static ArrayList<SleepBean> getSleepBean2(String[] details){
		ArrayList<SleepBean> beansArray = new ArrayList<SleepBean>();
		if(details.length == 0) 
			return beansArray;
		
		for(int i = 0; i < details.length / 2 - 1; i++) {
			SleepBean bean = new SleepBean();
			if(details[i * 2].equals("1")) {
				bean.setDeep(false);
				bean.setStartTime(formatRemoteTime(details[i * 2 + 1]));
				bean.setEndTime(formatRemoteTime(details[i * 2 + 3]));
				beansArray.add(bean);
			} else if(details[i * 2].equals("2")) {
				bean.setDeep(true);
				bean.setStartTime(formatRemoteTime(details[i * 2 + 1]));
				bean.setEndTime(formatRemoteTime(details[i * 2 + 3]));
				beansArray.add(bean);
			}
		}
		return beansArray;
	}

	public static String formatRemoteTime(String old_time) {
		String new_time = old_time.substring(0, 2);
		new_time += ":";
		new_time += old_time.substring(2, 4);
		return new_time;
	}
}
