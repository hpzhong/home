package com.zhuoyou.plugin.resideMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyou.plugin.ble.BindBleDeviceActivity;
import com.zhuoyou.plugin.ble.BleManagerService;
import com.zhuoyou.plugin.bluetooth.attach.BTPluginActivity;
import com.zhuoyou.plugin.bluetooth.attach.PluginManager;
import com.zhuoyou.plugin.bluetooth.data.Util;
import com.zhuoyou.plugin.bluetooth.product.ProductManager;
import com.zhuoyou.plugin.bluetooth.service.BluetoothService;
import com.zhuoyou.plugin.custom.CustomAlertDialog;
import com.zhuoyou.plugin.gps.FirstGpsActivity;
import com.zhuoyou.plugin.gps.GaoDeMapActivity;
import com.zhuoyou.plugin.mainFrame.MineFragment;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;

public class EquipManagerListActivity extends Activity implements OnClickListener {
	public static Handler mHandler;
	public static final int ENTER_PLUGIN_DETAIL = 1;
	public static final int REMOVE_DEVICE_ITEM = 2;
	public static final int BATTERY = 3;
	private static List<Map<String, Object>> bondedDevices;
	private HashMap<String, String> bleBondMap; 
	private static Button mEdit;
	private RelativeLayout device_layout;
	private ImageView device_logo;
	private TextView device_name;
	private static TextView device_battery;
	private RelativeLayout details_layout, remove_layout;
	public ListView mDeviceList;
	private String battery;
	private static DeviceListAdapter mDeviceListAdapter = null;
	private static BluetoothAdapter btAdapt;
	private static BluetoothDevice connectDevice = null;
	private static BluetoothDevice gattconnectDevice = null;
	private List<BluetoothDevice> bondDevices = null;
	private static EquipManagerListActivity this_ = null;
	public static boolean isEditMode = false;
	private RelativeLayout mSearch;
	private static TextView mConntedTv;
	private static TextView mBondedTv;
	private String connectProductName;
	private PluginManager manager;
	private String BLEDeviceFilter = "Unik 1|Unik 2|LEO|Unik 3|LUNA 3";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.equip_manage_dialog_layout);
		btAdapt = BluetoothAdapter.getDefaultAdapter();
		isEditMode = false;
		this_ = this;
		manager = PluginManager.getInstance();
		if (getIntent() != null) {
			battery = getIntent().getStringExtra("battery");
		}
		initView();
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				Map<String, Object> map;
				switch (msg.what) {
				case ENTER_PLUGIN_DETAIL:
					Intent intent;
					map = (Map<String, Object>) msg.obj;
					PluginManager manager = PluginManager.getInstance();
					String name = (String) map.get("name");
					String productName = Util.getProductName(name);
					String nickname = ProductManager.getInstance().getProductCategory(productName);
					manager.processPlugList(productName);
					if (manager.getPlugBeans().size() > 0) {
						intent = new Intent(this_, BTPluginActivity.class);
						intent.putExtra("nick_name", nickname);
						intent.putExtra("remote_name", productName);
						intent.putExtra("enable_state", false);
						this_.startActivity(intent);
					}
					break;
				case REMOVE_DEVICE_ITEM:
					map = (Map<String, Object>) msg.obj;
					String devicename= (String)map.get("name");
					String address = (String) map.get("address");
					BluetoothDevice device = btAdapt.getRemoteDevice(address);
					Util.removeBond(device);
					bondedDevices.remove(map);
					Tools.removeBleBindInfo(EquipManagerListActivity.this, devicename, address);
					if(address.equals(Util.getLatestConnectDeviceAddress(EquipManagerListActivity.this))){
						Util.updateLatestConnectDeviceAddress(EquipManagerListActivity.this, "");
					}
					
					mDeviceListAdapter.notifyDataSetChanged();
					if(bondedDevices.size() == 0)
						mBondedTv.setVisibility(View.GONE);
					if(connectDevice == null && bondedDevices.size() <= 0){
						isEditMode = false;
						mEdit.setText(R.string.bt_edit);
					}
					break;
				case BATTERY:
					int status = msg.arg1;
					int battery_num = msg.arg2 - 0x20;
					if (battery_num == 0xff) {
						battery = getResources().getString(R.string.low_power_consumption);
					} else {
						if (status == 1) {
							battery = getResources().getString(R.string.charging);
						} else {
							if (status == 2) {
								battery = getResources().getString(R.string.charging_complete);
							} else {
								battery = getResources().getString(R.string.remaining_battery) + battery_num + "%";
							}
						}
					}
					device_battery.setText(battery);
					break;
				default:
					break;
				}
			}
		};
		IntentFilter intent = new IntentFilter();
		intent.addAction("com.zhuoyou.running.connect.success");
		intent.addAction("com.zhuoyou.running.connect.failed");
		registerReceiver(mBTConnectReceiver, intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i("hello", "Equ onResume");
		updateProductView();
		
		isEditMode = false;
		mEdit.setText(R.string.bt_edit);
		if (connectDevice != null) {
			manager.processPlugList(connectProductName);
			if (manager.getPlugBeans().size() > 0){
				details_layout.setVisibility(View.VISIBLE);
				remove_layout.setVisibility(View.GONE);
			}
	
		}else if(Util.getLatestDeviceType(this)){
			String preDeviceAddress = Util.getLatestConnectDeviceAddress(this_);
			manager.processPlugList(Util.getProductName(Tools.keyString(bleBondMap, preDeviceAddress)));
			details_layout.setVisibility(View.VISIBLE);
			remove_layout.setVisibility(View.GONE);
	
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mBTConnectReceiver);
	}
	
	private BroadcastReceiver mBTConnectReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("com.zhuoyou.running.connect.success")) {
				updateProductView();
			} else if (action.equals("com.zhuoyou.running.connect.failed")) {
				updateProductView();
			}
		}		
	};

	private void initView() {
		mEdit = (Button) findViewById(R.id.edit);
		mSearch = (RelativeLayout) findViewById(R.id.searching);
		mSearch.setOnClickListener(this_);
		mConntedTv = (TextView)findViewById(R.id.connted);
		device_layout = (RelativeLayout) findViewById(R.id.device_layout);
		device_logo = (ImageView) findViewById(R.id.device_logo);
		device_name = (TextView)findViewById(R.id.device_name);
		device_battery = (TextView)findViewById(R.id.device_battery);
		details_layout = (RelativeLayout) findViewById(R.id.details_layout);
		details_layout.setOnClickListener(this_);
		remove_layout = (RelativeLayout) findViewById(R.id.remove_layout);
		remove_layout.setOnClickListener(this_);
		mBondedTv = (TextView)findViewById(R.id.bonded);
		mDeviceList = (ListView) findViewById(R.id.device_list);
	}

	private void updateProductView() {
		bondedDevices = new ArrayList<Map<String, Object>>();
		if (BluetoothService.getInstance() != null) {
			Map<String, Object> map;
			connectDevice = Util.getConnectDevice();
			bleBondMap = Tools.getBleBindDevice(this);

			if(connectDevice != null){
				String name = connectDevice.getName();
				connectProductName = Util.getProductName(name);
				manager.processPlugList(connectProductName);
				mConntedTv.setVisibility(View.VISIBLE);
				device_layout.setVisibility(View.VISIBLE);
				device_logo.setImageResource(Util.getIconByDeviceName(name, true));
				device_name.setText(connectDevice.getName());
				if (manager.getPlugBeans().size() == 0) {
					details_layout.setVisibility(View.GONE);
				}
				if (battery.equals(getResources().getString(R.string.getting_electricity)) || battery.equals(getResources().getString(R.string.not_connected))) {
					Intent intent1 = new Intent("com.tyd.plugin.receiver.sendmsg");
					intent1.putExtra("plugin_cmd", 0x03);
					intent1.putExtra("plugin_content", "");
					sendBroadcast(intent1);
					device_battery.setText(R.string.getting_electricity);
				} else {
					device_battery.setText(battery);
				}
			}

			bondDevices = Util.getBondDevice();
			for (int i = 0; i < bondDevices.size(); i++) {
				if (connectDevice != null && connectDevice.equals(bondDevices.get(i))) {
					bondDevices.remove(i);
				}
			}
			if (bondDevices.size() > 0) {
				for (int i = 0; i < bondDevices.size(); i++) {
					map = new HashMap<String, Object>();
					map.put("icon", Util.getIconByDeviceName(bondDevices.get(i).getName(), false));
					map.put("name", bondDevices.get(i).getName());
					map.put("address", bondDevices.get(i).getAddress());
					map.put("state", false);
					map.put("connect", getResources().getString(R.string.not_connected));
					bondedDevices.add(map);
				}
			}
		}
		
		//Only BLE Device
    	//Get Ble had binded devices.
		if(RunningApp.isBLESupport){
	    	List<BluetoothDevice> gattConnectedDeviceList = BleManagerService.getInstance().getGattCurrentDevice();
	    	if(gattConnectedDeviceList != null){
	    		if (!bleBondMap.isEmpty()) {
					for (int i = 0; i < gattConnectedDeviceList.size(); i++) {
						if (bleBondMap.containsValue(gattConnectedDeviceList.get(i).getAddress())) {
							gattconnectDevice = gattConnectedDeviceList.get(i);
						}
					}
	
					if (gattconnectDevice != null) {
						Log.d("yangyang","gattconnectDevice"+gattconnectDevice);
						String name = Tools.keyString(bleBondMap, gattconnectDevice.getAddress());
						connectProductName = Util.getProductName(name);
						manager.processPlugList(connectProductName);
						mConntedTv.setVisibility(View.VISIBLE);
						device_layout.setVisibility(View.VISIBLE);
						device_logo.setImageResource(Util.getIconByDeviceName(name, true));
						device_name.setText(connectProductName);
						
						if (manager.getPlugBeans().size() == 0) {
							details_layout.setVisibility(View.GONE);
						}
						if (battery.equals(getResources().getString(R.string.getting_electricity)) || battery.equals(getResources().getString(R.string.not_connected))) {
							Intent intent1 = new Intent("com.tyd.plugin.receiver.sendmsg");
							intent1.putExtra("plugin_cmd", 0x03);
							intent1.putExtra("plugin_content", "");
							sendBroadcast(intent1);
							device_battery.setText(R.string.getting_electricity);
						} else {
							device_battery.setText(battery);
						}
						bleBondMap.remove(name);
					}			
				}
			} else {
				gattconnectDevice = null;
			}
	    	
			if(!bleBondMap.isEmpty()){
				Iterator iter = bleBondMap.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					String deviceName = (String) entry.getKey();
					HashMap map = new HashMap<String, Object>();
					map.put("icon",Util.getIconByDeviceName(deviceName, false));
					map.put("name", entry.getKey());
					map.put("address", entry.getValue());
					map.put("state", false);
					map.put("connect",getResources().getString(R.string.not_connected));
					bondedDevices.add(map);
				}
			}
		}
		if (connectDevice == null && gattconnectDevice ==null) {
			mConntedTv.setVisibility(View.GONE);
			device_layout.setVisibility(View.GONE);
		}
		
		mDeviceListAdapter = new DeviceListAdapter(this, bondedDevices);
		mDeviceList.setAdapter(mDeviceListAdapter);
		mDeviceList.setOnItemClickListener(listViewItemClick);
		mBondedTv.setVisibility(View.VISIBLE);
    	if(bondedDevices.size() == 0){
    		mBondedTv.setVisibility(View.GONE);
		}
	}

	OnItemClickListener listViewItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Map<String, Object> map = bondedDevices.get(position);
			if ((Boolean) map.get("state")) {
				// nothing to do for the moment
			} else {
				if (!isEditMode) {
					String deviceName =  (String) map.get("name");
					String deviceAddress = (String)map.get("address");
					if(Util.isBleDevice(deviceName)){
						if(deviceName != null){
							Tools.setConnectNotVibtation(false);
							Intent intent = new Intent(BleManagerService.ACTION_CONNECT_BINDED_DEVICE);
							intent.putExtra("deviceName", deviceName);
							intent.putExtra("deviceAddress", deviceAddress);
							sendBroadcast(intent);
						}  
					}else{
						String address = (String) map.get("address");
						final BluetoothDevice device = btAdapt.getRemoteDevice(address);
						if (connectDevice == null) {
							Util.connect(device);
							Map<String, Object> newmap = new HashMap<String, Object>();
							newmap.put("icon", map.get("icon"));
							newmap.put("name", map.get("name"));
							newmap.put("address", map.get("address"));
							newmap.put("state", false);
							newmap.put("connect", getResources().getString(R.string.connecting));
							bondedDevices.set(position, newmap);
							mDeviceListAdapter.notifyDataSetChanged();
						} else {
							Util.connect(device);
							new Thread() {
								public void run() {
									try {
										Thread.sleep(3000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									Util.connect(device);
								}
							}.start();
						}
					}
				}
			}
		}
	};

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.edit:
			if (isEditMode) {
				isEditMode = false;
				mEdit.setText(R.string.bt_edit);
				if (connectDevice != null) {
					manager.processPlugList(connectProductName);
					if (manager.getPlugBeans().size() > 0){
						details_layout.setVisibility(View.VISIBLE);
						remove_layout.setVisibility(View.GONE);
					}
				

				}else if(Util.getLatestDeviceType(this)){
					String preDeviceAddress = Util.getLatestConnectDeviceAddress(this_);
					manager.processPlugList(Util.getProductName(Tools.keyString(bleBondMap, preDeviceAddress)));
					details_layout.setVisibility(View.VISIBLE);
					remove_layout.setVisibility(View.GONE);
			

				}
			} else {
				if (bondedDevices.size() > 0 || connectDevice != null) {
					isEditMode = true;
					mEdit.setText(R.string.it_is_ok);
					if (connectDevice != null) {
						details_layout.setVisibility(View.GONE);
						remove_layout.setVisibility(View.VISIBLE);
					
					}
				}else if(Util.getLatestDeviceType(this_)){  // zhongyang 20150508: add for ble 
					isEditMode = true;
					mEdit.setText(R.string.it_is_ok);
					details_layout.setVisibility(View.GONE);
					remove_layout.setVisibility(View.VISIBLE);
			
				}
				if(mDeviceListAdapter!=null){
					mDeviceListAdapter.notifyDataSetChanged();
				}
			}
			if(mDeviceListAdapter!=null){
				mDeviceListAdapter.notifyDataSetChanged();
			}
			break;
		case R.id.searching:
			if(BluetoothService.getInstance().isConnected()
					|| (RunningApp.isBLESupport && BleManagerService.getInstance()
							.GetBleConnectState())){
			CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(EquipManagerListActivity.this);
			builder.setTitle(R.string.search_tip);
			builder.setMessage(R.string.search_message);
			builder.setPositiveButton(R.string.know, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					
					
				}
			});
			builder.create().show();	
			}else{
				Intent intent = new Intent(this_, BindBleDeviceActivity.class);
				startActivity(intent);
				}
