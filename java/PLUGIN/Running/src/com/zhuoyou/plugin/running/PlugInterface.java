package com.zhuoyou.plugin.running;

import java.util.Map;

import android.graphics.drawable.Drawable;

public interface PlugInterface {
	public String getSupportCmd();
	public String getName();
	public Drawable getIcon();
	public String getEntryMethodName();
	public Map<String , String> getWorkMethodName();
}
