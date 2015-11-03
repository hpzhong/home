package com.zhuoyou.plugin.mainFrame;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.add.SportTimePopupWindow;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.SleepBean;
import com.zhuoyou.plugin.running.SleepItem;
import com.zhuoyou.plugin.selfupdate.TerminalInfo;
import com.zhuoyou.plugin.view.BarChartSleep;

public class SleepDetailActivity extends Activity implements OnClickListener {

	private SleepItem item;
	/** Program UI */
	private RelativeLayout rLayout;
	private TextView tv_SleepText; //睡眠类型
	private TextView tv_SleepRanger; // 睡眠时间范围
	private ImageView mSleepLine;//睡眠竖线
	private TextView tv_GoSleep; // 入睡时间
	private TextView tv_WakeUp; // 醒来时间
	private TextView tv_SleepTime; // 睡眠时长
	private TextView tv_DeepSleepTime; // 深睡时长
	private TextView tv_LightSleepTime; // 浅睡时长
	private TextView tv_GoSleepTime; // 入睡时间
	private TextView tv_WakeUpTime; // 醒来时间
	private LinearLayout mBarchartLayout; // 柱状图Layout
	private SportTimePopupWindow startPopuWindow;
	private List<SleepBean> turnData;
	
	private Context mCtx=RunningApp.getInstance().getApplicationContext();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sleep_detail_activity);
		findView();
		initData();
		setBarView();
		tv_SleepText.setText("");
		tv_SleepRanger.setText("");
	}

	private void findView() {
		rLayout = (RelativeLayout)findViewById(R.id.layout_sleep);
		tv_SleepText = (TextView) findViewById(R.id.sleep_text);
		tv_SleepRanger = (TextView) findViewById(R.id.tv_SleepRanger);
		mSleepLine = (ImageView) findViewById(R.id.sleep_line);
		tv_GoSleep = (TextView) findViewById(R.id.tv_GoSleep);
		tv_WakeUp = (TextView) findViewById(R.id.tv_WakeUp);
		tv_SleepTime = (TextView) findViewById(R.id.tv_SleepTime);
		tv_DeepSleepTime = (TextView) findViewById(R.id.tv_DeepSleepTime);
		tv_LightSleepTime = (TextView) findViewById(R.id.tv_LightSleepTime);
		tv_GoSleepTime = (TextView) findViewById(R.id.tv_GoSleepTime);
		tv_WakeUpTime = (TextView) findViewById(R.id.tv_WakeUpTime);
		mBarchartLayout = (LinearLayout)findViewById(R.id.layout_sleep_barchart_draw);
	}
	
	private void initData() {
		Intent  intent = getIntent();
		item = (SleepItem) intent.getSerializableExtra("item");
		turnData = item.getData();
		
		int sleepTime = item.getmSleepT()/60;
		String sleepString = ((int)sleepTime/60) + mCtx.getString(R.string.hour) + ((int)sleepTime%60) + mCtx.getString(R.string.minute) ;
		tv_SleepTime.setText(sleepString);
		
		int deepTime = item.getmDSleepT()/60;
		String deepString = ((int)deepTime/60) + mCtx.getString(R.string.hour) + ((int)deepTime%60) + mCtx.getString(R.string.minute) ;
		tv_DeepSleepTime.setText(deepString);
		
//		int lightTime = item.getmWSleepT()/60;
		int lightTime =sleepTime - deepTime  ;
		String lightString = ((int)lightTime/60) + mCtx.getString(R.string.hour) + ((int)lightTime%60) + mCtx.getString(R.string.minute) ;
		tv_LightSleepTime.setText(lightString);
		
		String startTime = item.getmStartT();
		String endTime = item.getmEndT();
		tv_SleepRanger.setText(startTime + "-" + endTime);
		tv_GoSleep.setText(startTime);
		tv_WakeUp.setText(endTime);
		tv_GoSleepTime.setText(startTime);
		tv_WakeUpTime.setText(endTime);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}



	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sleep_gosleep_time:
			showPopupWindow1();
			break;
		case R.id.sleep_wakeup_time:
			showPopupWindow2();
			break;
		case R.id.title_share:
			Intent intent = new Intent(SleepDetailActivity.this,SleepShareActivity.class);
			intent.putExtra("item",item);
			startActivity(intent);
			break;
			
		case R.id.title_delete:
