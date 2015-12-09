package com.zhuoyou.plugin.view;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;

public class NewTextView extends TextView implements NewScrollView.ScrollListener {

    private int rate = 1;
    private int mFormat = 0;
    private int mState = 0;
    private int locHeight;
    private double mRate;
    private double mValue;
    private double mCurValue;
    private double mGalValue;
    private boolean refreshing;
    public boolean refreshDisabled = false;
    private static final int REFRESH = 1;
    private static final int SCROLL = 2;
    DecimalFormat intNum = new DecimalFormat("0");
    DecimalFormat floatNum = new DecimalFormat("0.0");
    private WRHandler mHandler = new WRHandler(this);
    private Operator mOperator;
 
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
        if((!isShown() || i + mOperator.getWindowHeight() <= 100 + locHeight || mState != 1) && (!isShown() || i >= locHeight || mState != 2))
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
	
	private static class WRHandler extends Handler {
		WeakReference<NewTextView> mNewTextView;

		public WRHandler(NewTextView NTV) {
			mNewTextView = new WeakReference<NewTextView>(NTV);
		}

		@Override
		public void handleMessage(Message msg) {
			if (mNewTextView != null) {
				NewTextView newTextView = mNewTextView.get();
				if (newTextView != null) {
					switch (msg.what) {
					case REFRESH:
						if ((double) newTextView.rate * newTextView.mCurValue >= newTextView.mGalValue) {
							newTextView.refreshing = false;
							if (newTextView.mFormat == 0)
								newTextView.setText(newTextView.intNum.format(newTextView.mGalValue));
							else if (newTextView.mFormat == 1)
								newTextView.setText(newTextView.floatNum.format(newTextView.mGalValue));
							newTextView.refreshDisabled = true;
						} else {
							if (!newTextView.refreshDisabled) {
								newTextView.refreshing = true;
								if (newTextView.mFormat == 0)
									newTextView.setText(newTextView.intNum.format(newTextView.mCurValue));
								else if (newTextView.mFormat == 1)
									newTextView.setText(newTextView.floatNum.format(newTextView.mCurValue));
								
								newTextView.mCurValue = newTextView.mCurValue+ newTextView.mRate * (double) newTextView.rate;
								newTextView.mHandler.sendEmptyMessageDelayed(1, 10L);
							}
						}
						break;
					case SCROLL:
						newTextView.doScroll(msg.arg1, msg.arg2);
						break;
					default:
						break;
					}
				}
			}
		}
	}
	
	public interface Operator {
		public int getWindowHeight();
	}
    
	public void setOperator(Operator operatorP) {
		mOperator = operatorP;
	}
}
