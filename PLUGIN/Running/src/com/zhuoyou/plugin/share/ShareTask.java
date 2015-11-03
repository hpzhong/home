package com.zhuoyou.plugin.share;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.weibo.net.AsyncWeiboRunner;
import com.weibo.net.AsyncWeiboRunner.RequestListener;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboException;
import com.weibo.net.WeiboParameters;
import com.zhuoyou.plugin.running.R;

public class ShareTask implements Runnable {

	private String mStatus = null;
	private RequestListener mRequestListener = null;
	private Context mContext = null;
	private String mPicPath = null;

	public ShareTask(Context context, String picPath, String status, RequestListener listener) {
		this.mStatus = status;
		this.mRequestListener = listener;
		this.mContext = context;
		this.mPicPath = picPath;
	}

	@Override
	public void run() {
		try {
			Weibo weibo = Weibo.getInstance();
			// 好像没找到只发图片的接口，
			if (!TextUtils.isEmpty(mPicPath) && !TextUtils.isEmpty(mStatus) && !mPicPath.equals("") && !mStatus.equals("")) {
				upload(weibo, Weibo.getAppKey(), mPicPath, mStatus, "", "");
			} else {
				Toast.makeText(mContext, R.string.share_mistake, Toast.LENGTH_SHORT).show();
			}
		} catch (WeiboException e) {
			e.printStackTrace();
		}
	}
	private String upload(Weibo weibo, String source, String file, String status, String lon, String lat) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
		bundle.add("source", source);
		bundle.add("pic", file);
		bundle.add("status", status);
		if (!TextUtils.isEmpty(lon)) {
			bundle.add("lon", lon);
		}
		if (!TextUtils.isEmpty(lat)) {
			bundle.add("lat", lat);
		}
		String rlt = "";
		String url = Weibo.SERVER + "statuses/upload.json";
		AsyncWeiboRunner weiboRunner = new AsyncWeiboRunner(weibo);
		weiboRunner.request(mContext, url, bundle, Utility.HTTPMETHOD_POST, mRequestListener);

		return rlt;
	}
}
