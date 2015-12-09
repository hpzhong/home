package com.zhuoyou.plugin.running;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.zhuoyou.plugin.add.AddPicture;
import com.zhuoyou.plugin.add.AddSports;
import com.zhuoyou.plugin.add.AddWeight;
import com.zhuoyou.plugin.add.AddWords;
import com.zhuoyou.plugin.album.AlbumPopupWindow;
import com.zhuoyou.plugin.album.AsyncLoadImageTask;
import com.zhuoyou.plugin.album.SportsAlbumAdapter;
import com.zhuoyou.plugin.album.SportsAlbumAdapter.LoadedDrawable;
import com.zhuoyou.plugin.album.SportsAlbumAdapter.MyGridViewHolder;

public class HomePageListItemAdapter extends BaseAdapter {
	private List<RunningItem> mTodayLists;
	private Context mContext;
	private Typeface mNewtype;
	private String date;
	private AlbumPopupWindow albumPW;

	public HomePageListItemAdapter(Context ctx, List<RunningItem> list,String date) {
		mContext = ctx;
		mTodayLists = list;
		mNewtype = Typeface.createFromAsset(ctx.getAssets(),
				"font/akzidenzgrotesklightcond.ttf");
		this.date = date;
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
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View mList = null;
		if (mTodayLists.get(position).getmType() == 1) {
			mList = LinearLayout.inflate(mContext, R.layout.listitem_weight,
					null);

			TextView mWeight = (TextView) mList.findViewById(R.id.weight);
			TextView mBMI = (TextView) mList.findViewById(R.id.bmi);
			TextView wei_unit = (TextView) mList.findViewById(R.id.unit_weight);
			TextView mTime = (TextView) mList.findViewById(R.id.time);

			mWeight.setTypeface(mNewtype);
			mBMI.setTypeface(mNewtype);
			wei_unit.setTypeface(mNewtype);

			String start = mTodayLists.get(position).getStartTime();
			String s_time = start;
			mTime.setText(s_time);
			mWeight.setText(mTodayLists.get(0).getmWeight());
			mBMI.setText(mTodayLists.get(0).getmBmi());
			mList.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent();
					intent.setClass(mContext, AddWeight.class);
					intent.putExtra("id", mTodayLists.get(0).getID());
					intent.putExtra("date", date);
					intent.putExtra("weightCount", mTodayLists.get(0).getmWeight());
					mContext.startActivity(intent);
				}
			});

		} else if (mTodayLists.get(position).getmType() == 2) {
			if (mTodayLists.get(position).getSportsType() != 0) {
				mList = LinearLayout.inflate(mContext,
						R.layout.listitem_sports, null);
				TextView mTime = (TextView) mList.findViewById(R.id.time);
				TextView mDuration = (TextView) mList
						.findViewById(R.id.duration);
				TextView run_unit = (TextView) mList
						.findViewById(R.id.run_unit);
				TextView mCalories = (TextView) mList
						.findViewById(R.id.calories);
				TextView cal_unit = (TextView) mList
						.findViewById(R.id.unit_calories);

				mDuration.setTypeface(mNewtype);
				cal_unit.setTypeface(mNewtype);
				run_unit.setTypeface(mNewtype);
				mCalories.setTypeface(mNewtype);

				mDuration.setText(mTodayLists.get(position).getDuration() + "");
				mCalories.setText(mTodayLists.get(position).getCalories() + "");
				String start = mTodayLists.get(position).getStartTime();
				String end = mTodayLists.get(position).getEndTime();
				String s_time = start + " - " + end;
				mTime.setText(s_time);
				mList.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent intent = new Intent();
						intent.setClass(mContext, AddSports.class);
						intent.putExtra("id", mTodayLists.get(position).getID());
						intent.putExtra("sportType", mTodayLists.get(position).getSportsType());
						intent.putExtra("sportStartTime", mTodayLists.get(position).getStartTime());
						intent.putExtra("date", date);
						intent.putExtra("sportDuration", mTodayLists.get(position).getDuration() + "");
						intent.putExtra("wasteCalories", mTodayLists.get(position).getCalories() + "");
						mContext.startActivity(intent);
					}
				});

			} else {
				mList = LinearLayout.inflate(mContext, R.layout.listitem_steps,
						null);
				TextView mTime = (TextView) mList.findViewById(R.id.time);
				TextView mSteps = (TextView) mList.findViewById(R.id.steps);
				TextView mDistance = (TextView) mList
						.findViewById(R.id.distance);
				TextView mCalories = (TextView) mList
						.findViewById(R.id.calories);
				TextView cal_unit = (TextView) mList
						.findViewById(R.id.unit_calories);
				TextView dur_unit = (TextView) mList
						.findViewById(R.id.run_distance);
				dur_unit.setTypeface(mNewtype);
				cal_unit.setTypeface(mNewtype);
				mSteps.setTypeface(mNewtype);
				mDistance.setTypeface(mNewtype);
				mCalories.setTypeface(mNewtype);
				String start = mTodayLists.get(position).getStartTime();
				String end = mTodayLists.get(position).getEndTime();
				String s_time = start + " - " + end;
				mTime.setText(s_time);
				mSteps.setText(mTodayLists.get(position).getSteps() + "");
				mDistance
						.setText(mTodayLists.get(position).getKilometer() + "");
				mCalories.setText(mTodayLists.get(position).getCalories() + "");
			}
		} else if (mTodayLists.get(position).getmType() == 3) {
			final WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
			lp.alpha = 1.0f;
			
			MyGridViewHolder viewHolder = new MyGridViewHolder();
			convertView = LinearLayout.inflate(mContext,
					R.layout.listitem_graphic_information, null);
			viewHolder.imageview_thumbnail = (ImageView) convertView
					.findViewById(R.id.picture);
			convertView.setTag(viewHolder);
			
			TextView mGraphicInfo = (TextView) convertView
					.findViewById(R.id.graphic_info);
			LinearLayout relayout = (LinearLayout) convertView
					.findViewById(R.id.relativelayout_card);
			mGraphicInfo.setTypeface(mNewtype);
			mGraphicInfo.setText(mTodayLists.get(position).getmExplain() + "");
			relayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent();
					intent.setClass(mContext, AddPicture.class);
					intent.putExtra("id", mTodayLists.get(position).getID());
					intent.putExtra("imgUri", mTodayLists.get(position).getmImgUri());
					intent.putExtra("date", date);
					intent.putExtra("words", mTodayLists.get(position).getmExplain() + "");
					mContext.startActivity(intent);
				}
			});
			
			final ImageView img=viewHolder.imageview_thumbnail;
			viewHolder.imageview_thumbnail.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				albumPW=new AlbumPopupWindow(mContext,mTodayLists.get(position).getmImgUri());
				albumPW.showAtLocation(img, Gravity.CENTER_VERTICAL
						| Gravity.CENTER_HORIZONTAL, 0, 0);
				albumPW.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss() {
						lp.alpha = 1.0f;
						((Activity) mContext).getWindow().setAttributes(lp);
						if(albumPW.bmp!=null){
							albumPW.bmp.recycle();
							albumPW.bmp = null;
							System.gc();
						}			
					}
				});
			}
		});	
			
			int w=Tools.dip2px(mContext, 130);
			int h=Tools.dip2px(mContext, 100);
			String url = mTodayLists.get(position).getmImgUri();
			if(url!=null && viewHolder.imageview_thumbnail!=null){
				if (SportsAlbumAdapter.cancelPotentialLoad(url, viewHolder.imageview_thumbnail)) {
					AsyncLoadImageTask task = new AsyncLoadImageTask(
							viewHolder.imageview_thumbnail,url,w,h, mTodayLists.get(position).getDate(),position);
					LoadedDrawable loadedDrawable = new LoadedDrawable(task);
					viewHolder.imageview_thumbnail.setImageDrawable(loadedDrawable);
					task.execute(position);
				}
			}
			
			TextView mTime = (TextView) convertView.findViewById(R.id.time);
			String start = mTodayLists.get(position).getStartTime();
			String s_time = start;
			mTime.setText(s_time);	
			
			if ((position + 2) % 2 == 0) {
				convertView.setBackgroundResource(R.drawable.selector_listitem_dark_bg);
			} else {
				convertView.setBackgroundResource(R.drawable.selector_listitem_light_bg);
			}
			
			return convertView;

		} else if (mTodayLists.get(position).getmType() == 4) {
			mList = LinearLayout.inflate(mContext,
					R.layout.listitem_description, null);
			TextView mDescription = (TextView) mList
					.findViewById(R.id.description);
			mDescription.setTypeface(mNewtype);
			mDescription.setText(mTodayLists.get(position).getmExplain() + "");
			mList.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent();
					intent.setClass(mContext, AddWords.class);
					intent.putExtra("id", mTodayLists.get(position).getID());
					intent.putExtra("words", mTodayLists.get(position).getmExplain() + "");
					intent.putExtra("date", date);
					mContext.startActivity(intent);
				}
			});
			TextView mTime = (TextView) mList.findViewById(R.id.time);
			String start = mTodayLists.get(position).getStartTime();
			String s_time = start;
			mTime.setText(s_time);
		}

		if ((position + 2) % 2 == 0) {
			mList.setBackgroundResource(R.drawable.selector_listitem_dark_bg);
		} else {
			mList.setBackgroundResource(R.drawable.selector_listitem_light_bg);
		}

		return mList;
	}

}
