package com.zhuoyou.plugin.resideMenu;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyou.plugin.ble.BindBleDeviceActivity;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;

public class AddEquipActivity extends Activity implements OnClickListener {

	private ListView mListView;
	private String[] deviceName = {"EAMEY P1", "EAMEY P3","Primo 1", "Primo 3", "TJ01", "Meegoo A10", "Megoo2", "Unik 1","Unik 2","Unik 3","LEO","LUNA3","S3"};
	private int[] deviceIcon = {R.drawable.p1_connect, R.drawable.p3_connect,R.drawable.p1_connect, R.drawable.p3_connect, R.drawable.t1_connect, R.drawable.a1_connect, R.drawable.m2_connect, R.drawable.luna1_connect,R.drawable.leo_connect,R.drawable.luna3_connect,R.drawable.leo_connect,R.drawable.luna3_connect,R.drawable.s3_connect};
	private int[] deviceDescription = {R.string.device_1, R.string.device_2,R.string.device_1, R.string.device_2, R.string.device_3, R.string.device_4, R.string.device_2,  R.string.device_1 ,R.string.device_3,R.string.device_1,R.string.device_3,R.string.device_1,R.string.device_1};
	private int[] deviceBle = {0, 0, 0, 0, 0, 0, 0 , 1 , 1,0,1,0,0};
	private List<DeviceItem> mDeviceItems = new ArrayList<DeviceItem>();
	private DeviceAdapter mAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.add_equip_layout);
		initView();
	}

	private void initView() {
		for (int i = 0; i < deviceName.length; i++) {
			DeviceItem item = new DeviceItem();
			item.setName(deviceName[i]);
			item.setIcon(deviceIcon[i]);
			item.setDes(getResources().getString(deviceDescription[i]));
			item.setBle(deviceBle[i]);
			mDeviceItems.add(item);
		}
		mListView = (ListView) findViewById(R.id.device_list);
		mAdapter = new DeviceAdapter(this, mDeviceItems);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(listViewItemClick);
		
		
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.back:
			finish();
			break;
		}
	}
	
	OnItemClickListener listViewItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			DeviceItem item = mDeviceItems.get(position);
//			if(TextUtils.isEmpty(RunningApp.getCurrentConnectDeviceType())) {
//				
//			}else if(!TextUtils.isEmpty(RunningApp.getCurrentConnectDeviceType())) {
//				
//			}else if(!TextUtils.isEmpty(RunningApp.getCurrentConnectDeviceType())) {
//				
//			}
			if (item.getBle() == 1) {
				if (!RunningApp.isBLESupport) {
					Toast.makeText(getApplicationContext(), R.string.not_support_ble, Toast.LENGTH_SHORT).show();
				} else {
					Intent intent = new Intent(AddEquipActivity.this,BindBleDeviceActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("BLE_DEVICE_NAME", item.getName());
					intent.putExtras(bundle);
					startActivity(intent);
				}
			} else {
				Intent intent = new Intent("android.settings.BLUETOOTH_SETTINGS");
				startActivity(intent);
			}
		}		
	};
	private class DeviceItem {

		private String name;
		private int icon;
		private String description;
		private int ble;
		
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
		
		public void setDes(String description) {
			this.description = description;
		}

		public String getDes() {
			return description;
		}
		
		public void setBle(int ble) {
			this.ble = ble;
		}

		public int getBle() {
			return ble;
		}
	}
	
	private class DeviceAdapter extends BaseAdapter {

		private Context mContext;
		private List<DeviceItem> mDeviceList = new ArrayList<DeviceItem>();

		public DeviceAdapter(Context context, List<DeviceItem> list) {
			mContext = context;
			mDeviceList = list;
		}

		@Override
		public int getCount() {
			return mDeviceList.size();
		}

		@Override
		public Object getItem(int position) {
			return mDeviceList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewCache holder;

			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.add_device_item, null);
				holder = new ViewCache();
				holder.device_icon = (ImageView) convertView.findViewById(R.id.device_type);
				holder.device_name = (TextView) convertView.findViewById(R.id.device_name);
				holder.device_des = (TextView) convertView.findViewById(R.id.connect_state);
				convertView.setTag(holder);
			} else {
				holder = (ViewCache) convertView.getTag();
			}
			DeviceItem item = mDeviceList.get(position);
			holder.device_icon.setImageResource(item.getIcon());
			holder.device_name.setText(item.getName());
			holder.device_des.setText(item.getDes());
			if (item.getBle() == 1) {
				if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//					holder.device_name.setTextColor(0xffff0000);
//					holder.device_des.setTextColor(0xffff0000);
				}
			}
			return convertView;
		}
		
		private class ViewCache {
			private ImageView device_icon;
			private TextView device_name;
			private TextView device_des;
		}

	}
}
