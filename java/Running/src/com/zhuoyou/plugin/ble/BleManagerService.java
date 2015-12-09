package com.zhuoyou.plugin.ble;

import java.util.List;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import com.mcube.lib.ped.PedBackgroundService;
import com.zhuoyou.plugin.bluetooth.connection.CustomCmd;
import com.zhuoyou.plugin.bluetooth.data.Util;
import com.zhuoyou.plugin.custom.CustomAlertDialog;
import com.zhuoyou.plugin.running.DayPedometerActivity;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;

@SuppressLint("NewApi")
public class BleManagerService extends Service {

	private static final String TAG = BleManagerService.class.getSimpleName();
	private static final Context sContext = RunningApp.getInstance()
			.getApplicationContext();
	private final IBinder binder = new LocalBinder();
	// BLE management
	private boolean mBleSupport = true;
	private boolean mBTSupport = false;
	private boolean mCloseThread = true;
	private boolean mIsConnecting = false;
	private boolean mIsUnbinding = false;
	private String mBindedDeviceAddress = null;
	private static BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBtAdapter = null;
	private BluetoothLeService mBluetoothLeService = null;
	private IntentFilter mFilter;
	private IntentFilter mGATTFilter;
	protected static BleManagerService sInstance = null;

	// for bt_read&write control
	public final static String ACTION_BATTERY_READ = "com.zhuoyou.plugin.batteryread";

	public final static String ACTION_VIBRATION_REMIND = "com.zhuoyou.plugin.vibration.remind";
	public final static String ACTION_CONNECT_BINDED_DEVICE = "com.zhuoyou.plugin.connect.binded.device";
	public final static String ACTION_DISCONNECT_BINDED_DEVICE = "com.zhuoyou.plugin.disconnect.binded.device";
	public final static String ACTION_STEP_DATA_READ = "com.zhuoyou.running.step.dataread";
	public final static String ACTION_STEP_TOTAL_DATA = "com.zhuoyou.running.total.step";
	public final static String ACTION_STATISTICS_STEP_READ = "com.zhuoyou.running.statistics.step.read";
	public final static String ACTION_UPDATE_ALARM_INFO = "com.zhuoyou.running.update.alarm.info";
	public final static String ACTION_NOTICE_NEW_SMS = "com.zhuoyou.running.notice.new.sms";
	public final static String ACTION_NOTICE_READ_SMS = "com.zhuoyou.running.notice.read.sms";
	public final static String ACTION_NOTICE_NEW_CALL = "com.zhuoyou.running.notice.new.call";
	public final static String ACTION_NOTICE_CALL_END = "com.zhuoyou.running.notice.call.end";
	public final static String ACTION_NOTICE_MISS_CALL = "com.zhuoyou.running.notice.miss.call";
	public final static String ACTION_NOTICE_NEW_WECHAT_MSG = "com.zhuoyou.running.notice.new.wechatMsg";
	public final static String ACTION_NOTICE_READ_WECHAT_MSG = "com.zhuoyou.running.notice.read.wechatMsg";
	public final static String ACTION_GET_SLEEP_INFO = "com.zhuoyou.running.get.sleep.info";
	public final static String ACTION_DISABLE_SLEEP_INFO = "com.zhuoyou.running.disable.sleep.info";

	public final static String ACTION_LOW_BATTERY_REMIND = "com.zhuoyou.running.low.battery.remind";
	public final static String ACTION_CLOSE_BLE_PHONE_STEPS = "com.zhuoyou.running.close.ble.phone.steps";

	public final static String ACTION_FIND_PHONE_REMIND = "com.zhuoyou.running.find.phone.remind";
	public final static String ACTION_GET_FIRMWARE_VERSION = "com.zhuoyou.running.get.firmware.version";
	public final static String ACTION_READY_FIRMWARE_UPDATE = "com.zhuoyou.running.update.firmware.ready";
	public final static String ACTION_TOTALSTEP_DISABLE_DATA_READ = "com.zhuoyou.running.disable.total.step";
	public final static String ACTION_GET_DEVICE_NAME = "com.zhuoyou.plugin.get.device.name";
	// gatt
	private List<BluetoothGattService> mServiceList = null;
	private static final int GATT_TIMEOUT = 100; // milliseconds

	private String mDeviceType = null;
	private byte[] remindInfo = null;
	private SharedPreferences msharepreference;
	private boolean connect_status = false;
	
	// Handler Msg
	private static final int STEP_ENABLE_NOTIFATION = 1;
	private static final int STEP_DISABLE_NOTIFATION = 2;
	private static final int GET_TIME_FROM_REMOTE = 3;
	private static final int SET_TIME_TO_REMOTE = 4;
	private static final int GET_BATTERYLV_FROM_REMOTE = 5;
	private static final int SET_VIBRATION_REMIND = 6;
	private static final int CONNECT_BINDED_DEVICE = 7;
	private static final int DISCONNECT_BINDED_DEVICE = 8;
	private static final int SET_ALARM_TIME = 9;
	private static final int NOTICE_NEW_SMS = 10;
	private static final int NOTICE_READ_SMS = 11;
	private static final int NOTICE_NEW_CALL = 12;
	private static final int NOTICE_CALL_END = 13;
	private static final int NOTICE_MISS_CALL = 14;
	private static final int NOTICE_NEW_WECHAT_MSG = 15;
	private static final int NOTICE_READ_WECHAT_MSG = 16;
	private static final int GET_SLEEP_INFO = 17;
	private static final int DISABLE_SLEEP_INFO = 18;
	private static final int STEP_TOTAL_READ = 19;
	private static final int GET_DEVICE_NAME = 20;
	private static final int STEP_GETTOTAL_READY = 21;
	// zhongyang 20150415 for ble find phone
	private static final int DESCRIPTOR_FIND_PHONE = 22;
	private static final int FIRMWARE_VERSION = 23;
	private static final int FIRMWARE_UPDATE_READY = 24;
	private static final int NOTIFY_FIND_PHONE = 25;
	private static final int STEP_TOTAL_READ_CLOSE = 26;
	private static final int SEND_BACK_CONNECT_NOT_VIBRATION = 27;
	private static final int SEND_BATTERYLY_NOTIFY_TO_REMOTE = 28;
	private static final int MSG_TEST = 99;
	public static int low_battery_remind = 1;

