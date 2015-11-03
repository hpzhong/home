package com.zhuoyou.plugin.antilost;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyou.plugin.base.CustomProgressDialog;
import com.zhuoyou.plugin.setting.SettingActivity;

public class Main extends Activity {
	private Context mContext;
	private TextView mTextView;
	private TextView mAntilost_switch;
	private ImageView mBack;
	private TextView mDefaultView;
	private ImageView mImageView;
	private ListView mListView;
	private Uri musicTableForSD = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	private String musicTitle = MediaStore.Audio.AudioColumns.TITLE;
	private String musicId = MediaStore.Audio.Media._ID;
	private String musicData = MediaStore.Audio.Media.DATA;
	private String musicName = "";
	private int[] mIds = null;
	private String[] mTitles = null;
	private String[] mPath = null;
	private ListAdapter mListAdapter;
	private boolean isOpen;
	public static Handler mHandler;
	public static final int SELECT_MUSIC_IN_SD = 1;
	private static final String mChangeStatus = "com.zhoyou.plugin.antilost.changestatus";
	private CustomProgressDialog mDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;

		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case SELECT_MUSIC_IN_SD:
					mImageView.setBackgroundResource(R.drawable.setlist_radio_off);
					break;
				case 1008:
					Toast.makeText(Main.this, R.string.get_data_error, Toast.LENGTH_SHORT).show();
					new LoadMusicListTask().execute();
					break;
				}
			}
		};
		initView();
	}

	private void initView() {
		ImageView mSetting = (ImageView) findViewById(R.id.title_setting);
		mSetting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, SettingActivity.class);
				startActivity(intent);
			}
		});
		mTextView = (TextView) findViewById(R.id.myTextView);
		mAntilost_switch = (TextView) findViewById(R.id.antilost_switch);
		isOpen = PlugTools.getDataBoolean(mContext, "switch", false);
		if (isOpen == true) {
			mAntilost_switch.setText(R.string.on);
		} else {
			mAntilost_switch.setText(R.string.off);
		}
		mBack = (ImageView) findViewById(R.id.back);
		mBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mDefaultView = (TextView) findViewById(R.id.defaultView);
		mImageView = (ImageView) findViewById(R.id.imageview);
		mDefaultView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mImageView.setBackgroundResource(R.drawable.setlist_radio_on);
				PlugTools.saveDataString(mContext, "music", "");
				mListAdapter.setMyList(mTitles);
			}
		});
		mImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mImageView.setBackgroundResource(R.drawable.setlist_radio_on);
				PlugTools.saveDataString(mContext, "music", "");
				mListAdapter.setMyList(mTitles);
			}
		});
		mListView = (ListView) findViewById(R.id.music_list);

		mDialog = CustomProgressDialog.createDialog(Main.this);
		mDialog.setCancelable(false);
		mDialog.setMessage(getString(R.string.progress_dialog_message));

		getRemoteData();
	}

	private void getRemoteData() {
		mDialog.show();

		Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
		intent.putExtra("plugin_cmd", 0x16);
		intent.putExtra("plugin_content", "himan");
		sendBroadcast(intent);
		Message msg = new Message();
		msg.what = 1008;
		mHandler.sendMessageDelayed(msg, 10000);
	}

	private class LoadMusicListTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			// gchk add:workround:TODO dismiss in mReceiver
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Cursor c = getContentResolver().query(musicTableForSD, new String[] { musicId, musicTitle, musicData }, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
			if (c.moveToFirst()) {
				mIds = new int[c.getCount()];
				mTitles = new String[c.getCount()];
				mPath = new String[c.getCount()];
				for (int i = 0; i < c.getCount(); i++) {
					mIds[i] = c.getInt(0);
					mTitles[i] = c.getString(1);
					mPath[i] = c.getString(2).substring(8);
					c.moveToNext();
				}
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (mDialog != null && mDialog.isShowing()) {
				mDialog.dismiss();
				mDialog = null;
			}

			if (mTitles != null && mTitles.length > 0) {
				mListAdapter = new ListAdapter(mContext, mTitles, mListView);
				mListView.setAdapter(mListAdapter);

				musicName = PlugTools.getDataString(mContext, "music");
				if (musicName != null && musicName.trim().length() > 0) {
					Cursor cursor = getContentResolver().query(musicTableForSD, new String[] { musicId, musicTitle }, musicTitle + " LIKE ?", new String[] { musicName }, null);
					if (cursor.getCount() > 0) {
						if (cursor.moveToNext()) {
							mImageView.setBackgroundResource(R.drawable.setlist_radio_off);
						}
						cursor.close();
					}
				} else {
					mImageView.setBackgroundResource(R.drawable.setlist_radio_on);
				}
			} else {
				mTextView.setVisibility(View.VISIBLE);
				mListView.setVisibility(View.GONE);
				mImageView.setBackgroundResource(R.drawable.setlist_radio_on);
			}
		}
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(mChangeStatus)) {
				boolean status = intent.getBooleanExtra("status", false);
				if (mAntilost_switch != null) {
					if (status == true) {
						mAntilost_switch.setText(R.string.on);
					} else {
						mAntilost_switch.setText(R.string.off);
					}
				}
				mHandler.removeMessages(1008);
				new LoadMusicListTask().execute();
			}
		}
	};

	private void registerBc() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(mChangeStatus);
		registerReceiver(mReceiver, intentFilter);
	}

	private void unRegisterBc() {
		unregisterReceiver(mReceiver);
	}

	@Override
	public void onPause() {
		super.onPause();
		unRegisterBc();
	}

	@Override
	public void onResume() {
		super.onResume();
		registerBc();
	}
}
