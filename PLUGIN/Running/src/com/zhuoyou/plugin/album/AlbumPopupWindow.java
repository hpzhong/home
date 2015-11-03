package com.zhuoyou.plugin.album;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.PaintDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.zhuoyou.plugin.running.R;

public class AlbumPopupWindow extends PopupWindow {
	private ImageView img;
	private View view;
	public Bitmap bmp = null;

	public AlbumPopupWindow(Context context, String url) {
		final WindowManager.LayoutParams lp = ((Activity) context).getWindow()
				.getAttributes();
		lp.alpha = 0.5f;
		((Activity) context).getWindow().setAttributes(lp);
		view = LayoutInflater.from(context).inflate(
				R.layout.sports_album_item_display, null);
		img = (ImageView) view.findViewById(R.id.picture);
		bmp = BitmapUtils.getBitmapFromUrl(url);
		img.setImageBitmap(bmp);
		img.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismiss();
			}
		});

		this.setContentView(view);
		this.setWidth(LayoutParams.WRAP_CONTENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setFocusable(true);

		this.setBackgroundDrawable(new PaintDrawable());

	}

}
