package com.zhuoyou.plugin.running;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.zhuoyou.plugin.database.DBOpenHelper;
import com.zhuoyou.plugin.database.DataBaseContants;

@SuppressLint("SdCardPath")
public class SleepPageFragment extends Fragment implements OnPageChangeListener {
	//modefy by zhouzhongbo@20150106 start
	private TextView title_bar_text;
	private Button mSleep;
	public static SleepPageFragment mInstance;
	private Context mCtx = RunningApp.getInstance();
	private View mRootView;
	private List<String> date_list = new ArrayList<String>();
	public String firstDay = "";
	private SleepPageAdapter mSleepAdapter = null;
	private ViewPager mViewPager;
	private static final int NOTIFY_DATASET_CHANGED = 5;
	public static Handler mHandler;	
	private DateChangeReceiver mDateChangeReceiver = new DateChangeReceiver();
	private UpdateHandler mUpdateHandler = new UpdateHandler(this);
	private ContentObserver mContentObserver = new ContentObserver(
			mUpdateHandler) {
		@Override
		public void onChange(boolean selfChange) {
			Log.i("gchk", "database changed!");
			if (mUpdateHandler != null) {
				Log.i("gchk", "send to target");
				// 由于一次载入的数据可能不止一条，此处消息delay 50再发送，并且先移除对应的消息
				mUpdateHandler.removeMessages(1008);
				Message msg = new Message();
				msg.what = 1008;
				mUpdateHandler.sendMessageDelayed(msg, 50);
			}
		}
	};
	public static class UpdateHandler extends Handler {
		WeakReference<SleepPageFragment> mMyFragment;

		public UpdateHandler(SleepPageFragment f) {
			mMyFragment = new WeakReference<SleepPageFragment>(f);
		}

		@Override
		public void handleMessage(Message msg) {
			Log.i("gchk", "UpdateHandler receiver msg");
			if (mMyFragment != null) {
				SleepPageFragment home = mMyFragment.get();
				if (home != null && home.mSleepAdapter != null) {
					Log.i("gchk", "update");
					home.initData();
				}
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("gchk","homepagefragment onCreateView");
		mRootView = inflater.inflate(R.layout.sleep_page, container, false);
		mRootView.setBackgroundColor(Color.parseColor("#fcfcfc"));
		mInstance = this;
		initView();
		mSleepAdapter = new SleepPageAdapter(getChildFragmentManager(), date_list);
		mViewPager.setAdapter(mSleepAdapter);
		
		return mRootView;
	}

	@Override
	public void onActivityCreated(Bundle paramBundle) {
		super.onActivityCreated(paramBundle);
    	mHandler = new Handler() {
    		public void handleMessage(Message msg) {
    			switch (msg.what) {
				case NOTIFY_DATASET_CHANGED:
					mSleepAdapter.notifyDataSetChanged(date_list);
					break;
    			}
    		}
    	};
	}

	private String formatRemoteDate(String old_date) {
		String new_date = old_date.substring(0, 4);
		new_date += "-";
		new_date += old_date.substring(4, 6);
		new_date += "-";
		new_date += old_date.substring(6, 8);
		return new_date;
	}

	private void initData() {
		date_list.clear();
		firstDay = "";
		long start = System.currentTimeMillis();
		String today = Tools.getDate(0);
		String enter_day = today;
		
		DBOpenHelper dbHelper = new DBOpenHelper(mCtx);
		SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
		Cursor mCursor = sqlDB.query(DataBaseContants.TABLE_SLEEP, new String[] { "_id", "start_time" } , null, null, null, null, DataBaseContants.SLEEP_STARTTIME + " ASC");
		mCursor.moveToFirst();
		int count = mCursor.getCount();
		if (count > 0) {
			firstDay = formatRemoteDate(mCursor.getString(mCursor.getColumnIndex(DataBaseContants.SLEEP_STARTTIME)));
		}
		if (firstDay.equals("")) {
			mCursor = sqlDB.query(DataBaseContants.TABLE_SLEEP_2, new String[] { "_id", "date" } , null, null, null, null, DataBaseContants.DATE + " ASC");
			mCursor.moveToFirst();
			int count2 = mCursor.getCount();
			if (count2 > 0) {
				firstDay = mCursor.getString(mCursor.getColumnIndex(DataBaseContants.DATE));
			}
		}
		sqlDB.close();
		mCursor.close();
		mCursor = null;
		
		if (!firstDay.equals(""))
			enter_day = firstDay;
		int count3 = Tools.getDayCount(enter_day, today, "yyyy-MM-dd");
		for (int i = 0; i < count3; i++) {
			String day = Tools.getDate(enter_day, 0 - i);
			date_list.add(day);
		}
		mSleepAdapter.notifyDataSetChanged(date_list);
		int index = date_list.size() - 1;
		mViewPager.setCurrentItem(index, false);
		long end = System.currentTimeMillis();
		Log.i("gchk", "getAllDate耗时" + (end - start));
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i("gchk", "home onResume");
		initData();
		mCtx.getContentResolver().registerContentObserver(
				DataBaseContants.CONTENT_URI, true, mContentObserver);
		IntentFilter intentf = new IntentFilter(Intent.ACTION_DATE_CHANGED);
		mCtx.registerReceiver(mDateChangeReceiver, intentf);
		mHandler.sendEmptyMessageDelayed(NOTIFY_DATASET_CHANGED, 50);
		int index = Tools.getSleepCurrPageIndex();
		if (index == 0) {
			mViewPager.setCurrentItem(date_list.size() - 1, false);
			if (date_list.size() == 1) {
				setTitle(Tools.getDate(0));
			}
		} else {
			mViewPager.setCurrentItem(index, false);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i("gchk", "home onPause");
		mCtx.getContentResolver().unregisterContentObserver(mContentObserver);
		mCtx.unregisterReceiver(mDateChangeReceiver);
		int index = mViewPager.getCurrentItem();
		Tools.setSleepCurrPageIndex(index);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {
		String curr_date = date_list.get(arg0);
		setTitle(curr_date);
	}

	private void setTitle(String title) {
		if (title.equals(Tools.getDate(0))) {
			title_bar_text.setText(R.string.today);
		} else {
			title_bar_text.setText(title.substring(5));
		}
	}

	public class DateChangeReceiver extends BroadcastReceiver {
		private static final String ACTION_DATE_CHANGED = Intent.ACTION_DATE_CHANGED;

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			String action = arg1.getAction();
			if (ACTION_DATE_CHANGED.equals(action)) {
				Log.d("gchk", "---DATE_CHANGED!---");
				mUpdateHandler.removeMessages(1008);
				Message msg = new Message();
				msg.what = 1008;
				mUpdateHandler.sendMessageDelayed(msg, 50);
			}
		}
	}

	//modefy by zhouzhongbo@20150106 start
	private void initView() {
		mViewPager = (ViewPager) mRootView.findViewById(R.id.main_viewpager);
		mViewPager.setOnPageChangeListener(this);
		
		title_bar_text = (TextView) mRootView.findViewById(R.id.title_bar_text);
		title_bar_text.setText(R.string.today);
		mSleep = (Button) mRootView.findViewById(R.id.sleep_btn);
		mSleep.setOnClickListener(btnSleep);
	}
	
	OnClickListener btnSleep = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Main.vFragment = true;
			if (Main.mHandler != null) {
				Main.mHandler.sendEmptyMessage(Main.SLEEP_FRAGMENT_HOME);
			}
		}
	};
}
