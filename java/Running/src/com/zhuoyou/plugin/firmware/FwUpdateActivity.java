package com.zhuoyou.plugin.firmware;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyou.plugin.ble.BleManagerService;
import com.zhuoyou.plugin.ble.BluetoothLeService;
import com.zhuoyou.plugin.ble.DfuService;
import com.zhuoyou.plugin.bluetooth.data.Util;
import com.zhuoyou.plugin.custom.CustomAlertDialog;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.Tools;
import com.zhuoyou.plugin.view.HoloCircularProgressBar;

public class FwUpdateActivity extends Activity implements OnClickListener {
	private final String tag = "FwUpdateActivity";
	
	private String device_name = "";
	private Button mExit, mNext, mPre;
	private TextView mIntroduction;
	private FrameLayout mLayout;
	private HoloCircularProgressBar mProgressBar;
	private ImageView mImageView1;
	private ImageView mImageView2;
	private ImageView mImageView3;
	private LinearLayout mText_layout, mStep2_layout, mStep3_layout,mStep4_firmLayot, mButton_layout;
	private TextView mTVstate;
	private TextView mTVrate;
	private TextView mToast;
	private AnimationDrawable animationDrawable = null;
	private IntentFilter mViweIntent;
	private IntentFilter mBlueFilter;
	public static final String ACTION_DOWNLOADING = "com.zhuoyou.running.action.downloading";
	public static final String ACTION_DOWNLOADED = "com.zhuoyou.running.action.downloaded";
	public static final String ACTION_DOWNLOAD_PAUSED = "com.zhuoyou.running.action.download.paused";
	public static final String ACTION_DOWNLOAD_FAILED = "com.zhuoyou.running.action.download.failed";
	private int rate = 0;
	private String filePath;
	private SharedPreferences prefs;
	private int step = 1;
	private Handler mHandler;
	private boolean isBleDevice;
	private boolean isUpdateReady = false;
	
	// 尝试次数
	private int testNum = 0;

