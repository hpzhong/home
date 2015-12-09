package com.zhuoyou.plugin.resideMenu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningTitleBar;

public class SettingActivity extends Activity implements OnClickListener{
	private RelativeLayout tvSupportType;
	private RelativeLayout tvIntelligenceDevice;
	private RelativeLayout tvFrequentlyQuestions;
//	private RelativeLayout tvSuggestionFeedback;
	private RelativeLayout tvApplicationIntroducing;
	private TextView mVersion;
	private final int APPLICATION_INTRODUING = 1;
	private String version;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		if (getIntent() != null)
			version = getIntent().getStringExtra("version");
		
		initView();
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		RunningTitleBar.getTitleBar(this, getResources().getString(R.string.setting));
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {//界面返回上一级
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	private void initView() {
		tvSupportType = (RelativeLayout) findViewById(R.id.tv_support_type);
		tvIntelligenceDevice = (RelativeLayout) findViewById(R.id.tv_intelligent_device);
		tvFrequentlyQuestions = (RelativeLayout) findViewById(R.id.tv_frequently_questions);
//		tvSuggestionFeedback = (RelativeLayout) findViewById(R.id.tv_suggestion_feedback);
		tvApplicationIntroducing = (RelativeLayout) findViewById(R.id.tv_application_introducing);
		mVersion = (TextView) findViewById(R.id.tv_version);
		tvSupportType.setOnClickListener(this);
		tvIntelligenceDevice.setOnClickListener(this);
		tvFrequentlyQuestions.setOnClickListener(this);
//		tvSuggestionFeedback.setOnClickListener(this);
		tvApplicationIntroducing.setOnClickListener(this);
		if (!version.equals(""))
			mVersion.setText(version);
	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.tv_support_type:
			intent = new Intent(SettingActivity.this,SupportTypeActivity.class);
			startActivity(intent);
			break;
		case R.id.tv_intelligent_device:
			intent = new Intent(SettingActivity.this,IntelligentDeviceActivity.class);
			startActivity(intent);
			break;
		case R.id.tv_frequently_questions:
			intent = new Intent(SettingActivity.this,FrequentlyQuestionsActivity.class);
			startActivity(intent);
			break;
		/*case R.id.tv_suggestion_feedback:
			intent = new Intent(SettingActivity.this,SuggestionFeedback.class);
			startActivity(intent);
			break;*/
		case R.id.tv_application_introducing:
			intent = new Intent(SettingActivity.this,ApplicationIntroduingActivity.class);
			startActivityForResult(intent, APPLICATION_INTRODUING);
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
