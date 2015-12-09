package com.zhuoyou.plugin.info;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zhuoyou.plugin.running.R;

public class CustomAvatarPopupWindow extends PopupWindow {

	public CustomAvatarPopupWindow(Context context, Boolean flag, OnClickListener listener) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.custom_avatar_popwindow, null);
		TextView tv_select = (TextView) view.findViewById(R.id.tv_select);
		TextView tv_take_photo = (TextView) view.findViewById(R.id.tv_take_photo);
		TextView tv_chose_camera = (TextView) view.findViewById(R.id.tv_chose_pic);
		TextView tv_cancle = (TextView) view.findViewById(R.id.tv_cancle);
		if (flag) {
			tv_select.setOnClickListener(listener);
		} else {
			tv_select.setVisibility(View.GONE);
		}
		tv_take_photo.setOnClickListener(listener);
		tv_chose_camera.setOnClickListener(listener);
		tv_cancle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		this.setContentView(view);
		//设置弹出窗的宽
		this.setWidth(LayoutParams.MATCH_PARENT);
		//设置弹出窗的高
		this.setHeight(LayoutParams.WRAP_CONTENT);
		//设置弹出窗获得焦点
		this.setFocusable(true);
	}
}
