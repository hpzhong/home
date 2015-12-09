package com.zhuoyou.plugin.view;

import java.util.Timer;
import java.util.TimerTask;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.running.CalTools;
import com.zhuoyou.plugin.running.HomePageFragment;
import com.zhuoyou.plugin.running.PersonalGoal;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningItem;
import com.zhuoyou.plugin.running.Tools;
import com.zhuoyou.plugin.weather.WeatherTools;

public class StatsCircleView extends FrameLayout {
	private ClickAnimationView clickAnimationView_;
	private CircleMaskNew circleMask_;
	private RunningItem mDateBean;
	private ImageView mCircleFull, mPmBg, mPmCloud;

	private RelativeLayout mPmContainer;
	private RelativeLayout mCircleAni;

	private TextView mCurrUnit, mCurrBig, mGoalUnit, mCurrTip;

	private RelativeLayout mStepContainer, mCalContainer, mPM25Container;

	private RelativeLayout mCenterContainer;

	private ImageView mStepLogo, mCalLogo, mPm25Logo;

	private TextView mStepUnit, mCalUnit, mPm25Unit;

	private TextView mStepValue, mCalValue, mPm25Value;

	private ImageButton mLeftBtn, mRightBtn, mShare;

	private PersonalGoal mPersonalGoal;
	private int currentRadian_ = 0;
	private int finalRadian_ = 0;

	private int goalCalories_ = 678;
	private int goalSteps_ = 7000;

	private Timer fontTimer_;

	private MyHandler myHandler_;

	private Typeface mNewtype;
	
	private View.OnClickListener leftClickListener = new View.OnClickListener() {
		public void onClick(View paramView) {
			if (HomePageFragment.mInstance != null) {
				HomePageFragment.mInstance.onTapLeft();
			}
		}
	};

	private View.OnClickListener rightClickListener = new View.OnClickListener() {
		public void onClick(View paramView) {
			if (HomePageFragment.mInstance != null) {
				HomePageFragment.mInstance.onTapRight();
			}
		}
	};

	private View.OnClickListener shareClickListener = new View.OnClickListener() {
		public void onClick(View paramView) {
			if (HomePageFragment.mInstance != null) {
				HomePageFragment.mInstance.toShareActivity();
			}
		}
	};
	
	private View.OnClickListener switchClickListener = new View.OnClickListener() {
		public void onClick(View paramView) {
			if (mDateBean.getPm25() > 0) {
				if (StatsCircleView.SwitchType.currentSwitchType == 2) {
					if (paramView.getId() != R.id.pageview_pm2_5_container) {
						mPmContainer.startAnimation(animate(R.anim.fade_out));
					}
				} else {
					if (paramView.getId() == R.id.pageview_pm2_5_container) {
						mPmContainer.startAnimation(animate(R.anim.fade_in));
					} else if (paramView.getId() == R.id.pageview_circle_rl && StatsCircleView.SwitchType.currentSwitchType == 1) {
						mPmContainer.startAnimation(animate(R.anim.fade_in));
					}
				}
			}

			switch (paramView.getId()) {
			default:
				break;
			case R.id.pageview_setps_container:
				StatsCircleView.SwitchType.currentSwitchType = StatsCircleView.SwitchType.steps;
				break;
			case R.id.pageview_calories_container:
				StatsCircleView.SwitchType.currentSwitchType = StatsCircleView.SwitchType.calories;
				break;
			case R.id.pageview_pm2_5_container:
				// 必须要今天的才处理
				if (mDateBean.getDate().equals(Tools.getDate(0))) {
					if (Tools.getPm25(Tools.getDate(0)) == 0) {
						WeatherTools.newInstance().getCurrAqi();
					}
				}
				StatsCircleView.SwitchType.currentSwitchType = StatsCircleView.SwitchType.pm2_5;
				break;
			case R.id.pageview_circle_rl:
				StatsCircleView.SwitchType.currentSwitchType = (1 + StatsCircleView.SwitchType.currentSwitchType) % StatsCircleView.SwitchType.typeCount;
				break;
			}

			StatsCircleView.SwitchType.isChanged = true;

			SharedPreferences.Editor localSharedPreferences = getContext().getSharedPreferences("app_config", Context.MODE_PRIVATE).edit();
			localSharedPreferences.putInt("CURRENT_SWITCH_TYPE", StatsCircleView.SwitchType.currentSwitchType);
			localSharedPreferences.commit();

			StatsCircleView.this.clickAnimationView_.endAnimation();
			StatsCircleView.this.clickAnimationView_.startAnimation();
			StatsCircleView.this.updateCircle(false);
			StatsCircleView.this.updateCircleMask(StatsCircleView.this.currentRadian_, StatsCircleView.this.finalRadian_, true);

			if (StatsCircleView.this.fontTimer_ != null) {
				StatsCircleView.this.fontTimer_.cancel();
			}

			StatsCircleView.this.fontTimer_ = new Timer();
			StatsCircleView.this.fontTimer_.schedule(new TimerTask() {
				@Override
				public void run() {
					myHandler_.sendEmptyMessage(1024);
				}
			}, 50L);

			int color_i = getContext().getResources().getColor(R.color.grey_bc);
			mCurrBig.setTextColor(color_i);
			mCurrUnit.setTextColor(color_i);
		}
	};

