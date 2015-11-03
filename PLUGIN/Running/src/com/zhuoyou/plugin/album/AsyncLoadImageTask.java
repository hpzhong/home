package com.zhuoyou.plugin.album;

import java.lang.ref.WeakReference;
import java.util.List;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.zhuoyou.plugin.running.HomePageItemFragment;

public class AsyncLoadImageTask extends AsyncTask<Integer, Void, Bitmap> {

	private String url = null;
	private final WeakReference<ImageView> imageViewReference;
	private int width,height;
	private List<String> mLists = null;
	private boolean album;
	private String date;
	private int position;

	public AsyncLoadImageTask(ImageView imageview,List<String> path,int w,int h) {
		super();
		// TODO Auto-generated constructor stub
		imageViewReference = new WeakReference<ImageView>(imageview);
		this.width=w;
		this.height=h;
		this.mLists=path;
		album=true;
	}
	
	public AsyncLoadImageTask(ImageView imageview,String path,int w,int h,String date,int position) {
		super();
		// TODO Auto-generated constructor stub
		imageViewReference = new WeakReference<ImageView>(imageview);
		this.width=w;
		this.height=h;
		this.setUrl(path);
		this.date=date;
		this.position=position;
		album=false;
	}

	@Override
	protected Bitmap doInBackground(Integer... params) {
		// TODO Auto-generated method stub
		Bitmap bitmap = null;
		if(album){
			this.setUrl(mLists.get(params[0]));
			bitmap = BitmapUtils.decodeSampledBitmapFromFd(getUrl(), width, height, 1);
			SportsAlbum.gridviewBitmapCaches.put(mLists.get(params[0]), bitmap);
		}else{
			bitmap = BitmapUtils.decodeSampledBitmapFromFd(getUrl(), width, height, 2);
			HomePageItemFragment.gridviewBitmapCaches.put(getUrl()+date+position, bitmap);
		}
		return bitmap;
	}

	@Override
	protected void onPostExecute(Bitmap resultBitmap) {
		// TODO Auto-generated method stub
		if (isCancelled()) {
			resultBitmap = null;
		}
		if (imageViewReference != null) {
			ImageView imageview = imageViewReference.get();
			AsyncLoadImageTask loadImageTask = SportsAlbumAdapter.getAsyncLoadImageTask(imageview);
			if (this == loadImageTask) {
				imageview.setImageBitmap(resultBitmap);
				imageview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			}
		}
		super.onPostExecute(resultBitmap);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
