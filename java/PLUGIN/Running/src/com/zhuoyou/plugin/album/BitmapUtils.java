package com.zhuoyou.plugin.album;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.zhuoyou.plugin.running.HomePageItemFragment;

public class BitmapUtils {

	private static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	// 如果是放大图片，filter决定是否平滑，如果是缩小图片，filter无影响
	private static Bitmap createScaleBitmap(Bitmap src, int dstWidth,
			int dstHeight) {
		Bitmap dst = null;
		if (src != null) {
			dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
			if (src != dst) { // 如果没有缩放，那么不回收
				src.recycle(); // 释放Bitmap的native像素数组
			}
		}
		return dst;
	}

	// 从Resources中加载图片
	public static Bitmap decodeSampledBitmapFromResource(Resources res,
			int resId, int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options); // 读取图片长款
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight); // 计算inSampleSize
		options.inJustDecodeBounds = false;
		Bitmap src = BitmapFactory.decodeResource(res, resId, options); // 载入一个稍大的缩略图
		return createScaleBitmap(src, reqWidth, reqHeight); // 进一步得到目标大小的缩略图
	}

	// 从sd卡上加载图片
	public static Bitmap decodeSampledBitmapFromFd(String pathName,
			int reqWidth, int reqHeight, int type) {
		Bitmap bitmap = null;
		if (type==1) {
			bitmap = SportsAlbum.gridviewBitmapCaches.get(pathName);
		} else if(type==2) {
			bitmap = HomePageItemFragment.gridviewBitmapCaches.get(pathName);
		}else{
			bitmap = null;
		}
		if (bitmap != null) {
			System.out.println(pathName);
			return bitmap;
		}
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		options.inJustDecodeBounds = false;
		Bitmap src = BitmapFactory.decodeFile(pathName, options);
		return createScaleBitmap(src, reqWidth, reqHeight);
	}
	
	public static Bitmap getBitmapFromUrl(String url) {
		Bitmap bitmap = null;
		try {
			FileInputStream is = new FileInputStream(url);
			bitmap = BitmapFactory.decodeFileDescriptor(is.getFD());
		} catch (OutOfMemoryError e) {
			System.gc();
			bitmap = null;
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (bitmap != null) {
			bitmap = BitmapUtilities.getBitmapThumbnail(bitmap,
					bitmap.getWidth(), bitmap.getHeight());
		}
		return bitmap;
	}

}