//			
//		BluetoothDevice	currentDevice=null;
//		String preDeviceAddress = Util.getLatestConnectDeviceAddress(EquipManagerListActivity.this);
//		Boolean	preDeviceType = Util.getLatestDeviceType(EquipManagerListActivity.this);		
//		if (preDeviceType) {//ble
//			CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(EquipManagerListActivity.this);
//			builder.setTitle(R.string.search_tip);
//			builder.setMessage(R.string.search_message);
//			builder.setPositiveButton(R.string.know, new DialogInterface.OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					dialog.dismiss();
//					
//					
//				}
//			});
//			builder.create().show();	
//			}else{
//			Intent intent = new Intent(this_, BindBleDeviceActivity.class);
//			startActivity(intent);
//			}
			break;
		case R.id.details_layout:
			if (!isEditMode) {
				String nickname = ProductManager.getInstance().getProductCategory(connectProductName);
				manager.processPlugList(connectProductName);
				if (manager.getPlugBeans().size() > 0) {
					Intent intents = new Intent(this_, BTPluginActivity.class);
					intents.putExtra("nick_name", nickname);
					intents.putExtra("remote_name", connectProductName);
					intents.putExtra("enable_state", true);
					this_.startActivity(intents);
				}
			}
			break;
		case R.id.remove_layout:
			if (isEditMode) {
				// zhongyang 20150508 add for ble
				if(Util.isBleDevice(connectProductName)){
					String address = Util.getLatestConnectDeviceAddress(this);
					Tools.removeBleBindInfo(this, connectProductName, address);
					if(address.equals(Util.getLatestConnectDeviceAddress(EquipManagerListActivity.this))){
						Util.updateLatestConnectDeviceAddress(EquipManagerListActivity.this, "");
					}	
					Intent unBindIntent = new Intent(BleManagerService.ACTION_DISCONNECT_BINDED_DEVICE);
					unBindIntent.putExtra("BINDED_DEVICE_ADDRESS", address);
					sendBroadcast(unBindIntent);
					mConntedTv.setVisibility(View.GONE);
					device_layout.setVisibility(View.GONE);
					isEditMode = false;
					mEdit.setText(R.string.bt_edit);
				}else{
					String address = connectDevice.getAddress();
					BluetoothDevice device = btAdapt.getRemoteDevice(address);
					Util.removeBond(device);
					connectDevice = null;
					mConntedTv.setVisibility(View.GONE);
					device_layout.setVisibility(View.GONE);
					if(connectDevice == null && bondedDevices.size() <= 0){
						isEditMode = false;
						mEdit.setText(R.string.bt_edit);
					}
				}
			}
			break;
		default:
			break;
		}
	}
}
