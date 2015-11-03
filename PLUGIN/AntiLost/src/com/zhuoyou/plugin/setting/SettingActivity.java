package com.zhuoyou.plugin.setting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.zhuoyou.plugin.antilost.PlugTools;
import com.zhuoyou.plugin.antilost.R;

public class SettingActivity extends Activity implements OnClickListener {

	private ImageView mBack;
	private LinearLayout mPassword;
	private LinearLayout mPassword_cancle;
	private String password = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_setting);
		initView();
	}
	
	private void initView() {
		mBack = (ImageView) findViewById(R.id.back);
		mBack.setOnClickListener(this);
		mPassword = (LinearLayout)findViewById(R.id.password);
		mPassword.setOnClickListener(this);
		mPassword_cancle = (LinearLayout)findViewById(R.id.password_cancle);
		mPassword_cancle.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.password:
			intent = new Intent();
			intent.setClass(this, PasswordActivity.class);
	        startActivity(intent);
			break;
		case R.id.password_cancle:
			password = PlugTools.getDataString(this, "password");
			if (password == null || password.equals("****")) {
				Toast.makeText(this, R.string.no_antilost_password, Toast.LENGTH_SHORT).show();
			} else {
				intent = new Intent();
				intent.setClass(this, CancelPasswordActivity.class);
		        startActivity(intent);
			}
			break;
		default:
			break;
		}		
	}
	
}
