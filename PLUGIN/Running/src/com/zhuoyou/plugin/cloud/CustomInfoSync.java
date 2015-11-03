package com.zhuoyou.plugin.cloud;

import java.util.HashMap;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.zhuoyou.plugin.running.PersonalConfig;
import com.zhuoyou.plugin.running.Tools;
import com.zhuoyou.plugin.selfupdate.TerminalInfo;

public class CustomInfoSync {
	private boolean is_debug = true;
	private String TAG = "CustomInfoSync";
	Context mContext;
	TerminalInfo info;
	final int NO_VALUE = 100000;
	PersonalConfig mPersonalConfig;
	Handler hander;

	public CustomInfoSync(Context context,Handler hander) {
		this.mContext = context;
		mPersonalConfig = Tools.getPersonalConfig();
		info = TerminalInfo.generateTerminalInfo(mContext);
		this.hander=hander;
	}

	public void postCustomInfo() {
		HashMap<String, String> params = new HashMap<String, String>();
		String openid = Tools.getOpenId(mContext);
		String username = Tools.getUsrName(mContext);
		if(username.equals("")){
			username=Tools.getLoginName(mContext);
		}
		
		int headId = Tools.getHead(mContext);
		int sex = mPersonalConfig.getSex();
		int birthday = mPersonalConfig.getYear();
		String signature = Tools.getSignature(mContext);
		String likeSportIndex = Tools.getLikeSportsIndex(mContext);
		int weight = mPersonalConfig.getWeight();
		int height = mPersonalConfig.getHeight();
		int step = Tools.getPersonalGoal().mGoalSteps;
		int calorie = Tools.getPersonalGoal().mGoalCalories;
		String qq = "";
		String weibo = "";
		String phone = Tools.getPhoneNum(mContext);
		String email = Tools.getEmail(mContext);
		int proviceIndex = Tools.getProviceIndex(mContext);
		int countyIndex = Tools.getCityIndex(mContext);
		params.put("account", openid);
		params.put("imsi", info.getImsi());
		params.put("name", username);
		params.put("headimgId", Integer.toString(headId));
		params.put("sex", Integer.toString(sex));
		params.put("birthday", Integer.toString(birthday));
		params.put("signature", signature);
		params.put("favoriteSport", likeSportIndex);
		params.put("weight", Integer.toString(weight));
		params.put("height", Integer.toString(height));
		params.put("step", Integer.toString(step));
		params.put("calorie", Integer.toString(calorie));
		params.put("qq", qq);
		params.put("weibo", weibo);
		params.put("phone", phone);
		params.put("email", email);
		params.put("location", Integer.toString(proviceIndex));
		params.put("county", Integer.toString(countyIndex));
		new GetDataFromNet(NetMsgCode.postCustomInfo, hander, params, mContext)
				.execute(NetMsgCode.URL);
		if (is_debug) {
			Log.d("txhlog", "postCustomInfo ");
		}
	}

	public void getCustomInfo() {
		HashMap<String, String> params = new HashMap<String, String>();
		String openId = Tools.getOpenId(mContext);
		params.put("account", openId);
		params.put("imsi", info.getImsi());
		new GetDataFromNet(NetMsgCode.getCustomInfo, hander, params, mContext)
				.execute(NetMsgCode.URL);
		if (is_debug) {
			Log.d("txhlog", "getCustomInfo ");
		}
	}

}
