package com.zhuoyou.plugin.add;

import android.content.Context;
import android.graphics.drawable.PaintDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.running.R;

public class ChooseSportPopoWindow extends PopupWindow {
	private ListView listView;
	private MyAdapter page_one_Adapter ;
	private String[] wordsData;
	private int[] imgData = { R.drawable.paobu, R.drawable.qixing,
			R.drawable.zuqiu, R.drawable.lanqiu, R.drawable.pashan,
			R.drawable.yumaoqiu, R.drawable.palouti, R.drawable.tiaowu,
			R.drawable.buxing, R.drawable.youyong, R.drawable.tiaosheng,
			R.drawable.paiqiu, R.drawable.pingpang, R.drawable.jianshencao,
			R.drawable.lunhua, R.drawable.quanji, R.drawable.wangqiu,
			R.drawable.gaoerfu, R.drawable.bangqiu, R.drawable.huaxue,
			R.drawable.danbanhuaxue, R.drawable.yujia, R.drawable.huachuan,
			R.drawable.baolingqiu,R.drawable.feibiao, R.drawable.tiaoshui,
			R.drawable.menqiu, R.drawable.chonglang, R.drawable.biqiu,
			R.drawable.ganlanqiu, R.drawable.jijian, R.drawable.banqiu,
			R.drawable.binghu, R.drawable.feipan
	};
	private Context mContext;
	private String choiced = null;
	private View view;
	private TextView tv_ok;
	
	public ChooseSportPopoWindow(Context context, String selete) {
		mContext = context;
		choiced = selete;
		
		LayoutInflater inflater = LayoutInflater.from(context);
		view = inflater.inflate(R.layout.viewpage_demo, null);
		listView = (ListView) view.findViewById(R.id.sport_select_listview);
		wordsData = mContext.getResources().getStringArray(R.array.page_sport);
		page_one_Adapter = new MyAdapter(wordsData, imgData);
		listView.setAdapter(page_one_Adapter);
		tv_ok = (TextView) view.findViewById(R.id.tv_ok);
		tv_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismiss();
			}
		});
		
		this.setContentView(view);
		this.setWidth(LayoutParams.MATCH_PARENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setFocusable(true);
		
		this.setBackgroundDrawable(new PaintDrawable());
		this.setOutsideTouchable(true);
	}
	
	private class MyAdapter extends BaseAdapter {
		private String[] wordsData;
		private int[] imgData;

		public MyAdapter(String[] wordsData, int[] imgData) {
			// TODO Auto-generated constructor stub
			this.wordsData = wordsData;
			this.imgData = imgData;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return wordsData.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return wordsData[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_choose_sport, null);
			}
			ImageView im = (ImageView) convertView.findViewById(R.id.image_icon);
			TextView tv = (TextView) convertView.findViewById(R.id.sport_name);
			RelativeLayout relative = (RelativeLayout) convertView.findViewById(R.id.listview_item);
			im.setImageResource(imgData[position]);
			tv.setText(wordsData[position]);
			final ImageView isSelected = (ImageView) convertView.findViewById(R.id.sport_select);
			if (choiced.equals(wordsData[position])) {
				Log.d("txhlog", "choiced:" + choiced);
				isSelected.setVisibility(View.VISIBLE);
			} else {
				isSelected.setVisibility(View.GONE);
			}
			isSelected.setTag(wordsData[position]);
			relative.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					ImageView pre_isSelected_tag = null;
					if (choiced != null && choiced != wordsData[position]) {
						pre_isSelected_tag = (ImageView) listView.findViewWithTag(choiced);
					}
					ImageView isSelected_tag = (ImageView) listView.findViewWithTag(wordsData[position]);
					choiced = wordsData[position];
					if (pre_isSelected_tag != null) {
						pre_isSelected_tag.setVisibility(View.GONE);
					}
					if (isSelected_tag != null) {
						isSelected_tag.setVisibility(View.VISIBLE);
					}
				}
				
			});
			return convertView;
		}
	}
	
	public String getSport() {
		return choiced;
	}
}
