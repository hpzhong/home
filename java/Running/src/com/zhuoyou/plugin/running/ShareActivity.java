package com.zhuoyou.plugin.running;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
import com.zhuoyou.plugin.share.AuthorizeActivity;
import com.zhuoyou.plugin.share.ShareTask;
import com.zhuoyou.plugin.share.ShareToWeixin;

public class ShareActivity extends Activity implements OnClickListener {

	private Context mContext;
	private ScrollView mScreenshot;
	private ImageView mUser_logo;
	private TextView mData, mShare_step, mShare_distance, mShare_cal, mShare_food;
	private ImageView mShare_weixin;
	private ImageView mShare_quan;
	private ImageView mShare_qq;
	private ImageView mMore;
	private SharePopupWindow mPopupWindow;
	private String data, food;
	private int step, cal;
	private float distance;
	private static int select = 0;
	private boolean isWXInstalled = false;
	private boolean isWBInstalled = false;
	private boolean isQQInstalled = false;
	private Weibo weibo = Weibo.getInstance();	
	private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
	private Typeface mNumberTP;
	private Bitmap bmp = null;
	private TextView mUser_name;
	private ImageView mTargetState;
	private PersonalGoal mPersonalGoal;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_layout);
		mContext = this;
		mNumberTP = RunningApp.getCustomNumberFont();
			
		Intent intent =  getIntent();
		if(intent!=null){
			Log.i("gchk", intent.getIntExtra("steps" , 0) + "");
			Log.i("gchk", intent.getIntExtra("cals" , 0) + "");
			Log.i("gchk", intent.getFloatExtra("km" , 0) + "");
			Log.i("gchk", intent.getStringExtra("date" ));
			data = intent.getStringExtra("date");
			step = intent.getIntExtra("steps" , 0);
			cal = intent.getIntExtra("cals" , 0);
			distance = intent.getFloatExtra("km" , 0.0f);
		}
		getShareAppStatus();
		initData();
		initView();	
	}

	public void initData() {
		mPersonalGoal = Tools.getPersonalGoal();
	}

	private void initView() {
		mScreenshot = (ScrollView) findViewById(R.id.screenshot);
		mUser_logo = (ImageView) findViewById(R.id.user_logo);
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

		mUser_name = (TextView) findViewById(R.id.user_name);
		if (Tools.getUsrName(this).equals("")) {
			if (!Tools.getLoginName(this).equals("")) {
				mUser_name.setText(Tools.getLoginName(this));
			} else {
				mUser_name.setText(R.string.username);
			}
		} else {
			mUser_name.setText(Tools.getUsrName(this));
		}
		mTargetState = (ImageView) findViewById(R.id.target_state);
		if (mPersonalGoal.mGoalSteps <= step) {
			mTargetState.setImageResource(R.drawable.share_complete_target);
		} else {
			mTargetState.setImageResource(R.drawable.share_no_complete_target);
		}
		mData = (TextView) findViewById(R.id.data);
		mData.setText(data);
		mShare_step = (TextView)findViewById(R.id.share_step);
		mShare_step.setText(step + "");
		mShare_step.setTypeface(mNumberTP);
		mShare_distance = (TextView)findViewById(R.id.share_distance);
		mShare_distance.setText(getResources().getString(R.string.walk) + " " + String.valueOf(distance) + " " + getResources().getString(R.string.kilometre));
		mShare_cal = (TextView)findViewById(R.id.share_cal);
		mShare_cal.setText(cal + "");
		mShare_cal.setTypeface(mNumberTP);
		mShare_food = (TextView)findViewById(R.id.share_food);
		mShare_food.setText("≈" + CalTools.getResultFromCal(mContext, cal));
		mShare_weixin = (ImageView)findViewById(R.id.share_weixin);
		mShare_weixin.setOnClickListener(this);
		mShare_quan = (ImageView)findViewById(R.id.share_quan);
		mShare_quan.setOnClickListener(this);
		mShare_qq = (ImageView)findViewById(R.id.share_qq);
		mShare_qq.setOnClickListener(this);
		mMore = (ImageView)findViewById(R.id.share_more);
		mMore.setOnClickListener(this);
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
			mPopupWindow = new SharePopupWindow(ShareActivity.this, itemsOnClick, fileName);
			mPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
			mPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
			mPopupWindow.showAtLocation(ShareActivity.this.findViewById(R.id.main), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
			break;
		case R.id.img_back:
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
					intent.setClass(ShareActivity.this, AuthorizeActivity.class);
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
                    Toast.makeText(ShareActivity.this, R.string.share_success, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ShareActivity.this, e.getStatusMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ShareActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
