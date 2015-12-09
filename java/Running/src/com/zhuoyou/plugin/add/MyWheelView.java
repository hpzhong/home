package com.zhuoyou.plugin.add;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhuoyou.plugin.running.Tools;

public class MyWheelView extends BaseAdapter {
	private String[] data;
	private Context context;
	private int mHeight = 50;
	private int selectPos;
	
	public MyWheelView(String[] data, Context context) {
		this.data = data;
		this.context = context;
		mHeight = (int) Tools.dip2px(context, mHeight);
	}
	
	public void setSelectPos(int pos){
		selectPos = pos;
	}

	public void setData(String[] data){
		this.data = data;
	}

	@Override
	public int getCount() {
		return (null != data) ? data.length : 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView txtView = null;
		if (null == convertView) {
			convertView = new TextView(context);
			convertView.setLayoutParams(new TosGallery.LayoutParams(-1, mHeight));
			txtView = (TextView) convertView;
			txtView.setGravity(Gravity.CENTER);
		}
		if (null == txtView) {
			txtView = (TextView) convertView;
		}
        if(position == selectPos){
			txtView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28);
        	txtView.setTextColor(0xff2b2b2b);
		}else if(position==selectPos-1 || position==selectPos+1){
			txtView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 26);
			txtView.setTextColor(0xffc6c6c6);
		}else if(position==selectPos-2 || position==selectPos+2){
			txtView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
			txtView.setTextColor(0xffc5c5c5);
		}else{
			txtView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
			txtView.setTextColor(0xffefefef);
		}
		txtView.setText(data[position]);
		return convertView;
	}
}
