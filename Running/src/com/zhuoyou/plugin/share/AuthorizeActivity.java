package com.zhuoyou.plugin.share;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.weibo.net.AccessToken;
import com.weibo.net.DialogError;
import com.weibo.net.Oauth2AccessTokenHeader;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboDialogListener;
import com.weibo.net.WeiboException;
import com.weibo.net.WeiboParameters;
import com.weibo.net.WeiboWebView;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.ShareActivity;
import com.zhuoyou.plugin.running.SharePopupWindow;

public class AuthorizeActivity extends Activity {
	private WebView mWebView = null;
	private Weibo weibo = Weibo.getInstance();
	private static final String OAUTH2_ACCESS_TOKEN_URL = "https://open.weibo.cn/oauth2/access_token";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authorize);
		TextView tv_title = (TextView) findViewById(R.id.title);
		tv_title.setText(R.string.weibo_login);
		RelativeLayout im_back = (RelativeLayout) findViewById(R.id.back);
		im_back.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		mWebView = (WebView) findViewById(R.id.webview);
		weiBoAuth();
	}

	public void weiBoAuth() {
		weibo.setupConsumerConfig(WeiboConstant.CONSUMER_KEY, WeiboConstant.CONSUMER_SECRET);
		weibo.setRedirectUrl(WeiboConstant.REDIRECT_URL);
		authorize(this, mWebView);
	}

	private void authorize(Activity activity, WebView webview) {
		Utility.setAuthorization(new Oauth2AccessTokenHeader());
		startWebViewAuth(activity, webview);
	}

	private void startWebViewAuth(Activity activity, WebView webview) {
		WeiboParameters params = new WeiboParameters();
		CookieSyncManager.createInstance(activity);
		webview(activity, webview, params, new WeiboDialogListener() {
			@Override
			public void onCancel() {
				Toast.makeText(AuthorizeActivity.this, R.string.weibosdk_demo_toast_auth_canceled, Toast.LENGTH_SHORT).show();
				setResult(RESULT_OK);
				finish();
			}

			@Override
			public void onComplete(Bundle values) {
				CookieSyncManager.getInstance().sync();
				Log.i("caixinxin", "weibo onComplete");
				if (null == values) {
					Toast.makeText(AuthorizeActivity.this, R.string.weibosdk_demo_toast_obtain_code_failed, Toast.LENGTH_SHORT).show();
					return;
				}
				String code = values.getString("code");
				if (TextUtils.isEmpty(code)) {
					Toast.makeText(AuthorizeActivity.this, R.string.weibosdk_demo_toast_obtain_code_failed, Toast.LENGTH_SHORT).show();
					return;
				}
				Log.i("caixinxin", code);
				new FetchTokenAsync().execute(code);
			}

			@Override
			public void onError(DialogError arg0) {
				Toast.makeText(AuthorizeActivity.this, R.string.weibosdk_demo_toast_auth_failed, Toast.LENGTH_SHORT).show();
				setResult(RESULT_OK);
				finish();
			}

			@Override
			public void onWeiboException(WeiboException arg0) {
				Toast.makeText(AuthorizeActivity.this, R.string.weibosdk_demo_toast_auth_failed, Toast.LENGTH_SHORT).show();
				setResult(RESULT_OK);
				finish();
			}
		});
	}

	public void webview(Context context, WebView webview, WeiboParameters parameters, WeiboDialogListener listener) {
		parameters.add("client_id", WeiboConstant.CONSUMER_KEY);
		parameters.add("response_type", "code");
		parameters.add("redirect_uri", WeiboConstant.REDIRECT_URL);
		parameters.add("display", "mobile");

		if (weibo.isSessionValid()) {
			parameters.add("access_token", weibo.getAccessToken().getToken());
		}
		String url = Weibo.URL_OAUTH2_ACCESS_AUTHORIZE + "?" + Utility.encodeUrl(parameters);
		if (context.checkCallingOrSelfPermission("android.permission.INTERNET") != 0)
			Utility.showAlert(context, "Error", "Application requires permission to access the Internet");
		else
			new WeiboWebView(weibo, webview, context, url, listener);
	}

	private class FetchTokenAsync extends AsyncTask<String, Object, Integer> {
		@Override
		protected void onPreExecute() {

		}

		@Override
		protected Integer doInBackground(String... arg0) {
			String code = arg0[0];

			WeiboParameters parameters = new WeiboParameters();
			parameters.add(WeiboConstant.AUTH_PARAMS_CLIENT_ID, WeiboConstant.CONSUMER_KEY);
			parameters.add(WeiboConstant.AUTH_PARAMS_CLIENT_SECRET, WeiboConstant.CONSUMER_SECRET);
			parameters.add(WeiboConstant.AUTH_PARAMS_GRANT_TYPE, "authorization_code");
			parameters.add(WeiboConstant.AUTH_PARAMS_CODE, code);
			parameters.add(WeiboConstant.AUTH_PARAMS_REDIRECT_URL, WeiboConstant.REDIRECT_URL);

			String url = OAUTH2_ACCESS_TOKEN_URL + "?" + Utility.encodeUrl(parameters);
			String result = "";
			try {
				HttpPost httpPost = new HttpPost(url);
				HttpResponse httpResponse;
				httpResponse = new DefaultHttpClient().execute(httpPost);
				result = EntityUtils.toString(httpResponse.getEntity());

				Log.i("caixinxin", result);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				return -1;
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}

			JSONObject obj;
			try {
				obj = new JSONObject(result);
				String access_token = obj.getString("access_token");
				String expires_in = obj.getString("expires_in");
				Log.i("gchk", "access_token= " + access_token);
				Log.i("gchk", "expires_in= " + expires_in);
				AccessToken accessToken = new AccessToken(access_token, WeiboConstant.CONSUMER_SECRET);
				accessToken.setExpiresIn(expires_in);
				weibo.setAccessToken(accessToken);
				
				//save to file
				AccessTokenKeeper.writeAccessToken(AuthorizeActivity.this, accessToken);
			} catch (JSONException e) {
				e.printStackTrace();
				return -1;
			}

			if (weibo.isSessionValid()) {
				return 0;
			} else {
				return -1;
			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			if(result == -1){
				Toast.makeText(AuthorizeActivity.this, R.string.weibosdk_demo_toast_auth_failed, Toast.LENGTH_SHORT).show();
			}else if(result == 0){
				Toast.makeText(AuthorizeActivity.this, R.string.weibosdk_demo_toast_auth_success, Toast.LENGTH_SHORT).show();
				SharePopupWindow.mInstance.getWeiboView().setImageResource(R.drawable.share_wb_select);
				Message msg = new Message();
				msg.what = 1;
				ShareActivity.mHandler.sendMessage(msg);
			}
			setResult(RESULT_OK);
			finish();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
