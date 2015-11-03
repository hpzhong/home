package com.zhuoyou.plugin.running;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.zhuoyi.appStatistics.entry.AppStatisticsEntry;
import com.zhuoyi.system.promotion.listener.ZyPromSDK;
import com.zhuoyou.plugin.add.AddPicture;
import com.zhuoyou.plugin.add.AddSports;
import com.zhuoyou.plugin.add.AddWeight;
import com.zhuoyou.plugin.ble.BleManagerService;
import com.zhuoyou.plugin.bluetooth.service.BluetoothService;
import com.zhuoyou.plugin.cloud.AlarmUtils;
import com.zhuoyou.plugin.cloud.CloudSync;
import com.zhuoyou.plugin.cloud.NetMsgCode;
import com.zhuoyou.plugin.cloud.RankInfoSync;
import com.zhuoyou.plugin.cloud.SportDataSync;
import com.zhuoyou.plugin.custom.CustomAlertDialog;
import com.zhuoyou.plugin.gps.FirstGpsActivity;
import com.zhuoyou.plugin.gps.GaoDeMapActivity;
import com.zhuoyou.plugin.gps.GaodeService;
import com.zhuoyou.plugin.gps.ServiceUtil;
import com.zhuoyou.plugin.info.PersonalInformation;
import com.zhuoyou.plugin.mainFrame.BottomViewItem;
import com.zhuoyou.plugin.mainFrame.FinderFragment;
import com.zhuoyou.plugin.mainFrame.MineFragment;
import com.zhuoyou.plugin.mainFrame.RankFragment;
import com.zhuoyou.plugin.rank.RankInfo;
import com.zhuoyou.plugin.selfupdate.Constants;
import com.zhuoyou.plugin.selfupdate.SelfUpdateMain;
import com.zhuoyou.plugin.view.BadgeView;
import com.zhuoyou.plugin.weather.WeatherTools;

public class Main extends FragmentActivity implements OnClickListener {

