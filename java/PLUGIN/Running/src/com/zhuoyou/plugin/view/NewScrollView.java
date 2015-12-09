package com.zhuoyou.plugin.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;

public class NewScrollView extends ScrollView {

	private View inner;
	private int state = 0;
	public static final int UP = 1;
	public static final int DOWN = 2;
	public static final int STOP = 3;
	private List<ScrollListener> mListeners = new ArrayList<ScrollListener>();
	
	public NewScrollView(Context context) {
		super(context);
	}

	public NewScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NewScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

    public void addListener(ScrollListener scrolllistener) {
        if(mListeners == null)
            mListeners = new ArrayList<ScrollListener>();
        mListeners.add(scrolllistener);
    }

    @Override
    protected void onFinishInflate() {
    	if (getChildCount() > 0) {
    		inner = getChildAt(0);
    	}
    }
    
    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        
        if (inner.getMeasuredHeight() < getScrollY() + getHeight())
        	return;
        
        if (y > oldy)
            state = UP;
        else if (y < oldy)
            state = DOWN;
        else
            state = STOP;
        sendScroll(state, y);
    }

    public void sendScroll(int paramInt1, int paramInt2) {
        Iterator<ScrollListener> iterator = mListeners.iterator();
        do
        {
            if(!iterator.hasNext())
                return;
            ((ScrollListener)iterator.next()).onScrollChanged(paramInt1, paramInt2);
        } while(true);
    }

    public static interface ScrollListener {
        public abstract void onScrollChanged(int paramInt1, int paramInt2);
    }

}