	private StatsCircleView(Context paramContext) {
		super(paramContext);
	}

	public StatsCircleView(Context paramContext, RunningItem bean) {
		super(paramContext);
		mDateBean = bean;
		initView();
		updateData();
	}

	private void initView() {
		this.myHandler_ = new MyHandler();
		mNewtype = Typeface.createFromAsset(getContext().getAssets(),"font/akzidenzgrotesklightcond.ttf");
		
		View mRoot = LayoutInflater.from(getContext()).inflate(R.layout.viewpage_item, null);
		addView(mRoot);

		mCircleFull = (ImageView) mRoot.findViewById(R.id.circle_image_full);
		mCircleAni = (RelativeLayout) mRoot.findViewById(R.id.pageview_circle_animation_rl);
		circleMask_ = new CircleMaskNew(getContext());
		this.clickAnimationView_ = new ClickAnimationView(getContext());
		mCircleAni.addView(circleMask_);
		mCircleAni.addView(clickAnimationView_);

		mPmContainer = (RelativeLayout) mRoot.findViewById(R.id.relativelayout_pm);
		mPmBg = (ImageView) mRoot.findViewById(R.id.imageview_pm);
		mPmCloud = (ImageView) mRoot.findViewById(R.id.imageview_pm_cloud);

		mCenterContainer = (RelativeLayout) mRoot.findViewById(R.id.pageview_circle_rl);
		mCenterContainer.setOnClickListener(switchClickListener);
		mCurrUnit = (TextView) mRoot.findViewById(R.id.pageview_circle_unit_tv);
		mCurrBig = (TextView) mRoot.findViewById(R.id.pageview_circle_big_tv);
		mCurrBig.setTypeface(mNewtype);
		mGoalUnit = (TextView) mRoot.findViewById(R.id.pageview_circle_goalunit_tv);

		mCurrTip = (TextView) mRoot.findViewById(R.id.data_description);

		mStepContainer = (RelativeLayout) mRoot.findViewById(R.id.pageview_setps_container);
		mStepContainer.setOnClickListener(switchClickListener);
		mStepLogo = (ImageView) mRoot.findViewById(R.id.setps_icon);
		mStepUnit = (TextView) mRoot.findViewById(R.id.setps_icon_desc);
		mStepValue = (TextView) mRoot.findViewById(R.id.pageview_steps_value);
		mStepValue.setTypeface(mNewtype);
		mStepValue.setText(mDateBean.getSteps() + "");

		mCalContainer = (RelativeLayout) mRoot.findViewById(R.id.pageview_calories_container);
		mCalContainer.setOnClickListener(switchClickListener);
		mCalLogo = (ImageView) mRoot.findViewById(R.id.calories_icon);
		mCalUnit = (TextView) mRoot.findViewById(R.id.calories_icon_desc);
		mCalValue = (TextView) mRoot.findViewById(R.id.pageview_calories_value);
		mCalValue.setTypeface(mNewtype);
		mCalValue.setText(mDateBean.getCalories() + "");

		mPM25Container = (RelativeLayout) mRoot.findViewById(R.id.pageview_pm2_5_container);
		mPM25Container.setOnClickListener(switchClickListener);
		mPm25Logo = (ImageView) mRoot.findViewById(R.id.activity_icon);
		mPm25Unit = (TextView) mRoot.findViewById(R.id.pageview_pm2_5_icon_desc);
		mPm25Value = (TextView) mRoot.findViewById(R.id.pageview_pm2_5_value);
		mPm25Value.setTypeface(mNewtype);
		String pm25 = (mDateBean.getPm25() == 0) ? "?" : mDateBean.getPm25() + "";
		mPm25Value.setText(pm25);

		mLeftBtn = (ImageButton) mRoot.findViewById(R.id.pageview_left_btn);
		mRightBtn = (ImageButton) mRoot.findViewById(R.id.pageview_right_btn);
		mShare  = (ImageButton) mRoot.findViewById(R.id.share_btn);
		mLeftBtn.setOnClickListener(leftClickListener);
		mRightBtn.setOnClickListener(rightClickListener);
		mShare.setOnClickListener(shareClickListener);
		
	}

