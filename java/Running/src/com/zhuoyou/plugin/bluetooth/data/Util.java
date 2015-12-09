package com.zhuoyou.plugin.bluetooth.data;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import android.app.KeyguardManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;

import com.zhuoyou.plugin.bluetooth.connection.BtProfileReceiver;
import com.zhuoyou.plugin.bluetooth.service.BluetoothService;
import com.zhuoyou.plugin.running.R;

public class Util {

    private static final String LOG_TAG = "Util";
    private static int sMessageId = 0x0100;
    
    public final static String[] filterNames = {"EAMEY P1", "EAMEY P3", "Primo 1", "Primo 3", "TJ01", "Meegoo A10", "GEMINI1", "Megoo2", "S3","Unik 3","LUNA 3"};
    public final static String[] bleDevices = { "Unik 1" ,"Unik 2","LEO"};

    public static final int APP_ICON_WIDTH = 40;
    public static final int APP_ICON_HEIGHT = 40;
    public static final int NOTIFYMINIHEADERLENTH = 8;
    public static final int NOTIFYSYNCLENTH = 4;
    public static final int TEXT_MAX_LENGH = 256;
    public static final int TICKER_TEXT_MAX_LENGH = 128;
    public static final int TITLE_TEXT_MAX_LENGH = 128;
    public static final String TEXT_POSTFIX = "...";
    public static final String NULL_TEXT_NAME = "(unknown)";
    private static final String SP_DEVICE_FILENAME = "BLE_Device";
    private static final String SP_LATEST_DEVICE_FILENAME = "BLE_LATEST_Device";
    private static final String SP_DEVICE_KEY = "ble_address";
    private static final String SP_LATEST_DEVICE_KEY = "ble_latest_address";
    private static final String SP_DEVICE_TYPE = "ble_device_type";

    public static int genMessageId() {
        Log.i(LOG_TAG, "genMessageId(), messageId=" + sMessageId);

        return sMessageId++;
    }

    public static boolean isScreenLocked(Context context) {
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        Boolean isScreenLocked = km.inKeyguardRestrictedInputMode();
        
        Log.i(LOG_TAG, "isScreenOn(), isScreenOn=" + isScreenLocked);
        return isScreenLocked;
    }

    public static boolean isScreenOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        Boolean isScreenOn = pm.isScreenOn();
        
