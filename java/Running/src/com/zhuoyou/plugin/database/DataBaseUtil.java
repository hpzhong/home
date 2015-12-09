package com.zhuoyou.plugin.database;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.zhuoyou.plugin.gps.GpsSportDataModel;
import com.zhuoyou.plugin.gps.GuidePointModel;
import com.zhuoyou.plugin.gps.OperationTimeModel;
import com.zhuoyou.plugin.running.SleepBean;
import com.zhuoyou.plugin.running.SleepItem;
import com.zhuoyou.plugin.running.SleepTools;
import com.zhuoyou.plugin.running.Tools;

public class DataBaseUtil {
	
	private Context mContext;
	
	public DataBaseUtil(Context mCon){
		mContext = mCon;
	}
	
	public void inserPoint(GuidePointModel guidePointModel) {
		ContentValues pointItem = new ContentValues();
		if(guidePointModel!=null){
			pointItem.put(DataBaseContants.GPS_ID, guidePointModel.getGuideId());
			pointItem.put(DataBaseContants.LATITUDE, guidePointModel.getLatitude());
			pointItem.put(DataBaseContants.LONGTITUDE, guidePointModel.getLongitude());
			pointItem.put(DataBaseContants.ADDRESS,guidePointModel.getAddress());
			pointItem.put(DataBaseContants.ACCURACY, guidePointModel.getAccuracy());
			pointItem.put(DataBaseContants.PROVIDER,guidePointModel.getProvider());
			pointItem.put(DataBaseContants.LOCATION_TIME, guidePointModel.getTime());
			pointItem.put(DataBaseContants.SPEED, guidePointModel.getSpeed());
			pointItem.put(DataBaseContants.ALTITUDE, guidePointModel.getAltitude());
			pointItem.put(DataBaseContants.GPS_NUMBER, guidePointModel.getGpsStatus());
			pointItem.put(DataBaseContants.LOCATION_SYS_TIME, guidePointModel.getSysTime());
			pointItem.put(DataBaseContants.LOCATION_POINT_STATE, guidePointModel.getPointState());
			pointItem.put(DataBaseContants.GPS_SYNC, guidePointModel.getSyncState());
		}
		Uri result = mContext.getContentResolver().insert(DataBaseContants.CONTENT_URI_POINT,
				pointItem);
	}
	
	public void inserTempPoint(GuidePointModel guidePointModel) {
		ContentValues pointItem = new ContentValues();
		if(guidePointModel!=null){
			pointItem.put(DataBaseContants.LATITUDE, guidePointModel.getLatitude());
			pointItem.put(DataBaseContants.LONGTITUDE, guidePointModel.getLongitude());
			pointItem.put(DataBaseContants.ADDRESS,guidePointModel.getAddress());
			pointItem.put(DataBaseContants.ACCURACY, guidePointModel.getAccuracy());
			pointItem.put(DataBaseContants.PROVIDER,guidePointModel.getProvider());
			pointItem.put(DataBaseContants.LOCATION_TIME, guidePointModel.getTime());
			pointItem.put(DataBaseContants.SPEED, guidePointModel.getSpeed());
			pointItem.put(DataBaseContants.ALTITUDE, guidePointModel.getAltitude());
			pointItem.put(DataBaseContants.GPS_NUMBER, guidePointModel.getGpsStatus());
			pointItem.put(DataBaseContants.LOCATION_SYS_TIME, guidePointModel.getSysTime());
			pointItem.put(DataBaseContants.LOCATION_POINT_STATE, guidePointModel.getPointState());
			pointItem.put(DataBaseContants.GPS_SYNC, guidePointModel.getSyncState());
		}
		Uri result = mContext.getContentResolver().insert(DataBaseContants.CONTENT_URI_TEMPPOINT,
				pointItem);
	}		
	
	
//	public void inserPoint(List<GuidePointModel> listPoint) {
//		for(int i=0;i<listPoint.size();i++){
//			ContentValues pointItem = new ContentValues();
//			GuidePointModel guidePointModel=listPoint.get(i);
//			if(guidePointModel!=null){
//				pointItem.put(DataBaseContants.GPS_ID, guidePointModel.getGuideId());
//				pointItem.put(DataBaseContants.LATITUDE, guidePointModel.getLatitude());
//				pointItem.put(DataBaseContants.LONGTITUDE, guidePointModel.getLongitude());
//				pointItem.put(DataBaseContants.ADDRESS,guidePointModel.getAddress());
//				pointItem.put(DataBaseContants.ACCURACY, guidePointModel.getAccuracy());
//				pointItem.put(DataBaseContants.PROVIDER,guidePointModel.getProvider());
//				pointItem.put(DataBaseContants.LOCATION_TIME, guidePointModel.getTime());
//				pointItem.put(DataBaseContants.SPEED, guidePointModel.getSpeed());
//				pointItem.put(DataBaseContants.ALTITUDE, guidePointModel.getAltitude());
//				pointItem.put(DataBaseContants.GPS_NUMBER, guidePointModel.getGpsStatus());
//				pointItem.put(DataBaseContants.LOCATION_SYS_TIME, guidePointModel.getSysTime());
//				pointItem.put(DataBaseContants.LOCATION_POINT_STATE, guidePointModel.getPointState());
//				pointItem.put(DataBaseContants.GPS_SYNC, guidePointModel.getSyncState());
//			}
//			Uri result = mContext.getContentResolver().insert(DataBaseContants.CONTENT_URI_POINT,
//					pointItem);
//		}
//	}
		
