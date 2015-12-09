package com.zhuoyou.plugin.info;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.PaintDrawable;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyou.plugin.running.R;

public class ChoseLikeSportPopoWindow extends PopupWindow {
	private TextView tv_ok;
	private ViewPager vPager;
	private GridView gv_one, gv_two, gv_three;
	private String[] wordsDataOne, wordsDataTwo, wordsDataThree;
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
	private String likeSportIndex=null;
	private List<String> selected ,choiceId;

	public ChoseLikeSportPopoWindow(Context context, String sportList) {
		likeSportIndex = sportList;
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
		tv_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				choiceId = selected;
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
	
	public List<String> getSport() {
		return choiceId;
	}

	private class MyAdapter extends BaseAdapter {
		private String[] wordsData;
		private int[] imgData;
		private Context context;
		private String[] sportArray = new String[100];

		public MyAdapter(String[] wordsData, int[] imgData, Context context) {
			// TODO Auto-generated constructor stub
			this.wordsData = wordsData;
			this.imgData = imgData;
			this.context = context;
			selected= new ArrayList<String>();
			choiceId= new ArrayList<String>();
			sportArray = context.getResources().getStringArray(R.array.whole_sport_type);
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
						R.layout.grid_item_chose_like_sport, null);
			}
			ImageView im = (ImageView) convertView.findViewById(R.id.im_icon);
			TextView tv = (TextView) convertView.findViewById(R.id.tv_name);
			im.setImageResource(imgData[position]);
			tv.setText(wordsData[position]);
			final ImageView isSelected = (ImageView) convertView.findViewById(R.id.isSelected);
			String likeIndex[]=null;
			if(!likeSportIndex.equals("")){
				likeIndex=likeSportIndex.split(",");
				if (likeIndex.length > 0) {
					for (int i = 0; i < likeIndex.length; i++) {
						String sport = wordsData[position];
						if(sport.equals(sportArray[Integer.parseInt(likeIndex[i])])){
							if (!selected.contains(sport)) {
								isSelected.setVisibility(View.VISIBLE);
								selected.add(wordsData[position]);
								choiceId.add(wordsData[position]);
							}
						}
					}
				}
			}
			
			im.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					likeSportIndex = "";
					if(isSelected.getVisibility() == View.VISIBLE){
						isSelected.setVisibility(View.GONE);
						for(int i=0;i<selected.size();i++){
							if(wordsData[position].equals(selected.get(i))){
								selected.remove(i);
							}
						}
					}else if(isSelected.getVisibility() == View.GONE){
						if(selected.size()<4){
							isSelected.setVisibility(View.VISIBLE);
							selected.add(wordsData[position]);
						} else {
							Toast.makeText(context, R.string.like_sport_limite, Toast.LENGTH_SHORT).show();
						}
					}
				}
			});
			
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
}
