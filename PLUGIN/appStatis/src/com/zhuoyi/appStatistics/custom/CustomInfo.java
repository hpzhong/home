package com.zhuoyi.appStatistics.custom;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.zhuoyi.appStatistics.R;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class CustomInfo {
	private HashMap<String, Object> mInfoMap;
	//public static boolean isWifi =true;
	
	public CustomInfo(){
		mInfoMap = new HashMap<String, Object>();
	}
	
	public void addOne(String key, Object value){
		mInfoMap.put(key, value);
	}
	
	public  boolean getWifiBoolean( boolean isWifi){
		
		return isWifi;
		
	}
	public static String getChannelID(Context context){
		//InputStream is = null;
		 AssetManager assets = null;
		assets = context.getAssets();
		//is =	assets.open("raw/td");
		Log.i("123", "getRawData(context,is).toLowerCase() =="+getRawData(context,assets).toLowerCase());
		return getRawData(context,assets).toLowerCase();
		
	}    
	
	public static String getRawData(Context context,AssetManager assets) {
		String td;
		//InputStream is = context.getResources().openRawResource(id);
		InputStream is = null;
		try { 
			is = assets.open("td");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		DataInputStream dis = new DataInputStream(is);  
		byte[] buffer = null;
		try {
			buffer = new byte[is.available()];
			dis.readFully(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {
				dis.close();
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		td=new String(buffer);
		return td!=null?td:"";
	}
	public JSONObject addInfoToJA(JSONObject jo){
		Iterator iter = mInfoMap.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry entry = (Map.Entry)iter.next();
			String key = (String)entry.getKey();
			Object value = entry.getValue();
			try {
				jo.put(key, value);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return jo;
	}
}
