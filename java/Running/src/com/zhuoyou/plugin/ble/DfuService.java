package com.zhuoyou.plugin.ble;

import no.nordicsemi.android.dfu.DfuBaseService;
import android.app.Activity;

import com.zhuoyou.plugin.firmware.NotificationActivity;

public class DfuService extends DfuBaseService {

	@Override
	protected Class<? extends Activity> getNotificationTarget() {
		
		return NotificationActivity.class;
	}
	
}
