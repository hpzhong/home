package com.zhuoyou.plugin.antilost;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ListAdapter extends BaseAdapter {

	private Context mContext;
	private String[] mList;
	private ListView mListView;
	private String musicName = "";
	private int temp = -1;

	public ListAdapter(Context context, String[] list, ListView listView) {
		mContext = context;
		mList = list;
		mListView = listView;
		musicName = PlugTools.getDataString(mContext , "music");
		Log.i("gchk", "read inner" + musicName);
		if (musicName!=null  && musicName.trim().length() > 0) {
			for (int i = 0; i < mList.length; i++) {
				if (mList[i].equals(musicName)) {
					temp = i;
					break;
				}
			}
		} else {
			temp = -1;
		}
	}

	public void setMyList(String[] list) {
		temp = -1;
		this.notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return mList.length;
	}

	@Override
	public Object getItem(int position) {
		return mList == null ? null : mList[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View root = LayoutInflater.from(mContext).inflate(R.layout.list_item_layout, null);
		TextView name = (TextView) root.findViewById(R.id.item_name);
		ImageView icon = (ImageView) root.findViewById(R.id.radio);
		final int mPosition = position;
		name.setText(mList[position]);
		icon.setTag(mList[position]);
		name.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mPosition == temp)
					return;
				if (temp != -1) {
					ImageView preImage = (ImageView) mListView.findViewWithTag(mList[temp]);
					if (preImage != null) {
						preImage.setBackgroundResource(R.drawable.setlist_radio_off);
					}
				}else {
					Message msg = new Message();
					msg.what = Main.SELECT_MUSIC_IN_SD;
					Main.mHandler.sendMessage(msg);
				}
				ImageView tempImage = (ImageView) mListView.findViewWithTag(mList[mPosition]);
				if (tempImage != null) {
					tempImage.setBackgroundResource(R.drawable.setlist_radio_on);
				}
				temp = mPosition;
				// store data to data/data/files
				if (PlugTools.saveDataString(mContext, "music" , mList[mPosition])) {
					Log.i("gchk", "write inner success");
				} else {
					Log.i("gchk", "write inner failed");
				}
			}
		});
		icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mPosition == temp)
					return;
				if (temp != -1) {
					ImageView preImage = (ImageView) mListView.findViewWithTag(mList[temp]);
					if (preImage != null) {
						preImage.setBackgroundResource(R.drawable.setlist_radio_off);
					}
				}else {
					Message msg = new Message();
					msg.what = Main.SELECT_MUSIC_IN_SD;
					Main.mHandler.sendMessage(msg);
				}
				ImageView tempImage = (ImageView) mListView.findViewWithTag(mList[mPosition]);
				if (tempImage != null) {
					tempImage.setBackgroundResource(R.drawable.setlist_radio_on);
				}
				temp = mPosition;
				// store data to data/data/files
				if (PlugTools.saveDataString(mContext, "music" , mList[mPosition])) {
					Log.i("gchk", "write inner success");
				} else {
					Log.i("gchk", "write inner failed");
				}
			}
		});
		if (position == temp)
			icon.setBackgroundResource(R.drawable.setlist_radio_on);
		return root;
	}

}
