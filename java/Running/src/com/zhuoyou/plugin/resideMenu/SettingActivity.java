package com.zhuoyou.plugin.resideMenu;

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

import com.zhuoyou.plugin.bluetooth.attach.NoticationActivity;
import com.zhuoyou.plugin.bluetooth.data.PreferenceData;
import com.zhuoyou.plugin.bluetooth.service.BluetoothService;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;

public class SettingActivity extends Activity implements OnClickListener {
	private LinearLayout show_accessibility_menu;
	private LinearLayout show_connection_status;
	private TextView show_enable_summary;
	private CheckBox show_enable_status;
	private LinearLayout always_forward;
	private TextView forward_enable_summary;
	private CheckBox forward_enable_status;
    private Context sContext = RunningApp.getInstance().getApplicationContext();
    private SharedPreferences sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(sContext);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_activity_layout);
		TextView tv_title = (TextView) findViewById(R.id.title);
		tv_title.setText(R.string.setting);
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
		show_connection_status = (LinearLayout)findViewById(R.id.show_connection_status);
		show_connection_status.setOnClickListener(this);
		show_enable_summary = (TextView)findViewById(R.id.show_enable_summary);
		show_enable_status = (CheckBox)findViewById(R.id.show_enable_status);
		show_enable_status.setChecked(sSharedPreferences.getBoolean(PreferenceData.PREFERENCE_KEY_SHOW_CONNECTION_STATUS ,true));
		if (show_enable_status.isChecked()) {
			show_enable_summary.setText(R.string.show_connect_status_preference_summary_yes);
		} else {
			show_enable_summary.setText(R.string.show_connect_status_preference_summary_no);
		}
		always_forward = (LinearLayout)findViewById(R.id.always_forward);
		always_forward.setOnClickListener(this);
		forward_enable_summary = (TextView)findViewById(R.id.forward_enable_summary);
		forward_enable_status = (CheckBox)findViewById(R.id.forward_enable_status);
		forward_enable_status.setChecked(sSharedPreferences.getBoolean(PreferenceData.PREFERENCE_KEY_ALWAYS_FORWARD ,true));
		if (forward_enable_status.isChecked()) {
			forward_enable_summary.setText(R.string.always_forward_preference_summary);
		} else {
			forward_enable_summary.setText(R.string.lock_forward_preference_summary);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.show_accessibility_menu:
			startActivity(NoticationActivity.ACCESSIBILITY_INTENT);
			break;
		case R.id.show_connection_status:
			BluetoothService service = BluetoothService.getInstance();
            if (service != null) {
    			SharedPreferences.Editor editor = sSharedPreferences.edit();
    			if (show_enable_status.isChecked()) {
    				show_enable_status.setChecked(false);
    				editor.putBoolean(PreferenceData.PREFERENCE_KEY_SHOW_CONNECTION_STATUS, false);
    				show_enable_summary.setText(R.string.show_connect_status_preference_summary_no);
    			} else {
    				show_enable_status.setChecked(true);
    				editor.putBoolean(PreferenceData.PREFERENCE_KEY_SHOW_CONNECTION_STATUS, true);
    				show_enable_summary.setText(R.string.show_connect_status_preference_summary_yes);
    			}
    			editor.commit();
                service.updateConnectionStatus(false,0,0,0);
            }
			break;
		case R.id.always_forward:
			SharedPreferences.Editor editor = sSharedPreferences.edit();
			if (forward_enable_status.isChecked()) {
				forward_enable_status.setChecked(false);
				editor.putBoolean(PreferenceData.PREFERENCE_KEY_ALWAYS_FORWARD, false);
				forward_enable_summary.setText(R.string.lock_forward_preference_summary);
			} else {
				forward_enable_status.setChecked(true);
				editor.putBoolean(PreferenceData.PREFERENCE_KEY_ALWAYS_FORWARD, true);
				forward_enable_summary.setText(R.string.always_forward_preference_summary);
			}
			editor.commit();
			break;
		}
	}
}
