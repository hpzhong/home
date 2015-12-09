package com.zhuoyi.appStatistics.task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.zhuoyi.appStatistics.custom.ActionType;
import com.zhuoyi.appStatistics.custom.CustomInfo;
import com.zhuoyi.appStatistics.httpConnect.HttpConnection;
import com.zhuoyi.appStatistics.info.AppStatisticInfo;
import com.zhuoyi.appStatistics.info.HttpEntityInfo;
import com.zhuoyi.appStatistics.storage.AppStatisticsStorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

public class AppEndTask extends UploadTask {

	private Context mContext;
	private JSONObject mBodyJsonOb, mHeadJsonOb;
	private String mMf;

	private AppStatisticInfo mAppInfo;

	public AppEndTask(Context context, Handler handler, AppStatisticInfo appInfo) {
		super(handler);

		mContext = context;
		mBodyJsonOb = new JSONObject();
		mHeadJsonOb = new JSONObject();
		mMf = CustomInfo.getChannelID(mContext); // channel id;

		mAppInfo = appInfo;

	}

	private void putAppStatisticInfo(AppStatisticInfo appInfo) {
		long startMillis = AppStatisticsStorage.getStartTime(mContext);
		HttpEntityInfo entityInfo = new HttpEntityInfo(mContext);
		String apk = mContext.getPackageName();
		String arry[] = null;
		arry = apk.split("\\.");
		String xm = arry[0] + "." + arry[1] + "." + arry[2];
		String apk_n = mContext.getApplicationInfo()
				.loadLabel(mContext.getPackageManager()).toString();
		try {
			PackageInfo pkgInfo;
			pkgInfo = mContext.getPackageManager().getPackageInfo(
					mContext.getPackageName(), 0);
			JSONObject dataJo = new JSONObject();

			dataJo.put("ac_id", ActionType.APP_EXIT);
			dataJo.put("s_dt", startMillis);
			dataJo.put("e_dt", appInfo.getTimeMillis());
			dataJo.put("m_cd", Integer.toString(pkgInfo.versionCode));
			dataJo.put("ch", CustomInfo.getChannelID(mContext));
			dataJo.put("xm", xm);
			dataJo.put("apk", apk);
			dataJo.put("apk_n", apk_n);
			Log.i("111", "appInfo.getTimeMillis() = " + appInfo.getTimeMillis());
			/*
			 * mJsonOb.put("u_dt", appInfo.getTimeMillis());
			 * mJsonOb.put("app_id", appInfo.getAppId()); mJsonOb.put("m_id",
			 * appInfo.getAppTypeId()); mJsonOb.put("ch",
			 * appInfo.getChannelId()); mJsonOb.put("m_cd",
			 * Integer.toString(pkgInfo.versionCode));
			 */
			JSONArray sDataJa = AppStatisticsStorage
					.getSavedUnuploadData(mContext);
			if (sDataJa == null) {
				sDataJa = new JSONArray();
			}
			sDataJa.put(dataJo);

			JSONObject cparamJo = new JSONObject();
			cparamJo.put("IE", entityInfo.getImei());
			cparamJo.put("IS", entityInfo.getImsi());
			cparamJo.put("PT", entityInfo.getCpu());
			cparamJo.put("MD", entityInfo.getDev());
			cparamJo.put("lbs", entityInfo.getLbs());
			cparamJo.put("LCD", entityInfo.getLcd());
			cparamJo.put("mac", entityInfo.getMac());
			// cparamJo.put("ip", entityInfo.getNetworkIp());
			cparamJo.put("net_t", entityInfo.getNetworkType());
			cparamJo.put("MF", entityInfo.getOem());
			cparamJo.put("RAM", entityInfo.getRam());
			cparamJo.put("ROM", entityInfo.getRom());
			cparamJo.put("AND", entityInfo.getAndroidVer());

			mBodyJsonOb.put("sdata", sDataJa.toString());
			mBodyJsonOb.put("cparam", cparamJo.toString());
			mBodyJsonOb.put("mf", mMf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void putHttpEntityInfo() {

		try {
			mHeadJsonOb.put("ver", 1);
			mHeadJsonOb.put("type", (byte) 1);
			mHeadJsonOb.put("msb", (long) 1);
			mHeadJsonOb.put("lsb", (long) 1);
			// mHeadJsonOb.put("mcd", MESSAGE_CODE);

			// mHeadJsonOb.put("app_id", entityInfo.getAppId());
			// mHeadJsonOb.put("m_id", entityInfo.getAppTypeId());
			// mHeadJsonOb.put("ch", entityInfo.getChannelId());
			// mHeadJsonOb.put("m_cd", Integer.toString(pkgInfo.versionCode));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void run() {
		putAppStatisticInfo(mAppInfo);
		putHttpEntityInfo();
		/*ConnectivityManager connectMgr = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo info = connectMgr.getActiveNetworkInfo();*/
		Log.i("111", "787878787878787() = ");
		JSONObject resJo;
		try {
			resJo = new JSONObject(mBodyJsonOb.toString());
			String sdata = resJo.getString("sdata");
			JSONArray array = new JSONArray(sdata);
			AppStatisticsStorage.saveUnuploadData(mContext, array);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int saveStartTime = 0;
		long saveTime = 0;
		// long saveTime1 = 0;
		// long fixedTime = 0;
		long saveHour = 0;
		long hour = 1*1000;
		SharedPreferences sp = mContext.getSharedPreferences("endTime",
				Context.MODE_APPEND);
		saveTime = sp.getLong("endCurtime", 0);

		// saveTime1 = sp.getLong("curtime1", 0);

		saveStartTime = sp.getInt("endSaveTime", 0);
		saveHour = sp.getLong("endHour", hour);
		long curTime = AppStatisticsStorage.getStartTime(mContext);

		Log.i("111", "endSaveTime  =====22= " + saveTime);

		int Rate = HttpConnection.getInstance(mContext).getRate();

		Log.i("111", "Rate  =====22= " + Rate);
		if (saveStartTime == 0 || ((curTime - saveTime) > saveHour)) {

			SharedPreferences sps = mContext.getSharedPreferences("endTime",
					Context.MODE_APPEND);
			SharedPreferences.Editor editor = sps.edit();
			editor.putLong("endCurtime", curTime).commit();

			editor.putInt("endSaveTime", 1).commit();

			if (saveStartTime == 1) {
				hour = Rate*60*60*1000;

				editor.putLong("endHour", hour).commit();

				// Log.i("111", "saveStartTime33  " + saveStartTime);
				Log.i("111", "endCurtime 3377 " + curTime);

				/*HttpConnection.getInstance(mContext).uploadStatisticsData(
						mBodyJsonOb, mHeadJsonOb, mMf);*/

			}
			HttpConnection.getInstance(mContext).uploadStatisticsData(
					mBodyJsonOb, mHeadJsonOb, mMf);
		}
		stopService();
	}

}
