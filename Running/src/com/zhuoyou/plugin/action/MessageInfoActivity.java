package com.zhuoyou.plugin.action;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.running.R;

public class MessageInfoActivity extends Activity {
	private String content;
	private TextView msgContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_info_layout);
		Intent intent = getIntent();
		content = intent.getStringExtra("msg_content");
		TextView tv_title = (TextView) findViewById(R.id.title);
		tv_title.setText(R.string.sys_info);
		RelativeLayout im_back = (RelativeLayout) findViewById(R.id.back);
		im_back.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		msgContent = (TextView) findViewById(R.id.content);
		msgContent.setText(content);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

}
