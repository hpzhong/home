package com.zhuoyou.plugin.running;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import com.zhuoyou.plugin.database.DBOpenHelper;
import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.info.ImageAsynTask;

@SuppressLint("SimpleDateFormat")
public class Tools {
	private static DBOpenHelper mDBOpenHelper;
	private static final String SP_GOAL_FILENAME = "personal_goal";
	private static final String SP_GOAL_KEY_CAL = "cal";
	private static final String SP_GOAL_KEY_STEPS = "steps";
	private static final String SP_CONFIG_FILENAME = "personal_config";
	private static final String SP_CONFIG_KEY_SEX = "sex";
	private static final String SP_CONFIG_KEY_WIDTH = "width";
	private static final String SP_CONFIG_KEY_WIGHT = "wight";
	private static final String SP_CONFIG_KEY_HEIGHT = "height";
	private static final String SP_CONFIG_KEY_YEAR = "year";
	public static final String SP_PM25_FILENAME = "weather";
	public static final String SP_SPP_FLAG_FILENAME = "spp_flag";
	public static final String SP_SPP_FLAG_KEY_FLAG = "flag";
	public static final String SP_SPP_FLAG_KEY_SYNCNOW = "syncnow";
	public static final String SP_SPP_FLAG_KEY_SHOWDIALOG = "show_dialog";
	public static final String SP_SPP_FLAG_KEY_VIEWINDEX = "page_index";
	public static final String SP_SPP_FLAG_KEY_SLEEP_VIEWINDEX = "sleep_page_index";
	public static final String USR_INFO = "usr_info";
	public static final String AUTO_SYNC_TIME = "auto_sync_time";
	public static final String PHONE_PED="phone_pedometer";
	public static final String PED_STATE="phone_pedometer_state";
	public static final String PHONE_SEDENTARY="phone_sedentary";
	public static final String SEDENTARY_STATE="phone_sedentary_state";
    private static long[] ls = new long[3000];
    private static int li = 0;
    
    private static Context mCont=RunningApp.getInstance().getApplicationContext();

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static int progress2degree(int progress) {
		float degree = progress;

		degree = degree * 3.6F;

		int ret = (int) (degree + 0.5);

		return ret;
	}

	public static boolean checkIsFirstEntry(Context ctx) {
		SharedPreferences sp = ctx.getSharedPreferences("app_config", Context.MODE_PRIVATE);
		boolean ret = sp.getBoolean("is_first_enter", true);
		return ret;
	}

