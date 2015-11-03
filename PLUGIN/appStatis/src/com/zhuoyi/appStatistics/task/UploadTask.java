package com.zhuoyi.appStatistics.task;

import com.zhuoyi.appStatistics.service.StatisService;
import com.zhuoyi.appStatistics.thread.TaskThread;
import com.zhuoyi.appStatistics.utils.LogUtil;

import android.os.Handler;

public abstract class UploadTask {
	public static final String TAG = "uploadTask";
	
	private static int taskCount = 0;
	
	private Handler mHandler;
	
	UploadTask(Handler handler){
		mHandler = handler;
	}
	
	public void start(){
		taskCount ++;
		TaskThread.getInstance(this).start();
	}
	
	protected int getCurrTaskCount(){
		LogUtil.logI(TAG, "getCurrTaskCount", "current task num: " + taskCount);
		return taskCount;
	}
	
	public void runTask(){
		run();
		taskCount--;
	}
	
	protected void stopService(){
		mHandler.sendEmptyMessage(StatisService.MSG_STOP_SERVICE);
	}
	
	protected abstract void run();
}
