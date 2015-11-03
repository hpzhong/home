package com.zhuoyou.plugin.album;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.ExifInterface;

import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;

public class BitmapUtils {

	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (width > height) {
			if ((width / height) > 3) {
				return inSampleSize;
			}
		} else {
			if ((height / width) > 3) {
				return inSampleSize;
			}
		}
		if (height > reqHeight || width > reqWidth) {
			int heightRatio = (int) Math.ceil(height / reqHeight);
			if ((height % reqHeight) > 0) {
				heightRatio = heightRatio + 1;
			}
			inSampleSize = heightRatio;
		}
		return inSampleSize;
	}
	
	private static int scaleSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			int widthRatio = (int) Math.ceil(width / reqWidth);
			int heightRatio = (int) Math.ceil(height / reqHeight);
			if (widthRatio <= heightRatio) {
				inSampleSize = widthRatio;
			} else {
				inSampleSize = heightRatio;
			}
		}
		return inSampleSize;
	}
	
	private static int scaleSampleSize3(BitmapFactory.Options options, int reqWidth) {
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (width > reqWidth) {
			int widthRatio = (int) Math.ceil(width / reqWidth);
			inSampleSize = widthRatio;
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
				System.gc();
				src = null;
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
	public static Bitmap decodeSampledBitmapFromFd(String pathName, int reqWidth, int type) {
		Bitmap bitmap = null;
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inJustDecodeBounds = true;
		options.inPurgeable = true;// 设置图片可以被回收 
		BitmapFactory.decodeFile(pathName, options);
		if(options.outHeight > 0 && options.outWidth>0){
			if(options.outHeight > 0 && options.outWidth>0){
				options.inSampleSize = scaleSampleSize3(options, reqWidth);
			} else {
				return null;
			}
		} else {
			return null;
		}
		options.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(pathName, options);
		return compressImage(bitmap, 30,  Tools.getSDPath() +"/Running/.albumthumbnail/", pathName);
	}
	
	// 从sd卡上加载图片
	public static Bitmap decodeSampledBitmapFromFd2(String pathName, int reqWidth, int reqHeight, int type) {
		Bitmap bitmap = null;
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inJustDecodeBounds = true;
		options.inPurgeable = true;// 设置图片可以被回收 
		BitmapFactory.decodeFile(pathName, options);
		if(options.outHeight > 0 && options.outWidth>0){
			options.inSampleSize = scaleSampleSize(options, reqWidth, reqHeight);
		} else {
			return null;
		}
		options.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(pathName, options);
		if (bitmap.getWidth() < reqWidth || bitmap.getHeight() < reqHeight) {
			float width_scal = (float)reqWidth / bitmap.getWidth();
			float height_scal = (float)reqHeight / bitmap.getHeight();
			if (width_scal >= height_scal) {
				bitmap = createScaleBitmap(bitmap, reqWidth, (int)(bitmap.getHeight() * width_scal));
			}else{
				bitmap = createScaleBitmap(bitmap, (int)(bitmap.getWidth() * height_scal), reqHeight);
			}

		}
		
		Matrix matrix = new Matrix();
		matrix.postRotate(readPictureDegree(pathName));
		bitmap = Bitmap.createBitmap(bitmap, (bitmap.getWidth() - reqWidth) / 2, (bitmap.getHeight() - reqHeight) / 2, reqWidth, reqHeight, matrix, true);
		if (type == 1) {
			return compressImage(bitmap, 100, Tools.getSDPath() +"/Running/.thumbnailnew/", pathName);
		} else if (type == 3) {
			return compressImage(bitmap, 30, Tools.getSDPath() + "/Running/.albumthumbnail/", pathName);
		} else {
			return compressImage(bitmap, 40, pathName);
		}
	}
	
	public static Bitmap getBitmapFromUrl(String url) {
		Bitmap bitmap = null;
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inJustDecodeBounds = true;
		options.inPurgeable = true;// 设置图片可以被回收 
		BitmapFactory.decodeFile(url, options);
		// 获取比例大小
		int wRatio = (int) Math.ceil(options.outWidth / ScreenHelper.getScreenWidth(RunningApp.getInstance().getApplicationContext()));
		int hRatio = (int) Math.ceil(options.outHeight/ ScreenHelper.getScreenHeight(RunningApp.getInstance().getApplicationContext()));
		// 如果超出指定大小，则缩小相应的比例
		if (wRatio > 1 && hRatio > 1) {
			if (wRatio > hRatio) {
				options.inSampleSize = wRatio;
			} else {
				options.inSampleSize = hRatio;
			}
		}
		options.inJustDecodeBounds = false;
		try {
			bitmap = BitmapFactory.decodeFile(url, options);
			//bitmap = createScaleBitmap(bitmap, width, height);
			// 旋转图片动作
			Matrix matrix = new Matrix();
			matrix.postRotate(readPictureDegree(url));
			// 创建新的图片
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),bitmap.getHeight(), matrix, true);
		} catch (OutOfMemoryError e) {
			System.gc();
			bitmap = null;
			e.printStackTrace();
		}
		return bitmap;
	}
	
	public static Bitmap getScaleBitmap(String url) {
		Bitmap bitmap = null;
		try {
			FileInputStream fs = new FileInputStream(url);
			int length = 0;
			length = fs.available();
			byte[] buffer = new byte[length];
			fs.read(buffer);// 读取图片文件为byte数组

			ByteArrayInputStream isBm = new ByteArrayInputStream(buffer);//byte数组转InputStream
			final BitmapFactory.Options options2 = new BitmapFactory.Options();
			options2.inPreferredConfig = Bitmap.Config.RGB_565;//RGB_565是RGB_8888的 1/2
			bitmap = BitmapFactory.decodeStream(isBm, null, options2);// 把ByteArrayInputStream数据生成图片
			fs.close();
			//Log.d("yangyang", "getScaleBitmap bitmap.size = " + bitmap.getByteCount());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bitmap;
	}
	
	private static Bitmap compressImage(Bitmap image, int pSize, String flieUrl, String url) {
		if(image==null){
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int options = 90;
		image.compress(Bitmap.CompressFormat.JPEG, 90, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		while ((baos.toByteArray().length / 1024 > pSize) && options>0) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
			baos.reset();// 重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
			options -= 20;// 每次都减少20
		}
		if (image != null) {
			image.recycle();
			System.gc();
			image = null;
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		final BitmapFactory.Options options2 = new BitmapFactory.Options();
		options2.inPreferredConfig = Bitmap.Config.RGB_565;//RGB_565是RGB_8888的 1/2
		image = BitmapFactory.decodeStream(isBm, null, options2);// 把ByteArrayInputStream数据生成图片
		saveBitmap(baos, flieUrl, url);
		return image;
	}
	
	public static Bitmap compressImage(Bitmap image, int pSize, String url) {
		if(image==null){
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int options = 90;
		image.compress(Bitmap.CompressFormat.JPEG, 90, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		while ((baos.toByteArray().length / 1024 > pSize) && options>0) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
			baos.reset();// 重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
			options -= 20;// 每次都减少20
		}
		if (image != null) {
			image.recycle();
			System.gc();
			image = null;
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		final BitmapFactory.Options options2 = new BitmapFactory.Options();
		options2.inPreferredConfig = Bitmap.Config.RGB_565;//RGB_565是RGB_8888的 1/2
		image = BitmapFactory.decodeStream(isBm, null, options2);// 把ByteArrayInputStream数据生成图片
		return image;
	}
	
	/** 保存方法 */
	public static void saveBitmap(ByteArrayOutputStream baos, String flieUrl, String url) {
		File fd = new File(flieUrl);
		if (!fd.exists()) {
			fd.mkdirs();
		}
		try {
			File f = new File(flieUrl, url.substring(url.lastIndexOf("/")+1, url.lastIndexOf(".")));
			FileOutputStream out = new FileOutputStream(f);
			out.write(baos.toByteArray());
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** 
     * 获取裁剪后的圆形图片 
     *  
     * @param radius 直径 
     */  
    public static Bitmap getCroppedRoundBitmap(Bitmap bmp, int radius) {  
        Bitmap scaledSrcBmp;  
        int diameter = radius;
        // 为了防止宽高不相等，造成圆形图片变形，因此截取长方形中处于中间位置最大的正方形图片  
        int bmpWidth = bmp.getWidth();  
        int bmpHeight = bmp.getHeight();  
        int squareWidth = 0, squareHeight = 0;  
        int x = 0, y = 0;  
        Bitmap squareBitmap;  
        if (bmpHeight > bmpWidth) {// 高大于宽  
            squareWidth = squareHeight = bmpWidth;  
            x = 0;  
            y = (bmpHeight - bmpWidth) / 2;  
            // 截取正方形图片  
            squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth,  
                    squareHeight);  
        } else if (bmpHeight < bmpWidth) {// 宽大于高  
            squareWidth = squareHeight = bmpHeight;  
            x = (bmpWidth - bmpHeight) / 2;  
            y = 0;  
            squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth,  
                    squareHeight);  
        } else {  
            squareBitmap = bmp;  
        }  
  
        if (squareBitmap.getWidth() != diameter  
                || squareBitmap.getHeight() != diameter) {  
            scaledSrcBmp = Bitmap.createScaledBitmap(squareBitmap, diameter,  
                    diameter, true);  
  
        } else {  
            scaledSrcBmp = squareBitmap;  
        }  
        Bitmap output = Bitmap.createBitmap(scaledSrcBmp.getWidth(),  
                scaledSrcBmp.getHeight(), Config.ARGB_8888);  
        Canvas canvas = new Canvas(output);  
  
        Paint paint = new Paint();  
        Rect rect = new Rect(1, 1, scaledSrcBmp.getWidth() - 1,  
                scaledSrcBmp.getHeight() - 1);  
  
        paint.setAntiAlias(true);  
        paint.setFilterBitmap(true);  
        paint.setDither(true);  
        canvas.drawARGB(0, 0, 0, 0);  
        canvas.drawCircle(scaledSrcBmp.getWidth() / 2,  
                scaledSrcBmp.getHeight() / 2, scaledSrcBmp.getWidth() / 2 - 1,  
                paint);  
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));  
        canvas.drawBitmap(scaledSrcBmp, rect, rect, paint);  
        // bitmap回收(recycle导致在布局文件XML看不到效果)  
        // bmp.recycle();  
        // squareBitmap.recycle();  
        // scaledSrcBmp.recycle();  
        bmp = null;  
        squareBitmap = null;  
        scaledSrcBmp = null;  
        return output;  
    }
    
	/**
	 * 读取图片属性：旋转的角度
	 * 
	 * @param path: 图片绝对路径
	 * 	 
	 * @return degree旋转的角度
	 */
	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
			System.out.println("orientation=" + orientation);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}
}