        Log.i(LOG_TAG, "isScreenOn(), isScreenOn=" + isScreenOn);
        return isScreenOn;
    }

    public static boolean isSystemApp(ApplicationInfo appInfo) {
        boolean isSystemApp = false; 
        if (((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                || ((appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)) {
            isSystemApp = true;
        }

        return isSystemApp;        
    }

    public static int getUtcTime(long localTime) {
        Log.i(LOG_TAG, "getUTCTime(), local time=" + localTime);
        
        // Get UTC time
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(localTime);
        
        // Transform to seconds
        int utcTime = (int) (cal.getTimeInMillis() / 1000);
        Log.i(LOG_TAG, "getUTCTime(), UTC time=" + utcTime);
        
        return utcTime;
    }

    public static int getUtcTimeZone(long localTime) {
        // Get UTC time zone
        TimeZone tz = TimeZone.getDefault();
        
        // Transform to seconds
        int tzs = tz.getRawOffset();
        Date dt = new Date(localTime);
        if(tz.inDaylightTime(dt)) {
            tzs += tz.getDSTSavings();
        }
        Log.i(LOG_TAG, "getUtcTimeZone(), UTC time zone=" + tzs);
        
        return tzs;
    }

    public static Date getAlarmTime(Context context) {

        String nextAlarm = Settings.System.getString(context.getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE HH:mm", Locale.getDefault());

        try {
            if (nextAlarm.isEmpty()) {
                return null;
            }
            return sdf.parse(nextAlarm);

        } catch (Exception e) {

        }

        try {
            sdf.applyLocalizedPattern("EE HH:mm");
            return sdf.parse(nextAlarm);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static ApplicationInfo getAppInfo(Context context, CharSequence packageName) {
        PackageManager packagemanager = context.getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = packagemanager.getApplicationInfo(packageName.toString(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        Log.i(LOG_TAG, "getAppInfo(), appInfo=" + appInfo);
        return appInfo;
    }

    public static String getAppName(Context context, ApplicationInfo appInfo) {
        String appName;
        if ((context == null) || (appInfo == null)) {
            appName = NULL_TEXT_NAME;
        } else {
            appName = context.getPackageManager().getApplicationLabel(appInfo).toString();
        }

        Log.i(LOG_TAG, "getAppName(), appName=" + appName);
        return appName;
    }

    public static Bitmap getAppIcon(Context context, ApplicationInfo appInfo) {
        Log.i(LOG_TAG, "getAppIcon()");
        
        return createIcon(context, appInfo, true);
    }

    public static Bitmap getMessageIcon(Context context, ApplicationInfo appInfo) {
        Log.i(LOG_TAG, "getMessageIcon()");
        
        return createIcon(context, appInfo, false);
    }

    private static Bitmap createIcon(Context context, ApplicationInfo appInfo, boolean isAppIcon) {        
    	Bitmap icon;
    	if ((context == null) || (appInfo == null)) {
    		icon = null;
    	} else {
		    Drawable drawable = context.getPackageManager().getApplicationIcon(appInfo);
		   
		    if (isAppIcon) {
		        icon =  Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
		    } else {
		        icon = createWhiteBitmap();
		    }
		   
		    Canvas canvas = new Canvas(icon);
		    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		    drawable.draw(canvas);
    	}
	
    	Log.i(LOG_TAG, "createIcon(), icon width=" + icon.getWidth());
    	return icon;
    }

    private static Bitmap createWhiteBitmap() {
        Bitmap whiteBitmap = Bitmap.createBitmap(APP_ICON_WIDTH, APP_ICON_HEIGHT, Config.RGB_565);
        int[] pixels = new int[APP_ICON_WIDTH * APP_ICON_HEIGHT];

        for (int y = 0; y < APP_ICON_HEIGHT; y++) {
        	for (int x = 0; x < APP_ICON_WIDTH; x++) {
                int index = y * APP_ICON_WIDTH + x;
                int r = ((pixels[index] >> 16) & 0xff) | 0xff;
                int g = ((pixels[index] >> 8) & 0xff) | 0xff;
                int b = (pixels[index] & 0xff) | 0xff;
                pixels[index] = 0xff000000 | (r << 16) | (g << 8) | b;
            }
        }
       
        Log.i(LOG_TAG, "createWhiteBitmap(), pixels num=" + pixels.length);
        whiteBitmap.setPixels(pixels, 0, APP_ICON_WIDTH, 0, 0, APP_ICON_WIDTH, APP_ICON_HEIGHT);
        return whiteBitmap;
    }

    public static String getContactName(Context context, String phoneNum) {
        // Lookup contactName from phonebook by phoneNum
        if (phoneNum == null) {
            return null;
        } else if (phoneNum.equals("")) {
            return null;
        } else {
            try {
                String contactName = phoneNum;
                Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(contactName));
                Cursor cursor = context.getContentResolver().query(uri, new String[] { "display_name" }, null, null, null);
                if ((cursor != null) && cursor.moveToFirst()) {
                    contactName = cursor.getString(0);
                }
                cursor.close();
                Log.i(LOG_TAG, "getContactName(), contactName=" + contactName);
                return contactName;
            } catch (Exception e) {
                Log.i(LOG_TAG, "getContactName Exception");
                return null;
            }
        }

    }

    public static String getKeyFromValue(CharSequence charSequence) {
        Map<Object, Object> appList = AppList.getInstance().getAppList();
        Set<?> set= appList.entrySet();
        Iterator<?> it=set.iterator();
        String key = null;
        while(it.hasNext()) {
             @SuppressWarnings("rawtypes")
            Map.Entry entry=(Map.Entry)it.next();
             if(entry.getValue() != null && entry.getValue().equals(charSequence)) {
                  key = entry.getKey().toString();
                 break;
             }
        }
        return key;
    }

    public static void setUnreadSmsToRead(Context ctx, int id) {
		Uri sms = Uri.parse("content://sms/inbox");
		Cursor c = ctx.getContentResolver().query(sms, null, "_id = " + id, null, null);
		if(c.moveToFirst()) {
			int read = c.getInt(c.getColumnIndex("read"));
			if(read == 0) {
				ContentValues cv = new ContentValues();
				cv.put("read", 1);
				int ret = ctx.getContentResolver().update(sms, cv, "_id = " + id  , null);
				Log.i("gchk", "msg id = " + id  + "  成功设置为已读ret =" + ret);
			} else {
				Log.i("gchk", "msg id = " + id  + "  在此之前已经被设置为已读");
			} 
		} else {
			Log.i("gchk", "没找到msg id = " + id);
		}
    }

	public static String getProductName(String name) {
		String productName = "";
		if (name != null) {
			if (name.startsWith("EAMEY P1")) {
				productName = "EAMEY P1";
			} else if (name.startsWith("EAMEY P3")) {
				productName = "EAMEY P3";
			}else if (name.startsWith("Primo 1")) {
				productName = "Primo 1";
			} else if (name.startsWith("Primo 3")) {
				productName = "Primo 3";
			}else if (name.startsWith("TJ01")) {
				productName = "TJ01";
			} else if (name.startsWith("Meegoo A10")) {
				productName = "Meegoo A10";
			} else if (name.startsWith("GEMINI1")) {
				productName = "GEMINI1";
			} else if (name.startsWith("Megoo2")) {
				productName = "Megoo2";
			} else if(name.startsWith("Unik 3")){
				productName = "Unik 3" ;
			}else if (name.startsWith("LUNA3")) {
				productName = "LUNA3";
			} else if (name.startsWith("Unik 1")) {
				productName = "Unik 1";
			} else if(name.startsWith("Unik 2")) {
				productName = "Unik 2";
			}else if(name.startsWith("LEO")) {
				productName = "LEO";
			}else if(name.startsWith("S3")) {
				productName = "S3";
			}

		}
		return productName;
	}
	
	public static boolean isBleDevice(String deviceName){
		for(int i = 0 ;i<bleDevices.length;i++){
			if(bleDevices[i].equals(deviceName)){
				return true;
			}
		}
		return false;
	}
	
	public static int getIconByDeviceName(String name, boolean flag) {
		int resid = 0;
		String productName = getProductName(name);
		if (!productName.equals("")) {
			resid = getProductIcon(productName, flag);
		}
		return resid;
	}
//	Heph add 20150609
	public static int getSearchByDeviceName(String name, boolean flag) {
		int resid = 0;
		String productName = getProductName(name);
		if (!productName.equals("")) {
			resid = getSearchIcon(productName, flag);
		}
		return resid;
	}
	public static int getSearchIcon(String name, boolean flag) {
		int resid = 0;
		if(name != null) {
			if(name.startsWith("LEO")) {
				resid = R.drawable.search_leo;
			}else if(name.startsWith("Unik 1")) {
				resid = R.drawable.search_luna1;
		    }else if(name.startsWith("LUNA3")) {
			resid = R.drawable.search_luna3;
		    }else if(name.startsWith("Unik 2")) {
		    	resid = R.drawable.search_leo;
		    }
	    }
		return resid;
    }	
	public static int getProductIcon(String name, boolean flag) {
		int resid = 0;
		if (flag) {
			resid = R.drawable.p1_connect;
			if (name != null) {
				if (name.startsWith("EAMEY P1")) {
					resid = R.drawable.p1_connect;
				} else if (name.startsWith("EAMEY P3")) {
					resid = R.drawable.p3_connect;
				} else if (name.startsWith("Primo 1")) {
					resid = R.drawable.p1_connect;
				} else if (name.startsWith("Primo 3")) {
					resid = R.drawable.p3_connect;
				} else if (name.startsWith("TJ01")) {
					resid = R.drawable.t1_connect;
				} else if (name.startsWith("Meegoo A10")) {
					resid = R.drawable.a1_connect;
				} else if (name.startsWith("Megoo2")) {
					resid = R.drawable.m2_connect;
				} else if (name.startsWith("LUNA3")) {
					resid = R.drawable.luna3_connect;
				} else if (name.startsWith("Unik 3")){
					resid = R.drawable.luna3_connect;
				} else if (name.startsWith("Unik 1")) {
					resid = R.drawable.luna1_connect;
				} else if (name.startsWith("Unik 2")) {
					resid = R.drawable.leo_connect;
				}else if (name.startsWith("LEO")) {
					resid = R.drawable.leo_connect;
				}else if (name.startsWith("S3")) {
					resid = R.drawable.s3_connect;
				}
			}
		} else {
			resid = R.drawable.p1_disconnect;
			if (name != null) {
				if (name.startsWith("EAMEY P1")) {
					resid = R.drawable.p1_disconnect;
				} else if (name.startsWith("EAMEY P3")) {
					resid = R.drawable.p3_disconnect;
				} else if (name.startsWith("Primo 1")) {
					resid = R.drawable.p1_disconnect;
				} else if (name.startsWith("Primo 3")) {
					resid = R.drawable.p3_disconnect;
				} else if (name.startsWith("TJ01")) {
					resid = R.drawable.t1_disconnect;
				} else if (name.startsWith("Meegoo A10")) {
					resid = R.drawable.a1_disconnect;
				} else if (name.startsWith("Megoo2")) {
					resid = R.drawable.m2_disconnect;
				} else if (name.startsWith("Unik 3")){
					resid = R.drawable.luna3_disconnect ;
				}else if (name.startsWith("LUNA3")) {
					resid = R.drawable.luna3_disconnect;
				} else if (name.startsWith("Unik 1")) {
					resid = R.drawable.luna1_disconnect;
				} else if (name.startsWith("Unik 2")) {
					resid = R.drawable.leo_disconnect;
				} else if (name.startsWith("LEO")) {
					resid = R.drawable.leo_disconnect;
				} else if (name.startsWith("S3")) {
					resid = R.drawable.s3_disconnect;
				}
			}
		}
		return resid;
	}
	
	public static boolean getLatestDeviceType(Context sContext){
		SharedPreferences sp = sContext.getSharedPreferences(SP_DEVICE_FILENAME, Context.MODE_PRIVATE);
		return sp.getBoolean(SP_DEVICE_TYPE, false);
	}


	public static void setLatestDeviceType(Context sContext, boolean deviceType){
		SharedPreferences sp = sContext.getSharedPreferences(SP_DEVICE_FILENAME, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putBoolean(SP_DEVICE_TYPE, deviceType);
		Log.i(LOG_TAG, "deviceType:"+deviceType);
		et.commit();
	}
	
	public static String getLatestConnectDeviceAddress(Context sContext){
		SharedPreferences sp = sContext.getSharedPreferences(SP_DEVICE_FILENAME, Context.MODE_PRIVATE);
		return sp.getString(SP_DEVICE_KEY, "");
	}
	
	public static void updateLatestConnectDeviceAddress(Context sContext, String address){
		SharedPreferences sp = sContext.getSharedPreferences(SP_DEVICE_FILENAME, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putString(SP_DEVICE_KEY, address);
		Log.i(LOG_TAG, "BLEaddress:"+address);
		et.commit();
	}
	
	public static String getLLatsetConnectDeviceAddress(Context sContext){
		SharedPreferences sp = sContext.getSharedPreferences(SP_LATEST_DEVICE_FILENAME, Context.MODE_PRIVATE);
		return sp.getString(SP_LATEST_DEVICE_KEY, "");
	}
	
	public static void updateLLatestConnectDeviceAddress(Context sContext,String address) {
		SharedPreferences sp = sContext.getSharedPreferences(SP_LATEST_DEVICE_FILENAME, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putString(SP_LATEST_DEVICE_KEY, address);
		et.commit();
	}
	
	public static List<BluetoothDevice> getBondDevice() {
		List<BluetoothDevice> bondDevices = new ArrayList<BluetoothDevice>();
		BluetoothAdapter btAdapt = BluetoothAdapter.getDefaultAdapter();
		Object[] listDevice = btAdapt.getBondedDevices().toArray();
		for (int i = 0; i < listDevice.length; i++) {
			BluetoothDevice device = (BluetoothDevice) listDevice[i];
			for (int j = 0; j < filterNames.length; j++) {
				if (filterNames[j].equals(device.getName())) {
					bondDevices.add(device);
					break;
				}
			}
		}
		return bondDevices;
	}

	public static BluetoothDevice getConnectDevice() {
		BluetoothDevice connectDevice = null;
		if (BluetoothService.getInstance().isConnected()) {
			BluetoothDevice device = BtProfileReceiver.getRemoteDevice();
			if (device != null) {
				connectDevice = device;
			}
		} else {
			connectDevice = null;
		}
		return connectDevice;
	}
	
	public static void connect(BluetoothDevice device) {
		if (BluetoothService.getInstance() != null)
			BluetoothService.getInstance().connectToRemoteDevice(device);
	}
	
	public static void removeBond(BluetoothDevice device) {
		try {
			Method removeBondMethod = BluetoothDevice.class.getMethod("removeBond");
			removeBondMethod.invoke(device);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void bondDevice(BluetoothDevice device) {
		try {
			Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
			createBondMethod.invoke(device);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void handUnlink(Context sContext,boolean isHand){
		SharedPreferences sp = sContext.getSharedPreferences(SP_DEVICE_FILENAME, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putBoolean("isHandUnlink", isHand);
		et.commit();
	}
	
	public static boolean isHandUnlink(Context sContext){
		SharedPreferences sp = sContext.getSharedPreferences(SP_DEVICE_FILENAME, Context.MODE_PRIVATE);
		return sp.getBoolean("isHandUnlink", true);
	}
	
	//获取设置--蓝牙界面已经连接的设备
	private static BluetoothHeadset mBluetoothHeadset;
	private static BluetoothDevice getBTProxy(Context context) {
		BluetoothDevice remoteDevice = null;
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET) == BluetoothProfile.STATE_CONNECTED
                || mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET) == BluetoothProfile.STATE_CONNECTING)
			mBluetoothAdapter.getProfileProxy(context, mProfileListener, BluetoothProfile.HEADSET);
		if (mBluetoothHeadset != null) {
			List<BluetoothDevice> deviceList = mBluetoothHeadset.getConnectedDevices();
			if (deviceList != null && deviceList.size() > 0) {
				Log.i("caixinxin", "deviceList = " + deviceList);
                BluetoothDevice device = deviceList.get(0);
                String name = device.getName();
                if (name != null) {
        			for (int j = 0; j < filterNames.length; j++) {
        				if (filterNames[j].equals(name)) {
        					remoteDevice = device;
        					break;
        				}
        			}
                }
            }
		}
		mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
		
		return remoteDevice;
	}
	
	private static BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
		public void onServiceConnected(int profile, BluetoothProfile proxy) {
		    if (profile == BluetoothProfile.HEADSET) {
		        mBluetoothHeadset = (BluetoothHeadset) proxy;
		    }
		}
		public void onServiceDisconnected(int profile) {
		    if (profile == BluetoothProfile.HEADSET) {
		        mBluetoothHeadset = null;
		    }
		}
	};

	public static void autoConnect(Context context) {
		BluetoothDevice currentDevice = getBTProxy(context);
		if (currentDevice != null && !BluetoothService.getInstance().isConnected()) {
			connect(currentDevice);
		}
	}
	
	
	 public static int dip2pixel(Context paramContext, float paramFloat)
	  {
	    return (int)TypedValue.applyDimension(1, paramFloat, paramContext.getResources().getDisplayMetrics());
	  }

}
