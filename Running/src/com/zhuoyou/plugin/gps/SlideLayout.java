package com.zhuoyou.plugin.gps;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zhuoyou.plugin.running.R;

public class SlideLayout extends RelativeLayout {
	
	public static final int MSG_LOCK_SUCESS = 0x123;

	private boolean isSpliding;
	private boolean isContain;
	private int ini_image_left;
	
//	private int VIEW_TOP;
//	private int VIEW_BOTTOM;
	private int VIEW_RIGHT;
//	private int VIEW_LEFT;
	
	private int ImageTOP;
	private int ImageBOTTOM;
	private int ImageRIGHT;
	private int ImageLEFT;
	private int ImageWIDTH;
	
	private int imageOff;
	private ImageView mImageView;
	private int moveX;
	
	private Handler sHandler;
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			invalidate();
		}
	};
	
	public SlideLayout(Context context) {
		super(context);
	}
	public SlideLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public SlideLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}


	
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
//		VIEW_TOP = getTop();
//		VIEW_BOTTOM = getBottom();
		VIEW_RIGHT = getRight();
//		VIEW_LEFT = getLeft();
		ImageTOP = mImageView.getTop();
		ImageBOTTOM = mImageView.getBottom();
		ImageRIGHT = mImageView.getRight();
		ImageLEFT = mImageView.getLeft();
		ImageWIDTH = ImageRIGHT - ImageLEFT;
		
		if(isContain && (moveX >= ini_image_left) && moveX < VIEW_RIGHT){
			ImageLEFT = moveX;
			ImageRIGHT = ImageLEFT + ImageWIDTH;
		}
		if(isSpliding){
			ImageLEFT = moveX;
			ImageRIGHT = ImageLEFT + ImageWIDTH;
		}
		mImageView.layout(ImageLEFT, ImageTOP, ImageRIGHT, ImageBOTTOM);
		returnImage();
	}
	
	/** 判断是否滑动成功 */
	private void isSlipFinish(){
		int offSet = VIEW_RIGHT -  ImageRIGHT;
		if(offSet < 0.4 * ImageWIDTH ){
			if(sHandler !=null){
				isContain = false;
				moveX = ini_image_left;
				ImageLEFT = ini_image_left;
				ImageRIGHT = ImageLEFT + ImageWIDTH;
				mImageView.layout(ImageLEFT, ImageTOP, ImageRIGHT, ImageBOTTOM);
				sHandler.sendEmptyMessage(MSG_LOCK_SUCESS);
			}
		}
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			isContain = isClickImage(event);
			break;
		case MotionEvent.ACTION_MOVE:
			int curX = (int)event.getX() - imageOff;
			if(isContain){
				if(curX < ini_image_left){
					moveX = ini_image_left;
				}else if((curX + ImageWIDTH) > (VIEW_RIGHT - ini_image_left)){
					moveX = (VIEW_RIGHT - ini_image_left - ImageWIDTH);
				}else{
					moveX = curX;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			isSlipFinish();
			isContain = false;
			returnImage();
			break;
		default:
			break;
		}
		invalidate();
		return true;
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mImageView = (ImageView)findViewById(R.id.slider_icon);
		ini_image_left = mImageView.getLeft();
	}
	
	private boolean isClickImage(MotionEvent event){
		int x = (int)event.getX();
		int y = (int)event.getY();
		
		if(ImageLEFT < x && x < ImageRIGHT && ImageTOP < y && y < ImageBOTTOM){
			imageOff = x-ImageLEFT;
			return true;
		}else{
			imageOff = 0 ;
			return false;
		}
	}
	
	private void returnImage(){
		final int speed = 5;
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(ImageLEFT > ini_image_left){
					if(isContain){
						isSpliding = false;
						return;
					}
					isSpliding = true;
					moveX = moveX - speed;
					
					if(moveX <ini_image_left){
						moveX = ini_image_left;
					}
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mHandler.sendEmptyMessage(0);
				}
			}
		}).start();
	}
	
	public void setMainHandler(Handler handler) {
		sHandler = handler;
	}
}