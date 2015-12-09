package com.zhuoyou.plugin.mainFrame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyi.account.IAccountListener;
import com.zhuoyi.account.ZyAccount;
import com.zhuoyou.plugin.action.MessageActivity;
import com.zhuoyou.plugin.album.SportsAlbum;
import com.zhuoyou.plugin.ble.BindBleDeviceActivity;
import com.zhuoyou.plugin.ble.BleManagerService;
import com.zhuoyou.plugin.ble.BluetoothLeService;
import com.zhuoyou.plugin.bluetooth.attach.BTPluginActivity;
import com.zhuoyou.plugin.bluetooth.attach.PluginManager;
import com.zhuoyou.plugin.bluetooth.connection.BtProfileReceiver;
import com.zhuoyou.plugin.bluetooth.data.Util;
import com.zhuoyou.plugin.bluetooth.product.ProductManager;
import com.zhuoyou.plugin.cloud.NetUtils;
import com.zhuoyou.plugin.component.AlarmMainActivity;
import com.zhuoyou.plugin.firmware.FirmwareService;
import com.zhuoyou.plugin.firmware.FwUpdateActivity;
import com.zhuoyou.plugin.info.PersonalInformation;
import com.zhuoyou.plugin.resideMenu.AddEquipActivity;
import com.zhuoyou.plugin.resideMenu.EquipManagerListActivity;
import com.zhuoyou.plugin.resideMenu.HelpActivity;
import com.zhuoyou.plugin.resideMenu.SettingActivity;
import com.zhuoyou.plugin.running.DayPedometerActivity;
import com.zhuoyou.plugin.running.HomePageFragment;
import com.zhuoyou.plugin.running.Main;
import com.zhuoyou.plugin.running.MotionDataActivity;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;
import com.zhuoyou.plugin.running.WaterIntakeActivity;
import com.zhuoyou.plugin.selfupdate.Constants;
import com.zhuoyou.plugin.selfupdate.MyHandler;
import com.zhuoyou.plugin.selfupdate.RequestAsyncTask;
import com.zhuoyou.plugin.selfupdate.SelfUpdateMain;

public class MineFragment extends Fragment implements OnClickListener {
    private static final String TAG = "MineFragment";

	private PluginManager manager;
	private Context mContext;
	private View mRootView;
	private ImageView title_setting;
	private LinearLayout user_layout;
	private ImageView drawer_top_face;
	private TextView usrname;
	private TextView signature;
	private Button log_in;
	private RelativeLayout device_layout;
	private ImageView device_logo;
	private LinearLayout connected_device;
	private TextView device_name;
	private TextView device_battery;
	private ImageView connect_state;
	private LinearLayout connect_device;
	private LinearLayout manager_layout;
	private ListView widget_listView;
	private WidgetAdapter widgetAdapter;
	private BluetoothAdapter mBluetoothAdapter;

	private ProgressBar progressCircle;
	private List<WidgetItem> mWidgetItems = new ArrayList<WidgetItem>();
	private int[] widgetName = { R.string.equip_manager,
			R.string.firmware_upgrade, R.string.data_center,
			 R.string.sedentary_remind,R.string.water_intake,R.string.day_pedometer,
			R.string.brain_alarm,/* R.string.message,*/
			R.string.software_updates, R.string.help };
	private int[] widgetIcon = { R.drawable.my_device,
			R.drawable.firmware_upgrade, R.drawable.data_center,
			 R.drawable.sedentary_remind,R.drawable.water_intakes,R.drawable.day_pedometer,
			R.drawable.alarm_brain,/*R.drawable.message,*/ 
			R.drawable.software_update, R.drawable.help };
	private TextView enjoy_day;
	private ZyAccount mZyAccount;
	private int headIndex;
	private Bitmap bmp = null;
	private BluetoothAdapter btAdapt;
	private int connectState;
	private List<BluetoothDevice> bondList;		//Only Classic BT
	private HashMap<String, String> bleBondMap; //Only Ble BT
	private String preDeviceAddress;			//latest device address
	private boolean preDeviceType = false; 		//latest device Type, classic or ble
	private BluetoothDevice currentDevice = null;
	private String productName = "";
	private String nickname;
	private boolean isManager;
	private boolean isUpdate;	  
	private int battery = 0;
	public static Handler mHandler;
	public static final int BATTERY = 1;
	public static final int UPDATE_BATTERY = 2;
	public static final int MSG_UPDATE_START = 1000;
	public static final int MSG_UPDATE_VIEW = MSG_UPDATE_START + 1;
	public static final int MSG_UNREAD = 4;
	public MineFragment(){
	}
	
