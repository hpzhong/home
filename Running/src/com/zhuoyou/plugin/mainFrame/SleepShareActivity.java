package com.zhuoyou.plugin.mainFrame;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.weibo.net.AsyncWeiboRunner.RequestListener;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboException;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.SharePopupWindow;
import com.zhuoyou.plugin.running.SleepItem;
import com.zhuoyou.plugin.running.Tools;
import com.zhuoyou.plugin.share.AuthorizeActivity;
import com.zhuoyou.plugin.share.ShareTask;
import com.zhuoyou.plugin.share.ShareToWeixin;

public class SleepShareActivity extends Activity implements OnClickListener {

	private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
	
	private Context mContext;
	private Resources res;
	private ScrollView mScreenshot;
	private ImageView mUser_logo;
	private ImageView mShare_weixin;
	private ImageView mShare_quan;
	private ImageView mShare_qq;
	private ImageView mMore;
	private SharePopupWindow mPopupWindow;
	private static int select = 0;
	private boolean isWXInstalled = false;
	private boolean isWBInstalled = false;
	private boolean isQQInstalled = false;
	private Weibo weibo = Weibo.getInstance();
	private Bitmap bmp = null;
	private TextView mUser_name;
	private TextView mSleepHour;
	private TextView mSleepMin;
	private TextView mSleepDate;
	private TextView mDeepSleep;
	private TextView mWakeSleep;
	
