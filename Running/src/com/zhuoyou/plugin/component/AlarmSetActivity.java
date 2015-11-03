package com.zhuoyou.plugin.component;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;


import com.zhuoyou.plugin.add.TosAdapterView;
import com.zhuoyou.plugin.add.TosAdapterView.OnItemSelectedListener;
import com.zhuoyou.plugin.info.WheelTextAdapter;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.view.WheelView;

public class AlarmSetActivity extends Activity implements OnClickListener {

	private final String tag = "AlarmSetActivity";
	private final int requestCode = 0x10001;
	/** program UI */
	private Button bt_brainOff;
	private Button bt_alarmOnce;
	private Button bt_alarmEveryday;
	private Button bt_alarmWorkdays;
	private Button bt_alarmCustoms;
	private WheelView wv_hour;
	private WheelView wv_min;
	private WheelTextAdapter hourAdaptor;
	private WheelTextAdapter minAdaptor;
	
	private AlarmBean mBean;
	private int selectPostion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(tag, "onCreate");
		setContentView(R.layout.alarm_set_activity);
		findView();
		initView();
	}

	private void findView() {
		mBean = (AlarmBean)getIntent().getSerializableExtra("alarmbean");
		if(mBean == null ){	mBean = new AlarmBean(); }
		
		bt_brainOff = (Button) findViewById(R.id.button_alarm_switch);
		bt_alarmOnce = (Button) findViewById(R.id.alarm_once);
		bt_alarmEveryday = (Button) findViewById(R.id.alarm_everyday);
		bt_alarmWorkdays = (Button) findViewById(R.id.alarm_workdays);
		bt_alarmCustoms = (Button) findViewById(R.id.alarm_cumtoms);
		wv_hour = (WheelView) findViewById(R.id.hour_select);
		wv_min = (WheelView) findViewById(R.id.min_select);
		
		bt_brainOff.setOnClickListener(this);
		bt_alarmOnce.setOnClickListener(this);
		bt_alarmEveryday.setOnClickListener(this);
		bt_alarmWorkdays.setOnClickListener(this);
		bt_alarmCustoms.setOnClickListener(this);
		
		hourAdaptor = new WheelTextAdapter(this, getHourArray(), 30);
		minAdaptor = new WheelTextAdapter(this, getMinArray(), 30);
		wv_hour.setAdapter(hourAdaptor);
		wv_min.setAdapter(minAdaptor);
		
		wv_hour.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(TosAdapterView<?> parent, View view,	int position, long id){
				selectPostion = wv_hour.getSelectedItemPosition();
				hourAdaptor.SetSelecttion(selectPostion);
				hourAdaptor.notifyDataSetChanged();
				mBean.setHour(position);				
			}
			@Override
			public void onNothingSelected(TosAdapterView<?> parent) {}
		});
		
		wv_min.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(TosAdapterView<?> parent, View view,	int position, long id){
				selectPostion = wv_min.getSelectedItemPosition();
				minAdaptor.SetSelecttion(selectPostion);
				minAdaptor.notifyDataSetChanged();
				mBean.setMin(position);				
			}
			@Override
			public void onNothingSelected(TosAdapterView<?> parent) {}
		});
		
	}
	
	private void initView(){
		wv_hour.setSelection(mBean.getHour());
		wv_min.setSelection(mBean.getMin());
		
		if(mBean.isBrain()){
			bt_brainOff.setBackgroundResource(R.drawable.alarm_button_openon);
		}else{
			bt_brainOff.setBackgroundResource(R.drawable.alarm_button_closeoff);
		}
		
		bt_alarmOnce.setBackgroundResource(R.drawable.alarm_button_unselected);
		bt_alarmEveryday.setBackgroundResource(R.drawable.alarm_button_unselected);
		bt_alarmWorkdays.setBackgroundResource(R.drawable.alarm_button_unselected);
		bt_alarmCustoms.setBackgroundResource(R.drawable.alarm_button_unselected);
		int resColor = Color.parseColor("#c7c7c7");
		bt_alarmOnce.setTextColor(resColor);
		bt_alarmEveryday.setTextColor(resColor);
		bt_alarmWorkdays.setTextColor(resColor);
		bt_alarmCustoms.setTextColor(resColor);
		
		int type =  mBean.getOpenType();
		if(type == 0){
			bt_alarmOnce.setTextColor(Color.WHITE);
			bt_alarmOnce.setBackgroundResource(R.drawable.alarm_button_selected);
		}else if(type == 1){
			bt_alarmEveryday.setTextColor(Color.WHITE);
			bt_alarmEveryday.setBackgroundResource(R.drawable.alarm_button_selected);
		}else if(type == 2){
			bt_alarmWorkdays.setTextColor(Color.WHITE);
			bt_alarmWorkdays.setBackgroundResource(R.drawable.alarm_button_selected);
		}else if(type == 3){
			bt_alarmCustoms.setTextColor(Color.WHITE);
			bt_alarmCustoms.setBackgroundResource(R.drawable.alarm_button_selected);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.back:
			finishActivity();
			break;
		case R.id.button_alarm_switch:
			mBean.setBrain(!mBean.isBrain());
			break;
		case R.id.alarm_once:
			mBean.setOpenType(0);
			break;
		case R.id.alarm_everyday:
			mBean.setOpenType(1);
			break;
		case R.id.alarm_workdays:
			mBean.setOpenType(2);
			break;
		case R.id.alarm_cumtoms:
			
			mBean.setOpenType(3);
			Intent intent = new Intent(this,AlarmDateActivity.class);
			intent.putExtra("alarmbean", mBean);
			startActivityForResult(intent, requestCode);
			
			break;
		default:
			break;
		}
		initView();
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(tag, "onActivityResult");
		if(requestCode == this.requestCode){
			mBean = (AlarmBean)data.getSerializableExtra("alarmReturn");
			initView();
		}
	}
	
	@Override  
    public void onBackPressed() {
    	finishActivity();
    }  

	private void finishActivity(){
		Intent intent = new Intent();
		intent.putExtra("alarmReturn", mBean);
		setResult(100, intent);
		finish();
	}
	
	
	private String[] getHourArray() {
		String[] temp = getResources().getStringArray(R.array.hour);
		return temp;
	}

	private String[] getMinArray() {
		String[] temp = getResources().getStringArray(R.array.minute);
		return temp;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(tag, "onResume");
	}
}
