package com.zhuoyou.plugin.cloud;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class CvsUtils {
	
	private static final String Tag = "CvsUtils";
	/**
	 * 用迭代的方式 删除文件或或文件夹里面所有的文件
	 * @param dir 要删除文件的文件目录
	 */
	public static void deleteFolder(File dir) {
	       File to = new File(dir.getAbsolutePath() + System.currentTimeMillis());
	       dir.renameTo(to);
	       if (to.isDirectory()) {
	           String[] children = to.list();
	           for (int i = 0; i < children.length; i++) {
	               File temp = new File(to, children[i]);
	               if (temp.isDirectory()) {
	            	   deleteFolder(temp);
	               } else {
	                   boolean b = temp.delete();
	                   if (b == false) {
	                       Log.d("deleteSDCardFolder", "DELETE FAIL");
	                   }
	               }
	           }
	           to.delete();
	       }
	 }
	/**
	 * 获得 存储的目录
	 * @return
	 */
	public static String GetDir(){
		String emove_dir;
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
//			File sdCardDir = Environment.getExternalStorageDirectory();  
			emove_dir = Environment.getExternalStorageDirectory()+"/emove_gps";
		} else {
			emove_dir = "com/zhuoyou/plugin/running/emove_tmp";
		}
		File destDir = new File(emove_dir);
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		return emove_dir;
	}   
	
	/**
	 * 将 数据库的中的表 写入文件中
	 * @param mCtx  		获取 Context
	 * @param accountId  	用户的AccountID
	 * @param filePath		要写入的文件的路径
	 * @param fileName		要写入的文件的名称
	 * @param tableUri		要导出来的table表
	 * @param projection
	 * @param selection	
	 * @param selectionArgs	
	 * @param sortOrder
	 * @throws IOException 
	 */
	public static void DBTableToFile(Context mCtx,String accountId,String filePath,String fileName,Uri tableUri, String[] projection, String selection, String[] selectionArgs, String sortOrder) throws IOException{
		ContentResolver cr = mCtx.getContentResolver();
		Cursor mCursor = cr.query(tableUri, projection, selection, selectionArgs, sortOrder);
		if(mCursor ==null || mCursor.getCount()==0){
			mCursor.close();
			mCursor = null;
			return;
		}
		File fileDirs = new File(filePath);
		if(!fileDirs.exists()){
			fileDirs.mkdirs();
		}
		File txtFile = new File(filePath,fileName);
		if(!txtFile.exists()){
			txtFile.createNewFile();
		}
		FileWriter mFw = new FileWriter(txtFile);
		BufferedWriter mBW = new BufferedWriter(mFw);
		while(mCursor.moveToNext()){
			for(int i = 0 ;i<mCursor.getColumnCount();i++){
			   	if(i == 0) {
			   		
			   		mBW.write(mCursor.getString(0) + ','+accountId+',');
			   		
            	} else if (i == (mCursor.getColumnCount()-1)) {
            		String res = mCursor.getString(i);
            		if(!TextUtils.isEmpty(res)){                    			
            			mBW.write(getValue(mCursor,i,res));  
            		}
            		
        		} else {
        			String res = mCursor.getString(i);
            		if(!TextUtils.isEmpty(res)){                    			
            			mBW.write(getValue(mCursor,i,res) + ',');  
            		}else{
            			mBW.write(',');                      			
            		}
        		}  
			}
			mBW.newLine();
		}
		mCursor.close();
		mCursor = null;
		mBW.flush();  
		mBW.close();
		mFw.close();
	}
	
	
	private static String getValue(Cursor mCursor,int columnIndex,String defRes){
		int type = mCursor.getType(columnIndex);
		if(type == Cursor.FIELD_TYPE_FLOAT){
			return ""+mCursor.getDouble(columnIndex);
		}else{
			return defRes;
		}
	}
	
	/**
	 * 功能：把 sourceDir 目录下的所有文件进行 zip 格式的压缩，保存为指定 zip 文件
	 * @param sourceDir
	 *            <li>如果是目录，eg：D:\aaa\bbb\test，则压缩目录下所有文件;
	 *            <li>如果是文件，eg：D:\aaa\bbb\test\aa.txt，则只压缩本文件
	 * @param zipFile
	 *            最后压缩的文件路径和名称,eg:D:\aaa\bbb\aa.zip  注：如果使用压缩文件的路径，容易出现迭代，导致错误
	 */
	public static File doZip(String sourceDir, String zipFilePath) throws IOException {
		File file = new File(sourceDir);
		File zipFile = new File(zipFilePath);
		ZipOutputStream zos = null;
		try {
			// 创建写出流操作
			OutputStream os = new FileOutputStream(zipFile);
			BufferedOutputStream bos = new BufferedOutputStream(os);
			zos = new ZipOutputStream(bos);
			String basePath = null;
			// 获取目录
			if (file.isDirectory()) {
				basePath = file.getPath();
			} else {
				basePath = file.getParent();
			}

			zipFile(file, basePath, zos);
		} finally {
			if (zos != null) {
				zos.closeEntry();
				zos.close();
			}
		}
		return zipFile;
	}

	/**
	 * @param source   源文件
	 * @param basePath
	 * @param zos
	 */
	private static void zipFile(File source, String basePath, ZipOutputStream zos)	throws IOException {
		File[] files = null;
		if (source.isDirectory()) {
			files = source.listFiles();
		} else {
			files = new File[1];
			files[0] = source;
		}

		InputStream is = null;
		String pathName;
		byte[] buf = new byte[1024];
		int length = 0;
		try {
			for (File file : files) {
				if (file.isDirectory()) {
					pathName = file.getPath().substring(basePath.length() + 1)	+ "/";
					zos.putNextEntry(new ZipEntry(pathName));
					zipFile(file, basePath, zos);
				} else {
					pathName = file.getPath().substring(basePath.length() + 1);
					is = new FileInputStream(file);
					BufferedInputStream bis = new BufferedInputStream(is);
					zos.putNextEntry(new ZipEntry(pathName));
					while ((length = bis.read(buf)) > 0) {
						zos.write(buf, 0, length);
					}
					bis.close();
				}
			}
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}
	/**
	 * 解压文件
	 * @param zipFilePath  zip文件地址
	 * @param outFilePath  解压后放入地址
	 * @throws Exception
	 */
	public static void CVSUnzipFile(String zipFilePath, String outFilePath)throws Exception {
		Log.i(Tag, "into CVSUnzipFile");
        java.util.zip.ZipInputStream inZip = new java.util.zip.ZipInputStream(new java.io.FileInputStream(zipFilePath));  
        java.util.zip.ZipEntry zipEntry;  
        String szName = "";
        File decodepath = new File(outFilePath);
        if(decodepath.exists()){
        	if(!decodepath.isDirectory()){
        		decodepath.delete();
        		decodepath.mkdir();
        	}
        }else{
        	decodepath.mkdirs();
        }
        Log.i(Tag, "into CVSUnzipFile1");
        while ((zipEntry = inZip.getNextEntry()) != null) {  
            szName = zipEntry.getName();
            if (zipEntry.isDirectory()) {            
                szName = szName.substring(0, szName.length() - 1);  
                File folder = new File(outFilePath + File.separator + szName);  
                folder.mkdirs();  
          
            } else {  
                File file = new File(outFilePath + File.separator + szName);  
                file.createNewFile();  
                java.io.FileOutputStream out = new java.io.FileOutputStream(file);  
                int len;  
                byte[] buffer = new byte[1024];  
                while ((len = inZip.read(buffer)) != -1) {  
                    out.write(buffer, 0, len);  
                    out.flush();  
                }  
                out.close();  
            }  
        }
        inZip.close();       
    }
	
	/**
	 * 解压zip格式的压缩包
	 * @param filePath   压缩文件路径
	 * @param outPath    输出路径
	 * @return 解压成功或失败标志
	 */
	public static Boolean unZip(String filePath, String outPath) {
		try {
			ZipInputStream zin = new ZipInputStream(new FileInputStream(filePath));
			ZipEntry entry;
			// 创建文件夹
			while ((entry = zin.getNextEntry()) != null) {
				if (entry.isDirectory()) {
					File directory = new File(outPath, entry.getName());
					if (!directory.exists()) {
						if (!directory.mkdirs()) {
							return false;
						}
					}
					zin.closeEntry();
				} else {
					File myFile = new File(entry.getName());
					FileOutputStream fout = new FileOutputStream(outPath + myFile.getPath());
					DataOutputStream dout = new DataOutputStream(fout);
					byte[] b = new byte[1024];
					int len = 0;
					while ((len = zin.read(b)) != -1) {
						dout.write(b, 0, len);
					}
					dout.close();
					fout.close();
					zin.closeEntry();
				}
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/** 将文件转化为 ArrayList<ArrayList<String>> 传出来
	 * @param filePath 文件路径
	 * @param fileName 文件名
	 * @return
	 */
	public static ArrayList<ArrayList<String>> parseFile(String filePath,String fileName){
		File file = new File(filePath + "/" + fileName);
		if(!file.exists() || file==null){
			return null;
		}
		
		ArrayList<ArrayList<String>> mResultList = new ArrayList<ArrayList<String>>();
		ArrayList<String> subList = new ArrayList<String>();
		
		try {
	        String lineString;
	        BufferedReader mBreader = new BufferedReader(new FileReader(file));
			while((lineString = mBreader.readLine())!=null){
				int point = 0;
	            if(lineString!=null && !lineString.equals("")){
	            	for(int i=0 ;i<lineString.length();i++){
	            		char c = lineString.charAt(i);
	            		if(c == ','){
	            			String str = lineString.substring(point, i);
	            			subList.add(str);
	            			point = i+1;
	            		}
	            	}
	            	point = lineString.lastIndexOf(",") + 1;
	            	subList.add(lineString.substring(point,lineString.length()));
	            	
	            	mResultList.add(subList);
	            	subList = new ArrayList<String>();
	            }
	        }
	        mBreader.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return mResultList;
	}
}
