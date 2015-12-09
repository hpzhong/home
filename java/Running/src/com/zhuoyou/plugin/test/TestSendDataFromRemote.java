package com.zhuoyou.plugin.test;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class TestSendDataFromRemote extends BroadcastReceiver {
	private static final String ACTION_SEND_MSG = "com.tyd.plugin.receiver.sendmsg";
	private static final boolean DEBUG = false;

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		if (DEBUG) {
			String action = arg1.getAction();
			Log.i("gchk", "TestSendDataFromRemote action = " + action);
			if (action.equals(ACTION_SEND_MSG)) {
				int cmd = arg1.getIntExtra("plugin_cmd", 0);
				Log.i("gchk", "test cmd  = " + cmd);
				switch (cmd) {
				case 0X80: {
					char[] c_tag = new char[4];
					String s_utf8;
					Intent intent;
					
					c_tag[0] = 0X21;
					c_tag[1] = 0X27;
					c_tag[2] = 0XFF;
					c_tag[3] = 0XFF;
					s_utf8 = "20150710|1|2101|2|2345|1|2400|0|0100|";
					intent = new Intent("com.zhuoyou.plugin.running.sleep");
					intent.putExtra("tag", c_tag);
					intent.putExtra("content", s_utf8);
					arg0.sendBroadcast(intent);
					
					c_tag[0] = 0X22;
					c_tag[1] = 0X27;
					c_tag[2] = 0XFF;
					c_tag[3] = 0XFF;
					s_utf8 = "20150711|1|0130|2|0200|1|0530|0|0700|";
					intent = new Intent("com.zhuoyou.plugin.running.sleep");
					intent.putExtra("tag", c_tag);
					intent.putExtra("content", s_utf8);
					arg0.sendBroadcast(intent);
					
					c_tag[0] = 0X23;
					c_tag[1] = 0X27;
					c_tag[2] = 0XFF;
					c_tag[3] = 0XFF;
					s_utf8 = "20150711|2|2101|1|2345|0|0101|";
					intent = new Intent("com.zhuoyou.plugin.running.sleep");
					intent.putExtra("tag", c_tag);
					intent.putExtra("content", s_utf8);
					arg0.sendBroadcast(intent);
					
					c_tag[0] = 0X24;
					c_tag[1] = 0X27;
					c_tag[2] = 0XFF;
					c_tag[3] = 0XFF;
					s_utf8 = "20150712|1|0130|2|0300|1|0529|0|0700|";
					intent = new Intent("com.zhuoyou.plugin.running.sleep");
					intent.putExtra("tag", c_tag);
					intent.putExtra("content", s_utf8);
					arg0.sendBroadcast(intent);
					
					c_tag[0] = 0X25;
					c_tag[1] = 0X27;
					c_tag[2] = 0XFF;
					c_tag[3] = 0XFF;
					s_utf8 = "20150713|2|2101|1|2345|0|2400|";
					intent = new Intent("com.zhuoyou.plugin.running.sleep");
					intent.putExtra("tag", c_tag);
					intent.putExtra("content", s_utf8);
					arg0.sendBroadcast(intent);
					
					c_tag[0] = 0X26;
					c_tag[1] = 0X27;
					c_tag[2] = 0XFF;
					c_tag[3] = 0XFF;
					s_utf8 = "20150714|1|0100|2|0400|1|0600|0|0800|";
					intent = new Intent("com.zhuoyou.plugin.running.sleep");
					intent.putExtra("tag", c_tag);
					intent.putExtra("content", s_utf8);
					arg0.sendBroadcast(intent);
					
					c_tag[0] = 0X27;
					c_tag[1] = 0X27;
					c_tag[2] = 0XFF;
					c_tag[3] = 0XFF;
					s_utf8 = "20150714|2|2101|1|2345|0|2400|";
					intent = new Intent("com.zhuoyou.plugin.running.sleep");
					intent.putExtra("tag", c_tag);
					intent.putExtra("content", s_utf8);
					arg0.sendBroadcast(intent);
				}
					break;
				case 0X73: {
					// send personal config to app
					char[] c_tag = new char[4];
					c_tag[0] = 0XFF;
					c_tag[1] = 0XFF;
					c_tag[2] = 0XFF;
					c_tag[3] = 0XFF;

					String s_utf8 = 0 + "|" + 189 + "|" + 66 + "|" + 1999 + "|7000|$|";
					Intent intent = new Intent("com.zhuoyou.plugin.running.sync.personal");
					intent.putExtra("tag", c_tag);
					intent.putExtra("content", s_utf8);
					arg0.sendBroadcast(intent);
				}
					break;
				case 0X70: {
					char[] c_tag = new char[4];
					String s_utf8;
					Intent intent;
					
					c_tag[0] = 0X21;
					c_tag[1] = 0X27;
					c_tag[2] = 0XFF;
					c_tag[3] = 0XFF;
					s_utf8 = "2835|20150702|0906|0935|";
					intent = new Intent("com.zhuoyou.plugin.running.get");
					intent.putExtra("tag", c_tag);
					intent.putExtra("content", s_utf8);
					arg0.sendBroadcast(intent);
					
					c_tag[0] = 0X22;
					c_tag[1] = 0X27;
					c_tag[2] = 0XFF;
					c_tag[3] = 0XFF;
					s_utf8 = "3680|20150703|1011|1045|";
					intent = new Intent("com.zhuoyou.plugin.running.get");
					intent.putExtra("tag", c_tag);
					intent.putExtra("content", s_utf8);
					arg0.sendBroadcast(intent);
					
					c_tag[0] = 0X23;
					c_tag[1] = 0X27;
					c_tag[2] = 0XFF;
					c_tag[3] = 0XFF;
					s_utf8 = "6760|20150704|1934|2056|";
					intent = new Intent("com.zhuoyou.plugin.running.get");
					intent.putExtra("tag", c_tag);
					intent.putExtra("content", s_utf8);
					arg0.sendBroadcast(intent);
					
					c_tag[0] = 0X24;
					c_tag[1] = 0X27;
					c_tag[2] = 0XFF;
					c_tag[3] = 0XFF;
					s_utf8 = "3680|20150705|1011|1045|";
					intent = new Intent("com.zhuoyou.plugin.running.get");
					intent.putExtra("tag", c_tag);
					intent.putExtra("content", s_utf8);
					arg0.sendBroadcast(intent);
					
					c_tag[0] = 0X25;
					c_tag[1] = 0X27;
					c_tag[2] = 0XFF;
					c_tag[3] = 0XFF;
					s_utf8 = "9086|20150706|2250|2359|";
					intent = new Intent("com.zhuoyou.plugin.running.get");
					intent.putExtra("tag", c_tag);
					intent.putExtra("content", s_utf8);
					arg0.sendBroadcast(intent);
					
					c_tag[0] = 0X26;
					c_tag[1] = 0X27;
					c_tag[2] = 0XFF;
					c_tag[3] = 0XFF;
					s_utf8 = "3680|20150707|1011|1045|";
					intent = new Intent("com.zhuoyou.plugin.running.get");
					intent.putExtra("tag", c_tag);
					intent.putExtra("content", s_utf8);
					arg0.sendBroadcast(intent);
					
					c_tag[0] = 0X27;
					c_tag[1] = 0X27;
					c_tag[2] = 0XFF;
					c_tag[3] = 0XFF;
					s_utf8 = "4000|20150702|5000|20150703|7500|20150704|4000|20150705|10000|20150706|5000|20150707|";
					intent = new Intent("com.zhuoyou.plugin.running.get");
					intent.putExtra("tag", c_tag);
					intent.putExtra("content", s_utf8);
					arg0.sendBroadcast(intent);
				}
					break;
				default:
					break;
				}
			}
		}
	}
}
