package com.zhuoyou.plugin.running;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.album.AlbumPopupWindow;
import com.zhuoyou.plugin.album.AsyncLoadImageTask;
import com.zhuoyou.plugin.album.BitmapUtils;
import com.zhuoyou.plugin.album.SportsAlbumAdapter;
import com.zhuoyou.plugin.album.SportsAlbumAdapter.LoadedDrawable;
import com.zhuoyou.plugin.view.PolylineChart;

public class HomePageListItemAdapter extends BaseAdapter {
	private List<RunningItem> mTodayLists;
	private Context mContext;
	private LayoutInflater inflater;
	private Typeface mNumberTP;
	private String date;
	private AlbumPopupWindow albumPW;
	private static final int TYPE_SUMMARY = 0;
	private static final int TYPE_WEIGHT = 1;
	private static final int TYPE_DEVICE_SPORT = 2;
	private static final int TYPE_ADD_SPORT = 3;
	private static final int TYPE_GRAPHIC = 4;
	private static final int TYPE_GPS = 5;
	private static final int TYPE_PHONE_STEPS = 6;
	private int chartViewHeight;
	private ArrayList<Double> weightList = null;
	private float record;
    private  Display display;
	
	public HomePageListItemAdapter(Context ctx, List<RunningItem> list, String date, ArrayList<Double> weight, float z) {
		mContext = ctx;
		mTodayLists = list;
		mNumberTP = RunningApp.getCustomNumberFont();
		this.date = date;
		weightList = weight;
		record = z;
		WindowManager windowManager = ((Activity) mContext).getWindowManager();
        display = windowManager.getDefaultDisplay();
	}

	public void UpdateDate(Context ctx, List<RunningItem> list, String date, ArrayList<Double> weight, float z){
		mContext = ctx;
		mTodayLists = list;
		this.date = date;
		weightList = weight;
		record = z;
	}
	
	@Override
	public int getCount() {
		return mTodayLists.size();
	}

	@Override
	public RunningItem getItem(int position) {
		// TODO Auto-generated method stub
		return mTodayLists.get(position);
	}

