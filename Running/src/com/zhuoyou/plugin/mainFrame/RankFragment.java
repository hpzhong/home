package com.zhuoyou.plugin.mainFrame;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.custom.TitlePageIndicator;
import com.zhuoyou.plugin.custom.ViewPagerScroller;
import com.zhuoyou.plugin.rank.RankInfo;
import com.zhuoyou.plugin.rank.RankListAdapter;
import com.zhuoyou.plugin.rank.ShareRankActivity;
import com.zhuoyou.plugin.running.Main;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;

public class RankFragment extends Fragment implements OnClickListener {
	
	private Context mContext;
	private View mRootView;
	private ImageView titleShare;
	private ViewPager viewPager;
	private TitlePageIndicator mIndicator;
	public static  Handler handler = null; 
	private List<RankInfo> sevenDalysList = new ArrayList<RankInfo>();
	private List<RankInfo> mouthDalysList = new ArrayList<RankInfo>();
	private List<RankInfo> highestStepList = new ArrayList<RankInfo>();
	private List<RankInfo> accountServenData = new ArrayList<RankInfo>();
	private List<RankInfo> accountMouthData = new ArrayList<RankInfo>();
	private List<RankInfo> accountHighestData = new ArrayList<RankInfo>();
	private TextView myRank, myName, mySteps;
	private ImageView myIcon, myRankBg;
	private List<String> mPageTitleContent;
	private LinearLayout noRankLayout;
	private RelativeLayout myRankLayout;
	private boolean isLogin;
	private int currIndex;
	private Typeface mNumberTP;
	//private Typeface mTextTP;

	private Bitmap bmp = null;
	
    private ImageView mImgRankTheme;
    private int mRankThemeBgHeight = 0;
    private LinearLayout noLogin;
    private View mSubFrame;
    private FrameLayout.LayoutParams realHeaderLayoutParams;
    private static float event_y = 0f;
    private static float event_x = 0f;
    private boolean flag = false ;
    private float top_margin = 0;
    private float last_margin = 0;
	Main.MyOnTouchListener myOnTouchListener;
	private OnScrollListener mListViewScrollListener;
	private boolean isListViewFirstItemVisible;
	private View mView0;
	private View mView1;
	private View mView2;
    private View mView3;
    private View mView4;
	private ListView list0;
	private ListView list1;
	private ListView list2;
	private ListView list3;
	private ListView list4;
	private RankListAdapter rAdapterDay;
	private RankListAdapter rAdapterWeek;
	private RankListAdapter rAdapterMouth;
	private RankListPageAdatper rankListPA;
	
	private RelativeLayout progress;
	
	private ProgressBar titleProgress;
	public RankFragment(Context context){
		mContext = context;
	}
	
