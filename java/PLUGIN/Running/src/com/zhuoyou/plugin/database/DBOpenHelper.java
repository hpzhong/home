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
				+ DataBaseContants.KILOMETER+ " INTEGER ," 
				+ DataBaseContants.CALORIES+ " INTEGER ," 
				+ DataBaseContants.CONF_WEIGHT+ " TEXT ," 
				+ DataBaseContants.BMI+ " TEXT ," 
				+ DataBaseContants.IMG_URI+ " TEXT ," 
				+ DataBaseContants.EXPLAIN+ " TEXT ," 
				+ DataBaseContants.SPORTS_TYPE+ " INTEGER DEFAULT (0) NOT NULL,"
				+ DataBaseContants.TYPE+ " INTEGER ," 
				+ DataBaseContants.IMG_CLOUD+ " TEXT ," 
				+ DataBaseContants.COMPLETE+ " INTEGER DEFAULT (0) NOT NULL ," 
				+ DataBaseContants.SYNC_STATE+ " INTEGER DEFAULT (0) NOT NULL,"
				+ DataBaseContants.STATISTICS + " BOOLEAN );");
		db.execSQL(" CREATE TABLE IF NOT EXISTS " + DataBaseContants.TABLE_DELETE_NAME
				+ " ( " + DataBaseContants.DELETE_ID + " integer PRIMARY KEY AUTOINCREMENT , "
				+ DataBaseContants.DELETE_VALUE + " LONG );");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		db.execSQL("DROP TABLE IF EXISTS" + DataBaseContants.TABLE_DATA_NAME);
//		onCreate(db);
	}
}
