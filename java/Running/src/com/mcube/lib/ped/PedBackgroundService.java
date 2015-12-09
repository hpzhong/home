package com.mcube.lib.ped;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.gps.GaodeService;
import com.zhuoyou.plugin.gps.ilistener.StepWatcher;
import com.zhuoyou.plugin.running.MainService;
import com.zhuoyou.plugin.running.PersonalConfig;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.RunningItem;
import com.zhuoyou.plugin.running.Tools;
import com.zhuoyou.plugin.running.HomePageFragment.DateChangeReceiver;
import com.zhuoyou.plugin.weather.WeatherTools;

/**
 * 开启计步
 * @author zhaojunhui
 *
 */
public class PedBackgroundService extends Service {
	
	private static String TAG = "PedBackgroundService";
//	private static PedBackgroundService mInstance;
	private PedometerService.LocalBinder mSubBinder;
	private boolean mBound =false;
	private WakeLock mWakeLock = null;
	private int steps_num=0;
	private int steps_state=0;
	private int currentSteps=0;
	FileOutputStream output=null;
	FileInputStream input=null;
	PrintStream out;
	
	private static Context sContext=RunningApp.getInstance().getApplicationContext();
	private static Object INSTANCE_LOCK = new Object(); 
	private static final String FILENAME="steps.txt";
	private static final String STEPSFILE="/data/data/com.zhuoyou.plugin.running/files/steps.txt";
	private ContentResolver mContentResolver;
	private Context mCtx = RunningApp.getInstance();
	private DateChangeReceiver mDateChangeReceiver = new DateChangeReceiver();

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
//		mInstance = this;
//		Log.d("yangyang","mInstance =" +mInstance);
		mContentResolver = getContentResolver();
		IntentFilter intentf = new IntentFilter(Intent.ACTION_DATE_CHANGED);
		mCtx.registerReceiver(mDateChangeReceiver, intentf);
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PedometerService.class.getName());
		mWakeLock.acquire();
		registerReceiver(mAliveReceiver, new IntentFilter("com.zhuoyou.steps.service.hello"));
		startPedometerService();
		File stepsFile=new File(STEPSFILE);
		if(stepsFile.exists()){
			try {
				input=super.openFileInput(FILENAME);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Scanner scan=new Scanner(input);
			while(scan.hasNextInt()){
				currentSteps=scan.nextInt();
			}
			scan.close();
		}
			try {
				Log.d("yangyang", "PedBackgroundService output....");
				output=super.openFileOutput(FILENAME, Activity.MODE_PRIVATE);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				Log.d("yangyang", "PedBackgroundService FileNotFoundException....");
				e.printStackTrace();
			}
			out=new PrintStream(output);
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}
	
	private BroadcastReceiver mAliveReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			Intent mAliveIntent = new Intent("com.zhuoyou.steps.activity.hi");
			sendBroadcast(mAliveIntent);
		}
	};

	public class LocalBinder extends Binder {
		public PedBackgroundService getService() {
			return PedBackgroundService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d("yangyang", "PedBackgroundService onbind ");
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d("yangyang", "PedBackgroundService onUnbind ");
		return super.onUnbind(intent);
	}

	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.d("yangyang", "PedBackgroundService onDestroy ");
		super.onDestroy();
		if (mWakeLock != null) {
			mWakeLock.release();
			mWakeLock = null;
		}
		if(out!=null){
			out.close();
		}
		mSubBinder.unRegisterListeners(pedListener);
		//startPedBackgroundService();
		unregisterReceiver(mAliveReceiver);
		unbindService(mConnection);
//		mBound = false;
//		saveTotalSteps(hadRunStep);
		mCtx.unregisterReceiver(mDateChangeReceiver);

	}



	private final IBinder binder = new LocalBinder();
	
//	public static PedBackgroundService getInstance() {
//		Log.d("yangyang","getInstance1 :" +mInstance);
//		synchronized (INSTANCE_LOCK) {
//			if (mInstance == null) {
//				startPedBackgroundService();
//				Log.d("yangyang","getInstance2 :" +mInstance);
//			}			
//			return mInstance;
//		}
//
//	}

