package com.zhuoyou.plugin.running;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.view.ViewTreeObserver;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zhuoyou.plugin.bluetooth.data.Util;
import com.zhuoyou.plugin.custom.XListView;
import com.zhuoyou.plugin.custom.XListView.IXListViewListener;
import com.zhuoyou.plugin.custom.XListView.OnXListHeaderViewListener;
import com.zhuoyou.plugin.database.DataBaseUtil;
import com.zhuoyou.plugin.mainFrame.SleepDetailActivity;
import com.zhuoyou.plugin.running.HomePageItemFragment.HomepageListListener;

 public class SleepPageItemFragment extends Fragment implements
		OnItemClickListener, IXListViewListener, OnXListHeaderViewListener {
	private XListView mListView;
	private TextView mSummayTv, mSummayUnit, mStepsTv, mCaloriesTv;
	private ImageView mPercentArc, mDayLeft, mDayRight;
	private View mroot;
	private View realHeader;
	private SleepPageListItemAdapter mListAdapter;
	private List<SleepItem> mTodayLists = null;
	private String mDate;
	private Context sContext = RunningApp.getInstance().getApplicationContext();
	private Typeface mNumberTP = RunningApp.getCustomNumberFont();
	public static Map<String, Bitmap> gridviewBitmapCaches = new HashMap<String, Bitmap>();
	public static Handler mHandler;
	public static final int SYNC_DEVICE_FAILED = 1;
	public static final int SYNC_DEVICE_SUCCESSED = 2;
	private int headerHeight;
	private int headerTop;
	private int lastheaderTop;
	private View dummyHeader;
	private int lastFirstVisibleItem;
	private int lastTop;
	private int lastHeight;
	private int drag_down_delta = 0;
	private boolean waitingForExactHeaderHeight = true;
	private SleepPageListListener mSleeppageListListener;

	interface SleepPageListListener {
		void onScrollStateChange(int state);

		void onScrollListDispatch(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount, int delta);
	}

	public static SleepPageItemFragment newInstance(String bean) {
		SleepPageItemFragment fragment = new SleepPageItemFragment();
		fragment.mDate = bean;
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
		mroot = inflater.inflate(R.layout.sleep_page_item, container, false);
		realHeader = mroot.findViewById(R.id.header);
		mListView = (XListView) mroot.findViewById(R.id.listview_sleep);
		mListView.setOnItemClickListener(this);
		mListView.setXListViewListener(this);
		mListView.setSleepHeaderBackground();
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
				if (mSleeppageListListener != null) {
					mSleeppageListListener.onScrollListDispatch(view,
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

	@Override
	public void onResume() {
		super.onResume();
		Log.i("gchk", "item onResume");

		mPercentArc = (ImageView) mroot.findViewById(R.id.percent_arc_iv);
		mSummayTv = (TextView) mroot.findViewById(R.id.summay_tv);
		mSummayUnit = (TextView) mroot.findViewById(R.id.summay_unit);
		mSummayUnit.setText("%");
		mStepsTv = (TextView) mroot.findViewById(R.id.steps_tv);
		mCaloriesTv = (TextView) mroot.findViewById(R.id.calories_tv);

		mSummayTv.setTypeface(mNumberTP);
		mSummayUnit.setTypeface(mNumberTP);
		mStepsTv.setTypeface(mNumberTP);
		mCaloriesTv.setTypeface(mNumberTP);

		mDayLeft = (ImageView) mroot.findViewById(R.id.day_btn_left);
		mDayRight = (ImageView) mroot.findViewById(R.id.day_btn_right);

		mSummayTv.setText("0");
		mStepsTv.setText(Tools.getTimer(0));
		mCaloriesTv.setText(Tools.getTimer(0));
		updateNotificationRemoteViews(sContext, mPercentArc, 0f);

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
		Log.i("SleepPageItemFragment", "item onDetach");
	}

	private void initListData() {
		Log.i("SleepPageItemFragment", "mDateBean.getDate():" + mDate);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");// 小写的mm表示的是分钟
		java.util.Date date = new Date();
		try {
			date = sdf.parse(mDate);
		} catch (ParseException e) {
		}
		long start = (date.getYear() + 1900) * 10000000000L
				+ (date.getMonth() + 1) * 100000000L + date.getDate()
				* 1000000L;
		long end = (date.getYear() + 1900) * 10000000000L
				+ (date.getMonth() + 1) * 100000000L + (date.getDate() + 1)
				* 1000000L;
		Log.i("SleepPageItemFragment", "start:" + start);
		Log.i("SleepPageItemFragment", "end:" + end);
		mTodayLists = DataBaseUtil.getSleepItem(sContext, start, end);
		if (mTodayLists != null && mTodayLists.size() == 0) {
			mTodayLists = DataBaseUtil.getClassicSleepItem(sContext, mDate);
		}
		mListAdapter = new SleepPageListItemAdapter(getActivity(), mTodayLists);
		mListView.setAdapter(mListAdapter);
		int Sleep = 0;
		int DeepSleep = 0;
		int LightSleep = 0;
		if (mTodayLists != null && mTodayLists.size() > 0) {
			for (int i = 0; i < mTodayLists.size(); i++) {
				SleepItem item = mTodayLists.get(i);
				int sleep = item.getmSleepT();
				Sleep = Sleep + sleep;
				int deepSleep = item.getmDSleepT();
				DeepSleep = DeepSleep + deepSleep;
				// int lightSleep = item.getmWSleepT();
				int lightSleep = sleep - deepSleep;
				LightSleep = LightSleep + lightSleep;
				if (Sleep != 0 && Sleep > DeepSleep) {
					int rate = DeepSleep * 100 / Sleep;
					mSummayTv.setText(rate + "");
					mStepsTv.setText(Tools.getTimer(Sleep));
					mCaloriesTv.setText(Tools.getTimer(DeepSleep));
					updateNotificationRemoteViews(sContext, mPercentArc,
							rate / 100f);
				}

			}
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		int index = position - 2;
		if (index < 0)
			return;
		SleepItem item = mTodayLists.get(index);
		Log.i("2222", "position="+position);
		Log.i("2222", "item="+item);

		Intent intent = new Intent(getActivity(), SleepDetailActivity.class);
		intent.putExtra("item", item);
		startActivity(intent);
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
				Util.dip2pixel(paramContext, 73.0F),
				Util.dip2pixel(paramContext, 73.0F), Bitmap.Config.ARGB_8888);
		Canvas localCanvas = new Canvas(localBitmap);
		Paint localPaint = new Paint();
		localPaint.setAntiAlias(true);
		localPaint.setColor(Color.WHITE);
		localPaint.setStyle(Paint.Style.STROKE);
		localPaint.setStrokeWidth(Util.dip2pixel(paramContext, 3.0F));
		RectF localRectF = new RectF(Util.dip2pixel(paramContext, 1.8F),
				Util.dip2pixel(paramContext, 1.8F), Util.dip2pixel(
						paramContext, 71.0F), Util.dip2pixel(paramContext,
						71.0F));
		int j = (int) (360.0D * d);
		localCanvas.drawArc(localRectF, -90, j, false, localPaint);
		mPerArc.setImageBitmap(localBitmap);
	}

	@Override
	public void onRefresh() {
		if (MainService.getInstance() != null) {
			MainService.getInstance().syncWithDevice();
		}

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
