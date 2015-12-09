package com.zhuoyou.plugin.database;

import android.net.Uri;

public class DataBaseContants {
	public static final String DB_NAME = "running";

	public static final int DB_VERSION = 6;

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
	
	//0为分段数据，1为总的统计数据，2为各种设备的统计数据
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

	public static final String DATA_FROM = "data_from";
	
	public static final String DELETE_ID = "_id";
	
	public static final String DELETE_VALUE = "delete_value";

	//add for action msg
	public static final String TABLE_ACTION_MSG = "action_msg_info";
	
	public static final String ACTIVITY_ID = "activity_id";
	
	public static final String MSG_ID = "msg_id";
	
	public static final String MSG_CONTENT = "content";
	
	public static final String MSG_TYPE = "msg_type";
	
	public static final String MSG_TIME = "msg_time";

	public static final String MSG_STATE = "state";
	
	public static final String AUTHORITY = "com.zhuoyou.plugin.database";
	
	public static final String PATH = "data";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);
	public static final Uri CONTENT_DELETE_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_DELETE_NAME);
	public static final Uri CONTENT_MSG_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_ACTION_MSG);
	
	//add for gps sport start
	
	public static final String TABLE_POINT_NAME = "point_message2";
	
	public static final String TEMP_POINT_NAME = "point_temp";

	public static final String TABLE_OPERATION_NAME = "operation_time2";

	public static final String TABLE_GPSSPORT_NAME = "gps_sport2";
	
	public static final String TABLE_GPS_SYNC = "gps_sync";

	public static final String GPS_ID = "_id";
	
	/** GPS同步状态：默认0--需新增，1--已同步，2--要修改 */
	public static final String GPS_SYNC="sync_state";

	public static final String LATITUDE = "latitude";

	public static final String LONGTITUDE = "longtitude";

	public static final String ADDRESS = "address";

	public static final String ACCURACY = "accuracy";

	public static final String PROVIDER = "provider";

	public static final String LOCATION_TIME = "location_time";

	public static final String SPEED = "speed";
	
	public static final String ALTITUDE = "altitude";

	public static final String GPS_NUMBER = "gps_number";
	
	public static final String LOCATION_SYS_TIME = "location_systime";
	
	public static final String LOCATION_POINT_STATE = "point_state";

	public static final String OPERATION_TIME = "operation_time";
	
	public static final String OPERATION_SYSTIME = "operation_systime";

	public static final String OPERATION_STATE = "operation_state";

	public static final String GPS_SYSSTARTTIME = "sys_starttime";

	public static final String GPS_SYSENDTIME = "sys_endtime";
	
	public static final String GPS_STARTTIME = "starttime";

	public static final String GPS_ENDTIME = "endtime";
	
	public static final String GPS_STARTADDRESS = "startaddress";

	public static final String GPS_ENDADDRESS = "endaddress";

	public static final String GPS_DURATIONTIME = "durationtime";

	public static final String AVESPEED = "avespeed";
	
	public static final String TOTAL_DISTANCE = "total_distance";
	
	public static final String GPS_STEPS = "steps";
	
	public static final String GPS_CALORIE = "calorie";
	
	public static final String GPS_TABLE = "table_name";
	
	public static final String GPS_DELETE = "delete_id";

	public static final Uri CONTENT_URI_POINT = Uri.parse("content://" + AUTHORITY + "/" + TABLE_POINT_NAME);
	
	public static final Uri CONTENT_URI_TEMPPOINT = Uri.parse("content://" + AUTHORITY + "/" + TEMP_POINT_NAME);
	
	public static final Uri CONTENT_URI_OPERATION = Uri.parse("content://" + AUTHORITY + "/" + TABLE_OPERATION_NAME);
	
	public static final Uri CONTENT_URI_GPSSPORT = Uri.parse("content://" + AUTHORITY + "/" + TABLE_GPSSPORT_NAME);
	
	public static final Uri CONTENT_URI_GPSSYNC =  Uri.parse("content://" + AUTHORITY + "/" + TABLE_GPS_SYNC);
	
	//add for gps sport end
	public static final String TABLE_SLEEP = "sleep";
	
	public static final String SLEEP_ID = "_id";
	
	public static final String SLEEP_TYPE = "type";
	
	public static final String SLEEP_STARTTIME = "start_time";
	
	public static final String SLEEP_ENDTIME = "end_time";
	
	public static final String SLEEP_TURNDATA = "turn_data";
	
	public static final String TABLE_SLEEP_2 = "sleep2";
	public static final String SLEEP_DETAILS = "sleep_details";
}
