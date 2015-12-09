package com.zhuoyou.plugin.running;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhuoyou.plugin.add.AddPicture;
import com.zhuoyou.plugin.add.AddSports;
import com.zhuoyou.plugin.add.AddWeight;
import com.zhuoyou.plugin.bluetooth.data.Util;
import com.zhuoyou.plugin.custom.XListView;
import com.zhuoyou.plugin.custom.XListView.IXListViewListener;
import com.zhuoyou.plugin.custom.XListView.OnXListHeaderViewListener;
import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.gps.GpsSportInfo;
import com.zhuoyou.plugin.mainFrame.QuickReturnHeaderHelper;

public class HomePageItemFragment extends Fragment implements
		OnItemClickListener, IXListViewListener, OnXListHeaderViewListener {
	private XListView mListView;
	private TextView mWeatherTv, mSummayTv, mSummayUnit, mStepsTv,
			mKilometreTv, mCaloriesTv;
	private ImageView mPercentArc, mDayLeft, mDayRight;
	private View mroot;
	private View realHeader;
	private HomePageListItemAdapter mListAdapter;

	private List<RunningItem> mTodayLists = new ArrayList<RunningItem>();
	private HomepageListListener mHomepageListListener;
	private RunningItem mDateBean;
	private QuickReturnHeaderHelper helper;
	public static Map<String, Bitmap> gridviewBitmapCaches = new HashMap<String, Bitmap>();
	private Context sContext = RunningApp.getInstance().getApplicationContext();
	public static Handler mHandler;
	public static final int SYNC_DEVICE_FAILED = 1;
	public static final int SYNC_DEVICE_SUCCESSED = 2;
	private Typeface mNumberTP = RunningApp.getCustomNumberFont();
	private Map<String, String> weight = null;
	private Map<String, Integer> steps = null;
	private PersonalGoal personal;
	private int[] mSteps = null;
	private float num = 0;
	private ArrayList<Double> weightList = null;

	// add by zhouzhongbo@201520150305 for quickreturn view start
	private int headerHeight;
	private int headerTop;
	private int lastheaderTop;
	private View dummyHeader;
	private int lastFirstVisibleItem;
	private int lastTop;
	private int scrollPosition;
	private int lastHeight;
	private int drag_down_delta = 0;
	private FrameLayout.LayoutParams realHeaderLayoutParams;
	private boolean waitingForExactHeaderHeight = true;

	// add by zhouzhongbo@201520150305 for quickreturn view end

	interface HomepageListListener {
		void onScrollStateChange(int state);

		void onScrollListDispatch(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount, int delta);
	}

	public static HomePageItemFragment newInstance(RunningItem bean,
			Map<String, String> wei, Map<String, Integer> step) {
		HomePageItemFragment fragment = new HomePageItemFragment();
		fragment.mDateBean = bean;
		fragment.weight = wei;
		fragment.steps = step;
		fragment.personal = Tools.getPersonalGoal();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case SYNC_DEVICE_FAILED:
					mListView.stopRefresh();
					break;
				case SYNC_DEVICE_SUCCESSED:
					mListView.stopRefresh();
					setLastUpdateTime();
					break;
				default:
					break;
				}
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mroot = inflater.inflate(R.layout.home_page_item, container, false);
		realHeader = mroot.findViewById(R.id.fg_header);
		mListView = (XListView) mroot.findViewById(R.id.listview_activity);
		mListView.setOnItemClickListener(this);
		mListView.setXListViewListener(this);
		return mroot;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		realHeader.measure(0, 0);
		headerHeight = realHeader.getMeasuredHeight();
		mListView.setOnScrollListener(new OnScrollListener() {
			@SuppressLint("NewApi")
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

				View firstChild = view.getChildAt(0);
				if (firstChild == null) {
					return;
				}
				int top = firstChild.getTop();// view top which is instead of
												// headerview
				int height = firstChild.getHeight();
				int delta;
				int skipped = 0;
				if (lastFirstVisibleItem == firstVisibleItem) {
					delta = lastTop - top;
				} else if (firstVisibleItem > lastFirstVisibleItem) {
					skipped = firstVisibleItem - lastFirstVisibleItem - 1;
					delta = skipped * height + lastHeight + lastTop - top;
				} else {
					skipped = lastFirstVisibleItem - firstVisibleItem - 1;// number
					delta = skipped * -height + lastTop - (height + top);// distance
				}

				// boolean exact = skipped > 0;
				// scrollPosition += -delta;
				if (firstVisibleItem == 1) {
					if (delta < 0) {// scroll down
						if (top + delta < headerHeight && headerTop != 0) {// headview
																			// half
																			// show
							headerTop = top;
						} else {
							headerTop = 0;
						}
					} else if (delta > 0) {// scroll up
						if (top + delta < 0) {
							headerTop = top;
						} else {
							headerTop = 0;
						}
					}
				} else if (firstVisibleItem == 0) {// roll back show
					Log.d("zzb", "drag_down delta in OnHeaderSyncShowDown = "
							+ drag_down_delta);
					if (delta == 0 && top == 0) {
						headerTop = drag_down_delta;
					} else {
						if (delta > 0)
							headerTop = drag_down_delta;
					}

				} else {
					Log.d("zzb", "lastFirstVisibleItem ="
							+ lastFirstVisibleItem + "firstVisibleItem ="
							+ firstVisibleItem);
					if (delta < 0) {// scroll down
						headerTop = 0;
					} else if (delta > 0) {// scroll up
						headerTop = -headerHeight;
					}
				}
				// I'm aware that offsetTopAndBottom is more efficient, but it
				// gave me trouble when scrolling to the bottom of the list
				if (lastheaderTop != headerTop) {
					lastheaderTop = headerTop;
					Log.d("zzb", "topMargin=" + headerTop);
					realHeader.setTranslationY(headerTop);
					// realHeader.setLayoutParams(realHeaderLayoutParams);
				}
				lastFirstVisibleItem = firstVisibleItem;
				lastTop = top;
				lastHeight = firstChild.getHeight();
				// for recyle bitmap
				recycleBitmapCaches(0, firstVisibleItem);
				recycleBitmapCaches(firstVisibleItem + visibleItemCount - 2,
						totalItemCount);
				if (mHomepageListListener != null) {
					mHomepageListListener.onScrollListDispatch(view,
							firstVisibleItem, visibleItemCount, totalItemCount,
							delta);
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
		});

		mListView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						Log.d("zzb", "onGlobalLayout");
						if (waitingForExactHeaderHeight
								&& dummyHeader.getHeight() > 0) {
							headerHeight = dummyHeader.getHeight();
							waitingForExactHeaderHeight = false;
							LayoutParams params = dummyHeader.getLayoutParams();
							params.height = headerHeight;
							dummyHeader.setLayoutParams(params);
							mListView.getViewTreeObserver()
									.removeGlobalOnLayoutListener(this);
						}
					}
				});
		mListView.SetXlistHeaderLisetener(this);

		dummyHeader = new View(getActivity());
		AbsListView.LayoutParams params = new AbsListView.LayoutParams(
				LayoutParams.MATCH_PARENT, headerHeight);
		dummyHeader.setLayoutParams(params);
		mListView.addHeaderView(dummyHeader);

		mListAdapter = new HomePageListItemAdapter(getActivity(), mTodayLists,
				mDateBean.getDate(), weightList, num);
		mListView.setAdapter(mListAdapter);
	}

	public void hideImageAnimated(final ImageView iv) {

		Animation alpha = new AlphaAnimation(1.0f, 0.0f);
		alpha.setDuration(2000); // whatever duration you want

		// add AnimationListener
		alpha.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation arg0) {
				iv.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationStart(Animation arg0) {
			}

		});

		iv.startAnimation(alpha);
	}

	public void SetListener(HomepageListListener mlistener) {
		mHomepageListListener = mlistener;
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i("gchk", "item onResume");
		mListView.setTag("listview");
		int step = mDateBean.getSteps();// + GaodeService.hadRunStep;
		int z = mDateBean.getmBmi() == null ? 0 : Integer.getInteger(mDateBean
				.getmBmi());
		num = 0;
		if (step != 0) {
			if (z != 0) {
				num = (float) step / z;
			} else {
				num = (float) step / personal.mGoalSteps;
			}
		}
		int pm = mDateBean.getPm25();
		mWeatherTv = (TextView) mroot.findViewById(R.id.weather_tv);
		if (pm < 0) {
			mWeatherTv.setText(R.string.day_pm_0);
		} else if (0 <= pm && pm <= 50) {
			mWeatherTv.setText(R.string.day_pm_1);
		} else if (51 <= pm && pm <= 100) {
			mWeatherTv.setText(R.string.day_pm_2);
		} else if (101 <= pm && pm <= 150) {
			mWeatherTv.setText(R.string.day_pm_3);
		} else if (151 <= pm && pm <= 200) {
			mWeatherTv.setText(R.string.day_pm_4);
		} else if (201 <= pm && pm <= 300) {
			mWeatherTv.setText(R.string.day_pm_5);
		} else if (pm > 300) {
			mWeatherTv.setText(R.string.day_pm_6);
		}
		mPercentArc = (ImageView) mroot.findViewById(R.id.percent_arc_iv);
		updateNotificationRemoteViews(sContext, mPercentArc, num);
		mSummayTv = (TextView) mroot.findViewById(R.id.summay_tv);
		mSummayTv.setText((int) (num * 100 / 1) + "");
		mSummayUnit = (TextView) mroot.findViewById(R.id.summay_unit);
		mSummayUnit.setText("%");
		mStepsTv = (TextView) mroot.findViewById(R.id.steps_tv);
		mStepsTv.setText(step + "");
		mKilometreTv = (TextView) mroot.findViewById(R.id.kilometre_tv);
		mKilometreTv.setText(mDateBean.getKilometer() + "");
		mCaloriesTv = (TextView) mroot.findViewById(R.id.calories_tv);
		mCaloriesTv.setText(mDateBean.getCalories() + "");

		mSummayTv.setTypeface(mNumberTP);
		mSummayUnit.setTypeface(mNumberTP);
		mStepsTv.setTypeface(mNumberTP);
		mCaloriesTv.setTypeface(mNumberTP);

		mDayLeft = (ImageView) mroot.findViewById(R.id.day_btn_left);
		mDayRight = (ImageView) mroot.findViewById(R.id.day_btn_right);

		hideImageAnimated(mDayLeft);
		hideImageAnimated(mDayRight);
		initListData();
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i("gchk", "item onPause");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.i("gchk", "item onDetach");
	}

	private void initListData() {
		mTodayLists.clear();
		mTodayLists = new ArrayList<RunningItem>();
		long start = System.currentTimeMillis();
		ContentResolver cr = getActivity().getContentResolver();
		String day = mDateBean.getDate();
		mTodayLists.add(0, mDateBean);
		Cursor c = cr.query(DataBaseContants.CONTENT_URI, new String[] { "_id",
				"date", "time_duration", "time_start", "time_end", "steps",
				"kilometer", "calories", "weight", "bmi", "img_uri",
				"img_explain", "sports_type", "type" }, DataBaseContants.DATE
				+ "  = ? AND " + DataBaseContants.STATISTICS + " = ? ",
				new String[] { day, "0" }, DataBaseContants.ID + " DESC");
		SortCursor sc = new SortCursor(c, DataBaseContants.TIME_START);
		sc.moveToFirst();
		int count = sc.getCount();
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				long id = c.getLong(c.getColumnIndex(DataBaseContants.ID));
				String start_time = c.getString(sc
						.getColumnIndex(DataBaseContants.TIME_START));
				int sport_type = c.getInt(sc
						.getColumnIndex(DataBaseContants.SPORTS_TYPE));
				int type = c.getInt(sc.getColumnIndex(DataBaseContants.TYPE));
				int size = mTodayLists.size();
				if (size > 0) {
					if (type == 2 && sport_type == 0) {
						if (mTodayLists.get(size - 1).getmType() == 2
								&& mTodayLists.get(size - 1).getSportsType() == 0) {
							if (mTodayLists.get(size - 1).getStartTime()
									.equals(start_time)) {
								cr.delete(DataBaseContants.CONTENT_URI,
										DataBaseContants.ID + " = ?",
										new String[] { String.valueOf(id) });
								ContentValues values = new ContentValues();
								values.put(DataBaseContants.DELETE_VALUE, id);
								cr.insert(DataBaseContants.CONTENT_DELETE_URI,
										values);
								break;
							}
						}
					}
				}
				if (type != 4 && type != 3) {
					RunningItem item = new RunningItem();
					item.setID(id);
					item.setDate(c.getString(sc
							.getColumnIndex(DataBaseContants.DATE)));
					item.setDuration(c.getString(sc
							.getColumnIndex(DataBaseContants.TIME_DURATION)));
					item.setStartTime(start_time);
					item.setEndTime(c.getString(sc
							.getColumnIndex(DataBaseContants.TIME_END)));
					item.setCalories(c.getInt(sc
							.getColumnIndex(DataBaseContants.CALORIES)));
					item.setSteps(c.getInt(sc
							.getColumnIndex(DataBaseContants.STEPS)));
					item.setKilometer(c.getInt(sc
							.getColumnIndex(DataBaseContants.KILOMETER)));
					item.setmWeight(c.getString(sc
							.getColumnIndex(DataBaseContants.CONF_WEIGHT)));
					item.setmBmi(c.getString(sc
							.getColumnIndex(DataBaseContants.BMI)));
					item.setmImgUri(c.getString(sc
							.getColumnIndex(DataBaseContants.IMG_URI)));
					item.setmExplain(c.getString(sc
							.getColumnIndex(DataBaseContants.EXPLAIN)));
					item.setSportsType(sport_type);
					item.setmType(type);
					item.setisStatistics(0);
					mTodayLists.add(item);
				}
				sc.moveToNext();
			}
		}
		c.close();
		c = null;
		sc.close();
		sc = null;
		if (mDateBean.getSteps() > 0 && steps != null && steps.size() > 0) {
			Object[] s = steps.keySet().toArray();
			List<Object> list = Arrays.asList(s);
			int index = list.indexOf(day);
			mSteps = new int[index + 1];
			for (int y = 0; y <= index; y++) {
				mSteps[y] = steps.get(list.get(y));
			}
			Arrays.sort(mSteps);
			if (mSteps[mSteps.length - 1] == mDateBean.getSteps()) {
				num = 10000;
			}
		}
		weightList = new ArrayList<Double>();
		if (weight != null && weight.size() > 0) {
			Object[] s = weight.keySet().toArray();
			List<Object> list = Arrays.asList(s);
			int index = list.indexOf(day);
			for (int y = (index - 6 < 0 ? 0 : index - 6); y <= index; y++) {
				weightList.add(Double.valueOf(weight.get(list.get(y))));
			}
		}
		// modefy by zhouzhongbo
		mListAdapter.UpdateDate(getActivity(), mTodayLists, day, weightList,
				num);
		mListAdapter.notifyDataSetChanged();

		long end = System.currentTimeMillis();
		Log.i("gchk", "initListData耗时" + (end - start));
	}

	// 释放图片的函数
	private void recycleBitmapCaches(int fromPosition, int toPosition) {
		Bitmap delBitmap = null;
		for (int del = fromPosition; del < toPosition - 2; del++) {
			String url = mTodayLists.get(del).getmImgUri();
			if (url != null && !url.equals("")) {
				String path = Tools.getSDPath()
						+ "/Running/.thumbnailnew/"
						+ url.substring(url.lastIndexOf("/") + 1,
								url.lastIndexOf("."));
				if (gridviewBitmapCaches != null
						&& gridviewBitmapCaches.size() > 0) {
					delBitmap = gridviewBitmapCaches.get(path
							+ mTodayLists.get(del).getDate() + del);
					if (delBitmap != null) {
						// 如果非空则表示有缓存的bitmap，需要清理
						// 从缓存中移除该del->bitmap的映射
						gridviewBitmapCaches.remove(path
								+ mTodayLists.get(del).getDate() + del);
						delBitmap.recycle();
						delBitmap = null;
						System.gc();
					}
				}
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		int index = position - 2;
		if (index < 0)
			return;
		if (mListAdapter != null) {
			int type = mListAdapter.getItemViewType(index);
			Intent intent;
			switch (type) {
			case 1:
				intent = new Intent();
				intent.setClass(sContext, AddWeight.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("id", mTodayLists.get(index).getID());
				intent.putExtra("date", mTodayLists.get(index).getDate());
				intent.putExtra("weightCount", mTodayLists.get(index)
						.getmWeight());
				intent.putExtra("startTime", mTodayLists.get(index)
						.getStartTime());
				sContext.startActivity(intent);
				break;
			case 3:
				if (mTodayLists.get(index).getSportsType() != 0) {
					intent = new Intent();
					intent.setClass(sContext, AddSports.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra("id", mTodayLists.get(index).getID());
					intent.putExtra("sportType", mTodayLists.get(index)
							.getSportsType());
					intent.putExtra("sportStartTime", mTodayLists.get(index)
							.getStartTime());
					intent.putExtra("date", mTodayLists.get(index).getDate());
					intent.putExtra("sportDuration", mTodayLists.get(index)
							.getDuration() + "");
					intent.putExtra("wasteCalories", mTodayLists.get(index)
							.getCalories() + "");
					sContext.startActivity(intent);
				}
				break;
			case 4:/*
					 * edit mood intent = new Intent();
					 * intent.setClass(sContext, AddPicture.class);
					 * intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					 * intent.putExtra("id", mTodayLists.get(index).getID());
					 * intent.putExtra("imgUri",
					 * mTodayLists.get(index).getmImgUri());
					 * intent.putExtra("date",
					 * mTodayLists.get(index).getDate());
					 * intent.putExtra("words",
					 * mTodayLists.get(index).getmExplain() + "");
					 * intent.putExtra("startTime",
					 * mTodayLists.get(index).getStartTime());
					 * sContext.startActivity(intent);
					 */
				break;
			case 5:
				String gpsId = mTodayLists.get(index).getmExplain();
				intent = new Intent();
				intent.setClass(sContext, GpsSportInfo.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("gpsid", gpsId);
				sContext.startActivity(intent);
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void onRefresh() {
		if (MainService.getInstance() != null) {
			MainService.getInstance().syncWithDevice();
		}
	}

	private void setLastUpdateTime() {
		String text = formatDateTime(System.currentTimeMillis());
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(sContext);
		Editor editor = sp.edit();
		editor.putString("sync_device_time", text);
		editor.commit();
	}

	private String formatDateTime(long time) {
		SimpleDateFormat mDateFormat = new SimpleDateFormat("MM-dd HH:mm");
		if (0 == time) {
			return "";
		}

		return mDateFormat.format(new Date(time));
	}

	public void reSetHeadMargin() {
		if (headerTop != 0) {
			headerTop = 0;
			lastheaderTop = 0;
			realHeader.setTranslationY(headerTop);
		}
	}

	public void updateNotificationRemoteViews(Context paramContext,
			ImageView mPerArc, float num) {
		double d = num;
		if (num > 0) {
			if (num < 0.01) {
				d = 0.01d;
			}
			if (num >= 1.0d) {
				d = 1.0d;
			}
		}
		NumberFormat localNumberFormat = NumberFormat.getPercentInstance();
		localNumberFormat.setMinimumFractionDigits(0);
		Bitmap localBitmap = Bitmap.createBitmap(
				Util.dip2pixel(paramContext, 47.0F),
				Util.dip2pixel(paramContext, 47.0F), Bitmap.Config.ARGB_8888);
		Canvas localCanvas = new Canvas(localBitmap);
		Paint localPaint = new Paint();
		localPaint.setAntiAlias(true);
		localPaint.setColor(Color.WHITE);
		localPaint.setStyle(Paint.Style.STROKE);
		localPaint.setStrokeWidth(Util.dip2pixel(paramContext, 1.0F));
		RectF localRectF = new RectF(Util.dip2pixel(paramContext, 2.0F),
				Util.dip2pixel(paramContext, 2.0F), Util.dip2pixel(
						paramContext, 44.0F), Util.dip2pixel(paramContext,
						44.0F));
		int j = (int) (360.0D * d);
		localCanvas.drawArc(localRectF, -90, j, false, localPaint);
		mPerArc.setImageBitmap(localBitmap);
	}

	@Override
	public void OnHeaderSyncShowDown(int delta) {
		Log.d("zzb", "11OnHeaderSyncShowDown:" + drag_down_delta);
		drag_down_delta = delta;
	}

	@Override
	public void OnHeaderSyncScrollBack(int position, int time) {
		drag_down_delta = position;
		headerTop = drag_down_delta;

	}
}