//			DBOpenHelper dbHelper = new DBOpenHelper(SleepDetailActivity.this);
//			SQLiteDatabase db = dbHelper.getWritableDatabase();
//			String whereClause = DataBaseContants.SLEEP_ID + " = ? ";
//			String[] whereArgs = {""+item.getId()};
//			db.delete(DataBaseContants.TABLE_SLEEP, whereClause, whereArgs);
//			finish();
			break;
		case R.id.back_m:
			finish();
			break;
		default:
			break;
		}
	}
	
	private void showPopupWindow1(){
		final DecimalFormat mFormat = new DecimalFormat("#00");
		String[] time = ((String) tv_GoSleepTime.getText()).split(":");
		final WindowManager.LayoutParams lp = getWindow().getAttributes();
		int startHour = Integer.valueOf(time[0]);
		int startOther= Integer.valueOf(time[1]);
		startPopuWindow = new SportTimePopupWindow(SleepDetailActivity.this, startHour, startOther);
		startPopuWindow.setColor(0xFF000000);
		startPopuWindow.showAtLocation(rLayout, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
		lp.alpha = 0.7f;
		getWindow().setAttributes(lp);
		startPopuWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				lp.alpha = 1.0f;
				getWindow().setAttributes(lp);
				int time = startPopuWindow.getStartTime();
				String textTime = mFormat.format((int)(time/60))+":"+mFormat.format((int)(time%60));
				tv_GoSleepTime.setText(textTime);
			}
		});
	}
	
	private void showPopupWindow2(){
		final DecimalFormat mFormat = new DecimalFormat("#00");
		String[] time = ((String) tv_WakeUpTime.getText()).split(":");
		final WindowManager.LayoutParams lp = getWindow().getAttributes();
		int startHour = Integer.valueOf(time[0]);
		int startOther= Integer.valueOf(time[1]);
		startPopuWindow = new SportTimePopupWindow(SleepDetailActivity.this, startHour, startOther);
		startPopuWindow.setColor(0xFF000000);
		startPopuWindow.showAtLocation(rLayout, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
		lp.alpha = 0.7f;
		getWindow().setAttributes(lp);
		startPopuWindow.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				lp.alpha = 1.0f;
				getWindow().setAttributes(lp);
				int time = startPopuWindow.getStartTime();
				String textTime = mFormat.format((int)(time/60))+":"+mFormat.format((int)(time%60));
				tv_WakeUpTime.setText(textTime);
			}
		});
	}
	
	private void setBarView() {
		
		if(turnData!=null && turnData.size()> 0){
			BarChartSleep bar = new BarChartSleep(this, item, handler);
			LayoutParams params = mBarchartLayout.getLayoutParams();
			bar.setLayoutParams(params);
			mBarchartLayout.removeAllViews();
			mBarchartLayout.addView(bar);
			bar.invalidate();
		}
	}
	
	private Handler handler = new Handler() {
//		Animation sleepAnim1 = AnimationUtils.loadAnimation(SleepDetailActivity.this, R.anim.sleep_alpha);
		
		@Override
		public void handleMessage(Message msg) {
			Log.i("hello", "msg:"+msg.what);
			switch (msg.what) {
			case 1: 
				SleepBean bean = (SleepBean)msg.obj;
				if(bean.isDeep()) {
		        	tv_SleepText.setText(R.string.deep_sleep);
		        	tv_SleepRanger.setText(bean.getStartTime() + " - "+bean.getEndTime());
		        	mSleepLine.setVisibility(View.VISIBLE);
				} else {
					tv_SleepText.setText(R.string.light_sleep);
					tv_SleepRanger.setText(bean.getStartTime() +" - "+bean.getEndTime());
		        	mSleepLine.setVisibility(View.VISIBLE);
                }
				break;
			case 2:
				tv_SleepText.setText("");
				tv_SleepRanger.setText("");
				mSleepLine.setVisibility(View.GONE);
				break;

			default:
				break;
			}
		}
	};
	
}
