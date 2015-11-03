package com.zhuoyou.plugin.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.Tools;

public class LineNet extends View {
	
	private final int initPosition;
	private final int mHeight;
	private final Paint mPaint;
	private final Path mPathLine;
	private final int mWidth;
	private final int mWindowWidth;
	private final int offsetPx;
	private final int paintWidth;
	
	public LineNet(Context paramContext, int paramInt1, int paramInt2) {
		super(paramContext);
		mHeight = paramInt1;
		mWindowWidth = paramInt2;
		offsetPx = Tools.dip2px(paramContext, 22.0F);
		mWidth = mWindowWidth - Tools.dip2px(paramContext, 24.0F);
		initPosition = mWidth / 2 - offsetPx / 2;
		paintWidth = Tools.dip2px(paramContext, 1.0F);
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
		mPaint.setStrokeWidth(paintWidth);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(0xffffffff);
		mPathLine.moveTo(0.0F, 0.0F);
		mPathLine.lineTo(mWidth, 0.0F);
		paramCanvas.drawPath(mPathLine, mPaint);
		float f1 = initPosition;
		float f2 = mHeight;
		float f3 = initPosition + offsetPx;
		while ((f1 >= 0.0F)) {
			mPathLine.moveTo(f1, 0.0F);
			mPathLine.lineTo(f1, mHeight);
			f1 -= offsetPx;
			paramCanvas.drawPath(mPathLine, mPaint);
			mPathLine.reset();
		}
		while (f3 <= mWidth) {
			mPathLine.moveTo(f3, 0.0F);
			mPathLine.lineTo(f3, mHeight);
			f3 += offsetPx;
			paramCanvas.drawPath(mPathLine, mPaint);
			mPathLine.reset();
		}
		while (f2 >= 0.0F) {
			mPathLine.moveTo(0.0F, f2);
			mPathLine.lineTo(mWidth, f2);
			f2 -= offsetPx;
			paramCanvas.drawPath(mPathLine, mPaint);
			mPathLine.reset();
		}
	}
}
