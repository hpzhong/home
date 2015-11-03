package com.zhuoyou.plugin.bluetooth.attach;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.bluetooth.data.PreferenceData;
import com.zhuoyou.plugin.bluetooth.service.BluetoothService;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;

public class SmsServiceActivity extends Activity implements OnClickListener {

	private LinearLayout enable_sms_service;
	private TextView enable_summary;
	private CheckBox enable_status;
    private static final Context sContext = RunningApp.getInstance().getApplicationContext();
    private static final SharedPreferences sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(sContext);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sms_activity_layout);
		TextView tv_title = (TextView) findViewById(R.id.title);
		tv_title.setText(R.string.sms_preference_title);
		RelativeLayout im_back = (RelativeLayout) findViewById(R.id.back);
		im_back.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		enable_sms_service = (LinearLayout)findViewById(R.id.enable_sms_service);
		enable_sms_service.setOnClickListener(this);
		enable_summary = (TextView)findViewById(R.id.enable_summary);
		enable_status = (CheckBox)findViewById(R.id.enable_status);
		enable_status.setChecked(sSharedPreferences.getBoolean(PreferenceData.PREFERENCE_KEY_SMS ,true));
		if (enable_status.isChecked()) {
			enable_summary.setText(R.string.sms_preference_summary_yes);
		} else {
			enable_summary.setText(R.string.sms_preference_summary_no);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.enable_sms_service:
			BluetoothService service = BluetoothService.getInstance();
            if (service == null) {
                return;
            }
			SharedPreferences.Editor editor = sSharedPreferences.edit();
            if (enable_status.isChecked()) {
				enable_status.setChecked(false);
				editor.putBoolean(PreferenceData.PREFERENCE_KEY_SMS, false);
				enable_summary.setText(R.string.sms_preference_summary_no);
                service.stopSmsService();
            } else {
				enable_status.setChecked(true);
				editor.putBoolean(PreferenceData.PREFERENCE_KEY_SMS, true);
				enable_summary.setText(R.string.sms_preference_summary_yes);
                service.startSmsService();
            }
			editor.commit();
			break;
		}
	}
}
