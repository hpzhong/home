package com.zhuoyou.plugin.add;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.add.TosAdapterView.OnItemSelectedListener;
import com.zhuoyou.plugin.add.TosGallery.OnEndFlingListener;
import com.zhuoyou.plugin.custom.CustomAlertDialog;
import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.running.PersonalConfig;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;
import com.zhuoyou.plugin.view.WheelView;

public class AddWeight extends Activity implements OnClickListener,
		OnEndFlingListener {

	private WheelView wheelViewKg, wheelViewOther;
	private TextView tv_start_date;
	private LinearLayout rLayout;
	private RelativeLayout rlayout_startDate;
	private int kg = 50;
	private int other = 0;
	private float initWeightCount = 0;
	private Intent intent;
	private Button mButton;
	private TextView tv_add_weight;
	private boolean isModify = false;
	private String[] kgData = new String[300];
	private String[] otherData = new String[10];
	private String weightCount;
	private String date;
	private RelativeLayout imView;
	private long id;
	private int selectPostion;
	private DateSelectPopupWindow datePopuWindow;
	private String updateDate = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_weight);
		imView = (RelativeLayout) findViewById(R.id.back);
		wheelViewKg = (WheelView) findViewById(R.id.weight_kg);
		wheelViewOther = (WheelView) findViewById(R.id.weight_other);
		tv_add_weight = (TextView) findViewById(R.id.title);
		tv_add_weight.setText(R.string.add_weight);
		mButton = (Button) findViewById(R.id.save);
		mButton.setOnClickListener(this);
		tv_start_date = (TextView) findViewById(R.id.tv_start_date);
		rLayout = (LinearLayout) findViewById(R.id.rlayout);
		rlayout_startDate = (RelativeLayout) findViewById(R.id.rlayout_startDate);
		rlayout_startDate.setOnClickListener(this);
		imView.setOnClickListener(this);

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
		if (date.equals(Tools.getDate(0))) {
			tv_start_date.setText(R.string.today);
		} else {
			tv_start_date.setText(date);
		}
		if (weightCount != null) {
			updateDate = date;
			initWeightCount = Float.parseFloat(intent.getStringExtra("weightCount"));
			kg = (int) (initWeightCount / 1);
			other = (int) ((initWeightCount * 10) % 10);
			wheelViewKg.setSelection(kg);
			wheelViewOther.setSelection(other);
			tv_add_weight.setText(R.string.edit_weight);
			mButton.setText(R.string.gpsdata_delete);
		} else {
			wheelViewKg.setSelection(50);
			wheelViewOther.setSelection(0);
			mButton.setText(R.string.ok);
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
		case R.id.save:
			if (weightCount != null) {
				if (isModify) {
					personalConfig.setWeight(kg + "." + other);
					Bmi = Tools.getBMI(personalConfig);
					aobj[0] = Double.valueOf(Bmi);
					bmi = String.format("%.1f", aobj);
					updateDateBaseWeight(kg + "." + other, date, bmi);
				} else {
					deleteDateBaseWeight();
					finish();
				}
			} else {
				String curTime = Tools.getStartTime();
				personalConfig.setWeight(kg + "." + other);
				Bmi = Tools.getBMI(personalConfig);
				aobj[0] = Double.valueOf(Bmi);
				bmi = String.format("%.1f", aobj);			
				insertDataBaseWeight(date, curTime, kg + "." + other, bmi + "", 1, 0);
			}
			break;
		case R.id.rlayout_startDate:
			final WindowManager.LayoutParams lp = getWindow().getAttributes();
			lp.alpha = 0.7f;
			final String finalDate = date;
			datePopuWindow = new DateSelectPopupWindow(AddWeight.this, finalDate);
			datePopuWindow.showAtLocation(rLayout, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
			getWindow().setAttributes(lp);
			datePopuWindow.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss() {
					lp.alpha = 1.0f;
					getWindow().setAttributes(lp);
					date = datePopuWindow.getStartDate();
					if (!date.equals(finalDate)) {
						isModify = true;
						mButton.setText(R.string.ok);
					}
					if (date.equals(Tools.getDate(0))) {
						tv_start_date.setText(R.string.today);
					} else {
						tv_start_date.setText(date);
					}
				}
			});
			break;
		default:
			break;
		}
	}

	// 向数据库中插入 体重
	private void insertDataBaseWeight(final String date, String time, final String weight,
			final String bmi, int type, int statistics) {
		ContentResolver cr = this.getContentResolver();
		Cursor c = cr.query(DataBaseContants.CONTENT_URI, new String[] { "_id" }, DataBaseContants.DATE + " = ? " + " and "+DataBaseContants.TYPE + " = ? " + " and "+DataBaseContants.STATISTICS + " = ? " , new String[] { date, "1", "0" }, null);
		if (c.getCount() > 0 && c.moveToFirst()) {
			final Long mId = c.getLong(c.getColumnIndex(DataBaseContants.ID));
			CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(AddWeight.this);
			builder.setTitle(R.string.alert_title);
			builder.setMessage(R.string.date_have_weight);
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					updateDateBaseWeight(mId, weight, bmi);
					dialog.dismiss();
					finish();
				}
			});
			builder.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.setCancelable(false);
			builder.create().show();
			c.close();
			c = null;
		} else {
			ContentValues runningItem = new ContentValues();
			runningItem.put(DataBaseContants.ID, Tools.getPKL());
			runningItem.put(DataBaseContants.DATE, date);
			runningItem.put(DataBaseContants.TIME_START, time);
			runningItem.put(DataBaseContants.CONF_WEIGHT, weight);
			runningItem.put(DataBaseContants.BMI, bmi);
			runningItem.put(DataBaseContants.TYPE, type);
			runningItem.put(DataBaseContants.STATISTICS, statistics);
			this.getContentResolver().insert(DataBaseContants.CONTENT_URI, runningItem);
			c.close();
			c = null;
			finish();
		}
	}

	private void updateDateBaseWeight(Long id, String weight, String bmi) {
		ContentResolver cr = this.getContentResolver();
		ContentValues updateValues = new ContentValues();
		Cursor c = cr.query(DataBaseContants.CONTENT_URI, new String[] { "sync" }, DataBaseContants.ID + " = ? " , new String[] { String.valueOf(id) }, null);
		if (c.getCount() > 0 && c.moveToFirst()) {
			int sync = c.getInt(c.getColumnIndex(DataBaseContants.SYNC_STATE));
			if (sync == 0) {
				updateValues.put(DataBaseContants.CONF_WEIGHT, weight);
				updateValues.put(DataBaseContants.BMI, bmi);
				updateValues.put(DataBaseContants.SYNC_STATE, 0);
			} else {
				updateValues.put(DataBaseContants.CONF_WEIGHT, weight);
				updateValues.put(DataBaseContants.BMI, bmi);
				updateValues.put(DataBaseContants.SYNC_STATE, 2);
			}
		}
		c.close();
		c = null;
		cr.update(DataBaseContants.CONTENT_URI, updateValues, DataBaseContants.ID + " = ? ", new String[] { String.valueOf(id) });
	}

	// 更新数据库中的一条 体重信息
	private void updateDateBaseWeight(final String weight, String date, final String bmi) {
		ContentResolver cr = this.getContentResolver();
		ContentValues updateValues = new ContentValues();
		if (updateDate.equals(date)) {
			Cursor c = cr.query(DataBaseContants.CONTENT_URI, new String[] { "sync" }, DataBaseContants.ID + " = ? " , new String[] { String.valueOf(id) }, null);
			if (c.getCount() > 0 && c.moveToFirst()) {
				int sync = c.getInt(c.getColumnIndex(DataBaseContants.SYNC_STATE));
				if (sync == 0) {
					updateValues.put(DataBaseContants.DATE, date);
					updateValues.put(DataBaseContants.CONF_WEIGHT, weight);
					updateValues.put(DataBaseContants.BMI, bmi);
					updateValues.put(DataBaseContants.SYNC_STATE, 0);
				} else {
					updateValues.put(DataBaseContants.DATE, date);
					updateValues.put(DataBaseContants.CONF_WEIGHT, weight);
					updateValues.put(DataBaseContants.BMI, bmi);
					updateValues.put(DataBaseContants.SYNC_STATE, 2);
				}
			}
			c.close();
			c = null;
			cr.update(DataBaseContants.CONTENT_URI, updateValues, DataBaseContants.ID + " = ? ", new String[] { String.valueOf(id) });
			finish();
		} else {
			Cursor c = cr.query(DataBaseContants.CONTENT_URI, new String[] { "_id" }, DataBaseContants.DATE + " = ? " + " and "+DataBaseContants.TYPE + " = ? " + " and "+DataBaseContants.STATISTICS + " = ? " , new String[] { date, "1", "0" }, null);
			if (c.getCount() > 0 && c.moveToFirst()) {
				final Long mId = c.getLong(c.getColumnIndex(DataBaseContants.ID));
				CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(AddWeight.this);
				builder.setTitle(R.string.alert_title);
				builder.setMessage(R.string.date_have_weight);
				builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						updateDateBaseWeight(mId, weight, bmi);
						finish();
						dialog.dismiss();
					}
				});
				builder.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.setCancelable(false);
				builder.create().show();
				c.close();
				c = null;
			} else {
				ContentValues runningItem = new ContentValues();
				runningItem.put(DataBaseContants.ID, Tools.getPKL());
				runningItem.put(DataBaseContants.DATE, date);
				runningItem.put(DataBaseContants.TIME_START, Tools.getStartTime());
				runningItem.put(DataBaseContants.CONF_WEIGHT, weight);
				runningItem.put(DataBaseContants.BMI, bmi);
				runningItem.put(DataBaseContants.TYPE, 1);
				runningItem.put(DataBaseContants.STATISTICS, 0);
				cr.insert(DataBaseContants.CONTENT_URI, runningItem);
				c.close();
				c = null;
				finish();
			}
		}
	}

	// 删除数据库中的一条 体重信息
	private void deleteDateBaseWeight() {
		ContentResolver cr = this.getContentResolver();
		cr.delete(DataBaseContants.CONTENT_URI, DataBaseContants.ID + " = ?", new String[]{ String.valueOf(id) });
		ContentValues values = new ContentValues();
		values.put(DataBaseContants.DELETE_VALUE, id);
		cr.insert(DataBaseContants.CONTENT_DELETE_URI, values);
	}

	public class MyWheelView extends BaseAdapter {
		String[] data;
		int mHeight = 50;
		int selectPos;
		private Typeface mNumberTP = RunningApp.getCustomNumberFont();;
		
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
			if (null == convertView) {
				convertView = new TextView(AddWeight.this);
				convertView.setLayoutParams(new TosGallery.LayoutParams(-1,mHeight));
				txtView = (TextView) convertView;
				txtView.setGravity(Gravity.CENTER);
			}
			
			if (null == txtView) {
				txtView = (TextView) convertView;
			}
			
	        if(position == selectPos){
				txtView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
	        	txtView.setTextColor(0xfff1378a);
			}else if(position==selectPos-1 || position==selectPos+1){
				txtView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 32);
				txtView.setTextColor(0x99f1378a);
			}else if(position==selectPos-2 || position==selectPos+2){
				txtView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28);
				txtView.setTextColor(0x55f1378a);
			}else{
				txtView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
				txtView.setTextColor(0x11f1378a);
			}
			txtView.setText(data[position]);
			txtView.setTypeface(mNumberTP);
			return convertView;
		}

	}

	@Override
	public void onEndFling(TosGallery v) {
		// TODO Auto-generated method stub
		isModify = true;
		mButton.setText(R.string.ok);
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
