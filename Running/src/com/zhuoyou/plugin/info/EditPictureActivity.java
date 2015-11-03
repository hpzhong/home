package com.zhuoyou.plugin.info;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.zhuoyou.plugin.album.BitmapUtils;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.Tools;

public class EditPictureActivity extends Activity implements OnClickListener, OnTouchListener {

	private ImageView screenshot_imageView;
	private LinearLayout cancel_btn, ok_btn, left_rotate_btn, right_rotate_btn;
	private ClipView clipview;
	private int width;//屏幕宽度
	private int height;//屏幕高度
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();
	DisplayMetrics dm;
	private Bitmap bitmap;
	private Rect rectIV;
	private int statusBarHeight = 0;
	private int titleBarHeight = 0;
	private int bottomBarHeight = 0;
	private int angleInt = 0; //旋转次数
	private int n = 0;//angleInt % 4 的值，用于计算旋转后图片区域
	float minScaleR;//最小缩放比例
	static final float MAX_SCALE = 10f;//最大缩放比例
	// Remember some things for zooming
	PointF prev = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;
	private int mode = NONE;
	private static final int NONE = 0;//初始状态
	private static final int DRAG = 1;//拖动
	private static final int ZOOM = 2;//缩放

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_picture);
        
        setUpViews();
		setUpListeners();
		Intent intent = getIntent();
		Uri uri = intent.getData();
		if (uri != null) {
			bitmap = getBitmapFromUri(uri);
			if (bitmap != null) {
				screenshot_imageView.setImageBitmap(bitmap);
			}
		}
		rectIV = screenshot_imageView.getDrawable().getBounds();
		bottomBarHeight = Tools.dip2px(this, 115);
		getStatusBarHeight();
		minZoom();
		center();
		screenshot_imageView.setImageMatrix(matrix);
    }

	private void setUpViews(){
		screenshot_imageView = (ImageView) findViewById(R.id.imageView);
		cancel_btn = (LinearLayout) findViewById(R.id.cancel_btn);
		ok_btn = (LinearLayout) findViewById(R.id.ok_btn);
		left_rotate_btn = (LinearLayout) findViewById(R.id.left_rotate_btn);
		right_rotate_btn = (LinearLayout) findViewById(R.id.right_rotate_btn);
		clipview = (ClipView) findViewById(R.id.clipview);
		
		dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels;
		screenshot_imageView.setImageMatrix(matrix);
	}

	private void setUpListeners(){
		cancel_btn.setOnClickListener(this);
		ok_btn.setOnClickListener(this);
		left_rotate_btn.setOnClickListener(this);
		right_rotate_btn.setOnClickListener(this);
		screenshot_imageView.setOnTouchListener(this);
	}

	private Bitmap getBitmapFromUri(Uri uri) {
		Bitmap bitmap = null;
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);
			int picWidth = options.outWidth;
			int picHeight = options.outHeight;
			WindowManager windowManager = getWindowManager();
			Display display = windowManager.getDefaultDisplay();
			int screenWidth = display.getWidth();
			int screenHeight = display.getHeight();
			options.inSampleSize = 1;
			if (picWidth > picHeight) {
				if (picWidth > screenWidth)
					options.inSampleSize = picWidth / screenWidth;
			} else {
				if (picHeight > screenHeight)
					options.inSampleSize = picHeight / screenHeight;
			}
			options.inJustDecodeBounds = false;
			bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return bitmap;
	}

	private void getStatusBarHeight(){
		try {
			Class<?> c = Class.forName("com.android.internal.R$dimen");
			Object obj = c.newInstance();
			Field field = c.getField("status_bar_height");  
			int x = Integer.parseInt(field.get(obj).toString());  
			statusBarHeight = getResources().getDimensionPixelSize(x);  
		} catch (Exception e) {
			e.printStackTrace();
		}  
	}

	/**
     * 最小缩放比例，最大为100%
     */
    private void minZoom() {
        minScaleR = Math.min(
                (float) dm.widthPixels / (float) bitmap.getWidth() / 2,
                (float) dm.widthPixels / (float) bitmap.getHeight() / 2);
        if (minScaleR < 1.0/2) {
	        float scale = Math.max(
	        		(float) dm.widthPixels / (float) bitmap.getWidth(),
	                (float) dm.widthPixels / (float) bitmap.getHeight());
	        matrix.postScale(scale, scale);
        } else {
			minScaleR = 1.0f;
		}
    }

    private void center() {
		center(true, true);
	}

	/**
	 * 横向、纵向居中
	 */
	protected void center(boolean horizontal, boolean vertical) {

		Matrix m = new Matrix();
		m.set(matrix);
		RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
		m.mapRect(rect);

		float height = rect.height();
		float width = rect.width();

		float deltaX = 0, deltaY = 0;

		if (vertical) {
			// 图片小于屏幕大小，则居中显示。大于屏幕，上方留空则往上移，下方留空则往下移
			int screenHeight = dm.heightPixels;
			if (height < screenHeight) {
				deltaY = (screenHeight - height - statusBarHeight - bottomBarHeight) / 2 - rect.top;
			} else if (rect.top > 0) {
				deltaY = -rect.top;
			} else if (rect.bottom < screenHeight) {
				deltaY = screenshot_imageView.getHeight() - rect.bottom;
			}
		}

		if (horizontal) {
			int screenWidth = dm.widthPixels;
			if (width < screenWidth) {
				deltaX = (screenWidth - width) / 2 - rect.left;
			} else if (rect.left > 0) {
				deltaX = -rect.left;
			} else if (rect.right > screenWidth) {
				deltaX = (screenWidth - width)/2 - rect.left;
			}
		}
		matrix.postTranslate(deltaX, deltaY);
		if (n % 4 != 0) {
			matrix.postRotate(- 90 * (n % 4), screenshot_imageView.getWidth() / 2, screenshot_imageView.getHeight() / 2);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		ImageView view = (ImageView) v;
		// Handle touch events here...
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		// 主点按下
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			// 设置初始点位置
			prev.set(event.getX(), event.getY());
			if (isOnCP(event.getX(), event.getY())) {
				// 触点在图片区域内
				mode = DRAG;
			} else {
				mode = NONE;
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			// 判断触点是否在图片区域内
			boolean isonpic = isOnCP(event.getX(), event.getY());
			// 如果连续两点距离大于10，则判定为多点模式
			if (oldDist > 10f && isonpic) {
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				// ...
				matrix.set(savedMatrix);
				matrix.postTranslate(event.getX() - prev.x, event.getY() - prev.y);
			} else if (mode == ZOOM) {
				float newDist = spacing(event);
				if (newDist > 10f) {
					matrix.set(savedMatrix);
					float scale = newDist / oldDist;
					matrix.postScale(scale, scale, mid.x, mid.y);
				}
			}
			break;
		}
		view.setImageMatrix(matrix);
		CheckView();
		return true; // indicate event was handled
	}

	/**
	 * 判断点所在的控制点
	 * 
	 * @param evX
	 * @param evY
	 * @return
	 */
	private boolean isOnCP(float evx, float evy) {
        float p[] = new float[9];
        matrix.getValues(p);
        float scale = Math.max(Math.abs(p[0]), Math.abs(p[1]));
        RectF rectf = null;
        switch (n) {
        case 0:
        	rectf = new RectF(p[2], p[5], (p[2] + rectIV.width()*scale), (p[5] + rectIV.height()*scale));
        	break;
		case 1:
			rectf = new RectF(p[2], p[5] - rectIV.width()*scale, p[2] + rectIV.height()*scale, p[5]);
			break;
		case 2:
			rectf = new RectF(p[2] - rectIV.width()*scale, p[5] - rectIV.height()*scale, p[2], p[5]);
			break;
		case 3:
			rectf = new RectF(p[2] - rectIV.height()*scale, p[5], p[2], p[5] + rectIV.width()*scale);
			break;
		}
        if (rectf != null && rectf.contains(evx, evy)) {
			return true;
		}
		return false;
	}

	/** 
	 * 两点的距离
	 * Determine the space between the first two fingers 
	 */
	private float spacing(MotionEvent event)
	{
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/** 
	 * 两点的中点
	 * Calculate the mid point of the first two fingers 
	 * */
	private void midPoint(PointF point, MotionEvent event)
	{
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

    /**
     * 限制最大最小缩放比例
     */
    private void CheckView() {
        float p[] = new float[9];
        matrix.getValues(p);
        float scale = Math.max(Math.abs(p[0]), Math.abs(p[1]));
        if (mode == ZOOM) {
            if (scale < minScaleR) {
            	matrix.setScale(minScaleR, minScaleR);
            	center();
            }
            if (scale > MAX_SCALE) {
                matrix.set(savedMatrix);
            }
        }
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.cancel_btn:
			finish();
			break;
		case R.id.ok_btn:
			Intent intent = new Intent();
			Bitmap fianBitmap = getBitmap();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			fianBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
			byte[] bitmapByte = baos.toByteArray();
			intent.putExtra("bitmap", bitmapByte);
	        setResult(RESULT_OK, intent);
			finish();
			break;
		case R.id.left_rotate_btn:
			n = ++angleInt % 4;
			//图片旋转-90度
			matrix.postRotate(-90, screenshot_imageView.getWidth() / 2, screenshot_imageView.getHeight() / 2);
			savedMatrix.postRotate(-90);
			screenshot_imageView.setImageMatrix(matrix);
			break;
		case R.id.right_rotate_btn:
			n = (3 + angleInt) % 4;
			//图片旋转90度
			matrix.postRotate(90, screenshot_imageView.getWidth() / 2, screenshot_imageView.getHeight() / 2);
			savedMatrix.postRotate(90);
			screenshot_imageView.setImageMatrix(matrix);
			break;
		}
	}

	/*获取矩形区域内的截图*/
	private Bitmap getBitmap() {
		getBarHeight();
		Bitmap screenShoot = takeScreenShot();
		int SX = width;
		Bitmap bitmap = Bitmap.createBitmap(screenShoot, (width - SX) / 2, 
				(height - SX + statusBarHeight + titleBarHeight - bottomBarHeight) / 2, SX, SX);
        Bitmap finalBitmap = BitmapUtils.getCroppedRoundBitmap(bitmap, 102);
		return finalBitmap;
	}

	private void getBarHeight() {
		// 获取状态栏高度
		Rect frame = new Rect();
		this.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		statusBarHeight = frame.top;
		int contenttop = this.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
		// statusBarHeight是上面所求的状态栏的高度
		titleBarHeight = contenttop - statusBarHeight;
	}

	// 获取Activity的截屏
	private Bitmap takeScreenShot() {
		View view = this.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		return view.getDrawingCache();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (bitmap != null) {
			bitmap.recycle();
			bitmap = null;
			System.gc();
		}
	}

}
