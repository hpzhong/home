package com.zhuoyi.appStatistics.info;

public class AppStatisticInfo {
	
	private String mAppId;
	private long mTimeMillis;		//for app start time millis and end time millis
	private String mChannelId;
	private String mAppTypeId;
	
	public AppStatisticInfo(String appId, long timeMillis, String channelId, String appTypeId){
		mAppId = appId;
		mTimeMillis = timeMillis;
		mChannelId = channelId;
		mAppTypeId = appTypeId;
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
	
	public String getAppTypeId(){
		return mAppTypeId;
	}
}
