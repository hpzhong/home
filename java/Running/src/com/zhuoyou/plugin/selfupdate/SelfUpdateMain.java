package com.zhuoyou.plugin.selfupdate;

import android.content.Context;

public class SelfUpdateMain {
	public static boolean isDownloading = false;
	public static void execApkSelfUpdateRequest(Context context, String appid, String chnid) {
		MyHandler h = new MyHandler(context);
		new RequestAsyncTask(context, h, MyHandler.MSG_UPDATE_VIEW, appid, chnid).startRun();
	}
}
