package com.zhuoyou.plugin.running;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;



public class ColorLineView extends View {
	private int mCount;
	//*/add by tyd renjing 2014-12-03
	private float mStartx;
	private float mStarty;
	private float mStopx;
	private float mSpace;
	//*/
	private Paint mBackPaint;
	private Paint mPaint1;
	private Paint mPaint2;
	private Paint mPaint3;
	private Paint mPaint4;
	private Paint mPaint5;
	private Paint mPaint6;
	private Paint mPaint7;
	private Paint mPaint8;
	public ColorLineView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mStartx = getResources().getDimension(R.dimen.
				color_line_view_startx);
		mStarty = getResources().getDimension(R.dimen.
				color_line_view_starty);
		mStopx = getResources().getDimension(R.dimen.
				color_line_view_stopx);
		mSpace = getResources().getDimension(R.dimen.
				color_line_view_space);
		
		mBackPaint = new Paint();
		mPaint1 = new Paint();
		mPaint2 = new Paint();
		mPaint3 = new Paint();
		mPaint4 = new Paint();
		mPaint5 = new Paint();
		mPaint6 = new Paint();
		mPaint7 = new Paint();
		mPaint8 = new Paint();
		
		mBackPaint.setColor(getResources().getColor(R.color.line_background));
		mBackPaint.setStrokeWidth(7);
		mPaint1.setColor(getResources().getColor(R.color.line_color1));
		mPaint1.setStrokeWidth(7);
		mPaint2.setColor(getResources().getColor(R.color.line_color1));
		mPaint2.setStrokeWidth(7);
		mPaint3.setColor(getResources().getColor(R.color.line_color1));
		mPaint3.setStrokeWidth(7);
		mPaint4.setColor(getResources().getColor(R.color.line_color1));
		mPaint4.setStrokeWidth(7);
		mPaint5.setColor(getResources().getColor(R.color.line_color1));
		mPaint5.setStrokeWidth(7);
		mPaint6.setColor(getResources().getColor(R.color.line_color1));
		mPaint6.setStrokeWidth(7);
		mPaint7.setColor(getResources().getColor(R.color.line_color1));
		mPaint7.setStrokeWidth(7);
		mPaint8.setColor(getResources().getColor(R.color.line_color1));
		mPaint8.setStrokeWidth(7);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		canvas.drawLine(mStartx, mStarty, mStopx, mStarty, mBackPaint);
		if(mCount == 0){
			return;
		}
		if(mCount == 1){
			drawColorLine1(canvas);
		}
		else if(mCount == 2){
			drawColorLine2(canvas);
		}
		else if(mCount == 3){
			drawColorLine3(canvas);
		}
		else if(mCount == 4){
			drawColorLine4(canvas);
		}
		else if(mCount == 5){
			drawColorLine5(canvas);
		}
		else if(mCount == 6){
			drawColorLine6(canvas);
		}
		else if(mCount == 7){
			drawColorLine7(canvas);
		}
		else if(mCount == 8){
			drawColorLine8(canvas);
		}else{
			drawColorLine8(canvas);
		}
		
	}
	
	public void drawColorLine1(Canvas canvas){
		canvas.drawLine(mStartx, mStarty, mStartx+mSpace, mStarty, mPaint1);
		invalidate();
	}
	public void drawColorLine2(Canvas canvas){
		canvas.drawLine(mStartx, mStarty, mStartx+mSpace, mStarty, mPaint1);
		canvas.drawLine(mStartx+mSpace, mStarty, mStartx+2*mSpace, mStarty, mPaint2);
		invalidate();
	}
	public void drawColorLine3(Canvas canvas){
		canvas.drawLine(mStartx, mStarty, mStartx+mSpace, mStarty, mPaint1);
		canvas.drawLine(mStartx+mSpace, mStarty, mStartx+2*mSpace, mStarty, mPaint2);
		canvas.drawLine(mStartx+2*mSpace, mStarty, mStartx+3*mSpace, mStarty, mPaint3);
		invalidate();
	}
	public void drawColorLine4(Canvas canvas){
		canvas.drawLine(mStartx, mStarty, mStartx+mSpace, mStarty, mPaint1);
		canvas.drawLine(mStartx+mSpace, mStarty, mStartx+2*mSpace, mStarty, mPaint2);
		canvas.drawLine(mStartx+2*mSpace, mStarty, mStartx+3*mSpace, mStarty, mPaint3);
		canvas.drawLine(mStartx+3*mSpace, mStarty, mStartx+4*mSpace, mStarty, mPaint4);
		invalidate();
	}
	public void drawColorLine5(Canvas canvas){
		canvas.drawLine(mStartx, mStarty, mStartx+mSpace, mStarty, mPaint1);
		canvas.drawLine(mStartx+mSpace, mStarty, mStartx+2*mSpace, mStarty, mPaint2);
		canvas.drawLine(mStartx+2*mSpace, mStarty, mStartx+3*mSpace, mStarty, mPaint3);
		canvas.drawLine(mStartx+3*mSpace, mStarty, mStartx+4*mSpace, mStarty, mPaint4);
		canvas.drawLine(mStartx+4*mSpace, mStarty, mStartx+5*mSpace, mStarty, mPaint5);
		invalidate();
	}
	public void drawColorLine6(Canvas canvas){
		canvas.drawLine(mStartx, mStarty, mStartx+mSpace, mStarty, mPaint1);
		canvas.drawLine(mStartx+mSpace, mStarty, mStartx+2*mSpace, mStarty, mPaint2);
		canvas.drawLine(mStartx+2*mSpace, mStarty, mStartx+3*mSpace, mStarty, mPaint3);
		canvas.drawLine(mStartx+3*mSpace, mStarty, mStartx+4*mSpace, mStarty, mPaint4);
		canvas.drawLine(mStartx+4*mSpace, mStarty, mStartx+5*mSpace, mStarty, mPaint5);
		canvas.drawLine(mStartx+5*mSpace, mStarty, mStartx+6*mSpace, mStarty, mPaint6);
		invalidate();
	}
	public void drawColorLine7(Canvas canvas){
		canvas.drawLine(mStartx, mStarty, mStartx+mSpace, mStarty, mPaint1);
		canvas.drawLine(mStartx+mSpace, mStarty, mStartx+2*mSpace, mStarty, mPaint2);
		canvas.drawLine(mStartx+2*mSpace, mStarty, mStartx+3*mSpace, mStarty, mPaint3);
		canvas.drawLine(mStartx+3*mSpace, mStarty, mStartx+4*mSpace, mStarty, mPaint4);
		canvas.drawLine(mStartx+4*mSpace, mStarty, mStartx+5*mSpace, mStarty, mPaint5);
		canvas.drawLine(mStartx+5*mSpace, mStarty, mStartx+6*mSpace, mStarty, mPaint6);
		canvas.drawLine(mStartx+6*mSpace, mStarty, mStartx+7*mSpace, mStarty, mPaint7);
		invalidate();
	}
	public void drawColorLine8(Canvas canvas){
		canvas.drawLine(mStartx, mStarty, mStartx+mSpace, mStarty, mPaint1);
		canvas.drawLine(mStartx+mSpace, mStarty, mStartx+2*mSpace, mStarty, mPaint2);
		canvas.drawLine(mStartx+2*mSpace, mStarty, mStartx+3*mSpace, mStarty, mPaint3);
		canvas.drawLine(mStartx+3*mSpace, mStarty, mStartx+4*mSpace, mStarty, mPaint4);
		canvas.drawLine(mStartx+4*mSpace, mStarty, mStartx+5*mSpace, mStarty, mPaint5);
		canvas.drawLine(mStartx+5*mSpace, mStarty, mStartx+6*mSpace, mStarty, mPaint6);
		canvas.drawLine(mStartx+6*mSpace, mStarty, mStartx+7*mSpace, mStarty, mPaint7);
		canvas.drawLine(mStartx+7*mSpace, mStarty, mStartx+8*mSpace, mStarty, mPaint8);
		invalidate();
	}
	
	public void setNumber(int i){
		mCount = i;
		invalidate();
	}

}