//	private static void startPedBackgroundService() {
//		Log.i("yangyang", "startPedBackgroundService()");
//		Log.d("yangyang","sContext"+sContext);
//		Intent startServiceIntent = new Intent(sContext, PedBackgroundService.class);
//		sContext.startService(startServiceIntent);
//	}
	
	private boolean startPedometerService() {
		Log.d("yangyang","startPedometerService");
		boolean sucess;

		Intent bindIntent = new Intent(this, PedometerService.class);
		sucess = bindService(bindIntent, mConnection,Context.BIND_AUTO_CREATE);
		hadRunStep = 0;
		return sucess;
	}
	
	
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mSubBinder = (PedometerService.LocalBinder) service;
			Log.d("yangyang","onServiceConnected " + mSubBinder);
			mSubBinder.registerListeners(pedListener);
			mSubBinder.startPedometer();
			mBound = true;
			//启动定时器
			timer.schedule(task, 0, 1 * 1000);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.d("yangyang","onServiceDisconnected " + mSubBinder);
			mSubBinder.unRegisterListeners(pedListener);
			mSubBinder.stopPedometer();
			mBound = false;
		}
	};
	public class DateChangeReceiver extends BroadcastReceiver {
		private static final String ACTION_DATE_CHANGED = Intent.ACTION_DATE_CHANGED;

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			String action = arg1.getAction();
			if (ACTION_DATE_CHANGED.equals(action)) {
				Log.d("gchk", "---DATE_CHANGED!---");
				// 新的一天到来后重启一下计步service
				Intent phoneStepsIntent = new Intent(getApplicationContext(), PedBackgroundService.class); 
                stopService(phoneStepsIntent);
                startService(phoneStepsIntent);
			}
		}
	}
	public static int hadRunStep;
	private long start_time;
	private long last_time;
	public static int start_step;
	private boolean flag = false;
	private int times = 0;
	private int steps = 0;
    private int total_step;
	private void getPaceGroup(int stepCount) {
		long current_time = System.currentTimeMillis();
		if (!flag) {
			flag = true;
			start_step = stepCount;
			last_time = current_time;
		}
//		Log.i("333", "stepCount="+stepCount);
//		Log.i("111", "last_time = " + last_time);
//		Log.i("111", "current_time = " + current_time);
		long ii = current_time - last_time;
//		Log.i("111", "yyyyyyyyyyyy = " + ii);
		if (current_time - last_time > 1000 * 60) {
			Log.i("111", "111111111111");
			if (stepCount - start_step >= 30) {
				Log.i("111", "start 22222222222");
				if (times == 0) {
					start_time = last_time;
				}
				times++;
				Log.i("111", "times = " + times);
				steps += stepCount - start_step;
			} else if (times >= 10) {
				Log.i("111", "end 333333333333");
				steps += stepCount - start_step;
				save(steps, start_time, current_time);
				times = 0;
				steps = 0;
				flag = false;
			} else {
				Log.i("111", "clear 44444444444");
				times = 0;
				steps = 0;
				flag = false;
			}
			last_time = current_time;
			start_step = stepCount;
			Log.i("333", "start_step="+start_step);
			Log.i("333", "start_step="+start_step);
		}
	}

	private void save(int step, long start_time, long end_time) {
		String time = Tools.transformLongTime2StringFormat2(start_time);
		String date = time.substring(0, 10);
		String start = time.substring(11, 16);
		String end = Tools.transformLongTime2StringFormat2(end_time).substring(11, 16);
		PersonalConfig config = Tools.getPersonalConfig();
		int meter = Tools.calcDistance(step, config.getHeight());
		int calories = Tools.calcCalories(meter, config.getWeightNum());
		
		RunningItem runningItem = new RunningItem();
		runningItem.setDate(date);
		runningItem.setStartTime(start);
		runningItem.setEndTime(end);
		runningItem.setKilometer(meter);
		runningItem.setCalories(calories);
		runningItem.setSteps(step);
		runningItem.setmType(6);
		runningItem.setisStatistics(0);
		Intent phoneIntent = new Intent("ACTION_PHONE_STEPS");
		phoneIntent.putExtra("phone_steps", runningItem);
		phoneIntent.putExtra("total_step", hadRunStep);
		sendBroadcast(phoneIntent);
		
	}
	
	private static int PHONE_STEP = 0;
	private void readPhoneStep(String day) {
		ContentResolver cr = mCtx.getContentResolver();
		Cursor c = cr.query(DataBaseContants.CONTENT_URI, new String[] { "_id", "steps" },
				DataBaseContants.DATE + "  = ? AND " + DataBaseContants.DATA_FROM + "  = ? AND " + DataBaseContants.STATISTICS + " = ?", new String[] {day, "phone", "2" }, null);
		c.moveToFirst();
		if (c.getCount() > 0) {
			for (int y = 0; y < c.getCount(); y++) {
				PHONE_STEP = c.getInt(c.getColumnIndex(DataBaseContants.STEPS));
				c.moveToNext();
			}
		}
		c.close();
		c = null;
	}
	
	private void saveTotalSteps(int step) {
		
		if(step == 10) {
			String day = Tools.getDate(0);
			readPhoneStep(day);
			total_step = PHONE_STEP;
		}else if (step >= 11) {
	    	Log.i("444", "step="+step);
			Log.i("444", "hadRunStep="+step);
			PersonalConfig config = Tools.getPersonalConfig();
			int meter = Tools.calcDistance(step, config.getHeight());
			int calories = Tools.calcCalories(meter, config.getWeightNum());
			String day = Tools.getDate(0);
	        step = step + total_step;
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
								.withValue(DataBaseContants.STEPS, step)
								.withValue(DataBaseContants.CALORIES, calories)
								.withValue(DataBaseContants.KILOMETER, meter)
								.withYieldAllowed(true).build();
					} else {
						op2 = ContentProviderOperation.newUpdate(DataBaseContants.CONTENT_URI)
								.withSelection(DataBaseContants.DATE + " = ? AND " + DataBaseContants.STATISTICS + " = ? AND " + DataBaseContants.DATA_FROM + " = ? ", new String[] { day, "2", "phone" })
								.withValue(DataBaseContants.STEPS, step)
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
							.withValue(DataBaseContants.STEPS, step)
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
			MainService.getInstance().checkDataBase();
		}
	
	}
	
	
	private PedometerListener pedListener = new PedometerListener(){

		@Override
		public void onStepCount(int stepCount) {
			hadRunStep = stepCount;
			/** design by zhongyang 20150117*/
			StepWatcher.getInstance().notifyStepCount(stepCount);
			Log.i("444", "-->"+stepCount);
//			getPaceGroup(stepCount);
			saveTotalSteps(stepCount);
		}

		@Override
		public void onStateChanged(int newState) {
			/** design by zhongyang 20150117*/
			StepWatcher.getInstance().notifyStateChanged(newState);
		}
		
	};
	
	private Handler handler  = new Handler(){
	    public void handleMessage(Message msg) {
	        super.handleMessage(msg);
	        if(msg.what == 1){
				getPaceGroup(hadRunStep);
//				saveTotalSteps(hadRunStep);
	        }
	    }
	};
	 
	 
	private Timer timer = new Timer(true);
	 
	//任务
	private TimerTask task = new TimerTask() {
	  public void run() {
	    Message msg = new Message();
	    msg.what = 1;
	    handler.sendMessage(msg);
	  }
	};
	
	private int i = 0;
	
	public void clearPedometerHistory(){
		if(mSubBinder != null)
		mSubBinder.clearPedometerStepCount();
	}
	
	public void resetPedometer(){
		if(mSubBinder != null){
			mSubBinder.stopPedometer();
			mSubBinder.clearPedometerStepCount();
			mSubBinder.startPedometer();
		}
	}	
}
