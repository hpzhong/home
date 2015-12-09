package com.zhuoyou.plugin.bluetooth.attach;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.StatFs;

public class PlugUtils {
	public static void uninstallUseIntent(String packageName, Activity a) {
		Uri packageURI = Uri.parse("package:" + packageName);
		Intent intent = new Intent(Intent.ACTION_DELETE);
		intent.setData(packageURI);
		a.startActivity(intent);
	}

	private static Class<?>[] getParamTypes(Class<?> cls, String mName) {
		Class<?> cs[] = null;

		Method[] mtd = cls.getMethods();

		for (int i = 0; i < mtd.length; i++) {
			if (!mtd[i].getName().equals(mName)) {
				continue;
			}
			cs = mtd[i].getParameterTypes();
		}
		return cs;
	}

	public static Object invoke(PlugBean bean, String method_name) {
		try {
			Method target;
			target = bean.mClasses.getMethod(method_name);
			Object o = target.invoke(bean.mInstance);
			return o;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * invoke plugin method�� need add tag and content
	 * @param bean
	 * @param method_name
	 * @param tag
	 * @param content
	 * @return
	 */
	public static Object invoke_method(PlugBean bean, String method_name, char[] tag, String content) {
		try {
			Method target;
			Class<?>[] params = getParamTypes(bean.mClasses , method_name);
			target = bean.mClasses.getMethod(method_name , params);
			Object o =null;
			if(params==null ||params.length == 0){
				o = target.invoke(bean.mInstance);
			}else{
				o = target.invoke(bean.mInstance , tag , content);
			}
			return o;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean writeStreamToFile(InputStream stream, File file) {
		boolean ret = false;
		try {
			OutputStream output = null;
			try {
				output = new FileOutputStream(file);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			try {
				try {
					final byte[] buffer = new byte[1024];
					int read;

					while ((read = stream.read(buffer)) != -1)
						output.write(buffer, 0, read);
					output.flush();
					ret = true;
				} finally {
					output.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	public static boolean canCopy(String sdcard) {
		File file = new File(sdcard);
		StatFs statFs = new StatFs(file.getPath());
		long availableSpare = (long) (statFs.getBlockSize() * ((long) statFs.getAvailableBlocks()));
		if (availableSpare / 1024 / 1024 > 20) {
			return true;
		} else {
			return false;
		}
	}
}
