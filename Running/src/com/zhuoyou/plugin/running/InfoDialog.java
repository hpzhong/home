package com.zhuoyou.plugin.running;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

public class InfoDialog extends Dialog {

	private Context mContext;
	private int layout=0;

	public InfoDialog(Context context, int theme) {
	    super(context, theme);
	    this.mContext = context;
	}

	public InfoDialog(Context context, int theme, int lay) {
		super(context, theme);
		this.mContext = context;
		this.layout = lay;
	}

	public InfoDialog(Context context) {
	    super(context); 
	    this.mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (layout != 0) {
			setContentView(layout);
		}
	}
}
