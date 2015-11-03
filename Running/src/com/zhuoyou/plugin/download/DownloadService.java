package com.zhuoyou.plugin.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.selfupdate.Constants;
import com.zhuoyou.plugin.selfupdate.SelfUpdateMain;

public class DownloadService extends Service {
	public static final String Tag = "gchk";

	// notification
	NotificationManager notificationManager;

	Notification mNotification;

	Intent intent2;

	PendingIntent contentIntent;

	String version;

	// DownloadService instance
	public static DownloadService downloadServiceInstance;

	// 存放各个下载器
	public static Map<String, Downloader> downloaders = new HashMap<String, Downloader>();

	// 初始化一个downloader下载器
	Downloader downloader;

	String sdPathString;

	@Override
	public void onCreate() {
		Log.e(Tag, "download service onCreate");
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		downloadServiceInstance = this;
		mNotification = new Notification(R.drawable.plug_download_m_icon, getString(R.string.has_new_download), System.currentTimeMillis());
		// initiate download folder
		sdPathString = Util_update.FileManage.getSDPath();

		if (null != sdPathString) {
			Util_update.FileManage.newFolder(sdPathString + Constants.DownloadPath);
		}
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	String urlstr;

	private String mTempPath;

	private String mApkPath;

	String appName;

	byte[] bitmap;

	String size;

	private String mVersion = "";

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e(Tag, "**************start download service onStartCommand");

		if (intent == null) {
			return super.onStartCommand(intent, flags, startId);
		}

		String dataString = intent.getStringExtra("url");
		String[] dataStrings = dataString.split("&");
		urlstr = dataStrings[0];
		mVersion = dataStrings[1];

		downloader = downloaders.get(urlstr);

		appName = intent.getStringExtra("appName");
		bitmap = intent.getByteArrayExtra("bitmap");
		version = intent.getStringExtra("version");

		Log.e("version", "version + service " + version);
		size = intent.getStringExtra("size");
		mTempPath = sdPathString + Constants.DownloadPath + appName + ".tmp";
		mApkPath = sdPathString + Constants.DownloadApkPath + appName + ".apk";

		String folder = sdPathString + Constants.DownloadPath;
		File f = new File(folder);
		if (!f.exists()) {
			f.mkdir();
		}

		/**
		 * delete the temporary file if it existed
		 */
		File tmpFile = new File(mTempPath);
		if (tmpFile.exists()) {
			tmpFile.delete();
		}

		int notifaction_flag = (int) System.currentTimeMillis();
		downloader = new Downloader(this, urlstr, mTempPath, mHandler, notifaction_flag, appName, bitmap, size, version);
		downloaders.put(urlstr, downloader);

		Log.e(Tag, "-----init");
		new DownloaderThread(downloader, this).start();

		Log.e(Tag, "**************end download service onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		SelfUpdateMain.isDownloading = false;
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 2:
				String split_string = (String) msg.obj;
				String temp_str[] = split_string.split(",");
				String url1 = temp_str[0];
				long Surplus_size = Long.parseLong(temp_str[1]);
				if (null == downloaders.get(url1)) {
					return;
				}

				int notifaction_flag = (Integer) msg.arg1;
				int rate = msg.arg2;

				String appName = downloaders.get(url1).getAppName();
				mNotification.setLatestEventInfo(DownloadService.this, appName, getResources().getString(R.string.Surplus) + humanReadableByteCount(Surplus_size, true), null);
				notificationManager.notify(notifaction_flag, mNotification);

				if (rate >= 100) {
					notificationManager.cancel(notifaction_flag);
					SelfUpdateMain.isDownloading = false;
				}
				break;
			case 3:
				String url = (String) msg.obj;
				String localPath = downloaders.get(url).getLocalfile();
				String mappName = downloaders.get(url).getAppName();
				downloaders.remove(url);
				if (null != downloaders && downloaders.size() == 0) {
					stopSelf();
				}

				/**
				 * copy temporary file to download folder and deleted
				 */
				copyFile(localPath, mApkPath);
				/**
				 * delete old path
				 */
				File old = new File(localPath);
				if (old.exists()) {
					old.delete();
				}

				Intent i = new Intent();
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.setAction(android.content.Intent.ACTION_VIEW);
				i.setDataAndType(Uri.fromFile(new File(mApkPath)), "application/vnd.android.package-archive");
				contentIntent = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(), i, PendingIntent.FLAG_ONE_SHOT);
				mNotification.flags = Notification.FLAG_AUTO_CANCEL;
				mNotification.setLatestEventInfo(DownloadService.this, mappName, getString(R.string.download_notification), contentIntent);
				notificationManager.notify(1, mNotification);
				Util_update.AppInfoManager.AppInstall(mApkPath, DownloadService.this);
				break;
			case 4:
				// 提示用户出错
				Toast.makeText(DownloadService.this, getString(R.string.canot_getsize), 1000).show();
				break;
			}
		}
	};

	/**
	 * 复制单个文件
	 * 
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf.txt
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 */
	public void copyFile(String oldPath, String newPath) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);

			if (oldfile.exists()) {
				InputStream inStream = new FileInputStream(oldPath); // 读入原文件
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				int length;
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // 字节数 文件大小
					System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				fs.close();
			}
		} catch (Exception e) {
			System.out.println("复制单个文件操作出错");
			e.printStackTrace();
		}
	}

	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.2f %sB", bytes / Math.pow(unit, exp), pre);
	}

}
