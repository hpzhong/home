package com.zhuoyou.plugin.album;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.running.R;

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
		TextView tv_title = (TextView) findViewById(R.id.title);
		tv_title.setText(R.string.sports_photo);
		RelativeLayout im_back = (RelativeLayout) findViewById(R.id.back);
		im_back.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
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
	    WindowManager windowManager = getWindowManager();
	    Display display = windowManager.getDefaultDisplay();
		int width = display.getWidth();
		gridview.setAdapter(new SportsAlbumAdapter(this, mList, width / 3));
		gridview.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (albumPW != null && albumPW.isShowing())
					return;
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
						albumPW = null;
					}
				});
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
		Cursor c = cr.query(DataBaseContants.CONTENT_URI, new String[] { "img_uri" }, DataBaseContants.TYPE + " = ? AND " + DataBaseContants.STATISTICS + " = ? ", new String[] { "3", "0" }, null);
		HashSet<String> hashSet = new LinkedHashSet<String>();
		if (c.getCount() > 0 && c.moveToFirst()) {
			do {
				String img_uri = c.getString(c.getColumnIndex(DataBaseContants.IMG_URI));
				if (img_uri != null && !img_uri.equals("")) {
					hashSet.add(img_uri);
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
			delBitmap = gridviewBitmapCaches.get(mList.get(del) + del);
			if (delBitmap != null) {
				// 如果非空则表示有缓存的bitmap，需要清理
				Log.d("txhlog", "release position:" + del);
				// 从缓存中移除该del->bitmap的映射
				gridviewBitmapCaches.remove(mList.get(del) + del);
				delBitmap.recycle();
				System.gc();
				delBitmap = null;
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
