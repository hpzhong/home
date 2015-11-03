package com.zhuoyou.plugin.ble;

import com.zhuoyou.plugin.running.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class BindHelpActivity extends Activity implements View.OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bind_help);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.bind_help_back:
			case R.id.bind_help_title:
				finish();
				break;
		}
	}
}
