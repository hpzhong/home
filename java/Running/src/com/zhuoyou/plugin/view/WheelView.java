package com.zhuoyou.plugin.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.SoundEffectConstants;
import android.view.View;

import com.zhuoyou.plugin.add.TosGallery;
import com.zhuoyou.plugin.running.R;

public class WheelView extends TosGallery {
	private Drawable mSelectorDrawable = null;
	private Rect mSelectorBound = new Rect();
	private boolean attr1;

	public WheelView(Context context) {
		super(context);
		initialize(context);
	}

	public WheelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		getAttrs(context, attrs);
		initialize(context);
	}

	public WheelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		getAttrs(context, attrs);
		initialize(context);
	}

	private void getAttrs(Context context, AttributeSet attrs) {
		TypedArray ta = context.obtainStyledAttributes(attrs,
				R.styleable.myWheelView);
		attr1 = ta.getBoolean(R.styleable.myWheelView_selectorDrawable, true);
		
		
		ta.recycle();
	}

	private void initialize(Context context) {
		this.setVerticalScrollBarEnabled(false);
		this.setSlotInCenter(true);
		this.setOrientation(TosGallery.VERTICAL);
		this.setGravity(Gravity.CENTER_HORIZONTAL);
		this.setUnselectedAlpha(1.0f);

		// This lead the onDraw() will be called.
		this.setWillNotDraw(false);

		// The selector rectangle drawable.
		if (attr1) {
			this.mSelectorDrawable = getContext().getResources().getDrawable(
					R.drawable.wheel_val);
		}
		this.mSelectorDrawable = getContext().getResources().getDrawable(R.drawable.wheel_select_info);
		this.setSoundEffectsEnabled(false);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		drawCenterRect(canvas);
	}

	@Override
	public void setOrientation(int orientation) {
		if (TosGallery.HORIZONTAL == orientation) {
			throw new IllegalArgumentException(
					"The orientation must be VERTICAL");
		}

		super.setOrientation(orientation);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		int galleryCenter = getCenterOfGallery();
		View v = this.getChildAt(0);
//		int height = (null != v) ? v.getMeasuredHeight() : 50;
		final float scale = getContext().getResources().getDisplayMetrics().density;
		int height=(int) (50 * scale + 0.5f);
		int top = galleryCenter - height / 2;
		int bottom = top + height;

		mSelectorBound.set(getPaddingLeft(), top, getWidth()
				- getPaddingRight(), bottom);
	}

	@Override
	protected void selectionChanged() {
		super.selectionChanged();

		playSoundEffect(SoundEffectConstants.CLICK);
	}

	private void drawCenterRect(Canvas canvas) {
		if (null != mSelectorDrawable) {
			mSelectorDrawable.setBounds(mSelectorBound);
			mSelectorDrawable.draw(canvas);
		}
	}

//	private void drawShadows(Canvas canvas) {
//        int height = (int) (2.0 * mSelectorBound.height());
//        mTopShadow.setBounds(0, 0, getWidth(), height);
//        mTopShadow.draw(canvas);
//        mBottomShadow.setBounds(0, getHeight() - height, getWidth(), getHeight());
//        mBottomShadow.draw(canvas);
//    }
}
