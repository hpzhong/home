package com.zhuoyou.plugin.running;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;



public class WavesAnimView extends View {
	private Path mPath;
	private Paint mPaint;
	private Paint mPaint1;
	private Paint mPaint2;
	private int width = 720;
	private float height;
	private ValueAnimator mScrollAnimator;
	private int mWavePerimeter;
	private int mScrollPosition = 0;
	private float mStartPoint_x = 0f;
	private float mStartPoint_y = 0f;
	private float mEndPoint_x;
	private float mEndPoint_y;
	
	private float mControlF_x;
	private float mControlF_y;
	private float mControlE_x;
	private float mControlE_y;
	
	public WavesAnimView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mPath = new Path();
		mPaint = new Paint();
		mPaint1 = new Paint();
		mPaint2 = new Paint();
		height = getResources().getDimension(R.dimen.
				waves_anim_view_height);
		mWavePerimeter = (int)getResources().getDimension(R.dimen.
				wave_perimeter);
		mStartPoint_x = 0f;
		mEndPoint_x = 2*mWavePerimeter;
		mEndPoint_y = mStartPoint_y;
		
		this.mScrollAnimator = new ValueAnimator();
		this.mScrollAnimator.setDuration(12000L);
		this.mScrollAnimator.setInterpolator(new LinearInterpolator());
		this.mScrollAnimator
				.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					public void onAnimationUpdate(
							ValueAnimator paramValueAnimator) {
						if(WavesAnimView.this.mScrollPosition <= -mWavePerimeter)
							WavesAnimView.this.mScrollPosition = 0;
						WavesAnimView.this.mScrollPosition = ((Integer) paramValueAnimator
								.getAnimatedValue()).intValue();
						WavesAnimView.this.invalidate();
					}
				});
		int[] arrayOfInt = new int[2];
		arrayOfInt[0] = this.mScrollPosition;
		arrayOfInt[1] = -mWavePerimeter;
		this.mScrollAnimator.setIntValues(arrayOfInt);
		this.mScrollAnimator.setRepeatCount(ValueAnimator.INFINITE);
		this.mScrollAnimator.setRepeatMode(ValueAnimator.RESTART);
		this.mScrollAnimator.start();
	}

	public WavesAnimView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public WavesAnimView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		canvas.save();
		mPath.moveTo(mStartPoint_x, mStartPoint_y);
		mPath.cubicTo(mControlF_x, mControlF_y, mControlE_x, mControlE_y, mWavePerimeter, mStartPoint_y);
		mPath.cubicTo(mControlF_x + mWavePerimeter, mControlF_y, mControlE_x + mWavePerimeter, mControlE_y, 2*mWavePerimeter, mStartPoint_y);
		mPath.lineTo(2*mWavePerimeter, height);
		mPath.lineTo(mStartPoint_x, height);
		mPath.close();
		Matrix matrix = canvas.getMatrix();
		matrix.setTranslate(mScrollPosition, 0.0f);
		canvas.setMatrix(matrix);
		
		mPaint.setColor(getResources().getColor(R.color.water_color1));
		mPaint.setStrokeWidth(15);
		mPaint.setAntiAlias(true);
		mPaint1.setColor(getResources().getColor(R.color.water_color2));
		mPaint1.setStrokeWidth(15);
		mPaint1.setAlpha(70);
		mPaint1.setAntiAlias(true);
		mPaint2.setColor(getResources().getColor(R.color.water_color3));
		mPaint2.setStrokeWidth(15);
		mPaint2.setAlpha(70);
		mPaint2.setAntiAlias(true);
		
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
		
		canvas.drawPath(mPath, mPaint2);
		matrix.setTranslate(mScrollPosition-100, 0.0f);
		canvas.setMatrix(matrix);
		canvas.drawPath(mPath, mPaint1);
		matrix.setTranslate(mScrollPosition-200, 0.0f);
		canvas.setMatrix(matrix);
		canvas.drawPath(mPath, mPaint);
		
		canvas.restore();
		mPath.reset();
	}

	public void setStartPointY(int i){
		Log.v("renjing", "i"+i);
		if(i >= 8){
			mStartPoint_y = getResources().getDimension(R.dimen.
					waves_anim_view_mStartPoint_y8);//high  magin top
			mControlF_x = getResources().getDimension(R.dimen.
					waves_anim_view_mControlF_x8);//width1
			mControlF_y = getResources().getDimension(R.dimen.
					waves_anim_view_mControlF_y8);//low higth
			mControlE_x = getResources().getDimension(R.dimen.
					waves_anim_view_mControlE_x8);//width2
			mControlE_y = getResources().getDimension(R.dimen.
					waves_anim_view_mControlE_y8);//more higth
			invalidate();
		}
		if(i == 0){
			mStartPoint_y = getResources().getDimension(R.dimen.
					waves_anim_view_mStartPoint_y0);
			mControlF_x = getResources().getDimension(R.dimen.
					waves_anim_view_mControlF_x0);
			mControlF_y = getResources().getDimension(R.dimen.
					waves_anim_view_mControlF_y0);
			mControlE_x = getResources().getDimension(R.dimen.
					waves_anim_view_mControlE_x0);
			mControlE_y = getResources().getDimension(R.dimen.
					waves_anim_view_mControlE_y0);
			invalidate();
		}
		if(i == 1){
			mStartPoint_y = getResources().getDimension(R.dimen.
					waves_anim_view_mStartPoint_y1);
			mControlF_x = getResources().getDimension(R.dimen.
					waves_anim_view_mControlF_x1);
			mControlF_y = getResources().getDimension(R.dimen.
					waves_anim_view_mControlF_y1);
			mControlE_x = getResources().getDimension(R.dimen.
					waves_anim_view_mControlE_x1);
			mControlE_y = getResources().getDimension(R.dimen.
					waves_anim_view_mControlE_y1);
			invalidate();
		}
		if(i == 2){
			mStartPoint_y = getResources().getDimension(R.dimen.
					waves_anim_view_mStartPoint_y2);
			mControlF_x = getResources().getDimension(R.dimen.
					waves_anim_view_mControlF_x2);
			mControlF_y = getResources().getDimension(R.dimen.
					waves_anim_view_mControlF_y2);
			mControlE_x = getResources().getDimension(R.dimen.
					waves_anim_view_mControlE_x2);
			mControlE_y = getResources().getDimension(R.dimen.
					waves_anim_view_mControlE_y2);
			invalidate();
		}
		if(i == 3){
			mStartPoint_y = getResources().getDimension(R.dimen.
					waves_anim_view_mStartPoint_y3);
			mControlF_x = getResources().getDimension(R.dimen.
					waves_anim_view_mControlF_x3);
			mControlF_y = getResources().getDimension(R.dimen.
					waves_anim_view_mControlF_y3);
			mControlE_x = getResources().getDimension(R.dimen.
					waves_anim_view_mControlE_x3);
			mControlE_y = getResources().getDimension(R.dimen.
					waves_anim_view_mControlE_y3);
			invalidate();
		}
		if(i == 4){
			mStartPoint_y = getResources().getDimension(R.dimen.
					waves_anim_view_mStartPoint_y4);
			mControlF_x = getResources().getDimension(R.dimen.
					waves_anim_view_mControlF_x4);
			mControlF_y = getResources().getDimension(R.dimen.
					waves_anim_view_mControlF_y4);
			mControlE_x = getResources().getDimension(R.dimen.
					waves_anim_view_mControlE_x4);
			mControlE_y = getResources().getDimension(R.dimen.
					waves_anim_view_mControlE_y4);
			invalidate();
		}
		if(i == 5){
			mStartPoint_y = getResources().getDimension(R.dimen.
					waves_anim_view_mStartPoint_y5);
			mControlF_x = getResources().getDimension(R.dimen.
					waves_anim_view_mControlF_x5);
			mControlF_y = getResources().getDimension(R.dimen.
					waves_anim_view_mControlF_y5);
			mControlE_x = getResources().getDimension(R.dimen.
					waves_anim_view_mControlE_x5);
			mControlE_y = getResources().getDimension(R.dimen.
					waves_anim_view_mControlE_y5);
			invalidate();
		}
		if(i == 6){
			mStartPoint_y = getResources().getDimension(R.dimen.
					waves_anim_view_mStartPoint_y6);
			mControlF_x = getResources().getDimension(R.dimen.
					waves_anim_view_mControlF_x6);
			mControlF_y = getResources().getDimension(R.dimen.
					waves_anim_view_mControlF_y6);
			mControlE_x = getResources().getDimension(R.dimen.
					waves_anim_view_mControlE_x6);
			mControlE_y = getResources().getDimension(R.dimen.
					waves_anim_view_mControlE_y6);
			invalidate();
		}
		if(i == 7){
			mStartPoint_y = getResources().getDimension(R.dimen.
					waves_anim_view_mStartPoint_y7);//wave high
			mControlF_x = getResources().getDimension(R.dimen.
					waves_anim_view_mControlF_x7);//wave crest
			mControlF_y = getResources().getDimension(R.dimen.
					waves_anim_view_mControlF_y7);
			mControlE_x = getResources().getDimension(R.dimen.
					waves_anim_view_mControlE_x7);//wave trough
			mControlE_y = getResources().getDimension(R.dimen.
					waves_anim_view_mControlE_y7);
			invalidate();
		}
		
	}

}
