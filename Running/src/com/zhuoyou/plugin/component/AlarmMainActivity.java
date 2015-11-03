package com.zhuoyou.plugin.component;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.zhuoyou.plugin.ble.BleManagerService;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.Tools;

public class AlarmMainActivity extends Activity implements OnClickListener {
	
	private DecimalFormat intFormat = new DecimalFormat("#00");
	private final int requestCode = 0x10000;
	/** program UI */
	private TextView tv_AlarmTime0;
	private TextView tv_AlarmTime1;
	private TextView tv_AlarmTime2;
	private TextView tv_AlarmDate0;
	private TextView tv_AlarmDate1;
	private TextView tv_AlarmDate2;
	private TextView tv_AlarmNofy0;
	private TextView tv_AlarmNofy1;
	private TextView tv_AlarmNofy2;
	private Button bt_AlarmOff0;
	private Button bt_AlarmOff1;
	private Button bt_AlarmOff2;
	
	private AlarmBean mBean0;
	private AlarmBean mBean1;
	private AlarmBean mBean2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_activity);
		findView();
		getData();
		setView();
	}

	private void findView() {
		tv_AlarmTime0 = (TextView) findViewById(R.id.text_alarm0_time);
		tv_AlarmTime1 = (TextView) findViewById(R.id.text_alarm1_time);
		tv_AlarmTime2 = (TextView) findViewById(R.id.text_alarm2_time);
		tv_AlarmDate0 = (TextView) findViewById(R.id.text_alarm0_date);
		tv_AlarmDate1 = (TextView) findViewById(R.id.text_alarm1_date);
		tv_AlarmDate2 = (TextView) findViewById(R.id.text_alarm2_date);
		tv_AlarmNofy0 = (TextView) findViewById(R.id.text_alarm0_notify);
		tv_AlarmNofy1 = (TextView) findViewById(R.id.text_alarm1_notify);
		tv_AlarmNofy2 = (TextView) findViewById(R.id.text_alarm2_notify);
		bt_AlarmOff0 = (Button) findViewById(R.id.button_alarm0_off);
		bt_AlarmOff1 = (Button) findViewById(R.id.button_alarm1_off);
		bt_AlarmOff2 = (Button) findViewById(R.id.button_alarm2_off);
	}
	
	private void getData(){
		mBean0 = new AlarmBean();
		mBean0.setId(0);
		mBean1 = new AlarmBean();
		mBean1.setId(1);
		mBean2 = new AlarmBean();
		mBean2.setId(2);
		
		String alarmSt = Tools.getAlarmBrain();
//		123456789012345678901
//		0000|0|0|0|0|0011111|  0000|1|0|0|0|0011111|  0000|2|0|1|0|0011111|
		Log.i("hepenghui","alarmSt:"+alarmSt );
		Log.i("hepenghui","alarmSt:"+alarmSt.length() );
		if(alarmSt.length() == 63){
			int index1 = 63 / 3 * 1;
			int index2 = 63 / 3 * 2;
			mBean0.setHour(Integer.valueOf(alarmSt.substring(0, 2)));
			mBean1.setHour(Integer.valueOf(alarmSt.substring(0 +index1 , 2+index1)));
			mBean2.setHour(Integer.valueOf(alarmSt.substring(0+index2, 2+index2)));
			
			mBean0.setMin(Integer.valueOf(alarmSt.substring(2, 4)));
			mBean1.setMin(Integer.valueOf(alarmSt.substring(2+index1, 4+index1)));
			mBean2.setMin(Integer.valueOf(alarmSt.substring(2+index2, 4+index2)));
			
			
			mBean0.setBrain( alarmSt.substring(7, 8).equals("1") ? true:false );
			mBean1.setBrain( alarmSt.substring(7+index1, 8+index1).equals("1") ? true:false );
			mBean2.setBrain( alarmSt.substring(7+index2, 8+index2).equals("1") ? true:false );
			
			
			mBean0.setOpen( alarmSt.substring(9, 10).equals("1") ? true:false );
			mBean1.setOpen( alarmSt.substring(9+index1, 10+index1).equals("1") ? true:false );
			mBean2.setOpen( alarmSt.substring(9+index2, 10+index2).equals("1") ? true:false );
			
			
			mBean0.setOpenType(Integer.valueOf(alarmSt.substring(11, 12)));
			mBean1.setOpenType(Integer.valueOf(alarmSt.substring(11+index1, 12+index1)));
			mBean2.setOpenType(Integer.valueOf(alarmSt.substring(11+index2, 12+index2)));
			
			
			mBean0.setCustomData(Integer.valueOf(alarmSt.substring(13, 20)));
			mBean1.setCustomData(Integer.valueOf(alarmSt.substring(13+index1, 20+index1)));
			mBean2.setCustomData(Integer.valueOf(alarmSt.substring(13+index2, 20+index2)));
		}
	}
	
	private void setView(){
		tv_AlarmTime0.setText(intFormat.format(mBean0.getHour())+":"+intFormat.format(mBean0.getMin()));
		tv_AlarmTime1.setText(intFormat.format(mBean1.getHour())+":"+intFormat.format(mBean1.getMin()));
		tv_AlarmTime2.setText(intFormat.format(mBean2.getHour())+":"+intFormat.format(mBean2.getMin()));
		
		tv_AlarmDate0.setText(getText(mBean0));
		tv_AlarmDate1.setText(getText(mBean1));
		tv_AlarmDate2.setText(getText(mBean2));
		
		if(mBean0.isBrain()){
			tv_AlarmNofy0.setVisibility(View.VISIBLE);
		}else{
			tv_AlarmNofy0.setVisibility(View.INVISIBLE);
		}
		
		if(mBean1.isBrain()){
			tv_AlarmNofy1.setVisibility(View.VISIBLE);
		}else{
			tv_AlarmNofy1.setVisibility(View.INVISIBLE);
		}
		
		if(mBean2.isBrain()){
			tv_AlarmNofy2.setVisibility(View.VISIBLE);
		}else{
			tv_AlarmNofy2.setVisibility(View.INVISIBLE);
		}
		
		if(mBean0.isOpen()){
			bt_AlarmOff0.setBackgroundResource(R.drawable.alarm_button_openon);
		}else{
			bt_AlarmOff0.setBackgroundResource(R.drawable.alarm_button_closeoff);
		}
		
		if(mBean1.isOpen()){
			bt_AlarmOff1.setBackgroundResource(R.drawable.alarm_button_openon);
		}else{
			bt_AlarmOff1.setBackgroundResource(R.drawable.alarm_button_closeoff);
		}
		
		if(mBean2.isOpen()){
			bt_AlarmOff2.setBackgroundResource(R.drawable.alarm_button_openon);
		}else{
			bt_AlarmOff2.setBackgroundResource(R.drawable.alarm_button_closeoff);
		}
		
	}
	
	private int getText(AlarmBean bean){
		int type = bean.getOpenType();
		if(type == 0){
			return R.string.alarm_once;
		}else if(type == 1){
			return R.string.alarm_everyday;
		}else if(type == 2){
			return R.string.alarm_workdays;
		}else{
			return R.string.alarm_customs;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.back:
			finishActivity();
			break;
		case R.id.button_alarm0_off:
			mBean0.setOpen(!mBean0.isOpen());
			broadAlarmInfo(mBean0.toString());
			break;
		case R.id.button_alarm1_off:
			mBean1.setOpen(!mBean1.isOpen());
			broadAlarmInfo(mBean1.toString());
			break;
		case R.id.button_alarm2_off:
			mBean2.setOpen(!mBean2.isOpen());
			broadAlarmInfo(mBean2.toString());
			break;
			
		case R.id.layout_alarm_0:
			Intent intent0 = new Intent(this,AlarmSetActivity.class);
			intent0.putExtra("alarmbean", mBean0);
			startActivityForResult(intent0, requestCode);
			break;
		case R.id.layout_alarm_1:
			Intent intent1 = new Intent(this,AlarmSetActivity.class);
			intent1.putExtra("alarmbean", mBean1);
			startActivityForResult(intent1, requestCode);
			break;
		case R.id.layout_alarm_2:
			Intent intent2 = new Intent(this,AlarmSetActivity.class);
			intent2.putExtra("alarmbean", mBean2);
			startActivityForResult(intent2, requestCode);
			break;
		default:
			break;
		}
		setView();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == this.requestCode){
			AlarmBean mBean = (AlarmBean)data.getSerializableExtra("alarmReturn");
			int id = mBean.getId();
			if(id == 0){
				mBean0 = mBean;
			}else if(id == 1){
				mBean1 = mBean;
			}else if(id == 2){
				mBean2 = mBean;
			}
			setView();
			broadAlarmInfo(mBean.toString());
		}
	}
	
	private void broadAlarmInfo(String info){
		Intent bluetoothBroadIntent = new Intent(BleManagerService.ACTION_UPDATE_ALARM_INFO);
		bluetoothBroadIntent.putExtra("alarmInfo", info);
		sendBroadcast(bluetoothBroadIntent);
		
		Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
		intent.putExtra("plugin_cmd", 0x95);
		intent.putExtra("plugin_content", info);
		sendBroadcast(intent);
		
	}
	
	@Override  
    public void onBackPressed() {
    	finishActivity();
	}
	
	private void finishActivity(){
		Tools.saveAlarmBrain(mBean0.saveShareP()+mBean1.saveShareP()+mBean2.saveShareP());
		finish();
	}
}
