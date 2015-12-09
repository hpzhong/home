package com.zhuoyou.plugin.gps;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhuoyou.plugin.running.R;

public class ResultGpsActivity extends Activity {
	private boolean step_sensor;
	private boolean isHaveGps;
	private String from;
	Button mButton;
	TextView mTextView;
	TextView modelText;
	TextView andrText;
	TextView errorText;
	ImageView mImage;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		 setContentView(R.layout.gps_result);
		 Intent mItent=this.getIntent();
	     step_sensor=mItent.getBooleanExtra("sensor_isuse", false);
	     from=mItent.getStringExtra("from");
	     isHaveGps=hasGPSDevice(this);
	     mButton = (Button) findViewById(R.id.use_begin);
	     mTextView = (TextView) findViewById(R.id.result_test);
	     modelText = (TextView) findViewById(R.id.mob_model);
	     andrText = (TextView) findViewById(R.id.andr_version);
	     errorText = (TextView) findViewById(R.id.error_reason);
	     mImage = (ImageView) findViewById(R.id.gps_result);
	     modelText.setText(getResources().getString(R.string.mobe_model)+android.os.Build.MODEL);
    	 andrText.setText(getResources().getString(R.string.android_version)+android.os.Build.VERSION.RELEASE);
	   
    	 if(step_sensor && isHaveGps){
    		 Log.i("zhaojunhui", 1+" step_sensor="+step_sensor+";isHaveGps="+isHaveGps);
	    	 mTextView.setText(getResources().getString(R.string.result_ok));
	    	 mImage.setImageDrawable(getResources().getDrawable(R.drawable.gps_support));
	    	 errorText.setVisibility(View.GONE);
	     }else if(!step_sensor && isHaveGps){
	    	 Log.i("zhaojunhui", 2+" step_sensor="+step_sensor+";isHaveGps="+isHaveGps);
	    	 mTextView.setText(getResources().getString(R.string.result_some));
	    	 mImage.setImageDrawable(getResources().getDrawable(R.drawable.gps_no_support));
	    	 errorText.setVisibility(View.VISIBLE);
	    	 errorText.setText(getResources().getString(R.string.error_steps));
	     }else if(!isHaveGps && step_sensor){
	    	 Log.i("zhaojunhui", 3+" step_sensor="+step_sensor+";isHaveGps="+isHaveGps);
	    	 mTextView.setText(getResources().getString(R.string.result_some));
	    	 mImage.setImageDrawable(getResources().getDrawable(R.drawable.gps_no_support));
	    	 errorText.setVisibility(View.VISIBLE);
	    	 errorText.setText(getResources().getString(R.string.error_gps));
	     }else if(!isHaveGps && !step_sensor){
	    	 Log.i("zhaojunhui", 4+" step_sensor="+step_sensor+";isHaveGps="+isHaveGps);
	    	 mTextView.setText(getResources().getString(R.string.result_none));
	    	 mImage.setImageDrawable(getResources().getDrawable(R.drawable.gps_no_support));
	    	 errorText.setVisibility(View.VISIBLE);
	    	 errorText.setText(getResources().getString(R.string.error_all));
	     }
	     mButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(from.equals("Main")){
				Intent mIntent = new Intent(ResultGpsActivity.this,GaoDeMapActivity.class);
				startActivity(mIntent);
				ResultGpsActivity.this.finish();
				}else{
					ResultGpsActivity.this.finish();
				}
			}
		});
	}
	
	//判断手机有没有GPS模块
		public boolean hasGPSDevice(Context context){
	        final LocationManager mgr = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
	        if ( mgr == null )
	            return false;
	        final List<String> providers = mgr.getAllProviders();
	        if ( providers == null )
	            return false;
	        return providers.contains(LocationManager.GPS_PROVIDER);
	    }
		
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
}
