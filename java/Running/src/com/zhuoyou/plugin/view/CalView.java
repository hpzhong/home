package com.zhuoyou.plugin.view;

import com.zhuoyou.plugin.running.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

public class CalView extends View{
	
	private Context mContext;
	
	private boolean isShowCircle;
	
	private String text;
	
	private boolean isShowBack;
	
	private int textColor;
	
	private int height ;
	private Paint paint;
	
	public CalView(Context context){
		super(context);
	}
	public CalView(Context context,String text, boolean isShowCircle ,boolean isShowBack,int textColor,int height) {
		super(context);
		this.mContext=context;
		paint =new Paint();
		this.isShowBack =isShowBack;
		this.isShowCircle=isShowCircle;
		this.text=text;
		this.textColor=textColor;
		this.height=height;
		
	}
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		onDrawBigBack(canvas);
		onDrawText(canvas);
		onDrawCircle(canvas);
	}
	
	//画正方形背景
	public void onDrawBigBack(Canvas canvas){
		if(isShowBack){
			paint.setColor(Color.parseColor("#dff7eb"));
		}else{
			paint.setColor(Color.parseColor("#ffffffff"));
		}
		Rect rect= new Rect(0,0,height,height);
		canvas.drawRect(rect, paint);
	}
	
	public void onDrawText(Canvas canvas){
		Log.i("zoujian",height+"");
		
		float textSize = (float) (height / 3.5);
		paint.setTextSize(textSize);
		float width=paint.measureText(text);
		Bitmap bitmap=BitmapFactory.decodeResource(mContext.getResources(),R.drawable.cal_today_bg);
		if(textColor == 1 ){
			canvas.drawBitmap(bitmap, height/4+2, height/4, paint);
			paint.setColor(Color.parseColor("#FFFFFFFF"));
		}else if(textColor == 2){
			paint.setColor(Color.parseColor("#616b75"));
		}else if(textColor ==3){
			paint.setColor(Color.parseColor("#c7cacd"));
		}
		canvas.drawText(text, height/2-width/2, height/2+5, paint);
		
	}
	
	public void onDrawCircle(Canvas canvas){
		float width=paint.measureText(text);
		Bitmap cricleBitmap=BitmapFactory.decodeResource(mContext.getResources(), R.drawable.mark_bg);
		if(isShowCircle){
			canvas.drawBitmap(cricleBitmap, height/2-width/3, height - (height/3) + 5, paint);
		}
	}
	
}
