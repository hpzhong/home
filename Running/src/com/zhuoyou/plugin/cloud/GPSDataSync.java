package com.zhuoyou.plugin.cloud;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.StatFs;
import android.util.Log;

import com.zhuoyou.plugin.database.DBOpenHelper;
import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.running.Tools;

public class GPSDataSync {
	private static final String TAG = "GPSDataSync";
	
	/** 上传文件 gps_sport 表生成的文件命名 */
	public static final String GPS_SPORT_ADD = "gps_sport_info_add";
	public static final String GPS_SPORT_UPDATE = "gps_sport_info_update";
	public static final String GPS_SPORT_DELETE = "gps_sport_info_delete";
	
	/** 上传文件 operation_time 表生成的文件命名 */
	public static final String GPS_OPERATION_ADD = "operation_time_info_add";
	public static final String GPS_OPERATION_UPDATE = "operation_time_info_update";
	public static final String GPS_OPERATION_DELETE = "operation_time_info_delete";
	
	/** 上传文件 point_message 表生成的文件命名 */
	public static final String GPS_POINT_ADD = "point_message_info_add";
	public static final String GPS_POINT_UPDATE = "point_message_info_update";
	public static final String GPS_POINT_DELETE = "point_message_info_delete";
	
	/** 下载文件命名 */
	public final String GPS_DOWNLOAD_SPORT = "gps_info_down";
	public final String GPS_DOWNLOAD_OPERATION = "operation_info_down";
	public final String GPS_DOWNLOAD_POINT = "point_info_down";
	
	private String rootPath = CvsUtils.GetDir();
	private String txtPath;
	private Context mContext;
	private String openid;
	private Handler mHandler;
	private int result = 0;
//	private int postSportType;
	private long recordId;	//下载数据时需要上传的参数
	
	public GPSDataSync(Context context, int type) {
		this.mContext = context;
//		this.postSportType = type;
		openid = Tools.getOpenId(mContext);
	}

	public void postSportData(Handler handler) {
		this.mHandler=handler;
		new Thread(upRunnable).start();
	}
	
	private Runnable upRunnable = new Runnable(){
		@Override
		public void run() {
			txtPath = rootPath + "/" + "zipfile/";
			
			if(!isSpaceEnough(rootPath)){
				return;
			}
			File mfile = new File(rootPath);
			CvsUtils.deleteFolder(mfile);
			
//			if (postSportType != 2)
//	    		return;
			createGSPFile(txtPath);
//			if (HomePageFragment.mHandler != null) {
//				Message message = new Message();
//				message.what = HomePageFragment.SYNC_DEVICE_PROGRESS;
//				message.obj = "90%";
//				HomePageFragment.mHandler.sendMessage(message);
//			}
			//upload zip file
			String up_zip = "upload.zip";
			try {
				CvsUtils.doZip(txtPath, rootPath + "/" + up_zip);
			} catch (IOException e) {
				e.printStackTrace();
			}
			result = upload_file(rootPath,up_zip);
			
			if(result == 1){
//            	mCvsHelper.UpdataLocalDate();
            	UpdataLocalDate();
            	Message msg = mHandler.obtainMessage();
    			msg.what = NetMsgCode.The_network_link_success;
    			msg.arg1 = NetMsgCode.postGPSInfo;
    			mHandler.sendMessage(msg);
            	
    			//clear tmp file
            }else{
				Log.d(TAG,"update failed");
				mHandler.sendEmptyMessage(110011);
            	return;
            }
		}
	};
	
