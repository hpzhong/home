package com.zhuoyou.plugin.running;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyou.plugin.bluetooth.data.Util;
import com.zhuoyou.plugin.bluetooth.service.BluetoothService;
import com.zhuoyou.plugin.cloud.CloudSync;
import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.gps.ilistener.IStepListener;
import com.zhuoyou.plugin.gps.ilistener.StepWatcher;
import com.zhuoyou.plugin.weather.WeatherTools;

@SuppressLint("SdCardPath")
public class HomePageFragment extends Fragment implements OnPageChangeListener {
	//modefy by zhouzhongbo@20150106 start
	private TextView title_bar_text;
	private ImageView title_share, title_cloud;
	private RelativeLayout cal_lay;
	private CalendarView calPopupWindow;
	private String currDate = null;
	private TextView mHintTextView, mHeaderTimeViewTitle, mHeaderTimeView, mProgress;
	private RelativeLayout mListViewRel;
	private View mFooter;
	private Button mSleep;
	//HomePageItemFragment cur_fragment;
	//modefy by zhouzhongbo@20150106 start
	private RunningItem item;
	public static HomePageFragment mInstance;
	private Context mCtx = RunningApp.getInstance();
	private View mRootView;
	private List<String> date_list = new ArrayList<String>();
	public String firstDay = "";
	private HomePageAdapter mHomeAdapter = null;
	private ViewPager mViewPager;
	private int caloriesAddSport;
	private List<RunningItem> mRunningDays = new ArrayList<RunningItem>();
	public static final int SYNC_DEVICE_START = 1;
	public static final int SYNC_DEVICE_SUCCESSED = 2;
	public static final int SYNC_DEVICE_FAILED = 3;
	public static final int SYNC_DEVICE_PROGRESS = 4;
	private static final int NOTIFY_DATASET_CHANGED = 5;
	public static Handler mHandler;	
	private Map<String, String> weight;
	private Map<String, Integer> steps;
	private StepObserver mStepObserver;		// 步数 观察者
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
		Log.d("gchk","homepagefragment onCreateView");
		mRootView = inflater.inflate(R.layout.home_page, container, false);
		mRootView.setBackgroundColor(Color.parseColor("#fcfcfc"));
		mInstance = this;
		initView();
//		if (mHomeAdapter == null) {
			mHomeAdapter = new HomePageAdapter(getChildFragmentManager(),
					mRunningDays, weight, steps);
//		}
		mViewPager.setAdapter(mHomeAdapter);
		mStepObserver = new StepObserver();
//		StepWatcher.getInstance().addWatcher(mStepObserver);
		
