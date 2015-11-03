package com.zhuoyou.plugin.gps;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

import com.mcube.lib.ped.PedBackgroundService;
import com.zhuoyou.plugin.database.DataBaseUtil;
import com.zhuoyou.plugin.running.Tools;

/**
 * 该类主要是通过广播方式判断后台Service是否在裕兴状态
 * 注释：isgaodeSerAlive GPS轨迹点记录
 * isstepsSerAlive 记步记录
 */

public class ServiceUtil {
	public static  boolean isgaodeSerAlive;
	public boolean isstepsSerAlive;
	public Context mContext;
	public java.text.SimpleDateFormat formatter= new java.text.SimpleDateFormat("yyyyMMddHHmmss");
	public DataBaseUtil mDataBaseUtil;


		public ServiceUtil(Context mContext) {
			super();
			this.mContext = mContext;
			initFilter();
		}

		//add zhaojunhui
		public boolean closeGaodeService(){
			boolean isclose=mContext.stopService(new Intent(mContext,GaodeService.class));
			if(isclose){
				isgaodeSerAlive=false;
			}
			return isclose;
			 
		}
		public boolean isServiceRunning(){
	    	isgaodeSerAlive = false;
	    	isstepsSerAlive = false;
	    	mContext.sendBroadcast(new Intent("com.zhuoyou.gaode.service.hello"));
	    	mContext.sendBroadcast(new Intent("com.zhuoyou.steps.service.hello"));
	    	mAlivegpsHandler.sendEmptyMessageDelayed(0, 3000);
	    	mAlivestepsHandler.sendEmptyMessageDelayed(0, 3000);
	    	return true;
	    }
		
		
		public Handler mAlivegpsHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
		           	if(!isgaodeSerAlive){
		           		mContext.startService(new Intent(mContext,GaodeService.class));
		           		if(GaoDeMapActivity.is_line){
		           			mDataBaseUtil=new DataBaseUtil(mContext);
		           			GaodeService.is_running=true;
		           			OperationTimeModel mOperation=new OperationTimeModel();
		           			mOperation.setOperatId(Tools.getPKL());
					        mOperation.setOperationtime(conversTime(System.currentTimeMillis()));
					        mOperation.setOperationSystime(System.currentTimeMillis());
					        GaodeService.point_state=5;
							mOperation.setOperationState(OperationTimeModel.SERVICE_IS_STOP);
							mOperation.setSyncState(0);
							mDataBaseUtil.insertOperation(mOperation);
		           	}
	        	}
			}
		};
		
		public Handler mAlivestepsHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
	           	if(!isstepsSerAlive){
	           		mContext.startService(new Intent(mContext.getApplicationContext(),PedBackgroundService.class));
	           	}
			}
		};
		
		public void initFilter(){
	    	IntentFilter mGATTFilter = new IntentFilter();
	        mGATTFilter.addAction("com.zhuoyou.gaode.activity.hi");
	        mGATTFilter.addAction("com.zhuoyou.steps.activity.hi");
	        mContext.registerReceiver(mBroadcastReceiver, mGATTFilter);
	    }
		
		  //接收广播
	    public BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				 if(action.equals("com.zhuoyou.gaode.activity.hi")){
					isgaodeSerAlive = true;
				}else if(action.equals("com.zhuoyou.steps.activity.hi")){
					isstepsSerAlive = true;
				}
			}
		};
		
		
		//时间转换，把系统时间改变成日前
		public long conversTime(long systemTime){
			Date date = new Date(systemTime);
			String str_time = formatter.format(date);
			Long pointTime = Long.parseLong(str_time);
			return pointTime;
		}
		
		public void uninitFilter(){
			mContext.unregisterReceiver(mBroadcastReceiver);
	    }
}
