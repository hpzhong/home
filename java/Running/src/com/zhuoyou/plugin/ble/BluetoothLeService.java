package com.zhuoyou.plugin.ble;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.zhuoyou.plugin.bluetooth.connection.CustomCmd;
import com.zhuoyou.plugin.bluetooth.data.Util;
import com.zhuoyou.plugin.database.DataBaseUtil;
import com.zhuoyou.plugin.firmware.FirmwareService;
import com.zhuoyou.plugin.mainFrame.MineFragment;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;

/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
@SuppressLint({ "SimpleDateFormat", "NewApi" })
public class BluetoothLeService extends Service {
	static final String TAG = "BluetoothLeService";
	
	private static final Context sContext = RunningApp.getInstance().getApplicationContext();
    private static final SharedPreferences sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(sContext);

	public final static String ACTION_GATT_CONNECTED = "com.zhuoyou.running.ble.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "com.zhuoyou.running.ble.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.zhuoyou.running.ble.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_READ = "com.zhuoyou.running.ble.ACTION_DATA_READ";
	public final static String ACTION_DATA_NOTIFY = "com.zhuoyou.running.ble.ACTION_DATA_NOTIFY";
	public final static String ACTION_DATA_WRITE = "com.zhuoyou.running.ble.ACTION_DATA_WRITE";
	public final static String ACTION_DATABASE_CHANGE = "com.zhuoyou.running.ble.ACTION_DATABASE_CHANGE";
	public final static String EXTRA_DATA = "com.zhuoyou.running.ble.EXTRA_DATA";
	public final static String EXTRA_UUID = "com.zhuoyou.running.ble.EXTRA_UUID";
	public final static String EXTRA_STATUS = "com.zhuoyou.running.ble.EXTRA_STATUS";
	public final static String EXTRA_ADDRESS = "com.zhuoyou.running.ble.EXTRA_ADDRESS";
	public final static String LEADOFF_STATUS = "com.zhuoyou.running.ble.LEADOFF_STATUS";
	public final static String ACTION_BACK_CONNECT = "com.zhuoyou.running.ble.ACTION_BACK_CONNECT";
	public final static int BACK_CONNECT_DELAY = 1;
	public final static int BACK_CONNECT_OPERATION = 2;
	// BLE
	private BluetoothManager mBluetoothManager = null;
	private BluetoothAdapter mBtAdapter = null;
	private BluetoothGatt mBluetoothGatt = null;
	private static BluetoothLeService mThis = null;
	private volatile boolean mBusy = false; // Write/read pending response
	private String mBluetoothDeviceAddress;

    public final static UUID UUID_HEART_RATE_MEASUREMENT = GattInfo.HEART_RATE_MEASUREMENT;
    public final static UUID UUID_BATTERY_MEASUREMENT = GattInfo.BATTERY_LEVEL;
    public final static UUID UUID_STEPS_SERVICE = GattInfo.STEPS_SERVICE;
    public final static UUID UUID_SEGMENT_STEPS_MEASUREMENT = GattInfo.SEGMENT_STEPS_MEASUREMENT;
    public final static UUID UUID_TOTAL_STEPS_MEASUREMENT = GattInfo.TOTAL_STEPS_MEASUREMENT;
    public final static UUID UUID_SLEEP_INFO = GattInfo.SLEEP_INFO_CHAR;
    public final static UUID UUID_TIME_INFO = GattInfo.TIME_AND_ALARM_INFO;

