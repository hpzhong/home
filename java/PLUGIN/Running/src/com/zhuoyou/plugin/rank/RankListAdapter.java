package com.zhuoyou.plugin.rank;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhuoyou.plugin.running.R;

public class RankListAdapter extends BaseAdapter {

	private Context mContext;
	private List<RankInfo> mList;

	public RankListAdapter(Context context, List<RankInfo> list) {
		mContext = context;		
		mList = list;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewCache holder ;
		if(convertView==null)      
		{  
			convertView = LayoutInflater.from(mContext).inflate(R.layout.rank_list_item, parent,false);
			holder = new ViewCache(); 			
			holder.mRank_bg = (ImageView)convertView.findViewById(R.id.rank_bg);				
			holder.mRank = (TextView)convertView.findViewById(R.id.rank);
			holder.mIcon = (ImageView)convertView.findViewById(R.id.icon);				
			holder.mName = (TextView)convertView.findViewById(R.id.name);
			holder.mStep  = (TextView)convertView.findViewById(R.id.step);
			convertView.setTag(holder);
		}  
		else
		{
			holder = (ViewCache)convertView.getTag();
		}
		if (position == 0) {
			holder.mRank_bg.setVisibility(View.VISIBLE);
			holder.mRank_bg.setBackgroundResource(R.drawable.rank_first);
			holder.mRank.setTextColor(0xffffffff);
			holder.mStep.setTextColor(0xffea2960);
		} else if (position == 1) {
			holder.mRank_bg.setVisibility(View.VISIBLE);
			holder.mRank_bg.setBackgroundResource(R.drawable.rank_second);
			holder.mRank.setTextColor(0xffffffff);
			holder.mStep.setTextColor(0xff953fd2);
		} else if (position == 2) {
			holder.mRank_bg.setVisibility(View.VISIBLE);
			holder.mRank_bg.setBackgroundResource(R.drawable.rank_third);
			holder.mRank.setTextColor(0xffffffff);
			holder.mStep.setTextColor(0xff2f7fe0);
		} else {
			holder.mRank_bg.setVisibility(View.GONE);
			holder.mRank.setTextColor(0xff393939);
			holder.mStep.setTextColor(0xff00c5c7);
		}
		holder.mRank.setText(mList.get(position).getRank() + "");
		holder.mIcon.setImageDrawable(mList.get(position).getImg());
		holder.mName.setText(mList.get(position).getName());
		holder.mStep.setText(mList.get(position).getmSteps());
		return convertView;
	}
	
	static class ViewCache
	{
		ImageView   mRank_bg;
		TextView 	mRank;  
		ImageView 	mIcon;        
		TextView 	mName;  
		TextView 	mStep;
	}
	
}
