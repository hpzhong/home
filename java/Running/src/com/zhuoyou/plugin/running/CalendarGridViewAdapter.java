package com.zhuoyou.plugin.running;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.zhuoyou.plugin.view.CalView;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CalendarGridViewAdapter extends BaseAdapter {

    private Calendar calStartDate = Calendar.getInstance();// 当前显示的日历
    private Calendar calSelected = Calendar.getInstance(); // 选择的日历
	private Calendar firstCalendar = Calendar.getInstance();
	private Calendar lastCalendar = Calendar.getInstance();

    public void setSelectedDate(Calendar cal) {
        calSelected = cal;
    }

    private Calendar calToday = Calendar.getInstance(); // 今日

	private String currDate = "";
	private int[] mHDate = null;
	private List<Integer> mDays = null;
    // 根据改变的日期更新日历
    // 填充日历控件用
    private void UpdateStartDateForMonth() {
        calStartDate.set(Calendar.DATE, 1); // 设置成当月第一天
		if (mHDate != null) {
			firstCalendar.setTime(calStartDate.getTime());
	        lastCalendar.setTime(calStartDate.getTime()); 
			firstCalendar.add(Calendar.MONTH, -1);
			lastCalendar.add(Calendar.MONTH, 2);
//		    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//		    int startTime = Integer.parseInt(sdf.format(firstCalendar.getTime()));
//		    int endTime = Integer.parseInt(sdf.format(lastCalendar.getTime()));
		    int year_s = firstCalendar.get(Calendar.YEAR);
		    int month_s = firstCalendar.get(Calendar.MONTH)+1;
		    int day_s = firstCalendar.get(Calendar.DAY_OF_MONTH);
		    int startTime = year_s*10000 +month_s*100 +day_s;
		    int year_e = lastCalendar.get(Calendar.YEAR);
		    int month_e = lastCalendar.get(Calendar.MONTH)+1;
		    int day_e = lastCalendar.get(Calendar.DAY_OF_MONTH);
		    int endTime = year_e*10000 +month_e*100 +day_e;
		    
	        mDays = new ArrayList<Integer>();
			for (int i = 0; i < mHDate.length; i++) {
				if (startTime <= mHDate[i] && mHDate[i] <= endTime) {
					mDays.add(mHDate[i]);
				}else if(endTime < mHDate[i]){
					break;
				}
			}
		}

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

        calStartDate.add(Calendar.DAY_OF_MONTH, -1);// 周日第一位

    }

    ArrayList<java.util.Date> titles;

    private ArrayList<java.util.Date> getDates() {

        UpdateStartDateForMonth();
        ArrayList<java.util.Date> alArrayList = new ArrayList<java.util.Date>();
		for (int i = 1; i <= 42; i++) {
			 Date mdate = calStartDate.getTime();
			 long nd = mdate.getTime()+86400000l*(i-1);//24*60*60*1000;
			 mdate.setTime(nd);
			 alArrayList.add(mdate);
		}
        return alArrayList;
    }

    private Context mContext;
    private Resources resources;
    private DisplayMetrics metric;
    // construct
    public CalendarGridViewAdapter(Context con, Calendar cal,int[] days, String date, DisplayMetrics met) {
        calStartDate = cal;
        mContext = con;
        resources = mContext.getResources();
        currDate = date;
        mHDate = days;
        titles = getDates();
        metric = met;
    }

    public CalendarGridViewAdapter(Context a) {
    	mContext = a;
        resources = mContext.getResources();
    }

    @Override
    public int getCount() {
        return titles.size();
    }

    @Override
    public Object getItem(int position) {
        return titles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
		Date myDate = (Date) getItem(position);
		int i = metric.widthPixels / 7;
		boolean isShowCricle=false;
		int textColor=1;
		boolean isShowBack=false;
		int day = Tools.DataToInteger(myDate);
		int calDay = Tools.DataToInteger(calToday.getTime());
		// 设置背景颜色
		if (equalsDate(Tools.stringToDate(currDate), myDate)) {
			// 选择的
			isShowBack = true;
		}
		if (mHDate.length > 0) {
			if (day == calDay) {
				textColor=1;
			} else if (mHDate[0] <= day && day < calDay) {
				textColor=2;
			} else {
				textColor=3;
			}
		} else {
			if (day == calDay) {
				textColor =1;
			} else {
				textColor =3;
			}
		}

		for (int y = 0; y < mDays.size(); y++) {
			if (day == mDays.get(y)) {
				isShowCricle=true;
				break;
			}
		}
		String text=String.valueOf(myDate.getDate());
		convertView =new CalView(mContext,text,isShowCricle,isShowBack,textColor,i);
		convertView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, i));
		convertView.setId(position + 5000);
		convertView.setTag(myDate);
		
		return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

//need to modefy by zhouzhongbo
    private Boolean equalsDate(Date date1, Date date2) {
    	boolean rs1 = false;
		long time1 = date1.getTime();     
		long time2 = date2.getTime();
		long MS_OF_ONE_DAY = 86400000l;//24*60*60*1000;  
		long l = time1/MS_OF_ONE_DAY;  
		long l2 = time2/MS_OF_ONE_DAY;  
		if(l == l2)
			rs1 = true;
        return rs1;
    }
}
