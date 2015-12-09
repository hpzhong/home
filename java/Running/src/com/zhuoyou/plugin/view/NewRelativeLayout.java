package com.zhuoyou.plugin.view;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.ViewParent;
import android.widget.RelativeLayout;

public class NewRelativeLayout extends RelativeLayout implements NewScrollView.ScrollListener {

	private int mState = 0;
	private int locHeight;
	public ViewParent parent = getParent();
	public boolean canAnimate = false;
	private Handler mHandler = new WRHandler(this);
	private static final int ANIMATE = 1;
	private static final int SCROLL = 2;
	private Operator mOperator;
	
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
        if(!isShown() || i + mOperator.getWindowHeight() <= 20 + locHeight || mState != 1)
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
    
	private static class WRHandler extends Handler {
		WeakReference<NewRelativeLayout> mNewRelativeLayout;

		public WRHandler(NewRelativeLayout NRL) {
			mNewRelativeLayout = new WeakReference<NewRelativeLayout>(NRL);
		}

		@Override
		public void handleMessage(Message msg) {
			if (mNewRelativeLayout != null) {
				NewRelativeLayout newRelativeLayout = mNewRelativeLayout.get();
				if (newRelativeLayout != null) {
					switch (msg.what) {
					case ANIMATE:
						newRelativeLayout.canAnimate = true;
						newRelativeLayout.mOperator.refreshLayout();
						break;
					case SCROLL:
						newRelativeLayout.doScroll(msg.arg1, msg.arg2);
						break;
					default:
						break;
					}
				}
			}
		}
	}
    
	public interface Operator {
		public void refreshLayout();
		public int getWindowHeight();
	}
    
	public void setOperator(Operator operatorP) {
		mOperator = operatorP;
	}
	
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
