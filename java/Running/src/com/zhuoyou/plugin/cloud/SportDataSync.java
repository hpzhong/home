package com.zhuoyou.plugin.cloud;

import java.io.File;
import java.util.HashMap;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;

import com.zhuoyou.plugin.running.HomePageFragment;
import com.zhuoyou.plugin.running.MainService;
import com.zhuoyou.plugin.running.Tools;
import com.zhuoyou.plugin.selfupdate.TerminalInfo;

public class SportDataSync {
	private static final String TAG = "SportDataSync";
	private boolean is_debug = true;
	static Context mContext;
	TerminalInfo info;
	String openid = "";
	private Handler mHandler;
	private Cvshelper mcvs;
	private String path = null;
	int result = 0;
	
	private GPSDataSync gpsSync;

	public SportDataSync(Context context, int type) {
		this.mContext = context;
		openid = Tools.getOpenId(mContext);
		info = TerminalInfo.generateTerminalInfo(mContext);
		mcvs= new Cvshelper(mContext, type);
		gpsSync = new GPSDataSync(context,type);
	}

	public void postSportData(Handler handler) {
		this.mHandler=handler;
		new Thread(upRunnable).start();
		if (is_debug) {
			Log.d("txhlog", "NetMsgCode.postSportInfo");
		}

	}
	
	Runnable upRunnable = new Runnable(){

		@Override
		public void run() {
			path = mcvs.GetDir();
			String up_zip = "Running_up.zip";
//			File up_file = new File(path+"/"+up_zip);
			
			//check ram is enough!
			StatFs sf = new StatFs(mcvs.GetDir());
		     long blockSize = sf.getBlockSize();
		     long freeBlocks = sf.getAvailableBlocks();
		     //MB 
		     long freesize = (freeBlocks * blockSize)/1024 /1024;
		     if(freesize <10){
				return;
		     }
		     
			//new sync ,first we export data,zip file,commit file,then download file.
	        //clear tmp file
			File mfile = new File(mcvs.GetDir());
			deleteSDCardFolder(mfile);
			mcvs.ExportDateCSVBYTYPE(0);
			mcvs.ExportDateCSVBYTYPE(1);
			mcvs.ExportDateCSVBYTYPE(2);
			mcvs.ExportDateCSVBYTYPE(3);
			
			gpsSync.createGSPFile(mcvs.GetDir());
			
			try {
				mcvs.ExportCVSToZip(up_zip);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (HomePageFragment.mHandler != null) {
				Message message = new Message();
				message.what = HomePageFragment.SYNC_DEVICE_PROGRESS;
				message.obj = "30%";
				HomePageFragment.mHandler.sendMessage(message);
			}
			//update zip file
			result = update_file(path,up_zip);
			if(result == 1){//update sucess
				Log.d(TAG,"updagte success");
            	mcvs.UpdataLocalDate();
            	gpsSync.UpdataLocalDate();
            	
            	Message msg = mHandler.obtainMessage();
    			msg.what = NetMsgCode.The_network_link_success;
    			msg.arg1 = NetMsgCode.postSportInfo;
    			mHandler.sendMessage(msg);
            	//clear tmp file
            }else{
				Log.d(TAG,"update failed");
				mHandler.sendEmptyMessage(110011);
            	return;
            }
			
		}
		
	};
	
	/**
	 * delete tmp dir
	 */
	 public void DeleteFile(File file) { 
        if (file.exists() == false) { 
            return; 
        } else { 
            if (file.isFile()) { 
                file.delete(); 
                return; 
            } 
            if (file.isDirectory()) { 
                File[] childFile = file.listFiles(); 
                if (childFile == null || childFile.length == 0) { 
                    file.delete(); 
                    return; 
                } 
                for (File f : childFile) { 
                    DeleteFile(f); 
                } 
                file.delete(); 
            } 
        }
    } 

	 private void deleteSDCardFolder(File dir) {
	       File to = new File(dir.getAbsolutePath() + System.currentTimeMillis());
	       dir.renameTo(to);
	       if (to.isDirectory()) {
	           String[] children = to.list();
	           for (int i = 0; i < children.length; i++) {
	               File temp = new File(to, children[i]);
	               if (temp.isDirectory()) {
	                   deleteSDCardFolder(temp);
	               } else {
	                   boolean b = temp.delete();
	                   if (b == false) {
	                       Log.d("deleteSDCardFolder", "DELETE FAIL");
	                   }
	               }
	           }
	           to.delete();
	       }
	 }
	/*
	 * implement update file
	 */
	private int update_file(String path, String filename) {
		int result = 0;
		HttpConnect httpCon = new HttpConnect();
		String filePath = path + "/" + filename;
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("account", openid);
		result = httpCon.uploadFile(NetMsgCode.UP_URL, params, filePath);
		return result;
	}
	
	Runnable dowmRunnable = new Runnable(){

		@Override
		public void run() {
			path = mcvs.GetDir();
			String down_zip ="Running_down.zip";
			//check ram is enough!
			StatFs sf = new StatFs(mcvs.GetDir());
		     long blockSize = sf.getBlockSize();
		     long freeBlocks = sf.getAvailableBlocks();
		     //MB 
		     long freesize = (freeBlocks * blockSize)/1024 /1024;
		     if(freesize <10){
				return;
		     }
		     
			//download zip file
			result = download_file(path,down_zip);
			Log.d(TAG,"result:"+result);
			if(result == 3){//down load sucess
				Log.d(TAG,"download  success");
				String zip_path = path+"/"+down_zip;
				String out_path = path;
				try {
					mcvs.CVSUnzipFile(zip_path, out_path);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.d(TAG,"download failed");
				}
				if (HomePageFragment.mHandler != null) {
					Message message = new Message();
					message.what = HomePageFragment.SYNC_DEVICE_PROGRESS;
					message.obj = "100%";
					HomePageFragment.mHandler.sendMessage(message);
				}
				mcvs.ImportCSVFile(mContext);
				MainService.getInstance().checkDataBase();
				Message msg = mHandler.obtainMessage();
				msg.what = NetMsgCode.The_network_link_success;
				msg.arg1 = NetMsgCode.getSportInfo;
				mHandler.sendMessage(msg);
			}else{
				Log.d(TAG,"download failed");
				mHandler.sendEmptyMessage(110011);
				return;
			}
		}
		
	};
	
	/*
	 * implement down load file
	 */
	private int download_file(String path,String filename){
		int result  = 0;
		HttpConnect httpCon = new HttpConnect();
        String filePath = path+"/"+filename;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("account", openid);
        result = httpCon.downloadFile(NetMsgCode.DOWN_URL, new HashMap<String, String>(), params, filePath);
        return result ;
	}

	public void getSportFromCloud(Handler handler) {
		// TODO Auto-generated method stub
		this.mHandler=handler;
		new Thread(dowmRunnable).start();
	}

}
