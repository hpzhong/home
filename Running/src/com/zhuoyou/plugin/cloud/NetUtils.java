package com.zhuoyou.plugin.cloud;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

public class NetUtils {
	/**
	 * 获取当前的网络状态 -1：没有网络 1：WIFI网络 2：wap网络 3：net网络
	 */
	public static int getAPNType(Context context) {
		int netType = -1;
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo == null) {
			return netType;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE)
		{
			String netString = "";
			if(!TextUtils.isEmpty(networkInfo.getExtraInfo())) {
			    netString = networkInfo.getExtraInfo().toLowerCase();
			}
			if (netString.equals("cmnet")||netString.equals("uninet")) {
				netType = 3;
			} else {
				netType = 2;
			}
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = 1;
		}
		return netType;
	}
}
