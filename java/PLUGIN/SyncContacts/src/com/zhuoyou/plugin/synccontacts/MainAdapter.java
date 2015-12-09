package com.zhuoyou.plugin.synccontacts;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MainAdapter extends ArrayAdapter<ContactItem> {
	private Context mCtx;
	private List<ContactItem> mListItems;
	
	public MainAdapter(Context context, int resource, List<ContactItem> objects) {
		super(context, resource, objects);
		mCtx = context;
		mListItems = objects;
	}
	@Override
	public int getCount() {
		return mListItems.size();
	}

	@Override
	public ContactItem getItem(int position) {
		return mListItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * TODO checkbox in holder will casue some bugs .fixed in feature
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ContactHolder holder;
		
		convertView = View.inflate(mCtx, R.layout.item, null);
		holder = new ContactHolder();
		holder.mName = (TextView) convertView.findViewById(R.id.item_name);
		holder.mNumber = (TextView) convertView.findViewById(R.id.item_number);
		convertView.setTag(holder);

		ContactItem item = mListItems.get(position);
		holder.mName.setText(item.getName());
		holder.mNumber.setText(item.getNumber());

		return convertView;
	}
	
	public void notifyDataSetChanged(List<ContactItem> objects){
		mListItems	= objects;
		super.notifyDataSetChanged();
	}
	
	private class ContactHolder {
		public TextView mName;
		public TextView mNumber;
	}
}
