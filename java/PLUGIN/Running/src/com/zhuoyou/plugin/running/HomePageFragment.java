package com.zhuoyou.plugin.running;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.FileObserver;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.zhuoyou.plugin.add.AddPicture;
import com.zhuoyou.plugin.add.AddSports;
import com.zhuoyou.plugin.add.AddWeight;
import com.zhuoyou.plugin.add.AddWords;
import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.resideMenu.ResideMenu;
import com.zhuoyou.plugin.view.ArcMenu;
import com.zhuoyou.plugin.view.StatsCircleView;
import com.zhuoyou.plugin.weather.WeatherTools;

@SuppressLint("SdCardPath")
public class HomePageFragment extends Fragment implements OnPageChangeListener {
	private int[] item_drawables = new int[] { R.drawable.add_weight_selector,
			R.drawable.add_pic_selector, R.drawable.add_words_selector,
			R.drawable.add_sport_selector };
	private ArcMenu add_function;
	public static HomePageFragment mInstance;
	private Context mCtx = RunningApp.getInstance();
	private View mRootView;
	private ResideMenu resideMenu;
	// private Fragment mFragment;
	private HomePageAdapter mHomeAdapter = null;
	private ViewPager mViewPager;
	private int caloriesAddSport;
	private Map<String, Boolean> maps = new HashMap<String, Boolean>();
	private List<RunningItem> mRunningDays = new ArrayList<RunningItem>();
	private WeatherObserver mWeatherObserver = new WeatherObserver(
			"/data/data/" + mCtx.getPackageName() + "/shared_prefs/",
			FileObserver.MODIFY);
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
		WeakReference<HomePageFragment> mMyFragment;

		public UpdateHandler(HomePageFragment f) {
			mMyFragment = new WeakReference<HomePageFragment>(f);
		}

		@Override
		public void handleMessage(Message msg) {
			Log.i("gchk", "UpdateHandler receiver msg");
			if (mMyFragment != null) {
				HomePageFragment home = mMyFragment.get();
				if (home != null && home.mHomeAdapter != null) {
					Log.i("gchk", "update");
					home.initData();
				}
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.home_page, container, false);
		mRootView.setBackgroundColor(Color.parseColor("#fcfcfc"));
		mInstance = this;
		initView();
		return mRootView;
	}

	@Override
	public void onActivityCreated(Bundle paramBundle) {
		super.onActivityCreated(paramBundle);
	}

	void initAddSport(String day) {
		Boolean flag = false;
		caloriesAddSport = 0;
		int calories = 0;
		ContentResolver cr = mCtx.getContentResolver();
		Cursor cAddSport = cr.query(DataBaseContants.CONTENT_URI, new String[] {
				"_id", "calories", "sports_type", "type" },
				DataBaseContants.DATE + "  = ? AND "
						+ DataBaseContants.STATISTICS + " = ?", new String[] {
						day, "0" }, null);
		cAddSport.moveToFirst();
		if (cAddSport.getCount() > 0) {
			for (int y = 0; y < cAddSport.getCount(); y++) {
				if (cAddSport.getInt(cAddSport
						.getColumnIndex(DataBaseContants.TYPE)) == 2) {
					if (cAddSport.getInt(cAddSport
							.getColumnIndex(DataBaseContants.SPORTS_TYPE)) != 0) {
						calories = calories
								+ cAddSport
										.getInt(cAddSport
												.getColumnIndex(DataBaseContants.CALORIES));
						caloriesAddSport = calories;
					}
				}
				if (cAddSport.getCount() > 0
						&& cAddSport.getInt(cAddSport
								.getColumnIndex(DataBaseContants.TYPE)) == 1) {
					flag = true;
				}
				cAddSport.moveToNext();
			}
			if (flag) {
				maps.put(day, true);
			} else {
				maps.put(day, false);
			}
		} else {
			maps.put(day, false);
		}
		cAddSport.close();
		cAddSport = null;
	}

