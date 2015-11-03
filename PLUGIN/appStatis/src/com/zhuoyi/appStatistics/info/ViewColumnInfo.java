package com.zhuoyi.appStatistics.info;

public class ViewColumnInfo {
	private String mAppId;
	private long mTimeMillis;
	private String mChannelId;
	private String mTheme;
	private String mColumn;
	
	public ViewColumnInfo(String appId, long timeMillis, String channelId, String theme, String column){
		mAppId = appId;
		mTimeMillis = timeMillis;
		mChannelId = channelId;
		mTheme = theme;
		mColumn = column;
	}
	
	public String getAppId(){
		return mAppId;
	}
	
	public long getTimeMillis(){
		return mTimeMillis;
	}
	
	public String getChannelId(){
		return mChannelId;
	}
	
	public String getTheme(){
		return mTheme;
	}
	
	public String getColumn(){
		return mColumn;
	}
}
