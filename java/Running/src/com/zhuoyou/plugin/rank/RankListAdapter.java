package com.zhuoyou.plugin.rank;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zhuoyou.plugin.rank.AsyncImageLoader.ImageCallback;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;

public class RankListAdapter extends BaseAdapter {

	private Context mContext;
	private List<RankInfo> mList;
	private Typeface mNumberTP;
	//private Typeface mTextTP;
	private ListView mListView;
	private AsyncImageLoader mAsyncImageLoader;

	public RankListAdapter(Context context) {
		mContext = context;
		//mTextTP = RunningApp.getCustomChineseFont();
		mNumberTP = RunningApp.getCustomNumberFont();
		mAsyncImageLoader = new AsyncImageLoader();
	}

	@Override
	public int getCount() {
		if(mList !=null)
			return mList.size();
		return 0;
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
		Drawable drawable = null;
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
		
		mListView = (ListView) parent;
		holder.mRank.setTypeface(mNumberTP);
		//holder.mName.setTypeface(mTextTP, Typeface.BOLD);
		holder.mStep.setTypeface(mNumberTP);
		
		if (position == 0) {
			holder.mRank_bg.setVisibility(View.VISIBLE);
			holder.mRank_bg.setBackgroundResource(R.drawable.rank_first);
			holder.mRank.setTextSize(38f);
			holder.mStep.setTextSize(38f);
			holder.mRank.setTextColor(0xffee3b18);
			holder.mStep.setTextColor(0xffee3b18);
		} else if (position == 1) {
			holder.mRank_bg.setVisibility(View.VISIBLE);
			//holder.mRank_bg.setBackgroundResource(R.drawable.rank_second);
			holder.mRank.setTextSize(33f);
			holder.mStep.setTextSize(33f);
			holder.mRank.setTextColor(0xfff69126);
			holder.mStep.setTextColor(0xfff69126);
		} else if (position == 2) {
			holder.mRank_bg.setVisibility(View.VISIBLE);
			//holder.mRank_bg.setBackgroundResource(R.drawable.rank_third);
			holder.mRank.setTextSize(28f);
			holder.mStep.setTextSize(28f);
			holder.mRank.setTextColor(0xffe2c922);
			holder.mStep.setTextColor(0xffe2c922);
		} else {
			holder.mRank_bg.setVisibility(View.GONE);
			holder.mRank.setTextSize(23f);
			holder.mStep.setTextSize(23f);
			holder.mRank.setTextColor(0xff97918f);
			holder.mStep.setTextColor(0xff97918f);
		}
		holder.mRank.setText(mList.get(position).getRank() + "");
		int headId = mList.get(position).getmImgId();
		String headUrl = mList.get(position).getHeadUrl();
		if (headId == 1000 || headId == 10000) {
			holder.mIcon.setTag(headUrl);
			drawable = mAsyncImageLoader.loadDrawable(headUrl, new ImageCallback() {
				@Override
				public void imageLoaded(Drawable imageDrawable, String imageUrl) {
					ImageView imageViewByTag = (ImageView) mListView.findViewWithTag(imageUrl);  
					if (imageViewByTag != null) {
						if (imageDrawable != null)
							imageViewByTag.setImageDrawable(imageDrawable);
						else
							imageViewByTag.setImageResource(R.drawable.logo_default);
					}
				}				
			});
			if (drawable == null) {
				holder.mIcon.setImageResource(R.drawable.logo_default);
			} else {
				holder.mIcon.setImageDrawable(drawable);
			}
		} else {
			holder.mIcon.setImageResource(Tools.selectByIndex(headId));
		}
		holder.mName.setText(mList.get(position).getName());
		holder.mStep.setText(mList.get(position).getmSteps());
		if(mList.get(position).getmSteps().equals("0")){
			convertView.setVisibility(View.GONE);
		}else{
			convertView.setVisibility(View.VISIBLE);
		}
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
	
	public void refreshListInfo(List<RankInfo> list) {
		mList = list;
		super.notifyDataSetChanged();
	}
}
