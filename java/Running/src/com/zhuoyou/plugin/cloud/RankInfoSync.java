package com.zhuoyou.plugin.cloud;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.zhuoyou.plugin.rank.RankInfo;
import com.zhuoyou.plugin.running.Tools;
import com.zhuoyou.plugin.selfupdate.TerminalInfo;

public class RankInfoSync {
	private boolean is_debug = true;

	private Context mContext;
	private TerminalInfo phoneInfo;
	HashMap<String, List<RankInfo>> resRankList = new HashMap<String, List<RankInfo>>();

	public RankInfoSync(Context con) {
		mContext = con;
		phoneInfo = TerminalInfo.generateTerminalInfo(mContext.getApplicationContext());
	}

	public void getNetRankInfo(Handler handler) {
		String openId = Tools.getOpenId(mContext);
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("imsi", phoneInfo.getImsi());
		params.put("account", openId);
		new GetDataFromNet(NetMsgCode.getNetRankInfo, handler, params,
				mContext).execute(NetMsgCode.URL);
		
		if (is_debug) {
			Log.d("txhlog", "getNetRankInfo");
		}

	}

}
