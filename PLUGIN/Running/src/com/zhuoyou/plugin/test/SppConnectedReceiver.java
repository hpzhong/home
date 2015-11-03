package com.zhuoyou.plugin.test;

import com.zhuoyou.plugin.running.MainService;
import com.zhuoyou.plugin.running.Tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SppConnectedReceiver extends BroadcastReceiver {
	private static final String ACTION_SPP_CONNECTED = "com.tyd.bt.spp.connected";
	private static final boolean DEBUG = false;//暂时不做此功能

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		if (DEBUG) {
			String action = arg1.getAction();
			Log.i("gchk", "SppConnectedReceiver action = " + action);

			if(MainService.getInstance()==null){
				Log.e("gchk", "main service didnot create");
			}else{
				Log.e("gchk", "main service has created");
			}
			
			if (action.equals(ACTION_SPP_CONNECTED)) {
				//收到SPP连接成功的广播后，则主动向对方请求数据
				Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
				//TODO:由于小曲那边73没做好。现在直接70
				intent.putExtra("plugin_cmd", 0x73);
				intent.putExtra("plugin_content", "himan");
				arg0.sendBroadcast(intent);
				Tools.setSppConnectedFlag(false);
			}
		}
	}
}
