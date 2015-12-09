package com.zhuoyou.plugin.firmware;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.zhuoyou.plugin.cloud.GetDataFromNet;
import com.zhuoyou.plugin.cloud.NetMsgCode;
import com.zhuoyou.plugin.cloud.NetUtils;
import com.zhuoyou.plugin.custom.CustomAlertDialog;
import com.zhuoyou.plugin.download.Util_update;
import com.zhuoyou.plugin.running.Main;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;
import com.zhuoyou.plugin.selfupdate.Constants;

public class FirmwareService extends Service {
	private static final String tag = "FirmwareService";
	
	public static final String ACTION_RECEIVER_DEVICE_INFO = "com.zhouyou.device.receiver.deviceinfo";
	private Context mContext = RunningApp.getInstance().getApplicationContext();
	private boolean downloading = false;
	private IntentFilter mDeviceFilter;
	private IntentFilter mDownLoadFilter;
	private IntentFilter mBlueToothFilter;
	private GetDataFromNet task;
	private Firmware ware;
	private SharedPreferences prefs;
	private DownloadManager downloadManager;
	private long fileId;
	private ThreadGetDownRate mThreadRate;
	private int temp;
	private String fileName = "image.bin";
	private String device_name = "";
	
	private boolean isBleDevice = false;

	private class FirmwareHandler extends Handler {
		/** 1:手动更新，其他为自动更新  */
		private int mType;

