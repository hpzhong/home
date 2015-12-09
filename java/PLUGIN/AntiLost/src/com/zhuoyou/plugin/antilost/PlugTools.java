package com.zhuoyou.plugin.antilost;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class PlugTools {
	private static final String FILE_NAME = "anti_lost";
	// five space
	private static final String S_GAP = "     ";
	
	private static String getFilePath(Context ctx){
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();
		} else {
			return null;
		}
		
		String sd  = sdDir.toString();
		String folder = sd + "/Running/antilost/data/" ;
		try {
			String filePath = folder;
			File myFilePath = new File(filePath);
			if (!myFilePath.exists()) {
				boolean flag = myFilePath.mkdirs();
				if (flag) {
				} else {
				}
			} else {
				Log.v("gchk", "folderPath is exists " + folder);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String path = folder+FILE_NAME +".txt";
		
		return path;
	}
	
	private static boolean createFile(Context ctx){
		boolean ret = false;
		String path = getFilePath(ctx);
		Log.i("gchk", "createFile path =" + path);
		File file = new File(path);
		if(!file.exists()){
			try {
				ret = file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			Log.i("gchk", "createFile file exsited");
			ret = true;
		}
		
		return ret;
	}
	
	public static String getDataString(Context ctx, String key) {
		if(!createFile(ctx)){
			return null;
		}
		Log.i("gchk", "getDataString");
		String value = null;

		try {
			InputStream iStream = new FileInputStream(getFilePath(ctx));
			InputStreamReader isr = new InputStreamReader(iStream, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			String str = null;
			do {
				str = br.readLine();
				if (str == null || str.equals("")) {
					break;
				}
				// spilte sapce to key and value
				String[] s = str.split(S_GAP);
				if (s[0].equals(key)) {
					value = s[1];
					break;
				}
			} while (str != null);
			br.close();
			isr.close();
			iStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.i("gchk", "e = " + e.getMessage());
		}

		return value;
	}

	public static boolean getDataBoolean(Context ctx, String key, boolean default_value) {
		if(!createFile(ctx)){
			return default_value;
		}
		Log.i("gchk", "getDataBoolean");
		boolean value = default_value;

		try {
			InputStream iStream = new FileInputStream(getFilePath(ctx));
			InputStreamReader isr = new InputStreamReader(iStream, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			String str = null;
			do {
				str = br.readLine();
				if (str == null || str.equals("")) {
					break;
				}
				// spilt space to key and value
				String[] s = str.split(S_GAP);
				if (s[0].equals(key)) {
					value = Boolean.parseBoolean(s[1]);
					break;
				}
			} while (str != null);
			br.close();
			isr.close();
			iStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.i("gchk", "e = " + e.getMessage());
		}

		return value;
	}

	public static boolean saveDataBoolean(Context context, String key, boolean value) {
		if(!createFile(context)){
			return false;
		}
		String s = key + S_GAP + value;
		String data = reBuilderData(context , key , s);
		return save(context , data);
	}

	public static boolean saveDataString(Context context, String key, String value) {
		if(!createFile(context)){
			return false;
		}
		String s = key + S_GAP + value;
		String data = reBuilderData(context , key , s);
		return save(context , data);
	}
	
	private static boolean save(Context context, String s){
		Log.i("gchk", "save s=" + s);
		try {
			OutputStream fout = new FileOutputStream(getFilePath(context), false);
			fout.write(s.getBytes());
			fout.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.i("gchk", "e1 =" + e.getMessage());
			return false;
		} catch (IOException e) {
			Log.i("gchk", "e2 =" + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	private static String reBuilderData(Context ctx , String key ,String value) {
		Log.i("gchk", "reBuilderData");
		StringBuilder sb  = new StringBuilder();
		
		try {
			InputStream iStream = new FileInputStream(getFilePath(ctx));
			InputStreamReader isr = new InputStreamReader(iStream, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			String str = br.readLine();
			//if read line is empty when first read .must return value and save to file .
			//means file is empty
			if (str == null || str.equals("") || str.equals("null")){
				br.close();
	 			isr.close();
				iStream.close();
				Log.i("gchk", "add first ");
				value += "\n";
				return value;
			}
			boolean find = false; 
			while(str!=null){
				String[] s = str.split(S_GAP);
				if (s[0].equals(key)) {
					find = true;
					str = value;
				}
				str+= "\n";
				sb.append(str);
				str = br.readLine();
			}
			
			if(!find){
				sb.append(value);
				sb.append("\n");
			}
			
			br.close();
			isr.close();
			iStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.i("gchk", "e = " + e.getMessage());
		}

		return sb.toString();
	}
}
