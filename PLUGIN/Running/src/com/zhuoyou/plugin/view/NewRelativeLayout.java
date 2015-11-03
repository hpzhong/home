package com.zhuoyou.plugin.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import com.zhuoyou.plugin.running.MotionDataActivity;

public class NewRelativeLayout extends RelativeLayout implements NewScrollView.ScrollListener {

	private int mState = 0;
	private int mHeight;
	private int locHeight;
	public ViewParent parent = getParent();
	public boolean canAnimate = false;
	private Handler mHandler = new myHandler();
	private static final int ANIMATE = 1;
	private static final int SCROLL = 2;
	private static final int OFFSET = 20;
	
    public NewRelativeLayout(Context context) {
        super(context);
    }

    public NewRelativeLayout(Context context, AttributeSet attributeset) {
        super(context, attributeset);
    }

    public NewRelativeLayout(Context context, AttributeSet attributeset, int i) {
        super(context, attributeset, i);
    }

    private boolean doAnimate(int i) {
        boolean flag = true;
        if(!isShown() || i + MotionDataActivity.mWindowHeight <= 20 + locHeight || mState != 1)
            flag = false;
        return flag;
    }

    private void doScroll(int state, int y) {
        if(mState != state || !canAnimate) {
	        mState = state;
	        if(doAnimate(y))
	            mHandler.sendEmptyMessage(1);
        }
    }

    public void setLocHeight(int i) {
        locHeight = i;
    }

    private class myHandler extends Handler {
		public void handleMessage(Message message) {
			super.handleMessage(message);
			switch (message.what) {
			case ANIMATE:
				canAnimate = true;
				MotionDataActivity.mInstance.refreshLayout();
				break;
			case SCROLL:
				doScroll(message.arg1, message.arg2);
				break;
			default:
				break;
			}
		}
    };
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHeight = getMeasuredHeight();
    }

	@Override
	public void onScrollChanged(int i, int j) {
        Message message = mHandler.obtainMessage();
        message.what = 2;
        message.arg1 = i;
        message.arg2 = j;
        mHandler.sendMessage(message);
	}

}
