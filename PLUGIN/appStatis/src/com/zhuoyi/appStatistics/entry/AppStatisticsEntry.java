/*modification phHe*/

package com.zhuoyi.appStatistics.entry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.zhuoyi.appStatistics.entry.HomeWatcher.OnHomePressedListener;
import com.zhuoyi.appStatistics.info.ApkStatisticInfo;
import com.zhuoyi.appStatistics.info.AppStatisticInfo;
import com.zhuoyi.appStatistics.info.ViewColumnInfo;
import com.zhuoyi.appStatistics.service.StatisService;
import com.zhuoyi.appStatistics.storage.AppStatisticsStorage;
import com.zhuoyi.appStatistics.utils.LogUtil;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

public class AppStatisticsEntry {
	private static AppStatisticsEntry mSelf;

	private Context mContext;
	private String mImsi;
	private MyRunable showTime = new MyRunable();
	private long e_dt = 0;
	private boolean	isStop =true;
	private HomeWatcher mHomeWatcher; 
	public static boolean isStart = false;
	//private ScreenBroadcastReceiver mScreenReceiver;
	public AppStatisticsEntry(Context context) {
		mContext = context;

		TelephonyManager tm = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		mImsi = tm.getSubscriberId();

		String sdDirPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		File debugFile = new File(sdDirPath + "/TydAppStatisticsDebugSwitchOn");
		if (debugFile.exists()) {
			LogUtil.SWITCH = true;
		}
	}

	public static AppStatisticsEntry getInstance(Context context) {
		if (mSelf == null) {
			mSelf = new AppStatisticsEntry(context);
		}

		return mSelf;
	}

	private boolean isServiceHandlerActive() {
		Handler handler = getServiceHandler();
		return handler == null ? false : true;
	}

	private Handler getServiceHandler() {
		return StatisService.getServiceHandler();
	}

	public void startAppStatistics(Context context) {
		// StatisService statisService = null;
		context.getPackageName();
		String pkgName = context.getPackageName();
		String arry[] = null;
		arry = pkgName.split("\\.");
		String newPkgName = arry[0] + "." + arry[1] + "." + arry[2];
		String appName = context.getApplicationInfo()
				.loadLabel(context.getPackageManager()).toString();
		startAppStatis("appId", "channelId", "appTypeId");

		//PowerManager pm = (PowerManager) mContext
			//	.getSystemService(Context.POWER_SERVICE);
		//boolean isScreenOn = pm.isScreenOn();//true 表示屏幕亮了，否则表示黑了
		mHomeWatcher = new HomeWatcher(mContext);  
        mHomeWatcher.setOnHomePressedListener(new OnHomePressedListener() {  
            @Override  
            public void onHomePressed() {  
            	 ActivityManager mActivityManager = (ActivityManager) mContext
                         .getSystemService(Context.ACTIVITY_SERVICE);
				 List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
				  String curPackageName = rti.get(0).topActivity.getPackageName();
				  Log.i("234", "1111上山打老虎=====");
				  Log.i("234", "e_dt+++++125====="+curPackageName);
				if(mContext.getPackageName().equals(curPackageName)){
					
					getIsHome(); 
					//endAppStatis("appId", "channelId", "appTypeId");
					Log.i("234", "e_dt+++++125====="+curPackageName);
					//isStart = false;
					//mHandler.removeCallbacks(showTime);
				}
            	
        		
            }  
  
            @Override  
            public void onHomeLongPressed() {  
                Log.e("234", "onHomeLongPressed");  
            }  
        });  
        mHomeWatcher.startWatch();  
		
		Log.i("12345", "newPkgName123=" + newPkgName + "       appName = "
				+ appName);
		//startScreenBroadcastReceiver();
	}



	
	public void endAppStatistics(Context context) {
	//	endAppStatis("appId", "channelId", "appTypeId");
		
		
		
		

	}
	

    public void getIsHome() {
    	new Thread(showTime).start();

	//	PowerManager pm = (PowerManager) context
		//		.getSystemService(Context.POWER_SERVICE);
	//	boolean isScreenOn = pm.isScreenOn();//true 表示屏幕亮了，否则表示黑了
		
	//	if (isScreenOn == false || isHome(context) == true) {
			//Log.i("345", "isScreenOn45=====" );
			
			
		//	new Thread(showTime).start();
			
		//}else{
			
		//	isStop =false;
			Log.i("345", "isScreenOn2=====" );
		//}  
    	

	}

