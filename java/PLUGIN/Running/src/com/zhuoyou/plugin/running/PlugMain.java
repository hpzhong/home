package com.zhuoyou.plugin.running;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class PlugMain implements PlugInterface {
	private Context mCtx;
	private Context mNotiCtx;

	public PlugMain(Context ctx, Context notiCtx) {
		mCtx = ctx;
		mNotiCtx = notiCtx;
	}

	@Override
	public String getSupportCmd() {
		return "3|70|71|72|73|74|75";
	}

	@Override
	public String getName() {
		String name = mCtx.getString(R.string.app_name);
		return name;
	}

	@Override
	public Drawable getIcon() {
		Drawable da = mCtx.getResources().getDrawable(R.drawable.icon_selector);
		Log.i("gchk", "Drawable = " + da);
		return da;
	}

	@Override
	public String getEntryMethodName() {
		return "com.zhuoyou.plugin.running.welcome";
	}

	@Override
	public Map<String, String> getWorkMethodName() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("3", "battery");
		map.put("71", "sync");
		map.put("74", "sync_personal");
		return map;
	}

	public void sync_personal(char[] c_tag, String s_utf8){
		Log.i("gchk", "recevier 0X73 TAG[0] =" + c_tag[0] + "TAG[1]= " + c_tag[1] + "||| c= " + s_utf8);
		Intent intent = new Intent("com.zhuoyou.plugin.running.sync.personal");
		intent.putExtra("tag", c_tag);
		intent.putExtra("content", s_utf8);
		mCtx.sendBroadcast(intent);
	}
	
	/**
	 * remote tell me his data
	 */
	public void sync(char[] c_tag, String s_utf8) {
		Log.i("gchk", "recevier 0X71 TAG[0] =" + c_tag[0] + "TAG[1]= " + c_tag[1] + "||| c= " + s_utf8);
		Intent intent = new Intent("com.zhuoyou.plugin.running.get");
		intent.putExtra("tag", c_tag);
		intent.putExtra("content", s_utf8);
		mCtx.sendBroadcast(intent);
	}
	
	public void battery(char[] c_tag, String s_utf8) {
		Log.i("caixinxin", "11111111recevier 0X03 TAG[0] =" + c_tag[0] + "TAG[1]= " + c_tag[1] + "||| c= " + s_utf8);
		Intent intent = new Intent("com.tyd.bt.device.battery");
		intent.putExtra("tag", c_tag);
		mCtx.sendBroadcast(intent);
	}
}
