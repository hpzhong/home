package com.zhuoyou.plugin.running;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

import com.zhuoyou.plugin.ble.BleManagerService;
import com.zhuoyou.plugin.cloud.AlarmUtils;
import com.zhuoyou.plugin.cloud.CloudSync;
import com.zhuoyou.plugin.custom.CustomAlertDialog;
import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.database.DataBaseUtil;
import com.zhuoyou.plugin.mainFrame.MineFragment;
import com.zhuoyou.plugin.resideMenu.EquipManagerListActivity;

public class MainService extends Service {
	private static MainService mInstance;
	private static final Context sContext = RunningApp.getInstance().getApplicationContext();
	private UpdateHandler mHandler = new UpdateHandler(this);
	private List<RunningItem> mTempLists = new ArrayList<RunningItem>();
	private static final int HANDLE_MSG_CANCEL_MSG = 1008;
	private static final int HANDLE_MSG_STORE_MSG = 1024;
	private static final int HANDLE_MSG_FINISH_MSG = 1111;
	private static final int HANDLE_MSG_NO_SLEEP_MSG = 1112;
	private ContentResolver mContentResolver;
	public static Boolean syncnow = false;

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
						Log.i("1111", "HANDLE_MSG_CANCEL_MSG");
						Toast.makeText(home, R.string.connect_time_out, Toast.LENGTH_SHORT).show();
						syncnow = false;
				  		
						if (HomePageItemFragment.mHandler != null) {
							Message message = new Message();
							message.what = HomePageItemFragment.SYNC_DEVICE_FAILED;
							HomePageItemFragment.mHandler.sendMessage(message);
						}
						if (HomePageFragment.mHandler != null) {
							Message message = new Message();
							message.what = HomePageFragment.SYNC_DEVICE_FAILED;
							message.arg1 = 1;
							HomePageFragment.mHandler.sendMessage(message);
						}
						if (SleepPageItemFragment.mHandler != null) {
							Message message = new Message();
							message.what = SleepPageItemFragment.SYNC_DEVICE_FAILED;
							SleepPageItemFragment.mHandler.sendMessage(message);
						}
						break;
					case HANDLE_MSG_STORE_MSG:
						Log.i("1111", "HANDLE_MSG_STORE_MSG");
						home.insertToDatabase();
						home.syncSleepData();
						break;
					case HANDLE_MSG_NO_SLEEP_MSG:
						Log.i("1111", "HANDLE_MSG_NO_SLEEP_MSG");
						syncnow = false;
						Toast.makeText(sContext, R.string.get_complete, Toast.LENGTH_SHORT).show();
						if (HomePageItemFragment.mHandler != null) {
							Message message = new Message();
							message.what = HomePageItemFragment.SYNC_DEVICE_SUCCESSED;
							HomePageItemFragment.mHandler.sendMessage(message);
						}
						if (HomePageFragment.mHandler != null) {
							Message message = new Message();
							message.what = HomePageFragment.SYNC_DEVICE_SUCCESSED;
							message.arg1 = 1;
							HomePageFragment.mHandler.sendMessage(message);
						}
						if (SleepPageItemFragment.mHandler != null) {
							Message message = new Message();
							message.what = SleepPageItemFragment.SYNC_DEVICE_SUCCESSED;
							SleepPageItemFragment.mHandler.sendMessage(message);
						}
						
						break;  
					case HANDLE_MSG_FINISH_MSG:
						Log.i("1111", "HANDLE_MSG_FINISH_MSG");
						syncnow = false;
						Toast.makeText(sContext, R.string.get_complete, Toast.LENGTH_SHORT).show();
						if (HomePageItemFragment.mHandler != null) {
							Message message = new Message();
							message.what = HomePageItemFragment.SYNC_DEVICE_SUCCESSED;
							HomePageItemFragment.mHandler.sendMessage(message);
						}
						if (HomePageFragment.mHandler != null) {
							Message message = new Message();
							message.what = HomePageFragment.SYNC_DEVICE_SUCCESSED;
							message.arg1 = 1;
							HomePageFragment.mHandler.sendMessage(message);
						}
						if (SleepPageItemFragment.mHandler != null) {
							Message message = new Message();
							message.what = SleepPageItemFragment.SYNC_DEVICE_SUCCESSED;
							SleepPageItemFragment.mHandler.sendMessage(message);
						}
						  
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
		intentF1.addAction("com.zhuoyou.plugin.running.get.gatt");
		intentF1.addAction("com.zhuoyou.plugin.running.sleep");
		registerReceiver(mGetDataReceiver, intentF1);
		
