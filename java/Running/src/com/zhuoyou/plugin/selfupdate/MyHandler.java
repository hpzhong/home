package com.zhuoyou.plugin.selfupdate;

import java.io.File;
import java.util.HashMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.zhuoyou.plugin.custom.CustomAlertDialog;
import com.zhuoyou.plugin.download.DownloadService;
import com.zhuoyou.plugin.download.Util_update;
import com.zhuoyou.plugin.running.Main;
import com.zhuoyou.plugin.running.R;

public class MyHandler extends Handler {
	public static final int MSG_UPDATE_START = 1000;
	public static final int MSG_UPDATE_VIEW = MSG_UPDATE_START + 1;

	private Context mCtx = null;
	
	private boolean isHand = false;

	public MyHandler(Context ctx) {
		super();
		mCtx = ctx;
	}
	public MyHandler(Context ctx,boolean isHand) {
		super();
		mCtx = ctx;
		this.isHand = isHand;
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);

		switch (msg.what) {
		case MSG_UPDATE_START:
			break;
		case MSG_UPDATE_VIEW:
			// get hashmap form message
			HashMap<String, Object> map = (HashMap<String, Object>) msg.obj;
			if (map == null) {
				if(isHand){
					Toast.makeText(mCtx, mCtx.getResources().getString(R.string.islasted_version), Toast.LENGTH_LONG).show();
				}
				return;
			}

			// check policy
			int policy = (Integer) map.get("policy");
			String content = (String) map.get("content");
			String url = (String) map.get("fileUrl");
			String version = (String) map.get("ver");
			final String data = url + "&" + version;
			Log.i("gchk", "data = " + data);
			/**
			 * policy : 1:强制更新 2:提示更新 3:不更新 4:后台更新 we only use 1 here
			 */
			if (policy == 3) {
				if(isHand){
					Toast.makeText(mCtx, mCtx.getResources().getString(R.string.islasted_version), Toast.LENGTH_LONG).show();
				}
				return;
			} else {
				/**
				 * 如果有更新.跳转到下载模块去处理.
				 */
				Log.i("gchk", "need update policy =" + policy + " ||content = " + content + " ||");

				/**
				 * CHECK
				 */
				final String sdcardPath = Util_update.FileManage.getSDPath();
				Log.i("gchk", "sdcardPath  = " + sdcardPath);

				if (sdcardPath == null) {
					Toast.makeText(mCtx, R.string.please_insert, Toast.LENGTH_SHORT).show();
					return;
				}
				Util_update.FileManage.FileHolder fileHolder = Util_update.FileManage.readSDCardSpace();
				if (fileHolder != null && fileHolder.availSpace < 1024 * 10) {
					Toast.makeText(mCtx, R.string.space_not_enough, Toast.LENGTH_SHORT).show();
					return;
				}

				/**
				 * 检查是否有新版本已经下载过了.上次没安装而已.
				 */
				String path = Util_update.FileManage.getSDPath() + Constants.DownloadApkPath + 
						mCtx.getResources().getString(R.string.app_name) + ".apk";
				File f = new File(path);
				if (f.exists()) {
					/**
					 * check version code
					 
					try {
						PackageInfo pinfo = mCtx.getPackageManager().getPackageInfo(mCtx.getPackageName(), PackageManager.GET_CONFIGURATIONS);
						int v = pinfo.versionCode;
						if (v < Integer.parseInt(version)) {
							showAlertDialog(policy, content, path, data, true);
						}
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}*/
					f.delete();
				} 
				showAlertDialog(policy, content, sdcardPath, data, false);
			}
			break;
		default:
			break;
		}
	}

	private void showAlertDialog(int policy, String content, final String sdcardPath, final String data, final boolean exsit) {
		/**
		 * show alert dialog
		 */
		int yes_id = 0;
		int no_id = 0;
		if (exsit) {
			yes_id = R.string.alert_btn_install;
			no_id = R.string.alert_btn_no_install;
		} else {
			yes_id = R.string.alert_btn_yes;
			no_id = R.string.alert_btn_no;
		}

		CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(mCtx);
		builder.setTitle(R.string.alert_title);
		if(TextUtils.isEmpty(content)){
			builder.setMessage(R.string.alert_msg);
		}else{
			builder.setMessage(content);
		}

		if (policy == 1) {
			//强制更新
			builder.setPositiveButton(yes_id, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (exsit) {
						Util_update.AppInfoManager.AppInstall(sdcardPath, mCtx);
					} else {
						Intent downloadServiceIntent = new Intent(mCtx, DownloadService.class);
						downloadServiceIntent.putExtra("sdPath", sdcardPath);
						downloadServiceIntent.putExtra("url", data);
						downloadServiceIntent.putExtra("appName", mCtx.getResources().getString(R.string.app_name));
						ComponentName name = mCtx.startService(downloadServiceIntent);
						Log.i("gchk", "start download thread end name=" + name.getClassName());
					}
					dialog.dismiss();
				}
			});
			builder.setNegativeButton(no_id, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Main.mHandler.sendEmptyMessage(Main.SHUT_DOWN_APP);
				}
			});
			builder.setCancelable(false);
			builder.create().show();
		} else if (policy == 2) {
			builder.setPositiveButton(yes_id, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (exsit) {
						Util_update.AppInfoManager.AppInstall(sdcardPath, mCtx);
					} else {
						Intent downloadServiceIntent = new Intent(mCtx, DownloadService.class);
						downloadServiceIntent.putExtra("sdPath", sdcardPath);
						downloadServiceIntent.putExtra("url", data);
						downloadServiceIntent.putExtra("appName", mCtx.getResources().getString(R.string.app_name));
						ComponentName name = mCtx.startService(downloadServiceIntent);
						SelfUpdateMain.isDownloading = true;
					}
					dialog.dismiss();
				}
			});
			builder.setNegativeButton(no_id, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.setCancelable(false);
			builder.create().show();
		} else if (policy == 4) {
			Intent downloadServiceIntent = new Intent(mCtx, DownloadService.class);
			downloadServiceIntent.putExtra("sdPath", sdcardPath);
			downloadServiceIntent.putExtra("url", data);
			downloadServiceIntent.putExtra("appName", mCtx.getResources().getString(R.string.app_name));
			ComponentName name = mCtx.startService(downloadServiceIntent);
			Log.i("gchk", "start download thread end name=" + name.getClassName());
		}
	}

}
