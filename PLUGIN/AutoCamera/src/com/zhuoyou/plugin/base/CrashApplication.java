package com.zhuoyou.plugin.base;

import android.app.Application;
import android.util.Log;

public class CrashApplication extends Application {

    @Override
    public void onCreate() {
        Log.i("gchk", "onCreate(), BTNoticationApplication create!");
        super.onCreate();
        MyExceptionHandel crashHandler = MyExceptionHandel.getInstance();
        crashHandler.init(getApplicationContext());
    }

}
