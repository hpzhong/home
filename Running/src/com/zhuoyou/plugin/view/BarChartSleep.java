package com.zhuoyou.plugin.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.zhuoyou.plugin.running.SleepBean;
import com.zhuoyou.plugin.running.SleepItem;

public class BarChartSleep extends View{
	
	private int WIDTH;
	private int HEIGHT;
	private SleepItem item;
	private int deep_Height;
	private int light_Height;
	private int deepColor = Color.rgb(34, 214, 250);
	private int lightColor = Color.rgb(0, 169, 255);
	
	private Handler handler;
	public BarChartSleep(Context context,SleepItem item,Handler handler) {
		super(context);
		this.item = item;
		this.handler = handler;
	}
	
	@Override
	protected void onDraw(Canvas paramCanvas) {
		super.onDraw(paramCanvas);
		if(item == null) return ;
		WIDTH = getWidth();
		HEIGHT = getHeight();
		
		deep_Height = HEIGHT / 5 * 2;
		light_Height = HEIGHT / 2;
		double perPx = (double)WIDTH/(double)item.getmSleepT();
		Paint paint  = new Paint();
		
		for(int i= 0 ;i<item.getData().size();i++){
			SleepBean mBean = item.getData().get(i);
			Rect mRect = new Rect();
			float left = (float) (perPx * diffSecond(item.getmStartT(),mBean.getStartTime()));
			float top = light_Height;
			float right = (float) (left + perPx * diffSecond(mBean.getStartTime(),mBean.getEndTime()));
			float bottom = HEIGHT;
			
			if(mBean.isDeep()){
				top = deep_Height;
				paint.setColor(deepColor);
			}else{
				paint.setColor(lightColor);
			}
			
			mRect.set((int)left, (int)top, (int)right, (int)bottom);
			Log.i("hello", "Rect:left:"+left + ",top:"+top + ",right:"+right + ",bottom:"+bottom);
			
			
			paint.setTextSize(35);
			if(mRect.contains(touchX,touchY)){
				paint.setColor(Color.rgb(255, 98, 00));
				Message msg = handler.obtainMessage();
				msg.what = 1;
				msg.obj = mBean;
				handler.sendMessage(msg );
			}
			
			paramCanvas.drawRect(mRect,paint);
		}

	}
	
	private int touchX;
	private int touchY;
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touchX = (int)event.getX();
			touchY = (int)event.getY();
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			touchX = -1;
			touchY = -1;
			handler.sendEmptyMessage(2);
			invalidate();
			break;
		}
		return true;
	}
	
	// time结构 eg: 晚上20:00 和早晨08:00之间的差值
	private int diffSecond(String baseTime,String time){
		int baseHour = Integer.valueOf(baseTime.split(":")[0]);
		int baseMin =  Integer.valueOf(baseTime.split(":")[1]);
		int timeHour = Integer.valueOf(time.split(":")[0]);
		int timeMin  = Integer.valueOf(time.split(":")[1]);
		
		int timeSec = timeHour * 60 * 60 + timeMin * 60;
		int baseSec = baseHour * 60 * 60 + baseMin * 60;
		int diffValue = timeSec - baseSec;
		
		if(diffValue >= 0){
			return diffValue;
		}else{
			return diffValue + 24 * 60 * 60;
		}
	}
}