		return mRootView;
	}

	@Override
	public void onActivityCreated(Bundle paramBundle) {
		super.onActivityCreated(paramBundle);
    	mHandler = new Handler() {
    		public void handleMessage(Message msg) {
    			int type = 0;
    			switch (msg.what) {
    			case SYNC_DEVICE_START:
    				type = msg.arg1;
    				refreshView(true, type);
    				break;
				case SYNC_DEVICE_SUCCESSED:
					type = msg.arg1;
					refreshView(false, type);
					setLastUpdateTime(type);
					break;
				case SYNC_DEVICE_FAILED:
					type = msg.arg1;
					refreshView(false, type);
					break;
				case SYNC_DEVICE_PROGRESS:
					String pro = (String) msg.obj;
					if (mProgress != null) {
						mProgress.setText(pro);
					}
					break;
				case NOTIFY_DATASET_CHANGED:
					mHomeAdapter.notifyDataSetChanged(mRunningDays, weight, steps);
					break;
    			}
    		}
    	};
	}

	void initAddSport(String day) {
		caloriesAddSport = 0;
		int calories = 0;
		ContentResolver cr = mCtx.getContentResolver();
		Cursor cAddSport = cr.query(DataBaseContants.CONTENT_URI, new String[] { "_id", "date", "calories", "weight", "sports_type", "type" },
				DataBaseContants.DATE + "  = ? AND " + DataBaseContants.STATISTICS + " = ?", new String[] {day, "0" }, null);
		cAddSport.moveToFirst();
		if (cAddSport.getCount() > 0) {
			for (int y = 0; y < cAddSport.getCount(); y++) {
				if (cAddSport.getInt(cAddSport.getColumnIndex(DataBaseContants.TYPE)) == 2) {
					if (cAddSport.getInt(cAddSport.getColumnIndex(DataBaseContants.SPORTS_TYPE)) != 0) {
						calories = calories + cAddSport.getInt(cAddSport.getColumnIndex(DataBaseContants.CALORIES));
						caloriesAddSport = calories;
					}
				} else if (cAddSport.getInt(cAddSport.getColumnIndex(DataBaseContants.TYPE)) == 1) {
					if (weight.get(cAddSport.getString(cAddSport.getColumnIndex(DataBaseContants.DATE))) == null) {
						weight.put(cAddSport.getString(cAddSport.getColumnIndex(DataBaseContants.DATE)), cAddSport.getString(cAddSport.getColumnIndex(DataBaseContants.CONF_WEIGHT)));
					}
				}
				cAddSport.moveToNext();
			}
		}
		cAddSport.close();
		cAddSport = null;
	}

	private static int PHONE_STEP = 0;
	private void readPhoneStep(String day) {
		ContentResolver cr = mCtx.getContentResolver();
		Cursor c = cr.query(DataBaseContants.CONTENT_URI, new String[] { "_id", "steps" },
				DataBaseContants.DATE + "  = ? AND " + DataBaseContants.DATA_FROM + "  = ? AND " + DataBaseContants.STATISTICS + " = ?", new String[] {day, "phone", "2" }, null);
		c.moveToFirst();
		if (c.getCount() > 0) {
			for (int y = 0; y < c.getCount(); y++) {
				PHONE_STEP = c.getInt(c.getColumnIndex(DataBaseContants.STEPS));
				c.moveToNext();
			}
		}
		c.close();
		c = null;
	}
	
	private void initData() {
		mRunningDays.clear();
		firstDay = "";
		weight = new LinkedHashMap<String, String>();
		steps = new LinkedHashMap<String, Integer>();
		long start = System.currentTimeMillis();
		String today = Tools.getDate(0);
		String enter_day = today;
		date_list = Tools.getDateFromDb(mCtx);
		if (date_list != null && date_list.size() > 0)
			enter_day = date_list.get(0);
		int count = Tools.getDayCount(enter_day, today, "yyyy-MM-dd");
		float progress=0;
		int notificationStep;
		int notificationCal;
		ContentResolver cr = mCtx.getContentResolver();
		for (int i = 0; i < count; i++) {
			RunningItem runningdate = new RunningItem();
			String day = Tools.getDate(enter_day, 0 - i);
			if (date_list != null && date_list.size() > 0 && date_list.indexOf(day) != -1) {
				initAddSport(day);
				Cursor c = cr.query(DataBaseContants.CONTENT_URI, new String[] {"_id", "date", "steps", "kilometer", "calories" },
						DataBaseContants.DATE + "  = ? AND " + DataBaseContants.STATISTICS + " = ? ", new String[] { day, "1" }, DataBaseContants.STEPS+" DESC");
				c.moveToFirst();
				if (c.getCount() > 0) {
					if (firstDay.equals(""))
						firstDay = day;
					for(int j = 0; j < c.getCount(); j++) {
						long id = c.getLong(c.getColumnIndex(DataBaseContants.ID));
						if (j == 0) {
							runningdate.setDate(c.getString(c.getColumnIndex(DataBaseContants.DATE)));
							runningdate.setCalories(c.getInt(c.getColumnIndex(DataBaseContants.CALORIES)) + caloriesAddSport);
							runningdate.setSteps(c.getInt(c.getColumnIndex(DataBaseContants.STEPS)));
							runningdate.setKilometer(c.getInt(c.getColumnIndex(DataBaseContants.KILOMETER)));
							runningdate.setStartTime("");
							runningdate.setEndTime("");
							runningdate.setDuration("");
							runningdate.setPm25(Tools.getPm25(day));
							steps.put(c.getString(c.getColumnIndex(DataBaseContants.DATE)), c.getInt(c.getColumnIndex(DataBaseContants.STEPS)));
						} else {
							cr.delete(DataBaseContants.CONTENT_URI, DataBaseContants.ID + " = ?", new String[]{ String.valueOf(id) });							
							ContentValues values = new ContentValues();
							values.put(DataBaseContants.DELETE_VALUE, id);
							cr.insert(DataBaseContants.CONTENT_DELETE_URI, values);
						}
						c.moveToNext();
					}
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
			} else {
				runningdate.setDate(day);
				runningdate.setCalories(0);
				runningdate.setSteps(0);
				runningdate.setKilometer(0);
				runningdate.setStartTime("");
				runningdate.setEndTime("");
				runningdate.setDuration("");
				runningdate.setPm25(Tools.getPm25(day));
			}
			mRunningDays.add(runningdate);
			/*if (i == count - 1) {
				readPhoneStep(day);
				item = runningdate;
				int baseSteps = PedBackgroundService.hadRunStep - PedBackgroundService.start_step;
				if (baseSteps != 0) {
					RunningItem current = null;
					try {
						current = (RunningItem)item.clone();
					} catch (CloneNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					current.setSteps(item.getSteps() + baseSteps);
					mRunningDays.set(count - 1, current);
				}
				if (!steps.containsKey(day)) {
					steps.put(day, 0);
				}
			}*/
		}
		mHomeAdapter.notifyDataSetChanged(mRunningDays, weight, steps);
		int index = mRunningDays.size() - 1;
		mViewPager.setCurrentItem(index, false);
		long end = System.currentTimeMillis();
		Log.i("gchk", "getAllDate耗时" + (end - start));

		BluetoothService service = BluetoothService.getInstance();
		if (service != null) {
			PersonalGoal personal=Tools.getPersonalGoal();
			int targetStep=personal.getStep();
			notificationStep = mRunningDays.get(mRunningDays.size() - 1).getSteps();
			notificationCal = mRunningDays.get(mRunningDays.size() - 1).getCalories();
			service.updateConnectionStatus(false,notificationStep,notificationCal,targetStep);
			Log.i("444", "notificationStep="+mRunningDays.get(mRunningDays.size() - 1).getSteps());
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i("gchk", "home onResume");
		initData();
		Boolean flag = Welcome.isentry;
		if (flag) {
			if (mRunningDays.size() > 0) {
				mViewPager.setCurrentItem(mRunningDays.size() - 1, false);
			}
			if (mRunningDays.size() == 1) {
				setTitle(Tools.getDate(0));
			}
			Util.autoConnect(mCtx);
			Welcome.isentry = false;
		} else {

			int index = Tools.getCurrPageIndex();
			if (index == 0) {
				mViewPager.setCurrentItem(mRunningDays.size() - 1, false);
				if (mRunningDays.size() == 1) {
					setTitle(Tools.getDate(0));
				}
			} else {
				mViewPager.setCurrentItem(index, false);
			}
		}
		mWeatherObserver.startWatching();
		StepWatcher.getInstance().addWatcher(mStepObserver);
		mCtx.getContentResolver().registerContentObserver(
				DataBaseContants.CONTENT_URI, true, mContentObserver);
		IntentFilter intentf = new IntentFilter(Intent.ACTION_DATE_CHANGED);
		mCtx.registerReceiver(mDateChangeReceiver, intentf);
		mHandler.sendEmptyMessageDelayed(NOTIFY_DATASET_CHANGED, 50);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i("gchk", "home onPause");
		mWeatherObserver.stopWatching();
		StepWatcher.getInstance().removeWatcher(mStepObserver);
		mCtx.getContentResolver().unregisterContentObserver(mContentObserver);
		mCtx.unregisterReceiver(mDateChangeReceiver);
		Tools.setCurrPageIndex(mViewPager.getCurrentItem());
		if (calPopupWindow != null && calPopupWindow.isShowing()) {
			calPopupWindow.dismiss();
		}
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
		//here we just do two things ,first show header ,second show tab!
		//modefy by zhouzhongbo@20150109 start
		if(mFooter != null&&mFooter.getVisibility()!=View.VISIBLE){
			mFooter.setVisibility(View.VISIBLE);
		}
		HomePageItemFragment cur_fragment = (HomePageItemFragment) mHomeAdapter.registeredFragments.get(mViewPager.getCurrentItem());//instantiateItem(mViewPager,index);
		if (cur_fragment != null)
			cur_fragment.reSetHeadMargin();
		//modefy by zhouzhongbo@20150109 end
	}

	@Override
	public void onPageSelected(int arg0) {
		String curr_date = mRunningDays.get(arg0).getDate();
		setTitle(curr_date);
	}

	private void setTitle(String title) {
		currDate = title;
		if (title.equals(Tools.getDate(0))) {
			title_bar_text.setText(R.string.today);
		} else {
			title_bar_text.setText(title.substring(5));
		}
	}

	public List<String> getDateList() {
		return date_list;
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
	
	public void onViewPagerIndex(int index) {
		mViewPager.setCurrentItem(index, true);

	}

	public void onViewPagerCurrent() {
		int index = mRunningDays.size() - 1;
		mViewPager.setCurrentItem(index, true);
	}

	public RunningItem getCurrPageData() {
		RunningItem data = null;
		if (mRunningDays != null) {
			data = mRunningDays.get(mViewPager.getCurrentItem());
		}

		return data;
	}

	private void toShareActivity() {
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
									mHomeAdapter.notifyDataSetChanged(mRunningDays, weight, steps);
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
					WeatherTools.newInstance().getCurrWeather();
				}
			}
		}
	}

	//modefy by zhouzhongbo@20150106 start
	private void initView() {
		mViewPager = (ViewPager) mRootView.findViewById(R.id.main_viewpager);
		mViewPager.setOnPageChangeListener(this);
		
		title_bar_text = (TextView) mRootView.findViewById(R.id.title_bar_text);
		title_bar_text.setText(R.string.today);
		cal_lay = (RelativeLayout) mRootView.findViewById(R.id.cal_lay);
		cal_lay.setOnClickListener(calOnClick);
		title_share = (ImageView) mRootView.findViewById(R.id.title_share);
		title_cloud = (ImageView) mRootView.findViewById(R.id.title_cloud);
		title_share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toShareActivity();
			}			
		});
		title_cloud.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	        	CloudSync.prepareSync();
			}			
		});
		
		mHintTextView = (TextView)mRootView.findViewById(R.id.xlistview_header_hint_textview);
		mHeaderTimeViewTitle = (TextView) mRootView.findViewById(R.id.xlistview_header_time_text);
		mHeaderTimeView = (TextView) mRootView.findViewById(R.id.xlistview_header_time);
		mListViewRel = (RelativeLayout)mRootView.findViewById(R.id.xlistview_header_content);
		mProgress = (TextView)mRootView.findViewById(R.id.progress);
		mProgress.setVisibility(View.GONE);
		mFooter = (View) getActivity().findViewById(R.id.foot_id);
		mSleep = (Button) mRootView.findViewById(R.id.sleep_btn);
		mSleep.setOnClickListener(btnSleep);
	}

	OnClickListener btnSleep = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Main.vFragment = false;
			if (Main.mHandler != null) {
				Main.mHandler.sendEmptyMessage(Main.SLEEP_FRAGMENT_HOME);
			}
		}
	};
	

	OnClickListener calOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			initmPopupWindowView();
			calPopupWindow.showAsDropDown(v, 0, 0);
		}
	};
	
	
	public void initmPopupWindowView() {
		calPopupWindow = new CalendarView(getActivity(), currDate);
		calPopupWindow.setAnimationStyle(R.style.AnimationFade);
		calPopupWindow.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				calPopupWindow = null;
			}
		});
	}

	public void refreshView(Boolean state, int type) {
		if (state) {
	    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mCtx);
	    	String label = "";
	        // 如果最后更新的时间的文本是空的话，隐藏前面的标题
			if (type == 1) {
				label = sp.getString("sync_device_time", "");
				mHintTextView.setText(R.string.xlistview_header_hint_loading);
			} else if (type == 2) {
				mProgress.setVisibility(View.VISIBLE);
				mProgress.setText("0%"); 
				label = sp.getString("sync_cloud_time", "");
				mHintTextView.setText(R.string.progressbar_dialog_sync);
			}
	    	if (TextUtils.isEmpty(label)) {
	            mHeaderTimeViewTitle.setVisibility(View.GONE);
	            mHeaderTimeView.setVisibility(View.GONE);
	    	} else {
	            mHeaderTimeViewTitle.setVisibility(View.VISIBLE);
	            mHeaderTimeView.setVisibility(View.VISIBLE);
	            mHeaderTimeView.setText(label);
	    	}
			mListViewRel.setVisibility(View.VISIBLE);
		} else
			mListViewRel.setVisibility(View.GONE);
	}
	
    private void setLastUpdateTime(int type) {
        String text = formatDateTime(System.currentTimeMillis());
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mCtx);
		Editor editor = sp.edit();
		if (type == 1)
			editor.putString("sync_device_time", text);
		else if (type == 2)
			editor.putString("sync_cloud_time", text);
		editor.commit();
    }

    private String formatDateTime(long time) {
    	SimpleDateFormat mDateFormat = new SimpleDateFormat("MM-dd HH:mm");
        if (0 == time) {
            return "";
        }
        
        return mDateFormat.format(new Date(time));
    }

