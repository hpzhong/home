package com.zhuoyou.plugin.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.Tools;

public class PolylineChart extends View {

	private static final int BLUE = 1;
	private static final int ORANGE = 2;
	public static int mPadding;
	private static int mWidth;
	public ArrayList<Float> XList = new ArrayList<Float>();
	public ArrayList<Float> YList = new ArrayList<Float>();
	private final Context context;
	public ArrayList<Double> dataList = new ArrayList<Double>();
	private final int initOffset;
	private int mColorType;
	private final int mHeight;
	private double mIntervalNum;
	private double mMax;
	private final int mMaxHeight;
	private double mMaxValue = 0.0D;
	private double mMinValue = 0.0D;
	private final Paint mPaint;
	private final Path mPathFiller;
	private final Path mPathLine;
	private final int mWindowWidth;
	private final int minPosition;
	private final int offset = 45;

	public PolylineChart(Context paramContext, ArrayList<Double> paramArrayList, int paramInt1, int paramInt2, int paramInt3) {
		super(paramContext);
		context = paramContext;
		dataList = paramArrayList;
		if (paramInt1 == 0) {
			mColorType = BLUE;
		} else if (paramInt1 == 1) {
			mColorType = ORANGE;
		}
		mHeight = paramInt2;
		mWindowWidth = paramInt3;
//		initOffset = mWindowWidth / 2 - Tools.dip2px(context, 12.0F);
		initOffset = 18;
		mWidth = (dataList.size() - 1) * Tools.dip2px(paramContext, 45.0F) + 2 * initOffset;
		mMaxHeight = mHeight - 2 * Tools.dip2px(context, 40.0F);
		minPosition = mHeight -  Tools.dip2px(context, 40.0F);
		mMinValue = ((Double)dataList.get(0)).doubleValue();
		for (int i = 0; i < dataList.size(); i++) {
			if (this.mMaxValue < ((Double)dataList.get(i)).doubleValue())
				this.mMaxValue = ((Double)dataList.get(i)).doubleValue();
			if (this.mMinValue > ((Double)dataList.get(i)).doubleValue())
				this.mMinValue = ((Double)dataList.get(i)).doubleValue();
		}
		if (this.mMaxValue != this.mMinValue)
			mIntervalNum = mMaxHeight / (this.mMaxValue - this.mMinValue);
		else
			mIntervalNum = 0.0D;
		
		mPaint = new Paint(1);
		mPathFiller = new Path();
		mPathLine = new Path();
	}
	
	public int getCanvasWidth() {
		return mWidth;
	}
	
	@Override
	protected void onDraw(Canvas paramCanvas) {
		super.onDraw(paramCanvas);
		
		float f1 = mHeight;
		float f2 = initOffset;
		float f3 = minPosition - (float)((((Double)dataList.get(0)).doubleValue() - mMinValue) * mIntervalNum);
		XList.clear();
		YList.clear();
		XList.add(Float.valueOf(f2));
		YList.add(Float.valueOf(f3));
		mPathFiller.reset();
		mPathLine.reset();
		paramCanvas.drawColor(getResources().getColor(R.color.transparent));
		mPathFiller.moveTo(initOffset, f1);
		mPathFiller.lineTo(f2, minPosition);
		mPathFiller.lineTo(f2, f3);
		mPathLine.moveTo(f2, f3);
		for (int i = 1; i < dataList.size(); i++) {
			float f4 = f2 + Tools.dip2px(context, 20.0F);
			float f5 = minPosition - (float)((((Double)dataList.get(i)).doubleValue() - mMinValue) * mIntervalNum);
			mPathFiller.lineTo(f4, f5);
			mPathLine.lineTo(f4, f5);
			XList.add(Float.valueOf(f4));
			YList.add(Float.valueOf(f5));
			f2 = f4;
		}
		mPathFiller.lineTo(f2, mHeight);
		if (mColorType == BLUE)
			mPaint.setColor(Color.argb(51, 7, 210, 246));
		else if (mColorType == ORANGE)
			mPaint.setColor(Color.argb(51, 255, 190, 0));
		else {
			mPaint.setColor(Color.TRANSPARENT);
		}
		mPaint.setAntiAlias(true);
		mPaint.setPathEffect(null);
		mPaint.setStrokeWidth(1.0F);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		paramCanvas.drawPath(this.mPathFiller, this.mPaint);
		mPathFiller.reset();
		
		
		if (mColorType == BLUE)
			mPaint.setColor(Color.rgb(86, 198, 241));
		else if (mColorType == ORANGE)
			mPaint.setColor(Color.rgb(255, 126, 0));
		else
			mPaint.setColor(Color.rgb(255, 156, 202));
		mPaint.setAntiAlias(true);
		mPaint.setPathEffect(null);
		mPaint.setStrokeWidth(4.0F);
		mPaint.setStyle(Paint.Style.STROKE);
		paramCanvas.drawPath(this.mPathLine, this.mPaint);
		mPathLine.reset();
		
		Paint localPaint = new Paint(1);
		localPaint.setColor(-1);
		localPaint.setAntiAlias(true);
		localPaint.setPathEffect(null);
		localPaint.setStrokeWidth(1.0F);
		localPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		if (mColorType == BLUE)
			mPaint.setColor(Color.rgb(88, 206, 255));
		else if (mColorType == ORANGE)
			mPaint.setColor(Color.rgb(255, 190, 0));
		else
			mPaint.setColor(Color.rgb(243, 55, 139));
		mPaint.setAntiAlias(true);
		mPaint.setPathEffect(null);
		mPaint.setStrokeWidth(4.0F);
		mPaint.setStyle(Paint.Style.STROKE);
		for (int k = 0; k < XList.size(); k++) {
			paramCanvas.drawCircle(((Float)XList.get(k)).floatValue(), ((Float)YList.get(k)).floatValue(), Tools.dip2px(context, 2.0F), localPaint);
			paramCanvas.drawCircle(((Float)XList.get(k)).floatValue(), ((Float)YList.get(k)).floatValue(), Tools.dip2px(context, 3.0F), mPaint);
		}
	}
}
