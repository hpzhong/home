package com.zhuoyou.plugin.add;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.Tools;

public class AddSports extends Activity implements OnClickListener {
	private LinearLayout rLayout;
	private RelativeLayout im_back;
	private Button mButton;
	private TextView tv_waste_kll, tv_run, tv_start_date, tv_start_time, tv_last_time,
			tv_add_sport;
	private boolean isEdit;
	private DateSelectPopupWindow datePopuWindow;
	private SportTimePopupWindow startPopuWindow;
	private DurationPopupWindow stopPopuWindow;
	private ChooseSportPopoWindow sportChose;
	private String sportType, endTime, sportStartTime, durationTime;
	private String[] sportArray = new String[40];
	private int wasteKll, lastTime, startTime, sportIndex;
	private Intent intent;
	private String wasteCalories;
	private int durationHour, durationOther, startHour, startOther;
	private int sportNum;
	private String date;
	private long id;
	private boolean hasChanged = false;
	private RelativeLayout rlayout_choseSport,rlayout_startDate,rlayout_startTime,rlayout_lastTime;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_sport);
		rLayout = (LinearLayout) findViewById(R.id.rlayout);
		im_back = (RelativeLayout) findViewById(R.id.back);
		im_back.setOnClickListener(this);
		mButton = (Button) findViewById(R.id.save);
		mButton.setOnClickListener(this);
		tv_waste_kll = (TextView) findViewById(R.id.kll_count);
		tv_run = (TextView) findViewById(R.id.tv_run);
		tv_start_date = (TextView) findViewById(R.id.tv_start_date);
		tv_start_time = (TextView) findViewById(R.id.tv_chose_start);
		tv_last_time = (TextView) findViewById(R.id.tv_chose_close);
		tv_add_sport = (TextView) findViewById(R.id.title);
		tv_add_sport.setText(R.string.add_sport);
		rlayout_choseSport = (RelativeLayout) findViewById(R.id.rlayout_choseSport);
		rlayout_startDate = (RelativeLayout) findViewById(R.id.rlayout_startDate);
		rlayout_startTime = (RelativeLayout) findViewById(R.id.rlayout_startTime);
		rlayout_lastTime = (RelativeLayout) findViewById(R.id.rlayout_lastTime);

		rlayout_choseSport.setOnClickListener(this);
		rlayout_startDate.setOnClickListener(this);
		rlayout_startTime.setOnClickListener(this);
		rlayout_lastTime.setOnClickListener(this);
		sportArray = getResources().getStringArray(R.array.whole_sport_type);

		intent = getIntent();
		sportStartTime = intent.getStringExtra("sportStartTime");
		date = intent.getStringExtra("date");
		id = intent.getLongExtra("id", 0);
		if (date.equals(Tools.getDate(0))) {
			tv_start_date.setText(R.string.today);
		} else {
			tv_start_date.setText(date);
		}
		if (sportStartTime != null) {
			isEdit = true;
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
				tv_last_time.setText(durationHour + getResources().getString(R.string.hour) + durationOther + getResources().getString(R.string.minute));
			} else {
				durationHour = 00;
				durationOther = lastTime;
				tv_last_time.setText(durationOther + getResources().getString(R.string.minute));
			}
			mButton.setText(R.string.gpsdata_delete);
			tv_add_sport.setText(R.string.edit_sport);
			tv_run.setText(sportType);
			tv_start_time.setText(sportStartTime);
			tv_waste_kll.setText(wasteCalories);
		}else{
			isEdit = false;
			sportIndex = 28;
			mButton.setText(R.string.ok);
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
		case R.id.save:
			if (isEdit) {
				if (hasChanged) {
					String start_edit = tv_start_time.getText().toString();
					String last_edit = tv_last_time.getText().toString();
					if (start_edit.equals(getResources().getString(R.string.choice))) {
						Toast.makeText(this, R.string.ps_starttime, Toast.LENGTH_SHORT).show();
					} else if (last_edit.equals(getResources().getString(R.string.choice))) {
						Toast.makeText(this, R.string.ps_duration, Toast.LENGTH_SHORT).show();
					} else {
						endTime = getEndTime(startTime, lastTime);
						updateDateBaseSport(sportStartTime, sportIndex + 1, lastTime + "", endTime,wasteKll + "", date);
						finish();
					}
				} else {
					deleteDateBaseSport();
					finish();
				}
			} else {
				String start = tv_start_time.getText().toString();
				String last = tv_last_time.getText().toString();
				if (start.equals(getResources().getString(R.string.choice))) {
					Toast.makeText(this, R.string.ps_starttime, Toast.LENGTH_SHORT).show();
				} else if (last.equals(getResources().getString(R.string.choice))) {
					Toast.makeText(this, R.string.ps_duration, Toast.LENGTH_SHORT).show();
				} else {
					endTime = getEndTime(startTime, lastTime);
					insertDataBaseSportType(date, sportStartTime, sportIndex + 1, lastTime + "", endTime, wasteKll + "", 2, 0);
					finish();
				}
			}
			break;
		case R.id.rlayout_choseSport:
			final int finalIndex = sportIndex;
			sportChose = new ChooseSportPopoWindow(AddSports.this, sportArray[finalIndex]);
			sportChose.showAtLocation(rLayout, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
			getWindow().setAttributes(lp);
			sportChose.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					lp.alpha = 1.0f;
					getWindow().setAttributes(lp);
					sportType = sportChose.getSport();
					sportIndex = Tools.getSportIndex(sportArray, sportType);
					if (finalIndex != sportIndex) {
						hasChanged = true;
						mButton.setText(R.string.ok);
					}
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
		case R.id.rlayout_startDate:
			final String finalDate = date;
			datePopuWindow = new DateSelectPopupWindow(AddSports.this, finalDate);
			datePopuWindow.setColor(0xff62aa43);
			datePopuWindow.showAtLocation(rLayout, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
			getWindow().setAttributes(lp);
			datePopuWindow.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss() {
					lp.alpha = 1.0f;
					getWindow().setAttributes(lp);
					date = datePopuWindow.getStartDate();
					if (!date.equals(finalDate)) {
						hasChanged = true;
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
		case R.id.rlayout_startTime:
			final int finalStartTime = startTime;
			startHour = finalStartTime / 60;
			startOther = finalStartTime % 60;

			startPopuWindow = new SportTimePopupWindow(AddSports.this, startHour, startOther);
			startPopuWindow.setColor(0xff62aa43);
			startPopuWindow.showAtLocation(rLayout, Gravity.BOTTOM
					| Gravity.CENTER_HORIZONTAL, 0, 0);
			getWindow().setAttributes(lp);
			startPopuWindow.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					lp.alpha = 1.0f;
					getWindow().setAttributes(lp);
					startTime = startPopuWindow.getStartTime();
					if (finalStartTime != startTime) {
						hasChanged = true;
						mButton.setText(R.string.ok);
					}
					if (startTime >= 60) {
						if(startTime % 60 < 10){
							sportStartTime = startTime / 60  + ":0" + startTime % 60;
						}else{
							sportStartTime = startTime / 60 + ":" + startTime % 60;
						}
					} else {
						if (startTime == 0) {
							sportStartTime = "00:00";
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
			final int finalLastTime = lastTime;
			durationHour = finalLastTime / 60;
			durationOther = finalLastTime % 60;
			stopPopuWindow = new DurationPopupWindow(AddSports.this, durationHour, durationOther);
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
					if (finalLastTime != lastTime) {
						hasChanged = true;
						mButton.setText(R.string.ok);
					}
					if (lastTime >= 60) {
						tv_last_time.setText(lastTime / 60 + getResources().getString(R.string.hour) + lastTime
								% 60 + getResources().getString(R.string.minute));
					} else if (lastTime == 0) {
						tv_last_time.setText(R.string.choice);
					} else {
						tv_last_time.setText(lastTime + getResources().getString(R.string.minute));
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
		default:
			break;
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
		Cursor c = cr.query(DataBaseContants.CONTENT_URI, new String[] { "sync" }, DataBaseContants.ID + " = ? " , new String[] { String.valueOf(id) }, null);
		if (c.getCount() > 0 && c.moveToFirst()) {
			int sync = c.getInt(c.getColumnIndex(DataBaseContants.SYNC_STATE));
			if (sync == 0) {
				updateValues.put(DataBaseContants.DATE, date);
				updateValues.put(DataBaseContants.TIME_START, startTime);
				updateValues.put(DataBaseContants.TIME_DURATION, duration);
				updateValues.put(DataBaseContants.TIME_END, endTime);
				updateValues.put(DataBaseContants.SPORTS_TYPE, sportType);
				updateValues.put(DataBaseContants.CALORIES, calories);
				updateValues.put(DataBaseContants.SYNC_STATE, 0);
			} else {
				updateValues.put(DataBaseContants.DATE, date);
				updateValues.put(DataBaseContants.TIME_START, startTime);
				updateValues.put(DataBaseContants.TIME_DURATION, duration);
				updateValues.put(DataBaseContants.TIME_END, endTime);
				updateValues.put(DataBaseContants.SPORTS_TYPE, sportType);
				updateValues.put(DataBaseContants.CALORIES, calories);
				updateValues.put(DataBaseContants.SYNC_STATE, 2);
			}
		}
		c.close();
		c = null;
		cr.update(DataBaseContants.CONTENT_URI, updateValues, DataBaseContants.ID + " = ?", new String[] { String.valueOf(id) });
	}

	// 删除数据库中的一条 运动信息
	private void deleteDateBaseSport() {
		ContentResolver cr = this.getContentResolver();
		cr.delete(DataBaseContants.CONTENT_URI, DataBaseContants.ID + " = ?", new String[]{ String.valueOf(id) });
		
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
