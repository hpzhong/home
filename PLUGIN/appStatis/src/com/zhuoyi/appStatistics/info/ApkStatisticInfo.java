package com.zhuoyi.appStatistics.info;

public class ApkStatisticInfo {
	private String mAppId;
	private long mTimeMillis;		//for apk download complete time millis and install success time millis
	private String mChannelId;
	private String mPkgName;
	//private String mPctName;
	private String mAppName;
	private int mVerCode;
	private String mFrom;
	private int mApkId;
	/*String s = "com.yy.yu.iuyu";
	String arry[] = null;
	
	arry   	= s.split("\\.");    
	String s1 = arry[0]+"."+arry[1]+"."+arry[2];*/
	public ApkStatisticInfo(String appId, long timeMillis, String channelId, String pkgName, String appName, int verCode, String from, int apkId){
		mAppId = appId;
		mTimeMillis = timeMillis;
		mChannelId = channelId;
		mPkgName = pkgName;
		mAppName = appName;
		mVerCode = verCode;
		mFrom = from;
		mApkId = apkId;
	}
	
	public String getAppId(){
		return mAppId;
	}
	
	public int getApkId(){
		return mApkId;
	}
	
	public long getTimeMillis(){
		return mTimeMillis;
	}
	
	public String getChannelId(){
		return mChannelId;
	}
	
	public String getPkgName(){
		
		return mPkgName;
	}
	
	public String getAppName(){
		return mAppName;
	}
	
	public int getVerCode(){
		return mVerCode;
	}
	
	public String getFrom(){
		return mFrom;
	}
}
