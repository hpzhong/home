/**
 * 
 */
package com.zhuoyou.plugin.running;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zhuoyou.plugin.bluetooth.service.BluetoothService;

/**
 * This class receives BOOT_COMPLETED action to start NotificationWatcher main
 * service.
 */
public class BootReceiver extends BroadcastReceiver {
	// Debugging
	private static final String LOG_TAG = "BootReceiver";

	public BootReceiver() {
		Log.i(LOG_TAG, "BootReceiver(), BootReceiver created!");
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(LOG_TAG, "onReceive(), action=" + intent.getAction());
		Log.i(LOG_TAG, "BootReceiver(), Start MainService!");
		context.startService(new Intent(context, MainService.class));
		context.startService(new Intent(context, BluetoothService.class));
	}
}