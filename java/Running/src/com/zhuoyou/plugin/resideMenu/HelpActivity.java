package com.zhuoyou.plugin.resideMenu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyou.plugin.cloud.NetUtils;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.Tools;
import com.zhuoyou.plugin.selfupdate.Constants;
import com.zhuoyou.plugin.selfupdate.MyHandler;
import com.zhuoyou.plugin.selfupdate.RequestAsyncTask;
import com.zhuoyou.plugin.selfupdate.SelfUpdateMain;

public class HelpActivity extends Activity implements OnClickListener{
	private RelativeLayout tvSupportType;
	private RelativeLayout tvFrequentlyQuestions;
	private RelativeLayout tvApplicationUpdate;
	private RelativeLayout tvApplicationIntroducing;
	private TextView mVersion;
	private final int APPLICATION_INTRODUING = 1;
	private String version;
	private RequestAsyncTask tast;
	private Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		mContext = this;
		version = Tools.setAppVersionInfo(this);
		initView();
		
	}
	
	private void initView() {
		TextView tv_title = (TextView) findViewById(R.id.title);
		tv_title.setText(R.string.help);
		RelativeLayout im_back = (RelativeLayout) findViewById(R.id.back);
		im_back.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		tvSupportType = (RelativeLayout) findViewById(R.id.tv_support_type);
		tvFrequentlyQuestions = (RelativeLayout) findViewById(R.id.tv_frequently_questions);
		tvApplicationUpdate = (RelativeLayout) findViewById(R.id.tv_application_update);
		tvApplicationIntroducing = (RelativeLayout) findViewById(R.id.tv_application_introducing);
		mVersion = (TextView) findViewById(R.id.tv_version);
		tvSupportType.setOnClickListener(this);
		tvFrequentlyQuestions.setOnClickListener(this);
		tvApplicationUpdate.setOnClickListener(this);
		tvApplicationIntroducing.setOnClickListener(this);
		if (!version.equals(""))
			mVersion.setText(version);
	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.tv_support_type:
			intent = new Intent(HelpActivity.this,SupportTypeActivity.class);
			startActivity(intent);
			break;
		case R.id.tv_frequently_questions:
			intent = new Intent(HelpActivity.this,FrequentlyQuestionsActivity.class);
			startActivity(intent);
			break;
		case R.id.tv_application_introducing:
			intent = new Intent(HelpActivity.this,ApplicationIntroduingActivity.class);
			startActivityForResult(intent, APPLICATION_INTRODUING);
			break;
		case R.id.tv_application_update:
			if ( NetUtils.getAPNType(mContext) == -1) {
				Toast.makeText(mContext, R.string.check_network, Toast.LENGTH_SHORT).show();
				return;
			}
			if(SelfUpdateMain.isDownloading == false){
				Toast.makeText(mContext, getResources().getString(R.string.isgoing_check_update),Toast.LENGTH_SHORT).show();
				tvApplicationUpdate.setEnabled(false);
				final Handler handler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						tvApplicationUpdate.setEnabled(true);
					}
				};
				handler.sendEmptyMessageDelayed(1, 600);
				
				MyHandler h = new MyHandler(HelpActivity.this, true);
				if (tast == null || tast.getStatus() == AsyncTask.Status.FINISHED) {
					tast = new RequestAsyncTask(HelpActivity.this, h, MyHandler.MSG_UPDATE_VIEW, Constants.APPID, Constants.CHNID);
					tast.startRun();
				}
			}
			break;
		default:
			break;
		}
		
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	switch (requestCode) {
    	case APPLICATION_INTRODUING:
    		if (resultCode == RESULT_OK)
    			finish();
    		break;
    	default:
    		break;
    	}
    }
}