	public void updateData() {
		SharedPreferences localSharedPreferences = getContext().getSharedPreferences("app_config", Context.MODE_PRIVATE);
		SwitchType.currentSwitchType = localSharedPreferences.getInt("CURRENT_SWITCH_TYPE", SwitchType.currentSwitchType);
		mPersonalGoal = Tools.getPersonalGoal();

		this.goalSteps_ = mPersonalGoal.mGoalSteps;
		this.goalCalories_ = mPersonalGoal.mGoalCalories;
		updateCircle(false);
		updateCircleMask(this.currentRadian_, this.finalRadian_, false);
	}

	private void updateCircle(boolean paramBoolean) {
		if (mDateBean == null) {
			Log.i("gchk", "aquery_ is null or dailyStats is null");
		} else {
			double d1 = 0.0D;
			double d2 = 100.0D;
			// String localDate = mDateBean.getDate();
			int color_i = getContext().getResources().getColor(R.color.left_fragment);
			int color_j = getContext().getResources().getColor(R.color.grey_bc);
			mPmContainer.setVisibility(View.INVISIBLE);
			mCalLogo.setImageResource(R.drawable.day_icon_calories);
			mCalUnit.setTextColor(color_j);
			mCalValue.setTextColor(color_j);
			mStepLogo.setImageResource(R.drawable.day_icon_walking);
			mStepUnit.setTextColor(color_j);
			mStepValue.setTextColor(color_j);
			mPm25Logo.setImageResource(R.drawable.day_icon_active);
			mPm25Unit.setTextColor(color_j);
			mPm25Value.setTextColor(color_j);

			mStepValue.setText(mDateBean.getSteps() + "");
			mCalValue.setText(mDateBean.getCalories() + "");
			mPm25Value.setText(mDateBean.getPm25() + "");
			if (mDateBean.getPm25() <= 0.0D) {
				mPm25Value.setText("?");
			}

			switch (SwitchType.currentSwitchType) {
			case 0:
				mCurrUnit.setText(R.string.steps);
				mCurrTip.setText(getResources().getString(R.string.walk) + " " + String.valueOf(mDateBean.getKilometer()) + " " + getResources().getString(R.string.kilometre));
				mStepLogo.setImageResource(R.drawable.day_icon_walking_light);
				mStepUnit.setTextColor(color_i);
				mStepValue.setTextColor(color_i);
				d2 = this.goalSteps_;
				d1 = this.mDateBean.getSteps();
				mCurrBig.setText((int) d1 + "");
				mGoalUnit.setText(getResources().getString(R.string.goals) + (int) d2);
				break;
			case 1:
				mCurrUnit.setText(R.string.calories);
				mCurrTip.setText(CalTools.getResultFromCal(getContext(), mDateBean.getCalories()));
				mCalLogo.setImageResource(R.drawable.day_icon_calories_light);
				mCalUnit.setTextColor(color_i);
				mCalValue.setTextColor(color_i);
				d2 = this.goalCalories_;
				d1 = this.mDateBean.getCalories();
				mCurrBig.setText((int) d1 + "");
				mGoalUnit.setText(getResources().getString(R.string.goals) + (int) d2);
				break;
			case 2:
				String str1 = "";
				String str2 = "";
				int img1 = R.drawable.circle_pm_cloud_1;
				int img2 = R.drawable.circle_pm_1;
				mCurrUnit.setText(R.string.pm2_5);
				mPm25Logo.setImageResource(R.drawable.day_icon_active_light);
				mPm25Unit.setTextColor(color_i);
				mPm25Value.setTextColor(color_i);
				mPmContainer.setVisibility(View.VISIBLE);
				if (mDateBean.getPm25() > 300.0D) {
					str1 = getResources().getString(R.string.serious_pollution);
					str2 = getResources().getString(R.string.serious_pollution_in);
					img1 = R.drawable.circle_pm_cloud_3;
					img2 = R.drawable.circle_pm_3;
				}

				if (mDateBean.getPm25() <= 300.0D) {
					str1 = getResources().getString(R.string.middle_pollution);
					str2 = getResources().getString(R.string.middle_pollution_in);
					img1 = R.drawable.circle_pm_cloud_2;
					img2 = R.drawable.circle_pm_2;
				}

				if (mDateBean.getPm25() <= 200.0D) {
					str1 = getResources().getString(R.string.slightly_pollution);
					str2 = getResources().getString(R.string.slightly_pollution_in);
					img1 = R.drawable.circle_pm_cloud_2;
					img2 = R.drawable.circle_pm_2;
				}

				if (mDateBean.getPm25() <= 100.0D) {
					str1 = getResources().getString(R.string.fine);
					str2 = getResources().getString(R.string.fine_in);
					img1 = R.drawable.circle_pm_cloud_1;
					img2 = R.drawable.circle_pm_1;
				}
				if (mDateBean.getPm25() <= 50.0D) {
					str1 = getResources().getString(R.string.excellent);
					str2 = getResources().getString(R.string.excellent_in);
					img1 = R.drawable.circle_pm_cloud_1;
					img2 = R.drawable.circle_pm_1;
				}

				if (mDateBean.getPm25() <= 0.0D) {
					str1 = "";
					str2 = getResources().getString(R.string.no_pm2_5);
					mCurrBig.setText("?");
					mPmContainer.setVisibility(View.INVISIBLE);
					img1 = R.drawable.circle_pm_cloud_1;
					img2 = R.drawable.circle_pm_1;
				}

				mPmCloud.setImageResource(img1);
				mPmBg.setImageResource(img2);
				mCurrTip.setText(str2);
				mGoalUnit.setText(str1);

				mCurrBig.setText(mDateBean.getPm25() + "");
				break;
			default:
				break;
			}

			this.currentRadian_ = this.finalRadian_;
			this.finalRadian_ = (int) (360L * Math.round(d1) / d2);
			if (this.finalRadian_ >= 360) {
				mCircleFull.startAnimation(animate(R.anim.fade_in));
				mCircleFull.setVisibility(View.VISIBLE);
				this.finalRadian_ = 360;
			} else {
				mCircleFull.startAnimation(animate(R.anim.fade_out));
				mCircleFull.setVisibility(View.GONE);
			}
		}
	}

