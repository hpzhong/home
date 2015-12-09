package com.zhuoyou.plugin.add;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyou.plugin.album.BitmapUtils;
import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.Tools;

public class AddPicture extends Activity implements OnClickListener {
	private LinearLayout rlayout;
	private PicSelectorPopupWindow popupWindow;
	private TextView tv_start_date, tv_start_time;
	private RelativeLayout rlayout_startDate, rlayout_startTime;
	private ImageView btn_add_bg, btn_add_pic;
	private RelativeLayout im_back;
	private Button mButton;
	private EditText ed;
	private String fileName = "";
	private Intent intent;
	private String wordsExplain;
	private TextView tv_add_pic;
	private String img_uri = "";
	private boolean picAdded = false;
	private String date;
	private long id;
	private Bitmap thumbnailBitmap=null;
	private String name, filePath;
	private String startTime;
	private DateSelectPopupWindow datePopuWindow;
	private SportTimePopupWindow startPopuWindow;
	private int startHour, startOther;
    private  Display display;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		WindowManager windowManager = getWindowManager();
        display = windowManager.getDefaultDisplay();
		setContentView(R.layout.add_pic);
		rlayout = (LinearLayout) findViewById(R.id.rv_main);
		im_back = (RelativeLayout) findViewById(R.id.back);
		im_back.setOnClickListener(this);
		tv_add_pic = (TextView) findViewById(R.id.title);
		tv_add_pic.setText(R.string.add_picture);
		mButton = (Button) findViewById(R.id.save);
		mButton.setOnClickListener(this);
		btn_add_bg = (ImageView) findViewById(R.id.btn_add_bg);
		btn_add_pic = (ImageView) findViewById(R.id.btn_add_pic);
		btn_add_pic.setOnClickListener(this);
		ed = (EditText) findViewById(R.id.ed_some_words);
		tv_start_date = (TextView) findViewById(R.id.tv_start_date);
		tv_start_time = (TextView) findViewById(R.id.tv_chose_start);
		rlayout_startDate = (RelativeLayout) findViewById(R.id.rlayout_startDate);
		rlayout_startTime = (RelativeLayout) findViewById(R.id.rlayout_startTime);
		rlayout_startDate.setOnClickListener(this);
		rlayout_startTime.setOnClickListener(this);
		ed.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		ed.setGravity(Gravity.TOP);
		ed.setSingleLine(false);
		ed.setHorizontallyScrolling(false);

