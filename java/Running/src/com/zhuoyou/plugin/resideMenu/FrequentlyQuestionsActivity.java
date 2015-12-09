package com.zhuoyou.plugin.resideMenu;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.running.R;

public class FrequentlyQuestionsActivity extends Activity implements
		OnClickListener {
	private TextView tvNewbieGuide, tvNewbieGuide3, tvNewbieGuide4, tvNewbieGuide5;
	private TextView tvStandbyTime;
	private TextView tvSolvep1, tvSolvep2, tvSolvep3, tvSolvep4, tvSolvep5;
	private TextView tvDropdownTriangle1, tvDropdownTriangle2, tvDropdownTriangle3, tvDropdownTriangle4, tvDropdownTriangle5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.frequently_questions);
		initView();
		TextView tv_title = (TextView) findViewById(R.id.title);
		tv_title.setText(R.string.asked_questions);
		RelativeLayout im_back = (RelativeLayout) findViewById(R.id.back);
		im_back.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	private void initView() {
		/*
		tvNewbieGuide = (TextView) findViewById(R.id.tv_newbie_guide);
		tvNewbieGuide3 = (TextView) findViewById(R.id.tv_newbie_guide3);
		tvNewbieGuide4 = (TextView) findViewById(R.id.tv_newbie_guide4);
		tvNewbieGuide5 = (TextView) findViewById(R.id.tv_newbie_guide5);

		tvSolvep1 = (TextView) findViewById(R.id.tv_solvep1);
		tvSolvep2 = (TextView) findViewById(R.id.tv_solvep2);
		tvSolvep3 = (TextView) findViewById(R.id.tv_solvep3);
		tvSolvep4 = (TextView) findViewById(R.id.tv_solvep4);
		tvSolvep5 = (TextView) findViewById(R.id.tv_solvep5);


		tvStandbyTime.setOnClickListener(this);
		 
		 */
	}

	@Override
	public void onClick(View v) {
		/*
		Drawable up_indicator = getResources().getDrawable(
				R.drawable.fq_up_arrow);
		up_indicator.setBounds(0, 0, up_indicator.getMinimumWidth(),
				up_indicator.getMinimumHeight());
		Drawable down_indicator = getResources().getDrawable(
				R.drawable.fq_down_arrow);
		down_indicator.setBounds(0, 0, down_indicator.getMinimumWidth(),
				down_indicator.getMinimumHeight());
		
		switch (v.getId()) {
		case R.id.common_problem:
			if (tvSolvep1.getVisibility() == View.GONE) {
				tvSolvep1.setVisibility(View.VISIBLE);
				tvDropdownTriangle1.setVisibility(View.VISIBLE);
				tvNewbieGuide.setCompoundDrawables(null, null, up_indicator, null);

				tvSolvep3.setVisibility(View.GONE);
				tvDropdownTriangle3.setVisibility(View.GONE);
				tvNewbieGuide3.setCompoundDrawables(null, null, down_indicator, null);
				tvSolvep4.setVisibility(View.GONE);
				tvDropdownTriangle4.setVisibility(View.GONE);
				tvNewbieGuide4.setCompoundDrawables(null, null, down_indicator, null);
				tvSolvep5.setVisibility(View.GONE);
				tvDropdownTriangle5.setVisibility(View.GONE);
				tvNewbieGuide5.setCompoundDrawables(null, null, down_indicator, null);
			} else {
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
			} else {
				tvSolvep2.setVisibility(View.GONE);
				tvDropdownTriangle2.setVisibility(View.GONE);
				tvStandbyTime.setCompoundDrawables(null, null, down_indicator, null);
			}
			break;
		case R.id.common_problem2:
			if (tvSolvep3.getVisibility() == View.GONE) {
				tvSolvep3.setVisibility(View.VISIBLE);
				tvDropdownTriangle3.setVisibility(View.VISIBLE);
				tvNewbieGuide3.setCompoundDrawables(null, null, up_indicator, null);
				
				tvSolvep1.setVisibility(View.GONE);
				tvDropdownTriangle1.setVisibility(View.GONE);
				tvNewbieGuide.setCompoundDrawables(null, null, down_indicator, null);
				tvSolvep4.setVisibility(View.GONE);
				tvDropdownTriangle4.setVisibility(View.GONE);
				tvNewbieGuide4.setCompoundDrawables(null, null, down_indicator, null);
				tvSolvep5.setVisibility(View.GONE);
				tvDropdownTriangle5.setVisibility(View.GONE);
				tvNewbieGuide5.setCompoundDrawables(null, null, down_indicator, null);
			} else {
				tvSolvep3.setVisibility(View.GONE);
				tvDropdownTriangle3.setVisibility(View.GONE);
				tvNewbieGuide3.setCompoundDrawables(null, null, down_indicator, null);
			}
			break;
		case R.id.common_problem3:
			if (tvSolvep4.getVisibility() == View.GONE) {
				tvSolvep4.setVisibility(View.VISIBLE);
				tvDropdownTriangle4.setVisibility(View.VISIBLE);
				tvNewbieGuide4.setCompoundDrawables(null, null, up_indicator, null);
				
				tvSolvep1.setVisibility(View.GONE);
				tvDropdownTriangle1.setVisibility(View.GONE);
				tvNewbieGuide.setCompoundDrawables(null, null, down_indicator, null);
				tvSolvep3.setVisibility(View.GONE);
				tvDropdownTriangle3.setVisibility(View.GONE);
				tvNewbieGuide3.setCompoundDrawables(null, null, down_indicator, null);
				tvSolvep5.setVisibility(View.GONE);
				tvDropdownTriangle5.setVisibility(View.GONE);
				tvNewbieGuide5.setCompoundDrawables(null, null, down_indicator, null);
			} else {
				tvSolvep4.setVisibility(View.GONE);
				tvDropdownTriangle4.setVisibility(View.GONE);
				tvNewbieGuide4.setCompoundDrawables(null, null, down_indicator, null);
			}
			break;
		case R.id.common_problem4:
			if (tvSolvep5.getVisibility() == View.GONE) {
				tvSolvep5.setVisibility(View.VISIBLE);
				tvDropdownTriangle5.setVisibility(View.VISIBLE);
				tvNewbieGuide5.setCompoundDrawables(null, null, up_indicator, null);
				
				tvSolvep1.setVisibility(View.GONE);
				tvDropdownTriangle1.setVisibility(View.GONE);
				tvNewbieGuide.setCompoundDrawables(null, null, down_indicator, null);
				tvSolvep3.setVisibility(View.GONE);
				tvDropdownTriangle3.setVisibility(View.GONE);
				tvNewbieGuide3.setCompoundDrawables(null, null, down_indicator, null);
				tvSolvep4.setVisibility(View.GONE);
				tvDropdownTriangle4.setVisibility(View.GONE);
				tvNewbieGuide4.setCompoundDrawables(null, null, down_indicator, null);
			} else {
				tvSolvep5.setVisibility(View.GONE);
				tvDropdownTriangle5.setVisibility(View.GONE);
				tvNewbieGuide5.setCompoundDrawables(null, null, down_indicator, null);
			}
			break;

		default:
			break;
		}
		*/

	}

}
