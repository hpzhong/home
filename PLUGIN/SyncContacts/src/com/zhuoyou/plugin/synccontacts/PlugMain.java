package com.zhuoyou.plugin.synccontacts;

import java.util.HashMap;
import java.util.Map;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.zhuoyou.plugin.base.PlugInterface;

public class PlugMain implements PlugInterface {
	private Context mCtx;
	private Context mNotiCtx;

	public PlugMain(Context ctx, Context notiCtx) {
		mCtx = ctx;
		mNotiCtx = notiCtx;
	}

	@Override
	public String getSupportCmd() {
		return "60|61";
	}

	@Override
	public String getName() {
		String name = mCtx.getString(R.string.app_name);
		return name;
	}

	@Override
	public Drawable getIcon() {
		Drawable da = mCtx.getResources().getDrawable(R.drawable.contacts_selector);
		Log.i("gchk", "Drawable = " + da);
		return da;
	}

	@Override
	public String getEntryMethodName() {
		return "com.zhuoyou.plugin.synccontacts.main";
	}

	@Override
	public Map<String, String> getWorkMethodName() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("60", "require");
		map.put("61", "sync");
		return map;
	}

	private static boolean isScreenLocked(Context context) {
		KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		Boolean isScreenLocked = km.inKeyguardRestrictedInputMode();

		Log.i("gchk", "isScreenOn(), isScreenOn=" + isScreenLocked);
		return isScreenLocked;
	}
	/**
	 * remote need me organized data to send 
	 */
	public void require(char[] tag , String content) {
		Log.i("gchk", "require = " + content);
		
		if(isScreenLocked(mCtx)){
			return;
		}
		
		//get backup and send to remote 
		new LoadBackUpTask(mCtx).execute();
	}
	
	/**
	 * remote tell me his data
	 */
	public void sync(char[] c_tag , String s_utf8){
		Log.i("gchk", "recevier 0X61 TAG[0] =" + c_tag[0] + "TAG[1]= " + c_tag[1] + "||| c= " + s_utf8);
		if(isScreenLocked(mCtx)){
			return;
		}
		Intent intent = new Intent("com.zhuoyou.plugin.synccontacts.get");
		intent.putExtra("tag", c_tag);
		intent.putExtra("content", s_utf8);
		mCtx.sendBroadcast(intent);
	}
}