		ed.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				mButton.setText(R.string.ok);
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
		if (date.equals(Tools.getDate(0))) {
			tv_start_date.setText(R.string.today);
		} else {
			tv_start_date.setText(date);
		}
		if (wordsExplain != null) {
			startTime = intent.getStringExtra("startTime");
			img_uri = intent.getStringExtra("imgUri");
			tv_add_pic.setText(R.string.edit_picture);
			ed.setText(wordsExplain);
			ed.setSelection(wordsExplain.length());
			mButton.setText(R.string.gpsdata_delete);
			if (img_uri != null && !img_uri.equals("")) {
				int w = display.getWidth() - (Tools.dip2px(this, 5)*2);
				int h = w / 4 * 3;
				thumbnailBitmap = BitmapUtils.decodeSampledBitmapFromFd2(img_uri, w, h, 2);
				btn_add_bg.setImageBitmap(thumbnailBitmap);
				btn_add_pic.setVisibility(View.GONE);
				fileName = img_uri;
			}
		} else {
			startTime = Tools.getStartTime();
			mButton.setText(R.string.ok);
		}
		tv_start_time.setText(startTime);
		filePath = Tools.getSDPath() + "/Running/sport_album/";
		File dir = new File(filePath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		final WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.alpha = 0.7f;
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
		case R.id.save:
			if (wordsExplain != null) {
				if (mButton.getText().toString().equals(getResources().getString(R.string.ok))) {
					updateDateBasePicture(fileName, ed.getText().toString().replace("," , "，"),date);
					finish();
				} else {
					deleteDateBasePicture();
					finish();
				}
			} else {
				if (picAdded || !ed.getText().toString().equals("")) {
					insertDataBasePicture(date, startTime, fileName, ed.getText().toString().replace("," , "，"), 3, 0);
					finish();
				} else {
					Toast.makeText(this, R.string.no_content, Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.tv_take_photo:
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
			name = format.format(new Date()) + ".jpg";
			Uri imageUri = Uri.fromFile(new File(filePath, name));
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
		case R.id.rlayout_startDate:
			final String finalDate = date;
			datePopuWindow = new DateSelectPopupWindow(AddPicture.this, finalDate);
			datePopuWindow.setColor(0xffb35dc0);
			datePopuWindow.showAtLocation(rlayout, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
			getWindow().setAttributes(lp);
			datePopuWindow.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss() {
					lp.alpha = 1.0f;
					getWindow().setAttributes(lp);
					date = datePopuWindow.getStartDate();
					if (!date.equals(finalDate)) {
						mButton.setText(R.string.ok);
					}
					if (date.equals(Tools.getDate(0))) {
						tv_start_date.setText(R.string.today);
					} else {
						tv_start_date.setText(date);
					}
				}
			});
			break;
		case R.id.rlayout_startTime:
			final String finalStartTime = startTime;
			String[] time = finalStartTime.split(":");
			String hour = time[0];
			String other = time[1];
			if (other.startsWith("0")) {
				startHour = Integer.parseInt(hour.toString());
				other = other.substring(1);
				startOther = Integer.parseInt(other.toString());
			} else {
				startHour = Integer.parseInt(hour.toString());
				startOther = Integer.parseInt(other.toString());
			}
			startPopuWindow = new SportTimePopupWindow(AddPicture.this, startHour, startOther);
			startPopuWindow.showAtLocation(rlayout, Gravity.BOTTOM
					| Gravity.CENTER_HORIZONTAL, 0, 0);
			getWindow().setAttributes(lp);
			startPopuWindow.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					lp.alpha = 1.0f;
					getWindow().setAttributes(lp);
					int minutes = startPopuWindow.getStartTime();
					if (minutes >= 60) {
						if(minutes % 60 < 10){
							startTime = minutes / 60  + ":0" + minutes % 60;
						}else{
							startTime = minutes / 60 + ":" + minutes % 60;
						}
					} else {
						if (minutes == 0) {
							startTime = "00:00";
						} else if (minutes < 10) {
							startTime = "00:0" + minutes;
						}else{
							startTime = "00:" + minutes;
						}
					}
					tv_start_time.setText(startTime);
					if (!startTime.equals(finalStartTime)) {
						mButton.setText(R.string.ok);
					}
				}
			});
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		} else if (requestCode == 0x001) {	
			fileName =filePath + name;
			picAdded = true;
			int w = display.getWidth() - (Tools.dip2px(this, 5)*2);
			int h = w / 4 * 3;
			thumbnailBitmap=BitmapUtils.decodeSampledBitmapFromFd2(fileName, w, h, 2);
			btn_add_bg.setImageBitmap(thumbnailBitmap);
			btn_add_pic.setVisibility(View.GONE);
		} else if (requestCode == 0x002) {
			Uri uri = data.getData();
			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor cursor = managedQuery(uri, proj, null, null, null);
			int colume_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			fileName = cursor.getString(colume_index);
			int w = display.getWidth() - (Tools.dip2px(this, 5)*2);
			int h = w / 4 * 3;
			thumbnailBitmap = BitmapUtils.decodeSampledBitmapFromFd2(fileName, w, h, 2);
			btn_add_bg.setImageBitmap(thumbnailBitmap);
			btn_add_pic.setVisibility(View.GONE);
			picAdded = true;
		}
		if(!fileName.equals(img_uri)){
			mButton.setText(R.string.ok);
		}
	
	}	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("name", name);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (TextUtils.isEmpty(name)) {
			name = savedInstanceState.getString("name");
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

	// 更新数据库中的一条 图片、文字信息(未同步的仍为插入状态)
	private void updateDateBasePicture(String img_uri, String explain,String date) {
		ContentValues updateValues = new ContentValues();
		ContentResolver cr = this.getContentResolver();
		Cursor c = cr.query(DataBaseContants.CONTENT_URI, new String[] { "sync" }, DataBaseContants.ID + " = ? " , new String[] { String.valueOf(id) }, null);
		if (c.getCount() > 0 && c.moveToFirst()) {
			int sync = c.getInt(c.getColumnIndex(DataBaseContants.SYNC_STATE));
			if (sync == 0) {
				updateValues.put(DataBaseContants.DATE, date);
				updateValues.put(DataBaseContants.TIME_START, startTime);
				updateValues.put(DataBaseContants.IMG_URI, img_uri);
				updateValues.put(DataBaseContants.EXPLAIN, explain);
				updateValues.put(DataBaseContants.SYNC_STATE, 0);
			} else {
				updateValues.put(DataBaseContants.DATE, date);
				updateValues.put(DataBaseContants.TIME_START, startTime);
				updateValues.put(DataBaseContants.IMG_URI, img_uri);
				updateValues.put(DataBaseContants.EXPLAIN, explain);
				updateValues.put(DataBaseContants.SYNC_STATE, 2);
			}
		}
		c.close();
		c = null;
		cr.update(DataBaseContants.CONTENT_URI, updateValues, DataBaseContants.ID + " = ? ", new String[] { String.valueOf(id) });
	}

	// 删除数据库中的一条 图片、文字信息
	private void deleteDateBasePicture() {
		ContentResolver cr = this.getContentResolver();
		cr.delete(DataBaseContants.CONTENT_URI, DataBaseContants.ID + " = ?", new String[]{ String.valueOf(id) });
		
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
