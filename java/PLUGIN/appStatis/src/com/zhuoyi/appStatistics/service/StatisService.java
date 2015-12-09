package com.zhuoyi.appStatistics.service;

import com.zhuoyi.appStatistics.info.ApkStatisticInfo;
import com.zhuoyi.appStatistics.info.AppStatisticInfo;
import com.zhuoyi.appStatistics.info.ViewColumnInfo;
import com.zhuoyi.appStatistics.task.ApkDownSuccessTask;
import com.zhuoyi.appStatistics.task.ApkInstallSuccessTask;
import com.zhuoyi.appStatistics.task.AppEndTask;
import com.zhuoyi.appStatistics.task.AppStartTask;
import com.zhuoyi.appStatistics.task.ViewColumnTask;
import com.zhuoyi.appStatistics.thread.TaskThread;
import com.zhuoyi.appStatistics.utils.LogUtil;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class StatisService extends Service {
	public static final String TAG = "StatisService";
	
	public static final String EXTRA_MSGID = "msgId";
	public static final String EXTRA_APPID = "appId";
	public static final String EXTRA_TIMEMILLIS = "timeMillis";
	public static final String EXTRA_APPTYPEID = "appTypeId";
	public static final String EXTRA_CHANNELID = "channelId";
	
	public static final String EXTRA_PKGNAME = "pkgName";
	public static final String EXTRA_APPNAME = "appName";
	public static final String EXTRA_VERCODE = "verCode";
	public static final String EXTRA_FROM = "from";
	public static final String EXTRA_APKID = "apkId";
	
	public static final String EXTRA_THEME = "theme";
	public static final String EXTRA_COLUMN = "column";
	
	private static final int MSG_DEFAULT = 0;
	public static final int MSG_STOP_SERVICE = 1;
	public static final int MSG_APP_START_UPLOAD = 2;
	public static final int MSG_APP_EXIT_UPLOAD = 3;
	public static final int MSG_APK_DOWN_SUCCESS = 4;
	public static final int MSG_APK_INSTALL_SUCCESS = 5;
	public static final int MSG_VIEW_COLUMN = 6;
	
	private static Handler staticHandler = null;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Handler getServiceHandler(){
		return staticHandler;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		LogUtil.logI(TAG, "onCreate", "==============================================");
		
		staticHandler = mHandler;
		
		LogUtil.logI(TAG, "onCreate", "----------------------------------------------");
	}

	
	@Override
	public void onDestroy() {
		staticHandler = null;
		
		super.onDestroy();
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent != null){
			//getPackageName();
			int msgId = intent.getIntExtra(EXTRA_MSGID, MSG_DEFAULT);
			if(msgId != MSG_DEFAULT){
				if(msgId == MSG_APP_START_UPLOAD
						|| msgId == MSG_APP_EXIT_UPLOAD){
					String appId = intent.getStringExtra(EXTRA_APPID);
					if(appId != null){
						long millis = intent.getLongExtra(EXTRA_TIMEMILLIS, System.currentTimeMillis());
						String channelId = intent.getStringExtra(EXTRA_CHANNELID);
						String appTypeId = intent.getStringExtra(EXTRA_APPTYPEID);
						AppStatisticInfo info = new AppStatisticInfo(appId, millis, channelId, appTypeId);
						
						Message msg = new Message();
						msg.what = msgId;
						msg.obj = info;
						
						mHandler.sendMessage(msg);
					}
					
				}else if(msgId == MSG_APK_DOWN_SUCCESS
						|| msgId == MSG_APK_INSTALL_SUCCESS){
					String appId = intent.getStringExtra(EXTRA_APPID);
					if(appId != null){
						long millis = intent.getLongExtra(EXTRA_TIMEMILLIS, System.currentTimeMillis());
						String channelId = intent.getStringExtra(EXTRA_CHANNELID);
						String from = intent.getStringExtra(EXTRA_FROM);
						String pkgName = intent.getStringExtra(EXTRA_PKGNAME);
						String appName = intent.getStringExtra(EXTRA_APPNAME);
						int verCode = intent.getIntExtra(EXTRA_VERCODE, 0);
						int apkId = intent.getIntExtra(EXTRA_APKID, 0);
						ApkStatisticInfo info = new ApkStatisticInfo(appId, millis, channelId, pkgName, appName, verCode, from, apkId);
						
						Message msg = new Message();
						msg.what = msgId;
						msg.obj = info;
						
						mHandler.sendMessage(msg);
						Log.i("111","sendMessage"  );
					}
					
				}else if(msgId == MSG_VIEW_COLUMN){
					String appId = intent.getStringExtra(EXTRA_APPID);
					if(appId != null){
						long millis = intent.getLongExtra(EXTRA_TIMEMILLIS, System.currentTimeMillis());
						String channelId = intent.getStringExtra(EXTRA_CHANNELID);
						String theme = intent.getStringExtra(EXTRA_THEME);
						String column = intent.getStringExtra(EXTRA_COLUMN);
						
						ViewColumnInfo info = new ViewColumnInfo(appId, millis, channelId, theme, column);

						Message msg = new Message();
						msg.what = msgId;
						msg.obj = info;
						
						mHandler.sendMessage(msg);
						

					}
				}
			}
		}
		
		return super.onStartCommand(intent, flags, startId);
	}

	

	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			LogUtil.logI(TAG, "handleMessage", "receive msg: " + msg.what);
			switch(msg.what){
				case MSG_STOP_SERVICE:
					stopSelf();
					break;
					
				case MSG_APP_START_UPLOAD:
					AppStatisticInfo startInfo = (AppStatisticInfo)msg.obj;
					AppStartTask startTask = new AppStartTask(StatisService.this, mHandler, startInfo);
					startTask.start();
					break;
					
				case MSG_APP_EXIT_UPLOAD:
					AppStatisticInfo endInfo = (AppStatisticInfo)msg.obj;
					AppEndTask endTask = new AppEndTask(StatisService.this, mHandler, endInfo);
					endTask.start();
					break;
					
				case MSG_APK_DOWN_SUCCESS:
					ApkStatisticInfo downInfo = (ApkStatisticInfo)msg.obj;
					ApkDownSuccessTask downTask = new ApkDownSuccessTask(StatisService.this, mHandler, downInfo);
					downTask.start();
					break;
					
				case MSG_APK_INSTALL_SUCCESS:
					ApkStatisticInfo installInfo = (ApkStatisticInfo)msg.obj;
					ApkInstallSuccessTask installTask = new ApkInstallSuccessTask(StatisService.this, mHandler, installInfo);
					installTask.start();
					break;
					
				case MSG_VIEW_COLUMN:
					ViewColumnInfo columnInfo = (ViewColumnInfo)msg.obj;
					ViewColumnTask columnTask = new ViewColumnTask(StatisService.this, mHandler, columnInfo);
					columnTask.start();
					break;
			}
		}
		
	};
}
