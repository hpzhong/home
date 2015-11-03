package com.zhuoyou.plugin.cloud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.running.RunningItem;
import com.zhuoyou.plugin.running.Tools;
import com.zhuoyou.plugin.selfupdate.TerminalInfo;

public class SportDataSync {
	private boolean is_debug = true;
	static Context mContext;
	TerminalInfo info;
	String openid = "";
	private static ContentResolver mContentResolver;

	public SportDataSync(Context context) {
		this.mContext = context;
		mContentResolver = context.getContentResolver();
		openid = Tools.getOpenId(mContext);
		info = TerminalInfo.generateTerminalInfo(mContext);
	}

	public void postSportData(Handler mHandler) {
		List<RunningItem> sportList = getSportInfo(0);
		List<RunningItem> updateSportList = getSportInfo(2);
		String deleteId = getDeleteSportInfo();
		HashMap<String, String> params = new HashMap<String, String>();
		if (sportList.size() != 0 || updateSportList.size() != 0 || deleteId != null) {
			params.put("imsi", info.getImsi());
			params.put("account", openid);
			params.put(NetMsgCode.insertData, sportDataToJson(sportList));
			params.put(NetMsgCode.updateData, sportDataToJson(updateSportList));
			params.put(NetMsgCode.deleteData, deleteId);
			new GetDataFromNet(NetMsgCode.postSportInfo, mHandler, params,
					mContext).execute(NetMsgCode.URL);
		} else {
			Message msg = new Message();
			msg.what=NetMsgCode.The_network_link_success;
			msg.arg1 = NetMsgCode.postSportInfo;
			msg.obj = sportList;
			mHandler.sendMessage(msg);
		}
		if (is_debug) {
			Log.d("txhlog", "NetMsgCode.postSportInfo");
		}

	}

	public void getSportFromCloud(Handler mHandler) {
		// down cloud data
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("imsi", info.getImsi());
		params.put("account", openid);
		params.put("idList", getSportDataId());
		Log.d("txhlog", " getSportDataId: "+ getSportDataId());
		new GetDataFromNet(NetMsgCode.getSportInfo, mHandler, params, mContext)
				.execute(NetMsgCode.URL);
		if (is_debug) {
			Log.d("txhlog", "GetSportFromCloud ");
		}
	}

