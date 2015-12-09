package com.zhuoyou.plugin.album;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningTitleBar;

public class SportsAlbum extends Activity implements OnScrollListener {

	private AlbumPopupWindow albumPW;
	private LinearLayout sportsAlbum;
	private List<String> mList = null;
	public static Map<String, Bitmap> gridviewBitmapCaches = new HashMap<String, Bitmap>();
	private TextView text;
	private HashSet<String> set;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sports_album);
		RunningTitleBar.getTitleBar(this,
				getResources().getString(R.string.sports_photo));
		initDate();
		try {
			initView();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initDate() {
		mList=new ArrayList<String>();
		set = imagePath();
		Iterator<String> iterator = set.iterator();
		while (iterator.hasNext()) {
			mList.add(iterator.next());
		}
	}

	private void initView() throws FileNotFoundException {
		final WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.alpha = 1.0f;
		sportsAlbum = (LinearLayout) findViewById(R.id.sports_album);
		final GridView gridview = (GridView) findViewById(R.id.gridview);
		DisplayMetrics metric = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(metric);
		int width = metric.widthPixels;
		gridview.setAdapter(new SportsAlbumAdapter(this, mList, width / 3));
		gridview.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				/*
				 * if (gridview.getCount() - 1 == position) { Intent
				 * openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
				 * openAlbumIntent.setType("image/*"); } else {
				 */
				String path = mList.get(position);
				albumPW = new AlbumPopupWindow(SportsAlbum.this, path);
				albumPW.showAtLocation(sportsAlbum, Gravity.CENTER_VERTICAL
						| Gravity.CENTER_HORIZONTAL, 0, 0);
				SportsAlbum.this.getWindow().setAttributes(lp);
				albumPW.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss() {
						lp.alpha = 1.0f;
						SportsAlbum.this.getWindow().setAttributes(lp);
						if (albumPW.bmp != null) {
							albumPW.bmp.recycle();
							albumPW.bmp = null;
							System.gc();
						}
					}
				});
				// }
			}
		});
		gridview.setOnScrollListener(this);
		text = (TextView) findViewById(R.id.text);
		if (mList != null && !mList.isEmpty()) {
			gridview.setVisibility(View.VISIBLE);
			text.setVisibility(View.GONE);
		} else {
			gridview.setVisibility(View.GONE);
			text.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 获取图片地址列表
	 * 
	 * @param file
	 * @return
	 */

	private HashSet<String> imagePath() {
		ContentResolver cr = this.getContentResolver();
		Cursor c = cr.query(DataBaseContants.CONTENT_URI,
				new String[] { "img_uri" }, DataBaseContants.STATISTICS
						+ " = ? ", new String[] { "0" }, null);
		HashSet<String> hashSet = new HashSet<String>();
		if (c.getCount() > 0 && c.moveToFirst()) {
			do {
				if (c.getString(c.getColumnIndex(DataBaseContants.IMG_URI)) != null) {
					hashSet.add(c.getString(c.getColumnIndex(DataBaseContants.IMG_URI)));
				}
			} while (c.moveToNext());
		}
		c.close();
		c = null;
		return hashSet;
	}

	// 释放图片的函数
	private void recycleBitmapCaches(int fromPosition, int toPosition) {
		Bitmap delBitmap = null;
		for (int del = fromPosition; del < toPosition; del++) {
			delBitmap = gridviewBitmapCaches.get(mList.get(del));
			if (delBitmap != null) {
				// 如果非空则表示有缓存的bitmap，需要清理
				Log.d("txhlog", "release position:" + del);
				// 从缓存中移除该del->bitmap的映射
				gridviewBitmapCaches.remove(mList.get(del));
				delBitmap.recycle();
				delBitmap = null;
				System.gc();
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		recycleBitmapCaches(0, firstVisibleItem);
		recycleBitmapCaches(firstVisibleItem + visibleItemCount, totalItemCount);
	}
}
