package com.zhuoyou.plugin.album;

import java.lang.ref.WeakReference;
import java.util.List;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.zhuoyou.plugin.running.R;

public class SportsAlbumAdapter extends BaseAdapter {

	private Context mContext = null;
	private LayoutInflater mLayoutInflater = null;
	private List<String> mLists = null;

	private int width = 0;// 每个Item的宽度,可以根据实际情况修改
	private int height = 0;// 每个Item的高度,可以根据实际情况修改

	public SportsAlbumAdapter(Context c, List<String> path,int with) {
		mContext = c;
		mLists = path;
		width=with;
		height=with;
		mLayoutInflater = LayoutInflater.from(c);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mLists.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mLists.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		MyGridViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new MyGridViewHolder();
			convertView = mLayoutInflater.inflate(
					R.layout.layout_album_gridview_item, null);
			viewHolder.imageview_thumbnail = (ImageView) convertView
					.findViewById(R.id.imageview_thumbnail);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (MyGridViewHolder) convertView.getTag();
		}

		String url = mLists.get(position);
		if (cancelPotentialLoad(url, viewHolder.imageview_thumbnail)) {
			AsyncLoadImageTask task = new AsyncLoadImageTask(
					viewHolder.imageview_thumbnail,mLists,width,height);
			LoadedDrawable loadedDrawable = new LoadedDrawable(task);
			viewHolder.imageview_thumbnail.setImageDrawable(loadedDrawable);
			task.execute(position);
		}

		return convertView;
	}

	public static boolean cancelPotentialLoad(String url, ImageView imageview) {
		AsyncLoadImageTask loadImageTask = getAsyncLoadImageTask(imageview);

		if (loadImageTask != null) {
			String bitmapUrl = loadImageTask.getUrl();
			if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
				loadImageTask.cancel(true);
			} else {
				// 相同的url已经在加载中.
				return false;
			}
		}
		return true;

	}

	public static AsyncLoadImageTask getAsyncLoadImageTask(ImageView imageview) {
		if (imageview != null) {
			Drawable drawable = imageview.getDrawable();
			if (drawable instanceof LoadedDrawable) {
				LoadedDrawable loadedDrawable = (LoadedDrawable) drawable;
				return loadedDrawable.getLoadImageTask();
			}
		}
		return null;
	}

	public static class MyGridViewHolder {
		public ImageView imageview_thumbnail;
	}

	public static class LoadedDrawable extends ColorDrawable {
		private final WeakReference<AsyncLoadImageTask> loadImageTaskReference;

		public LoadedDrawable(AsyncLoadImageTask loadImageTask) {
			super(Color.TRANSPARENT);
			loadImageTaskReference = new WeakReference<AsyncLoadImageTask>(
					loadImageTask);
		}

		public AsyncLoadImageTask getLoadImageTask() {
			return loadImageTaskReference.get();
		}

	}

}
