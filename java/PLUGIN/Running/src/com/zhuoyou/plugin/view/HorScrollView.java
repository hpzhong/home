package com.zhuoyou.plugin.view;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class HorScrollView extends HorizontalScrollView {
	private OnScrollListener onScrollListener;
	private int lastScrollX;

	public HorScrollView(Context context) {
		super(context);
		init();
	}

	public HorScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HorScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (onScrollListener != null) {
			onScrollListener.onScroll(lastScrollX = this.getScrollX());
		}
		switch (ev.getAction()) {
		case MotionEvent.ACTION_UP:
			handler.sendMessageDelayed(handler.obtainMessage(), 5);
			break;
		}
		return super.onTouchEvent(ev);
	}
	
	private UpdateHandler handler = new UpdateHandler(this);
	public static class UpdateHandler extends Handler {
		WeakReference<HorScrollView> mMyFragment;

		public UpdateHandler(HorScrollView f) {
			mMyFragment = new WeakReference<HorScrollView>(f);
		}

		@Override
		public void handleMessage(Message msg) {
			if (mMyFragment != null) {
				HorScrollView home = mMyFragment.get();
				if (home != null ) {
					int scrollX = home.getScrollX();

					if (home.lastScrollX != scrollX) {
						home.lastScrollX = scrollX;
						home.handler.sendMessageDelayed(home.handler.obtainMessage(), 5);
					}
					if (home.onScrollListener != null) {
						home.onScrollListener.onScroll(scrollX);
					}
				}
			}
		}
	}

	public void setOnScrollListener(OnScrollListener onScrollListener) {
		this.onScrollListener = onScrollListener;
	}

	public interface OnScrollListener {
		public void onScroll(int scrollX);
	}

}
