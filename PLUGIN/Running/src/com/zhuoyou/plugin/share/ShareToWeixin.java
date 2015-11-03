package com.zhuoyou.plugin.share;

import android.content.Context;
import android.graphics.Bitmap;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.platformtools.Util;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;

public class ShareToWeixin {

	public static final String APP_ID = "wx9e68ecf43e6b8493";
	public static final String APP_KEY = "90aeacf25a411cc51265c0161803daa5";
	public static IWXAPI api = WXAPIFactory.createWXAPI(RunningApp.getInstance().getApplicationContext(), APP_ID, true);;
	private static final int THUMB_SIZE = 150;

	public static void regToWx(){
		api.registerApp(APP_ID);
	}

	public static void SharetoWX(Context context,boolean isFriend) {
		regToWx();
		Bitmap bmp = Tools.convertFileToBitmap();
		if (bmp == null)
			return;
		WXImageObject imageObject = new WXImageObject(bmp);
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = imageObject;
		
		Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
		bmp.recycle();
		msg.thumbData = Util.bmpToByteArray(thumbBmp, true);  // 缩略图

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("img");
		req.message = msg;
		req.scene = isFriend ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
		api.sendReq(req);
	}
	
	private static String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}

}
