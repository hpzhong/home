package com.zhuoyou.plugin.fitness;

import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningTitleBar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class FitnessMain extends Activity implements OnClickListener{
	private LinearLayout llayout_walk,llayout_primary_run,llayout_advance_run,llayout_first_5km,llayout_beyond_marathon;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fitness_main);
		RunningTitleBar.getTitleBar(this, getResources().getString(R.string.fitness_plan));
		
		llayout_walk = (LinearLayout) findViewById(R.id.llayout_walk);
		llayout_primary_run = (LinearLayout) findViewById(R.id.llayout_primary_run);
		llayout_advance_run = (LinearLayout) findViewById(R.id.llayout_advance_run);
		llayout_first_5km = (LinearLayout) findViewById(R.id.llayout_first_5km);
		llayout_beyond_marathon = (LinearLayout) findViewById(R.id.llayout_beyond_marathon);
		
		
		
		llayout_walk.setOnClickListener(this);
		llayout_primary_run.setOnClickListener(this);
		llayout_advance_run.setOnClickListener(this);
		llayout_first_5km.setOnClickListener(this);
		llayout_beyond_marathon.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.llayout_walk:
			Intent walk_intent = new Intent();
			walk_intent.putExtra("tmp",0x001);
			walk_intent.setClass(FitnessMain.this, WalkPrimaryPlan.class);
			startActivity(walk_intent);
			break;
			
		case R.id.llayout_primary_run:
			Intent primary_run_intent = new Intent();
			primary_run_intent.putExtra("tmp",0x002);
			primary_run_intent.setClass(FitnessMain.this, WalkPrimaryPlan.class);
			startActivity(primary_run_intent);
			break;

		case R.id.llayout_advance_run:
			Intent advance_run_intent = new Intent();
			advance_run_intent.putExtra("tmp",0x003);
			advance_run_intent.setClass(FitnessMain.this, WalkPrimaryPlan.class);
			startActivity(advance_run_intent);
			break;

		case R.id.llayout_first_5km:
			Intent first_marathon_intent = new Intent();
			first_marathon_intent.putExtra("tmp",0x004);
			first_marathon_intent.setClass(FitnessMain.this, WalkPrimaryPlan.class);
			startActivity(first_marathon_intent);
			break;

		case R.id.llayout_beyond_marathon:
			Intent beyond_marathon_intent = new Intent();
			beyond_marathon_intent.putExtra("tmp",0x005);
			beyond_marathon_intent.setClass(FitnessMain.this, WalkPrimaryPlan.class);
			startActivity(beyond_marathon_intent);
			break;
		default:
			break;
		}
		
	}
  }

	