	public RankFragment( ){
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mRootView = inflater.inflate(R.layout.rank_fragment, container, false);	
		InitListener();
		initView();
		sevenDalysList = ((Main)getActivity()).GetRankDate(1);
		mouthDalysList = ((Main)getActivity()).GetRankDate(2);
		highestStepList = ((Main)getActivity()).GetRankDate(3);
		accountServenData = ((Main)getActivity()).GetRankDate(4);
		accountMouthData = ((Main)getActivity()).GetRankDate(5);
		accountHighestData = ((Main)getActivity()).GetRankDate(6);
		if(sevenDalysList.size()>0&&
				mouthDalysList.size()>0&&
				highestStepList.size()>0&&
				accountServenData.size()>0&&
				accountMouthData.size()>0&&
				accountHighestData.size()>0){
			if(progress.getVisibility()==View.VISIBLE){
				progress.setVisibility(View.GONE);
			}
			if(titleProgress.getVisibility()==View.VISIBLE){
				titleProgress.setVisibility(View.GONE);
			}
			setInitAdapter();
			setShowType(currIndex);
		}
		return mRootView;				
	}
	

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}		
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		((Main) getActivity()).unregisterMyOnTouchListener( myOnTouchListener );
		//add here for oom ;
		rAdapterDay = null;
		rAdapterWeek = null;
		rAdapterMouth = null;
		rankListPA = null;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	void initView() {
		Log.d("yangyang","initView");
		titleShare = (ImageView) mRootView.findViewById(R.id.title_share);
		titleShare.setOnClickListener(this);
		mImgRankTheme = (ImageView) mRootView.findViewById(R.id.rank_theme_bg);
		noLogin = (LinearLayout) mRootView.findViewById(R.id.nologin);
        mSubFrame = (View) mRootView.findViewById(R.id.container);
		realHeaderLayoutParams = (android.widget.FrameLayout.LayoutParams) mSubFrame.getLayoutParams();
		top_margin = realHeaderLayoutParams.topMargin;
		
		viewPager = (ViewPager) mRootView.findViewById(R.id.vPager);
		initViewPagerScroll(viewPager);
		mIndicator = (TitlePageIndicator) mRootView.findViewById(R.id.indicator);

		noRankLayout = (LinearLayout) mRootView.findViewById(R.id.noranklayout);
		myRankLayout = (RelativeLayout) mRootView.findViewById(R.id.mylayout);
		myRankLayout.setVisibility(View.GONE);
		myRank = (TextView) mRootView.findViewById(R.id.my_rank);
		myRankBg=(ImageView)mRootView.findViewById(R.id.my_rank_bg);
		myIcon=(ImageView)mRootView.findViewById(R.id.my_icon);
		myName = (TextView) mRootView.findViewById(R.id.my_name);
		mySteps = (TextView) mRootView.findViewById(R.id.step);
		//mTextTP = RunningApp.getCustomChineseFont();
		mNumberTP = RunningApp.getCustomNumberFont();	
		
		progress =(RelativeLayout) mRootView.findViewById(R.id.progress_bar);
		titleProgress =(ProgressBar) mRootView.findViewById(R.id.title_progress);
		mPageTitleContent = new ArrayList<String>();
		
		String dayTitle = mContext.getResources().getString(R.string.dayRank);
		String weekTitle = mContext.getResources().getString(R.string.weekRank);
		String mouthTitle = mContext.getResources().getString(R.string.mouthRank);
		
		mPageTitleContent.add(mouthTitle);
		mPageTitleContent.add(dayTitle);
		mPageTitleContent.add(weekTitle);
		mPageTitleContent.add(mouthTitle);
		mPageTitleContent.add(dayTitle);
		
		mView0 = ((Activity)mContext).getLayoutInflater().inflate(R.layout.motion_rank_viewpager, null);
		mView1 = ((Activity)mContext).getLayoutInflater().inflate(R.layout.motion_rank_viewpager, null);
		mView2 = ((Activity)mContext).getLayoutInflater().inflate(R.layout.motion_rank_viewpager, null);
		mView3 = ((Activity)mContext).getLayoutInflater().inflate(R.layout.motion_rank_viewpager, null);
		mView4 = ((Activity)mContext).getLayoutInflater().inflate(R.layout.motion_rank_viewpager, null);
		
		isLogin = Tools.getLogin(mContext);		
		if (isLogin) {
			noLogin.setVisibility(View.GONE);
			mSubFrame.setVisibility(View.VISIBLE);
		} else {
			noLogin.setVisibility(View.VISIBLE);
			mSubFrame.setVisibility(View.GONE);
		}
	}

	
	private void InitListener(){
		myOnTouchListener = new Main.MyOnTouchListener() {
			@Override
			public boolean onTouch(MotionEvent ev) {
				// TODO Auto-generated method stub
				mRankThemeBgHeight = mImgRankTheme.getHeight();
				switch(ev.getAction()){
				case MotionEvent.ACTION_DOWN:
					event_y = ev.getY();
					event_x = ev.getX();
					realHeaderLayoutParams = (android.widget.FrameLayout.LayoutParams) mSubFrame.getLayoutParams();
					top_margin = realHeaderLayoutParams.topMargin;
					last_margin = top_margin;
					break;
				case MotionEvent.ACTION_MOVE:
					float currentX = ev.getX();
					float currentY = ev.getY();	
					float deltayX = currentX - event_x;
					float deltayY = currentY - event_y;
					event_x = currentX;
					event_y = currentY;
					
//					Log.d("zzb","deltay = "+deltayY);
					if(Math.abs(deltayY) > Math.abs(deltayX)){
						if (deltayY > 0) {// scrolldown							
							//theme bg hide,listview firstitem is not 0
							if(flag == false && !isListViewFirstItemVisible){
								return true;
							}
							
							if (top_margin + deltayY < 0) {
								top_margin = (top_margin + deltayY);
								flag = true;
							} else {
								top_margin = 0;
								flag = false;
							}
//							Log.d("zzb", "top_margin1111 = " + top_margin);

						} else {// scroll up
							if (top_margin + deltayY > -1 * mRankThemeBgHeight) {
								top_margin = (top_margin + deltayY);
								flag = true;
							} else {
								top_margin = -1 * mRankThemeBgHeight;
								flag = false;
							}
//							Log.d("zzb", "top_margin2222 = " + top_margin);

						}
						if (Math.abs(last_margin - top_margin) > 1) {
							realHeaderLayoutParams.topMargin = (int) top_margin;
							mSubFrame.setLayoutParams(realHeaderLayoutParams);
							last_margin = top_margin;
						}
					}
					if(Math.abs(deltayX) > Math.abs(deltayY))
						return true;
					
					if(flag)
						return false;
					
					break;
				case MotionEvent.ACTION_UP:
//					Log.d("zzb","action up");
					break;
			}
				
				return true;
			}
			
		};
		
		mListViewScrollListener = new OnScrollListener() {
			int viewHeight = 0;
			int displayItemSize = 0;
			int itemHeight = 0;

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				switch (scrollState) {
				// 当不滚动时
				case OnScrollListener.SCROLL_STATE_IDLE:
					// 判断滚动到底部
					if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
					}
					// 判断滚动到顶部
					if (view.getFirstVisiblePosition() == 0) {
					}
					break;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				if (itemHeight == 0 && view.getChildAt(0) != null) {
					itemHeight = view.getChildAt(0).getHeight();
				}

				if (viewHeight == 0 | view.getHeight() > viewHeight) {
					viewHeight = view.getHeight();

				}

				if (viewHeight != 0 && itemHeight != 0) {
					displayItemSize = viewHeight / itemHeight;
				}

				if (firstVisibleItem == 0) {
					if (view.getChildAt(0) != null) {
						View firstView = view.getChildAt(0);

						if (firstView.getY() > -10&& visibleItemCount < displayItemSize + 2) {
							isListViewFirstItemVisible = true;
						} else {
							//firstItem not full display
							isListViewFirstItemVisible = false;
						}
					}
				} else {
					isListViewFirstItemVisible = false;
				}

			}
		};

		((Main) getActivity()).registerMyOnTouchListener( myOnTouchListener );
	}
	


	private void setInitAdapter() {
		Log.d("yangyang","setInitAdapter ");
		
		if(rAdapterDay == null)
		rAdapterDay = new RankListAdapter(mContext);
		rAdapterDay.refreshListInfo(highestStepList);
		
		if(rAdapterWeek == null)
		rAdapterWeek = new RankListAdapter(mContext);
		rAdapterWeek.refreshListInfo(sevenDalysList);
		
		if(rAdapterMouth == null)
		rAdapterMouth = new RankListAdapter(mContext);
		rAdapterMouth.refreshListInfo(mouthDalysList);
		
		list0 = (ListView)mView0.findViewById(R.id.moRankList);
		list0.setOnScrollListener(mListViewScrollListener);
		list0.setDivider(null);
		list0.setAdapter(rAdapterMouth);

		list1 = (ListView)mView1.findViewById(R.id.moRankList);
		list1.setOnScrollListener(mListViewScrollListener);
		list1.setDivider(null);
		list1.setAdapter(rAdapterDay);

		list2 = (ListView)mView2.findViewById(R.id.moRankList);
		list2.setOnScrollListener(mListViewScrollListener);
		list2.setDivider(null);
		list2.setAdapter(rAdapterWeek);

		list3 = (ListView)mView3.findViewById(R.id.moRankList);
		list3.setOnScrollListener(mListViewScrollListener);
		list3.setDivider(null);
		list3.setAdapter(rAdapterMouth);

		list4 = (ListView)mView4.findViewById(R.id.moRankList);
		list4.setOnScrollListener(mListViewScrollListener);
		list4.setDivider(null);
		list4.setAdapter(rAdapterDay);
		
		List<View> mAllRankViews = new ArrayList<View>();	
		mAllRankViews.add(mView0);
		mAllRankViews.add(mView1);
		mAllRankViews.add(mView2);
		mAllRankViews.add(mView3);
		mAllRankViews.add(mView4);
		currIndex = 2;
		if(rankListPA == null){
			rankListPA = new RankListPageAdatper(mAllRankViews);
		}
		else {
			rankListPA.refresh(mAllRankViews);
		}
		viewPager.setAdapter(rankListPA);
		viewPager.setCurrentItem(currIndex);//set defalut page
		mIndicator.setViewPager(viewPager);
		mIndicator.setOnPageChangeListener(new MyOnPageChangeListener());
	}
	
	private void setShowType(int position) {
		List<RankInfo> myRank = null;

		if (position == 1) {
			myRank = accountHighestData;
		} else if (position == 2) {
			myRank = accountServenData;
		} else if (position == 3) {
			myRank = accountMouthData;
		}

		myRankInfo(myRank);
	}

	void myRankInfo(List<RankInfo> mList) {

		String openid = Tools.getOpenId(mContext);
		if (mList != null && mList.size() > 0 && mList.get(0).getAccountId().equals(openid)) {
			myRankLayout.setVisibility(View.VISIBLE);
			noRankLayout.setVisibility(View.GONE);
			RankInfo info = mList.get(0);
			String rank = info.getRank() + "";
			String name = info.getName();
			String steps = info.getmSteps();
			int headId = info.getmImgId();
			if (headId == 10000) {
				bmp = Tools.convertFileToBitmap("/Running/download/custom");
				myIcon.setImageBitmap(bmp);
			} else if (headId == 1000) {
				bmp = Tools.convertFileToBitmap("/Running/download/logo");
				myIcon.setImageBitmap(bmp);
			} else {
				myIcon.setImageResource(Tools.selectByIndex(headId));
			}
			
			int myRankPosion = info.getRank();

			if(myRankPosion == 1){
				myRankBg.setVisibility(View.VISIBLE);
				myRank.setTextSize(38f);
				mySteps.setTextSize(38f);
				myRank.setTextColor(0xffee3b18);
				mySteps.setTextColor(0xffee3b18);
			}else if(myRankPosion == 2){
				myRankBg.setVisibility(View.GONE);
				myRank.setTextSize(33f);
				mySteps.setTextSize(33f);
				myRank.setTextColor(0xfff69126);
				mySteps.setTextColor(0xfff69126);
			}else if(myRankPosion == 3){
				myRankBg.setVisibility(View.GONE);
				myRank.setTextSize(33f);
				mySteps.setTextSize(33f);
				myRank.setTextColor(0xffe2c922);
				mySteps.setTextColor(0xffe2c922);
			}else {
				myRankBg.setVisibility(View.GONE);
				myRank.setTextSize(23f);
				mySteps.setTextSize(23f);
				myRank.setTextColor(0xff97918f);
				mySteps.setTextColor(0xff97918f);
			}
			
			myRank.setTypeface(mNumberTP);
			//myName.setTypeface(mTextTP, Typeface.BOLD);
			mySteps.setTypeface(mNumberTP);

			myRank.setText(rank);
			myName.setText(name);
			mySteps.setText(steps);

		} else {
			myRankLayout.setVisibility(View.GONE);
			noRankLayout.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.title_share:
			Intent intent = new Intent(getActivity(), ShareRankActivity.class);
			switch (currIndex) {
			case 1:
				intent.putExtra("type", 3);
				if (highestStepList != null) {
					intent.putExtra("RankList", highestStepList.toArray());
				}
				if (accountHighestData != null) {
					intent.putExtra("MyRank", accountHighestData.toArray());
				}
				break;
			case 2:
				intent.putExtra("type", 1);
				if (sevenDalysList != null) {
					intent.putExtra("RankList", sevenDalysList.toArray());
				}
				if (accountServenData != null) {
					intent.putExtra("MyRank", accountServenData.toArray());
				}
				break;
			case 3:
				intent.putExtra("type", 2);
				if (mouthDalysList != null) {
					intent.putExtra("RankList", mouthDalysList.toArray());
				}
				if (accountMouthData != null) {
					intent.putExtra("MyRank", accountMouthData.toArray());
				}
				break;
			}
			getActivity().startActivity(intent);
			break;
		}
	}
	
	private void initViewPagerScroll(ViewPager mViewPager) {
		try {
			Field mScroller = null;
			mScroller = ViewPager.class.getDeclaredField("mScroller");
			mScroller.setAccessible(true);
			ViewPagerScroller scroller = new ViewPagerScroller(mViewPager.getContext());
			mScroller.set(mViewPager, scroller);
		} catch (NoSuchFieldException e) {

		} catch (IllegalArgumentException e) {

		} catch (IllegalAccessException e) {

		}
	}
	
	class RankListPageAdatper extends PagerAdapter {
		private List<View> mViews;
		
		public RankListPageAdatper(List<View> rankViews) {
			// TODO Auto-generated constructor stub
			mViews = rankViews;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(mViews.get(position % mViews.size()));
		}

		@Override
		public int getCount() {
			if (mViews != null) {
				return mViews.size();
			}
			return 0;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mPageTitleContent.get(position % mPageTitleContent.size());
		}
		
		@Override
		public Object instantiateItem(View container, int position) {
			Log.d("yangyang","position =" +position);
			((ViewPager) container).addView(mViews.get(position), 0);
			return mViews.get(position);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return (arg0 == arg1);
		}
		
		public void refresh(List<View> rankViews){
			mViews.clear();
			mViews = rankViews;
			super.notifyDataSetChanged();
		}
	}

	public class MyOnPageChangeListener implements OnPageChangeListener {
		int pageIndex;
		int positions;
		@Override
		public void onPageScrollStateChanged(int arg0) {
			if(arg0==0){
				if (positions != pageIndex) {
					viewPager.setCurrentItem(pageIndex, false);
				}
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int position) {
			pageIndex = position;
			positions = position;
			if (positions == 0) {
				pageIndex = 3;
			} else if (positions == 4) {
				pageIndex = 1;  
			}
			setShowType(pageIndex);
			currIndex = pageIndex;
		}
		
	}

	/*
	 * add for refrash ranklist view
	 * @param: all list date of rank as named
	 * 
	 */
	public void UpdateRankView(List<RankInfo> msevenDalysList,
			List<RankInfo> mmouthDalysList,
			List<RankInfo> mhighestStepList,
			List<RankInfo> maccountServenData,
			List<RankInfo> maccountMouthData,
			List<RankInfo> maccountHighestData){

			if(progress.getVisibility()==View.VISIBLE){
				progress.setVisibility(View.GONE);
			}
			if(titleProgress.getVisibility()==View.VISIBLE){
				titleProgress.setVisibility(View.GONE);
			}
			sevenDalysList = msevenDalysList;
			mouthDalysList = mmouthDalysList;
			highestStepList = mhighestStepList;
			accountServenData = maccountServenData;
			accountMouthData = maccountMouthData;
			accountHighestData = maccountHighestData;
			
			//if twice inie ,we only refash list view .
			setInitAdapter();
			setShowType(currIndex);
	}
	
}
