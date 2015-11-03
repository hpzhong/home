package com.zhuoyou.plugin.running;
/**
 * @author zhouxin@easier
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.zhuoyou.plugin.view.CalView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("SdCardPath") 
public class CalendarView extends PopupWindow implements OnTouchListener {

    /**
     * 日历布局ID
     */
    private static final int CAL_LAYOUT_ID = 55;
    private ViewPager viewPager;
    GestureDetector mGesture = null;

    /**
     * 今天按钮
     */
    private Button mTodayBtn;

    /**
     * 上一个月按钮
     */
    private ImageView mPreMonthImg ,mNextMonthImg;

    /**
     * 下一个月按钮
     */
    private RelativeLayout  mBgDis;

    /**
     * 用于显示今天的日期
     */
    private TextView mDayMessage;

    /**
     * 用于装截日历的View
     */
    private RelativeLayout mCalendarMainLayout;

    // 基本变量
    private Context mContext;
    /**
     * 上一个月View
     */
    private GridView firstGridView;

    /**
     * 当前月View
     */
    private GridView currentGridView;

    /**
     * 下一个月View
     */
    private GridView lastGridView;

    /**
     * 当前界面展示的数据源
     */
    private CalendarGridViewAdapter currentGridAdapter;

    /**
     * 预装载上一个月展示的数据源
     */
    private CalendarGridViewAdapter firstGridAdapter;

    /**
     * 预装截下一个月展示的数据源
     */
    private CalendarGridViewAdapter lastGridAdapter;
    //
    /**
     * 当前视图月
     */
    private int mMonthViewCurrentMonth = 0;

    /**
     * 当前视图年
     */
    private int mMonthViewCurrentYear = 0;

    /**
     * 起始周
     */
    private int iFirstDayOfWeek = Calendar.MONDAY;
 
    private String currDate = null;
    
    /**
     * 当前显示的日历
     */
    private Calendar calStartDate = null;

    /**
     * 选择的日历
     */
    private Calendar calSelected = null;

    /**
     * 今日
     */
    private Calendar calToday = Calendar.getInstance();
    
	private List<String> days = null;
	private int[] mHDate = null;
	private Calendar firstCalendar = null;
	private Calendar currentCalendar = null;
	private Calendar lastCalendar = null;
	private View view;
    private DisplayMetrics metric;
    private int index;
