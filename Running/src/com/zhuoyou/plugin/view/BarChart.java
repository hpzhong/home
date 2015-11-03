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

public class BarChart extends View {

	private static final int BLUE = 1;
	private static final int ORANGE = 2;
	public ArrayList<Double> dataList = new ArrayList<Double>();
	private final int height;
	private final int initOffset;
	private int mColorType;
	private final double mIntervalNum;
	private final int mMaxHeight;
	private double mMaxValue = 0.0D;
	private final Paint mPaint;
	private final Path mPathLine;
	private final int mWidth;
	private final int mWindowWidth;
	private final int offsetPx;
	
	public BarChart(Context paramContext, ArrayList<Double> paramArrayList, int paramInt1, int paramInt2, int paramInt3) {
		super(paramContext);
		dataList = paramArrayList;
		if (paramInt1 == 0) {
			mColorType = BLUE;
		} else if (paramInt1 == 1) {
			mColorType = ORANGE;
		}
		height = paramInt2;
		mWindowWidth = paramInt3;
		offsetPx = Tools.dip2px(getContext(), 22.0F);
		initOffset = mWindowWidth / 2 - Tools.dip2px(paramContext, 23.0F);
		mWidth = dataList.size() * Tools.dip2px(paramContext, 22.0F) + 2 * initOffset;
		mMaxHeight = height - Tools.dip2px(paramContext, 22.0F);
		for (int i = 0; i < dataList.size(); i++) {
			if (mMaxValue < ((Double)dataList.get(i)).doubleValue())
				mMaxValue = ((Double)dataList.get(i)).doubleValue();
		}
		mIntervalNum = mMaxHeight / mMaxValue;
		mPaint = new Paint(1);
		mPathLine = new Path();
	}
	
	public int getCanvasWidth() {
		return mWidth;
	}
	
	@Override
	protected void onDraw(Canvas paramCanvas) {
		super.onDraw(paramCanvas);
		
		paramCanvas.drawColor(getResources().getColor(R.color.transparent));
		mPathLine.reset();
		mPaint.setPathEffect(null);
		mPaint.setStrokeWidth(2.0F);
		mPaint.setStyle(Paint.Style.FILL);
		if (mColorType == BLUE)
			mPaint.setColor(Color.rgb(122, 227, 255));
		else if (mColorType == ORANGE)
			mPaint.setColor(Color.rgb(255, 190, 0));
		float f1 = initOffset;
		float f2 = height;
		mPathLine.moveTo(f1, f2);
		for (int i = 0; i < dataList.size(); i++) {
			float f3 = height - (float)(((Double)dataList.get(i)).doubleValue() * mIntervalNum);
			mPathLine.lineTo(f1, f3);
			float f4 = f1 + offsetPx;
			mPathLine.lineTo(f4, f3);
			f1 = f4;
			mPathLine.lineTo(f1, height);
			f2 = height;
			paramCanvas.drawPath(mPathLine, mPaint);
			mPathLine.reset();
			mPathLine.moveTo(f1, f2);
		}
		mPathLine.moveTo(f1 + initOffset, f2);
		paramCanvas.drawPath(mPathLine, mPaint);
	}
	
	
}
