package com.zhuoyou.plugin.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.util.Log;

import com.zhuoyou.plugin.resideMenu.ResideMenuRightTop;

public class DeviceNameReceiver extends BroadcastReceiver {
	public static String productName = "";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals("com.tyd.bt.connected.device")) {
			productName = intent.getStringExtra("device");
			Log.i("caixinxin", "productName = " + productName);
			if (ResideMenuRightTop.mHandler != null) {
				Message msg = new Message();
				msg.what = ResideMenuRightTop.REFRESH;
				msg.obj = productName;
				ResideMenuRightTop.mHandler.sendMessage(msg);
			}
		} else if (action.equals("com.tyd.bt.device.battery")) {
			if (ResideMenuRightTop.mHandler != null) {
				char[] c_tag = intent.getCharArrayExtra("tag");
				Message msg = new Message();
				msg.what = ResideMenuRightTop.BATTERY;
				msg.arg1 = c_tag[0] - 0x20;
				msg.arg2 = c_tag[1];
				ResideMenuRightTop.mHandler.sendMessage(msg);
			}
		}
	}

}
