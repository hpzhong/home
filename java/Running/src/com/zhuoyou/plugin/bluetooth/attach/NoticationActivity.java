package com.zhuoyou.plugin.bluetooth.attach;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.zhuoyou.plugin.custom.CustomAlertDialog;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;

public class NoticationActivity extends Activity implements OnClickListener {

	private LinearLayout show_accessibility_menu;
	private LinearLayout enable_notifi_service;
	private LinearLayout select_notifi;
	private TextView enable_summary;
	private CheckBox enable_status;
	public static final Intent ACCESSIBILITY_INTENT = new Intent("android.settings.ACCESSIBILITY_SETTINGS");
    private static final Context sContext = RunningApp.getInstance().getApplicationContext();
    private static final SharedPreferences sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(sContext);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notication_activity_layout);
		TextView tv_title = (TextView) findViewById(R.id.title);
		tv_title.setText(R.string.notification_preference_title);
		RelativeLayout im_back = (RelativeLayout) findViewById(R.id.back);
		im_back.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		show_accessibility_menu = (LinearLayout)findViewById(R.id.show_accessibility_menu);
		show_accessibility_menu.setOnClickListener(this);
		enable_notifi_service = (LinearLayout)findViewById(R.id.enable_notifi_service);
		enable_notifi_service.setOnClickListener(this);
		select_notifi = (LinearLayout)findViewById(R.id.select_notifi);
		select_notifi.setOnClickListener(this);
		enable_summary = (TextView)findViewById(R.id.enable_summary);
		enable_status = (CheckBox)findViewById(R.id.enable_status);
		enable_status.setChecked(sSharedPreferences.getBoolean(PreferenceData.PREFERENCE_KEY_NOTIFI ,true));
		if (enable_status.isChecked()) {
			enable_summary.setText(R.string.notification_preference_summary_yes);
		} else {
			enable_summary.setText(R.string.notification_preference_summary_no);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.show_accessibility_menu:
			startActivity(ACCESSIBILITY_INTENT);
			break;
		case R.id.enable_notifi_service:
			BluetoothService service = BluetoothService.getInstance();
			if (service == null) {
				return;
			}
			SharedPreferences.Editor editor = sSharedPreferences.edit();
			if (enable_status.isChecked()) {
				enable_status.setChecked(false);
				editor.putBoolean(PreferenceData.PREFERENCE_KEY_NOTIFI, false);
				enable_summary.setText(R.string.notification_preference_summary_no);
				service.stopNotificationService();
			} else {
				enable_status.setChecked(true);
				editor.putBoolean(PreferenceData.PREFERENCE_KEY_NOTIFI, true);
				enable_summary.setText(R.string.notification_preference_summary_yes);
				service.startNotificationService();
				if (!BluetoothService.isNotificationServiceActived()) {
					showAccessibilityPrompt();
				}
			}
			editor.commit();
			break;
		case R.id.select_notifi:
			startActivity(new Intent(NoticationActivity.this, SelectNotifiActivity.class));
			break;
		}
		
	}
	
	private void showAccessibilityPrompt() {
		CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(this);
		builder.setTitle(R.string.accessibility_prompt_title);
		builder.setMessage(R.string.accessibility_prompt_content);
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				startActivity(NoticationActivity.ACCESSIBILITY_INTENT);
			}
		});
		builder.create().show();
	}
}
