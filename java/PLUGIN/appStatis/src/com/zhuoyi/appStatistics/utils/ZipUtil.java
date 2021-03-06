package com.zhuoyi.appStatistics.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ZipUtil
{
	
	public static byte[] compress(byte[] byteArray) throws IOException 
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(byteArray);
		gzip.close();
		byte[] compressByteArray = out.toByteArray();
		out.close();
		return compressByteArray;
	}

	
	public static byte[] uncompress(byte[] byteArry) throws IOException 
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = new ByteArrayInputStream(byteArry);
		GZIPInputStream gunzip = new GZIPInputStream(in);
		byte[] buffer = new byte[256];
		int n;
		while ((n = gunzip.read(buffer)) >= 0) 
		{
			out.write(buffer, 0, n);
		}
		
		byte[] uncompressByteArray = out.toByteArray();
		gunzip.close();
		out.close();
		return uncompressByteArray;
	}

}
