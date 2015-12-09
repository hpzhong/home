package com.zhuoyou.plugin.cloud;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;

import com.zhuoyou.plugin.running.Tools;

public class AlarmUtils {

	private static int Time_Interval_Change = 120;

	public static void setAutoSyncAlarm(Context context) {
		long millis = Tools.getAutoSyncTime(context);
		if(millis <= 0) {
			Time time=new Time(); 
			time.setToNow();
			millis = getNextAutoSyncTime(time);
		} else {
			Time time = new Time();
			time.set(millis);
			millis = getNextAutoSyncTime(time);
			if (millis < System.currentTimeMillis()) {
				time.setToNow();
				millis = getNextAutoSyncTime(time);
			}
		}
		cancelAutoSyncAlarm(context);
		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent("com.zhuoyou.running.autosync.alarm");
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		alarm.set(AlarmManager.RTC_WAKEUP, millis, sender);
	}
	
	public static void cancelAutoSyncAlarm(Context context) {
		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent("com.zhuoyou.running.autosync.alarm");
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		alarm.cancel(sender);
	}
	
	public static long getNextAutoSyncTime(Time time)
	{
		time.minute = time.minute + Time_Interval_Change;
		if(time.minute >= 60)
		{
			time.hour = time.hour + time.minute/60;
			time.minute = time.minute%60;
		}
		
		if(time.hour >= 24)
		{
			time.monthDay = time.monthDay + time.hour/24;
			time.hour = time.hour%24;
		}
		int daysofmonth = getDaysofMonth(time.year, time.month);
		if(time.monthDay > daysofmonth)
		{
			time.monthDay = time.monthDay - daysofmonth;
			time.month += 1;
			if(time.month >= 12)
			{
				time.year += 1;
				time.month = time.month % 12;
			}
		}	
		
		return time.toMillis(true);
	}

	//获取所给月的总天数
	public static int getDaysofMonth(int year, int month)
	{
		int days = 0;
		switch(month+1)
		{
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			days = 31;
			break;
		case 2:
			if(year/4 == 0 || (year/100 == 0&&year/400 != 0))
			{
				days = 29;
			}
			else
			{
				days = 28;
			}
			break;
		default:
			days = 30;
			break;
		}
		return days;
	}

}