	public void inserPoint(List<GuidePointModel> listPoint) {
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		for(int i=0;i<listPoint.size();i++){
			GuidePointModel guidePointModel=listPoint.get(i);
			if(guidePointModel!=null){
			ContentProviderOperation cpOperation = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI_POINT)
					.withValue(DataBaseContants.GPS_ID,guidePointModel.getGuideId())
					.withValue(DataBaseContants.LATITUDE,guidePointModel.getLatitude())
					.withValue(DataBaseContants.LONGTITUDE,guidePointModel.getLongitude())
					.withValue(DataBaseContants.ADDRESS,guidePointModel.getAddress())
					.withValue(DataBaseContants.ACCURACY,guidePointModel.getAccuracy())
					.withValue(DataBaseContants.PROVIDER,guidePointModel.getProvider())
					.withValue(DataBaseContants.LOCATION_TIME,guidePointModel.getTime())
					.withValue(DataBaseContants.LOCATION_SYS_TIME,guidePointModel.getSysTime())
					.withValue(DataBaseContants.SPEED,guidePointModel.getSpeed())
					.withValue(DataBaseContants.ALTITUDE,guidePointModel.getAltitude())
					.withValue(DataBaseContants.LOCATION_POINT_STATE,guidePointModel.getPointState())
					.withValue(DataBaseContants.GPS_NUMBER,guidePointModel.getGpsStatus())
					.withValue(DataBaseContants.GPS_SYNC,guidePointModel.getSyncState())
					.withYieldAllowed(true).build();
			operations.add(cpOperation);
			}
		}
		if (operations != null && operations.size() > 0){
			try{
				mContext.getContentResolver().applyBatch(DataBaseContants.AUTHORITY, operations);
			}catch(Exception e){
				Log.e("DataBaseUtil", e.getMessage());
			}
		}
	}
	
	public void insertOperation(OperationTimeModel operationTimeModel){
		ContentValues operationItem =new ContentValues();
		if(operationTimeModel!=null){
			operationItem.put(DataBaseContants.GPS_ID, operationTimeModel.getOperatId());
			operationItem.put(DataBaseContants.OPERATION_TIME,operationTimeModel.getOperationtime());
			operationItem.put(DataBaseContants.OPERATION_SYSTIME,operationTimeModel.getOperationSystime());
			operationItem.put(DataBaseContants.OPERATION_STATE, operationTimeModel.getOperationState());
			operationItem.put(DataBaseContants.GPS_SYNC, operationTimeModel.getSyncState());
		}
		mContext.getContentResolver().insert(DataBaseContants.CONTENT_URI_OPERATION, operationItem);
	}
	
	public void insertGpsInfo(GpsSportDataModel gpsSportDataModel){
		ContentValues gpsInfoItem = new ContentValues();
		if(gpsSportDataModel!=null){
			gpsInfoItem.put(DataBaseContants.GPS_ID, gpsSportDataModel.getGpsId());
			gpsInfoItem.put(DataBaseContants.GPS_STARTTIME, gpsSportDataModel.getStarttime());
			gpsInfoItem.put(DataBaseContants.GPS_ENDTIME, gpsSportDataModel.getEndtime());
			gpsInfoItem.put(DataBaseContants.GPS_SYSSTARTTIME, gpsSportDataModel.getStarSysttime());
			gpsInfoItem.put(DataBaseContants.GPS_SYSENDTIME, gpsSportDataModel.getEndSystime());
			gpsInfoItem.put(DataBaseContants.GPS_DURATIONTIME,gpsSportDataModel.getDurationtime());
			gpsInfoItem.put(DataBaseContants.AVESPEED, gpsSportDataModel.getAvespeed());
			gpsInfoItem.put(DataBaseContants.TOTAL_DISTANCE, gpsSportDataModel.getTotalDistance());
			gpsInfoItem.put(DataBaseContants.GPS_STEPS, gpsSportDataModel.getSteps());
			gpsInfoItem.put(DataBaseContants.GPS_CALORIE,gpsSportDataModel.getCalorie());
			gpsInfoItem.put(DataBaseContants.GPS_STARTADDRESS,gpsSportDataModel.getStartAddress());
			gpsInfoItem.put(DataBaseContants.GPS_ENDADDRESS,gpsSportDataModel.getEndAddress());
			gpsInfoItem.put(DataBaseContants.GPS_SYNC, gpsSportDataModel.getSyncState());
		}
		mContext.getContentResolver().insert(DataBaseContants.CONTENT_URI_GPSSPORT, gpsInfoItem);
	}
	
	public void deletePoint(long starttime,long endtime) {
		ContentResolver cr = mContext.getContentResolver();
		cr.delete(DataBaseContants.CONTENT_URI_POINT, DataBaseContants.LOCATION_TIME + " between ? and ?", new String[]{Long.toString(starttime),Long.toString(endtime)});
	}
	
	public void deleteTempPoint(long starttime,long endtime) {
		ContentResolver cr = mContext.getContentResolver();
		cr.delete(DataBaseContants.CONTENT_URI_TEMPPOINT, DataBaseContants.LOCATION_TIME + " between ? and ?", new String[]{Long.toString(starttime),Long.toString(endtime)});
	}
	
	public void deleteOperation(long starttime,long endtime){
		ContentResolver cr = mContext.getContentResolver();
		cr.delete(DataBaseContants.CONTENT_URI_OPERATION, DataBaseContants.OPERATION_TIME + " between ? and ?", new String[]{Long.toString(starttime),Long.toString(endtime)});
	}
	
	public void deleteGpsInfo(long starttime){
		ContentResolver cr = mContext.getContentResolver();
		cr.delete(DataBaseContants.CONTENT_URI_GPSSPORT, DataBaseContants.GPS_STARTTIME + " = ?", new String[]{Long.toString(starttime)});
	}
	
	public void deleteGpsFromID(long gpsId){
		ContentResolver cr = mContext.getContentResolver();
		cr.delete(DataBaseContants.CONTENT_URI_GPSSPORT, DataBaseContants.GPS_ID + " = ?", new String[]{Long.toString(gpsId)});
	}
	
	public void deleteGpsInfo(long starttime,long endtime){
		ContentResolver cr = mContext.getContentResolver();
		cr.delete(DataBaseContants.CONTENT_URI_GPSSPORT, DataBaseContants.GPS_STARTTIME + " between ? and ?", new String[]{Long.toString(starttime),Long.toString(endtime)});
	}
	
	public List<GuidePointModel> selectPoint(long starttime,long endtime){
		List<GuidePointModel> pointList =new ArrayList<GuidePointModel>();
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr.query(DataBaseContants.CONTENT_URI_POINT,new String[] {"latitude","longtitude",
				"address","accuracy","provider","location_time","location_systime",
				"speed","altitude","gps_number","point_state","sync_state"}, DataBaseContants.LOCATION_TIME + " between ? and ?" , 
				new String[]{Long.toString(starttime),Long.toString(endtime)}, null);
		//Log.i("lsz", "-------cursor.getColumnCount()----"+cursor.getCount());
		if (cursor.getCount() > 0 && cursor.moveToFirst()) {
			for (int i = 0; i < cursor.getCount(); i++) {
				GuidePointModel mpoint=new GuidePointModel();
				mpoint.setLatitude(cursor.getDouble(cursor.getColumnIndex(DataBaseContants.LATITUDE)));
				mpoint.setLongitude(cursor.getDouble(cursor.getColumnIndex(DataBaseContants.LONGTITUDE)));
				mpoint.setAddress(cursor.getString(cursor.getColumnIndex(DataBaseContants.ADDRESS)));
				mpoint.setAccuracy(cursor.getFloat(cursor.getColumnIndex(DataBaseContants.ACCURACY)));
				mpoint.setProvider(cursor.getString(cursor.getColumnIndex(DataBaseContants.PROVIDER)));
				mpoint.setTime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.LOCATION_TIME)));
				mpoint.setSysTime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.LOCATION_SYS_TIME)));
				mpoint.setSpeed(cursor.getFloat(cursor.getColumnIndex(DataBaseContants.SPEED)));
				mpoint.setAltitude(cursor.getDouble(cursor.getColumnIndex(DataBaseContants.ALTITUDE)));
				mpoint.setGpsStatus(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_NUMBER)));
				mpoint.setPointState(cursor.getInt(cursor.getColumnIndex(DataBaseContants.LOCATION_POINT_STATE)));
				mpoint.setSyncState(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_SYNC)));
				pointList.add(mpoint);
				cursor.moveToNext();
			}
		} 
		cursor.close();
		cursor = null;
		return pointList;
	}
	
	public List<GuidePointModel> selectTempPoint(){
		List<GuidePointModel> pointList =new ArrayList<GuidePointModel>();
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr.query(DataBaseContants.CONTENT_URI_TEMPPOINT,new String[] {"latitude","longtitude",
				"address","accuracy","provider","location_time","location_systime",
				"speed","altitude","gps_number","point_state","sync_state"}, null , null, null);
		//Log.i("lsz", "-------cursor.getColumnCount()----"+cursor.getCount());
		if (cursor.getCount() > 0 && cursor.moveToFirst()) {
			for (int i = 0; i < cursor.getCount(); i++) {
				GuidePointModel mpoint=new GuidePointModel();
				mpoint.setGuideId(Tools.getPKL());
				mpoint.setLatitude(cursor.getDouble(cursor.getColumnIndex(DataBaseContants.LATITUDE)));
				mpoint.setLongitude(cursor.getDouble(cursor.getColumnIndex(DataBaseContants.LONGTITUDE)));
				mpoint.setAddress(cursor.getString(cursor.getColumnIndex(DataBaseContants.ADDRESS)));
				mpoint.setAccuracy(cursor.getFloat(cursor.getColumnIndex(DataBaseContants.ACCURACY)));
				mpoint.setProvider(cursor.getString(cursor.getColumnIndex(DataBaseContants.PROVIDER)));
				mpoint.setTime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.LOCATION_TIME)));
				mpoint.setSysTime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.LOCATION_SYS_TIME)));
				mpoint.setSpeed(cursor.getFloat(cursor.getColumnIndex(DataBaseContants.SPEED)));
				mpoint.setAltitude(cursor.getDouble(cursor.getColumnIndex(DataBaseContants.ALTITUDE)));
				mpoint.setGpsStatus(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_NUMBER)));
				mpoint.setPointState(cursor.getInt(cursor.getColumnIndex(DataBaseContants.LOCATION_POINT_STATE)));
				mpoint.setSyncState(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_SYNC)));
				pointList.add(mpoint);
				cursor.moveToNext();
			}
		} 
		cursor.close();
		cursor = null;
		return pointList;
	}
	
	public List<GuidePointModel> selectPoint(long starttime,long endtime,int pointstate){
		List<GuidePointModel> pointList =new ArrayList<GuidePointModel>();
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr.query(DataBaseContants.CONTENT_URI_POINT,new String[] {"latitude","longtitude",
				"address","accuracy","provider","location_time","location_systime",
				"speed","altitude","gps_number","point_state","sync_state"}, DataBaseContants.LOCATION_TIME + " between ? and ? and " 
				+ DataBaseContants.LOCATION_POINT_STATE+" > ?" , 
				new String[]{Long.toString(starttime),Long.toString(endtime),pointstate+""}, null);
		//Log.i("lsz", "-------cursor.getColumnCount()----"+cursor.getCount());
		if (cursor.getCount() > 0 && cursor.moveToFirst()) {
			for (int i = 0; i < cursor.getCount(); i++) {
				GuidePointModel mpoint=new GuidePointModel();
				mpoint.setLatitude(cursor.getDouble(cursor.getColumnIndex(DataBaseContants.LATITUDE)));
				mpoint.setLongitude(cursor.getDouble(cursor.getColumnIndex(DataBaseContants.LONGTITUDE)));
				mpoint.setAddress(cursor.getString(cursor.getColumnIndex(DataBaseContants.ADDRESS)));
				mpoint.setAccuracy(cursor.getFloat(cursor.getColumnIndex(DataBaseContants.ACCURACY)));
				mpoint.setProvider(cursor.getString(cursor.getColumnIndex(DataBaseContants.PROVIDER)));
				mpoint.setTime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.LOCATION_TIME)));
				mpoint.setSysTime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.LOCATION_SYS_TIME)));
				mpoint.setSpeed(cursor.getFloat(cursor.getColumnIndex(DataBaseContants.SPEED)));
				mpoint.setAltitude(cursor.getDouble(cursor.getColumnIndex(DataBaseContants.ALTITUDE)));
				mpoint.setGpsStatus(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_NUMBER)));
				mpoint.setPointState(cursor.getInt(cursor.getColumnIndex(DataBaseContants.LOCATION_POINT_STATE)));
				mpoint.setSyncState(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_SYNC)));
				pointList.add(mpoint);
				cursor.moveToNext();
			}
		} 
		cursor.close();
		cursor = null;
		return pointList;
	}
	
	public List<GuidePointModel> selectTempPoint(int pointstate){
		List<GuidePointModel> pointList =new ArrayList<GuidePointModel>();
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr.query(DataBaseContants.CONTENT_URI_TEMPPOINT,new String[] {"latitude","longtitude",
				"address","accuracy","provider","location_time","location_systime",
				"speed","altitude","gps_number","point_state","sync_state"}, DataBaseContants.LOCATION_POINT_STATE+" < ?" , 
				new String[]{pointstate+""}, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst()) {
			for (int i = 0; i < cursor.getCount(); i++) {
				GuidePointModel mpoint=new GuidePointModel();
				mpoint.setGuideId(Tools.getPKL());
				mpoint.setLatitude(cursor.getDouble(cursor.getColumnIndex(DataBaseContants.LATITUDE)));
				mpoint.setLongitude(cursor.getDouble(cursor.getColumnIndex(DataBaseContants.LONGTITUDE)));
				mpoint.setAddress(cursor.getString(cursor.getColumnIndex(DataBaseContants.ADDRESS)));
				mpoint.setAccuracy(cursor.getFloat(cursor.getColumnIndex(DataBaseContants.ACCURACY)));
				mpoint.setProvider(cursor.getString(cursor.getColumnIndex(DataBaseContants.PROVIDER)));
				mpoint.setTime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.LOCATION_TIME)));
				mpoint.setSysTime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.LOCATION_SYS_TIME)));
				mpoint.setSpeed(cursor.getFloat(cursor.getColumnIndex(DataBaseContants.SPEED)));
				mpoint.setAltitude(cursor.getDouble(cursor.getColumnIndex(DataBaseContants.ALTITUDE)));
				mpoint.setGpsStatus(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_NUMBER)));
				mpoint.setPointState(cursor.getInt(cursor.getColumnIndex(DataBaseContants.LOCATION_POINT_STATE)));
				mpoint.setSyncState(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_SYNC)));
				pointList.add(mpoint);
				cursor.moveToNext();
			}
		} 
		cursor.close();
		cursor = null;
		return pointList;
	}
	
	public List<Integer> selectPointID(long starttime,long endtime,int syncState){
		List<Integer> listPointId =new ArrayList<Integer>();
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr.query(DataBaseContants.CONTENT_URI_POINT,new String[] {DataBaseContants.GPS_ID}, DataBaseContants.LOCATION_TIME + " between ? and ? and " 
				+ DataBaseContants.GPS_SYNC+" != ?" , 
				new String[]{Long.toString(starttime),Long.toString(endtime),syncState+""}, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst()) {
			for (int i = 0; i < cursor.getCount(); i++) {
				listPointId.add(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_ID)));
				cursor.moveToNext();
			}
		} 
		cursor.close();
		cursor = null;
		return listPointId;
	}
	
	public GuidePointModel selectFirstPoint(long starttime,long endtime){
		GuidePointModel firstPonit =new GuidePointModel();
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr.query(DataBaseContants.CONTENT_URI_POINT,new String[] { DataBaseContants.LATITUDE,DataBaseContants.LONGTITUDE,
				DataBaseContants.ADDRESS,DataBaseContants.ACCURACY,DataBaseContants.PROVIDER,DataBaseContants.LOCATION_TIME,DataBaseContants.LOCATION_SYS_TIME,
				DataBaseContants.SPEED,DataBaseContants.ALTITUDE,DataBaseContants.GPS_NUMBER,DataBaseContants.LOCATION_POINT_STATE,DataBaseContants.GPS_SYNC}, DataBaseContants.LOCATION_TIME + " between ? and ?" , 
				new String[]{Long.toString(starttime),Long.toString(endtime)}, null);
		//Log.d("lsz", "cursor.getCount()----------------------"+cursor.getCount());
		if (cursor.getCount() > 0 && cursor.moveToFirst()) {
				firstPonit.setLatitude(cursor.getDouble(cursor.getColumnIndex(DataBaseContants.LATITUDE)));
				firstPonit.setLongitude(cursor.getDouble(cursor.getColumnIndex(DataBaseContants.LONGTITUDE)));
				firstPonit.setAddress(cursor.getString(cursor.getColumnIndex(DataBaseContants.ADDRESS)));
				firstPonit.setAccuracy(cursor.getFloat(cursor.getColumnIndex(DataBaseContants.ACCURACY)));
				firstPonit.setProvider(cursor.getString(cursor.getColumnIndex(DataBaseContants.PROVIDER)));
				firstPonit.setTime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.LOCATION_TIME)));
				firstPonit.setSysTime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.LOCATION_SYS_TIME)));
				firstPonit.setSpeed(cursor.getFloat(cursor.getColumnIndex(DataBaseContants.SPEED)));
				firstPonit.setAltitude(cursor.getDouble(cursor.getColumnIndex(DataBaseContants.ALTITUDE)));
				firstPonit.setGpsStatus(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_NUMBER)));
				firstPonit.setPointState(cursor.getInt(cursor.getColumnIndex(DataBaseContants.LOCATION_POINT_STATE)));
				firstPonit.setSyncState(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_SYNC)));
		} 
		cursor.close();
		cursor = null;
		return firstPonit;
	}
	
	public GuidePointModel selectLasttPoint(long starttime,long endtime){
		GuidePointModel firstPonit =new GuidePointModel();
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr.query(DataBaseContants.CONTENT_URI_POINT,new String[] { DataBaseContants.LATITUDE,DataBaseContants.LONGTITUDE,
				DataBaseContants.ADDRESS,DataBaseContants.ACCURACY,DataBaseContants.PROVIDER,DataBaseContants.LOCATION_TIME,DataBaseContants.LOCATION_SYS_TIME,
				DataBaseContants.SPEED,DataBaseContants.ALTITUDE,DataBaseContants.GPS_NUMBER,DataBaseContants.LOCATION_POINT_STATE,DataBaseContants.GPS_SYNC}, DataBaseContants.LOCATION_TIME + " between ? and ?" , 
				new String[]{Long.toString(starttime),Long.toString(endtime)}, null);
				//Log.i("lsz", "-------cursor.getColumnCount()----"+cursor.getCount());
		if (cursor.getCount() > 0 && cursor.moveToLast()) {
			    //Log.i("lsz", "-------cursor.getPosition()----"+cursor.getPosition());
				//Log.i("lsz",  + cursor.getDouble(cursor.getColumnIndex(DataBaseContants.LATITUDE))+"-----------"+cursor.getDouble(cursor.getColumnIndex(DataBaseContants.LONGTITUDE)));
				firstPonit.setLatitude(cursor.getDouble(cursor.getColumnIndex(DataBaseContants.LATITUDE)));
				firstPonit.setLongitude(cursor.getDouble(cursor.getColumnIndex(DataBaseContants.LONGTITUDE)));
				firstPonit.setAddress(cursor.getString(cursor.getColumnIndex(DataBaseContants.ADDRESS)));
				firstPonit.setAccuracy(cursor.getFloat(cursor.getColumnIndex(DataBaseContants.ACCURACY)));
				firstPonit.setProvider(cursor.getString(cursor.getColumnIndex(DataBaseContants.PROVIDER)));
				firstPonit.setTime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.LOCATION_TIME)));
				firstPonit.setSysTime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.LOCATION_SYS_TIME)));
				firstPonit.setSpeed(cursor.getFloat(cursor.getColumnIndex(DataBaseContants.SPEED)));
				firstPonit.setAltitude(cursor.getDouble(cursor.getColumnIndex(DataBaseContants.ALTITUDE)));
				firstPonit.setGpsStatus(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_NUMBER)));
				firstPonit.setPointState(cursor.getInt(cursor.getColumnIndex(DataBaseContants.LOCATION_POINT_STATE)));
				firstPonit.setSyncState(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_SYNC)));
		} 
		cursor.close();
		cursor = null;
		return firstPonit;
	}
	
	
	public List<OperationTimeModel> selectOperation(long starttime,long endtime){
		List<OperationTimeModel> operationList =new ArrayList<OperationTimeModel>();
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr.query(DataBaseContants.CONTENT_URI_OPERATION,new String[] { "*" }, DataBaseContants.OPERATION_TIME + " between ? and ?" , new String[]{Long.toString(starttime),Long.toString(endtime)}, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst()) {
			for (int i = 0; i < cursor.getCount(); i++) {
				OperationTimeModel mOperation=new OperationTimeModel();
				mOperation.setOperationtime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.OPERATION_TIME)));
				mOperation.setOperationSystime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.OPERATION_SYSTIME)));
				mOperation.setOperationState(cursor.getInt(cursor.getColumnIndex(DataBaseContants.OPERATION_STATE)));
				mOperation.setSyncState(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_SYNC)));
				operationList.add(mOperation);
				cursor.moveToNext();
			}
		} 
		cursor.close();
		cursor = null;
		return operationList;
	}
	
	public List<OperationTimeModel> selectOperation(long starttime,long endtime,int operation){
		List<OperationTimeModel> operationList =new ArrayList<OperationTimeModel>();
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr.query(DataBaseContants.CONTENT_URI_OPERATION,new String[] { "*" }, DataBaseContants.OPERATION_TIME + " between ? and ? and "
					+DataBaseContants.OPERATION_STATE+" = ? " , new String[]{Long.toString(starttime),Long.toString(endtime),operation+""}, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst()) {
			for (int i = 0; i < cursor.getCount(); i++) {
				OperationTimeModel mOperation=new OperationTimeModel();
				mOperation.setOperationtime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.OPERATION_TIME)));
				mOperation.setOperationSystime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.OPERATION_SYSTIME)));
				mOperation.setOperationState(cursor.getInt(cursor.getColumnIndex(DataBaseContants.OPERATION_STATE)));
				mOperation.setSyncState(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_SYNC)));
				operationList.add(mOperation);
				cursor.moveToNext();
			}
		} 
		cursor.close();
		cursor = null;
		return operationList;
	}
	
	public List<Integer> selectOperationId(long starttime,long endtime,int syncState){
		List<Integer> operationListId =new ArrayList<Integer>();
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr.query(DataBaseContants.CONTENT_URI_OPERATION,new String[] { "*" }, DataBaseContants.OPERATION_TIME + " between ? and ? and "
					+DataBaseContants.GPS_SYNC+" != ? " , new String[]{Long.toString(starttime),Long.toString(endtime),syncState+""}, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst()) {
			for (int i = 0; i < cursor.getCount(); i++) {
				operationListId.add(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_ID)));
				cursor.moveToNext();
			}
		} 
		cursor.close();
		cursor = null;
		return operationListId;
	}
	
	public long selectLastOperation(int operation){
		long startTime=0;
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr.query(DataBaseContants.CONTENT_URI_OPERATION,new String[] {DataBaseContants.OPERATION_TIME}, DataBaseContants.OPERATION_STATE + " = ?" , new String[]{operation+""}, null);
		if (cursor.getCount() > 0 && cursor.moveToLast()) {
			startTime=cursor.getLong(cursor.getColumnIndex(DataBaseContants.OPERATION_TIME));
		} 
		cursor.close();
		cursor = null;
		return startTime;
	}
	
	public long selectOperSysTime(int operation){
		long startTime=0;
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr.query(DataBaseContants.CONTENT_URI_OPERATION,new String[] {DataBaseContants.OPERATION_SYSTIME}, DataBaseContants.OPERATION_STATE + " = ?" , new String[]{operation+""}, null);
		if (cursor.getCount() > 0 && cursor.moveToLast()) {
			startTime=cursor.getLong(cursor.getColumnIndex(DataBaseContants.OPERATION_SYSTIME));
		} 
		cursor.close();
		cursor = null;
		return startTime;
	}
	
	public List<OperationTimeModel> selectlistOperTime(int operation){
		List<OperationTimeModel> operationList =new ArrayList<OperationTimeModel>();
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr.query(DataBaseContants.CONTENT_URI_OPERATION,new String[] { "*" }, DataBaseContants.OPERATION_STATE + " = ?" , new String[]{operation+""}, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst()) {
			for (int i = 0; i < cursor.getCount(); i++) {
				OperationTimeModel mOperation=new OperationTimeModel();
				mOperation.setOperationtime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.OPERATION_TIME)));
				mOperation.setOperationSystime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.OPERATION_SYSTIME)));
				mOperation.setOperationState(cursor.getInt(cursor.getColumnIndex(DataBaseContants.OPERATION_STATE)));
				mOperation.setSyncState(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_SYNC)));
				operationList.add(mOperation);
				cursor.moveToNext();
			}
		} 
		cursor.close();
		cursor = null;
		return operationList;
	}
	
	public List<GpsSportDataModel> selectGpsInfo(long starttime,long endtime){
		List<GpsSportDataModel> gpsInfoList =new ArrayList<GpsSportDataModel>();
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr.query(DataBaseContants.CONTENT_URI_GPSSPORT,new String[] { "*" }, DataBaseContants.GPS_STARTTIME + " between ? and ?" , new String[]{Long.toString(starttime),Long.toString(endtime)}, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst()) {
			for (int i = 0; i < cursor.getCount(); i++) {
				GpsSportDataModel mGpsInfo=new GpsSportDataModel();
				mGpsInfo.setStarttime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.GPS_STARTTIME)));
				mGpsInfo.setEndtime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.GPS_ENDTIME)));
				mGpsInfo.setStarSysttime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.GPS_SYSSTARTTIME)));
				mGpsInfo.setEndSystime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.GPS_SYSENDTIME)));
				mGpsInfo.setDurationtime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.GPS_DURATIONTIME)));
				mGpsInfo.setAvespeed(cursor.getFloat(cursor.getColumnIndex(DataBaseContants.AVESPEED)));
				mGpsInfo.setTotalDistance(cursor.getFloat(cursor.getColumnIndex(DataBaseContants.TOTAL_DISTANCE)));
				mGpsInfo.setSteps(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_STEPS)));
				mGpsInfo.setCalorie(cursor.getFloat(cursor.getColumnIndex(DataBaseContants.GPS_CALORIE)));
				mGpsInfo.setStartAddress(cursor.getString(cursor.getColumnIndex(DataBaseContants.GPS_STARTADDRESS)));
				mGpsInfo.setEndAddress(cursor.getString(cursor.getColumnIndex(DataBaseContants.GPS_ENDADDRESS)));
				mGpsInfo.setSyncState(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_SYNC)));
				gpsInfoList.add(mGpsInfo);
				cursor.moveToNext();
			}
		} 
		cursor.close();
		cursor = null;
		return gpsInfoList;
	}
	
	public GpsSportDataModel selectGpsInfo(long starttime){
		GpsSportDataModel mGpsInfo=new GpsSportDataModel();
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr.query(DataBaseContants.CONTENT_URI_GPSSPORT,new String[] { "*" }, DataBaseContants.GPS_STARTTIME + " = ?" , new String[]{Long.toString(starttime)}, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst()) {
				mGpsInfo.setStarttime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.GPS_STARTTIME)));
				mGpsInfo.setEndtime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.GPS_ENDTIME)));
				mGpsInfo.setStarSysttime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.GPS_SYSSTARTTIME)));
				mGpsInfo.setEndSystime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.GPS_SYSENDTIME)));
				mGpsInfo.setDurationtime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.GPS_DURATIONTIME)));
				mGpsInfo.setAvespeed(cursor.getFloat(cursor.getColumnIndex(DataBaseContants.AVESPEED)));
				mGpsInfo.setTotalDistance(cursor.getFloat(cursor.getColumnIndex(DataBaseContants.TOTAL_DISTANCE)));
				mGpsInfo.setSteps(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_STEPS)));
				mGpsInfo.setCalorie(cursor.getFloat(cursor.getColumnIndex(DataBaseContants.GPS_CALORIE)));
				mGpsInfo.setStartAddress(cursor.getString(cursor.getColumnIndex(DataBaseContants.GPS_STARTADDRESS)));
				mGpsInfo.setEndAddress(cursor.getString(cursor.getColumnIndex(DataBaseContants.GPS_ENDADDRESS)));
				mGpsInfo.setSyncState(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_SYNC)));
		} 
		cursor.close();
		cursor = null;
		return mGpsInfo;
	}
	
	public GpsSportDataModel selectGpsInfoForID(long gpsId){
		GpsSportDataModel mGpsInfo=new GpsSportDataModel();
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr.query(DataBaseContants.CONTENT_URI_GPSSPORT,new String[] { "*" }, DataBaseContants.GPS_ID + " = ?" , new String[]{Long.toString(gpsId)}, null);
		if (cursor.getCount() > 0 && cursor.moveToFirst()) {
				mGpsInfo.setStarttime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.GPS_STARTTIME)));
				mGpsInfo.setEndtime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.GPS_ENDTIME)));
				mGpsInfo.setStarSysttime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.GPS_SYSSTARTTIME)));
				mGpsInfo.setEndSystime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.GPS_SYSENDTIME)));
				mGpsInfo.setDurationtime(cursor.getLong(cursor.getColumnIndex(DataBaseContants.GPS_DURATIONTIME)));
				mGpsInfo.setAvespeed(cursor.getFloat(cursor.getColumnIndex(DataBaseContants.AVESPEED)));
				mGpsInfo.setTotalDistance(cursor.getFloat(cursor.getColumnIndex(DataBaseContants.TOTAL_DISTANCE)));
				mGpsInfo.setSteps(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_STEPS)));
				mGpsInfo.setCalorie(cursor.getFloat(cursor.getColumnIndex(DataBaseContants.GPS_CALORIE)));
				mGpsInfo.setStartAddress(cursor.getString(cursor.getColumnIndex(DataBaseContants.GPS_STARTADDRESS)));
				mGpsInfo.setEndAddress(cursor.getString(cursor.getColumnIndex(DataBaseContants.GPS_ENDADDRESS)));
				mGpsInfo.setSyncState(cursor.getInt(cursor.getColumnIndex(DataBaseContants.GPS_SYNC)));
		} 
		cursor.close();
		cursor = null;
		return mGpsInfo;
	}
	
	
	public double[] findGpsBound(long starttime,long endtime){
		double infos[] = new double [4];
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr.query(DataBaseContants.CONTENT_URI_POINT,new String[] { "MAX("+DataBaseContants.LATITUDE+"),MAX("+DataBaseContants.LONGTITUDE+")," +
				"MIN("+DataBaseContants.LATITUDE+"),MIN("+DataBaseContants.LONGTITUDE+")" },  DataBaseContants.LOCATION_TIME + " between ? and ?" , 
				new String[]{Long.toString(starttime),Long.toString(endtime)}, null);
		Log.i("hello","s:"+starttime +",e:"+endtime);
		if (cursor.getCount() > 0 && cursor.moveToFirst()) {
			infos[0] = cursor.getDouble(0);
			infos[1] = cursor.getDouble(1);
			infos[2] = cursor.getDouble(2);
			infos[3] = cursor.getDouble(3);
			Log.i("hello", infos[0] + ","+infos[1] + ","+infos[2] + ","+infos[3] + ",");
		}		
		cursor.close();
		cursor = null;
		
		for(double info :infos){
			if(info == 0.0d){
				return null;
			}
		}
		return infos;
	}
	
	public void updateGpsInfo(GpsSportDataModel gpsSportDataModel){
		ContentValues gpsInfoItem = new ContentValues();
		if(gpsSportDataModel!=null){
			gpsInfoItem.put(DataBaseContants.GPS_STARTTIME, gpsSportDataModel.getStarttime());
			gpsInfoItem.put(DataBaseContants.GPS_ENDTIME, gpsSportDataModel.getEndtime());
			gpsInfoItem.put(DataBaseContants.GPS_SYSSTARTTIME, gpsSportDataModel.getStarSysttime());
			gpsInfoItem.put(DataBaseContants.GPS_SYSENDTIME, gpsSportDataModel.getEndSystime());
			gpsInfoItem.put(DataBaseContants.GPS_DURATIONTIME,gpsSportDataModel.getDurationtime());
			gpsInfoItem.put(DataBaseContants.AVESPEED, gpsSportDataModel.getAvespeed());
			gpsInfoItem.put(DataBaseContants.TOTAL_DISTANCE, gpsSportDataModel.getTotalDistance());
			gpsInfoItem.put(DataBaseContants.GPS_STEPS, gpsSportDataModel.getSteps());
			gpsInfoItem.put(DataBaseContants.GPS_CALORIE,gpsSportDataModel.getCalorie());
			gpsInfoItem.put(DataBaseContants.GPS_STARTADDRESS,gpsSportDataModel.getStartAddress());
			gpsInfoItem.put(DataBaseContants.GPS_ENDADDRESS,gpsSportDataModel.getEndAddress());
			gpsInfoItem.put(DataBaseContants.GPS_SYNC, gpsSportDataModel.getSyncState());
		}
		mContext.getContentResolver().update(DataBaseContants.CONTENT_URI_GPSSPORT, gpsInfoItem, DataBaseContants.GPS_ID + "= ?",new String[]{Long.toString(gpsSportDataModel.getGpsId())});
	}
	
	public static boolean UpdateGPSInfo(Context mCtx,double latitude,double longtitude,String address){
		ContentResolver cr = mCtx.getContentResolver();
		String[] selectionArgs ={latitude+"",longtitude+""};
		ContentValues values = new ContentValues();
		values.put(DataBaseContants.ADDRESS, address);
		int res =  cr.update(DataBaseContants.CONTENT_URI_POINT, values, DataBaseContants.LATITUDE + "= ? and " + 
					DataBaseContants.LONGTITUDE + " = ? ", selectionArgs );
		Log.i("hello", "updateData:"+res);
		if(res == -1){
			return false;
		}
		return true;		
	}
	
	public static void insertSleep(Context mCtx,int type,long startTime,long endTime,String turnData){
		DBOpenHelper dbHelper = new DBOpenHelper(mCtx);
		SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
		
		String[] columns = { DataBaseContants.SLEEP_ID };
		
		String selection = DataBaseContants.SLEEP_TYPE + " = ? and " + DataBaseContants.SLEEP_STARTTIME + " = ? and "
				+ DataBaseContants.SLEEP_ENDTIME + " = ? and " + DataBaseContants.SLEEP_TURNDATA + " = ?  ";
		
		String[] selectionArgs  = { ""+type , ""+startTime , ""+endTime , turnData };
		
		Cursor mCursor = sqlDB.query(DataBaseContants.TABLE_SLEEP, columns , selection, selectionArgs, null, null, null);
		
		if(mCursor == null || ( mCursor!=null&&mCursor.getCount() == 0 ) ){
			ContentValues values = new ContentValues();
			values.put(DataBaseContants.SLEEP_ID, Tools.getPKL());
			values.put(DataBaseContants.SLEEP_TYPE, type);
			values.put(DataBaseContants.SLEEP_STARTTIME, startTime);
			values.put(DataBaseContants.SLEEP_ENDTIME, endTime);
			values.put(DataBaseContants.SLEEP_TURNDATA, turnData);
			sqlDB.insert(DataBaseContants.TABLE_SLEEP, null, values);
		}
		
		if(mCursor != null){ mCursor.close();}
		mCursor = null;		
	}
	
	public static List<SleepItem> getSleepItem(Context mCtx,long start,long end){
		DecimalFormat intFormat = new DecimalFormat("#00");
		List<SleepItem> sleepItems = new ArrayList<SleepItem>();
		
		DBOpenHelper dbHelper = new DBOpenHelper(mCtx);
		SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
		String[] columns = { DataBaseContants.SLEEP_ID, DataBaseContants.SLEEP_TYPE, DataBaseContants.SLEEP_STARTTIME, DataBaseContants.SLEEP_ENDTIME, DataBaseContants.SLEEP_TURNDATA};
		String selection = DataBaseContants.SLEEP_TYPE + " = ? and " + DataBaseContants.SLEEP_ENDTIME + " >= ?  and  " + DataBaseContants.SLEEP_ENDTIME + " <= ? ";
		String[] selectionArgs  = { "0" , ""+start , ""+end };
		Cursor mCursor = sqlDB.query(DataBaseContants.TABLE_SLEEP, columns , selection, selectionArgs, null, null, null);
		if (mCursor != null) {
			while (mCursor.moveToNext()) {
				long id = mCursor.getLong(mCursor.getColumnIndex(DataBaseContants.SLEEP_ID));
				int type = mCursor.getInt(mCursor.getColumnIndex(DataBaseContants.SLEEP_TYPE));
				long startTime = mCursor.getLong(mCursor.getColumnIndex(DataBaseContants.SLEEP_STARTTIME));
				long endTime = mCursor.getLong(mCursor.getColumnIndex(DataBaseContants.SLEEP_ENDTIME));
				String turnData = mCursor.getString(mCursor.getColumnIndex(DataBaseContants.SLEEP_TURNDATA));
				
				Calendar startCal = SleepTools.getCalendar(startTime);
				Calendar endCal = SleepTools.getCalendar(endTime);
				Log.i("hepenghui", "startCal="+SleepTools.getCalendar(startTime));
				Log.i("hepenghui", "endCal="+SleepTools.getCalendar(endTime));

				long timeSub = endCal.getTimeInMillis() - startCal.getTimeInMillis();
				Log.i("hepenghui", "timeSub="+endCal.getTimeInMillis());	
				Log.i("hepenghui", "timeSub="+startCal.getTimeInMillis());	

				int sub = (int)timeSub / 1000 / (60 * 30);//数据每半个小时记录一次
				Log.i("hepenghui", "sub="+ (int)timeSub / 1000 / (60 * 30));	

				List<Integer> turnArray = SleepTools.getData(turnData,sub);
				List<SleepBean> beans = SleepTools.getSleepBean(startCal,endCal,turnArray);
//		Heph
				int deepSleep = SleepTools.getDeepSleep(turnArray);
				int lightSleep = (int) (timeSub/1000 - deepSleep);
				
				SleepItem item = new SleepItem();
				item.setId(id);
				item.setStartCal(startCal);
				item.setEndCal(endCal);
// Heph
				item.setmDSleepT(deepSleep);
				item.setmWSleepT(lightSleep);
				item.setData(beans);
				item.setmSleepT(SleepTools.getDurationTime(startTime, endTime));
				
				String startTimeString = intFormat.format(startCal.get(Calendar.HOUR_OF_DAY)) + ":" + intFormat.format(startCal.get(Calendar.MINUTE));
				String endTimeString = intFormat.format(endCal.get(Calendar.HOUR_OF_DAY)) + ":" + intFormat.format(endCal.get(Calendar.MINUTE));
				
				item.setmStartT(startTimeString);
				item.setmEndT(endTimeString);
				Log.i("hepenghui", "setmStartT="+ startTimeString);	
				Log.i("hepenghui", "setmEndT="+ endTimeString);	

				sleepItems.add(item);
			}
		}
		return sleepItems;
	}
	
	public static void insertClassicSleep(Context mCtx, String date, String details){
		DBOpenHelper dbHelper = new DBOpenHelper(mCtx);
		SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();		
		String[] columns = { DataBaseContants.SLEEP_ID };		
		String selection = DataBaseContants.DATE + " = ? and " + DataBaseContants.SLEEP_DETAILS + " =? ";		
		String[] selectionArgs  = { date, details };		
		Cursor mCursor = sqlDB.query(DataBaseContants.TABLE_SLEEP_2, columns , selection, selectionArgs, null, null, null);
		mCursor.moveToFirst();
		if (mCursor.getCount() == 0) {
			ContentValues values = new ContentValues();
			values.put(DataBaseContants.DATE, date);
			values.put(DataBaseContants.SLEEP_DETAILS, details);
			sqlDB.insert(DataBaseContants.TABLE_SLEEP_2, null, values);
		}
		sqlDB.close();
		mCursor.close();
		mCursor = null;
	}

	public static List<SleepItem> getClassicSleepItem(Context mCtx, String date) {
		DBOpenHelper dbHelper = new DBOpenHelper(mCtx);
		SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
		String[] columns = { DataBaseContants.SLEEP_ID, DataBaseContants.DATE, DataBaseContants.SLEEP_DETAILS};
		String selection = DataBaseContants.DATE + " = ? ";
		String[] selectionArgs  = { date };
		Cursor mCursor = sqlDB.query(DataBaseContants.TABLE_SLEEP_2, columns , selection, selectionArgs, null, null, null);
		mCursor.moveToFirst();
		StringBuilder details = new StringBuilder();
		int count = mCursor.getCount();
		if (count > 0) {
			for(int i = 0; i < count; i++) {
				details.append(mCursor.getString(mCursor.getColumnIndex(DataBaseContants.SLEEP_DETAILS)));
				mCursor.moveToNext();
			}
		}
		sqlDB.close();
		mCursor.close();
		mCursor = null;
		
		List<SleepItem> sleepItems = new ArrayList<SleepItem>();
		String[] s = details.toString().split("\\|");
		int number = s.length;
		if (number > 1) {
			String startTime;
			String endTime;
			int stime = Integer.parseInt(s[1]);
			if (stime >= 2100) {
				startTime = Tools.getDate(date, 1).replaceAll("-", "") + s[1];
			} else {
				startTime = date.replaceAll("-", "") + s[1];
			}
			int etime = Integer.parseInt(s[number - 1]);
			if (etime >= 2100) {
				endTime = date.replaceAll("-", "") + s[number - 1];
			} else {
				endTime = Tools.getDate(date, -1).replaceAll("-", "") + s[number - 1];
			}
			Calendar startCal = SleepTools.getCalendar(Long.valueOf(startTime));
			Calendar endCal = SleepTools.getCalendar(Long.valueOf(endTime));
			int deepSleep = SleepTools.getDeepSleep2(s);
			int lightSleep = SleepTools.getlightSleep2(s);
			List<SleepBean> beans = SleepTools.getSleepBean2(s);
			SleepItem item = new SleepItem();
			item.setStartCal(startCal);
			item.setEndCal(endCal);
			item.setmDSleepT(deepSleep);
			item.setmWSleepT(lightSleep);
			item.setData(beans);
			item.setmSleepT(SleepTools.getIntervalTime(startTime, endTime));
			item.setmStartT(SleepTools.formatRemoteTime(s[1]));
			item.setmEndT(SleepTools.formatRemoteTime(s[number - 1]));

			sleepItems.add(item);
		}
		return sleepItems;
	}
}
