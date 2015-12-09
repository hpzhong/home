package com.zhuoyou.plugin.add;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.Tools;

public class AddSports extends Activity implements OnClickListener {
	private ImageView im_run, im_start_time, im_last_time;
	private TextView tv_waste_kll, tv_run, tv_start_time, tv_last_time,
			tv_add_sport;
	private ImageView im_complete, im_delete, im_edit_ok,im_cancle;
	private SportTimePopupWindow startPopuWindow;
	private DurationPopupWindow stopPopuWindow;
	private SportChosePopoWindow sportChose;
	private String[] hourData = new String[25];
	private String[] minuteData = new String[61];
	private String[] lastMinuteData = new String[61];
	private String[] lastHourData = new String[3];
	private RelativeLayout rLayout;
	private String sportType, endTime, sportStartTime, durationTime;
	private String[] sportArray = new String[40];
	private int wasteKll, lastTime, startTime, sportIndex;
	private Intent intent;
	private String wasteCalories;
	private int durationHour, durationOther, startHour, startOther;
	private int sportNum;
	private String date;
	private long id;
	private RelativeLayout im_back;
	private boolean hasChanged = false;
	private RelativeLayout rlayout_choseSport,rlayout_startTime,rlayout_lastTime;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_sport);
		im_back = (RelativeLayout) findViewById(R.id.back);
		im_run = (ImageView) findViewById(R.id.im_chose_sport);
		im_start_time = (ImageView) findViewById(R.id.start_chose);
		im_last_time = (ImageView) findViewById(R.id.im_close_time);
		tv_waste_kll = (TextView) findViewById(R.id.kll_count);
		im_complete = (ImageView) findViewById(R.id.im_complete);
		rLayout = (RelativeLayout) findViewById(R.id.rlayout);
		tv_run = (TextView) findViewById(R.id.tv_run);
		tv_start_time = (TextView) findViewById(R.id.tv_chose_start);
		tv_last_time = (TextView) findViewById(R.id.tv_chose_close);
		tv_add_sport = (TextView) findViewById(R.id.title);
		tv_add_sport.setText(R.string.add_sport);
		im_delete = (ImageView) findViewById(R.id.im_delete);
		im_edit_ok = (ImageView) findViewById(R.id.im_edit_ok);
		im_cancle = (ImageView) findViewById(R.id.im_cancle);
		rlayout_choseSport = (RelativeLayout) findViewById(R.id.rlayout_choseSport);
		rlayout_startTime = (RelativeLayout) findViewById(R.id.rlayout_startTime);
		rlayout_lastTime = (RelativeLayout) findViewById(R.id.rlayout_lastTime);

		im_back.setOnClickListener(this);
		rlayout_choseSport.setOnClickListener(this);
		rlayout_startTime.setOnClickListener(this);
		rlayout_lastTime.setOnClickListener(this);
		im_complete.setOnClickListener(this);
		im_delete.setOnClickListener(this);
		im_edit_ok.setOnClickListener(this);
		im_cancle.setOnClickListener(this);

		tv_run.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				hasChanged = true;
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
		tv_start_time.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				hasChanged = true;
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
		tv_last_time.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				hasChanged = true;
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});

		hourData = getResources().getStringArray(R.array.hour);
		minuteData = getResources().getStringArray(R.array.minute);
		lastMinuteData = getResources().getStringArray(R.array.last_minute);
		lastHourData = getResources().getStringArray(R.array.duration_hour);
		sportArray = getResources().getStringArray(R.array.whole_sport_type);

		intent = getIntent();
		sportStartTime = intent.getStringExtra("sportStartTime");
		date = intent.getStringExtra("date");
		id = intent.getLongExtra("id", 0);
		if (sportStartTime != null) {
			sportNum = intent.getIntExtra("sportType", 0);
			sportIndex = sportNum - 1;
			sportType = sportArray[sportNum - 1];
			durationTime = intent.getStringExtra("sportDuration");
			wasteCalories = intent.getStringExtra("wasteCalories");
			getStartIntTime(sportStartTime);
			wasteKll = Integer.parseInt(wasteCalories);
			startTime = startHour * 60 + startOther;

			lastTime = Integer.parseInt(durationTime);
			if (lastTime >= 60) {
				durationHour = lastTime / 60;
				durationOther = lastTime % 60;
				tv_last_time.setText(durationHour + "小时" + durationOther + "分钟");
			} else {
				durationHour = 00;
				durationOther = lastTime;
				tv_last_time.setText(durationOther + "分钟");
			}
			tv_add_sport.setText(R.string.edit_sport);
			im_delete.setVisibility(View.VISIBLE);
			im_edit_ok.setVisibility(View.VISIBLE);
			im_complete.setVisibility(View.GONE);
			tv_run.setText(sportType);
			tv_start_time.setText(sportStartTime);

			tv_waste_kll.setText(wasteCalories);
		}else{
			sportIndex = 28;
		}
	}

	@Override
	public void onClick(View v) {
		final WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.alpha = 0.7f;
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.rlayout_choseSport:
			sportChose = new SportChosePopoWindow(AddSports.this,sportArray[sportIndex]);
			sportChose.showAtLocation(rLayout, Gravity.BOTTOM
					| Gravity.CENTER_HORIZONTAL, 0, 0);
			getWindow().setAttributes(lp);
			sportChose.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					lp.alpha = 1.0f;
					getWindow().setAttributes(lp);
					sportType = sportChose.getSport();
					sportIndex = Tools.getSportIndex(sportArray, sportType);
					tv_run.setText(sportType);

					if (sportType == null) {
						wasteKll = 9 * lastTime;
						tv_waste_kll.setText(wasteKll + "");
					} else {
						wasteKll = Tools.getSportKll(sportIndex, lastTime);
						tv_waste_kll.setText(wasteKll + "");
					}

				}
			});

			break;
		case R.id.rlayout_startTime:
			startHour = startTime / 60;
			startOther = startTime % 60;

			startPopuWindow = new SportTimePopupWindow(AddSports.this,
					new MyWheelView(hourData), new MyWheelView(minuteData),
					startHour, startOther);
			startPopuWindow.showAtLocation(rLayout, Gravity.BOTTOM
					| Gravity.CENTER_HORIZONTAL, 0, 0);
			getWindow().setAttributes(lp);
			startPopuWindow.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					lp.alpha = 1.0f;
					getWindow().setAttributes(lp);
					startTime = startPopuWindow.getStartTime();
					if (startTime >= 60) {
						if(startTime % 60 < 10){
							sportStartTime = startTime / 60  + ":0" + startTime % 60;
						}else{
							sportStartTime = startTime / 60 + ":" + startTime % 60;
						}
					} else {
						if (startTime == 0) {
							sportStartTime = "请选择";
						} else if (startTime < 10) {
							sportStartTime = "00:0" + startTime;
						}else{
							sportStartTime = "00:" + startTime;
						}
					}
					tv_start_time.setText(sportStartTime);
				}
			});
			break;
		case R.id.rlayout_lastTime:
			durationHour = lastTime / 60;
			durationOther = lastTime % 60;
			stopPopuWindow = new DurationPopupWindow(AddSports.this,
					new MyWheelView(lastHourData), new MyWheelView(
							lastMinuteData), durationHour, durationOther);
			stopPopuWindow.showAtLocation(rLayout, Gravity.BOTTOM
					| Gravity.CENTER_HORIZONTAL, 0, 0);
			getWindow().setAttributes(lp);
			stopPopuWindow.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					// TODO Auto-generated method stub
					lp.alpha = 1.0f;
					getWindow().setAttributes(lp);
					lastTime = stopPopuWindow.getLastTime();
					if (lastTime >= 60) {
						tv_last_time.setText(lastTime / 60 + "小时" + lastTime
								% 60 + "分钟");
					} else if (lastTime == 0) {
						tv_last_time.setText("请选择");
					} else {
						tv_last_time.setText(lastTime + "分钟");
					}

					if (sportType == null) {
						wasteKll = 9 * lastTime;
						tv_waste_kll.setText(wasteKll + "");
					} else {
						wasteKll = Tools.getSportKll(sportIndex, lastTime);
						tv_waste_kll.setText(wasteKll + "");
					}

				}
			});

			break;
		case R.id.im_edit_ok:
			endTime = getEndTime(startTime, lastTime);
			if(hasChanged){
				updateDateBaseSport(sportStartTime, sportIndex + 1, lastTime + "",
					endTime,wasteKll + "", date);
			}
			finish();
			break;
		case R.id.im_complete:

			if (tv_start_time.getText().equals("请选择")) {
				Toast.makeText(this, "请输入开始时间.", 2000).show();
			} else if (tv_last_time.getText().equals("请选择")) {
				Toast.makeText(this, "请输入持续时间.", 2000).show();
			} else {
				endTime = getEndTime(startTime, lastTime);
				insertDataBaseSportType(date, sportStartTime,
						sportIndex + 1, lastTime + "", endTime, wasteKll + "",
						2, 0);
				finish();
			}

			break;
		case R.id.im_delete:
			endTime = getEndTime(startTime, lastTime);
			deleteDateBaseSport(sportStartTime, sportNum, lastTime + "",
					endTime, wasteKll + "", date);
			finish();
			break;
		case R.id.im_cancle:
			finish();
			break;
		default:
			break;
		}

	}

	public class MyWheelView extends BaseAdapter {
		String[] data;
		int mHeight = 50;
		int selectPos;
		public MyWheelView(String[] data) {
			this.data = data;
			mHeight = (int) Tools.dip2px(AddSports.this, mHeight);
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
				convertView = new TextView(AddSports.this);
				convertView.setLayoutParams(new TosGallery.LayoutParams(-1,
						mHeight));
				txtView = (TextView) convertView;
				txtView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
				txtView.setTextColor(Color.BLACK);
				txtView.setGravity(Gravity.CENTER);
			}
			if (null == txtView) {
				txtView = (TextView) convertView;
			}
			
			if(position == selectPos){
				txtView.setTextColor(0xFF40E0D0);
			}else{
				txtView.setTextColor(0xFF616161);
			}

			txtView.setText(data[position]);
			return convertView;
		}

	}

	// 向数据库中插入一条 运动的信息。
	private void insertDataBaseSportType(String date, String startTime,
			int sportType, String duration, String endTime, String calories,
			int type, int statistics) {
		ContentValues runningItem = new ContentValues();
		runningItem.put(DataBaseContants.ID, Tools.getPKL());
		runningItem.put(DataBaseContants.DATE, date);
		runningItem.put(DataBaseContants.TIME_START, startTime);
		runningItem.put(DataBaseContants.TIME_DURATION, duration);
		runningItem.put(DataBaseContants.TIME_END, endTime);
		runningItem.put(DataBaseContants.SPORTS_TYPE, sportType);
		runningItem.put(DataBaseContants.CALORIES, calories);
		runningItem.put(DataBaseContants.TYPE, type);
		runningItem.put(DataBaseContants.STATISTICS, statistics);
		this.getContentResolver().insert(DataBaseContants.CONTENT_URI,
				runningItem);
	}

	// 更新数据库中的一条 运动信息
	private void updateDateBaseSport(String startTime, int sportType,
			String duration, String endTime,String calories, String date) {
		ContentResolver cr = this.getContentResolver();
		ContentValues updateValues = new ContentValues();
		updateValues.put(DataBaseContants.TIME_START, startTime);
		updateValues.put(DataBaseContants.TIME_DURATION, duration);
		updateValues.put(DataBaseContants.TIME_END, endTime);
		updateValues.put(DataBaseContants.SPORTS_TYPE, sportType);
		updateValues.put(DataBaseContants.CALORIES, calories);
		updateValues.put(DataBaseContants.SYNC_STATE, 2);
		cr.update(DataBaseContants.CONTENT_URI, updateValues,
				DataBaseContants.ID + " = ?" + " and " + DataBaseContants.SPORTS_TYPE + " = ?" + " and " + DataBaseContants.DATE + " = ?", 
				new String[] { String.valueOf(id), String.valueOf(sportNum), date });
	}

	// 删除数据库中的一条 运动信息
	private void deleteDateBaseSport(String startTime, int sportType,
			String duration, String endTime, String calories, String date) {
		ContentResolver cr = this.getContentResolver();
		cr.delete(DataBaseContants.CONTENT_URI, 
				DataBaseContants.ID + " = ?" + " and " + DataBaseContants.SPORTS_TYPE + " = ?" + " and " + DataBaseContants.DATE + " = ?", 
				new String[] { String.valueOf(id), String.valueOf(sportType), date });
		
		ContentValues values = new ContentValues();
		values.put(DataBaseContants.DELETE_VALUE, id);
		cr.insert(DataBaseContants.CONTENT_DELETE_URI, values);
	}

	/**
	 * get sport EndTime
	 */
	private String getEndTime(int startTime, int lastTime) {
		String endTime;
		if (startTime + lastTime >= 60) {
			if ((startTime + lastTime) / 60 >= 24) {
				if ((startTime + lastTime) % 60 < 10)
					endTime = ((startTime + lastTime) / 60 - 24) + ":0" + (startTime + lastTime) % 60;
				else
					endTime = ((startTime + lastTime) / 60 - 24) + ":" + (startTime + lastTime) % 60;
			} else {
				if ((startTime + lastTime) % 60 < 10)
					endTime = (startTime + lastTime) / 60 + ":0" + (startTime + lastTime) % 60;
				else
					endTime = (startTime + lastTime) / 60 + ":" + (startTime + lastTime) % 60;
			}
		} else {
			if ((startTime + lastTime) < 10)
				endTime = "00:0" + (startTime + lastTime);
			else
				endTime = "00:" + (startTime + lastTime);
		}
		return endTime;
	}

	/**
	 * 得到sportStart的int小时值，int分钟值。
	 */
	private void getStartIntTime(String sportStartTime) {
		String[] time = sportStartTime.split(":");
		String hour = time[0];
		String other = time[1];
		if (other.startsWith("0")) {
			this.startHour = Integer.parseInt(hour.toString());
			other = other.substring(1);
			this.startOther = Integer.parseInt(other.toString());
		} else {
			this.startHour = Integer.parseInt(hour.toString());
			this.startOther = Integer.parseInt(other.toString());
		}
	}
	
}
