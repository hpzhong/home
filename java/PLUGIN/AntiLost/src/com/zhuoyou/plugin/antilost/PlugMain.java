package com.zhuoyou.plugin.antilost;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.zhuoyou.plugin.base.CustomAlertDialog;
import com.zhuoyou.plugin.base.PlugInterface;

public class PlugMain implements PlugInterface {
	private Context mCtx;
	private Context mNotificationCtx ;
	private boolean process = true;
	private boolean antilostProcess = true;
	private int index = 0x00;
	private static final int isLookingForMobile = 0x01;
	private static final int isAntiLostOn = 0x02;
	private Vibrator vibrator;
	private MediaPlayer mp;
	private Uri musicTableForSD = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	private String musicTitle = MediaStore.Audio.AudioColumns.TITLE;
	private String musicId = MediaStore.Audio.Media._ID;
	private String musicName = "";
	private AudioManager audioManager;
	private PowerManager pm;
	private PowerManager.WakeLock wl;
	private int mRingType = AudioManager.STREAM_ALARM;
	private CustomAlertDialog dialog = null;
	private CustomAlertDialog passwordDialog = null;
	private CustomAlertDialog passwordDialog2 = null;
	
	public PlugMain(Context ctx , Context mNotiCtx) {
		mCtx = ctx;
		mNotificationCtx = mNotiCtx;
		audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
		pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "AntiLost");

