package com.zhuoyou.plugin.rank;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Random;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.zhuoyou.plugin.running.Tools;

public class AsyncImageLoader {
	private Random mRandom;
	private ImageUtils mImageUtils;

	public AsyncImageLoader() {
		mRandom = new Random();
		mImageUtils = new ImageUtils();
	}

	public Drawable loadDrawable(final String imageUrl,
			final ImageCallback imageCallback) {
		Drawable drawable = null;
		drawable = mImageUtils.getDrawable(imageUrl);
		if (drawable != null)
			return drawable;

		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				imageCallback.imageLoaded((Drawable) message.obj, imageUrl);
			}
		};
		new Thread() {
			@Override
			public void run() {
				Drawable drawable = loadImageFromUrl(imageUrl);
				mImageUtils.putDrawable(imageUrl, drawable);
				Message message = handler.obtainMessage(0, drawable);
				handler.sendMessage(message);
			}
		}.start();
		return null;
	}
	
	public static String GetFileName(String url){
		String filename = "";
		if(url != null){
	        String tmp = url;
	        String file_tmp =url;
			for(int i = 0;i<5;i++){
				tmp = tmp.substring(0,tmp.lastIndexOf("/"));
			}
			for(String aa:file_tmp.substring(tmp.length()+1).split("/")){
				filename +=aa;
			}
			filename = filename.substring(0, filename.lastIndexOf("."));
			
		}
		return filename;
	}

	public static Drawable loadImageFromUrl(String url) {
		URL m;
		FileInputStream fis = null;
		InputStream i = null;
		Drawable d = null;
		if (url != null && !url.equals("")) {
			String fileName = GetFileName(url);
			Log.i("caixinxin", "filaName = " + fileName);
			String filePath = Tools.getSDPath() + "/Running/download/cache";
			File dirs = new File(filePath.toString());
			if (!dirs.exists()) {
				dirs.mkdirs();
			}
			File f = new File(filePath, fileName);
			if (f.exists()) {
				try {
					fis = new FileInputStream(f);
					d = Drawable.createFromStream(fis, "src");
					fis.close();
					return d;
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					System.gc();
					return null;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
			try {
				m = new URL(url);
				i = (InputStream) m.getContent();
				DataInputStream in = new DataInputStream(i);
				FileOutputStream out = new FileOutputStream(f);
				byte[] buffer = new byte[1024];
				int byteread = 0;
				while ((byteread = in.read(buffer)) != -1) {
					out.write(buffer, 0, byteread);
				}
				in.close();
				out.close();
				fis = new FileInputStream(f);
				d = Drawable.createFromStream(i, "src");
				fis.close();
				i.close();
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				System.gc();
				return null;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (i != null)
						i.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return d;
	}

	public interface ImageCallback {
		public void imageLoaded(Drawable imageDrawable, String imageUrl);
	}
}
