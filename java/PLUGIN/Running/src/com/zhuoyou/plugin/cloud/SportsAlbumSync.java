package com.zhuoyou.plugin.cloud;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

public class SportsAlbumSync {
	private Context mContext;

	public SportsAlbumSync(Context con) {
		mContext = con;
	}

	private byte[] getUriToBytes(String uri) throws IOException {
		InputStream inStream = new FileInputStream(uri);
		byte[] buffer = new byte[1024];
		int len = -1;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		byte[] data = outStream.toByteArray();
		outStream.close();
		inStream.close();
		return data;
	}

}
