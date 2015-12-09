package com.zhuoyou.plugin.cloud;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.zhuoyou.plugin.rank.RankInfo;
import com.zhuoyou.plugin.running.PersonalConfig;
import com.zhuoyou.plugin.running.PersonalGoal;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningItem;
import com.zhuoyou.plugin.running.Tools;

public class GetDataFromNet extends AsyncTask<Object, Object, String> {

	private Handler mHandler = null;
	private OpenUrlGetStyle mUrlGetStyle = null;
	private Context mContext;
	private String postParams = "";
	final int NO_VALUE = 100000;
	private int msgCode;
	HashMap<String, String> params;
	PersonalConfig mPersonalConfig;
	PersonalGoal mPersonalGoal;
	private int[] headIcon;

	public GetDataFromNet(int msgCode, Handler handler,
			HashMap<String, String> map, Context context) {
		mHandler = handler;
		mUrlGetStyle = new OpenUrlGetStyle();
		mContext = context;
		this.msgCode = msgCode;
		this.params = map;
		mPersonalConfig = Tools.getPersonalConfig();
		mPersonalGoal = Tools.getPersonalGoal();
	}

	public GetDataFromNet(int msgCode, Handler handler, Context context) {
		mHandler = handler;
		mUrlGetStyle = new OpenUrlGetStyle();
		mContext = context;
		this.msgCode = msgCode;
		mPersonalConfig = Tools.getPersonalConfig();
		mPersonalGoal = Tools.getPersonalGoal();
	}

	public GetDataFromNet(Handler handler) {
		mHandler = handler;
		mUrlGetStyle = new OpenUrlGetStyle();
		mPersonalConfig = Tools.getPersonalConfig();
		mPersonalGoal = Tools.getPersonalGoal();
	}

	@Override
	protected void onPreExecute() {
		try {
			JSONObject jsObject = new JSONObject();
			jsObject.put("head", buildHeadData());
			jsObject.put("body", buildBodyData());
			postParams = jsObject.toString();

		} catch (JSONException e) {

		}
		Log.i("txhlog", "postParams:" + postParams);
	}

	@Override
	protected String doInBackground(Object... params) {
		// TODO Auto-generated method stub
		String result = null;
		String urlHeader = "";
		urlHeader = (String) params[0];
		try {

			result = mUrlGetStyle.accessNetworkByPost(urlHeader, postParams);

		} catch (IOException e) {

		}

		Log.i("txhlog", "result:" + result);
		return result;
	}

