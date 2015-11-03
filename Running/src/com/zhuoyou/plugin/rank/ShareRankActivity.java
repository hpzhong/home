package com.zhuoyou.plugin.rank;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.weibo.net.AsyncWeiboRunner.RequestListener;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboException;
import com.zhuoyou.plugin.running.CalTools;
import com.zhuoyou.plugin.running.PersonalConfig;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.SharePopupWindow;
import com.zhuoyou.plugin.running.Tools;
import com.zhuoyou.plugin.share.AuthorizeActivity;
import com.zhuoyou.plugin.share.ShareTask;
import com.zhuoyou.plugin.share.ShareToWeixin;

public class ShareRankActivity extends Activity implements OnClickListener {

	private Context mContext;
	private RelativeLayout mScreenshot;
	private ScrollView myScreenshot;
	private ImageView mUser_logo, myUser_logo, mLogobg, myLogobg, mTargetState, right, left, mTypeIcon, myTypeIcon;
	private ImageView mShare_weixin, myShare_weixin;
	private ImageView mShare_quan, myShare_quan;
	private ImageView mShare_qq, myShare_qq;
	private ImageView mMore, myMore;
	private SharePopupWindow mPopupWindow;
	private static int select = 0;
	private boolean isWXInstalled = false;
	private boolean isWBInstalled = false;
	private boolean isQQInstalled = false;
	private Weibo weibo = Weibo.getInstance();
	private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
	private TextView mUser_name, myUser_name, mPlace, myPlace, mNumTv, myNumTv;
	private ListView moRankList;
	private List<RankInfo> rankList = new ArrayList<RankInfo>();
	private List<RankInfo> myRank = new ArrayList<RankInfo>();
	private RankListAdapter mListAdapter = null;
	private List<View> viewGroup = new ArrayList<View>();
	private View appView1, appView2;
	private ViewPager viewPager;
	private TextView mData, myData, mShare_step, mShare_distance, mShare_cal, mShare_food;
	private Typeface mNumberTP;
	private int distance, cal = 0;
	private int type;
	private ImageView myOptimal, myOptimal2;
	private Bitmap bmp = null;

	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_rank);
		mContext = this;
		Intent intent = getIntent();
		if (intent != null && Tools.getLogin(mContext)) {
			type = intent.getIntExtra("type", 0);
			Object[] objs = (Object[]) intent.getSerializableExtra("RankList");
			if (objs != null) {
				for (int i = 0; i < objs.length; ++i) {
					rankList.add((RankInfo) objs[i]);
				}
			}
			Object[] objs2 = (Object[]) intent.getSerializableExtra("MyRank");
			if (objs2 != null) {
				for (int i = 0; i < objs2.length; ++i) {
					myRank.add((RankInfo) objs2[i]);
				}
			}
		}

		viewPager = (ViewPager) findViewById(R.id.share_viewpage);
		@SuppressWarnings("static-access")
		LayoutInflater layoutInflater = getLayoutInflater().from(this);
		appView1 = layoutInflater.inflate(R.layout.share_rank_list, null);
		appView2 = layoutInflater.inflate(R.layout.share_myrank, null);
		viewGroup.add(appView1);
		viewGroup.add(appView2);
		viewPager.setAdapter(new MyPagerAdapter(viewGroup));

		mNumberTP = RunningApp.getCustomNumberFont();
		initView();
		initData(rankList, myRank);
		getShareAppStatus();
	}

	void initData(List<RankInfo> mList, List<RankInfo> myList) {
		if (mList != null) {
			mListAdapter = new RankListAdapter(this);
			mListAdapter.refreshListInfo(mList);
			moRankList.setDivider(null);
			moRankList.setAdapter(mListAdapter);
		}
	}

	private void initView() {
		mScreenshot = (RelativeLayout) appView1.findViewById(R.id.screenshot);
		myScreenshot = (ScrollView) appView2.findViewById(R.id.screenshot);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String str = formatter.format(curDate);
		mData = (TextView) appView1.findViewById(R.id.data);
		myData = (TextView) appView2.findViewById(R.id.data);
		mData.setText(str);
		myData.setText(str);

		mTypeIcon = (ImageView) appView1.findViewById(R.id.typeIcon);
		myTypeIcon = (ImageView) appView2.findViewById(R.id.typeIcon);
		if (type != 0) {
			switch (type) {
			case 1:
				mTypeIcon.setImageResource(R.drawable.week_rank_icon);
				myTypeIcon.setImageResource(R.drawable.week_rank_icon);
				break;
			case 2:
				mTypeIcon.setImageResource(R.drawable.mouth_rank_icon);
				myTypeIcon.setImageResource(R.drawable.mouth_rank_icon);
				break;
			case 3:
				mTypeIcon.setImageResource(R.drawable.day_rank_icon);
				myTypeIcon.setImageResource(R.drawable.day_rank_icon);
				break;
			default:
				break;
			}
		}

		mUser_logo = (ImageView) appView1.findViewById(R.id.user_logo);
		myUser_logo = (ImageView) appView2.findViewById(R.id.user_logo);
		int headIndex = Tools.getHead(this);
		if (headIndex == 10000) {
			bmp = Tools.convertFileToBitmap("/Running/download/custom");
			mUser_logo.setImageBitmap(bmp);
			myUser_logo.setImageBitmap(bmp);
		} else if (headIndex == 1000) {
			bmp = Tools.convertFileToBitmap("/Running/download/logo");
			mUser_logo.setImageBitmap(bmp);
			myUser_logo.setImageBitmap(bmp);
		} else {
			mUser_logo.setImageResource(Tools.selectByIndex(headIndex));
			myUser_logo.setImageResource(Tools.selectByIndex(headIndex));
		}

		mUser_name = (TextView) appView1.findViewById(R.id.user_name);
		myUser_name = (TextView) appView2.findViewById(R.id.user_name);
		if (Tools.getUsrName(this).equals("")) {
			if (!Tools.getLoginName(this).equals("")) {
				mUser_name.setText(Tools.getLoginName(this));
				myUser_name.setText(Tools.getLoginName(this));
			} else {
				mUser_name.setText(R.string.username);
				myUser_name.setText(R.string.username);
			}
		} else {
			mUser_name.setText(Tools.getUsrName(this));
			myUser_name.setText(Tools.getUsrName(this));
		}
		mPlace = (TextView) appView1.findViewById(R.id.place);
		myPlace = (TextView) appView2.findViewById(R.id.place);
		mNumTv = (TextView) appView1.findViewById(R.id.num_tv);
		myNumTv = (TextView) appView2.findViewById(R.id.num_tv);
		mPlace.setTypeface(mNumberTP);
		myPlace.setTypeface(mNumberTP);
		int place = 0;
		int steps = 0;
		if (myRank.size() > 0) {
			place = myRank.get(0).getRank();
			steps = Integer.parseInt(myRank.get(0).getmSteps());
			mPlace.setText(place + "");
			myPlace.setText(place + "");
		} else {
			mPlace.setText("");
			myPlace.setText("");
			mNumTv.setText(R.string.no_place);
			myNumTv.setText(R.string.no_place);
		}

		mTargetState = (ImageView) appView2.findViewById(R.id.target_state);
		mLogobg = (ImageView) appView1.findViewById(R.id.user_logo_bg);
		myLogobg = (ImageView) appView2.findViewById(R.id.user_logo_bg);
		myOptimal = (ImageView) appView2.findViewById(R.id.optimal_icon);
		myOptimal2 = (ImageView) appView2.findViewById(R.id.optimal_icon2);
		
		switch (place) {
		case 1:
			mLogobg.setImageResource(R.drawable.head_decoration_top1);
			myLogobg.setImageResource(R.drawable.head_decoration_top1);
			mTargetState.setImageResource(R.drawable.share_rank_top);
			myOptimal.setVisibility(View.VISIBLE);
			myOptimal2.setVisibility(View.VISIBLE);
			break;
		case 2:
			mLogobg.setImageResource(R.drawable.head_decoration_top2);
			myLogobg.setImageResource(R.drawable.head_decoration_top2);
			mTargetState.setImageResource(R.drawable.share_rank_top);
			myOptimal.setVisibility(View.VISIBLE);
			myOptimal2.setVisibility(View.VISIBLE);
			break;
		case 3:
			mLogobg.setImageResource(R.drawable.head_decoration_top3);
			myLogobg.setImageResource(R.drawable.head_decoration_top3);
			mTargetState.setImageResource(R.drawable.share_rank_top);
			myOptimal.setVisibility(View.VISIBLE);
			myOptimal2.setVisibility(View.VISIBLE);
			break;
		default:
			mLogobg.setImageResource(R.drawable.head_decoration);
			myLogobg.setImageResource(R.drawable.head_decoration);
			mTargetState.setImageResource(R.drawable.share_rank_back);
			myOptimal.setVisibility(View.GONE);
			myOptimal2.setVisibility(View.GONE);
			break;
		}

		right = (ImageView) appView1.findViewById(R.id.next);
		right.setOnClickListener(rightOnclick);
		left = (ImageView) appView2.findViewById(R.id.left);
		left.setOnClickListener(leftOnclick);

		moRankList = (ListView) appView1.findViewById(R.id.moListRank);

		mShare_step = (TextView) appView2.findViewById(R.id.share_step);
		mShare_step.setText(steps + "");
		mShare_step.setTypeface(mNumberTP);
		PersonalConfig config = Tools.getPersonalConfig();
		distance = Tools.calcDistance(steps, config.getHeight());
		mShare_distance = (TextView) appView2.findViewById(R.id.share_distance);
		mShare_distance.setText(getResources().getString(R.string.walk) + " "
				+ String.valueOf(getKilometer(distance)) + " "
				+ getResources().getString(R.string.kilometre));
		mShare_cal = (TextView) appView2.findViewById(R.id.share_cal);
		cal = Tools.calcCalories(distance, config.getWeightNum());
		mShare_cal.setText(cal + "");
		mShare_cal.setTypeface(mNumberTP);
		mShare_food = (TextView) appView2.findViewById(R.id.share_food);
		mShare_food.setText("≈" + CalTools.getResultFromCal(mContext, cal));

		mShare_weixin = (ImageView) appView1.findViewById(R.id.share_weixin);
		mShare_weixin.setOnClickListener(this);
		mShare_quan = (ImageView) appView1.findViewById(R.id.share_quan);
		mShare_quan.setOnClickListener(this);
		mShare_qq = (ImageView) appView1.findViewById(R.id.share_qq);
		mShare_qq.setOnClickListener(this);
		mMore = (ImageView) appView1.findViewById(R.id.share_more);
		mMore.setOnClickListener(this);
		
		myShare_weixin = (ImageView) appView2.findViewById(R.id.share_my_weixin);
		myShare_weixin.setOnClickListener(this);
		myShare_quan = (ImageView) appView2.findViewById(R.id.share_my_quan);
		myShare_quan.setOnClickListener(this);
		myShare_qq = (ImageView) appView2.findViewById(R.id.share_my_qq);
		myShare_qq.setOnClickListener(this);
		myMore = (ImageView) appView2.findViewById(R.id.share_my_more);
		myMore.setOnClickListener(this);
	}

	OnClickListener rightOnclick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int index = viewPager.getCurrentItem();
			viewPager.setCurrentItem(++index, true);
		}
	};

	OnClickListener leftOnclick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int index = viewPager.getCurrentItem();
			viewPager.setCurrentItem(--index, true);
		}
	};

	private float getKilometer(int meter) {
		float f = meter / 1000;
		int scale = 2;
		int roundingMode = 4;
		BigDecimal bd = new BigDecimal((double) f);
		bd = bd.setScale(scale, roundingMode);
		f = bd.floatValue();
		return f;
	}

	private void getScreenShot(View mScreenshot, String fileName) {
		int h = mScreenshot.getHeight();
		if (mScreenshot instanceof ScrollView) {
			ScrollView scrollView = (ScrollView) mScreenshot;
			h = 0;
			for (int i = 0; i < scrollView.getChildCount(); i++) {
				h += scrollView.getChildAt(i).getHeight();
			}
		}
		Bitmap bmp = Bitmap.createBitmap(mScreenshot.getWidth(), h, Config.RGB_565);
		mScreenshot.draw(new Canvas(bmp));
		Tools.saveBitmapToFile(bmp, fileName);
		bmp.recycle();
		bmp = null;
	}

	@Override
	public void onClick(View v) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String fileName = format.format(new Date(System.currentTimeMillis())) + ".jpg";
		int item = viewPager.getCurrentItem();
		switch (v.getId()) {
		case R.id.share_weixin:
			if (item == 0) {
				getScreenShot(mScreenshot, fileName);
			} else if (item == 1) {
				getScreenShot(myScreenshot, fileName);
			}
			if (isWXInstalled) {
				if (ShareToWeixin.api.isWXAppSupportAPI())
					ShareToWeixin.SharetoWX(mContext, false, fileName);
				else
					Toast.makeText(mContext, R.string.weixin_no_support,
							Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext, R.string.install_weixin,
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.share_quan:
			if (item == 0) {
				getScreenShot(mScreenshot, fileName);
			} else if (item == 1) {
				getScreenShot(myScreenshot, fileName);
			}
			if (isWXInstalled) {
				int wxSdkVersion = ShareToWeixin.api.getWXAppSupportAPI();
				if (wxSdkVersion >= TIMELINE_SUPPORTED_VERSION)
					ShareToWeixin.SharetoWX(mContext, true, fileName);
				else
					Toast.makeText(mContext, R.string.weixin_no_support_quan,
							Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext, R.string.install_weixin,
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.share_qq:
			if (item == 0) {
				getScreenShot(mScreenshot, fileName);
			} else if (item == 1) {
				getScreenShot(myScreenshot, fileName);
			}
			if (isQQInstalled) {
				shareToQQ(fileName);
			} else {
				Toast.makeText(mContext, R.string.install_qq,
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.share_more:
			if (item == 0) {
				getScreenShot(mScreenshot, fileName);
			} else if (item == 1) {
				getScreenShot(myScreenshot, fileName);
			}
			mPopupWindow = new SharePopupWindow(ShareRankActivity.this, itemsOnClick, fileName);
			mPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
			mPopupWindow
					.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
			mPopupWindow.showAtLocation(
					ShareRankActivity.this.findViewById(R.id.main),
					Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
			break;
		case R.id.share_my_weixin:
			if (item == 0) {
				getScreenShot(mScreenshot, fileName);
			} else if (item == 1) {
				getScreenShot(myScreenshot, fileName);
			}
			if (isWXInstalled) {
				if (ShareToWeixin.api.isWXAppSupportAPI())
					ShareToWeixin.SharetoWX(mContext, false, fileName);
				else
					Toast.makeText(mContext, R.string.weixin_no_support,
							Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext, R.string.install_weixin,
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.share_my_quan:
			if (item == 0) {
				getScreenShot(mScreenshot, fileName);
			} else if (item == 1) {
				getScreenShot(myScreenshot, fileName);
			}
			if (isWXInstalled) {
				int wxSdkVersion = ShareToWeixin.api.getWXAppSupportAPI();
				if (wxSdkVersion >= TIMELINE_SUPPORTED_VERSION)
					ShareToWeixin.SharetoWX(mContext, true, fileName);
				else
					Toast.makeText(mContext, R.string.weixin_no_support_quan,
							Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext, R.string.install_weixin,
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.share_my_qq:
			if (item == 0) {
				getScreenShot(mScreenshot, fileName);
			} else if (item == 1) {
				getScreenShot(myScreenshot, fileName);
			}
			if (isQQInstalled) {
				shareToQQ(fileName);
			} else {
				Toast.makeText(mContext, R.string.install_qq,
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.share_my_more:
			if (item == 0) {
				getScreenShot(mScreenshot, fileName);
			} else if (item == 1) {
				getScreenShot(myScreenshot, fileName);
			}
			mPopupWindow = new SharePopupWindow(ShareRankActivity.this, itemsOnClick, fileName);
			mPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
			mPopupWindow
					.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
			mPopupWindow.showAtLocation(
					ShareRankActivity.this.findViewById(R.id.main),
					Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
			break;
		case R.id.img_back:
			finish();
			break;
		}

	}

	private OnClickListener itemsOnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.share_weibo:
				if (!weibo.isSessionValid()) {
					Intent intent = new Intent();
					intent.setClass(ShareRankActivity.this, AuthorizeActivity.class);
					startActivity(intent);
				} else {
					select = 1;
					SharePopupWindow.mInstance.getWeiboView().setImageResource(R.drawable.share_wb_select);
				}
				break;
			case R.id.share:
				if (select > 0) {
					if (SharePopupWindow.mInstance != null) {
						String share_s = SharePopupWindow.mInstance.getShareContent();
						share2weibo(share_s, Tools.getScreenShot(SharePopupWindow.mInstance.getShareFileName()));
					}
				} else {
					Toast.makeText(mContext, R.string.select_platform, Toast.LENGTH_SHORT).show();
				}
				break;
			}
		}

	};

	private void getShareAppStatus() {
		PackageManager pm = getPackageManager();
		Intent filterIntent = new Intent(Intent.ACTION_SEND, null);
		filterIntent.addCategory(Intent.CATEGORY_DEFAULT);
		filterIntent.setType("text/plain");

		List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();
		resolveInfos.addAll(pm.queryIntentActivities(filterIntent,
				PackageManager.COMPONENT_ENABLED_STATE_DEFAULT));

		for (int i = 0; i < resolveInfos.size(); i++) {
			ResolveInfo resolveInfo = resolveInfos.get(i);
			String mPackageName = resolveInfo.activityInfo.packageName;
			if (mPackageName.equals("com.tencent.mm")) {
				isWXInstalled = true;
			}
			if (mPackageName.equals("com.sina.weibo")) {
				isWBInstalled = true;
			}
			if (mPackageName.equals("com.tencent.mobileqq")) {
				isQQInstalled = true;
			}
		}
	}

	private void shareToQQ(String fileName) {
		ComponentName cp = new ComponentName("com.tencent.mobileqq",
				"com.tencent.mobileqq.activity.JumpActivity");
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setComponent(cp);
		intent.setType("image/*");
		File file = new File(Tools.getScreenShot(fileName));
		Uri uri = Uri.fromFile(file);
		intent.putExtra(Intent.EXTRA_STREAM, uri);
		this.startActivity(intent);
	}

	private void share2weibo(String content, String picpath) {
		ShareTask task = new ShareTask(this, picpath, content, mRequestListener);
		new Thread(task).start();
		if (mPopupWindow.isShowing())
			mPopupWindow.dismiss();
	}

	private RequestListener mRequestListener = new RequestListener() {

		@Override
		public void onComplete(String response) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(ShareRankActivity.this,
							R.string.share_success, Toast.LENGTH_SHORT).show();
				}
			});
		}

		@Override
		public void onError(final WeiboException e) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(ShareRankActivity.this,
							e.getStatusMessage(), Toast.LENGTH_SHORT).show();
				}
			});
		}

		@Override
		public void onIOException(final IOException e) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(ShareRankActivity.this, e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}
			});
		}

	};

	public static Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				select = 1;
				break;
			}
		}
	};

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (bmp != null) {
			bmp.recycle();
			bmp = null;
			System.gc();
		}
	}

	private class MyPagerAdapter extends PagerAdapter {
		public List<View> mlist;

		public MyPagerAdapter(List<View> mlist) {
			this.mlist = mlist;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView(mlist.get(position));
		}

		@Override
		public void finishUpdate(ViewGroup container) {
			super.finishUpdate(container);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			((ViewPager) container).addView(mlist.get(position), 0);
			return mlist.get(position);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
			super.restoreState(state, loader);
		}

		@Override
		public int getCount() {
			return mlist.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

	}
}
