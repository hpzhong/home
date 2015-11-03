package com.zhuoyou.plugin.resideMenu;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.running.R;

public class SupportTypeActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.support_type);
		TextView tv_title = (TextView) findViewById(R.id.title);
		tv_title.setText(R.string.supported_models);
		RelativeLayout im_back = (RelativeLayout) findViewById(R.id.back);
		im_back.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}
	
}
