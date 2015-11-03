//package com.zhuoyou.plugin.database;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.DataSetObservable;
//import android.database.DataSetObserver;
//import android.database.sqlite.SQLiteDatabase;
//import android.util.Log;
//
//import com.zhuoyou.plugin.running.PersonalConfig;
//import com.zhuoyou.plugin.running.PersonalGoal;
//import com.zhuoyou.plugin.running.RunningApp;
//import com.zhuoyou.plugin.running.RunningDate;
//import com.zhuoyou.plugin.running.RunningItem;
//import com.zhuoyou.plugin.running.Tools;
//
//public class DataUtils {
//	private static DataUtils mInstance = null;
//	private DBOpenHelper mDBOpenHelper;
//	private Context mCtx = RunningApp.getInstance().getApplicationContext();
//	private DataSetObservable mDataSetObservable = new DataSetObservable();
//
//	public static DataUtils getInstance() {
//		if (mInstance == null) {
//			mInstance = new DataUtils();
//		}
//		return mInstance;
//	}
//
//	public DataUtils() {
//		mDBOpenHelper = new DBOpenHelper(mCtx);
//		
//		//测试使用：查询今天是否有数据，没有则插入
//		//testInsertToday();
//		
//	}
//
//	public void registerDataSetObserver(DataSetObserver observer) {
//		mDataSetObservable.registerObserver(observer);
//	}
//
//	public void unregisterDataSetObserver(DataSetObserver observer) {
//		mDataSetObservable.unregisterObserver(observer);
//	}
//
//	/**
//	 * Notifies the attached observers that the underlying data has been changed
//	 * and any View reflecting the data set should refresh itself.
//	 */
//	public void notifyDataSetChanged() {
//		mDataSetObservable.notifyChanged();
//	}
//
//	/******************************************************************************
//	 * TABLE DATA START
//	 *******************************************************************************/
//	/**
//	 * 唯一插入接口，没有其它
//	 * 
//	 * @param item
//	 */
////	public synchronized void item_insert(RunningItem item) {
////		SQLiteDatabase db = getWritableDatabase();
////		ContentValues cv = item.toContentValues();
////		db.insert(DataBaseContants.TABLE_DATA_NAME, null, cv);
////		db.close();
////		notifyDataSetChanged();
////	}
//
//	/**
//	 * 查询改天的详情
//	 * 
//	 * @param query_day
//	 * @return
//	 */
////	public synchronized List<RunningItem> item_query_from_day(String query_day) {
////		List<RunningItem> list = new ArrayList<RunningItem>();
////
////		SQLiteDatabase db = getReadDatabase();
////		StringBuilder sb = new StringBuilder();
////		sb.append("SELECT date , time_duration , time_start , time_end , steps, kilometer, calories FROM data WHERE date = '" + query_day + "'");
////		Cursor cursor = db.rawQuery(sb.toString(), null);
////		if (cursor != null && cursor.moveToFirst()) {
////			do {
////				RunningItem data = new RunningItem();
////				String day = cursor.getString(cursor.getColumnIndex(DataBaseContants.DATE));
////				if (day == null || day.equals("") || day.equals("null")) {
////					data.setDate(query_day);
////				} else {
////					data.setDate(day);
////				}
////				String duration = cursor.getString(cursor.getColumnIndex(DataBaseContants.TIME_DURATION));
////				if (duration == null || duration.equals("") || duration.equals("null")) {
////					data.setDuration("");
////				} else {
////					data.setDuration(duration);
////				}
////				String start = cursor.getString(cursor.getColumnIndex(DataBaseContants.TIME_START));
////				if (start == null || start.equals("") || start.equals("null")) {
////					data.setStartTime("");
////				} else {
////					data.setStartTime(start);
////				}
////				String end = cursor.getString(cursor.getColumnIndex(DataBaseContants.TIME_END));
////				if (end == null || end.equals("") || end.equals("null")) {
////					data.setEndTime("");
////				} else {
////					data.setEndTime(end);
////				}
////				data.setSteps(cursor.getInt(cursor.getColumnIndex(DataBaseContants.STEPS)));
////				String k = cursor.getString(cursor.getColumnIndex(DataBaseContants.KILOMETER));
////				if (k != null) {
////					data.setKilometer(Float.parseFloat(k));
////				} else {
////					data.setKilometer(0.0F);
////				}
////				data.setCalories(cursor.getInt(cursor.getColumnIndex(DataBaseContants.CALORIES)));
////				list.add(data);
////			} while (cursor.moveToNext());
////		}
////		cursor.close();
////		db.close();
////		return list;
////	}
//
//	/**
//	 * 通过日期查询改天的数据项之和
//	 * 
//	 * @param query_day
//	 * @return
//	 */
////	public synchronized RunningDate item_query_day(String query_day) {
////		RunningDate data = new RunningDate();
////
////		SQLiteDatabase db = getReadDatabase();
////		StringBuilder sb = new StringBuilder();
////		sb.append("SELECT date , SUM(steps), SUM(kilometer), SUM(calories) FROM data WHERE date = '" + query_day + "'");
////		Cursor cursor = db.rawQuery(sb.toString(), null);
////		if (cursor != null && cursor.moveToFirst()) {
////			do {
////				String day = cursor.getString(cursor.getColumnIndex(DataBaseContants.DATE));
////				if(day == null){
////					break;
////				}
////				if (day == null || day.equals("") || day.equals("null")) {
////					data.setDate(query_day);
////				} else {
////					data.setDate(day);
////				}
////				data.setSteps(cursor.getInt(cursor.getColumnIndex("SUM(steps)")));
////				String k = cursor.getString(cursor.getColumnIndex("SUM(kilometer)"));
////				if (k != null) {
////					data.setKilometer(Float.parseFloat(k));
////				} else {
////					data.setKilometer(0.0F);
////				}
////				data.setCalories(cursor.getInt(cursor.getColumnIndex("SUM(calories)")));
////			} while (cursor.moveToNext());
////		}
////		cursor.close();
////		db.close();
////		return data;
////	}
//
//	public void item_query() {
//
//	}
//
//	public void item_update() {
//
//	}
//
//	/******************************************************************************
//	 * TABLE DATA END
//	 *******************************************************************************/
//
//	/******************************************************************************
//	 * PM25 START
//	 *******************************************************************************/
//	/**
//	 * 查询当天的PM
//	 */
////	public synchronized void PM25_insert(String today, int pm25) {
////		SQLiteDatabase db = getWritableDatabase();
////		ContentValues cv = new ContentValues();
////		cv.put(DataBaseContants.DATE, today);
////		cv.put(DataBaseContants.WEATHER_PM25, pm25);
////		db.insert(DataBaseContants.TABLE_WEATHER_NAME, null, cv);
////		db.close();
////		notifyDataSetChanged();
////	}
////
////	/**
////	 * 查询改天的PM25的值
////	 * 
////	 * @param query_day
////	 * @return
////	 */
////	public synchronized int PM25_query(String query_day) {
////		int pm25 = 0;
////		SQLiteDatabase db = getReadDatabase();
////		StringBuilder sb = new StringBuilder();
////		sb.append("SELECT pm25 FROM weather WHERE date = '" + query_day + "'");
////		Cursor cursor = db.rawQuery(sb.toString(), null);
////		if (cursor != null && cursor.moveToFirst()) {
////			pm25 = cursor.getInt(cursor.getColumnIndex(DataBaseContants.WEATHER_PM25));
////		}
////		cursor.close();
////		db.close();
////		return pm25;
////	}
//
//	/******************************************************************************
//	 * PM25 END
//	 *******************************************************************************/
//
////	/******************************************************************************
////	 * PERSON GOAL START
////	 *******************************************************************************/
////	public synchronized PersonalGoal goal_insert(PersonalGoal goal) {
////		SQLiteDatabase db = getWritableDatabase();
////		ContentValues cv = new ContentValues();
////		cv.put(DataBaseContants.GOAL_STEPS, goal.mGoalSteps);
////		cv.put(DataBaseContants.GOAL_CALORIES, goal.mGoalCalories);
////		long id = db.insert(DataBaseContants.TABLE_GOAL_NAME, null, cv);
////		db.close();
////		Log.i("gchk", "goal_insert id =" + id);
////		goal.id = id;
////		return goal;
////	}
////
////	public synchronized PersonalGoal goal_query() {
////		PersonalGoal goal = null;
////		SQLiteDatabase db = getReadDatabase();
////		StringBuilder sb = new StringBuilder();
////		sb.append("SELECT _id , goal_steps , goal_calories FROM goal ");
////		Cursor cursor = db.rawQuery(sb.toString(), null);
////		if (cursor != null && cursor.getCount() > 0) {
////			cursor.moveToFirst();
////			goal = new PersonalGoal();
////			goal.id = cursor.getInt(cursor.getColumnIndex(DataBaseContants.ID));
////			goal.mGoalSteps = cursor.getInt(cursor.getColumnIndex(DataBaseContants.GOAL_STEPS));
////			goal.mGoalCalories = cursor.getInt(cursor.getColumnIndex(DataBaseContants.GOAL_CALORIES));
////		}
////		cursor.close();
////		db.close();
////		return goal;
////	}
////
////	public synchronized void goal_update(PersonalGoal goal) {
////		SQLiteDatabase db = getReadDatabase();
////		ContentValues cv = new ContentValues();
////		cv.put(DataBaseContants.GOAL_STEPS, goal.mGoalSteps);
////		cv.put(DataBaseContants.GOAL_CALORIES, goal.mGoalCalories);
////		db.update(DataBaseContants.TABLE_GOAL_NAME, cv, "_id = " + goal.id, null);
////		db.close();
////	}
//
//	/******************************************************************************
//	 * PERSON GOAL END
//	 *******************************************************************************/
//
//	/******************************************************************************
//	 * PERSON CONFIGRATION START
//	 *******************************************************************************/
////	public synchronized PersonalConfig conf_insert(PersonalConfig conf) {
////		SQLiteDatabase db = getWritableDatabase();
////		ContentValues cv = new ContentValues();
////		cv.put(DataBaseContants.CONF_SEX, conf.getSex());
////		cv.put(DataBaseContants.CONF_WEIDTH, conf.getWeight());
////		cv.put(DataBaseContants.CONF_HEIGHT, conf.getHeight() + "");
////		cv.put(DataBaseContants.CONF_YEAR, conf.getYear());
////		long id = db.insert(DataBaseContants.TABLE_CONFIG_NAME, null, cv);
////		db.close();
////		Log.i("gchk", "conf_insert id =" + id);
////		conf.id = id;
////		return conf;
////	}
////
////	public synchronized PersonalConfig conf_query() {
////		PersonalConfig conf = null;
////		SQLiteDatabase db = getReadDatabase();
////		StringBuilder sb = new StringBuilder();
////		sb.append("SELECT _id , sex , height , weidth , year FROM config ");
////		Cursor cursor = db.rawQuery(sb.toString(), null);
////		if (cursor != null && cursor.getCount() > 0) {
////			cursor.moveToFirst();
////			conf = new PersonalConfig();
////			conf.id = cursor.getInt(cursor.getColumnIndex(DataBaseContants.ID));
////			conf.setSex(cursor.getInt(cursor.getColumnIndex(DataBaseContants.CONF_SEX)));
////			conf.setWeight(cursor.getInt(cursor.getColumnIndex(DataBaseContants.CONF_WEIDTH)));
////			conf.setHeight(Float.parseFloat(cursor.getString(cursor.getColumnIndex(DataBaseContants.CONF_SEX))));
////			conf.setYear(cursor.getInt(cursor.getColumnIndex(DataBaseContants.CONF_YEAR)));
////		}
////		cursor.close();
////		db.close();
////		return conf;
////	}
////
////	public synchronized void conf_update(PersonalConfig conf) {
////		SQLiteDatabase db = getReadDatabase();
////		ContentValues cv = new ContentValues();
////		cv.put(DataBaseContants.CONF_SEX, conf.getSex());
////		cv.put(DataBaseContants.CONF_WEIDTH, conf.getWeight());
////		cv.put(DataBaseContants.CONF_HEIGHT, conf.getHeight() + "");
////		cv.put(DataBaseContants.CONF_YEAR, conf.getYear());
////		db.update(DataBaseContants.TABLE_CONFIG_NAME, cv, "_id = " + conf.id, null);
////		db.close();
////	}
//
//	/******************************************************************************
//	 * PERSON CONFIGRATION END
//	 *******************************************************************************/
//
//	public synchronized SQLiteDatabase getWritableDatabase() {
//		return mDBOpenHelper.getWritableDatabase();
//	}
//
//	public synchronized SQLiteDatabase getReadDatabase() {
//		return mDBOpenHelper.getReadableDatabase();
//	}
//
////	/*********************************************************************
////	 * test
////	 */
////	private void testInsertToday() {
////		String today = Tools.getDate(0);
////		if(item_query_day(today).getDate().equals("")){
////			RunningItem databean = new RunningItem();
////			databean.setDate(today);
////			databean.setDuration("30");
////			databean.setStartTime("10:01");
////			databean.setEndTime("10:31");
////			databean.setCalories(36);
////			databean.setSteps(1000);
////			databean.setKilometer(1.1F);
////			item_insert(databean);
////			
////			databean.setDate(today);
////			databean.setDuration("38");
////			databean.setStartTime("12:18");
////			databean.setEndTime("12:56");
////			databean.setCalories(45);
////			databean.setSteps(1653);
////			databean.setKilometer(1.5F);
////			item_insert(databean);
////
////			databean.setDate(today);
////			databean.setDuration("20");
////			databean.setStartTime("16:27");
////			databean.setEndTime("16:47");
////			databean.setCalories(28);
////			databean.setSteps(689);
////			databean.setKilometer(0.8F);
////			item_insert(databean);
////		}
////	}
////	
////	public void testInsertFirst(){
////		for(int i = 0 ; i< 5 ; i++){
////			String today = Tools.getDate(i+1);
////			if(item_query_day(today).getDate().equals("")){
////				RunningItem databean = new RunningItem();
////				databean.setDate(today);
////				databean.setDuration("30");
////				databean.setStartTime("10:01");
////				databean.setEndTime("10:31");
////				databean.setCalories(36);
////				databean.setSteps(1000);
////				databean.setKilometer(1.1F);
////				item_insert(databean);
////				
////				databean.setDate(today);
////				databean.setDuration("38");
////				databean.setStartTime("12:18");
////				databean.setEndTime("12:56");
////				databean.setCalories(45);
////				databean.setSteps(1653);
////				databean.setKilometer(1.5F);
////				item_insert(databean);
////
////				databean.setDate(today);
////				databean.setDuration("20");
////				databean.setStartTime("16:27");
////				databean.setEndTime("16:47");
////				databean.setCalories(28);
////				databean.setSteps(689);
////				databean.setKilometer(0.8F);
////				item_insert(databean);
////			}
////		}
////	}
//
//}
