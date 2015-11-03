package com.zhuoyou.plugin.cloud;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.util.ByteArrayBuffer;

import com.zhuoyou.plugin.selfupdate.DESUtil;
import com.zhuoyou.plugin.selfupdate.ZipUtil;

public class OpenUrlGetStyle {
	public static String ENCODE_DECODE_KEY = "x_s0_s22";//"_x22_x22";
	
	public static String accessNetworkByPost(String urlString, String contents)	throws IOException {
		String line = "";
		DataOutputStream out = null;
		URL postUrl;
		BufferedInputStream bis = null;
		ByteArrayBuffer baf = null;
		boolean isPress = false;
		HttpURLConnection connection = null;
		try {
			byte[] encrypted = DESUtil.encrypt(contents.getBytes("utf-8"),ENCODE_DECODE_KEY.getBytes());
			postUrl = new URL(urlString);
			connection = (HttpURLConnection) postUrl.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(20000);
			connection.setRequestMethod("POST");
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("contentType", "utf-8");
			connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", ""+ encrypted.length);
			out = new DataOutputStream(connection.getOutputStream());
			out.write(encrypted);
			out.flush();
			out.close();

			bis = new BufferedInputStream(connection.getInputStream());
			baf = new ByteArrayBuffer(1024);

			isPress = Boolean.valueOf(connection.getHeaderField("isPress"));

			int current = 0;

			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}
			
			if (baf.length() > 0) {
				byte unCompressByte[];
				byte[] decrypted;
				if (isPress) {
					decrypted = DESUtil.decrypt(baf.toByteArray(),ENCODE_DECODE_KEY.getBytes());
					unCompressByte = ZipUtil.uncompress(decrypted);
					line = new String(unCompressByte);
				} else {
					decrypted = DESUtil.decrypt(baf.toByteArray(),ENCODE_DECODE_KEY.getBytes());
					line = new String(decrypted);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				connection.disconnect();
			if (bis != null)
				bis.close();
			if (baf != null)
				baf.clear();
		}
		return line.trim();
	}

}
