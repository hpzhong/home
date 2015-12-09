package com.zhuoyou.plugin.view;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.bluetooth.data.Util;
import com.zhuoyou.plugin.running.R;

public class SearchListDialog extends Dialog implements android.view.View.OnClickListener{

	private Context context;
	private List<BluetoothDevice> mList = new ArrayList<BluetoothDevice>();
	public ListView mListView = null;
	private TextView bTSettings;
	private List<String> mDeviceNames;
	public ArrayAdapter<String> adtDevices;
	private LeaveMyDialogListener listener;
	public RelativeLayout search_lay;
	
	public List<BluetoothDevice> getmList() {
		return mList;
	}

	public void setmList(List<BluetoothDevice> mList) {
		this.mList = mList;
	}

	public SearchListDialog(Context context) {
		super(context);
		this.context = context;
	}

	public SearchListDialog(Context context, int theme) {
		super(context, theme);
		this.context = context;
	}

	public SearchListDialog(Context context, int theme, List<BluetoothDevice> list, LeaveMyDialogListener listener) {
		super(context, theme);
		this.context = context;
		this.mList = list;
		this.listener = listener;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		View root = View.inflate(context, R.layout.devices_dialog_layout, null);
		this.setContentView(root);
		search_lay = (RelativeLayout) root.findViewById(R.id.search_lay);
		
		bTSettings = (TextView) root.findViewById(R.id.bluetooth_settings);
		bTSettings.setOnClickListener(this);
		
		mListView = (ListView) root.findViewById(R.id.device_list);
		updateProductView(context, mList);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Util.bondDevice(mList.get(arg2));
				dismiss();
			}
			
		});
	}
	
	public void updateProductView(Context con,List<BluetoothDevice> list) {
		mDeviceNames = new ArrayList<String>();
		if (list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				StringBuilder sb  = new StringBuilder();
				String name = list.get(i).getName();
				String address = list.get(i).getAddress();
				sb.append(name);
				sb.append("  ");
				sb.append(address);
				mDeviceNames.add(sb.toString());
			}
		}
		if (mListView != null) {
			adtDevices = new ArrayAdapter<String>(con, R.layout.simple_list_item, mDeviceNames);
			mListView.setAdapter(adtDevices);
		}
	}
	
	public interface LeaveMyDialogListener{   
        public void onClick(View view);   
    }   

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		listener.onClick(v);
	}
	
	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		super.dismiss();
	}

}
