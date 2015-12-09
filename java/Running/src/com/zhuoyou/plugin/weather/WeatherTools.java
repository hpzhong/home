package com.zhuoyou.plugin.weather;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.zhuoyou.plugin.bluetooth.connection.BtProfileReceiver;
import com.zhuoyou.plugin.bluetooth.data.Util;
import com.zhuoyou.plugin.bluetooth.service.BluetoothService;
import com.zhuoyou.plugin.cloud.NetUtils;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;

@SuppressLint("SdCardPath")
public class WeatherTools implements BDLocationListener {
	private static WeatherTools mInstance = null;
	private Context mCtx = RunningApp.getInstance();
	private String DATABASE_NAME = "city.db";
	private String DATABASE_PATH = "/data/data/" + mCtx.getPackageName() + "/databases/";
	private LocationClient mLocationClient = null;
	private static final long EXPIRESIN_TIME = 1000 * 60 * 60 * 6;// 6小时
	private static final String JSON_KEY_CITYNAME = "cityname";
	private static final String JSON_KEY_RESULT = "result";
	private static final String JSON_KEY_TODAY = "todayWeather";
	private static final String JSON_KEY_TODAY_DETAIL = "detail";
	private static final String JSON_KEY_TODAY_AIR = "air";
	private static final String JSON_KEY_TODAY_TEMP = "temperature";
	private static final String JSON_KEY_TODAY_WEATHER = "weather";
	private static final String JSON_KEY_TODAY_WIND = "wind";
	private static final String JSON_KEY_PM25 = "pm25";
	private static final String JSON_KEY_PM25_AQI = "aqi";

	public static WeatherTools newInstance() {
		if (mInstance == null) {
			mInstance = new WeatherTools();
		}
		return mInstance;
	}

	public WeatherTools() {
		createCityDatabase(mCtx);
	}

	/**
	 * 外部调用该函数去获取当前AQI，获取成功后数据库会更新。 外部只要监听数据库就型了。此处不返回任何数据
	 */
	public void getCurrWeather() {
		if (NetUtils.getAPNType(mCtx) == -1) {
			Toast.makeText(mCtx, R.string.check_network, Toast.LENGTH_SHORT).show();
			return;
		}

		// 如果定位数据不合法，先定位。
		if (!isLocationVaild()) {
			//复位
			if (mLocationClient != null) {
				mLocationClient.stop();
				mLocationClient.unRegisterLocationListener(this);
				mLocationClient = null;
			}
			// 设置定位参数
			mLocationClient = new LocationClient(mCtx);
			mLocationClient.registerLocationListener(this);
			LocationClientOption option = new LocationClientOption();
			option.setCoorType("bd09ll");
			option.setPriority(LocationClientOption.NetWorkFirst);
			option.setProdName("LocationDemo");
			option.setAddrType("all");
			mLocationClient.setLocOption(option);

			new Thread() {
				@Override
				public void run() {
					requestLocation();
				}
			}.start();
		} else {
			getWeatherData();
		}
	}

	private boolean isLocationVaild() {
		String region = getRegion();
		long expiresin = getExpiresin();

		return (!TextUtils.isEmpty(region) && ((expiresin == 0L) || (System.currentTimeMillis() < expiresin)));
	}

	private void requestLocation() {
		if (mLocationClient != null && mLocationClient.isStarted())
			mLocationClient.requestLocation();
		else {
			mLocationClient.start();
			mLocationClient.requestLocation();
		}
	}

	// 设置定位的 城市
	private void setRegion(String region) {
		SharedPreferences mSharedPreferences = mCtx.getSharedPreferences("app_config", 0);
		SharedPreferences.Editor mEditor = mSharedPreferences.edit();
		mEditor.putString("location_region", region);
		mEditor.commit();
	}

	// 获取所保存的当前位置的 城市
	public String getRegion() {
		SharedPreferences mSharedPreferences = mCtx.getSharedPreferences("app_config", 0);
		return mSharedPreferences.getString("location_region", "");
	}

	// 设置过期时间
	private void setExpiresin(long time) {
		SharedPreferences mSharedPreferences = mCtx.getSharedPreferences("app_config", 0);
		SharedPreferences.Editor mEditor = mSharedPreferences.edit();
		mEditor.putLong("location_expiresin", time);
		mEditor.commit();
	}

