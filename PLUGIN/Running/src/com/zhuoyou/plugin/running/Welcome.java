package com.zhuoyou.plugin.running;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.zhuoyou.plugin.weather.WeatherTools;

public class Welcome extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.welcome);
		new InitTask().execute();
	}

	private class InitTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			// 实例化天气类
			WeatherTools.newInstance();

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {			
//			if(Tools.getShowCreateScDialog()){
//				View v = View.inflate(Welcome.this, R.layout.shortcut_view, null);
//				final CheckBox cb = (CheckBox)v.findViewById(R.id.sc_cb);
//				cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//					@Override
//					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//						Tools.setShowCreateScDialog(isChecked);
//					}
//				});
//				new AlertDialog.Builder(Welcome.this)
//				.setTitle(R.string.shortcut_title)
//				.setView(v)
//				.setNegativeButton(R.string.creat, new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						Tools.setShowCreateScDialog(cb.isChecked());
//						if(!checkShortcutExsit()){
//							createShortCut();
//						}else{
//							Toast.makeText(Welcome.this, R.string.shortcut_exsited, Toast.LENGTH_SHORT).show();
//						}
//						gotoMainScreen();
//					}
//				})
//				.setPositiveButton(R.string.cancle, new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						Tools.setShowCreateScDialog(cb.isChecked());
//						gotoMainScreen();
//					}
//				})
//				.setCancelable(false)
//				.create().show();
//			}else{
			gotoMainScreen();
//			}
		}
	}
	
	private void gotoMainScreen(){
		if (Tools.checkIsFirstEntry(Welcome.this)) {
			Intent intent = new Intent(Welcome.this, Guide.class);
			startActivity(intent);
		} else {
			Intent intent = new Intent(Welcome.this, Main.class);
			startActivity(intent);
		}
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		finish();
	}

	private boolean checkShortcutExsit() {
		boolean isInstallShortcut = false;
		ContentResolver cr = getContentResolver();
		String AUTHORITY = "com.android.launcher2.settings";
		Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");
		Cursor c = cr.query(CONTENT_URI, new String[] { "iconPackage" }, "iconPackage=?", new String[] { getApplication().getPackageName() }, null);
		if (c != null && c.getCount() > 0) {
			isInstallShortcut = true;
			c.close();
		}
		return isInstallShortcut;
	}
	
	/**
	 * 原生android launcher支持此种方式创建 koobee修改的frmeos步支持shortcut
	 */
	public void createShortCut() {
		Intent intent = new Intent();
		// install_shortcut action
		intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		// 点击shortcut时进入的activity，这里是自己
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(Welcome.this, Welcome.class));
		// shortcut的name
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
		Parcelable iconResource = Intent.ShortcutIconResource.fromContext(Welcome.this, R.drawable.ic_launcher);
		// shortcut的icon
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
		// 是否可以重复放置shortcut，默认true
		intent.putExtra("duplicate", false);
		sendBroadcast(intent);
	}
}
