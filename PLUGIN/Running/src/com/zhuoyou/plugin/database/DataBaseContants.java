package com.zhuoyou.plugin.database;

import android.net.Uri;

public class DataBaseContants {
	public static final String DB_NAME = "running";

	public static final int DB_VERSION = 1;

	public static final String TABLE_DATA_NAME = "data";

	public static final String TABLE_GOAL_NAME = "goal";

	public static final String TABLE_CONFIG_NAME = "config";

	public static final String TABLE_WEATHER_NAME = "weather";

	public static final String TABLE_DELETE_NAME = "cloud";

	public static final String ID = "_id";

	public static final String DATE = "date";

	public static final String TIME_DURATION = "time_duration";

	public static final String TIME_START = "time_start";

	public static final String TIME_END = "time_end";

	public static final String STEPS = "steps";

	public static final String KILOMETER = "kilometer";

	public static final String CALORIES = "calories";
	
	public static final String STATISTICS = "statistics";

	public static final String GOAL_STEPS = "goal_steps";

	public static final String GOAL_CALORIES = "goal_calories";

	public static final String CONF_SEX = "sex";

	public static final String CONF_HEIGHT = "height";

	public static final String CONF_WEIDTH = "weidth";

	public static final String CONF_YEAR = "year";

	public static final String WEATHER_PM25 = "pm25";
	
	public static final String CONF_WEIGHT = "weight";
	
	public static final String BMI = "bmi";
	
	public static final String IMG_URI = "img_uri";
	
	public static final String EXPLAIN = "img_explain";
	
	public static final String SPORTS_TYPE = "sports_type";
	
	public static final String TYPE = "type";

	public static final String IMG_CLOUD = "img_cloud";

	public static final String COMPLETE = "complete";

	//同步状态：默认0--需新增，1--已同步，2--要修改
	public static final String SYNC_STATE = "sync";

	public static final String DELETE_ID = "_id";
	
	public static final String DELETE_VALUE = "delete_value";

	public static final String AUTHORITY = "com.zhuoyou.plugin.database";
	public static final String PATH = "data";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);
	public static final Uri CONTENT_DELETE_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_DELETE_NAME);
}