	// 获取过期时间
	private long getExpiresin() {
		SharedPreferences mSharedPreferences = mCtx.getSharedPreferences("app_config", 0);
		return mSharedPreferences.getLong("location_expiresin", 0);
	}

	private String startConnectUrl(String url) {
		String result = null;
		InputStream is = null;
		BufferedInputStream bis = null;
		ByteArrayBuffer baf = null;
		try {
			URL myURL = new URL(url);
			URLConnection ucon = myURL.openConnection();
			ucon.setConnectTimeout(15 * 1000);
			ucon.setReadTimeout(30 * 1000);
			is = ucon.getInputStream();
			bis = new BufferedInputStream(is);
			baf = new ByteArrayBuffer(1024);
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}
			result = EncodingUtils.getString(baf.toByteArray(), "UTF-8");
			if (null != result && result.equals("zero")) {
				result = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (baf != null) {
				baf.clear();
				baf = null;
			}
			try {
				if (bis != null) {
					bis.close();
					bis = null;
				}

				if (is != null) {
					is.close();
					is = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	private void getCurrAqiEtc(String result) {
		int aqi = -1;
		Log.i("gchk", "" + result);

		try {
			JSONObject allWeather = new JSONObject(result);
			String cityname = allWeather.getString(JSON_KEY_CITYNAME);
			int ret = allWeather.getInt(JSON_KEY_RESULT);
			if (ret == 0) {
				JSONObject todayWeather = allWeather.getJSONObject(JSON_KEY_TODAY);
				String detail = todayWeather.getString(JSON_KEY_TODAY_DETAIL);
				String air = todayWeather.getString(JSON_KEY_TODAY_AIR);
				String temperature = todayWeather.getString(JSON_KEY_TODAY_TEMP);
				String weather = todayWeather.getString(JSON_KEY_TODAY_WEATHER);
				String wind = todayWeather.getString(JSON_KEY_TODAY_WIND);
				JSONObject pm25 = allWeather.getJSONObject(JSON_KEY_PM25);
				aqi = pm25.getInt(JSON_KEY_PM25_AQI);
				String pm_quality = pm25.getString("pm_quality");
				
				StringBuilder comment = new StringBuilder();
				String[] details = detail.split("\\：|\\；");
				String[] airs = air.split("\\：|\\；");
				String[] temperatures = temperature.split("/");
				String[] weathers = weather.split("\\ ");
				comment.append(cityname + "|");
				if (details.length > 2)
					comment.append(details[2] + "|");
				else
					comment.append("无|");
				if (details.length > 6)
					comment.append(details[6] + "|");
				else
					comment.append("无|");
				
				if(airs.length>3){ comment.append(airs[3] + "|");}
				
				comment.append(temperatures[0] + "|");
				comment.append(temperatures[1] + "|");
				comment.append(weathers[1] + "|");
				comment.append(wind + "|");
				comment.append(aqi + "|");
				comment.append(pm_quality + "|");
				BluetoothDevice currRemoteDevice = BtProfileReceiver.getRemoteDevice();
				if (currRemoteDevice != null && BluetoothService.getInstance().isConnected() && Util.getProductName(currRemoteDevice.getName()).equals("TJ01")) {
					Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
					intent.putExtra("plugin_cmd", 0x04);
					intent.putExtra("plugin_content", comment.toString());
					mCtx.sendBroadcast(intent);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e("gchk", "get aqi error =" + e.getMessage());
		}
		Log.i("gchk", "aqi = " + aqi);
		// save to aqi
		if (aqi > 0) {
			Tools.updatePm25(Tools.getDate(0), aqi);
		}

	}

	/**
	 * 把raw中的城市数据库存到database下
	 * 
	 * @param context
	 */
	private boolean createCityDatabase(Context context) {
		boolean ret = false;
		String outFileName = DATABASE_PATH + DATABASE_NAME;
		// 检查文件是否存在。
		File dir = new File(outFileName);
		if (dir.exists()) {
			// 如果文件存在直接退出。
			return true;
		}
		// 如果文件不存在。检查database文件夹是否存在
		dir = new File(DATABASE_PATH);
		if (!dir.exists()) {
			dir.mkdir();
		}
		InputStream input = null;
		OutputStream output = null;
		input = context.getResources().openRawResource(R.raw.city);
		try {
			output = new FileOutputStream(outFileName);
			byte[] buffer = new byte[2048];
			int length;
			while ((length = input.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}
			ret = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ret = false;
		} catch (IOException e) {
			e.printStackTrace();
			ret = false;
		} finally {
			try {
				output.flush();
				output.close();
			} catch (IOException e) {
			}
			try {
				input.close();
			} catch (IOException e) {
			}
		}
		return ret;
	}

	@Override
	public void onReceiveLocation(BDLocation location) {
		if (mLocationClient != null) {
			mLocationClient.stop();
			mLocationClient.unRegisterLocationListener(this);
			mLocationClient = null;
		}

		Log.e("gchk", "onReceiveLocation");
		if (location != null && location.getLocType() == BDLocation.TypeNetWorkLocation) {
			String region = location.getCity();
			if(region != null && !region.equals("")) {
				Log.e("gchk", region);
				setRegion(region);
				setExpiresin(System.currentTimeMillis() + EXPIRESIN_TIME);
				getWeatherData();
			}else {
				Tools.updatePm25(Tools.getDate(0), -1);
			}
		}
	}

	@Override
	public void onReceivePoi(BDLocation arg0) {
		// TODO Auto-generated method stub

	}

	private void getWeatherData() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				postWeatherData();
			}
		}).start();
	}

	private int postWeatherData() {
		if (!isLocationVaild()) {
			return -1;
		}

		String city = getRegion();
		city = city.substring(0, city.length() - 1);
		int code = -1;
		SQLiteDatabase db = SQLiteDatabase.openDatabase(DATABASE_PATH + DATABASE_NAME, null, SQLiteDatabase.OPEN_READONLY);
		Cursor cursor = db.rawQuery("select code from city where city = '" + city + "'", null);
		if (cursor.getCount()>0 && cursor.moveToFirst()) {
			code = cursor.getInt(cursor.getColumnIndex("code"));
			String url = "http://newweather.oo523.com:8080/=/pm2_5?cityid=" + code + "&mode=new" + "&format=json";
			String result = startConnectUrl(url);
			if (result != null) {
				try{
					getCurrAqiEtc(result);
				}catch(Exception e){
					Log.i("gchk", "result has error");
				}
			}
		} else {
			Log.i("gchk", "未获得城市 code ");
		}
		cursor.close();
		db.close();
		return 1;
	}

	public String[] selectProvice() {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(DATABASE_PATH
				+ DATABASE_NAME, null, SQLiteDatabase.OPEN_READONLY);
		Cursor cursor = db.rawQuery("select distinct provice from city ", null);
		String[] provice = new String[cursor.getCount()];
		if (cursor.getCount() > 0 && cursor.moveToFirst()) {
			for (int i = 0; i < cursor.getCount(); i++) {
				provice[i] = (cursor
						.getString(cursor.getColumnIndex("provice")));
				cursor.moveToNext();
			}
		} else {
			Log.i("txhlog", "未获得provice  ");
		}
		cursor.close();
		db.close();
		return provice;
	}
	
	public String[] selectCity(Context con,String provice) {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(DATABASE_PATH
				+ DATABASE_NAME, null, SQLiteDatabase.OPEN_READONLY);
		Cursor cursor = db.rawQuery("select city from city where provice = '"+provice+"'", null);
		String[] city = new String[cursor.getCount()];
		if (cursor.getCount() > 0 && cursor.moveToFirst()) {
			for (int i = 0; i < cursor.getCount(); i++) {
				String temp = cursor.getString(cursor.getColumnIndex("city"));
				if (temp.equals(con.getResources().getString(R.string.counties)))
					temp = con.getResources().getString(R.string.city);
				city[i] = temp;
				cursor.moveToNext();
			}
		} else {
			Log.i("txhlog", "未获得city ");
		}
		cursor.close();
		db.close();
		return city;
	}
}