	private Context mContext = RunningApp.getInstance().getApplicationContext();
	private boolean config_dialog = false;
	private int mWidth;
	private RelativeLayout mAdd_layout;
	private ImageView mAdd_image_bg, mAdd_image_circle, mAdd_image,
			mAdd_card_background;
	private RelativeLayout mAdd_card_layout;
	private LinearLayout mAdd_heartrate, mAdd_weight, mAdd_mood, mAdd_gps,
			mAdd_sport;
	public static Handler mHandler;
	public static final int SHUT_DOWN_APP = 1;
	public static final int SELECT_FRAGMENT_HOME = 2;
	public static final int SHOW_CLOUD_SYNC_PROGRESS = 3;
	public static final int MSG_UNREAD = 4;
	public static final int ACT_STATE = 5;
	public static final int SLEEP_FRAGMENT_HOME = 6;
	// modefy by zhouzhongbo@20150106 start
	private BottomViewItem item;
	private BadgeView badgeView2, badgeView3;
	// modefy by zhouzhongbo@20150106 start
	private boolean isClick = false;
	private boolean mExpanded = false;
	private final int ANIM_DURATION = 400;
	private int perHeightShow = Tools.dip2px(mContext, 51);
	private int overlapping = Tools.dip2px(mContext, 10);
	private int tabHeight = Tools.dip2px(mContext, 71);
	private int currentTab = -1;
	private int lastTab = -1;
	private int current_tab_id = 0;
	private FragmentManager fragmentManager;
	private FragmentTransaction transaction;
	private Fragment newFragment;
	private SharedPreferences sharepreference;
	private boolean first_gps;
	public static boolean vFragment = true;
	public boolean isAutoSync = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			String FRAGMENTS_TAG = "android:support:fragments";
			savedInstanceState.putParcelable(FRAGMENTS_TAG, null);
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		WeatherTools.newInstance().getCurrWeather();
		DisplayMetrics outMetrics = getResources().getDisplayMetrics();
		sharepreference = this.getSharedPreferences("gaode_location_info",
				Context.MODE_PRIVATE);
		mWidth = outMetrics.widthPixels;
		// modefy by zhouzhongbo@20150106 start
		item = BottomViewItem.getInstance();
		fragmentManager = getSupportFragmentManager();
		initViews();
		setTabSelection(0);
		// modefy by zhouzhongbo@20150106 end
		initOC();
		if (getIntent() != null) {
			config_dialog = getIntent().getBooleanExtra("config_dialog", false);
			if (getIntent().getBooleanExtra("firmwear", false)) {
				sendBroadcast(new Intent(
						"com.zhuoyou.running.notification.firmwear"));
			}
		}
		if (Tools.checkIsFirstEntry(mContext)) {
			if (config_dialog) {
				showConfigDialog();
			}
			Tools.setFirstEntry(mContext);
		}
		if (SelfUpdateMain.isDownloading == false)
			SelfUpdateMain.execApkSelfUpdateRequest(this, Constants.APPID,
					Constants.CHNID);

		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				boolean state;
				switch (msg.what) {
				case SHUT_DOWN_APP:
					finish();
					break;
				case SELECT_FRAGMENT_HOME:
					setTabSelection(0);
					mHandler.sendEmptyMessageDelayed(SHOW_CLOUD_SYNC_PROGRESS,
							1000);
					break;
				case SHOW_CLOUD_SYNC_PROGRESS:
					AlarmUtils.setAutoSyncAlarm(mContext);
					CloudSync.autoSyncType = 1;
					CloudSync.syncAfterLogin(0);
					break;
				case MSG_UNREAD:
					state = Tools.getMsgState(mContext);
					if (state) {
						drawCircle(badgeView3);
					} else {
						cancleDrawCircle(badgeView3);
					}
					break;
				case ACT_STATE:
					state = Tools.getActState(mContext);
					if (state) {
						drawCircle(badgeView2);
					} else {
						cancleDrawCircle(badgeView2);
					}
					break;
				case SLEEP_FRAGMENT_HOME:
					if (vFragment) {
						setTabSelection(0);
					} else {
						Tools.setSleepCurrPageIndex(0);
						setTabSelection(4);
					}

					break;
				}
			}
		};
		// caixinxin add for delete thumbnail folder
		File fd = new File(Tools.getSDPath() + "/Running/.thumbnail/");
		if (fd.exists()) {
			Tools.deleteSDCardFolder(fd);
		}
		/* Add declaration for UpdateSelf and appStatistics. START */
		 AppStatisticsEntry entry2 = new
		 AppStatisticsEntry(getApplicationContext());
		 entry2.startAppStatistics(getApplicationContext());
		/* Add declaration for UpdateSelf and appStatistics. END */
	}

	// modefy by zhouzhongbo@20150106 start
	/**
	 * 底部菜单栏控件初始化
	 */
	private void initViews() {
		mAdd_card_layout = (RelativeLayout) findViewById(R.id.add_card_layout);
		mAdd_card_layout.setOnClickListener(onClickListener);

		mAdd_card_background = (ImageView) findViewById(R.id.add_card_background);
		mAdd_heartrate = (LinearLayout) findViewById(R.id.add_heartrate);
		mAdd_heartrate.setOnClickListener(onClickListener);
		mAdd_weight = (LinearLayout) findViewById(R.id.add_weight);
		mAdd_weight.setOnClickListener(onClickListener);
		// mAdd_mood = (LinearLayout) findViewById(R.id.add_mood);
		// mAdd_mood.setOnClickListener(onClickListener);
		mAdd_gps = (LinearLayout) findViewById(R.id.add_gps);
		mAdd_gps.setOnClickListener(onClickListener);
		mAdd_sport = (LinearLayout) findViewById(R.id.add_sport);
		mAdd_sport.setOnClickListener(onClickListener);
		mAdd_layout = (RelativeLayout) findViewById(R.id.add_layout);
		mAdd_image_bg = (ImageView) findViewById(R.id.add_image_bg);
		mAdd_image_circle = (ImageView) findViewById(R.id.add_image_circle);
		mAdd_image = (ImageView) findViewById(R.id.add_image);
		mAdd_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isClick) {
					isClick = true;
					if (mExpanded) {
						closeAddAnimation();
					} else {
						openAddAnimation();
					}
				}
			}
		});
		for (int i = 0; i < item.viewNum; i++) {
			item.linears[i] = (LinearLayout) findViewById(item.linears_id[i]);
			item.linears[i].setOnClickListener(this);
			item.images[i] = (ImageView) findViewById(item.images_id[i]);
			item.texts[i] = (TextView) findViewById(item.texts_id[i]);
		}
		badgeView2 = new BadgeView(this, item.images[2]);
		badgeView3 = new BadgeView(this, item.images[3]);
		if (Tools.getMsgState(mContext)) {
			drawCircle(badgeView3);
		}
		if (Tools.getActState(mContext)) {
			drawCircle(badgeView2);
		}
	}

	public void drawCircle(BadgeView bv) {
		bv.setBackgroundResource(R.drawable.remind_circle);
		bv.setBadgeMargin(3, 3);
		bv.setWidth(8);
		bv.setHeight(8);
		bv.toggle(false);
	}

	public void cancleDrawCircle(BadgeView bv) {
		if (bv != null) {
			bv.toggle(true);
			bv = null;
		}
	}

	private void showConfigDialog() {
		CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(this);
		builder.setTitle(R.string.alert_title);
		builder.setMessage(R.string.complete_personal_config);
		builder.setPositiveButton(R.string.perfect,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent intent = new Intent(Main.this,
								PersonalInformation.class);
						startActivity(intent);
					}
				});
		builder.setNegativeButton(R.string.no_thanks,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.setCancelable(true);
		// builder.create().show();
	}

	@Override
	public void onClick(View v) {
		if (current_tab_id != v.getId()) {
			for (int i = 0; i < item.linears_id.length; i++)
				if (v.getId() == item.linears_id[i]) {
					setTabSelection(i);
				}
		}
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String curr_date = Tools.getDate(0);
			switch (v.getId()) {
			case R.id.add_card_layout:
				if (mExpanded) {
					closeAddAnimation();
				}
				break;
			case R.id.add_heartrate:
				Toast.makeText(mContext, R.string.discover_hint,
						Toast.LENGTH_SHORT).show();
				break;
			case R.id.add_weight:
				Intent weightIntent = new Intent();
				weightIntent.putExtra("date", curr_date);
				weightIntent.setClass(mContext, AddWeight.class);
				startActivity(weightIntent);
				closeAddAnimation();
				break;
			// case R.id.add_mood:
			// Toast.makeText(mContext, R.string.discover_hint,
			// Toast.LENGTH_SHORT).show();
			// Intent moodIntent = new Intent();
			// moodIntent.putExtra("date", curr_date);
			// moodIntent.setClass(mContext, AddPicture.class);
			// startActivity(moodIntent);
			// closeAddAnimation();
			// break;
			case R.id.add_gps:
				Intent gpsIntent = new Intent();
				gpsIntent.putExtra("date", curr_date);
				gpsIntent.putExtra("from", "Main");
				first_gps = sharepreference.contains("is_first_gps");
				if (first_gps) {
					gpsIntent.setClass(mContext, GaoDeMapActivity.class);
				} else {
					gpsIntent.setClass(mContext, FirstGpsActivity.class);
				}
				startActivity(gpsIntent);
				closeAddAnimation();
				// Toast.makeText(mContext, R.string.discover_hint,
				// Toast.LENGTH_SHORT).show();
				break;
			case R.id.add_sport:
				Intent sportIntent = new Intent();
				sportIntent.putExtra("date", curr_date);
				sportIntent.setClass(mContext, AddSports.class);
				startActivity(sportIntent);
				closeAddAnimation();
				break;
			}
		}
	};

	/**
	 * @param index
	 *            根据索引值切换fragment
	 */
	private void setTabSelection(int index) {
		if (index == currentTab)
			return;
		lastTab = currentTab;
		currentTab = index;
		transaction = fragmentManager.beginTransaction();
		if (currentTab != lastTab && lastTab != -1) {
			Fragment mlastFragment = fragmentManager.findFragmentByTag(String
					.valueOf(lastTab));
			if ((mlastFragment != null&&mlastFragment.isAdded())) {
				transaction.detach(mlastFragment);
				Log.d("zzb11", "setTabSelection detach  mlastFragment ="
						+ mlastFragment);
			}
		}
		Fragment curFragment = fragmentManager.findFragmentByTag(String
				.valueOf(index));
		// note here,transaction is not realtime,so fragment.isDetached is
		// unsafe for judging.
		switch (index) {
		case 0:
			if (curFragment == null) {
				curFragment = new HomePageFragment();
				transaction.add(R.id.main_fragment, curFragment,
						String.valueOf(index));
			} else {
				transaction.attach(curFragment);
			}
			break;
		case 1:
			if (curFragment == null) {
				curFragment = new RankFragment(this);
				transaction.add(R.id.main_fragment, curFragment,
						String.valueOf(index));
			} else {
				transaction.attach(curFragment);
			}

			GetRankListDate();
			// zzb add for rank msg;
			break;
		case 2:
			if (curFragment == null) {
				curFragment = new FinderFragment();
				transaction.add(R.id.main_fragment, curFragment,
						String.valueOf(index));
			} else {
				transaction.attach(curFragment);
			}
			break;
		case 3:
			if (curFragment == null) {
				curFragment = new MineFragment(this);
				transaction.add(R.id.main_fragment, curFragment,
						String.valueOf(index));
			} else {
				transaction.attach(curFragment);
			}
			break;
		case 4:
			if (curFragment == null) {
				curFragment = new SleepPageFragment();
				transaction.add(R.id.main_fragment, curFragment,
						String.valueOf(index));
			} else {
				transaction.attach(curFragment);
			}
			break;
		default:
			break;

		}
		transaction.commit();// commitAllowingStateLoss();
		if (index != 4) {
			current_tab_id = item.linears_id[index];
		} else {
			current_tab_id = item.linears_id[0];
		}
		clearSelection();
		if (index != 4) {
			item.images[index].setImageResource(item.images_selected[index]);
			item.texts[index].setTextColor(getResources().getColor(
					R.color.bottom_text_selected));
		} else {
			item.images[0].setImageResource(item.images_selected[0]);
			item.texts[0].setTextColor(getResources().getColor(
					R.color.bottom_text_selected));
		}

	}

	// modefy by zhouzhongbo@20150106 end

	/**
	 * 清空所有图标和文字状态
	 */
	private void clearSelection() {
		for (int i = 0; i < item.viewNum; i++) {
			item.images[i].setImageResource(item.images_unselected[i]);
			item.texts[i].setTextColor(getResources().getColor(
					R.color.bottom_text_unselected));
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (BluetoothService.getInstance().isConnected()|| 
				(RunningApp.isBLESupport && BleManagerService.getInstance().GetBleConnectState())) {
			if (!isAutoSync) {
				// app 从后台唤醒，进入前台
				isAutoSync = true;

				if (MainService.getInstance() != null) {
					MainService.getInstance().syncWithDevice();
					Log.i("1111", "qqqqq");
					Message message = new Message();
					message.what = HomePageFragment.SYNC_DEVICE_START;
					message.arg1 = 1;
					HomePageFragment.mHandler.sendMessage(message);
				}
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (!isAppOnForeground()) {
			// app 进入后台
			isAutoSync = false;
			Log.i("1111", "eeeeee");

		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		/* Add call for UpdateSelf and appStatistics. START */
		AppStatisticsEntry entry2 = new AppStatisticsEntry(
				getApplicationContext());
		entry2.endAppStatistics(getApplicationContext());
		/* Add call for UpdateSelf and appStatistics. END */
		stopService(new Intent(Main.this, GaodeService.class)); 
	}

	@Override
	public void onBackPressed() {
		if (mExpanded) {
			closeAddAnimation();
		} else {
			// 直接放入后台
			moveTaskToBack(true);
		}
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		super.onActivityResult(arg0, arg1, arg2);
	}

	private void initOC() {
		ZyPromSDK.getInstance().init(getApplicationContext(), false);
	}

	/**
	 * 以下的几个方法用来，让fragment能够监听touch事件
	 */
	private ArrayList<MyOnTouchListener> onTouchListeners = new ArrayList<MyOnTouchListener>(
			10);

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		if (currentTab == 1) { // only listen in RankFragment
			for (MyOnTouchListener listener : onTouchListeners) {
				boolean result = listener.onTouch(ev);
				if (!result)
					return false;
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	public void registerMyOnTouchListener(MyOnTouchListener myOnTouchListener) {
		onTouchListeners.add(myOnTouchListener);
	}

	public void unregisterMyOnTouchListener(MyOnTouchListener myOnTouchListener) {
		onTouchListeners.remove(myOnTouchListener);
	}

	public interface MyOnTouchListener {
		public boolean onTouch(MotionEvent ev);
	}

	private void openAddAnimation() {
		AnimatorSet add_image = buildRoateAnimation(mAdd_image, false);
		AnimatorSet add_image_bg = buildScaleAnimation(mAdd_image_bg, false);
		AnimatorSet add_sport = buildTranslateAnimation(mAdd_sport, false,
				ANIM_DURATION / 4, 0 - overlapping);
		AnimatorSet add_gps = buildTranslateAnimation(mAdd_gps, false,
				ANIM_DURATION / 4 * 2, perHeightShow - overlapping);
		// AnimatorSet add_mood = buildTranslateAnimation(mAdd_mood, false,
		// ANIM_DURATION / 5 * 3, perHeightShow * 2 - overlapping);
		AnimatorSet add_weight = buildTranslateAnimation(mAdd_weight, false,
				ANIM_DURATION / 4 * 3, perHeightShow * 2 - overlapping);
		AnimatorSet add_heartrate = buildTranslateAnimation(mAdd_heartrate,
				false, ANIM_DURATION, perHeightShow * 3 - overlapping);
		add_image.playTogether(add_image_bg);
		add_image.playTogether(add_sport);
		add_image.playTogether(add_gps);
		// add_image.playTogether(add_mood);
		add_image.playTogether(add_weight);
		add_image.playTogether(add_heartrate);
		add_image.addListener(animationListener);
		add_image.start();
	}

	private void closeAddAnimation() {
		AnimatorSet add_image = buildRoateAnimation(mAdd_image, true);
		AnimatorSet add_image_bg = buildScaleAnimation(mAdd_image_bg, true);
		AnimatorSet add_sport = buildTranslateAnimation(mAdd_sport, true,
				ANIM_DURATION / 4, 0 - overlapping);
		AnimatorSet add_gps = buildTranslateAnimation(mAdd_gps, true,
				ANIM_DURATION / 4 * 2, perHeightShow - overlapping);
		// AnimatorSet add_mood = buildTranslateAnimation(mAdd_mood, true,
		// ANIM_DURATION / 5 * 3, perHeightShow * 2 - overlapping);
		AnimatorSet add_weight = buildTranslateAnimation(mAdd_weight, true,
				ANIM_DURATION / 4 * 3, perHeightShow * 2 - overlapping);
		AnimatorSet add_heartrate = buildTranslateAnimation(mAdd_heartrate,
				true, ANIM_DURATION, perHeightShow * 3 - overlapping);
		add_image.playTogether(add_image_bg);
		add_image.playTogether(add_sport);
		add_image.playTogether(add_gps);
		// add_image.playTogether(add_mood);
		add_image.playTogether(add_weight);
		add_image.playTogether(add_heartrate);
		add_image.addListener(animationListener);
		add_image.start();
	}

	private AnimatorSet buildRoateAnimation(View target, boolean flag) {
		AnimatorSet roate = new AnimatorSet();
		roate.playTogether(ObjectAnimator.ofFloat(target, "Rotation", flag ? 45
				: 0, flag ? 0 : 45));
		roate.setInterpolator(new DecelerateInterpolator());
		roate.setDuration(ANIM_DURATION);
		return roate;
	}

	private AnimatorSet buildScaleAnimation(View target, boolean flag) {
		int scale = mWidth / Tools.dip2px(mContext, 44) + 1;
		AnimatorSet scaleAni = new AnimatorSet();
		scaleAni.playTogether(
				ObjectAnimator.ofFloat(target, "scaleX", flag ? 1.0f : scale),
				ObjectAnimator.ofFloat(target, "scaleY", flag ? 1.0f : scale),
				ObjectAnimator.ofFloat(target, "alpha", flag ? 1.0f : 0.8f));
		scaleAni.setInterpolator(new DecelerateInterpolator());
		scaleAni.setDuration(ANIM_DURATION);
		return scaleAni;
	}

	private AnimatorSet buildTranslateAnimation(View target, boolean flag,
			int duration, int distance) {
		AnimatorSet translation = new AnimatorSet();
		translation.playTogether(ObjectAnimator.ofFloat(target, "translationY",
				flag ? 0 - distance : tabHeight, flag ? tabHeight
						: 0 - distance));
		translation.setInterpolator(new OvershootInterpolator(0.6F));
		translation.setDuration(duration);
		return translation;
	}

	private Animator.AnimatorListener animationListener = new Animator.AnimatorListener() {

		@Override
		public void onAnimationCancel(Animator arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationEnd(Animator arg0) {
			isClick = false;
			if (mExpanded) {
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						Tools.dip2px(mContext, 44), LayoutParams.MATCH_PARENT);
				params.addRule(RelativeLayout.CENTER_HORIZONTAL);
				mAdd_layout.setLayoutParams(params);
				mAdd_image_circle.setVisibility(View.GONE);
				mAdd_card_layout.setVisibility(View.GONE);
				mExpanded = false;
			} else {
				mExpanded = true;
			}
		}

		@Override
		public void onAnimationRepeat(Animator arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationStart(Animator arg0) {
			if (!mExpanded) {
				Animation animation = AnimationUtils.loadAnimation(mContext,
						R.anim.add_card_backgroud_image);
				animation.setFillAfter(true);
				mAdd_card_background.setAnimation(animation);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				mAdd_layout.setLayoutParams(params);
				mAdd_image_circle.setVisibility(View.VISIBLE);
				mAdd_card_layout.setVisibility(View.VISIBLE);
				mAdd_card_layout.setLayoutParams(params);

			}
		}

	};

	private List<RankInfo> sevenDalysList = new ArrayList<RankInfo>();
	private List<RankInfo> mouthDalysList = new ArrayList<RankInfo>();
	private List<RankInfo> highestStepList = new ArrayList<RankInfo>();
	private List<RankInfo> accountServenData = new ArrayList<RankInfo>();
	private List<RankInfo> accountMouthData = new ArrayList<RankInfo>();
	private List<RankInfo> accountHighestData = new ArrayList<RankInfo>();
	private long synctime = 0;

	public void GetRankListDate() {
		boolean isLogin = false;
		isLogin = Tools.getLogin(mContext);
		if (isLogin) {
			boolean isSync = CloudSync.isSync;
			long current_time = System.currentTimeMillis();
			// we limit Frequency of get rank data for change tab Frequently
			if ((synctime == 0)
					|| (((current_time - synctime) > 60000) && synctime != 0)) {
				if (Tools.getLogin(mContext) && !MainService.syncnow && !isSync) {
					// here we only sync sport date with has steps (by mars3
					// etc..)
					SportDataSync sportData = new SportDataSync(mContext, 2);
					sportData.postSportData(rank_handler);
				}
			} else {
				rank_handler.sendEmptyMessageDelayed(110011, 500);
			}
		}
	}

	public Handler rank_handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.d("zzb1", "msg:" + msg);
			switch (msg.what) {
			// case NetMsgCode.rank_state_wait:
			// break;
			// case NetMsgCode.rank_state_request:
			// new Thread(CloudSync.getNetRankInfoRunnable).start();
			// break;
			case NetMsgCode.The_network_link_success:
				switch (msg.arg1) {

				case NetMsgCode.getNetRankInfo:
					HashMap<String, List<RankInfo>> resRankList = (HashMap<String, List<RankInfo>>) msg.obj;
					if (resRankList.size() > 0) {
						if (resRankList.get("sevenDaysStepList") != null) {
							sevenDalysList = resRankList
									.get("sevenDaysStepList");
						}
						if (resRankList.get("monthStepList") != null) {
							mouthDalysList = resRankList.get("monthStepList");
						}
						if (resRankList.get("highestStepList") != null) {
							highestStepList = resRankList
									.get("highestStepList");
						}
						if (resRankList.get("accountSevenData") != null) {
							accountServenData = resRankList
									.get("accountSevenData");
						}
						if (resRankList.get("accountMonthData") != null) {
							accountMouthData = resRankList
									.get("accountMonthData");
						}
						if (resRankList.get("accountHighestData") != null) {
							accountHighestData = resRankList
									.get("accountHighestData");
						}
					}
					synctime = System.currentTimeMillis();
					if (currentTab == 1) {
						RankFragment curFragment = (RankFragment) fragmentManager
								.findFragmentByTag(String.valueOf(currentTab));
						if (curFragment != null && curFragment.isVisible()) {
							curFragment.UpdateRankView(sevenDalysList,
									mouthDalysList, highestStepList,
									accountServenData, accountMouthData,
									accountHighestData);
						}
					}
					break;

				case NetMsgCode.postSportInfo:
					new Thread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-gen public static Runnable
							// getNetRankInfoRunnable = new Runnable() {
							RankInfoSync rankInfo = new RankInfoSync(mContext);
							rankInfo.getNetRankInfo(rank_handler);
						};
					}).start();
					break;

				default:
					break;
				}
				break;
			case NetMsgCode.The_network_link_failure:
				Toast.makeText(mContext, R.string.network_link_failure,
						Toast.LENGTH_SHORT).show();
				break;

			case 110011:
				if (currentTab == 1) {
					RankFragment curFragment = (RankFragment) fragmentManager
							.findFragmentByTag(String.valueOf(currentTab));
					if (curFragment != null && curFragment.isVisible()) {
						curFragment.UpdateRankView(sevenDalysList,
								mouthDalysList, highestStepList,
								accountServenData, accountMouthData,
								accountHighestData);
					}
				}
				break;
			}
		}
	};

	/*
	 * add by zzb for get rank list msg
	 * 
	 * @param arg1:the type rank list
	 * 
	 * @return : list date of type
	 * 1:sevenDalysList,2:mouthDalysList,3highestStepList
	 * 4:accountServenData,5:accountMouthData,6:accountHighestData
	 */
	public List<RankInfo> GetRankDate(int type) {
		List<RankInfo> result = new ArrayList<RankInfo>();
		switch (type) {
		case 1:
			result = sevenDalysList;
			break;
		case 2:
			result = mouthDalysList;
			break;
		case 3:
			result = highestStepList;
			break;
		case 4:
			result = accountServenData;
			break;
		case 5:
			result = accountMouthData;
			break;
		case 6:
			result = accountHighestData;
			break;
		}
		return result;
	}

	// 从后台进入前台后进行设备数据自动同步
	public boolean isAppOnForeground() {
		// Returns a list of application processes that are running on the
		// device

		ActivityManager activityManager = (ActivityManager) getApplicationContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
		String packageName = getApplicationContext().getPackageName();

		List<RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		if (appProcesses == null)
			return false;

		for (RunningAppProcessInfo appProcess : appProcesses) {
			// The name of the process that this object is associated with.
			if (appProcess.processName.equals(packageName)
					&& appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				return true;
			}
		}

		return false;
	}
}
