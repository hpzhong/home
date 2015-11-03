package com.zhuoyou.plugin.running;

import java.util.Calendar;



import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WaterIntakeActivity extends Activity implements OnClickListener {
	private ColorLineView mColorLine;
	private ImageView mAddButton;
	private ImageView  mMinusButton;
	private ImageView mImage;
	private TextView mOpenWaterIntakeTextView;
	private TextView mTitle;
	private Boolean mEnable;
	private WaterIntakeLinearLayout mWaterLayout;
	private WavesAnimView mWavesVies;
	private SharedPreferences mSharedPres;
	private int i = 0;
	private RelativeLayout im_back;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mSharedPres = getSharedPreferences("TestResult", Context.MODE_WORLD_WRITEABLE);
		i = mSharedPres.getInt("water_num", 0);
		mEnable = mSharedPres.getBoolean("warn_enable", false);
	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.water_intake_view);
		mTitle=(TextView)findViewById(R.id.title);
		mTitle.setText(R.string.water_intake_title);
		mColorLine = (ColorLineView) findViewById(R.id.water_intake_view);
		mColorLine.setNumber(i);
		mWavesVies = (WavesAnimView) findViewById(R.id.mVideoView);
		mWavesVies.setStartPointY(i);
		mWaterLayout = (WaterIntakeLinearLayout) findViewById(R.id.water_intake_number);
		mWaterLayout.setWaterNumber(i);
		mAddButton = (ImageView) findViewById(R.id.add_water_intake);
		mMinusButton = (ImageView) findViewById(R.id.minus_water_intake);
		mAddButton.setOnClickListener(this);
		mMinusButton.setOnClickListener(this);
		mOpenWaterIntakeTextView=(TextView)findViewById(R.id.open_water_intake);
		mImage = (ImageView) findViewById(R.id.warn_enable);
		if(mEnable){
			mOpenWaterIntakeTextView.setText(R.string.open_water_intake);
			mImage.setImageResource(R.drawable.warn_on);
		}else{
			mImage.setImageResource(R.drawable.warn_off);
			mOpenWaterIntakeTextView.setText(R.string.close_water_intake);
		}
		mImage.setOnClickListener(this);
		
		im_back = (RelativeLayout) findViewById(R.id.back);
		im_back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.add_water_intake:
			if(i>=0 && i<99){
				i++;
			}
			mColorLine.setNumber(i);
			mWaterLayout.setWaterNumber(i);
			mWavesVies.setStartPointY(i);
			break;
		case R.id.minus_water_intake:
			if(i > 0){
				i--;
			}
			mColorLine.setNumber(i);
			mWaterLayout.setWaterNumber(i);
			mWavesVies.setStartPointY(i);
			break;
		case R.id.warn_enable:
			mEnable = !mEnable;
			openWater_Warn(mEnable);
			updateOnOffImageView(mImage,mEnable);
		}
		SharedPreferences.Editor editor  = mSharedPres.edit();
		editor.putInt("water_num", i);
		editor.putBoolean("warn_enable", mEnable);
		editor.commit();
	}

	private void openWater_Warn(Boolean mEnable2) {
		// TODO Auto-generated method stub
		
		Intent intent = new Intent("Drink_Water_Warn");
		PendingIntent pendIntent = PendingIntent.
				getBroadcast(getApplicationContext(),
						0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		 //long firstime=SystemClock.elapsedRealtime();
		 long current = System.currentTimeMillis();
		 Log.v("renjing", "firstime"+current);
		 AlarmManager am=(AlarmManager)getSystemService(Context.ALARM_SERVICE);

		 if(mEnable2 &&WaterIntakeUtils.isInWarnTime(getApplicationContext(), current)){			 
			 am.setRepeating(AlarmManager.RTC_WAKEUP,
					 System.currentTimeMillis()+3600*1000, 3600*1000, pendIntent);//start interval	
			 
		 }else{
			 am.cancel(pendIntent);
			 if(mEnable){
				 Toast.makeText(this, "没有在指定时间内设置，设置失败", Toast.LENGTH_SHORT).show();
			 }
			 mEnable = false;
		 } 
		 	 
	}

	private void updateOnOffImageView(ImageView mImage2, Boolean mEnable2) {
		// TODO Auto-generated method stub
		if(mEnable2){
			mImage2.setImageResource(R.drawable.warn_on);
			mOpenWaterIntakeTextView.setText(R.string.open_water_intake);
		}else{
			mImage2.setImageResource(R.drawable.warn_off);
			mOpenWaterIntakeTextView.setText(R.string.close_water_intake);
		}
	}

//	public  boolean isInWarnTime(Context context, long paramLong) {
//		String str1 = getTimeString(context, paramLong);
//
//		int i = 8;
//		int j = 0;
//		int k = 22;
//		int m = 0;
//		int n = 1;
//		if ((i >= k) && (j >= m)){
//			n = 0;
//		}
//		String str2 = "0800";
//		String str3 = "2100";
//		if (n != 0) {
//					
//			if ((str1.compareTo(str2) >= 0) && (str1.compareTo(str3) <= 0)){
//						return true;
//			}
//		}
//		return false;
//	}
	
//	public static String getTimeString(Context paramContext, long paramLong)
//	  {
//		String str="";
//		Calendar mCalendar=Calendar.getInstance();
//		mCalendar.setTimeInMillis(paramLong);
//	    if (android.text.format.DateFormat.is24HourFormat(paramContext))
//	    {
//	    	int i = mCalendar.get(Calendar.HOUR_OF_DAY);
//	    	if(i < 10){
//	    		str=str+"0";
//	    	}
//	    	str = ""+i;
//	    }else{
//	    	int i=mCalendar.get(Calendar.HOUR);
//	    	if(mCalendar.get(Calendar.AM_PM)==0)
//	    	{
//	    		if(i<10)
//	    		{
//	    			str=str+"0";
//	    		}
//	    	}else{
//	    		i=i+12;
//	    	}
//	    	str=str+i;
//	    }
//	    str=str+mCalendar.get(Calendar.MINUTE);
//	    return str;
//	  }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case android.R.id.home:
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
