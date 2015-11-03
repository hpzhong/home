package com.zhuoyou.plugin.cloud;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.HashMap;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.zhuoyou.plugin.running.PersonalConfig;
import com.zhuoyou.plugin.running.Tools;
import com.zhuoyou.plugin.selfupdate.TerminalInfo;

public class CustomInfoSync {
	private boolean is_debug = true;
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
		String weight = mPersonalConfig.getWeight();
		int height = mPersonalConfig.getHeight();
		int step = Tools.getPersonalGoal().mGoalSteps;
		int calorie = Tools.getPersonalGoal().mGoalCalories;
		String qq = "";
		String weibo = "";
		String phone = Tools.getPhoneNum(mContext);
		String email = Tools.getEmail(mContext);
		String uploadBuffer = "";
		if (headId == 10000) {
			uploadBuffer = creatHeadData("/Running/download/custom");
		} else if (headId == 1000) {
			uploadBuffer = creatHeadData("/Running/download/logo");
		}
		int proviceIndex = Tools.getProviceIndex(mContext);
		int countyIndex = Tools.getCityIndex(mContext);
		String consigneeName = Tools.getConsigneeName(mContext);
		String consigneePhone = Tools.getConsigneePhone(mContext);
		String consigneeLocation = Tools.getConsigneeAddress(mContext);
		params.put("account", openid);
		params.put("imsi", info.getImsi());
		params.put("name", username);
		params.put("headimgId", Integer.toString(headId));
		params.put("sex", Integer.toString(sex));
		params.put("birthday", Integer.toString(birthday));
		params.put("signature", signature);
		params.put("favoriteSport", likeSportIndex);
		params.put("weight", weight.split("\\.")[0]);
		if (weight.split("\\.").length < 2)
			params.put("weightN", "0");
		else
			params.put("weightN", weight.split("\\.")[1]);
		params.put("height", Integer.toString(height));
		params.put("step", Integer.toString(step));
		params.put("calorie", Integer.toString(calorie));
		params.put("qq", qq);
		params.put("weibo", weibo);
		params.put("phone", phone);
		params.put("email", email);
		params.put("location", Integer.toString(proviceIndex));
		params.put("county", Integer.toString(countyIndex));
		params.put("headImg", uploadBuffer);
		params.put("consigneeName", consigneeName);
		params.put("consigneePhone", consigneePhone);
		params.put("consigneeLocation", consigneeLocation);
		new GetDataFromNet(NetMsgCode.postCustomInfo, hander, params, mContext)
				.execute(NetMsgCode.URL);
		if (is_debug) {
			Log.d("txhlog", "postCustomInfo ");
		}
	}

	private String creatHeadData(String path) {
		String uploadBuffer = "";
		try {
			String srcUrl = Tools.getSDPath();
			FileInputStream fis = new FileInputStream(srcUrl + path);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int count = 0;
			while ((count = fis.read(buffer)) >= 0) {
				baos.write(buffer, 0, count);
			}
			uploadBuffer = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT)); // 进行Base64编码
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return uploadBuffer;
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
