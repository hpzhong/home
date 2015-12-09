package com.zhuoyou.plugin.view;

import android.view.View;
import android.widget.RelativeLayout;

public class ViewWrapper {

	private RelativeLayout.LayoutParams layoutParms;
	private View mTarget;

	public ViewWrapper(View paramView) {
		this.mTarget = paramView;
	}

	public int getBottomMargin() {
		this.layoutParms = ((RelativeLayout.LayoutParams) this.mTarget.getLayoutParams());
		return this.layoutParms.bottomMargin;
	}

	public int getWidth() {
		this.layoutParms = ((RelativeLayout.LayoutParams) this.mTarget.getLayoutParams());
		return this.layoutParms.width;
	}

	public void setBottomMargin(int paramInt) {
		this.layoutParms = ((RelativeLayout.LayoutParams) this.mTarget.getLayoutParams());
		this.layoutParms.bottomMargin = paramInt;
		this.mTarget.requestLayout();
	}

	public void setWidth(int paramInt) {
		this.layoutParms = ((RelativeLayout.LayoutParams) this.mTarget.getLayoutParams());
		this.layoutParms.width = paramInt;
		this.mTarget.requestLayout();
	}

}
