package com.zhuoyou.plugin.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.zhuoyou.plugin.cloud.AlarmUtils;
import com.zhuoyou.plugin.cloud.CloudSync;
import com.zhuoyou.plugin.running.Tools;

public class ConnectionChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (CloudSync.autoSyncType == 2) {
			ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = manager.getActiveNetworkInfo();
			if(networkInfo != null) {
				CloudSync.startAutoSync(4);
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

}
