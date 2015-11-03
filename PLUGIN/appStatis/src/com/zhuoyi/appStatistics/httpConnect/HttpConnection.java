/*modification phHe*/
package com.zhuoyi.appStatistics.httpConnect;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Entity;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.zhuoyi.appStatistics.storage.AppStatisticsStorage;
import com.zhuoyi.appStatistics.utils.DESUtil;
import com.zhuoyi.appStatistics.utils.LogUtil;
import com.zhuoyi.appStatistics.utils.ZipUtil;

public class HttpConnection {
	public static final String TAG = "httpConnect";

	// private static final String UPLOAD_URL = "http://211.151.183.152:2510";
	private static final String UPLOAD_URL = "http://service-ants.yy845.com:2510";

	private static HttpConnection mSelf;

	public static String ENCODE_DECODE_KEY = "x_s0_s22";// "_x22_x22";

	public int rate;
	private Context mContext;
	private DefaultHttpClient mHttpClient;
	private String date;

	public static HttpConnection getInstance(Context context) {
		if (mSelf == null) {
			mSelf = new HttpConnection(context);
		}

		return mSelf;
	}

	HttpConnection(Context context) {
		mContext = context;
	}

	public int getRate() {
		int saveRate = 0;
		SharedPreferences sp = mContext.getSharedPreferences("rate",
				Context.MODE_APPEND);
		saveRate = sp.getInt("saverate", 0);
		Log.i("111", "saveRate ==" + saveRate);
		return saveRate;

	}

	public void uploadStatisticsData(JSONObject dataJa, JSONObject headJo,
			String ch) {
		boolean success = false;
		// int count = 1;
		NetworkInfo info = null;

		Log.i("112", "fops0089998766 ===");

		ConnectivityManager connectMgr = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectMgr != null) {
			info = connectMgr.getActiveNetworkInfo();
		}

		if (info != null) {

			if (info.getType() == ConnectivityManager.TYPE_WIFI) {

				try {
					// if(dataJa != null){

					Log.i("111", "JSONObject===== ");

					LogUtil.logI(TAG, "uploadStatisticsData", "data str:"
							+ dataJa.toString());
					JSONObject jo = new JSONObject();

					jo.put("body", dataJa.toString());
					jo.put("head", headJo.toString());
					Log.i("111", "rat9999995===== ");
					String result = accessNetworkByPost(UPLOAD_URL,
							jo.toString());
					Log.i("111", "result = " + result);
					
					if(TextUtils.isEmpty(result)){
						Log.i("111", "error result is null " );
						return;
					}
					JSONObject resJo = new JSONObject(result);
					// String resultStr = resJo.getString("rate");

					String bodyStr = resJo.getString("body");
					JSONObject resBodyJo = new JSONObject(bodyStr);
					int ret = resBodyJo.getInt("rst");
					int errorCode = resBodyJo.getInt("errorCode");

					rate = resBodyJo.getInt("rate");
					SharedPreferences sps = mContext.getSharedPreferences(
							"rate", Context.MODE_APPEND);
					SharedPreferences.Editor editor = sps.edit();
					editor.putInt("saverate", rate).commit();

					Log.i("111", "rate55555555===== " + rate);
					if (errorCode == 0) {
						success = true;
					}

				} catch (Exception e) {
					Log.i("111", "error = " + e);
					e.printStackTrace();
				}
			}
		}

		Log.i("111", "success = " + success);
		if (success) {
			AppStatisticsStorage.clearUnuploadData(mContext);
			
			Log.i("112", "fops0011e ===");
		} else {
			try {
				JSONObject resJo = new JSONObject(dataJa.toString());
				String sdata = resJo.getString("sdata");
				JSONArray array = new JSONArray(sdata);
				Log.i("111", "array34 = " + array);
				AppStatisticsStorage.saveUnuploadData(mContext, array);

				
				//AppStatisticsStorage.saveUnuploadData(mContext, dataJa.getJSONArray("sdata"));
			
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		shutdownConnection();
	}

	// }

	private void shutdownConnection() {
		if (mHttpClient != null) {
			mHttpClient.getConnectionManager().shutdown();
		}
		mHttpClient = null;
	}

	private String getChunkedContent(HttpEntity entity) throws IOException {
		int rCount = 0;
		byte[] ret = new byte[0];
		byte[] buff = new byte[1024];
		InputStream in = entity.getContent();
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream();

		while ((rCount = in.read(buff, 0, 1024)) > 0) {
			swapStream.write(buff, 0, rCount);
		}
		ret = swapStream.toByteArray();

		return EncodingUtils.getString(ret, "utf-8");
	}

	public static String accessNetworkByPost(String urlString, String contents)
			throws IOException {
		String line = null;
		DataOutputStream out = null;
		URL postUrl;

		BufferedInputStream bis = null;
		ByteArrayBuffer baf = null;
		boolean isPress = false;
		HttpURLConnection connection = null;

		try {
			byte[] encrypted = DESUtil.encrypt(contents.getBytes("utf-8"),
					ENCODE_DECODE_KEY.getBytes());

			postUrl = new URL(urlString);
			connection = (HttpURLConnection) postUrl.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(20000);
			connection.setRequestMethod("POST");
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("contentType", "utf-8");
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", ""
					+ encrypted.length);

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
					decrypted = DESUtil.decrypt(baf.toByteArray(),
							ENCODE_DECODE_KEY.getBytes());
					unCompressByte = ZipUtil.uncompress(decrypted);
					line = new String(unCompressByte);
				} else {
					decrypted = DESUtil.decrypt(baf.toByteArray(),
							ENCODE_DECODE_KEY.getBytes());
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
		return line != null ? line.trim() : null;
	}

	private HttpResponse doPost(String url, Map<String, String> headers,
			ArrayList<BasicNameValuePair> bnvp) {
		HttpResponse response = null;
		if (mHttpClient == null) {
			HttpParams httpParam = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParam, 30 * 1000);
			HttpConnectionParams.setSoTimeout(httpParam, 30 * 1000);
			mHttpClient = new DefaultHttpClient(httpParam);
		}
		HttpContext localcontext = new BasicHttpContext();
		LogUtil.logI(TAG, "doPost", "url = " + url);
		try {
			HttpHost host = null;
			HttpPost httpPost = null;
			if (url.contains("https")) {
				Uri u = Uri.parse(url);
				host = new HttpHost(u.getHost(), 443, u.getScheme());
				httpPost = new HttpPost(u.getPath());
			} else {
				httpPost = new HttpPost(url);
			}

			if (headers != null) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					String headKey = entry.getKey();
					String value = entry.getValue();
					LogUtil.logI(TAG, "doPost", "add header, key=" + headKey
							+ ", value=" + value);
					httpPost.addHeader(headKey, value);

				}
			}

			if (bnvp != null) {
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(bnvp,
						"utf-8");
				httpPost.setEntity(entity);
			}

			if (url.contains("https")) {
				response = mHttpClient.execute(host, httpPost);
			} else {
				response = mHttpClient.execute(httpPost, localcontext);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}
}
