package com.zhuoyou.plugin.info;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;

public class ClipView extends View {
	public ClipView(Context context)
	{
		super(context);
	}

	public ClipView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}


	public ClipView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.save();
		int width = this.getWidth();
		int height = this.getHeight();
		Paint paint = new Paint();
		paint.setColor(0xaa000000);
		Path path = new Path();
		path.reset();
		path.addCircle(width / 2, height / 2, width / 2, Path.Direction.CW);
		canvas.clipPath(path, Region.Op.XOR);
		canvas.drawRect(0, 0, width, height, paint);
		canvas.restore();
	}
}
