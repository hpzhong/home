package com.zhuoyou.plugin.running;
import com.zhuoyou.plugin.cloud.CloudSync;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class PersonalGoalActivity extends Activity implements OnClickListener {
	private Context mCtx;
	private ImageView mPersonal;
	private TextView mGoals1, mGoals2, mGoals3;
	private EditText mSteps, mCal;
	private Button mSave;
	private PersonalGoal mPersonalGoal;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personal_goals_layout);
		mCtx = this;
		RunningTitleBar.getTitleBar(this, getResources().getString(R.string.persion_goal));
		initData();
		initView();
	}

	public void initData() {
		mPersonalGoal = Tools.getPersonalGoal();
	}

	public void initView() {
		mPersonal = (ImageView) findViewById(R.id.personal);
		mGoals1 = (TextView) findViewById(R.id.goals1);
		mGoals1.setOnClickListener(this);
		mGoals2 = (TextView) findViewById(R.id.goals2);
		mGoals2.setOnClickListener(this);
		mGoals3 = (TextView) findViewById(R.id.goals3);
		mGoals3.setOnClickListener(this);
		mSteps = (EditText) findViewById(R.id.steps);
		mCal = (EditText) findViewById(R.id.cal);
		mSave = (Button) findViewById(R.id.save);
		mSave.setOnClickListener(this);

		mSteps.setText(mPersonalGoal.mGoalSteps + "");
		mCal.setText(mPersonalGoal.mGoalCalories + "");
		
		if (mPersonalGoal.mGoalSteps <= 7000) {
			mPersonal.setImageResource(R.drawable.general);
			mGoals1.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.goals_selected, 0, 0);
			mGoals1.setTextColor(0xFF808080);
		} else if (mPersonalGoal.mGoalSteps >= 15000) {
			mPersonal.setImageResource(R.drawable.madman);
			mGoals3.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.goals_selected, 0, 0);
			mGoals3.setTextColor(0xFF808080);
		} else {
			mPersonal.setImageResource(R.drawable.activists);
			mGoals2.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.goals_selected, 0, 0);
			mGoals2.setTextColor(0xFF808080);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.goals1:
			selectGoals(1);
			break;
		case R.id.goals2:
			selectGoals(2);
			break;
		case R.id.goals3:
			selectGoals(3);
			break;
		case R.id.save:
			String temp_step = "";
			String temp_cal = "";
			temp_step = mSteps.getText().toString();
			temp_cal = mCal.getText().toString();
			if (!temp_step.equals("") && !temp_cal.equals("")) {
				int step = Integer.parseInt(temp_step);
				int cal = Integer.parseInt(temp_cal);
				if (step >= 5000 && cal >= 200) {
					mPersonalGoal.mGoalSteps = step;
					mPersonalGoal.mGoalCalories = cal;
					Tools.updatePersonalGoal(mPersonalGoal);
					CloudSync.startSyncInfo();
					finish();
				} else if (step < 5000 && cal >= 200) {
					showAlertDilog(mCtx.getResources().getString(R.string.least_step));
				} else if (step >= 5000 && cal < 200) {
					showAlertDilog(mCtx.getResources().getString(R.string.least_cal));
				} else {
					showAlertDilog(mCtx.getResources().getString(R.string.least_step_cal));
				}
			}
			break;
		default:
			break;
		}
	}

	private void selectGoals(int index) {
		switch (index) {
		case 1:
			mPersonal.setImageResource(R.drawable.general);
			mGoals1.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.goals_selected, 0, 0);
			mGoals2.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.goals_normal, 0, 0);
			mGoals3.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.goals_normal, 0, 0);
			mGoals1.setTextColor(0xFF808080);
			mGoals2.setTextColor(0xFFB9B9B9);
			mGoals3.setTextColor(0xFFB9B9B9);
			mSteps.setText("7000");
			mCal.setText("200");
			break;
		case 2:
			mPersonal.setImageResource(R.drawable.activists);
			mGoals1.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.goals_normal, 0, 0);
			mGoals2.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.goals_selected, 0, 0);
			mGoals3.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.goals_normal, 0, 0);
			mGoals1.setTextColor(0xFFB9B9B9);
			mGoals2.setTextColor(0xFF808080);
			mGoals3.setTextColor(0xFFB9B9B9);
			mSteps.setText("10000");
			mCal.setText("300");
			break;
		case 3:
			mPersonal.setImageResource(R.drawable.madman);
			mGoals1.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.goals_normal, 0, 0);
			mGoals2.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.goals_normal, 0, 0);
			mGoals3.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.goals_selected, 0, 0);
			mGoals1.setTextColor(0xFFB9B9B9);
			mGoals2.setTextColor(0xFFB9B9B9);
			mGoals3.setTextColor(0xFF808080);
			mSteps.setText("15000");
			mCal.setText("400");
			break;
		default:
			break;
		}
	}

	private void showAlertDilog(String string) {
		Builder builder = new AlertDialog.Builder(mCtx);
		builder.setMessage(string);
		builder.setPositiveButton(R.string.know, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();				
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	// @Override
	// public void recoveryState() {
	// mSteps.setText(mPersonalGoal.mGoalSteps + "");
	// mCal.setText(mPersonalGoal.mGoalCalories + "");
	//
	// mPersonal.setImageResource(R.drawable.general);
	//
	// mGoals1.setImageResource(R.drawable.goals_normal);
	// mGoals2.setImageResource(R.drawable.goals_normal);
	// mGoals3.setImageResource(R.drawable.goals_normal);
	//
	// mTextView1.setTextColor(0xFFB9B9B9);
	// mTextView2.setTextColor(0xFFB9B9B9);
	// mTextView3.setTextColor(0xFFB9B9B9);
	// }
}
