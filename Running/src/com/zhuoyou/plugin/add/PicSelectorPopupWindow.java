package com.zhuoyou.plugin.add;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.zhuoyou.plugin.running.R;

public class PicSelectorPopupWindow extends PopupWindow{
	private TextView tv_take_photo,tv_chose_camera,tv_cancle;
	private View view;
	private SimpleAdapter adapter;
	private List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
	public PicSelectorPopupWindow(Context context,OnClickListener listener){
		super(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.popupwindow_demo, null);
		tv_take_photo = (TextView) view.findViewById(R.id.tv_take_photo);
		tv_chose_camera = (TextView) view.findViewById(R.id.tv_chose_pic);
		tv_cancle = (TextView) view.findViewById(R.id.tv_cancle);
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