	@Override
	public int getItemViewType(int position) {
		int type = 0;
		switch(getItem(position).getmType()) {
		case 0:
			type =  TYPE_SUMMARY;
			break;
		case 1:
			type =  TYPE_WEIGHT;
			break;
		case 2:
			if (getItem(position).getSportsType() == 0)
				type =  TYPE_DEVICE_SPORT;
			else
				type =  TYPE_ADD_SPORT;
			break;
		case 3:
			type =  TYPE_GRAPHIC;
			break;
		case 4:
			type =  TYPE_GRAPHIC;
			break;
		case 5:
			type =  TYPE_GPS;
			break;
		case 6:
			type = TYPE_PHONE_STEPS;
		}
		
		return type;
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 7;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder0 holder0 = null;	
		ViewHolder1 holder1 = null;
		ViewHolder2 holder2 = null;
		ViewHolder3 holder3 = null;
		ViewHolder4 holder4 = null;
		ViewHolder5 holder5 = null;
		ViewHolder6 holder6 = null;
		int type = getItemViewType(position);
		if (convertView == null) {
			inflater = LayoutInflater.from(mContext);
			switch (type) {
			case TYPE_SUMMARY:
				convertView = inflater.inflate(R.layout.listitem_summay, parent, false);
				holder0 = new ViewHolder0();
				holder0.value1 = (TextView) convertView.findViewById(R.id.description);
				holder0.img = (ImageView) convertView.findViewById(R.id.item_run_logo);
				holder0.summay_lay = (RelativeLayout) convertView.findViewById(R.id.summay_lay);
				convertView.setTag(holder0);
				break;
			case TYPE_WEIGHT:
				convertView = inflater.inflate(R.layout.listitem_weight, parent, false);
				holder1 = new ViewHolder1();
				holder1.value1 = (TextView) convertView.findViewById(R.id.weight);
				holder1.time = (TextView) convertView.findViewById(R.id.time);
				holder1.ray = (RelativeLayout) convertView.findViewById(R.id.polyline);
				holder1.weight_lay = (RelativeLayout) convertView.findViewById(R.id.weight_lay);
				convertView.setTag(holder1);
				break;
			case TYPE_DEVICE_SPORT:
				convertView =  inflater.inflate(R.layout.listitem_device_steps, parent, false);
				holder2 = new ViewHolder2();
				holder2.value1 = (TextView) convertView.findViewById(R.id.steps);
				holder2.value2 = (TextView) convertView.findViewById(R.id.distance);
				holder2.value3 = (TextView) convertView.findViewById(R.id.calories);
				holder2.time = (TextView) convertView.findViewById(R.id.time);
				holder2.steps_lay = (RelativeLayout) convertView.findViewById(R.id.steps_lay);
				convertView.setTag(holder2);
				break;
			case TYPE_ADD_SPORT:
				convertView =  inflater.inflate(R.layout.listitem_sports, parent, false);
				holder3 = new ViewHolder3();
				holder3.value1 = (TextView) convertView.findViewById(R.id.duration);
				holder3.value2 = (TextView) convertView.findViewById(R.id.calories);
				holder3.time = (TextView) convertView.findViewById(R.id.time);
				holder3.img = (ImageView) convertView.findViewById(R.id.item_run_logo);
				holder3.sports_lay = (RelativeLayout) convertView.findViewById(R.id.sports_lay);
				convertView.setTag(holder3);
				break;
			case TYPE_GRAPHIC:
				convertView =  inflater.inflate(R.layout.listitem_graphic_information, parent, false);
				holder4 = new ViewHolder4();
				holder4.imageview_thumbnail= (ImageView) convertView.findViewById(R.id.picture);
				holder4.value1 = (TextView) convertView.findViewById(R.id.graphic_info);
				holder4.time = (TextView) convertView.findViewById(R.id.time);
				holder4.graphic_lay = (RelativeLayout) convertView.findViewById(R.id.graphic_lay);
				convertView.setTag(holder4);
			//	convertView.setVisibility(View.GONE);//add zhaojunhui
				break;
			case TYPE_GPS:
				convertView = inflater.inflate(R.layout.listitem_gps, parent, false);
				holder5 = new ViewHolder5();
				holder5.imageview = (ImageView) convertView.findViewById(R.id.picture);
				holder5.value1 = (TextView) convertView.findViewById(R.id.value1);
				holder5.value2 = (TextView) convertView.findViewById(R.id.value2);
				holder5.value3 = (TextView) convertView.findViewById(R.id.value3);
				holder5.value4 = (TextView) convertView.findViewById(R.id.value4);
				holder5.time = (TextView) convertView.findViewById(R.id.time);
				holder5.gps_lay = (RelativeLayout) convertView.findViewById(R.id.gps_lay);
				convertView.setTag(holder5);
				break;
			case TYPE_PHONE_STEPS:
				convertView =  inflater.inflate(R.layout.listitem_phone_steps, parent, false);
				holder6 = new ViewHolder6();
				holder6.value1 = (TextView) convertView.findViewById(R.id.steps);
				holder6.value2 = (TextView) convertView.findViewById(R.id.distance);
				holder6.value3 = (TextView) convertView.findViewById(R.id.calories);
				holder6.time = (TextView) convertView.findViewById(R.id.time);
				holder6.steps_lay = (RelativeLayout) convertView.findViewById(R.id.steps_lay);
				convertView.setTag(holder6);
				break;
			}
		} else {
			switch (type) {
			case TYPE_SUMMARY:
				holder0 = (ViewHolder0) convertView.getTag();
				break;
			case TYPE_WEIGHT:
				holder1 = (ViewHolder1) convertView.getTag();
				break;
			case TYPE_DEVICE_SPORT:
				holder2 = (ViewHolder2) convertView.getTag();
				break;
			case TYPE_ADD_SPORT:
				holder3 = (ViewHolder3) convertView.getTag();
				break;
			case TYPE_GRAPHIC:
				holder4 = (ViewHolder4) convertView.getTag();
				break;
			case TYPE_GPS:
				holder5 = (ViewHolder5) convertView.getTag();
				break;
			case TYPE_PHONE_STEPS:
				holder6 = (ViewHolder6) convertView.getTag();
			}
		}

		String start = "";
		String end = "";
		String s_time = "";
		switch (type) {
		case TYPE_SUMMARY:
			if (record == 10000) {
				holder0.value1.setText(R.string.day_summary_5);
				holder0.img.setImageResource(R.drawable.day_summary_5);
			} else if (record == 0) {
				holder0.value1.setText(R.string.day_summary_1);
				holder0.img.setImageResource(R.drawable.day_summary_1);
			} else if (record > 0 && record < 0.5) {
				holder0.value1.setText(R.string.day_summary_2);
				holder0.img.setImageResource(R.drawable.day_summary_2);
			} else if (record >= 0.5 && record < 1) {
				holder0.value1.setText(R.string.day_summary_3);
				holder0.img.setImageResource(R.drawable.day_summary_3);
			} else if (record >= 1) {
				holder0.value1.setText(R.string.day_summary_4);
				holder0.img.setImageResource(R.drawable.day_summary_4);
			}
			if ((mTodayLists.size() - 1) == position) {
				holder0.summay_lay.setBackgroundResource(R.drawable.listitem_summay_bg_1);
			}
			break;
		case TYPE_WEIGHT:
			holder1.value1.setTypeface(mNumberTP);
			
			start = mTodayLists.get(position).getStartTime();
			s_time = start;
			holder1.time.setText(s_time);
			holder1.value1.setText(mTodayLists.get(position).getmWeight());
			if (weightList.size() > 0) {
				setPolylineView(weightList, holder1.ray);
			}
			if ((mTodayLists.size() - 1) == position) {
				holder1.weight_lay.setBackgroundResource(R.drawable.listitem_weight_bg_1);
			}
			break;
		case TYPE_DEVICE_SPORT:
			holder2.value1.setTypeface(mNumberTP);
			holder2.value2.setTypeface(mNumberTP);
			holder2.value3.setTypeface(mNumberTP);
			
			start = mTodayLists.get(position).getStartTime();
			end = mTodayLists.get(position).getEndTime();
			s_time = start + " - " + end;
			holder2.time.setText(s_time);
			holder2.value1.setText(mTodayLists.get(position).getSteps() + "");
			holder2.value2.setText(mTodayLists.get(position).getKilometer() + "");
			holder2.value3.setText(mTodayLists.get(position).getCalories() + "");
			if ((mTodayLists.size() - 1) == position) {
				holder2.steps_lay.setBackgroundResource(R.drawable.listitem_sports_bg_1);
			}
			break;
		case TYPE_ADD_SPORT:
			holder3.value1.setTypeface(mNumberTP);
			holder3.value2.setTypeface(mNumberTP);

			holder3.img.setImageResource(Tools.sportType[mTodayLists.get(position).getSportsType() - 1]);
			holder3.value1.setText(mTodayLists.get(position).getDuration() + "");
			holder3.value2.setText(mTodayLists.get(position).getCalories() + "");
			start = mTodayLists.get(position).getStartTime();
			end = mTodayLists.get(position).getEndTime();
			s_time = start + " - " + end;
			holder3.time.setText(s_time);
			if ((mTodayLists.size() - 1) == position) {
				holder3.sports_lay.setBackgroundResource(R.drawable.listitem_sports_bg_1);
			}
			break;
		case TYPE_GRAPHIC:
			final WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
			lp.alpha = 1.0f;
			if (mTodayLists.get(position).getmExplain() != null && !mTodayLists.get(position).getmExplain().equals("")) {
				holder4.value1.setText(mTodayLists.get(position).getmExplain());
			} else {
				holder4.value1.setText(null);
			}
			final ImageView img=holder4.imageview_thumbnail;	
			int w = display.getWidth() - (Tools.dip2px(mContext, 5)*2);
			int h = w / 4 * 3;
			String url = mTodayLists.get(position).getmImgUri();
			if (url != null && !url.equals("") && img != null) {
				String path = Tools.getSDPath() + "/Running/.thumbnailnew/" + url.substring(url.lastIndexOf("/")+1, url.lastIndexOf("."));
				Bitmap bmp = null;
				bmp = HomePageItemFragment.gridviewBitmapCaches.get(path + date + position);
				if (bmp != null) {	
					img.setImageBitmap(bmp);		
				}else{
					bmp= BitmapUtils.getScaleBitmap(path);
					if(bmp!=null){
						img.setImageBitmap(bmp);
						HomePageItemFragment.gridviewBitmapCaches.put(path+date+position, bmp);
					}else{
						if (SportsAlbumAdapter.cancelPotentialLoad(url, img)) {
							AsyncLoadImageTask task = new AsyncLoadImageTask(img,url,w,h,path+date+position);
							LoadedDrawable loadedDrawable = new LoadedDrawable(task);
							img.setImageDrawable(loadedDrawable);
							task.execute(position);
						}
					}		
				}
			} else {
				img.setImageDrawable(null);
			}
			
			start = mTodayLists.get(position).getStartTime();
			s_time = start;
			holder4.time.setText(s_time);	
			
			holder4.imageview_thumbnail.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (albumPW != null && albumPW.isShowing())
						return;
					albumPW = new AlbumPopupWindow(mContext,mTodayLists.get(position).getmImgUri());
					albumPW.showAtLocation(img, Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
					albumPW.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss() {
							lp.alpha = 1.0f;
							((Activity) mContext).getWindow().setAttributes(lp);
							albumPW = null;
						}
					});
				}
			});
			if ((mTodayLists.size() - 1) == position) {
				holder4.graphic_lay.setBackgroundResource(R.drawable.listitem_graphic_information_bg_1);
			}
			break;
		case TYPE_GPS: 
			holder5.value1.setText(mTodayLists.get(position).getmExplain() + "");
			int wgps=Tools.dip2px(mContext, 155);
			int hgps=Tools.dip2px(mContext, 98);
			String urlGps = mTodayLists.get(position).getmImgUri();
			if (urlGps != null && !urlGps.equals("") && holder5.imageview != null) {
				String path = Tools.getSDPath() + "/Running/.thumbnail/" + urlGps.substring(urlGps.lastIndexOf("/")+1, urlGps.lastIndexOf("."));
				Bitmap bmp = null;
				bmp = HomePageItemFragment.gridviewBitmapCaches.get(path + date + position);
				if (bmp != null) {	
					holder5.imageview.setImageBitmap(bmp);		
				}else{
					bmp= BitmapUtils.getScaleBitmap(path);
					if(bmp!=null){
						holder5.imageview.setImageBitmap(bmp);
						HomePageItemFragment.gridviewBitmapCaches.put(path+date+position, bmp);
					}else{
						if (SportsAlbumAdapter.cancelPotentialLoad(urlGps, holder5.imageview)) {
							AsyncLoadImageTask task = new AsyncLoadImageTask(holder5.imageview, urlGps, wgps, path+date+position);
							LoadedDrawable loadedDrawable = new LoadedDrawable(task);
							holder5.imageview.setImageDrawable(loadedDrawable);
							task.execute(position);
						}
					}		
				}
			} else {
				holder5.imageview.setVisibility(View.GONE);
			}
			
			start = mTodayLists.get(position).getStartTime();
			end = mTodayLists.get(position).getEndTime();
			int hour = Integer.valueOf(end.split(":")[0]);
			if (hour >= 24) {
				String minute = end.split(":")[1];
				int day = hour / 24;
				int newHour = hour - day * 24;
				if (newHour < 10)
					end = "0" + newHour + ":" + minute;
				else
					end = newHour + ":" + minute;
				StringBuilder builder = new StringBuilder(start);
				builder.append(" - ");
				builder.append(Tools.getDate(date, 0 - day).substring(5));
				builder.append(" ");
				builder.append(end);
				s_time = builder.toString();
			} else {
				s_time = start + " - " + end;
			}
			holder5.time.setText(s_time);
			if ((mTodayLists.size() - 1) == position) {
				holder5.gps_lay.setBackgroundResource(R.drawable.listitem_gps_bg_1);
			}
			holder5.value1.setText(mTodayLists.get(position).getmWeight());
			holder5.value2.setText(mTodayLists.get(position).getmBmi());
			holder5.value3.setText(mTodayLists.get(position).getKilometer() + "");
			holder5.value4.setText(mTodayLists.get(position).getCalories() + "");
			break;
		case TYPE_PHONE_STEPS:
			holder6.value1.setTypeface(mNumberTP);
			holder6.value2.setTypeface(mNumberTP);
			holder6.value3.setTypeface(mNumberTP);
			
			start = mTodayLists.get(position).getStartTime();
			end = mTodayLists.get(position).getEndTime();
			s_time = start + " - " + end;
			holder6.time.setText(s_time);
			holder6.value1.setText(mTodayLists.get(position).getSteps() + "");
			holder6.value2.setText(mTodayLists.get(position).getKilometer() + "");
			holder6.value3.setText(mTodayLists.get(position).getCalories() + "");
			if ((mTodayLists.size() - 1) == position) {
				holder6.steps_lay.setBackgroundResource(R.drawable.listitem_sports_bg_1);
			}
		}

		return convertView;
	}
	
	private class ViewHolder0 {
		private TextView value1;
		private ImageView img;
		private RelativeLayout summay_lay;
	}
	
	private class ViewHolder1 {
		private TextView value1;
		private TextView time;
		private RelativeLayout ray;
		private RelativeLayout weight_lay;
	}
	
	private class ViewHolder2 {
		private TextView value1;
		private TextView value2;
		private TextView value3;
		private TextView time;
		private RelativeLayout steps_lay;
	}

	private class ViewHolder3 {
		private TextView value1;
		private TextView value2;
		private TextView time;
		private ImageView img;
		private RelativeLayout sports_lay;
	}

	private class ViewHolder4 {
		private TextView value1;
		private TextView time;
		private ImageView imageview_thumbnail;
		private RelativeLayout graphic_lay;
	}
	
	private class ViewHolder5 {
		private TextView value1;
		private TextView value2;
		private TextView value3;
		private TextView value4;
		private TextView time;
		private ImageView imageview;
		private RelativeLayout gps_lay;
	}
	
	private class ViewHolder6 {
	    private TextView value1;
	    private TextView value2;
	    private TextView value3;
	    private TextView time;
	    private RelativeLayout steps_lay;
	}
	private void setPolylineView(ArrayList<Double> weightList, RelativeLayout ray) {
		chartViewHeight = Tools.dip2px(mContext, 100);
		PolylineChart polylineChart = new PolylineChart(mContext, weightList, 3, chartViewHeight, 80);
		RelativeLayout localRelativeLayout = new RelativeLayout(mContext);
		RelativeLayout.LayoutParams localLayoutParams = new RelativeLayout.LayoutParams(polylineChart.getCanvasWidth(), chartViewHeight);
		localLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		localRelativeLayout.setLayoutParams(localLayoutParams);
		localRelativeLayout.addView(polylineChart);
		ray.addView(localRelativeLayout);
	}
}