	private void initData() {
		mRunningDays.clear();
		maps.clear();
		long start = System.currentTimeMillis();
		String enter_day = Tools.getFirstEnterDay(mCtx);
		String today = Tools.getDate(0);
		int count = Tools.getDayCount(enter_day, today);
		ContentResolver cr = mCtx.getContentResolver();
		for (int i = 0; i < count; i++) {
			String day = Tools.getDate(enter_day, 0 - i);
			initAddSport(day);
			RunningItem runningdate = new RunningItem();
			Cursor c = cr.query(DataBaseContants.CONTENT_URI, new String[] {
					"_id", "date", "steps", "kilometer", "calories",
					"time_duration", "time_start", "time_end" },
					DataBaseContants.DATE + "  = ? AND "
							+ DataBaseContants.STATISTICS + " = ? ",
					new String[] { day, "1" }, null);
			c.moveToFirst();
			if (c.getCount() > 0 && c.moveToFirst()
					&& c.getLong(c.getColumnIndex(DataBaseContants.ID)) > 0) {
				runningdate.setDate(c.getString(c
						.getColumnIndex(DataBaseContants.DATE)));
				runningdate.setCalories(c.getInt(c
						.getColumnIndex(DataBaseContants.CALORIES))
						+ caloriesAddSport);
				runningdate.setSteps(c.getInt(c
						.getColumnIndex(DataBaseContants.STEPS)));
				runningdate.setisStatistics(true);
				runningdate.setKilometer(c.getInt(c
						.getColumnIndex(DataBaseContants.KILOMETER)));
				runningdate.setStartTime(c.getString(c
						.getColumnIndex(DataBaseContants.TIME_START)));
				runningdate.setEndTime(c.getString(c
						.getColumnIndex(DataBaseContants.TIME_END)));
				runningdate.setDuration(c.getString(c
						.getColumnIndex(DataBaseContants.TIME_DURATION)));
				runningdate.setPm25(Tools.getPm25(day));
			} else {
				runningdate.setDate(day);
				runningdate.setCalories(0 + caloriesAddSport);
				runningdate.setSteps(0);
				runningdate.setKilometer(0);
				runningdate.setStartTime("");
				runningdate.setEndTime("");
				runningdate.setDuration("");
				runningdate.setPm25(Tools.getPm25(day));
			}
			c.close();
			c = null;
			mRunningDays.add(runningdate);
		}
		mHomeAdapter.notifyDataSetChanged(mRunningDays);
		mViewPager.setCurrentItem(mRunningDays.size() - 1, false);
		long end = System.currentTimeMillis();
		Log.i("gchk", "getAllDate耗时" + (end - start));

		// 移除所有子图标，并重新添加一次，用于判断是否添加过体重. 
		// 判断是否 有体重添加 。若有，改变添加体重的图标。 
		int index = mViewPager.getCurrentItem();
		String curr_date = mRunningDays.get(index).getDate();
		if (maps.get(curr_date)) {
			item_drawables[0] = R.drawable.add_weight_down;
		} else {
			item_drawables[0] = R.drawable.add_weight_selector;
		}
		initArcMenu(add_function, item_drawables);
	}