		if (dialog == null) {
			createSystemDialog();
		}
	}

	private void createSystemDialog() {
		dialog = new CustomAlertDialog.Builder(mCtx).setTitle(R.string.alert_title).setMessage(R.string.alert_message).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
				intent.putExtra("plugin_cmd", 0x11);
				intent.putExtra("plugin_content", "found");
				mCtx.sendBroadcast(intent);
				off();
			}
		}).setCancelable(false).create();
		dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
	}

	private void createPasswordDialog() {
		LayoutInflater factory = LayoutInflater.from(mCtx);
		final View view = factory.inflate(R.layout.layout_dialog_edit, null);
		passwordDialog = new CustomAlertDialog.Builder(mCtx).setTitle(R.string.input_antilost_password)
								.setContentView(view)
								.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										EditText dialog_edit = (EditText) view.findViewById(R.id.dialog_edit);
										String input = dialog_edit.getText().toString();
										String password = PlugTools.getDataString(mCtx, "password");
										Log.i("gchk", "input = " + input);
										Log.i("gchk", "password = " + password);
										if (input != null && input.length() > 0) {
											if (input.equals(password)) {
												dialog_edit.setText("");
												dialog.dismiss();
												Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
												intent.putExtra("plugin_cmd", 0x15);
												intent.putExtra("plugin_content", "found");
												mCtx.sendBroadcast(intent);
												antilostEnd();
											}
										}
									}
								})
								.setCancelable(false).create();
		passwordDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
		passwordDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}
	
	private void createPasswordDialog2() {
		passwordDialog2 = new CustomAlertDialog.Builder(mCtx).setTitle(R.string.alert_title).setMessage(R.string.alert_message).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
				intent.putExtra("plugin_cmd", 0x15);
				intent.putExtra("plugin_content", "found");
				mCtx.sendBroadcast(intent);
				antilostEnd();
			}
		}).setCancelable(false).create();
		passwordDialog2.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
	}
	
	@Override
	public String getSupportCmd() {
		return "10|11|12|13|14|15|17";
	}

	@Override
	public String getName() {
		String name = mCtx.getString(R.string.app_name);
		return name;
	}

	@Override
	public Drawable getIcon() {
		Drawable da = mCtx.getResources().getDrawable(R.drawable.antilost_selector);
		Log.i("gchk", "Drawable = " + da);
		return da;
	}

	@Override
	public String getEntryMethodName() {
		return "com.zhuoyou.antilost.main";
	}

	@Override
	public Map<String, String> getWorkMethodName() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("10", "on");
		map.put("11", "off");
		map.put("12", "antilostOn");
		map.put("13", "antilostOff");
		map.put("14", "antilostStart");
		map.put("15", "antilostEnd");
		return map;
	}

	public void on() {
		if (process) {
			process = false;
			if (wl != null) {
				wl.acquire();
				Log.i("gchk", "antilost(on) wl acquire");
			} else {
				Log.i("gchk", "antilost(on) wl is null");
			}
			Log.i("gchk", "index = " + index);
			if (index == 0) {
				stopMP();
				pauseMusic();
				startMP();
				vibrate();
				Log.i("gchk", "playMusic find mobile");
			}

			index |= isLookingForMobile;

			if (dialog == null) {
				createSystemDialog();
			}

			if (dialog != null && !dialog.isShowing()) {
				dialog.show();
				Log.i("gchk", "antilost(on) show dialog"); 
			}
		} else {
			Log.i("gchk", "error error error error"); 
		}
	}

	public void antilostStart() {
		if (antilostProcess) {
			antilostProcess = false;
			if (wl != null) {
				wl.acquire();
				Log.i("gchk", "antilost(on) wl acquire");
			} else {
				Log.i("gchk", "antilost(on) wl is null");
			}

			Log.i("gchk", "index = " + index);
			if (index == 0) {
				Log.i("gchk", "playMusic antilost");
				stopMP();
				pauseMusic();
				startMP();
				vibrate();
			}

			index |= isAntiLostOn;
		
			String passWord = PlugTools.getDataString(mCtx, "password");
			if (passWord != null && !passWord.equals("****")) {
				if (passwordDialog == null) {
					createPasswordDialog();
				}

				if (passwordDialog != null && !passwordDialog.isShowing()) {
					passwordDialog.show();
					Log.i("gchk", "antilost(start) show dialog"); 
				}
			} else {
				if (passwordDialog2 == null) {
					createPasswordDialog2();
				}

				if (passwordDialog2 != null && !passwordDialog2.isShowing()) {
					passwordDialog2.show();
					Log.i("gchk", "antilost(start) show dialog2"); 
				}
			}
		}
	}
	
	public void off() {
		process = true;
		index &= ~isLookingForMobile;

		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
			Log.i("gchk", "antilost(off) dialog !=null ,release");
		} else {
			Log.i("gchk", "antilost(off) dialog ==null , error check");
		}
		Log.i("gchk", "index = " + index);
		if (index == 0) {
			stopMP();
			stopVibrate();
			Log.i("gchk", "stopMusic");
		}

		if (wl != null) {
			wl.setReferenceCounted(false);
			wl.release();
			Log.i("gchk", "antilost(off) wl !=null ,release");
		} else {
			Log.i("gchk", "antilost(off) wl ==null ,error check");
		}
	}

	public void antilostEnd() {
		antilostProcess = true;
		index &= ~isAntiLostOn;

		if (passwordDialog != null && passwordDialog.isShowing()) {
			passwordDialog.dismiss();
			passwordDialog = null;
			Log.i("gchk", "antilost(end) dialog !=null ,release");
		}

		if (passwordDialog2 != null && passwordDialog2.isShowing()) {
			passwordDialog2.dismiss();
			passwordDialog2 = null;
			Log.i("gchk", "antilost(end) dialog2 !=null ,release");
		}
		Log.i("gchk", "index = " + index);
		if (index == 0) {
			stopMP();
			stopVibrate();
			Log.i("gchk", "stopMusic2");
		}

		if (wl != null) {
			wl.setReferenceCounted(false);
			wl.release();
			Log.i("gchk", "antilost(off) wl !=null ,release");
		} else {
			Log.i("gchk", "antilost(off) wl ==null ,error check");
		}
	}
	
	private MediaPlayer getMp(int resid, Context ctx) {
		MediaPlayer _mp = null;
		int vol = audioManager.getStreamMaxVolume(mRingType);
		Log.e("gchk", "max vol -= " + vol);
		audioManager.setMode(AudioManager.MODE_NORMAL);
		audioManager.setStreamVolume(mRingType, vol, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

		try {
			_mp = new MediaPlayer();
			_mp.setAudioStreamType(mRingType);
			AssetFileDescriptor afd = ctx.getResources().openRawResourceFd(R.raw.music1);
			_mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
			afd.close();
			_mp.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return _mp;
	}

	private MediaPlayer getMp(Uri uri, Context ctx) {
		MediaPlayer _mp = null;
		int vol = audioManager.getStreamMaxVolume(mRingType);
		Log.e("gchk", "max vol -= " + vol);
		audioManager.setMode(AudioManager.MODE_NORMAL);
		audioManager.setStreamVolume(mRingType, vol, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

		try {
			_mp = new MediaPlayer();
			_mp.setAudioStreamType(mRingType);
			_mp.setDataSource(ctx, uri);
			_mp.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return _mp;
	}
	
	private void pauseMusic() {
		Intent freshIntent = new Intent();
		freshIntent.setAction("com.android.music.musicservicecommand.pause");
		freshIntent.putExtra("command", "pause");
		mCtx.sendBroadcast(freshIntent);
	}
	
	private void startMP() {
		musicName = PlugTools.getDataString(mCtx, "music");
		Log.i("gchk", "anti lost musicname = " + musicName);

		if (musicName != null && musicName.trim().length() > 0) {
			Cursor cursor = mCtx.getContentResolver().query(musicTableForSD, new String[] { musicId, musicTitle }, musicTitle + " LIKE ?", new String[] { musicName }, null);
			if (cursor.getCount() > 0) {
				if (cursor.moveToNext()) {
					int position = cursor.getInt(cursor.getColumnIndex(musicId));
					Uri uri = Uri.withAppendedPath(musicTableForSD, "/" + position);
					mp = getMp(uri, mCtx);
				}
				cursor.close();
			} else {
				mp = getMp(R.raw.music1, mCtx);
			}
		} else {
			mp = getMp(R.raw.music1, mCtx);
		}

		try {
			mp.setLooping(true);
			mp.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void stopMP() {
		if (mp != null) {
			mp.stop();
			mp.reset();
			mp.release();
			mp = null;
			Log.i("gchk", "mp !=null ,must release");
		}
	}
	
	private void vibrate() {
		if (vibrator != null) {
			vibrator.cancel();
			Log.i("gchk", "vibrator !=null ,must release");
		}

		vibrator = (Vibrator) mCtx.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(new long[] { 100, 3000, 100, 3000 }, 0);
	}
	
	private void stopVibrate() {
		if (vibrator != null) {
			vibrator.cancel();
			Log.i("gchk", "vibrator !=null ,must release");
		}
	}
	
	public void antilostOn() {
		if (PlugTools.saveDataBoolean(mCtx, "switch", true)) {
			Intent intent = new Intent("com.zhoyou.plugin.antilost.changestatus");
			intent.putExtra("status", true);
			mCtx.sendBroadcast(intent);
		} else {
			Log.i("gchk", "saveDataBoolean failed");
		}
	}
	
	public void antilostOff() {
		antilostEnd();
		if (PlugTools.saveDataBoolean(mCtx, "switch", false)) {
			Intent intent = new Intent("com.zhoyou.plugin.antilost.changestatus");
			intent.putExtra("status", false);
			mCtx.sendBroadcast(intent);
		}
	}
}
