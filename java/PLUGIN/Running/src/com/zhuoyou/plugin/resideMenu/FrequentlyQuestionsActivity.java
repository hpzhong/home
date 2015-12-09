package com.zhuoyou.plugin.resideMenu;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningTitleBar;

public class FrequentlyQuestionsActivity extends Activity implements OnClickListener{
	private TextView tvNewbieGuide;
	private TextView tvStandbyTime;
	private TextView tvSolvep1,tvSolvep2;
	private TextView tvDropdownTriangle1,tvDropdownTriangle2;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.frequently_questions);
		
		initView();
		
		RunningTitleBar.getTitleBar(this, "常见问题");
	}
	
	private void initView() {
		tvNewbieGuide = (TextView) findViewById(R.id.tv_newbie_guide);
		tvStandbyTime = (TextView) findViewById(R.id.tv_standby_time);
		tvSolvep1 = (TextView) findViewById(R.id.tv_solvep1);
		tvSolvep2 = (TextView) findViewById(R.id.tv_solvep2);
		tvDropdownTriangle1 = (TextView) findViewById(R.id.tv_dropdown_triangle1);
		tvDropdownTriangle2 = (TextView) findViewById(R.id.tv_dropdown_triangle2);
		
		tvNewbieGuide.setOnClickListener(this);
		tvStandbyTime.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Drawable up_indicator = getResources().getDrawable(R.drawable.fq_up_arrow);
		up_indicator.setBounds(0, 0, up_indicator.getMinimumWidth(), up_indicator.getMinimumHeight());
		Drawable down_indicator = getResources().getDrawable(R.drawable.fq_down_arrow);
		down_indicator.setBounds(0, 0, down_indicator.getMinimumWidth(), down_indicator.getMinimumHeight());
		switch (v.getId()) {
		case R.id.tv_newbie_guide:
			if (tvSolvep1.getVisibility() == View.GONE) {
				tvSolvep1.setVisibility(View.VISIBLE);
				tvDropdownTriangle1.setVisibility(View.VISIBLE);
				tvNewbieGuide.setCompoundDrawables(null, null, up_indicator, null);
				
				tvSolvep2.setVisibility(View.GONE);
				tvDropdownTriangle2.setVisibility(View.GONE);
				tvStandbyTime.setCompoundDrawables(null, null, down_indicator, null);
			}else {
				tvSolvep1.setVisibility(View.GONE);
				tvDropdownTriangle1.setVisibility(View.GONE);
				tvNewbieGuide.setCompoundDrawables(null, null, down_indicator, null);
			}
			break;
		case R.id.tv_standby_time:
			if (tvSolvep2.getVisibility() == View.GONE) {
				tvSolvep2.setVisibility(View.VISIBLE);
				tvDropdownTriangle2.setVisibility(View.VISIBLE);
				tvStandbyTime.setCompoundDrawables(null, null, up_indicator, null);
				
				tvSolvep1.setVisibility(View.GONE);
				tvDropdownTriangle1.setVisibility(View.GONE);
				tvNewbieGuide.setCompoundDrawables(null, null, down_indicator, null);
			}else {
				tvSolvep2.setVisibility(View.GONE);
				tvDropdownTriangle2.setVisibility(View.GONE);
				tvStandbyTime.setCompoundDrawables(null, null, down_indicator, null);
			}
			break;

		default:
			break;
		}
		
	}
	

}
