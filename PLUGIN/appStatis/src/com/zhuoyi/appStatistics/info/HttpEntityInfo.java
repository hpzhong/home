package com.zhuoyi.appStatistics.info;

import java.io.File;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import com.zhuoyi.appStatistics.utils.LogUtil;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.DisplayMetrics;
import android.util.Log;

public class HttpEntityInfo {
	public static final String TAG = "HttpEntityInfo";
	
	private Context mContext;
	   
	private String mImsi;
	private String mImei;
	private String mLcd;
	private String mOem;
	private String mRam;
	private String mRom;
	private String mAndroidVer;
	private String mDev;
	private String mLbs;
	private String mNetType;
	private String mNetIp;
	private String mCpu;
	private String mMac;
	
	public HttpEntityInfo(Context context){
		mContext = context;
		init();
	}
	
	private void init(){
		
		mCpu = Build.HARDWARE;	//get the chip model
		mOem = Build.MANUFACTURER;	//get the OEM(Origin Entrusted Manufacturer)
		//get lcd
		DisplayMetrics outMetrics = mContext.getResources().getDisplayMetrics();
		mLcd = Integer.toString(outMetrics.widthPixels) + "x" + Integer.toString(outMetrics.heightPixels);
		//get imei
		TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = tm.getDeviceId();
		LogUtil.logI("123456", "mImei="+mImei, mLbs);

		if(imei == null){
			imei = "null";
		}
		mImei = imei;
		
		//get imsi
		String imsi = tm.getSubscriberId();
		if(imsi == null){
			imsi = "null";
		}
		mImsi = imsi;
		
		//get lbs
		mLbs = getLBSinfo(tm);
		LogUtil.logI("123456", "mLbs="+mLbs, mLbs);

		//get sms server number
		//TBD
		
		//RAM
		String[] meminfoLabels = {"MemTotal:"};
		long[] meminfoValues = new long[1];
		meminfoValues[0] = -1;
		Class<?> proc = null;
		try{
			proc = Class.forName("android.os.Process");
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			Method method = proc.getMethod("readProcLines", String.class, String[].class, long[].class);
			method.invoke(proc.newInstance(), "/proc/meminfo", meminfoLabels, meminfoValues);
		}catch(Exception e){
			e.printStackTrace();
		}
		String RAM = "null";
		if(meminfoValues[0] != -1){
			RAM = Long.toString(meminfoValues[0] / 1024) + "M";
		}
		mRam = RAM;
		LogUtil.logI("123456", "RAM="+RAM, RAM);
		
		//ROM
		File romPath = Environment.getRootDirectory();
		StatFs stat = new StatFs(romPath.getPath());
		long blockSize = stat.getBlockSize();
		long blockCount = stat.getBlockCount();
		long totalBytes = blockSize * blockCount;
		mRom = Long.toString(totalBytes / 1024 / 1024) + "M";
		LogUtil.logI("123456", "mRom="+mRom, mRom);

		//android version
		mAndroidVer = Build.VERSION.RELEASE;
		
		//device name
		mDev = Build.DEVICE;
		
		//network type
		mNetType = getCurrNetworkType();
		
		//network ip
		mNetIp = "0.0.0.0";
		if(mNetType.equals("wifi")){
			mNetIp = getIpAddress(true);
		}else{
			mNetIp = getIpAddress(false);
		}
		
		//mac address
		WifiManager wifi = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);     
		WifiInfo info = wifi.getConnectionInfo();     
		mMac = info.getMacAddress();     
		
		
	}
	
	/*
	 * interface for get LBS information
	 */
	private String getLBSinfo(TelephonyManager tm){
		int phoneType = tm.getPhoneType();
		int lac = 0;
		int cellId = 0;
		
		CellLocation cellLocation = tm.getCellLocation();
		if(cellLocation != null && phoneType == TelephonyManager.PHONE_TYPE_GSM){
			GsmCellLocation gsmCellLocation = (GsmCellLocation)cellLocation;
			lac = gsmCellLocation.getLac();
			cellId = gsmCellLocation.getCid();
		}else if(cellLocation == null){
			return null;
		}
		String simNumeric = tm.getSimOperator();
		String mcc = "000";
		String mnc = "00";
		if(simNumeric != null && !simNumeric.equals("")){
			mcc = simNumeric.substring(0, 3);
			mnc = simNumeric.substring(3);
		}
		
		String lbs = mcc + ":" + mnc + ":" + Integer.toString(cellId) + ":" + Integer.toString(lac);
		LogUtil.logI(TAG, "getLBSinfo", "lbs = " + lbs);
		

		return lbs;
	}
	
	private String getCurrNetworkType()
	{
		String netStatus = "none";
		ConnectivityManager connectMgr = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo info = connectMgr.getActiveNetworkInfo();
		
		if (info!=null)
		{
			if(info.getType()==ConnectivityManager.TYPE_WIFI)
			{
				//wifi network
				netStatus = "wifi";
			}
			else if(info.getType()==ConnectivityManager.TYPE_MOBILE)
			{   //TelephonyManager.NETWORK_TYPE_LTE
				int subType = info.getSubtype();
				
				if(subType ==TelephonyManager.NETWORK_TYPE_UMTS ||
				   subType ==TelephonyManager.NETWORK_TYPE_HSDPA ||
				   subType ==TelephonyManager.NETWORK_TYPE_EVDO_0 ||
				   subType ==TelephonyManager.NETWORK_TYPE_EVDO_A)
				{
					//3G network
					netStatus = "3G";
				}
				else
				{
					//2G or 2.5G
					netStatus = "2G";
				}
			}			
		}
		return netStatus;
	}
	
	/*
	 * interface for get ip address
	 */
	private String getIpAddress(boolean isWifiNow){
		if(isWifiNow){
			WifiManager wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			int ipAddress = wifiInfo.getIpAddress();
			return (ipAddress & 0xFF ) + "." + ((ipAddress >> 8 ) & 0xFF) + "." + ((ipAddress >> 16 ) & 0xFF) + "." + ((ipAddress >> 24) & 0xFF);
		}else{
			try{
				for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();){
					NetworkInterface intf = en.nextElement();
					for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();){
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()){
							return inetAddress.getHostAddress().toString();
						}
					}
				}
			}catch (SocketException ex){
				//do nothing now
			}
		}
		
		return "0.0.0.0";
	}
	
	
	public String getImei(){
		return mImei;
	}
	
	public String getImsi(){
		return mImsi;
	}
	
	public String getLcd(){
		return mLcd;
	}
	
	public String getRam(){
		return mRam;
	}
	
	public String getRom(){
		return mRom;
	}
	
	public String getAndroidVer(){
		return mAndroidVer;
	}
	
	public String getNetworkType(){
		return mNetType;
	}
	
	public String getNetworkIp(){
		return mNetIp;
	}
	
	public String getCpu(){
		return mCpu;
	}
	
	public String getOem(){
		return mOem;
	}
	
	public String getDev(){
		return mDev;
	}
	
	public String getLbs(){
		return mLbs;
	}
	
	public String getMac(){
		return mMac;
	}
}
