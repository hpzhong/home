package com.zhuoyou.plugin.info;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.zhuoyou.plugin.album.BitmapUtils;
import com.zhuoyou.plugin.running.Tools;

public class ImageAsynTask extends AsyncTask<String, Void, Bitmap> {

	String fileName = "";
	
	@Override
	protected Bitmap doInBackground(String... params) {
		Bitmap bitmap = null;
		String url = params[0];
		fileName = params[1];
		try {
			bitmap = BitmapFactory.decodeStream(getImageStream(url));
		} catch (Exception e) {
            e.printStackTrace();
        }
		return bitmap;
	}

	@Override
	protected void onPostExecute(Bitmap resultBitmap) {
		if (resultBitmap == null)
			return;
		String file = Tools.getSDPath() +"/Running/download/";
        File dirFile = new File(file);
        if(!dirFile.exists()){
            dirFile.mkdir();
        }
        File myCaptureFile = new File(file + fileName);
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
            Bitmap bmp = BitmapUtils.getCroppedRoundBitmap(resultBitmap, 100);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, bos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	if (resultBitmap != null && !resultBitmap.isRecycled()) {
            	resultBitmap.recycle();
            	resultBitmap = null;
            	System.gc();
        	}
        	try {
                bos.flush();
                bos.close();
        	} catch (Exception e) {
                e.printStackTrace();
            }
        }
		super.onPostExecute(resultBitmap);
	}
	
	private InputStream getImageStream(String path) throws Exception{
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5 * 1000);
        conn.setRequestMethod("GET");
        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
            return conn.getInputStream();
        }
        return null;
    }
}
