package com.zhuoyou.plugin.view;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

public class MofeiScrollView extends ScrollView {

	private View view;

	public MofeiScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MofeiScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}


	public MofeiScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if (view != null && checkArea(view, ev)) {
			return false;
		}
		return super.onInterceptTouchEvent(ev);
	}

	private boolean checkArea(View v, MotionEvent event) {
		float x = event.getRawX();  
        float y = event.getRawY();  
        int[] locate = new int[2];
        if (v != null) {
            v.getLocationOnScreen(locate);
            int l = locate[0];  
            int r = l + v.getWidth();  
            int t = locate[1];  
            int b = t + v.getHeight();
            if (l < x && x < r && t < y && y < b) {  
                return true;  
            }
        }
		return false;
	}
	
	public View getView() {  
        return view;  
    }  
  
    public void setView(View view) {  
        this.view = view;  
    } 
}
