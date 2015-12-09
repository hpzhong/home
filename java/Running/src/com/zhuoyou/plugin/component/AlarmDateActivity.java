package com.zhuoyou.plugin.component;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zhuoyou.plugin.running.R;

public class AlarmDateActivity extends Activity implements OnClickListener {

	private AlarmBean mBean;
	/** program UI */
	private RelativeLayout layoutMonday;
	private RelativeLayout layoutTuesday;
	private RelativeLayout layoutWednesday;
	private RelativeLayout layoutThursday;
	private RelativeLayout layoutFriday;
	private RelativeLayout layoutSaturday;
	private RelativeLayout layoutSunday;

	private ImageView imageMonday;
	private ImageView imageTuesday;
	private ImageView imageWednesday;
	private ImageView imageThursday;
	private ImageView imageFriday;
	private ImageView imageSaturday;
	private ImageView imageSunday;
	
	private boolean monday;
	private boolean tuesday;
	private boolean wednesday;
	private boolean thursday;
	private boolean friday;
	private boolean saturday;
	private boolean sunday;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_date_activity);
		mBean = (AlarmBean) getIntent().getSerializableExtra("alarmbean");
		if(mBean == null) { finish(); }
		
		findView();
		initData();
		setImageView();
	}

	private void findView() {
		layoutMonday = (RelativeLayout) findViewById(R.id.alarm_layout_monday);
		layoutTuesday = (RelativeLayout) findViewById(R.id.alarm_layout_tuesday);
		layoutWednesday = (RelativeLayout) findViewById(R.id.alarm_layout_wednesday);
		layoutThursday = (RelativeLayout) findViewById(R.id.alarm_layout_thursday);
		layoutFriday = (RelativeLayout) findViewById(R.id.alarm_layout_friday);
		layoutSaturday = (RelativeLayout) findViewById(R.id.alarm_layout_saturday);
		layoutSunday = (RelativeLayout) findViewById(R.id.alarm_layout_sunday);

		imageMonday = (ImageView) findViewById(R.id.alarm_imageview_monday);
		imageTuesday = (ImageView) findViewById(R.id.alarm_imageview_tuesday);
		imageWednesday = (ImageView) findViewById(R.id.alarm_imageview_wednesday);
		imageThursday = (ImageView) findViewById(R.id.alarm_imageview_thursday);
		imageFriday = (ImageView) findViewById(R.id.alarm_imageview_friday);
		imageSaturday = (ImageView) findViewById(R.id.alarm_imageview_saturday);
		imageSunday = (ImageView) findViewById(R.id.alarm_imageview_sunday);

		layoutMonday.setOnClickListener(this);
		layoutTuesday.setOnClickListener(this);
		layoutWednesday.setOnClickListener(this);
		layoutThursday.setOnClickListener(this);
		layoutFriday.setOnClickListener(this);
		layoutSaturday.setOnClickListener(this);
		layoutSunday.setOnClickListener(this);
	}
	private void initData(){
		int customs = mBean.getCustomData();
		monday   = (customs %10 ) / 1  == 1 ? true:false;
		tuesday  = (customs %100 ) / 10  == 1 ? true:false;
		wednesday= (customs %1000 ) / 100  == 1 ? true:false;
		thursday = (customs %10000 ) / 1000  == 1 ? true:false;
		friday   = (customs %100000 ) / 10000  == 1 ? true:false;
		saturday = (customs %1000000 ) / 100000  == 1 ? true:false;
		sunday   = (customs %10000000 ) / 1000000  == 1 ? true:false;
	}
	
	private void setImageView(){
		if(monday){
			imageMonday.setVisibility(View.VISIBLE);
		}else{
			imageMonday.setVisibility(View.INVISIBLE);
		}
		
		if(tuesday){
			imageTuesday.setVisibility(View.VISIBLE);
		}else{
			imageTuesday.setVisibility(View.INVISIBLE);
		}
		
		if(wednesday){
			imageWednesday.setVisibility(View.VISIBLE);
		}else{
			imageWednesday.setVisibility(View.INVISIBLE);
		}
		
		if(thursday){
			imageThursday.setVisibility(View.VISIBLE);
		}else{
			imageThursday.setVisibility(View.INVISIBLE);
		}
		
		if(friday){
			imageFriday.setVisibility(View.VISIBLE);
		}else{
			imageFriday.setVisibility(View.INVISIBLE);
		}
		
		if(saturday){
			imageSaturday.setVisibility(View.VISIBLE);
		}else{
			imageSaturday.setVisibility(View.INVISIBLE);
		}
		
		if(sunday){
			imageSunday.setVisibility(View.VISIBLE);
		}else{
			imageSunday.setVisibility(View.INVISIBLE);
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
			
		case R.id.alarm_layout_monday:
			monday = !monday;
			break;
		case R.id.alarm_layout_tuesday:
			tuesday = !tuesday;
			break;
		case R.id.alarm_layout_wednesday:
			wednesday = !wednesday;
			break;
		case R.id.alarm_layout_thursday:
			thursday = !thursday;
			break;
		case R.id.alarm_layout_friday:
			friday = !friday;			
			break;
		case R.id.alarm_layout_saturday:
			saturday = !saturday;
			break;
		case R.id.alarm_layout_sunday:
			sunday = !sunday;
			break;
		default:
			break;
		}
		setImageView();
	}
	
    @Override  
    public void onBackPressed() {
    	finishActivity();
    }  
	
	private void finishActivity(){
		int res = 0;
		if(monday)   { res = res + 1 ;};
		if(tuesday)  { res = res + 10 ;};
		if(wednesday){ res = res + 100 ;};
		if(thursday) { res = res + 1000 ;};
		if(friday)   { res = res + 10000 ;};
		if(saturday) { res = res + 100000 ;};
		if(sunday)   { res = res + 1000000 ;};
		mBean.setCustomData(res);
		
		Intent intent = new Intent();
		intent.putExtra("alarmReturn", mBean);
		setResult(100, intent);
		finish();
	}
}
