package com.zhuoyou.plugin.running;


import java.util.List;

import com.baidu.location.r;
import com.zhuoyou.plugin.add.TosGallery;
import com.zhuoyou.plugin.add.TosGallery.OnEndFlingListener;
import com.zhuoyou.plugin.running.HomePageFragment.StepObserver;
import com.zhuoyou.plugin.view.WheelView;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SedentaryTimeSetActivity extends Activity implements OnClickListener,OnEndFlingListener{
	private TextView tv_title;
	private ImageView im_back;
	private SedentaryDeviceItem mDevice;
	private WheelView PeriodWheelView,StartWheelViewHour,EndWheelViewHour,StartWheelViewMinute,EndWheelViewMinute;
	private String[] mPeriodTimeArray = {"30分钟", "60分钟", "90分钟", "120分钟"};
	
	private ImageView isEnableImageView;
	private TextView mPeriodTimeTextView;
	private TextView mStartTimeTextView;
	private TextView mEndTimeTextView;
	private ImageView HindPeriod;
	
	private boolean is_edit=false;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub              
		super.onCreate(savedInstanceState);
		                    
		setContentView(R.layout.sedentary_time_set_layout);
		mDevice=(SedentaryDeviceItem) getIntent().getSerializableExtra("device_info");
		if(mDevice!=null){
			Toast.makeText(this, "name="+mDevice.getDeviceName(), 1).show();
		}
		initView();
		

		}

	
	
	

	private void initView() {
		// TODO Auto-generated method stub
		//title
		tv_title = (TextView) findViewById(R.id.sedentary_title);
		tv_title.setText(R.string.sedentary_remind);
		im_back = (ImageView) findViewById(R.id.back_sedentary);
		im_back.setOnClickListener(this);
		//View
		 isEnableImageView=(ImageView) findViewById(R.id.is_enable);
		 isEnableImageView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				is_edit=true;
				mDevice.setState(!mDevice.getState());//
				isEnableImageView.setBackgroundResource(mDevice.getState()?R.drawable.warn_on:R.drawable.warn_off);
						
			}
			 
		 });
		mPeriodTimeTextView=(TextView) findViewById(R.id.period_text);
		mPeriodTimeTextView.setOnClickListener(this);
		 mStartTimeTextView=(TextView) findViewById(R.id.start_text);
		mEndTimeTextView=(TextView) findViewById(R.id.end_text);
		
		isEnableImageView.setBackgroundResource(mDevice.getState()?R.drawable.warn_on:R.drawable.warn_off);
		mPeriodTimeTextView.setText(mDevice.getTimeLag()*30+"分钟");
		mStartTimeTextView.setText(mDevice.getStartTime());
		mEndTimeTextView.setText(mDevice.getEndTime());
		
		
		//WheelView_period
		PeriodWheelView=(WheelView) findViewById(R.id.period_select);
		PeriodWheelView.setAdapter(new WheelTextAdapter(mPeriodTimeArray));
		PeriodWheelView.setOnEndFlingListener(this);
		
		//start
		StartWheelViewHour=(WheelView) findViewById(R.id.start_select_hour);
		StartWheelViewHour.setAdapter(new WheelTextAdapter(getData(24)));
		StartWheelViewHour.setOnEndFlingListener(this);
		
		StartWheelViewMinute=(WheelView) findViewById(R.id.start_select_minute);
		StartWheelViewMinute.setAdapter(new WheelTextAdapter(getData(60)));
		StartWheelViewMinute.setOnEndFlingListener(this);
		
		//end
		EndWheelViewHour=(WheelView) findViewById(R.id.end_select_hour);
		EndWheelViewHour.setAdapter(new WheelTextAdapter(getData(24)));
		EndWheelViewHour.setOnEndFlingListener(this);
		
		EndWheelViewMinute=(WheelView) findViewById(R.id.end_select_minute);
		EndWheelViewMinute.setAdapter(new WheelTextAdapter(getData(60)));
		EndWheelViewMinute.setOnEndFlingListener(this);
	}

	@Override
	public void onClick(View v){
		switch(v.getId()){
		case R.id.back_sedentary:
			finish();
			break;
		case R.id.ensure_period:
			mPeriodTimeTextView.setText(mPeriodTimeArray[PeriodWheelView.getSelectedItemPosition()]);
			this.findViewById(R.id.period_layout).setVisibility(View.GONE);
			break;
		case R.id.end_text:
			this.findViewById(R.id.end_layout).setVisibility(View.VISIBLE);
			break;
		case R.id.hine_period:
			
			break;
		case R.id.hine_end:
			
			break;
		case R.id.period_text:
			this.findViewById(R.id.period_layout).setVisibility(View.VISIBLE);
			break;
		case R.id.hine_start:
//			this.findViewById(R.id.start_layout).setVisibility(View.GONE);
			break;
		case R.id.start_text:
			this.findViewById(R.id.start_layout).setVisibility(View.VISIBLE);
			break;
		case R.id.ensure_start:
			int startHour=StartWheelViewHour.getSelectedItemPosition();
			int startMimute=StartWheelViewMinute.getSelectedItemPosition();
			if(startMimute<10){
				mStartTimeTextView.setText(startHour+":0"+startMimute);	
			}else{
				mStartTimeTextView.setText(startHour+":"+startMimute);	
			}
			this.findViewById(R.id.start_layout).setVisibility(View.GONE);
			break;
		case R.id.ensure_end:
			int endHour=EndWheelViewHour.getSelectedItemPosition();
			int endMimute=EndWheelViewMinute.getSelectedItemPosition();
			if(endMimute<10){
				mEndTimeTextView.setText(endHour+":0"+endMimute);	
			}else{
				mEndTimeTextView.setText(endHour+":"+endMimute);	
			}
			this.findViewById(R.id.end_layout).setVisibility(View.GONE);
			break;
		case R.id.ensure_set:
			finish();
			break;
		}
	}
	
	@Override
	public void onEndFling(TosGallery v) {
//		switch(v.getId()){
//	}
		is_edit=true;

		
	}
	
	private String[] getData(int length) {
		String[] temp = new String[length];;
		for (int i = 0; i < length; i++) {
			temp[i] = Integer.toString(i);
		}
		return temp;
	}
private class WheelTextAdapter extends BaseAdapter {
		
		String[] mData = {};
		int mHeight = 50;
		
		public WheelTextAdapter(String[] String) {
			mData = String;
			mHeight = (int) Tools.dip2px(SedentaryTimeSetActivity.this, mHeight);
		}

		@Override
		public int getCount() {
            return (null != mData) ? mData.length : 0;
		}

		@Override
		public Object getItem(int arg0) {
			return mData[arg0];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
            TextView txtView = null;
			if (null == convertView) {
                convertView = new TextView(SedentaryTimeSetActivity.this);
                convertView.setLayoutParams(new TosGallery.LayoutParams(-1, mHeight));//With Hight
                txtView = (TextView) convertView;
                txtView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
                txtView.setTextColor(Color.BLACK);
                txtView.setGravity(Gravity.CENTER);
			}
            if (null == txtView) {
                txtView = (TextView) convertView;
            }

            txtView.setText(mData[position]);
			return convertView;
		}
	}


}
