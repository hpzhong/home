package com.zhuoyou.plugin.autocamera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

public class Main extends Activity {
	private static Main mInstance;
	private boolean isCmd;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private Camera mCamera;
    private Size previewSize;
    private Size pictureSize;
	private boolean isPreview = false;
	private boolean isExit = false;
	private static final String mCapturePath = "/DCIM/autocamera/";
	private String picPath = "";
	private static final String mActionCapture = "com.zhoyou.plugin.autocamera.capture";
	private static final String mActionExit = "com.zhoyou.plugin.autocamera.exit.main";
	private static final int AUTO_EXIT_CAMERA = 1;
	
	public static Main getInstance() {
		return mInstance;
	}
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.preview);
		mInstance = this;
		Log.i("gchk", "main screen createed mInstance = " + mInstance);
		Intent intent = getIntent();
		if (intent != null) {
			isCmd = intent.getBooleanExtra("isCmd", false);
		}
		
		registerBc();
		
		if (PlugTools.isScreenLocked(this)) {
			finish();
		}
	}

	@SuppressWarnings("deprecation")
	private void initCameraFirst() {
		mSurfaceView = (SurfaceView) findViewById(R.id.sView);
		mSurfaceHolder = mSurfaceView.getHolder();
		
		mSurfaceHolder.addCallback(new Callback() {
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			}

			public void surfaceCreated(SurfaceHolder holder) {
				initCamera();
	            updateCameraParameters();
				LayoutParams laParams = (LayoutParams)mSurfaceView.getLayoutParams();
				int width = getScreenWH().widthPixels;
				int height = getScreenWH().heightPixels;
				Log.i("caixinxin", "width = " + width + " height = " + height); 
	            int previewWidth = width;
	            int previewHeight = height;
	            if (previewSize != null) {
	                previewWidth = previewSize.height;
	                previewHeight = previewSize.width;
	            }
				if (width * previewHeight > height * previewWidth) {
					final int scaledChildWidth = previewWidth * height / previewHeight;
		    		Log.i("caixinxin", "scaledChildWidth : " + scaledChildWidth);    
					laParams.width = scaledChildWidth;
					laParams.height = height;
					mSurfaceView.setLayoutParams(laParams);
					mSurfaceView.layout((width - scaledChildWidth) / 2, 0, (width + scaledChildWidth) / 2, height);
				} else {
	                final int scaledChildHeight = previewHeight * width / previewWidth;
		    		Log.i("caixinxin", "scaledChildHeight : " + scaledChildHeight);    
					laParams.width = width;
					laParams.height = scaledChildHeight;
					mSurfaceView.setLayoutParams(laParams);
	                mSurfaceView.layout(0, (height - scaledChildHeight) / 2, width, (height + scaledChildHeight) / 2);
				}
			}

			public void surfaceDestroyed(SurfaceHolder holder) {
				releaseCamera();
			}
		});
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	private void releaseCamera(){
		if (mCamera != null) {
			if (isPreview) {
				mCamera.stopPreview();
				isPreview = false;
			}
			Log.e("gchk", "release camera");
			mCamera.release();
			mCamera = null;
		}
	}
	
	private BroadcastReceiver mReceive = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(mActionCapture)) {
				_doCapture();
			} else if (intent.getAction().equals(mActionExit)) {
            	Log.e("gchk", "exit");
            	isExit = intent.getBooleanExtra("reactive", false);
    			isPreview = false;
    			finish();
			}
		}
	};
	
	private void registerBc(){
		IntentFilter intentF = new IntentFilter();
		intentF.addAction(mActionCapture);
		intentF.addAction(mActionExit);
		registerReceiver(mReceive, intentF);
	}
	
	private void unRegisterBc(){
		unregisterReceiver(mReceive);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		//releaseCamera();
		Log.e("gchk", "mInstance is null");
		finish();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Log.e("gchk", "mInstance is create");
		PlugTools.saveDataString(Main.this, "screen", "Main");
		initCameraFirst();
	}
	
	@Override
	public void onDestroy(){
		releaseCamera();
		unRegisterBc();
		PlugTools.saveDataString(Main.this, "screen", "other");
		if (!isExit) {
			Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
			intent.putExtra("plugin_cmd", 0x54);
			intent.putExtra("plugin_content", "exit");
			sendBroadcast(intent);
		}
		super.onDestroy();
	}
	
	private void initCamera() {
		if (!isPreview) {
			int cameraCount = 0;
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			cameraCount = Camera.getNumberOfCameras(); // get cameras number
			Log.e("gchk", "cameraCount =" + cameraCount);

			if (cameraCount <= 0) {
				Log.e("gchk", "did not find pre camera");
			} else {
				Log.e("gchk", "__________4  ");
				for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
					Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
					Log.e("gchk", "__________5  cameraInfo.facing = " + cameraInfo.facing);
					if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) { // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
						Log.e("gchk", "__________6  ");
						try {
							mCamera = Camera.open(camIdx);// 调用Camera的open()方法打开相机。
							mCamera.setDisplayOrientation(90);
							Log.e("gchk", "__________7  ");
						} catch (RuntimeException e) {
							Log.e("gchk", "__________8  ");
							e.printStackTrace();
						}
					}
				}
			}
		}
		if (mCamera != null && !isPreview) {
			Log.e("gchk", "__________9  ");
			try {
				mCamera.setPreviewDisplay(mSurfaceHolder);
			} catch (IOException e) {
				Log.e("gchk", "__________10  ");
				e.printStackTrace();
			}
			mCamera.startPreview();
			Log.e("gchk", "__________11  ");
			isPreview = true;
			Log.e("gchk", "__________12  ");
			if (isCmd) {
				Log.e("gchk", "preview succssed");
				Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
				intent.putExtra("plugin_cmd", 0x53);
				intent.putExtra("plugin_content", "entryPreview");
				sendBroadcast(intent);
			}
			
			Message msg = mHandler.obtainMessage(AUTO_EXIT_CAMERA);
	        mHandler.sendMessageDelayed(msg, 30000);
		} else {
			if (isCmd) {
				Log.e("gchk", "preview failed");
				Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
				intent.putExtra("plugin_cmd", 0x53);
				intent.putExtra("plugin_content", "entryPreview");
				intent.putExtra("plugin_tag", getTag());
				sendBroadcast(intent);
			}
			finish();
		}
	}

	public PictureCallback myjpegCallback = new PictureCallback() {
		@SuppressLint("SimpleDateFormat")
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.e("gchk", "take picture success");
			final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			if (ExistSDCard()) {
				String sd = getSDPath();
				Log.v("gchk", "sd() = " + sd);
				newFolder(sd + mCapturePath);
				picPath = sd + mCapturePath + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg";
				Log.e("gchk", "picPath = " + picPath);
				File file = new File(picPath);
				FileOutputStream outStream = null;
				try {
					outStream = new FileOutputStream(file);
					Log.e("gchk", "save pic");
					Matrix matrix = new Matrix();
					Bitmap bm = Bitmap.createBitmap(1200, 1200 * bitmap.getHeight() / bitmap.getWidth(), Config.ARGB_8888);// 固定所拍相片大小
					matrix.setScale((float) bm.getWidth() / (float) bitmap.getWidth(), (float) bm.getHeight() / (float) bitmap.getHeight());// 注意参数一定是float哦
					Canvas canvas = new Canvas(bm);
					canvas.drawBitmap(bitmap, matrix, null);
					bm.compress(CompressFormat.JPEG, 40, outStream);
					outStream.close();
					Toast.makeText(Main.this, picPath, Toast.LENGTH_SHORT).show();
					ExifInterface exifInterface = new ExifInterface(picPath);
					exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
					exifInterface.saveAttributes();
					
					if(hasKitkat()) {
						MediaScannerConnection.scanFile(mInstance, new String[] { sd }, new String[]{ "image/*" },
		                        new MediaScannerConnection.OnScanCompletedListener() {
				                    public void onScanCompleted(String path, Uri uri) {
				                        sendBroadcast(new Intent(android.hardware.Camera.ACTION_NEW_PICTURE, uri));
				                        sendBroadcast(new Intent("com.android.camera.NEW_PICTURE", uri));
				                    }
		                		});
						scanPhotos(picPath, mInstance);
					} else {
						sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
					}
				} catch (IOException e) {
					e.printStackTrace();
					Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
					intent.putExtra("plugin_cmd", 0x51);
					intent.putExtra("plugin_content", "capture");
					intent.putExtra("plugin_tag", getTag());
					sendBroadcast(intent);
				}
				Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
				intent.putExtra("plugin_cmd", 0x51);
				intent.putExtra("plugin_content", "capture");
				sendBroadcast(intent);
			} else {
				Log.e("gchk", "SD卡不可用");
				Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
				intent.putExtra("plugin_cmd", 0x51);
				intent.putExtra("plugin_content", "capture");
				intent.putExtra("plugin_tag", getTag());
				sendBroadcast(intent);
			}
			camera.startPreview();
			resetTime();
		}
	};

	public static boolean hasKitkat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }
	
	public static void scanPhotos(String filePath, Context context) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(new File(filePath));
        intent.setData(uri);
        context.sendBroadcast(intent);
    }
	
	public void _doCapture() {
		if (mCamera != null) {
			Log.i("gchk", "camera auto camera");
			mCamera.autoFocus(new AutoFocusCallback() {
				@Override
				public void onAutoFocus(boolean success, Camera camera) {
					Log.e("gchk", "__________15  ");
					mCamera.takePicture(null, null, myjpegCallback);
				}
			});
		}else{
			Log.i("gchk", "camera is null");
			Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
			intent.putExtra("plugin_cmd", 0x51);
			intent.putExtra("plugin_content", "capture");
			intent.putExtra("plugin_tag", getTag());
			sendBroadcast(intent);
		}
	}

	public static void doCapture(){
		Log.i("gchk", "capture doCapture");
		if(mInstance!=null){
			Log.i("gchk", "capture __doCapture");
			mInstance._doCapture();
		}else{
			Log.i("gchk", "main is null");
		}
	}

	public static boolean ExistSDCard() {
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			return true;
		} else
			return false;
	}

	public static String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();
		} else {
			return null;
		}
		return sdDir.toString();
	}

	public static void newFolder(String folderPath) {
		Log.v("gchk", "newFolder() = " + folderPath);
		if (folderPath == null || folderPath.equals("")) {
			return;
		}
		try {
			String filePath = folderPath;
			File myFilePath = new File(filePath);
			if (!myFilePath.exists()) {
				boolean flag = myFilePath.mkdirs();
				Log.v("gchk", "newFolder() = RET" + flag);
				if (flag) {
				} else {
				}
			} else {
				Log.v("gchk", "folderPath is exists " + folderPath);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static char[] getTag() {
		char[] c_tag = new char[4];
		c_tag[0] = 0x21;
		c_tag[1] = 0xFF;
		c_tag[2] = 0xFF;
		c_tag[3] = 0xFF;
		return c_tag;
	}

	private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == AUTO_EXIT_CAMERA)
            {
    			isPreview = false;
    			finish();
            }
        }
    };
    
    private void resetTime() {
        mHandler.removeMessages(AUTO_EXIT_CAMERA);
        Message msg = mHandler.obtainMessage(AUTO_EXIT_CAMERA);
        mHandler.sendMessageDelayed(msg, 30000);
    }
	
    private void updateCameraParameters() {
    	if (mCamera != null) {
    		Camera.Parameters parameters = mCamera.getParameters();
    		
    		pictureSize = findBestPictureSize(parameters);
    		parameters.setPictureSize(pictureSize.width, pictureSize.height);
    		
    		previewSize = findBestPreviewSize(parameters);
    		parameters.setPreviewSize(previewSize.width,previewSize.height);
    		
    		mCamera.setParameters(parameters);
    	}
    }
    
    private Size findBestPictureSize(Camera.Parameters parameters) {
    	int  diff = Integer.MIN_VALUE;
    	List<Size> mSupportedPictureSizes = mCamera.getParameters().getSupportedPictureSizes();    
    	int bestX = 0;
    	int bestY = 0;
    	for(Size pictureSize : mSupportedPictureSizes)
    	{
    		Log.i("caixinxin", "pictureSize : " + pictureSize);    
    		int newX = pictureSize.width;
    		int newY = pictureSize.height;
    		Log.i("caixinxin", "newX = " + newX + "newY = " + newY);   
    		Point screenResolution = new Point (getScreenWH().widthPixels,getScreenWH().heightPixels);
                 
    		int newDiff = Math.abs(newX - screenResolution.y)+Math.abs(newY- screenResolution.x);
    		if(newDiff == diff)
    		{
    			bestX = newX;
    			bestY = newY;
    			break;
    		} else if(newDiff > diff){
    			if ((9 * newX) == (16 * newY)) {
                    bestX = newX;
                    bestY = newY;
                    diff = newDiff;
            	} else if((3 * newX) == (4 * newY)) {
    				bestX = newX;
    				bestY = newY;
    				diff = newDiff;
    			}
    		}
    	}
                 
    	if (bestX > 0 && bestY > 0) {
    		Log.i("caixinxin", "bestX = " + bestX + " bestY = " + bestY);
    		return mCamera.new Size(bestX, bestY);
    	}
    	return null;
    }
    
    private Size findBestPreviewSize(Camera.Parameters parameters) {
    	int diff = Integer.MAX_VALUE;
        List<Size> mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();    
        Log.i("caixinxin", "mSupportedPreviewSizes : " + mSupportedPreviewSizes);
        
        int bestX = 0;
        int bestY = 0;
        for(Size prewsize : mSupportedPreviewSizes) {
			Log.i("caixinxin", "prewsize : " + prewsize);    
			int newX = prewsize.width;
			int newY = prewsize.height;
			Log.i("caixinxin", "newX = " + newX + "newY = " + newY);   
			Point screenResolution = new Point (getScreenWH().widthPixels,getScreenWH().heightPixels);
			Log.i("caixinxin", "screenResolution.x = " + screenResolution.x + "screenResolution.y = " + screenResolution.y);  
            int newDiff = Math.abs(newX - screenResolution.y)+Math.abs(newY- screenResolution.x);
                
            if(newDiff == diff)
            {
                bestX = newX;
                bestY = newY;
                break;
            } else if(newDiff < diff){
            	if ((9 * newX) == (16 * newY)) {
                    bestX = newX;
                    bestY = newY;
                    diff = newDiff;
            	} else if((3 * newX) == (4 * newY)) {
                    bestX = newX;
                    bestY = newY;
                    diff = newDiff;
                }
            }
        }
        if (bestX > 0 && bestY > 0) {
        	Log.i("caixinxin", "bestX = " + bestX + " bestY = " + bestY);  
            return mCamera.new Size(bestX, bestY);
         }
    	return null;
    }
    
    private DisplayMetrics getScreenWH() {
    	return getResources().getDisplayMetrics();
    }
    
}