/*	@Override
	public void onScrollStateChange(int state) {
		
	}

	@Override
	public void onScrollListDispatch(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount, int delta) {
		// TODO Auto-generated method stub
		int lastlistpositon = view.getLastVisiblePosition();
		if (delta < 0&&Math.abs(delta)>5) {//scroll down 
			if (mFooter != null && mFooter.getVisibility() != View.VISIBLE) {
				mFooter.setVisibility(View.VISIBLE);
			}
		} else if (Math.abs(delta) > 2 && delta > 0 && totalItemCount != 2 && firstVisibleItem != 0){//scroll up     
			if (mFooter != null && mFooter.getVisibility() != View.GONE)
				mFooter.setVisibility(View.GONE);
        	
        }

	}*/
	
	/** 运动步数信息监听 */
	class StepObserver implements IStepListener{
		
		@Override
		public void onStepCount(int stepCount) {
			/*int size = mRunningDays.size() - 1;
			if (mViewPager.getCurrentItem() == size) {								
				RunningItem current = null;
				try {
					current = (RunningItem)item.clone();
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.i("222", item.getSteps() + "qqqqqqqqqq" + PHONE_STEP + "qqqqqqqqqq" + stepCount);
				current.setSteps(item.getSteps() - PHONE_STEP + stepCount);
				mRunningDays.set(size, current);
				mHomeAdapter.notifyDataSetChanged(mRunningDays, weight, steps);
			}*/
		}
		
		@Override
		public void onStateChanged(int newState) {
			
		}

		@SuppressWarnings("static-access")
		@Override
		public void onHadRunStep(int hadRunStep) {
		}
	}
}
