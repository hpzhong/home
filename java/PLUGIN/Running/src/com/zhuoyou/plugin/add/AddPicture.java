package com.zhuoyou.plugin.add;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyou.plugin.album.BitmapUtils;
import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.Tools;

public class AddPicture extends Activity implements OnClickListener {
	private RelativeLayout rlayout;
	private PicSelectorPopupWindow popupWindow;
	private ImageView im_complete, im_delete, im_edit_ok,im_cancle;
	private ImageView btn_add_pic;
	private RelativeLayout im_back;
	private EditText ed;
	private String fileName;
	private Intent intent;
	private String wordsExplain;
	private TextView tv_add_pic;
	private String img_uri;
	private boolean picAdded = false;
	private String date;
	private long id;
	private boolean hasChanged = false;
	private Bitmap thumbnailBitmap=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_pic);
		btn_add_pic = (ImageView) findViewById(R.id.btn_add_pic);
		im_complete = (ImageView) findViewById(R.id.im_complete);
		im_back = (RelativeLayout) findViewById(R.id.back);
		rlayout = (RelativeLayout) findViewById(R.id.rv_main);
		ed = (EditText) findViewById(R.id.ed_some_words);
		im_delete = (ImageView) findViewById(R.id.im_delete);
		im_edit_ok = (ImageView) findViewById(R.id.im_edit_ok);
		tv_add_pic = (TextView) findViewById(R.id.title);
		tv_add_pic.setText(R.string.add_picture);
		im_cancle = (ImageView) findViewById(R.id.im_cancle);
		btn_add_pic.setOnClickListener(this);
		im_back.setOnClickListener(this);
		im_complete.setOnClickListener(this);
		im_delete.setOnClickListener(this);
		im_edit_ok.setOnClickListener(this);

		im_cancle.setOnClickListener(this);

		
		ed.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		ed.setGravity(Gravity.TOP);
		ed.setSingleLine(false);
		ed.setHorizontallyScrolling(false);

		ed.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				hasChanged = true;
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});

		intent = getIntent();
		wordsExplain = intent.getStringExtra("words");
		date = intent.getStringExtra("date");
		id = intent.getLongExtra("id", 0);
		if (wordsExplain != null) {
			img_uri = intent.getStringExtra("imgUri");
			tv_add_pic.setText(R.string.edit_picture);
			im_delete.setVisibility(View.VISIBLE);
			im_edit_ok.setVisibility(View.VISIBLE);
			im_complete.setVisibility(View.GONE);
			ed.setText(wordsExplain);
			ed.setSelection(wordsExplain.length());
			
			int w=Tools.dip2px(AddPicture.this, 150);
			int h=Tools.dip2px(AddPicture.this, 150);
			thumbnailBitmap=BitmapUtils.decodeSampledBitmapFromFd(img_uri, w,h, 3);
			btn_add_pic.setImageBitmap(thumbnailBitmap);
			fileName = img_uri;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		final WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.alpha = 0.7f;
		Uri imageUri = Uri.fromFile(new File(Environment
				.getExternalStorageDirectory(), "yipaoImage.jpg"));
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.btn_add_pic:
			// 将popuwindow 显示在屏幕 正下方
			popupWindow = new PicSelectorPopupWindow(AddPicture.this, this);
			popupWindow.showAtLocation(rlayout, Gravity.BOTTOM
					| Gravity.CENTER_HORIZONTAL, 0, 0);
			getWindow().setAttributes(lp);
			popupWindow.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					// TODO Auto-generated method stub
					lp.alpha = 1.0f;
					getWindow().setAttributes(lp);
				}
			});
			break;
		case R.id.back:
			finish();
			break;
		case R.id.im_edit_ok:
			if(hasChanged){
				updateDateBasePicture(fileName, ed.getText().toString(),date);
			}
			finish();
			break;
		case R.id.im_complete:
			if (!picAdded) {
				Toast.makeText(this, "还没添加内容哦~", 2000).show();
			} else {
				insertDataBasePicture(date, Tools.getStartTime(),
						fileName, ed.getText().toString(), 3, 0);
				finish();
			}

			break;
		case R.id.tv_take_photo:
			intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			startActivityForResult(intent, 0x001);
			popupWindow.dismiss();
			break;
		case R.id.tv_chose_pic:
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("image/jpg");
			startActivityForResult(intent, 0x002);
			popupWindow.dismiss();
			break;
		case R.id.im_delete:
			deleteDateBasePicture(fileName, ed.getText().toString(),date);
			finish();
			break;
		case R.id.im_cancle:
			finish();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		String path = Environment.getExternalStorageDirectory() + "/yipaoImage.jpg" ;
		if (resultCode != RESULT_OK) {
			return;
		} else if (requestCode == 0x001) {
			String name = new DateFormat().format("yyyyMMdd_hhmmss",
			Calendar.getInstance(Locale.CHINA)) + ".jpg";
			String img_uri = Environment
					.getExternalStorageDirectory() + "/yipaoImage.jpg";
			thumbnailBitmap=BitmapUtils.getBitmapFromUrl(img_uri);
			//这一步 将照片 位置摆正。
			thumbnailBitmap = rotate(thumbnailBitmap, readPicDegree(path));
			FileOutputStream b = null;
			fileName = Tools.getSDPath() + "/Running/" + name;
			try {
				b = new FileOutputStream(fileName);
				picAdded = true;
				//将原图保存到 sd卡下的 running 文件夹下。
				thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);
				//生成缩略图，显示在ImageView中。
				int w=Tools.dip2px(AddPicture.this, 150);
				int h=Tools.dip2px(AddPicture.this, 150);
				thumbnailBitmap=BitmapUtils.decodeSampledBitmapFromFd(fileName, w,h, 3);
				btn_add_pic.setImageBitmap(thumbnailBitmap);
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					b.flush();
					b.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else if (requestCode == 0x002) {
			ContentResolver resolver = getContentResolver();
			try {
				Uri uri = data.getData();
				thumbnailBitmap = MediaStore.Images.Media.getBitmap(resolver, uri);
				String[] proj = {MediaStore.Images.Media.DATA};
				Cursor cursor = managedQuery(uri, proj, null, null, null);
				int colume_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				fileName =cursor.getString(colume_index);
				int w=Tools.dip2px(AddPicture.this, 150);
				int h=Tools.dip2px(AddPicture.this, 150);
				thumbnailBitmap=BitmapUtils.decodeSampledBitmapFromFd(fileName, w,h, 3);
				btn_add_pic.setImageBitmap(thumbnailBitmap);
				picAdded = true;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		if(!fileName.equals(img_uri)){
			hasChanged = true;
		}
	
	}	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(thumbnailBitmap!=null){
			thumbnailBitmap.recycle();
			thumbnailBitmap=null;
			System.gc();
		}
	}

	// 向数据库中插入 图片、文字
	private void insertDataBasePicture(String date, String time,
			String img_uri, String explain, int type, int statistics) {
		ContentValues runningItem = new ContentValues();
		runningItem.put(DataBaseContants.ID, Tools.getPKL());
		runningItem.put(DataBaseContants.DATE, date);
		runningItem.put(DataBaseContants.TIME_START, time);
		runningItem.put(DataBaseContants.IMG_URI, img_uri);
		runningItem.put(DataBaseContants.EXPLAIN, explain);
		runningItem.put(DataBaseContants.TYPE, type);
		runningItem.put(DataBaseContants.STATISTICS, statistics);
		this.getContentResolver().insert(DataBaseContants.CONTENT_URI,
				runningItem);
	}

	// 更新数据库中的一条 图片、文字信息
	private void updateDateBasePicture(String img_uri, String explain,String date) {
		ContentResolver cr = this.getContentResolver();
		ContentValues updateValues = new ContentValues();
		updateValues.put(DataBaseContants.IMG_URI, img_uri);
		updateValues.put(DataBaseContants.EXPLAIN, explain);
		updateValues.put(DataBaseContants.SYNC_STATE, 2);
		cr.update(DataBaseContants.CONTENT_URI, updateValues,
				DataBaseContants.ID + " = ? " + " and "+ DataBaseContants.DATE + " = ? ", 
				new String[] { String.valueOf(id), date });
	}

	// 删除数据库中的一条 图片、文字信息
	private void deleteDateBasePicture(String img_uri, String explain,String date) {
		ContentResolver cr = this.getContentResolver();
		cr.delete(DataBaseContants.CONTENT_URI, 
				DataBaseContants.ID + " = ?" + " and " + DataBaseContants.DATE + " = ?", 
				new String[]{ String.valueOf(id), date });
		
		ContentValues values = new ContentValues();
		values.put(DataBaseContants.DELETE_VALUE, id);
		cr.insert(DataBaseContants.CONTENT_DELETE_URI, values);
	}
	
	//得到照片的 角度
	private int readPicDegree(String path){
		int degree= 0;
		ExifInterface exifInterface;
		try {
			exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			default:
				break;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return degree;
	}
	
	//根据照片的角度 旋转照片
	private Bitmap rotate(Bitmap b,int degree){
		if(degree == 0){
			return b;
		}
		if(degree != 0 && b != null){
			Matrix m = new Matrix();
			m.setRotate(degree,(float)b.getWidth(),(float)b.getHeight());
			try {
				Bitmap b2 = Bitmap.createBitmap(b,0,0,b.getWidth(),b.getHeight(),m,true);
				if(b != b2){
					b.recycle();
					b = b2;
				}
			} catch (OutOfMemoryError e) {
				// TODO: handle exception
			}
			
		}
		return b;
	}
		
}
