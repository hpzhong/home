package com.zhuoyou.plugin.bluetooth.attach;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyou.plugin.bluetooth.data.PreferenceData;
import com.zhuoyou.plugin.bluetooth.service.BluetoothService;
import com.zhuoyou.plugin.custom.CustomAlertDialog;
import com.zhuoyou.plugin.running.R;

public class BTPluginActivity extends Activity {
	public static BTPluginActivity this_;
	private String mNickName = null;
	private String mRemoteName = "";
	private boolean mEnable;
	private Button mEdit;
	private PlugInAdapter mPlugInAdapter;
	private List<PlugBean> mPlugLists = new ArrayList<PlugBean>();
	private List<String> mInstalledPlugs= new ArrayList<String>();
	private List<String> mFileNames;
	private String filePath = null;
	private refreshBroadcast mRefreshBroadcast = null;
	private ProgressDialog mPb = null;
	private static final String PLUG_PACKAGENAME_PREX = "com.zhuoyou.plugin.";
	public static boolean isEditMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_plugin);
		this_ = this;

		Intent intent = getIntent();
		if (intent != null) {
			mNickName = intent.getStringExtra("nick_name");
			mRemoteName = intent.getStringExtra("remote_name");
			mEnable = intent.getBooleanExtra("enable_state", true);
		}

		initViews();

		if (mRefreshBroadcast == null) {
			mRefreshBroadcast = new refreshBroadcast();
			IntentFilter intentfilter = new IntentFilter();
			intentfilter.addAction("com.tyd.plugin.PLUGIN_LIST_REFRESH");
			registerReceiver(mRefreshBroadcast, intentfilter);
		}
		
		if (PreferenceData.isNotificationServiceEnable() && !BluetoothService.isNotificationServiceActived() && PluginManager.getInstance().hasNotication) {
			showAccessibilityPrompt();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		isEditMode = false;
		if (!isEditMode) {
			mEdit.setText(R.string.bt_edit);
		}
		buildData();
		checkPlug();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mRefreshBroadcast != null) {
			unregisterReceiver(mRefreshBroadcast);
		}
	}

	private void initViews() {
		RelativeLayout mBack = (RelativeLayout) findViewById(R.id.back);
		mBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}			
		});
		TextView tv = (TextView) findViewById(R.id.title_plug_name);
		tv.setText(mRemoteName);
		
		mEdit = (Button) findViewById(R.id.edit);
		mEdit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isEditMode) {
					isEditMode = false;
					mPlugInAdapter.notifyDataSetChanged();
					mEdit.setText(R.string.bt_edit);
				} else {
					if (mInstalledPlugs != null && mInstalledPlugs.size() > 0) {
						isEditMode = true;
						mPlugInAdapter.notifyDataSetChanged();
						mEdit.setText(R.string.it_is_ok);
					} else {
						Toast.makeText(this_, R.string.not_uninstall, Toast.LENGTH_SHORT).show();
					}
				}
			}			
		});
		
		ListView gv = (ListView) findViewById(R.id.plug_gv);
		mPlugInAdapter = new PlugInAdapter(this, mPlugLists);
		gv.setAdapter(mPlugInAdapter);
	}
	
	private void checkPlug() {
		getFilesList();
		if (mFileNames != null && mFileNames.size() > 0) {
			List<String> temp = new ArrayList<String>();
			temp.addAll(mFileNames);
			for (int i = 0; i < mFileNames.size(); i++) {
				String fileName = mFileNames.get(i);
				if (mInstalledPlugs != null && mInstalledPlugs.size() > 0) {
					for (int j = 0; j < mInstalledPlugs.size(); j++) {
						if (mInstalledPlugs.get(j).endsWith(fileName)) {
							temp.remove(fileName);
						}
					}
				}
			}
			if (temp != null && temp.size() > 0) {
				for (int n = 0; n < temp.size(); n++) {
					File file=new File(filePath+"/"+temp.get(n)+"/data");
					clear(file);
				}
			}
		}
	}
	
	private void getInstalledPlugs() {
		mInstalledPlugs.clear();
		PackageManager mPackageManager = this.getPackageManager();
		List<PackageInfo> pkgs = mPackageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		for (PackageInfo pkg : pkgs) {
			if (pkg.packageName.startsWith(PLUG_PACKAGENAME_PREX) && !pkg.packageName.equals(getPackageName())) {
				mInstalledPlugs.add(pkg.packageName);
			} else {
				continue;
			}
		}
	}
	
	private void getFilesList() {
		mFileNames = new ArrayList<String>();
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();
		} else {
			return;
		}
		
		String sd  = sdDir.toString();
		filePath = sd + "/Running" ;
		File file=new File(filePath);
		if(file.exists()) {
			File[] files = file.listFiles();
			for (File f : files) {
				if (f.isDirectory()) {
					mFileNames.add(f.getName());
				}
			}
		}
	}
	
	private void clear(File file) {
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
			} else if (file.isDirectory()) {
				File files[] = file.listFiles(); 
				for (int i = 0; i < files.length; i++) {
					this.clear(files[i]); 
	            }
			}
			file.delete();
		}
	}

	private void buildData() {
		getInstalledPlugs();
		mPlugLists = PluginManager.getInstance().getPlugBeans();
		if (mPlugLists == null) {
			PluginManager manager = PluginManager.getInstance();
			manager.processPlugList(mRemoteName);
			mPlugLists = PluginManager.getInstance().getPlugBeans();
		}
		if (mPlugInAdapter == null) {
			mPlugInAdapter = new PlugInAdapter(this, mPlugLists);
		}
		mPlugInAdapter.notify(mPlugLists);
	}
	
	private class refreshBroadcast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			buildData();
		}		
	}
	
	public static void onClick(int index) {
		this_.__onClick(index);
	}

	public static void onUninstallClick(int index) {
		this_.uninstall(index);
	}

	public void __onClick(int index) {
		if (isEditMode) 
			return;
		PlugBean bean = mPlugLists.get(index);
		if (bean.isSystem) {
			if (mEnable) {
				Intent intent = new Intent(bean.mMethod_Entry);
				intent.putExtra("product_name", mNickName);
				startActivity(intent);
			} else {
				Toast.makeText(this_, R.string.device_not_connected, Toast.LENGTH_LONG).show();
			}
		} else {
			if (bean.isInstalled) {
				if (mEnable) {
					if (bean.mMethod_Entry == null || bean.mMethod_Entry.length() == 0) {

					} else {
						Intent intent = new Intent(bean.mMethod_Entry);
						startActivity(intent);
					}
				} else {
					Toast.makeText(this_, R.string.device_not_connected, Toast.LENGTH_LONG).show();
				}
			} else if (bean.isPreInstall) {
				new InstallAsynTask().execute(bean.mPackageName);
			}
		}
	}
	
	public void uninstall(int index) {
		if (mPlugLists.get(index).isSystem || !mPlugLists.get(index).isInstalled) {
			return;
		}
		final String packageName = mPlugLists.get(index).mPackageName;
		PlugUtils.uninstallUseIntent(packageName, this_);
	}
	
	private class InstallAsynTask extends AsyncTask<String, String, Integer> {
		private String path = "";

		@Override
		protected void onPreExecute() {
			if (mPb == null) {
				mPb = new ProgressDialog(this_);
			}

			mPb.setCancelable(false);
			mPb.setMessage(getString(R.string.progress_plugin_wait));
			mPb.show();

			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(String... arg0) {
			String packageName = arg0[0];

			if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				return -5;
			}

			// 先把plug复制到内存卡中.
			AssetManager assets = getAssets();
			InputStream stream;
			try {
				stream = assets.open("preinstall/plug/" + packageName + ".plg");
			} catch (IOException e1) {
				e1.printStackTrace();
				return -1;
			}
			if (stream == null) {
				return -1;
			}

			String sdcard = Environment.getExternalStorageDirectory().getPath();
			boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
			if (sdCardExist) {
				sdcard = Environment.getExternalStorageDirectory().getPath();// 获取跟目录
			} else {
				sdcard = Environment.getDataDirectory().getPath();
			}

			if (PlugUtils.canCopy(sdcard)) {
				path = sdcard + "/" + packageName + ".apk";
				File file = new File(path);
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					return -3;
				}
				if (PlugUtils.writeStreamToFile(stream, file)) {
					return 0;
				} else {
					return -4;
				}
			} else {
				return -2;
			}
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			if (mPb.isShowing()) {
				mPb.dismiss();
			}

			switch (result) {
			case 0:
				Uri uri = Uri.fromFile(new File(path));
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(uri, "application/vnd.android.package-archive");
				startActivity(intent);
				break;
			case -1:
				Toast.makeText(this_, R.string.toast_cannot_find_plugin, Toast.LENGTH_SHORT).show();
				break;
			case -2:
				Toast.makeText(this_, R.string.toast_not_enough_space, Toast.LENGTH_SHORT).show();
				break;
			case -3:
				Toast.makeText(this_, R.string.toast_cannot_create_new_file, Toast.LENGTH_SHORT).show();
				break;
			case -4:
				Toast.makeText(this_, R.string.toast_cannot_copy_file, Toast.LENGTH_SHORT).show();
				break;
			case -5:
				Toast.makeText(this_, R.string.toast_close_usb_mass_mode, Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
			super.onPostExecute(result);
		}
	}
	
	private void showAccessibilityPrompt() {
		CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(this);
		builder.setTitle(R.string.accessibility_prompt_title);
		builder.setMessage(R.string.accessibility_prompt_content);

		// Cancel, do nothing
		builder.setNegativeButton(R.string.cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		// Go to accessibility settings
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				startActivity(NoticationActivity.ACCESSIBILITY_INTENT);
			}
		});
		builder.create().show();
	}
}
