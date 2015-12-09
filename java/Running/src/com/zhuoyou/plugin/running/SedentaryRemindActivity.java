package com.zhuoyou.plugin.running;

import java.util.ArrayList;
import java.util.List;

import com.mcube.lib.ped.PedBackgroundService;
import com.zhuoyou.plugin.ble.BindBleDeviceActivity;
import com.zhuoyou.plugin.ble.BleDeviceInfo;
import com.zhuoyou.plugin.bluetooth.connection.BtProfileReceiver;
import com.zhuoyou.plugin.bluetooth.data.Util;
import com.zhuoyou.plugin.bluetooth.product.ProductManager;
import com.zhuoyou.plugin.gps.ilistener.IStepListener;
import com.zhuoyou.plugin.gps.ilistener.StepWatcher;
import com.zhuoyou.plugin.running.HomePageFragment.StepObserver;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SedentaryRemindActivity extends Activity {
	private int mNumDevs = 0;
	private ListView mListView;
	private SedentaryListAdapter mAdapter;
	private TextView tv_title;
	private RelativeLayout im_back;
	private static final int  REQUEST=1;
	
	private String[] mDeviceFilter = {"Unik 3","Unik 2"};
	private List<BluetoothDevice> bondList;	
	BluetoothDevice  currentDevice ;
	String preDeviceAddress;
	private BluetoothAdapter bluetoothAdapt; 
	
	private StepObserver mStepObserver;	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sedentary_remind_activity);
		tv_title = (TextView) findViewById(R.id.title);
		tv_title.setText(R.string.sedentary_remind);
		im_back = (RelativeLayout) findViewById(R.id.back);
		im_back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		
		mAdapter=new SedentaryListAdapter();
		initData();
		mListView=(ListView)findViewById(R.id.sedentary_device_list);
		mListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent=new Intent(SedentaryRemindActivity.this,SedentaryTimeSetActivity.class);
				Bundle bundle=new Bundle();
				bundle.putSerializable("device_info", mAdapter.Devices.get(position));
				intent.putExtras(bundle);
				startActivityForResult(intent, REQUEST);
			}
			
		});
		
		mListView.setAdapter(mAdapter);
		
		mStepObserver=new StepObserver();
		StepWatcher.getInstance().addWatcher(mStepObserver);
	}
	private void initData() {
		// TODO Auto-generated method stub
		
		SedentaryDeviceItem Phone=new SedentaryDeviceItem("Phone","08:00","22:00",3,Tools.getPhoneSedentaryState());
		mAdapter.addDevice(Phone);
		
		bluetoothAdapt= BluetoothAdapter.getDefaultAdapter();
		preDeviceAddress = Util.getLatestConnectDeviceAddress(getApplicationContext());
		bondList = Util.getBondDevice();
		currentDevice = BtProfileReceiver.getRemoteDevice();//已连接的设备--当前设备为连接的设备
    	if (currentDevice != null) {
//    		connectState = 3;//已连接
    		
    	} else {
    		if (bondList != null && bondList.size() > 0) {
//    			connectState = 2;//已配对未连接
    			if (preDeviceAddress.equals(""))
    				currentDevice = bondList.get(0);//当前设备为未连接的第一个
    			else
    				currentDevice = bluetoothAdapt.getRemoteDevice(preDeviceAddress);
    		} else {
//    			connectState = 1;//未配对
    		}
    	}
    	
    	if (currentDevice != null){
    		addDevice(currentDevice);
    		Toast.makeText(this,  Util.getProductName(currentDevice.getName()), 1).show();
    	}else
    		Toast.makeText(this, "null", 1).show();
	
	}
       public void  addDevice(BluetoothDevice device){
    	   boolean found=false;
    		for (int i = 0; i < mDeviceFilter.length && !found	&& !TextUtils.isEmpty(device.getName()); i++) {
    			found = currentDevice.getName().equals(mDeviceFilter[i]);	
        }
    		if(found){
    			SedentaryDeviceItem deviceItem=new SedentaryDeviceItem();
    			deviceItem.setDeviceName(Util.getProductName(device.getName()));
    			deviceItem.setEndTime("22:00");
    			deviceItem.setStartTime("8:00");
    			deviceItem.setState(false);
    			deviceItem.setTimeLag(1);
    			mAdapter.addDevice(deviceItem);
    			mAdapter.notifyDataSetChanged();
    		}
    		
       }


	
	
	
	public class ViewHolder{
		ImageView DeviceImg;
		TextView DeviceNameTextView;
		TextView DeviceTimeSet;
		ImageView enable;
	}
	public class SedentaryListAdapter extends BaseAdapter{
		private ArrayList<SedentaryDeviceItem> Devices;
		private LayoutInflater mInflator;

		public SedentaryListAdapter() {
			super();
			Devices = new ArrayList<SedentaryDeviceItem>();
			mInflator = SedentaryRemindActivity.this.getLayoutInflater();
		}

		public void addDevice(SedentaryDeviceItem device) {
			if (!Devices.contains(device)) {
				mNumDevs++;
			Devices.add(device);
			}
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return Devices.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return Devices.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder viewholeder;
			if(convertView==null){
				convertView=mInflator.inflate(R.layout.sedentary_remind_item, null);
				viewholeder=new ViewHolder();
				
				viewholeder.DeviceImg=(ImageView) convertView.findViewById(R.id.divice_img);
				viewholeder.DeviceNameTextView=(TextView) convertView.findViewById(R.id.sedentary_device_name);
				viewholeder.DeviceTimeSet=(TextView) convertView.findViewById(R.id.device_time_set);
				viewholeder.enable=(ImageView) convertView.findViewById(R.id.divice_enable_img);
				convertView.setTag(viewholeder);
			}else{
				viewholeder=(ViewHolder) convertView.getTag();
			}
			if(position==0){
				viewholeder.DeviceImg.setBackgroundResource(R.drawable.leo_connect);
			}else{
				viewholeder.DeviceImg.setBackgroundResource(Util.getProductIcon(Devices.get(position).getDeviceName(), true));
			}
		
			
			viewholeder.DeviceNameTextView.setText(Devices.get(position).getDeviceName());
			viewholeder.DeviceTimeSet.setText(Devices.get(position).getTimeLag()*30+"分钟/"+
					Devices.get(position).getStartTime()+"-"+Devices.get(position).getEndTime());
			boolean is_enable=Devices.get(position).getState();
			viewholeder.enable.setBackgroundResource(is_enable?R.drawable.warn_on:R.drawable.warn_off);
			
			viewholeder.enable.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					boolean item_state=Devices.get(position).getState();
					Devices.get(position).setState(!item_state);
					mAdapter.notifyDataSetChanged();	
					if(position==0){
						if(!Tools.getPhonePedState()&&!item_state){
							//open
							Intent phoneStepsIntent = new Intent(getApplicationContext(),
									PedBackgroundService.class);
							startService(phoneStepsIntent);
							Tools.setPhoneSedentaryState(true);
							Toast.makeText(SedentaryRemindActivity.this, "open", 1).show();
						}else if(!Tools.getPhonePedState()&&item_state){
							//close
							Intent phoneStepsIntent = new Intent(getApplicationContext(),
									PedBackgroundService.class);
							stopService(phoneStepsIntent);
							Tools.setPhoneSedentaryState(false);

							Toast.makeText(SedentaryRemindActivity.this, "close", 1).show();
						}
						
					}

					
				}
				
			});
			return convertView;
		}
		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==REQUEST){
			
		}
	}
	
	
	/** 运动步数信息监听 */
	class StepObserver implements IStepListener{
		
		@Override
		public void onStepCount(int stepCount) {
			/*int size = mRunningDays.size() - 1;
			if (mViewPager.getCurrentItem() == size) {								
				RunningItem current = null;
				try {
					current = (RunningItem)item.clone();
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.i("222", item.getSteps() + "qqqqqqqqqq" + PHONE_STEP + "qqqqqqqqqq" + stepCount);
				current.setSteps(item.getSteps() - PHONE_STEP + stepCount);
				mRunningDays.set(size, current);
				mHomeAdapter.notifyDataSetChanged(mRunningDays, weight, steps);
			}*/
		}
		
		@Override
		public void onStateChanged(int newState) {
			
		}

		@SuppressWarnings("static-access")
		@Override
		public void onHadRunStep(int hadRunStep) {
		}
	}
	

}
