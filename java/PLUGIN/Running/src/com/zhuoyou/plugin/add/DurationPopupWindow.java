package com.zhuoyou.plugin.add;


import android.content.Context;
import android.graphics.drawable.PaintDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zhuoyou.plugin.add.AddSports.MyWheelView;
import com.zhuoyou.plugin.add.TosAdapterView.OnItemSelectedListener;
import com.zhuoyou.plugin.add.TosGallery.OnEndFlingListener;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.view.WheelView;
public class DurationPopupWindow extends PopupWindow implements OnEndFlingListener{
	private TextView tv_ok;
	private WheelView wView_hour,wView_minute;
	private View view;
	private int sport_hour,sport_minute;
	private int choice_hour,choice_minute;
	private int selectPostion;
	public DurationPopupWindow(Context context,final MyWheelView hourAdapter,final MyWheelView minuteAdapter
			,int hourSelection,int minuteSelection){
		view = LayoutInflater.from(context).inflate(R.layout.add_duration,null);
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
		wView_hour.setAdapter(hourAdapter);
		wView_minute.setAdapter(minuteAdapter);
		wView_hour.setSelection(hourSelection);
		wView_minute.setSelection(minuteSelection);
		
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

	public int getLastTime(){
		return choice_hour*60 + choice_minute;
	}
}
