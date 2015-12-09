package com.zhuoyou.plugin.share;

import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.graphics.Bitmap;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;

public class ShareToWeixin {

	public static final String APP_ID = "wx9e68ecf43e6b8493";
	public static final String APP_KEY = "90aeacf25a411cc51265c0161803daa5";
	public static IWXAPI api = WXAPIFactory.createWXAPI(RunningApp.getInstance().getApplicationContext(), APP_ID, true);;
	private static final int THUMB_SIZE = 300;

	public static void regToWx(){
		api.registerApp(APP_ID);
	}

	public static void SharetoWX(Context context, boolean isFriend, String fileName) {
		regToWx();
		Bitmap bmp = Tools.convertFileToBitmap("/Running/share/" + fileName);
		if (bmp == null)
			return;
		WXImageObject imageObject = new WXImageObject(bmp);
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = imageObject;
		
		//根据微信提供的API 提供的缩略图最大支持32K
		int scaleHeight = bmp.getHeight() * THUMB_SIZE / bmp.getWidth() ;
		Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, scaleHeight, true);
		bmp.recycle();
		msg.thumbData = compressImage(thumbBmp);  // 缩略图

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("img");
		req.message = msg;
		req.scene = isFriend ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
		api.sendReq(req);
	}
	
	private static byte[] compressImage(Bitmap image) {  
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中  
		int options = 100;  
		while ( baos.toByteArray().length / 1024> 32) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩         
			baos.reset();//重置baos即清空baos  
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中  
			options -= 10;//每次都减少10  
		}
		return baos.toByteArray(); 
	} 

	private static String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}

}
