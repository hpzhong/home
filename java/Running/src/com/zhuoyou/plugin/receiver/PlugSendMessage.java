package com.zhuoyou.plugin.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zhuoyou.plugin.bluetooth.connection.CustomCmd;
import com.zhuoyou.plugin.bluetooth.service.BluetoothService;

public class PlugSendMessage extends BroadcastReceiver {
	private static final String ACTION_SEND_MSG = "com.tyd.plugin.receiver.sendmsg";

	@Override
	public void onReceive(final Context arg0, Intent arg1) {		
		String action = arg1.getAction();
		Log.i("gchk", "receiver msg action = " + action);
		if (action.equals(ACTION_SEND_MSG)) {
			final int cmd = arg1.getIntExtra("plugin_cmd", 0);
			final String s = arg1.getStringExtra("plugin_content");
			final char[] c_tag = arg1.getCharArrayExtra("plugin_tag");
			
			Log.i("gchk", "receiver msg cmd = " + cmd);
			Log.i("gchk", "receiver msg s = " + s);
			
			if (BluetoothService.getInstance() == null)
				return;
			
			if (cmd != 0) {
				if (c_tag != null) {
					Log.i("gchk", "receiver msg tag =_" + c_tag[0] + "_"  + c_tag[1] + "_"  + c_tag[2] + "_"  + c_tag[3]  );
					CustomCmd.sendCustomCmd(cmd, s , c_tag);
				} else {
					CustomCmd.sendCustomCmd(cmd, s);
				}
			}
		}
	}

}
