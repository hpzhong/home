package com.zhuoyou.plugin.running;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SleepPageListItemAdapter extends BaseAdapter {
	private List<SleepItem> mTodayLists;
	private Context mContext;
	private LayoutInflater inflater;
	
	public SleepPageListItemAdapter(Context ctx, List<SleepItem> list) {
		mContext = ctx;
		mTodayLists = list;
	}

	public void UpdateDate(Context ctx, List<SleepItem> list){
		mContext = ctx;
		mTodayLists = list;
	}
	
	@Override
	public int getCount() {
		return mTodayLists.size();
	}

	@Override
	public SleepItem getItem(int position) {
		// TODO Auto-generated method stub
		return mTodayLists.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewCache holder ;
		Drawable drawable = null;
		if(convertView==null) {  
			convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_sleep, parent, false);
			holder = new ViewCache();
			holder.mTime = (TextView) convertView.findViewById(R.id.time);
			holder.mSleepT = (TextView) convertView.findViewById(R.id.sleepTimeV);
			holder.mDSleepT = (TextView) convertView.findViewById(R.id.dSleepTimeV);
			holder.mWSleepT = (TextView) convertView.findViewById(R.id.sSleepTimeV);
			holder.sleepLay = (RelativeLayout) convertView.findViewById(R.id.sleep_lay);
			convertView.setTag(holder);
		} else {
			holder = (ViewCache)convertView.getTag();
		}
		holder.mTime.setText(mTodayLists.get(position).getmStartT() + " -" + mTodayLists.get(position).getmEndT());
		holder.mSleepT.setText(Tools.getTimer(mTodayLists.get(position).getmSleepT()));
		holder.mDSleepT.setText(Tools.getTimer(mTodayLists.get(position).getmDSleepT()));
		holder.mWSleepT.setText(Tools.getTimer(mTodayLists.get(position).getmWSleepT()));
		
		if (position == 0) {
			if ((mTodayLists.size() - 1) == position) {
				holder.sleepLay.setBackgroundResource(R.drawable.listitem_sleep_bg_1);
			} else {
				holder.sleepLay.setBackgroundResource(R.drawable.listitem_sleep_bg);
			}
		} else {
			if ((mTodayLists.size() - 1) == position) {
				holder.sleepLay.setBackgroundResource(R.drawable.listitem_sleep_bg_3);
			} else {
				holder.sleepLay.setBackgroundResource(R.drawable.listitem_sleep_bg_2);
			}
		}
		return convertView;
	}
	
	class ViewCache {
		RelativeLayout sleepLay;
		TextView mTime;
		TextView mSleepT;
		TextView mDSleepT;
		TextView mWSleepT;
	}

}
