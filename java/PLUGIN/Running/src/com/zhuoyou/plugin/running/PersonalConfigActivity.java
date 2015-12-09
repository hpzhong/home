package com.zhuoyou.plugin.running;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.cloud.CloudSync;
import com.zhuoyou.plugin.view.HorScrollView;
import com.zhuoyou.plugin.view.VerScrollView;

public class PersonalConfigActivity extends Activity implements View.OnClickListener {
	private Context mCtx;

	private RelativeLayout mTitleDescParent;

	private RelativeLayout mUserInfoWoman;

	private ImageView mUserInfoHeadWoman;

	private ImageView mUserInfoBodyWoman;

	private TextView mUserInfoSexWoman;

	private TextView mUserInfoSexDescWoman;

	private RelativeLayout mUserInfoMan;

	private ImageView mUserInfoHeadMan;

	private ImageView mUserInfoBodyMan;

	private TextView mUserInfoSexMan;

	private TextView mUserInfoSexDescMan;

	private RelativeLayout mWeightContainer;

	private TextView mWeightValue;

	private HorScrollView mWeightScroll;

	private float mWeightPanelTickValue;

	private RelativeLayout mHeightContainer;

	private TextView mHeightValue;

	private VerScrollView mHeightScroll;

	private float mHeightTickValue;

	private RelativeLayout mYearContainer;

	private TextView mYearValue;

	private HorScrollView mYearScroll;

	private float mYearTickValue;

	private RelativeLayout mBtnContainer;

	private Button mBtnPrev;

	private Button mBtnNext;

	private int mCurrStep = 0;

	private PersonalConfig mPersonalConfig;

	private float mTitleY, mWomanHeadY, mManHeadY;