		IntentFilter intentF2 = new IntentFilter();
		intentF2.addAction("com.tyd.bt.device.battery");
		registerReceiver(mGetBatteryReceiver, intentF2);

		IntentFilter intentF3 = new IntentFilter();
		intentF3.addAction("ACTION_GPS_INFO");
		intentF3.addAction("ACTION_PHONE_STEPS");
		intentF3.addAction("ACTION_PHONE_TOTAL_STEPS");
		registerReceiver(mGetGPSDateReceiver, intentF3);
	}

	private void unRegisterBc() {
		unregisterReceiver(mSyncPersionalReceiver);
		unregisterReceiver(mGetDataReceiver);
		unregisterReceiver(mGetBatteryReceiver);
		unregisterReceiver(mGetGPSDateReceiver);
	}

	private BroadcastReceiver mGetGPSDateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("ACTION_GPS_INFO")) {
				RunningItem gps_info = (RunningItem) intent.getSerializableExtra("gps_info");
//				String day = gps_info.getDate();
//				int step = gps_info.getSteps();
//				int cal = gps_info.getCalories();
//				int meter = gps_info.getMeter();
				try {
					ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
					ContentProviderOperation op1 = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI)
							.withValues(gps_info.toContentValues())
							.withValue(DataBaseContants.ID, Tools.getPKL())
							.withValue(DataBaseContants.CONF_WEIGHT, gps_info.getmWeight())
							.withValue(DataBaseContants.BMI, gps_info.getmBmi())
							.withValue(DataBaseContants.IMG_URI, gps_info.getmImgUri())
							.withValue(DataBaseContants.EXPLAIN, gps_info.getmExplain())
							.withValue(DataBaseContants.TYPE, 5)
							.withValue(DataBaseContants.STATISTICS, 0)
							.withValue(DataBaseContants.DATA_FROM, "GPS")
							.withYieldAllowed(true).build();
					operations.add(op1);
					mContentResolver.applyBatch(DataBaseContants.AUTHORITY, operations);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OperationApplicationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (intent.getAction().endsWith("ACTION_PHONE_STEPS")) {
				Log.i("111", "save 5555555555555");
				RunningItem item = (RunningItem) intent.getSerializableExtra("phone_steps");
				int total_step = intent.getIntExtra("total_step", 0);
				PersonalConfig config = Tools.getPersonalConfig();
				int meter = Tools.calcDistance(total_step, config.getHeight());
				int calories = Tools.calcCalories(meter, config.getWeightNum());
				String day = item.getDate();
				try {
					ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
					ContentProviderOperation op1 = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI)
							.withValues(item.toContentValues())
							.withValue(DataBaseContants.ID, Tools.getPKL())
							.withValue(DataBaseContants.DATA_FROM, "phone")
							.withYieldAllowed(true).build();
					operations.add(op1);
					/*Cursor c = mContentResolver.query(DataBaseContants.CONTENT_URI, new String[] { DataBaseContants.ID, DataBaseContants.STEPS, DataBaseContants.KILOMETER, DataBaseContants.CALORIES, DataBaseContants.SYNC_STATE }, 
					DataBaseContants.DATE + "  = ? AND " + DataBaseContants.STATISTICS + " = ? AND " + DataBaseContants.DATA_FROM + " = ? ", new String[] { day, "2", "phone" }, null);
					if (c.getCount() > 0 && c.moveToFirst() && c.getLong(c.getColumnIndex(DataBaseContants.ID)) > 0) {
						int sync = c.getInt(c.getColumnIndex(DataBaseContants.SYNC_STATE));
						ContentProviderOperation op2;
						if (sync == 0) {
							op2 = ContentProviderOperation.newUpdate(DataBaseContants.CONTENT_URI)
									.withSelection(DataBaseContants.DATE + " = ? AND " + DataBaseContants.STATISTICS + " = ? AND " + DataBaseContants.DATA_FROM + " = ? ", new String[] { day, "2", "phone" })
									.withValue(DataBaseContants.STEPS, total_step)
									.withValue(DataBaseContants.CALORIES, calories)
									.withValue(DataBaseContants.KILOMETER, meter)
									.withYieldAllowed(true).build();
						} else {
							op2 = ContentProviderOperation.newUpdate(DataBaseContants.CONTENT_URI)
									.withSelection(DataBaseContants.DATE + " = ? AND " + DataBaseContants.STATISTICS + " = ? AND " + DataBaseContants.DATA_FROM + " = ? ", new String[] { day, "2", "phone" })
									.withValue(DataBaseContants.STEPS, total_step)
									.withValue(DataBaseContants.CALORIES, calories)
									.withValue(DataBaseContants.KILOMETER, meter)
									.withValue(DataBaseContants.SYNC_STATE, 2)
									.withYieldAllowed(true).build();
						}
						operations.add(op2);
					} else {
						ContentProviderOperation op2 = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI)
								.withValue(DataBaseContants.ID, Tools.getPKL())
								.withValue(DataBaseContants.DATE, day)
								.withValue(DataBaseContants.STEPS, total_step)
								.withValue(DataBaseContants.CALORIES, calories)
								.withValue(DataBaseContants.KILOMETER, meter)
								.withValue(DataBaseContants.TYPE, 6)
								.withValue(DataBaseContants.STATISTICS, 2)
								.withValue(DataBaseContants.DATA_FROM, "phone")
								.withYieldAllowed(true).build();
						operations.add(op2);
					}
					c.close();
					c = null;*/
					mContentResolver.applyBatch(DataBaseContants.AUTHORITY, operations);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OperationApplicationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else if (intent.getAction().endsWith("ACTION_PHONE_TOTAL_STEPS")) {
				int total_step = intent.getIntExtra("total_step", 0);
				PersonalConfig config = Tools.getPersonalConfig();
				int meter = Tools.calcDistance(total_step, config.getHeight());
				int calories = Tools.calcCalories(meter, config.getWeightNum());
				String day = Tools.getDate(0);
				try {
					ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
					Cursor c = mContentResolver.query(DataBaseContants.CONTENT_URI, new String[] { DataBaseContants.ID, DataBaseContants.STEPS, DataBaseContants.KILOMETER, DataBaseContants.CALORIES, DataBaseContants.SYNC_STATE }, 
					DataBaseContants.DATE + "  = ? AND " + DataBaseContants.STATISTICS + " = ? AND " + DataBaseContants.DATA_FROM + " = ? ", new String[] { day, "2", "phone" }, null);
					if (c.getCount() > 0 && c.moveToFirst() && c.getLong(c.getColumnIndex(DataBaseContants.ID)) > 0) {
						int sync = c.getInt(c.getColumnIndex(DataBaseContants.SYNC_STATE));
						ContentProviderOperation op2;
						if (sync == 0) {
							op2 = ContentProviderOperation.newUpdate(DataBaseContants.CONTENT_URI)
									.withSelection(DataBaseContants.DATE + " = ? AND " + DataBaseContants.STATISTICS + " = ? AND " + DataBaseContants.DATA_FROM + " = ? ", new String[] { day, "2", "phone" })
									.withValue(DataBaseContants.STEPS, total_step)
									.withValue(DataBaseContants.CALORIES, calories)
									.withValue(DataBaseContants.KILOMETER, meter)
									.withYieldAllowed(true).build();
						} else {
							op2 = ContentProviderOperation.newUpdate(DataBaseContants.CONTENT_URI)
									.withSelection(DataBaseContants.DATE + " = ? AND " + DataBaseContants.STATISTICS + " = ? AND " + DataBaseContants.DATA_FROM + " = ? ", new String[] { day, "2", "phone" })
									.withValue(DataBaseContants.STEPS, total_step)
									.withValue(DataBaseContants.CALORIES, calories)
									.withValue(DataBaseContants.KILOMETER, meter)
									.withValue(DataBaseContants.SYNC_STATE, 2)
									.withYieldAllowed(true).build();
						}
						operations.add(op2);
					} else {
						ContentProviderOperation op2 = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI)
								.withValue(DataBaseContants.ID, Tools.getPKL())
								.withValue(DataBaseContants.DATE, day)
								.withValue(DataBaseContants.STEPS, total_step)
								.withValue(DataBaseContants.CALORIES, calories)
								.withValue(DataBaseContants.KILOMETER, meter)
								.withValue(DataBaseContants.TYPE, 6)
								.withValue(DataBaseContants.STATISTICS, 2)
								.withValue(DataBaseContants.DATA_FROM, "phone")
								.withYieldAllowed(true).build();
						operations.add(op2);
					}
					c.close();
					c = null;
					mContentResolver.applyBatch(DataBaseContants.AUTHORITY, operations);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OperationApplicationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			checkDataBase();
		}
	};
	
	private BroadcastReceiver mGetBatteryReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			char[] c_tag = intent.getCharArrayExtra("tag");
			if (MineFragment.mHandler != null) {
				Message msg = new Message();
				msg.what = MineFragment.BATTERY;
				msg.arg1 = c_tag[0] - 0x20;
				msg.arg2 = c_tag[1];
				MineFragment.mHandler.sendMessage(msg);
			}
			if (EquipManagerListActivity.mHandler != null) {
				Message msg = new Message();
				msg.what = EquipManagerListActivity.BATTERY;
				msg.arg1 = c_tag[0] - 0x20;
				msg.arg2 = c_tag[1];
				EquipManagerListActivity.mHandler.sendMessage(msg);
			}
		}
		
	};
	
	private BroadcastReceiver mGetDataReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (CloudSync.isSync)
				return;
			if (intent.getAction().equals("com.zhuoyou.plugin.running.get")) {
				char[] c_tag = intent.getCharArrayExtra("tag");
				String content = intent.getStringExtra("content");
				String data_from = intent.getStringExtra("from");
				if (data_from == null || data_from.equals(""))
					data_from = "phone";
				Log.i("gchk", "mGetDataReceiver 0X71 TAG[0] =" + c_tag[0] + "TAG[1]= " + c_tag[1] + "||| c= " + content + "||| from= " + data_from);
				int curr_index = c_tag[0] - 0x20;
				int totle_number = c_tag[1] - 0x20;
				Log.i("gchk", "curr_index = " + curr_index + " |||totle_number =" + totle_number);
				String[] s = content.split("\\|");
				PersonalConfig config = Tools.getPersonalConfig();
				PersonalGoal Goal = Tools.getPersonalGoal();

				if (curr_index == totle_number) {
					// 收到最后一条了
					// 最后一条是远端传过来的一个统计数据，包含他那边存储的所有天数的统计
					// 统计数据只有2个值 步数|日期 2835|20140529
					int number = s.length / 2;
					for (int i = 0; i < number; i++) {
						int steps = Integer.parseInt(s[i * 2 + 0]);
						int meter = Tools.calcDistance(steps, config.getHeight());
						int calories = Tools.calcCalories(meter, config.getWeightNum());

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
						item.setisComplete(0);
						item.setisStatistics(2);
						item.setDataFrom(data_from);
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
						int calories = Tools.calcCalories(meter, config.getWeightNum());
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
						item.setisStatistics(0);
						item.setDataFrom(data_from);
						Log.i("gchk", item.toString());
						mTempLists.add(item);

					}
					mHandler.removeMessages(HANDLE_MSG_CANCEL_MSG);
					Message msg = new Message();
					msg.what = HANDLE_MSG_CANCEL_MSG;
					mHandler.sendMessageDelayed(msg, 10000);
				}
			} else if (intent.getAction().equals("com.zhuoyou.plugin.running.get.gatt")) {
				int statistics = intent.getIntExtra("statistics", -1);
				String data_from = intent.getStringExtra("from");
				PersonalConfig config = Tools.getPersonalConfig();
				if (statistics == 2) {
					// 统计数据只有2个值 步数|日期 2835|2014-05-29
					String content = intent.getStringExtra("content");
					String[] s = content.split("\\|");
					int number = s.length / 2;
					for (int i = 0; i < number; i++) {
						int steps = Integer.parseInt(s[i * 2 + 0]);
						int meter = Tools.calcDistance(steps, config.getHeight());
						int calories = Tools.calcCalories(meter, config.getWeightNum());

						RunningItem item = new RunningItem();
						item.setSteps(steps);
						item.setCalories(calories);
						item.setKilometer(meter);
						item.setDate(s[i * 2 + 1]);
						item.setStartTime("");
						item.setEndTime("");
						item.setDuration("");
						item.setSportsType(0);
						item.setmType(0);
						item.setisComplete(0);
						item.setisStatistics(statistics);
						item.setDataFrom(data_from);
						Log.i("gchk", item.toString());
						mTempLists.add(item);
					}
					mHandler.removeMessages(HANDLE_MSG_CANCEL_MSG);
					insertToDatabase();
					syncnow = false;
					Toast.makeText(sContext, R.string.get_complete, Toast.LENGTH_SHORT).show();
					if (HomePageItemFragment.mHandler != null) {
						Message message = new Message();
						message.what = HomePageItemFragment.SYNC_DEVICE_SUCCESSED;
						HomePageItemFragment.mHandler.sendMessage(message);
					}
					if (HomePageFragment.mHandler != null) {
						Message message = new Message();
						message.what = HomePageFragment.SYNC_DEVICE_SUCCESSED;
						message.arg1 = 1;
						HomePageFragment.mHandler.sendMessage(message);
					}
					if (SleepPageItemFragment.mHandler != null) {
						Message message = new Message();
						message.what = SleepPageItemFragment.SYNC_DEVICE_SUCCESSED;
						SleepPageItemFragment.mHandler.sendMessage(message);
					}
					
				} else if (statistics == 0) {
					// 分段数据
					int steps = intent.getIntExtra("step", -1);
					String date = intent.getStringExtra("date");
					String startTime = intent.getStringExtra("start");
					String endTime = intent.getStringExtra("end");
					int meter = Tools.calcDistance(steps, config.getHeight());
					int calories = Tools.calcCalories(meter, config.getWeightNum());
					RunningItem item = new RunningItem();
					item.setSteps(steps);
					item.setCalories(calories);
					item.setKilometer(meter);
					item.setDate(date);
					item.setStartTime(formatRemoteTime(startTime));
					item.setEndTime(formatRemoteTime(endTime));
					item.setDuration(getDuration(startTime, endTime));
					item.setSportsType(0);
					item.setmType(2);
					item.setisComplete(0);
					item.setisStatistics(statistics);
					item.setDataFrom(data_from);
					Log.i("gchk", item.toString());
					mTempLists.add(item);
					mHandler.removeMessages(HANDLE_MSG_CANCEL_MSG);
					Message msg = new Message();
					msg.what = HANDLE_MSG_CANCEL_MSG;
					mHandler.sendMessageDelayed(msg, 10000);
				}
			} else if (intent.getAction().equals("com.zhuoyou.plugin.running.sleep")) {
				char[] c_tag = intent.getCharArrayExtra("tag");
				String content = intent.getStringExtra("content");
				Log.i("caixinxin", "get sleep data 0X81 TAG[0] =" + c_tag[0] + "TAG[1]= " + c_tag[1] + "||| c= " + content);
				int curr_index = c_tag[0] - 0x20;
				int totle_number = c_tag[1] - 0x20;
				Log.i("caixinxin", "curr_index = " + curr_index + " |||totle_number =" + totle_number);
				String[] s = content.split("\\|");
				if (totle_number > 0) {
					String date = formatRemoteDate(s[0]);
					if (Integer.parseInt(s[2]) >= 2100) {
						date = Tools.getDate(date, -1);
					}
					String details = content.substring(s[0].length() + 1);
					DataBaseUtil.insertClassicSleep(sContext, date, details);
					mHandler.removeMessages(HANDLE_MSG_NO_SLEEP_MSG);
					Message msg = new Message();
					msg.what = HANDLE_MSG_NO_SLEEP_MSG;
					mHandler.sendMessageDelayed(msg, 10000);
				}
				if (totle_number == curr_index) {
					mHandler.removeMessages(HANDLE_MSG_NO_SLEEP_MSG);
					Message msg = new Message();
					msg.what = HANDLE_MSG_FINISH_MSG;
					mHandler.sendMessage(msg);
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
					intent1.putExtra("plugin_content", "himan");
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

				String w = s[2] + ".0";
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
								if (config.getHeight() != 0 && config.getWeightNum() != 0) {
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
								if (config.getHeight() != 0 && config.getWeightNum() != 0) {
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
				mHandler.removeMessages(HANDLE_MSG_CANCEL_MSG);
			}
		}
	};

	public void sendSyncPeronalConfigCallBack(){
		Intent intent1 = new Intent("com.tyd.plugin.receiver.sendmsg");
		intent1.putExtra("plugin_cmd", 0x75);
		intent1.putExtra("plugin_content", "himan");
		sendBroadcast(intent1);
	}
	
	public void sendSyncRunningDataCallBack(){
		Intent intent1 = new Intent("com.tyd.plugin.receiver.sendmsg");
		intent1.putExtra("plugin_cmd", 0x72);
		intent1.putExtra("plugin_content", "himan");
		sendBroadcast(intent1);
	}
	
	public void syncWithDevice() {
		syncnow = true;
		if (mTempLists.size() > 0) {
			mTempLists.clear();
		}
		if (RunningApp.isBLESupport && BleManagerService.getInstance().GetBleConnectState()) {
//			Intent intent = new Intent(BleManagerService.ACTION_STEP_DATA_READ);
//			Intent intent = new Intent(BleManagerService.ACTION_STEP_TOTAL_DATA);
			Intent intent = new Intent(BleManagerService.ACTION_STEP_DATA_READ);
			sendBroadcast(intent);
		} else {
			Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
			intent.putExtra("plugin_cmd", 0x73);
			intent.putExtra("plugin_content", "himan");
			sendBroadcast(intent);
		}
		Message msg = new Message();
		msg.what = HANDLE_MSG_CANCEL_MSG;
		mHandler.sendMessageDelayed(msg, 10000);
	}
	
	// 第一次同步完成个人设置之后，紧跟着直接同步记步数据
	public void syncRunningData() {
		Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
		Log.i("sleep", "send 0x70 running to BlueTooth");
		intent.putExtra("plugin_cmd", 0x70);
		intent.putExtra("plugin_content", "himan");
		sendBroadcast(intent);
		Message msg = new Message();
		msg.what = HANDLE_MSG_CANCEL_MSG;
		mHandler.sendMessageDelayed(msg, 10000);
		//syncSleepData();
	}
	
	public void syncSleepData(){
		Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
		intent.putExtra("plugin_cmd", 0x80);
		intent.putExtra("plugin_content", "himan");
		sendBroadcast(intent);
		Message msg = new Message();
		msg.what = HANDLE_MSG_NO_SLEEP_MSG;
		mHandler.sendMessageDelayed(msg, 2000);
		Log.i("sleep", "send 0x80 sleep to BlueTooth");
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
				String data_from = item.getDataFrom();
				// 某个设备的统计数据
				if (item.getIsStatistics() == 2) {
					String day = item.getDate();
					int steps = item.getSteps();
					// 检查数据库中是否存在该天相同设备的数据
					Cursor c = mContentResolver.query(DataBaseContants.CONTENT_URI, new String[] { DataBaseContants.ID, DataBaseContants.STEPS, DataBaseContants.SYNC_STATE }, 
							DataBaseContants.DATE + "  = ? AND " + DataBaseContants.STATISTICS + " = ? AND " + DataBaseContants.DATA_FROM + " = ? ", new String[] { day, "2", data_from }, null);
					if (c.getCount() > 0 && c.moveToFirst() && c.getLong(c.getColumnIndex(DataBaseContants.ID)) > 0) {
						int sync = c.getInt(c.getColumnIndex(DataBaseContants.SYNC_STATE));
						if (steps == c.getInt(c.getColumnIndex(DataBaseContants.STEPS)) && sync == 1) {
							ContentProviderOperation op1 = ContentProviderOperation.newUpdate(DataBaseContants.CONTENT_URI)
									.withSelection(DataBaseContants.DATE + " = ? AND " + DataBaseContants.STATISTICS + " = ? AND " + DataBaseContants.DATA_FROM + " = ? ", new String[] { day, "2", data_from })
									.withValues(item.toContentValues())
									.withYieldAllowed(true).build();
							operations.add(op1);
						} else {
							ContentProviderOperation op1;
							if (sync == 0) {
								op1 = ContentProviderOperation.newUpdate(DataBaseContants.CONTENT_URI)
										.withSelection(DataBaseContants.DATE + " = ? AND " + DataBaseContants.STATISTICS + " = ? AND " + DataBaseContants.DATA_FROM + " = ? ", new String[] { day, "2", data_from })
										.withValues(item.toContentValues())
										.withValue(DataBaseContants.SYNC_STATE, 0)
										.withYieldAllowed(true).build();
							} else {
								op1 = ContentProviderOperation.newUpdate(DataBaseContants.CONTENT_URI)
										.withSelection(DataBaseContants.DATE + " = ? AND " + DataBaseContants.STATISTICS + " = ? AND " + DataBaseContants.DATA_FROM + " = ? ", new String[] { day, "2", data_from })
										.withValues(item.toContentValues())
										.withValue(DataBaseContants.SYNC_STATE, 2)
										.withYieldAllowed(true).build();
							}
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
					Cursor c = mContentResolver.query(DataBaseContants.CONTENT_URI, new String[] { DataBaseContants.ID, DataBaseContants.STEPS, DataBaseContants.SYNC_STATE }, DataBaseContants.DATE
							+ "  = ? AND " + DataBaseContants.TIME_START + " = ? AND " + DataBaseContants.SPORTS_TYPE + " = ? AND " + DataBaseContants.STATISTICS + " = ? AND " + DataBaseContants.DATA_FROM + " = ? ", new String[] { day, start, "0", "0", data_from }, null);
					if (c.getCount() > 0 && c.moveToFirst() && c.getLong(c.getColumnIndex(DataBaseContants.ID)) > 0){
						int sync = c.getInt(c.getColumnIndex(DataBaseContants.SYNC_STATE));
						if (steps == c.getInt(c.getColumnIndex(DataBaseContants.STEPS)) && sync == 1) {
							ContentProviderOperation op1 = ContentProviderOperation.newUpdate(DataBaseContants.CONTENT_URI)
									.withSelection(DataBaseContants.DATE + " = ? AND " + DataBaseContants.TIME_START + " = ? AND " + DataBaseContants.SPORTS_TYPE + " = ? AND " + DataBaseContants.STATISTICS + " = ? AND " + DataBaseContants.DATA_FROM + " = ? ", new String[] { day, start, "0", "0", data_from })
									.withValues(item.toContentValues())
									.withYieldAllowed(true).build();
							operations.add(op1);
						} else {
							ContentProviderOperation op1;
							if (sync == 0) {
								op1 = ContentProviderOperation.newUpdate(DataBaseContants.CONTENT_URI)
										.withSelection(DataBaseContants.DATE + " = ? AND " + DataBaseContants.TIME_START + " = ? AND " + DataBaseContants.SPORTS_TYPE + " = ? AND " + DataBaseContants.STATISTICS + " = ? AND " + DataBaseContants.DATA_FROM + " = ? ", new String[] { day, start, "0", "0", data_from })
										.withValues(item.toContentValues())
										.withValue(DataBaseContants.SYNC_STATE, 0)
										.withYieldAllowed(true).build();
							} else {
								op1 = ContentProviderOperation.newUpdate(DataBaseContants.CONTENT_URI)
										.withSelection(DataBaseContants.DATE + " = ? AND " + DataBaseContants.TIME_START + " = ? AND " + DataBaseContants.SPORTS_TYPE + " = ? AND " + DataBaseContants.STATISTICS + " = ? AND " + DataBaseContants.DATA_FROM + " = ? ", new String[] { day, start, "0", "0", data_from })
										.withValues(item.toContentValues())
										.withValue(DataBaseContants.SYNC_STATE, 2)
										.withYieldAllowed(true).build();
							}
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

		checkDataBase();
	}
	
	public void checkDataBase() {
		try {
			ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
			PersonalConfig config = Tools.getPersonalConfig();
			PersonalGoal goal = Tools.getPersonalGoal();
			String today = Tools.getDate(0);
			String enter_day = today;
			List<String> date_list = Tools.getDateFromDb(sContext);
			if (date_list != null && date_list.size() > 0)
				enter_day = date_list.get(0);
			int count = Tools.getDayCount(enter_day, today, "yyyy-MM-dd");
			for (int i = 0; i < count; i++) {
				String day = Tools.getDate(enter_day, 0 - i);
				if (date_list != null && date_list.size() > 0 && date_list.indexOf(day) != -1) {
					Cursor c = mContentResolver.query(DataBaseContants.CONTENT_URI, new String[] {"_id", "steps", "sync", "statistics" },
							DataBaseContants.DATE + "  = ? ", new String[] { day }, null);
					if (c.getCount() > 0 && c.moveToFirst()) {
						int effective_step = 0;//所有分段数据中有效的步数
						int invalid_step = 0;//无效的步数
						int total_step = 0;//所有设备统计数据中的总步数
						int statistics_step = 0;//真正的统计总步数
						Boolean has_statistics = false;
						int sync = 0;
						int calories = 0;//真正的统计总卡路里
						for(int j = 0; j < c.getCount(); j++) {
							int step = c.getInt(c.getColumnIndex(DataBaseContants.STEPS));
							int data_statistics = c.getInt(c.getColumnIndex(DataBaseContants.STATISTICS));
							switch (data_statistics) {
							case 0:
								effective_step = effective_step + step;
								break;
							case 1:
								has_statistics = true;
								sync = c.getInt(c.getColumnIndex(DataBaseContants.SYNC_STATE));
								statistics_step = step;
								break;
							case 2:
								total_step = total_step + step;
								break;
							}
							c.moveToNext();
						}
						
						Log.i("caixinxin", "day = " + day);
						Log.i("caixinxin", "effective_step = " + effective_step);
						Log.i("caixinxin", "statistics_step = " + statistics_step);
						Log.i("caixinxin", "total_step = " + total_step);
						Log.i("caixinxin", "has_statistics = " + has_statistics);
						Log.i("caixinxin", "sync = " + sync);
						
						if (effective_step >= total_step)
							total_step = effective_step;
						if (statistics_step > total_step)
							total_step = statistics_step;
						//根据有效步数和无效步数计算卡路里
						invalid_step = total_step - effective_step;
						int meter = Tools.calcDistance(effective_step + invalid_step, config.getHeight());
						calories = Tools.calcCalories(meter, config.getWeightNum());
						
						if (has_statistics) {
							if (total_step == statistics_step && sync == 1) {
								ContentProviderOperation op1 = ContentProviderOperation.newUpdate(DataBaseContants.CONTENT_URI)
										.withSelection(DataBaseContants.DATE + " = ? AND " + DataBaseContants.STATISTICS + " = ? ", new String[] { day, "1" })
										.withValue(DataBaseContants.STEPS, total_step)
										.withValue(DataBaseContants.KILOMETER, meter)
										.withValue(DataBaseContants.CALORIES, calories)
										.withValue(DataBaseContants.COMPLETE, total_step >= goal.mGoalSteps ? 1 : 0)
										.withYieldAllowed(true).build();
								operations.add(op1);
							} else {
								ContentProviderOperation op1;
								if (sync == 0) {
									op1 = ContentProviderOperation.newUpdate(DataBaseContants.CONTENT_URI)
											.withSelection(DataBaseContants.DATE + " = ? AND " + DataBaseContants.STATISTICS + " = ? ", new String[] { day, "1" })
											.withValue(DataBaseContants.STEPS, total_step)
											.withValue(DataBaseContants.KILOMETER, meter)
											.withValue(DataBaseContants.CALORIES, calories)
											.withValue(DataBaseContants.COMPLETE, total_step >= goal.mGoalSteps ? 1 : 0)
											.withValue(DataBaseContants.SYNC_STATE, 0)
											.withYieldAllowed(true).build();
								} else {
									op1 = ContentProviderOperation.newUpdate(DataBaseContants.CONTENT_URI)
											.withSelection(DataBaseContants.DATE + " = ? AND " + DataBaseContants.STATISTICS + " = ? ", new String[] { day, "1" })
											.withValue(DataBaseContants.STEPS, total_step)
											.withValue(DataBaseContants.KILOMETER, meter)
											.withValue(DataBaseContants.CALORIES, calories)
											.withValue(DataBaseContants.COMPLETE, total_step >= goal.mGoalSteps ? 1 : 0)
											.withValue(DataBaseContants.SYNC_STATE, 2)
											.withYieldAllowed(true).build();
								}
								operations.add(op1);
							}
						} else {
							if (total_step > 0) {
								ContentProviderOperation op1 = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI)
										.withValue(DataBaseContants.ID, Tools.getPKL())
										.withValue(DataBaseContants.STEPS, total_step)
										.withValue(DataBaseContants.KILOMETER, meter)
										.withValue(DataBaseContants.CALORIES, calories)
										.withValue(DataBaseContants.DATE, day)
										.withValue(DataBaseContants.SPORTS_TYPE, 0)
										.withValue(DataBaseContants.TYPE, 0)
										.withValue(DataBaseContants.COMPLETE, total_step >= goal.mGoalSteps ? 1 : 0)
										.withValue(DataBaseContants.STATISTICS, 1)
										.withValue(DataBaseContants.BMI, goal.mGoalSteps)
										.withYieldAllowed(true).build();
								operations.add(op1);
							}
						}
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
	}
}
