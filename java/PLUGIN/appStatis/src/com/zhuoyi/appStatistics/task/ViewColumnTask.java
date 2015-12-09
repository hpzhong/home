package com.zhuoyi.appStatistics.task;

import org.json.JSONArray;
import org.json.JSONObject;

import com.zhuoyi.appStatistics.custom.ActionType;
import com.zhuoyi.appStatistics.httpConnect.HttpConnection;
import com.zhuoyi.appStatistics.info.ViewColumnInfo;
import com.zhuoyi.appStatistics.storage.AppStatisticsStorage;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.telephony.TelephonyManager;

public class ViewColumnTask extends UploadTask {

	private Context mContext;
	private JSONObject mJsonOb;
	
	private ViewColumnInfo mColumnInfo;
	
	public ViewColumnTask(Context context, Handler handler, ViewColumnInfo columnInfo) {
		super(handler);
		
		mContext = context;
		mJsonOb = new JSONObject();
		mColumnInfo = columnInfo;
	}

	private void putColumnStatisInfo(){
		TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = tm.getSubscriberId();
		if(imsi == null){
			imsi = "null";
		}
		
		try{
			PackageManager pm = mContext.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), 0);
			
			mJsonOb.put("IS", imsi);
			mJsonOb.put("ac_id", ActionType.VIEW_COLUMN);
			mJsonOb.put("app_id", mColumnInfo.getAppId());
			mJsonOb.put("ch", mColumnInfo.getChannelId());
			mJsonOb.put("u_dt", mColumnInfo.getTimeMillis());
			mJsonOb.put("col", mColumnInfo.getColumn());
			mJsonOb.put("ch2", mColumnInfo.getTheme());
			mJsonOb.put("m_cd", Integer.toString(pi.versionCode));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	protected void run() {
		putColumnStatisInfo();
		
		JSONArray entityJa = AppStatisticsStorage.getSavedUnuploadData(mContext);
		if(entityJa == null){
			entityJa = new JSONArray();
		}
		entityJa.put(mJsonOb);
		
		//HttpConnection.getInstance(mContext).uploadStatisticsData(entityJa,null);
	}

}
