package com.zhuoyou.plugin.cloud;

public class NetMsgCode {
	public final static String URL = "http://service-erunning.yy845.com:2761";
//	public final static String URL = "http://192.168.0.52:2761";
	
	public static final int The_network_link_success = 200;
	public static final int The_network_link_failure = 404;
	
	public static final int rank_state_wait = 1110;
	public static final int rank_state_request = 1111;

	public final static int postCustomInfo = 101001;

	public final static int getCustomInfo = 102001;

	public final static String insertData = "insertData";
	public final static String updateData = "updateData";
	public final static String deleteData = "deleteData";

	public final static int postSportInfo = 104001;

	public final static int getSportInfo = 105001;
	
	public final static int getNetRankInfo = 103001;
	
	public final static int postGPSInfo = 10401;
	public final static int getGPSInfo = 10501;
	
	public final static String UP_URL = "http://service-erunning.yy845.com:8080/mars/services/FileService/gpsFileUpLoad";
	public final static String DOWN_URL = "http://service-erunning.yy845.com:8080/mars/services/FileService/fileDownLoad";
	public final static String GPS_DOWN_URL = "http://service-erunning.yy845.com:8080/mars/services/FileService/gpsFileDownLoad";
//	public final static String UP_URL = "http://101.95.97.178:8890/mars/services/FileService/gpsFileUpLoad";
//	public final static String DOWN_URL = "http://192.168.0.52:8890/mars/services/FileService/fileDownLoad";
//	public final static String GPS_DOWN_URL = "http://101.95.97.178:8890/mars/services/FileService/gpsFileDownLoad";
	
	//add for Action net msg
	//app启动访问
	public static final int APP_RUN_ACTION_INIT = 100011;
	//单独获取消息信息
	public static final int ACTION_GET_MSG = 100012;
	//马上参加
	public static final int ACTION_JOIN = 100013; 
	//具体某个活动
	public static final int ACTION_GET_IDINFO = 100014; 
	//下拉刷新
	public static final int ACTION_GET_NEXTPAGE = 100015; 
	//分页详情
	public static final int ACTION_GET_REFRESHPAGE= 100016; 
	
	public static final int FIRMWARE_GET_VERSION = 200101;
}
