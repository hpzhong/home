package com.zhuoyou.plugin.cloud;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.zhuoyou.plugin.custom.CustomAlertDialog;
import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.info.InformationActivity;
import com.zhuoyou.plugin.rank.MotionRank;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.RunningItem;
import com.zhuoyou.plugin.running.Tools;

public class CloudSync {

	private static final Context mContext = RunningApp.getInstance().getApplicationContext();
	public static int autoSyncType = 1;//1是不自动或2小时 2是wifi触发
	public static int syncType;
	public static boolean isSync = false;
	static ProgressDialog m_pDialog;
	private static int getInfo = 0;

	public static void prepareSync() {
		int net = NetUtils.getAPNType(mContext);
		if (!Tools.getLogin(mContext)) {
			Toast.makeText(mContext, R.string.login_before_sync, Toast.LENGTH_SHORT).show();
			return;
		}
		if (net == -1) {
			Toast.makeText(mContext, R.string.check_network, Toast.LENGTH_SHORT).show();
		} else if (net == 1) {
			syncData(0);
		} else {
			CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(mContext);
			builder.setTitle(R.string.cloud_syncs);
			builder.setMessage(R.string.cloud_sync_message);
			builder.setPositiveButton(R.string.cancle, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.setNegativeButton(R.string.continueto, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					syncData(0);
				}
			});
			builder.setCancelable(false);
			CustomAlertDialog dialog = builder.create();
			dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			dialog.show();
		}
	}
	
	public static void startAutoSync(int netType) {
		if (netType == 1) {
			syncData(1);
		} else {
			syncData(2);
		}
	}
	
	public static void startSyncInfo() {
		int net = NetUtils.getAPNType(mContext);
		if (net == -1 || !Tools.getLogin(mContext))
			return;
		else
			syncData(3);
	}
	
	static Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NetMsgCode.The_network_link_success:
				switch (msg.arg1) {
				case NetMsgCode.postCustomInfo:
					if (syncType == 3) {
						setSyncEnd();
					} else {
						new Thread(postSportDataRunnable).start();
					}
					break;
				case NetMsgCode.postSportInfo:
					SportDataSync.updateSportSyncState(mContext);
					InformationActivity.clearFeedTable(DataBaseContants.TABLE_DELETE_NAME);
					if (syncType == 0)
						downloadData(1);
					else
						setSyncEnd();
					break;
				case NetMsgCode.getCustomInfo:
					if(getInfo == 1){
						Log.d("txhlog", " start getSportFromCloudRunnable");
						new Thread(getSportFromCloudRunnable).start();
						getInfo = 0;
					}else{
						Intent intent = new Intent();
						intent.setAction("com.zhuoyou.plugin.updateInfo");
						mContext.sendBroadcast(intent);
					}
					break;
				case NetMsgCode.getSportInfo:
					List<RunningItem> mysport=(List<RunningItem>)msg.obj;
					Log.d("txhlog","msg getSportInfo! "+"mlist.size ="+mysport.size());
					if(mysport != null && mysport.size() > 0){
						SportDataSync.insertSportData(mContext,mysport);
					}
					setSyncEnd();
					Toast.makeText(mContext, R.string.complete_sync, Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}
				break;
			case NetMsgCode.The_network_link_failure:
				if(getInfo == 0){
					return;
				}
				if (syncType == 0)
					Toast.makeText(mContext, R.string.network_link_failure, Toast.LENGTH_SHORT).show();
				setSyncEnd();
				break;
			default:
				break;
			}
		}
	};
	
	/*云同步：上传
	 * 参数type：0为个人信息运动数据图片都同步 1为同步运动信息和图片 2为只同步运动信息 3为只同步个人信息
	 */	
	public static void syncData(int type) {
		if(Tools.getLogin(mContext)){
			syncType = type;
			isSync = true;
			switch (type) {
			case 0:
				m_pDialog = new ProgressDialog(mContext);
				m_pDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				m_pDialog.setMessage(mContext.getResources().getString(R.string.progressbar_dialog_sync));
				m_pDialog.setCancelable(false);
				m_pDialog.show();
			case 3:
				CustomInfoSync cusInfo=new CustomInfoSync(mContext,handler);
				cusInfo.postCustomInfo();
				break;
			case 1:
			case 2:
				SportDataSync sportData=new SportDataSync(mContext);
				sportData.postSportData(handler);
				break;
			default:
				break;
			}
			
		}	
	}
	
	private static void setSyncEnd() {
		isSync = false;
		if(m_pDialog!=null && m_pDialog.isShowing()){
			m_pDialog.dismiss();
		}
		Message msg= new Message();
		msg.what =NetMsgCode.rank_state_request; 
		if (MotionRank.handler != null)
			MotionRank.handler.sendMessage(msg);
	}
	
	/*云同步：下载
	 * 下载所有云端的数据
	 */	
	public static void downloadData(int type) {
		if(Tools.getLogin(mContext)){
			getInfo = type;
			CustomInfoSync cusInfo=new CustomInfoSync(mContext,handler);
			cusInfo.getCustomInfo();
		}
	}
	
	static Runnable postSportDataRunnable = new Runnable() {
		@Override
		public void run() {
			SportDataSync sportData=new SportDataSync(mContext);
			sportData.postSportData(handler);
		}	
	};	
	
	static Runnable getSportFromCloudRunnable = new Runnable() {
		@Override
		public void run() {
			SportDataSync sportData=new SportDataSync(mContext);
			sportData.getSportFromCloud(handler);
		}	
	};	
	
	public static Runnable getNetRankInfoRunnable = new Runnable() {
		@Override
		public void run() {
			RankInfoSync rankInfo=new RankInfoSync(mContext);
			if (MotionRank.handler != null){
				rankInfo.getNetRankInfo(MotionRank.handler);
			}

		}	
	};	
	
}