	private SleepItem item;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sleepshare_layout);
		mContext = this;
		item = (SleepItem) getIntent().getSerializableExtra("item");
		getShareAppStatus();
		initView();	
		initData();
	}

	/** 初始化显示数据 */
	public void initData() {
		/** 头像 */
		int headIndex = Tools.getHead(this);
		if (headIndex == 10000) {
			bmp = Tools.convertFileToBitmap("/Running/download/custom");
			mUser_logo.setImageBitmap(bmp);
		} else if (headIndex == 1000) {
			bmp = Tools.convertFileToBitmap("/Running/download/logo");
			mUser_logo.setImageBitmap(bmp);
		} else {
			mUser_logo.setImageResource(Tools.selectByIndex(headIndex));
		}
		/** 用户名  */
		if (Tools.getUsrName(this).equals("")) {
			if (!Tools.getLoginName(this).equals("")) {
				mUser_name.setText(Tools.getLoginName(this));
			} else {
				mUser_name.setText(R.string.username);
			}
		} else {
			mUser_name.setText(Tools.getUsrName(this));
		}
		
		int sleepHour=0;
		int sleepMin =0;
		int deepHour =00;
		int deepMin  =0;
		int lightHour=0;
		int lightMin =0;
		if(item !=null){
			sleepHour= item.getmSleepT() / 3600;
			sleepMin =(item.getmSleepT() % 3600 ) / 60;
			deepHour = item.getmDSleepT()/ 3600;
			deepMin  =(item.getmDSleepT()% 3600 ) / 60;
			lightHour= item.getmWSleepT()/ 3600;
			lightMin= (item.getmWSleepT()% 3600 ) / 60;
			mDeepSleep.setText(mContext.getString(R.string.share_deep)+" "+deepHour+mContext.getString(R.string.hour)+deepMin+ mContext.getString(R.string.sleep_minutes)+"，"+mContext.getString(R.string.share_light)+" "+lightHour+mContext.getString(R.string.hour)+ lightMin+mContext.getString(R.string.sleep_minutes));
			mWakeSleep.setText(mContext.getString(R.string.gosleep)+" "+item.getmStartT()+"，" + mContext.getString(R.string.wakeup)+" "+ item.getmEndT());
		}
		
		mSleepHour.setText(""+ sleepHour);
		mSleepMin.setText(""+sleepMin);
		mSleepDate.setText((item.getEndCal().get(Calendar.MONTH)+1) + mContext.getString(R.string.pop_mouth) +  item.getEndCal().get(Calendar.DATE) + mContext.getString(R.string.pop_date));
	}

	private void initView() {
		mScreenshot = (ScrollView) findViewById(R.id.screenshot);
		mUser_logo = (ImageView) findViewById(R.id.user_logo);
		mUser_name = (TextView) findViewById(R.id.user_name);
		mSleepHour = (TextView)findViewById(R.id.sleep_duration_hour);
		mSleepMin = (TextView)findViewById(R.id.sleep_duration_min);
		mSleepDate = (TextView)findViewById(R.id.sleep_date);
		mDeepSleep = (TextView)findViewById(R.id.sleep_deeptime);
		mWakeSleep = (TextView)findViewById(R.id.sleep_wakesleep_time);		
		mShare_weixin = (ImageView)findViewById(R.id.share_weixin);
		mShare_weixin.setOnClickListener(this);
		mShare_quan = (ImageView)findViewById(R.id.share_quan);
		mShare_quan.setOnClickListener(this);
		mShare_qq = (ImageView)findViewById(R.id.share_qq);
		mShare_qq.setOnClickListener(this);
		mMore = (ImageView)findViewById(R.id.share_more);
		mMore.setOnClickListener(this);
		res = getResources();
	}
	
	private void getScreenShot(String fileName) {
		int h = 0;
		for (int i = 0; i < mScreenshot.getChildCount(); i++) {
			h += mScreenshot.getChildAt(i).getHeight();
		}
		Bitmap bmp = Bitmap.createBitmap(mScreenshot.getWidth(), h, Config.RGB_565);
		mScreenshot.draw(new Canvas(bmp));
		Tools.saveBitmapToFile(bmp, fileName);
		bmp.recycle();
		bmp = null;
	}

	@Override
	public void onClick(View v) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String fileName = format.format(new Date()) + ".jpg";
		switch(v.getId()) {
		case R.id.share_weixin:
			getScreenShot(fileName);
			if (isWXInstalled) {
				if (ShareToWeixin.api.isWXAppSupportAPI())
					ShareToWeixin.SharetoWX(mContext, false, fileName);
				else
					Toast.makeText(mContext, R.string.weixin_no_support, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext, R.string.install_weixin, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.share_quan:
			getScreenShot(fileName);
			if (isWXInstalled) {
				int wxSdkVersion = ShareToWeixin.api.getWXAppSupportAPI();
				if (wxSdkVersion >= TIMELINE_SUPPORTED_VERSION)
					ShareToWeixin.SharetoWX(mContext, true, fileName);
				else
					Toast.makeText(mContext, R.string.weixin_no_support_quan, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext, R.string.install_weixin, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.share_qq:
			getScreenShot(fileName);
			if (isQQInstalled) {
				shareToQQ(fileName);
			} else {
				Toast.makeText(mContext, R.string.install_qq, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.share_more:
			getScreenShot(fileName);
			mPopupWindow = new SharePopupWindow(SleepShareActivity.this, itemsOnClick, fileName);
			mPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
			mPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
			mPopupWindow.showAtLocation(SleepShareActivity.this.findViewById(R.id.main), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
			break;
		case R.id.back_m:
			finish();
			break;
		}
		
	}

	private OnClickListener  itemsOnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.share_weibo:
				if (!weibo.isSessionValid()) {
					Intent intent = new Intent();
					intent.setClass(SleepShareActivity.this, AuthorizeActivity.class);
					startActivity(intent);
				} else {
					select = 1;
					SharePopupWindow.mInstance.getWeiboView().setImageResource(R.drawable.share_wb_select);
				}
				break;
			case R.id.share:
				if (select > 0) {
					if (SharePopupWindow.mInstance != null) {
						String share_s = SharePopupWindow.mInstance.getShareContent();
						share2weibo(share_s, Tools.getScreenShot(SharePopupWindow.mInstance.getShareFileName()));
					}
				} else {
					Toast.makeText(mContext, R.string.select_platform, Toast.LENGTH_SHORT).show();
				}
				break;
			}
		}
		
	};

	private void getShareAppStatus() {
		PackageManager pm = getPackageManager();
		Intent filterIntent = new Intent(Intent.ACTION_SEND,null);
		filterIntent.addCategory(Intent.CATEGORY_DEFAULT);
		filterIntent.setType("text/plain");

		List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();
		resolveInfos.addAll(pm.queryIntentActivities(filterIntent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT));
		
		for(int i=0;i<resolveInfos.size();i++) {
			ResolveInfo resolveInfo = resolveInfos.get(i);
			String mPackageName = resolveInfo.activityInfo.packageName;
			if(mPackageName.equals("com.tencent.mm")) {
				isWXInstalled = true;
			}
			if(mPackageName.equals("com.sina.weibo")) {
				isWBInstalled = true;
			}
			if(mPackageName.equals("com.tencent.mobileqq")) {
				isQQInstalled = true;
			}
		}
	}

	private void shareToQQ(String fileName) {
		ComponentName cp = new ComponentName("com.tencent.mobileqq","com.tencent.mobileqq.activity.JumpActivity");
		Intent intent=new Intent(Intent.ACTION_SEND);
		intent.setComponent(cp);
		intent.setType("image/*");
		File file = new File(Tools.getScreenShot(fileName));
		Uri uri = Uri.fromFile(file);
		intent.putExtra(Intent.EXTRA_STREAM, uri);
		this.startActivity(intent);
	}

	private void share2weibo(String content, String picpath) {
		ShareTask task = new ShareTask(this, picpath, content, mRequestListener);
		new Thread(task).start();
        if (mPopupWindow.isShowing())
        	mPopupWindow.dismiss();
    }

    private RequestListener mRequestListener = new RequestListener() {

		@Override
		public void onComplete(String response) {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(SleepShareActivity.this, R.string.share_success, Toast.LENGTH_SHORT).show();
                }
            });
		}

		@Override
		public void onError(final WeiboException e) {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(SleepShareActivity.this, e.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
		}

		@Override
		public void onIOException(final IOException e) {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(SleepShareActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
		}
    	
    };
    
    public static Handler mHandler = new Handler() {
    	public void handleMessage(Message msg) {
    		switch(msg.what) {
    		case 1:
    			select = 1;
    			break;
    		}
    	}
    };
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (bmp != null) {
			bmp.recycle();
			bmp = null;
			System.gc();
		}
	}

    @Override
    public void onBackPressed(){
    	super.onBackPressed();
    	overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }
}
