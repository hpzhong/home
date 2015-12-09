package com.zhuoyou.plugin.add;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.PaintDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zhuoyou.plugin.add.TosAdapterView.OnItemSelectedListener;
import com.zhuoyou.plugin.add.TosGallery.OnEndFlingListener;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.Tools;
import com.zhuoyou.plugin.view.WheelView;

public class DateSelectPopupWindow extends PopupWindow implements OnEndFlingListener {
	private Context mContext;
	private String curr_date = Tools.getDate(0);
	private TextView today, tv_ok;
	private WheelView wView_year, wView_month, wView_date;
	private int selectYear, selectMonth, selectDate;
	private int positonYear, positonMonth, positonDate;
	private String[] mYears;
	private String[] mMonths;
	private String[] mDates;
	private MyWheelView yearAdapter;
	private MyWheelView monthAdapter;
	private MyWheelView dateAdapter;
    private static final String[] MONTH_NAME = { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" };
    private static final int[] DAYS_PER_MONTH = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
	private int selectPostion;
	private Handler mHandler;
	
	public DateSelectPopupWindow(Context context, String date){
		mContext = context;
		mHandler = new Handler();
		View view = LayoutInflater.from(context).inflate(R.layout.date_select, null);
		/*today = (TextView) view.findViewById(R.id.today);
		today.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				String[] temp = curr_date.split("-");
				int curr_year = Integer.valueOf(temp[0]);
				int curr_month = Integer.valueOf(temp[1]);
				int curr_date = Integer.valueOf(temp[2]);
				selectYear = curr_year;
				selectMonth = curr_month;
				selectDate = curr_date;
				dismiss();
			}
		});*/
		tv_ok = (TextView) view.findViewById(R.id.tv_ok);
		tv_ok.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				selectYear = positonYear;
				selectMonth = positonMonth;
				selectDate = positonDate;
				dismiss();
			}
		});
		wView_year = (WheelView) view.findViewById(R.id.wView_year);
		wView_year.setOnEndFlingListener(this);
		wView_year.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(TosAdapterView<?> parent, View view,
					int position, long id) {
				selectPostion = wView_year.getSelectedItemPosition();
				yearAdapter.setSelectPos(selectPostion);
				yearAdapter.notifyDataSetChanged();
			}
			@Override
			public void onNothingSelected(TosAdapterView<?> parent) {
			}			
		});
		wView_month = (WheelView) view.findViewById(R.id.wView_month);
		wView_month.setOnEndFlingListener(this);
		wView_month.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(TosAdapterView<?> parent, View view,
					int position, long id) {
				selectPostion = wView_month.getSelectedItemPosition();
				monthAdapter.setSelectPos(selectPostion);
				monthAdapter.notifyDataSetChanged();
			}
			@Override
			public void onNothingSelected(TosAdapterView<?> parent) {
			}			
		});
		wView_date = (WheelView) view.findViewById(R.id.wView_date);
		wView_date.setOnEndFlingListener(this);
		wView_date.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(TosAdapterView<?> parent, View view,
					int position, long id) {
				selectPostion = wView_date.getSelectedItemPosition();
				dateAdapter.setSelectPos(selectPostion);
				dateAdapter.notifyDataSetChanged();
			}
			@Override
			public void onNothingSelected(TosAdapterView<?> parent) {
			}			
		});
		String[] temp = date.split("-");
		if (Integer.valueOf(temp[0]) < 2014)
			selectYear = positonYear = 2014;
		else
			selectYear = positonYear = Integer.valueOf(temp[0]);
		selectMonth = positonMonth = Integer.valueOf(temp[1]);
		selectDate = positonDate = Integer.valueOf(temp[2]);
		prepareData();
		yearAdapter = new MyWheelView(mYears, context);
		monthAdapter = new MyWheelView(mMonths, context);
		dateAdapter = new MyWheelView(mDates, context);
		wView_year.setAdapter(yearAdapter);
		wView_month.setAdapter(monthAdapter);
		wView_date.setAdapter(dateAdapter);
		wView_year.setSelection(selectYear - 2014);
		wView_month.setSelection(selectMonth - 1);
		wView_date.setSelection(selectDate - 1);
		yearAdapter.setSelectPos(selectYear - 2014);
		monthAdapter.setSelectPos(selectMonth - 1);
		dateAdapter.setSelectPos(selectDate - 1);
		
		this.setContentView(view);
		this.setWidth(LayoutParams.MATCH_PARENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setFocusable(true);
		
		this.setBackgroundDrawable(new PaintDrawable());
		this.setOutsideTouchable(true);
	}

	private void prepareData() {
		String[] temp = curr_date.split("-");
		int curr_year = Integer.valueOf(temp[0]);
		int curr_month = Integer.valueOf(temp[1]);
		int curr_date = Integer.valueOf(temp[2]);
		List<String> list = new ArrayList<String>();
		if (curr_year < 2014) {
			list.add("2014" + mContext.getResources().getString(R.string.pop_year));
		} else {
			for (int i = 2014; i <= curr_year; i++) {
				list.add(String.valueOf(i) + mContext.getResources().getString(R.string.pop_year));
			}
		}
		mYears = list.toArray(new String[list.size()]);
		
        list.clear();
        if (positonYear == curr_year) {
    		for (int i = 0; i < curr_month; i++) {
    			list.add(MONTH_NAME[i] + mContext.getResources().getString(R.string.pop_mouth));
    		}
        } else {
    		for (int i = 0; i < 12; i++) {
    			list.add(MONTH_NAME[i] + mContext.getResources().getString(R.string.pop_mouth));
    		}
        }
		mMonths = list.toArray(new String[list.size()]);
		if (positonMonth > mMonths.length) {
			positonMonth = mMonths.length;
			monthAdapter.setSelectPos(positonMonth - 1);
		}		
		
        int days = DAYS_PER_MONTH[positonMonth - 1];
        if (2 == positonMonth) {
            days = isLeapYear(positonYear) ? 29 : 28;
        }
        list.clear();
        if (positonYear == curr_year && positonMonth == curr_month) {
    		for (int i = 1; i <= curr_date; ++i) {
    			list.add(String.valueOf(i) + mContext.getResources().getString(R.string.pop_date));
    		}
        } else {
    		for (int i = 1; i <= days; ++i) {
    			list.add(String.valueOf(i) + mContext.getResources().getString(R.string.pop_date));
    		}
        }
		mDates = list.toArray(new String[list.size()]);
		if (positonDate > mDates.length) {
			positonDate = mDates.length;
			dateAdapter.setSelectPos(positonDate - 1);
		}		
	}
	
    private boolean isLeapYear(int year) {
        return ((0 == year % 4) && (0 != year % 100) || (0 == year % 400));
    }

	@Override
	public void onEndFling(TosGallery v) {
		int pos = v.getSelectedItemPosition();
		switch (v.getId()) {
		case R.id.wView_year:
			positonYear = pos + 2014;
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					prepareData();
					yearAdapter.setData(mYears);
					yearAdapter.notifyDataSetChanged();
					monthAdapter.setData(mMonths);
					monthAdapter.notifyDataSetChanged();
					dateAdapter.setData(mDates);
					dateAdapter.notifyDataSetChanged();
				}
			}, 1000);
			break;
		case R.id.wView_month:
			positonMonth = pos + 1;
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					prepareData();
					yearAdapter.setData(mYears);
					yearAdapter.notifyDataSetChanged();
					monthAdapter.setData(mMonths);
					monthAdapter.notifyDataSetChanged();
					dateAdapter.setData(mDates);
					dateAdapter.notifyDataSetChanged();
				}
			}, 1000);
			break;
		case R.id.wView_date:
			positonDate = pos + 1;
			break;
		default:
			break;
		}
	}

	public String getStartDate() {
		String month = "" + selectMonth;
		String date = "" + selectDate;
		if (selectMonth < 10)
			month = "0" + selectMonth;
		if (selectDate < 10)
			date = "0" + selectDate;
		return selectYear + "-" + month + "-" + date;
	}
	
	public void setColor(int color) {
		tv_ok.setTextColor(color);
	}
}