	public MineFragment(Context con){
		mContext = con;
	}
	
	private RequestAsyncTask tast;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.mine_fragment, container, false);
		btAdapt = BluetoothAdapter.getDefaultAdapter();
		mZyAccount = new ZyAccount(mContext, "1102927580", "1690816199");
		manager = PluginManager.getInstance();
		initViews();
		initBluetoothView();

		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case BATTERY:
					if (isAdded()) {
						int status = msg.arg1;
						battery = msg.arg2 - 0x20;
						if (battery == 0xff) {
							device_battery.setText(R.string.low_power_consumption);
							device_battery.setTextColor(0xffff002a);
						} else {
							if (status == 1) {
								device_battery.setText(R.string.charging);
								device_battery.setTextColor(0xffff002a);
								battery = -1;
							} else {
								if (status == 2)
									device_battery.setText(R.string.charging_complete);
								else {
									device_battery.setText(getResources().getString(R.string.remaining_battery) + battery + "%");
									if (battery > 20) {
										device_battery.setTextColor(0xff99a0a7);
									} else {
										device_battery.setTextColor(0xffff002a);
									}
								}
							}
						}
						progressCircle.setVisibility(View.GONE);
					}
					break;
				case UPDATE_BATTERY:
					battery = msg.arg1;
					device_battery.setText(getResources().getString(R.string.remaining_battery) + battery + "%");
					if (20 < battery && battery <= 100 ) {
						device_battery.setTextColor(0xff99a0a7);
					}else if(battery == 101) {
						device_battery.setText(R.string.connecting_battery);
					}else if(battery == 102) {
						device_battery.setText(R.string.connected_battery);
					}else {
						device_battery.setTextColor(0xffff002a);
					}
					progressCircle.setVisibility(View.GONE);
    				break;	
				case MSG_UPDATE_VIEW:
					// get hashmap form message
					HashMap<String, Object> map = (HashMap<String, Object>) msg.obj;
					if (map == null) {
						return;
					}
					isUpdate = true;
					widgetAdapter.notifyDataSetChanged();
					break;
				case MSG_UNREAD:
					widgetAdapter.notifyDataSetChanged();
    				break;
				}
			}
		};
		IntentFilter intent = new IntentFilter();
		intent.addAction("com.zhuoyou.running.connect.success");
		intent.addAction("com.zhuoyou.running.connect.failed");
		mContext.registerReceiver(mBTConnectReceiver, intent);
		return mRootView;				
	}
	private void initBluetoothView() {
		if (mBluetoothAdapter.isEnabled()) {// bluetooth is open

		} else {// bluetooth is close
			Toast.makeText(mContext, R.string.ensure_bluetooth_isenable, Toast.LENGTH_SHORT).show();
		}

	}
	private BroadcastReceiver mBTConnectReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("com.zhuoyou.running.connect.success")) {
				connectState = 3;
				Intent intent1 = new Intent("com.tyd.plugin.receiver.sendmsg");
				intent1.putExtra("plugin_cmd", 0x03);
				intent1.putExtra("plugin_content", "");
				mContext.sendBroadcast(intent1);
				updateView(Util.getConnectDevice(), R.string.getting_electricity);
				connect_state.setBackgroundResource(R.drawable.state_connect);
			} else if (action.equals("com.zhuoyou.running.connect.failed")) {
				connectState = 2;
				updateView(Util.getConnectDevice(), R.string.not_connected);
				connect_state.setBackgroundResource(R.drawable.state_disconnect);

			}
		}		
	};

	private void updateView(BluetoothDevice connectDevice, int state){
    	device_battery.setTextColor(0xff99a0a7);
    	device_battery.setCompoundDrawables(null, null, null, null);
		device_battery.setText(state);
		progressCircle.setVisibility(View.GONE);
		initWidgets();
	}

	private void initViews() {
		title_setting = (ImageView) mRootView.findViewById(R.id.title_setting);
		title_setting.setOnClickListener(this);
		user_layout = (LinearLayout) mRootView.findViewById(R.id.user_layout);
		user_layout.setOnClickListener(this);
		drawer_top_face = (ImageView) mRootView.findViewById(R.id.drawer_top_face);
		usrname = (TextView) mRootView.findViewById(R.id.usrname);
		signature = (TextView) mRootView.findViewById(R.id.signature);
		log_in = (Button) mRootView.findViewById(R.id.log_in);
		log_in.setOnClickListener(this);
		device_layout = (RelativeLayout) mRootView.findViewById(R.id.device_layout);
		device_layout.setOnClickListener(this);
		device_logo = (ImageView) mRootView.findViewById(R.id.device_logo);
		connected_device = (LinearLayout) mRootView.findViewById(R.id.connected_device);
		device_name = (TextView) mRootView.findViewById(R.id.device_name);
		device_battery = (TextView) mRootView.findViewById(R.id.device_battery);
		connect_state = (ImageView) mRootView.findViewById(R.id.connect_state);
		connect_device = (LinearLayout) mRootView.findViewById(R.id.connect_device);
		manager_layout = (LinearLayout) mRootView.findViewById(R.id.manager_layout);
		manager_layout.setOnClickListener(this);
		widget_listView = (ListView) mRootView.findViewById(R.id.widget_list);
		widgetAdapter = new WidgetAdapter(mContext, mWidgetItems);
		widget_listView.setAdapter(widgetAdapter);
		enjoy_day = (TextView) mRootView.findViewById(R.id.enjoy_day);
		progressCircle=(ProgressBar) mRootView.findViewById(R.id.progress_circle);
	}

    private void setListViewHeightBasedOnChildren(ListView listView) {  
        ListAdapter listAdapter = listView.getAdapter();   
        if (listAdapter == null) {  
            // pre-condition  
            return;  
        }  
  
        int totalHeight = 0;  
        for (int i = 0; i < listAdapter.getCount(); i++) {  
            View listItem = listAdapter.getView(i, null, listView);  
            listItem.measure(0, 0);  
            totalHeight += listItem.getMeasuredHeight();  
        }  
  
        ViewGroup.LayoutParams params = listView.getLayoutParams();  
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));  
        listView.setLayoutParams(params);  
    }  
	
	private void getEnjoyDays() {
		String today = Tools.getDate(0);
		String enter_day = HomePageFragment.mInstance.firstDay;
		if (enter_day.equals(""))
			enter_day = today;
		int count = Tools.getDayCount(enter_day, today, "yyyy-MM-dd");
		enjoy_day.setText(" " + count + " ");
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		
		updateUserInfo();
		
		updateMyDeviceInfo();
				
    	initWidgets();
		
		getEnjoyDays();
		
    	request();
	}
	
	private void updateUserInfo(){
		headIndex = Tools.getHead(mContext);
		if (headIndex == 10000) {
			bmp = Tools.convertFileToBitmap("/Running/download/custom");
			drawer_top_face.setImageBitmap(bmp);
		} else if (headIndex == 1000) {
			bmp = Tools.convertFileToBitmap("/Running/download/logo");
			drawer_top_face.setImageBitmap(bmp);
		} else {
			drawer_top_face.setImageResource(Tools.selectByIndex(headIndex));
		}
		
		if (Tools.getLogin(mContext)) {
			log_in.setVisibility(View.GONE);
		} else {
			log_in.setVisibility(View.VISIBLE);
		}

		if (Tools.getUsrName(mContext).equals("")) {
			if (!Tools.getLoginName(mContext).equals("")) {
				usrname.setText(Tools.getLoginName(mContext));
			} else {
				usrname.setText(R.string.username);
			}
		} else {
			usrname.setText(Tools.getUsrName(mContext));
		}
		
		if (!Tools.getSignature(mContext).equals("")) {
			signature.setText(Tools.getSignature(mContext));
		} else {
			signature.setText(R.string.no_signature);
		}
	}
	
	private void updateMyDeviceInfo(){
		//Get class bt had binded devices
    	bondList = Util.getBondDevice();
    	
    	//Get Ble had binded devices.
    	bleBondMap = Tools.getBleBindDevice(mContext);
    	//Get latest device type
    	preDeviceType = Util.getLatestDeviceType(mContext);
    	//Get latest device address
    	preDeviceAddress = Util.getLatestConnectDeviceAddress(mContext);
    	
    	Log.d("TAG","preDeviceType" + preDeviceType);
    	Log.d("TAG","preDeviceAddress" + preDeviceAddress);
    	
		if (preDeviceType) {//ble
			List<BluetoothDevice> gattConnectedDeviceList = BleManagerService.getInstance().getGattCurrentDevice();
			Log.d("TAG","gattConnectedDeviceList" + gattConnectedDeviceList);
			if(gattConnectedDeviceList == null){
				currentDevice = null;
			} else {
				if(!TextUtils.isEmpty(preDeviceAddress)){
					for(int i=0 ; i<gattConnectedDeviceList.size(); i++ ){
						if(preDeviceAddress.equals(gattConnectedDeviceList.get(i).getAddress())){
							currentDevice =  gattConnectedDeviceList.get(i);
						}
					}
				}
			}
			
			Log.d("TAG","currentDevice" + currentDevice);
			
	    	if (currentDevice != null) {
	    		connectState = 3;//已连接
	    		productName = Util.getProductName(Tools.keyString(bleBondMap, preDeviceAddress));
	    	} else {
				if (!TextUtils.isEmpty(preDeviceAddress)) {
					if (bleBondMap != null && bleBondMap.size() > 0) {
						if (bleBondMap.containsValue(preDeviceAddress)) {
							connectState = 2;// 已配对未连接

						} else {
							connectState = 1;// 未配对
						}
					} else {
						connectState = 1;// 未配对
					}
				} else {
					connectState = 1;// 未配对
				}
	    	}
	    	Log.d("TAG","connectState" + connectState);
	    	
	    	if (connectState == 2)// 已配对未连接
	    		productName = Util.getProductName(Tools.keyString(bleBondMap, preDeviceAddress));
	    		Log.d("TAG","productName2" + productName);
	    	if (connectState == 1){
	        	device_logo.setVisibility(View.GONE);
	        	connected_device.setVisibility(View.GONE);
	        	connect_state.setVisibility(View.GONE);
	        	manager_layout.setVisibility(View.GONE);
	        	connect_device.setVisibility(View.VISIBLE);
	        	isManager = false;
	    	} else {
				manager.processPlugList(productName);
				nickname = ProductManager.getInstance().getProductCategory(productName);
				device_logo.setVisibility(View.VISIBLE);
	        	device_logo.setImageResource(Util.getProductIcon(productName, true));
	        	connected_device.setVisibility(View.VISIBLE);
	        	connect_device.setVisibility(View.GONE);
	        	connect_state.setVisibility(View.VISIBLE);
	        	if (manager.getPlugBeans().size() > 0)
	        		manager_layout.setVisibility(View.VISIBLE);
	        	else
	        		manager_layout.setVisibility(View.INVISIBLE);
	        	isManager = true;
	        	device_name.setText(productName);
				device_battery.setTextColor(0xff99a0a7);
				if (connectState == 2) {
					device_battery.setText(R.string.not_connected);
					connect_state.setBackgroundResource(R.drawable.state_disconnect);
	
				} else {
//					Intent intent = new Intent(BleManagerService.ACTION_BATTERY_READ);
//					mContext.sendBroadcast(intent);
			    	Message msg = new Message();
					msg.what = UPDATE_BATTERY;
					msg.arg1 = Tools.getBatteryLevel();
					mHandler.sendMessage(msg);
					Log.d(TAG, "batteryValue="+battery);
					progressCircle.setVisibility(View.GONE);
					device_battery.setText(R.string.remaining_battery);
					connect_state.setBackgroundResource(R.drawable.state_connect);
				}
				
	        }
	    	
		} else {//classic
			currentDevice = BtProfileReceiver.getRemoteDevice();
	    	if (currentDevice != null) {
	    		connectState = 3;//已连接
	    	} else {
	    		if (bondList != null && bondList.size() > 0) {
	    			connectState = 2;//已配对未连接
	    			if (preDeviceAddress.equals(""))
	    				currentDevice = bondList.get(0);
	    			else
	    				currentDevice = btAdapt.getRemoteDevice(preDeviceAddress);
	    		} else {
	    			connectState = 1;//未配对
	    		}
	    	}
	    	
	    	if (currentDevice != null)
	    		productName = Util.getProductName(currentDevice.getName());
	    	if (connectState == 1){
	        	device_logo.setVisibility(View.GONE);
	        	connected_device.setVisibility(View.GONE);
	        	connect_state.setVisibility(View.GONE);
	        	manager_layout.setVisibility(View.GONE);
	        	connect_device.setVisibility(View.VISIBLE);
	        	isManager = false;
	    	} else {
				manager.processPlugList(productName);
				nickname = ProductManager.getInstance().getProductCategory(productName);
				device_logo.setVisibility(View.VISIBLE);
	        	device_logo.setImageResource(Util.getProductIcon(productName, true));
	        	connected_device.setVisibility(View.VISIBLE);
	        	connect_device.setVisibility(View.GONE);
	        	connect_state.setVisibility(View.VISIBLE);
	        	if (manager.getPlugBeans().size() > 0)
	        		manager_layout.setVisibility(View.VISIBLE);
	        	else
	        		manager_layout.setVisibility(View.INVISIBLE);
	        	isManager = true;
	        	device_name.setText(productName);
				device_battery.setTextColor(0xff99a0a7);
				if (connectState == 2) {
					device_battery.setText(R.string.not_connected);
					connect_state.setBackgroundResource(R.drawable.state_disconnect);
				} else {
					Intent intent1 = new Intent("com.tyd.plugin.receiver.sendmsg");
					intent1.putExtra("plugin_cmd", 0x03);
					intent1.putExtra("plugin_content", "");
					mContext.sendBroadcast(intent1);
					if (battery == 0){
						device_battery.setText(R.string.getting_electricity);
					} else if (battery == -1) {
						progressCircle.setVisibility(View.VISIBLE);
						device_battery.setText(R.string.charging);
						device_battery.setTextColor(0xffff002a);
					} else if (battery == 0xff) {
						progressCircle.setVisibility(View.VISIBLE);
						device_battery.setText(R.string.low_power_consumption);
						device_battery.setTextColor(0xffff002a);
					} else {
						progressCircle.setVisibility(View.VISIBLE);
						device_battery.setText(getResources().getString(R.string.remaining_battery) + battery + "%");
					}
					connect_state.setBackgroundResource(R.drawable.state_connect);
				}
	        }
		}
    	
    	
	}
	
	private void initWidgets() {
		mWidgetItems.clear();
		for (int i = 0; i < widgetIcon.length; i++) {
			WidgetItem item = new WidgetItem();
			if (i == 0) {
				if (isManager) {
					item.setIcon(widgetIcon[i]);
					item.setName(getResources().getString(widgetName[i]));
					mWidgetItems.add(item);
				}
			} else if (i == 1) {
				if (connectState == 3) {
					item.setIcon(widgetIcon[i]);
					item.setName(getResources().getString(widgetName[i]));
					mWidgetItems.add(item);
				}
			} else {
				item.setIcon(widgetIcon[i]);
				item.setName(getResources().getString(widgetName[i]));
				mWidgetItems.add(item);
			}
		}
		if (widgetAdapter == null) {
			widgetAdapter = new WidgetAdapter(mContext, mWidgetItems);
		}
		widgetAdapter.notify(mWidgetItems);
		setListViewHeightBasedOnChildren(widget_listView);
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.title_setting:
			intent.setClass(mContext, SettingActivity.class);
			startActivity(intent);
			break;
		case R.id.user_layout:
			intent.setClass(mContext, PersonalInformation.class);
			startActivityForResult(intent, 100);
			break;
		case R.id.log_in:
			mZyAccount.login(new IAccountListener() {
				@Override
				public void onCancel() {
					
				}
				@Override
				public void onSuccess(String userInfo) {
					Tools.saveInfoToSharePreference(mContext, userInfo);
					Tools.setLogin(mContext, true);
					if (Tools.getUsrName(mContext).equals("")) {
						usrname.setText(Tools.getLoginName(mContext));
					} else {
						usrname.setText(Tools.getUsrName(mContext));
					}
					Main.mHandler.sendEmptyMessage(Main.SELECT_FRAGMENT_HOME);
				}				
			});
			break;
		case R.id.device_layout:
			if (connectState == 1) {
				intent = new Intent(mContext, BindBleDeviceActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(intent);
			} else if (connectState == 2) {
				device_battery.setText(R.string.connecting);
				if(preDeviceType){
					if(productName != null){
						Tools.setConnectNotVibtation(false);
						intent = new Intent(BleManagerService.ACTION_CONNECT_BINDED_DEVICE);
						intent.putExtra("deviceName", productName);
						mContext.sendBroadcast(intent);
					}
				} else {
					Util.connect(currentDevice);
				}
			} else if (connectState == 3) {
				//进入插件界面
				if (manager.getPlugBeans().size() > 0) {
					intent = new Intent(mContext, BTPluginActivity.class);
					intent.putExtra("nick_name", nickname);
					intent.putExtra("remote_name", productName);
					intent.putExtra("enable_state", true);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(intent);
				}
			}
			break;
		case R.id.manager_layout:
			if (manager.getPlugBeans().size() > 0) {
				intent = new Intent(mContext, BTPluginActivity.class);
				intent.putExtra("nick_name", nickname);
				intent.putExtra("remote_name", productName);
				intent.putExtra("enable_state", true);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(intent);
			}
			break;
		}
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		super.onDestroy();
		mContext.unregisterReceiver(mBTConnectReceiver);
		if (bmp != null) {
			bmp.recycle();
			bmp = null;
			System.gc();
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 100) {
			if (resultCode == -1) {
				if (Tools.getUsrName(mContext).equals("")) {
					usrname.setText(Tools.getLoginName(mContext));
				} else {
					usrname.setText(Tools.getUsrName(mContext));
				}
				Main.mHandler.sendEmptyMessage(Main.SELECT_FRAGMENT_HOME);
			}
		}
	}
	
	public void request(){
		isUpdate = false;
		if(SelfUpdateMain.isDownloading == false){
			new RequestAsyncTask(mContext, mHandler, MSG_UPDATE_VIEW,  Constants.APPID, Constants.CHNID).startRun();
		}
	}
	
	private void widgetClick(int index) {
		String name = mWidgetItems.get(index).getName();
		Intent intent = new Intent();
		if (name.equals(getResources().getString(R.string.equip_manager))) {
			intent.setClass(mContext, EquipManagerListActivity.class);
			intent.putExtra("battery", device_battery.getText());
			startActivity(intent);
		} else if (name.equals(getResources().getString(R.string.firmware_upgrade))) {			
			Toast.makeText(mContext, R.string.isgoing_check_update, Toast.LENGTH_SHORT).show();
			Intent fwService = new Intent(mContext, FirmwareService.class);
			mContext.startService(fwService);
			//  zhongyang 20150422 add for : determine whether for BLE devices
			preDeviceType = Util.getLatestDeviceType(mContext);
			if(preDeviceType){
				Intent intent2 = new Intent(BleManagerService.ACTION_GET_FIRMWARE_VERSION);
				mContext.sendBroadcast(intent2);
			}else{
				Intent intent1 = new Intent("com.tyd.plugin.receiver.sendmsg");
				intent1.putExtra("plugin_cmd", 0x05);
				intent1.putExtra("plugin_content", "1");
				mContext.sendBroadcast(intent1);
			}
		} else if (name.equals(getResources().getString(R.string.data_center))) {
			intent.setClass(mContext, MotionDataActivity.class);
			startActivity(intent);
		} else if (name.equals(getResources().getString(R.string.sports_photo))) {
			intent.setClass(mContext, SportsAlbum.class);
			startActivity(intent);
		} else if (name.equals(getResources().getString(R.string.sedentary_remind))) {
			Toast.makeText(mContext, R.string.discover_hint, Toast.LENGTH_SHORT).show();
		} else if (name.equals(getResources().getString(R.string.message))) {
			intent.setClass(mContext, MessageActivity.class);
			startActivity(intent);
		} else if (name.equals(getResources().getString(R.string.software_updates))) {
			if ( NetUtils.getAPNType(mContext) == -1) {
				Toast.makeText(mContext, R.string.check_network, Toast.LENGTH_SHORT).show();
				return;
			}
			if(SelfUpdateMain.isDownloading == false){
				Toast.makeText(mContext, R.string.isgoing_check_update, Toast.LENGTH_SHORT).show();
				MyHandler h = new MyHandler(mContext, true);
				if (tast == null || tast.getStatus() == AsyncTask.Status.FINISHED) {
					tast = new RequestAsyncTask(mContext, h, MyHandler.MSG_UPDATE_VIEW, Constants.APPID, Constants.CHNID);
					tast.startRun();
				}
			}
		} else if (name.equals(getResources().getString(R.string.help))) {
			intent.setClass(mContext, HelpActivity.class);
			startActivity(intent);
		} else if (name.equals(getResources().getString(R.string.brain_alarm))){
			Intent alarmIntent = new Intent(mContext, AlarmMainActivity.class);
			startActivity(alarmIntent);
		}else if (name.equals(getResources().getString(R.string.water_intake))){
			Intent alarmIntent = new Intent(mContext, WaterIntakeActivity.class);
			startActivity(alarmIntent);
		}else if (name.equals(getResources().getString(R.string.day_pedometer))){
			Intent alarmIntent = new Intent(mContext, DayPedometerActivity.class);
			startActivity(alarmIntent);
		}
		
		
	}
	
	private class WidgetItem {
		private int icon;
		private String name;
		
		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setIcon(int icon) {
			this.icon = icon;
		}

		public int getIcon() {
			return icon;
		}
	}

	private class WidgetAdapter extends BaseAdapter {
		private Context mContext;
		private List<WidgetItem> mWidgetList = new ArrayList<WidgetItem>();
		private static final int MAX_H_NUM = 4;

		public WidgetAdapter(Context context, List<WidgetItem> list) {
			mContext = context;
			mWidgetList = list;
		}

		@Override
		public int getCount() {
			int size = mWidgetList.size();
			size = (size + MAX_H_NUM - 1) / MAX_H_NUM;
			return size;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View root = LayoutInflater.from(mContext).inflate(R.layout.widget_item, null);
			RelativeLayout[] roots = new RelativeLayout[MAX_H_NUM];
			ImageView[] icons = new ImageView[MAX_H_NUM];
			ImageView[] states = new ImageView[MAX_H_NUM];
			TextView[] names = new TextView[MAX_H_NUM];
			findViews(root, roots, icons, states, names);
			int max = MAX_H_NUM;
			if (getCount() == (position + 1)) {
				max = mWidgetList.size() % MAX_H_NUM;
				if (max == 0) {
					max = MAX_H_NUM;
				}
			}
			for (int i = 0; i < max; i++) {
				final int index = position * MAX_H_NUM + i;
				WidgetItem item = mWidgetList.get(index);
				icons[i].setImageResource(item.getIcon());
				names[i].setText(item.getName());				
				if (item.getName().equals(getResources().getString(R.string.software_updates))) {
					if (isUpdate) {
						states[i].setVisibility(View.VISIBLE);
						states[i].setImageResource(R.drawable.remind_circle);
					}
				} else if (item.getName().equals(getResources().getString(R.string.firmware_upgrade))) {
					if (Tools.getFirmwear()) {
						states[i].setVisibility(View.VISIBLE);
						states[i].setImageResource(R.drawable.remind_circle);
					}
				} else if (item.getName().equals(getResources().getString(R.string.message))) {
					if (Tools.getMsgState(mContext)) {
						states[i].setVisibility(View.VISIBLE);
						states[i].setImageResource(R.drawable.remind_circle);
					}
				}
				roots[i].setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						widgetClick(index);
					}
				});
			}
			return root;
		}
		
		private void findViews(View root, RelativeLayout[] roots, ImageView[] icons, ImageView[] states, TextView names[]) {
			int index = 0;

			roots[index] = (RelativeLayout) root.findViewById(R.id.gv_item_root1);
			icons[index] = (ImageView) root.findViewById(R.id.gv_item_icon1);
			states[index] = (ImageView) root.findViewById(R.id.gv_item_state1);
			states[index].setVisibility(View.GONE);
			names[index++] = (TextView) root.findViewById(R.id.gv_item_name1);

			roots[index] = (RelativeLayout) root.findViewById(R.id.gv_item_root2);
			icons[index] = (ImageView) root.findViewById(R.id.gv_item_icon2);
			states[index] = (ImageView) root.findViewById(R.id.gv_item_state2);
			states[index].setVisibility(View.GONE);
			names[index++] = (TextView) root.findViewById(R.id.gv_item_name2);

			roots[index] = (RelativeLayout) root.findViewById(R.id.gv_item_root3);
			icons[index] = (ImageView) root.findViewById(R.id.gv_item_icon3);
			states[index] = (ImageView) root.findViewById(R.id.gv_item_state3);
			states[index].setVisibility(View.GONE);
			names[index++] = (TextView) root.findViewById(R.id.gv_item_name3);

			roots[index] = (RelativeLayout) root.findViewById(R.id.gv_item_root4);
			icons[index] = (ImageView) root.findViewById(R.id.gv_item_icon4);
			states[index] = (ImageView) root.findViewById(R.id.gv_item_state4);
			states[index].setVisibility(View.GONE);
			names[index++] = (TextView) root.findViewById(R.id.gv_item_name4);
		}
		
		public void notify(List<WidgetItem> lists) {
			mWidgetList = lists;
			notifyDataSetChanged();
		}
	}
}
