package com.zhuoyou.plugin.running;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.cloud.CloudSync;
import com.zhuoyou.plugin.view.HorScrollView;
import com.zhuoyou.plugin.view.VerScrollView;

public class Guide extends Activity implements View.OnClickListener {

	private Context mCtx;
	private PersonalConfig mPersonalConfig;
	private PersonalGoal mPersonalGoal;
	private Typeface mNewtype;
	private LinearLayout mGoal_layout,mGoal_layout_title;
	private RelativeLayout mConfig_layout;
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
	private float mTitleY, mWomanHeadY, mManHeadY, mBtnY;

	private ImageView mPersonal;
	private TextView mGoals1, mGoals2, mGoals3;
	private EditText mSteps, mCal;
	private Button mSave;
	private RelativeLayout mBtn_layout;
	private Button mBtn_pre;
	private Button mBtn_done;
	
	Boolean test_center = false;
	private LinearLayout mTestCenter_layout;
	private ImageView test_personal;
	private TextView bmi_value;
	private TextView bmi_state;
	private ImageView state1, state2, state3, state4, state5, state6;
	private Button mBack;
	private Button mDone;
	private TextView tv_title;
	private ImageView back_img;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personal_config_layout);
		mCtx = this;
		Intent intent = getIntent();
		if (intent != null) {
			test_center = intent.getBooleanExtra("test_center", false);
		}
		
		tv_title = (TextView) findViewById(R.id.title);
		back_img = (ImageView) findViewById(R.id.back_m);
		back_img.setVisibility(View.GONE);
		initData();
		initView();
		init();
	}
	
	public void initData() {
		mPersonalConfig = Tools.getPersonalConfig();
		mPersonalGoal = Tools.getPersonalGoal();
		mNewtype = Typeface.createFromAsset(mCtx.getAssets(),"font/zidenzgroteskboldcond.ttf");
	}

	public void initView() {
		mGoal_layout = (LinearLayout) findViewById(R.id.goal_layout);
		mGoal_layout_title=(LinearLayout)mGoal_layout.findViewById(R.id.running_title);
		mGoal_layout_title.setVisibility(View.GONE);
		mPersonal = (ImageView) mGoal_layout.findViewById(R.id.personal);
		mGoals1 = (TextView) mGoal_layout.findViewById(R.id.goals1);
		mGoals1.setOnClickListener(this);
		mGoals2 = (TextView) mGoal_layout.findViewById(R.id.goals2);
		mGoals2.setOnClickListener(this);
		mGoals3 = (TextView) mGoal_layout.findViewById(R.id.goals3);
		mGoals3.setOnClickListener(this);
		mSteps = (EditText) mGoal_layout.findViewById(R.id.steps);
		mCal = (EditText) mGoal_layout.findViewById(R.id.cal);
		mSave = (Button) mGoal_layout.findViewById(R.id.save);
		mSave.setVisibility(View.GONE);
		mBtn_layout = (RelativeLayout) mGoal_layout.findViewById(R.id.btn_layout);
		mBtn_layout.setVisibility(View.VISIBLE);
		mBtn_pre = (Button) mGoal_layout.findViewById(R.id.btn_pre);
		mBtn_done = (Button) mGoal_layout.findViewById(R.id.btn_done);
		mBtn_pre.setOnClickListener(this);
		mBtn_done.setOnClickListener(this);
		
		mTestCenter_layout = (LinearLayout)findViewById(R.id.test_center_layout);
		test_personal = (ImageView) mTestCenter_layout.findViewById(R.id.test_personal);
		bmi_value = (TextView) mTestCenter_layout.findViewById(R.id.bmi_value);
		bmi_state = (TextView) mTestCenter_layout.findViewById(R.id.bmi_state);
		state1 = (ImageView) mTestCenter_layout.findViewById(R.id.state1);
		state2 = (ImageView) mTestCenter_layout.findViewById(R.id.state2);
		state3 = (ImageView) mTestCenter_layout.findViewById(R.id.state3);
		state4 = (ImageView) mTestCenter_layout.findViewById(R.id.state4);
		state5 = (ImageView) mTestCenter_layout.findViewById(R.id.state5);
		state6 = (ImageView) mTestCenter_layout.findViewById(R.id.state6);
		mBack = (Button) mTestCenter_layout.findViewById(R.id.tBack);
		mDone = (Button) mTestCenter_layout.findViewById(R.id.tDone);
		mBack.setOnClickListener(this);
		mDone.setOnClickListener(this);

		mConfig_layout = (RelativeLayout) findViewById(R.id.config_layout);
		
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
		mCurrStep = 0;
		tv_title.setText(R.string.guide_step_1);

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
		
		mTestCenter_layout.setVisibility(View.GONE);
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
				goto4();
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
		case R.id.goals1:
			selectGoals(1);
			break;
		case R.id.goals2:
			selectGoals(2);
			break;
		case R.id.goals3:
			selectGoals(3);
			break;
		case R.id.btn_pre:
			if (mCurrStep == 4) {
				back3();
			}
			break;
		case R.id.btn_done:
			if (mCurrStep == 4) {
				doDone();
			}
			break;
		case R.id.tBack:
			if (mCurrStep == 4) {
				back3();
			}
			break;
		case R.id.tDone:
			if (mCurrStep == 4) {
				tDone();
			}
			break;
		default:
			break;
		}
	}
	
	void tDone(){
		Tools.updatePersonalConfig(mPersonalConfig);
		CloudSync.startSyncInfo();
		finish();
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
			mBtnContainer.getLocationOnScreen(location);
			mBtnY = location[1];
			Log.i("gchk", mTitleY + " | " + mWomanHeadY + " | " + mManHeadY + " | " + mBtnY);
		}
	}

	private void goto1() {
		mCurrStep = 1;
		tv_title.setText(R.string.guide_step_2);

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
		tv_title.setText(R.string.guide_step_3);

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
		tv_title.setText(R.string.guide_step_4);

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
	}

	private void goto4() {
		mCurrStep = 4;
		mConfig_layout.setVisibility(View.GONE);
		if(test_center){
			testCenterShow();
		}else{
			tv_title.setText(R.string.guide_step_5);
			mGoal_layout.setVisibility(View.VISIBLE);
		}
	}
	
	private void testCenterShow() {
		tv_title.setText(R.string.guide_test_center_5);		
		mTestCenter_layout.setVisibility(View.VISIBLE);
		if (mPersonalConfig.getSex() == PersonalConfig.SEX_WOMAN)
			test_personal.setImageResource(R.drawable.test_center_1);
		else if (mPersonalConfig.getSex() == PersonalConfig.SEX_MAN)
			test_personal.setImageResource(R.drawable.test_center_2);
		double d = Tools.getBMI(mPersonalConfig);
		Object aobj[] = new Object[1];
		aobj[0] = Double.valueOf(d);
		bmi_value.setText(String.format("%.1f", aobj));
		state1.setImageResource(R.drawable.test1);
		state2.setImageResource(R.drawable.test2);
		state3.setImageResource(R.drawable.test3);
		state4.setImageResource(R.drawable.test4);
		state5.setImageResource(R.drawable.test5);
		state6.setImageResource(R.drawable.test6);
		if(d <= 17D) {
			bmi_state.setText("太瘦");
			state1.setImageResource(R.drawable.test1_b);
		} else if(d > 17D && d <= 19D) {
			bmi_state.setText("偏瘦");
			state2.setImageResource(R.drawable.test2_b);
		} else if(d > 19D && d <= 23D) {
			bmi_state.setText("普通");
			state3.setImageResource(R.drawable.test3_b);
		} else if(d > 23D && d <= 25D) {
			bmi_state.setText("稍胖");
			state4.setImageResource(R.drawable.test4_b);
		} else if(d > 25D && d <= 28D) {
			bmi_state.setText("偏胖");
			state5.setImageResource(R.drawable.test5_b);
		} else if(d > 28D) {
			bmi_state.setText("太胖啦");
			state6.setImageResource(R.drawable.test6_b);
		}
	}
	
	private void back0() {
		mCurrStep = 0;
		tv_title.setText(R.string.guide_step_1);

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
		tv_title.setText(R.string.guide_step_2);

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
		tv_title.setText(R.string.guide_step_3);

		if (mPersonalConfig.getSex() == PersonalConfig.SEX_MAN) {
			TranslateDownLeftAni(mUserInfoMan, mTitleY - mManHeadY);
		} else if (mPersonalConfig.getSex() == PersonalConfig.SEX_WOMAN) {
			TranslateDownLeftAni(mUserInfoWoman, mTitleY - mWomanHeadY);
		}

		CommonFadeIn(mHeightContainer);

		CommonFadeOut(mYearContainer);
	}

	private void back3() {
		mCurrStep = 3;
		tv_title.setText(R.string.guide_step_4);
		mConfig_layout.setVisibility(View.VISIBLE);
		if(test_center){
			mTestCenter_layout.setVisibility(View.GONE);
		}else{
			mGoal_layout.setVisibility(View.GONE);
		}
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

	private void selectGoals(int index) {
		switch (index) {
		case 1:
			mPersonal.setImageResource(R.drawable.general);
			mGoals1.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.goals_selected, 0, 0);
			mGoals2.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.goals_normal, 0, 0);
			mGoals3.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.goals_normal, 0, 0);
			mGoals1.setTextColor(0xFF808080);
			mGoals2.setTextColor(0xFFB9B9B9);
			mGoals3.setTextColor(0xFFB9B9B9);
			mSteps.setText("7000");
			mCal.setText("200");
			break;
		case 2:
			mPersonal.setImageResource(R.drawable.activists);
			mGoals1.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.goals_normal, 0, 0);
			mGoals2.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.goals_selected, 0, 0);
			mGoals3.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.goals_normal, 0, 0);
			mGoals1.setTextColor(0xFFB9B9B9);
			mGoals2.setTextColor(0xFF808080);
			mGoals3.setTextColor(0xFFB9B9B9);
			mSteps.setText("10000");
			mCal.setText("300");
			break;
		case 3:
			mPersonal.setImageResource(R.drawable.madman);
			mGoals1.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.goals_normal, 0, 0);
			mGoals2.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.goals_normal, 0, 0);
			mGoals3.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.goals_selected, 0, 0);
			mGoals1.setTextColor(0xFFB9B9B9);
			mGoals2.setTextColor(0xFFB9B9B9);
			mGoals3.setTextColor(0xFF808080);
			mSteps.setText("15000");
			mCal.setText("400");
			break;
		default:
			break;
		}
	}

	
	private void doDone() {
		Tools.updatePersonalConfig(mPersonalConfig);

		String temp_step = "";
		String temp_cal = "";
		temp_step = mSteps.getText().toString();
		temp_cal = mCal.getText().toString();
		if (!temp_step.equals("") && !temp_cal.equals("")) {
			int step = Integer.parseInt(temp_step);
			int cal = Integer.parseInt(temp_cal);
			if (step >= 5000 && cal >= 200) {
				mPersonalGoal.mGoalSteps = step;
				mPersonalGoal.mGoalCalories = cal;
				Tools.updatePersonalGoal(mPersonalGoal);
				
				if (Tools.checkIsFirstEntry(mCtx)) {
					Tools.setFirstEntry(mCtx);
				}

				Intent intent = new Intent(Guide.this, Main.class);
				startActivity(intent);
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				finish();
			} else if (step < 5000 && cal >= 200) {
				showAlertDilog(mCtx.getResources().getString(R.string.least_step));
			} else if (step >= 5000 && cal < 200) {
				showAlertDilog(mCtx.getResources().getString(R.string.least_cal));
			} else {
				showAlertDilog(mCtx.getResources().getString(R.string.least_step_cal));
			}
		}
	}
	
	private void showAlertDilog(String string) {
		Builder builder = new AlertDialog.Builder(mCtx);
		builder.setMessage(string);
		builder.setPositiveButton(R.string.know, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();				
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

}
