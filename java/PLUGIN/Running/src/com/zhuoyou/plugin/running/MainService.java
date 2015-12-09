package com.zhuoyou.plugin.running;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.zhuoyou.plugin.cloud.AlarmUtils;
import com.zhuoyou.plugin.cloud.CloudSync;
import com.zhuoyou.plugin.custom.CustomAlertDialog;
import com.zhuoyou.plugin.database.DataBaseContants;

public class MainService extends Service {
	private static MainService mInstance;
	private static final Context sContext = RunningApp.getInstance().getApplicationContext();
	private ProgressDialog mDialog;
	private UpdateHandler mHandler = new UpdateHandler(this);
	private List<RunningItem> mTempLists = new ArrayList<RunningItem>();
	private static final int HANDLE_MSG_CANCEL_MSG = 1008;
	private static final int HANDLE_MSG_STORE_MSG = 1024;
	private ContentResolver mContentResolver;
	private static Boolean syncnow;

	public static class UpdateHandler extends Handler {
		WeakReference<MainService> mMyFragment;

		public UpdateHandler(MainService f) {
			mMyFragment = new WeakReference<MainService>(f);
		}

		@Override
		public void handleMessage(Message msg) {
			if (mMyFragment != null) {
				MainService home = mMyFragment.get();
				if (home != null) {
					switch (msg.what) {
					case HANDLE_MSG_CANCEL_MSG:
						if (home.mDialog != null && home.mDialog.isShowing()) {
							home.mDialog.dismiss();
						}
						Toast.makeText(home, R.string.connect_time_out, Toast.LENGTH_SHORT).show();
						syncnow = false;
						break;
					case HANDLE_MSG_STORE_MSG:
						home.insertToDatabase();
						syncnow = false;
						break;
					default:
						break;
					}
				}
			}
		}
	}

	public static MainService getInstance() {
		if (mInstance == null) {
			Log.i("gchk", "getInstance(), Main service is null.");
			startMainService();
		}

		return mInstance;
	}

	private static void startMainService() {
		Log.i("gchk", "startMainService()");
		Intent startServiceIntent = new Intent(sContext, MainService.class);
		sContext.startService(startServiceIntent);
	}

