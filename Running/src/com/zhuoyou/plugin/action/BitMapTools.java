package com.zhuoyou.plugin.action;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class BitMapTools {
	
		public static Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
			int width = bm.getWidth();
			int height = bm.getHeight();
			float scaleWidth = ((float) newWidth) / width;
			float scaleHeight = ((float) newHeight) / height;
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleHeight);
			Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,	true);
			return newbm;
		}


}
