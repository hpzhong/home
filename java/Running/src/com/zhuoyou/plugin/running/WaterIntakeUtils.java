package com.zhuoyou.plugin.running;

import java.util.Calendar;

import android.content.Context;
import android.util.Log;

public class WaterIntakeUtils {
	public static boolean isInWarnTime(Context context, long paramLong) {
		int i = 8;
		String str1 = getTimeString(context, paramLong);
//		int i = 8;
		int j = 0;
		int k = 22;
		int m = 0;
		int n = 1;
		if ((i >= k) && (j >= m))
			n = 0;
		String str2 = "08:00";
		String str3 = "22:00";
		Log.i(" liuzhiying  136  ","n = "+n+" i = "+i+" j = "+j);
		if (n != 0) {
			if ((str1.compareTo(str2) >= 0) && (str1.compareTo(str3) <= 0))
				{Log.i(" liuzhiying  140 ",str1); 
				return true;
				}
			Log.i(" liuzhiying  143 ",str1);
		}
		Log.i(" liuzhiying  145  ",str1);
		return false;
	}
	private static String getTimeString(Context paramContext, long paramLong)
	  {
		int i=-1;
		String str="";
		Calendar mCalendar=Calendar.getInstance();
		mCalendar.setTimeInMillis(paramLong);
	    if (android.text.format.DateFormat.is24HourFormat(paramContext))
	    {
	    	i=mCalendar.get(Calendar.HOUR_OF_DAY);
	    	if(i<10)
    		{
    			str=str+"0";
    		}
	    	str=str+i+":";
	    	
	    }else{
	        i=mCalendar.get(Calendar.HOUR);
	    	if(mCalendar.get(Calendar.AM_PM)==0)
	    	{
	    		if(i<10)
	    		{
	    			str=str+"0";
	    		}
	    	}else{
	    		i=i+12;
	    	}
	    	str=str+i+":";
	    }
	    
	    int u=mCalendar.get(Calendar.MINUTE);
	    if(u<10)
	    {
	    	str=str+"0";
	    }
	    
	    str=str+u;
	    return str;
	  }

}