	public void onCreate() {
		super.onCreate();
		Log.i("gchk", "onCreate()");

		mInstance = this;

		mDialog = new ProgressDialog(this);
		mDialog.setCancelable(false);
		mDialog.setMessage(getString(R.string.loading_data));
		mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

		mContentResolver = getContentResolver();

		registerBc();
		syncnow = false;
		if (Tools.getLogin(sContext)) {
			AlarmUtils.setAutoSyncAlarm(sContext);
			CloudSync.autoSyncType = 1;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unRegisterBc();
		startMainService();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private void registerBc() {
		IntentFilter intentF = new IntentFilter();
		intentF.addAction("com.zhuoyou.plugin.running.sync.personal");
		registerReceiver(mSyncPersionalReceiver, intentF);

		IntentFilter intentF1 = new IntentFilter();
		intentF1.addAction("com.zhuoyou.plugin.running.get");
		registerReceiver(mGetDataReceiver, intentF1);
	}

	private void unRegisterBc() {
		unregisterReceiver(mSyncPersionalReceiver);
		unregisterReceiver(mGetDataReceiver);
	}

	private BroadcastReceiver mGetDataReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("com.zhuoyou.plugin.running.get")) {
				char[] c_tag = intent.getCharArrayExtra("tag");
				String content = intent.getStringExtra("content");
				Log.i("gchk", "mGetDataReceiver 0X71 TAG[0] =" + c_tag[0] + "TAG[1]= " + c_tag[1] + "||| c= " + content);
				int curr_index = c_tag[0] - 0x20;
				int totle_number = c_tag[1] - 0x20;
				Log.i("gchk", "curr_index = " + curr_index + " |||totle_number =" + totle_number);
				String[] s = content.split("\\|");
				PersonalConfig config = Tools.getPersonalConfig();
				PersonalGoal Goal = Tools.getPersonalGoal();
				int goalSteps = Goal.mGoalSteps;
				int goalCals = Goal.mGoalCalories;

				if (curr_index == totle_number) {
					// 收到最后一条了
					// 最后一条是远端传过来的一个统计数据，包含他那边存储的所有天数的统计
					// 统计数据只有2个值 步数|日期 2835|20140529
					int number = s.length / 2;
					for (int i = 0; i < number; i++) {
						int steps = Integer.parseInt(s[i * 2 + 0]);
						int meter = Tools.calcDistance(steps, config.getHeight());
						int calories = Tools.calcCalories(meter, config.getWeight());

						RunningItem item = new RunningItem();
						item.setSteps(steps);
						item.setCalories(calories);
						item.setKilometer(meter);
						item.setDate(formatRemoteDate(s[i * 2 + 1]));
						item.setStartTime("");
						item.setEndTime("");
						item.setDuration("");
						item.setSportsType(0);
						item.setmType(0);
						if (steps >= goalSteps)
							item.setisComplete(1);
						else
							item.setisComplete(0);
						item.setisStatistics(true);
						Log.i("gchk", item.toString());
						mTempLists.add(item);
					}
					mHandler.removeMessages(HANDLE_MSG_CANCEL_MSG);
					Message msg = new Message();
					msg.what = 1024;
					mHandler.sendMessage(msg);
					
					sendSyncRunningDataCallBack();
				} else {
					// 2835|20140529|0906|0935|
					// 步数|日期(年-月-日)|起始时间|结束时间(时-分-24小时制)
					int number = s.length / 4;
					for (int i = 0; i < number; i++) {
						int steps = Integer.parseInt(s[i * 4 + 0]);
						int meter = Tools.calcDistance(steps, config.getHeight());
						int calories = Tools.calcCalories(meter, config.getWeight());
						RunningItem item = new RunningItem();
						item.setSteps(steps);
						item.setCalories(calories);
						item.setKilometer(meter);
						item.setDate(formatRemoteDate(s[i * 4 + 1]));
						item.setStartTime(formatRemoteTime(s[i * 4 + 2]));
						item.setEndTime(formatRemoteTime(s[i * 4 + 3]));
						item.setDuration(getDuration(s[i * 4 + 2], s[i * 4 + 3]));
						item.setSportsType(0);
						item.setmType(2);
						item.setisComplete(0);
						item.setisStatistics(false);
						Log.i("gchk", item.toString());
						mTempLists.add(item);

					}
					mHandler.removeMessages(HANDLE_MSG_CANCEL_MSG);
					Message msg = new Message();
					msg.what = HANDLE_MSG_CANCEL_MSG;
					mHandler.sendMessageDelayed(msg, 10000);
				}
			}
		}
	};

	private BroadcastReceiver mSyncPersionalReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, Intent intent) {
			if (intent.getAction().equals("com.zhuoyou.plugin.running.sync.personal")) {
				char[] c_tag = intent.getCharArrayExtra("tag");
				String content = intent.getStringExtra("content");
				Log.i("gchk", "mSyncPersionalReceiver 0X73 TAG[0] =" + c_tag[0] + "TAG[1]= " + c_tag[1] + "||| c= " + content);
				int curr_index = c_tag[0] - 0x20;
				int totle_number = c_tag[1] - 0x20;
				Log.i("gchk", "curr_index = " + curr_index + " |||totle_number =" + totle_number);
				String[] s = content.split("\\|");
				
				if(s.length<4){
					char[] tag = new char[4];
					tag[0] = 0x21;
					tag[1] = 0xff;
					tag[2] = 0xff;
					tag[3] = 0xff;
					Intent intent1 = new Intent("com.tyd.plugin.receiver.sendmsg");
					intent1.putExtra("plugin_cmd", 0x75);
					intent1.putExtra("plugin_content", "");
					intent1.putExtra("plugin_tag", tag);
					sendBroadcast(intent1);
					return;
				}
				final PersonalConfig config = new PersonalConfig();
				final PersonalGoal goal = new PersonalGoal();
				int sex = Integer.parseInt(s[0]);
				if (sex == 0) {
					config.setSex(PersonalConfig.SEX_WOMAN);
				} else if (sex == 1) {
					config.setSex(PersonalConfig.SEX_MAN);
				}
				int year = Calendar.getInstance().get(Calendar.YEAR) - Integer.parseInt(s[3]);
				config.setYear(year);

				int h = Integer.parseInt(s[1]);
				config.setHeight(h);

				int w = Integer.parseInt(s[2]);
				config.setWeight(w);
				
				if (s.length > 4 && !s[4].equals("$")) {
					int step = Integer.parseInt(s[4]);
					goal.setStep(step);
				}
				
				final PersonalConfig config2 = Tools.getPersonalConfig();
				final PersonalGoal goal2 = Tools.getPersonalGoal();
				if (s.length < 5 || s[4].equals("$")) {
					if (config.isEquals(config2)) {
						sendSyncPeronalConfigCallBack();
						syncRunningData();
					} else {
						// 启动对话框让用户选择是否
						CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(context);
						builder.setTitle(R.string.syncs_persion_setting);
						builder.setMessage(R.string.syncs_persion_setting_message);
						builder.setPositiveButton(R.string.upload, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// 如果用户选择上传，则发送给远端
								String s = config2.toString() + "|$|$|";
								Log.i("gchk", "send to remote personal:" + s);
								Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
								intent.putExtra("plugin_cmd", 0x74);
								intent.putExtra("plugin_content", s);
								context.sendBroadcast(intent);
								dialog.dismiss();
								syncRunningData();
							}
						});
						builder.setNegativeButton(R.string.download, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								sendSyncPeronalConfigCallBack();
								// 如果用户选择下载，则直接覆盖本地数据
								if (config.getHeight() != 0 && config.getWeight() != 0) {
									Tools.updatePersonalConfig(config);
								}
								dialog.dismiss();
								syncRunningData();
							}
						});
						builder.setCancelable(false);
						CustomAlertDialog dialog = builder.create();
						dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
						dialog.show();
					}
				} else {
					if (config.isEquals(config2) && goal.getStep() == goal2.getStep()) {
						sendSyncPeronalConfigCallBack();
						syncRunningData();
					} else {
						// 启动对话框让用户选择是否
						CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(context);
						builder.setTitle(R.string.syncs_persion_setting);
						builder.setMessage(R.string.syncs_persion_setting_message);
						builder.setPositiveButton(R.string.upload, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// 如果用户选择上传，则发送给远端
								String s = config2.toString() + goal2.toString();
								Log.i("gchk", "send to remote personal:" + s);
								Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
								intent.putExtra("plugin_cmd", 0x74);
								intent.putExtra("plugin_content", s);
								context.sendBroadcast(intent);
								dialog.dismiss();
								syncRunningData();
							}
						});
						builder.setNegativeButton(R.string.download, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								sendSyncPeronalConfigCallBack();
								// 如果用户选择下载，则直接覆盖本地数据
								if (config.getHeight() != 0 && config.getWeight() != 0) {
									Tools.updatePersonalConfig(config);
								}
								Tools.updatePersonalGoalStep(goal);
								dialog.dismiss();
								syncRunningData();
							}
						});
						builder.setCancelable(false);
						CustomAlertDialog dialog = builder.create();
						dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
						dialog.show();
					}
				}
			}
		}
	};

	public void sendSyncPeronalConfigCallBack(){
		Intent intent1 = new Intent("com.tyd.plugin.receiver.sendmsg");
		intent1.putExtra("plugin_cmd", 0x75);
		intent1.putExtra("plugin_content", "");
		sendBroadcast(intent1);
	}
	
	public void sendSyncRunningDataCallBack(){
		Intent intent1 = new Intent("com.tyd.plugin.receiver.sendmsg");
		intent1.putExtra("plugin_cmd", 0x72);
		intent1.putExtra("plugin_content", "");
		sendBroadcast(intent1);
	}
	
	// 第一次同步完成个人设置之后，紧跟着直接同步记步数据
	public void syncRunningData() {
		if (!syncnow) {
			if (mTempLists.size() > 0) {
				mTempLists.clear();
			}
			mDialog.show();
			Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
			intent.putExtra("plugin_cmd", 0x70);
			intent.putExtra("plugin_content", "himan");
			sendBroadcast(intent);
			syncnow = true;
			Message msg = new Message();
			msg.what = HANDLE_MSG_CANCEL_MSG;
			mHandler.sendMessageDelayed(msg, 10000);
		}
	}

	private String getDuration(String start, String end) {
		String sh = start.substring(0, 2);
		String sm = start.substring(2, 4);
		String eh = end.substring(0, 2);
		String em = end.substring(2, 4);
		Date d1 = new Date();
		d1.setHours(Integer.parseInt(sh));
		d1.setMinutes(Integer.parseInt(sm));
		Date d2 = new Date();
		d2.setHours(Integer.parseInt(eh));
		d2.setMinutes(Integer.parseInt(em));
		long misc = d2.getTime() - d1.getTime();
		long min = misc / 1000 / 60;
		Log.i("gchk", min + "");
		return min + "";
	}

	private String formatRemoteTime(String old_time) {
		String new_time = old_time.substring(0, 2);
		new_time += ":";
		new_time += old_time.substring(2, 4);
		return new_time;
	}

	private String formatRemoteDate(String old_date) {
		String new_date = old_date.substring(0, 4);
		new_date += "-";
		new_date += old_date.substring(4, 6);
		new_date += "-";
		new_date += old_date.substring(6, 8);
		return new_date;
	}

	private void insertToDatabase() {
		try {
			ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
			Log.i("gchk", "mTempLists size = " + mTempLists.size() + System.currentTimeMillis() + "mTempLists = " + mTempLists);
			for (int i = 0; i < mTempLists.size(); i++) {
				RunningItem item = mTempLists.get(i);
				// 如果不是统计数据，则肯定要插入
				if (item.getIsStatistics()) {
					String day = item.getDate();
					int steps = item.getSteps();
					// 检查数据库中是否存在该天的数据
					Cursor c = mContentResolver.query(DataBaseContants.CONTENT_URI, new String[] { DataBaseContants.ID, DataBaseContants.STEPS }, 
							DataBaseContants.DATE + "  = ? AND " + DataBaseContants.STATISTICS + " = ? ", new String[] { day, "1" }, null);
					if (c.getCount() > 0 && c.moveToFirst() && c.getLong(c.getColumnIndex(DataBaseContants.ID)) > 0) {
						if (steps == c.getInt(c.getColumnIndex(DataBaseContants.STEPS))) {
							ContentProviderOperation op1 = ContentProviderOperation.newUpdate(DataBaseContants.CONTENT_URI)
									.withSelection(DataBaseContants.DATE + " = ? AND " + DataBaseContants.STATISTICS + " = ? ", new String[] { day, "1" })
									.withValues(item.toContentValues())
									.withYieldAllowed(true).build();
							operations.add(op1);
						} else {
							ContentProviderOperation op1 = ContentProviderOperation.newUpdate(DataBaseContants.CONTENT_URI)
									.withSelection(DataBaseContants.DATE + " = ? AND " + DataBaseContants.STATISTICS + " = ? ", new String[] { day, "1" })
									.withValues(item.toContentValues())
									.withValue(DataBaseContants.SYNC_STATE, 2)
									.withYieldAllowed(true).build();
							operations.add(op1);
						}
					} else {
						ContentProviderOperation op1 = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI)
								.withValues(item.toContentValues())
								.withValue(DataBaseContants.ID, Tools.getPKL())
								.withYieldAllowed(true).build();
						operations.add(op1);
					}
					c.close();
					c = null;
				} else {
					String day = item.getDate();
					String start = item.getStartTime();
					int steps = item.getSteps();
					Cursor c = mContentResolver.query(DataBaseContants.CONTENT_URI, new String[] { DataBaseContants.ID, DataBaseContants.STEPS }, DataBaseContants.DATE
							+ "  = ? AND " + DataBaseContants.TIME_START + " = ? AND " + DataBaseContants.SPORTS_TYPE + " = ? AND " + DataBaseContants.STATISTICS + " = ? ", new String[] { day, start, "0", "0" }, null);
					if (c.getCount() > 0 && c.moveToFirst() && c.getLong(c.getColumnIndex(DataBaseContants.ID)) > 0){
						if (steps == c.getInt(c.getColumnIndex(DataBaseContants.STEPS))) {
							ContentProviderOperation op1 = ContentProviderOperation.newUpdate(DataBaseContants.CONTENT_URI)
									.withSelection(DataBaseContants.DATE + " = ? AND " + DataBaseContants.TIME_START + " = ? AND " + DataBaseContants.SPORTS_TYPE + " = ? AND " + DataBaseContants.STATISTICS + " = ? ", new String[] { day, start, "0", "0" })
									.withValues(item.toContentValues())
									.withYieldAllowed(true).build();
							operations.add(op1);
						} else {
							ContentProviderOperation op1 = ContentProviderOperation.newUpdate(DataBaseContants.CONTENT_URI)
									.withSelection(DataBaseContants.DATE + " = ? AND " + DataBaseContants.TIME_START + " = ? AND " + DataBaseContants.SPORTS_TYPE + " = ? AND " + DataBaseContants.STATISTICS + " = ? ", new String[] { day, start, "0", "0" })
									.withValues(item.toContentValues())
									.withValue(DataBaseContants.SYNC_STATE, 2)
									.withYieldAllowed(true).build();
							operations.add(op1);
						}
					} else {
						ContentProviderOperation op1 = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI)
								.withValues(item.toContentValues())
								.withValue(DataBaseContants.ID, Tools.getPKL())
								.withYieldAllowed(true).build();
						operations.add(op1);
					}
					c.close();
					c = null;
				}
			}
			mContentResolver.applyBatch(DataBaseContants.AUTHORITY, operations);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
		Toast.makeText(sContext, R.string.get_complete, Toast.LENGTH_SHORT).show();
	}
}
