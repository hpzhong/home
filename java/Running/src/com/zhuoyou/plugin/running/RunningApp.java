package com.zhuoyou.plugin.running;


import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.mcube.lib.ped.PedBackgroundService;
import com.zhuoyou.plugin.action.ActionWelcomeInfo;
import com.zhuoyou.plugin.action.AppInitForAction;
import com.zhuoyou.plugin.action.CacheTool;
import com.zhuoyou.plugin.action.MessageInfo;
import com.zhuoyou.plugin.ble.BleManagerService;
import com.zhuoyou.plugin.bluetooth.data.CrashHandler;
import com.zhuoyou.plugin.bluetooth.service.BluetoothService;
import com.zhuoyou.plugin.cloud.NetMsgCode;

public class RunningApp extends Application {
	
	private final static String TAG = RunningApp.class.getSimpleName();
	private static RunningApp sInstance = null;
	private static Typeface mNumberTP;
	private CacheTool mcachetool = new CacheTool(this);
	public static boolean isBLESupport = false;
	private static String mDeviceName = null;
    public static String mCurrentConnectDeviceType;
	public static RunningApp getInstance() {
		return sInstance;
	}
	
	@Override
    public void onCreate() {
        Log.d(TAG, "onCreate(), RunningApp create!");
        super.onCreate();


        /*Intent phoneStepsIntent = new Intent(getApplicationContext(), PedBackgroundService.class); 
        startService(phoneStepsIntent);
		Log.i("111", "phoneStepsIntent="+phoneStepsIntent);*/


 
        sInstance = this;
        
        MainService.getInstance();
        BluetoothService.getInstance();
        if(isSupportBle()){
        	BleManagerService.getInstance();
        }
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }
	private Handler main_handler = new Handler(){

		@Override
		public void dispatchMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){
			case NetMsgCode.The_network_link_failure:
				Toast.makeText(getApplicationContext(), "net connect is error ,get data failed!", Toast.LENGTH_SHORT);
				break;
			case NetMsgCode.The_network_link_success:
				switch(msg.arg1){
				
					//add by zhouzhongbo@20150129 for action module	start
					case NetMsgCode.APP_RUN_ACTION_INIT:
						AppInitForAction mm  = (AppInitForAction)msg.obj;
						mcachetool.SaveActionInitDate(mm);
						new Thread(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								LoadwelcodeAd();
							}
						}).start();
						break;
						
					case NetMsgCode.ACTION_GET_MSG:
						List<MessageInfo> msglist  = (List<MessageInfo>) msg.obj;
						mcachetool.SaveMsgList(msglist);
						break;
//					case NetMsgCode.ACTION_JOIN:
//					case NetMsgCode.ACTION_GET_IDINFO:
//					case NetMsgCode.ACTION_GET_NEXTPAGE:
//						break;
				
				}
				break;
			}
			super.dispatchMessage(msg);
		}
		
	};
	
	public Handler GetAppHandler(){
		return main_handler;
	}

	public CacheTool GetCacheTool(){
		return mcachetool;
	}
	
	@SuppressLint("NewApi")
	private boolean isSupportBle() {
		isBLESupport = false;
		if(Build.VERSION.SDK_INT >= 18){//android 4.3 begin support ble 
			isBLESupport = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
		} else {
			isBLESupport = false;
		}
		Log.d(TAG,"isSupportBle = " + isBLESupport);
		return isBLESupport;
	}

	public static Typeface getCustomChineseFont(){
		if(mNumberTP == null){
			mNumberTP = Typeface.createFromAsset(sInstance.getApplicationContext().getAssets(),"font/fzlantingblackbold.ttf");
		}
		return mNumberTP;
	}
	
	public static Typeface getCustomNumberFont(){
		if(mNumberTP == null){
			mNumberTP = Typeface.createFromAsset(sInstance.getApplicationContext().getAssets(),"font/cmtattoodragon.ttf");
		}
		return mNumberTP;
	}
   public static void setCurrentConnectDeviceType(String CurrentConnectDeviceType) {
	    mCurrentConnectDeviceType = CurrentConnectDeviceType;
   }
   public static String getDeviceName() {
	   return mDeviceName;
   }
	public String GetFileName(String url){
		String filename = "";
		if(url != null){
	        String tmp = url;
	        String file_tmp =url;
			for(int i = 0;i<5;i++){
				tmp = tmp.substring(0,tmp.lastIndexOf("/"));
			}
			for(String aa:file_tmp.substring(tmp.length()+1).split("/")){
				filename +=aa;
			}
			filename = filename.substring(0, filename.lastIndexOf("."));
			
		}
		return filename;
	}
	
	private void LoadwelcodeAd(){
		String url = null;
		URL m;
		FileInputStream fis = null;
		InputStream i = null;
		Drawable d = null;
		ActionWelcomeInfo mwelcomedate = mcachetool.GetWelcomeDate();
		if(mwelcomedate != null){
			url = mwelcomedate.GetImgUrl();
			try {
				url = url.substring(0, url.lastIndexOf("/")+1)+URLEncoder.encode(url.substring(url.lastIndexOf("/")+1),"UTF-8").replace("+", "%20");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (url != null && !url.equals("")) {
			String fileName = GetFileName(url);
			String filePath = Tools.getSDPath() + "/Running/download/cache";
			File dirs = new File(filePath.toString());
			if (!dirs.exists()) {
				dirs.mkdirs();
			}
			File f = new File(filePath, fileName);
			if (f.exists()) {
				try {
					fis = new FileInputStream(f);
					d = Drawable.createFromStream(fis, "src");
					fis.close();
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					System.gc();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				m = new URL(url);
				i = (InputStream) m.getContent();
				DataInputStream in = new DataInputStream(i);
				FileOutputStream out = new FileOutputStream(f);
				byte[] buffer = new byte[1024];
				int byteread = 0;
				while ((byteread = in.read(buffer)) != -1) {
					out.write(buffer, 0, byteread);
				}
				in.close();
				out.close();
				fis = new FileInputStream(f);
				d = Drawable.createFromStream(i, "src");
				fis.close();
				i.close();
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				System.gc();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (i != null)
						i.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
