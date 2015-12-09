package com.zhuoyou.plugin.action;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ActionImageView extends ImageView {
	
	
	
    public ActionImageView(Context context) {
        super(context);
    }

    public ActionImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //
        if(getDrawable() != null){
	        int width = getDrawable().getIntrinsicWidth();
	        int height = getDrawable().getIntrinsicHeight();
	        int currentWidth = getMeasuredWidth();
	        int currentHeight = getMeasuredHeight();
	        float ratio = (float)currentWidth / (float)width;
	        currentHeight = (int)(ratio * height);
//	        Log.d("zzb","currentHeight ="+currentHeight+"currentWidth ="+currentWidth);
	        setMeasuredDimension(currentWidth, currentHeight);
        }
	}
    
    
    
    
    
    
    
}
