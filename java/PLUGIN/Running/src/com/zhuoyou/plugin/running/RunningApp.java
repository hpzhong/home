package com.zhuoyou.plugin.running;


import android.app.Application;
import android.util.Log;

public class RunningApp extends Application {
	private static RunningApp sInstance = null;
	
	public static RunningApp getInstance() {
		return sInstance;
	}
	
	@Override
    public void onCreate() {
        Log.i("gchk", "onCreate(), RunningApp create!");
        super.onCreate();
        sInstance = this;
        
        MainService.getInstance();
    }
}
