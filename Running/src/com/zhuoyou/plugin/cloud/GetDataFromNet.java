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

import com.zhuoyou.plugin.action.ActParagraph;
import com.zhuoyou.plugin.action.ActionInfo;
import com.zhuoyou.plugin.action.ActionListItemInfo;
import com.zhuoyou.plugin.action.ActionPannelItemInfo;
import com.zhuoyou.plugin.action.ActionRankingItemInfo;
import com.zhuoyou.plugin.action.ActionWelcomeInfo;
import com.zhuoyou.plugin.action.AppInitForAction;
import com.zhuoyou.plugin.action.MessageInfo;
import com.zhuoyou.plugin.firmware.Firmware;
import com.zhuoyou.plugin.info.ImageAsynTask;
import com.zhuoyou.plugin.rank.RankInfo;
import com.zhuoyou.plugin.running.PersonalConfig;
import com.zhuoyou.plugin.running.PersonalGoal;
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
			case NetMsgCode.getNetRankInfo:
				HashMap<String, List<RankInfo>> list = SplitRankInfoList(result);
				msg.arg1 = NetMsgCode.getNetRankInfo;
				msg.obj = list;
				break;

			//add by zhouzhongbo@20150129 for action module	start
			case NetMsgCode.APP_RUN_ACTION_INIT:
				AppInitForAction mm = SplitActionInitDate(result);
				msg.arg1 = NetMsgCode.APP_RUN_ACTION_INIT;
				msg.obj = mm;
				break;
				
			case NetMsgCode.ACTION_GET_MSG:
				List<MessageInfo> msglist = SpiltMsg(result);
				msg.arg1 = NetMsgCode.ACTION_GET_MSG;
				msg.obj = msglist;
				break;

			case NetMsgCode.ACTION_JOIN:
				
				msg.arg1 = NetMsgCode.ACTION_JOIN;
				//here we set 0 as sucess
				msg.obj = 0;
				break;

			case NetMsgCode.ACTION_GET_IDINFO:
				Log.d("zzb","ACTION_GET_IDINFO:result = "+result);
				ActionInfo actioninfo = SpiltActionInfo(result);
				msg.arg1 = NetMsgCode.ACTION_GET_IDINFO;
				msg.obj = actioninfo;
				break;
			case NetMsgCode.ACTION_GET_REFRESHPAGE:
			case NetMsgCode.ACTION_GET_NEXTPAGE:
				 List<ActionListItemInfo> mactionlist = SpiltActionListitem(result);
					msg.arg1 = msgCode;
					msg.obj = mactionlist;
				break;
			//add by zhouzhongbo@20150129 for action module	end
			case NetMsgCode.FIRMWARE_GET_VERSION:
				Firmware ware = getFirmware(result);
				msg.obj= ware;
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
			String sevenList = body.optString("sevenDaysStepList");
			if (sevenList != null && !sevenList.equals("")) {
				List<RankInfo> mSportList = new ArrayList();
				JSONArray jasonArray = new JSONArray(sevenList);
				if(jasonArray != null && jasonArray.length()>0){
					for (int i = 0; i < jasonArray.length(); i++) {
						RankInfo mRankInfo = new RankInfo();
						JSONObject tempJSONObject = jasonArray.getJSONObject(i);
						mRankInfo.setRank(Integer.valueOf(tempJSONObject.optString("count")));
						mRankInfo.setAccountId(tempJSONObject.optString("accountId"));
						int headId=Integer.valueOf(tempJSONObject.optString("headimgId"));
						mRankInfo.setmImgId(headId);
						mRankInfo.setName(tempJSONObject.optString("name"));
						mRankInfo.setmSteps(tempJSONObject.optString("steps"));
						mRankInfo.setHeadUrl(tempJSONObject.optString("headImgUrl"));
						mSportList.add(mRankInfo);
					}
					resRankList.put("sevenDaysStepList", mSportList);
				}
			}	
			
			String mouthList = body.optString("monthStepList");
			if (mouthList != null && !mouthList.equals("")) {
				List<RankInfo> mMouthSportList = new ArrayList();
				JSONArray mjasonArray = new JSONArray(mouthList);
				if(mjasonArray != null && mjasonArray.length()>0){
					for (int i = 0; i < mjasonArray.length(); i++) {
						RankInfo mRankInfo = new RankInfo();
						JSONObject tempJSONObject = mjasonArray.getJSONObject(i);
						mRankInfo.setRank(Integer.valueOf(tempJSONObject.optString("count")));
						mRankInfo.setAccountId(tempJSONObject.optString("accountId"));
						int headId=Integer.valueOf(tempJSONObject.optString("headimgId"));
						mRankInfo.setmImgId(headId);
						mRankInfo.setName(tempJSONObject.optString("name"));
						mRankInfo.setmSteps(tempJSONObject.optString("steps"));
						mRankInfo.setHeadUrl(tempJSONObject.optString("headImgUrl"));
						mMouthSportList.add(mRankInfo);
					}
					resRankList.put("monthStepList", mMouthSportList);
				}
			}
			
			String highestList = body.optString("highestStepList");
			if (highestList != null && !highestList.equals("")) {
				List<RankInfo> mhSportList = new ArrayList();
				JSONArray hJasonArray = new JSONArray(highestList);
				if(hJasonArray != null && hJasonArray.length()>0){
					for (int i = 0; i < hJasonArray.length(); i++) {
						RankInfo mRankInfo = new RankInfo();
						JSONObject tempJSONObject = hJasonArray.getJSONObject(i);
						mRankInfo.setRank(Integer.valueOf(tempJSONObject.optString("count")));
						mRankInfo.setAccountId(tempJSONObject.optString("accountId"));
						int headId=Integer.valueOf(tempJSONObject.optString("headimgId"));
						mRankInfo.setmImgId(headId);
						mRankInfo.setName(tempJSONObject.optString("name"));
						mRankInfo.setmSteps(tempJSONObject.optString("steps"));
						mRankInfo.setHeadUrl(tempJSONObject.optString("headImgUrl"));
						mhSportList.add(mRankInfo);
					}
					resRankList.put("highestStepList", mhSportList);
				}
			}
			
			//获得自己的排名信息
			String sportRanking = body.optString("accountSevenData");
			if(sportRanking != null && !sportRanking.equals("")){
				List<RankInfo> mySportRanking = new ArrayList();
				JSONObject temObject=new JSONObject(sportRanking);
				RankInfo myRankInfo=new RankInfo();
				myRankInfo.setRank(Integer.valueOf(temObject.optString("count")));
				myRankInfo.setAccountId(temObject.optString("accountId"));
				int headId=Integer.valueOf(temObject.optString("headimgId"));
				myRankInfo.setmImgId(headId);
				myRankInfo.setName(temObject.optString("name"));
				myRankInfo.setmSteps(temObject.optString("steps"));
				myRankInfo.setHeadUrl(temObject.optString("headImgUrl"));
				mySportRanking.add(myRankInfo);
				resRankList.put("accountSevenData", mySportRanking);
			}
			
			String sportRankingM = body.optString("accountMonthData");
			if(sportRankingM != null && !sportRankingM.equals("")){
				List<RankInfo> mySportRanking = new ArrayList();
				JSONObject temObject=new JSONObject(sportRankingM);
				RankInfo myRankInfo=new RankInfo();
				myRankInfo.setRank(Integer.valueOf(temObject.optString("count")));
				myRankInfo.setAccountId(temObject.optString("accountId"));
				int headId=Integer.valueOf(temObject.optString("headimgId"));
				myRankInfo.setmImgId(headId);
				myRankInfo.setName(temObject.optString("name"));
				myRankInfo.setmSteps(temObject.optString("steps"));
				myRankInfo.setHeadUrl(temObject.optString("headImgUrl"));
				mySportRanking.add(myRankInfo);
				resRankList.put("accountMonthData", mySportRanking);
			}
						
			String hSportRanking = body.optString("accountHighestData");
			if(hSportRanking != null && !hSportRanking.equals("")){
				List<RankInfo> myhSportRanking = new ArrayList();
				JSONObject htemObject=new JSONObject(hSportRanking);
				RankInfo myhRankInfo=new RankInfo();
				myhRankInfo.setRank(Integer.valueOf(htemObject.optString("count")));
				myhRankInfo.setAccountId(htemObject.optString("accountId"));
				int headId=Integer.valueOf(htemObject.optString("headimgId"));
				myhRankInfo.setmImgId(headId);
				myhRankInfo.setName(htemObject.optString("name"));
				myhRankInfo.setmSteps(htemObject.optString("steps"));
				myhRankInfo.setHeadUrl(htemObject.optString("headImgUrl"));
				myhSportRanking.add(myhRankInfo);
				resRankList.put("accountHighestData", myhSportRanking);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resRankList;
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
				int headId = mAccoInfo.optInt("headimgId", 6);
				String headImgUrl = mAccoInfo.optString("headImgUrl", "");
				if (!headImgUrl.equals("") && headImgUrl.startsWith("http:")) {
					if (headId == 10000) {
						new ImageAsynTask().execute(headImgUrl, "custom");
					} else if (headId == 1000) {
						new ImageAsynTask().execute(headImgUrl, "logo");
					}
				}
				Tools.setUsrName(mContext, mAccoInfo.optString("name", ""));
				Tools.setHead(mContext, headId);
				mPersonalConfig.setSex(mAccoInfo.optInt("sex", 0));
				mPersonalConfig.setYear(mAccoInfo.optInt("birthday", 1990));
				Tools.setSignature(mContext,
						mAccoInfo.optString("signature", ""));
				Tools.setLikeSportsIndex(mContext,
						mAccoInfo.optString("favoriteSport", ""));
				String weight = mAccoInfo.optInt("weight", 75) + "." + mAccoInfo.optString("weightN", "0");
				mPersonalConfig.setWeight(weight);
				mPersonalConfig.setHeight(mAccoInfo.optInt("height", 180));
				mPersonalGoal.mGoalSteps = mAccoInfo.optInt("step", 7000);
				mPersonalGoal.mGoalCalories = mAccoInfo.optInt("calorie", 200);
				Tools.updatePersonalGoal(mPersonalGoal);
				Tools.updatePersonalConfig(mPersonalConfig);
				Tools.setPhoneNum(mContext, mAccoInfo.optString("phone", ""));
				Tools.setEmail(mContext, mAccoInfo.optString("email", ""));
				Tools.setProviceIndex(mContext, mAccoInfo.optInt("location", 10000));
				Tools.setCityIndex(mContext, mAccoInfo.optInt("county", 10000));
				Tools.saveConsigneeInfo(mAccoInfo.optString("consigneeName", ""), mAccoInfo.optString("consigneePhone", ""), mAccoInfo.optString("consigneeLocation", ""));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/*
	 * decode app init for action
	 * zhouzhongbo@20150128
	 */
	private AppInitForAction SplitActionInitDate(String result) {
		AppInitForAction mappinitforaction = new AppInitForAction();
		try {
			JSONObject resObject = new JSONObject(result);
			String sbody=resObject.optString("body");
			JSONObject AppInitObject  = new JSONObject(sbody);
			//here decode welcome page data
			String openAppActInfo =AppInitObject.optString("openAppAct");
			if (openAppActInfo != null && !openAppActInfo.equals("")) {
				JSONObject OpenAppActObject  = new JSONObject(openAppActInfo);
				ActionWelcomeInfo mactionWelcomeinfo = new ActionWelcomeInfo(OpenAppActObject.optString("imgUrl"), OpenAppActObject.optInt("id"));
				mappinitforaction.SetWelcomeInfo(mactionWelcomeinfo);
			}
			
			//here decode msg data
			String MsgInfo =AppInitObject.optString("msgs");
			if (MsgInfo != null && !MsgInfo.equals("")) {
				JSONArray MsgList = new JSONArray(MsgInfo);	
				for (int i = 0; i < MsgList.length(); i++) {
					JSONObject object = MsgList.getJSONObject(i);						
					MessageInfo msg_item =  new MessageInfo(object.optInt("noticeId"),object.optString("content"),object.optInt("activityId"),object.optInt("type"));
					mappinitforaction.AddMsgItem(msg_item);
				}
			}
						
			//here decode action list info
			String ActsInfo =AppInitObject.optString("acts");
			if (ActsInfo != null && !ActsInfo.equals("")) {
				JSONArray ActsList = new JSONArray(ActsInfo);
				
				for (int i = 0; i < ActsList.length(); i++) {
					JSONObject object = ActsList.getJSONObject(i);
					ActionListItemInfo maction_item =  new ActionListItemInfo(object.optInt("actId"),
																				object.optString("title"),
																				object.optString("startTime"),
																				object.optString("endTime"),
																				object.optString("curTime"),
																				object.optString("num"),
																				object.optInt("flag"),
																				object.optInt("top"),
																				object.optString("imgUrl"));
					mappinitforaction.AddActionListItem(maction_item);
				}

			}					
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return mappinitforaction;
	}
	
	/*
	 * spilt msg list from result
	 * zhouzhongbo@20150128
	 */
	private  List<MessageInfo>  SpiltMsg(String result){
		List<MessageInfo> msgList = new ArrayList<MessageInfo>();
		try {
			JSONObject resObject = new JSONObject(result);
			String sbody=resObject.optString("body");
			JSONObject MsgObject  = new JSONObject(sbody);
			//here decode welcome page data
			String msglist =MsgObject.optString("msgs");
			if (msglist != null && !msglist.equals("")) {
				JSONArray MsgList = new JSONArray(msglist);					
				for (int i = 0; i < MsgList.length(); i++) {
					JSONObject object = MsgList.getJSONObject(i);						
					MessageInfo msg_item =  new MessageInfo(object.optInt("noticeId"),object.optString("content"),object.optInt("activityId"),object.optInt("type"));
					msgList.add(msg_item);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return msgList;
	}
	
	
	/*
	 * spilt action info from result
	 * zhouzhongbo@20150128
	 */
	private ActionInfo SpiltActionInfo(String result){
		ActionInfo mActioninfo = new ActionInfo();
		
		try {
			JSONObject resObject = new JSONObject(result);
			String sbody=resObject.optString("body");
			JSONObject MsgObject  = new JSONObject(sbody);
			//here decode welcome page data
			String pannels =MsgObject.optString("pannels");
			if (pannels != null && !pannels.equals("")) {
				JSONArray PannelList = new JSONArray(pannels);
				for (int i = 0; i < PannelList.length(); i++) {
					ActionPannelItemInfo mpannelitem = new ActionPannelItemInfo();

					JSONObject object = PannelList.getJSONObject(i);

					mpannelitem.SetPannelTitle(object.optString("pannelTitle"));
					String paragraphlist = object.optString("actParagraph");

					if(paragraphlist != null && !paragraphlist.equals("")){
						JSONArray ParagraphList = new JSONArray(paragraphlist);
						for (int j = 0; j < ParagraphList.length(); j++) {
							JSONObject paragraph_object = ParagraphList.getJSONObject(j);
							ActParagraph mactparagraph = new ActParagraph(paragraph_object.optString("description"),
																			paragraph_object.optInt("paragraphNum"),
																			paragraph_object.optString("img"),
																			paragraph_object.optString("content"));
							mpannelitem.AddPannelParagraph(mactparagraph);
						}
					}
					mActioninfo.AddPannel(mpannelitem);
				}
			}
			
			
			String ranklist =MsgObject.optString("ranking");
			if (ranklist != null && !ranklist.equals("")) {
				JSONArray RankList = new JSONArray(ranklist);					
				for (int i = 0; i < RankList.length(); i++) {
					JSONObject object = RankList.getJSONObject(i);
					ActionRankingItemInfo ranking_item =  new ActionRankingItemInfo(object.optInt("count"),
																					object.optString("accountId"),
																					object.optInt("steps"),
																					object.optInt("headimgId"),
																					object.optString("name"),
																					object.optString("headImgUrl"));
					mActioninfo.AddRank(ranking_item);
				}
			}
			
			
			
			String myranking =MsgObject.optString("myRanking");
			if (myranking != null && !myranking.equals("")) {
				JSONObject MyRanking  = new JSONObject(myranking);
				mActioninfo.SetMyRankInfo(MyRanking.optInt("count"),
						MyRanking.optString("accountId"),
						MyRanking.optInt("steps"),
						MyRanking.optInt("headimgId"),
						MyRanking.optString("name"),
						MyRanking.optString("headImgUrl")
						);
			}
			
			String is_join =MsgObject.optString("flag");
			if (is_join != null && !is_join.equals("")) {
				mActioninfo.SetJoinFlag(Integer.valueOf(is_join));
			}			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return mActioninfo;
	}
	
	
	/*
	 * spilt next page date ,list data of ActionListItemInfo
	 * 
	 */
	private List<ActionListItemInfo> SpiltActionListitem(String result){
		List<ActionListItemInfo> actionitemlist = new ArrayList<ActionListItemInfo>();
		try {
			JSONObject resObject = new JSONObject(result);
			String sbody=resObject.optString("body");
			JSONObject Actionlist  = new JSONObject(sbody);
			String ActionlistItem =Actionlist.optString("acts");
			if (ActionlistItem != null && !ActionlistItem.equals("")) {
				JSONArray listitem = new JSONArray(ActionlistItem);
				
				for (int i = 0; i < listitem.length(); i++) {
					JSONObject object = listitem.getJSONObject(i);
					actionitemlist.add(new ActionListItemInfo(object.optInt("actId"),
							object.optString("title"),
							object.optString("startTime"),
							object.optString("endTime"),
							object.optString("curTime"),
							object.optString("num"),
							object.optInt("flag"),
							object.optInt("top"),
							object.optString("imgUrl")));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return actionitemlist;
	}
	
	private Firmware getFirmware(String result) {
		Firmware ware = new Firmware();
		try {
			JSONObject resObject = new JSONObject(result);
			String sbody = resObject.optString("body");
			JSONObject bodyObject = new JSONObject(sbody);
			String fwString = bodyObject.optString("bluetooth");
			JSONObject firmObject = new JSONObject(fwString);
			ware.setName(firmObject.optString("packageName"));
			ware.setContent(firmObject.optString("content"));
			ware.setCurrentVer(firmObject.optString("versionCode"));
			ware.setDescription(firmObject.optString("description"));
			ware.setFileUrl(firmObject.optString("fileUrl"));
			ware.setTitle(firmObject.optString("title"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ware;
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
