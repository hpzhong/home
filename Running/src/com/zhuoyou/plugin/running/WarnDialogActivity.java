package com.zhuoyou.plugin.running;


import java.io.IOException;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

public class WarnDialogActivity extends Activity {
	Button mConfirm;
	static MediaPlayer media;
	KeyguardLock mkeyguardLock;
	KeyguardManager mkeyguardManager;
	private DisplayMetrics dm;
	private static PowerManager.WakeLock timerWakeLock;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		final Window win = getWindow();
		int flags1 = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
		int flags2 = WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
		int flags3 = WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
		int flags4 = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		win.addFlags(flags1|flags2|flags3|flags4);
		win.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		/* Sensei the screen */
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		timerWakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
				| PowerManager.FULL_WAKE_LOCK, "--");
		timerWakeLock.acquire();
		/* unlock the screen lock */
		mkeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		mkeyguardLock = mkeyguardManager.newKeyguardLock("");
		mkeyguardLock.disableKeyguard();
		setContentView(R.layout.water_drink_dialog);
		mConfirm = (Button) findViewById(R.id.confirm);
		mConfirm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mkeyguardLock.reenableKeyguard();
				if(media != null && media.isPlaying()){
	     			media.stop();
		
		}
				finish();
			}
		});
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		Uri uri= RingtoneManager.getActualDefaultRingtoneUri(this,
//                RingtoneManager.TYPE_ALARM);
//		Uri uri = Uri.parse("content://media/internal/audio/media/10");

//		media = MediaPlayer.create(WarnDialogActivity.this, uri);
		media=MediaPlayer.create(WarnDialogActivity.this, R.raw.water_voice);
		
		if(!IntakeThread.isAlive()){
			IntakeThread.start();
		}
		
	
	}
	private static Thread IntakeThread=new Thread(){
		public void run() {
			// TODO Auto-generated method stub
			media.start();
			media.setLooping(true);
			
			try {
				sleep(80000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	            if(media != null && media.isPlaying()){
	     			media.stop();
		
		}}
 };
			 
		
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if(media != null && media.isPlaying()){
			media.stop();
		}
		if (timerWakeLock != null) {
			timerWakeLock.release();
			timerWakeLock = null;
		}
		mkeyguardLock.reenableKeyguard();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(media != null && media.isPlaying()){
			media.stop();
		}
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if(media != null && media.isPlaying()){
			media.stop();
			media.release();
			media=null;
		}
		if (timerWakeLock != null) {
			timerWakeLock.release();
			timerWakeLock = null;
		}
		mkeyguardLock.reenableKeyguard();
	}


}