	private Typeface mNewtype;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personal_config_layout);
		mCtx = this;
		RunningTitleBar.getTitleBar(this, getResources().getString(R.string.persion_config));

		initData();
		initView();
		init();
	}
	
	public void initData() {
		mPersonalConfig = Tools.getPersonalConfig();
		mNewtype = Typeface.createFromAsset(mCtx.getAssets(),"font/zidenzgroteskboldcond.ttf");
	}

	public void initView() {
		mTitleDescParent = (RelativeLayout) findViewById(R.id.setting_title_rl);

		mUserInfoWoman = (RelativeLayout) findViewById(R.id.setting_woman);
		mUserInfoHeadWoman = (ImageView) findViewById(R.id.userinfo_head_1);
		mUserInfoBodyWoman = (ImageView) findViewById(R.id.userinfo_body_1);
		mUserInfoSexWoman = (TextView) findViewById(R.id.userinfo_sex_1);
		mUserInfoSexDescWoman = (TextView) findViewById(R.id.userinfo_sex_desc_1);
		mUserInfoHeadWoman.setOnClickListener(this);
		mUserInfoBodyWoman.setVisibility(View.GONE);

		mUserInfoMan = (RelativeLayout) findViewById(R.id.setting_man);
		mUserInfoHeadMan = (ImageView) findViewById(R.id.userinfo_head_2);
		mUserInfoBodyMan = (ImageView) findViewById(R.id.userinfo_body_2);
		mUserInfoSexMan = (TextView) findViewById(R.id.userinfo_sex_2);
		mUserInfoSexDescMan = (TextView) findViewById(R.id.userinfo_sex_desc_2);
		mUserInfoHeadMan.setOnClickListener(this);
		mUserInfoBodyMan.setVisibility(View.GONE);

		mWeightContainer = (RelativeLayout) findViewById(R.id.weight_container);
		mWeightValue = (TextView) findViewById(R.id.weight_value);
		mWeightValue.setTypeface(mNewtype);
		mWeightScroll = (HorScrollView) findViewById(R.id.weight_scrollview);
		mWeightScroll.setOnScrollListener(mWeightScrollListener);
		mWeightPanelTickValue = Tools.dip2px(mCtx, 1262) / 180.0f;

		mHeightContainer = (RelativeLayout) findViewById(R.id.height_container);
		mHeightValue = (TextView) findViewById(R.id.height_value);
		mHeightValue.setTypeface(mNewtype);
		mHeightScroll = (VerScrollView) findViewById(R.id.height_scrollview);
		mHeightScroll.setOnScrollListener(mHeightScrollListener);
		mHeightTickValue = Tools.dip2px(mCtx, 1262) / 180.0f;

		mYearContainer = (RelativeLayout) findViewById(R.id.year_container);
		mYearValue = (TextView) findViewById(R.id.year_value);
		mYearValue.setTypeface(mNewtype);
		mYearScroll = (HorScrollView) findViewById(R.id.year_scrollview);
		mYearScroll.setOnScrollListener(mYearScrollListener);
		mYearTickValue = Tools.dip2px(mCtx, 632) / 90.0f;

		mBtnContainer = (RelativeLayout) findViewById(R.id.btn_container);
		mBtnPrev = (Button) findViewById(R.id.btn_previous);
		mBtnNext = (Button) findViewById(R.id.btn_next);
		mBtnPrev.setOnClickListener(this);
		mBtnNext.setOnClickListener(this);
	}

	public void init() {
		Log.i("gchk", "PersonalConfigFragment init");

		mCurrStep = 0;

		mTitleDescParent.setVisibility(View.VISIBLE);

		mUserInfoWoman.setVisibility(View.VISIBLE);
		mUserInfoHeadWoman.setVisibility(View.VISIBLE);
		mUserInfoBodyWoman.setVisibility(View.GONE);
		mUserInfoSexWoman.setVisibility(View.VISIBLE);
		mUserInfoSexDescWoman.setVisibility(View.VISIBLE);

		mUserInfoMan.setVisibility(View.VISIBLE);
		mUserInfoHeadMan.setVisibility(View.VISIBLE);
		mUserInfoBodyMan.setVisibility(View.GONE);
		mUserInfoSexMan.setVisibility(View.VISIBLE);
		mUserInfoSexDescMan.setVisibility(View.VISIBLE);

		mWeightContainer.setVisibility(View.GONE);

		mHeightContainer.setVisibility(View.GONE);

		mYearContainer.setVisibility(View.GONE);

		mBtnContainer.setVisibility(View.GONE);
		mBtnNext.setText(R.string.next);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_previous:
			if (mCurrStep == 3) {
				back2();
			} else if (mCurrStep == 2) {
				back1();
			} else if (mCurrStep == 1) {
				back0();
			}
			break;
		case R.id.btn_next:
			if (mCurrStep == 0) {
				goto1();
			} else if (mCurrStep == 1) {
				goto2();
			} else if (mCurrStep == 2) {
				goto3();
			} else if (mCurrStep == 3) {
				doDone();
			}
			break;
		case R.id.userinfo_head_1:
			if (mCurrStep == 0) {
				getViewParams();
				mPersonalConfig.setSex(PersonalConfig.SEX_WOMAN);
				goto1();
			}
			break;
		case R.id.userinfo_head_2:
			if (mCurrStep == 0) {
				getViewParams();
				mPersonalConfig.setSex(PersonalConfig.SEX_MAN);
				goto1();
			}
			break;
		default:
			break;
		}
	}

	private void getViewParams() {
		if (mTitleY == 0) {
			int[] location = new int[2];
			mTitleDescParent.getLocationOnScreen(location);
			mTitleY = location[1];
			mUserInfoHeadWoman.getLocationOnScreen(location);
			mWomanHeadY = location[1];
			mUserInfoHeadMan.getLocationOnScreen(location);
			mManHeadY = location[1];
			Log.i("gchk", mTitleY + " | " + mWomanHeadY + " | " + mManHeadY);
		}
	}

	private void goto1() {
		mCurrStep = 1;

		// hide title
		CommonFadeOut(mTitleDescParent);

		if (mPersonalConfig.getSex() == PersonalConfig.SEX_MAN) {
			// up man info view
			CommonUp(mUserInfoMan, mTitleY - mManHeadY);
			// hide woman info view
			CommonFadeOut(mUserInfoWoman);
			// hide man description
			CommonFadeOut(mUserInfoSexMan);
			CommonFadeOut(mUserInfoSexDescMan);
			// show man body
			CommonFadeIn(mUserInfoBodyMan);
		} else if (mPersonalConfig.getSex() == PersonalConfig.SEX_WOMAN) {
			// up woman info view
			CommonUp(mUserInfoWoman, mTitleY - mWomanHeadY);
			// hide man info view
			CommonFadeOut(mUserInfoMan);
			// hide woman description
			CommonFadeOut(mUserInfoSexWoman);
			CommonFadeOut(mUserInfoSexDescWoman);
			// show woman body
			CommonFadeIn(mUserInfoBodyWoman);
		}

		// show weight
		setWeightScrollFromWeight(mPersonalConfig.getWeight());
		CommonFadeIn(mWeightContainer);

		// show button
		CommonFadeIn(mBtnContainer);
	}

	private void goto2() {
		mCurrStep = 2;

		// move info view down and left according to sex
		if (mPersonalConfig.getSex() == PersonalConfig.SEX_MAN) {
			TranslateDownLeftAni(mUserInfoMan, mTitleY - mManHeadY);
		} else if (mPersonalConfig.getSex() == PersonalConfig.SEX_WOMAN) {
			TranslateDownLeftAni(mUserInfoWoman, mTitleY - mWomanHeadY);
		}

		// hide weight，
		CommonFadeOut(mWeightContainer);

		// show height
		setHeightScrollFromHeight(mPersonalConfig.getHeight());
		CommonFadeIn(mHeightContainer);
	}

	private void goto3() {
		mCurrStep = 3;

		// move info view up and right according to sex
		if (mPersonalConfig.getSex() == PersonalConfig.SEX_MAN) {
			TranslateUpRightAni(mUserInfoMan, mTitleY - mManHeadY);
		} else if (mPersonalConfig.getSex() == PersonalConfig.SEX_WOMAN) {
			TranslateUpRightAni(mUserInfoWoman, mTitleY - mWomanHeadY);
		}

		// hide height
		CommonFadeOut(mHeightContainer);

		// show year
		setYearScrollFromYear(mPersonalConfig.getYear());
		CommonFadeIn(mYearContainer);

		// change next button to done
		mBtnNext.setText(R.string.done);
	}

	private void back0() {
		mCurrStep = 0;

		CommonFadeIn(mTitleDescParent);

		if (mPersonalConfig.getSex() == PersonalConfig.SEX_WOMAN) {
			CommonDown(mUserInfoWoman, mTitleY - mWomanHeadY);
			CommonFadeIn(mUserInfoMan);
			CommonFadeIn(mUserInfoSexWoman);
			CommonFadeIn(mUserInfoSexDescWoman);
			CommonFadeOut(mUserInfoBodyWoman);
		} else if (mPersonalConfig.getSex() == PersonalConfig.SEX_MAN) {
			CommonDown(mUserInfoMan, mTitleY - mManHeadY);
			CommonFadeIn(mUserInfoWoman);
			CommonFadeIn(mUserInfoSexMan);
			CommonFadeIn(mUserInfoSexDescMan);
			CommonFadeOut(mUserInfoBodyMan);
		}

		CommonFadeOut(mWeightContainer);

		CommonFadeOut(mBtnContainer);
	}

	private void back1() {
		mCurrStep = 1;

		if (mPersonalConfig.getSex() == PersonalConfig.SEX_MAN) {
			TranslateUpRightAni(mUserInfoMan, mTitleY - mManHeadY);
		} else if (mPersonalConfig.getSex() == PersonalConfig.SEX_WOMAN) {
			TranslateUpRightAni(mUserInfoWoman, mTitleY - mWomanHeadY);
		}

		CommonFadeIn(mWeightContainer);

		CommonFadeOut(mHeightContainer);
	}

	private void back2() {
		mCurrStep = 2;

		if (mPersonalConfig.getSex() == PersonalConfig.SEX_MAN) {
			TranslateDownLeftAni(mUserInfoMan, mTitleY - mManHeadY);
		} else if (mPersonalConfig.getSex() == PersonalConfig.SEX_WOMAN) {
			TranslateDownLeftAni(mUserInfoWoman, mTitleY - mWomanHeadY);
		}

		CommonFadeIn(mHeightContainer);

		CommonFadeOut(mYearContainer);

		mBtnNext.setText(R.string.next);
	}

	private void CommonFadeOut(View v) {
		Animation FadeOut = AnimationUtils.loadAnimation(mCtx, R.anim.fade_out);
		v.startAnimation(FadeOut);
		v.setVisibility(View.GONE);
	}

	private void CommonFadeIn(View v) {
		Animation FadeIn = AnimationUtils.loadAnimation(mCtx, R.anim.fade_in);
		v.startAnimation(FadeIn);
		v.setVisibility(View.VISIBLE);
	}

	private void CommonUp(View v, float H) {
		TranslateAnimation up = new TranslateAnimation(0, 0, 0, H);
		up.setDuration(500);
		up.setInterpolator(new DecelerateInterpolator());
		up.setFillAfter(true);
		v.startAnimation(up);
	}

	private void CommonDown(View v, float H) {
		TranslateAnimation up = new TranslateAnimation(0, 0, H, 0);
		up.setDuration(500);
		up.setInterpolator(new DecelerateInterpolator());
		up.setFillAfter(true);
		v.startAnimation(up);
	}

	private void TranslateDownLeftAni(View v, float H) {
		int xDelta = Tools.dip2px(mCtx, 40);
		int yDelta = Tools.dip2px(mCtx, 130);
		TranslateAnimation up = new TranslateAnimation(0, -xDelta, H, H + yDelta);
		up.setDuration(500);
		up.setInterpolator(new DecelerateInterpolator());
		up.setFillAfter(true);
		v.startAnimation(up);
	}

	private void TranslateUpRightAni(View v, float H) {
		int xDelta = Tools.dip2px(mCtx, 40);
		int yDelta = Tools.dip2px(mCtx, 130);
		TranslateAnimation up = new TranslateAnimation(-xDelta, 0, H + yDelta, H);
		up.setDuration(500);
		up.setInterpolator(new DecelerateInterpolator());
		up.setFillAfter(true);
		v.startAnimation(up);
	}

	private HorScrollView.OnScrollListener mWeightScrollListener = new HorScrollView.OnScrollListener() {
		@Override
		public void onScroll(int scrollX) {
			float x = scrollX;
			float a = x / mWeightPanelTickValue;
			int b = (int) (a + 0.5);
			mWeightValue.setText(b + 25 + "");
			mPersonalConfig.setWeight(b + 25);
		}
	};

	private void setWeightScrollFromWeight(int weight) {
		mWeightValue.setText(weight + "");
		float a = weight - 25;
		float b = a * mWeightPanelTickValue;
		final int c = (int) (b + 0.5);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mWeightScroll.scrollTo(c, 0);
			}
		}, 100);
	}

	private VerScrollView.OnScrollListener mHeightScrollListener = new VerScrollView.OnScrollListener() {
		@Override
		public void onScroll(int scrollY) {
			float y = scrollY;
			float a = y / mHeightTickValue;
			int b = (int) (a + 0.5);
			b = 235 - b;

			mHeightValue.setText(b + "");
			mPersonalConfig.setHeight(b);
		}
	};

	private void setHeightScrollFromHeight(int height) {
		mHeightValue.setText(height + "");
		int a = 235 - height;
		float b = a * mHeightTickValue;
		final int c = (int) (b + 0.5);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mHeightScroll.scrollTo(0, c);
			}
		}, 100);
	}

	private HorScrollView.OnScrollListener mYearScrollListener = new HorScrollView.OnScrollListener() {
		@Override
		public void onScroll(int scrollX) {
			float x = scrollX;
			float a = x / mYearTickValue;
			int b = (int) (a + 0.5);
			mYearValue.setText(b + 1925 + "");
			mPersonalConfig.setYear(b + 1925);
		}
	};

	private void setYearScrollFromYear(int year) {
		mYearValue.setText(year + "");
		float a = year - 1925;
		float b = a * mYearTickValue;
		final int c = (int) (b + 0.5);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mYearScroll.scrollTo(c, 0);
			}
		}, 100);
	}

	private void doDone() {
		Tools.updatePersonalConfig(mPersonalConfig);
		CloudSync.startSyncInfo();
		finish();
	}
}
