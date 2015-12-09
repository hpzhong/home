/*package com.zhuoyou.plugin.receiver;

import java.util.Map;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;

import com.zhuoyou.plugin.resideMenu.ResideMenuRightTop;

public class BTStateChangedReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		BluetoothDevice device = null;
		String action = intent.getAction();
		Map<String, Object> map;
		if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
			device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			int state = device.getBondState();
			switch (state) {
			case BluetoothDevice.BOND_BONDING:
				if (ResideMenuRightTop.mHandler != null) {
					Message msg1 = new Message();
					msg1.what = ResideMenuRightTop.BOND_BONDING;
					msg1.obj = device;
					ResideMenuRightTop.mHandler.sendMessage(msg1);
				}
				break;
			case BluetoothDevice.BOND_BONDED:
				if (ResideMenuRightTop.mHandler != null) {
					Message msg = new Message();
					msg.what = ResideMenuRightTop.START_CONNECT_DEVICE;
					msg.obj = device;
					ResideMenuRightTop.mHandler.sendMessage(msg);
				}
				break;
			default:
				break;
			}
		}
	}

}
*/