package com.zhuoyou.plugin.add;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zhuoyou.plugin.running.R;

public class SportChosePopoWindow extends PopupWindow implements
		OnItemClickListener {
	private TextView tv_ok;
	private ViewPager vPager;
	private GridView gv_one, gv_two, gv_three;
	private String[] wordsDataOne, wordsDataTwo, wordsDataThree;
	private String seleted = "跑步";
	private String choiced = "跑步";
	private int[] imgDataOne = { R.drawable.paobu, R.drawable.qixing,
			R.drawable.zuqiu, R.drawable.lanqiu, R.drawable.pashan,
			R.drawable.yumaoqiu, R.drawable.palouti, R.drawable.tiaowu,
			R.drawable.buxing, R.drawable.youyong, R.drawable.tiaosheng,
			R.drawable.paiqiu

	};
	private int[] imgDataTwo = { R.drawable.pingpang, R.drawable.jianshencao,
			R.drawable.lunhua, R.drawable.quanji, R.drawable.wangqiu,
			R.drawable.gaoerfu, R.drawable.bangqiu, R.drawable.huaxue,
			R.drawable.danbanhuaxue, R.drawable.yujia, R.drawable.huachuan,
			R.drawable.baolingqiu };
	private int[] imgDataThree = { R.drawable.feibiao, R.drawable.tiaoshui,
			R.drawable.menqiu, R.drawable.chonglang, R.drawable.biqiu,
			R.drawable.ganlanqiu, R.drawable.jijian, R.drawable.banqiu,
			R.drawable.binghu, R.drawable.feipan};
	private View view;
	
	private List<View> viewGroup = new ArrayList<View>();
	private MyAdapter page_one_Adapter ;
	private MyAdapter page_two_Adapter ;
	private MyAdapter page_three_Adapter;

	public SportChosePopoWindow(Context context,String selete) {
		this.seleted = selete;
		this.choiced = selete;
		LayoutInflater inflater = LayoutInflater.from(context);
		view = inflater.inflate(R.layout.viewpage_demo, null);
		vPager = (ViewPager) view.findViewById(R.id.vpage);
		tv_ok = (TextView) view.findViewById(R.id.tv_ok);
		View page_one = inflater.inflate(R.layout.grid_demo, null);
		View page_two = inflater.inflate(R.layout.grid_demo1, null);
		View page_three = inflater.inflate(R.layout.grid_demo2, null);
		gv_one = (GridView) page_one.findViewById(R.id.gv);
		gv_two = (GridView) page_two.findViewById(R.id.gv1);
		gv_three = (GridView) page_three.findViewById(R.id.gv2);
		gv_one.setOnItemClickListener(this);
		gv_two.setOnItemClickListener(this);
		gv_three.setOnItemClickListener(this);
		tv_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				choiced = seleted;
				dismiss();
			}
		});
		wordsDataOne = context.getResources().getStringArray(
				R.array.page_one_sports);
		wordsDataTwo = context.getResources().getStringArray(
				R.array.page_two_sports);
		wordsDataThree = context.getResources().getStringArray(
				R.array.page_three_sports);
		page_one_Adapter = new MyAdapter(wordsDataOne, imgDataOne, context);
		page_two_Adapter = new MyAdapter(wordsDataTwo, imgDataTwo, context);
		page_three_Adapter = new MyAdapter(wordsDataThree, imgDataThree, context);
		gv_one.setAdapter(page_one_Adapter);
		gv_two.setAdapter(page_two_Adapter);
		gv_three.setAdapter(page_three_Adapter);

		viewGroup.add(page_one);
		viewGroup.add(page_two);
		viewGroup.add(page_three);
		vPager.setAdapter(new MyPagerAdapter(viewGroup));

		this.setContentView(view);
		this.setWidth(LayoutParams.MATCH_PARENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setFocusable(true);

		this.setBackgroundDrawable(new PaintDrawable());
		this.setOutsideTouchable(true);
	}

	private class MyAdapter extends BaseAdapter {
		private String selectionItem = null;
		private String[] wordsData;
		private int[] imgData;
		private Context context;

		public MyAdapter(String[] wordsData, int[] imgData, Context context) {
			// TODO Auto-generated constructor stub
			this.wordsData = wordsData;
			this.imgData = imgData;
			this.context = context;
		}
		
		public void setSelection(String choseItem){
			selectionItem = choseItem;
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
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.grid_item_demo, null);
			}
			convertView.setBackgroundResource(R.drawable.grid_item_select);
			if(wordsData[position].equals(selectionItem) || wordsData[position].equals(seleted)){
				convertView.setBackgroundResource(R.drawable.sport_selected);
			}else{
				convertView.setBackgroundColor(Color.TRANSPARENT);
			}
			ImageView im = (ImageView) convertView.findViewById(R.id.im_icon);
			TextView tv = (TextView) convertView.findViewById(R.id.tv_name);
			im.setImageResource(imgData[position]);
			tv.setText(wordsData[position]);
			
			// TODO Auto-generated method stub
			return convertView;
		}

	}

	private class MyPagerAdapter extends PagerAdapter {
		public List<View> mlist;

		public MyPagerAdapter(List<View> mlist) {
			this.mlist = mlist;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			((ViewPager) container).removeView(mlist.get(position));
		}

		@Override
		public void finishUpdate(ViewGroup container) {
			// TODO Auto-generated method stub
			super.finishUpdate(container);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			// TODO Auto-generated method stub
			((ViewPager) container).addView(mlist.get(position), 0);
			return mlist.get(position);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
			// TODO Auto-generated method stub
			super.restoreState(state, loader);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mlist.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == (arg1);
		}

	}

	public String getSport() {
		return choiced;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.gv:
			seleted = wordsDataOne[arg2];
			page_one_Adapter.setSelection(seleted);
			page_two_Adapter.setSelection(null);
			page_three_Adapter.setSelection(null);
			page_one_Adapter.notifyDataSetChanged();
			page_two_Adapter.notifyDataSetChanged();
			page_three_Adapter.notifyDataSetChanged();
			break;
		case R.id.gv1:
			seleted = wordsDataTwo[arg2];
			page_two_Adapter.setSelection(seleted);
			page_one_Adapter.setSelection(null);
			page_three_Adapter.setSelection(null);
			page_one_Adapter.notifyDataSetChanged();
			page_two_Adapter.notifyDataSetChanged();
			page_three_Adapter.notifyDataSetChanged();
			break;
		case R.id.gv2:
			seleted = wordsDataThree[arg2];
			page_three_Adapter.setSelection(seleted);
			page_one_Adapter.setSelection(null);
			page_two_Adapter.setSelection(null);
			page_one_Adapter.notifyDataSetChanged();
			page_two_Adapter.notifyDataSetChanged();
			page_three_Adapter.notifyDataSetChanged();
			break;
		default:
			break;
		}
	}
}