	public void startAppStatis(String appId, String channelId, String appTypeId) {
		if (mImsi == null) {
			return;
		}

		long startMillis = System.currentTimeMillis();
		AppStatisticsStorage.saveStartTime(mContext, startMillis);
		if (isServiceHandlerActive()) {
			Handler handler = getServiceHandler();
			Message msg = new Message();
			msg.what = StatisService.MSG_APP_START_UPLOAD;
			msg.obj = new AppStatisticInfo(appId, startMillis, channelId,
					appTypeId);

			handler.sendMessage(msg);

		} else {
			Intent serviceIntent = new Intent(mContext, StatisService.class);
			serviceIntent.putExtra(StatisService.EXTRA_MSGID,
					StatisService.MSG_APP_START_UPLOAD);
			serviceIntent.putExtra(StatisService.EXTRA_TIMEMILLIS,
					System.currentTimeMillis());  
			serviceIntent.putExtra(StatisService.EXTRA_APPID, appId);
			serviceIntent.putExtra(StatisService.EXTRA_APPTYPEID, appTypeId);
			serviceIntent.putExtra(StatisService.EXTRA_CHANNELID, channelId);

			mContext.startService(serviceIntent);

		}
	}

	public void endAppStatis(String appId, String channelId, String appTypeId) {
		if (mImsi == null) {
			return;
		}
		Log.i("111", "endAppStatis  = ");
		if (isServiceHandlerActive()) {
			Handler handler = getServiceHandler();
			Message msg = new Message();
			msg.what = StatisService.MSG_APP_EXIT_UPLOAD;
			msg.obj = new AppStatisticInfo(appId, System.currentTimeMillis(),
					channelId, appTypeId);

			handler.sendMessage(msg);

		} else {
			Intent serviceIntent = new Intent(mContext, StatisService.class);
			serviceIntent.putExtra(StatisService.EXTRA_MSGID,
					StatisService.MSG_APP_EXIT_UPLOAD);
			serviceIntent.putExtra(StatisService.EXTRA_TIMEMILLIS,
					System.currentTimeMillis());
			serviceIntent.putExtra(StatisService.EXTRA_APPID, appId);
			serviceIntent.putExtra(StatisService.EXTRA_APPTYPEID, appTypeId);
			serviceIntent.putExtra(StatisService.EXTRA_CHANNELID, channelId);

			mContext.startService(serviceIntent);
		}
	}

	public void apkDownSucStatis(String marketId, String channelId,
			String from, String pkgName, String appName, int verCode, int appId) {
		if (mImsi == null) {
			return;
		}

		if (isServiceHandlerActive()) {
			Handler handler = getServiceHandler();
			Message msg = new Message();
			msg.what = StatisService.MSG_APK_DOWN_SUCCESS;
			msg.obj = new ApkStatisticInfo(marketId,
					System.currentTimeMillis(), channelId, pkgName, appName,
					verCode, from, appId);

			handler.sendMessage(msg);

		} else {
			Intent serviceIntent = new Intent(mContext, StatisService.class);
			serviceIntent.putExtra(StatisService.EXTRA_MSGID,
					StatisService.MSG_APK_DOWN_SUCCESS);
			serviceIntent.putExtra(StatisService.EXTRA_TIMEMILLIS,
					System.currentTimeMillis());
			serviceIntent.putExtra(StatisService.EXTRA_APPID, marketId);
			serviceIntent.putExtra(StatisService.EXTRA_FROM, from);
			serviceIntent.putExtra(StatisService.EXTRA_CHANNELID, channelId);
			serviceIntent.putExtra(StatisService.EXTRA_PKGNAME, pkgName);
			serviceIntent.putExtra(StatisService.EXTRA_APPNAME, appName);
			serviceIntent.putExtra(StatisService.EXTRA_VERCODE, verCode);
			serviceIntent.putExtra(StatisService.EXTRA_APKID, appId);

			mContext.startService(serviceIntent);
		}
	}