		public FirmwareHandler(int type) {
			mType = type;
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case NetMsgCode.The_network_link_success:
				// 升级
				if(isBleDevice){
					fileName = "image.hex";
				}
				Tools.setFirmwear(true);
				ware = (Firmware)msg.obj;
				String sdcardPath = Util_update.FileManage.getSDPath();
				Util_update.FileManage.FileHolder fileHolder = Util_update.FileManage.readSDCardSpace();
				if (sdcardPath == null) {
					if (mType == 1)
						Toast.makeText(mContext, R.string.please_insert, Toast.LENGTH_SHORT).show();
					stopSelf();
				} else if (fileHolder != null && fileHolder.availSpace < 1024 * 10) {
					if (mType == 1)
						Toast.makeText(mContext, R.string.space_not_enough, Toast.LENGTH_SHORT).show();
					stopSelf();
				} else {
					if (isTopActivity()) {
						showUpgradeDialog();
					} else {
						showNotification();
					}
				}
				break;
			case NetMsgCode.The_network_link_failure:
				// 不需要升级
				Tools.setFirmwear(false);
				if (mType == 1)
					Toast.makeText(mContext, R.string.firmware_upgrade_none, Toast.LENGTH_SHORT).show();
				stopSelf();
				break;
			}
		}
	}

	//判断当前应用是否在前台运行
	private boolean isTopActivity() {
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
		String packageName = mContext.getPackageName();
		if (tasksInfo.size() > 0) {
			// 应用程序位于堆栈的顶层
			if (packageName.equals(tasksInfo.get(0).topActivity.getPackageName())) {
				return true;
			}
		}
		return false;
	}
	
	private void showUpgradeDialog() {
		String path = Util_update.FileManage.getSDPath() + Constants.DownloadFirmwarePath + fileName;
		File f = new File(path);
		if (f.exists()) {
			f.delete();
		}
		Editor editor = prefs.edit();
		if (prefs.contains(ware.getCurrentVer()))
			editor.remove(ware.getCurrentVer());
		if (prefs.contains("path"))
			editor.remove("path");
		editor.commit();
		CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(mContext);
		builder.setTitle(R.string.alert_title);
		builder.setMessage(R.string.upgrade_alert);
		builder.setPositiveButton(R.string.upgrade, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(mContext, FwUpdateActivity.class);
				intent.putExtra("device_name", device_name);
				intent.putExtra("isBleDevice", isBleDevice);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				dialog.dismiss();
				downloadBin();
			}
		});
		builder.setNegativeButton(R.string.upgrade_none, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				stopSelf();
			}
		});
		builder.setCancelable(false);
		CustomAlertDialog dialog = builder.create();
		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.show();
	}
	
	private void showNotification() {
		NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(R.string.firmware_upgrade);
		Notification notification = new Notification();
		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = mContext.getText(R.string.firmwear_notiy_title);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		Intent intent = new Intent(mContext, Main.class);
		intent.putExtra("firmwear", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        notification.setLatestEventInfo(mContext,
                mContext.getText(R.string.firmwear_notiy_title),
                mContext.getText(R.string.firmwear_notiy_message),
                pendingIntent);
        manager.notify(R.string.firmware_upgrade, notification);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		initValue();
		initIntentFilter();
		registerBc();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		downloading = false;
		mThreadRate = null;
		unRegisterBc();
	}

	private void initValue() {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
	}
	
	private void initIntentFilter() {
		mDeviceFilter = new IntentFilter();
		mDeviceFilter.addAction("com.tyd.bt.device.firmware");
		mDeviceFilter.addAction("com.zhuoyou.running.cancel.firmwear");
		mDeviceFilter.addAction("com.zhuoyou.running.notification.firmwear");
		mDownLoadFilter = new IntentFilter();
		mDownLoadFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		mDownLoadFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
		mDownLoadFilter.addAction(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
		mBlueToothFilter = new IntentFilter();
		mBlueToothFilter.addAction(ACTION_RECEIVER_DEVICE_INFO);
	}

	private void registerBc() {
		registerReceiver(mGetFirmwareReceiver, mDeviceFilter);
		registerReceiver(mDownloadReceiver, mDownLoadFilter);
		registerReceiver(mBlueToothStateReceiver, mBlueToothFilter);
	}

	private void unRegisterBc() {
		unregisterReceiver(mGetFirmwareReceiver);
		unregisterReceiver(mDownloadReceiver);
		unregisterReceiver(mBlueToothStateReceiver);
	}

	private BroadcastReceiver mGetFirmwareReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("com.tyd.bt.device.firmware")) {
				isBleDevice = false;
				String content = intent.getStringExtra("content");
				Log.i("caixinxin", "content = " + content);
				String[] s = content.split("\\|");
				String device_code = s[0];
				String version_code = s[1];
				device_name = device_code;
				int type = Integer.parseInt(s[2]);
				if ( NetUtils.getAPNType(context) == -1) {
					if (type == 1)
						Toast.makeText(context, R.string.check_network, Toast.LENGTH_SHORT).show();
					stopSelf();
				} else {
					FirmwareHandler mHandler = new FirmwareHandler(type);
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("dev", device_code);
					params.put("ver", version_code);
					if (task == null || task.getStatus() == AsyncTask.Status.FINISHED) {
						task = new GetDataFromNet(NetMsgCode.FIRMWARE_GET_VERSION, mHandler, params, context);
						task.execute(NetMsgCode.URL);
					}
				}
			} else if (action.equals("com.zhuoyou.running.cancel.firmwear")) {
				downloadManager.remove(fileId);
				Editor editor = prefs.edit();
				if (prefs.contains(ware.getCurrentVer()))
					editor.remove(ware.getCurrentVer());
				if (prefs.contains("path"))
					editor.remove("path");
				editor.commit();
				stopSelf();
			} else if (action.equals("com.zhuoyou.running.notification.firmwear")) {
				showUpgradeDialog();
			}
		}
		
	};
	
	/** BLE 连接接收到的广播 */
	private BroadcastReceiver mBlueToothStateReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if(action.equals(ACTION_RECEIVER_DEVICE_INFO)){
				Log.i(tag, "receiver ACTION_RECEIVER_DEVICE_INFO");
				isBleDevice = true;
				String version =  intent.getStringExtra("device_version");
				Log.i(tag, "device.version:"+version);// LEO_5
				int slitIndex = version.lastIndexOf("_");
				if(slitIndex > 1 && slitIndex < version.length() ){
					device_name = version.substring(0,slitIndex);
					String deviceVer = version.substring(slitIndex+1);
					int type = 1;
					if ( NetUtils.getAPNType(context) == -1) {
						if (type == 1)
							Toast.makeText(context, R.string.check_network, Toast.LENGTH_SHORT).show();
						stopSelf();
					} else {
						FirmwareHandler mHandler = new FirmwareHandler(type);
						HashMap<String, String> params = new HashMap<String, String>();
						params.put("dev", device_name);
						params.put("ver", deviceVer);
						if (task == null || task.getStatus() == AsyncTask.Status.FINISHED) {
							task = new GetDataFromNet(NetMsgCode.FIRMWARE_GET_VERSION, mHandler, params, context);
							Log.i(tag, "device_name:"+device_name + ",ver:" + deviceVer);
							task.execute(NetMsgCode.URL);
						}
					}
				}
			}
		}
	};

	private BroadcastReceiver mDownloadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if(action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)){
				Log.i("caixinxin", "mDownloadReceiver download complete");
				downloading = false;
				try {
					queryDownloadStatus();
				} catch (Exception e) {
				}
			}else if(action.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)){
				
				
			}else if(action.equals(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS)){
				
			}
		}
	};
	private void downloadBin() {
		Log.i("caixinxin", "downloadBin");
		if (!prefs.contains(ware.getCurrentVer())) {
			Log.i("caixinxin", "downloadBin1");
			String url = ware.getFileUrl();
			Uri resource = Uri.parse(url);
			DownloadManager.Request request = new DownloadManager.Request(resource);
			request.setAllowedNetworkTypes(Request.NETWORK_MOBILE|Request.NETWORK_WIFI);
			request.setAllowedOverRoaming(false);
			request.setVisibleInDownloadsUi(false);
			request.setNotificationVisibility(Request.VISIBILITY_HIDDEN);
			request.setDestinationInExternalPublicDir(Constants.DownloadFirmwarePath, fileName);
			fileId = downloadManager.enqueue(request);
			prefs.edit().putLong(ware.getCurrentVer(), fileId).commit();
			downloading = true;
			mThreadRate = new ThreadGetDownRate();
			mThreadRate.start();
		} else {
			Log.i("caixinxin", "downloadBin2");
			try {
				queryDownloadStatus();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	class ThreadGetDownRate extends Thread{

		@Override
		public void run() {
			while(downloading){
				try {
					sleep(500);
					int rate = 0 ;
					try {
						rate = queryDownloadStatus();
					} catch (Exception e) {
						
					}
					sendBroadCast(rate);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private int queryDownloadStatus () throws Exception {
		Log.i("caixinxin", "queryDownloadStatus");
		long id = prefs.getLong(ware.getCurrentVer(), -1);
		if (id == -1) {
			return 0;
		}
		int rate = 0; // 下载的百分比
		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterById(id);
		Cursor c = downloadManager.query(query);
		if (c.moveToFirst()) {
			int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
			int reasonIdx = c.getColumnIndex(DownloadManager.COLUMN_REASON);   
			int fileSizeIdx = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
			int bytesDLIdx = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
			int fileSize = c.getInt(fileSizeIdx);
			int bytesDL = c.getInt(bytesDLIdx);
			int reason = c.getInt(reasonIdx);
			Intent intent;
			switch (reason) {
			case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
				Log.i("caixinxin", "Waiting for connectivity");
				intent = new Intent(FwUpdateActivity.ACTION_DOWNLOAD_PAUSED);
				sendBroadcast(intent);		
				break;
			}
			if (fileSize != 0 && 0 <= bytesDL && bytesDL <= fileSize) {
				rate = bytesDL * 100 / fileSize;
				Log.i("caixinxin", "Downloaded rate = "+rate);
			}
			switch (status) {
			case DownloadManager.STATUS_PAUSED:
				break;
			case DownloadManager.STATUS_PENDING:
				break;
			case DownloadManager.STATUS_RUNNING:
				break;
			case DownloadManager.STATUS_SUCCESSFUL:
				Uri uri = downloadManager.getUriForDownloadedFile(id);
				File f = new File(new URI(uri.toString()));
				prefs.edit().putString("path",f.getAbsolutePath()).commit();
				Log.i("caixinxin", "download successful");
				intent = new Intent(FwUpdateActivity.ACTION_DOWNLOADED);
				sendBroadcast(intent);		
				stopSelf();
				break;
			case DownloadManager.STATUS_FAILED:
				// 清除已下载的内容，重新下载
				downloadManager.remove(fileId);
				Editor editor = prefs.edit();
				if (prefs.contains(ware.getCurrentVer()))
					editor.remove(ware.getCurrentVer());
				if (prefs.contains("path"))
					editor.remove("path");
				editor.commit();
				break;
			}
		}
		if(c!= null){
			c.close();
		}
		return rate;
	}
	
	private void sendBroadCast(int rate){
		if(temp !=rate){
			temp = rate;
			Intent intent = new Intent(FwUpdateActivity.ACTION_DOWNLOADING);
			intent.putExtra("rate", rate);
			Log.i("caixinxin", "rate:"+rate);
			sendBroadcast(intent);		
		}
	}

}
