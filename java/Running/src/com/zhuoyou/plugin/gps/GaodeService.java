package com.zhuoyou.plugin.gps;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.mcube.lib.ped.PedBackgroundService;
import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.database.DataBaseUtil;
import com.zhuoyou.plugin.gps.ilistener.IStepListener;
import com.zhuoyou.plugin.gps.ilistener.MonitorWatcher;
import com.zhuoyou.plugin.gps.ilistener.SignalWatcher;
import com.zhuoyou.plugin.gps.ilistener.StepWatcher;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningItem;
import com.zhuoyou.plugin.running.Tools;



public class GaodeService extends Service implements LocationSource, AMapLocationListener{
	public static final String tag = "GaodeService";
	
	private MapView mapView;
    private AMap aMap;
    private OnLocationChangedListener mListener;
	private LocationManagerProxy mAMapLocationManager;
	private SharedPreferences sharepreference;
	private LocationManager locationManager;
//	private GpsStatus.Listener statusListener;
	private int satelliteNum=0;
	private int usedcount = 0;
	private java.text.SimpleDateFormat formatter;
	private DataBaseUtil mDataBaseUtil;
    private long locationTime=0;//获得定时时间，根据当前时间与之比较，来判断是否总未获得定位
    private long gpsSingalTime=0;//判定gps强弱
    private long location_is_change=0;
    private long gpsSingal_is_change=0;
    private Editor editor;
    
    private final static int DELAY_NO_GPSSINGAL = 1000; //运动过程中GPS无信号判定
    private final static int GPS_STRENGTH_JUDGE = 1001;  //GPS信号强弱判断
    private final static int SAVE_GPS_INFO = 1002;      //按暂停按钮时倒计时处理
    private final static int FIRST_GPS_ADDRESS = 1003; //第一个轨迹点的处理
    public static double mdistace=0;		//判断运动的距离
	public static boolean is_running; //判断是否处于划轨迹状态
	public static int gpsSignal_state=0;   //gps信号强度 0代表正在搜索，1代表信号强  2代表信号弱
	public static GpsSportDataModel gpsSportInfo = new GpsSportDataModel();  //存储临时的GPS运动数据
	public static TempDataModel tempDataModel = new TempDataModel();  //存储临时的GPS运动数据,该数据用于存储data表中
	private Resources res;
	
	private double sumRunDis;		// 运动的总距离
	private GuidePointModel upPoint;	//上一个点；
	private GuidePointModel firstPoint;
	private boolean is_first_point;
	public static int point_state=0; //判断点的装态 0代表正常点 1代表点击开始后的第一个点 3为继续 5为Service异常点 6为GPS无信号点
	public static String startAddress = "",endAddress = "";
	public static List<GuidePointModel> handlerList=new ArrayList<GuidePointModel>();  //该集合存储为存进数据库，待处理的点
	
	public static int hadRunStep;
	private int mRunStep;
	private int initRunStep;
	private String no_address;
	
	public static double mSumDis;	//存储临时运动距离
	public StepObserver mStepObserver;
	
	public static int baseStep;
	public static double baseDistance;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private List<GpsSatellite> numSatelliteList = new ArrayList<GpsSatellite>(); // 卫星信号

	/**
	  * 卫星状态监听器
	  */
	private final GpsStatus.Listener statusListener = new GpsStatus.Listener() {
		public void onGpsStatusChanged(int event) { // GPS状态变化时的回调，如卫星数
			GpsStatus status = locationManager.getGpsStatus(null); // 取当前状态
			updateGpsStatus(event, status);
		}
	};	
	
	
 	private void updateGpsStatus(int event, GpsStatus status) {                        
//		if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
			int maxSatellites = status.getMaxSatellites();
			Iterator<GpsSatellite> it = status.getSatellites().iterator();
			numSatelliteList.clear();
			while (it.hasNext() && satelliteNum <= maxSatellites) {
				GpsSatellite s = it.next();
				numSatelliteList.add(s);
				if(s.usedInFix())
					usedcount++;//可用卫星数量
				satelliteNum++;
			}
//		}
		String gps_state_show = "";
		switch(event){
			case GpsStatus.GPS_EVENT_STARTED:
				gps_state_show = "STARTED";
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				gps_state_show = "STOPPED";
				break;
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				gps_state_show = "FIRST_FIX";
				break;
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				gps_state_show = "SATELLITE_STATUS";
				break;
			
		}
		String gps_info = gps_state_show+usedcount+"/"+satelliteNum;
		//Toast.makeText(GaodeService.this, gps_info, Toast.LENGTH_SHORT).show();
	}	
	
