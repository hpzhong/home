package com.zhuoyi.appStatistics.utils;

import android.util.Log;

public class LogUtil {
	public static final String LOGTAG = "TydAppStatis";
	
	public static boolean SWITCH = true;
	
	/*
	 * 
	 */
	public static void logI(String tag, String func, String msg){
		if(!SWITCH){
			return;
		}
		
		String logStr = "[" + tag + "]" + func + "():" + msg;
		Log.i(LOGTAG, logStr);
	}
	
	public static void logE(String tag, String func, String msg){
		if(!SWITCH){
			return;
		}
		
		String logStr = "[" + tag + "]" + func + "():" + msg;
		Log.e(LOGTAG, logStr);
	}
	
	public static void logV(String tag, String func, String msg){
		if(!SWITCH){
			return;
		}
		
		String logStr = "[" + tag + "]" + func + "():" + msg;
		Log.v(LOGTAG, logStr);
	}
}