//    private boolean isFirst=true;
//    private boolean first=true;
//	private boolean slideFirst=true;
	public CalendarView(Context con, String date) {
		mContext = con;
		currDate = date;
		
        metric = new DisplayMetrics();
		((Activity) con).getWindowManager().getDefaultDisplay().getMetrics(metric);
		view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.calendar_main, null, false);
		initView(view);
		
		calStartDate = stringToCal(currDate);
		calSelected = stringToCal(currDate);
		days = HomePageFragment.mInstance.getDateList();
		mHDate = getCurrentMouthDate(days);
		Arrays.sort(mHDate);
		updateStartDateForMonth();
		generateContetView(mCalendarMainLayout);
		mGesture = new GestureDetector(mContext, new GestureListener());
		view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		
		this.setContentView(view);
		this.setWidth(LayoutParams.MATCH_PARENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setFocusable(true);
		
		ColorDrawable colorDrawable = new ColorDrawable(Color.argb(0, 0, 0, 0));
		this.setBackgroundDrawable(colorDrawable);
		this.setOutsideTouchable(true);
		}
    
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGesture.onTouchEvent(event);
    }

    /**
     * 用于初始化控件
     */
    private void initView(View v) {
        mTodayBtn = (Button) v.findViewById(R.id.today_btn);
        mDayMessage = (TextView) v.findViewById(R.id.day_message);
        mCalendarMainLayout = (RelativeLayout) v.findViewById(R.id.calendar_main);
        mPreMonthImg = (ImageView) v.findViewById(R.id.left_img);
        mNextMonthImg = (ImageView) v.findViewById(R.id.right_img);
        mTodayBtn.setOnClickListener(onTodayClickListener);
        mPreMonthImg.setOnClickListener(onPreMonthClickListener);
        mNextMonthImg.setOnClickListener(onNextMonthClickListener);
        mBgDis = (RelativeLayout) v.findViewById(R.id.bg_dis);
        mBgDis.setOnClickListener(onDisClickListener);
    }
    
    private View.OnClickListener onDisClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
			dismiss();
        }
    };

    /**
     * 用于加载到当前的日期的事件
     */
    private View.OnClickListener onTodayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (HomePageFragment.mInstance != null) {
				HomePageFragment.mInstance.onViewPagerCurrent();
			}
			dismiss();
        }
    };


    /**
     * 用于加载上一个月日期的事件
     */
    private View.OnClickListener onPreMonthClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        	setPrevViewItem();
			CreateGirdView();
        }
    };

    /**
     * 用于加载下一个月日期的事件
     */
    private View.OnClickListener onNextMonthClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        	setNextViewItem();
			CreateGirdView();
        }
    };
    
    /**
     * 主要用于生成发前展示的日历View
     *
     * @param layout 将要用于去加载的布局
     */
    private void generateContetView(RelativeLayout layout) {
        int i = metric.widthPixels / 7;
        // 创建一个垂直的线性布局（整体内容）
		viewPager = new ViewPager(mContext);
		viewPager.setId(CAL_LAYOUT_ID);
        calStartDate = getCalendarStartDate();
    	CreateGirdView();
		RelativeLayout.LayoutParams params_cal = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, i * 6 + 5);
        layout.addView(viewPager, params_cal);
        
    }

    /**
     * 用于创建当前将要用于展示的View
     */
    private void CreateGirdView() {
        mDayMessage.setText(generateTitle());
    	
		firstCalendar = Calendar.getInstance();
		currentCalendar = Calendar.getInstance();
		lastCalendar = Calendar.getInstance();
    	
        firstCalendar.setTime(calStartDate.getTime());
        currentCalendar.setTime(calStartDate.getTime());
        lastCalendar.setTime(calStartDate.getTime()); 

		firstGridView = new CalendarGridView(mContext);
		firstCalendar.add(Calendar.MONTH, -1);
		firstCalendar.get(Calendar.MONTH);

		currentGridView = new CalendarGridView(mContext);

		lastGridView = new CalendarGridView(mContext);
		lastCalendar.add(Calendar.MONTH, 1);
		
		setInitAdapter();
			
		if (viewPager.getChildCount() != 0) {
			viewPager.removeAllViews();
		}

		List<View> views = new ArrayList<View>();
		views.add(firstGridView);
		views.add(currentGridView);
		views.add(lastGridView);
		viewPager.setAdapter(new MyViewPagerAdapter(views));
		viewPager.setCurrentItem(1);
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		
		
    }
    
	public void setInitAdapter() {
		
		firstGridAdapter = new CalendarGridViewAdapter(mContext, firstCalendar, mHDate, currDate, metric);
		firstGridView.setAdapter(firstGridAdapter);
		firstGridView.setId(CAL_LAYOUT_ID);
		currentGridAdapter = new CalendarGridViewAdapter(mContext,currentCalendar, mHDate, currDate, metric);
		currentGridView.setAdapter(currentGridAdapter);
		currentGridView.setId(CAL_LAYOUT_ID);
		lastGridAdapter = new CalendarGridViewAdapter(mContext, lastCalendar,mHDate, currDate, metric);
		lastGridView.setAdapter(lastGridAdapter);
		lastGridView.setId(CAL_LAYOUT_ID);
		
		currentGridView.setOnTouchListener(this);
		firstGridView.setOnTouchListener(this);
		lastGridView.setOnTouchListener(this);
	}

    /**
     * 上一个月
     */
    private void setPrevViewItem() {
        mMonthViewCurrentMonth--;// 当前选择月--
        // 如果当前月为负数的话显示上一年
        if (mMonthViewCurrentMonth == -1) {
            mMonthViewCurrentMonth = 11;
            mMonthViewCurrentYear--;
        }
        calStartDate.set(Calendar.DAY_OF_MONTH, 1); // 设置日为当月1日
        calStartDate.set(Calendar.MONTH, mMonthViewCurrentMonth); // 设置月
        calStartDate.set(Calendar.YEAR, mMonthViewCurrentYear); // 设置年
    }

    /**
     * 下一个月
     */
    private void setNextViewItem() {
        mMonthViewCurrentMonth++;
        if (mMonthViewCurrentMonth == 12) {
            mMonthViewCurrentMonth = 0;
            mMonthViewCurrentYear++;
        }
        calStartDate.set(Calendar.DAY_OF_MONTH, 1);
        calStartDate.set(Calendar.MONTH, mMonthViewCurrentMonth);
        calStartDate.set(Calendar.YEAR, mMonthViewCurrentYear);
        mDayMessage.setText(generateTitle());
    }

    /**
     * 根据改变的日期更新日历
     * 填充日历控件用
     */
    private void updateStartDateForMonth() {
        calStartDate.set(Calendar.DATE, 1); // 设置成当月第一天
        mMonthViewCurrentMonth = calStartDate.get(Calendar.MONTH);// 得到当前日历显示的月
        mMonthViewCurrentYear = calStartDate.get(Calendar.YEAR);// 得到当前日历显示的年
        mDayMessage.setText(generateTitle());
        // 星期一是2 星期天是1 填充剩余天数
        int iDay = 0;
        int iFirstDayOfWeek = Calendar.MONDAY;
        int iStartDay = iFirstDayOfWeek;
        if (iStartDay == Calendar.MONDAY) {
            iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
            if (iDay < 0)
                iDay = 6;
        }
        if (iStartDay == Calendar.SUNDAY) {
            iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
            if (iDay < 0)
                iDay = 6;
        }
        calStartDate.add(Calendar.DAY_OF_WEEK, -iDay);
    }

    /**
     * 用于获取当前显示月份的时间
     *
     * @return 当前显示月份的时间
     */
    private Calendar getCalendarStartDate() {
        calToday.setTimeInMillis(System.currentTimeMillis());
        calToday.setFirstDayOfWeek(iFirstDayOfWeek);

        if (calSelected.getTimeInMillis() == 0) {
            calStartDate.setTimeInMillis(System.currentTimeMillis());
            calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
        } else {
            calStartDate.setTimeInMillis(calSelected.getTimeInMillis());
            calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
        }

        return calStartDate;
    }

    class GestureListener extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            //得到当前选中的是第几个单元格
            int pos = currentGridView.pointToPosition((int) e.getX(), (int) e.getY());
            CalView txtDay = (CalView) currentGridView.findViewById(pos + 5000);
            if (txtDay != null) {
                if (txtDay.getTag() != null) {
                    Date date = (Date) txtDay.getTag();
                    calSelected.setTime(date);
                    int calSeletedDay = Tools.DataToInteger(date);
                    int calSelectIndex = getCurrentDate() - calSeletedDay;
					if (mHDate.length > 0 && mHDate[0] <= calSeletedDay  && calSelectIndex >= 0) {
						currentGridAdapter.setSelectedDate(calSelected);
						currentGridAdapter.notifyDataSetChanged();
	                    index =  Tools.getDayCount(mHDate[0]+"", calSeletedDay+"", "yyyyMMdd") - 1;
	                    if (HomePageFragment.mInstance != null) {
	        				HomePageFragment.mInstance.onViewPagerIndex(index);
	        			}
        				dismiss();
					}
                }
            }
            return false;
        }
    }
    
    int getIndex(){
    	return index;
    }
    
	class MyViewPagerAdapter extends PagerAdapter {
		private List<View> mListViews;
		
		public MyViewPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mListViews.get(position));
		}

		/**
		 * Create the location of the specified page views
		 */
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mListViews.get(position), 0);
//			if(isFirst){
//				currentGridAdapter = new CalendarGridViewAdapter(mContext,currentCalendar, mHDate, currDate, metric);
//				currentGridView.setAdapter(currentGridAdapter);
//				currentGridView.setId(CAL_LAYOUT_ID);
//				currentGridView.setOnTouchListener(CalendarView.this);
//				isFirst=false;
//			}
//			CalendarGridViewAdapter card = new CalendarGridViewAdapter(mContext);
//			card.setSelectedDate(calSelected);
//			card.notifyDataSetChanged();
			return mListViews.get(position);
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}
		

	}
	
	public class MyOnPageChangeListener implements OnPageChangeListener {
		int positionState = -1;
		public void onPageScrollStateChanged(int state) {
//			if(slideFirst){
//				if(state==1){
//					CreateGirdView();
//					slideFirst=false;
//				}
//			}else{
				if (state == 0) {
					if (positionState == 0) {
						setPrevViewItem();
						CreateGirdView();
					}
					if (positionState == 2) {
						setNextViewItem();
						CreateGirdView();
					}
					
				}
//			}
		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		public void onPageSelected(int position) {
			positionState = position;
		}
	}
	
	private String leftPad_Tow_Zero(int str) {
		java.text.DecimalFormat format = new java.text.DecimalFormat("00");
		return format.format(str);
	}
	
    private int[] getCurrentMouthDate(List<String> days) {
		int[] mDays = new int[days.size()];
		for(int i=0;i<days.size();i++){
			mDays[i] = Tools.DataToInteger(Tools.stringToDate(days.get(i)));
		}
		return mDays;
	}
    
    public static int getCurrentDate() {
    	Calendar mcalendar = Calendar.getInstance();
	    int year = mcalendar.get(Calendar.YEAR);
	    int month = mcalendar.get(Calendar.MONTH)+1;
	    int day = mcalendar.get(Calendar.DAY_OF_MONTH);
	    int time = year*10000 +month*100 +day;
		return time;
	}
    
	public static Calendar stringToCal(String str){
		Calendar calendar = Calendar.getInstance();
	    String a[] = str.split("-");
    	int year = Integer.valueOf(a[0]);
    	int month = Integer.valueOf(a[1]) - 1;
    	int day = Integer.valueOf(a[2]);
    	calendar.set(year, month, day);
		return calendar;
	}
	
	private String translateToEn(int month) {
		String monthEn = "";
		switch(month) {
		case 1:
			monthEn = "January";
			break;
		case 2:
			monthEn = "February";
			break;
		case 3:
			monthEn = "March";
			break;
		case 4:
			monthEn = "April";
			break;
		case 5:
			monthEn = "May";
			break;
		case 6:
			monthEn = "June";
			break;
		case 7:
			monthEn = "July";
			break;
		case 8:
			monthEn = "August";
			break;
		case 9:
			monthEn = "September";
			break;
		case 10:
			monthEn = "October";
			break;
		case 11:
			monthEn = "November";
			break;
		case 12:
			monthEn = "December";
			break;
		}
		return monthEn;
	}
	
	private String generateTitle() {
		String title = "";
        String language = mContext.getResources().getConfiguration().locale.getLanguage();
        if (language.endsWith("en")) {
        	title = translateToEn(calStartDate.get(Calendar.MONTH) + 1) + " " + calStartDate.get(Calendar.YEAR);
        } else {
        	title = calStartDate.get(Calendar.YEAR)
                    + mContext.getResources().getString(R.string.year)
                    + leftPad_Tow_Zero(calStartDate
                    .get(Calendar.MONTH) + 1) + mContext.getResources().getString(R.string.mouth);
        }
		return title;
	}
}