	@Override
	public void onCreate() {
		super.onCreate();
		res = getResources();
		sumRunDis = 0;
		upPoint = null;
		is_first_point=true;
		mapView=new MapView(getApplicationContext());
		if (aMap == null) {
			aMap=mapView.getMap();
		}
		no_address=res.getString(R.string.gps_addressunknow);
		startAddress=no_address;
		mDataBaseUtil=new DataBaseUtil(getApplicationContext());
		gpsSingalTime=System.currentTimeMillis();
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		locationManager.addGpsStatusListener(statusListener);
		sharepreference = this.getSharedPreferences("gaode_location_info", Context.MODE_PRIVATE);
		mHandler.sendEmptyMessageDelayed(GPS_STRENGTH_JUDGE, 10*1000);
        initFilter();
		setUpMap();
		
		mStepObserver = new StepObserver();
		StepWatcher.getInstance().addWatcher(mStepObserver);
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		List<GuidePointModel> pointList=mDataBaseUtil.selectTempPoint(10);
		if(pointList.size()>0){
			if(pointList.size()>5){
				mDataBaseUtil.inserPoint(filterPoint(pointList));
			}else{
				mDataBaseUtil.inserPoint(pointList);
			}
			mDataBaseUtil.deleteTempPoint(0, conversTime());
			pointList.clear();
		}
		int operation_state=sharepreference.getInt("map_activity_state", 0);
		if(operation_state==2||operation_state == 4){
			GaoDeMapActivity.is_line=true;
			is_running = true;
		}else{
			GaoDeMapActivity.is_line=true;
			is_running = false;
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(handlerList.size()>0){
			if(handlerList.size()>5){
				mDataBaseUtil.inserPoint(filterPoint(handlerList));
			}else{
				mDataBaseUtil.inserPoint(handlerList);
			}
			mDataBaseUtil.deleteTempPoint(0, conversTime());
			handlerList.clear();
		}
		locationManager.removeGpsStatusListener(statusListener);
		deactivate();
		mapView.onDestroy();
		unregisterReceiver(mBroadcastReceiver);
		stopService(new Intent(getApplicationContext(),PedBackgroundService.class));
		StepWatcher.getInstance().removeWatcher(mStepObserver);
	}
	
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (mListener != null && amapLocation != null) {
			if (amapLocation!=null&& amapLocation.getAMapException().getErrorCode() == 0) {
				//次方法是判定gps强弱的
				if(amapLocation.getProvider()=="gps"|| amapLocation.getProvider().equals("gps")){
					if(GaodeService.gpsSignal_state !=1 ){
						GaodeService.gpsSignal_state=1;
						Editor edit = sharepreference.edit();
						edit.putInt("gps_singal_state", 1);
						edit.commit();
						gpsSingalTime=System.currentTimeMillis();
						SignalWatcher.getInstance().notifyWatchers(gpsSignal_state);
					}
				}
				if(is_running){
					//在轨迹运行状态进行的一系列处理：1、排除非GPS数据 2、排除精度大于15的数据  3、排除两点之前距离小于2m的点
					//数据存储方式的修改 1、在用户进行相关操作后的第一个点单独存储  2其他情况点数量积累到10个，统一处理后存储
					if(amapLocation.getProvider()=="gps"|| amapLocation.getProvider().equals("gps")){
						GuidePointModel tydguidePoint=initGuidePoint(amapLocation);
						if(is_first_point){
							boolean is_have=sharepreference.contains("is_have_beginaddr");
							startAddress=sharepreference.getString("is_have_beginaddr", no_address);
							firstPoint=tydguidePoint;
							is_first_point=false;
							if(!is_have){
								GpsUtil.getAdresByNet(GaodeService.this,firstPoint,firstHandler,0);
							}
						}
						/** design by zhongyang 20150117 */
						GpsUtil.getAdresByNet(GaodeService.this,tydguidePoint,adresHandler,0);
						locationTime=System.currentTimeMillis();
						if(tydguidePoint.getPointState()==0){
							if(tydguidePoint.getAccuracy()<15){
								/** 修改 计算SUM_DISTANCE 的距离 */
								double distance=calSumDistance(tydguidePoint);
								if(distance>2){
									MonitorWatcher.getInstance().notifyWatchers(tydguidePoint);
									mDataBaseUtil.inserTempPoint(tydguidePoint);
									handlerList.add(tydguidePoint);
									if(handlerList.size()==10){
										mDataBaseUtil.inserPoint(filterPoint(handlerList));
										mDataBaseUtil.deleteTempPoint(0, conversTime());
										handlerList.clear();
									}
								}
							}
						}else{
							/** 修改 计算SUM_DISTANCE 的距离 */
							calSumDistance(tydguidePoint);
							MonitorWatcher.getInstance().notifyWatchers(tydguidePoint);
							if(handlerList.size()!=0){
								if(handlerList.size()<5){
									mDataBaseUtil.inserPoint(handlerList);
								}else{
									mDataBaseUtil.inserPoint(filterPoint(handlerList));
								}
								mDataBaseUtil.deleteTempPoint(0, conversTime());
							}
							handlerList.clear();
							mDataBaseUtil.inserPoint(tydguidePoint);
						}
					}
				}
					
						boolean point_first=sharepreference.getBoolean("is_begin_point", true);
						if(point_first){
							GuidePointModel tydguidePoint = initGuidePoint(amapLocation);
							/** design by zhongyang 20150117 */
							MonitorWatcher.getInstance().notifyWatchers(tydguidePoint);
						}
			}
		}
	}
	