	@Override
	protected void onPostExecute(String result) {
		Message msg = mHandler.obtainMessage();

		if (result == null || result.equals("zero") || !getSUCCESS(result)) {
			msg.what = NetMsgCode.The_network_link_failure;
			mHandler.sendMessage(msg);
			return;
		} else {
			msg.what = NetMsgCode.The_network_link_success;
			switch (msgCode) {
			case NetMsgCode.postCustomInfo:
				Log.i("txhlog", "result:NetMsgCode.postCustomInfo");
				msg.arg1 = NetMsgCode.postCustomInfo;
				break;
			case NetMsgCode.getCustomInfo:
				saveCustInfoToSharePrefer(result);
				msg.arg1 = NetMsgCode.getCustomInfo;
				break;
			case NetMsgCode.postSportInfo:
				postSportInfo(result, msg);
				break;
			case NetMsgCode.getSportInfo:
				try {
					getSportData(result, msg);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case NetMsgCode.getNetRankInfo:
				HashMap<String, List<RankInfo>> list = SplitRankInfoList(result);
				msg.arg1 = NetMsgCode.getNetRankInfo;
				msg.obj = list;
				break;
			default:
				break;
			}
			Log.d("txhlog", "hander sendmes =" + msg.arg1);
			mHandler.sendMessage(msg);
		}

	}

	private HashMap<String, List<RankInfo>> SplitRankInfoList(String result) {
		// TODO Auto-generated method stub
		HashMap<String, List<RankInfo>> resRankList = new HashMap<String, List<RankInfo>>();
		Log.i("txhlog", "result:" + result);
		if (TextUtils.isEmpty(result)) {
			return resRankList;
		}
		try {
			JSONObject object = new JSONObject(result.trim());
			JSONObject body = new JSONObject(object.optString("body"));
			//获得排名的 list
			List<RankInfo> mSportList = new ArrayList();
			headIcon = Tools.headIcon;
			JSONArray jasonArray = new JSONArray(body.optString("sevenDaysStepList"));
			if(jasonArray.length()>0){
				for (int i = 0; i < jasonArray.length(); i++) {
					RankInfo mRankInfo = new RankInfo();
					JSONObject tempJSONObject = jasonArray.getJSONObject(i);
					mRankInfo.setRank(Integer.valueOf(tempJSONObject.optString("count")));
					mRankInfo.setAccountId(tempJSONObject.optString("accountId"));
					int headId=Integer.valueOf(tempJSONObject.optString("headimgId"));
					if(headId==6){
						mRankInfo.setImg(mContext.getResources().getDrawable(R.drawable.logo_default));
					}else{
						mRankInfo.setImg(mContext.getResources().getDrawable(headIcon[headId]));
					}
					mRankInfo.setName(tempJSONObject.optString("name"));
					mRankInfo.setmSteps(tempJSONObject.optString("steps"));
					mSportList.add(mRankInfo);
				}
				resRankList.put("sevenDaysStepList", mSportList);
			}
			
			List<RankInfo> mhSportList = new ArrayList();
			JSONArray hJasonArray = new JSONArray(body.optString("highestStepList"));
			if(hJasonArray.length()>0){
				for (int i = 0; i < hJasonArray.length(); i++) {
					RankInfo mRankInfo = new RankInfo();
					JSONObject tempJSONObject = hJasonArray.getJSONObject(i);
					mRankInfo.setRank(Integer.valueOf(tempJSONObject.optString("count")));
					mRankInfo.setAccountId(tempJSONObject.optString("accountId"));
					int headId=Integer.valueOf(tempJSONObject.optString("headimgId"));
					if(headId==6){
						mRankInfo.setImg(mContext.getResources().getDrawable(R.drawable.logo_default));
					}else{
						mRankInfo.setImg(mContext.getResources().getDrawable(headIcon[headId]));
					}
					mRankInfo.setName(tempJSONObject.optString("name"));
					mRankInfo.setmSteps(tempJSONObject.optString("steps"));
					mhSportList.add(mRankInfo);
				}
				resRankList.put("highestStepList", mhSportList);
			}
			
			//获得自己的排名信息
			
			String sportRanking = body.optString("accountSevenData");
			if(sportRanking!=null){
				List<RankInfo> mySportRanking = new ArrayList();
				JSONObject temObject=new JSONObject(sportRanking);
				RankInfo myRankInfo=new RankInfo();
				myRankInfo.setRank(Integer.valueOf(temObject.optString("count")));
				myRankInfo.setAccountId(temObject.optString("accountId"));
				int headId=Integer.valueOf(temObject.optString("headimgId"));
				if(headId==6){
					myRankInfo.setImg(mContext.getResources().getDrawable(R.drawable.logo_default));
				}else{
					myRankInfo.setImg(mContext.getResources().getDrawable(headIcon[headId]));
				}
				myRankInfo.setName(temObject.optString("name"));
				myRankInfo.setmSteps(temObject.optString("steps"));
				mySportRanking.add(myRankInfo);
				resRankList.put("accountSevenData", mySportRanking);
			}
						
			String hSportRanking = body.optString("accountHighestData");
			if(hSportRanking!=null){
				List<RankInfo> myhSportRanking = new ArrayList();
				JSONObject htemObject=new JSONObject(hSportRanking);
				RankInfo myhRankInfo=new RankInfo();
				myhRankInfo.setRank(Integer.valueOf(htemObject.optString("count")));
				myhRankInfo.setAccountId(htemObject.optString("accountId"));
				int headId=Integer.valueOf(htemObject.optString("headimgId"));
				if(headId==6){
					myhRankInfo.setImg(mContext.getResources().getDrawable(R.drawable.logo_default));
				}else{
					myhRankInfo.setImg(mContext.getResources().getDrawable(headIcon[headId]));
				}
				myhRankInfo.setName(htemObject.optString("name"));
				myhRankInfo.setmSteps(htemObject.optString("steps"));
				myhSportRanking.add(myhRankInfo);
				resRankList.put("accountHighestData", myhSportRanking);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resRankList;
	}

	private void postSportInfo(String result, Message msg) {
		Log.d("txhlog", "postSportInfo");
		int sportresult = -1;
		try {
			JSONObject resObject = new JSONObject(result);
			String sbody = resObject.optString("body");
			JSONObject SportObject = new JSONObject(sbody);
			sportresult = SportObject.optInt(result);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		msg.arg1 = NetMsgCode.postSportInfo;
		msg.obj = sportresult;
	}

	private void getSportData(String result, Message msg) throws JSONException {
		List<RunningItem> mysport = new ArrayList<RunningItem>();
		JSONObject resObject = new JSONObject(result);
		String sbody = resObject.optString("body");
		JSONObject SportBodyObject = new JSONObject(sbody);

		String sportlist = SportBodyObject.optString("sportList");
		Log.d("txhlog", "getSportData:"+sportlist.length());
		JSONArray sport = new JSONArray(sportlist);
		if(sport.length()>0){
			for (int i = 0; i < sport.length(); i++) {
				JSONObject object = sport.getJSONObject(i);
				RunningItem item = new RunningItem(object.optLong("id"),
						object.optString("date"),
						object.optString("timeDuration"),
						object.optString("timeStart"),
						object.optString("timeEnd"), object.optInt("steps"),
						object.optInt("kilometer"), object.optInt("calorie"),
						object.optString("weight"), object.optString("bmi"),
						object.optString("imgUri"),
						object.optString("imgExplain"),
						object.optInt("sportsType"), object.optInt("type"),
						object.optString("imgCloud"),
						object.optInt("complete"),
						object.optBoolean("statistics"));
				mysport.add(item);
			}
		}	
		msg.arg1 = NetMsgCode.getSportInfo;
		msg.obj = mysport;
	}

	private void saveCustInfoToSharePrefer(String result) {
		String josnBody = "";
		String mAccountInfo = "";
		int mJsonResult = 0;
		// Log.i("json","json start");
		try {
			// Josn
			JSONTokener mJsonParser = new JSONTokener(result);
			JSONObject mJson = (JSONObject) mJsonParser.nextValue();
			josnBody = mJson.getString("body");
			// Log.i("json","body = " + josnBody);
			// Body
			JSONTokener mBodyParser = new JSONTokener(josnBody);
			JSONObject mBody = (JSONObject) mBodyParser.nextValue();
			mJsonResult = mBody.getInt("result");
			// Log.i("json","mJsonResult = " + mJsonResult);

			// AccountForm
			mAccountInfo = mBody.getString("accountForm");
			// Log.i("json","mAccountInfo = " + mAccountInfo);
			JSONTokener mAccInfoParser = new JSONTokener(mAccountInfo);
			JSONObject mAccoInfo = (JSONObject) mAccInfoParser.nextValue();

			Tools.setInfoResult(mContext, mJsonResult);
			// name,sex,birthday,weight,height,sportState,step,calorie
			if (0 == mJsonResult) {
				Tools.setUsrName(mContext, mAccoInfo.optString("name", ""));
				Tools.setHead(mContext, mAccoInfo.optInt("headimgId", 6));
				mPersonalConfig.setSex(mAccoInfo.optInt("sex", 0));
				mPersonalConfig.setYear(mAccoInfo.optInt("birthday", 1980));
				Tools.setSignature(mContext,
						mAccoInfo.optString("signature", ""));
				Tools.setLikeSportsIndex(mContext,
						mAccoInfo.optString("favoriteSport", ""));
				mPersonalConfig.setWeight(mAccoInfo.optInt("weight", 65));
				mPersonalConfig.setHeight(mAccoInfo.optInt("height", 172));
				mPersonalGoal.mGoalSteps = mAccoInfo.optInt("step", 7000);
				mPersonalGoal.mGoalCalories = mAccoInfo.optInt("calorie", 200);
				Tools.updatePersonalGoal(mPersonalGoal);
				Tools.setPhoneNum(mContext, mAccoInfo.optString("phone", ""));
				Tools.setEmail(mContext, mAccoInfo.optString("email", ""));
				Tools.setProviceIndex(mContext,
						mAccoInfo.optInt("location", 10000));
				Tools.setCityIndex(mContext, mAccoInfo.optInt("county", 10000));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String buildHeadData() {
		String result = "";
		UUID uuid = UUID.randomUUID();
		Header header = new Header();
		header.setBasicVer((byte) 1);
		header.setLength(84);
		header.setType((byte) 1);
		header.setReserved((short) 0);
		header.setFirstTransaction(uuid.getMostSignificantBits());
		header.setSecondTransaction(uuid.getLeastSignificantBits());
		header.setMessageCode(msgCode);
		result = header.toString();
		return result;
	}

	public String buildBodyData() {
		JSONObject jsonObjBody = new JSONObject();
		try {
			if (params != null && params.size() > 0) {
				for (String key : params.keySet()) {
					jsonObjBody.put(key, params.get(key));
				}
			}

			// jsonObjBody.put("imsi", "1451654");
			// jsonObjBody.put("account", "1011");

			return jsonObjBody.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
	}

	public boolean getSUCCESS(String result) {
		int netResult = -1;
		try {

			JSONObject object = new JSONObject(result);
			String bodyString = object.getString("body");
			JSONObject bodyObject = new JSONObject(bodyString);
			netResult = bodyObject.optInt("result", -1);

		} catch (JSONException e) {
			netResult = -1;
		}
		if (netResult == 0) {
			return true;
		}
		return false;
	}

}
