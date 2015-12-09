package com.zhuoyou.plugin.gps;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.zhuoyou.plugin.database.DataBaseContants;

public class GpsUtil {
	
	public static List<GuidePointModel> handlePoint(List<GuidePointModel> listPoint){
		 if(listPoint.size()<5){
			 return listPoint;
		 }else if(listPoint.size()>4 && listPoint.size()<10){
			return avePoint(listPoint,2);   
		 }else if(listPoint.size()>9 && listPoint.size()<20){
				return avePoint(listPoint,4);   
		 }else{
				return avePoint(listPoint,6);   
		 }
	 }
	 
	 private static List<GuidePointModel> avePoint(List<GuidePointModel> listPoint,int avepoint){
		 List<GuidePointModel> filterList=new ArrayList<GuidePointModel>();
		 List<GuidePointModel> smallList=new ArrayList<GuidePointModel>();
		 for(int i=0;i<listPoint.size();i++){
			 smallList.add(listPoint.get(i));
			 if(smallList.size()==avepoint){
				 double totalLatitude=0;
				 double totalLongitude=0;
				 for(int j=0;j<smallList.size();j++){
					 totalLatitude=totalLatitude+smallList.get(j).getLatitude();
					 totalLongitude=totalLongitude+smallList.get(j).getLongitude();
				 }
				 double aveLatitude=totalLatitude/avepoint;
				 double aveLongitude=totalLongitude/avepoint;
				 GuidePointModel mguide=new GuidePointModel(aveLatitude,aveLongitude);
				 filterList.add(mguide);
				 smallList.remove(0);
			 }
		 }
		 return filterList;
	 }
	 
	 /**
		 * 根据经纬度获取地址信息
		 * @param mPoint	点的坐标
		 * @param handle	回传的handler (不允许为空)
		 * @param flag		回传的标志位 	(msg.what)
		 */
		public static void getAdresByNet(final Context mCtx,final GuidePointModel mPoint, final Handler handle,final int flag){
//			final GuidePointModel mPoint = point.clone();
			final Message msg = handle.obtainMessage();
			GeocodeSearch mGSearch = new GeocodeSearch(mCtx);
	 		LatLonPoint mLatLongPoint = new LatLonPoint(mPoint.getLatitude(), mPoint.getLongitude());
			RegeocodeQuery mRQuery = new RegeocodeQuery(mLatLongPoint , 200, GeocodeSearch.AMAP);
			mGSearch.getFromLocationAsyn(mRQuery);
			mGSearch.setOnGeocodeSearchListener( new OnGeocodeSearchListener() {
				@Override
				public void  onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode){
					msg.what = flag;
					String address = "";
					if(rCode == 0 ){ 		//根据高德API 返回的rCode=0 时 为成功，其余为失败
						RegeocodeAddress mRAddress = regeocodeResult.getRegeocodeAddress();
						address = mRAddress.getFormatAddress();
					} else if (rCode == 27) {
						Log.i("hello","error_network");
					} else if (rCode == 32) {
						Log.i("hello","error_key");
					} else {
						Log.i("hello","error_other");
					}
					mPoint.setAddress(address);
					msg.obj = mPoint;
					handle.sendMessage(msg);
				}
				
				@Override
				public void onGeocodeSearched(GeocodeResult regeocodeResult, int rCode) {
					
				}
			});
		}
		
		public static int updateSport(Context mContext,Long gpsId,String startAddress,String endAddress,Long durationTime,Float avgSpeed,Float totalDis,Integer step,Float calorie){
			ContentResolver cr = mContext.getContentResolver();
			ContentValues updateValues = new ContentValues();
			updateValues.put(DataBaseContants.GPS_STARTADDRESS, startAddress);
			updateValues.put(DataBaseContants.GPS_ENDADDRESS, endAddress);
			if(durationTime!=null){
				updateValues.put(DataBaseContants.GPS_DURATIONTIME, durationTime);
			}
			if(avgSpeed!=null){
				updateValues.put(DataBaseContants.AVESPEED,avgSpeed);
			}
			if(totalDis!=null){
				updateValues.put(DataBaseContants.TOTAL_DISTANCE, totalDis);
			}
			if(step !=null){
				updateValues.put(DataBaseContants.GPS_STEPS, step);
			}
			if(calorie!=null){
				updateValues.put(DataBaseContants.GPS_CALORIE, calorie);
			}
			return cr.update(DataBaseContants.CONTENT_URI_GPSSPORT, updateValues, DataBaseContants.GPS_ID +" = ? " ,new String[]{ String.valueOf(gpsId) });
		}
}
