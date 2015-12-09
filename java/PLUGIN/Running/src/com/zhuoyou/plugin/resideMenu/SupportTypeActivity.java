package com.zhuoyou.plugin.resideMenu;

import android.app.Activity;
import android.os.Bundle;

import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningTitleBar;

public class SupportTypeActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.support_type);
		RunningTitleBar.getTitleBar(this, "支持的机型");
	}
	
}
