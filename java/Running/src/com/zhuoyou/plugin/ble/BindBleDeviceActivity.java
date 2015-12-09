package com.zhuoyou.plugin.ble;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyou.plugin.bluetooth.connection.BtProfileReceiver;
import com.zhuoyou.plugin.bluetooth.data.Util;
import com.zhuoyou.plugin.bluetooth.service.BluetoothService;
import com.zhuoyou.plugin.custom.CustomAlertDialog;
import com.zhuoyou.plugin.resideMenu.EquipManagerListActivity;
import com.zhuoyou.plugin.resideMenu.FrequentlyQuestionsActivity;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;

@SuppressLint("NewApi")
public class BindBleDeviceActivity extends Activity implements
		View.OnClickListener {

	public static Activity instance = null;
	private String TAG = "BindBleDeviceActivity";

	private ImageView mDeviceAni = null;
	// private TextView mBondTips = null;
	private TextView mNumDevices = null;
	private LinearLayout mBondHelp, mBondScan = null;

	private boolean mBTEnadble = false;
	private Animation animation;
	private Animation BluetoothSinalAni;
	private ListView mDecList;
	private ImageView mTitleBack;
	private TextView mTitle;
	private LinearLayout mMoreEquip, mFreQuestion;

	private boolean mScanning = false;
	private boolean mIsConnected = false;
	private BleDeviceInfo curConnectingDevice;
	private BluetoothAdapter mBluetoothAdapter;
	// private List<BleDeviceInfo> mDeviceInfoList;
	private LeDeviceListAdapter mLeDeviceListAdapter;
	private String[] mDeviceFilter = { "Unik 1", "Unik 2", "LEO" };
	// private String
	private int mNumDevs = 0;
	private CustomTimer mScanTimer = null;
	private CustomTimer mConnectTimer = null;
	private Thread checkServiceThread = null;
	private BleManagerService mBleDeviceManagerService = null;

	private static final int REQUEST_ENABLE_BT = 1;
	private static final int SCAN_TIMEOUT = 10; // Seconds
	private static final int CONNECT_TIMEOUT = 40; // Seconds
	private static final int GETTING_SERVICE_TIMEOUT = 60; // Seconds
	private LeScanCallback mLeScanCallback;
	private int clickposition = -1;
	private int connState = -1;
	private int lastClickPosition = -1;

	private boolean isClickConn = false;
	// private ImageView mSearchIcon = null;
	private boolean is_conneting = false;
	private String SearchName;
	private static BluetoothDevice connectDevice = null;
	private boolean isSupportBle = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_bind_venus);
		instance = this;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		mDecList = (ListView) findViewById(R.id.devices_listview);
		LinearLayout footer = new LinearLayout(this);
		footer = (LinearLayout) this.getLayoutInflater().inflate(R.layout.equipment_footer, null);
		mDecList.addFooterView(footer, null, false);
		initView();

		IntentFilter mFilter = new IntentFilter(BluetoothLeService.ACTION_GATT_CONNECTED);
		mFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		mFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

		registerReceiver(mBroadcastReceiver, mFilter);

		initBluetoothView();
	}

	private void initCallback() {
		// TODO Auto-generated method stub
		// Device scan callback.
		mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
			@Override
			public void onLeScan(final BluetoothDevice device, final int rssi,
					byte[] scanRecord) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Log.d(TAG, "onLeScan Thread");
						// Filter devices
						if (checkDeviceFilter(device)) {
							if (!deviceInfoExists(device.getAddress())) {

								// New device
								Log.d(TAG,
										"device.getAddress() :"
												+ device.getAddress());
								BleDeviceInfo deviceInfo = createDeviceInfo(
										device, rssi);
								addDevice(deviceInfo);
							} else {
								// Already in list, update RSSI info
								BleDeviceInfo deviceInfo = findDeviceInfo(device);
								deviceInfo.updateRssi(rssi);
							}
							mLeDeviceListAdapter.notifyDataSetChanged();
						}
					}
				});
			}
		};
	}

	private void initNotSupportView() {
		// TODO Auto-generated method stub
		mDecList.setAdapter(null);
	}

	private void initBluetooth() {
		// TODO Auto-generated method stub

		mBleDeviceManagerService = BleManagerService.getInstance();

		mNumDevices = (TextView) findViewById(R.id.num_devices);
		mNumDevices.setVisibility(View.VISIBLE);
		// mBondHelp = (LinearLayout) findViewById(R.id.bind_help);

		mLeDeviceListAdapter = new LeDeviceListAdapter();
		mDecList.setAdapter(mLeDeviceListAdapter);

		mDecList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Tools.setConnectNotVibtation(false);
				connectDeviceByListPosition(position);
				isClickConn = true;
				Log.d(TAG, "sendBroadcast ACTION_DISCONNECT_BINDED_DEVICE");

			}
		});
		//
		// final BluetoothManager bluetoothManager = (BluetoothManager)
		// getSystemService(Context.BLUETOOTH_SERVICE);
		// mBluetoothAdapter = bluetoothManager.getAdapter();

		mDeviceAni = (ImageView) findViewById(R.id.ani_device);
		mDeviceAni.setVisibility(View.VISIBLE);
		mDeviceAni.setBackgroundResource(R.drawable.refresh_devices);
		animation = AnimationUtils.loadAnimation(this,
				R.anim.search_devices_ani);
		mDeviceAni.setAnimation(animation);
		// animation.start();
		animation.startNow();
	}

	private void initBluetoothView() {
		if (mBluetoothAdapter.isEnabled()) {// bluetooth is open
			if (!RunningApp.isBLESupport) {// don't support ble

				initNotSupportView();
				isSupportBle = false;
				Toast.makeText(getApplicationContext(),
						R.string.not_support_ble_tip, Toast.LENGTH_SHORT)
						.show();
			} else {// support ble

				isSupportBle = true;
				initCallback();
				initBluetooth();
				startScan();
				updateBtStatusAndUI();
			}

		} else {// bluetooth is close
			if (mDeviceAni != null) {
				mDeviceAni.setVisibility(View.GONE);
			}
			if (mNumDevices != null) {
				if (animation.hasStarted()) {
					animation.cancel();
					mDeviceAni.clearAnimation();

				}
				mNumDevices.setVisibility(View.GONE);
			}

			Toast.makeText(getApplicationContext(),
					R.string.ensure_bluetooth_isenable, Toast.LENGTH_SHORT)
					.show();
			mDecList.setAdapter(null);
		}

	}

	private void initView() {
		// TODO Auto-generated method stub
		mTitleBack = (ImageView) findViewById(R.id.bt_back);
		mTitleBack.setOnClickListener(this);
		mTitle = (TextView) findViewById(R.id.bt_title);
		mTitle.setText(R.string.nearby_equip);
		mMoreEquip = (LinearLayout) findViewById(R.id.more_equip);
		mMoreEquip.setOnClickListener(this);
		// mFreQuestion = (LinearLayout) findViewById(R.id.bind_help);
		// mFreQuestion.setOnClickListener(this);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (isSupportBle) {
			if (mScanning) {
				stopScan();
				mScanning = false;
			}
			stopTimers();

		}
		unregisterReceiver(mBroadcastReceiver);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.ani_device:

			if (animation.hasEnded()) {

				startScan();
			} else {
				stopTimers();

				stopScan();
			}
			break;

		// case R.id.bind_help:
		// Intent intent = new Intent(BindBleDeviceActivity.this,
		// FrequentlyQuestionsActivity.class);
		// startActivity(intent);
		// break;
		case R.id.bt_back:
			finish();
			break;
		case R.id.more_equip:
			Intent intent_more = new Intent(BindBleDeviceActivity.this,
					MoreEquipActivity.class);
			startActivity(intent_more);
			break;

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		if (requestCode == REQUEST_ENABLE_BT
				&& resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void startScan() {
		// Start device discovery
		isClickConn = false;
		mNumDevs = 0;
		// mBondTips.setText("");
		mLeDeviceListAdapter.clear();
		scanLeDevice(true);
		if (mScanning) {

			startPBAnimator();
			mScanTimer = new CustomTimer(SCAN_TIMEOUT, mScanCallback);
		} else {
			Log.d(TAG, "Device discovery start failed");
		}

	}

	private void stopScan() {
		mScanning = false;
		scanLeDevice(false);
		stopPBAnimator();
	}

	private boolean scanLeDevice(boolean enable) {
		if (enable) {
			mScanning = mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
		return mScanning;
	}

	public void onScanTimeout() {
		runOnUiThread(new Runnable() {
			public void run() {
				stopScan();
				// updateSerchButtonStatus();
				autoChoiceByScanResult();
			}
		});
	}

	private BleDeviceInfo createDeviceInfo(BluetoothDevice device, int rssi) {
		BleDeviceInfo deviceInfo = new BleDeviceInfo(device, rssi);
		return deviceInfo;
	}

	private boolean checkDeviceFilter(BluetoothDevice device) {
		int n = 0;
		if (mDeviceFilter != null)
			n = mDeviceFilter.length;

		if (n > 0) {
			boolean found = false;
			for (int i = 0; i < n && !found
					&& !TextUtils.isEmpty(device.getName()); i++) {
				found = device.getName().equals(mDeviceFilter[i]);
				Log.i(TAG, "deviceName=" + device.getName());

			}
			Log.d(TAG, "found = " + found);
			return found;
		} else {
			// Allow all devices if the device filter is empty
			return true;
		}
	}

	private boolean deviceInfoExists(String address) {
		for (int i = 0; i < mLeDeviceListAdapter.getCount(); i++) {
			if (mLeDeviceListAdapter.getDevice(i).getBluetoothDevice()
					.getAddress().equals(address)) {
				return true;
			}
		}
		return false;
	}

	private BleDeviceInfo findDeviceInfo(BluetoothDevice device) {
		for (int i = 0; i < mLeDeviceListAdapter.getCount(); i++) {
			if (mLeDeviceListAdapter.getDevice(i).getBluetoothDevice()
					.getAddress().equals(device.getAddress())) {
				return mLeDeviceListAdapter.getDevice(i);
			}
		}
		return null;
	}

	private void addDevice(BleDeviceInfo device) {
		mLeDeviceListAdapter.addDevice(device);
		if (mNumDevs > 1 || mNumDevs == 1)
			mNumDevices.setText(mNumDevs
					+ getResources().getString(R.string.bind_venus_devices));
	}

	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			connState = -1;
			final String action = intent.getAction();
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {

				int status = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
						BluetoothAdapter.STATE_OFF);
				Log.d(TAG, "ACTION_STATE_CHANGED :" + status);
				if (status == BluetoothAdapter.STATE_OFF) {// close

					mNumDevs = 0;
					// mLeDeviceListAdapter.clear();
					setScanButtonClickable(false);
					// initBluetoothView();
					refresh();
				} else if (status == BluetoothAdapter.STATE_ON) {// open

					setScanButtonClickable(true);
					// initBluetoothView();
					refresh();

				}

			}

			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {// connected
				mIsConnected = true;
				stopTimers();
				stopPBAnimator();
				checkServiceThread = new Thread(WaitGetServiceRunnable);
				checkServiceThread.start();
				is_conneting = false;
			}

			if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)// disconnected
					&& isClickConn) {
				mIsConnected = false;
				Log.d(TAG, "ACTION_GATT_DISCONNECTED");
				setScanButtonClickable(true);
				stopTimers();
				stopPBAnimator();

				connState = 3;
				mLeDeviceListAdapter.notifyDataSetChanged();
				is_conneting = false;

				if (checkServiceThread != null) {
					checkServiceThread.interrupt();
					checkServiceThread = null;
				}

			}
		}

		private void refresh() {
			// TODO Auto-generated method stub
			finish();
			Intent intent = new Intent(BindBleDeviceActivity.this,BindBleDeviceActivity.class);
			startActivity(intent);
			onCreate(null);
		}
	};

	Runnable WaitGetServiceRunnable = new Runnable() {
		@Override
		public void run() {
			int i = GETTING_SERVICE_TIMEOUT; // timeout = 31*500ms==15s
			if (mBleDeviceManagerService != null) {
				while (mBleDeviceManagerService.IsSupportedServicesListEmpty()&& i > 0) {
					try {
						i--;
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (mBleDeviceManagerService.IsSupportedServicesListEmpty()) {
					Log.d(TAG, "Get Service TimeOut,disconnect the devices");
					disconnectCurrentDevice();
					stopPBAnimator();
					is_conneting = false;
				} else {
					Log.d(TAG, "Get Service OK");
					stopPBAnimator();
					BindBleDeviceActivity.this.setResult(RESULT_OK);

					finish();
				}
			}
		}
	};

	private void stopTimers() {
		if (mScanTimer != null) {
			Log.d(TAG, "stopScanTimers");
			mScanTimer.stop();
			mScanTimer = null;
		}

		if (mConnectTimer != null) {
			Log.d(TAG, "stopConnectTimers");
			mConnectTimer.stop();
			mConnectTimer = null;
		}
	}

	// Listener for scan timer expiration
	private CustomTimerCallback mScanCallback = new CustomTimerCallback() {
		public void onTimeout() {
			Log.d(TAG, "onTimeout");
			onScanTimeout();
		}

		public void onTick(int i) {
		}
	};

	// Listener for connect/disconnect expiration
	private CustomTimerCallback mConnectCallback = new CustomTimerCallback() {
		public void onTimeout() {
			Log.d(TAG, "Connecting TimeOut,disconnect the devices");
			stopPBAnimator();
			disconnectCurrentDevice();
		}

		public void onTick(int i) {
		}
	};

	private void connectDeviceByListPosition(int position) {

		// delete device if exist
//ble
		String BleAddress = Util.getLatestConnectDeviceAddress(BindBleDeviceActivity.this);
		Intent DisconnectDevicesIntent = new Intent(BleManagerService.ACTION_DISCONNECT_BINDED_DEVICE);// disconnect current  device
		DisconnectDevicesIntent.putExtra("BINDED_DEVICE_ADDRESS", BleAddress);
		sendBroadcast(DisconnectDevicesIntent);

		
		//classic
		connectDevice = Util.getConnectDevice();
		if (connectDevice != null) {
			String ClassicAddress = connectDevice.getAddress();
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(ClassicAddress);
			Util.removeBond(device);
		}
		
		connState = -1;
		clickposition = position;
		mLeDeviceListAdapter.notifyDataSetChanged();
		if (!is_conneting) {
			is_conneting = true;
			final BleDeviceInfo device = mLeDeviceListAdapter.getDevice(position);
			Log.d(TAG, "device" + device);
			Log.d(TAG, "mBleDeviceManagerService" + mBleDeviceManagerService);
			if (device == null)
				return;
			if (mBleDeviceManagerService != null) {
				Log.d(TAG, "onClick:Connect device :"
						+ device.getBluetoothDevice().getName() + ";address ="
						+ device.getBluetoothDevice().getAddress());
				boolean result = false;
				if (device.getBluetoothDevice().getAddress() != null) {

					setScanButtonClickable(false);

					// updateBondTipsStatus(R.string.pg_message_connect);
					mConnectTimer = new CustomTimer(CONNECT_TIMEOUT,
							mConnectCallback);
					result = mBleDeviceManagerService.ConnectToDevice(device);
				}

				if (result) {
					Log.d(TAG, "connect sussec");
					curConnectingDevice = device;
					setResult(RESULT_OK);

				} else {
					Log.d(TAG, "connect failed");
					setResult(RESULT_CANCELED);
				}
			}
		}
	}

	private void disconnectCurrentDevice() {
		setScanButtonClickable(true);
		if (mIsConnected && curConnectingDevice != null)
			if (curConnectingDevice.getBluetoothDevice().getAddress() != null) {
				mBleDeviceManagerService.disConnectDevice(curConnectingDevice);
			}
	}

	private void autoChoiceByScanResult() {
		if (mNumDevs == 1) {
			connectDeviceByListPosition(0);

		}
	}

	// private void updateSerchButtonStatus() {
	// Log.d(TAG, "updateSerchButtonStatus");
	// Log.d(TAG, "mScanning :" + mScanning);
	// if (mScanning) {
	// // mBondScan.setText(R.string.bind_venus_stopscan);
	// } else {
	// // mBondScan.setText(R.string.bind_venus_rescan);
	// }
	// }

	// private void updateBondTipsStatus(int resId) {
	// Log.d(TAG, "updateBondTipsStatus");
	// mBondTips.setText(resId);
	// }

	// private void resetBondTips() {
	// Log.d(TAG, "resetBondTips");
	// mBondTips.setText("");
	// }

	private void setScanButtonClickable(boolean enable) {
		if (mDeviceAni != null)
			mDeviceAni.setClickable(enable);
		;
	}

	private void updateBtStatusAndUI() {
		// final BluetoothManager bluetoothManager = (BluetoothManager)
		// getSystemService(Context.BLUETOOTH_SERVICE);
		// BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (bluetoothAdapter.isEnabled()) {
			mBTEnadble = true;
		} else {
			mBTEnadble = false;
		}

		if (mBTEnadble) {
			setScanButtonClickable(true);
		} else {
			// updateBondTipsStatus(R.string.tip_open_bt);
			setScanButtonClickable(false);
		}
	}

	private void startPBAnimator() {
		Log.i("hello", "start startPBAnimator");
		animation.reset();
		mDeviceAni.startAnimation(animation);
		animation.start();

	}

	private void stopPBAnimator() {
		Log.i("hello", "stop stopPBAnimator");
		if (animation.hasStarted()) {
			animation.cancel();
			mDeviceAni.clearAnimation();

		}
	}

	static class ViewHolder {
		ImageView deviceVenus;
		TextView deviceName;
		TextView deviceAddress;
		ImageView deviceRssi;

	}

	private class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<BleDeviceInfo> mLeDevices;
		private LayoutInflater mInflator;

		public LeDeviceListAdapter() {
			super();
			mLeDevices = new ArrayList<BleDeviceInfo>();
			mInflator = BindBleDeviceActivity.this.getLayoutInflater();
		}

		public void addDevice(BleDeviceInfo device) {
			if (!mLeDevices.contains(device)) {
				mNumDevs++;
				mLeDevices.add(device);
			}
		}

		public BleDeviceInfo getDevice(int position) {
			return mLeDevices.get(position);
		}

		public void clear() {
			mLeDevices.clear();
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;
			// General ListView optimization code.
			if (view == null) {
				view = mInflator.inflate(R.layout.listitem_device, null);

				viewHolder = new ViewHolder();
				viewHolder.deviceVenus = (ImageView) view.findViewById(R.id.device_venus_little);
				viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
				viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
				viewHolder.deviceRssi = (ImageView) view.findViewById(R.id.device_rssi);

				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			BleDeviceInfo device = mLeDevices.get(i);
			final String deviceName = device.getBluetoothDevice().getName();
			if (deviceName != null && deviceName.length() > 0)
				viewHolder.deviceName.setText(deviceName);
			else
				viewHolder.deviceName.setText(R.string.unknown_device);
			viewHolder.deviceVenus.setBackgroundResource(Util.getProductIcon(deviceName, true));
			viewHolder.deviceAddress.setText(device.getBluetoothDevice().getAddress());

			if (i == clickposition) {
				if (connState == 3) {
					viewHolder.deviceRssi.setBackgroundResource(R.drawable.equip_disconnected);
					view.findViewById(R.id.bt_disconnected).setVisibility(View.VISIBLE);
				} else {
					viewHolder.deviceRssi.setBackgroundResource(R.anim.bluetooth_singal);
					AnimationDrawable animationdrawable = (AnimationDrawable) viewHolder.deviceRssi.getBackground();
					animationdrawable.setOneShot(false);
					animationdrawable.start();
					view.findViewById(R.id.bt_disconnected).setVisibility(View.GONE);
				}

			} else {
				viewHolder.deviceRssi.setBackgroundResource(getRssiImage(mLeDevices.get(i).getRssi()));
				view.findViewById(R.id.bt_disconnected).setVisibility(View.GONE);
			}

			return view;
		}

		private int getRssiImage(int rssi) {
			if (rssi > -70 && rssi < -55) {
				return R.drawable.rssi_batter;
			} else if (rssi > -55) {
				return R.drawable.rssi_good;
			} else {
				return R.drawable.rssi_bad;
			}
		}
	}

}