	public void updateCircleMask(int paramInt1, int paramInt2, boolean paramBoolean) {
		if (this.circleMask_ == null) {
			return;
		}
		this.circleMask_.updateCircleMask(paramInt1, paramInt2, paramBoolean);
	}

	public void updateStatsCircle(RunningItem paramDailyStats) {
		this.mDateBean = paramDailyStats;
		updateData();
	}

	public class ClickAnimationView extends View implements ValueAnimator.AnimatorUpdateListener {
		private ValueAnimator bounceAnim_ = null;
		int cricleWdithPx_ = 0;
		private Paint paint_;
		private int radius = 0;
		private RadiusShapeHolder radiusShapeHolder;

		public ClickAnimationView(Context arg2) {
			super(arg2);
			this.radius = Tools.dip2px(getContext(), 50.0F);
			this.cricleWdithPx_ = Tools.dip2px(getContext(), CircleMaskNew.CIRCLE_MASK_WIDTH);
			this.radiusShapeHolder = new RadiusShapeHolder(2 * this.radius);
			this.paint_ = new Paint();
			this.paint_.setStyle(Paint.Style.STROKE);
			this.paint_.setColor(getContext().getResources().getColor(R.color.grep_ripple));
		}

		private void createAnimation() {
			if (this.bounceAnim_ != null)
				return;
			RadiusShapeHolder localRadiusShapeHolder = this.radiusShapeHolder;
			int[] arrayOfInt = new int[2];
			arrayOfInt[0] = this.radius;
			arrayOfInt[1] = (2 * this.radius);
			this.bounceAnim_ = ObjectAnimator.ofInt(localRadiusShapeHolder, "radius", arrayOfInt);
			this.bounceAnim_.setDuration(700L);
			this.bounceAnim_.setInterpolator(new AccelerateDecelerateInterpolator());
			this.bounceAnim_.addUpdateListener(this);
		}