	/** 生成 GPS 文件
	 * @param filePath 文件所在的目录
	 */
	public void createGSPFile(String filePath){
		
		try {
			/** ADD file */
			CvsUtils.DBTableToFile(mContext, openid, filePath , GPS_SPORT_ADD, 
					DataBaseContants.CONTENT_URI_GPSSPORT,	new String[]{ "_id","starttime"	,"endtime"
					,"sys_starttime","sys_endtime","durationtime","avespeed","total_distance","steps","calorie" }, 
					DataBaseContants.GPS_SYNC + "  = ? ",new String[] {Integer.toString(0)}, null);
			
			CvsUtils.DBTableToFile(mContext, openid, filePath , GPS_OPERATION_ADD, 
					DataBaseContants.CONTENT_URI_OPERATION,	new String[]{ "_id"	,"operation_time","operation_systime","operation_state" }, 
					DataBaseContants.GPS_SYNC + "  = ? ",new String[] {Integer.toString(0)}, null);
			
			CvsUtils.DBTableToFile(mContext, openid, filePath , GPS_POINT_ADD, 
					DataBaseContants.CONTENT_URI_POINT,	new String[]{ "_id" , "latitude" ,"longtitude" ,"address" ,"accuracy"
					,"provider"	,"location_time" ,"location_systime" ,"speed" ,"altitude" ,"point_state","gps_number" }, 
					DataBaseContants.GPS_SYNC + "  = ? ",new String[] {Integer.toString(0)}, null);
			
			/** DELETE file */
			CvsUtils.DBTableToFile(mContext, openid, filePath , GPS_SPORT_DELETE, 
					DataBaseContants.CONTENT_URI_GPSSYNC,	new String[]{ DataBaseContants.GPS_DELETE }, 
					DataBaseContants.GPS_TABLE + "  = ? ",new String[] { DataBaseContants.TABLE_GPSSPORT_NAME }, null);
			
			CvsUtils.DBTableToFile(mContext, openid, filePath , GPS_OPERATION_DELETE, 
					DataBaseContants.CONTENT_URI_GPSSYNC,	new String[]{ DataBaseContants.GPS_DELETE }, 
					DataBaseContants.GPS_TABLE + "  = ? ",new String[] { DataBaseContants.TABLE_OPERATION_NAME }, null);
			
			CvsUtils.DBTableToFile(mContext, openid, filePath , GPS_POINT_DELETE, 
					DataBaseContants.CONTENT_URI_GPSSYNC,	new String[]{ DataBaseContants.GPS_DELETE }, 
					DataBaseContants.GPS_TABLE + "  = ? ",new String[] { DataBaseContants.TABLE_POINT_NAME }, null);
			
			/** UPDATE file */
			CvsUtils.DBTableToFile(mContext, openid, filePath , GPS_SPORT_UPDATE, 
					DataBaseContants.CONTENT_URI_GPSSPORT,	new String[]{ "_id","starttime"	,"endtime"
					,"sys_starttime","sys_endtime","durationtime","avespeed","total_distance","steps","calorie" }, 
					DataBaseContants.GPS_SYNC + "  = ? ",new String[] {Integer.toString(2)}, null);
			
			CvsUtils.DBTableToFile(mContext, openid, filePath , GPS_OPERATION_UPDATE, 
					DataBaseContants.CONTENT_URI_OPERATION,	new String[]{ "_id"	,"operation_time","operation_systime","operation_state" }, 
					DataBaseContants.GPS_SYNC + "  = ? ",new String[] {Integer.toString(2)}, null);
			
			CvsUtils.DBTableToFile(mContext, openid, filePath , GPS_POINT_UPDATE, 
					DataBaseContants.CONTENT_URI_POINT,	new String[]{ "_id" , "latitude" ,"longtitude" ,"address" ,"accuracy"
					,"provider"	,"location_time" ,"location_systime" ,"speed" ,"altitude" ,"point_state","gps_number" }, 
					DataBaseContants.GPS_SYNC + "  = ? ",new String[] {Integer.toString(2)}, null);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	 
	/**生成上传的文件的参数
	 * @param path  	文件路径
	 * @param filename	文件名
	 * @return 1 代表成功
	 */
	private int upload_file(String path, String filename) {
		int result = 0;
		HttpConnect httpCon = new HttpConnect();
		String filePath = path + "/" + filename;
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("account", openid);
		result = httpCon.uploadFile(NetMsgCode.UP_URL, params, filePath);
		return result;
	}
	
	private Runnable dowmRunnable = new Runnable(){
		@Override
		public void run() {
			String down_zip ="Running_down.zip";
			if(!isSpaceEnough(rootPath)){
				return ;
			}
			File mfile = new File(rootPath);
			if(mfile.exists()){
				CvsUtils.deleteFolder(mfile);
			}
			mfile.mkdirs();
			
			result = download_file(rootPath,down_zip);
			Log.d(TAG,"result:"+result);
			if(result == 3){
				Log.d(TAG,"download  success");
				String zip_path = rootPath+"/"+down_zip;
				String out_path = rootPath+"/";
				try {
					CvsUtils.unZip(zip_path, out_path);
				} catch (Exception e) {
					e.printStackTrace();
					Log.d(TAG,"download failed");
				}
				Log.d(TAG,"CVSUnzipFile  success");
				int aptResult = -1;
				try {
					aptResult = ImportCSVFile(out_path);
					Log.i(TAG, ""+aptResult);
				} catch (RemoteException e) {
					e.printStackTrace();
					Log.d(TAG,"ImportCSVFile failed");
				} catch (OperationApplicationException e) {
					e.printStackTrace();
				}
				if(aptResult == -1){
					Log.d(TAG,"ImportCSVFile failed");
					mHandler.sendEmptyMessage(110011);
					return;
				}else{
					Log.d(TAG,"ImportCSVFile  success");
					Message msg = mHandler.obtainMessage();
					msg.what = NetMsgCode.getGPSInfo;
					msg.arg1 = NetMsgCode.The_network_link_success;
					mHandler.sendMessage(msg);
				}
			}else{
				Log.d(TAG,"download failed");
				mHandler.sendEmptyMessage(110011);
				return;
			}
		}
		
	};
	
	/** 下载文件
	 * @param path
	 * @param filename
	 * @return 3 代表成功
	 */
	private int download_file(String path,String filename){
		int result  = 0;
		HttpConnect httpCon = new HttpConnect();
        String filePath = path+"/"+filename;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("account", openid);
        Log.i("txhlog", "account:"+openid);
        params.put("id", Long.toString(recordId));
        Log.i("txhlog", "recordId:"+recordId);
        result = httpCon.downloadFile(NetMsgCode.GPS_DOWN_URL, new HashMap<String, String>(), params, filePath);
        Log.i("txhlog", "result"+result);
        return result ;
	}

	/** 从云服务器中下载数据
	 * @param handler
	 * @param recordId
	 */
	public void getGPSFromCloud(Handler handler,long recordId) {
		this.recordId = recordId;
		this.mHandler=handler;
		new Thread(dowmRunnable).start();
	}
	
	/**
	 * 判断 文件所在的磁盘剩余空间是否足够
	 * @param dir 文件路径
	 * @return true:足够 ； false:不足
	 */
	private boolean isSpaceEnough(String dir){
		StatFs sf = new StatFs(rootPath);
		long blockSize = sf.getBlockSize();
	    long freeBlocks = sf.getAvailableBlocks();
	    long freesize = (freeBlocks * blockSize)/1024 /1024;
	    /** 判断文件夹所在的磁盘剩余的容量，如果小于10MB 返回假 */
	    if(freesize <10){
	    	return false;
	    }else{
	    	return true;
	    }
	}
	
	/** 更新数据库 */
	public void UpdataLocalDate(){
//    	if (postSportType != 2)
//    		return;
		emove_db_update();
		emove_db_delete();
	}
	
	private void emove_db_update(){
		ContentResolver cr = mContext.getContentResolver();
		ContentValues updateValues = new ContentValues();
		updateValues.put(DataBaseContants.GPS_SYNC, 1);
		cr.update(DataBaseContants.CONTENT_URI_POINT, updateValues, null, null);
		cr.update(DataBaseContants.CONTENT_URI_OPERATION, updateValues, null, null);
		cr.update(DataBaseContants.CONTENT_URI_GPSSPORT, updateValues, null, null);
	}
	
	private void emove_db_delete(){
		DBOpenHelper mDBOpenHelper = new DBOpenHelper(mContext);
		String deleteSql = "DELETE FROM " + DataBaseContants.TABLE_GPS_SYNC ;
		SQLiteDatabase db =  mDBOpenHelper.getWritableDatabase();
		db.execSQL(deleteSql);
		db.close();
		mDBOpenHelper.close();
	}
	
	/**
	 * 解析从cloud 下载的数据，并与数据库相互操作
	 * @param filePath 文件地址
	 * @param fileName 文件名
	 * @throws OperationApplicationException 
	 * @throws RemoteException 
	 */
	private int ImportCSVFile(String filePath) throws RemoteException, OperationApplicationException{
		File downfile = new File(filePath);
		if(downfile == null || !downfile.exists()){
			return 0;
		}
		String filename[] = {GPS_DOWNLOAD_SPORT,GPS_DOWNLOAD_OPERATION,GPS_DOWNLOAD_POINT};
		
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		
		for(String mFile : filename){
			ArrayList<ArrayList<String>> resLists = CvsUtils.parseFile(filePath, mFile);
			if( resLists!=null && resLists.size() > 0){
				if(mFile.equals(GPS_DOWNLOAD_POINT)){
					for(int i = 0;i<resLists.size();i++){
						ArrayList<String> subList = resLists.get(i);
						Log.i("hello","subList1:" + subList.size());
						ContentProviderOperation cpOperation = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI_POINT)
								.withValue(DataBaseContants.GPS_ID,V_NULL(subList,0))
								.withValue(DataBaseContants.LATITUDE,V_NULL(subList,2))
								.withValue(DataBaseContants.LONGTITUDE,V_NULL(subList,3))
								.withValue(DataBaseContants.ADDRESS,V_NULL(subList,4))
								.withValue(DataBaseContants.ACCURACY,V_NULL(subList,5))
								.withValue(DataBaseContants.PROVIDER,V_NULL(subList,6))
								.withValue(DataBaseContants.LOCATION_TIME,V_NULL(subList,7))
								.withValue(DataBaseContants.LOCATION_SYS_TIME,V_NULL(subList,8))
								.withValue(DataBaseContants.SPEED,V_NULL(subList,9))
								.withValue(DataBaseContants.ALTITUDE,V_NULL(subList,10))
								.withValue(DataBaseContants.LOCATION_POINT_STATE,V_NULL(subList,11))
								.withValue(DataBaseContants.GPS_NUMBER,V_NULL(subList,12))
								.withValue(DataBaseContants.GPS_SYNC,"1")
								.withYieldAllowed(true).build();
						operations.add(cpOperation);
					}
				}else if(mFile.equals(GPS_DOWNLOAD_OPERATION)){
					for(int i = 0;i<resLists.size();i++){
						ArrayList<String> subList = resLists.get(i);
						Log.i("hello","subList2:" + subList.size());
						ContentProviderOperation cpOperation = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI_OPERATION)
								.withValue(DataBaseContants.GPS_ID,V_NULL(subList,0))
								.withValue(DataBaseContants.OPERATION_TIME,V_NULL(subList,2))
								.withValue(DataBaseContants.OPERATION_SYSTIME,V_NULL(subList,3))
								.withValue(DataBaseContants.OPERATION_STATE,V_NULL(subList,4))
								.withValue(DataBaseContants.GPS_SYNC,"1")
								.withYieldAllowed(true).build();
						operations.add(cpOperation);
					}
				}else if(mFile.equals(GPS_DOWNLOAD_SPORT)){
					for(int i = 0;i<resLists.size();i++){
						ArrayList<String> subList = resLists.get(i);
						Log.i("hello","subList3:" + subList.size());
						ContentProviderOperation cpOperation = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI_GPSSPORT)
								.withValue(DataBaseContants.GPS_ID,V_NULL(subList,0))
								.withValue(DataBaseContants.GPS_STARTTIME,V_NULL(subList,2))
								.withValue(DataBaseContants.GPS_ENDTIME,V_NULL(subList,3))
								.withValue(DataBaseContants.GPS_SYSSTARTTIME,V_NULL(subList,4))
								.withValue(DataBaseContants.GPS_SYSENDTIME,V_NULL(subList,5))
								.withValue(DataBaseContants.GPS_DURATIONTIME,V_NULL(subList,6))
								.withValue(DataBaseContants.AVESPEED,V_NULL(subList,7))
								.withValue(DataBaseContants.TOTAL_DISTANCE,V_NULL(subList,8))
								.withValue(DataBaseContants.GPS_STEPS,V_NULL(subList,9))
								.withValue(DataBaseContants.GPS_CALORIE,V_NULL(subList,10))
								.withValue(DataBaseContants.GPS_SYNC,"1")
								.withYieldAllowed(true).build();
						operations.add(cpOperation);
					}
				}
			}
		}
		
		int res = -1;
		if (operations != null && operations.size() > 0){
			try{
				res= mContext.getContentResolver().applyBatch(DataBaseContants.AUTHORITY, operations).length;
			}catch(Exception e){
				Log.e(TAG, e.getMessage());
			}
		}
		return res;
		
	}
	
	static String V_NULL(String s){
		if(s == null||s == ""||s.equals("")){
			return null;
		}
		return s;
	}
	static String V_NULL(ArrayList<String> list,int index){
		String s = null;
		if(list == null || list.size()== 0){
			return null;
		}else if(list.size()>index){
			s = V_NULL(list.get(index));
		}
		return s;
	}
}
