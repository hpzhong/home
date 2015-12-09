package com.zhuoyou.plugin.info;

import android.content.Context;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.Tools;

public class HeadAdapter extends BaseAdapter {

	private Context mContext;
	private GridView mGridView;
	private int[] headIcon = Tools.headIcon;
	private int selectIndex = -1;
	
	//add zhaojunhui 20150625
	private ImageView myHead;
	private boolean isDefaults=false;

	public HeadAdapter(Context context, GridView view) {
		mContext = context;
		mGridView = view;
	}

	@Override
	public int getCount() {
		return headIcon.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	//add zhaojunhui 20150625
	@Override
	public void notifyDataSetChanged(){
		super.notifyDataSetChanged();
		isDefaults=true;
	}
		public static int headBySelect(int selectIndex) {
			int resId = R.drawable.logo_default;
			int length1 = Tools.headIcon1.length;
			int length2 = Tools.headIcon2.length;
			int length3 = Tools.headIcon3.length;
			int length4 = Tools.headIcon4.length;
			int headindex=-1;
			switch(selectIndex/100){
			case 0:
				if(selectIndex<length1){
					headindex=selectIndex;
				}
				break;
			case 1:
				headindex=selectIndex%100+length1;break;
			case 2:
				headindex=selectIndex%100+length1+length2;break;
			case 3:
				headindex=headindex=selectIndex%100+length1+length2+length3;break;
			case 4:
				headindex=headindex=selectIndex%100+length1+length2+length3+length4;break;
			
			}
			return headindex;
		}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewCache holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.choose_head_item, parent, false);
			holder = new ViewCache();
			holder.mIcon= (ImageView) convertView.findViewById(R.id.head_icon);
			holder.mSelect= (ImageView) convertView.findViewById(R.id.select);
			convertView.setTag(holder);
		} else {
			holder = (ViewCache) convertView.getTag();
		}
		final int mPosition = position;
		holder.mIcon.setImageResource(headIcon[position]);
		
		//modify zhaojunhui 20150624
		if(headBySelect(Tools.getHead(mContext))==position&&isDefaults==false){
			holder.mSelect.setVisibility(View.VISIBLE);
			myHead=holder.mSelect;
		}else{
			holder.mSelect.setVisibility(View.GONE);
		}
		
		holder.mSelect.setTag(position);
		holder.mIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isDefaults=false;
				ImageView preselectByTag = null, selectByTag = null;
				if (selectIndex != -1 && selectIndex != mPosition) {
					preselectByTag = (ImageView) mGridView.findViewWithTag(selectIndex);
				}else{
					preselectByTag=myHead;
				}
				selectByTag = (ImageView) mGridView.findViewWithTag(mPosition);
				if (preselectByTag != null)
					preselectByTag.setVisibility(View.GONE);
				if (selectByTag != null)
					selectByTag.setVisibility(View.VISIBLE);
				selectIndex = mPosition;
				if (ChooseHeadActivity.mHandler != null) {
					Message msg = new Message();
					msg.what = 1;
					msg.arg1 = selectIndex;
					ChooseHeadActivity.mHandler.sendMessage(msg);
				}
			}			
		});
		return convertView;
	}

	private class ViewCache
	{
		ImageView   mIcon;
		ImageView 	mSelect;        
	}

}