	public void apkInstallSucStatis(String marketId, String channelId,
			String from, String pkgName, String appName, int verCode, int appId) {
		if (mImsi == null) {
			return;
		}

		if (isServiceHandlerActive()) {
			Handler handler = getServiceHandler();
			Message msg = new Message();
			msg.what = StatisService.MSG_APK_INSTALL_SUCCESS;
			msg.obj = new ApkStatisticInfo(marketId,
					System.currentTimeMillis(), channelId, pkgName, appName,
					verCode, from, appId);

			handler.sendMessage(msg);

		} else {
			Intent serviceIntent = new Intent(mContext, StatisService.class);
			serviceIntent.putExtra(StatisService.EXTRA_MSGID,
					StatisService.MSG_APK_INSTALL_SUCCESS);
			serviceIntent.putExtra(StatisService.EXTRA_TIMEMILLIS,
					System.currentTimeMillis());
			serviceIntent.putExtra(StatisService.EXTRA_APPID, marketId);
			serviceIntent.putExtra(StatisService.EXTRA_FROM, from);
			serviceIntent.putExtra(StatisService.EXTRA_CHANNELID, channelId);
			serviceIntent.putExtra(StatisService.EXTRA_PKGNAME, pkgName);
			serviceIntent.putExtra(StatisService.EXTRA_APPNAME, appName);
			serviceIntent.putExtra(StatisService.EXTRA_VERCODE, verCode);
			serviceIntent.putExtra(StatisService.EXTRA_APKID, appId);

			mContext.startService(serviceIntent);
		}
	}

	public void viewColumnStatis(String appId, String channelId, String theme,
			String column) {
		if (mImsi == null) {
			return;
		}

		if (isServiceHandlerActive()) {
			Handler handler = getServiceHandler();
			Message msg = new Message();
			msg.what = StatisService.MSG_VIEW_COLUMN;
			msg.obj = new ViewColumnInfo(appId, System.currentTimeMillis(),
					channelId, theme, column);
			handler.sendMessage(msg); 

		} else {
			Intent serviceIntent = new Intent(mContext, StatisService.class);
			serviceIntent.putExtra(StatisService.EXTRA_MSGID,
					StatisService.MSG_VIEW_COLUMN);
			serviceIntent.putExtra(StatisService.EXTRA_TIMEMILLIS,
					System.currentTimeMillis());
			serviceIntent.putExtra(StatisService.EXTRA_APPID, appId);
			serviceIntent.putExtra(StatisService.EXTRA_THEME, theme);
			serviceIntent.putExtra(StatisService.EXTRA_CHANNELID, channelId);
			serviceIntent.putExtra(StatisService.EXTRA_COLUMN, column);

			mContext.startService(serviceIntent);
		}
	}

	public Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			//if(isStart == true){
				
				/*
				 ActivityManager mActivityManager = (ActivityManager) mContext
                         .getSystemService(Context.ACTIVITY_SERVICE);
				 List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
				  String curPackageName = rti.get(0).topActivity.getPackageName();
				if(!mContext.getPackageName().equals(curPackageName)){
					
					endAppStatis("appId", "channelId", "appTypeId");
					Log.i("234", "e_dt+++++123====="+curPackageName);
					isStart = false;
					mHandler.removeCallbacks(showTime);
				}
				*/
				
				endAppStatis("appId", "channelId", "appTypeId");
		//	}
			
		};
	};
	
	class MyRunable implements Runnable {

		@Override 
		public void run() {

			mHandler.sendMessage(Message.obtain());
			mHandler.postDelayed(showTime, 20000);
			mHandler.removeCallbacks(showTime);
			Log.i("234", "running2------");
			

		}

	}
	
	/*
	*//**
     * 判断是否为桌面
     * 
     * @return
     *//*
    public static boolean isHome(Context context) {
            List<String> homes = getHomes(context);
            ActivityManager mActivityManager = (ActivityManager) context
                            .getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
            return homes.contains(rti.get(0).topActivity.getPackageName());
    }

    *//**
     * 获取所有桌面
     * 
     * @return
     *//*
    private static List<String> getHomes(Context context) {
            List<String> packages = new ArrayList<String>();
            PackageManager packageManager = context.getPackageManager();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(
                            intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo info : resolveInfo) {
                    packages.add(info.activityInfo.packageName);
            }
            return packages;
    }
	*/

	
	

}
