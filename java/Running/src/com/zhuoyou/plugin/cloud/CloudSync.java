package com.zhuoyou.plugin.cloud;

import java.util.HashMap;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.zhuoyou.plugin.custom.CustomAlertDialog;
import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.mainFrame.RankFragment;
import com.zhuoyou.plugin.running.HomePageFragment;
import com.zhuoyou.plugin.running.Main;
import com.zhuoyou.plugin.running.MainService;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;

public class CloudSync {

	private static final Context mContext = RunningApp.getInstance().getApplicationContext();
	public static int autoSyncType = 1;//1是不自动或2小时 2是wifi触发
	public static int syncType = 0;
	public static boolean isSync = false;
	private static int getInfo = 2;

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
		} else if (netType == 2) {
			syncData(2);
		} else if (netType == 4) {
			syncData(4);
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
					Log.d("txhlog", " start postCustomInfo");
					if (HomePageFragment.mHandler != null) {
						Message message = new Message();
						message.what = HomePageFragment.SYNC_DEVICE_PROGRESS;
						message.obj = "10%";
						HomePageFragment.mHandler.sendMessage(message);
					}
					if (syncType == 3) {
						setSyncEnd();
					} else {
						new Thread(postSportDataRunnable).start();
					}
					break;
				case NetMsgCode.postSportInfo:
					Log.d("txhlog", " start postSportInfo");
					if (HomePageFragment.mHandler != null) {
						Message message = new Message();
						message.what = HomePageFragment.SYNC_DEVICE_PROGRESS;
						message.obj = "50%";
						HomePageFragment.mHandler.sendMessage(message);
					}
					Tools.clearFeedTable(DataBaseContants.TABLE_DELETE_NAME, mContext);
					if (syncType == 0)
						downloadData();
					else
						setSyncEnd();
					break;
				case NetMsgCode.getCustomInfo:
					Log.d("txhlog", " start getSportInfo");
					if (HomePageFragment.mHandler != null) {
						Message message = new Message();
						message.what = HomePageFragment.SYNC_DEVICE_PROGRESS;
						message.obj = "60%";
						HomePageFragment.mHandler.sendMessage(message);
					}
					new Thread(getSportFromCloudRunnable).start();
					break;
				case NetMsgCode.getSportInfo:
					Log.d("txhlog","msg getSportInfo! ");
					setSyncEnd();
					if (HomePageFragment.mHandler != null) {
						Message message = new Message();
						message.what = HomePageFragment.SYNC_DEVICE_SUCCESSED;
						message.arg1 = 2;
						HomePageFragment.mHandler.sendMessage(message);
					}
					Toast.makeText(mContext, R.string.complete_sync, Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}
				break;
			case NetMsgCode.The_network_link_failure:
				if (HomePageFragment.mHandler != null) {
					Message message = new Message();
					message.what = HomePageFragment.SYNC_DEVICE_FAILED;
					message.arg1 = 2;
					HomePageFragment.mHandler.sendMessage(message);
				}
				if (getInfo != 2) {
					isSync = false;
					getInfo = 2;
					return;
				}
				if (syncType == 0)
					Toast.makeText(mContext, R.string.network_link_failure, Toast.LENGTH_SHORT).show();
				setSyncEnd();
				break;
			case 110011:
				if (HomePageFragment.mHandler != null) {
					Message message = new Message();
					message.what = HomePageFragment.SYNC_DEVICE_FAILED;
					message.arg1 = 2;
					HomePageFragment.mHandler.sendMessage(message);
				}
				if (syncType == 0)
					Toast.makeText(mContext,R.string.update_failed,Toast.LENGTH_SHORT).show();
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
	private static void syncData(int type) {
		if(Tools.getLogin(mContext) && !MainService.syncnow && !isSync){
			getInfo = 2;
			syncType = type;
			isSync = true;
			switch (type) {
			case 0:
				if (HomePageFragment.mHandler != null) {
					Message message = new Message();
					message.what = HomePageFragment.SYNC_DEVICE_START;
					message.arg1 = 2;
					HomePageFragment.mHandler.sendMessage(message);
				}
			case 3:
				CustomInfoSync cusInfo=new CustomInfoSync(mContext,handler);
				cusInfo.postCustomInfo();
				break;
			case 1:
			case 2:
				SportDataSync sportData=new SportDataSync(mContext, getInfo);
				sportData.postSportData(handler);
				break;
			default:
				break;
			}
			
		}
		if (type == 4) {
			msgInfo();
		}
	}
	
	private static void setSyncEnd() {
		isSync = false;
	}
	
	/*云同步：下载
	 * 下载所有云端的数据
	 */	
	public static void downloadData() {
		if(Tools.getLogin(mContext)){
			CustomInfoSync cusInfo=new CustomInfoSync(mContext,handler);
			cusInfo.getCustomInfo();
		}
	}

	/*登录完成后同步服务端所有信息
	 */	
	public static void syncAfterLogin(int type) {
		if(Tools.getLogin(mContext) && !MainService.syncnow){
			getInfo = type;
			syncType = 0;
			isSync = true;
			if (HomePageFragment.mHandler != null) {
				Message message = new Message();
				message.what = HomePageFragment.SYNC_DEVICE_START;
				message.arg1 = 2;
				HomePageFragment.mHandler.sendMessage(message);
			}
			new Thread(postSportDataRunnable).start();
		}
	}
	
	static Runnable postSportDataRunnable = new Runnable() {
		@Override
		public void run() {
			SportDataSync sportData=new SportDataSync(mContext, getInfo);
			sportData.postSportData(handler);
		}	
	};	
	
	static Runnable getSportFromCloudRunnable = new Runnable() {
		@Override
		public void run() {
			SportDataSync sportData=new SportDataSync(mContext, getInfo);
			sportData.getSportFromCloud(handler);
		}	
	};	
	
	public static Runnable getNetRankInfoRunnable = new Runnable() {
		@Override
		public void run() {
			RankInfoSync rankInfo=new RankInfoSync(mContext);
			if (RankFragment.handler != null){
				rankInfo.getNetRankInfo(RankFragment.handler);
			}

		}	
	};
	
	public static Runnable postGPSDataRunnable = new Runnable() {
		@Override
		public void run() {
			GPSDataSync gpsData=new GPSDataSync(mContext, getInfo);
			gpsData.postSportData(handler);
		}	
	};
	
	private static void msgInfo() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				HashMap<String, String> params = new HashMap<String, String>();
				String openId;
				if (Tools.getLogin(mContext)) {
					openId = Tools.getOpenId(mContext);
				} else {
					openId = "0";
				}
				params.put("openId", openId);
				new GetDataFromNet(NetMsgCode.ACTION_GET_MSG, ((RunningApp) mContext).GetAppHandler(), params, mContext).execute(NetMsgCode.URL);
			}
		}).start();
	}
}
