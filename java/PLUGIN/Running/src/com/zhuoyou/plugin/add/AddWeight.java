package com.zhuoyou.plugin.add;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.add.TosAdapterView.OnItemSelectedListener;
import com.zhuoyou.plugin.add.TosGallery.OnEndFlingListener;
import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.running.PersonalConfig;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.Tools;
import com.zhuoyou.plugin.view.WheelView;

public class AddWeight extends Activity implements OnClickListener,
		OnEndFlingListener {

	private ImageView im_delete, im_edit_ok,im_cancle;

	private WheelView wheelViewKg, wheelViewOther;
	private int kg = 50;
	private int other = 0;
	private float initWeightCount = 0;
	private Intent intent;
	private ImageView im_ok;
	private TextView tv_add_weight;
	private String[] kgData = new String[300];
	private String[] otherData = new String[10];
	private String weightCount;
	private String date;
	private RelativeLayout imView;
	private long id;
	private boolean hasChanged = false;
	private int selectPostion;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_weight);
		imView = (RelativeLayout) findViewById(R.id.back);
		wheelViewKg = (WheelView) findViewById(R.id.weight_kg);
		wheelViewOther = (WheelView) findViewById(R.id.weight_other);
		im_ok = (ImageView) findViewById(R.id.im_ok);
		tv_add_weight = (TextView) findViewById(R.id.title);
		tv_add_weight.setText(R.string.add_weight);
		im_delete = (ImageView) findViewById(R.id.im_delete);
		im_edit_ok = (ImageView) findViewById(R.id.im_edit_ok);
		im_cancle = (ImageView) findViewById(R.id.im_cancle);
		imView.setOnClickListener(this);
		im_ok.setOnClickListener(this);
		im_edit_ok.setOnClickListener(this);
		im_delete.setOnClickListener(this);
		im_cancle.setOnClickListener(this);

		kgData = getResources().getStringArray(R.array.weight_kg);
		otherData = getResources().getStringArray(R.array.weight_point);
		final MyWheelView kgAdapter = new MyWheelView(kgData);
		final MyWheelView otherAdapter = new MyWheelView(otherData);
		wheelViewKg.setAdapter(kgAdapter);
		wheelViewOther.setAdapter(otherAdapter);
		wheelViewKg.setOnEndFlingListener(this);
		wheelViewOther.setOnEndFlingListener(this);
		wheelViewKg.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(TosAdapterView<?> parent, View view,
					int position, long id) {
				selectPostion = wheelViewKg.getSelectedItemPosition();
				kgAdapter.setSelectPos(selectPostion);
				kgAdapter.notifyDataSetChanged();
				
				if (position != kg) {
					hasChanged = true;
				}
			}

			@Override
			public void onNothingSelected(TosAdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});
		wheelViewOther.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(TosAdapterView<?> parent, View view,
					int position, long id) {
				selectPostion = wheelViewOther.getSelectedItemPosition();
				otherAdapter.setSelectPos(selectPostion);
				otherAdapter.notifyDataSetChanged();
				
				if (position != other) {
					hasChanged = true;
				}
			}

			@Override
			public void onNothingSelected(TosAdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});
		intent = getIntent();
		weightCount = intent.getStringExtra("weightCount");
		date = intent.getStringExtra("date");
		id = intent.getLongExtra("id", 0);
		if (weightCount != null) {
			initWeightCount = Float.parseFloat(intent
					.getStringExtra("weightCount"));

			kg = (int) (initWeightCount / 1);
			other = (int) ((initWeightCount % 1.0) * 10);
			wheelViewKg.setSelection(kg);
			wheelViewOther.setSelection(other);
			tv_add_weight.setText(R.string.edit_weight);
			im_ok.setVisibility(View.GONE);
			im_cancle.setVisibility(View.GONE);
			im_edit_ok.setVisibility(View.VISIBLE);
			im_delete.setVisibility(View.VISIBLE);
			
		} else {
			wheelViewKg.setSelection(50);
			wheelViewOther.setSelection(0);
		}

	}

	@SuppressLint("DefaultLocale") @Override
	public void onClick(View v) {
		PersonalConfig personalConfig = Tools.getPersonalConfig();
		double Bmi = 0.0D;
		String bmi = null;
		Object aobj[] = new Object[1];
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.im_ok:
			String curTime = Tools.getStartTime();
			

			personalConfig.setWeight(kg);
			Bmi = Tools.getBMI(personalConfig);
			aobj[0] = Double.valueOf(Bmi);
			bmi = String.format("%.1f", aobj);
			
			insertDataBaseWeight(date, curTime, kg + "." + other,
					bmi + "", 1, 0);
			finish();
			break;
		case R.id.im_edit_ok:
			
			personalConfig.setWeight(kg);
			Bmi = Tools.getBMI(personalConfig);
			aobj[0] = Double.valueOf(Bmi);
			bmi = String.format("%.1f", aobj);
			if(hasChanged){
				updateDateBaseWeight(kg + "." + other,date,bmi);
			}
			finish();
			break;
		case R.id.im_delete:
			deleteDateBaseWeight(weightCount,date);
			finish();
			break;
		case R.id.im_cancle:
			finish();
			break;
		default:
			break;
		}
	}

	// 向数据库中插入 体重
	private void insertDataBaseWeight(String date, String time, String weight,
			String bmi, int type, int statistics) {
		ContentValues runningItem = new ContentValues();
		runningItem.put(DataBaseContants.ID, Tools.getPKL());
		runningItem.put(DataBaseContants.DATE, date);
		runningItem.put(DataBaseContants.TIME_START, time);
		runningItem.put(DataBaseContants.CONF_WEIGHT, weight);
		runningItem.put(DataBaseContants.BMI, bmi);
		runningItem.put(DataBaseContants.TYPE, type);
		runningItem.put(DataBaseContants.STATISTICS, statistics);
		this.getContentResolver().insert(DataBaseContants.CONTENT_URI,
				runningItem);
	}

	// 更新数据库中的一条 体重信息
	private void updateDateBaseWeight(String weight,String date,String bmi) {
		ContentResolver cr = this.getContentResolver();
		ContentValues updateValues = new ContentValues();
		updateValues.put(DataBaseContants.CONF_WEIGHT, weight);
		updateValues.put(DataBaseContants.BMI, bmi);
		updateValues.put(DataBaseContants.SYNC_STATE, 2);
		cr.update(DataBaseContants.CONTENT_URI, updateValues,
				DataBaseContants.TYPE + " = ? " + " and "+DataBaseContants.DATE + " = ? ", new String[] { "1" ,date});
	}

	// 删除数据库中的一条 体重信息
	private void deleteDateBaseWeight(String weight,String data) {
		ContentResolver cr = this.getContentResolver();
		cr.delete(DataBaseContants.CONTENT_URI, DataBaseContants.CONF_WEIGHT + " = ?" + " and " + DataBaseContants.DATE + " = ?", new String[]{weight,data});
		ContentValues values = new ContentValues();
		values.put(DataBaseContants.DELETE_VALUE, id);
		cr.insert(DataBaseContants.CONTENT_DELETE_URI, values);
	}

	public class MyWheelView extends BaseAdapter {
		String[] data;
		int mHeight = 50;
		int selectPos;
		public MyWheelView(String[] String) {
			data = String;
			mHeight = (int) Tools.dip2px(AddWeight.this, mHeight);
		}

		public void setSelectPos(int pos){
			selectPos = pos;
		}

		@Override
		public int getCount() {
			return (null != data) ? data.length : 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return data[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView txtView = null;
			Typeface tf = Typeface.createFromAsset(AddWeight.this.getAssets(),
					"font/akzidenzgrotesklightcond.ttf");
			if (null == convertView) {
				convertView = new TextView(AddWeight.this);
				convertView.setLayoutParams(new TosGallery.LayoutParams(-1,
						mHeight));
				txtView = (TextView) convertView;
				txtView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 23);
				txtView.setTextColor(Color.BLACK);
				txtView.setGravity(Gravity.CENTER);

			}
			if (null == txtView) {
				txtView = (TextView) convertView;
			}
			if(position == selectPos){
				txtView.setTextColor(0xFF4B4B4B);
				txtView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
			}else{
				txtView.setTextColor(0xFFAFAFAF);
			}
			txtView.setText(data[position]);
			txtView.setTypeface(tf);
			return convertView;
		}

	}

	@Override
	public void onEndFling(TosGallery v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.weight_kg:
			kg = wheelViewKg.getSelectedItemPosition();
			break;
		case R.id.weight_other:
			other = wheelViewOther.getSelectedItemPosition();
			break;

		default:
			break;
		}

	}
}