		public void cancelAnimation() {
			createAnimation();
			this.bounceAnim_.cancel();
		}

		public void endAnimation() {
			createAnimation();
			this.bounceAnim_.end();
		}

		public void onAnimationUpdate(ValueAnimator paramValueAnimator) {
			invalidate();
		}

		protected void onDraw(Canvas paramCanvas) {
			paramCanvas.save();
			int i = this.radiusShapeHolder.getRadius();
			int j = 2 * this.radius - 2 * (i - this.radius);
			this.paint_.setStrokeWidth(j);
			paramCanvas.drawCircle(this.cricleWdithPx_ / 2, this.cricleWdithPx_ / 2, i, this.paint_);
			paramCanvas.restore();
		}

		public void startAnimation() {
			createAnimation();
			this.bounceAnim_.start();
		}

		class RadiusShapeHolder {
			private int radius = 0;

			public RadiusShapeHolder(int arg2) {
				this.radius = arg2;
			}

			public int getRadius() {
				return this.radius;
			}

			public void setRadius(int paramInt) {
				this.radius = paramInt;
			}
		}
	}

	public static class SwitchType {
		public static int calories;
		public static int currentSwitchType;
		public static boolean isChanged;
		public static boolean isCircleAnimationOn;
		public static int pm2_5;
		public static int steps;
		public static int typeCount = 3;

		static {
			currentSwitchType = 0;
			pm2_5 = 2;
			calories = 1;
			steps = 0;
			isChanged = false;
			isCircleAnimationOn = false;
		}
	}

	@SuppressLint({ "HandlerLeak" })
	class MyHandler extends Handler {
		public MyHandler() {
		}

		public void handleMessage(Message paramMessage) {
			Log.d("gchk", "MyHandler: handleMessage......");
			super.handleMessage(paramMessage);
			switch (paramMessage.what) {
			case 1024:
				int i = getContext().getResources().getColor(R.color.grey_bc);
				int j = getContext().getResources().getColor(R.color.left_fragment);
				TextView localTextView1 = mCurrBig;
				TextView localTextView2 = mCurrUnit;
				int[] arrayOfInt1 = new int[2];
				arrayOfInt1[0] = i;
				arrayOfInt1[1] = j;
				ObjectAnimator localObjectAnimator1 = ObjectAnimator.ofInt(localTextView1, "textColor", arrayOfInt1);
				ObjectAnimator localObjectAnimator2 = ObjectAnimator.ofInt(localTextView2, "textColor", arrayOfInt1);
				localObjectAnimator1.setEvaluator(new ArgbEvaluator());
				localObjectAnimator2.setEvaluator(new ArgbEvaluator());
				AnimatorSet localAnimatorSet = new AnimatorSet();
				Animator[] arrayOfAnimator = new Animator[2];
				arrayOfAnimator[0] = localObjectAnimator1;
				arrayOfAnimator[1] = localObjectAnimator2;
				localAnimatorSet.playTogether(arrayOfAnimator);
				localAnimatorSet.setDuration(200L);
				localAnimatorSet.start();
				break;
			}
		}
	}

	private Animation animate(int paraInt) {
		return animate(paraInt, null);
	}

	private Animation animate(int paramInt, Animation.AnimationListener paramAnimationListener) {
		Animation localAnimation = AnimationUtils.loadAnimation(getContext(), paramInt);
		localAnimation.setAnimationListener(paramAnimationListener);
		return localAnimation;
	}
}