	public void initView() {
		Log.i("gchk", "initView");
		Main parentActivity = (Main) getActivity();
		resideMenu = parentActivity.getResideMenu();

		mViewPager = (ViewPager) mRootView.findViewById(R.id.main_viewpager);
		mViewPager.setOnPageChangeListener(this);
		resideMenu.addIgnoredView(mViewPager);
		add_function = (ArcMenu) mRootView.findViewById(R.id.add_function);
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i("gchk", "home onResume");

		if (mHomeAdapter == null) {
			mHomeAdapter = new HomePageAdapter(getChildFragmentManager(),
					mRunningDays);
		}
		mViewPager.setAdapter(mHomeAdapter);

		if (mRunningDays.size() > 0) {
			mViewPager.setCurrentItem(mRunningDays.size() - 1, false);
		}

		initData();
		mWeatherObserver.startWatching();
		mCtx.getContentResolver().registerContentObserver(
				DataBaseContants.CONTENT_URI, true, mContentObserver);
		IntentFilter intentf = new IntentFilter(Intent.ACTION_DATE_CHANGED);
		mCtx.registerReceiver(mDateChangeReceiver, intentf);

	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i("gchk", "home onPause");
		mWeatherObserver.stopWatching();
		mCtx.getContentResolver().unregisterContentObserver(mContentObserver);
		mCtx.unregisterReceiver(mDateChangeReceiver);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		switch (arg0) {
		case 1:
			if (StatsCircleView.SwitchType.isChanged) {
				mHomeAdapter.notifyDataSetChanged();
				StatsCircleView.SwitchType.isChanged = false;
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {
		String curr_date = mRunningDays.get(arg0).getDate();
		if (curr_date.equals(Tools.getDate(0))) {
			setTitle(mCtx.getString(R.string.today));
		} else {
			setTitle(curr_date.substring(5));
		}
		Log.i("gchk", "onPageSelected title");

		if (maps.get(curr_date)) {
			item_drawables[0] = R.drawable.add_weight_down;
		} else {
			item_drawables[0] = R.drawable.add_weight_selector;
		}
		initArcMenu(add_function, item_drawables);
	}

	private void setTitle(String title) {
		((Main) getActivity()).updateTitle(title);
	}

	public void onTapLeft() {
		int index = mViewPager.getCurrentItem();
		if (index == 0) {
			Toast.makeText(mCtx, R.string.left_error_tip, Toast.LENGTH_SHORT)
					.show();
		} else {
			mViewPager.setCurrentItem(--index, true);
		}
	}

	public void onTapRight() {
		int index = mViewPager.getCurrentItem();
		if (index == mRunningDays.size() - 1) {
			Toast.makeText(mCtx, R.string.right_error_tip, Toast.LENGTH_SHORT)
					.show();
		} else {
			mViewPager.setCurrentItem(++index, true);
		}
	}

	public RunningItem getCurrPageData() {
		RunningItem data = null;
		if (mRunningDays != null) {
			data = mRunningDays.get(mViewPager.getCurrentItem());
		}

		return data;
	}

	public void toShareActivity() {
		RunningItem data = getCurrPageData();
		Intent intent = new Intent(getActivity(), ShareActivity.class);
		intent.putExtra("steps", data.getSteps());
		intent.putExtra("cals", data.getCalories());
		intent.putExtra("km", data.getKilometer());
		intent.putExtra("date", data.getDate());
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.in_from_right,
				R.anim.out_to_left);
	}

	public class WeatherObserver extends FileObserver {
		public WeatherObserver(String path, int mask) {
			super(path, mask);
			Log.i("gchk", "WeatherObserver = " + path);
		}

		@Override
		public void onEvent(int event, String path) {
			switch (event) {
			case FileObserver.MODIFY:
				if (path.startsWith(Tools.SP_PM25_FILENAME)) {
					Log.i("gchk", "MODIFY = " + path);

					if (mRunningDays != null && mRunningDays.size() > 0) {
						RunningItem item = mRunningDays
								.get(mRunningDays.size() - 1);
						String day = item.getDate();
						int pm = Tools.getPm25(day);
						item.setPm25(pm);
						if (mHomeAdapter != null) {
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									mHomeAdapter
											.notifyDataSetChanged(mRunningDays);
								}
							});
						}
					}
				}
				break;

			default:
				break;
			}
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

				// 新的一天到来后自动更新下PM25
				if (Tools.getPm25(Tools.getDate(0)) == 0) {
					WeatherTools.newInstance().getCurrAqi();
				}
			}
		}
	}

	private void initArcMenu(ArcMenu menu, int[] itemDrawables) {
		final int itemCount = itemDrawables.length;
		menu.removeItem();
		for (int i = 0; i < itemCount; i++) {
			ImageView item = new ImageView(mCtx);
			item.setImageResource(itemDrawables[i]);

			final int position = i;
			menu.addItem(item, new OnClickListener() {

				@Override
				public void onClick(View v) {
					int index = mViewPager.getCurrentItem();
					String curr_date = mRunningDays.get(index).getDate();
					switch (position) {
					case 0:
						if (maps.get(curr_date)) {
							return;
						}
						Intent weightIntent = new Intent();
						weightIntent.putExtra("date", curr_date);
						weightIntent.setClass(getActivity(), AddWeight.class);
						startActivityForResult(weightIntent, 0x001);
						break;
					case 1:
						Intent picIntent = new Intent();
						picIntent.putExtra("date", curr_date);
						picIntent.setClass(getActivity(), AddPicture.class);
						startActivityForResult(picIntent, 0x002);
						break;
					case 2:
						Intent wordIntent = new Intent();
						wordIntent.putExtra("date", curr_date);
						wordIntent.setClass(getActivity(), AddWords.class);
						startActivityForResult(wordIntent, 0x003);
						break;
					case 3:
						Intent sportIntent = new Intent();
						sportIntent.putExtra("date", curr_date);
						sportIntent.setClass(getActivity(), AddSports.class);
						startActivityForResult(sportIntent, 0x004);
						break;
					default:
						break;
					}
				}
			});
		}
	}

}