	public boolean initialize() {
		Log.d(TAG, "BleManagerService initialize");

		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				mBTSupport = false;
				return false;
			}
		}

		mBtAdapter = mBluetoothManager.getAdapter();
		if (mBtAdapter == null) {
			mBTSupport = false;
			return false;
		} else {
			if (!mBtAdapter.isEnabled()) {
				mBTSupport = false;
			} else {
				mBTSupport = true;
			}
		}

		sInstance = this;

		// msharepreference =
		// this.getSharedPreferences("mofei_data",Context.MODE_PRIVATE);
		startBluetoothLeService();
		// config the BroadcastReceiver filter
		mFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		mFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		mFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);

		// // gatt BroadcastReceiver filter
		mGATTFilter = new IntentFilter();
		mGATTFilter
				.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		mGATTFilter.addAction(BluetoothLeService.ACTION_DATA_NOTIFY);
		mGATTFilter.addAction(BluetoothLeService.ACTION_DATA_WRITE);
		mGATTFilter.addAction(BluetoothLeService.ACTION_DATA_READ);
		// add for bt control
		mGATTFilter.addAction(ACTION_CONNECT_BINDED_DEVICE);
		mGATTFilter.addAction(ACTION_DISCONNECT_BINDED_DEVICE);
		mGATTFilter.addAction(ACTION_BATTERY_READ);
		mGATTFilter.addAction(ACTION_STEP_DATA_READ);
		mGATTFilter.addAction(ACTION_STEP_TOTAL_DATA);
		mGATTFilter.addAction(ACTION_STATISTICS_STEP_READ);
		mGATTFilter.addAction(ACTION_UPDATE_ALARM_INFO);
		mGATTFilter.addAction(ACTION_NOTICE_NEW_SMS);
		mGATTFilter.addAction(ACTION_NOTICE_READ_SMS);
		mGATTFilter.addAction(ACTION_NOTICE_NEW_CALL);
		mGATTFilter.addAction(ACTION_NOTICE_CALL_END);
		mGATTFilter.addAction(ACTION_NOTICE_MISS_CALL);
		mGATTFilter.addAction(ACTION_NOTICE_NEW_WECHAT_MSG);
		mGATTFilter.addAction(ACTION_NOTICE_READ_WECHAT_MSG);
		mGATTFilter.addAction(ACTION_GET_SLEEP_INFO);
		mGATTFilter.addAction(ACTION_DISABLE_SLEEP_INFO);
		mGATTFilter.addAction(ACTION_GET_FIRMWARE_VERSION);
		mGATTFilter.addAction(ACTION_READY_FIRMWARE_UPDATE);
		mGATTFilter.addAction(ACTION_TOTALSTEP_DISABLE_DATA_READ);
		mGATTFilter.addAction(ACTION_GET_DEVICE_NAME);
		mGATTFilter.addAction(ACTION_LOW_BATTERY_REMIND);
		mGATTFilter.addAction(ACTION_CLOSE_BLE_PHONE_STEPS);
		mGATTFilter.addAction(ACTION_FIND_PHONE_REMIND);
		return true;
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {

			case STEP_ENABLE_NOTIFATION:
				if (mBluetoothLeService != null) {
					Log.d(TAG, "STEP_ENABLE_NOTIFATION");
					getRemoteDeviceSteps(true);
				}
				break;

			case STEP_DISABLE_NOTIFATION:
				if (mBluetoothLeService != null) {
					Log.d(TAG, "STEP_DISABLE_NOTIFATION");
					getRemoteDeviceSteps(false);
					mHandler.sendEmptyMessageDelayed(GET_SLEEP_INFO, 1000);
				}
				break;

			case STEP_GETTOTAL_READY:
				if (mBluetoothLeService != null) {
					Log.d(TAG, "STEP_GETTOTAL_READY");
					// setRemoteDeviceStepsReady();
					mHandler.sendEmptyMessageDelayed(STEP_TOTAL_READ, 4000);
				}
				break;

			case STEP_TOTAL_READ:
				if (mBluetoothLeService != null) {
					Log.d(TAG, "STEP_TOTAL_READ");
					if (!getRemoteStepsDescriptor()) {
						getRemoteDeviceTotalSteps(true);
					}
				}
				break;
			case STEP_TOTAL_READ_CLOSE:
				if (mBluetoothLeService != null) {
					Log.d(TAG, "STEP_TOTAL_READ_CLOSE");
					if (!getRemoteStepsDescriptor()) {
						getRemoteDeviceTotalSteps(false);
					}
				}
				break;
			case MSG_TEST:
				// getRemoteDeviceSteps(true);
				// mHandler.sendEmptyMessageDelayed(GET_SLEEP_INFO, 4000);
				// mHandler.sendEmptyMessageDelayed(NOTICE_NEW_SMS, 4000);
				mHandler.sendEmptyMessageDelayed(MSG_TEST, 10000);

				break;

			case GET_TIME_FROM_REMOTE:
				if (mBluetoothLeService != null) {
					getRemoteDeviceTimeAndAlarm();
				}
				break;
			// 20150602 Heph add
			case SEND_BATTERYLY_NOTIFY_TO_REMOTE:
				if (mBluetoothLeService != null) {
					openBatteryNotify();
				}
				break;
			case SET_TIME_TO_REMOTE:
				if (mBluetoothLeService != null) {
					setRemoteDeviceTime();
					// mHandler.sendEmptyMessageDelayed(SEND_BATTERYLY_NOTIFY,
					// 600);
					mHandler.sendEmptyMessageDelayed(GET_TIME_FROM_REMOTE, 1000);
				}
				break;
			// 20150602 Heph add
			case SEND_BACK_CONNECT_NOT_VIBRATION:
				if (mBluetoothLeService != null) {
					Log.d(TAG, "SEND_BACK_CONNECT_NOT_VIBRATION");
					controlRemoteDeviceVibration();

				}
				break;
			case GET_BATTERYLV_FROM_REMOTE:
				if (mBluetoothLeService != null) {
					getRemoteDeviceBatteryInfo();
				}
				break;

			case SET_VIBRATION_REMIND:
				if (mBluetoothLeService != null) {
					SetVibrationRemind();
				}
				break;

			case CONNECT_BINDED_DEVICE:
				if (mBluetoothLeService != null) {
					String address = Util
							.getLatestConnectDeviceAddress(sInstance);
					Bundle mBundle = msg.getData();
					if (mBundle != null) {
						String temAddess = mBundle.getString("address");
						if (!TextUtils.isEmpty(temAddess)) {
							address = temAddess;
						}
					}

					if (!TextUtils.isEmpty(address)) {
						Log.i(TAG, "CONNECT_BINDED_DEVICE,Address:" + address);
						ConnectToDeviceByAddress(address);
					} else {
						Intent intent = new Intent(
								"com.zhuoyou.running.connect.failed");
						sendBroadcast(intent);
					}
				}
				break;

			case DISCONNECT_BINDED_DEVICE:
				if (mBluetoothLeService != null) {
					disConnectDeviceByAddress(mBindedDeviceAddress);
					mBindedDeviceAddress = null;
					mIsUnbinding = false;
					Log.i(TAG, "disConnectDeviceByAddress(mBindedDeviceAddress)");
				}
				break;

			case SET_ALARM_TIME:
				if (mBluetoothLeService != null) {
					// get alarmMsg
					setAlarmTime((String) msg.obj);
				}
				break;

			case NOTICE_NEW_SMS:
				if (mBluetoothLeService != null) {
					if (mDeviceType != null) {
						if ("Unik 1".equals(mDeviceType)) {
							noticeNewSMS();
						} else if ("Unik 2".equals(mDeviceType)) {
							noticeNewSMS();
						} else if ("LEO".equals(mDeviceType)) {
							noticeNewSMS();
						}

					}
				}
				break;

			case NOTICE_READ_SMS:
				if (mBluetoothLeService != null) {
					if (mDeviceType != null) {
						if ("Unik 2".equals(mDeviceType)) {
							noticeReadSMS();
						} else if ("LEO".equals(mDeviceType)) {
							noticeReadSMS();
						}
					}
				}
				break;

			case NOTICE_NEW_CALL:
				if (mBluetoothLeService != null) {
					if (mDeviceType != null) {
						if ("Unik 1".equals(mDeviceType)) {
							noticeNewCall();
						} else if ("Unik 2".equals(mDeviceType)) {
							noticeNewCallPro((String) msg.obj);
						} else if ("LEO".equals(mDeviceType)) {
							noticeNewCallPro((String) msg.obj);
						}

					}
				}
				break;

			case NOTICE_CALL_END:
				if (mBluetoothLeService != null) {
					if (mDeviceType != null) {
						if ("Unik 2".equals(mDeviceType)) {
							noticeCallEnd();
						} else if ("LEO".equals(mDeviceType)) {
							noticeCallEnd();
						}
					}
				}
				break;

			case NOTICE_MISS_CALL:
				if (mBluetoothLeService != null) {
					if (mDeviceType != null) {
						if ("Unik 2".equals(mDeviceType)) {
							noticeMissCall();
						} else if ("LEO".equals(mDeviceType)) {
							noticeMissCall();
						}
					}
				}
				break;

			case NOTICE_NEW_WECHAT_MSG:
				if (mBluetoothLeService != null) {
					if (mDeviceType != null) {
						if ("Unik 2".equals(mDeviceType)) {
							noticeNewWeChatMsg();
						} else if ("LEO".equals(mDeviceType)) {
							noticeNewWeChatMsg();
						}

					}
				}
				break;

			case NOTICE_READ_WECHAT_MSG:
				if (mBluetoothLeService != null) {
					if (mDeviceType != null) {
						if ("Unik 2".equals(mDeviceType)) {
							noticeReadWeChatMsg();
						} else if ("LEO".equals(mDeviceType)) {
							noticeReadWeChatMsg();
						}

					}
				}
				break;

			case GET_SLEEP_INFO:
				if (mBluetoothLeService != null) {
					if (!getSleepInfoDescriptor()) {
						getSleepInfoFromRemote(true);
					}
				}
				break;

			case DISABLE_SLEEP_INFO:
				if (mBluetoothLeService != null) {
					if (getSleepInfoDescriptor()) {
						getSleepInfoFromRemote(false);
						// 20150528
						mHandler.sendEmptyMessageDelayed(STEP_TOTAL_READ, 1000);
					}
				}
				break;

			case GET_DEVICE_NAME:
				if (mBluetoothLeService != null) {
					getDeviceName();
				}
				break;
			// zhongyang 20150415
			case NOTIFY_FIND_PHONE:
				if (mBluetoothLeService != null) {
					noticeFindPhone();
					mHandler.sendEmptyMessageDelayed(DESCRIPTOR_FIND_PHONE, 600);

				}
				break;
			case DESCRIPTOR_FIND_PHONE:
				if (mBluetoothLeService != null) {
					desciptorFindPhone(true);
				}
				break;

			case FIRMWARE_VERSION:
				if (mBluetoothLeService != null) {
					getDeviceVersion();
				}
				break;
			case FIRMWARE_UPDATE_READY:
				if (mBluetoothLeService != null) {
					setDeviceUpdateReady();
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Log.d(TAG, "BleManagerService onCreate");
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d(TAG, "BleManagerService onStartCommand");
		if (!initialize()) {
			stopSelf();
			Log.d(TAG, "BleManagerService onStartCommand-stop self!!");
		}
		registerReceiver();
		return super.onStartCommand(intent, flags, startId);
	}

	public class LocalBinder extends Binder {
		public BleManagerService getService() {
			return BleManagerService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "BleManagerService onbind service");
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "BleManagerService onUnbind service");
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(TAG, "BleDeviceManagerService onDestroy");
		super.onDestroy();
		mHandler.removeCallbacksAndMessages(null);
		if (mBluetoothLeService != null) {
			mCloseThread = false;
			mBluetoothLeService.close();
			unregisterReceiver();
			unbindService(mServiceConnection);
			mBluetoothLeService = null;
		}
	}

	public boolean ConnectToDevice(BleDeviceInfo mbledev) {
		boolean result = false;
		if (mbledev != null
				&& mbledev.getBluetoothDevice().getAddress() != null) {
			result = mBluetoothLeService.connect(mbledev.getBluetoothDevice()
					.getAddress());

			if (result && mbledev.getBluetoothDevice().getName() != null) {
				mDeviceType = mbledev.getBluetoothDevice().getName();
			}
		}
		return result;
	}

	public boolean ConnectToDeviceByAddress(String address) {
		boolean result = false;
		if (address != null && mBluetoothLeService != null) {
			result = mBluetoothLeService.connect(address);
		}
		return result;
	}

	public void disConnectDevice(BleDeviceInfo device) {
		if (mBluetoothManager.getConnectionState(device.getBluetoothDevice(),
				BluetoothGatt.GATT) == BluetoothGatt.STATE_CONNECTED) {
			mBluetoothLeService.disconnect(device.getBluetoothDevice()
					.getAddress());
		}
	}

	private void disConnectDeviceByAddress(String address) {
		if (address != null && mBluetoothLeService != null) {
			mBluetoothLeService.disconnect(address);
		}
	}

	public int getGattConnectState(BluetoothDevice device) {
		int connectionState = 2;
		if (mBluetoothManager != null) {
			connectionState = mBluetoothManager.getConnectionState(device,
					BluetoothProfile.GATT);

			// just for unify to classic bluetooth
			if (connectionState == BluetoothProfile.STATE_CONNECTED) {
				connectionState = 3;
			} else if (connectionState == BluetoothProfile.STATE_DISCONNECTED) {
				connectionState = 2;
			}
		}

		return connectionState;
	}

	public List<BluetoothDevice> getGattCurrentDevice() {
		return BluetoothLeService.getInstance().ConnectedDevicesList();
	}

	private void getSupportedrServices() {
		try {
			if (mBluetoothLeService != null)
				mServiceList = mBluetoothLeService.getSupportedGattServices();
			for (int i = 0; i < mServiceList.size() && mServiceList.size() > 1; i++)
				Log.d(TAG, "mServiceList" + i + ":"
						+ mServiceList.get(i).getUuid().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private BluetoothGattCharacteristic getGattCharacteristic(UUID srcUuid) {
		BluetoothGattCharacteristic characteristic = null;
		if (!IsSupportedServicesListEmpty() && mServiceList.size() != 0) {
			for (BluetoothGattService gattService : mServiceList) {
				List<BluetoothGattCharacteristic> gattCharacs = gattService
						.getCharacteristics();
				for (BluetoothGattCharacteristic gattCharac : gattCharacs) {
					UUID uuid = gattCharac.getUuid();
					Log.d("gatt",
							"gattCharacteristic.getUuid():" + uuid.toString());
					if (uuid.equals(srcUuid)) {
						characteristic = gattCharac;
						break;
					}
				}
			}
		}
		return characteristic;
	}

	private BluetoothGattCharacteristic getGattCharacteristic(UUID serviceUUid,
			UUID characteristicUUid) {
		BluetoothGattCharacteristic characteristic = null;
		if (!IsSupportedServicesListEmpty() && mServiceList.size() != 0) {
			for (BluetoothGattService gattService : mServiceList) {
				Log.d("gatt", "::services uuid ="
						+ gattService.getUuid().toString());
				if (gattService.getUuid().equals(serviceUUid)) {
					List<BluetoothGattCharacteristic> gattCharacs = gattService
							.getCharacteristics();
					Log.d("gatt", "right::services uuid ="
							+ gattService.getUuid().toString());
					for (BluetoothGattCharacteristic gattCharac : gattCharacs) {
						UUID uuid = gattCharac.getUuid();
						Log.d("gatt",
								"::gattCharacteristic uuid:" + uuid.toString());
						if (uuid.equals(characteristicUUid)) {
							Log.d("gatt", "right::gattCharacteristic uuid:"
									+ uuid.toString());
							characteristic = gattCharac;
							break;
						}
					}

				}
			}
		}
		return characteristic;
	}

	private void openBatteryNotify() {
		Log.d(TAG, "openBatteryNotify");
		if (mBluetoothLeService != null) {
			UUID battery_service_Uuid = GattInfo.BATTERY_SERVICE;
			UUID battery_level_character_Uuid = GattInfo.BATTERY_LEVEL;
			BluetoothGattCharacteristic find_charac = getGattCharacteristic(
					battery_service_Uuid, battery_level_character_Uuid);
			if (find_charac != null) {
				boolean result = mBluetoothLeService
						.setCharacteristicNotification(find_charac, true);
				Log.d(TAG,
						"openBatteryNotify setCharacteristicNotification result = "
								+ result);
				BluetoothGattDescriptor descriptor = find_charac
						.getDescriptor(GattInfo.STEPS_NOTICEFATION_ENABLE);
				if (descriptor != null) {
					descriptor
							.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				}
				mBluetoothLeService.writeDescriptor(descriptor);
			}
		}

	}

	private void getRemoteDeviceBatteryInfo() {
		Log.d(TAG, "getRemoteDeviceBatteryInfo");
		if (mBluetoothLeService != null) {
			UUID servUuid = GattInfo.BATTERY_LEVEL;
			BluetoothGattCharacteristic charac = getGattCharacteristic(servUuid);
			if (charac != null)
				mBluetoothLeService.readCharacteristic(charac);
			mBluetoothLeService.waitIdle(GATT_TIMEOUT);
		}
	}

	private void controlRemoteDeviceVibration() {
		UUID service_Uuid = GattInfo.STEPS_SERVICE;
		UUID character_Uuid = GattInfo.FIRMWARE_READY_MEASUMENT;
		BluetoothGattCharacteristic charac = getGattCharacteristic(
				service_Uuid, character_Uuid);
		byte[] byteArr = { 0x02 };
		charac.setValue(byteArr);
		if (charac != null) {
			boolean result = mBluetoothLeService.writeCharacteristic(charac);
			Log.d(TAG, "sendControlRemoteDeviceVibration result= " + result);
		}
		mBluetoothLeService.waitIdle(GATT_TIMEOUT);
	}

	private void getRemoteDeviceTimeAndAlarm() {
		Log.d(TAG, "getRemoteDeviceTimeAndAlarm");
		UUID servUuid = GattInfo.TIME_AND_ALARM_INFO;
		BluetoothGattCharacteristic charac = getGattCharacteristic(servUuid);
		if (charac != null)
			mBluetoothLeService.readCharacteristic(charac);
		mBluetoothLeService.waitIdle(GATT_TIMEOUT);
	}

	private void setRemoteDeviceTime() {
		Log.d(TAG, "setRemoteDeviceTime");
		UUID servUuid = GattInfo.TIME_SYNC;
		BluetoothGattCharacteristic charac = getGattCharacteristic(servUuid);
		long LocalTime = System.currentTimeMillis() / 1000L;
		byte[] LocalTimemsByte = long2Byte(LocalTime);
		// Log.d(TAG, "LocalTime set to remote device =" + LocalTime);
		// for(int i=0;i<LocalTimemsByte.length;i++){
		// Log.d(TAG, "LocalTimemsByte" +"["+i+"]"+"=" + LocalTimemsByte[i]);
		// }
		if (charac != null) {
			charac.setValue(LocalTimemsByte);

			boolean result = mBluetoothLeService.writeCharacteristic(charac);
			Log.d(TAG, "setRemoteDeviceTime result" + result);
		}
		mBluetoothLeService.waitIdle(GATT_TIMEOUT);
	}

	private void setAlarmTime(String alarmMsg) {
		Log.i(TAG, "setAlarmTime:" + alarmMsg);
		byte[] alarmMsgByte = alarmMsg.getBytes();
		UUID step_service_Uuid = GattInfo.ALARM_SERVICE;
		UUID step_character_Uuid = GattInfo.ALARM_MEASUREMENT;
		BluetoothGattCharacteristic charac = getGattCharacteristic(
				step_service_Uuid, step_character_Uuid);

		charac.setValue(alarmMsgByte);
		if (charac != null) {
			boolean result = mBluetoothLeService.writeCharacteristic(charac);
			Log.d(TAG, "setAlarmTime result= " + result);
		}
		mBluetoothLeService.waitIdle(GATT_TIMEOUT);
	}

	private void noticeNewSMS() {
		Log.d(TAG, "noticeNewSMS");
		UUID service_Uuid = GattInfo.STEPS_SERVICE;
		UUID character_Uuid = GattInfo.OTA;
		BluetoothGattCharacteristic charac = getGattCharacteristic(
				service_Uuid, character_Uuid);
		byte[] byteArr = { 0x04 };
		charac.setValue(byteArr);
		if (charac != null) {
			boolean result = mBluetoothLeService.writeCharacteristic(charac);
			Log.d(TAG, "noticeNewSMS result= " + result);
		}
		mBluetoothLeService.waitIdle(GATT_TIMEOUT);
	}

	private void noticeReadSMS() {
		Log.d(TAG, "noticeReadSMS");
		UUID service_Uuid = GattInfo.STEPS_SERVICE;
		UUID character_Uuid = GattInfo.OTA;
		BluetoothGattCharacteristic charac = getGattCharacteristic(
				service_Uuid, character_Uuid);
		byte[] byteArr = { 0x08 };
		charac.setValue(byteArr);
		if (charac != null) {
			boolean result = mBluetoothLeService.writeCharacteristic(charac);
			Log.d(TAG, "noticeReadSMS result= " + result);
		}
		mBluetoothLeService.waitIdle(GATT_TIMEOUT);
	}

	private void noticeNewCall() {
		Log.d(TAG, "noticeNewCall");
		UUID service_Uuid = GattInfo.STEPS_SERVICE;
		UUID character_Uuid = GattInfo.OTA;
		BluetoothGattCharacteristic charac = getGattCharacteristic(
				service_Uuid, character_Uuid);
		byte[] byteArr = { 0x05 };
		charac.setValue(byteArr);
		if (charac != null) {
			boolean result = mBluetoothLeService.writeCharacteristic(charac);
			Log.d(TAG, "noticeNewCall result= " + result);
		}
		mBluetoothLeService.waitIdle(GATT_TIMEOUT);
	}

	private void noticeNewCallPro(String phoneNum) {
		Log.d(TAG, "noticeNewCallPro:" + phoneNum);
		UUID service_Uuid = GattInfo.STEPS_SERVICE;
		UUID character_Uuid = GattInfo.OTA;
		BluetoothGattCharacteristic charac = getGattCharacteristic(
				service_Uuid, character_Uuid);
		/** {0x7,phoneNum---sting.byteArr,0xff} start */
		byte[] byteArr = { 0x07, (byte) 0xFF };
		if (phoneNum == null) {
			phoneNum = "";
		}
		byte[] phoneArr = phoneNum.getBytes();
		byte[] resArray = new byte[byteArr.length + phoneArr.length];
		System.arraycopy(byteArr, 0, resArray, 0, 1);
		System.arraycopy(phoneArr, 0, resArray, 1, phoneArr.length);
		System.arraycopy(byteArr, 1, resArray, resArray.length - 1, 1);
		/** {0x7,phoneNum---sting.byteArr,0xff} end */

		charac.setValue(resArray);
		if (charac != null) {
			boolean result = mBluetoothLeService.writeCharacteristic(charac);
			Log.d(TAG, "noticeNewCallPro result= " + result);
		}
		mBluetoothLeService.waitIdle(GATT_TIMEOUT);
	}

	private void noticeCallEnd() {
		Log.d(TAG, "noticeCallEnd");
		UUID service_Uuid = GattInfo.STEPS_SERVICE;
		UUID character_Uuid = GattInfo.OTA;
		BluetoothGattCharacteristic charac = getGattCharacteristic(
				service_Uuid, character_Uuid);
		byte[] byteArr = { 0x11 };
		charac.setValue(byteArr);
		if (charac != null) {
			boolean result = mBluetoothLeService.writeCharacteristic(charac);
			Log.d(TAG, "noticeCallEnd result= " + result);
		}
		mBluetoothLeService.waitIdle(GATT_TIMEOUT);
	}

	private void noticeMissCall() {
		Log.d(TAG, "noticeMissCall");
		UUID service_Uuid = GattInfo.STEPS_SERVICE;
		UUID character_Uuid = GattInfo.OTA;
		BluetoothGattCharacteristic charac = getGattCharacteristic(
				service_Uuid, character_Uuid);
		byte[] byteArr = { 0x05 };
		charac.setValue(byteArr);
		if (charac != null) {
			boolean result = mBluetoothLeService.writeCharacteristic(charac);
			Log.d(TAG, "noticeMissCall result= " + result);
		}
		mBluetoothLeService.waitIdle(GATT_TIMEOUT);
	}

	private void noticeNewWeChatMsg() {
		Log.d(TAG, "noticeNewWeChatMsg");
		UUID service_Uuid = GattInfo.STEPS_SERVICE;
		UUID character_Uuid = GattInfo.OTA;
		BluetoothGattCharacteristic charac = getGattCharacteristic(
				service_Uuid, character_Uuid);
		byte[] byteArr = { 0x06 };
		charac.setValue(byteArr);
		if (charac != null) {
			boolean result = mBluetoothLeService.writeCharacteristic(charac);
			Log.d(TAG, "noticeNewWeChatMsg result= " + result);
		}
		mBluetoothLeService.waitIdle(GATT_TIMEOUT);
	}

	private void noticeReadWeChatMsg() {
		Log.d(TAG, "noticeReadWeChatMsg");
		UUID service_Uuid = GattInfo.STEPS_SERVICE;
		UUID character_Uuid = GattInfo.OTA;
		BluetoothGattCharacteristic charac = getGattCharacteristic(
				service_Uuid, character_Uuid);
		byte[] byteArr = { 0x10 };
		charac.setValue(byteArr);
		if (charac != null) {
			boolean result = mBluetoothLeService.writeCharacteristic(charac);
			Log.d(TAG, "noticeReadWeChatMsg result= " + result);
		}
		mBluetoothLeService.waitIdle(GATT_TIMEOUT);
	}

	// zhongyang 2015015
	private boolean noticeFindPhone() {
		Log.d(TAG, "noticeFindPhone");
		UUID service_Uuid = GattInfo.FIND_PHONE_SERVICE;
		UUID character_Uuid = GattInfo.FIND_PHONE_MEASUREMENT;
		BluetoothGattCharacteristic charac = getGattCharacteristic(
				service_Uuid, character_Uuid);
		boolean result = false;
		if (charac != null) {
			result = mBluetoothLeService.setCharacteristicNotification(charac,
					true);
			Log.d(TAG, "noticeFindPhone result= " + result);
		}
		mBluetoothLeService.waitIdle(GATT_TIMEOUT);
		return result;
	}

	// zhongyang 2015015
	private void desciptorFindPhone(boolean enable) {
		Log.d(TAG, "desciptorFindPhone:" + enable);
		UUID service_Uuid = GattInfo.FIND_PHONE_SERVICE;
		UUID character_Uuid = GattInfo.FIND_PHONE_MEASUREMENT;
		BluetoothGattCharacteristic charac = getGattCharacteristic(
				service_Uuid, character_Uuid);
		if (charac != null) {
			BluetoothGattDescriptor descriptor = charac
					.getDescriptor(GattInfo.FIND_PHONE_NOTIFY_ENABLE);
			boolean result = false;
			if (descriptor != null) {
				if (enable) {
					descriptor
							.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				} else {
					descriptor
							.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
				}
				result = mBluetoothLeService.writeDescriptor(descriptor);
			}
			Log.d(TAG, "desciptorFindPhone result= " + result);
		}
		mBluetoothLeService.waitIdle(GATT_TIMEOUT);
	}

	private void getSleepInfoFromRemote(boolean enable) {
		Log.d(TAG, "getSleepInfoFromRemote:" + enable);
		if (mBluetoothLeService != null) {
			UUID service_Uuid = GattInfo.STEPS_SERVICE;
			UUID character_Uuid = GattInfo.SLEEP_INFO_CHAR;
			BluetoothGattCharacteristic charac = getGattCharacteristic(
					service_Uuid, character_Uuid);
			if (charac != null) {
				boolean result = mBluetoothLeService
						.setCharacteristicNotification(service_Uuid, charac,
								enable);
				Log.d(TAG,
						"getSleepInfoFromRemote setCharacteristicNotification result = "
								+ result);
			}
			mBluetoothLeService.waitIdle(GATT_TIMEOUT);
		}
	}

	private void getDeviceName() {
		Log.d(TAG, "getDeviceName");
		if (mBluetoothLeService != null) {
			UUID service_Uuid = GattInfo.DEVICE_NAME_SERVICE;
			UUID character_Uuid = GattInfo.DEVICE_NAME_CHAR;
			BluetoothGattCharacteristic charac = getGattCharacteristic(
					service_Uuid, character_Uuid);
			if (charac != null)
				mBluetoothLeService.readCharacteristic(charac);
			mBluetoothLeService.waitIdle(GATT_TIMEOUT);
		}
	}

	private void getDeviceVersion() {
		Log.d(TAG, "getDeviceVersion");
		if (mBluetoothLeService != null) {
			UUID service_Uuid = GattInfo.DEVICEINFO_SERVICE;
			UUID character_Uuid = GattInfo.DEVICEINFO_MEASUMENT;
			BluetoothGattCharacteristic charac = getGattCharacteristic(
					service_Uuid, character_Uuid);
			if (charac != null)
				mBluetoothLeService.readCharacteristic(charac);
			mBluetoothLeService.waitIdle(GATT_TIMEOUT);
		}
	}

	private void setDeviceUpdateReady() {
		Log.d(TAG, "setDeviceUpdateReady");
		if (mBluetoothLeService != null) {
			UUID service_Uuid = GattInfo.FIRMWARE_READY_SERVICE;
			UUID character_Uuid = GattInfo.FIRMWARE_READY_MEASUMENT;
			BluetoothGattCharacteristic charac = getGattCharacteristic(
					service_Uuid, character_Uuid);
			if (charac != null) {
				byte[] byteArr = { 0x01 };
				charac.setValue(byteArr);
				mBluetoothLeService.writeCharacteristic(charac);
			}
			mBluetoothLeService.waitIdle(GATT_TIMEOUT);
		}
	}

	private void setRemoteDeviceStepsReady() {
		// Log.d(TAG, "setRemoteDeviceStepsReady");
		// UUID step_service_Uuid = GattInfo.STEPS_SERVICE;
		// UUID step_character_Uuid = GattInfo.OTA;
		// Log.d(TAG, "step_character_Uuid rel = "+ step_character_Uuid);
		// BluetoothGattCharacteristic charac =
		// getGattCharacteristic(step_service_Uuid,step_character_Uuid);
		// byte[] byteArr = {0x02};
		// charac.setValue(byteArr);
		// if (charac != null) {
		// boolean result = mBluetoothLeService.writeCharacteristic(charac);
		// Log.d(TAG,
		// "setRemoteDeviceStepsDescriptor writeCharacteristic result = "+
		// result);
		// }
		// mBluetoothLeService.waitIdle(GATT_TIMEOUT);
	}

	private void SetVibrationRemind() {
		Log.d(TAG, "SetVibrationRemind");
		if (mBluetoothLeService != null && getRemoteStepsDescriptor()) {
			UUID ServiceUuid = GattInfo.VIBRATION_REMIND;
			BluetoothGattCharacteristic charac = getGattCharacteristic(ServiceUuid);
			if (remindInfo != null)
				charac.setValue(remindInfo);
			if (charac != null) {
				mBluetoothLeService.writeCharacteristic(charac);
			}
			mBluetoothLeService.waitIdle(GATT_TIMEOUT);
		}
	}

	// 统计步数打开或关闭通知
	private void getRemoteDeviceTotalSteps(boolean enable) {
		Log.d(TAG, "getRemoteDeviceTotalSteps :" + enable);
		if (mBluetoothLeService != null) {
			UUID step_service_Uuid = GattInfo.STEPS_SERVICE;
			UUID step_character_Uuid = GattInfo.TOTAL_STEPS_MEASUREMENT;
			BluetoothGattCharacteristic find_charac = getGattCharacteristic(
					step_service_Uuid, step_character_Uuid);
			boolean result = mBluetoothLeService.setCharacteristicNotification(
					find_charac, enable);
			Log.d(TAG,
					"getRemoteDeviceTotalSteps setCharacteristicNotification result = "
							+ result);
			BluetoothGattDescriptor descriptor = find_charac
					.getDescriptor(GattInfo.STEPS_NOTICEFATION_ENABLE);
			if (descriptor != null) {
				if (enable) {
					descriptor
							.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				} else {
					descriptor
							.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
				}
				mBluetoothLeService.writeDescriptor(descriptor);
			}
		}
	}

	// 获取分段数据
	private void getRemoteDeviceSteps(boolean enable) {
		Log.d(TAG, "getRemoteDeviceSteps :" + enable);
		if (mBluetoothLeService != null) {
			UUID step_service_Uuid = GattInfo.STEPS_SERVICE;
			UUID step_character_Uuid = GattInfo.SEGMENT_STEPS_MEASUREMENT;
			BluetoothGattCharacteristic charac = getGattCharacteristic(
					step_service_Uuid, step_character_Uuid);
			if (charac != null) {
				boolean result = mBluetoothLeService
						.setCharacteristicNotification(step_service_Uuid,
								charac, enable);
				Log.d(TAG,
						"getRemoteDeviceSteps setCharacteristicNotification result = "
								+ result);
			}
			mBluetoothLeService.waitIdle(GATT_TIMEOUT);
		}
	}

	private boolean getRemoteStepsDescriptor() {
		Log.d(TAG, "getRemoteStepsDescriptor");
		if (mBluetoothLeService != null) {
			UUID step_service_Uuid = GattInfo.STEPS_SERVICE;
			UUID step_character_Uuid = GattInfo.SEGMENT_STEPS_MEASUREMENT;
			BluetoothGattCharacteristic charac = getGattCharacteristic(
					step_service_Uuid, step_character_Uuid);
			if (charac != null) {
				BluetoothGattDescriptor descriptor = null;
				descriptor = charac
						.getDescriptor(GattInfo.STEPS_NOTICEFATION_ENABLE);
				if (descriptor != null) {
					if (BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE == descriptor
							.getValue()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean getSleepInfoDescriptor() {
		Log.d(TAG, "getSleepInfoDescriptor");
		if (mBluetoothLeService != null) {
			UUID step_service_Uuid = GattInfo.STEPS_SERVICE;
			UUID step_character_Uuid = GattInfo.SLEEP_INFO_CHAR;
			BluetoothGattCharacteristic charac = getGattCharacteristic(
					step_service_Uuid, step_character_Uuid);
			if (charac != null) {
				BluetoothGattDescriptor descriptor = null;
				descriptor = charac
						.getDescriptor(GattInfo.STEPS_NOTICEFATION_ENABLE);
				if (descriptor != null) {
					if (BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE == descriptor
							.getValue()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private void startBluetoothLeService() {
		boolean sucess = false;

		Intent bindIntent = new Intent(this, BluetoothLeService.class);
		sucess = bindService(bindIntent, mServiceConnection,
				Context.BIND_AUTO_CREATE);

		if (sucess)
			Log.d(TAG, "BluetoothLeService - success");
		else {
			Log.d(TAG, "BluetoothLeService - failed");
		}
	}

	// Broadcasted actions from Bluetooth adapter and BluetoothLeService
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				// Bluetooth adapter state change
				switch (mBtAdapter.getState()) {
				case BluetoothAdapter.STATE_ON:
					mBTSupport = true;
					startBluetoothLeService();
					break;
				case BluetoothAdapter.STATE_OFF:
					mBTSupport = false;
					break;
				default:
					Log.w(TAG, "Action STATE CHANGED not processed ");
					break;
				}
			} else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				Log.d(TAG, "ACTION_GATT_CONNECTED");
				// GATT connect
				connect_status = true;
				setIsConnecting(true);

			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
					.equals(action)) {
				Log.d(TAG, "ACTION_GATT_DISCONNECTED");

				connect_status = false;
				setIsConnecting(false);
				mDeviceType = null;

				if (mBluetoothLeService != null)
					mBluetoothLeService.close();
			}
		}
	};

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS,
					BluetoothGatt.GATT_SUCCESS);

			if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {
				if (status == BluetoothGatt.GATT_SUCCESS) {
					setIsConnecting(false);
					Log.d(TAG, "mGattUpdateReceiver--find device services");
					getSupportedrServices();

					if (!Tools.getConnectNotVibtation()) {
						Log.d(TAG, "Tools.getConnectNotVibtation()= "+Tools.getConnectNotVibtation());
						mHandler.sendEmptyMessageDelayed(SEND_BACK_CONNECT_NOT_VIBRATION, 2000);
						Tools.setConnectNotVibtation(true);
					}   
					// 连接成功后即获取电量 时间校准

					mHandler.sendEmptyMessageDelayed(SET_TIME_TO_REMOTE, 2800);
					mHandler.sendEmptyMessageDelayed(GET_BATTERYLV_FROM_REMOTE,3800);
					mHandler.sendEmptyMessageDelayed(NOTIFY_FIND_PHONE, 4600);
					mHandler.sendEmptyMessageDelayed(SEND_BATTERYLY_NOTIFY_TO_REMOTE, 5600);
					mHandler.sendEmptyMessageDelayed(GET_DEVICE_NAME, 7000);
					// 仅用作测试
					mHandler.sendEmptyMessageDelayed(MSG_TEST, 10000);

				} else {
					Log.d(TAG, "gatt Service discovery failed");
					return;
				}
			}

			// 连接绑定设备
			if (ACTION_CONNECT_BINDED_DEVICE.equals(action)) {
				if (false == connect_status && !IsConnecting()) {
					mDeviceType = intent.getStringExtra("deviceName");
					String address = intent.getStringExtra("deviceAddress");

					Log.d(TAG, "ACTION_CONNECT_BINDED_DEVICE,name:"
							+ mDeviceType + ",address:" + address);

					Bundle mBundle = new Bundle();
					mBundle.putString("name", mDeviceType);
					mBundle.putString("address", address);

					Message msg = mHandler.obtainMessage();
					msg.what = CONNECT_BINDED_DEVICE;
					msg.setData(mBundle);
					mHandler.sendMessageDelayed(msg, 500);
				}
			}

			// 断开绑定设备
			if (ACTION_DISCONNECT_BINDED_DEVICE.equals(action)) {
				if (true == connect_status) {
					Log.d(TAG, "ACTION_DISCONNECT_BINDED_DEVICE");
					Util.handUnlink(BleManagerService.this, true);
					mIsUnbinding = true;
					mBindedDeviceAddress = intent.getStringExtra("BINDED_DEVICE_ADDRESS");
					Log.d(TAG, "mBindedDeviceAddress111" + mBindedDeviceAddress);
					if (mBindedDeviceAddress != null&& !mBindedDeviceAddress.equals("0"))
						mHandler.sendEmptyMessageDelayed(DISCONNECT_BINDED_DEVICE, 0);
					Log.i(TAG, "mHandler.sendEmptyMessageDelayed(DISCONNECT_BINDED_DEVICE, 0)");
				}
			}

			// 久坐提醒
			if (ACTION_VIBRATION_REMIND.equals(action)) {
				Log.d(TAG, "ACTION_VIBRATION_REMIND");
				if (true == connect_status) {
					remindInfo = intent.getByteArrayExtra("remind_info");
					mHandler.sendEmptyMessageDelayed(SET_VIBRATION_REMIND, 2000);
				}
			}

			// 获取蓝牙电量
			if (ACTION_BATTERY_READ.equals(action)) {
				Log.d(TAG, "ACTION_BATTERY_READ");
				if (true == connect_status && !IsSupportedServicesListEmpty()) {
					mHandler.sendEmptyMessageDelayed(GET_BATTERYLV_FROM_REMOTE,
							2000);
				}
			}

			// 获取计步分段数据
			if (ACTION_STEP_DATA_READ.equals(action)) {
				Log.d(TAG, "ACTION_STEP_DATA_READ");
				if (true == connect_status && !IsSupportedServicesListEmpty()) {
					// zhongyang 20150527
					// mHandler.sendEmptyMessageDelayed(STEP_TOTAL_READ_CLOSE,100);
					mHandler.sendEmptyMessageDelayed(STEP_ENABLE_NOTIFATION,
							1000);
				}
			}

			// 20150528
			// 关闭统计步数的notify
			if (ACTION_TOTALSTEP_DISABLE_DATA_READ.equals(action)) {
				if (status == BluetoothGatt.GATT_SUCCESS) {
					Log.i(TAG,
							"receiver broadcast ACTION_TOTALSTEP_DISABLE_DATA_READ");
					mHandler.sendEmptyMessageDelayed(STEP_TOTAL_READ_CLOSE,
							1000);
				}
			}

			if (ACTION_STATISTICS_STEP_READ.equals(action)) {
				Log.d(TAG, "ACTION_STATISTICS_STEP_READ");
				if (true == connect_status && !IsSupportedServicesListEmpty()) {
					mHandler.sendEmptyMessageDelayed(STEP_DISABLE_NOTIFATION,
							1000);
				}
			}

			// 获取计步统计数据
			if (ACTION_STEP_TOTAL_DATA.equals(action)) {
				Log.d(TAG, "ACTION_TOTAL_STEP");
				if (true == connect_status && !IsSupportedServicesListEmpty()) {
					// zhongyang
					mHandler.sendEmptyMessageDelayed(STEP_GETTOTAL_READY, 1000);
				}
			}
			// 20150618 Heph add 连接后获取名字
			if (ACTION_GET_DEVICE_NAME.equals(action)) {
				Log.d(TAG, "ACTION_GET_DEVICE_NAME");
				if (true == connect_status && !IsSupportedServicesListEmpty()) {
					mHandler.sendEmptyMessageDelayed(GET_DEVICE_NAME, 100);
				}
			}

			// 闹钟设置
			if (ACTION_UPDATE_ALARM_INFO.equals(action)) {
				Log.d(TAG, "ACTION_UPDATE_ALARM_INFO");
				if (true == connect_status && !IsSupportedServicesListEmpty()) {
					Message msg = mHandler.obtainMessage();
					msg.what = SET_ALARM_TIME;
					msg.obj = intent.getStringExtra("alarmInfo");
					mHandler.sendMessageDelayed(msg, 1000);
				}
			}

			// 新短信通知
			if (ACTION_NOTICE_NEW_SMS.equals(action)) {
				Log.d(TAG, "ACTION_NOTICE_NEW_SMS");
				if (true == connect_status && !IsSupportedServicesListEmpty()) {
					mHandler.sendEmptyMessageDelayed(NOTICE_NEW_SMS, 100);
				}
			}

			// 短信已读通知
			if (ACTION_NOTICE_READ_SMS.equals(action)) {
				Log.d(TAG, "ACTION_NOTICE_READ_SMS");
				if (true == connect_status && !IsSupportedServicesListEmpty()) {
					mHandler.sendEmptyMessageDelayed(NOTICE_READ_SMS, 100);
				}
			}

			// 新来电通知
			if (ACTION_NOTICE_NEW_CALL.equals(action)) {
				Log.d(TAG, "ACTION_NOTICE_NEW_CALL");
				if (true == connect_status && !IsSupportedServicesListEmpty()) {
					// get phone number from intent
					Message msg = mHandler.obtainMessage();
					msg.what = NOTICE_NEW_CALL;
					msg.obj = intent.getStringExtra("incomingNumber");
					mHandler.sendMessageDelayed(msg, 100);
				}
			}

			// 来电已读通知
			if (ACTION_NOTICE_CALL_END.equals(action)) {
				Log.d(TAG, "ACTION_NOTICE_CALL_END");
				if (true == connect_status && !IsSupportedServicesListEmpty()) {
					mHandler.sendEmptyMessageDelayed(NOTICE_CALL_END, 100);
				}
			}

			// 未接电话通知
			if (ACTION_NOTICE_MISS_CALL.equals(action)) {
				Log.d(TAG, "ACTION_NOTICE_MISS_CALL");
				if (true == connect_status && !IsSupportedServicesListEmpty()) {
					mHandler.sendEmptyMessageDelayed(NOTICE_MISS_CALL, 100);
				}
			}

			// 微信未读通知
			if (ACTION_NOTICE_NEW_WECHAT_MSG.equals(action)) {
				Log.d(TAG, "ACTION_NOTICE_NEW_WECHAT_MSG");
				if (true == connect_status && !IsSupportedServicesListEmpty()) {
					mHandler.sendEmptyMessageDelayed(NOTICE_NEW_WECHAT_MSG, 100);
				}
			}

			// 微信已读通知
			if (ACTION_NOTICE_READ_WECHAT_MSG.equals(action)) {
				Log.d(TAG, "ACTION_NOTICE_READ_WECHAT_MSG");
				if (true == connect_status && !IsSupportedServicesListEmpty()) {
					mHandler.sendEmptyMessageDelayed(NOTICE_READ_WECHAT_MSG,
							100);
				}
			}

			// 获取睡眠信息
			if (ACTION_GET_SLEEP_INFO.equals(action)) {
				Log.d(TAG, "ACTION_GET_SLEEP_INFO");
				if (true == connect_status && !IsSupportedServicesListEmpty()) {
					mHandler.sendEmptyMessageDelayed(GET_SLEEP_INFO, 1000);
				}
			}

			// 关闭睡眠通知
			if (ACTION_DISABLE_SLEEP_INFO.equals(action)) {
				Log.d(TAG, "ACTION_DISABLE_SLEEP_INFO");
				if (true == connect_status && !IsSupportedServicesListEmpty()) {
					mHandler.sendEmptyMessageDelayed(DISABLE_SLEEP_INFO, 1000);
				}
			}

			// 获取固件版本号
			if (ACTION_GET_FIRMWARE_VERSION.equals(action)) {
				Log.d(TAG, "ACTION_GET_FIRMWARE_VERSION");
				if (true == connect_status && !IsSupportedServicesListEmpty()) {
					mHandler.sendEmptyMessageDelayed(FIRMWARE_VERSION, 100);
				}
			}

			// 准备固件升级，让设备reset一下
			if (ACTION_READY_FIRMWARE_UPDATE.equals(action)) {
				Log.d(TAG, "ACTION_READY_FIRMWARE_UPDATE");
				if (true == connect_status && !IsSupportedServicesListEmpty()) {
					Util.handUnlink(BleManagerService.this, true);
					mHandler.sendEmptyMessageDelayed(FIRMWARE_UPDATE_READY, 100);
				}
			}
			// low_battery_remind dialog
			if (ACTION_LOW_BATTERY_REMIND.equals(action)) {
				Log.d(TAG, ACTION_LOW_BATTERY_REMIND);
				if (low_battery_remind == 1) {
					if (true == connect_status
							&& !IsSupportedServicesListEmpty()) {
						CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(
								sContext);
						builder.setTitle(R.string.alert_title);
						builder.setMessage(R.string.low_battery);
						builder.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								});
						// builder.cre ate().show();
						CustomAlertDialog dialog = builder.create();
						dialog.getWindow().setType(
								WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
						dialog.show();
						low_battery_remind = 2;
					}
				}
			}

			if (ACTION_CLOSE_BLE_PHONE_STEPS.equals(action)) {
				Log.d(TAG, ACTION_CLOSE_BLE_PHONE_STEPS);
				if (true == Tools.getPhonePedState()) {
					CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(
							sContext);
					builder.setTitle(R.string.alert_title);
					builder.setMessage(R.string.close_phone_steps);
					builder.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent phoneStepsIntent = new Intent(
											getApplicationContext(),
											PedBackgroundService.class);
									stopService(phoneStepsIntent);
									// mEnable.setImageResource(R.drawable.warn_off);
//									DayPedometerActivity.isOpen = !DayPedometerActivity.isOpen;
									Tools.setPhonePedState(false);
									dialog.dismiss();
									// finish();
								}
							});
					builder.setNegativeButton(R.string.cancle,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
					builder.setCancelable(false);
					CustomAlertDialog dialog = builder.create();
					dialog.getWindow().setType(
							WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
					dialog.show();
				}
			}
			// find_phone_remind dialog
			if (ACTION_FIND_PHONE_REMIND.equals(action)) {
				Log.d(TAG, ACTION_FIND_PHONE_REMIND);
				if (true == connect_status && !IsSupportedServicesListEmpty()) {
					CustomCmd.praserInPlug((0x10 & 0xff), null, null);

				}
			}

			if (BluetoothLeService.ACTION_DATA_READ.equals(action)) {
				if (status == BluetoothGatt.GATT_SUCCESS) {
					Log.d(TAG, "ACTION_DATA_READ");
				}
			}

			if (BluetoothLeService.ACTION_DATA_WRITE.equals(action)) {
				if (status == BluetoothGatt.GATT_SUCCESS) {
					Log.d(TAG, "ACTION_DATA_WRITE");
				}
			}

			if (BluetoothLeService.ACTION_DATA_NOTIFY.equals(action)) {
				if (status == BluetoothGatt.GATT_SUCCESS) {
					Log.d(TAG, "ACTION_DATA_NOTIFY");
				}
			}

		}
	};

	// Code to manage Service life cycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
					.getService();

			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize BluetoothLeService");
				stopSelf();
			}
			// auto connect Thread
			// new Thread(AutoConnectRunnable).start();
		}

		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
			Log.i(TAG, "BluetoothLeService disconnected");
		}
	};

	public static BleManagerService getInstance() {
		if (sInstance == null) {
			Log.d(TAG, "getInstance(), BleManagerService is null.");
			startBleManagerService();
		}
		return sInstance;
	}

	private static void startBleManagerService() {
		Intent startServiceIntent = new Intent(sContext,
				BleManagerService.class);
		sContext.startService(startServiceIntent);
	}

	private void registerReceiver() {
		registerReceiver(mReceiver, mFilter);
		registerReceiver(mGattUpdateReceiver, mGATTFilter);
	}

	private void unregisterReceiver() {
		unregisterReceiver(mReceiver);
		unregisterReceiver(mGattUpdateReceiver);
	}

	public boolean GetBleConnectState() {
		return connect_status;
	}

	public boolean IsSupportedServicesListEmpty() {
		return mServiceList == null;
	}

	Runnable AutoConnectRunnable = new Runnable() {
		@Override
		public void run() {
			while (mBleSupport && mCloseThread) {
				Log.d(TAG, "AutoConnectRunnable:" + System.currentTimeMillis());
				Log.d(TAG, "mBTSupport:" + mBTSupport);
				if (mBTSupport) {
					Log.d(TAG, "mIsConnecting:" + mIsConnecting);
					Log.d(TAG, "mIsUnbinding:" + mIsUnbinding);
					Log.d(TAG, "isConnected():" + isConnected());
					Log.d(TAG, "isBinded():" + isBinded());
					if (!mIsConnecting && !mIsUnbinding && !isConnected()
							&& isBinded()) {
						try {
							// Log.d(TAG,"mBluetoothLeService:" +
							// mBluetoothLeService);
							if (mBluetoothLeService != null) {
								String deviceAddress = msharepreference
										.getString("BLE_BIND_STATE", "0");
								Log.d(TAG, "deviceAddress" + deviceAddress);
								if (!deviceAddress.equals("0")) {
									ConnectToDeviceByAddress(deviceAddress);
									setIsConnecting(true);
								}
							}
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} else {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	};

	private boolean isBinded() {
		// Log.d(TAG,"isBinded"+(msharepreference.getString("BLE_BIND_STATE",
		// "0").equals("0")?false:true));
		// Log.d(TAG,"deviceAddress"+msharepreference.getString("BLE_BIND_STATE",
		// "0"));
		return msharepreference.getString("BLE_BIND_STATE", "0").equals("0") ? false
				: true;
	}

	private boolean isConnected() {
		// Log.d(TAG,"isConnected"+
		// msharepreference.getBoolean("BLE_CONNECT_STATE", false));
		return msharepreference.getBoolean("BLE_CONNECT_STATE", false);
	}

	private synchronized void setIsConnecting(boolean val) {
		mIsConnecting = val;
	}

	private synchronized boolean IsConnecting() {
		return mIsConnecting;
	}

	private byte[] long2Byte(long src) {
		byte[] temp = new byte[4];
		temp[0] = (byte) (src >> 24);
		temp[1] = (byte) (src >> 16);
		temp[2] = (byte) (src >> 8);
		temp[3] = (byte) (src >> 0);

		return temp;
	}
}
