package com.zhuoyou.plugin.info;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.Tools;

public class ChooseHeadActivity extends Activity implements ObservableScrollView.Callbacks {

	private Context mContext = RunningApp.getInstance().getApplicationContext();
    private TextView mStickyView;
    private View mPlaceholderView;
    private ObservableScrollView mObservableScrollView;
    private GridView mGridView;
    private HeadAdapter mHeadAdapter;
    private RelativeLayout mDefault_layout, mQq_layout, mWeibo_layout, mCustom_layout;
    private ImageView mDefault_select, mQq_icon, mQq_select, mWeibo_icon, mWeibo_select, mCustom_icon, mCustom_select;
    public static WRHandler mHandler;
	private int selectIndex = -1;
	private int headIndex;
	private Boolean isLogin;
	private String headType;
	private Bitmap bmp = null;
	private Bitmap customeBmp = null;
	private byte[] bitmapByte = null;
	private int[] headIcon1 = Tools.headIcon1;
	private int[] headIcon2 = Tools.headIcon2;
	private int[] headIcon3 = Tools.headIcon3;
	private int[] headIcon4 = Tools.headIcon4;
	private CustomAvatarPopupWindow popupWindow;
	private String filePath;
	private Boolean isCustom = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_head);
		TextView tv_title = (TextView) findViewById(R.id.title);
		tv_title.setText(R.string.choose_head);
		RelativeLayout im_back = (RelativeLayout) findViewById(R.id.back);
		im_back.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		headIndex = Tools.getHead(mContext);
		isLogin = Tools.getLogin(mContext);
		headType = Tools.getHeadType(mContext);
		initView();
		initData();
		
		mHandler = new WRHandler(this);
		filePath = Tools.getSDPath() + "/Running/";
		File dir = new File(filePath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}
	
	static class WRHandler extends Handler {
		WeakReference<ChooseHeadActivity> mChooseHeadActivity;

		public WRHandler(ChooseHeadActivity cha) {
			mChooseHeadActivity = new WeakReference<ChooseHeadActivity>(cha);
		}

		@Override
		public void handleMessage(Message msg) {
			if (mChooseHeadActivity != null) {
				ChooseHeadActivity chooseHeadActivity = mChooseHeadActivity.get();
				if (chooseHeadActivity != null) {
					switch (msg.what) {
					case 1:
						chooseHeadActivity.selectIndex = msg.arg1;
						chooseHeadActivity.mDefault_select.setVisibility(View.GONE);
						chooseHeadActivity.mQq_select.setVisibility(View.GONE);
						chooseHeadActivity.mWeibo_select.setVisibility(View.GONE);
						chooseHeadActivity.mCustom_select.setVisibility(View.GONE);
						break;
					default:
						break;
					}
				}
			}
		}
	}
	
	private void initView() {
        mObservableScrollView = (ObservableScrollView) findViewById(R.id.scroll_view);
        mObservableScrollView.setCallbacks(this);

        mStickyView = (TextView) findViewById(R.id.sticky);
        mPlaceholderView = findViewById(R.id.placeholder);

        mObservableScrollView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        onScrollChanged(mObservableScrollView.getScrollY());
                    }
                });
		if (mObservableScrollView != null) {
			(mObservableScrollView).post(new Runnable() {
				@Override
				public void run() {
					mObservableScrollView.scrollTo(0, 0);
				}
			});
		}
        mGridView = (GridView) findViewById(R.id.head_edit);
        mHeadAdapter = new HeadAdapter(mContext, mGridView);
        mGridView.setAdapter(mHeadAdapter);
        setListViewHeightBasedOnChildren(mGridView);
        
        mDefault_layout = (RelativeLayout) findViewById(R.id.default_layout);
        mDefault_layout.setOnClickListener(OnClickListener);
        mDefault_select = (ImageView) findViewById(R.id.default_select);
        mQq_layout = (RelativeLayout) findViewById(R.id.qq_layout);
        mQq_layout.setOnClickListener(OnClickListener);
        mQq_icon = (ImageView) findViewById(R.id.qq_icon);
        mQq_select = (ImageView) findViewById(R.id.qq_select);
        mWeibo_layout = (RelativeLayout) findViewById(R.id.weibo_layout);
        mWeibo_layout.setOnClickListener(OnClickListener);
        mWeibo_icon = (ImageView) findViewById(R.id.weibo_icon);
        mWeibo_select = (ImageView) findViewById(R.id.weibo_select);
        mCustom_layout = (RelativeLayout) findViewById(R.id.custom_layout);
        mCustom_layout.setOnClickListener(OnClickListener);
        mCustom_icon = (ImageView) findViewById(R.id.custom_icon);
        mCustom_select = (ImageView) findViewById(R.id.custom_select);
	}

	OnClickListener OnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.default_layout:
				selectIndex = -1;
				headIndex = 6;
				mDefault_select.setVisibility(View.VISIBLE);
				mQq_select.setVisibility(View.GONE);
				mWeibo_select.setVisibility(View.GONE);
				mCustom_select.setVisibility(View.GONE);
				mHeadAdapter.notifyDataSetChanged();
				break;
			case R.id.qq_layout:
				selectIndex = -1;
				headIndex = 1000;
				mDefault_select.setVisibility(View.GONE);
				mQq_select.setVisibility(View.VISIBLE);
				mWeibo_select.setVisibility(View.GONE);
				mCustom_select.setVisibility(View.GONE);
				mHeadAdapter.notifyDataSetChanged();
				break;
			case R.id.weibo_layout:
				selectIndex = -1;
				headIndex = 1000;
				mDefault_select.setVisibility(View.GONE);
				mQq_select.setVisibility(View.GONE);
				mWeibo_select.setVisibility(View.VISIBLE);
				mCustom_select.setVisibility(View.GONE);
				mHeadAdapter.notifyDataSetChanged();
				break;
			case R.id.custom_layout:
				popupWindow = new CustomAvatarPopupWindow(mContext, isCustom, OnClickListener);
				popupWindow.showAtLocation(findViewById(R.id.main), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
				break;
			case R.id.tv_select:
				popupWindow.dismiss();
				selectIndex = -1;
				headIndex = 10000;
				mDefault_select.setVisibility(View.GONE);
				mQq_select.setVisibility(View.GONE);
				mWeibo_select.setVisibility(View.GONE);
				mCustom_select.setVisibility(View.VISIBLE);
				mHeadAdapter.notifyDataSetChanged();
				break;
			case R.id.tv_take_photo:
				popupWindow.dismiss();
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(filePath, "temp.jpg")));
				startActivityForResult(intent, 1);
				break;
			case R.id.tv_chose_pic:
				popupWindow.dismiss();
				Intent intent1 = new Intent(Intent.ACTION_PICK, null);
				intent1.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/jpeg");
				startActivityForResult(intent1, 2);
				break;
			}
		}		
	};
	
	private void initData() {
		if (isLogin) {
			bmp = Tools.convertFileToBitmap("/Running/download/logo");
			if (headType.equals("openqq")) {
				mQq_icon.setImageBitmap(bmp);
				mWeibo_layout.setVisibility(View.GONE);
			} else if (headType.equals("openweibo")) {
				mQq_layout.setVisibility(View.GONE);
				mWeibo_icon.setImageBitmap(bmp);
			} else {
				mQq_layout.setVisibility(View.GONE);
				mWeibo_layout.setVisibility(View.GONE);
			}
		} else {
			mQq_layout.setVisibility(View.GONE);
			mWeibo_layout.setVisibility(View.GONE);
		}
		customeBmp = Tools.convertFileToBitmap("/Running/download/custom");
		if (customeBmp != null) {
			isCustom = true;
			mCustom_icon.setImageBitmap(customeBmp);
		}
		
		//add zhaojunhui 20150625
		int headindex=Tools.getHead(mContext);
		if(headindex==6){
			mDefault_select.setVisibility(View.VISIBLE);
		}else if(headindex==10000){
			mCustom_select.setVisibility(View.VISIBLE);
		}
		
	}
	
	private void setListViewHeightBasedOnChildren(GridView gridView) {
		// 获取ListView对应的Adapter
		ListAdapter listAdapter = gridView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int rows;
		int columns = 0;
		int horizontalBorderHeight = 0;
		Class<?> clazz = gridView.getClass();
		try {
			// 利用反射，取得每行显示的个数
			Field column = clazz.getDeclaredField("mRequestedNumColumns");
			column.setAccessible(true);
			columns = (Integer) column.get(gridView);
			// 利用反射，取得横向分割线高度
			Field horizontalSpacing = clazz.getDeclaredField("mRequestedHorizontalSpacing");
			horizontalSpacing.setAccessible(true);
			horizontalBorderHeight = (Integer) horizontalSpacing.get(gridView);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		// 判断数据总数除以每行个数是否整除。不能整除代表有多余，需要加一行
		if (listAdapter.getCount() % columns > 0) {
			rows = listAdapter.getCount() / columns + 1;
		} else {
			rows = listAdapter.getCount() / columns;
		}
		int totalHeight = 0;
		for (int i = 0; i < rows; i++) { // 只计算每项高度*行数
			View listItem = listAdapter.getView(i, null, gridView);
			listItem.measure(0, 0); // 计算子项View 的宽高
			totalHeight += listItem.getMeasuredHeight(); // 统计所有子项的总高度
		}
		ViewGroup.LayoutParams params = gridView.getLayoutParams();
		params.height = totalHeight + horizontalBorderHeight * (rows - 1);// 最后加上分割线总高度
		gridView.setLayoutParams(params);
	}
	
	@Override
	public void onScrollChanged(int scrollY) {
		mStickyView.setTranslationY(Math.max(mPlaceholderView.getTop(), scrollY));
	}

	@Override
	public void onDownMotionEvent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpOrCancelMotionEvent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (bmp != null || customeBmp != null) {
			if (bmp != null) {
				bmp.recycle();
				bmp = null;
			}
			if (customeBmp != null) {
				customeBmp.recycle();
				customeBmp = null;
			}
			System.gc();
		}
	}
	
	@Override
	public void finish() {
		if (selectIndex != -1) {
			int length1 = headIcon1.length;
			int length2 = headIcon2.length;
			int length3 = headIcon3.length;
			int length4 = headIcon4.length;
			if (selectIndex < length1)
				headIndex = selectIndex;
			else if (selectIndex >= length1 && selectIndex < length1 + length2)
				headIndex = selectIndex - length1 + 100;
			else if (selectIndex >= length1 + length2 && selectIndex < length1 + length2 + length3)
				headIndex = selectIndex - length1 - length2 + 200;
			else if (selectIndex >= length1 + length2 + length3 && selectIndex < length1 + length2 + length3 + length4)
				headIndex = selectIndex - length1 - length2 - length3 + 300;
		}
		Intent intent = new Intent();
		intent.putExtra("headIndex", headIndex);
		intent.putExtra("bitmap", bitmapByte);
		setResult(RESULT_OK, intent);
		super.finish();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {
			return;
		}
		Uri uri = null;
		switch (requestCode) {
		case 1:
			File picture = new File(filePath + "/temp.jpg");
			uri = Uri.fromFile(picture);
			startPhotoZoom(uri);
			break;
		case 2:
			uri =data.getData();
        	startPhotoZoom(uri);
            break;
		case 3:
			if (data != null) {
				bitmapByte = data.getByteArrayExtra("bitmap");
				customeBmp = BitmapFactory.decodeByteArray(bitmapByte, 0, bitmapByte.length);
				mCustom_icon.setImageBitmap(customeBmp);
				isCustom = true;
				selectIndex = -1;
				headIndex = 10000;
				mDefault_select.setVisibility(View.GONE);
				mQq_select.setVisibility(View.GONE);
				mWeibo_select.setVisibility(View.GONE);
				mCustom_select.setVisibility(View.VISIBLE);
				mHeadAdapter.notifyDataSetChanged();
			}
			return;
		}
	}
	
	private void startPhotoZoom(Uri uri){
		Intent intent = new Intent(mContext, EditPictureActivity.class);
		intent.setDataAndType(uri, "image/jpeg");
		startActivityForResult(intent, 3);
	}
}
