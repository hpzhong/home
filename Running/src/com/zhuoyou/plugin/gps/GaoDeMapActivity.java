package com.zhuoyou.plugin.gps;

import java.io.File;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapScreenShotListener;
import com.amap.api.maps.AMap.OnMapTouchListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.mcube.lib.ped.PedBackgroundService;
import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.database.DataBaseUtil;
import com.zhuoyou.plugin.gps.ilistener.GPSSignalListener;
import com.zhuoyou.plugin.gps.ilistener.IGPSPointListener;
import com.zhuoyou.plugin.gps.ilistener.IStepListener;
import com.zhuoyou.plugin.gps.ilistener.MonitorWatcher;
import com.zhuoyou.plugin.gps.ilistener.SignalWatcher;
import com.zhuoyou.plugin.gps.ilistener.StepWatcher;
import com.zhuoyou.plugin.running.PersonalConfig;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningItem;
import com.zhuoyou.plugin.running.Tools;

/**
 * 高德运动地图轨迹，实时显示Activity <br>
 * 注释：map_activity_state 存入 sharePrefens 的值，用来记录当前的状态
 * 其中 0 代表 还未开始 或 完成 运动；2代表 开始运动 ；3代表 暂停运动 ; 4 代表继续开始运动
 */


public class GaoDeMapActivity extends Activity implements OnMarkerClickListener,OnCameraChangeListener,SensorEventListener,
	OnMapTouchListener,View.OnClickListener,OnMapScreenShotListener{
	private static final String tag = "GaoDeMapActivity";
	
	public static final String NEVER_SHOW_LOG = "never_show_exitDialog";
	private static final int LOCK_GPS_OPERATION = 5;
	private MapView mapView;
    private AMap aMap;
    private SharedPreferences sharepreference;
    private Editor editor;
	private Polyline polyline;
	private ArrayList<GuidePointModel> currentList;
	private String ACTION_POINT_LIST="ACTION_POINT_LIST";
	private String ACTION_TIME_MANAGE="ACTION_TIME_MANAGE";
	private GuidePointModel mGuidePoint;
	private SensorManager sensorManager = null;
    private Sensor gyroSensor = null;
    private Marker mMark;
    private Marker beiginMark,endMark;
    private long current_direction=0;
    private boolean is_first_dire=false;
    private float current_angle=0;
    public static boolean is_line=false;
    public static GuidePointModel staticGuide;
	private AlarmManager mAlarm;
	private int begin_point_state=0;
	private PendingIntent pendingIntent;
    private DataBaseUtil mDataBaseUtil;
    private java.text.SimpleDateFormat formatter;
    private Dialog loadDialog;
    private Dialog hanldmapDataDialog;  //完成GPS运动时打出的Dialog
    private Dialog cancelmapDataDialog;  //暂停GPS运动时打出的Dialog
	private WindowManager windowManager;    
	private Display display;
	private Resources res;
	public ServiceUtil mServiceUtil;
	public boolean isCompleteGps=false;
	private static Format intformat = new DecimalFormat("00");
	private static Format decFormat = new DecimalFormat("#0.00");
	private boolean is_china=true;
	private String no_address;
	private String start_address;
	private String end_address;
	private String current_address;
	
	public static final int BEGIN_MARK=1;
	public static final int END_MARK=2;
	public static final int CANCEL_MARK=3;
	public static final int CURRENT_MARK=4;
	private final static int GET_MAP_SCREEN = 2000;
	private final static int GET_DATA_SCREEN =2001;
	
	/** Program UI */
	private LinearLayout mLayoutDetailData;			// 详细信息的布局
	private RelativeLayout mLayoutOpeartion;		// 操作(按钮) 布局
	private TextView mTVDStep;						// 步数(详细)
	private TextView mTVDTimer;						// 计时(详细)
	private TextView mTVDDistance;					// 运动距离(详细)
	private TextView mTVDDisSpeed;					// 运动即时速度(详细)
	private TextView mTVDCalories;					// 消耗卡路里(详细)
	private TextView mGpsSignal;					// GPS信号强度
	private TextView mGPSSignalLevel;				// GPS信号强度指示
	private LinearLayout mLayoutShortData;			// 简短 信息的布局
	private TextView mTVSStep;						// 步数(简短)
	private TextView mTVSTimer;						// 计时(简短)
	private TextView mTVSDistance;					// 运动距离(简短)
	private TextView mTVCountdown;					// 倒计时
	private LinearLayout mLayoutShutdown;			// 倒计时布局
	private LinearLayout mLayoutSupStart;			//
	private Button mBtnStart;						//
	private Button mBtnSupStart;					//
	private Button mBtnFinish;						//
	private SlideLayout mLayoutSlide;
	private FrameLayout mLayoutLock;
	private ImageView mBtnZoom; //
	private long startTime;	
	private UiSettings mUiSettings;   		//地图显示控制
	
	/** calculate all data*/
	private boolean isZoomall=true;			//该标记判断地图全屏还是半屏
	private int sumStep;					//运动的总步数
	private int calories;   				//运动消耗卡路里
	private int durationTime;				//运动的总时间 单位：秒
	private String fileName;
	private boolean isViewOnTop;			//该页面是否显示在最顶层
	private PointObserver mPointOberver;	//GPS 观察者
	private StepObserver mStepObserver;		// 步数 观察者
	private GPSSignalObserver mSignalObserver;
	private double sumRunDis;				// 运动的总距离
	private double aveSpeed;				// 运动的总距离的平局速度
	private GuidePointModel upPoint;		//上一个点；
	private int mTaskState=0;               //另起线程状态 1处理数据库数据 2处理暂停键时的按钮 3处理结束键按钮
	private int Countdown = 15 * 60;		//倒计时
	private PersonalConfig config;
	private boolean first_zoom;
	
	private Handler mAlivegpsHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
        	 if(msg.what == 1){
        		// this is for Update TimeUI
        		 Log.i("GaoDeMapActivit1", "msg.arg1"+msg.arg1);
        		mTVDTimer.setText(formatTimer(msg.arg1));
				mTVSTimer.setText(formatTimer(msg.arg1));
        	}
		}
	};
	
	public static String formatTimer(long timer) {
		String hour = intformat.format(timer % (24 * 3600) / 3600);
		String minute = intformat.format(timer % 3600 / 60);
		String second = intformat.format(timer % 60);
		return hour + ":" + minute + ":" + second;
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_gaode);
        res = getResources();
        mapView = (MapView) findViewById(R.id.gaode_map);
        mapView.onCreate(savedInstanceState);// 必须要写
        mDataBaseUtil=new DataBaseUtil(getApplicationContext());
        mServiceUtil=new ServiceUtil(this);
        sharepreference = this.getSharedPreferences("gaode_location_info", Context.MODE_PRIVATE);
        no_address=res.getString(R.string.gps_addressunknow);
        start_address=res.getString(R.string.gps_startAddress);
        end_address=res.getString(R.string.gps_endAddress);
        current_address=res.getString(R.string.gps_currentAddress);
        windowManager = getWindowManager();    
 	    display = windowManager.getDefaultDisplay();
        mAlarm=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
        currentList = new ArrayList<GuidePointModel>();
        loadDialog = new AlertDialog.Builder(this).setTitle(res.getString(R.string.gps_loadingmMap)).setMessage(res.getString(R.string.gps_isLoadingMap)).create();
        hanldmapDataDialog = new AlertDialog.Builder(this).setTitle(res.getString(R.string.gps_fixMapData)).setMessage(res.getString(R.string.gps_isLoadingMap)).create();
        cancelmapDataDialog = new AlertDialog.Builder(this).setTitle(res.getString(R.string.gps_cancelMapData)).setMessage(res.getString(R.string.gps_ishanldingMap)).create();
        initView();
        getLanguageEnv();
        initFilter();
		initMapView();
		openGPSSettings();
		isNetuseful();
		initGuideline();
		
		mPointOberver = new PointObserver();
		MonitorWatcher.getInstance().addWatcher(mPointOberver);
		
		mSignalObserver = new GPSSignalObserver();
		SignalWatcher.getInstance().addWatcher(mSignalObserver);
		
		mStepObserver = new StepObserver();
		StepWatcher.getInstance().addWatcher(mStepObserver);
		timerThread.start();
    }
    
    private Thread timerThread = new Thread(new Runnable() {
		@Override
		public void run() {
			while(true){
				Log.i("GaoDeMapActivit1", "isViewOnTop:"+isViewOnTop);
				Log.i("GaoDeMapActivit1", "is_running:" +GaodeService.is_running);
				if(isViewOnTop && GaodeService.is_running){
					Message msg = mAlivegpsHandler.obtainMessage();
					msg.what = 1;
					msg.arg1 = (++durationTime);
					mAlivegpsHandler.sendMessage(msg);
					
					if(!unLockHandler.hasMessages(LOCK_GPS_OPERATION)){
						Message lockMsg = unLockHandler.obtainMessage();
						lockMsg.what = LOCK_GPS_OPERATION;
						unLockHandler.sendMessageDelayed(lockMsg,3*1000);
					}
				}
				
				 int operation_state=sharepreference.getInt("map_activity_state", 0);
				 if(operation_state == 3){
					 Message msg = unLockHandler.obtainMessage();
					 msg.what = 10 ;
					 msg.arg1 = Countdown--;
					 unLockHandler.sendMessage(msg);
				 }else if(Countdown != (int)15*60 ){
					 Countdown = 15 * 60;
					 Message msg = unLockHandler.obtainMessage();
					 msg.what = 9 ;
					 unLockHandler.sendMessage(msg);
				 }
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	});
    
	private void initGuideline() {
		List<GuidePointModel> mylist3 = mDataBaseUtil.selectTempPoint();
		int operation_state = sharepreference.getInt("map_activity_state", 0);
		if (operation_state > 1) {
			startTime = mDataBaseUtil
					.selectLastOperation(OperationTimeModel.BEGIN_GPS_GUIDE);
			// 防止断点，在此处加载可能画不到线的点
			mTaskState = 1;
			InitDataTask myTask = new InitDataTask();
			myTask.execute();
			List<GuidePointModel> mylist = mDataBaseUtil.selectPoint(startTime,
					conversTime());
			List<GuidePointModel> mylist_new = new ArrayList<GuidePointModel>();
			if (mylist.size() > 0) {
				mylist_new.add(mylist.get(mylist.size() - 1));
			}
			if (GaodeService.handlerList.size() > 0) {
				staticGuide = GaodeService.handlerList
						.get(GaodeService.handlerList.size() - 1);
				mylist_new.addAll(GaodeService.handlerList);
			} else {
				List<GuidePointModel> mylist2 = mDataBaseUtil.selectTempPoint(10);
				if (mylist3.size() > 0) {
					staticGuide = mylist3.get(mylist3.size() - 1);
				}
				if (mylist2.size() > 0) {
					mylist_new.addAll(mylist2);
				}
			}

			if (mylist_new.size() > 0) {
				initLine(mylist_new, false);
				currentList.add(mylist_new.get(mylist_new.size() - 1));
			} else {
				if (mylist.size() > 0) {
					currentList.add(mylist.get(mylist.size() - 1));
				}
			}
			if (staticGuide == null) {
				if (mylist.size() > 0) {
					staticGuide = mylist.get(mylist.size() - 1);
				}
			}
		}else{
			if (mylist3.size() > 0) {
				staticGuide = mylist3.get(mylist3.size() - 1);
			}
		}
		if (staticGuide != null) {
			LatLng latlng = new LatLng(staticGuide.getLatitude(),
					staticGuide.getLongitude());
			float current_zoom = sharepreference.getFloat("current_amp_zoom",
					14);
			aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,
					current_zoom));
			hanlderMarker(staticGuide, CURRENT_MARK);
			Editor edit = sharepreference.edit();
			edit.putBoolean("is_begin_point", false);
			edit.commit();
			first_zoom = false;
		} else {
			begin_point_state = 1;
			first_zoom = true;
			Editor edit = sharepreference.edit();
			edit.putBoolean("is_begin_point", true);
			edit.commit();
		}

	}
   
   /** 初始化地图上的按钮 */
   private void initMapView(){
	   if (aMap == null) {
           aMap = mapView.getMap();
           mUiSettings=aMap.getUiSettings();
           mUiSettings.setZoomControlsEnabled(false);
   		   aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
   		   aMap.setOnCameraChangeListener(this);//可视区域改变监听
   		   aMap.setOnMapTouchListener(this);
       }
   }
    
    private void initView() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    	mLayoutDetailData = (LinearLayout)findViewById(R.id.gps_layout_detaildata);
    	mLayoutOpeartion = (RelativeLayout)findViewById(R.id.gps_layout_operation);
    	mTVDStep = (TextView)findViewById(R.id.gps_tvde_step);
    	mTVDTimer = (TextView)findViewById(R.id.gps_tvde_timer);
    	mTVDDistance = (TextView)findViewById(R.id.gps_tvde_distance);
    	mTVDDisSpeed = (TextView)findViewById(R.id.gps_tvde_dipspeed);
    	mTVDCalories = (TextView)findViewById(R.id.gps_tvde_calorie);
    	mLayoutShortData = (LinearLayout)findViewById(R.id.gps_layout_shortdata);
    	mTVSStep = (TextView)findViewById(R.id.gps_tvsh_step);
    	mTVSTimer = (TextView)findViewById(R.id.gps_tvsh_timer);
    	mTVSDistance = (TextView)findViewById(R.id.gps_tvsh_travel);
    	mGpsSignal = (TextView)findViewById(R.id.gps_signal);
    	mGPSSignalLevel = (TextView)findViewById(R.id.gps_signal_level);
    	mLayoutSupStart = (LinearLayout)findViewById(R.id.gps_layout_restart);
    	mBtnStart = (Button)findViewById(R.id.gps_btn_start);
    	mBtnSupStart = (Button)findViewById(R.id.gps_btn_supstart);
    	mBtnFinish = (Button)findViewById(R.id.gps_btn_finish);
    	mLayoutSlide = (SlideLayout)findViewById(R.id.gps_slider_layout);
		mLayoutLock = (FrameLayout)findViewById(R.id.gps_layout_lock);
		mBtnZoom = (ImageView) findViewById(R.id.gps_igbtn_zoom);
		mTVCountdown = (TextView)findViewById(R.id.gps_countdown);
		mLayoutShutdown = (LinearLayout)findViewById(R.id.gps_layout_shutdown_notify);
    	initGpsSignal(sharepreference.getInt("gps_singal_state", 0));
    	findViewById(R.id.back).setOnClickListener(this);
    	mBtnStart.setOnClickListener(this);
    	mBtnSupStart.setOnClickListener(this);
    	mBtnFinish.setOnClickListener(this);
    	mBtnZoom.setOnClickListener(this);
    	mLayoutSlide.setMainHandler(unLockHandler);
    	config = Tools.getPersonalConfig();
    }
    
    private Handler unLockHandler = new Handler(){
  		@Override
  		public void handleMessage(Message msg) {
  			if(msg.what == SlideLayout.MSG_LOCK_SUCESS){
  				unLockHandler.removeMessages(LOCK_GPS_OPERATION);
  				mLayoutLock.setVisibility(View.GONE);
  				mBtnStart.setVisibility(View.GONE);
  				mLayoutSupStart.setVisibility(View.VISIBLE);
  			}else if(msg.what == LOCK_GPS_OPERATION){
  				if(mLayoutLock.getVisibility() == View.GONE && GaodeService.is_running){
  					mLayoutLock.setVisibility(View.VISIBLE);
  	  				mBtnStart.setVisibility(View.GONE);
  	  				mLayoutSupStart.setVisibility(View.GONE);
  				}
  			}else if(msg.what == 9){
  				
  				mLayoutShutdown.setVisibility(View.GONE);
  				
  			}else if(msg.what == 10){
  				if(mLayoutShutdown.getVisibility() != View.VISIBLE){
  					mLayoutShutdown.setVisibility(View.VISIBLE);
  				}
  				int time = msg.arg1;
  				int min = time / 60 ;
  				int sec = time % 60;
  				
  				mTVCountdown.setText( intformat.format(min) + res.getString(R.string.gps_minute) + intformat.format(sec)+res.getString(R.string.gps_second));
  				
  				if(msg.arg1 <= 0){
  					mLayoutShutdown.setVisibility(View.GONE);
  				}
  			}
  		}
  	};
  	
  	@Override
	public boolean onTouchEvent(MotionEvent event) {
  		if(unLockHandler.hasMessages(LOCK_GPS_OPERATION)){
  			unLockHandler.removeMessages(LOCK_GPS_OPERATION);
  		}
		return super.onTouchEvent(event);
	}
  	
    private void initGpsSignal(int gpsState){
    	switch (gpsState) {
		case 0: //搜索信号
			mGpsSignal.setText(res.getString(R.string.gps_signal_state0));
			mGPSSignalLevel.setVisibility(View.GONE);
			Toast.makeText(this, res.getString(R.string.gpsSignal_search), Toast.LENGTH_LONG).show();
			break;
		case 1: //信号强
			mGpsSignal.setText(res.getString(R.string.gps_signal_state1));
			mGPSSignalLevel.setVisibility(View.VISIBLE);
			mGPSSignalLevel.setText(res.getString(R.string.gps_signal_state2));
			break;
		case 2: //信号弱
			mGpsSignal.setText(res.getString(R.string.gps_signal_state1));
			mGPSSignalLevel.setVisibility(View.VISIBLE);
			mGPSSignalLevel.setText(res.getString(R.string.gps_signal_state3));
			Toast.makeText(this,  res.getString(R.string.gpsSignal_weak), Toast.LENGTH_LONG).show();
			break;
		default:
			break;
		}
    }
    
    //判断网络状态，并进行提示
    private boolean isNetuseful(){
    	boolean bisConnFlag=false;
    	ConnectivityManager conManager = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = conManager.getActiveNetworkInfo();
        if(network!=null){
            bisConnFlag=conManager.getActiveNetworkInfo().isAvailable();
        }
    	if(!bisConnFlag){
    		Toast.makeText(this, res.getString(R.string.open_net), Toast.LENGTH_LONG).show();
    	}
    	return bisConnFlag;
    }
    
    //注册监听
    private void initFilter(){
    	IntentFilter mGATTFilter = new IntentFilter();
        mGATTFilter.addAction("ACTION_POINT_LIST");
        mGATTFilter.addAction("ACTION_TIME_MANAGE");
        mGATTFilter.addAction("ACTION_SAVE_GPSSPORT");
        registerReceiver(mBroadcastReceiver, mGATTFilter);
    }
    
    //接收广播
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (ACTION_POINT_LIST.equals(action)) {
				
			} else if(ACTION_TIME_MANAGE.equals(action)){
					if(staticGuide!=null){
						LatLng latlng = new LatLng(staticGuide.getLatitude(), staticGuide.getLongitude());
						float current_zoom = sharepreference.getFloat("current_amp_zoom", 14);
			            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,current_zoom)); 
					}
			}else if("ACTION_SAVE_GPSSPORT".equals(action)){
				initRunData();
				mServiceUtil.isServiceRunning();
			}
		}
	};
	
	private void calGPSLocationPoint(GuidePointModel point){
		mGuidePoint = point;
		if(is_line){
			currentList.add(point);
			if(begin_point_state==1){
				hanlderMarker(currentList.get(0),BEGIN_MARK);
			}
			if(currentList.size()>1){
			initLine(currentList,false);
			currentList.clear();
			currentList.add(point);
				}
			}
		
	//触摸操作结束后开启定时器
	}
	
	//开启定时器
	public void timeManage(){	
		Intent intent = new Intent("ACTION_TIME_MANAGE");  
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 800, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarm.setRepeating(AlarmManager.RTC, System.currentTimeMillis()+3000, 3*1000, pendingIntent);
	}
	
	//该方法为在地图上画出对应点
	public void hanlderMarker(GuidePointModel mGuidePoint,int marker){
		LatLng latlng = new LatLng(mGuidePoint.getLatitude(), mGuidePoint.getLongitude());
		switch (marker) {
			case BEGIN_MARK:
					MarkerOptions markerOption1;
					if(is_china){
						markerOption1 = new MarkerOptions().anchor(0.5f, 0.5f)
				 				.position(latlng).title(start_address)
				 				.snippet(GaodeService.startAddress).icon(BitmapDescriptorFactory.fromResource(R.drawable.point_begin))
				 				.perspective(true).draggable(true);
					}else{
						markerOption1 = new MarkerOptions().anchor(0.5f, 0.5f)
				 				.position(latlng).title(start_address)
				 				.snippet(GaodeService.startAddress).icon(BitmapDescriptorFactory.fromResource(R.drawable.point_begin_en))
				 				.perspective(true).draggable(true);
					}
			 		beiginMark=aMap.addMarker(markerOption1);
			 		begin_point_state=2;
				
				break;
			case END_MARK:
				String maddress2 = GaodeService.endAddress;
				MarkerOptions markerOption2;
				if(is_china){
					markerOption2 = new MarkerOptions().anchor(0.5f, 0.5f)
			 				.position(latlng).title(end_address)
			 				.snippet(maddress2).icon(BitmapDescriptorFactory.fromResource(R.drawable.point_complete))
			 				.perspective(true).draggable(true);
				}else{
					markerOption2 = new MarkerOptions().anchor(0.5f, 0.5f)
			 				.position(latlng).title(end_address)
			 				.snippet(maddress2).icon(BitmapDescriptorFactory.fromResource(R.drawable.point_complete_en))
			 				.perspective(true).draggable(true);
				}
		 		endMark=aMap.addMarker(markerOption2);
				break;
			case CANCEL_MARK:
				MarkerOptions markerOption3 = new MarkerOptions().anchor(0.5f, 0.5f)
		 				.position(latlng).title(current_address)
		 				.icon(BitmapDescriptorFactory.fromResource(R.drawable.point_cancel))
		 				.perspective(true).draggable(true);
		 		Marker cancelMark=aMap.addMarker(markerOption3);
				break;
			case CURRENT_MARK:
				if(mMark!=null){
					mMark.remove();
				}
		 		MarkerOptions markerOption4 = new MarkerOptions().anchor(0.5f, 0.5f)
		 				.position(latlng).title(current_address)
		 				.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker))
		 				.perspective(true).draggable(true);
		 		mMark=aMap.addMarker(markerOption4);
		 		mMark.setRotateAngle(360-current_angle); 
				break;
			default:
				break;
		}
	}
	
	//该方法为在给出的一系列点上划线(),listPoint为给定的点，isVirtual为true画虚线，false画实线
    public void initLine(List<GuidePointModel> listPoint,boolean isVirtual){
    	List<LatLng> initList=new ArrayList<LatLng>();
    	List<LatLng> tempList=new ArrayList<LatLng>();
    	for(int i=0;i<listPoint.size();i++){
    		LatLng ponitLatlng=new LatLng(listPoint.get(i).getLatitude(), listPoint.get(i).getLongitude());
    		initList.add(ponitLatlng);
    	}
		if(initList.size()>1 && initList.size()<10000){
				polyline = aMap.addPolyline((new PolylineOptions())
						.addAll(initList)
						.width(14)
						.setDottedLine(isVirtual)
						.color(Color.RED));
		}else if(initList.size()>10000){
			int mp = initList.size()/10000;
			int mod= initList.size() % 10000;
			int j=0;
			for(int i=0;i<initList.size();i++){
				tempList.add(initList.get(i));
				if(j<mp){
					if(tempList.size()==10000){
						polyline = aMap.addPolyline((new PolylineOptions())
								.addAll(tempList)
								.width(14)
								.setDottedLine(isVirtual)
								.color(Color.RED));
						tempList.clear();
						tempList.add(initList.get(i));
						j=j+1;
					}
				}else{
					if(mod+mp<10000){
						if(initList.size()==mod+mp){
						polyline = aMap.addPolyline((new PolylineOptions())
								.addAll(tempList)
								.width(14)
								.setDottedLine(isVirtual)
								.color(Color.RED));
						initList.clear();
						}
					}else{
						if(tempList.size()==10000){
							polyline = aMap.addPolyline((new PolylineOptions())
									.addAll(tempList)
									.width(14)
									.setDottedLine(isVirtual)
									.color(Color.RED));
							tempList.clear();
						}
					}
				} 	
			}
		  }
	}
    
	@Override
	public boolean onMarkerClick(Marker marker) {
		if(marker.getTitle().trim().equals(current_address.trim())){
			return true;
		}else{
			if(marker.isInfoWindowShown()){
				marker.hideInfoWindow();
			}else{
				marker.showInfoWindow();
			}
			return false;
		}
	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
		
	}
	
	/** 该方法主要是获得放大和缩小后当前的缩放级别  */
	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		float mzoom = aMap.getCameraPosition().zoom;
        editor = sharepreference.edit();
		editor.putFloat("current_amp_zoom",mzoom);		
		editor.commit();
	}

	/** 通过传感器获得手机当前的方向，正北方向为0 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		if(is_first_dire){
			current_direction=System.currentTimeMillis();
			is_first_dire=false;
		}else{
			if(System.currentTimeMillis()-current_direction>2000){
				current_direction=System.currentTimeMillis();
				current_angle=event.values[0];
				if(mMark!=null){
					mMark.setRotateAngle(360-event.values[0]); 
				}
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.i("GaoDeMapActivity", "onResume");
		isViewOnTop = true;
		mapView.onResume();
		sensorManager.registerListener(this, gyroSensor,SensorManager.SENSOR_DELAY_NORMAL);
		is_first_dire=true;
		calRunData();
		mServiceUtil.isServiceRunning();
		int operation_state=sharepreference.getInt("map_activity_state", 0);
		if(operation_state==2||operation_state == 4){
			GaodeService.is_running = true;
			GaoDeMapActivity.is_line=true;
		}else{
			GaodeService.is_running = false;
			GaoDeMapActivity.is_line=true;
		}
		Log.i("GaoDeMapActivity", "operation_state:"+operation_state);
		if(operation_state == 0 ){
			initRunData();
		}else if(operation_state==2){
			beginViewState();
		}else if(operation_state==3){
			stopViewState();
		}else if(operation_state==4){
			continueViewState();
		}
	}

	private void initRunData(){
		sumStep = 0 ;
		durationTime = 0 ;
		sumRunDis = 0 ;
		aveSpeed = 0 ;
		mTVDStep.setText("0");
		mTVDTimer.setText("00:00:00");
		mTVDDistance.setText("0.00");
		mTVDDisSpeed.setText("0.00");
		mTVDCalories.setText("0");
		mTVSStep.setText("0");
		mTVSTimer.setText("00:00:00");
		mTVSDistance.setText("0.00");
		mBtnStart.setVisibility(View.VISIBLE);
		mLayoutSupStart.setVisibility(View.GONE);
		mLayoutLock.setVisibility(View.GONE);
	}
	
	private void calRunData() {
		int hadTime = sharepreference.getInt("durationTime", 0);
		long startT = sharepreference.getLong("onPause_time",System.currentTimeMillis());
		long currnT = System.currentTimeMillis();
		int dru = (int) ((currnT - startT) / 1000);
		int operation_state=sharepreference.getInt("map_activity_state", 0);
		durationTime = hadTime + dru;
		
		if(operation_state != 0){
			long gpsid = sharepreference.getLong("gps_sport_id", -1);
			if(gpsid!=-1){
				GpsSportDataModel mGpsInfo= mDataBaseUtil.selectGpsInfoForID(gpsid);
				
				if( sumRunDis< mGpsInfo.getTotalDistance()){
					sumRunDis = mGpsInfo.getTotalDistance();
				}
				if( sumStep< mGpsInfo.getSteps()){
					sumStep =  mGpsInfo.getSteps();
				}
			}
		}
		
		int meter = Tools.calcDistance(sumStep, config.getHeight());
		calories = Tools.calcCalories(meter, config.getWeightNum());
		
		if(operation_state == 3){
			int down = sharepreference.getInt("countdown", 15 * 60);
			long curr = sharepreference.getLong("countdown_init", System.currentTimeMillis());
			Countdown = down - (int)((System.currentTimeMillis() - curr)/1000);
			if(Countdown<=0){
				Countdown = 15 * 60;
			}
			int min = Countdown/60;
			int sec = Countdown%60;
			mTVCountdown.setText( intformat.format(min) + res.getString(R.string.gps_minute) + intformat.format(sec)+res.getString(R.string.gps_second));
		}
		if(durationTime != 0){
			aveSpeed = sumRunDis/(double)durationTime;
		}else{
			aveSpeed = 0;
		}
		
		mTVDDisSpeed.setText(decFormat.format(aveSpeed*3.6));
		
		mTVDStep.setText("" + sumStep);
		mTVSStep.setText("" + sumStep);
		mTVDTimer.setText(formatTimer(durationTime));
		mTVSTimer.setText(formatTimer(durationTime));
		mTVDDistance.setText(decFormat.format(sumRunDis/1000));
		mTVSDistance.setText(decFormat.format(sumRunDis/1000));
		mTVDCalories.setText(""+calories);
	}
	
	//读取数据库的信息,并作出处理
	public void initDatabaseline(){
		List<GuidePointModel> sublistPoint;
		List<GuidePointModel> handlistPoint;
		GuidePointModel beginPoint;
		List<GuidePointModel> smallistPoint =new ArrayList<GuidePointModel>();
		List<GuidePointModel> smallistPoint2 =new ArrayList<GuidePointModel>();
		if(startTime!=0){
			GuidePointModel firstPoint=mDataBaseUtil.selectFirstPoint(startTime, conversTime());
			if(firstPoint.getLongitude()!=0){
				sublistPoint=mDataBaseUtil.selectPoint(firstPoint.getTime(), conversTime(),0);//此数据查询的为状态大于0的点
				if(sublistPoint.size()>0){
					if(sublistPoint.size()==1){
						smallistPoint=mDataBaseUtil.selectPoint(sublistPoint.get(0).getTime(), conversTime());
						handlistPoint=GpsUtil.handlePoint(smallistPoint);
						handlistPoint.add(smallistPoint.get(smallistPoint.size()-1));
						beginPoint=handlistPoint.get(0);
						beginPoint.setTime(smallistPoint.get(0).getTime());
						hanlderMarker(beginPoint,BEGIN_MARK);
						initLine(handlistPoint,false);
					}else{
						for(int i=0;i<sublistPoint.size();i++){
							if(i>0){
								if(sublistPoint.get(i).getPointState()==3 || sublistPoint.get(i).getPointState()==5 || sublistPoint.get(i).getPointState()==6){
									smallistPoint=mDataBaseUtil.selectPoint(sublistPoint.get(i-1).getTime(), sublistPoint.get(i).getTime());
									if(smallistPoint.size()==2){
										initLine(smallistPoint,false);
									}else if(smallistPoint.size()>2){
										smallistPoint=mDataBaseUtil.selectPoint(sublistPoint.get(i-1).getTime(), sublistPoint.get(i).getTime()-1);
										handlistPoint=GpsUtil.handlePoint(smallistPoint);
										initLine(handlistPoint,false);
										if(i==sublistPoint.size()-1){
											smallistPoint2=mDataBaseUtil.selectPoint(sublistPoint.get(i).getTime(), conversTime());
										}else{
											smallistPoint2=mDataBaseUtil.selectPoint(sublistPoint.get(i).getTime(), sublistPoint.get(i+1).getTime()-1);
										}
										List<GuidePointModel> currentlistPoint =new ArrayList<GuidePointModel>();
										currentlistPoint.add(handlistPoint.get(handlistPoint.size()-1));
										currentlistPoint.add(GpsUtil.handlePoint(smallistPoint2).get(0));
										initLine(currentlistPoint,false);
									}
								}
							}
							if(i==1){
								smallistPoint=mDataBaseUtil.selectPoint(sublistPoint.get(0).getTime(), sublistPoint.get(1).getTime());
								handlistPoint=GpsUtil.handlePoint(smallistPoint);
								beginPoint=handlistPoint.get(0);
								beginPoint.setTime(smallistPoint.get(0).getTime());
								hanlderMarker(beginPoint,BEGIN_MARK);
							}
							if(i==sublistPoint.size()-1){
								smallistPoint=mDataBaseUtil.selectPoint(sublistPoint.get(i).getTime(), conversTime());
								handlistPoint=GpsUtil.handlePoint(smallistPoint);
								handlistPoint.add(smallistPoint.get(smallistPoint.size()-1));
								if(handlistPoint.size()>1){
									initLine(handlistPoint,false);
								}
							}
						}
					}
				}
			}
		}
	}
	
	 
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
		Log.i("GaoDeMapActivity", "onSaveInstanceState");
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.i("GaoDeMapActivity", "onRestoreInstanceState");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
		sensorManager.unregisterListener(this); // 解除监听器注册 
		if(pendingIntent !=null){
			mAlarm.cancel(pendingIntent);
		}
		saveRunData();
		// 正在结束运动的地方取消监听事件
		if(isFinishing()){
			MonitorWatcher.getInstance().removeWatcher(mPointOberver);
			SignalWatcher.getInstance().removeWatcher(mSignalObserver);
			StepWatcher.getInstance().removeWatcher(mStepObserver);
			unregisterReceiver(mBroadcastReceiver);
		}
		
	}
	/** 当页面不存在的时候保存数据 */
	private void saveRunData(){
		isViewOnTop = false;
		Editor edit = sharepreference.edit();
		int operation_state=sharepreference.getInt("map_activity_state", 0);
		if(operation_state != 0){
			edit.putInt("durationTime", durationTime);
			if(GaodeService.is_running){
				edit.putLong("onPause_time", System.currentTimeMillis());
			}else{
				edit.remove("onPause_time");
			}
		}else{
			edit.putInt("durationTime", durationTime);
			edit.remove("onPause_time");
		}
		
		if(operation_state == 3){
			edit.putInt("countdown", Countdown);
			edit.putLong("countdown_init", System.currentTimeMillis());
		}else{
			edit.remove("countdown");
			edit.remove("countdown_init");
		}
		edit.commit();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		int operation_state=sharepreference.getInt("map_activity_state", 0);
		
		if(operation_state<2){
//			Boolean isClose=GaoDeMapActivity.this.stopService(new Intent(GaoDeMapActivity.this,GaodeService.class));
			GaoDeMapActivity.this.stopService(new Intent(GaoDeMapActivity.this.getApplicationContext(),PedBackgroundService.class));
		}
		mServiceUtil.uninitFilter();
		mapView.onDestroy();
	}
	
	
	//判断gps是否开启，如果没开启跑的指定页面，提醒开启
	private void openGPSSettings() {  
        LocationManager alm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);  
        if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {  
            //Toast.makeText(this, res.getString(R.string.gps_GPSisNormal), Toast.LENGTH_SHORT).show();
            return;  
        }else{
        	dialogShow(1);	// 改功能需要打开GPS
        }       
    }
	
	//判断手机有没有GPS模块
	public boolean hasGPSDevice(Context context){
        final LocationManager mgr = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        if ( mgr == null )
            return false;
        final List<String> providers = mgr.getAllProviders();
        if ( providers == null )
            return false;
        return providers.contains(LocationManager.GPS_PROVIDER);
    }
	
	//判断当前gps的状态
	private boolean isGpsuseful() {  
        LocationManager alm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsIsUse=alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
        return gpsIsUse;
    }


	 //触摸监听，操作结束时开启定时器
	@Override
	public void onTouch(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:// 屏幕按下  
			if(pendingIntent !=null){
				mAlarm.cancel(pendingIntent);
			}
		break;
		case MotionEvent.ACTION_UP:// 按下抬起
			timeManage();
		break;
		}
      }

	//点击事件处理
	@Override
	public void onClick(View v) {
		if(unLockHandler.hasMessages(LOCK_GPS_OPERATION)){
  			unLockHandler.removeMessages(LOCK_GPS_OPERATION);
  		}
		
		switch (v.getId()) {
		case R.id.back:
        	boolean neverShowLog = sharepreference.getBoolean(NEVER_SHOW_LOG, false);			
        	if(neverShowLog == false && GaodeService.is_running){
				dialogShow(0);
			}else{
				this.finish();
			}
			break;
		case R.id.gps_btn_start:
			timeManage();
			startRun();
			break;
		case R.id.gps_btn_supstart:
			int operation_state=sharepreference.getInt("map_activity_state", 0);
			if(GaodeService.is_running){
				if(pendingIntent !=null){
					mAlarm.cancel(pendingIntent);
				}
				stopRun();
			}else{
				timeManage();
				reStartRun();
			}			
			break;
		case R.id.gps_btn_finish:
			if(pendingIntent !=null){
				mAlarm.cancel(pendingIntent);
			}
			finishRun();
			break;
		case R.id.gps_igbtn_zoom:
			zoomSwitch();
			break;
		}
	}
	private void removeData(){
		Editor edit = sharepreference.edit();
		edit.putInt("save_service_step",0);
		edit.putFloat("save_service_distance",0f);
		edit.commit();
	}
	
	//完成gps运动时，进行相应的操作
	private void compeleGpsSport(){
		isCompleteGps=true;
		GaodeService.point_state=0;
		is_line=false;
		GaodeService.is_running=false;
		removeData();
		if(!TextUtils.isEmpty(GaodeService.tempDataModel.getTempImageUrl())){
			File mFile=new File(GaodeService.tempDataModel.getTempImageUrl());
			if(mFile.exists()){
				mFile.delete();
			}
		}
		Intent continueIntent = new Intent("ACTION_COMPELE_GPSSPORT");
		sendBroadcast(continueIntent);
		handlerMap();
		OperationTimeModel mOperation2 = new OperationTimeModel();
		mOperation2.setOperatId(Tools.getPKL());
		mOperation2.setOperationtime(conversTime());
		mOperation2.setOperationSystime(System.currentTimeMillis());
		mOperation2.setOperationState(OperationTimeModel.COMPLETE_GPS_GUIDE);
		mOperation2.setSyncState(0);
		mDataBaseUtil.insertOperation(mOperation2);
	}
	
	private void handlerMap(){
		if(mGuidePoint!=null){
			hanlderMarker(mGuidePoint,END_MARK);
		}
		if(mMark!=null){
			mMark.remove();
		}
		mHandler.sendEmptyMessageDelayed(GET_DATA_SCREEN, 600);
	}
	
	public void getMapScreenShot() {
		aMap.getMapScreenShot(this);
	}
	
	@Override
	public void onMapScreenShot(Bitmap bitmap) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		fileName = format.format(new Date()) + ".jpg";
		savegpsInfo();
		if(null == bitmap){
			return;
		}
		Tools.saveGpsBitmapToFile(bitmap, fileName);
		bitmap.recycle();
		bitmap = null;
	}
	
	public void LoadedMaptoview() {
		long endTime=conversTime();
		double []points = mDataBaseUtil.findGpsBound(startTime, endTime);
		if(points!=null){
			LatLng startGuide = new LatLng(points[0],points[1]);
			LatLng endGuide = new LatLng(points[2],points[3]);
	        LatLngBounds bounds = new LatLngBounds.Builder().include(startGuide).include(endGuide).build();
	        // 移动地图，所有marker自适应显示。LatLngBounds与地图边缘50像素的填充区域
	        aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
		}
		mHandler.sendEmptyMessageDelayed(GET_MAP_SCREEN, 1000);
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case GET_MAP_SCREEN:
				getMapScreenShot();
				break;
			case GET_DATA_SCREEN:
				LoadedMaptoview();
				break;
			}
			super.handleMessage(msg);
		};
	};
	
	//删除gps运动时，进行相应的操作
	private void delteGpsSport(){
		deletefromDatabase();
		editor = sharepreference.edit();
		editor.putInt("map_activity_state",0 );
		editor.putBoolean("is_begin_point", true);
		if(sharepreference.contains("is_have_beginaddr")){
			editor.remove("is_have_beginaddr");
		}
		if(sharepreference.contains("gps_sport_id")){
			editor.remove("gps_sport_id");
		}
		editor.commit();
		is_line=false;
		GaodeService.is_running=false;
		initViewState();
		aMap.clear();
		removeData();
	}
	
	//删除gps运动时，同步删除数据库里面的数据
	 private void deletefromDatabase(){
		 List<Integer> listGpsId = mDataBaseUtil.selectPointID(startTime, conversTime(), 0);
		 List<Integer> listOperationId = mDataBaseUtil.selectOperationId(startTime, conversTime(), 0);
		 if(listGpsId.size()>0){
			 for(int i=0;i<listGpsId.size();i++){
				 ContentValues runningItem = new ContentValues();
				 runningItem.put(DataBaseContants.GPS_TABLE, DataBaseContants.TABLE_POINT_NAME);
				 runningItem.put(DataBaseContants.GPS_DELETE, listGpsId.get(i));	
				 GaoDeMapActivity.this.getContentResolver().insert(DataBaseContants.CONTENT_URI_GPSSYNC,runningItem);
			 }
		 }
		 if(listOperationId.size()>0){
			 for(int i=0;i<listOperationId.size();i++){
				 ContentValues runningItem = new ContentValues();
				 runningItem.put(DataBaseContants.GPS_TABLE, DataBaseContants.TABLE_OPERATION_NAME);
				 runningItem.put(DataBaseContants.GPS_DELETE, listOperationId.get(i));	
				 GaoDeMapActivity.this.getContentResolver().insert(DataBaseContants.CONTENT_URI_GPSSYNC,runningItem);
			 }
		 }
		 if(sharepreference.contains("gps_sport_id")){
			 mDataBaseUtil.deleteGpsFromID(sharepreference.getLong("gps_sport_id", -1));
			}
		 mDataBaseUtil.deleteOperation(startTime, conversTime());
		 mDataBaseUtil.deletePoint(startTime, conversTime());
	 }
	 
	//保存gps运动信息到数据库
	private void savegpsInfo(){
		long startSysTime = mDataBaseUtil.selectOperSysTime(OperationTimeModel.BEGIN_GPS_GUIDE);
		//float speed=(totalDistance/durationTime)*1000;
			GaodeService.gpsSportInfo.setGpsId(sharepreference.getLong("gps_sport_id", 0));
			GaodeService.gpsSportInfo.setStarttime(startTime);
			GaodeService.gpsSportInfo.setEndtime(conversTime());
			GaodeService.gpsSportInfo.setCalorie(calories);
			GaodeService.gpsSportInfo.setStarSysttime(startSysTime);
			GaodeService.gpsSportInfo.setEndSystime(System.currentTimeMillis());
			GaodeService.gpsSportInfo.setDurationtime(durationTime);
			GaodeService.gpsSportInfo.setTotalDistance(sumRunDis);
			GaodeService.gpsSportInfo.setAvespeed(aveSpeed);
			GaodeService.gpsSportInfo.setSteps(sumStep);
			String maddress = sharepreference.getString("is_have_beginaddr", no_address);
			if(!TextUtils.isEmpty(maddress)){
				GaodeService.gpsSportInfo.setStartAddress(maddress);
			}else{
				GaodeService.gpsSportInfo.setStartAddress(no_address);
			}
			if(!TextUtils.isEmpty(GaodeService.endAddress)){
				GaodeService.gpsSportInfo.setEndAddress(GaodeService.endAddress);
			}else{
				GaodeService.gpsSportInfo.setEndAddress(no_address);
			}
			GaodeService.gpsSportInfo.setSyncState(0);
			if(isCompleteGps){
				 editor = sharepreference.edit();
				 editor.putInt("map_activity_state",0 );
				 editor.putInt("gps_singal_state", 0);
				 editor.putBoolean("is_begin_point", true);
				 if(sharepreference.contains("is_have_beginaddr")){
						editor.remove("is_have_beginaddr");
					}
				 if(sharepreference.contains("gps_sport_id")){
						editor.remove("gps_sport_id");
					}
				 editor.commit();
			mDataBaseUtil.updateGpsInfo(GaodeService.gpsSportInfo);
			}
			insertDataBaseSportType(GaodeService.gpsSportInfo);
	}
	
	public String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();
		} else {
			return null;
		}
		return sdDir.toString();
	}
	
	//插入gps运动数据到data表中，方便主界面显示数据
	private void insertDataBaseSportType(GpsSportDataModel gpsSportInfo) {
		String beginAddress=null;
		String endAddress=null;
		if(!TextUtils.isEmpty(gpsSportInfo.getStartAddress())){
			beginAddress = gpsSportInfo.getStartAddress();
		}else{
			beginAddress = no_address;
		}
		
		if(!TextUtils.isEmpty(GaodeService.endAddress)){
			endAddress = GaodeService.endAddress;
		}else{
			endAddress = no_address;
		}
		String gpsfilePath = getSDPath() + "/Running/gps/"+fileName;
		String mTime=Long.toString(gpsSportInfo.getStarttime());
		String curr_date = mTime.substring(0, 4)+"-"+mTime.substring(4, 6)+"-"+mTime.substring(6,8);
		String startTime=mTime.substring(8, 10)+":"+mTime.substring(10, 12);
		String mduration=Double.toString(Math.rint(durationTime/60));
		String endTime=hanlderEndTime(gpsSportInfo);
		if(isCompleteGps){
			RunningItem runningItem = new RunningItem();
			runningItem.setDate(curr_date);
			runningItem.setStartTime(startTime);
			runningItem.setDuration(mduration);
			runningItem.setEndTime(endTime);
			runningItem.setCalories(calories);
			runningItem.setSteps(sumStep);
			runningItem.setKilometer((int)sumRunDis);
			runningItem.setmType(5);
			runningItem.setmWeight(beginAddress);
			runningItem.setmBmi(endAddress);
			runningItem.setmImgUri(gpsfilePath);
			runningItem.setmExplain(Long.toString(gpsSportInfo.getGpsId()));
			Intent gpsIntent = new Intent("ACTION_GPS_INFO");
			gpsIntent.putExtra("gps_info", runningItem);
			sendBroadcast(gpsIntent);
//			GaoDeMapActivity.this.getContentResolver().insert(DataBaseContants.CONTENT_URI,runningItem);
			this.finish();
		}else{
			if(endMark!=null){
				endMark.remove();
			}
			GaodeService.tempDataModel.setTempId(Tools.getPKL());
			GaodeService.tempDataModel.setTempDate(curr_date);
			GaodeService.tempDataModel.setTempStaTime(startTime);
			GaodeService.tempDataModel.setTempDuration(mduration);
			GaodeService.tempDataModel.setTempEndTime(endTime);
			GaodeService.tempDataModel.setTempCalories(calories);
			GaodeService.tempDataModel.setTempStep(sumStep);
			GaodeService.tempDataModel.setTempDistance(sumRunDis);
			GaodeService.tempDataModel.setTempType(5);
			GaodeService.tempDataModel.setTempStatistics(0);
			GaodeService.tempDataModel.setTempState(0);
			GaodeService.tempDataModel.setTempStaAddress(beginAddress);
			GaodeService.tempDataModel.setTempEndAddress(endAddress);
			GaodeService.tempDataModel.setTempImageUrl(gpsfilePath);
			GaodeService.tempDataModel.setTempGpsId(gpsSportInfo.getGpsId());
		}
	}
	
	private String hanlderEndTime(GpsSportDataModel gpsSportInfo){
		String endTime=null;
		long durSys=gpsSportInfo.getEndSystime()/1000-gpsSportInfo.getStarSysttime()/1000;
		String mTime=Long.toString(gpsSportInfo.getStarttime());
		String mTime2=Long.toString(gpsSportInfo.getEndtime());
		long currentMin=Integer.parseInt(mTime.substring(8, 10))*3600+Integer.parseInt(mTime.substring(10, 12))*60+Integer.parseInt(mTime.substring(12, 14));
		long disMin=24*60*60-currentMin;
		if(durSys<disMin){
			endTime=mTime2.substring(8, 10)+":"+mTime2.substring(10, 12);
		}else{
			long day=(durSys-disMin)/(24*3600)+1;
			endTime=String.valueOf(Integer.parseInt(mTime2.substring(8, 10))+day*24)+":"+mTime2.substring(10, 12);
		}
		return endTime;
	}
	
	private void getLanguageEnv() {  
	       Locale mlocale = Locale.getDefault();  
	       String language = mlocale.getLanguage();  
	       //Log.d("lan","language--------------"+language);
	       if ("zh".equals(language)) {  
	           is_china = true;
	       }else{
	    	   is_china = false;  
	       } 
	   }  
	
	//时间转换，把系统时间改变成日前
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
	
	
	private void initViewState(){
		mBtnStart.setVisibility(View.VISIBLE);
		mLayoutSupStart.setVisibility(View.GONE);
	}
	
	//点击开始操作后，对应的view变化
	private void beginViewState(){
		mBtnStart.setVisibility(View.GONE);
		mLayoutSupStart.setVisibility(View.VISIBLE);
		mBtnSupStart.setText(res.getString(R.string.gps_stop));
	}
	
	//点击继续操作后，对应的view变化
	private void continueViewState(){
		mBtnStart.setVisibility(View.GONE);
		mLayoutSupStart.setVisibility(View.VISIBLE);
		mBtnSupStart.setText(res.getString(R.string.gps_stop));
	}
	
	//点击暂停操作后，对应的view变化
	private void stopViewState(){
		mBtnStart.setVisibility(View.GONE);
		mLayoutSupStart.setVisibility(View.VISIBLE);
		mBtnSupStart.setText(res.getString(R.string.gps_continue));
	}
	
	 /** 开始运动 */
    private void startRun(){
    	Log.i(tag, "startRun");
    	initRunData();
		if(!isNetuseful() && !isGpsuseful()){
			Toast.makeText(this, res.getString(R.string.gps_GPSisSearching), Toast.LENGTH_SHORT).show();
		}
		GaodeService.gpsSportInfo.clearData();
		GaodeService.tempDataModel.clearData();
		
		GaodeService.baseStep = 0;
		GaodeService.baseDistance = 0.00;
		
		startTime=conversTime();
		beginViewState();
		is_line=true;
		GaodeService.is_running=true;
		begin_point_state=1;
        editor = sharepreference.edit();
		editor.putInt("map_activity_state",2 );
		editor.commit();
		Intent beginIntent = new Intent("ACTION_BEGIN_GPSSPORT");
		sendBroadcast(beginIntent);
		GaodeService.point_state=1;
		OperationTimeModel mOperation = new OperationTimeModel();
		mOperation.setOperatId(Tools.getPKL());
		mOperation.setOperationtime(conversTime());
		mOperation.setOperationSystime(System.currentTimeMillis());
		mOperation.setOperationState(OperationTimeModel.BEGIN_GPS_GUIDE);
		mOperation.setSyncState(0);
		mDataBaseUtil.insertOperation(mOperation);
		GpsSportDataModel mSportModel = new GpsSportDataModel();
		long sportId = Tools.getPKL();
		editor = sharepreference.edit();
		editor.putLong("gps_sport_id",sportId);
		editor.commit();
		mSportModel.setGpsId(sportId);
		mSportModel.setStarSysttime(System.currentTimeMillis());
		mSportModel.setStarttime(conversTime());
		mDataBaseUtil.insertGpsInfo(mSportModel);
    }
    
    /** 暂停运动 */
    private void stopRun(){
    	GaodeService.mdistace=sumRunDis;
    	GaodeService.is_running=false;
		is_line=false;
        editor = sharepreference.edit();
		editor.putInt("map_activity_state",3 );		
		editor.commit();
		Intent stopIntent = new Intent("ACTION_STOP_GPSSPORT");
		sendBroadcast(stopIntent);
		stopViewState();
		GaodeService.point_state=2;
		mTaskState=2;
		InitDataTask mapTask=new InitDataTask();
		mapTask.execute();
        OperationTimeModel mOperation=new OperationTimeModel();
        mOperation.setOperatId(Tools.getPKL());
        mOperation.setOperationtime(conversTime());
        mOperation.setOperationSystime(System.currentTimeMillis());
		mOperation.setOperationState(OperationTimeModel.STOP_GPS_GUIDE);
		mOperation.setSyncState(0);
		mDataBaseUtil.insertOperation(mOperation);
    }
    /** 重新开始运动 */
    private void reStartRun(){
    	is_line=true;
		GaodeService.is_running=true;
		continueViewState();
		if(!TextUtils.isEmpty(GaodeService.tempDataModel.getTempImageUrl())){
			File mFile=new File(GaodeService.tempDataModel.getTempImageUrl());
			if(mFile.exists()){
				mFile.delete();
			}
		}
		
		GaodeService.gpsSportInfo.clearData();
		GaodeService.tempDataModel.clearData();
        editor = sharepreference.edit();
		editor.putInt("map_activity_state",4 );		
		editor.commit();
		Intent continueIntent = new Intent("ACTION_CONTINUE_GPSSPORT");
		sendBroadcast(continueIntent);
		GaodeService.point_state=3;
		OperationTimeModel mOperation2 = new OperationTimeModel();
		mOperation2.setOperatId(Tools.getPKL());
		mOperation2.setOperationtime(conversTime());
		mOperation2.setOperationSystime(System.currentTimeMillis());
		mOperation2.setOperationState(OperationTimeModel.CONTINUE_GPS_GUIDE);
		mOperation2.setSyncState(0);
		mDataBaseUtil.insertOperation(mOperation2);
    }
    /** 结束运动 */
    private void finishRun(){
    	if(sumRunDis<100){
   			dialogShow(3);
   		}else{
   			mTaskState=3;
			InitDataTask mapTask=new InitDataTask();
			mapTask.execute();
		}    	
    }
    /** 半屏全屏切换 */
    private void zoomSwitch(){
    	if(isZoomall){
    		mLayoutDetailData.setVisibility(View.GONE);
			mLayoutShortData.setVisibility(View.VISIBLE);
			mLayoutOpeartion.setVisibility(View.GONE);
			mBtnZoom.setImageResource(R.drawable.gps_zoomin);
	        isZoomall=false;
		}else{
			mLayoutDetailData.setVisibility(View.VISIBLE);
    		mLayoutShortData.setVisibility(View.GONE);
    		mLayoutOpeartion.setVisibility(View.VISIBLE);
			mBtnZoom.setImageResource(R.drawable.gps_zoomout);
	        isZoomall=true;
		}
    }
    /**
     * 显示Dialog的内容 
     * @param policy 0:退出；1:打开GPS ；2:记录太短
     */
    private void dialogShow(int policy){
    	int res_title = 0;
    	int res_msg = 0;
    	int btn_pos = 0;
    	int btn_neg = 0;
    	
    	if(policy == 0 ){
    		res_title = R.string.gps_notify;
    		res_msg = R.string.gps_activityToBack;
    		btn_pos = R.string.gps_noNotify;
    		btn_neg = R.string.gps_yes;
    	}else if(policy == 1){
    		res_title = R.string.gps_notify;
    		res_msg = R.string.gps_dialog_needGPS;
    		btn_pos = R.string.gps_no;
    		btn_neg = R.string.gps_dialog_openGPS;
    	}else if(policy == 2){
    		res_title = R.string.gps_notify;
    		res_msg = R.string.gps_dis_short;
    		btn_pos = R.string.gps_no;
    		btn_neg = R.string.gps_exit;
    	}else if(policy == 3){
    		res_title = R.string.gps_notify;
    		res_msg = R.string.gps_dis_short2;
    		btn_pos = R.string.gps_no1;
    		btn_neg = R.string.gps_exit;
    	}else if(policy == 4){
    		res_title = R.string.gps_notify;
    		res_msg = R.string.gps_guide_remind;
    		btn_pos = R.string.gps_yes;
    		btn_neg = R.string.gps_no;
    	}
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(GaoDeMapActivity.this);
    	builder.setCancelable(false);
    	builder.setTitle(res_title);
    	builder.setMessage(res_msg);
    	
    	if(policy == 0 ){
    		
    		builder.setPositiveButton(btn_pos, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Editor edit = sharepreference.edit();
					edit.putBoolean(NEVER_SHOW_LOG, true);
					edit.commit();
					dialog.dismiss();
					GaoDeMapActivity.this.finish();
				}
			});
    		
    		builder.setNegativeButton(btn_neg, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					GaoDeMapActivity.this.finish();
				}
			});
    		
    	}else if(policy == 1){
    		
    		builder.setPositiveButton(btn_pos, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
    		
    		builder.setNegativeButton(btn_neg, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Toast.makeText(GaoDeMapActivity.this,res.getString(R.string.gps_openGPS), Toast.LENGTH_SHORT).show();
			        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);  
			        startActivityForResult(intent, 0); // 此为设置完成后返回到获取界面  
					dialog.dismiss();
				}
			});
    		
    	}else if(policy == 2){
    		builder.setPositiveButton(btn_pos, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
//					saveGpsData.cancel();
					dialog.dismiss();
				}
			});
    		builder.setNegativeButton(btn_neg, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					delteGpsSport();
					dialog.dismiss();
					initRunData();
				}
			});
    	}else if(policy == 3){
    		builder.setPositiveButton(btn_pos, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
//					saveGpsData.cancel();
					mTaskState=3;
					InitDataTask mapTask=new InitDataTask();
					mapTask.execute();
					dialog.dismiss();
				}
			});
    		builder.setNegativeButton(btn_neg, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					delteGpsSport();
					dialog.dismiss();
					initRunData();
				}
			});
    	}else if(policy == 4){
    		builder.setPositiveButton(btn_pos, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					boolean neverShowLog = sharepreference.getBoolean(NEVER_SHOW_LOG, false);			
		    		if(neverShowLog == false && GaodeService.is_running){
		    			dialogShow(0);
		    		}else{
		    			GaoDeMapActivity.this.finish();
		    		}
				}
				
			});
    		builder.setNegativeButton(btn_neg, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
    	}
    	builder.create().show();
    }
    
	private class InitDataTask extends AsyncTask<String, Integer, String> {
    	int operation_state=sharepreference.getInt("map_activity_state", 0);
    	@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if(mTaskState==1){
				if(operation_state>1){
				    loadDialog.show();
					}
			}else if(mTaskState==2){
				//cancelmapDataDialog.show();
			}else if(mTaskState==3){
				//hanldmapDataDialog.show();
			}
		}
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if(mTaskState==1){
				if(operation_state>1){
					loadDialog.dismiss();
					}
			}else if(mTaskState==2){
				//cancelmapDataDialog.dismiss();
			}else if(mTaskState==3){
				//hanldmapDataDialog.dismiss();
			}
		}
		
		@Override
		protected String doInBackground(String... params) {
			if(mTaskState==1){
				if(operation_state>1){
					initDatabaseline();
					}
			}else if(mTaskState==2){
				handlerMap();
			}else if(mTaskState==3){
				compeleGpsSport();
			}
			return null;
		}
    }
	
	/** GPS定位信息监听 */
	class PointObserver implements IGPSPointListener{
		
		Format mFormat = new java.text.DecimalFormat("#0.00");
		
		@Override
		public void update(GuidePointModel point) {
			if(point!=null){
				staticGuide=point;
			}
			
			if(first_zoom){
				point.setPointState(20);
				mDataBaseUtil.inserTempPoint(point);
				 LatLng latlng = new LatLng(point.getLatitude(), point.getLongitude());
		   			float current_zoom = sharepreference.getFloat("current_amp_zoom", 14);
		            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,current_zoom));
		            hanlderMarker(point,CURRENT_MARK);
		            Editor edit = sharepreference.edit();
					edit.putBoolean("is_begin_point", false);
					edit.commit();
		            first_zoom=false;
			}
			if(point.getProvider().equals("gps")){
				if(!isCompleteGps){
					hanlderMarker(point,CURRENT_MARK);
				}
				calGPSLocationPoint(point);
			}
			if(upPoint == null){upPoint = point; }
			if(upPoint.getTime() != point.getTime()){
				if(point.getProvider().equals("gps") && point.getAccuracy() < 15){
					if(GaodeService.is_running){
						/**这里是计算所有的参数变量 更新UI 可以在这里面做*/
						LatLng upLatLng = new LatLng(upPoint.getLatitude(), upPoint.getLongitude());
						LatLng nowLatLng = new LatLng(point.getLatitude(), point.getLongitude());
			
						if(durationTime!=0){
							aveSpeed = sumRunDis/(double)durationTime;
						}else{
							aveSpeed = 0;
						}
						upPoint = point;
						
						long gpsid = sharepreference.getLong("gps_sport_id", -1);
						if(gpsid!=-1){
							GpsSportDataModel mGpsInfo = new GpsSportDataModel();
							mGpsInfo.setGpsId(gpsid);
							mGpsInfo.setAvespeed(aveSpeed);
							mGpsInfo.setCalorie(calories);
							mGpsInfo.setDurationtime(durationTime);
							mGpsInfo.setEndAddress(end_address);
							mGpsInfo.setStartAddress(start_address);
							mGpsInfo.setSteps(sumStep);
							mGpsInfo.setTotalDistance(sumRunDis);
							mDataBaseUtil.updateGpsInfo(mGpsInfo);
						}
						
						mTVDDistance.setText(mFormat.format(sumRunDis/1000));
						mTVSDistance.setText(mFormat.format(sumRunDis/1000));
						mTVDDisSpeed.setText(mFormat.format(aveSpeed*3.6));
					}
				}				
			}
		}

		@Override
		public void sumDisChanged(double distance) {
			sumRunDis = distance;
		}
	}
	/** GPS信号强弱监听 */
	class GPSSignalObserver implements GPSSignalListener{
		@Override
		public void update(int gpsState) {
			initGpsSignal(gpsState);
		}
	}
	/** 运动步数信息监听 */
	class StepObserver implements IStepListener{
		
		@Override
		public void onStepCount(int stepCount) {
		}
		
		@Override
		public void onStateChanged(int newState) {
			
		}

		@Override
		public void onHadRunStep(int hadRunStep) {
			
			sumStep = hadRunStep;
			int meter = Tools.calcDistance(sumStep, config.getHeight());
			calories = Tools.calcCalories(meter, config.getWeightNum());
			
			mTVDStep.setText(""+sumStep);
			mTVSStep.setText(""+sumStep);
			mTVDCalories.setText(""+calories);
		}
	}
	
	 @Override  
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK ){
        	boolean neverShowLog = sharepreference.getBoolean(NEVER_SHOW_LOG, false);			
        	if(neverShowLog == false && GaodeService.is_running){
        		dialogShow(0);
        	}else{
        		this.finish();
        	}	
        	return true;
        }
        return false;  
    }
}