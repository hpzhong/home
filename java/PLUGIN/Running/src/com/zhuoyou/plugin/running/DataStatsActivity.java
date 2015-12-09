package com.zhuoyou.plugin.running;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.zhuoyou.plugin.view.BarChart;
import com.zhuoyou.plugin.view.HorScrollView;
import com.zhuoyou.plugin.view.HorScrollView.OnScrollListener;
import com.zhuoyou.plugin.view.LineNet;
import com.zhuoyou.plugin.view.PolylineChart;

public class DataStatsActivity extends Activity implements OnScrollListener {

	private Context mContext;
	private Typeface mNewtype;
	private int viewType;
	private int windowWidth;
	private int chartViewHeight;
	private int currentStatsType = 0;
	private DataStatsCenter mDataStatsCenter;
	private TextView text_stats_group1;
	private TextView unit_stats_group1;
	private TextView title_stats_group1;
	private RelativeLayout relativelayout_stats_group2;
	private TextView text_stats_group2;
	private TextView title_stats_group2;
	private RelativeLayout relativelayout_stats_group3;
	private HorScrollView scrollView;
	private RelativeLayout scrollContentLayout;
	private RelativeLayout bg_scrollview_circle;
	private View bg_bottom_line;
	private ImageView circle;
	private RelativeLayout bg_scrollview;
	private View center_bar;
	private TextView text_current_date;
	private RelativeLayout relativelayout_point_window;
	private RadioGroup radioGroup;
	public static final int STEPVIEW = 0;
	public static final int CALORIEVIEW = 1;
	public static final int DAILY = 11;
	public static final int WEEKLY = 22;
	public static final int MONTHLY = 33;
	public static final int VIEWCREATED = 0;
	public static final int SCROLLFINISHED = 1;
	private final ArrayList<Double> dataList = new ArrayList<Double>();
	private ArrayList<Float> XList;
	private ArrayList<Float> YList;
	private int dailyIndex;
	private int weeklyIndex;
	private int monthlyIndex;
	private TextView tv_title;
	private RelativeLayout im_back;
	
	
	private final Handler mHandler = new Handler() {
		public void handleMessage(Message paramMessage) {
			switch(paramMessage.what) {
			case VIEWCREATED:
				if (currentStatsType == DAILY)
					scrollView.scrollTo(Tools.dip2px(mContext, 22 * (dataList.size() - 1)), 0);
				else
					scrollView.scrollTo(Tools.dip2px(mContext, 45 * (dataList.size() - 1)), 0);
				break;
			case SCROLLFINISHED:
				if (currentStatsType == DAILY) {
					int k = Tools.dip2px(mContext, 22 * dailyIndex);
					scrollView.scrollTo(k, 0);
				} else if (currentStatsType == WEEKLY) {
					int j = Tools.dip2px(mContext, 45 * weeklyIndex);
					scrollView.smoothScrollTo(j, 0);
				} else if (currentStatsType == MONTHLY) {
					int i = Tools.dip2px(mContext, 45 * monthlyIndex);
					scrollView.smoothScrollTo(i, 0);
				}
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.data_stats_layout);
		mContext = this;
		mNewtype = Typeface.createFromAsset(getAssets(),"font/akzidenzgrotesklightcond.ttf");		
		tv_title = (TextView) findViewById(R.id.title);
		tv_title.setText(R.string.walk_primary_plan);
		im_back = (RelativeLayout) findViewById(R.id.back);
		im_back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		initView();
	}

	private void initView() {
		viewType = getIntent().getIntExtra("VIEWTYPE", 0);
		Rect localRect = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
		windowWidth = localRect.width();
		chartViewHeight = Tools.dip2px(this, 198.0F);
		mDataStatsCenter = new DataStatsCenter(mContext);
		text_stats_group1 = (TextView) findViewById(R.id.text_stats_group1);
		unit_stats_group1 = (TextView) findViewById(R.id.unit_stats_group1);
		title_stats_group1 = (TextView) findViewById(R.id.title_stats_group1);
		relativelayout_stats_group2 = (RelativeLayout) findViewById(R.id.relativelayout_stats_group2);
		text_stats_group2 = (TextView) findViewById(R.id.text_stats_group2);
		title_stats_group2 = (TextView) findViewById(R.id.title_stats_group2);
		relativelayout_stats_group3 = (RelativeLayout) findViewById(R.id.relativelayout_stats_group3);
		scrollView = (HorScrollView) findViewById(R.id.linechat_scrollview);
		scrollContentLayout = (RelativeLayout) findViewById(R.id.relativelayout_scroll_content);
		bg_scrollview_circle = (RelativeLayout) findViewById(R.id.bg_scrollview_circle);
		bg_bottom_line = (View) findViewById(R.id.bg_bottom_line);
		circle = (ImageView) findViewById(R.id.circle_stats);
		bg_scrollview = (RelativeLayout) findViewById(R.id.bg_scrollview);
		center_bar = (View) findViewById(R.id.center_bar);
		relativelayout_point_window = (RelativeLayout) findViewById(R.id.relativelayout_point_window);
		text_current_date = (TextView) findViewById(R.id.text_current_date);
		radioGroup = (RadioGroup) findViewById(R.id.rg_group_stats);
		setViews();
	}
	
	private void setViews() {
		switch (viewType) {
		case STEPVIEW:
			tv_title.setText("步行统计");
			setStepView();
			break;
		case CALORIEVIEW:
			tv_title.setText("卡路里统计");
			setCalorieView();
			break;
		default:
			break;
		}
	}
	
	private void setStepView() {
		int count = mDataStatsCenter.getDailyStatsesCount();
		text_stats_group1.setText(String.valueOf(mDataStatsCenter.getDailyStatses(0).getSteps()));
		text_stats_group1.setTypeface(mNewtype);
		Object[] arrayOfObject = new Object[1];
		arrayOfObject[0] = Double.valueOf(mDataStatsCenter.getDailyStatses(0).getMeter() / 1000.0D);
		text_stats_group2.setText(String.format("%.1f", arrayOfObject));
		text_stats_group2.setTypeface(mNewtype);
		relativelayout_stats_group3.setVisibility(View.GONE);
		String localDate = mDataStatsCenter.getDailyStatses(0).getDate();
		text_current_date.setText(Tools.dateFormat(localDate, "MM/dd"));
		text_current_date.setTextColor(0xffffffff);
		bg_bottom_line.setBackgroundColor(0xff56C6F1);
		center_bar.setBackgroundColor(0x667ae3ff);
		scrollView.setOnScrollListener(this);
		scrollView.setOnTouchListener(myDailyOnTouchListener);
		radioGroup.setOnCheckedChangeListener(onCheckedChangeListener);
		radioGroup.check(R.id.rb_stats_daily);
	}
	
	private void setBarView() {
		scrollContentLayout.removeAllViewsInLayout();
		BarChart barChart = new BarChart(this, dataList, viewType, chartViewHeight, windowWidth);
		RelativeLayout localRelativeLayout1 = new RelativeLayout(this);
		RelativeLayout.LayoutParams localLayoutParams1 = new RelativeLayout.LayoutParams(barChart.getCanvasWidth(), chartViewHeight);
		localLayoutParams1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		localRelativeLayout1.setLayoutParams(localLayoutParams1);
		localRelativeLayout1.addView(barChart);
		scrollContentLayout.addView(localRelativeLayout1);
		LineNet localLineNet = new LineNet(this, chartViewHeight,windowWidth);
		RelativeLayout localRelativeLayout3 = new RelativeLayout(this);
		RelativeLayout.LayoutParams localLayoutParams2 = new RelativeLayout.LayoutParams(localLineNet.getCanvasWidth(), chartViewHeight);
		localLayoutParams2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		localRelativeLayout3.setLayoutParams(localLayoutParams2);
		localRelativeLayout3.addView(localLineNet);
		bg_scrollview.addView(localRelativeLayout3);
	}
	
	private void setPolylineView() {
		scrollContentLayout.removeAllViewsInLayout();
		PolylineChart polylineChart = new PolylineChart(this, dataList, viewType, chartViewHeight, windowWidth);
		RelativeLayout localRelativeLayout = new RelativeLayout(this);
		RelativeLayout.LayoutParams localLayoutParams = new RelativeLayout.LayoutParams(polylineChart.getCanvasWidth(), chartViewHeight);
		localLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		localRelativeLayout.setLayoutParams(localLayoutParams);
		localRelativeLayout.addView(polylineChart);
		scrollContentLayout.addView(localRelativeLayout);
		XList = new ArrayList<Float>();
		YList = new ArrayList<Float>();
		int i = Tools.dip2px(this, 118.0F);
		int j = Tools.dip2px(this, 158.0F);
		double d1 = ((Double)dataList.get(0)).doubleValue();
		double d2 = 0.0D;
		double d3 = 0.0D;
		float f1 = 0.0F;
		for (int m = 0; m < dataList.size(); m++) {
			if (d2 < ((Double)dataList.get(m)).doubleValue())
				d2 = ((Double)dataList.get(m)).doubleValue();
			if (d1 > ((Double)dataList.get(m)).doubleValue())
				d1 = ((Double)dataList.get(m)).doubleValue();
		}
		if (d2 != d1) {
			d3 = i / (d2 - d1);
			for (int k = 0; k < dataList.size(); k++) {
				float f2 = f1 + Tools.dip2px(this, 45.0F);
				float f3 = j - (float)(d3 * (((Double)dataList.get(k)).doubleValue() - d1)) - Tools.dip2px(this, 12.0F);
				XList.add(Float.valueOf(f2));
				YList.add(Float.valueOf(f3));
				f1 = f2;
			}
		} else {
			XList.add(Float.valueOf(0.0F));
			YList.add(Float.valueOf(j - (float)(d3 * (((Double)dataList.get(0)).doubleValue() - d1)) - Tools.dip2px(this, 12.0F)));
		}
	}
	
	private void setCalorieView() {
		relativelayout_stats_group2.setVisibility(View.GONE);
		relativelayout_stats_group3.setVisibility(View.VISIBLE);
		int i = (int)mDataStatsCenter.getDailyStatses(0).getCalories();
		text_stats_group1.setText(String.valueOf(i));
		text_stats_group1.setTypeface(mNewtype);
		unit_stats_group1.setText("千卡");
		relativelayout_point_window.setBackgroundResource(R.drawable.bg_orange_window);
		String localDate = mDataStatsCenter.getDailyStatses(0).getDate();
		text_current_date.setText(Tools.dateFormat(localDate, "MM/dd"));
		text_current_date.setTextColor(0xffff7e00);
		bg_bottom_line.setBackgroundColor(0xffff7e00);
		center_bar.setBackgroundColor(0x66ffbe00);
		scrollView.setOnScrollListener(this);
		scrollView.setOnTouchListener(myDailyOnTouchListener);
		radioGroup.setOnCheckedChangeListener(onCheckedChangeListener);
		radioGroup.check(R.id.rb_stats_daily);
	}

	private void prepareDataSource() {
		if (mDataStatsCenter == null)
			return;
		if (viewType == STEPVIEW) {
			switch (currentStatsType) {
			case DAILY:
				dataList.clear();
				for (int i1 = 0; i1 < mDataStatsCenter.getDailyStatsesCount(); i1++) {
					dataList.add(Double.valueOf(mDataStatsCenter.getDailyStatses(i1).getSteps()));
				}
				dailyIndex = dataList.size() - 1;
				break;
			case WEEKLY:
				dataList.clear();
				for (int i2 = 0; i2 < mDataStatsCenter.getWeeklyStatsesCount(); i2++) {
					dataList.add(Double.valueOf(mDataStatsCenter.getWeeklyStatses(i2).getSteps()));
				}
				weeklyIndex = dataList.size() - 1;
				break;
			case MONTHLY:
				dataList.clear();
				for (int i3 = 0; i3 < mDataStatsCenter.getMonthlyStatsesCount(); i3++) {
					dataList.add(Double.valueOf(mDataStatsCenter.getMonthlyStatses(i3).getSteps()));
				}
				monthlyIndex = dataList.size() - 1;
				break;
			}
		} else if (viewType == CALORIEVIEW) {
			switch (currentStatsType) {
			case DAILY:
				dataList.clear();
				for (int i1 = 0; i1 < mDataStatsCenter.getDailyStatsesCount(); i1++) {
					dataList.add(Double.valueOf(mDataStatsCenter.getDailyStatses(i1).getCalories()));
				}
				dailyIndex = dataList.size() - 1;
				break;
			case WEEKLY:
				dataList.clear();
				for (int i2 = 0; i2 < mDataStatsCenter.getWeeklyStatsesCount(); i2++) {
					dataList.add(Double.valueOf(mDataStatsCenter.getWeeklyStatses(i2).getCalories()));
				}
				weeklyIndex = dataList.size() - 1;
				break;
			case MONTHLY:
				dataList.clear();
				for (int i3 = 0; i3 < mDataStatsCenter.getMonthlyStatsesCount(); i3++) {
					dataList.add(Double.valueOf(mDataStatsCenter.getMonthlyStatses(i3).getCalories()));
				}
				monthlyIndex = dataList.size() - 1;
				break;
			}
		}
	}
	
	@Override
	public void onScroll(int scrollX) {
		// TODO Auto-generated method stub
		
	}
	
	private final View.OnTouchListener myDailyOnTouchListener = new View.OnTouchListener() {

		private int lastX = 0;
		private final int touchEventId = 4660;
		
		Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == touchEventId) {
					if (lastX == scrollView.getScrollX())
						mHandler.sendEmptyMessage(DataStatsActivity.SCROLLFINISHED);
					
					lastX = scrollView.getScrollX();
					handler.sendMessageDelayed(handler.obtainMessage(touchEventId, scrollView), 5L);
				}
			}
		};
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			lastX = 0;
			int i = scrollView.getScrollX();
			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				if (lastX == i) {
					handler.sendMessageDelayed(handler.obtainMessage(touchEventId, v), 10L);
				}
				break;
			}
			return false;
		}
		
	};
	
	private final RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			DataStatsActivity.this.mHandler.sendEmptyMessageDelayed(DataStatsActivity.VIEWCREATED, 50L);
			if (viewType == STEPVIEW) {
				switch (checkedId) {
				case R.id.rb_stats_daily:
					currentStatsType = DAILY;
					bg_scrollview.setVisibility(View.VISIBLE);
					bg_scrollview_circle.setVisibility(View.GONE);
					title_stats_group1.setText("日总步数");
					title_stats_group2.setText("日总距离");
					prepareDataSource();
					setBarView();
					break;
				case R.id.rb_stats_weekly:
					currentStatsType = WEEKLY;
					bg_scrollview.setVisibility(View.GONE);
					bg_scrollview_circle.setVisibility(View.VISIBLE);
					title_stats_group1.setText("周总步数");
					title_stats_group2.setText("周总距离");
					prepareDataSource();
					setPolylineView();
					circle.setImageResource(R.drawable.circle_stats_blue);
					circle.setY(((Float)YList.get(YList.size() - 1)).floatValue());
					break;
				case R.id.rb_stats_monthly:
					currentStatsType = MONTHLY;
					bg_scrollview.setVisibility(View.GONE);
					bg_scrollview_circle.setVisibility(View.VISIBLE);
					title_stats_group1.setText("月总步数");
					title_stats_group2.setText("月总距离");
					prepareDataSource();
					setPolylineView();
					circle.setImageResource(R.drawable.circle_stats_blue);
					circle.setY(((Float)YList.get(YList.size() - 1)).floatValue());
					break;
				}
			} else if (viewType == CALORIEVIEW) {
				switch (checkedId) {
				case R.id.rb_stats_daily:
					currentStatsType = DAILY;
					bg_scrollview.setVisibility(View.VISIBLE);
					bg_scrollview_circle.setVisibility(View.GONE);
					title_stats_group1.setText("日消耗");
					prepareDataSource();
					setBarView();
					break;
				case R.id.rb_stats_weekly:
					currentStatsType = WEEKLY;
					bg_scrollview.setVisibility(View.GONE);
					bg_scrollview_circle.setVisibility(View.VISIBLE);
					title_stats_group1.setText("周消耗");
					prepareDataSource();
					setPolylineView();
					circle.setImageResource(R.drawable.circle_stats_orange);
					circle.setY(((Float)YList.get(YList.size() - 1)).floatValue());
					break;
				case R.id.rb_stats_monthly:
					currentStatsType = MONTHLY;
					bg_scrollview.setVisibility(View.GONE);
					bg_scrollview_circle.setVisibility(View.VISIBLE);
					title_stats_group1.setText("月消耗");
					prepareDataSource();
					setPolylineView();
					circle.setImageResource(R.drawable.circle_stats_orange);
					circle.setY(((Float)YList.get(YList.size() - 1)).floatValue());
					break;
				}
			}			
		}
		
	};
}
