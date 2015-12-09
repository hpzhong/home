package com.zhuoyou.plugin.running;

import java.lang.ref.WeakReference;
import java.util.Calendar;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.info.PersonalInformation;
import com.zhuoyou.plugin.view.NewRelativeLayout;
import com.zhuoyou.plugin.view.NewScrollView;
import com.zhuoyou.plugin.view.NewTextView;
import com.zhuoyou.plugin.view.NewTextView.Operator;
import com.zhuoyou.plugin.view.ViewWrapper;

public class MotionDataActivity extends Activity implements View.OnClickListener , NewRelativeLayout.Operator, Operator {

	private Context mCtx = null;
	private int mWindowHeight;
    private boolean isFirstProgressStep = true;
    private boolean isFirstProgressCal = true;
    private boolean canWeightDisplay = false;
    private boolean canSendScroll = false;
    public int clickTimes = 0;
    private NewScrollView mNewScrollView;
    private LinearLayout userInfoLayout;
    private RelativeLayout bestStepLayout;
    private RelativeLayout bestCalorieLayout;
    private RelativeLayout avgStepLayout;
    private RelativeLayout avgCalLayout;
    private RelativeLayout goalLayout;
    private RelativeLayout BMILayout;
    private RelativeLayout BMRLayout;
    private RelativeLayout BMIIntroLayout;
    private RelativeLayout BMRIntroLayout;
    private RelativeLayout weightTrendLayout;
    private TextView describe_total_step;
    private TextView describe_total_calorie;
    private TextView goalDays;
    private TextView goalPercent;
    private TextView BMIdescribe;
    private NewTextView goalStepText;
    private NewTextView bestStepText;
    private NewTextView bestCalText;
    private NewTextView avgStepText;
    private NewTextView avgCalText;
    private NewTextView totalStepText;
    private NewTextView totalCalText;
    private NewTextView BMIText;
    private NewTextView BMRText;
    private NewTextView weightText;
    private NewRelativeLayout progressStep;
    private NewRelativeLayout progressCal;
	private ImageView progress_step;
	private ImageView progress_cal;
	private Typeface mNumberTP;
	private PersonalConfig mPersonalConfig;
	private PersonalGoal mPersonalGoal;
	private MotionDataCenter mMotionDataCenter;
	private int targetStepWidth;
	private int targetCalWidth;
	private Bitmap bmp = null;
	private WRHandler mHandler = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.motion_data_layout);
		mCtx = this;
		initData();
		initView();
	}
	
	public void initData() {
		mPersonalConfig = Tools.getPersonalConfig();
		mPersonalGoal = Tools.getPersonalGoal();
		mMotionDataCenter = new MotionDataCenter(mCtx);
		mNumberTP = RunningApp.getCustomNumberFont();
	}
	
	public void initView() {
		TextView tv_title = (TextView) findViewById(R.id.title);
		tv_title.setText(R.string.data_center);
		RelativeLayout im_back = (RelativeLayout) findViewById(R.id.back);
		im_back.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		Rect rect = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
		mWindowHeight = rect.height();
		
        mNewScrollView = (NewScrollView) findViewById(R.id.new_scrollview);
		userInfoLayout = (LinearLayout) findViewById(R.id.dc_linearlayout_user_info);
        bestStepLayout = (RelativeLayout) findViewById(R.id.dc_relativelayout_record_step);
        bestCalorieLayout = (RelativeLayout) findViewById(R.id.dc_relativelayout_record_calorie);
        avgStepLayout = (RelativeLayout) findViewById(R.id.dc_relativelayout_avg_step);
        avgCalLayout = (RelativeLayout) findViewById(R.id.dc_relativelayout_avg_cal);
        goalLayout = (RelativeLayout) findViewById(R.id.dc_relativelayout_goal);
        BMILayout = (RelativeLayout) findViewById(R.id.dc_relativelayout_bmi);
        BMRLayout = (RelativeLayout) findViewById(R.id.dc_relativelayout_bmr);
        BMIIntroLayout = (RelativeLayout) findViewById(R.id.dc_relativelayout_intro_bmi);
        BMRIntroLayout = (RelativeLayout) findViewById(R.id.dc_relativelayout_intro_bmr);
        goalStepText = (NewTextView) findViewById(R.id.text_goal_step);
        bestStepText = (NewTextView) findViewById(R.id.record_text_step);
        bestCalText = (NewTextView) findViewById(R.id.record_text_calorie);
        avgStepText = (NewTextView) findViewById(R.id.text_avg_step);
        avgCalText = (NewTextView) findViewById(R.id.text_avg_cal);
        describe_total_step = (TextView)  findViewById(R.id.describe_total_step);
        describe_total_calorie = (TextView)  findViewById(R.id.describe_total_calorie);
        goalDays = (TextView) findViewById(R.id.text_goal_day);
        goalPercent = (TextView) findViewById(R.id.text_goal_percent);
        totalStepText = (NewTextView) findViewById(R.id.text_total_step);
        totalCalText = (NewTextView) findViewById(R.id.text_total_calorie);
        BMIText = (NewTextView) findViewById(R.id.text_bmi);
        BMRText = (NewTextView) findViewById(R.id.text_bmr);
        BMIdescribe = (TextView) findViewById(R.id.describe_bmi);
        weightTrendLayout = (RelativeLayout) findViewById(R.id.dc_relativelayout_weight_trend);
        weightText = (NewTextView) findViewById(R.id.dc_text_weight_trend);
        progressStep = (NewRelativeLayout) findViewById(R.id.relativelayout_progress_step);
        progressCal = (NewRelativeLayout) findViewById(R.id.relativelayout_progress_cal);
		progress_step = (ImageView) findViewById(R.id.progress_step);
		progress_cal = (ImageView) findViewById(R.id.progress_cal);
		
		progressStep.setOperator(this);
		progressCal.setOperator(this);
		goalStepText.setOperator(this);
		bestStepText.setOperator(this);
		bestCalText.setOperator(this);
		avgStepText.setOperator(this);
		avgCalText.setOperator(this);
		totalStepText.setOperator(this);
		totalCalText.setOperator(this);
		BMIText.setOperator(this);
		BMRText.setOperator(this);
		weightText.setOperator(this);
		
        setView();
        initListener();
        
        userInfoLayout.setOnClickListener(this);
        bestStepLayout.setOnClickListener(this);
        bestCalorieLayout.setOnClickListener(this);
        avgStepLayout.setOnClickListener(this);
        avgCalLayout.setOnClickListener(this);
        goalLayout.setOnClickListener(this);
        BMILayout.setOnClickListener(this);
        BMRLayout.setOnClickListener(this);
        BMIIntroLayout.setOnClickListener(this);
        BMRIntroLayout.setOnClickListener(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.i("gchk", "MotionDataActivity onResume");
		initData();
		setView();
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i("gchk", "MotionDataActivity onPause");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		Animation animation = AnimationUtils.loadAnimation(mCtx, R.anim.fade_out);
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.dc_linearlayout_user_info:
			intent.setClass(mCtx, PersonalInformation.class);
			intent.putExtra("from_center", true);
			startActivity(intent);
			break;
		case R.id.dc_relativelayout_record_step:
	        animation.setAnimationListener(new Animation.AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {
					bestStepLayout.setVisibility(View.GONE);
		            setViewAvgStep();
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub					
				}

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub					
				}
	        	
	        });
	        bestStepLayout.startAnimation(animation);
			break;
		case R.id.dc_relativelayout_record_calorie:
	        animation.setAnimationListener(new Animation.AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {
					bestCalorieLayout.setVisibility(View.GONE);
		            setViewAvgCal();
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					
				}
	        	
	        });
	        bestCalorieLayout.startAnimation(animation);
			break;
		case R.id.dc_relativelayout_avg_step:
	        animation.setAnimationListener(new Animation.AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {
					avgStepLayout.setVisibility(View.GONE);
		            setViewBestStep();
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub					
				}

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub					
				}
	        	
	        });
	        avgStepLayout.startAnimation(animation);
			break;
		case R.id.dc_relativelayout_avg_cal:
	        animation.setAnimationListener(new Animation.AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {
					avgCalLayout.setVisibility(View.GONE);
					setViewBestCalorie();
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub					
				}

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub					
				}
	        	
	        });
	        avgCalLayout.startAnimation(animation);
			break;
		case R.id.dc_relativelayout_goal:
			intent.setClass(mCtx, PersonalInformation.class);
			intent.putExtra("from_center", true);
			startActivity(intent);
			break;
		case R.id.dc_relativelayout_bmi:
	        animation.setAnimationListener(new Animation.AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {
					BMILayout.setVisibility(View.GONE);
					setViewBMIIntro();
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub					
				}

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub					
				}
	        	
	        });
	        BMILayout.startAnimation(animation);
			break;
		case R.id.dc_relativelayout_bmr:
	        animation.setAnimationListener(new Animation.AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {
					BMRLayout.setVisibility(View.GONE);
					setViewBMRIntro();
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub					
				}

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub					
				}
	        	
	        });
	        BMRLayout.startAnimation(animation);
			break;
		case R.id.dc_relativelayout_intro_bmi:
	        animation.setAnimationListener(new Animation.AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {
					BMIIntroLayout.setVisibility(View.GONE);
					setViewBMI();
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub					
				}

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub					
				}
	        	
	        });
	        BMIIntroLayout.startAnimation(animation);
			break;
		case R.id.dc_relativelayout_intro_bmr:
	        animation.setAnimationListener(new Animation.AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {
					BMRIntroLayout.setVisibility(View.GONE);
					setViewBMR();
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub					
				}

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub					
				}
	        	
	        });
	        BMRIntroLayout.startAnimation(animation);
			break;
		default:
			break;
		}
		
	}

	private void setView() {
        setViewUserInfo();
        setViewGoalSetting();
        setViewBestStep();
        setViewBestCalorie();
        setViewTotalKM();
        setViewTotalCal();
        setViewBMI();
        setViewBMR();
        if(canWeightDisplay) {
        	weightTrendLayout.setVisibility(View.VISIBLE);
            setViewWeightTrend();
        } else {
        	weightTrendLayout.setVisibility(View.GONE);
        }
	}
	
	private void setViewUserInfo() {
		ImageView face = (ImageView) findViewById(R.id.dc_image_avatar);
		int headIndex = Tools.getHead(this);
		if (headIndex == 10000) {
			bmp = Tools.convertFileToBitmap("/Running/download/custom");
			face.setImageBitmap(bmp);
		} else if (headIndex == 1000) {
			bmp = Tools.convertFileToBitmap("/Running/download/logo");
			face.setImageBitmap(bmp);
		} else {
			face.setImageResource(Tools.selectByIndex(headIndex));
		}

		TextView age = (TextView) findViewById(R.id.dc_text_age);
		age.setText(Calendar.getInstance().get(Calendar.YEAR) - mPersonalConfig.getYear() + getResources().getString(R.string.unit_age));

		TextView height = (TextView) findViewById(R.id.dc_text_height);
		height.setText(mPersonalConfig.getHeight() + getResources().getString(R.string.unit_length));

		TextView weight = (TextView) findViewById(R.id.dc_text_weight);
		weight.setText(mPersonalConfig.getWeight() + getResources().getString(R.string.unit_weight));
	}

	private void setViewGoalSetting() {
		int goalStep = mPersonalGoal.mGoalSteps;
		goalStepText.setTypeface(mNumberTP);
		if(goalStepText.refreshDisabled) {
			goalStepText.setValue(goalStep);
			goalStepText.setText((new StringBuilder()).append(goalStep).toString());
		} else {
			goalStepText.setValue(goalStep);
		}
		String goal = mMotionDataCenter.getGoal();
		goalDays.setText(goal.split(",")[0] + " " + getResources().getString(R.string.day));
		goalPercent.setText(goal.split(",")[1]);
	}
	
	private void setViewBestStep() {
        int i = mMotionDataCenter.getBestSteps();
        bestStepText.setTypeface(mNumberTP);
        if(bestStepText.refreshDisabled) {
            bestStepText.setValue(i);
            bestStepText.setText((new StringBuilder()).append(i).toString());
        } else {
            bestStepText.setText("0");
            bestStepText.setValue(i);
        }
        avgStepLayout.setVisibility(View.GONE);
        Animation animation = AnimationUtils.loadAnimation(mCtx, R.anim.fade_in);
        animation.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				bestStepLayout.setVisibility(View.VISIBLE);				
			}
        	
        });
        bestStepLayout.startAnimation(animation);
	}
	
	private void setViewAvgStep() {
        int i = mMotionDataCenter.getAvgSteps();
        avgStepText.setText((new StringBuilder()).append(i).toString());
        avgStepText.setTypeface(mNumberTP);
        bestStepLayout.setVisibility(View.GONE);
        Animation animation = AnimationUtils.loadAnimation(mCtx, R.anim.fade_in);
        animation.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				avgStepLayout.setVisibility(View.VISIBLE);
			}
        	
        });
        avgStepLayout.startAnimation(animation);
	}
	
	private void setViewBestCalorie() {
        int i = mMotionDataCenter.getBestCalories();
        bestCalText.setTypeface(mNumberTP);
        if(bestCalText.refreshDisabled) {
            bestCalText.setValue(i);
            bestCalText.setText((new StringBuilder()).append(i).toString());
        } else {
            bestCalText.setText("0");
            bestCalText.setValue(i);
        }
        avgCalLayout.setVisibility(View.GONE);
        Animation animation = AnimationUtils.loadAnimation(mCtx, R.anim.fade_in);
        animation.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				bestCalorieLayout.setVisibility(View.VISIBLE);				
			}
        	
        });
        bestCalorieLayout.startAnimation(animation);
	}
	
	private void setViewAvgCal() {
        int i = mMotionDataCenter.getAvgCalories();
        avgCalText.setText((new StringBuilder()).append(i).toString());
        avgCalText.setTypeface(mNumberTP);
        bestCalorieLayout.setVisibility(View.GONE);
        Animation animation = AnimationUtils.loadAnimation(mCtx, R.anim.fade_in);
        animation.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				avgCalLayout.setVisibility(View.VISIBLE);				
			}
        	
        });
        avgCalLayout.startAnimation(animation);
	}
	
	private void setViewTotalKM() {
		double d = mMotionDataCenter.getTotalKM();
		totalStepText.setTypeface(mNumberTP); 
		if (d < 0.0001D) {
			NewTextView localNewTextView2 = totalStepText;
			Object[] arrayOfObject2 = new Object[1];
			arrayOfObject2[0] = Double.valueOf(d);
			localNewTextView2.setText(String.format("%.0f", arrayOfObject2));
			describe_total_step.setText(R.string.describe_total_step_1);
		}

		if (totalStepText.refreshDisabled) {
			totalStepText.setValue(d);
			NewTextView localNewTextView1 = totalStepText;
			Object[] arrayOfObject1 = new Object[1];
			arrayOfObject1[0] = Double.valueOf(d);
			localNewTextView1.setText(String.format("%.1f", arrayOfObject1));
		} else {
			totalStepText.setText("0");
			totalStepText.setValue(d);
		}
		if(d > 0.0D && d < 9D) {
			describe_total_step.setText(R.string.describe_total_step_1);
			targetStepWidth = (int)((40D * d) / 9D);
		} else if ((d >= 9.0D) && (d < 36.0D)) {
			describe_total_step.setText(R.string.describe_total_step_2);
			targetStepWidth = (int) (40.0D + 64.0D * d / 36.0D);
		} else if ((d >= 36.0D) && (d < 67.0D)) {
			describe_total_step.setText(R.string.describe_total_step_3);
			targetStepWidth = (int) (104.0D + 44.0D * d / 67.0D);
		} else if ((d >= 67.0D) && (d < 130.0D)) {
			describe_total_step.setText(R.string.describe_total_step_4);
			targetStepWidth = (int) (148.0D + 32.0D * d / 130.0D);
		} else if ((d >= 130.0D) && (d < 356.0D)) {
			describe_total_step.setText(R.string.describe_total_step_5);
			targetStepWidth = (int) (180.0D + 36.0D * d / 356.0D);
		} else if ((d >= 356.0D) && (d < 780.0D)) {
			describe_total_step.setText(R.string.describe_total_step_6);
			targetStepWidth = (int) (216.0D + 45.0D * d / 780.0D);
		} else if ((d >= 780.0D) && (d < 1140.0D)) {
			describe_total_step.setText(R.string.describe_total_step_7);
			targetStepWidth = (int) (261.0D + 14.0D * d / 1140.0D);
		} else if ((d >= 1140.0D) && (d < 1956.0D)) {
			describe_total_step.setText(R.string.describe_total_step_8);
			targetStepWidth = (int) (275.0D + 45.0D * d / 1956.0D);
		} else if (d >= 1956.0D) {
			describe_total_step.setText(R.string.describe_total_step_9);
			targetStepWidth = 320;
		} else {
			describe_total_step.setText(R.string.describe_total_step_1);
			targetStepWidth = 0;
		}
	}
	
	private void setViewTotalCal() {
		int i = mMotionDataCenter.getTotalCalories();
		totalCalText.setTypeface(mNumberTP);
		if(totalCalText.refreshDisabled) {
			totalCalText.setValue(i);
			totalCalText.setText((new StringBuilder()).append(i).toString());
		} else {
			totalCalText.setText("0");
			totalCalText.setValue(i);
		}
		
		if(i > 0 && (double)i <= 500D) {
			double d = (double)i / 127D;
			Object aobj[] = new Object[1];
			aobj[0] = Double.valueOf(d);
			describe_total_calorie.setText(getResources().getString(R.string.equal_to) + " " + String.format("%.1f", aobj) + " " + getResources().getString(R.string.ice_cream));
			targetCalWidth = (int)((double)(i * 24) / 500D);
		} else if ((double)i > 500D && (double)i <= 1500D) {
			double d = (double)i / 320D;
			Object aobj[] = new Object[1];
			aobj[0] = Double.valueOf(d);
			describe_total_calorie.setText(getResources().getString(R.string.equal_to) + " " + String.format("%.1f", aobj) + " " + getResources().getString(R.string.beer));
			targetCalWidth = (int)(24D + (double)(i * 40) / 1500D);
		} else if ((double)i > 1500D && (double)i <= 5000D) {
			double d = (double)i / 1400D;
			Object aobj[] = new Object[1];
			aobj[0] = Double.valueOf(d);
			describe_total_calorie.setText(getResources().getString(R.string.equal_to) + " " + String.format("%.1f", aobj) + " " + getResources().getString(R.string.duck));
			targetCalWidth = (int)(64D + (double)(i * 44) / 5000D);
		} else if ((double)i > 5000D && (double)i <= 12000D) {
			double d = (double)i / 4320D;
			Object aobj[] = new Object[1];
			aobj[0] = Double.valueOf(d);
			describe_total_calorie.setText(getResources().getString(R.string.Ferrari) + " " + String.format("%.1f", aobj) + " " + getResources().getString(R.string.minute));
			targetCalWidth = (int)(108D + (double)(i * 57) / 12000D);
		} else if ((double)i > 12000D && (double)i <= 25000D) {
			double d = (double)i / 8600D;
			Object aobj[] = new Object[1];
			aobj[0] = Double.valueOf(d);			
			describe_total_calorie.setText(getResources().getString(R.string.family_of_three) + " " + String.format("%.1f", aobj) + " " + getResources().getString(R.string.day));
			targetCalWidth = (int)(163D + (double)(i * 48) / 25000D);
		} else if ((double)i > 25000D && (double)i <= 50000D) {
			double d = (double)i / 13000D;
			Object aobj[] = new Object[1];
			aobj[0] = Double.valueOf(d);
			describe_total_calorie.setText(getResources().getString(R.string.equal_to) + " " + String.format("%.1f", aobj) + " " + getResources().getString(R.string.elephant));
			targetCalWidth = (int)(211D + (double)(i * 64) / 50000D);
		} else if ((double)i > 50000D && (double)i <= 100000D) {
			double d = (0.29999999999999999D * (double)i) / 27109D;
			Object aobj[] = new Object[1];
			aobj[0] = Double.valueOf(d);
			describe_total_calorie.setText(getResources().getString(R.string.airplane_747) + " " + String.format("%.1f", aobj) + " " + getResources().getString(R.string.kilometre));
			targetCalWidth = (int)(275D + (double)(i * 45) / 100000D);
		} else if ((double)i > 100000D) {
			double d = (0.29999999999999999D * (double)i) / 27109D;
			Object aobj[] = new Object[1];
			aobj[0] = Double.valueOf(d);
			describe_total_calorie.setText(getResources().getString(R.string.airplane_747) + " " + String.format("%.1f", aobj) + " " + getResources().getString(R.string.kilometre));
			targetCalWidth = 320;
		} else {
			describe_total_calorie.setText(R.string.no_cal);
			targetCalWidth = 0;
		}
	}
	
	private void setViewBMI() {
		double d = mMotionDataCenter.getBMI();
		BMIText.setTypeface(mNumberTP);
		if(BMIText.refreshDisabled) {
			BMIText.setValue(d);
			Object aobj[] = new Object[1];
			aobj[0] = Double.valueOf(d);
			BMIText.setText(String.format("%.1f", aobj));
		} else {
			BMIText.setText("0");
			BMIText.setValue(d);
		}
		
		if (mPersonalConfig.getSex() == PersonalConfig.SEX_MAN) {
			if(d < 18.5D)
				BMIdescribe.setText(R.string.bm1_describe_1);
	        else if(d >= 18.5D && d <= 23D)
	        	BMIdescribe.setText(R.string.bm1_describe_2);
	        else if(d > 23D && d <= 24D)
	        	BMIdescribe.setText(R.string.bm1_describe_3);
	        else if(d > 24D)
	        	BMIdescribe.setText(R.string.bm1_describe_4);
		} else if (mPersonalConfig.getSex() == PersonalConfig.SEX_WOMAN) {
			if(d <= 17D)
				BMIdescribe.setText(R.string.bm1_describe_5);
			else if(d > 17D && d <= 19D)
				BMIdescribe.setText(R.string.bm1_describe_6);
	        else if(d > 19D && d <= 23D)
	        	BMIdescribe.setText(R.string.bm1_describe_7);
	        else if(d > 23D)
	        	BMIdescribe.setText(R.string.bm1_describe_4);
		}
		
		BMIIntroLayout.setVisibility(View.GONE);
		Animation animation = AnimationUtils.loadAnimation(mCtx, R.anim.fade_in);
        animation.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				BMILayout.setVisibility(View.VISIBLE);				
			}
        	
        });
        BMILayout.startAnimation(animation);
	}
	private void setViewBMIIntro() {
        Animation animation = AnimationUtils.loadAnimation(mCtx, R.anim.fade_in);
        animation.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				BMIIntroLayout.setVisibility(View.VISIBLE);				
			}
        	
        });
        BMIIntroLayout.startAnimation(animation);
	}
	
	private void setViewBMR() {
        int i = mMotionDataCenter.getBMR();
        BMRText.setTypeface(mNumberTP);
        if(BMRText.refreshDisabled) {
            BMRText.setValue(i);
            BMRText.setText((new StringBuilder()).append(i).toString());
        } else {
            BMRText.setText("0");
            BMRText.setValue(i);
        }
        BMRIntroLayout.setVisibility(View.GONE);
        Animation animation = AnimationUtils.loadAnimation(mCtx, R.anim.fade_in);
        animation.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				BMRLayout.setVisibility(View.VISIBLE);
			}
        	
        });
        BMRLayout.startAnimation(animation);
	}
	
	private void setViewBMRIntro() {
        Animation animation = AnimationUtils.loadAnimation(mCtx, R.anim.fade_in);
        animation.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				BMRIntroLayout.setVisibility(View.VISIBLE);				
			}
        	
        });
        BMRIntroLayout.startAnimation(animation);
	}
	
	private void setViewWeightTrend() {
		
	}
	
	private void initListener() {
		mNewScrollView.addListener(goalStepText);
        mNewScrollView.addListener(bestCalText);
        mNewScrollView.addListener(bestStepText);
        mNewScrollView.addListener(totalCalText);
        mNewScrollView.addListener(totalStepText);
        mNewScrollView.addListener(BMIText);
        mNewScrollView.addListener(BMRText);
        mNewScrollView.addListener(weightText);
        mNewScrollView.addListener(progressStep);
        mNewScrollView.addListener(progressCal);
        mHandler = new WRHandler(this);
        mHandler.sendEmptyMessageDelayed(0, 500L);
	}
	
	private static class WRHandler extends Handler {
		WeakReference<MotionDataActivity> mMDActivity;

		public WRHandler(MotionDataActivity MDActivity) {
			mMDActivity = new WeakReference<MotionDataActivity>(MDActivity);
		}

		@Override
		public void handleMessage(Message msg) {
			if (mMDActivity != null) {
				MotionDataActivity mda = mMDActivity.get();
				if (mda != null) {
					if (!mda.goalStepText.refreshDisabled) {
						mda.onMeasureTxt(mda.goalStepText);
						mda.canSendScroll = true;
					}
					if (!mda.totalCalText.refreshDisabled) {
						mda.onMeasureTxt(mda.totalCalText);
						mda.canSendScroll = true;
					}
					if (!mda.BMIText.refreshDisabled) {
						mda.onMeasureTxt(mda.BMIText);
						mda.canSendScroll = true;
					}
					if (!mda.BMRText.refreshDisabled) {
						mda.onMeasureTxt(mda.BMRText);
						mda.canSendScroll = true;
					}
					if (!mda.weightText.refreshDisabled) {
						mda.onMeasureTxt(mda.weightText);
						mda.canSendScroll = true;
					}
					if (mda.isFirstProgressCal) {
						mda.onMeasureLayout(mda.progressCal);
						mda.canSendScroll = true;
					}
					if (mda.canSendScroll)
						mda.mNewScrollView.sendScroll(1, 0);
				}
			}
		}
	}
	
    private void onMeasureTxt(NewTextView newtextview)
    {
        int location[] = new int[2];
        newtextview.getLocationInWindow(location);
        newtextview.setLocHeight(location[1]);
    }

    private void onMeasureLayout(NewRelativeLayout newrelativelayout)
    {
        int location[] = new int[2];
        newrelativelayout.getLocationInWindow(location);
        newrelativelayout.setLocHeight(location[1]);
    }  
    
	private void performPorpertyAnimate(View paramView, int paramInt1, int paramInt2, long paramLong) {
		ViewWrapper localViewWrapper = new ViewWrapper(paramView);
		int i = Tools.dip2px(mCtx, paramInt2);
		int[] arrayOfInt = new int[2];
		arrayOfInt[0] = paramInt1;
		arrayOfInt[1] = i;
		ObjectAnimator.ofInt(localViewWrapper, "width", arrayOfInt).setDuration(paramLong).start();
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

	@Override
	public void refreshLayout() {
		// TODO Auto-generated method stub
		if (isFirstProgressStep && progressStep.canAnimate) {
			performPorpertyAnimate(progressStep, 0, targetStepWidth, 1500L);
			isFirstProgressStep = false;
		} else if (!isFirstProgressStep) {
			if (progress_step != null) {
				ViewGroup.LayoutParams localLayoutParams = progress_step.getLayoutParams();
				int w = Tools.dip2px(mCtx, targetStepWidth);
				localLayoutParams.width = w;
				progress_step.setLayoutParams(localLayoutParams);
			}
		}
		
		if (isFirstProgressCal && progressCal.canAnimate) {
			performPorpertyAnimate(progressCal, 0, targetCalWidth, 1500L);
			isFirstProgressCal = false;
		} else if (!isFirstProgressCal) {
			if (progress_cal != null) {
				ViewGroup.LayoutParams localLayoutParams = progress_cal.getLayoutParams();
				int w = Tools.dip2px(mCtx, targetCalWidth);
				localLayoutParams.width = w;
				progress_cal.setLayoutParams(localLayoutParams);
			}
		}
	}

	@Override
	public int getWindowHeight() {
		// TODO Auto-generated method stub
		if(mWindowHeight!=0){
			return mWindowHeight;
		}
		
		return 0;
	}

}
