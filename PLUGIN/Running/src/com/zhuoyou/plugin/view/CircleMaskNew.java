package com.zhuoyou.plugin.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.zhuoyou.plugin.running.Tools;

public class CircleMaskNew extends View implements AnimatorUpdateListener {
	public static float CIRCLE_MASK_WIDTH;
	public static float SMALL_CIRCLE_MASK_WIDTH;
	public static String TAG = "gchk";
	private Point centerPoint_;
	int cricleWdithPx_ = 0;
	private int currentRadian = 0;
	public CircleMaskShapeHolder currentRadianSH = new CircleMaskShapeHolder(80);
	private ObjectAnimator objectAnimator_;
	private Paint paint_;
	private Path path_;
	float radius = 0.0F;
	private RectF rectFInside_;
	private RectF rectFOutside_;
	private RectF rectFSmallDown_;
	private RectF rectFSmallUp_;
	int smallCircleWidthPx_ = 0;
	float smallRadius = 0.0F;
	
	static {
		CIRCLE_MASK_WIDTH = 230.0F;
		SMALL_CIRCLE_MASK_WIDTH = 9.5F;
	}
	
	public CircleMaskNew(Context context) {
		this(context, null, 0);
	}
	
	public CircleMaskNew(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CircleMaskNew(Context paramContext, AttributeSet attrs, int defStyle) {
		super(paramContext, attrs, defStyle);
		
		this.cricleWdithPx_ = Tools.dip2px(paramContext, CIRCLE_MASK_WIDTH);
		this.smallCircleWidthPx_ = Tools.dip2px(paramContext, SMALL_CIRCLE_MASK_WIDTH);
		this.centerPoint_ = new Point(this.cricleWdithPx_ / 2, this.cricleWdithPx_ / 2);
		this.path_ = new Path();
		this.paint_ = new Paint();
		this.paint_.setAntiAlias(true);
		this.paint_.setColor(0xFFDBDBDB);
		this.radius = (this.cricleWdithPx_ / 2);
		this.rectFOutside_ = new RectF(this.centerPoint_.x - this.radius, this.centerPoint_.y - this.radius, this.centerPoint_.x + this.radius, this.centerPoint_.y + this.radius);
		this.rectFInside_ = new RectF(this.centerPoint_.x - this.radius + this.smallCircleWidthPx_, this.centerPoint_.y - this.radius + this.smallCircleWidthPx_, this.centerPoint_.x + this.radius
				- this.smallCircleWidthPx_, this.centerPoint_.y + this.radius - this.smallCircleWidthPx_);
		this.smallRadius = (this.smallCircleWidthPx_ / 2);
		this.rectFSmallUp_ = new RectF(this.centerPoint_.x - this.smallRadius, this.centerPoint_.y - this.radius, this.centerPoint_.x + this.smallRadius, this.centerPoint_.y - this.radius
				+ this.smallCircleWidthPx_);
		this.rectFSmallDown_ = new RectF();
	}

	private void createAnimator(int paramInt1, int paramInt2) {
		if (this.objectAnimator_ == null) {
			CircleMaskShapeHolder localCircleMaskShapeHolder = this.currentRadianSH;
			int[] arrayOfInt2 = new int[2];
			arrayOfInt2[0] = paramInt1;
			arrayOfInt2[1] = paramInt2;
			this.objectAnimator_ = ObjectAnimator.ofInt(localCircleMaskShapeHolder, "currentRadian", arrayOfInt2).setDuration(700L);
			this.objectAnimator_.addUpdateListener(this);
			this.objectAnimator_.setInterpolator(new AccelerateDecelerateInterpolator());
		} else {
			ObjectAnimator localObjectAnimator = this.objectAnimator_;
			int[] arrayOfInt1 = new int[2];
			arrayOfInt1[0] = paramInt1;
			arrayOfInt1[1] = paramInt2;
			localObjectAnimator.setIntValues(arrayOfInt1);
		}
	}

	public void onAnimationUpdate(ValueAnimator paramValueAnimator) {
		invalidate();
	}

	@Override
	protected void onDraw(Canvas paramCanvas) {
		super.onDraw(paramCanvas);
		paramCanvas.save();
		this.currentRadian = this.currentRadianSH.getCurrentRadian();
		if (this.currentRadian >= 350)
			return;
		else {
			this.path_.reset();
			if (this.currentRadian == 0) {
				this.paint_.setStyle(Paint.Style.STROKE);
				this.paint_.setStrokeWidth(this.smallCircleWidthPx_);
				paramCanvas.drawCircle(this.centerPoint_.x, this.centerPoint_.y, this.radius - this.smallRadius, this.paint_);
			}
			this.paint_.setStyle(Paint.Style.FILL);
			this.path_.arcTo(this.rectFOutside_, -90 + this.currentRadian, 360 - this.currentRadian);
			this.path_.arcTo(this.rectFSmallUp_, -90.0F, -180.0F);
			this.path_.arcTo(this.rectFInside_, -90.0F, -1 * (360 - this.currentRadian));
			float f1 = (float) (this.centerPoint_.x + Math.sin(Math.toRadians(this.currentRadian)) * (this.radius - this.smallRadius));
			float f2 = (float) (this.centerPoint_.y - Math.cos(Math.toRadians(this.currentRadian)) * (this.radius - this.smallRadius));
			this.rectFSmallDown_.set(f1 - this.smallRadius, f2 - this.smallRadius, f1 + this.smallRadius, f2 + this.smallRadius);
			this.path_.arcTo(this.rectFSmallDown_, 90 + this.currentRadian, -180.0F);
			paramCanvas.drawPath(this.path_, this.paint_);
			paramCanvas.restore();
		}
	}

	public void updateCircleMask(int paramInt1, int paramInt2, boolean paramBoolean) {
		if (paramBoolean) {
			Log.i(TAG, "start: " + paramInt1 + " end: " + paramInt2);
			createAnimator(paramInt1, paramInt2);
			this.objectAnimator_.start();
		} else {
			this.currentRadianSH.setCurrentRadian(paramInt2);
			invalidate();
		}
	}

	class CircleMaskShapeHolder {
		private int currentRadian = 0;

		public CircleMaskShapeHolder(int arg2) {
			this.currentRadian = arg2;
		}

		public int getCurrentRadian() {
			return this.currentRadian;
		}

		public void setCurrentRadian(int paramInt) {
			this.currentRadian = paramInt;
		}
	}
}
