package com.zhuoyou.plugin.download;


import android.content.Context;
import android.os.Message;


public class DownloaderThread extends Thread
{
	// 得到下载信息类的个数组成集合
	private Downloader downloader;
	private Context context;
	public DownloaderThread(Downloader downloader,Context context)
	{
		this.downloader = downloader;
		this.context = context;
	}

	@Override
	public void run()
	{
		if(!downloader.init())
		{
			downloader.getmHandler().sendEmptyMessage(4);
			return;
		}
		else
		{
			Message msg = new Message();
			msg.what = 1;
			msg.arg1 = downloader.getNotification_flag();
			downloader.downlaod();			
		}
		
		
	}
}
