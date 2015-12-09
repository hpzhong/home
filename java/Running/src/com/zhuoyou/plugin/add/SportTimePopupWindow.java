package com.zhuoyou.plugin.add;


import android.content.Context;
import android.graphics.drawable.PaintDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zhuoyou.plugin.add.TosAdapterView.OnItemSelectedListener;
import com.zhuoyou.plugin.add.TosGallery.OnEndFlingListener;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.view.WheelView;
public class SportTimePopupWindow extends PopupWindow implements OnEndFlingListener{
	private TextView tv_ok;
	private WheelView wView_hour,wView_minute;
	private View view;
	private int sport_hour,sport_minute;
	private int choice_hour,choice_minute;
	private int selectPostion;
	private MyWheelView hourAdapter;
	private MyWheelView minuteAdapter;
	private String[] hourData = new String[25];
	private String[] minuteData = new String[61];
	
	public SportTimePopupWindow(Context context, int hourSelection, int minuteSelection){
		view = LayoutInflater.from(context).inflate(R.layout.add_time,null);
		tv_ok = (TextView) view.findViewById(R.id.tv_ok);
		wView_hour = (WheelView) view.findViewById(R.id.wView_hour);
		wView_minute = (WheelView) view.findViewById(R.id.wView_minute);
		sport_hour = hourSelection;
		sport_minute = minuteSelection;
		choice_hour = hourSelection;
		choice_minute = minuteSelection;
		tv_ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				choice_hour = sport_hour;
				choice_minute = sport_minute;
				dismiss();
			}
		});
		hourData = context.getResources().getStringArray(R.array.hour);
		minuteData = context.getResources().getStringArray(R.array.minute);
		hourAdapter = new MyWheelView(hourData, context);
		minuteAdapter = new MyWheelView(minuteData, context);
		wView_hour.setAdapter(hourAdapter);
		wView_minute.setAdapter(minuteAdapter);
		wView_hour.setSelection(hourSelection);
		wView_minute.setSelection(minuteSelection);
		hourAdapter.setSelectPos(hourSelection);
		minuteAdapter.setSelectPos(minuteSelection);
		
		wView_hour.setOnEndFlingListener(this);
		wView_minute.setOnEndFlingListener(this);
		
		wView_hour.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(TosAdapterView<?> parent, View view,
					int position, long id) {
				selectPostion = wView_hour.getSelectedItemPosition();
				hourAdapter.setSelectPos(selectPostion);
				hourAdapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(TosAdapterView<?> parent) {
				// TODO Auto-generated method stub
			}
		});
		wView_minute.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(TosAdapterView<?> parent, View view,
					int position, long id) {
				selectPostion = wView_minute.getSelectedItemPosition();
				minuteAdapter.setSelectPos(selectPostion);
				minuteAdapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(TosAdapterView<?> parent) {
				// TODO Auto-generated method stub
			}
		});
		this.setContentView(view);
		this.setWidth(LayoutParams.MATCH_PARENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setFocusable(true);
		
		this.setBackgroundDrawable(new PaintDrawable());
		this.setOutsideTouchable(true);
	}

	@Override
	public void onEndFling(TosGallery v) {
		switch (v.getId()) {
		case R.id.wView_hour:
			sport_hour = wView_hour.getSelectedItemPosition();
			break;
		case R.id.wView_minute:
			sport_minute = wView_minute.getSelectedItemPosition();
			break;

		default:
			break;
		}
		
	}
	public int getStartTime(){
		return choice_hour*60 + choice_minute;
	}
	public int getLastTime(){
		return sport_hour*60 + sport_minute;
	}
	
	public void setColor(int color) {
		tv_ok.setTextColor(color);
	}
}
