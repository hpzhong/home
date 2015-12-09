package com.zhuoyou.plugin.running;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.view.StatsCircleView;

public class HomePageItemFragment extends Fragment implements OnScrollListener{
	private ListView mListView;

	private StatsCircleView mStatsCircleView;

	private HomePageListItemAdapter mListAdapter;

	private List<RunningItem> mTodayLists = new ArrayList<RunningItem>();

	private RunningItem mDateBean;
	
	public static Map<String, Bitmap> gridviewBitmapCaches = new HashMap<String, Bitmap>();

	
	public static HomePageItemFragment newInstance(RunningItem bean) {
		HomePageItemFragment fragment = new HomePageItemFragment();
		fragment.mDateBean = bean;
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.home_page_item, container, false);
		mListView = (ListView) root.findViewById(R.id.listview_activity);
		mListView.setOnScrollListener(this);
		return root;
	}

	/*挪到onResume()里面解决进入易动力有闪退的问题
	@Override
	public void onActivityCreated(Bundle paramBundle) {
		super.onActivityCreated(paramBundle);
		mListView.setTag("listview");
		mStatsCircleView = new StatsCircleView(getActivity(), mDateBean);
		if (mListView.getHeaderViewsCount() == 0) {
			mListView.addHeaderView(mStatsCircleView);
		}
		initListData();
	}*/

	@Override
	public void onResume() {
		super.onResume();
		Log.i("gchk", "item onResume");
		mListView.setTag("listview");
		mStatsCircleView = new StatsCircleView(getActivity(), mDateBean);
		if (mListView.getHeaderViewsCount() == 0) {
			mListView.addHeaderView(mStatsCircleView);
		}
		initListData();
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i("gchk", "item onPause");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.i("gchk", "item onDetach");
	}

	private void initListData() {
		mTodayLists.clear();
		long start = System.currentTimeMillis();
		ContentResolver cr = getActivity().getContentResolver();
		String day = mDateBean.getDate();
		Cursor c = cr.query(DataBaseContants.CONTENT_URI, new String[] { "_id", "date", "time_duration", "time_start", "time_end", "steps",
				"kilometer", "calories" ,"weight","bmi","img_uri","img_explain","sports_type","type"},DataBaseContants.DATE + "  = ? AND " + DataBaseContants.STATISTICS + " = ? ",
				new String[] { day,"0" }, DataBaseContants.ID+" DESC");
		SortCursor sc = new SortCursor(c, DataBaseContants.TIME_START);
		sc.moveToFirst();
		int count = sc.getCount();
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				RunningItem item = new RunningItem();
				item.setID(c.getLong(sc.getColumnIndex(DataBaseContants.ID)));
				item.setDate(c.getString(sc.getColumnIndex(DataBaseContants.DATE)));
				item.setDuration(c.getString(sc.getColumnIndex(DataBaseContants.TIME_DURATION)));
				item.setStartTime(c.getString(sc.getColumnIndex(DataBaseContants.TIME_START)));
				item.setEndTime(c.getString(sc.getColumnIndex(DataBaseContants.TIME_END)));
				item.setCalories(c.getInt(sc.getColumnIndex(DataBaseContants.CALORIES)));
				item.setSteps(c.getInt(sc.getColumnIndex(DataBaseContants.STEPS)));
				item.setKilometer(c.getInt(sc.getColumnIndex(DataBaseContants.KILOMETER)));
				item.setmWeight(c.getString(sc.getColumnIndex(DataBaseContants.CONF_WEIGHT)));
				item.setmBmi(c.getString(sc.getColumnIndex(DataBaseContants.BMI)));
				item.setmImgUri(c.getString(sc.getColumnIndex(DataBaseContants.IMG_URI)));
				item.setmExplain(c.getString(sc.getColumnIndex(DataBaseContants.EXPLAIN)));
				item.setSportsType(c.getInt(sc.getColumnIndex(DataBaseContants.SPORTS_TYPE)));
				item.setmType(c.getInt(sc.getColumnIndex(DataBaseContants.TYPE)));
				item.setisStatistics(false);
				if(c.getInt(sc.getColumnIndex(DataBaseContants.TYPE))==1){
					mTodayLists.add(0, item);
				}else{
					mTodayLists.add(item);
				}
				sc.moveToNext();
			}
		}
		c.close();
		c = null;
		sc.close();
		sc = null;
		if (mListAdapter == null) {
			mListAdapter = new HomePageListItemAdapter(getActivity(), mTodayLists,day);
			mListView.setAdapter(mListAdapter);
		}
		mListAdapter.notifyDataSetChanged();

		long end = System.currentTimeMillis();
		Log.i("gchk", "initListData耗时" + (end - start));
	}
	
	// 释放图片的函数
	private void recycleBitmapCaches(int fromPosition, int toPosition) {
		Bitmap delBitmap = null;
		for (int del = fromPosition; del < toPosition-1; del++) {
			if(gridviewBitmapCaches!=null){
				delBitmap = gridviewBitmapCaches.get(mTodayLists.get(del).getmImgUri()+mTodayLists.get(del).getDate()+del);
				if (delBitmap != null) {
					// 如果非空则表示有缓存的bitmap，需要清理
					// 从缓存中移除该del->bitmap的映射
					gridviewBitmapCaches.remove(mTodayLists.get(del).getmImgUri()+mTodayLists.get(del).getDate()+del);
					delBitmap.recycle();
					delBitmap = null;
					System.gc();
				}
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
