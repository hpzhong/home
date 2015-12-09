package com.zhuoyou.plugin.gps;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.zhuoyou.plugin.custom.CustomAlertDialog;
import com.zhuoyou.plugin.running.R;

public class FirstGpsActivity extends Activity {
	Button ignoreBu;
	Resources res;
	SensorManager mSensorManager = null;
	SensorEventListener sel = null;
	Sensor mAccel = null;
	private boolean screen_off=false;
	private boolean sensor_isuse=false;
	private SharedPreferences sharepreference;
    private Editor editor;
    String from="";
    

	private float lastX=0,lastY=0,lastZ=0;//记录上一次震动位置
	private float currentDistance,totalShake;//记录震动幅度
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.first_gps);
		res = getResources();
		initFilter();
		
		from=getIntent().getStringExtra("from");
		
		ignoreBu = (Button)findViewById(R.id.test_ignore);
		ignoreBu.setClickable(true);
		mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sharepreference = this.getSharedPreferences("gaode_location_info", Context.MODE_PRIVATE);
		
		ignoreBu.setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				
				CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(FirstGpsActivity.this);
				builder.setTitle(R.string.test_ensure);
				builder.setMessage(R.string.test_message);
				builder.setPositiveButton(R.string.test_postive, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						editor = sharepreference.edit();
						editor.putBoolean("is_first_gps",false );		
						editor.commit();
						
						if(from.equals("Main")){
						Intent mIntent=new Intent(FirstGpsActivity.this,GaoDeMapActivity.class);
						startActivity(mIntent);
						FirstGpsActivity.this.finish();
						}else if(from.equals("DayPedometerActivity")){
							FirstGpsActivity.this.finish();
						}
						from="";
						
						
					}
				});
				builder.setNegativeButton(R.string.test_cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.setCancelable(true);
				builder.create().show();	
			}
		});
		
		sel=new SensorEventListener(){ 
			@Override
			public void onSensorChanged(SensorEvent sensorEvent) {
            //	if(se.sensor==mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)){
            	if(screen_off){
                    //获取当前震动位置
                    float x = sensorEvent.values[0]; 
                    float y = sensorEvent.values[1]; 
                    float z = sensorEvent.values[2];                 
 
                    currentDistance=(Math.abs(x-lastX)+Math.abs(y-lastY)+Math.abs(z-lastZ));
                    Log.i("zhaojunhui","distance is ="+currentDistance);
                    if(currentDistance>=20){
                    	sensor_isuse=true;
                    }

                  }
            } 
            
            public void onAccuracyChanged(Sensor arg0, int arg1) { 
            	
            }

			
        };
        mSensorManager.registerListener(sel, mAccel,SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	private void initFilter(){
    	IntentFilter mGATTFilter = new IntentFilter();
        mGATTFilter.addAction("android.intent.action.SCREEN_ON");
        mGATTFilter.addAction("android.intent.action.SCREEN_OFF");
        registerReceiver(mBroadcastReceiver, mGATTFilter);
    }
    
    //接收广播
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if ("android.intent.action.SCREEN_ON".equals(action)) {
				 screen_off=false;
				 editor = sharepreference.edit();
				 editor.putBoolean("is_first_gps",false );		
				 editor.commit();
				 
				 Intent mIntent =new Intent(FirstGpsActivity.this,ResultGpsActivity.class);
				 mIntent.putExtra("sensor_isuse", sensor_isuse);
				 mIntent.putExtra("from", from);
				 startActivity(mIntent);
				 FirstGpsActivity.this.finish();
			} else if("android.intent.action.SCREEN_OFF".equals(action)){
				screen_off=true;
			}
		}
	};

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mSensorManager.unregisterListener(sel);
		unregisterReceiver(mBroadcastReceiver);
	}

}
