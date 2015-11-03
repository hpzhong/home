package com.zhuoyou.plugin.running;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class SharePopupWindow extends PopupWindow {
	public static SharePopupWindow mInstance;
	private String mFileName;
	private View mView;
	private EditText mShare_edit;
	private ImageView mShare_weibo;
	private TextView mShare;

	public SharePopupWindow(Activity context, OnClickListener itemsOnClick, String fileName) {
		super(context);
		mInstance = this;
		mFileName = fileName;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = inflater.inflate(R.layout.share_popwindow_layout, null);
		mShare_edit = (EditText) mView.findViewById(R.id.share_edit);
		mShare_weibo = (ImageView) mView.findViewById(R.id.share_weibo);
		mShare_weibo.setOnClickListener(itemsOnClick);
		mShare = (TextView) mView.findViewById(R.id.share);
		mShare.setOnClickListener(itemsOnClick);

		this.setContentView(mView);
		this.setWidth(LayoutParams.MATCH_PARENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setFocusable(true);
		this.setAnimationStyle(R.style.AnimBottom);
		ColorDrawable dw = new ColorDrawable(0x00000000);
		this.setBackgroundDrawable(dw);
		mView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int height = mView.findViewById(R.id.share_text).getTop();
				int y = (int) event.getY();
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (y < height) {
						dismiss();
					}
				}
				return true;
			}

		});
	}

	public String getShareContent() {
		if (mShare_edit != null) {
			return mShare_edit.getText().toString();
		} else {
			return null;
		}
	}
	
	public ImageView getWeiboView() {
		return mShare_weibo;
	}
	
	public String getShareFileName() {
		return mFileName;
	}
}
