package com.zhuoyou.plugin.info;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyi.account.IAccountListener;
import com.zhuoyi.account.ZyAccount;
import com.zhuoyou.plugin.add.TosAdapterView;
import com.zhuoyou.plugin.add.TosAdapterView.OnItemSelectedListener;
import com.zhuoyou.plugin.add.TosGallery;
import com.zhuoyou.plugin.add.TosGallery.OnEndFlingListener;
import com.zhuoyou.plugin.cloud.AlarmUtils;
import com.zhuoyou.plugin.cloud.CloudSync;
import com.zhuoyou.plugin.custom.CustomAlertDialog;
import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.running.PersonalConfig;
import com.zhuoyou.plugin.running.PersonalGoal;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;
import com.zhuoyou.plugin.running.Welcome;
import com.zhuoyou.plugin.view.MofeiScrollView;
import com.zhuoyou.plugin.view.WheelView;

public class PersonalInformation extends Activity implements OnClickListener, OnEndFlingListener {
	private MofeiScrollView mScrollView;
	TextView mage_text;
	TextView mheight_text;
	TextView mweight_text;

	TextView age_text_unit;
	TextView weight_text_unit;
	TextView height_text_unit;
	TextView weight_text_point_unit;

	WheelView mage_wheelView;
	WheelView mheight_wheelView;
	WheelView mweight_wheelView;
	WheelView mweight_wheelView_point;

	WheelTextAdapter age_adaptor;
	WheelTextAdapter height_adaptor;
	WheelTextAdapter weight_adaptor;
	WheelTextAdapter weight_point_adaptor;

	int current_weight = 75; // 25-205 精确到0.1
	int current_weight_point = 0;
	int Default_tagert = 10000; // 运动目标

	private ZyAccount mZyAccount;
	private Boolean needCloud = false;

	LinearLayout editUsrinfo;
	FrameLayout weight_layout,age_layout,height_layout;
	
	RelativeLayout editHead;
	LinearLayout choiceSports;
	int sportIndex;

	private ImageView face;
	private EditText nickname;
	private EditText signature;
	private LinearLayout likeSports;
	private List<String> sportType;
	private String[] sportArray = new String[100];
	int i = 0;
	int headIndex;
	String likeSportIndex = null;
	private Bitmap bmp = null;
	private byte[] bitmapByte = null;
	private Button btnLogout;
	private Context sContext = RunningApp.getInstance().getApplicationContext();
	private int size = Tools.dip2px(sContext, 30);
	private PersonalConfig mPersonalConfig;
	private PersonalGoal mPersonalGoal;