	public static void setFirstEntry(Context ctx){
		SharedPreferences sp = ctx.getSharedPreferences("app_config", Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putBoolean("is_first_enter", false);
		et.commit();
	}
	
	public static List<String> getDateFromDb(Context ctx) {
		List<String> date = new ArrayList<String>();
		ContentResolver cr = ctx.getContentResolver();
		Cursor c = cr.query(DataBaseContants.CONTENT_URI, new String[] { "_id", "date" }, null, null, DataBaseContants.DATE + " ASC");
		c.moveToFirst();
		int count = c.getCount();
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				String temp = c.getString(c.getColumnIndex(DataBaseContants.DATE));
				if (date.indexOf(temp) == -1)
					date.add(temp);
				c.moveToNext();
			}
		}
		c.close();
		c = null;
		return date;
	}
	
	/**
	 * 得到当前几天的日期
	 * 
	 * @param prev_index
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static String getDate(int prev_index) {
		Calendar c = Calendar.getInstance();
		int theYear = c.get(Calendar.YEAR);
		int theMonth = c.get(Calendar.MONTH);
		int theDay = c.get(Calendar.DAY_OF_MONTH) - prev_index;
		c.set(theYear, theMonth, theDay);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date currentDate = c.getTime();
		String curTime = formatter.format(currentDate);
		return curTime;
	}

	@SuppressLint("SimpleDateFormat")
	public static String getDate(String day, int prev_index) {
		Calendar c = Calendar.getInstance();
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date date1 = formatter.parse(day);
			c.setTime(date1);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		int theYear = c.get(Calendar.YEAR);
		int theMonth = c.get(Calendar.MONTH);
		int theDay = c.get(Calendar.DAY_OF_MONTH) - prev_index;
		c.set(theYear, theMonth, theDay);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date currentDate = c.getTime();
		String curTime = formatter.format(currentDate);
		return curTime;
	}

	@SuppressLint("SimpleDateFormat")
	public static int getDayCount(String enterDay, String today, String format) {
		int count = 0;
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(format);
			Date date1 = formatter.parse(enterDay);
			Date date2 = formatter.parse(today);
			c1.setTime(date1);
			c2.setTime(date2);
			long l = c2.getTimeInMillis() - c1.getTimeInMillis();
			count = (int) Math.ceil(l / 86400000.0D);
			if (l <= 0L || count == 0) {
				count = 1;
			} else {
				count++;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Log.i("gchk", "day count = " + count);
		return count;
	}

	@SuppressLint("SimpleDateFormat")
	public static String dateFormat(String date, String paramString) {
		if (paramString == null)
			paramString = "yyyy-MM-dd HH:mm:ss";
		Calendar localCalendar = Calendar.getInstance();
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date paramDate = formatter.parse(date);
			localCalendar.setTime(paramDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new SimpleDateFormat(paramString).format(localCalendar.getTime());
	}
	
	@SuppressLint("SimpleDateFormat")
	public static Boolean isSameWeek(String date1,String date2) {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date d1 = formatter.parse(date1);
			Date d2 = formatter.parse(date2);
			c1.setTime(d1);
			c2.setTime(d2);		
		} catch (ParseException e) {
			e.printStackTrace();
		}
		int subYear = c1.get(Calendar.YEAR)-c2.get(Calendar.YEAR);
		if(subYear == 0) {
			if(c1.get(Calendar.WEEK_OF_YEAR) == c2.get(Calendar.WEEK_OF_YEAR))
				return true;
		} else if(subYear==1 && c2.get(Calendar.MONTH)==11) {
			if(c1.get(Calendar.WEEK_OF_YEAR) == c2.get(Calendar.WEEK_OF_YEAR))
				return true;
		} else if(subYear==-1 && c1.get(Calendar.MONTH)==11) {
			if(c1.get(Calendar.WEEK_OF_YEAR) == c2.get(Calendar.WEEK_OF_YEAR))
				return true;
		}
		return false;
	}
	
	@SuppressLint("SimpleDateFormat")
	public static Boolean isSameMonth(String date1,String date2) {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date d1 = formatter.parse(date1);
			Date d2 = formatter.parse(date2);
			c1.setTime(d1);
			c2.setTime(d2);		
		} catch (ParseException e) {
			e.printStackTrace();
		}
		int subYear = c1.get(Calendar.YEAR)-c2.get(Calendar.YEAR);
		if(subYear == 0) {
			if(c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH))
				return true;
		}
		return false;
	}
	
	public static String getScreenShot(String fileName) {
		String filePath = getSDPath() + "/Running/share/" + fileName;
		File f = new File(filePath);
		if (f.exists())
			return filePath;
		else
			return null;
	}

	public static void saveBitmapToFile(Bitmap bitmap, String fileName) {
		String filePath = getSDPath() + "/Running/share/";
		File dir = new File(filePath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File f = new File(filePath + fileName);
		if (f.exists()) {
			f.delete();
		}
		try {
			f.createNewFile();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	public static void saveGpsBitmapToFile(Bitmap bitmap, String fileName) {
		String filePath = getSDPath() + "/Running/gps/";
		File dir = new File(filePath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File f = new File(filePath + fileName);
		if (f.exists()) {
			f.delete();
		}
		try {
			f.createNewFile();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	public static Bitmap convertFileToBitmap(String name) {
		Bitmap myBitmap = null;
		String sdPath = getSDPath();

		if (TextUtils.isEmpty(sdPath))
			return null;

		String fileString = sdPath + name;

		File file = new File(fileString);

		if (file.exists()) {
			try {
				myBitmap = BitmapFactory.decodeFile(fileString);

			} catch (OutOfMemoryError e) {
				System.gc();
				myBitmap = null;
				e.printStackTrace();
			}

		}

		return myBitmap;
	}

	public static String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();
		} else {
			return null;
		}
		return sdDir.toString();
	}
	
	public static PersonalGoal getPersonalGoal(){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		PersonalGoal goal = new PersonalGoal();
		SharedPreferences sp = sContext.getSharedPreferences(SP_GOAL_FILENAME, Context.MODE_PRIVATE);
		goal.mGoalCalories = sp.getInt(SP_GOAL_KEY_CAL, 200);
		goal.mGoalSteps = sp.getInt(SP_GOAL_KEY_STEPS, 7000);
		return goal;
	}
	
	public static void updatePersonalGoal(PersonalGoal goal){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(SP_GOAL_FILENAME, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putInt(SP_GOAL_KEY_CAL, goal.mGoalCalories);
		et.putInt(SP_GOAL_KEY_STEPS, goal.mGoalSteps);
		et.commit();
	}

	public static void updatePersonalGoalStep(PersonalGoal goal){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(SP_GOAL_FILENAME, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putInt(SP_GOAL_KEY_STEPS, goal.mGoalSteps);
		et.commit();
	}
	
	public static PersonalConfig getPersonalConfig(){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		PersonalConfig config= new PersonalConfig();
		String wight;
		SharedPreferences sp = sContext.getSharedPreferences(SP_CONFIG_FILENAME, Context.MODE_PRIVATE);
		config.setSex(sp.getInt(SP_CONFIG_KEY_SEX, PersonalConfig.SEX_MAN));
		if (sp.contains(SP_CONFIG_KEY_WIGHT)) {
			wight = sp.getString(SP_CONFIG_KEY_WIGHT, "175");
		} else {
			wight = sp.getInt(SP_CONFIG_KEY_WIDTH, 65) + ".0";
		}
		config.setWeight(wight);
		config.setHeight(sp.getInt(SP_CONFIG_KEY_HEIGHT, 180));
		config.setYear(sp.getInt(SP_CONFIG_KEY_YEAR, 1990));
		return config;
	}
	
	public static void updatePersonalConfig(PersonalConfig config){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(SP_CONFIG_FILENAME, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putInt(SP_CONFIG_KEY_SEX, config.getSex());
		et.putString(SP_CONFIG_KEY_WIGHT, config.getWeight());
		et.putInt(SP_CONFIG_KEY_HEIGHT, config.getHeight());
		et.putInt(SP_CONFIG_KEY_YEAR, config.getYear());
		et.commit();
	}

	public static void saveConsigneeInfo(String name, String phone, String address){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(SP_CONFIG_FILENAME, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putString("consigneeName", name);
		et.putString("consigneePhone", phone);
		et.putString("consigneeLocation", address);
		et.commit();
	}

	public static String getConsigneeName(Context sContext){
		SharedPreferences sp = sContext.getSharedPreferences(SP_CONFIG_FILENAME, Context.MODE_PRIVATE);
		return sp.getString("consigneeName", "");
	}

	public static String getConsigneePhone(Context sContext){
		SharedPreferences sp = sContext.getSharedPreferences(SP_CONFIG_FILENAME, Context.MODE_PRIVATE);
		return sp.getString("consigneePhone", "");
	}

	public static String getConsigneeAddress(Context sContext){
		SharedPreferences sp = sContext.getSharedPreferences(SP_CONFIG_FILENAME, Context.MODE_PRIVATE);
		return sp.getString("consigneeLocation", "");
	}

	public static int getPm25(String date){
		int pm25 = 0;
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(SP_PM25_FILENAME, Context.MODE_PRIVATE);
		pm25 = sp.getInt(date, 0);
		return pm25;
	}
	
	public static void updatePm25(String date , int pm25){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(SP_PM25_FILENAME, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putInt(date, pm25);
		et.commit();
	}
	
	public static void setSppConnectedFlag(boolean flag){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(SP_SPP_FLAG_FILENAME, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putBoolean(SP_SPP_FLAG_KEY_FLAG, flag);
		et.commit();
	}
	
	public static boolean getSppConnectedFlag(){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(SP_SPP_FLAG_FILENAME, Context.MODE_PRIVATE);
		return sp.getBoolean(SP_SPP_FLAG_KEY_FLAG, false);
	}
	
	public static void setSyncRnningDataNow(boolean flag){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(SP_SPP_FLAG_FILENAME, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putBoolean(SP_SPP_FLAG_KEY_SYNCNOW, flag);
		et.commit();
	}
	
	public static boolean getSyncRunningDataNow(){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(SP_SPP_FLAG_FILENAME, Context.MODE_PRIVATE);
		return sp.getBoolean(SP_SPP_FLAG_KEY_SYNCNOW, false);
	}
	
	public static void setShowCreateScDialog(boolean show){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(SP_SPP_FLAG_FILENAME, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putBoolean(SP_SPP_FLAG_KEY_SHOWDIALOG, show);
		et.commit();
	}
	
	public static boolean getShowCreateScDialog(){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(SP_SPP_FLAG_FILENAME, Context.MODE_PRIVATE);
		return sp.getBoolean(SP_SPP_FLAG_KEY_SHOWDIALOG, true);
	}
	
	public static void setCurrPageIndex(int index){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(SP_SPP_FLAG_FILENAME, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putInt(SP_SPP_FLAG_KEY_VIEWINDEX, index);
		et.commit();
	}
	
	public static int getCurrPageIndex(){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(SP_SPP_FLAG_FILENAME, Context.MODE_PRIVATE);
		return sp.getInt(SP_SPP_FLAG_KEY_VIEWINDEX, 0);
	}
	
	public static void setSleepCurrPageIndex(int index){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(SP_SPP_FLAG_FILENAME, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putInt(SP_SPP_FLAG_KEY_SLEEP_VIEWINDEX, index);
		et.commit();
	}
	
	public static int getSleepCurrPageIndex(){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(SP_SPP_FLAG_FILENAME, Context.MODE_PRIVATE);
		return sp.getInt(SP_SPP_FLAG_KEY_SLEEP_VIEWINDEX, 0);
	}
	public static int calcDistance(int step , int height){
		int meter = 0 ;
		double meterperstep = 0;
		
		if(height<155)
			meterperstep = 0.55;
		else if(height<=165)
			meterperstep = 0.6;
		else if(height<=175)
			meterperstep = 0.65;
		else if(height<=185)
			meterperstep = 0.7;
		else if(height<=195)
			meterperstep = 0.75;
		else 
			meterperstep = 0.8;
		meter = (int) (step * meterperstep);
		return meter;
	}
	
	public static int calcCalories(int meter , float weight){
		int cal = 0 ;
		int temp = (int)(weight / 1);
		cal = (int) (temp * meter / 1000 * 1.175);
		return cal;
	}
	
	public static String getStartTime(){
		String curTime = new DateFormat().format("kk:mm", Calendar.getInstance(Locale.CHINA))+"";
		return curTime;
	}

	public static String getCurData(){
		String data = new DateFormat().format("yyyy-MM-dd", Calendar.getInstance(Locale.CHINA))+"";
		return data;
	}
	
	/**
	 * 根据运动的 索引得到某种运动 某段时间之中消耗的 卡路里
	 * @param sportIndex
	 * @param lastTime
	 * @return
	 */
	public static int getSportKll(int sportIndex,int lastTime){
		int wasteKll = 0;
		switch (sportIndex) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
			wasteKll = 3 * lastTime;
			break;
		case 7:
		case 8:
		case 9:
		case 10:
			wasteKll = 4 * lastTime;
			break;
		case 11:
		case 12:
		case 13:
		case 14:
		case 15:
		case 16:
			wasteKll = 5 * lastTime;
			break;
		case 17:
		case 18:
		case 19:
		case 20:
		case 21:
			wasteKll = 6 * lastTime;
			break;
		case 22:
			wasteKll = 7 * lastTime;
			break;
		case 23:
		case 24:
		case 25:
		case 26:
			wasteKll = 8 * lastTime;
			break;
		case 27:
		case 28:
			wasteKll = 9 * lastTime;
			break;
		case 29:
			wasteKll = 10 * lastTime;
			break;
		case 30:
		case 31:
			wasteKll = 11 * lastTime;
			break;
		case 32:
		case 33:
			wasteKll = 13 * lastTime;
		default:
			break;
		}
		return wasteKll;
	}
	
	/**
	 * 得到 某种运动的在数组中的坐标
	 * -1  代表没有找到
	 */
	public static int getSportIndex(String[] arry,String sportName){
		for(int i=0;i<arry.length;i++){
			if(sportName.equals(arry[i])){
				return i;
			}
		}
		
		return -1;
	}
	
	public static double getBMI(PersonalConfig mPersonalConfig) {
		double bmi = 0.0D;
		float weight = mPersonalConfig.getWeightNum();
		int height = mPersonalConfig.getHeight();
		bmi = (double) (weight * 10000D) / (height * height);
		return bmi;
	}

	public static int[] sportType = { R.drawable.chonglang, R.drawable.menqiu,
			R.drawable.feipan, R.drawable.tiaoshui, R.drawable.feibiao,
			R.drawable.huachuan, R.drawable.baolingqiu, R.drawable.binghu,
			R.drawable.yujia, R.drawable.pingpang, R.drawable.buxing,
			R.drawable.danbanhuaxue, R.drawable.banqiu, R.drawable.bangqiu,
			R.drawable.gaoerfu, R.drawable.tiaowu, R.drawable.yumaoqiu,
			R.drawable.jijian, R.drawable.huaxue, R.drawable.paiqiu,
			R.drawable.lanqiu, R.drawable.palouti, R.drawable.jianshencao,
			R.drawable.wangqiu, R.drawable.zuqiu, R.drawable.pashan,
			R.drawable.qixing, R.drawable.ganlanqiu, R.drawable.paobu,
			R.drawable.quanji, R.drawable.tiaosheng, R.drawable.youyong,
			R.drawable.biqiu, R.drawable.lunhua };

	public static int[] headIcon = { R.drawable.mengmeizi, R.drawable.mm, R.drawable.dama, R.drawable.xiaozhengtai, R.drawable.gg, R.drawable.dashu, 
			R.drawable.head1_1, R.drawable.head1_2, R.drawable.head1_3, R.drawable.head1_4, R.drawable.head1_5, 
			R.drawable.head1_6, R.drawable.head1_7, R.drawable.head1_8, R.drawable.head2_1, R.drawable.head2_2, 
			R.drawable.head2_3, R.drawable.head2_4, R.drawable.head2_5, R.drawable.head2_6, R.drawable.head2_7, 
			R.drawable.head2_8, R.drawable.head2_9, R.drawable.head2_10, R.drawable.head2_11, R.drawable.head3_1, 
			R.drawable.head3_2, R.drawable.head3_3, R.drawable.head3_4, R.drawable.head3_5, R.drawable.head3_6, 
			R.drawable.head3_7, R.drawable.head3_8, R.drawable.head3_9, R.drawable.head3_10, R.drawable.head3_11,
			R.drawable.head3_12, R.drawable.head3_13, R.drawable.head3_14, R.drawable.head3_15 };
	public static int[] headIcon1 = { R.drawable.mengmeizi, R.drawable.mm, R.drawable.dama,
			R.drawable.xiaozhengtai, R.drawable.gg, R.drawable.dashu };
	public static int[] headIcon2 = { R.drawable.head1_1, R.drawable.head1_2, R.drawable.head1_3, R.drawable.head1_4, R.drawable.head1_5, 
			R.drawable.head1_6, R.drawable.head1_7, R.drawable.head1_8 };
	public static int[] headIcon3 = { R.drawable.head2_1, R.drawable.head2_2, R.drawable.head2_3, R.drawable.head2_4, R.drawable.head2_5, 
			R.drawable.head2_6, R.drawable.head2_7, R.drawable.head2_8, R.drawable.head2_9, R.drawable.head2_10, R.drawable.head2_11 };
	public static int[] headIcon4 = { R.drawable.head3_1, R.drawable.head3_2, R.drawable.head3_3, R.drawable.head3_4, R.drawable.head3_5, 
			R.drawable.head3_6, R.drawable.head3_7, R.drawable.head3_8, R.drawable.head3_9, R.drawable.head3_10,
			R.drawable.head3_11, R.drawable.head3_12, R.drawable.head3_13, R.drawable.head3_14, R.drawable.head3_15 };

	public static int getHead(Context ctx) {
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		return sp.getInt("edit_head", 6);
	}

	public static void setHead(Context ctx,int position){
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putInt("edit_head", position);
		et.commit();
	}
	
	public static String getUsrName(Context ctx) {
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		return sp.getString("edit_usr_name", "");
	}

	public static void setUsrName(Context ctx,String name){
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putString("edit_usr_name", name);
		et.commit();
	}
	
	public static String getSignature(Context ctx) {
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		return sp.getString("edit_setSignature", "");
	}

	public static void setSignature(Context ctx,String setSignature){
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putString("edit_setSignature", setSignature);
		et.commit();
	}
	
	public static String getLikeSportsIndex(Context ctx) {
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		return sp.getString("edit_like_sports_index", "");
	}

	public static void setLikeSportsIndex(Context ctx,String setLikeIndex){
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putString("edit_like_sports_index", setLikeIndex);
		et.commit();
	}
	
	public static String getPhoneNum(Context ctx) {
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		return sp.getString("phone_num", "");
	}

	public static void setPhoneNum(Context ctx,String string){
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putString("phone_num", string);
		et.commit();
	}
	
	public static String getEmail(Context ctx) {
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		return sp.getString("email_info", "");
	}

	public static void setEmail(Context ctx,String email){
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putString("email_info", email);
		et.commit();
	}
	
	public static int getProviceIndex(Context ctx) {
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		return sp.getInt("provice_index",10000);
	}

	public static void setProviceIndex(Context ctx,int index){
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putInt("provice_index", index);
		et.commit();
	}
	
	public static int getCityIndex(Context ctx) {
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		return sp.getInt("city_index",10000);
	}

	public static void setCityIndex(Context ctx,int index){
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putInt("city_index", index);
		et.commit();
	}

	public static String getHeadType(Context ctx) {
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		return sp.getString("regType", "");
	}

	public static String getHeadURI(Context ctx) {
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		return sp.getString("logoUrl", "");
	}
	
	public static boolean getLogin(Context ctx) {
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		return sp.getBoolean("usr_login", false);
	}

	public static void setLogin(Context ctx,boolean login){
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putBoolean("usr_login", login);
		et.commit();
	}
	
	public static String getLoginName(Context ctx) {
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		return sp.getString("login_name", "");
	}
	
	public static String getOpenId(Context ctx) {
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		return sp.getString("openid", "");
	}
	
	public static void saveInfoToSharePreference(Context ctx,String userInfo) {
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		Editor meditor = sp.edit();	
		if (userInfo.equals("")) {
			meditor.clear();
		} else {
			String subStr1 = userInfo.substring(userInfo.indexOf("username"),
					userInfo.length());
			String userName = subStr1.substring(subStr1.indexOf(':') + 2,
					subStr1.indexOf(',') - 1);
			meditor.putString("login_name", userName);
			// handle openId
			String subStr2 = userInfo.substring(userInfo.indexOf("openid"),
					userInfo.length());
			String openId = subStr2.substring(subStr2.indexOf(':') + 2,
					subStr2.indexOf(',') - 1);
			Log.d("txhlog", "openId:" + openId);
			meditor.putString("openid", openId);
			// handle regType
			if (userInfo.indexOf("regtype") != -1) {
				String subStr3 = userInfo.substring(userInfo.indexOf("regtype"),
						userInfo.length());
				String regType = subStr3.substring(subStr3.indexOf(':') + 2,
						subStr3.indexOf(',') - 1);
				Log.d("txhlog", "regType:" + regType);
				meditor.putString("regType", regType);
			}
			// handle logoUrl
			String subStr4 = userInfo.substring(userInfo.indexOf("logoUrl"),
					userInfo.length());
			String logoUrl = subStr4.substring(subStr4.indexOf(':') + 2,
					subStr4.indexOf('}') - 1);
			if (logoUrl.startsWith("http:")) {
				while (logoUrl.contains("\\")) {
					String tmpstr1 = logoUrl.substring(0, logoUrl.indexOf("\\"));
					String tmpstr2 = logoUrl.substring(logoUrl.indexOf("\\") + 1,
							logoUrl.length());
					logoUrl = tmpstr1 + "" + tmpstr2;
				}
			} else {
				logoUrl = "";
			}
			meditor.putString("logoUrl", logoUrl);
			if (!logoUrl.equals("")) {
				new ImageAsynTask().execute(logoUrl, "logo");
			}
		}
		meditor.commit();
	}
	
	public static long getAutoSyncTime(Context ctx) {
		SharedPreferences sp = ctx.getSharedPreferences(AUTO_SYNC_TIME, Context.MODE_PRIVATE);
		return sp.getLong("auto_sync_time", 0);
	}

	public static void setAutoSyncTime(Context ctx, long time){
		SharedPreferences sp = ctx.getSharedPreferences(AUTO_SYNC_TIME, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putLong("auto_sync_time", time);
		et.commit();
	}
	
	public static int getInfoResult(Context ctx) {
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		return sp.getInt("personal_info_result", -1);
	}

	public static void setInfoResult(Context ctx,int result){
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putInt("personal_info_result", result);
		et.commit();
	}
	
	
	public static HashMap<String,String> getBleBindDevice(Context ctx) {
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		HashMap<String,String> bondDevicesMap = new HashMap<String,String>();
		
		String bindInfo = sp.getString("ble_bind_info", "");
		//String bindInfo = "Unik 1|F0:C5:19:9C:5B:12;Unik 1|F0:C5:19:9C:5B:13;LEO|F0:C5:19:9C:5B:14;";
		if(!TextUtils.isEmpty(bindInfo)){
			String bindInfoArr[] = bindInfo.split(";");
			for(int i=0;i<bindInfoArr.length;i++){
				String deviceInfo = bindInfoArr[i];
				//Log.d("yangyang","deviceInfo"+ i  + "="+deviceInfo);
				String deviceName = deviceInfo.substring(0, deviceInfo.lastIndexOf("|"));
				String deviceAddress = deviceInfo.substring(deviceInfo.lastIndexOf("|")+1, deviceInfo.length());
				//Log.d("yangyang","deviceName" + "="+deviceName);
				//Log.d("yangyang","deviceAddress" + "="+deviceAddress);
				bondDevicesMap.put(deviceName, deviceAddress);
			}
		}
		
		return bondDevicesMap;
	}
	
	public static void updateBleBindInfo(Context ctx,String deviceName, String deviceAddress){
		//eg. Unik 1|F0:C5:19:9C:5B:12;
		if (!TextUtils.isEmpty(deviceName) && !TextUtils.isEmpty(deviceAddress)) {
			SharedPreferences sp = ctx.getSharedPreferences(USR_INFO,Context.MODE_PRIVATE);
			String bindInfo = sp.getString("ble_bind_info", "");
			String addInfo = deviceName + "|" + deviceAddress + ";";
			if (!bindInfo.contains(addInfo)) {
				Editor et = sp.edit();
				bindInfo = bindInfo + addInfo;
				et.putString("ble_bind_info", bindInfo);
				et.commit();
				Log.d("yangyang", "setBleBindAddress sucess:"+deviceName+",add:"+deviceAddress);
			} else {
				//Log.d("yangyang", "setBleBindAddress failed, Repeat info !");
			}
		}
	}
	
	public static void removeBleBindInfo(Context ctx,String deviceName, String deviceAddress){
		//eg. Unik 1|F0:C5:19:9C:5B:12;
		if (!TextUtils.isEmpty(deviceName) && !TextUtils.isEmpty(deviceAddress)) {
			SharedPreferences sp = ctx.getSharedPreferences(USR_INFO,Context.MODE_PRIVATE);
			String bindInfo = sp.getString("ble_bind_info", "");
			String removeInfo = deviceName + "|" + deviceAddress + ";";
			Log.i("yangyang", "removeInfo:"+removeInfo);
			Log.i("yangyang", "bindInfo:"+bindInfo);
			if (bindInfo.contains(removeInfo)) {
				Editor et = sp.edit();
				bindInfo = bindInfo.replace(removeInfo, "");
				et.putString("ble_bind_info", bindInfo);
				et.commit();
				Log.d("yangyang", "removeBleBindInfo sucess");
			} else {
				//Log.d("yangyang", "setBleBindAddress failed, Repeat info !");
			}
		}
	}
		
    public synchronized static long getPKL() {
        String a = String.valueOf((System.currentTimeMillis()/10L)%100000000000L);
        String d = (String.valueOf((1+Math.random())*100000)).substring(1,6);
        long lo = Long.parseLong(a + d);
        for (int i = 0; i < 3000; i++) {
            long lt = ls[i];
            if (lt == lo) {
                lo = getPKL();
                break;
            }
        }
        ls[li] = lo;
        li++;
        if (li == 3000) {
            li = 0;
        }
        return lo;
    }
    
    /** 
     * 返回当前程序版本名称 
     */  
    public static String setAppVersionInfo(Context context) {
    	String info = "";
        try {  
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String mVerNameString = pi.versionName;
            String mAppNameString = pi.applicationInfo.loadLabel(context.getPackageManager()).toString();
            info = mAppNameString + " V" + mVerNameString;
        } catch (Exception e) {  
        	Log.e("VersionInfo", "Exception", e); 
        }
        return info;
    }
	
//    public static Integer DataToInteger(Date date) {
//    	String time = null;
//    	long st = System.currentTimeMillis();
//    	for(int i=0;i<100;i++){
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//			time = sdf.format(date);
//    	}
//		long ct = System.currentTimeMillis() - st;
//    	Log.d("ssss","DataToInteger time_int ="+time+";costtime= "+ct);
//		return Integer.valueOf(time);
//	}
//    
//    public static Date stringToDate(String date) {
//		Date time = null;
//    	long st = System.currentTimeMillis();
//    	for(int i=0;i<100;i++){
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//			try {
//				time = sdf.parse(date);
//			} catch (ParseException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//    	}
//		long ct = System.currentTimeMillis() - st;
//    	Log.d("ssss","DataToInteger stringToDate ="+date+";costtime= "+ct);
//		return time;
//	}
    
    
    public static Integer DataToInteger(Date date) {

    	int time_int = 0;
	    Calendar c = Calendar.getInstance();
        c.setTime(date);
		int year = c.get(Calendar.YEAR);
	    int month = c.get(Calendar.MONTH)+1;
	    int day =  c.get(Calendar.DAY_OF_MONTH);
	    time_int = year*10000+month*100+day;
	    return time_int;
	}
    
    public static Date stringToDate(String date) {
    	Date time = null;
	    Calendar c = Calendar.getInstance();
	    String a[] = date.split("-");
    	int year = Integer.valueOf(a[0]);
    	int month = Integer.valueOf(a[1]) - 1;
    	int day = Integer.valueOf(a[2]);
    	c.set(year, month, day);
    	time = c.getTime();
		return time;
	}
    
    
	 public static void deleteSDCardFolder(File dir) {
	       File to = new File(dir.getAbsolutePath() + System.currentTimeMillis());
	       dir.renameTo(to);
	       if (to.isDirectory()) {
	           String[] children = to.list();
	           for (int i = 0; i < children.length; i++) {
	               File temp = new File(to, children[i]);
	               if (temp.isDirectory()) {
	                   deleteSDCardFolder(temp);
	               } else {
	                   boolean b = temp.delete();
	                   if (b == false) {
	                       Log.d("deleteSDCardFolder", "DELETE FAIL");
	                   }
	               }
	           }
	           to.delete();
	       }
	 }

	public static void clearFeedTable(String name, Context sContext) {
		mDBOpenHelper = new DBOpenHelper(sContext);
		String sql = "DELETE FROM " + name + ";";
		SQLiteDatabase db = mDBOpenHelper.getWritableDatabase();
		db.execSQL(sql);
		revertSeq(name);
		mDBOpenHelper.close();
	}

	private static void revertSeq(String name) {
		String sql = "update sqlite_sequence set seq=0 where name='" + name + "'";
		SQLiteDatabase db = mDBOpenHelper.getWritableDatabase();
		db.execSQL(sql);
		mDBOpenHelper.close();
	}
	
	public static void setFirmwear(boolean flag){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(SP_SPP_FLAG_FILENAME, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putBoolean("firm_wear", flag);
		et.commit();
	}
	
	public static boolean getFirmwear(){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(SP_SPP_FLAG_FILENAME, Context.MODE_PRIVATE);
		return sp.getBoolean("firm_wear", false);
	}
	
	public static boolean getMsgState(Context ctx) {
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		return sp.getBoolean("msg_state", false);
	}

	public static void setMsgState(Context ctx,boolean state){
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putBoolean("msg_state", state);
		et.commit();
	}
	
	public static boolean getActState(Context ctx) {
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		return sp.getBoolean("act_state", false);
	}

	public static void setActState(Context ctx,boolean state){
		SharedPreferences sp = ctx.getSharedPreferences(USR_INFO, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putBoolean("act_state", state);
		et.commit();
	}
	
	public static int selectByIndex(int headIndex) {
		int resId = R.drawable.logo_default;
		int length1 = headIcon1.length;
		int length2 = headIcon2.length;
		int length3 = headIcon3.length;
		int length4 = headIcon4.length;
		if (headIndex < length1)
			resId = headIcon1[headIndex];
		else if (headIndex / 100 == 1 && (headIndex - 100) < length2)
			resId = headIcon2[headIndex - 100];
		else if (headIndex / 100 == 2 && (headIndex - 200) < length3)
			resId = headIcon3[headIndex - 200];
		else if (headIndex / 100 == 3 && (headIndex - 300) < length4)
			resId = headIcon4[headIndex - 300];
		return resId;
	}
	
	public static String getTimer(int time){
		int hour = (time%(3600*24)) / 3600;
		int min = time%3600 /60;
//		return ""+ hour + "时" + min + "分";
		return hour + mCont.getString(R.string.hour) + min + mCont.getString(R.string.sleep_minutes) ;
	}

	public static boolean fileIsExists(String path) {
		try {
			File f = new File(path);
			if (f.exists()) {
				return true;
			}

		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}

		return false;
	}
	
	public static String keyString(HashMap<String,String> map,String value) {
          Iterator<String> it= map.keySet().iterator();
          while(it.hasNext())
          {
        	  String keyString=it.next();
              if(map.get(keyString).equals(value))
                   return keyString;
           }
           return null;
	}
	
	public static String transformLongTime2StringFormat(long timestamp) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HHmmss");
		String time = "";
		try {
			java.util.Date date  = new Date(timestamp * 1000);
			time = sdf.format(date);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return time;
	}
	
	public static String transformLongTime2StringFormat2(long timestamp) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = "";
		try {
			java.util.Date date  = new Date(timestamp);
			time = sdf.format(date);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return time;
	}

	public static long transformUTCTime2LongFormat(long timestamp) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String sSime = "";
		long time = 0;
		try {
			java.util.Date date  = new Date(timestamp * 1000);
			sSime = sdf.format(date);
			time = Long.valueOf(sSime);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return time;
	}
	
	public static void saveAlarmBrain(String alarm){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(SP_SPP_FLAG_FILENAME, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putString("alarm_brain", alarm);
		et.commit();
	}
	
	public static String getAlarmBrain(){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(SP_SPP_FLAG_FILENAME, Context.MODE_PRIVATE);
		return sp.getString("alarm_brain", "");
	}
	
	
	public static void saveBatteryLevel(int level){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(SP_SPP_FLAG_FILENAME, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putInt("Battery_Level", level);
		et.commit();
	}
	
	public static int getBatteryLevel(){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(SP_SPP_FLAG_FILENAME, Context.MODE_PRIVATE);
		return sp.getInt("Battery_Level",100);
	}
	
	public static void setPhonePedState(boolean state){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(PHONE_PED, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putBoolean(PED_STATE, state);
		et.commit();
	}
	public static boolean getPhonePedState() {
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(PHONE_PED, Context.MODE_PRIVATE);
		return sp.getBoolean(PED_STATE, false);
	}
	
	public static void setPhoneSedentaryState(boolean state){
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(PHONE_SEDENTARY, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putBoolean(SEDENTARY_STATE, state);
		et.commit();
	}
	public static boolean getPhoneSedentaryState() {
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(PHONE_SEDENTARY, Context.MODE_PRIVATE);
		return sp.getBoolean(SEDENTARY_STATE, false);
	}
	
	
	public static void setConnectNotVibtation(boolean state) {
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(PHONE_PED, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putBoolean(SP_SPP_FLAG_FILENAME, state);
		et.commit();
	}
	public static boolean getConnectNotVibtation() {
		Context sContext = RunningApp.getInstance().getApplicationContext();
		SharedPreferences sp = sContext.getSharedPreferences(PHONE_PED, Context.MODE_PRIVATE);
		return sp.getBoolean(SP_SPP_FLAG_FILENAME, false);
	}
}
