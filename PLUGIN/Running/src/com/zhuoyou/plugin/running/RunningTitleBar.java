package com.zhuoyou.plugin.running;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.app.Activity;
import com.zhuoyou.plugin.running.R;
public class RunningTitleBar {

	private static Activity mActivity;

	public static void getTitleBar(Activity activity, String title) {
		mActivity = activity;
		TextView textView = (TextView) activity.findViewById(R.id.title);
		textView.setText(title);
		RelativeLayout titleBack = (RelativeLayout) activity
				.findViewById(R.id.back);
		titleBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mActivity.finish();
			}
		});

	}
}
