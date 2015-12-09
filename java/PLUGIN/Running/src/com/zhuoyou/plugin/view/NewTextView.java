package com.zhuoyou.plugin.view;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;

import com.zhuoyou.plugin.running.MotionDataActivity;

public class NewTextView extends TextView implements NewScrollView.ScrollListener {

    private int rate = 1;
    private int mFormat = 0;
    private int mState = 0;
    private int mHeight;
    private int locHeight;
    private double mRate;
    private double mValue;
    private double mCurValue;
    private double mGalValue;
    private boolean refreshing;
    public boolean refreshDisabled = false;
    private static final int REFRESH = 1;
    private static final int SCROLL = 2;
    private static final int OFFSET = 100;
    DecimalFormat intNum = new DecimalFormat("0");
    DecimalFormat floatNum = new DecimalFormat("0.0");
 
    public NewTextView(Context context) {
        super(context);
    }

    public NewTextView(Context context, AttributeSet attributeset) {
        super(context, attributeset);
    }

    public NewTextView(Context context, AttributeSet attributeset, int i) {
        super(context, attributeset, i);
    }

    /*private boolean doMinus(int i)
    {
        boolean flag = true;
        if((i <= locHeight || mState != 1) && ((i + MotionDataActivity.mWindowHeight) - mHeight >= -100 + locHeight || mState != 2))
            flag = false;
        return flag;
    }*/

    private boolean doPlus(int i) {
        boolean flag = true;
        if((!isShown() || i + MotionDataActivity.mWindowHeight <= 100 + locHeight || mState != 1) && (!isShown() || i >= locHeight || mState != 2))
            flag = false;
        return flag;
    }

    private void doScroll(int i, int j) {
        if(mState != i || !refreshing || refreshDisabled) {
	        mState = i;
	        if(doPlus(j))
	        {
	            rate = 1;
	            mGalValue = mValue;
	            mHandler.sendEmptyMessage(1);
	        }
        }
    }

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

    public void setLocHeight(int i) {
        locHeight = i;
    }

    public void setValue(double value) {
        double temp = 0.0D;
        mFormat = 1;
        mCurValue = temp;
        if(isShown())
        	temp = value;
        mGalValue = temp;
        mValue = value;
        if (value < 40D) {
        	mRate = 1.0D;
        } else {
            mRate = mValue / 40D;
            mRate = (new BigDecimal(mRate)).setScale(2, 4).doubleValue();
        }
    }

    public void setValue(int value) {
        int temp = 0;
        mFormat = 0;
        mCurValue = 0.0D;
        if(isShown())
        	temp = value;
        mGalValue = temp;
        mValue = value;
        if (value < 40) {
        	mRate = 1.0D;
        } else {
            mRate = (int)(mValue / 40D);
            mRate = (new BigDecimal(mRate)).setScale(2, 4).intValue();
        }
    }

    private Handler mHandler = new Handler() {
		public void handleMessage(Message message) {
			super.handleMessage(message);
			switch (message.what) {
			case REFRESH:
				if((double)rate * mCurValue >= mGalValue) {
					refreshing = false;
			        if(mFormat == 0)
			            setText(intNum.format(mGalValue));
			        else if(mFormat == 1)
			            setText(floatNum.format(mGalValue));
			        refreshDisabled = true;
				} else {
					if(!refreshDisabled) {
						refreshing = true;
				        if(mFormat == 0)
				            setText(intNum.format(mCurValue));
				        else if(mFormat == 1)
				            setText(floatNum.format(mCurValue));
				        
				        NewTextView newtextview = NewTextView.this;
						newtextview.mCurValue = newtextview.mCurValue + mRate * (double) rate;
						mHandler.sendEmptyMessageDelayed(1, 10L);
					}
				}
				break;
			case SCROLL:
				doScroll(message.arg1, message.arg2);
				break;
			default:
				break;
			}
		}
	};
}
