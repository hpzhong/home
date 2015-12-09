package com.zhuoyi.appStatistics.thread;

import java.util.ArrayList;

import com.zhuoyi.appStatistics.task.UploadTask;
import com.zhuoyi.appStatistics.utils.LogUtil;

public class TaskThread extends Thread {
	public static final String TAG = "taskThread";

	public static TaskThread mSelf;
	
	private ArrayList<UploadTask> mUploadTaskArray;
	
	public static TaskThread getInstance(UploadTask task){
		if(mSelf == null){
			mSelf = new TaskThread(task);
		}else{
			mSelf.addTask(task);
		}
		
		return mSelf;
	}
	
	TaskThread(UploadTask task){
		mUploadTaskArray = new ArrayList<UploadTask>();
		mUploadTaskArray.add(task);
	}
	
	private void addTask(UploadTask task){
		mUploadTaskArray.add(task);
	}
	
	@Override
	public void run() {
		while(mUploadTaskArray.size() != 0){
			UploadTask task = mUploadTaskArray.remove(0);
			if(task!=null)
			    task.runTask();
		}
		
		mSelf = null;
	}

	@Override
	public synchronized void start() {
		if(isAlive()){
			LogUtil.logI(TAG, "start", "thread already started, return");
			return;
		}
		super.start();
	}
	
	
}
