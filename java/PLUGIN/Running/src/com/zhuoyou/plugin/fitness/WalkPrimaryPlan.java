package com.zhuoyou.plugin.fitness;

import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.Tools;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WalkPrimaryPlan extends Activity {
	private ListView  lv_first_week,lv_second_week, lv_third_week,
			lv_fourth_week,lv_fifth_week,lv_sixth_week,lv_seventh_week,lv_eighth_week;
	private TextView tv_fifth_week,tv_sixth_week,tv_seventh_week,tv_eighth_week,tv_plan_over,tv_title;;
	private RelativeLayout im_back;
	private String[] first_week_plan = new String[7];
	private String[] second_week_plan = new String[7];
	private String[] third_week_plan = new String[7];
	private String[] fourth_week_plan = new String[7];
	private String[] fifth_week_plan = new String[7];
	private String[] sixth_week_plan = new String[7];
	private String[] seventh_week_plan = new String[7];
	private String[] eighth_week_plan = new String[7];
	private int tmp = 0;
	private Intent intent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fitness_primary_plan);
		intent = getIntent();
		tmp = intent.getIntExtra("tmp", -1);
		
		lv_first_week = (ListView) findViewById(R.id.lv_first_week);
		lv_second_week = (ListView) findViewById(R.id.lv_second_week);
		lv_third_week = (ListView) findViewById(R.id.lv_third_week);
		lv_fourth_week = (ListView) findViewById(R.id.lv_fourth_week);
		lv_fifth_week = (ListView) findViewById(R.id.lv_fifth_week);
		lv_sixth_week = (ListView) findViewById(R.id.lv_sixth_week);
		lv_seventh_week = (ListView) findViewById(R.id.lv_seventh_week);
		lv_eighth_week = (ListView) findViewById(R.id.lv_eighth_week);
		
		tv_fifth_week = (TextView) findViewById(R.id.tv_fifth_week);
		tv_sixth_week = (TextView) findViewById(R.id.tv_sixth_week);
		tv_seventh_week = (TextView) findViewById(R.id.tv_seventh_week);
		tv_eighth_week = (TextView) findViewById(R.id.tv_eighth_week);
		tv_plan_over = (TextView) findViewById(R.id.tv_plan_over);
		tv_title = (TextView) findViewById(R.id.title);
		tv_title.setText(R.string.walk_primary_plan);
		im_back = (RelativeLayout) findViewById(R.id.back);
		im_back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		getWeekPlan(tmp);
		lv_first_week.setAdapter(new MyAdapter(first_week_plan));
		lv_second_week.setAdapter(new MyAdapter(second_week_plan));
		lv_third_week.setAdapter(new MyAdapter(third_week_plan));
		lv_fourth_week.setAdapter(new MyAdapter(fourth_week_plan));
		
		Tools.setListViewHeightBasedOnChildren(lv_first_week);
		Tools.setListViewHeightBasedOnChildren(lv_second_week);
		Tools.setListViewHeightBasedOnChildren(lv_third_week);
		Tools.setListViewHeightBasedOnChildren(lv_fourth_week);
		
		
		
	}

	private class MyAdapter extends BaseAdapter {
		
		
		String[] arr = new String[7];

		public boolean isEnabled(int position) {
			return false;
		};
		public MyAdapter(String[] arr) {
			// TODO Auto-generated constructor stub
			this.arr = arr;

		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return arr.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return arr[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = LayoutInflater.from(WalkPrimaryPlan.this).inflate(
					R.layout.week_list_item, null);
			TextView tv = (TextView) view.findViewById(R.id.textView1);

			if (arr[position].equals("休息一天")) {
				tv.setGravity(Gravity.CENTER);
				tv.setTextColor(0xffe59343);
				tv.setBackgroundColor(0xfffceece);
			}
			tv.setText(arr[position]);
			return view;
		}
		
	}
	
	public void getWeekPlan(int tmp){
		switch (tmp) {
		case 0x001:
			tv_fifth_week.setVisibility(View.GONE);
			tv_sixth_week.setVisibility(View.GONE);
			tv_seventh_week.setVisibility(View.GONE);
			tv_eighth_week.setVisibility(View.GONE);
			lv_fifth_week.setVisibility(View.GONE);
			lv_sixth_week.setVisibility(View.GONE);
			lv_seventh_week.setVisibility(View.GONE);
			lv_eighth_week.setVisibility(View.GONE);
			
			first_week_plan = getResources().getStringArray(
					R.array.walk_first_week_plan);
			second_week_plan = getResources().getStringArray(
					R.array.walk_second_week_plan);
			third_week_plan = getResources().getStringArray(
					R.array.walk_third_week_plan);
			fourth_week_plan = getResources().getStringArray(
			R.array.walk_fourth_week_plan);
			
			break;
		case 0x002:
			tv_fifth_week.setVisibility(View.GONE);
			tv_sixth_week.setVisibility(View.GONE);
			tv_seventh_week.setVisibility(View.GONE);
			tv_eighth_week.setVisibility(View.GONE);
			lv_fifth_week.setVisibility(View.GONE);
			lv_sixth_week.setVisibility(View.GONE);
			lv_seventh_week.setVisibility(View.GONE);
			lv_eighth_week.setVisibility(View.GONE);
			first_week_plan = getResources().getStringArray(
					R.array.primary_run_first_week_plan);
			second_week_plan = getResources().getStringArray(
					R.array.primary_run_second_week_plan);
			third_week_plan = getResources().getStringArray(
					R.array.primary_run_third_week_plan);
			fourth_week_plan = getResources().getStringArray(
			R.array.primary_run_fourth_week_plan);
			tv_title.setText(R.string.run_primary_plan);
			break;
		case 0x003:
			tv_fifth_week.setVisibility(View.VISIBLE);
			tv_sixth_week.setVisibility(View.VISIBLE);
			tv_seventh_week.setVisibility(View.GONE);
			tv_eighth_week.setVisibility(View.GONE);
			
			lv_fifth_week.setVisibility(View.VISIBLE);
			lv_sixth_week.setVisibility(View.VISIBLE);
			lv_seventh_week.setVisibility(View.GONE);
			lv_eighth_week.setVisibility(View.GONE);
			first_week_plan = getResources().getStringArray(
					R.array.advance_run_first_week_plan);
			second_week_plan = getResources().getStringArray(
					R.array.advance_run_second_week_plan);
			third_week_plan = getResources().getStringArray(
					R.array.advance_run_third_week_plan);
			fourth_week_plan = getResources().getStringArray(
			R.array.advance_run_fourth_week_plan);
			fifth_week_plan = getResources().getStringArray(
					R.array.advance_run_fifth_week_plan);
			sixth_week_plan = getResources().getStringArray(
			R.array.advance_run_sixth_week_plan);
			lv_fifth_week.setAdapter(new MyAdapter(fifth_week_plan));
			lv_sixth_week.setAdapter(new MyAdapter(sixth_week_plan));
			tv_title.setText(R.string.run_advance_plan);
			Tools.setListViewHeightBasedOnChildren(lv_fifth_week);
			Tools.setListViewHeightBasedOnChildren(lv_sixth_week);
			break;
		case 0x004:
			tv_fifth_week.setVisibility(View.VISIBLE);
			tv_sixth_week.setVisibility(View.VISIBLE);
			tv_seventh_week.setVisibility(View.VISIBLE);
			tv_eighth_week.setVisibility(View.VISIBLE);
			
			lv_fifth_week.setVisibility(View.VISIBLE);
			lv_sixth_week.setVisibility(View.VISIBLE);
			lv_seventh_week.setVisibility(View.VISIBLE);
			lv_eighth_week.setVisibility(View.VISIBLE);
			first_week_plan = getResources().getStringArray(
					R.array.first_marathon_first_week_plan);
			second_week_plan = getResources().getStringArray(
					R.array.first_marathon_second_week_plan);
			third_week_plan = getResources().getStringArray(
					R.array.first_marathon_third_week_plan);
			fourth_week_plan = getResources().getStringArray(
			R.array.first_marathon_fourth_week_plan);
			fifth_week_plan = getResources().getStringArray(
					R.array.first_marathon_fifth_week_plan);
			sixth_week_plan = getResources().getStringArray(
			R.array.first_marathon_sixth_week_plan);
			seventh_week_plan = getResources().getStringArray(
					R.array.first_marathon_seventh_week_plan);
			eighth_week_plan = getResources().getStringArray(
			R.array.first_marathon_eighth_week_plan);
			lv_fifth_week.setAdapter(new MyAdapter(fifth_week_plan));
			lv_sixth_week.setAdapter(new MyAdapter(sixth_week_plan));
			lv_seventh_week.setAdapter(new MyAdapter(seventh_week_plan));
			lv_eighth_week.setAdapter(new MyAdapter(eighth_week_plan));
			tv_plan_over.setText(R.string.notice_rest);
			tv_title.setText(R.string.first_five_km);
			Tools.setListViewHeightBasedOnChildren(lv_fifth_week);
			Tools.setListViewHeightBasedOnChildren(lv_sixth_week);
			Tools.setListViewHeightBasedOnChildren(lv_seventh_week);
			Tools.setListViewHeightBasedOnChildren(lv_eighth_week);
			break;
		case 0x005:
			tv_fifth_week.setVisibility(View.VISIBLE);
			tv_sixth_week.setVisibility(View.VISIBLE);
			tv_seventh_week.setVisibility(View.VISIBLE);
			tv_eighth_week.setVisibility(View.VISIBLE);
			
			lv_fifth_week.setVisibility(View.VISIBLE);
			lv_sixth_week.setVisibility(View.VISIBLE);
			lv_seventh_week.setVisibility(View.VISIBLE);
			lv_eighth_week.setVisibility(View.VISIBLE);
			first_week_plan = getResources().getStringArray(
					R.array.beyond_marathon_first_week_plan);
			second_week_plan = getResources().getStringArray(
					R.array.beyond_marathon_second_week_plan);
			third_week_plan = getResources().getStringArray(
					R.array.beyond_marathon_third_week_plan);
			fourth_week_plan = getResources().getStringArray(
			R.array.beyond_marathon_fourth_week_plan);
			fifth_week_plan = getResources().getStringArray(
					R.array.beyond_marathon_fifth_week_plan);
			sixth_week_plan = getResources().getStringArray(
			R.array.beyond_marathon_sixth_week_plan);
			seventh_week_plan = getResources().getStringArray(
					R.array.beyond_marathon_seventh_week_plan);
			eighth_week_plan = getResources().getStringArray(
			R.array.beyond_marathon_eighth_week_plan);
			lv_fifth_week.setAdapter(new MyAdapter(fifth_week_plan));
			lv_sixth_week.setAdapter(new MyAdapter(sixth_week_plan));
			lv_seventh_week.setAdapter(new MyAdapter(seventh_week_plan));
			lv_eighth_week.setAdapter(new MyAdapter(eighth_week_plan));
			tv_plan_over.setText(R.string.about_cross_training);
			tv_title.setText(R.string.beyond_marathon);
			Tools.setListViewHeightBasedOnChildren(lv_fifth_week);
			Tools.setListViewHeightBasedOnChildren(lv_sixth_week);
			Tools.setListViewHeightBasedOnChildren(lv_seventh_week);
			Tools.setListViewHeightBasedOnChildren(lv_eighth_week);
			break;
		default:
			break;
		}
	}
}