	@SuppressLint("HandlerLeak") 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_firmware_upgrade);
		device_name = getIntent().getStringExtra("device_name");
		isBleDevice = getIntent().getBooleanExtra("isBleDevice",false);
		mHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				if(msg.what == 200){
					startDFU();
				}
			}
		};
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		mExit = (Button) findViewById(R.id.exit);
		mExit.setOnClickListener(this);
		mIntroduction = (TextView) findViewById(R.id.introduction);
		mLayout = (FrameLayout) findViewById(R.id.layout);
		mProgressBar = (HoloCircularProgressBar) findViewById(R.id.holoCircularProgressBar);
		mImageView1 = (ImageView) findViewById(R.id.imageview1);
		mImageView1.setEnabled(false);
		mImageView2 = (ImageView) findViewById(R.id.imageview2);
		animationDrawable = (AnimationDrawable) mImageView2.getBackground();
		if (!animationDrawable.isRunning())
			animationDrawable.start();
		mImageView3 = (ImageView) findViewById(R.id.imageview3);
		mTVstate = (TextView) findViewById(R.id.fw_update_state);
		mTVrate = (TextView) findViewById(R.id.fw_update_rate);
		mToast = (TextView) findViewById(R.id.toast);
		mText_layout = (LinearLayout) findViewById(R.id.text_layout);
		mStep2_layout = (LinearLayout) findViewById(R.id.step2_layout);
		mStep3_layout = (LinearLayout) findViewById(R.id.step3_layout);
		mStep4_firmLayot = (LinearLayout)findViewById(R.id.step4_firmware_layout);
		mButton_layout = (LinearLayout) findViewById(R.id.button_layout);
		mNext = (Button) findViewById(R.id.next);
		mNext.setOnClickListener(this);
		mPre = (Button) findViewById(R.id.pre);
		mPre.setOnClickListener(this);
		mViweIntent = new IntentFilter();
		mViweIntent.addAction(ACTION_DOWNLOADING);
		mViweIntent.addAction(ACTION_DOWNLOADED);
		mViweIntent.addAction(ACTION_DOWNLOAD_PAUSED);
		mViweIntent.addAction(ACTION_DOWNLOAD_FAILED);
		registerReceiver(mUpdateUIRecivier,mViweIntent);
		mBlueFilter= new IntentFilter();
		mBlueFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		mBlueFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		mBlueFilter.addAction(DfuService.BROADCAST_PROGRESS);
		registerReceiver(mBlueToothRecivier,mBlueFilter);
		
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(DfuService.BROADCAST_PROGRESS);
		intentFilter.addAction(DfuService.BROADCAST_ERROR);
		LocalBroadcastManager.getInstance(this).registerReceiver(mDfuUpdateReceiver, intentFilter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mUpdateUIRecivier);
		unregisterReceiver(mBlueToothRecivier);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mDfuUpdateReceiver);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.exit:
			if (step == 1) {
				Intent intent = new Intent("com.zhuoyou.running.cancel.firmwear");
				sendBroadcast(intent);
				finish();
			} else if (step == 2) {
				finish();
			}
			break;
		case R.id.next:
			if (step == 2) {
				filePath = prefs.getString("path","");
				Log.i("caixinxin", "filePath = " + filePath);
				sendFileByBluetooth(filePath);
				mHandler.postDelayed(new Runnable() {					
					@Override
					public void run() {
						step = 3;
						mExit.setVisibility(View.GONE);
						mStep2_layout.setVisibility(View.GONE);
						mStep3_layout.setVisibility(View.VISIBLE);
						mPre.setVisibility(View.VISIBLE);
						mNext.setText(R.string.it_is_ok);
						if (device_name.startsWith("EAMEY_P3") || device_name.startsWith("EAMEY_P2") || device_name.startsWith("Primo_3"))
							mToast.setVisibility(View.VISIBLE);
					}
				}, 500);
			} else if (step == 3) {
				CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(this);
				builder.setTitle(R.string.alert_title);
				builder.setMessage(R.string.firmwear_des_4);
				builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Tools.setFirmwear(false);
						finish();
					}
				});
				builder.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.setCancelable(false);
				CustomAlertDialog dialog = builder.create();
				dialog.show();
			}else if (step == 4){
				filePath = prefs.getString("path","");
				isUpdateReady = true;
				Intent bleIntent = new Intent(BleManagerService.ACTION_READY_FIRMWARE_UPDATE);
				sendBroadcast(bleIntent);
				
				mHandler.postDelayed(new Runnable() {					
					@Override
					public void run() {
						mButton_layout.setVisibility(View.GONE);
						findViewById(R.id.toast_firmware).setVisibility(View.VISIBLE);
					}
				}, 500);
			}
			break;
		case R.id.pre:
			if (step == 3) {
				step = 2;
				mExit.setVisibility(View.VISIBLE);
				mStep2_layout.setVisibility(View.VISIBLE);
				mStep3_layout.setVisibility(View.GONE);
				mPre.setVisibility(View.GONE);
				mNext.setText(R.string.next);
				if (device_name.startsWith("EAMEY_P3") || device_name.startsWith("EAMEY_P2") || device_name.startsWith("Primo_3"))
					mToast.setVisibility(View.GONE);
			}
			break;
		default:
			break;
		}		
	}

	private final BroadcastReceiver mUpdateUIRecivier = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.i(tag, action);
			if (action.equals(ACTION_DOWNLOADING)) {
				if (!animationDrawable.isRunning())
					animationDrawable.start();
				mImageView2.setVisibility(View.VISIBLE);
				mImageView3.setVisibility(View.GONE);
				mTVstate.setText(R.string.firmware_downloading);
				int temp = intent.getIntExtra("rate", 0);
				if( temp > rate ){
					rate = temp;
					float frate = ((float)rate)/100f;
					mTVrate.setText(rate + "%");
					mProgressBar.setProgress(frate);
				}
			} else if (action.equals(ACTION_DOWNLOADED)) {
				if (step == 1) {
					if(isBleDevice){
						step = 4;
						mIntroduction.setVisibility(View.GONE);
						mLayout.setVisibility(View.GONE);
						mText_layout.setVisibility(View.GONE);
						mStep4_firmLayot.setVisibility(View.VISIBLE);
						mButton_layout.setVisibility(View.VISIBLE);
						mNext.setText(R.string.ok);
					}else{
						step = 2;
						mIntroduction.setVisibility(View.GONE);
						mLayout.setVisibility(View.GONE);
						mText_layout.setVisibility(View.GONE);
						mStep2_layout.setVisibility(View.VISIBLE);
						mButton_layout.setVisibility(View.VISIBLE);
					}
				}
			} else if (action.equals(ACTION_DOWNLOAD_PAUSED)) {
				if (animationDrawable.isRunning())
					animationDrawable.stop();
				mImageView2.setVisibility(View.GONE);
				mImageView3.setVisibility(View.VISIBLE);
				mImageView3.setImageResource(R.drawable.update_again);
				mTVstate.setText(R.string.firmwear_net_error);
				mTVrate.setText(null);
			}
		}
	};
	
	private final BroadcastReceiver mBlueToothRecivier = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if(BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)){
				Log.i(tag, "Firmware ACTION_GATT_DISCONNECTED");
				if(isUpdateReady){
					mHandler.sendEmptyMessageDelayed(200, 100);
				}
			}
		}
	};
	
	private final BroadcastReceiver mDfuUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			Log.i(tag, "action:"+action);
			if (DfuService.BROADCAST_PROGRESS.equals(action)) {
				final int progress = intent.getIntExtra(DfuService.EXTRA_DATA, 0);
				if(progress == DfuService.PROGRESS_COMPLETED){
					Toast.makeText(getApplicationContext(), "固件更新完成", Toast.LENGTH_LONG).show();
					FwUpdateActivity.this.finish();
				}
			} else if (DfuService.BROADCAST_ERROR.equals(action)) {
				if(testNum<2){
					Intent service = new Intent(FwUpdateActivity.this, DfuService.class);
					stopService(service);
					mHandler.sendEmptyMessageDelayed(200,1000);
				}else {
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
							manager.cancel(DfuService.NOTIFICATION_ID);
							Toast.makeText(getApplicationContext(), "固件更新失败", Toast.LENGTH_LONG).show();
							FwUpdateActivity.this.finish();
						}
					}, 200);
				}
			}
		}
	};
	
	/**  推送至经典蓝牙  */
	private void sendFileByBluetooth(String path) {
		if (path == null || path.equals(""))
			return;
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter(); 
		if (btAdapter == null)
			return;
		
		PackageManager localPackageManager = getPackageManager();
        Intent localIntent = null;
        HashMap<String, ActivityInfo> localHashMap = null;
        try {
            localIntent = new Intent();
            localIntent.setAction(Intent.ACTION_SEND);
            File file = new File(path);
            localIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            localIntent.setType("*/*");
            List<ResolveInfo> localList = localPackageManager.queryIntentActivities(localIntent, 0);
            localHashMap = new HashMap<String, ActivityInfo>();
            Iterator<ResolveInfo> localIterator1 = localList.iterator();
            while (localIterator1.hasNext()) {
                ResolveInfo resolveInfo = (ResolveInfo) localIterator1.next();
                ActivityInfo localActivityInfo2 = resolveInfo.activityInfo;
                String str = localActivityInfo2.applicationInfo.processName;
                if (str.contains("bluetooth"))
                    localHashMap.put(str, localActivityInfo2);
            }
        } catch (Exception localException) {
        }
        ActivityInfo localActivityInfo1 = (ActivityInfo) localHashMap.get("com.android.bluetooth");
        if (localActivityInfo1 == null) {
            localActivityInfo1 = (ActivityInfo) localHashMap.get("com.mediatek.bluetooth");
        }
        if (localActivityInfo1 == null) {
            Iterator<ActivityInfo> localIterator2 = localHashMap.values().iterator();
            if (localIterator2.hasNext())
                localActivityInfo1 = (ActivityInfo) localIterator2.next();
        }
        if (localActivityInfo1 != null) {
            localIntent.setComponent(new ComponentName(localActivityInfo1.packageName, localActivityInfo1.name));
            startActivity(localIntent);
        }
	}	
	
	private void startDFU(){
		testNum ++;
		String address = Util.getLatestConnectDeviceAddress(FwUpdateActivity.this);
		filePath = prefs.getString("path","");
		File file = new File(filePath);
		if(file.exists()){
			Log.i(tag, "start Update:"+address);
			final Intent service = new Intent(FwUpdateActivity.this, DfuService.class);
			service.putExtra(DfuService.EXTRA_DEVICE_ADDRESS, address);	
			service.putExtra(DfuService.EXTRA_FILE_MIME_TYPE, DfuService.MIME_TYPE_OCTET_STREAM);
			service.putExtra(DfuService.EXTRA_FILE_TYPE, DfuService.TYPE_APPLICATION);
			service.putExtra(DfuService.EXTRA_FILE_PATH, filePath);
			startService(service);		
		}
	}
	
	@Override
	public void onBackPressed() {
		return;
	}
}
