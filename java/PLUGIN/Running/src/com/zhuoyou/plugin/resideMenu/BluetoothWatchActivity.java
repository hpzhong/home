package com.zhuoyou.plugin.resideMenu;

import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningTitleBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

public class BluetoothWatchActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth_watch);
		
		RunningTitleBar.getTitleBar(this, "蓝牙手表");
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {//界面返回上一级
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
