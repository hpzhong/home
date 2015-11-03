package com.zhuoyou.plugin.info;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhuoyou.plugin.add.TosGallery;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.Tools;

public class WheelTextAdapter extends BaseAdapter{
	
	Context mcontext;
	String[] mData = {};
	int mHeight = 38;
	int selection =0;
	int size;
	String UNIT = "";
	int target=1;
	public WheelTextAdapter(Context context,String[] String,int sizes){
		mcontext = context;
		mData = String;
		mHeight = (int) Tools.dip2px(mcontext, mHeight);
		size=sizes;
		
	}
	public WheelTextAdapter(Context context,String[] String,String munit,int sizes) {
		mcontext = context;
		mData = String;
		mHeight = (int) Tools.dip2px(mcontext, mHeight);
		UNIT = munit;
		size=sizes;
	}
	@Override
	public int getCount() {
        return (null != mData) ? mData.length : 0;
	}

	@Override
	public Object getItem(int arg0) {
			return mData[arg0];				
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void  SetSelecttion(int position){
		selection = position;
	}	
	
	public void setTarget(int targets){
		target=targets;
	}
	
	public void setData(String[] data){
		this.mData = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView txtView = null;
		LayoutInflater mInflator;
		if (null == convertView) {
			mInflator = ((Activity) mcontext).getLayoutInflater();
			convertView = new TextView(mcontext);
			convertView.setLayoutParams(new TosGallery.LayoutParams(-1,
					mHeight));
			txtView = (TextView) convertView;
			txtView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 23);
			txtView.setTextColor(Color.BLACK);
			txtView.setGravity(Gravity.CENTER);
		}
       if (null == txtView) {
		txtView = (TextView) convertView;
		
       }
        String text= mData[position];
        if(text.length()>5){
        	text=text.substring(0,5);
        }
        txtView.setText(text);
        if(position == selection){
			txtView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
		}else if(position==selection-1 || position==selection+1){
			txtView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size-2);
			txtView.setTextColor(mcontext.getResources().getColor(R.color.list_view_item1));
		}else if(position==selection-2 || position==selection+2){
			txtView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size-4);
			txtView.setTextColor(mcontext.getResources().getColor(R.color.list_view_item2));
		}else if(position==selection-3 || position==selection+3){
			txtView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size-6);
			txtView.setTextColor(mcontext.getResources().getColor(R.color.list_view_item3));
		}else{
			txtView.setTextColor(0xFFAFAFAF);
		}
        return convertView;
	}
	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		return super.getItemViewType(position);
	}

	
}
