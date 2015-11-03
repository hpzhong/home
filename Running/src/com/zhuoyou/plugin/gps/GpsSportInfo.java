package com.zhuoyou.plugin.gps;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
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
import com.weibo.net.AsyncWeiboRunner.RequestListener;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboException;
import com.zhuoyou.plugin.album.BitmapUtils;
import com.zhuoyou.plugin.cloud.GPSDataSync;
import com.zhuoyou.plugin.cloud.NetMsgCode;
import com.zhuoyou.plugin.database.DataBaseUtil;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.SharePopupWindow;
import com.zhuoyou.plugin.running.Tools;
import com.zhuoyou.plugin.selfupdate.TerminalInfo;
import com.zhuoyou.plugin.share.AuthorizeActivity;
import com.zhuoyou.plugin.share.ShareTask;
import com.zhuoyou.plugin.share.ShareToWeixin;


public class GpsSportInfo extends Activity implements OnMarkerClickListener,OnClickListener,OnMapLoadedListener,
			OnMapTouchListener,OnCameraChangeListener,OnMapScreenShotListener{
	
	private AMap aMap;
	private MapView mapView;
	private long startTime;
	private long endTime;
	private String gpsId;
    private DataBaseUtil mDataBaseUtil;
    private Polyline polyline;
    private Marker mMark;
    private Marker beiginMark,endMark,cancelMark;
	private Dialog mDialog;
	private Dialog loadDialog;
	private GuidePointModel firstPoint;
	private GuidePointModel lastPoint;
	private GpsSportDataModel mGpsInfo;
	private Resources res;
	double []gpspoints; 
	private Context mContext;
	float current_mzoom;				//获得当前缩放级别
	private final static int DELAY_MAP_CENTER = 1200;
	private final static int SHARE_TO_WEIXIN = 1201;
	private final static int SHARE_TO_QUAN = 1202;
	private final static int SHARE_TO_QQ = 1203;
	private final static int SHARE_TO_MORE = 1204;
	private static int select = 0;
	private boolean isZoomall=true;		//该标记判断地图全屏还是半屏
	/** program UI */
	private TextView mTVDStep;						// 步数(详细)
	private TextView mTVDTimer;						// 计时(详细)
	private TextView mTVDDistance;					// 运动距离(详细)
	private TextView mTVDDisSpeed;					// 运动即时速度(详细)
	private TextView mTVDCalories;					// 消耗卡路里(详细)
//	private TextView mTVDAvgSpeed;					// 运动平局速度(详细)
	private ImageView mShare_weixin;
	private ImageView mShare_quan;
	private ImageView mShare_qq;
	private ImageView mMore;
	private boolean isWXInstalled = false;
	private boolean isWBInstalled = false;
	private boolean isQQInstalled = false;
	private boolean is_china = true;
	private Weibo weibo = Weibo.getInstance();
	private ImageView mBtnZoom;
	private ImageView BtnShare;
	private LinearLayout mLayoutDetailData;
	private LinearLayout mLayoutGpsShare;
	private String mstartAddress;
	private String mendAddress;
	public static final int BEGIN_MARK=1; //开始图标
	public static final int END_MARK=2;   //结束图标
	public static final int CANCEL_MARK=3;  //暂停图标
	public static final int CURRENT_MARK=4;	//当前图标
	private UiSettings mUiSettings;   //地图显示控制
	private View mview;
	private String fileName;
	private SharePopupWindow mPopupWindow;
	private String start_address;
	private String end_address;
	private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 setContentView(R.layout.gpssport_info);
		 res = getResources();
		 mapView = (MapView) findViewById(R.id.gpssport_map);
	     mapView.onCreate(savedInstanceState);		// 必须要写
	     start_address=res.getString(R.string.gps_startAddress);
	     end_address=res.getString(R.string.gps_endAddress);
	     if (aMap == null) {
	            aMap = mapView.getMap();
	            mUiSettings=aMap.getUiSettings();
	            mUiSettings.setZoomControlsEnabled(false);
	            aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
	            aMap.setOnMapLoadedListener(this);
	            aMap.setOnMapTouchListener(this);
	            aMap.setOnCameraChangeListener(this);//可视区域改变监听
	        }
	     mContext=this;
	     getShareAppStatus();
	     getLanguageEnv();
	     mview = (FrameLayout) findViewById(R.id.gps_record_info);
	     mTVDStep = (TextView)findViewById(R.id.gps_tvde_step);
    	 mTVDTimer = (TextView)findViewById(R.id.gps_tvde_timer);
    	 mTVDDistance = (TextView)findViewById(R.id.gps_tvde_distance);
    	 mTVDDisSpeed = (TextView)findViewById(R.id.gps_tvde_dipspeed);
    	 mTVDCalories = (TextView)findViewById(R.id.gps_tvde_calorie);
//    	 mTVDAvgSpeed = (TextView)findViewById(R.id.gps_tvde_avgspeed);
    	 mShare_weixin = (ImageView)findViewById(R.id.share_weixin);
 		 mShare_weixin.setOnClickListener(this);
 		 mShare_quan = (ImageView)findViewById(R.id.share_quan);
 		 mShare_quan.setOnClickListener(this);
 		 mShare_qq = (ImageView)findViewById(R.id.share_qq);
 		 mShare_qq.setOnClickListener(this);
 		 mMore = (ImageView)findViewById(R.id.share_more);
 		 mMore.setOnClickListener(this);
    	 mBtnZoom = (ImageView)findViewById(R.id.gps_igbtn_zoom);
    	 BtnShare = (ImageView)findViewById(R.id.gps_share);
    	 mLayoutDetailData = (LinearLayout)findViewById(R.id.gps_layout_detaildata);
    	 mLayoutGpsShare = (LinearLayout)findViewById(R.id.gps_share_choice);
    	 mBtnZoom.setOnClickListener(this);
    	 BtnShare.setOnClickListener(this);
    	 
	     mDataBaseUtil=new DataBaseUtil(getApplicationContext());
	     loadDialog = new AlertDialog.Builder(this).setTitle(res.getString(R.string.gps_loadingmMap)).setMessage(res.getString(R.string.gps_isLoadingMap)).create();
	     Intent mItent=this.getIntent();
	     gpsId = mItent.getStringExtra("gpsid");
	     if(!TextUtils.isEmpty(gpsId)){
	    	 mGpsInfo= mDataBaseUtil.selectGpsInfoForID(Long.parseLong(gpsId));
	    	 if(mGpsInfo.getStarttime()!=0){
	    		 loadingMapData();
	    	 }else{
	    		 downCloudGPSData(Long.valueOf(gpsId));
	    	 }
	     }else{
	    	 if(loadDialog.isShowing()){
	    		 loadDialog.dismiss();
	    	 }
	    	 Toast.makeText(this, R.string.update_failed, Toast.LENGTH_LONG).show();
	     }
	}
	
	private void loadingMapData(){
		 startTime=mGpsInfo.getStarttime();
    	 endTime=mGpsInfo.getEndtime();
    	 mstartAddress=mGpsInfo.getStartAddress();
    	 mendAddress=mGpsInfo.getEndAddress();
    	 initTextValue();	     
	     if(startTime!=0 && endTime!=0){
	    	 gpspoints = mDataBaseUtil.findGpsBound(startTime, endTime);
	    	 firstPoint=mDataBaseUtil.selectFirstPoint(startTime, endTime);
	    	 lastPoint=mDataBaseUtil.selectLasttPoint(startTime, startTime);
	         InitDataTask myTask=new InitDataTask();
	 		 myTask.execute();
	     }
	}
	
	 private void downCloudGPSData(long gpsId){
	    	TerminalInfo info = TerminalInfo.generateTerminalInfo(this);
	    	if(info.getNetworkType() == 0){
	    		Toast.makeText(this, R.string.open_net, Toast.LENGTH_SHORT).show();
	    	}else{
	    		GPSDataSync gpsSync = new GPSDataSync(this, 1);
	    		gpsSync.getGPSFromCloud(mCloudHandler, gpsId);
	    	}
	    }
		
		private Handler mCloudHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				/** 成功 */
				case NetMsgCode.getGPSInfo:
					mGpsInfo = mDataBaseUtil.selectGpsInfoForID(Long.parseLong(gpsId));
					loadingMapData();
					break;
				/** 失败 */
				case 110011:
					 if(loadDialog.isShowing()){
			    		 loadDialog.dismiss();
			    	 }
					Toast.makeText(GpsSportInfo.this,  R.string.update_failed, Toast.LENGTH_SHORT).show();
					break;
				}
			}
	    };
	
	
	private void initTextValue(){
		Format decFormat = new DecimalFormat("#0.00");
		Format intFormat = new DecimalFormat("00");
		mTVDStep.setText(String.valueOf(mGpsInfo.getSteps()));
		mTVDTimer.setText(formatTimer(mGpsInfo.getDurationtime()));
		mTVDDistance.setText(decFormat.format(mGpsInfo.getTotalDistance()/1000));
		
		double aveSpeed = mGpsInfo.getAvespeed()*3.6 ;
		mTVDDisSpeed.setText(decFormat.format(aveSpeed));
		mTVDCalories.setText(""+Double.valueOf(mGpsInfo.getCalorie()).intValue());
//		mTVDAvgSpeed.setText(format.format(mGpsInfo.getAvespeed()));					
	}
	
	
	private void getLanguageEnv() {  
	       Locale mlocale = Locale.getDefault();  
	       String language = mlocale.getLanguage();  
	       if ("zh".equals(language)) {  
	           is_china = true;
	       }else{
	    	   is_china = false;  
	       } 
	   } 
	
	public static String formatTimer(long timer) {
		Format format = new DecimalFormat("00");
		String hour = format.format(timer % (24 * 3600) / 3600);
		String minute = format.format(timer % 3600 / 60);
		String second = format.format(timer % 60);
		return hour + ":" + minute + ":" + second;
	}
	
	public void initDatabaseline(){
		List<GuidePointModel> sublistPoint;
		List<GuidePointModel> handlistPoint;
		GuidePointModel beginPoint;
		GuidePointModel endPoint;
		List<GuidePointModel> smallistPoint =new ArrayList<GuidePointModel>();
		List<GuidePointModel> smallistPoint2 =new ArrayList<GuidePointModel>();
		if(startTime!=0){
			//GuidePointModel firstPoint=mDataBaseUtil.selectFirstPoint(startTime, endTime);
			if(firstPoint.getLongitude()!=0){
				sublistPoint=mDataBaseUtil.selectPoint(firstPoint.getTime(), endTime,0);//此数据查询的为状态大于0的点
				if(sublistPoint.size()>0){
					if(sublistPoint.size()==1){
						smallistPoint=mDataBaseUtil.selectPoint(sublistPoint.get(0).getTime(), endTime);
						handlistPoint=GpsUtil.handlePoint(smallistPoint);
						beginPoint=handlistPoint.get(0);
						beginPoint.setTime(smallistPoint.get(0).getTime());
						hanlderMarker(beginPoint,BEGIN_MARK);
						endPoint=handlistPoint.get(handlistPoint.size()-1);
						endPoint.setTime(smallistPoint.get(smallistPoint.size()-1).getTime());
						hanlderMarker(endPoint,END_MARK);
						initLine(handlistPoint,false);
					}else{
						for(int i=0;i<sublistPoint.size();i++){
							if(i>0){
								if(sublistPoint.get(i).getPointState()==3 || sublistPoint.get(i).getPointState()==5 || sublistPoint.get(i).getPointState()==6){
									smallistPoint=mDataBaseUtil.selectPoint(sublistPoint.get(i-1).getTime(), sublistPoint.get(i).getTime());
									if(smallistPoint.size()==2){
										initLine(smallistPoint,false);
										if(i==sublistPoint.size()-1){
											smallistPoint2=mDataBaseUtil.selectPoint(sublistPoint.get(i).getTime(), endTime);
										}else{
											smallistPoint2=mDataBaseUtil.selectPoint(sublistPoint.get(i).getTime(), sublistPoint.get(i+1).getTime()-1);
										}
										List<GuidePointModel> currentlistPoint =new ArrayList<GuidePointModel>();
										currentlistPoint.add(smallistPoint.get(smallistPoint.size()-1));
										currentlistPoint.add(GpsUtil.handlePoint(smallistPoint2).get(0));
										initLine(currentlistPoint,false);
									}else if(smallistPoint.size()>2){
										smallistPoint=mDataBaseUtil.selectPoint(sublistPoint.get(i-1).getTime(), sublistPoint.get(i).getTime()-1);
										handlistPoint=GpsUtil.handlePoint(smallistPoint);
										initLine(handlistPoint,false);
										if(i==sublistPoint.size()-1){
											smallistPoint2=mDataBaseUtil.selectPoint(sublistPoint.get(i).getTime(), endTime);
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
									smallistPoint=mDataBaseUtil.selectPoint(sublistPoint.get(i).getTime(), endTime);
									handlistPoint=GpsUtil.handlePoint(smallistPoint);
									endPoint=handlistPoint.get(handlistPoint.size()-1);
									endPoint.setTime(smallistPoint.get(smallistPoint.size()-1).getTime());
									hanlderMarker(endPoint,END_MARK);
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
	

	 private class InitDataTask extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		    loadDialog.show();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			loadDialog.dismiss();
		}

		@Override
		protected String doInBackground(String... params) {
			initDatabaseline();
			return null;
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
    
	//该方法为在地图上画出对应点
	public void hanlderMarker(GuidePointModel mGuidePoint,int marker){
		LatLng latlng = new LatLng(mGuidePoint.getLatitude(), mGuidePoint.getLongitude());
		switch (marker) {
			case BEGIN_MARK:
					MarkerOptions markerOption1;
					if(is_china){
						markerOption1 = new MarkerOptions().anchor(0.5f, 0.5f)
				 				.position(latlng).title(start_address)
				 				.snippet(mstartAddress).icon(BitmapDescriptorFactory.fromResource(R.drawable.point_begin))
				 				.perspective(true).draggable(true);
					}else{
						markerOption1 = new MarkerOptions().anchor(0.5f, 0.5f)
				 				.position(latlng).title(start_address)
				 				.snippet(mstartAddress).icon(BitmapDescriptorFactory.fromResource(R.drawable.point_begin_en))
				 				.perspective(true).draggable(true);
					}
			 		beiginMark=aMap.addMarker(markerOption1);
				break;
			case END_MARK:
					MarkerOptions markerOption2;
					if(is_china){
						markerOption2 = new MarkerOptions().anchor(0.5f, 0.5f)
				 				.position(latlng).title(end_address)
				 				.snippet(mendAddress).icon(BitmapDescriptorFactory.fromResource(R.drawable.point_complete))
				 				.perspective(true).draggable(true);
					}else{
						markerOption2 = new MarkerOptions().anchor(0.5f, 0.5f)
				 				.position(latlng).title(end_address)
				 				.snippet(mendAddress).icon(BitmapDescriptorFactory.fromResource(R.drawable.point_complete_en))
				 				.perspective(true).draggable(true);
					}
			 		endMark=aMap.addMarker(markerOption2);
				break;
			case CANCEL_MARK:
					MarkerOptions markerOption3 = new MarkerOptions().anchor(0.5f, 0.5f)
			 				.position(latlng)
			 				.icon(BitmapDescriptorFactory.fromResource(R.drawable.point_cancel))
			 				.perspective(true).draggable(true);
			 		Marker cancelMark=aMap.addMarker(markerOption3);
				break;
			default:
				break;
		}
	}	  		
  	
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}
	
	 @Override
	protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

	@Override
	public boolean onMarkerClick(Marker marker) {
		if(marker.isInfoWindowShown()){
			marker.hideInfoWindow();
		}else{
			marker.showInfoWindow();
		}
		return false;
	}
	

	@Override
	public void onClick(View v) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		fileName = format.format(new Date()) + ".jpg";
		switch (v.getId()) {
		case R.id.back:
			this.finish();
			break;
		case R.id.gps_igbtn_zoom:
			zoomSwitch();
			break;
		case R.id.gps_share:
			mLayoutGpsShare.setVisibility(View.VISIBLE);
			break;
		case R.id.share_weixin:
			getMapScreenShot();
			mHandler.sendEmptyMessageDelayed(SHARE_TO_WEIXIN,1000);
			break;
		case R.id.share_quan:
			getMapScreenShot();
			mHandler.sendEmptyMessageDelayed(SHARE_TO_QUAN,1000);
			break;
		case R.id.share_qq:
			getMapScreenShot();
			mHandler.sendEmptyMessageDelayed(SHARE_TO_QQ,1000);
			break;
		case R.id.share_more:
			getMapScreenShot();
			mHandler.sendEmptyMessageDelayed(SHARE_TO_MORE,1000);
			break;
		case R.id.img_back:
			finish();
			break;
		}
	}
	
	public void getMapScreenShot() {
		aMap.getMapScreenShot(this);
	}
	
	@Override
	public void onMapScreenShot(Bitmap bit1) {
		Bitmap bitmap;
		if(isZoomall){
			Bitmap bit2=getScreenShot(mview);
			if(null == bit1){
				return;
			}
			int width =bit1.getWidth();
			int height =bit1.getHeight()+bit2.getHeight();
			bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888); 
			Canvas canvas = new Canvas(bitmap);
			canvas.drawBitmap(bit1, 0, 0, null); 
			canvas.drawBitmap(bit2, 0, bit1.getHeight(), null);
		}else{
			bitmap=bit1;
		}
		Bitmap newbitmap = BitmapUtils.compressImage(bitmap,100,"");
		Tools.saveBitmapToFile(newbitmap, fileName);
		//Tools.saveGpsBitmapToFile(bitmap, fileName);
		//bitmap.recycle();
		bitmap = null;
	}
	
	
	private Bitmap getScreenShot(View mScreenshot){	
		 Bitmap bmp = Bitmap.createBitmap(mScreenshot.getWidth(),mScreenshot.getHeight(),Config.ARGB_8888);
	     mScreenshot.draw(new Canvas(bmp));
	   	 return bmp;
	}
	
	/** 半屏全屏切换 */
    private void zoomSwitch(){
    	if(isZoomall){
    		mLayoutDetailData.setVisibility(View.GONE);
    		mBtnZoom.setImageResource(R.drawable.gps_zoomin);
	        isZoomall = false;
		}else{
			mLayoutDetailData.setVisibility(View.VISIBLE);
			mBtnZoom.setImageResource(R.drawable.gps_zoomout);
	        isZoomall = true;
		}
    }
    
    private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case DELAY_MAP_CENTER:
				LatLng latlng = new LatLng((gpspoints[0]+gpspoints[2])/2, (gpspoints[1]+gpspoints[3])/2);
				aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,current_mzoom));
				break;
			
		   case SHARE_TO_WEIXIN:
			   if (isWXInstalled) {
					if (ShareToWeixin.api.isWXAppSupportAPI()){
						ShareToWeixin.SharetoWX(mContext, false, fileName);
						mLayoutGpsShare.setVisibility(View.GONE);
					}else{
						Toast.makeText(mContext, R.string.weixin_no_support, Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(mContext, R.string.install_weixin, Toast.LENGTH_SHORT).show();
				}
			   break;
		   case SHARE_TO_QUAN:
			   if (isWXInstalled) {
					int wxSdkVersion = ShareToWeixin.api.getWXAppSupportAPI();
					if (wxSdkVersion >= TIMELINE_SUPPORTED_VERSION){
						ShareToWeixin.SharetoWX(mContext, true, fileName);
						mLayoutGpsShare.setVisibility(View.GONE);
					}else{
						Toast.makeText(mContext, R.string.weixin_no_support_quan, Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(mContext, R.string.install_weixin, Toast.LENGTH_SHORT).show();
				}
			   break;
		   case SHARE_TO_QQ:
			   if (isQQInstalled) {
					shareToQQ(fileName);
					mLayoutGpsShare.setVisibility(View.GONE);
				} else {
					Toast.makeText(mContext, R.string.install_qq, Toast.LENGTH_SHORT).show();
				}
			   break;
		   case SHARE_TO_MORE:
			    mPopupWindow = new SharePopupWindow(GpsSportInfo.this, itemsOnClick, fileName);
				mPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
				mPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
				mPopupWindow.showAtLocation(GpsSportInfo.this.findViewById(R.id.gps_sport_info), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
				mLayoutGpsShare.setVisibility(View.GONE);
			   break;
			}
			super.handleMessage(msg);
		};
	};
    
	@Override
	public void onMapLoaded() {
		if(gpspoints!=null){
			LatLng startGuide = new LatLng(gpspoints[0],gpspoints[1]);
			LatLng endGuide = new LatLng(gpspoints[2],gpspoints[3]);
	        LatLngBounds bounds = new LatLngBounds.Builder().include(startGuide).include(endGuide).build();
	        // 移动地图，所有marker自适应显示。LatLngBounds与地图边缘10像素的填充区域
	        aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
		}			
	}



	@Override
	public void onTouch(MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:// 屏幕按下  
			if(mHandler.hasMessages(DELAY_MAP_CENTER)){
			mHandler.removeMessages(DELAY_MAP_CENTER);
			}
			//Log.d("mHandler", "ACTION_DOWN...");
		break;
		case MotionEvent.ACTION_UP:// 按下抬起
			if(gpspoints!=null){
			mHandler.sendEmptyMessageDelayed(DELAY_MAP_CENTER, 3*1000);
			}
			//Log.d("mHandler", "ACTION_UP...");
		break;
		}
	}


	@Override
	public void onCameraChange(CameraPosition arg0) {
		
	}


	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		// TODO Auto-generated method stub
		current_mzoom = aMap.getCameraPosition().zoom;
	}
	private void getShareAppStatus() {
		PackageManager pm = getPackageManager();
		Intent filterIntent = new Intent(Intent.ACTION_SEND,null);
		filterIntent.addCategory(Intent.CATEGORY_DEFAULT);
		filterIntent.setType("text/plain");

		List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();
		resolveInfos.addAll(pm.queryIntentActivities(filterIntent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT));
		
		for(int i=0;i<resolveInfos.size();i++) {
			ResolveInfo resolveInfo = resolveInfos.get(i);
			String mPackageName = resolveInfo.activityInfo.packageName;
			if(mPackageName.equals("com.tencent.mm")) {
				isWXInstalled = true;
			}
			if(mPackageName.equals("com.sina.weibo")) {
				isWBInstalled = true;
			}
			if(mPackageName.equals("com.tencent.mobileqq")) {
				isQQInstalled = true;
			}
		}
	}
	
	private void shareToQQ(String fileName) {
		ComponentName cp = new ComponentName("com.tencent.mobileqq","com.tencent.mobileqq.activity.JumpActivity");
		Intent intent=new Intent(Intent.ACTION_SEND);
		intent.setComponent(cp);
		intent.setType("image/*");
		File file = new File(Tools.getScreenShot(fileName));
		Uri uri = Uri.fromFile(file);
		intent.putExtra(Intent.EXTRA_STREAM, uri);
		this.startActivity(intent);
	}

	private void share2weibo(String content, String picpath) {
		ShareTask task = new ShareTask(this, picpath, content, mRequestListener);
		new Thread(task).start();
        if (mPopupWindow.isShowing())
        	mPopupWindow.dismiss();
    }

    private RequestListener mRequestListener = new RequestListener() {

		@Override
		public void onComplete(String response) {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(GpsSportInfo.this, R.string.share_success, Toast.LENGTH_SHORT).show();
                }
            });
		}

		@Override
		public void onError(final WeiboException e) {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(GpsSportInfo.this, e.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
		}

		@Override
		public void onIOException(final IOException e) {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(GpsSportInfo.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
		}
    	
    };
    

	private OnClickListener  itemsOnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.share_weibo:
				if (!weibo.isSessionValid()) {
					Intent intent = new Intent();
					intent.setClass(GpsSportInfo.this, AuthorizeActivity.class);
					startActivity(intent);
				} else {
					select = 1;
					SharePopupWindow.mInstance.getWeiboView().setImageResource(R.drawable.share_wb_select);
				}
				break;
			case R.id.share:
				if (select > 0) {
					if (SharePopupWindow.mInstance != null) {
						String share_s = SharePopupWindow.mInstance.getShareContent();
						share2weibo(share_s, Tools.getScreenShot(SharePopupWindow.mInstance.getShareFileName()));
					}
				} else {
					Toast.makeText(mContext, R.string.select_platform, Toast.LENGTH_SHORT).show();
				}
				break;
			}
		}
		
	};
	
	
	private Handler mHandler2 = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case 1:
				select = 1;
				break;
			}
		}
    };
   
}
