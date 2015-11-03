package com.zhuoyou.plugin.view;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class VerScrollView extends ScrollView {
	private OnScrollListener onScrollListener;
	private int lastScrollY;

	public VerScrollView(Context context) {
		super(context);
		init();
	}

	public VerScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public VerScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (onScrollListener != null) {
			onScrollListener.onScroll(lastScrollY = this.getScrollY());
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
		WeakReference<VerScrollView> mMyFragment;

		public UpdateHandler(VerScrollView f) {
			mMyFragment = new WeakReference<VerScrollView>(f);
		}

		@Override
		public void handleMessage(Message msg) {
			if (mMyFragment != null) {
				VerScrollView home = mMyFragment.get();
				if (home != null ) {
					int scrollY =home.getScrollY();

					if (home.lastScrollY != scrollY) {
						home.lastScrollY = scrollY;
						home.handler.sendMessageDelayed(home.handler.obtainMessage(), 5);
					}
					if (home.onScrollListener != null) {
						home.onScrollListener.onScroll(scrollY);
					}
				}
			}
		}
	}
	
	public void setOnScrollListener(OnScrollListener onScrollListener) {
		this.onScrollListener = onScrollListener;
	}

	public interface OnScrollListener {
		public void onScroll(int scrollY);
	}

}