	private String sportDataToJson(List<RunningItem> mlist) {
		// TODO Auto-generated method stub
		String jsonresult = "";
		if (openid == null)
			return "";
		try {
			JSONArray jsonarray = new JSONArray();
			for (int i = 0; i < mlist.size(); i++) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("id", mlist.get(i).getID());
				jsonObj.put("accountId", openid);
				jsonObj.put("date", mlist.get(i).getDate());
				jsonObj.put("timeDuration", mlist.get(i).getDuration());
				jsonObj.put("timeStart", mlist.get(i).getStartTime());
				jsonObj.put("timeEnd", mlist.get(i).getEndTime());
				jsonObj.put("steps", mlist.get(i).getSteps());
				jsonObj.put("kilometer", mlist.get(i).getMeter());
				jsonObj.put("calorie", mlist.get(i).getCalories());
				jsonObj.put("weight", mlist.get(i).getmWeight());
				jsonObj.put("bmi", mlist.get(i).getmBmi());
				jsonObj.put("imgUri", mlist.get(i).getmImgUri());
				jsonObj.put("imgExplain", mlist.get(i).getmExplain());
				jsonObj.put("sportsType", mlist.get(i).getSportsType());
				jsonObj.put("type", mlist.get(i).getmType());
				jsonObj.put("imgCloud", mlist.get(i).getImg_cloud());
				jsonObj.put("complete", mlist.get(i).getIsComplete());
				jsonObj.put("statistics", mlist.get(i).getIsStatistics());
				jsonarray.put(jsonObj);
			}
			jsonresult = jsonarray.toString();
			if (is_debug) {
				Log.d("txhlog", "SportDataToJson get json result = " + jsonresult);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonresult;
	}
	
	private List<RunningItem> getSportInfo(int state) {
		List<RunningItem> mysport = new ArrayList<RunningItem>();
		ContentResolver cr = mContext.getContentResolver();
		Cursor c = null;
		c = cr.query(DataBaseContants.CONTENT_URI, new String[] { "_id",
				"date", "time_duration", "time_start", "time_end", "steps",
				"kilometer", "calories", "weight", "bmi", "img_uri",
				"img_explain", "sports_type", "type", "img_cloud", "complete",
				"statistics" }, DataBaseContants.SYNC_STATE + "  = ? ",
				new String[] { Integer.toString(state) }, null);
		while (c.moveToNext()) {
			RunningItem item = new RunningItem(c.getLong(0), c.getString(1),
					c.getString(2), c.getString(3), c.getString(4),
					c.getInt(5), c.getInt(6), c.getInt(7), c.getString(8),
					c.getString(9), c.getString(10), c.getString(11),
					c.getInt(12), c.getInt(13), c.getString(14), c.getInt(15),
					c.getInt(16) == 1 ? true : false);
			mysport.add(item);
		}
		c.close();
		c = null;
		return mysport;
	}

	private String getDeleteSportInfo() {
		String deletesport = null;
		ContentResolver cr = mContext.getContentResolver();
		Cursor c = null;
		c = cr.query(DataBaseContants.CONTENT_DELETE_URI,
				new String[] { "delete_value" }, null, null, null);
		c.moveToNext();
		if (c.getCount() > 0) {
			for (int i = 0; i < c.getCount(); i++) {
				if (i == 0) {
					deletesport = c.getLong(c.getColumnIndex(DataBaseContants.DELETE_VALUE))+"";
				} else {
					deletesport = deletesport+ ","+ c.getLong(c.getColumnIndex(DataBaseContants.DELETE_VALUE));
				}
				c.moveToNext();
			}
		}
		c.close();
		c = null;
		return deletesport;
	}

	private String getSportDataId() {
		String getSportDataId = null;
		ContentResolver cr = mContext.getContentResolver();
		Cursor c = null;
		c = cr.query(DataBaseContants.CONTENT_URI, new String[] { "_id" },
				null, null, null);
		c.moveToNext();
		if (c.getCount() > 0) {
			for (int i = 0; i < c.getCount(); i++) {
				if (i == 0) {
					getSportDataId = c.getLong(c.getColumnIndex(DataBaseContants.ID)) + "";
				} else {
					getSportDataId = getSportDataId+ ","+ c.getLong(c.getColumnIndex(DataBaseContants.ID));
				}
				c.moveToNext();
			}
		}
		c.close();
		c = null;
		return getSportDataId;
	}
	
	public static void insertSportData(Context con, List<RunningItem> sportInfo) {
		try {
			ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
			int count = sportInfo.size();
			for (int i = 0; i < count; i++) {
				RunningItem item = sportInfo.get(i);
				if (item.getIsStatistics()) {
					String day = item.getDate();
					// 检查数据库中是否存在该天的数据
					Cursor c = mContentResolver.query(DataBaseContants.CONTENT_URI, new String[] { DataBaseContants.ID }, 
							DataBaseContants.DATE + "  = ? AND " + DataBaseContants.STATISTICS + " = ? ", new String[] { day, "1" }, null);
					if(c.getCount() <= 0 && c.moveToFirst() ){
						deleteDateInfo(con,c.getLong(c.getColumnIndex(DataBaseContants.ID)));
					} else {		
						ContentProviderOperation op1 = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI)
								.withValue(DataBaseContants.ID, item.getID())
								.withValue(DataBaseContants.DATE, item.getDate())
								.withValue(DataBaseContants.STEPS, item.getSteps())
								.withValue(DataBaseContants.KILOMETER, item.getMeter())
								.withValue(DataBaseContants.CALORIES, item.getCalories())
								.withValue(DataBaseContants.TYPE, item.getmType())
								.withValue(DataBaseContants.COMPLETE, item.getIsComplete())
								.withValue(DataBaseContants.SYNC_STATE, 1)
								.withValue(DataBaseContants.STATISTICS, item.getIsStatistics())
								.withYieldAllowed(true).build();
						operations.add(op1);	
					}
					c.close();
					c = null;
				} else {
					int type = item.getmType();
					if (type == 1) {
						ContentProviderOperation op1 = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI)
								.withValue(DataBaseContants.ID, item.getID())
								.withValue(DataBaseContants.DATE, item.getDate())
								.withValue(DataBaseContants.TIME_START, item.getStartTime())
								.withValue(DataBaseContants.CONF_WEIGHT, item.getmWeight())
								.withValue(DataBaseContants.BMI, item.getmBmi())
								.withValue(DataBaseContants.TYPE, type)
								.withValue(DataBaseContants.SYNC_STATE, 1)
								.withValue(DataBaseContants.STATISTICS, item.getIsStatistics())
								.withYieldAllowed(true).build();
						operations.add(op1);
					} else if (type == 2) {
						if(item.getSportsType() == 0){
							String day = item.getDate();
							String start = item.getStartTime();
							String end = item.getEndTime();
							Cursor c = mContentResolver.query(DataBaseContants.CONTENT_URI, new String[] { DataBaseContants.ID, DataBaseContants.STEPS }, DataBaseContants.DATE
									+ "  = ? AND " + DataBaseContants.TIME_START + " = ? AND "+ DataBaseContants.TIME_END + " = ? AND " + DataBaseContants.SPORTS_TYPE + " = ? AND " + DataBaseContants.STATISTICS + " = ? ", new String[] { day, start, end, "0", "0" }, null);
							if(c.getCount() > 0 && c.moveToFirst()){
								deleteDateInfo(con,c.getLong(c.getColumnIndex(DataBaseContants.ID)));
							}else{
								ContentProviderOperation op1 = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI)
										.withValues(item.toContentValues())
										.withValue(DataBaseContants.ID, item.getID())
										.withValue(DataBaseContants.SYNC_STATE, 1)
										.withYieldAllowed(true).build();
								operations.add(op1);
							}
							c.close();
							c = null;
						}else{
							ContentProviderOperation op1 = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI)
									.withValues(item.toContentValues())
									.withValue(DataBaseContants.ID, item.getID())
									.withValue(DataBaseContants.SYNC_STATE, 1)
									.withYieldAllowed(true).build();
							operations.add(op1);
						}
					} else if (type == 3) {
						ContentProviderOperation op1 = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI)
								.withValue(DataBaseContants.ID, item.getID())
								.withValue(DataBaseContants.DATE, item.getDate())
								.withValue(DataBaseContants.TIME_START, item.getStartTime())
								.withValue(DataBaseContants.IMG_URI, item.getmImgUri())
								.withValue(DataBaseContants.EXPLAIN, item.getmExplain())
								.withValue(DataBaseContants.TYPE, type)
								.withValue(DataBaseContants.SYNC_STATE, 1)
								.withValue(DataBaseContants.STATISTICS, item.getIsStatistics())
								.withYieldAllowed(true).build();
						operations.add(op1);
					} else if (type== 4) {
						ContentProviderOperation op1 = ContentProviderOperation.newInsert(DataBaseContants.CONTENT_URI)
								.withValue(DataBaseContants.ID, item.getID())
								.withValue(DataBaseContants.DATE, item.getDate())
								.withValue(DataBaseContants.TIME_START, item.getStartTime())
								.withValue(DataBaseContants.EXPLAIN, item.getmExplain())
								.withValue(DataBaseContants.TYPE, type)
								.withValue(DataBaseContants.SYNC_STATE, 1)
								.withValue(DataBaseContants.STATISTICS, item.getIsStatistics())
								.withYieldAllowed(true).build();
						operations.add(op1);
					}
				}
			}
			mContentResolver.applyBatch(DataBaseContants.AUTHORITY, operations);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void deleteDateInfo(Context con,long id) {
		ContentValues values = new ContentValues();
		values.put(DataBaseContants.DELETE_VALUE, id);
		con.getContentResolver().insert(DataBaseContants.CONTENT_DELETE_URI, values);
	}
	
	public static void updateSportSyncState(Context con) {
		ContentResolver cr = con.getContentResolver();
		ContentValues updateValues = new ContentValues();
		updateValues.put(DataBaseContants.SYNC_STATE, 1);
		cr.update(DataBaseContants.CONTENT_URI, updateValues, null, null);
	}

}