	private TextView sMan, sWoman;
	private TextView mGoals1, mGoals2, mGoals3, mGoals4;
	private EditText target;
	private int selectPostion;
	private boolean from_center = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_information);
		mZyAccount = new ZyAccount(getApplicationContext(), "1102927580",
				"1690816199");
		mPersonalConfig = Tools.getPersonalConfig();
		mPersonalGoal=Tools.getPersonalGoal();
		initView();
		initDate();
		mage_wheelView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(TosAdapterView<?> parent, View view,
					int position, long id) {
				selectPostion = mage_wheelView.getSelectedItemPosition();
				age_adaptor.SetSelecttion(selectPostion);
				age_adaptor.notifyDataSetChanged();
			}

			public void onNothingSelected(TosAdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
		mheight_wheelView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(TosAdapterView<?> parent, View view,
					int position, long id) {
				selectPostion=mheight_wheelView.getSelectedItemPosition();
				height_adaptor.SetSelecttion(selectPostion);
				height_adaptor.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(TosAdapterView<?> parent) {
				
			}
		});
		
		mweight_wheelView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(TosAdapterView<?> parent, View view,
					int position, long id) {
				selectPostion=mweight_wheelView.getSelectedItemPosition();
				weight_adaptor.SetSelecttion(selectPostion);
				weight_adaptor.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(TosAdapterView<?> parent) {
				
			}
		});
		
		mweight_wheelView_point.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(TosAdapterView<?> parent, View view,
					int position, long id) {
				selectPostion=mweight_wheelView_point.getSelectedItemPosition();
				weight_point_adaptor.SetSelecttion(selectPostion);
				weight_point_adaptor.notifyDataSetChanged();
			}
			@Override
			public void onNothingSelected(TosAdapterView<?> parent) {
				
			}
		});
	}

	private void initDate() {
		likeSportIndex = null;
		likeSports.removeAllViews();
		sportArray = getResources().getStringArray(R.array.whole_sport_type);
		headIndex = Tools.getHead(this);
		if (headIndex == 10000) {
			bmp = Tools.convertFileToBitmap("/Running/download/custom");
			face.setImageBitmap(bmp);
		} else if (headIndex == 1000) {
			bmp = Tools.convertFileToBitmap("/Running/download/logo");
			face.setImageBitmap(bmp);
		} else {
			face.setImageResource(Tools.selectByIndex(headIndex));
		}

		if (Tools.getUsrName(this).equals("")) {
			if (!Tools.getLoginName(this).equals("")) {
				nickname.setText(Tools.getLoginName(this));
			} else {
				nickname.setText(null);
			}
		} else {
			nickname.setText(Tools.getUsrName(this));
		}
		nickname.setSelection(nickname.getText().length());
		if (mPersonalConfig.getSex() == PersonalConfig.SEX_MAN) {
			selectSex(0);
		} else if (mPersonalConfig.getSex() == PersonalConfig.SEX_WOMAN) {
			selectSex(1);
		}
		mage_text.setText(Calendar.getInstance().get(Calendar.YEAR) - mPersonalConfig.getYear() + getResources().getString(R.string.unit_age));
		mheight_text.setText(mPersonalConfig.getHeight() + getResources().getString(R.string.unit_length));
		mweight_text.setText(mPersonalConfig.getWeight() + getResources().getString(R.string.unit_weight));
		
		if(mPersonalGoal.getStep()==7000){
			selectGoals(1);
		}else if(mPersonalGoal.getStep()==10000){
			selectGoals(2);
		}else if(mPersonalGoal.getStep()==15000){
			selectGoals(3);
		}else{
			selectGoals(4);
		}
		target.setText(mPersonalGoal.getStep() + "");
		if (!Tools.getSignature(sContext).equals("")) {
			signature.setText(Tools.getSignature(sContext));
			signature.setSelection(signature.getText().length());
		}

		likeSportIndex = Tools.getLikeSportsIndex(sContext);
		String likeIndex[] = null;
		if (!likeSportIndex.equals("")) {
			likeIndex = likeSportIndex.split(",");
			if (likeIndex.length > 0) {
				for (int i = 0; i < likeIndex.length; i++) {
					ImageView img = new ImageView(sContext);
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
					params.leftMargin = 10;
					img.setLayoutParams(params);
					img.setImageResource(Tools.sportType[Integer.parseInt(likeIndex[i])]);
					likeSports.addView(img);
				}
			}
		} else {
			TextView text = new TextView(sContext);
			text.setText(R.string.choose_favorite_sport);
			likeSports.addView(text);
		}

		if (Tools.getLogin(sContext)) {
			btnLogout.setText(R.string.log_out);
		} else {
			btnLogout.setText(R.string.login);
		}
	}

	public void initView() {
		if (getIntent() != null) {
			from_center = getIntent().getBooleanExtra("from_center", false);
		}
		mScrollView = (MofeiScrollView) findViewById(R.id.scrollview);
		mage_text = (TextView) findViewById(R.id.age_text);
		mheight_text = (TextView) findViewById(R.id.height_text);
		mweight_text = (TextView) findViewById(R.id.weight_text);

		mage_wheelView = (WheelView) findViewById(R.id.age_select);
		mheight_wheelView = (WheelView) findViewById(R.id.height_select);
		mweight_wheelView = (WheelView) findViewById(R.id.weight_select);
		mweight_wheelView_point=(WheelView) findViewById(R.id.weight_select_small);

		mage_wheelView.setOnEndFlingListener(this);
		mage_wheelView.setSoundEffectsEnabled(true);
		age_adaptor = new WheelTextAdapter(this, getAge(), 30);
		mage_wheelView.setAdapter(age_adaptor);

		mheight_wheelView.setOnEndFlingListener(this);
		mheight_wheelView.setSoundEffectsEnabled(true);
		height_adaptor = new WheelTextAdapter(this, getHeight(), 30);
		mheight_wheelView.setAdapter(height_adaptor);

		mweight_wheelView.setOnEndFlingListener(this);
		mweight_wheelView.setSoundEffectsEnabled(true);
		weight_adaptor = new WheelTextAdapter(this, getWeight(), 30);
		mweight_wheelView.setAdapter(weight_adaptor);
		
		
		mweight_wheelView_point.setOnEndFlingListener(this);
		mweight_wheelView_point.setSoundEffectsEnabled(true);
		weight_point_adaptor = new WheelTextAdapter(this, getWeightPoint(),30);
		mweight_wheelView_point.setAdapter(weight_point_adaptor);

		face = (ImageView) findViewById(R.id.face_logo);
		nickname = (EditText) findViewById(R.id.nickname);
		signature = (EditText) findViewById(R.id.signature);
		likeSports = (LinearLayout) findViewById(R.id.like_sportss);
		btnLogout = (Button) findViewById(R.id.logouts);
		if (from_center)
			btnLogout.setVisibility(View.INVISIBLE);
		sMan = (TextView) findViewById(R.id.sex_man);
		sWoman = (TextView) findViewById(R.id.sex_woman);
		mGoals1 = (TextView) findViewById(R.id.information_goals1);
		mGoals1.setOnClickListener(this);
		mGoals2 = (TextView) findViewById(R.id.information_goals2);
		mGoals2.setOnClickListener(this);
		mGoals3 = (TextView) findViewById(R.id.information_goals3);
		mGoals3.setOnClickListener(this);
		mGoals4 = (TextView) findViewById(R.id.information_goals4);
		mGoals4.setOnClickListener(this);
		target = (EditText) findViewById(R.id.target);
		
		age_layout = (FrameLayout) findViewById(R.id.show_age_layout);
		height_layout = (FrameLayout) findViewById(R.id.show_height_layout);
		weight_layout = (FrameLayout) findViewById(R.id.weight_layout);
		
		
		age_text_unit=(TextView) findViewById(R.id.show_age_unit);
		weight_text_unit=(TextView) findViewById(R.id.show_weight_unit);
		height_text_unit=(TextView) findViewById(R.id.show_height_unit);
		weight_text_point_unit=(TextView) findViewById(R.id.show_weight_point_unit);
	}

	private void selectSex(int index) {
		switch (index) {
		case 0:
			sMan.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.information_man_select, 0, 0);
			sWoman.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.information_woman, 0, 0);			
			break;
		case 1:
			sMan.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.information_man, 0, 0);
			sWoman.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.information_woman_select, 0, 0);			
			break;

		default:
			break;
		}
	}

	private void selectGoals(int index) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
		switch (index) {
		case 1:
			mGoals1.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.goals_primary_select, 0, 0);
			mGoals2.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.goals_middle, 0, 0);
			mGoals3.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.goals_high, 0, 0);
			mGoals4.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.goals_custom, 0, 0);
			mGoals1.setTextColor(0xFFF69D72);
			mGoals2.setTextColor(0xFFB9B9B9);
			mGoals3.setTextColor(0xFFB9B9B9);
			mGoals4.setTextColor(0xFFB9B9B9);
			target.setFocusableInTouchMode(false);
			target.clearFocus();
			target.setText("7000");
			break;
		case 2:
			mGoals1.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.goals_primary, 0, 0);
			mGoals2.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.goals_middle_select, 0, 0);
			mGoals3.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.goals_high, 0, 0);
			mGoals4.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.goals_custom, 0, 0);
			mGoals1.setTextColor(0xFFB9B9B9);
			mGoals2.setTextColor(0xFFF69D72);
			mGoals3.setTextColor(0xFFB9B9B9);
			mGoals4.setTextColor(0xFFB9B9B9);
			target.setFocusableInTouchMode(false);
			target.clearFocus();
			target.setText("10000");
			break;
		case 3:
			mGoals1.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.goals_primary, 0, 0);
			mGoals2.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.goals_middle, 0, 0);
			mGoals3.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.goals_high_select, 0, 0);
			mGoals4.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.goals_custom, 0, 0);
			mGoals1.setTextColor(0xFFB9B9B9);
			mGoals2.setTextColor(0xFFB9B9B9);
			mGoals3.setTextColor(0xFFF69D72);
			mGoals4.setTextColor(0xFFB9B9B9);
			target.setFocusableInTouchMode(false);
			target.clearFocus();
			target.setText("15000");
			break;
		case 4:
			mGoals1.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.goals_primary, 0, 0);
			mGoals2.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.goals_middle, 0, 0);
			mGoals3.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.goals_high, 0, 0);
			mGoals4.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.goals_custom_select, 0, 0);
			mGoals1.setTextColor(0xFFB9B9B9);
			mGoals2.setTextColor(0xFFB9B9B9);
			mGoals3.setTextColor(0xFFB9B9B9);
			mGoals4.setTextColor(0xFFF69D72);
			target.setFocusableInTouchMode(true);
			imm.showSoftInput(target, 0);
			target.requestFocus();
			target.setSelection(target.getText().toString().length());
			break;
		default:
			break;
		}
	}

	private String[] getAge() {
		String[] mage = getResources().getStringArray(R.array.age_item);
		return mage;
	}

	private String[] getHeight() {
		String[] temp = getResources().getStringArray(R.array.height_item);
		return temp;
	}

	private String[] getWeight() {
		String[] temp = getResources().getStringArray(R.array.weight_item);
		return temp;
	}
	
	private String[] getWeightPoint(){
		String[] temp = getResources().getStringArray(R.array.weight_point);
		return temp;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
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
	public void finish() {
		if (needCloud) {
			setResult(RESULT_OK);
		}
		super.finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 100) {
			if (resultCode == RESULT_OK) {
				headIndex = data.getIntExtra("headIndex", 6);
				bitmapByte = data.getByteArrayExtra("bitmap");
				if (headIndex == 10000) {
					if (bitmapByte == null)
						bmp = Tools.convertFileToBitmap("/Running/download/custom");
					else
						bmp = BitmapFactory.decodeByteArray(bitmapByte, 0, bitmapByte.length);
					face.setImageBitmap(bmp);
				} else if (headIndex == 1000) {
					bmp = Tools.convertFileToBitmap("/Running/download/logo");
					face.setImageBitmap(bmp);
				} else {
					face.setImageResource(Tools.selectByIndex(headIndex));
				}
			}
		} else if (requestCode == 101) {
			if (resultCode == RESULT_OK) {
				List<String> selectedIndex = new ArrayList<String>();
				sportType = data.getStringArrayListExtra("sports");
				if (sportType.size() == 0) {
					likeSportIndex = "";
					likeSports.removeAllViews();
					TextView text = new TextView(sContext);
					text.setText(R.string.choose_favorite_sport);
					likeSports.addView(text);
				} else {
					for(int i=0;i<sportType.size();i++){
						selectedIndex.add(Integer.toString(Tools.getSportIndex(sportArray, sportType.get(i))));
					}
					for(int i=0;i<selectedIndex.size();i++){
						ImageView img = new ImageView(sContext);
						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
						params.leftMargin = 10;
						img.setLayoutParams(params);
						img.setImageResource(Tools.sportType[Integer.parseInt(selectedIndex.get(i))]);
						likeSports.addView(img);
						if(i== 0){
							likeSportIndex = selectedIndex.get(i);
						}else{
							likeSportIndex=likeSportIndex+","+selectedIndex.get(i);
						}
					}
					while(likeSports.getChildCount()>selectedIndex.size()){
						likeSports.removeViewAt(0);
						if(likeSports.getChildCount()<=selectedIndex.size()){
							break;
						}
					}
				}
			}
		}
	}

	private void saveCustomHead() {
		if (bitmapByte == null)
			return;
		String file = Tools.getSDPath() + "/Running/download/";
		File dirFile = new File(file);
		if (!dirFile.exists()) {
			dirFile.mkdir();
		}
		File myCaptureFile = new File(file + "custom");
		FileOutputStream fops = null;
		try {
			fops = new FileOutputStream(myCaptureFile);
			fops.write(bitmapByte);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fops.flush();
				fops.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void onClick(View v) {
		int positon = 0;
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.edit_heads:
			Intent intent = new Intent();
			intent.setClass(sContext, ChooseHeadActivity.class);
			startActivityForResult(intent, 100);
			break;
		case R.id.choice_sportss:
			Intent sIntent = new Intent();
			sIntent.putExtra("sportlike", likeSportIndex);
			sIntent.setClass(sContext, ChooseSport.class);
			startActivityForResult(sIntent, 101);
			break;
		case R.id.sex_man:			
			mPersonalConfig.setSex(PersonalConfig.SEX_MAN);
			selectSex(0);
			break;
		case R.id.sex_woman:			
			mPersonalConfig.setSex(PersonalConfig.SEX_WOMAN);
			selectSex(1);
			break;
		case R.id.information_goals1:
			selectGoals(1);
			break;
		case R.id.information_goals2:
			selectGoals(2);
			break;
		case R.id.information_goals3:
			selectGoals(3);
			break;
		case R.id.information_goals4:
			selectGoals(4);
			break;
		case R.id.save:
			if (target.getText().toString().equals("")){
				save();
				CloudSync.startSyncInfo();
				finish();
			} else {
				int targetStep = Integer.parseInt(target.getText().toString());
				int meter = Tools.calcDistance(targetStep, mPersonalConfig.getHeight());
				int cal = Tools.calcCalories(meter, mPersonalConfig.getWeightNum());
				if (targetStep < 5000) {
					showAlertDilog(this.getResources().getString(R.string.least_step));
				} else if (targetStep >= 60000) {
					showAlertDilog(this.getResources().getString(R.string.most_step));
				} else {
					mPersonalGoal.mGoalSteps = targetStep;
					mPersonalGoal.mGoalCalories = cal;
					save();
					CloudSync.startSyncInfo();
					finish();
				}
			}
			break;
		case R.id.age_lay:
			mScrollView.setView(null);
			if (age_layout.getVisibility() == View.GONE) {
				mScrollView.setView(age_layout);
				mage_wheelView.setVisibility(View.VISIBLE);
				age_layout.setVisibility(View.VISIBLE);
				age_text_unit.setVisibility(View.VISIBLE);
				positon = mPersonalConfig.getYear() - 1930;
				mage_wheelView.setSelection(positon, true);
				mheight_wheelView.setVisibility(View.GONE);
				mweight_wheelView.setVisibility(View.GONE);
				mweight_wheelView_point.setVisibility(View.GONE);
				weight_layout.setVisibility(View.GONE);
				height_layout.setVisibility(View.GONE);
			} else {
				mage_wheelView.setVisibility(View.GONE);
				age_layout.setVisibility(View.GONE);
				age_text_unit.setVisibility(View.GONE);
			}
			break;

		case R.id.height_lay:
			mScrollView.setView(null);
			if (height_layout.getVisibility() == View.GONE) {
				mScrollView.setView(height_layout);
				mheight_wheelView.setVisibility(View.VISIBLE);
				height_layout.setVisibility(View.VISIBLE);
				height_text_unit.setVisibility(View.VISIBLE);
				positon = mPersonalConfig.getHeight() - 55;
				mheight_wheelView.setSelection(positon, true);
				mage_wheelView.setVisibility(View.GONE);
				mweight_wheelView.setVisibility(View.GONE);
				mweight_wheelView_point.setVisibility(View.GONE);
				weight_layout.setVisibility(View.GONE);
				age_layout.setVisibility(View.GONE);
			} else {
				mheight_wheelView.setVisibility(View.GONE);
				height_layout.setVisibility(View.GONE);
				height_text_unit.setVisibility(View.GONE);
			}
			break;

		case R.id.weight_lay:
			mScrollView.setView(null);
			if (weight_layout.getVisibility() == View.GONE) {
				mScrollView.setView(weight_layout);
				mweight_wheelView.setVisibility(View.VISIBLE);
				mweight_wheelView_point.setVisibility(View.VISIBLE);
				weight_text_unit.setVisibility(View.VISIBLE);
				weight_text_point_unit.setVisibility(View.VISIBLE);
				weight_layout.setVisibility(View.VISIBLE);
				String[] weight = mPersonalConfig.getWeight().split("\\.");
				current_weight = Integer.valueOf(weight[0]);
				mweight_wheelView.setSelection(current_weight - 25, true);
				if (weight.length < 2)
					mweight_wheelView_point.setSelection(0, true);
				else {
					current_weight_point = Integer.valueOf(weight[1]);
					mweight_wheelView_point.setSelection(current_weight_point, true);
				}
				mage_wheelView.setVisibility(View.GONE);
				mheight_wheelView.setVisibility(View.GONE);
				age_layout.setVisibility(View.GONE);
				height_layout.setVisibility(View.GONE);
			} else {
				mweight_wheelView.setVisibility(View.GONE);
				mweight_wheelView_point.setVisibility(View.GONE);
				weight_layout.setVisibility(View.GONE);
				weight_text_unit.setVisibility(View.GONE);
				weight_text_point_unit.setVisibility(View.GONE);
			}
			break;
		case R.id.logouts:
			if (Tools.getLogin(sContext)) {
				CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(sContext);
				builder.setTitle(R.string.log_out);
				builder.setMessage(R.string.log_out_message);
				builder.setPositiveButton(R.string.cancle,new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,int which) {
								dialog.dismiss();
							}
						});
				builder.setNegativeButton(R.string.continueto,new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								if (Tools.getLoginName(sContext).equals(nickname.getText().toString())) {
									nickname.setText(null);
								}
								Tools.saveInfoToSharePreference(sContext, "");
								Tools.setLogin(sContext, false);
								btnLogout.setText(R.string.login);
								initDate();
								AlarmUtils.cancelAutoSyncAlarm(sContext);
								CloudSync.autoSyncType = 1;
								Tools.clearFeedTable(DataBaseContants.TABLE_DATA_NAME, sContext);
								Tools.clearFeedTable(DataBaseContants.TABLE_DELETE_NAME, sContext);
								Welcome.isentry = true;
							}
						});
				builder.setCancelable(false);
				CustomAlertDialog dialog = builder.create();
				dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				dialog.show();
			} else {
				// TODO Auto-generated method stub
				mZyAccount.login(new IAccountListener() {
					@Override
					public void onSuccess(String userInfo) {
						Tools.saveInfoToSharePreference(sContext, userInfo);
						Tools.setLogin(sContext, true);
						needCloud = true;
						finish();
					}

					@Override
					public void onCancel() {

					}
				});
			}
			break;
		case R.id.goods_address:
			Intent mIntent = new Intent();
			mIntent.setClass(sContext, GoodsAddressActivity.class);
			startActivity(mIntent);
			break;
		default:
			break;
		}
	}

	private void showAlertDilog(String string) {
		Builder builder = new AlertDialog.Builder(this);
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
	
	private void save() {
		saveCustomHead();
		Tools.updatePersonalGoal(mPersonalGoal);
		Tools.updatePersonalConfig(mPersonalConfig);
		Tools.setHead(sContext, headIndex);
		Tools.setUsrName(sContext, nickname.getText().toString());
		Tools.setSignature(sContext, signature.getText().toString());
		Tools.setLikeSportsIndex(sContext, likeSportIndex);
	}
	
	@Override
	public void onEndFling(TosGallery v) {
		int temp;
		switch (v.getId()) {
		case R.id.age_select:
			temp = mage_wheelView.getSelectedItemPosition();
			int current_birth = temp + 1930;
			mPersonalConfig.setYear(current_birth);
			mage_text.setText(Calendar.getInstance().get(Calendar.YEAR) - current_birth + getResources().getString(R.string.unit_age));
			break;
		case R.id.height_select:
			temp = mheight_wheelView.getSelectedItemPosition();
			int current_height = temp + 55;
			mPersonalConfig.setHeight(current_height);
			mheight_text.setText(current_height + getResources().getString(R.string.unit_length));
			break;
		case R.id.weight_select:
			temp = mweight_wheelView.getSelectedItemPosition();
			current_weight = temp + 25;
			mPersonalConfig.setWeight(current_weight + "." + current_weight_point);
			mweight_text.setText(mPersonalConfig.getWeight() + getResources().getString(R.string.unit_weight));
			break;
		case R.id.weight_select_small:
			current_weight_point = mweight_wheelView_point.getSelectedItemPosition();
			mPersonalConfig.setWeight(current_weight + "." + current_weight_point);
			mweight_text.setText(mPersonalConfig.getWeight() + getResources().getString(R.string.unit_weight));
			break;
		default:
			break;
		}
	}
}
