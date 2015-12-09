package com.zhuoyou.plugin.resideMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.bluetooth.attach.PluginManager;
import com.zhuoyou.plugin.bluetooth.data.Util;
import com.zhuoyou.plugin.running.R;

public class DeviceListAdapter extends BaseAdapter {

	private Context mContext;
	private List<Map<String, Object>> mDeviceList = new ArrayList<Map<String, Object>>();

	public DeviceListAdapter(Context context, List<Map<String, Object>> list) {
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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.device_list_item, null);
			holder = new ViewCache();
			holder.device_type = (ImageView) convertView.findViewById(R.id.device_type);
			holder.device_name = (TextView) convertView.findViewById(R.id.device_name);
			holder.connect_state = (TextView) convertView.findViewById(R.id.connect_state);
			holder.remove_lay = (RelativeLayout) convertView.findViewById(R.id.remove_lay);
			holder.details_lay = (RelativeLayout) convertView.findViewById(R.id.details_lay);
			convertView.setTag(holder);
		} else {
			holder = (ViewCache) convertView.getTag();
		}
		
		final Map<String, Object> item = mDeviceList.get(position);
		final int mPosition = position;
		holder.device_type.setImageResource((Integer)item.get("icon"));
		holder.device_name.setText(item.get("name").toString());
		holder.connect_state.setText(item.get("connect").toString());	
		PluginManager manager = PluginManager.getInstance();
		String productName = Util.getProductName(item.get("name").toString());
		manager.processPlugList(productName);
		if (EquipManagerListActivity.isEditMode) {
			holder.details_lay.setVisibility(View.GONE);
			holder.remove_lay.setVisibility(View.VISIBLE);
			holder.remove_lay.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Message msg = new Message();
					msg.what = EquipManagerListActivity.REMOVE_DEVICE_ITEM;
					msg.arg1 = mPosition;
					msg.obj = item;
					EquipManagerListActivity.mHandler.sendMessage(msg);
				}
			
			});
		} else {
			if (manager.getPlugBeans().size() > 0)
				holder.details_lay.setVisibility(View.VISIBLE);
			holder.remove_lay.setVisibility(View.GONE);
			holder.details_lay.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Message msg = new Message();
					msg.what = EquipManagerListActivity.ENTER_PLUGIN_DETAIL;
					msg.arg1 = mPosition;
					msg.obj = item;
					EquipManagerListActivity.mHandler.sendMessage(msg);
				}
			
			});
		}
		
		return convertView;
	}

	private class ViewCache {
		public ImageView device_type;
		public TextView device_name;
		public TextView connect_state;
		public RelativeLayout remove_lay;
		public RelativeLayout details_lay;
	}
}
