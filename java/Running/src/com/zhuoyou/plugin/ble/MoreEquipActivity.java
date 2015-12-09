package com.zhuoyou.plugin.ble;

import com.zhuoyou.plugin.running.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MoreEquipActivity extends Activity implements OnClickListener{

	private Button mFindMore;
	private ImageView mBack;
	private TextView mTitle;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.more_equip_activity);
		
		mFindMore=(Button)findViewById(R.id.find_more_equip);
		mFindMore.setOnClickListener(this);

		initView();
	}
	private void initView() {

		mTitle=(TextView)findViewById(R.id.title);
		mTitle.setText(R.string.more_equip);
		mBack=(ImageView) findViewById(R.id.back_m);
		mBack.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
	
		switch(v.getId()){
		case R.id.find_more_equip:

		if(BindBleDeviceActivity.instance!=null){
			BindBleDeviceActivity.instance.finish();
		}
			Intent intent_more = new Intent("android.settings.BLUETOOTH_SETTINGS");
			startActivity(intent_more);
			finish();
			break;
		case R.id.back_m:
			finish();
			break;
		}
	}
}
