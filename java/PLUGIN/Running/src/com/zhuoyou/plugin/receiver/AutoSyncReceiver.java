package com.zhuoyou.plugin.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.zhuoyou.plugin.cloud.AlarmUtils;
import com.zhuoyou.plugin.cloud.CloudSync;
import com.zhuoyou.plugin.running.Tools;

public class AutoSyncReceiver extends BroadcastReceiver {

//	public static ConnectionChangeReceiver mConnectionReceiver = null;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if(action.equals("com.zhuoyou.running.autosync.alarm")) {
			ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);;
			NetworkInfo networkInfo = manager.getActiveNetworkInfo();
			if(networkInfo == null) {
				CloudSync.autoSyncType = 2;
				return;
			}
			CloudSync.autoSyncType = 1;
			Tools.setAutoSyncTime(context, System.currentTimeMillis());
			AlarmUtils.setAutoSyncAlarm(context);
			int nType = networkInfo.getType();
			if(nType == ConnectivityManager.TYPE_MOBILE) {
				CloudSync.startAutoSync(2);
			} else if(nType == ConnectivityManager.TYPE_WIFI) {
				CloudSync.startAutoSync(1);
			}
		}
	}
}
