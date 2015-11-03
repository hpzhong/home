package com.zhuoyou.plugin.autocamera;

import java.util.HashMap;
import java.util.Map;

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
		return "50|52|54";
	}

	@Override
	public String getName() {
		String name = mCtx.getString(R.string.app_name);
		return name;
	}

	@Override
	public Drawable getIcon() {
		Drawable da = mCtx.getResources().getDrawable(R.drawable.autocamera_selector);
		Log.i("gchk", "Drawable = " + da);
		return da;
	}

	@Override
	public String getEntryMethodName() {
		return "com.zhuoyou.autocamera.main";
	}

	@Override
	public Map<String, String> getWorkMethodName() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("50", "capture");
		map.put("52", "entryPreview");
		map.put("54", "exit");
		return map;
	}

	public void entryPreview() {
		String ret = PlugTools.getDataString(mCtx, "screen");
		Log.i("gchk", "capture ret = " + ret);
		if (ret != null && ret.equals("Main")) {
			Log.i("gchk", "has been in preview");
			Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
			intent.putExtra("plugin_cmd", 0x53);
			intent.putExtra("plugin_content", "entryPreview");
			mCtx.sendBroadcast(intent);
		} else {
			Log.i("gchk", "entryPreview");
			Intent intent = new Intent("com.zhuoyou.autocamera.main");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("isCmd", true);
			mNotiCtx.startActivity(intent);
		}
	}

	public void capture() {
		Log.i("gchk", "capture 111111");
		// start preview activity
		String ret = PlugTools.getDataString(mCtx, "screen");
		Log.i("gchk", "capture ret = " + ret);
		if (ret != null && ret.equals("Main")) {
			Log.i("gchk", "capture ___2222____");
			Intent intent = new Intent("com.zhoyou.plugin.autocamera.capture");
			mCtx.sendBroadcast(intent);
		} else {
			Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
			intent.putExtra("plugin_cmd", 0x51);
			intent.putExtra("plugin_content", "capture");
			intent.putExtra("plugin_tag", Main.getTag());
			mCtx.sendBroadcast(intent);
		}
	}

	public void exit() {
		String ret = PlugTools.getDataString(mCtx, "screen");
		Log.i("gchk", "capture ret = " + ret);
		// if (ret != null && ret.equals("Main")) {
		Log.i("gchk", "exit main");
		Intent intent = new Intent("com.zhoyou.plugin.autocamera.exit.main");
		intent.putExtra("reactive", true);
		mCtx.sendBroadcast(intent);
		// }
	}
}
