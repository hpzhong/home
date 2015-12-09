package com.zhuoyi.appStatistics.storage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.json.JSONArray;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class AppStatisticsStorage {
	private static final String ST_APP_STATISTICS = "appStatisticsSt";
	
	private static final String SP_STATISTICS = "statisticsSp";
	private static final String KEY_START_MILLIS = "startMillis";
	
	public static void saveStartTime(Context context, long timeMillis){
		SharedPreferences sp = context.getSharedPreferences(SP_STATISTICS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putLong(KEY_START_MILLIS, timeMillis);
		editor.commit();
	}
	
	public static long getStartTime(Context context){
		SharedPreferences sp = context.getSharedPreferences(SP_STATISTICS, Context.MODE_PRIVATE);
		return sp.getLong(KEY_START_MILLIS, 0);
	}
	
	public static  void saveUnuploadData(Context context, JSONArray ja){
		try{
			Log.i("112", "htyhtyh");
			FileOutputStream fops = context.openFileOutput(ST_APP_STATISTICS, Context.MODE_PRIVATE);
			fops.write(ja.toString().getBytes());
			fops.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void clearUnuploadData(Context context){
		File dataFile = context.getFileStreamPath(ST_APP_STATISTICS);
		if(dataFile.exists()){
			dataFile.delete();
		}
	}
	
	public static JSONArray getSavedUnuploadData(Context context){
		try{
			FileInputStream inStream = context.openFileInput(ST_APP_STATISTICS);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length = -1;
			while((length=inStream.read(buffer))!=-1)   {
				stream.write(buffer,0,length);
			}
	        stream.close();
	        inStream.close();
	        String dataStr = stream.toString();
	        if(dataStr == null || dataStr.equals("")){
	        	return null;
	        }
	        JSONArray ja = new JSONArray(dataStr);
	        if(ja.length() >= 100){
	        	clearUnuploadData(context);
	        	return null;
	        }
	        
	        return ja;
		}catch(Exception e){
		    if (!(e instanceof FileNotFoundException)) {
		        e.printStackTrace();
		    }
		}
		return null;
	}
}
