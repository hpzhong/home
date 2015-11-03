package com.zhuoyou.plugin.info;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.zhuoyi.account.IAccountListener;
import com.zhuoyi.account.ZyAccount;
import com.zhuoyou.plugin.cloud.AlarmUtils;
import com.zhuoyou.plugin.cloud.CloudSync;
import com.zhuoyou.plugin.custom.CustomAlertDialog;
import com.zhuoyou.plugin.database.DBOpenHelper;
import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.running.Guide;
import com.zhuoyou.plugin.running.InfoDialog;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.RunningTitleBar;
import com.zhuoyou.plugin.running.Tools;

public class InformationActivity extends Activity implements OnClickListener {

	private static final Context sContext = RunningApp.getInstance().getApplicationContext();
	private static DBOpenHelper mDBOpenHelper;
	private ImageView mFace_logo;
	private TextView mUser_name;
	private TextView mSignature;
	private RatingBar mRating;
	private TextView mStepView;
	private TextView mDayView;
	private List<String> steps;
	private List<String> complete;
	private LinearLayout likeSports;
	private int[] headIcon;
	private Button btnLogout;
	private ZyAccount mZyAccount;
	private String likeSportIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.information_layout);
		mZyAccount = new ZyAccount(getApplicationContext(), "1102927580",
				"1690816199");
		getExercise();
		initView();
		initUI();
		registerBc();
	}
	
	private void registerBc() {
		IntentFilter intentF = new IntentFilter();
		intentF.addAction("com.zhuoyou.plugin.updateInfo");
		registerReceiver(mGetInfo, intentF);
	}
	
	private void unRegisterBc() {
		unregisterReceiver(mGetInfo);
	}
	
	private BroadcastReceiver mGetInfo = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("com.zhuoyou.plugin.updateInfo")) {
				if (Tools.getHead(InformationActivity.this) != 6) {
					mFace_logo.setImageResource(headIcon[Tools.getHead(InformationActivity.this)]);
				} else {
					mFace_logo.setImageResource(R.drawable.logo_default);
				}

				if (!Tools.getSignature(InformationActivity.this).equals("")) {
					mSignature.setText(Tools.getSignature(InformationActivity.this));
				} else {
					mSignature.setText(null);
				}
				likeSportIndex =Tools.getLikeSportsIndex(InformationActivity.this);
				String likeIndex[]=null;
				if(!likeSportIndex.equals("")){
					likeIndex=likeSportIndex.split(",");
					if (likeIndex.length > 0) {
						for (int i = 0; i < likeIndex.length; i++) {
							ImageView img = new ImageView(InformationActivity.this);
							img.setImageResource(Tools.sportType[Integer
									.parseInt(likeIndex[i])]);
							likeSports.addView(img);
						}
					}
					while(likeSports.getChildCount()>likeIndex.length){
						likeSports.removeViewAt(0);
						if(likeSports.getChildCount()<=likeIndex.length){
							break;
						}
					}
				}else{
					if(likeSports.getChildCount()<=0){
						ImageView img = new ImageView(InformationActivity.this);
						img.setImageResource(Tools.sportType[28]);
						likeSports.addView(img);
					}	
				}
			}
		}
	};

	private void initView() {
		mFace_logo = (ImageView) findViewById(R.id.face_logo);
		mUser_name = (TextView) findViewById(R.id.user_name);
		mSignature = (TextView) findViewById(R.id.signature);
		mRating = (RatingBar) findViewById(R.id.rating);

		mStepView = (TextView) findViewById(R.id.steps);
		mDayView = (TextView) findViewById(R.id.days);
		
		likeSports = (LinearLayout) findViewById(R.id.like_sports);

		btnLogout = (Button) findViewById(R.id.logout);

	}

	private void initUI() {
		mRating.setRating(getStarNum());
		mStepView.setText(getBestValue());
		mDayView.setText(getAchievDays());
	}

	private void initDate() {
		headIcon = Tools.headIcon;

		if (Tools.getHead(this) != 6) {
			mFace_logo.setImageResource(headIcon[Tools.getHead(this)]);
		} else {
			mFace_logo.setImageResource(R.drawable.logo_default);
		}

		if (Tools.getUsrName(this).equals("")) {
			if (!Tools.getLoginName(InformationActivity.this).equals("")) {
				mUser_name.setText(Tools.getLoginName(InformationActivity.this));
			} else {
				mUser_name.setText(null);
			}
		} else {
			mUser_name.setText(Tools.getUsrName(InformationActivity.this));
		}

		if (!Tools.getSignature(InformationActivity.this).equals("")) {
			mSignature.setText(Tools.getSignature(InformationActivity.this));
		} else {
			mSignature.setText(null);
		}
		
		likeSportIndex =Tools.getLikeSportsIndex(InformationActivity.this);
		String likeIndex[]=null;
		if(!likeSportIndex.equals("")){
			likeIndex=likeSportIndex.split(",");
			if (likeIndex.length > 0) {
				for (int i = 0; i < likeIndex.length; i++) {
					ImageView img = new ImageView(InformationActivity.this);
					img.setImageResource(Tools.sportType[Integer
							.parseInt(likeIndex[i])]);
					likeSports.addView(img);
				}
			}
			while(likeSports.getChildCount()>likeIndex.length){
				likeSports.removeViewAt(0);
				if(likeSports.getChildCount()<=likeIndex.length){
					break;
				}
			}
		}else{
			if(likeSports.getChildCount()<=0){
				ImageView img = new ImageView(InformationActivity.this);
				img.setImageResource(Tools.sportType[28]);
				likeSports.addView(img);
			}	
		}

		if (Tools.getLogin(InformationActivity.this)) {
			btnLogout.setText(R.string.log_out);
		} else {
			btnLogout.setText(R.string.login);
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void getExercise() {
		steps = new ArrayList<String>();
		complete = new ArrayList<String>();
		String enter_day = Tools.getFirstEnterDay(this);
		String today = Tools.getDate(0);
		int count = Tools.getDayCount(enter_day, today);
		ContentResolver cr = getContentResolver();
		for (int i = 0; i < count; i++) {
			String day = Tools.getDate(enter_day, 0 - i);
			Cursor c = cr.query(DataBaseContants.CONTENT_URI, new String[] {
					"_id", "steps", "complete" }, 
					DataBaseContants.DATE + "  = ? AND " + DataBaseContants.STATISTICS + " = ? ", 
					new String[] { day, "1" }, null);
			c.moveToFirst();
			if (c.getCount() > 0 && c.moveToFirst()
					&& c.getLong(c.getColumnIndex(DataBaseContants.ID)) > 0) {
				int step = c.getInt(c.getColumnIndex(DataBaseContants.STEPS));
				int state = c.getInt(c.getColumnIndex(DataBaseContants.COMPLETE));
				steps.add(String.valueOf(step));
				complete.add(String.valueOf(state));
			}
			c.close();
			c = null;
		}
	}

	private float getStarNum() {
		int star_num = 1;
		if (steps != null && steps.size() > 0) {
			int total = 0;
			int number = 0;
			for (int i = 0; i < steps.size(); i++) {
				int step = Integer.parseInt(steps.get(i));
				if (step > 0) {
					total = total + step;
					number++;
				}
			}
			if (number > 0) {
				if (total / number > 9000)
					star_num = 5;
				else if (total / number > 7000)
					star_num = 4;
				else if (total / number > 4000)
					star_num = 3;
				else if (total / number > 2000)
					star_num = 2;
				else
					star_num = 1;
			}
		}
		return (float) star_num;
	}

	private String getBestValue() {
		int best_value = 0;
		if (steps != null && steps.size() > 0) {
			for (int i = 0; i < steps.size(); i++) {
				int step = Integer.parseInt(steps.get(i));
				if (step > best_value)
					best_value = step;
			}
		}
		return String.valueOf(best_value);
	}

	private String getAchievDays() {
		int days = 0;
		int max = 0;
		if (complete != null && complete.size() > 0) {
			for (int i = 0; i < complete.size(); i++) {
				int state = Integer.parseInt(complete.get(i));
				if (state == 1) {
					days++;
					max = max > days ? max : days;
				} else {
					days = 0;
				}
			}
		}
		return String.valueOf(max);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.info:
			InfoDialog mDialog = new InfoDialog(InformationActivity.this,
					R.style.info_dialog, R.layout.info_dialog_layout);
			mDialog.setCanceledOnTouchOutside(true);
			mDialog.show();
			break;
		case R.id.modify_info:
			Intent eIntent = new Intent();
			eIntent.setClass(InformationActivity.this, EditInformation.class);
			startActivity(eIntent);
			break;
		case R.id.moreInfo:
			Intent mIntent = new Intent();
			mIntent.setClass(InformationActivity.this, MoreInformation.class);
			startActivity(mIntent);
			break;
		case R.id.testCenter:
			Intent tIntent = new Intent();
			tIntent.setClass(InformationActivity.this, Guide.class);
			tIntent.putExtra("test_center", true);
			startActivity(tIntent);
			break;
		case R.id.logout:
			if (Tools.getLogin(InformationActivity.this)) {				
				CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(InformationActivity.this);
				builder.setTitle(R.string.log_out);
				builder.setMessage(R.string.log_out_message);
				builder.setPositiveButton(R.string.cancle, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.setNegativeButton(R.string.continueto, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						if (Tools.getLoginName(InformationActivity.this).equals(
								mUser_name.getText().toString())) {
							mUser_name.setText(null);
						}
						Tools.saveInfoToSharePreference(InformationActivity.this, "");
						Tools.setLogin(InformationActivity.this, false);
						btnLogout.setText(R.string.login);
						onResume();
						AlarmUtils.cancelAutoSyncAlarm(InformationActivity.this);
						CloudSync.autoSyncType = 1;
						clearFeedTable(DataBaseContants.TABLE_DATA_NAME);
						clearFeedTable(DataBaseContants.TABLE_DELETE_NAME);
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
						Tools.saveInfoToSharePreference(
								InformationActivity.this, userInfo);
						Tools.setLogin(InformationActivity.this, true);
						if (Tools.getUsrName(InformationActivity.this).equals(
								"")) {
							mUser_name.setText(Tools
									.getLoginName(InformationActivity.this));
						} else {
							mUser_name.setText(Tools
									.getUsrName(InformationActivity.this));
						}
						btnLogout.setText(R.string.log_out);
						AlarmUtils.setAutoSyncAlarm(InformationActivity.this);
						CloudSync.autoSyncType = 1;
						CloudSync.downloadData(0);
					}

					@Override
					public void onCancel() {

					}
				});
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		RunningTitleBar.getTitleBar(this, getResources().getString(R.string.information));
		initDate();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unRegisterBc();
	}

	public static void clearFeedTable(String name){
		mDBOpenHelper = new DBOpenHelper(sContext);
		String sql = "DELETE FROM " + name +";";
		SQLiteDatabase db =  mDBOpenHelper.getWritableDatabase();
		db.execSQL(sql);
		revertSeq(name);
		mDBOpenHelper.close();
	}
	
	private static void revertSeq(String name) {
		String sql = "update sqlite_sequence set seq=0 where name='"+name+"'";
		SQLiteDatabase db = mDBOpenHelper.getWritableDatabase();
		db.execSQL(sql);
		mDBOpenHelper.close();
	}
}