	private Handler adresHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 0){
				GuidePointModel mPoint = (GuidePointModel) msg.obj;
				if(mPoint != null){
					String addres = mPoint.getAddress();
					
					if(!TextUtils.isEmpty(addres)){
						if(!endAddress.equals(addres)){
							endAddress = addres;
						}
					}
				}
			}
		}
	};
	
	private Handler firstHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 0){
				GuidePointModel mPoint = (GuidePointModel) msg.obj;
				if(mPoint != null){
					String addres = mPoint.getAddress();
					
					if(!TextUtils.isEmpty(addres)){
						startAddress = addres;
						Editor edit = sharepreference.edit();
						edit.putString("is_have_beginaddr", startAddress);
						edit.commit();
					}else{
						mHandler.sendEmptyMessageDelayed(FIRST_GPS_ADDRESS, 30*1000);
					}
				}
			}
		}
	};
	
	 private List<GuidePointModel> filterPoint(List<GuidePointModel> listPoint){
		 List<GuidePointModel> filterList=new ArrayList<GuidePointModel>();
		 double firsLatitude=(listPoint.get(0).getLatitude()+listPoint.get(1).getLatitude()+listPoint.get(2).getLatitude())/3;
		 double firstLongitude=(listPoint.get(0).getLongitude()+listPoint.get(1).getLongitude()+listPoint.get(2).getLongitude())/3;
		 double lastLatitude=(listPoint.get(listPoint.size()-3).getLatitude()+listPoint.get(listPoint.size()-2).getLatitude()+listPoint.get(listPoint.size()-1).getLatitude())/3;
		 double lastLongitude=(listPoint.get(listPoint.size()-3).getLongitude()+listPoint.get(listPoint.size()-2).getLongitude()+listPoint.get(listPoint.size()-1).getLongitude())/3;
		 GuidePointModel firstPoint=new GuidePointModel(firsLatitude,firstLongitude);
		 GuidePointModel lastPoint=new GuidePointModel(lastLatitude, lastLongitude);
		 List<Double> listSpeed = new ArrayList<Double>();
		 for(int i=0;i<listPoint.size();i++){
			 if(i==0){
				 double distance=pointDistance(firstPoint, listPoint.get(0), listPoint.get(1));
				 double aveSpeed=distance*1000/((listPoint.get(2).getSysTime()-listPoint.get(0).getSysTime())/2
						 +(listPoint.get(1).getSysTime()-listPoint.get(0).getSysTime()));
				 listSpeed.add(aveSpeed);
			 }else if(i==listPoint.size()-1){
				 double distance=pointDistance(listPoint.get(listPoint.size()-2), listPoint.get(listPoint.size()-1), lastPoint);
				 double aveSpeed=distance*1000/((listPoint.get(listPoint.size()-1).getSysTime()-listPoint.get(listPoint.size()-3)
						 .getSysTime())/2+(listPoint.get(listPoint.size()-2).getSysTime()-listPoint.get(listPoint.size()-1).getSysTime()));
				 listSpeed.add(aveSpeed);
			 }else{
				 double distance=pointDistance(listPoint.get(i-1), listPoint.get(i), listPoint.get(i+1));
				 double aveSpeed=distance*1000/((listPoint.get(i+1).getSysTime()-listPoint.get(i-1).getSysTime()));
				 listSpeed.add(aveSpeed); 
			 }
		 }
		 Collections.sort(listSpeed);
		 double filterSpeed=0;
		 if(listSpeed.size()<6){
			 filterSpeed=listSpeed.get(listSpeed.size()-2);
		 }else if(listSpeed.size()>5 && listSpeed.size()<11){
			 filterSpeed=listSpeed.get(listSpeed.size()-3);
		 }else{
			 filterSpeed=listSpeed.get(listSpeed.size()-6);
		 }
		 
		 for(int i=0;i<listPoint.size();i++){
			 if(i==0){
				 double distance=pointDistance(firstPoint, listPoint.get(0), listPoint.get(1));
				 double aveSpeed=distance*1000/((listPoint.get(2).getSysTime()-listPoint.get(0).getSysTime())/2
						 +(listPoint.get(1).getSysTime()-listPoint.get(0).getSysTime()));
				 if(aveSpeed<filterSpeed){
					 filterList.add(listPoint.get(i));
				 }
			 }else if(i==listPoint.size()-1){
				 double distance=pointDistance(listPoint.get(listPoint.size()-2), listPoint.get(listPoint.size()-1), lastPoint);
				 double aveSpeed=distance*1000/((listPoint.get(listPoint.size()-1).getSysTime()-listPoint.get(listPoint.size()-3)
						 .getSysTime())/2+(listPoint.get(listPoint.size()-2).getSysTime()-listPoint.get(listPoint.size()-1).getSysTime()));
				 if(aveSpeed<filterSpeed){
					 filterList.add(listPoint.get(i));
				 }
			 }else{
				 double distance=pointDistance(listPoint.get(i-1), listPoint.get(i), listPoint.get(i+1));
				 double aveSpeed=distance*1000/((listPoint.get(i+1).getSysTime()-listPoint.get(i-1).getSysTime()));
				 if(aveSpeed<filterSpeed){
					 filterList.add(listPoint.get(i));
				 }
			 }
		 }
		 
		 return filterList;
		 
	 }
	 
	 private double pointDistance(GuidePointModel guide1,GuidePointModel guide2,GuidePointModel guide3){
		 double mdistance=0;
		 LatLng point1=new LatLng(guide1.getLatitude(), guide1.getLongitude());
		 LatLng point2=new LatLng(guide2.getLatitude(), guide2.getLongitude());
		 LatLng point3=new LatLng(guide3.getLatitude(), guide3.getLongitude());
		 double mdistace1 = AMapUtils.calculateLineDistance(point1, point2);
		 double mdistace2 = AMapUtils.calculateLineDistance(point2, point3);
		 mdistance=mdistace1+mdistace2;
		 return mdistance;	 
	 }
	
	public GuidePointModel initGuidePoint(AMapLocation amapLocation){
		long pointTime=conversTime();
		GuidePointModel mguidePoint=new GuidePointModel();
		mguidePoint.setGuideId(Tools.getPKL());
		mguidePoint.setLatitude(amapLocation.getLatitude());
		mguidePoint.setLongitude(amapLocation.getLongitude());
		mguidePoint.setAddress(amapLocation.getAddress());
		mguidePoint.setAccuracy(amapLocation.getAccuracy());
		mguidePoint.setAltitude(amapLocation.getAltitude());
		mguidePoint.setProvider(amapLocation.getProvider());
		mguidePoint.setSpeed(usedcount);
		mguidePoint.setTime(pointTime);
		mguidePoint.setSysTime(System.currentTimeMillis());
		mguidePoint.setSyncState(0);
		mguidePoint.setGpsStatus(satelliteNum);
		if(GaodeService.point_state==0){
			mguidePoint.setPointState(0);
		}else{
			mguidePoint.setPointState(GaodeService.point_state);
			if(amapLocation.getProvider()=="gps"|| amapLocation.getProvider().equals("gps")){
				GaodeService.point_state=0;
			}
		}
		return mguidePoint;
	}
	
	
	public static long conversTime(){
		Calendar mCal = Calendar.getInstance();
		int year = mCal.get(Calendar.YEAR);
		int month = mCal.get(Calendar.MONTH)+1;
		int days = mCal.get(Calendar.DAY_OF_MONTH);
		int hour = mCal.get(Calendar.HOUR_OF_DAY);
		int min = mCal.get(Calendar.MINUTE);
		int second = mCal.get(Calendar.SECOND);
		long pointTime = second + min*100L + hour*10000L + days*1000000L + month * 100000000L + year* 10000000000L;
		return pointTime;
	}
	
	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(this);
			//此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
			//注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用removeUpdates()方法来取消定位请求
			//在定位结束后，在合适的生命周期调用destroy()方法		
			//其中如果间隔时间为-1，则定位只定一次
			//在单次定位情况下，定位无论成功与否，都无需调用removeUpdates()方法移除请求，定位sdk内部会移除
			mAMapLocationManager.requestLocationData(
					LocationProviderProxy.AMapNetwork, 2*1000, 10, this);
		}
	}

	@Override
	public void deactivate() {
		mListener = null;
		if (mAMapLocationManager != null) {
			mAMapLocationManager.removeUpdates(this);
			mAMapLocationManager.destroy();
		}
		mAMapLocationManager = null;
	}// TODO Auto-generated method stub
	
	/**
	 * 此方法已经废弃
	 */
	@Override
	public void onLocationChanged(Location location) {
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		
	}

	private void initFilter(){
    	IntentFilter mGATTFilter = new IntentFilter();
        mGATTFilter.addAction("ACTION_STOP_GPSSPORT");
        mGATTFilter.addAction("ACTION_BEGIN_GPSSPORT");
        mGATTFilter.addAction("ACTION_CONTINUE_GPSSPORT");
        mGATTFilter.addAction("ACTION_COMPELE_GPSSPORT");
        mGATTFilter.addAction("com.zhuoyou.gaode.service.hello");
        registerReceiver(mBroadcastReceiver, mGATTFilter);
    }
	
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case DELAY_NO_GPSSINGAL:
				if(locationTime!=0){
						if(location_is_change!=locationTime){
							if(System.currentTimeMillis()-locationTime>60*1000){
								GaodeService.gpsSignal_state=2;
								Editor edit = sharepreference.edit();
								edit.putInt("gps_singal_state", 2);
								edit.commit();
								SignalWatcher.getInstance().notifyWatchers(GaodeService.gpsSignal_state);
								location_is_change=locationTime;
								GaodeService.point_state=6;
								OperationTimeModel mOperation=new OperationTimeModel();
								mOperation.setOperatId(Tools.getPKL());
						        mOperation.setOperationtime(conversTime());
						        mOperation.setOperationSystime(System.currentTimeMillis());
								mOperation.setOperationState(OperationTimeModel.NO_LAOCTION_SINGAL);
								mOperation.setSyncState(0);
								mDataBaseUtil.insertOperation(mOperation);
								//Toast.makeText(getApplicationContext(), res.getString(R.string.gps_canotGetLoaction), Toast.LENGTH_LONG).show();
							}
						}
				}
				mHandler.sendEmptyMessageDelayed(DELAY_NO_GPSSINGAL, 10*1000);
				break;
				case GPS_STRENGTH_JUDGE:
					if(gpsSingalTime!=0){
						if(gpsSingal_is_change!=gpsSingalTime){
							if(System.currentTimeMillis()-gpsSingalTime>60*1000){
								GaodeService.gpsSignal_state=2;
								Editor edit = sharepreference.edit();
								edit.putInt("gps_singal_state", 2);
								edit.commit();
								SignalWatcher.getInstance().notifyWatchers(GaodeService.gpsSignal_state=2);
								gpsSingal_is_change=gpsSingalTime;
							}
						}
				}
				mHandler.sendEmptyMessageDelayed(GPS_STRENGTH_JUDGE, 10*1000);
				break;
				case SAVE_GPS_INFO:
					compeleGpsSport();
					Intent cancelIntent = new Intent("ACTION_SAVE_GPSSPORT");
					sendBroadcast(cancelIntent);
					insertTempData();
					GaodeService.this.stopService(new Intent(GaodeService.this.getApplicationContext(),PedBackgroundService.class));
					GaodeService.this.stopSelf();
				break;
				case FIRST_GPS_ADDRESS:
					GpsUtil.getAdresByNet(GaodeService.this,firstPoint,firstHandler,0);
				break;
			}
			super.handleMessage(msg);
		};
	};
	
	private void insertTempData(){
		if(GaodeService.gpsSportInfo.getGpsId()!=0){
			mDataBaseUtil.insertGpsInfo(GaodeService.gpsSportInfo);
		}
		if(GaodeService.tempDataModel.getTempId()!=0){
			RunningItem runningItem = new RunningItem();
			runningItem.setDate(GaodeService.tempDataModel.getTempDate());
			runningItem.setStartTime(GaodeService.tempDataModel.getTempStaTime());
			runningItem.setDuration(GaodeService.tempDataModel.getTempDuration());
			runningItem.setEndTime(GaodeService.tempDataModel.getTempEndTime());
			runningItem.setCalories( GaodeService.tempDataModel.getTempCalories());
			runningItem.setSteps(GaodeService.tempDataModel.getTempStep());
			runningItem.setmType(GaodeService.tempDataModel.getTempType());
			runningItem.setisStatistics(GaodeService.tempDataModel.getTempStatistics());
			runningItem.setmWeight(GaodeService.tempDataModel.getTempStaAddress());
			runningItem.setmBmi(GaodeService.tempDataModel.getTempEndAddress());
			runningItem.setmImgUri(GaodeService.tempDataModel.getTempImageUrl());
			runningItem.setmExplain(Long.toString(GaodeService.tempDataModel.getTempGpsId()));
			Intent gpsIntent = new Intent("ACTION_GPS_INFO");
			gpsIntent.putExtra("gps_info", runningItem);
			sendBroadcast(gpsIntent);
		}
	}
	
	//完成运动后需要该变的标志位
	private void compeleGpsSport(){
        editor = sharepreference.edit();
		editor.putInt("map_activity_state",0 );	
		editor.putBoolean("is_begin_point", true);
		editor.putInt("gps_singal_state", 0);
		if(sharepreference.contains("is_have_beginaddr")){
			editor.remove("is_have_beginaddr");
		}
		if(sharepreference.contains("gps_sport_id")){
			editor.remove("gps_sport_id");
		}
		editor.commit();
		GaodeService.point_state=0;
		OperationTimeModel mOperation2 = new OperationTimeModel();
		mOperation2.setOperatId(Tools.getPKL());
		mOperation2.setOperationtime(conversTime());
		mOperation2.setOperationSystime(System.currentTimeMillis());
		mOperation2.setOperationState(OperationTimeModel.COMPLETE_GPS_GUIDE);
		mOperation2.setSyncState(0);
		mDataBaseUtil.insertOperation(mOperation2);
		GaoDeMapActivity.is_line=false;
		GaodeService.is_running=false;
	}
    
	//删除gps运动时，同步删除数据库里面的数据
		 private void deletefromDatabase(GpsSportDataModel mGps){
			 List<Integer> listGpsId = mDataBaseUtil.selectPointID(mGps.getStarttime(),conversTime(), 0);
			 List<Integer> listOperationId = mDataBaseUtil.selectOperationId(mGps.getStarttime(), conversTime(), 0);
			 if(listGpsId.size()>0){
				 for(int i=0;i<listGpsId.size();i++){
					 ContentValues runningItem = new ContentValues();
					 runningItem.put(DataBaseContants.GPS_TABLE, DataBaseContants.TABLE_POINT_NAME);
					 runningItem.put(DataBaseContants.GPS_DELETE, listGpsId.get(i));	
					 GaodeService.this.getContentResolver().insert(DataBaseContants.CONTENT_URI_GPSSYNC,runningItem);
				 }
			 }
			 if(listOperationId.size()>0){
				 for(int i=0;i<listOperationId.size();i++){
					 ContentValues runningItem = new ContentValues();
					 runningItem.put(DataBaseContants.GPS_TABLE, DataBaseContants.TABLE_OPERATION_NAME);
					 runningItem.put(DataBaseContants.GPS_DELETE, listOperationId.get(i));	
					 GaodeService.this.getContentResolver().insert(DataBaseContants.CONTENT_URI_GPSSYNC,runningItem);
				 }
			 }
			 mDataBaseUtil.deleteOperation(mGps.getStarttime(), conversTime());
			 mDataBaseUtil.deletePoint(mGps.getStarttime(), conversTime());
		 }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if("ACTION_STOP_GPSSPORT".equals(action)){
				mHandler.removeMessages(DELAY_NO_GPSSINGAL);
				mHandler.sendEmptyMessageDelayed(SAVE_GPS_INFO, 898*1000);
			}else if("ACTION_BEGIN_GPSSPORT".equals(action)){
				initAllData();
				locationTime=System.currentTimeMillis();
				mHandler.sendEmptyMessageDelayed(DELAY_NO_GPSSINGAL, 10*1000);
			}else if("ACTION_CONTINUE_GPSSPORT".equals(action)){
				locationTime=System.currentTimeMillis();
				mHandler.removeMessages(SAVE_GPS_INFO);
				mHandler.sendEmptyMessageDelayed(DELAY_NO_GPSSINGAL, 10*1000);
			}else if("ACTION_COMPELE_GPSSPORT".equals(action)){
				initAllData();
				mHandler.removeMessages(SAVE_GPS_INFO);
				if(handlerList.size()>0){
					if(handlerList.size()>5){
						mDataBaseUtil.inserPoint(filterPoint(handlerList));
					}else{
						mDataBaseUtil.inserPoint(handlerList);
					}
					mDataBaseUtil.deleteTempPoint(0, conversTime());
					handlerList.clear();
				}
			}else if("com.zhuoyou.gaode.service.hello".equals(action)){
				Intent mAliveIntent = new Intent("com.zhuoyou.gaode.activity.hi");
				sendBroadcast(mAliveIntent);
			}
		}

		private void initAllData() {
			mSumDis = 0;
			hadRunStep = 0;
			mRunStep =0;
			initRunStep = -1;
			sumRunDis = 0;
			upPoint = null;
		}
	};
	
	 private void setUpMap() {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        //设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
    }
	
	private double calSumDistance(GuidePointModel point) {
		double pieRunDis=0;
		if(upPoint == null){
			upPoint = point; 
			return 0;
			}
			LatLng upLatLng = new LatLng(upPoint.getLatitude(), upPoint.getLongitude());
			LatLng nowLatLng = new LatLng(point.getLatitude(), point.getLongitude());
			pieRunDis = AMapUtils.calculateLineDistance(upLatLng,nowLatLng);
			if(pieRunDis>2){
				sumRunDis = sumRunDis + pieRunDis;
				upPoint = point;
				mSumDis = sumRunDis +baseDistance;
				notifyDistance();
			}
			
			long gpsid = sharepreference.getLong("gps_sport_id", -1);
			if(gpsid!=-1){
				GpsUtil.updateSport(GaodeService.this, gpsid, startAddress, endAddress, null,null, ((float)this.mSumDis), null, null);
			}
			return pieRunDis;
	}
	
	class StepObserver implements IStepListener{
		
		@Override
		public void onStepCount(int stepCount) {
			if(initRunStep <= 0 && stepCount >= mRunStep ){	//此处为初始化step的地方
				initRunStep = stepCount - mRunStep;
			}
			
			if(is_running){		//正在运行
				mRunStep = stepCount - initRunStep;
				hadRunStep = mRunStep + baseStep;
				notifyStep();
			}else{
				initRunStep = stepCount - mRunStep;
			}
		}

		@Override
		public void onStateChanged(int newState) {
			
		}

		@Override
		public void onHadRunStep(int hadRunStep) {
			long gpsid = sharepreference.getLong("gps_sport_id", -1);
			if(gpsid!=-1){
				GpsUtil.updateSport(GaodeService.this, gpsid, startAddress, endAddress, null,null,null,hadRunStep, null);
			}
		}
	}
	
	private void notifyStep(){
		long gpsid = sharepreference.getLong("gps_sport_id", -1);
		if(gpsid!=-1){
			GpsSportDataModel mGpsInfo= mDataBaseUtil.selectGpsInfoForID(gpsid);
			if(hadRunStep < mGpsInfo.getSteps()){
				hadRunStep =  mGpsInfo.getSteps();
				baseStep = hadRunStep - mRunStep;
			}
		}
		StepWatcher.getInstance().notifyHadRunStep(hadRunStep);
	}
	
	private void notifyDistance(){
		long gpsid = sharepreference.getLong("gps_sport_id", -1);
		if(gpsid!=-1){
			GpsSportDataModel mGpsInfo= mDataBaseUtil.selectGpsInfoForID(gpsid);
			if(mSumDis < mGpsInfo.getSteps()){
				mSumDis =  mGpsInfo.getSteps();
				baseDistance = mSumDis - sumRunDis;
			}
		}
		MonitorWatcher.getInstance().notifySumDistance(mSumDis);
	}
}