    private String deviceName = "";
    private String deviceAddress = "";
       //for analyze Sleep Info
    private CustomHandler customHandler = null;
    private int mSleepInfoPart = 1;
    private int curr_index;
    private int totle_number;
    private byte[] sleepInfoByte = new byte[40];
    private HashMap<String, String> bleBindMap;
	/**
	 * GATT client callbacks
	 */
	private BluetoothGattCallback mGattCallbacks = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			
			if (mBluetoothGatt == null) {
				Log.e(TAG, "mBluetoothGatt not created!");
				return;
			}
			BluetoothDevice device = gatt.getDevice();
			//Sometimes,this method always return null;
			deviceName = device.getName();
			deviceAddress = device.getAddress();
			Log.d(TAG, "onConnectionStateChange (" + deviceAddress + ") " + newState+ " status: " + status);
			Intent intent;
			try {
				switch (newState) {
				case BluetoothProfile.STATE_CONNECTED:
					relinkHandler.removeMessages(BACK_CONNECT_DELAY);
					relinkHandler.removeMessages(BACK_CONNECT_OPERATION);
					Util.handUnlink(BluetoothLeService.this, false);

					BleManagerService.low_battery_remind = 1;
					updateConnectInfo(true, deviceName, deviceAddress);
					Util.setLatestDeviceType(getApplicationContext(), true);
					Util.updateLatestConnectDeviceAddress(getApplicationContext(), deviceAddress);
					Util.updateLLatestConnectDeviceAddress(getApplicationContext(), deviceAddress);
					RunningApp.setCurrentConnectDeviceType(deviceAddress);
					broadcastUpdate(ACTION_GATT_CONNECTED, deviceAddress,status);
					mBluetoothGatt.discoverServices();
					intent = new Intent();
					intent.setAction("com.zhuoyou.running.connect.success");
					sendBroadcast(intent);
					Intent closePhoneStepsIntent = new Intent(BleManagerService.ACTION_CLOSE_BLE_PHONE_STEPS);
					sendBroadcast(closePhoneStepsIntent);
					break;
				case BluetoothProfile.STATE_DISCONNECTED:
					updateConnectInfo(false, deviceName, deviceAddress);
					Util.updateLLatestConnectDeviceAddress(getApplicationContext(), "");
					broadcastUpdate(ACTION_GATT_DISCONNECTED, deviceAddress,status);
					relinkHandler.sendEmptyMessage(BACK_CONNECT_DELAY);
					break;
				default:
					Log.e(TAG, "New state not processed: " + newState);
					break;
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}


		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
	        if (status == BluetoothGatt.GATT_SUCCESS) {
				BluetoothDevice device = gatt.getDevice();
				broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED,device.getAddress(), status);
	        } else {
	            Log.w(TAG, "onServicesDiscovered received: " + status);
	        }

		}    
		
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic) {
			Log.i(TAG, "onCharacteristicChanged");
			broadcastUpdate(ACTION_DATA_NOTIFY, characteristic,BluetoothGatt.GATT_SUCCESS);
			
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
    			broadcastUpdate(ACTION_DATA_READ, characteristic, status);
            }
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic, int status) {
			broadcastUpdate(ACTION_DATA_WRITE, characteristic, status);
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt,BluetoothGattDescriptor descriptor, int status) {
			mBusy = false;
			Log.i(TAG, "onDescriptorRead");
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,BluetoothGattDescriptor descriptor, int status) {
			mBusy = false;
			Log.i(TAG, "onDescriptorWrite");
		}
		
		
	};

	
	
	@SuppressLint("SimpleDateFormat")
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		deviceAddress = Util.getLatestConnectDeviceAddress(sContext);
		bleBindMap = Tools.getBleBindDevice(sContext);
        deviceName = Tools.keyString(bleBindMap, deviceAddress);
        Message msg = new Message();
        msg.what = BACK_CONNECT_DELAY;
        relinkHandler.sendMessage(msg);
		//We don't want run this thread on UI thread  
		HandlerThread handlerThread = new HandlerThread("handler_thread");
		handlerThread.start();
		customHandler = new CustomHandler(handlerThread.getLooper());
		
	}

	private void broadcastUpdate(final String action, final String address,final int status) {
		final Intent intent = new Intent(action);
		intent.putExtra(EXTRA_ADDRESS, address);
		intent.putExtra(EXTRA_STATUS, status);
		sendBroadcast(intent);
		mBusy = false;
	}

	private void broadcastUpdate(final String action,final BluetoothGattCharacteristic characteristic, final int status) {
        
        //notice data here
        if(ACTION_DATA_NOTIFY.equals(action)){
        	if(UUID_TOTAL_STEPS_MEASUREMENT.equals(characteristic.getUuid())){
        		byte[] data = characteristic.getValue();
        		boolean isEmptyMsg = false;
        		StringBuilder content = new StringBuilder();
        		if (data != null) {
					
					if(!isEmptyMsg){
						int curr_index = data[0] & 0xff;
						int totle_number = data[1] & 0xff;
						int step = ((data[2] & 0xff) << 24) 
								| ((data[3] & 0xff) << 16)
								| ((data[4] & 0xff) << 8)
								| ((data[5] & 0xff));
					
		    			long time = (long)(((data[6] & 0xff) << 24) 
		    								  | ((data[7] & 0xff) << 16)
		    								  | ((data[8] & 0xff) << 8) 
		    								  | ((data[9] & 0xff) )) + 1262275200l;
		    			String date = Tools.transformLongTime2StringFormat(time).substring(0, 10);
		    			Log.d(TAG,"date"+date);
		    			Log.d(TAG,"step"+step);
		    			content.append(step);
		    			content.append("|");
		    			content.append(date);
		    			content.append("|");
		            	String data_from = deviceName + "|" + deviceAddress;
		            	Log.i(TAG, "GATT get data = " + content + "||| from= " + data_from);
		            	Intent intent = new Intent("com.zhuoyou.plugin.running.get.gatt");
						intent.putExtra("content", content.toString());
						intent.putExtra("statistics", 2);
						intent.putExtra("from", data_from);
						sendBroadcast(intent);
		            	
						if (curr_index == totle_number) {
							Log.d(TAG,"sendBroadcast ACTION_STEP_DATA_READ");
							Intent intentSleep  = new Intent(BleManagerService.ACTION_TOTALSTEP_DISABLE_DATA_READ);
							sendBroadcast(intentSleep);
						}
					}
        		}
        		
            }else if(UUID_SEGMENT_STEPS_MEASUREMENT.equals(characteristic.getUuid())) {
        		byte[] data = characteristic.getValue();
        		Log.d(TAG, "data="+characteristic.getValue());
        		boolean isEmptyMsg = false;
        		if (data != null) {
					if (data[0] == (byte)0xff && data[13] == (byte)0xff) {
						isEmptyMsg = true;
						for (int i = 0; i < 12; i++) {
							if (data[i + 1] != 0) {
								isEmptyMsg = false;
								break;
							}
						}
						//there is no segment step,so we get statistics step
        				if(isEmptyMsg){
        					sendBroadcast(new Intent(BleManagerService.ACTION_STATISTICS_STEP_READ));
        				}
        			}
					
					if(!isEmptyMsg){
						int curr_index = data[0] & 0xff;
						int totle_number = data[1] & 0xff;
						int step = ((data[2] & 0xff) << 24) 
								| ((data[3] & 0xff) << 16)
								| ((data[4] & 0xff) << 8)
								| ((data[5] & 0xff));
					
		    			long startTime = (long)(((data[6] & 0xff) << 24) 
		    								  | ((data[7] & 0xff) << 16)
		    								  | ((data[8] & 0xff) << 8) 
		    								  | ((data[9] & 0xff) )) + 1262275200l;
		    			long endTime = (long) (((data[10] & 0xff) << 24 ) 
		    							     | ((data[11] & 0xff) << 16)
		    							     | ((data[12] & 0xff) << 8)
		    							     | ((data[13] & 0xff) )) + 1262275200l;
		    			String time = Tools.transformLongTime2StringFormat(startTime);
		    			String date = time.substring(0, 10);
		    			String start = time.substring(11, 15);
		    			String end = Tools.transformLongTime2StringFormat(endTime).substring(11, 15);
		    			Log.d(TAG,"curr_index"+curr_index);
		    			Log.d(TAG,"totle_number"+totle_number);
		    			Log.d(TAG,"start"+start);
		    			Log.d(TAG,"end"+end);
		    			Log.d(TAG,"step"+step);
		            	String data_from = deviceName + "|" + deviceAddress;
						Intent intent = new Intent("com.zhuoyou.plugin.running.get.gatt");
						intent.putExtra("step", step);
						intent.putExtra("date", date);
						intent.putExtra("start", start);
						intent.putExtra("end", end);
						intent.putExtra("statistics", 0);
						intent.putExtra("from", data_from);
						sendBroadcast(intent);
						if (curr_index == totle_number) {
							Log.d(TAG,"ACTION_STATISTICS_STEP_READ");
							sendBroadcast(new Intent(BleManagerService.ACTION_STATISTICS_STEP_READ));
						}
					}
        		}
        	} else if (UUID_SLEEP_INFO.equals(characteristic.getUuid())){
        		//every sleepInfo include 2 part sleepData
        		Log.d(TAG,"recevice sleep info mSleepInfoPart " + mSleepInfoPart);
        		byte[] sleepData = characteristic.getValue();
        		Log.d(TAG, "sleepData="+characteristic.getValue());
        		if(sleepData !=null && sleepData.length == 20){
        			if(mSleepInfoPart == 1){
	    				curr_index = sleepData[0] & 0xff;
	    				totle_number = sleepData[1] & 0xff;
	    				mSleepInfoPart =2;
	    				
	    				Log.d(TAG,"recevice sleep info curr_index " + curr_index);
	    				Log.d(TAG,"recevice sleep info totle_number " + totle_number);
	    				
						for(int i =0 ; i< 20; i++){
							sleepInfoByte[i] = sleepData[i];
						}
        			}else if(mSleepInfoPart ==2){
        				mSleepInfoPart =1;

						for (int i = 0; i < 20; i++) {
							sleepInfoByte[i+20] = sleepData[i];
						}
						
						Message msg = customHandler.obtainMessage();
						msg.what = CustomHandler.MSG_SLEEP_INFO;
						Bundle bundle = new Bundle();
						bundle.putByteArray("sleepInfoByte", sleepInfoByte);
						msg.setData(bundle);
						msg.sendToTarget();
						
	        			if(curr_index == totle_number){
	        				//close notify
	        				Intent intent = new Intent(BleManagerService.ACTION_DISABLE_SLEEP_INFO);
	        				sendBroadcast(intent);
	        			}
        			}

        		}        		
        	}else if(GattInfo.FIND_PHONE_MEASUREMENT.equals(characteristic.getUuid())){
        		Log.d(TAG,"recevice find phone info ");
    			if(characteristic!=null){
    				byte[] values = characteristic.getValue();
    				int type = (values[0] & 0xff);
    				Log.d(TAG,"recevice find phone info value:"+ type);
    				if(type == 1 ){
    					Intent findPhoneRemindIntent = new Intent(BleManagerService.ACTION_FIND_PHONE_REMIND);
    					sendBroadcast(findPhoneRemindIntent);
    				}else if(type == 2){
    					CustomCmd.praserInPlug( ( 0x11 & 0xff ), null , null );
    				}else if(type == 5){
    					Log.d(TAG, "type == ");
    					endCall();
    				}
    			}
        	}else if(UUID_BATTERY_MEASUREMENT.equals(characteristic.getUuid())){
            	byte[] battery =  characteristic.getValue();	
            	int batteryLevel = battery[0];
    			Log.d(TAG,"battery info::value[0] = " + batteryLevel);
    			
   			    Tools.saveBatteryLevel(batteryLevel);
   			 if(batteryLevel <= 10) {
     			Log.d(TAG,"batteryLevel = " + batteryLevel);
     			Intent lowBatteryIntent = new Intent(BleManagerService.ACTION_LOW_BATTERY_REMIND);
     			sendBroadcast(lowBatteryIntent);
 			}
    			Log.d(TAG, "updateLatestDeviceBatteryValue==");
    			if (MineFragment.mHandler != null) {
    				Message msg = new Message();
    				msg.what = MineFragment.UPDATE_BATTERY;
    				msg.arg1 = batteryLevel;
    				MineFragment.mHandler.sendMessage(msg);


    			}
            }
        }
        
        if(ACTION_DATA_READ.equals(action)){
          /*  if(UUID_BATTERY_MEASUREMENT.equals(characteristic.getUuid())){
            	byte[] battery =  characteristic.getValue();	
            	int batteryLevel = battery[0];
    			Log.d(TAG,"battery info::value[0] = " + batteryLevel);
   			    Tools.saveBatteryLevel(batteryLevel);
    			Log.d(TAG, "updateLatestDeviceBatteryValue==");
    			if (MineFragment.mHandler != null) {
    				Message msg = new Message();
    				msg.what = MineFragment.UPDATE_BATTERY;
    				msg.arg1 = batteryLevel;
    				MineFragment.mHandler.sendMessage(msg);


    			}
            }*/ if(UUID_TIME_INFO.equals(characteristic.getUuid())){
            	//TimeInfo "deviceTime|alarmTime|alarmNum|state|repeatType|custom"
            	//deviceTime uint32 UTC time
        		//alarmTime  eg。 1330 means 13:30
        		//alarmNum	 value 0~3,support three different alarm
        		//state		 0:close 1:open
        		//repeatType 0:one time 1:everyday 2:workday(from Mon to Fri) 3:custom
        		//custom     default value 00000000 (eg.01111111 means everyday,00000000 means useless) 
            	
            	for(int i=0 ; i <characteristic.getValue().length; i++){
            		Log.d(TAG,"characteristic = "+ characteristic.getValue()[i]);
            	}             	
            } 
            	
            if (GattInfo.DEVICE_NAME_CHAR.equals(characteristic.getUuid())){
            	byte[] data = characteristic.getValue();
            	String dName = new String(data);
            	deviceName = dName;
            	
            	Tools.updateBleBindInfo(this, dName, deviceAddress);	
            	
            }else if(GattInfo.DEVICEINFO_MEASUMENT.equals(characteristic.getUuid())){
            	byte[] data = characteristic.getValue();
            	String deviceVersion = new String(data);
            	Intent mIntent = new Intent(FirmwareService.ACTION_RECEIVER_DEVICE_INFO);
            	mIntent.putExtra("device_version", deviceVersion);
            	sendBroadcast(mIntent);
            	Log.i(TAG, "deviceVersion:" + deviceVersion);
            }
        }
		mBusy = false;
		Log.d(TAG,"broadcastUpdate reset mBusy to false");
	}
	


	
	private boolean checkGatt() {
		if (mBtAdapter == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return false;
		}
		if (mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothGatt not initialized");
			return false;
		}
		
		Log.d(TAG,"checkGatt mBusy =" +mBusy);
		if (mBusy) {
			Log.w(TAG, "LeService busy");
			return false;
		}
		return true;
	}

	/**
	 * Manage the BLE service
	 */
	public class LocalBinder extends Binder {
		public BluetoothLeService getService() {
			return BluetoothLeService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// After using a given device, you should make sure that
		// BluetoothGatt.close() is called
		// such that resources are cleaned up properly. In this particular
		// example,
		// close() is
		// invoked when the UI is disconnected from the Service.
		Log.d(TAG, "bleservices onunbind!!");
		close();
		return super.onUnbind(intent);
	}

	private final IBinder binder = new LocalBinder();

	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 * 
	 * @return Return true if the initialization is successful.
	 */
	public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
		mThis = this;

        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBtAdapter = mBluetoothManager.getAdapter();
        if (mBtAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

		//sharepreference = this.getSharedPreferences("mofei_data", Context.MODE_PRIVATE);        
        return true;
    }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Received start id " + startId + ": " + intent);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
//		deviceAddress = Util.getLatestConnectDeviceAddress(sContext);
//		deviceName = Tools.keyString(bleBindMap, deviceAddress);
//        Message msg = new Message();
//        msg.what = BACK_CONNECT_DELAY;
//        relinkHandler.sendMessage(msg);
 
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy() called");
		if (mBluetoothGatt != null) {
			mBluetoothGatt.close();
			mBluetoothGatt = null;
		}
	}

	//
	// GATT API
	//
	/**
	 * Request a read on a given {@code BluetoothGattCharacteristic}. The read
	 * result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 * 
	 * @param characteristic
	 *            The characteristic to read from.
	 */
	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (!checkGatt())
			return;
		mBusy = true;
		mBluetoothGatt.readCharacteristic(characteristic);
	}

	public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic, byte b) {
		if (!checkGatt())
			return false;

		byte[] val = new byte[1];
		val[0] = b;
		characteristic.setValue(val);

		mBusy = true;
		return mBluetoothGatt.writeCharacteristic(characteristic);
	}

	public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic, boolean b) {
		if (!checkGatt())
			return false;

		byte[] val = new byte[1];

		val[0] = (byte) (b ? 1 : 0);
		characteristic.setValue(val);
		mBusy = true;
		return mBluetoothGatt.writeCharacteristic(characteristic);
	}

	public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (!checkGatt())
			return false;
		mBusy = true;
		return mBluetoothGatt.writeCharacteristic(characteristic);
	}
	
	
	public boolean writeDescriptor(BluetoothGattDescriptor descriptor){
		if (!checkGatt())
			return false;
		mBusy = true;
		return mBluetoothGatt.writeDescriptor(descriptor);
	}

	/**
	 * Retrieves the number of GATT services on the connected device. This
	 * should be invoked only after {@code BluetoothGatt#discoverServices()}
	 * completes successfully.
	 * 
	 * @return A {@code integer} number of supported services.
	 */
	public int getNumServices() {
		if (mBluetoothGatt == null)
			return 0;

		return mBluetoothGatt.getServices().size();
	}

	/**
	 * Retrieves a list of supported GATT services on the connected device. This
	 * should be invoked only after {@code BluetoothGatt#discoverServices()}
	 * completes successfully.
	 * 
	 * @return A {@code List} of supported services.
	 */
	public List<BluetoothGattService> getSupportedGattServices() {
		if (mBluetoothGatt == null)
			return null;

		return mBluetoothGatt.getServices();
	}

	public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enable) {
		if (!checkGatt())
			return false;
		if (!mBluetoothGatt.setCharacteristicNotification(characteristic,enable)) {
			Log.w(TAG, "setCharacteristicNotification failed");
			return false;
		}
		return mBluetoothGatt.setCharacteristicNotification(characteristic,enable);
	}
	/**
	 * Enables or disables notification on a give characteristic.
	 * 
	 * @param characteristic
	 *            Characteristic to act on.
	 * @param enabled
	 *            If true, enable notification. False otherwise.
	 */
	public boolean setCharacteristicNotification(UUID service_UUid,BluetoothGattCharacteristic characteristic, boolean enable) {
		if (!checkGatt())
			return false;

		if (!mBluetoothGatt.setCharacteristicNotification(characteristic,enable)) {
			Log.w(TAG, "setCharacteristicNotification failed");
			return false;
		}
		BluetoothGattDescriptor descriptor =null;
		if(service_UUid.equals(GattInfo.HEART_RATE_SERVICE)){
			descriptor = characteristic.getDescriptor(GattInfo.HEART_RATE_NOTICEFATION_ENABLE);
		}else if(service_UUid.equals(GattInfo.STEPS_SERVICE)){
			descriptor = characteristic.getDescriptor(GattInfo.STEPS_NOTICEFATION_ENABLE);
		}else if(service_UUid.equals(GattInfo.OAD_SERVICE_UUID)){
			descriptor = characteristic.getDescriptor(GattInfo.OAD_ENABLE_UUID);
		}else if(service_UUid.equals(GattInfo.OAD_ENABLE_UUID)){
			descriptor = characteristic.getDescriptor(GattInfo.OAD_ENABLE_UUID);
		}
			
		 
		if (descriptor == null)
			return false;

		if (enable) {
			Log.i(TAG, "enable notification");
			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		} else {
			Log.i(TAG, "disable notification");
			descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
		}

		mBusy = true;
		return mBluetoothGatt.writeDescriptor(descriptor);
	}


	public boolean isNotificationEnabled(BluetoothGattCharacteristic characteristic) {
		if (!checkGatt())
			return false;

		BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(GattInfo.HEART_RATE_NOTICEFATION_ENABLE);
		if (clientConfig == null)
			return false;

		return clientConfig.getValue() == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
	}

	/**
	 * Connects to the GATT server hosted on the Bluetooth LE device.
	 * 
	 * @param address
	 *            The device address of the destination device.
	 * 
	 * @return Return true if the connection is initiated successfully. The
	 *         connection result is reported asynchronously through the
	 *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 *         callback.
	 */
	public boolean connect(final String address) {
		if (mBtAdapter == null || address == null) {
			Log.w(TAG,"BluetoothAdapter not initialized or unspecified address.");
			return false;
		}
		final BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
		int connectionState = mBluetoothManager.getConnectionState(device,BluetoothProfile.GATT);
		Log.i(TAG,"connect-connectionState = "+connectionState);
			
		Log.d(TAG,"mBluetoothDeviceAddress == " +mBluetoothDeviceAddress );
		Log.d(TAG,"address == " +address );
		Log.d(TAG,"mBluetoothGatt == " +mBluetoothGatt );
			// Previously connected device. Try to reconnect.
			if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
				Log.d(TAG, "Re-use GATT connection");
				if (mBluetoothGatt.connect()) {
					return true;
				} else {
					Log.w(TAG, "GATT re-connect failed.");
					return false;
				}
			}

			if (device == null) {
				Log.w(TAG, "Device not found.  Unable to connect.");
				return false;
			}
			// We want to directly connect to the device, so we are setting the
			// autoConnect parameter to false.
			Log.d(TAG, "Create a new GATT connection.");
			mBluetoothGatt = device.connectGatt(this, false, mGattCallbacks);
			mBluetoothDeviceAddress = address;
		return true;
	}
	
	/**
	 * Disconnects an existing connection or cancel a pending connection. The
	 * disconnection result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	public void disconnect(String address) {
		if (mBtAdapter == null) {
			Log.w(TAG, "disconnect: BluetoothAdapter not initialized");
			return;
		}
		
		final BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
		int connectionState = mBluetoothManager.getConnectionState(device,BluetoothProfile.GATT);

		if (mBluetoothGatt != null) {
			Log.i(TAG, "disconnect");
			if (connectionState != BluetoothProfile.STATE_DISCONNECTED) {
				mBluetoothGatt.disconnect();
			} else {
				Log.w(TAG, "Attempt to disconnect in state: " + connectionState);
			}
		}
	}

	/**
	 * After using a given BLE device, the app must call this method to ensure
	 * resources are released properly.
	 */
	public void close() {
		if (mBluetoothGatt != null) {
			Log.i(TAG, "close");
			//LogcatHelper.getInstance(this).stop();
			mBluetoothGatt.close();
			mBluetoothGatt = null;
		}
	}

	public int numConnectedDevices() {
		int n = 0;

		if (mBluetoothGatt != null) {
			List<BluetoothDevice> devList;
			devList = mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
			n = devList.size();
		}
		return n;
	}

	public List<BluetoothDevice> ConnectedDevicesList() {
		List<BluetoothDevice> devList = null;
		if (mBluetoothGatt != null) {
			devList = mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
		}
		return devList;
	}	
	
	//
	// Utility functions
	//
	public static BluetoothGatt getBtGatt() {
		return mThis.mBluetoothGatt;
	}

	public static BluetoothManager getBtManager() {
		return mThis.mBluetoothManager;
	}

	public static BluetoothLeService getInstance() {
		return mThis;
	}

	public boolean waitIdle(int i) {
		i /= 10;
		while (--i > 0) {
			if (mBusy)
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			else
				break;
		}

		return i > 0;
	}

	/** 更新连接状态状态
	 * @param is_connect
	 * @param name
	 * @param address
	 */
	private void updateConnectInfo(boolean is_connect, String name, String address){
		//reset global value
		mSleepInfoPart = 1;
		if(name == null && is_connect){
		Intent getDeviceNameIntent = new Intent(BleManagerService.ACTION_GET_DEVICE_NAME);
		sendBroadcast(getDeviceNameIntent);
			
			//get device name 
		}
		
		if(is_connect){
			Tools.updateBleBindInfo(this, name, address);		
		}
	}
	
	private class CustomHandler extends Handler {
		public final static int MSG_SLEEP_INFO = 0;
		public final static int MSG_STEP_INFO = 1;
		
		public CustomHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_SLEEP_INFO:
				Log.d("yangyang","MSG_SLEEP_INFO");
				byte[] sleepInfoByte = msg.getData().getByteArray("sleepInfoByte");
        		if (sleepInfoByte != null) {
            		boolean isEmptyMsg = false;
					if (sleepInfoByte[0] == (byte)0xff && sleepInfoByte[39] == (byte)0xff) {
						isEmptyMsg = true;
						for (int i = 0; i < 38; i++) {
							if (sleepInfoByte[i + 1] != 0) {
								isEmptyMsg = false;
								break;
							}
						}
						//there is no sleep info,so we disable this notice
        				if(isEmptyMsg){
        					Log.d(TAG,"isEmptyMsg"+isEmptyMsg);
        					sendBroadcast(new Intent(BleManagerService.ACTION_DISABLE_SLEEP_INFO));
        				}
        			}
        			
        			if(!isEmptyMsg){
		        		//[0][1][2][3-6][7-10][11-39]
		        		//[0]:    current item index
		        		//[2]:    total item number
		        		//[3~6]:  startTime UTC Time from 2010.01.01 00:00
		        		//[7~10]: endTime   UTC Time from 2010.01.01 00:00
		        		//[11~39]:every 30min turn body times
						curr_index = sleepInfoByte[0] & 0xff;
						totle_number = sleepInfoByte[1] & 0xff;
						int type = sleepInfoByte[2] & 0xff;
		    			long startTime = (long)(((sleepInfoByte[3] & 0xff) << 24) 
								  			  | ((sleepInfoByte[4] & 0xff) << 16)
								  			  | ((sleepInfoByte[5] & 0xff) << 8) 
								  			  | ((sleepInfoByte[6] & 0xff) )) + 1262275200l;
		    			long endTime = (long) (((sleepInfoByte[7] & 0xff) << 24 ) 
							     		 	 | ((sleepInfoByte[8] & 0xff) << 16)
							     		 	 | ((sleepInfoByte[9] & 0xff) << 8)
							     		 	 | ((sleepInfoByte[10] & 0xff) )) + 1262275200l;
		        		
		    			startTime = Tools.transformUTCTime2LongFormat(startTime);
		    			endTime = Tools.transformUTCTime2LongFormat(endTime);
		        		
		        		StringBuilder sb = new StringBuilder("");
		    			for(int i=0; i<29;i++){
		    				sb.append((int) (sleepInfoByte[i+11] & 0xff));
		    				sb.append("|");
		    			}
		    			Log.d(TAG,"type = "+type);
		    			Log.d(TAG,"startTime = "+startTime);
		    			Log.d(TAG,"endTime = "+endTime);
		    			Log.d(TAG,"sb = "+sb.toString());
		        		//save to database
		    			DataBaseUtil.insertSleep(mThis.getBaseContext(), type, startTime, endTime, sb.toString());
        			}
        		}
				break;
			case MSG_STEP_INFO:
				break;
			}
			super.handleMessage(msg);
		}
	}
	
	private Handler relinkHandler = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BACK_CONNECT_DELAY:
				relinkHandler.sendEmptyMessageDelayed(BACK_CONNECT_OPERATION ,10 * 1000);
				relinkHandler.sendEmptyMessageDelayed(BACK_CONNECT_OPERATION, 1 * 30 * 1000);
				relinkHandler.sendEmptyMessageDelayed(BACK_CONNECT_OPERATION, 2 * 30 * 1000);
				relinkHandler.sendEmptyMessageDelayed(BACK_CONNECT_OPERATION, 3 * 30 * 1000);
				break;
				
			case BACK_CONNECT_OPERATION:
				
				relinkDevice();
				relinkHandler.sendEmptyMessageDelayed(BACK_CONNECT_OPERATION, 10 * 60 * 1000);
				break;
				
			default:
				break;
			}
		}
	};
	
	private void relinkDevice(){
		Log.i(TAG, "IS going relinkDevice name:" + deviceName + ",address:" + deviceAddress);
		
		boolean isHand = Util.isHandUnlink(BluetoothLeService.this);
		if(isHand == false){  // 非手动断开
			if(deviceName != null){
			
//				Log.d(TAG, "isSendVibration= "+isSendVibration);

				Intent relinkDeviceIntent = new Intent(BleManagerService.ACTION_CONNECT_BINDED_DEVICE);
				relinkDeviceIntent.putExtra("deviceName", deviceName);
				relinkDeviceIntent.putExtra("deviceAddress", deviceAddress);
				sendBroadcast(relinkDeviceIntent);
			}
		}
	}
	
	
	private void endCall() {
        // IBinder iBinder = ServiceManager.getService(TELEPHONY_SERVICE);
        // ServiceManager 是被系统隐藏掉了 所以只能用反射的方法获取
        try {
            // 加载ServiceManager的字节码
            Class<?> clazz = getClassLoader().loadClass("android.os.ServiceManager");
            Method method = clazz.getDeclaredMethod("getService",String.class);
            IBinder iBinder = (IBinder) method.invoke(null,TELEPHONY_SERVICE);
//            ITelephony.Stub.asInterface(iBinder).endCall();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "拦截电话异常");
        }
    }
}
