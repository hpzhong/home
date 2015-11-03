package com.zhuoyou.plugin.base;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.zhuoyou.plugin.autocamera.Main;
import com.zhuoyou.plugin.autocamera.PlugTools;

public class MyExceptionHandel implements UncaughtExceptionHandler {

    private Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private static MyExceptionHandel INSTANCE = new MyExceptionHandel();

    private MyExceptionHandel() {
    }

    public static MyExceptionHandel getInstance() {
        return INSTANCE;
    }

    public void init(Context context) {
        mContext = context;
        //��ȡϵͳĬ�ϵ�UncaughtException������
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //���ø�CrashHandlerΪ�����Ĭ�ϴ�����
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {  
            //����û�û�д�������ϵͳĬ�ϵ��쳣������������  
            mDefaultHandler.uncaughtException(thread, ex);  
        } else {  
            try {  
                Thread.sleep(3000);  
            } catch (InterruptedException e) {  
             }  
            //�˳�����  
            android.os.Process.killProcess(android.os.Process.myPid());  
            System.exit(1);  
        }  
	}

	private boolean handleException(Throwable ex) {  
        if (ex == null) {  
            return false;  
        }  
        //ʹ��Toast����ʾ�쳣��Ϣ  
        new Thread() {  
            @Override  
            public void run() {  
                Looper.prepare();  
                Toast.makeText(mContext, "�ܱ�Ǹ,��������쳣,�����˳�.", Toast.LENGTH_LONG).show();  
                Looper.loop();  
            }  
        }.start();  
        PlugTools.saveDataString(mContext, "screen", "other");
		Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
		intent.putExtra("plugin_cmd", 0x54);
		intent.putExtra("plugin_content", "exit");
		mContext.sendBroadcast(intent);
        return true;  
    } 
}
