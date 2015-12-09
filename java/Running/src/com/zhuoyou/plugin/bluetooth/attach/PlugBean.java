package com.zhuoyou.plugin.bluetooth.attach;

import java.lang.reflect.Constructor;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhuoyou.plugin.running.R;

public class PlugBean {
	// 是否安装
	public boolean isInstalled = false;
	// 是否为系统自带的--一些通用的.无法做成插件的
	public boolean isSystem = false;
	// 是否为系统预安装列表中的
	public boolean isPreInstall = false;

	public String mSupportCmd = "";

	public String mPackageName = "";

	//public int mSupportType = ProductType.SUPPORT_TYPE_ALL;

	public int mLogoResId = 0;

	public Drawable mLogoBitmap = null;

	public String mTitle = "";

	public Map<String, String> mWorkMethod;

	public Context mCtx;
	
	public Class<?> mClasses;
	
	public Constructor<?> mConstructor;
	
	public Object mInstance;
	
	/**
	 * 除了system的意外,以前全为entryFuntion.
	 * 
	 * 写插件时强制定义
	 */
	public String mMethod_Entry = "";

	public void setBitmapId(ImageView iv) {
		iv.setImageResource(mLogoResId);
	}

	public void setBitmap(ImageView iv) {
		if (mLogoBitmap != null) {
			iv.setImageDrawable(mLogoBitmap);
		} else {
			iv.setImageResource(R.drawable.ic_launcher);
		}
	}

	public void setTitle(TextView tv) {
		if (mTitle != null) {
			tv.setText(mTitle);
		} else {
			tv.setText("Unknown");
		}
	}

}
