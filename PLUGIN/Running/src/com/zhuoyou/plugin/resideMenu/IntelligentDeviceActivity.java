package com.zhuoyou.plugin.resideMenu;

import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningTitleBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class IntelligentDeviceActivity extends Activity implements OnClickListener{
	private TextView tvIntelligentWatch,tvIntelligentHeadset;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intelligent_device);
		initView();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		RunningTitleBar.getTitleBar(this, "智能设备");
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


	private void initView() {
		tvIntelligentWatch = (TextView) findViewById(R.id.tv_intelligent_watch);
		tvIntelligentHeadset = (TextView) findViewById(R.id.tv_intelligent_headset);
		
		tvIntelligentWatch.setOnClickListener(this);
		tvIntelligentHeadset.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.tv_intelligent_watch:
			intent = new Intent(IntelligentDeviceActivity.this,BluetoothWatchActivity.class);
			startActivity(intent);
			break;
		case R.id.tv_intelligent_headset:
			intent = new Intent(IntelligentDeviceActivity.this,BluetoothHeadsetActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
		
	}
	
}
