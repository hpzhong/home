package com.zhuoyou.plugin.album;

import java.lang.ref.WeakReference;
import java.util.List;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.zhuoyou.plugin.running.HomePageItemFragment;
import com.zhuoyou.plugin.running.Tools;

public class AsyncLoadImageTask extends AsyncTask<Integer, Void, Bitmap> {

	private String url = null;
	private final WeakReference<ImageView> imageViewReference;
	private int width,height;
	private List<String> mLists = null;
	private int album;
	private String thumbnailPath = null;

	public AsyncLoadImageTask(ImageView imageview, List<String> path, int length, String thumbnailPath) {
		super();
		// TODO Auto-generated constructor stub
		imageViewReference = new WeakReference<ImageView>(imageview);
		this.width = length;
		this.height = length;
		this.mLists = path;
		this.thumbnailPath = thumbnailPath;
		album = 1;
	}
	
	public AsyncLoadImageTask(ImageView imageview,String path,int w,int h,String thumbnailPath) {
		super();
		// TODO Auto-generated constructor stub
		imageViewReference = new WeakReference<ImageView>(imageview);
		this.width=w;
		this.height=h;
		this.setUrl(path);
		this.thumbnailPath=thumbnailPath;
		album = 2;
	}
	
	public AsyncLoadImageTask(ImageView imageview,String path,int w,String thumbnailPath) {
		super();
		// TODO Auto-generated constructor stub
		imageViewReference = new WeakReference<ImageView>(imageview);
		this.width=w;
		this.setUrl(path);
		this.thumbnailPath=thumbnailPath;
		album = 3;
	}

	@Override
	protected Bitmap doInBackground(Integer... params) {
		// TODO Auto-generated method stub
		Bitmap bitmap = null;
		if(album == 1){
			this.setUrl(mLists.get(params[0]));
		}
		if (Tools.fileIsExists(getUrl())) {
			if (album == 1) {
				bitmap = BitmapUtils.decodeSampledBitmapFromFd2(getUrl(),width, height, 3);
				SportsAlbum.gridviewBitmapCaches.put(mLists.get(params[0]),bitmap);
			} else if (album == 2) {
				bitmap = BitmapUtils.decodeSampledBitmapFromFd2(getUrl(),width, height, 1);
			} else {
				bitmap = BitmapUtils.decodeSampledBitmapFromFd(getUrl(), width,1);
				HomePageItemFragment.gridviewBitmapCaches.put(thumbnailPath, bitmap);
			}
		}
		return bitmap;
	}

	@Override
	protected void onPostExecute(Bitmap resultBitmap) {
		// TODO Auto-generated method stub
		if (isCancelled()) {
			resultBitmap = null;
		}
		
		if(resultBitmap == null)
			return;
		
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
