     /*modification phHe*/

package com.zhuoyi.appStatistics.activity;

import com.zhuoyi.appStatistics.entry.AppStatisticsEntry;
import com.zhuoyi.appStatistics.utils.LogUtil;

import com.zhuoyi.appStatistics.R;
import android.app.Activity;
import android.os.Bundle;

public class TestActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_thread_pool_demo);
	}

	@Override
	protected void onResume() {
		super.onResume();
		/*
		String pkgName = "com.droi.troch.Tydtroch";
		String arry[] = null;
		arry   	= pkgName.split("\\.");    
		String newPkgName = arry[0]+"."+arry[1]+"."+arry[2];*/
		
		
		AppStatisticsEntry entry = AppStatisticsEntry.getInstance(this);
		entry.startAppStatis("testAppid", "testChannelId", "testMarketId");
		LogUtil.logI("test", "onResume", "----------------------------------------------------");
		
		//entry.apkDownSucStatis("testAppid", "testChannelId", "/1/HomeRecommend", newPkgName, "testAppName", 0, 0);
		//entry.apkInstallSucStatis("testAppid", "testChannelId", "/3/SoftRank", newPkgName, "testAppName", 0, 0);
		entry.viewColumnStatis("testAppid", "testChannelId", "testTheme", "testColumn");
		entry.endAppStatis("testAppid", "testChannelId", "testMarketId");
	}
	
}
