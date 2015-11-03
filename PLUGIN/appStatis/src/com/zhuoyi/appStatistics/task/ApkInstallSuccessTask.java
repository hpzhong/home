package com.zhuoyi.appStatistics.task;

import org.json.JSONArray;
import org.json.JSONObject;

import com.zhuoyi.appStatistics.custom.ActionType;
import com.zhuoyi.appStatistics.httpConnect.HttpConnection;
import com.zhuoyi.appStatistics.info.ApkStatisticInfo;
import com.zhuoyi.appStatistics.storage.AppStatisticsStorage;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.telephony.TelephonyManager;

public class ApkInstallSuccessTask extends UploadTask {

	private Context mContext;
	private JSONObject mJsonOb;
	
	private ApkStatisticInfo mApkInfo;
	
	public ApkInstallSuccessTask(Context context, Handler handler, ApkStatisticInfo apkInfo) {
		super(handler);

		mContext = context;
		mJsonOb = new JSONObject();
		mApkInfo = apkInfo;
	}

	private void putApkStatisInfo(){
		TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = tm.getSubscriberId();
		if(imsi == null){
			imsi = "null";
		}
		
		try{
			PackageManager pm = mContext.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), 0);
			
			mJsonOb.put("IS", imsi);
			mJsonOb.put("ac_id", ActionType.APK_INSTALL_SUCCESS);
			mJsonOb.put("app_id", mApkInfo.getAppId());
			mJsonOb.put("ch", mApkInfo.getChannelId());
			mJsonOb.put("u_dt", mApkInfo.getTimeMillis());
			mJsonOb.put("from", mApkInfo.getFrom());
			mJsonOb.put("apk", mApkInfo.getPkgName());
			mJsonOb.put("apk_n", mApkInfo.getAppName());
			mJsonOb.put("apk_v", mApkInfo.getVerCode());
			mJsonOb.put("apk_id", mApkInfo.getApkId());
			mJsonOb.put("m_cd", Integer.toString(pi.versionCode));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	protected void run() {
		putApkStatisInfo();

		JSONArray entityJa = AppStatisticsStorage.getSavedUnuploadData(mContext);
		if(entityJa == null){
			entityJa = new JSONArray();
		}
		entityJa.put(mJsonOb);
		
	   //HttpConnection.getInstance(mContext).uploadStatisticsData(entityJa,null);
	}

}
