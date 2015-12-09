package com.zhuoyou.plugin.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {
 
	public DBOpenHelper(Context context) {
		super(context, DataBaseContants.DB_NAME, null, DataBaseContants.DB_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(" CREATE TABLE IF NOT EXISTS " + DataBaseContants.TABLE_DATA_NAME
				+ " ( " + DataBaseContants.ID + " LONG primary key , "
				+ DataBaseContants.DATE + " TEXT , "
				+ DataBaseContants.TIME_DURATION + " TEXT , "
				+ DataBaseContants.TIME_START + " TEXT , "
				+ DataBaseContants.TIME_END + " TEXT , "
				+ DataBaseContants.STEPS + " INTEGER , " 
				+ DataBaseContants.KILOMETER + " INTEGER ," 
				+ DataBaseContants.CALORIES + " INTEGER ," 
				+ DataBaseContants.CONF_WEIGHT + " TEXT ," 
				+ DataBaseContants.BMI + " TEXT ," 
				+ DataBaseContants.IMG_URI + " TEXT ," 
				+ DataBaseContants.EXPLAIN + " TEXT ," 
				+ DataBaseContants.SPORTS_TYPE + " INTEGER DEFAULT (0) NOT NULL,"
				+ DataBaseContants.TYPE + " INTEGER DEFAULT (-1) NOT NULL," 
				+ DataBaseContants.IMG_CLOUD + " TEXT ," 
				+ DataBaseContants.COMPLETE + " INTEGER DEFAULT (0) NOT NULL ," 
				+ DataBaseContants.SYNC_STATE + " INTEGER DEFAULT (0) NOT NULL,"
				+ DataBaseContants.STATISTICS + " INTEGER ,"
				+ DataBaseContants.DATA_FROM + " TEXT );");
		db.execSQL(" CREATE TABLE IF NOT EXISTS " + DataBaseContants.TABLE_DELETE_NAME
				+ " ( " + DataBaseContants.DELETE_ID + " integer PRIMARY KEY AUTOINCREMENT , "
				+ DataBaseContants.DELETE_VALUE + " LONG );");
		db.execSQL(" CREATE TABLE IF NOT EXISTS " + DataBaseContants.TABLE_POINT_NAME
				+ " ( " + DataBaseContants.GPS_ID + " LONG primary key , "
				+ DataBaseContants.LATITUDE + " double , "
				+ DataBaseContants.LONGTITUDE + " double , "
				+ DataBaseContants.ADDRESS + " char , "
				+ DataBaseContants.ACCURACY + " float , "
				+ DataBaseContants.PROVIDER + " char , " 
				+ DataBaseContants.LOCATION_TIME + " long ,"
				+ DataBaseContants.LOCATION_SYS_TIME + " long ," 
				+ DataBaseContants.SPEED + " float ," 
				+ DataBaseContants.ALTITUDE + " double ," 
				+ DataBaseContants.LOCATION_POINT_STATE + " int ," 
				+ DataBaseContants.GPS_NUMBER + " integer DEFAULT(0), "
				+ DataBaseContants.GPS_SYNC + " integer DEFAULT(0) NOT NULL);");
		db.execSQL(" CREATE TABLE IF NOT EXISTS " + DataBaseContants.TEMP_POINT_NAME
				+ " ( " + DataBaseContants.GPS_ID + " integer PRIMARY KEY AUTOINCREMENT , "
				+ DataBaseContants.LATITUDE + " double , "
				+ DataBaseContants.LONGTITUDE + " double , "
				+ DataBaseContants.ADDRESS + " char , "
				+ DataBaseContants.ACCURACY + " float , "
				+ DataBaseContants.PROVIDER + " char , " 
				+ DataBaseContants.LOCATION_TIME + " long ,"
				+ DataBaseContants.LOCATION_SYS_TIME + " long ," 
				+ DataBaseContants.SPEED + " float ," 
				+ DataBaseContants.ALTITUDE + " double ," 
				+ DataBaseContants.LOCATION_POINT_STATE + " int ," 
				+ DataBaseContants.GPS_NUMBER + " integer DEFAULT(0), "
				+ DataBaseContants.GPS_SYNC + " integer DEFAULT(0) NOT NULL);");
		db.execSQL(" CREATE TABLE IF NOT EXISTS " + DataBaseContants.TABLE_OPERATION_NAME
				+ " ( " + DataBaseContants.GPS_ID + " LONG primary key , "
				+ DataBaseContants.OPERATION_TIME + " long,"
				+ DataBaseContants.OPERATION_SYSTIME + " long,"
				+ DataBaseContants.OPERATION_STATE + " integer ,"
				+ DataBaseContants.GPS_SYNC + " integer DEFAULT(0) NOT NULL);");
		db.execSQL(" CREATE TABLE IF NOT EXISTS " + DataBaseContants.TABLE_GPSSPORT_NAME
				+ " ( " + DataBaseContants.GPS_ID + " LONG primary key , "
				+ DataBaseContants.GPS_STARTTIME + " long , "
				+ DataBaseContants.GPS_ENDTIME + " long , "
				+ DataBaseContants.GPS_SYSSTARTTIME + " long , "
				+ DataBaseContants.GPS_SYSENDTIME + " long , "
				+ DataBaseContants.GPS_STARTADDRESS + " TEXT , "
				+ DataBaseContants.GPS_ENDADDRESS + " TEXT , "
				+ DataBaseContants.GPS_DURATIONTIME + " long , "
				+ DataBaseContants.AVESPEED + " float , "
				+ DataBaseContants.TOTAL_DISTANCE + " float , " 
				+ DataBaseContants.GPS_STEPS + " integer ," 
				+ DataBaseContants.GPS_CALORIE + " float,"
				+ DataBaseContants.GPS_SYNC + " integer DEFAULT(0) NOT NULL );");
		db.execSQL(" CREATE TABLE IF NOT EXISTS " + DataBaseContants.TABLE_GPS_SYNC
				+ " ( " + DataBaseContants.GPS_ID + " integer PRIMARY KEY AUTOINCREMENT , "
				+ DataBaseContants.GPS_TABLE + " TEXT,"
				+ DataBaseContants.GPS_DELETE + " TEXT);");

		db.execSQL(" CREATE TABLE IF NOT EXISTS " + DataBaseContants.TABLE_ACTION_MSG
				+ " ( _id integer PRIMARY KEY AUTOINCREMENT ," 
				+ DataBaseContants.ACTIVITY_ID + " integer , "
				+ DataBaseContants.MSG_ID + " integer , "
				+ DataBaseContants.MSG_CONTENT + " TEXT , "
				+ DataBaseContants.MSG_TYPE + " integer DEFAULT(0) NOT NULL ,"
				+ DataBaseContants.MSG_TIME + " TEXT , "
				+ DataBaseContants.MSG_STATE + " INTEGER DEFAULT(0) NOT NULL);");
		
		/** add for sleep */
		db.execSQL(" CREATE TABLE IF NOT EXISTS " + DataBaseContants.TABLE_SLEEP
				+ " ( " + DataBaseContants.SLEEP_ID + " LONG primary key , "
				+ DataBaseContants.SLEEP_TYPE + " integer , "
				+ DataBaseContants.SLEEP_STARTTIME + " long , "
				+ DataBaseContants.SLEEP_ENDTIME + " long , "
				+ DataBaseContants.SLEEP_TURNDATA + " text ) ;");
		/** add for classic bt sleep */
		db.execSQL(" CREATE TABLE IF NOT EXISTS " + DataBaseContants.TABLE_SLEEP_2
				+ " ( " + DataBaseContants.SLEEP_ID + " integer PRIMARY KEY AUTOINCREMENT , "
				+ DataBaseContants.DATE + " TEXT , "
				+ DataBaseContants.SLEEP_DETAILS + " TEXT ) ;");
	
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + " action_msg");
		db.execSQL("DROP TABLE IF EXISTS " + " point_message");	
		db.execSQL("DROP TABLE IF EXISTS " + " operation_time");	
		db.execSQL("DROP TABLE IF EXISTS " + " gps_sport");	
		onCreate(db);
	}
}
