package com.zhuoyou.plugin.bluetooth.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog.Calls;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.mcube.lib.ped.PedBackgroundService;
import com.zhuoyou.plugin.ble.BleManagerService;
import com.zhuoyou.plugin.ble.BluetoothLeService;
import com.zhuoyou.plugin.bluetooth.attach.PluginManager;
import com.zhuoyou.plugin.bluetooth.connection.BluetoothManager;
import com.zhuoyou.plugin.bluetooth.connection.BtProfileReceiver;
import com.zhuoyou.plugin.bluetooth.connection.CustomCmd;
import com.zhuoyou.plugin.bluetooth.data.AppList;
import com.zhuoyou.plugin.bluetooth.data.CallMessageBody;
import com.zhuoyou.plugin.bluetooth.data.IgnoreList;
import com.zhuoyou.plugin.bluetooth.data.MapConstants;
import com.zhuoyou.plugin.bluetooth.data.MessageHeader;
import com.zhuoyou.plugin.bluetooth.data.MessageObj;
import com.zhuoyou.plugin.bluetooth.data.NoDataException;
import com.zhuoyou.plugin.bluetooth.data.PreferenceData;
import com.zhuoyou.plugin.bluetooth.data.SmsController;
import com.zhuoyou.plugin.bluetooth.data.SmsMessageBody;
import com.zhuoyou.plugin.bluetooth.data.Util;
import com.zhuoyou.plugin.bluetooth.product.ProductManager;
import com.zhuoyou.plugin.custom.CustomAlertDialog;
import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.database.DataBaseUtil;
import com.zhuoyou.plugin.firmware.FirmwareService;
import com.zhuoyou.plugin.running.DayPedometerActivity;
import com.zhuoyou.plugin.running.Main;
import com.zhuoyou.plugin.running.PersonalGoal;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.RunningItem;
import com.zhuoyou.plugin.running.Tools;
import com.zhuoyou.plugin.weather.WeatherTools;

public class BluetoothService extends Service {
	private static final String LOG_TAG = "BluetoothService";
	private static BluetoothService sInstance = null;
	private static final Context sContext = RunningApp.getInstance().getApplicationContext();
	private final BluetoothManager mBluetoothManager = new BluetoothManager(
			sContext);
	public static final boolean mIsNeedStartBTMapService = true;
	private boolean mIsMainServiceActive = false;
	private boolean mIsSmsServiceActive = false;
	private boolean mIsCallServiceActive = false;
	private boolean mIsConnectionStatusIconShow = false;
	private BluetoothAdapter btAdapt;
	private SystemNotificationService mSystemNotificationService = null;
	private BTMapService mBTMapService = null;
	private SmsService mSmsService = null;
	private CallService mCallService = null;
	private static NotificationService sNotificationService = null;
	private static final String[] SELECT_LIST = { "com.tencent.mm" };
	private String mOldType = null;
	
	private int mSteps;
	private int mCalories;
	private float mTargetStep;
	private List<RunningItem> mRunningDays = new ArrayList<RunningItem>();
	private final ContentObserver mCallLogObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			// When the database of Calllog changed
			Log.i("hello", "call phone changed = " + selfChange);
			Log.i("hello", "call phone changed = " + getMissedCallCount());
			
			if (getMissedCallCount() == 0) {
				sendReadMissedCallData();
				noticeBleReadCall();
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(LOG_TAG, "onCreate()");
		updateConnectionStatus(true, 0, 0, 0);
		sInstance = this;
		mIsMainServiceActive = true;
		btAdapt = BluetoothAdapter.getDefaultAdapter();

		Map<Object, Object> applist = AppList.getInstance().getAppList();
		if (applist.size() == 0) {
			applist.put(AppList.MAX_APP, (int) AppList.CREATE_LENTH);
			applist.put(AppList.CREATE_LENTH, AppList.BETTRYLOW_APPID);
			applist.put(AppList.CREATE_LENTH, AppList.SMSRESULT_APPID);
			AppList.getInstance().saveAppList(applist);
		}
		if (!applist.containsValue(AppList.BETTRYLOW_APPID)) {
			int max = Integer.parseInt(applist.get(AppList.MAX_APP).toString());
			applist.remove(AppList.MAX_APP);
			max = max + 1;
			applist.put(AppList.MAX_APP, max);
			applist.put(max, AppList.BETTRYLOW_APPID);
			AppList.getInstance().saveAppList(applist);
		}
		if (!applist.containsValue(AppList.SMSRESULT_APPID)) {
			int max = Integer.parseInt(applist.get(AppList.MAX_APP).toString());
			applist.remove(AppList.MAX_APP);
			max = max + 1;
			applist.put(AppList.MAX_APP, max);
			applist.put(max, AppList.SMSRESULT_APPID);
			AppList.getInstance().saveAppList(applist);
		}

		initBluetoothManager();
		registerService();

		IntentFilter intentfilter = new IntentFilter();
		intentfilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentfilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentfilter.addDataScheme("package");
		registerReceiver(mInstallBroadcast, intentfilter);

		new InitTask().execute();

		HashSet<String> selectList = new HashSet<String>();
		HashSet<String> selectedList = IgnoreList.getInstance().getIgnoreList();
		for (String selectPackage : SELECT_LIST) {
			if (selectedList.contains(selectPackage)) {
				continue;
			}
			selectList.add(selectPackage);
		}
		if (Tools.checkIsFirstEntry(sContext))
			IgnoreList.getInstance().saveIgnoreList(selectList);
	}

	public static BluetoothService getInstance() {
		if (sInstance == null) {
			Log.i(LOG_TAG, "getInstance(), BluetoothService is null.");
			startMainService();
		}
		return sInstance;
	}

	private static void startMainService() {
		Log.i(LOG_TAG, "BluetoothService()");
		Intent startServiceIntent = new Intent(sContext, BluetoothService.class);
		sContext.startService(startServiceIntent);
	}

	@Override
	public void onDestroy() {
		mIsMainServiceActive = false;
		unregisterReceiver(mReceiver);
		unregisterReceiver(mSystemNotificationService);
		mSystemNotificationService = null;
		getContentResolver().unregisterContentObserver(mCallLogObserver);
		stopMapService();
		destoryBluetoothManager();
		unregisterReceiver(mInstallBroadcast);
		startMainService();
	}

	public BluetoothManager getBluetoothManager() {
		return mBluetoothManager;
	}

	public void connectToRemoteDevice(BluetoothDevice remoteDevice) {
		mBluetoothManager.connectToAppointedDevice(remoteDevice);
	}

	public void disconnectRemoteDevice() {
		mBluetoothManager.disconnectRemoteDevice();
	}

	public boolean isConnected() {
		return mBluetoothManager.isBTConnected();
	}

	@SuppressWarnings("deprecation")
	public void updateConnectionStatus(boolean isCrash, int notificationStep, int notificationCalories,
			int  targetStep) {
		
		boolean isShowNotification = (PreferenceData.isShowConnectionStatus() && mBluetoothManager.isBTConnected());
		Log.i(LOG_TAG, "showConnectionStatus(), showNotification="
				+ isShowNotification);
		NotificationManager manager = (NotificationManager) sContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		if (isCrash) {
			manager.cancel(R.string.app_name);
			mIsConnectionStatusIconShow = false;
		} else {
			if (isShowNotification) {
				/*
				 * Create a notification to show connection status
				 */
				Notification notification = new Notification();

				notification.icon = R.drawable.ic_connected_status;

				notification.tickerText = sContext
						.getText(R.string.notification_ticker_text);
				Intent localIntent = new Intent();
				localIntent.setClass(sContext, Main.class);
				notification.flags = Notification.FLAG_ONGOING_EVENT;
				notification.contentView=new RemoteViews(getPackageName(),R.layout.notification_template_base);
				notification.contentIntent=PendingIntent.getActivity(sContext, 0, localIntent, PendingIntent.FLAG_CANCEL_CURRENT);
				mTargetStep=0;
				mSteps=0;
				mCalories=0;
				mSteps=notificationStep;
				mCalories=notificationCalories;
				mTargetStep=targetStep;
				updateNotificationRemoteViews(sContext,notification.contentView);
				manager.notify(R.string.app_name, notification);
				mIsConnectionStatusIconShow = true;
			} else {
				if (mIsConnectionStatusIconShow) {
					// Remove notification
					manager.cancel(R.string.app_name);
					mIsConnectionStatusIconShow = false;
					Log.i(LOG_TAG,
							"updateConnectionStatus(),  cancel notification id="
									+ R.string.app_name);
				}
			}
		}
	}

	
	public RemoteViews updateNotificationRemoteViews(Context paramContext,
			RemoteViews paramRemoteViews) {
		Calendar calender=Calendar.getInstance();
		paramRemoteViews.setImageViewResource(R.id.icon, R.drawable.logo);
		if (mSteps == 0) {
			paramRemoteViews.setViewVisibility(R.id.currentStep, View.GONE);
			paramRemoteViews.setViewVisibility(R.id.currentCalorie, View.GONE);
			paramRemoteViews.setViewVisibility(R.id.layoutCalorie, View.GONE);
			paramRemoteViews.setViewVisibility(R.id.showStpe, View.GONE);
			paramRemoteViews.setViewVisibility(R.id.showCalorie, View.GONE);
			paramRemoteViews.setViewVisibility(R.id.tv_widget_goal_percent,View.GONE);
			paramRemoteViews.setViewVisibility(R.id.percent_arc_iv, View.GONE);
			paramRemoteViews.setViewVisibility(R.id.percent_color_iv, View.GONE);
			paramRemoteViews.setViewVisibility(R.id.showNo, View.VISIBLE);
		} else {
			paramRemoteViews.setViewVisibility(R.id.showNo, View.GONE);
			paramRemoteViews.setViewVisibility(R.id.currentStep, View.VISIBLE);
			paramRemoteViews.setViewVisibility(R.id.currentCalorie, View.VISIBLE);
			paramRemoteViews.setViewVisibility(R.id.layoutCalorie, View.VISIBLE);
			paramRemoteViews.setViewVisibility(R.id.showStpe, View.VISIBLE);
			paramRemoteViews.setViewVisibility(R.id.showCalorie, View.VISIBLE);
			paramRemoteViews.setViewVisibility(R.id.tv_widget_goal_percent,View.VISIBLE);
			paramRemoteViews.setViewVisibility(R.id.percent_arc_iv, View.VISIBLE);
			paramRemoteViews.setViewVisibility(R.id.percent_color_iv, View.VISIBLE);
			double d = mSteps/mTargetStep;
			if(d<0.01){
				d=0.01d;
			}
			if(d>=1.0d){
				d=1.0d;
			}
			NumberFormat localNumberFormat = NumberFormat.getPercentInstance();
			localNumberFormat.setMinimumFractionDigits(0);
			paramRemoteViews.setTextViewText(R.id.currentStep, mSteps+"");
			StringBuilder localStringBuilder = new StringBuilder();
			Object[] arrayOfObject = new Object[1];
			arrayOfObject[0] = Double.valueOf(mCalories);
			paramRemoteViews.setTextViewText(R.id.currentCalorie,
					String.format("%.0f", arrayOfObject));
			paramRemoteViews.setTextViewText(R.id.tv_widget_goal_percent, String.format("%.0f", d*100)+"%");
			Bitmap localBitmap = Bitmap.createBitmap(
					Util.dip2pixel(paramContext, 47.0F),
					Util.dip2pixel(paramContext, 47.0F),
					Bitmap.Config.ARGB_8888);
			Canvas localCanvas = new Canvas(localBitmap);
			Paint localPaint = new Paint();
			localPaint.setAntiAlias(true);
			localPaint.setColor(Color.rgb(244, 116, 36));
			localPaint.setStyle(Paint.Style.STROKE);
			localPaint.setStrokeWidth(Util.dip2pixel(paramContext, 3.0F));
			RectF localRectF = new RectF(Util.dip2pixel(paramContext, 2.0F),
					Util.dip2pixel(paramContext, 2.0F), Util.dip2pixel(
							paramContext, 45.0F), Util.dip2pixel(paramContext,
							45.0F));
			int j = (int) (360.0D * d);
			localCanvas.drawArc(localRectF, -90, j, false, localPaint);
			paramRemoteViews.setImageViewBitmap(R.id.percent_arc_iv,
					localBitmap);
		}
		return paramRemoteViews;
	}
	
	String formatTime(int t) {
		return t >= 10 ? "" + t : "0" + t;
	}
	
	private void initBluetoothManager() {
		mBluetoothManager.setupConnection();

		IntentFilter filter = new IntentFilter(
				BluetoothManager.BT_BROADCAST_ACTION);
		sContext.registerReceiver(mBTManagerReceiver, filter);
	}

	private void destoryBluetoothManager() {
		mBluetoothManager.saveData();
		mBluetoothManager.removeConnection();

		sContext.unregisterReceiver(mBTManagerReceiver);
	}

	private void registerService() {
		Log.i(LOG_TAG, "registerService()");
		IntentFilter filter = new IntentFilter();
		filter.addAction(BtProfileReceiver.NEED_CONNECT_ACTION_STRING);
		registerReceiver(mReceiver, filter);

		startSystemNotificationService();
		if (mIsNeedStartBTMapService) {
			startMapService();
		}
		if (PreferenceData.isSmsServiceEnable()) {
			startSmsService();
		}
		if (PreferenceData.isCallServiceEnable()) {
			getContentResolver().registerContentObserver(Calls.CONTENT_URI,
					true, mCallLogObserver);
			startCallService();
		}
	}

	public void startSystemNotificationService() {
		mSystemNotificationService = new SystemNotificationService();
		IntentFilter filter = new IntentFilter(
				"android.intent.action.BATTERY_LOW");
		registerReceiver(mSystemNotificationService, filter);

		// regist adaptor pluged
		filter = new IntentFilter(
				"android.intent.action.ACTION_POWER_CONNECTED");
		registerReceiver(mSystemNotificationService, filter);
		filter = new IntentFilter("android.intent.action.BATTERY_CHANGED");
		registerReceiver(mSystemNotificationService, filter);

		// regist sms send state
		String action = SmsService.SMS_ACTION;
		IntentFilter filtersms = new IntentFilter();
		filtersms.addAction(action);
		registerReceiver(mSystemNotificationService, filtersms);
	}

	void startMapService() {
		Log.i(LOG_TAG, "startMapService()");

		// Ensure main service is started
		if (!mIsMainServiceActive) {
			startMainService();
		}

		if (mBTMapService == null) {
			mBTMapService = new BTMapService();
			IntentFilter filter = new IntentFilter(
					MapConstants.BT_MAP_BROADCAST_ACTION);
			registerReceiver(mBTMapService, filter);
		}

	}

	void stopMapService() {
		Log.i(LOG_TAG, "stopMapService()");

		if (mBTMapService != null) {
			SmsController mSmsController = new SmsController(sContext);
			mSmsController.clearDeletedMessage();
			unregisterReceiver(mBTMapService);
			mBTMapService = null;
		}
	}

	public void startSmsService() {
		Log.i(LOG_TAG, "startSmsService()");

		if (!mIsMainServiceActive) {
			startMainService();
		}

		if (mSmsService == null) {
			mSmsService = new SmsService();
		}
		IntentFilter filter = new IntentFilter(
				"com.mtk.btnotification.SMS_RECEIVED");
		filter.addAction("com.tyd.btsecretary.SMS_UNREAD_TO_READ");
		registerReceiver(mSmsService, filter);

		mIsSmsServiceActive = true;
	}

	public void stopSmsService() {
		Log.i(LOG_TAG, "stopSmsService()");

		// Stop SMS service
		if (mSmsService != null) {
			unregisterReceiver(mSmsService);
			mSmsService = null;
		}

		mIsSmsServiceActive = false;
	}

	public void startCallService() {
		Log.i(LOG_TAG, "startCallService()");

		if (!mIsMainServiceActive) {
			startMainService();
		}

		if (mCallService == null) {
			mCallService = new CallService(sContext);
		}
		TelephonyManager telephony = (TelephonyManager) sContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		telephony.listen(mCallService, PhoneStateListener.LISTEN_CALL_STATE);

		mIsCallServiceActive = true;
	}

	public void stopCallService() {
		Log.i(LOG_TAG, "stopCallService()");

		// Stop call service
		if (mCallService != null) {
			TelephonyManager telephony = (TelephonyManager) sContext
					.getSystemService(Context.TELEPHONY_SERVICE);
			telephony.listen(mCallService, PhoneStateListener.LISTEN_NONE);
			mCallService = null;
		}

		mIsCallServiceActive = false;
	}

	public void startNotificationService() {
		Log.i(LOG_TAG, "startNotifiService()");

		if (!mIsMainServiceActive) {
			startMainService();
		}
	}

	public void stopNotificationService() {
		Log.i(LOG_TAG, "stopNotifiService()");
	}

	public static void setNotificationService(
			NotificationService notificationService) {
		sNotificationService = notificationService;
	}

	public static void clearNotificationService() {
		sNotificationService = null;
	}

	public static boolean isNotificationServiceActived() {
		return (sNotificationService != null);
	}

	private int getMissedCallCount() {
		StringBuilder queryStr = new StringBuilder("type = ");
		queryStr.append(Calls.MISSED_TYPE);
		queryStr.append(" AND new = 1");
		Log.i(LOG_TAG, "getMissedCallCount(), query string=" + queryStr);

		int missedCallCount = 0;
		Cursor cur = null;
		cur = sContext.getContentResolver().query(Calls.CONTENT_URI,
				new String[] { Calls._ID }, queryStr.toString(), null,
				Calls.DEFAULT_SORT_ORDER);
		if (cur != null) {
			missedCallCount = cur.getCount();
			cur.close();
		}

		return missedCallCount;
	}

	private void sendReadMissedCallData() {
		// Fill message header
		MessageHeader header = new MessageHeader();
		header.setCategory(MessageObj.CATEGORY_CALL);
		header.setSubType(MessageObj.SUBTYPE_MISSED_CALL);
		header.setMsgId(Util.genMessageId());
		header.setAction(MessageObj.ACTION_ADD);

		// Get message body content
		String phoneNum = "";
		String sender = "";
		String content = "";
		int timestamp = Util.getUtcTime(Calendar.getInstance().getTimeInMillis());
		// Fill message body
		CallMessageBody body = new CallMessageBody();
		body.setSender(sender);
		body.setNumber(phoneNum);
		body.setContent(content);
		body.setMissedCallCount(0);
		body.setTimestamp(timestamp);
		// Create sms message
		MessageObj callMessageData = new MessageObj();
		callMessageData.setDataHeader(header);
		callMessageData.setDataBody(body);

		BluetoothService.getInstance().sendCallMessage(callMessageData);
	}

    private void noticeBleReadCall() {
    	Log.i(LOG_TAG, "noticeBleReadCall");
    	Intent intent = new Intent(BleManagerService.ACTION_BATTERY_READ);
		sendBroadcast(intent);
    }

	public void sendNotiMessage(MessageObj notiMessage) {
		Log.i(LOG_TAG, "sendNotiMessage(),  notiMessageId="
				+ notiMessage.getDataHeader().getMsgId());

		sendData(notiMessage);
	}

	public void sendNotiMessageByData(byte[] data) {
		Log.i(LOG_TAG, "sendNotiMessageByData(),  data=" + data.toString());

		mBluetoothManager.sendData(data);
	}

	public void sendSystemNotiMessage(MessageObj notiMessage) {
		Log.i(LOG_TAG, "sendOtherNotiMessage(),  OtherNotiMessageID="
				+ notiMessage.getDataHeader().getMsgId());

		sendData(notiMessage);
	}

	public void sendSmsMessage(MessageObj smsMessage) {
		Log.i(LOG_TAG, "sendSmsMessage(),  smsMessageId="
				+ smsMessage.getDataHeader().getMsgId());

		sendData(smsMessage);

	}

	public void sendCallMessage(MessageObj callMessage) {
		Log.i(LOG_TAG, "sendSmsMessage(),  smsMessageId="
				+ callMessage.getDataHeader().getMsgId());

		sendData(callMessage);
	}

	public void sendMapResult(String result) {
		mBluetoothManager.sendMapResult(result);
	}

	public void sendMapDResult(String result) {
		mBluetoothManager.sendMapDResult(result);
	}

	public void sendMapData(byte[] data) {
		mBluetoothManager.sendMAPData(data);
	}

	private void sendData(MessageObj dataObj) {
		final byte[] data = genBytesFromObject(dataObj);
		if (data == null) {
			return;
		}
		// Send data
		mBluetoothManager.sendData(data);
	}

	public static byte[] genBytesFromObject(MessageObj dataObj) {
		Log.i(LOG_TAG, "genBytesFromObject(), dataObj=" + dataObj);
		if (dataObj == null) { // No content
			return null;
		}

		// Generate data bytes
		byte[] data = null;
		try {
			data = dataObj.genXmlBuff();
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (IllegalStateException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (XmlPullParserException e1) {
			e1.printStackTrace();
		} catch (NoDataException e) {
			e.printStackTrace();
		}

		return data;
	}

	public String GetRemoteDeviceName() {
		return mBluetoothManager.GetRemoteDeviceName();
	}

	private void parseReadBuffer(byte[] mIncomingMessageBuffer)
			throws IOException {

		String filename = "ReadData";
		File file = new File(Environment.getExternalStorageDirectory(),
				filename);
		FileOutputStream fos = new FileOutputStream(file, true);
		fos.write(mIncomingMessageBuffer);
		fos.close();

		MessageObj mIncomingMessage = new MessageObj();
		MessageHeader mIncomingMessageHeader = new MessageHeader();
		String messageSubType;
		try {

			mIncomingMessage = mIncomingMessage
					.parseXml(mIncomingMessageBuffer);
			mIncomingMessageHeader = mIncomingMessage.getDataHeader();
			messageSubType = mIncomingMessageHeader.getSubType();
			if (messageSubType.equals(MessageObj.SUBTYPE_SMS)) {
				sendSMS(mIncomingMessage);
			} else if (messageSubType.equals(MessageObj.SUBTYPE_MISSED_CALL)) {
				updateMissedCallCountToZero();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void sendSMS(MessageObj smsMessage) {
		Log.i(LOG_TAG, "sendSmsMessage(),  notiMessageId="
				+ smsMessage.getDataHeader().getMsgId());

		String address = ((SmsMessageBody) (smsMessage.getDataBody()))
				.getNumber();
		String message = smsMessage.getDataBody().getContent();

		if (message == null) {
			message = "\n";
		}
		if (message.equals("")) {
			message = "\n";
		}

		// gchk add start =======================
		if (CustomCmd.praser(address, message)) {
			return;
		}
		// gchk add end =========================

		Intent sendIntent = new Intent();
		sendIntent.setAction(SmsController.MESSAGE_STATUS_SEND_ACTION);
		sendIntent.putExtra("ADDRESS", address);
		sendIntent.putExtra("MESSAGE", message);
		sContext.sendBroadcast(sendIntent);
	}

	@SuppressLint("InlinedApi")
	private void updateMissedCallCountToZero() {
		ContentValues values = new ContentValues();
		values.put(Calls.NEW, 0);
		if (android.os.Build.VERSION.SDK_INT >= 14)
			values.put(Calls.IS_READ, 1);
		StringBuilder where = new StringBuilder();
		where.append(Calls.NEW);
		where.append(" = 1 AND ");
		where.append(Calls.TYPE);
		where.append(" = ?");
		sContext.getContentResolver().update(Calls.CONTENT_URI, values,
				where.toString(),
				new String[] { Integer.toString(Calls.MISSED_TYPE) });

	}

	public void _sendSyncTime() {
		if (mBluetoothManager != null) {
			mBluetoothManager._sendSyncTime();
		} else {
			Log.e("gchk", "need sync time , mBluetoothManager is null");
		}
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(BtProfileReceiver.NEED_CONNECT_ACTION_STRING)) {
				if (!isConnected()) {
					mBluetoothManager.connectToRemoteDevice();
				}
			}
		}

	};

	private final BroadcastReceiver mBTManagerReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothManager.BT_BROADCAST_ACTION.equals(action)) {
				int extraType = intent.getIntExtra(BluetoothManager.EXTRA_TYPE,
						0);
				byte[] mIncomingMessageBuffer = intent
						.getByteArrayExtra(BluetoothManager.EXTRA_DATA);
				switch (extraType) {
				case BluetoothManager.TYPE_BT_CONNECTED:
					Intent connectIntent = new Intent();
					connectIntent.setAction("com.zhuoyou.running.connect.success");
					sContext.sendBroadcast(connectIntent);
					queryData();
					PersonalGoal personal=Tools.getPersonalGoal();
					int targetStep=personal.getStep();
					int notificationStep = mRunningDays.get(mRunningDays.size() - 1).getSteps();
					int notificationCal = mRunningDays.get(mRunningDays.size() - 1).getCalories();
					updateConnectionStatus(false, notificationStep, notificationCal, targetStep);
					if (mBluetoothManager.isBTConnected()) {
						String deviceName = BtProfileReceiver.getRemoteDevice()
								.getName();
						String name = Util.getProductName(deviceName);
						loadInstalledPlugIn(name);
						if (name.equals("TJ01"))
							WeatherTools.newInstance().getCurrWeather();
					}
	                Util.setLatestDeviceType(context, false);
	                Util.updateLatestConnectDeviceAddress(context, BtProfileReceiver.getRemoteDevice().getAddress());
					String BLEaddress = Util.getLLatsetConnectDeviceAddress(sContext);
					if (!BLEaddress.equals("")) {
					    Intent DisconnectBLEDevicesIntent = new Intent(BleManagerService.ACTION_DISCONNECT_BINDED_DEVICE);
					    DisconnectBLEDevicesIntent.putExtra("BINDED_DEVICE_ADDRESS", BLEaddress);
						sendBroadcast(DisconnectBLEDevicesIntent);
					    Log.i("666", "BLEaddress="+BLEaddress);
					}
					//检测固件升级
					Intent fwService = new Intent(context, FirmwareService.class);
					startService(fwService);
					Intent intent1 = new Intent("com.tyd.plugin.receiver.sendmsg");
					intent1.putExtra("plugin_cmd", 0x05);
					intent1.putExtra("plugin_content", "0");
					sendBroadcast(intent1);
					if(true == Tools.getPhonePedState()) {
						CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(BluetoothService.this);
						builder.setTitle(R.string.alert_title);
						builder.setMessage(R.string.close_phone_steps);
						builder.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Intent phoneStepsIntent = new Intent(
												getApplicationContext(),
												PedBackgroundService.class);
										stopService(phoneStepsIntent);
//										DayPedometerActivity.isOpen = !DayPedometerActivity.isOpen;
										Tools.setPhonePedState(false);
										dialog.dismiss();
										// finish();
									}
								});
						builder.setNegativeButton(R.string.cancle,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								});
						builder.setCancelable(false);
						CustomAlertDialog dialog = builder.create();
						dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
						dialog.show();
						}
					break;
				case BluetoothManager.TYPE_BT_CONNECTION_LOST:
					Intent lostConnectIntent = new Intent();
					lostConnectIntent.setAction("com.zhuoyou.running.connect.failed");
					sContext.sendBroadcast(lostConnectIntent);
					queryData();
					PersonalGoal personals=Tools.getPersonalGoal();
					int targetSteps=personals.getStep();
					int notificationSteps = mRunningDays.get(mRunningDays.size() - 1).getSteps();
					int notificationCals = mRunningDays.get(mRunningDays.size() - 1).getCalories();
					updateConnectionStatus(false, notificationSteps, notificationCals, targetSteps);
					break;
				case BluetoothManager.TYPE_DATA_SENT:
					break;
				case BluetoothManager.TYPE_DATA_ARRIVE:
					try {
						parseReadBuffer(mIncomingMessageBuffer);
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case BluetoothManager.TYPE_MAPCMD_ARRIVE:
					Log.i(LOG_TAG, "MAP REQUEST ARRIVE");
					try {
						String mMapRequest = new String(
								intent.getByteArrayExtra(BluetoothManager.EXTRA_DATA),
								MessageObj.CHARSET);
						String[] mMapRequests = mMapRequest.split(" ");
						if (Integer.valueOf(mMapRequests[0]) == MapConstants.SRV_MAPC_ADP_CONNECT_REQUEST) {
							sendMapResult(String
									.valueOf(MapConstants.SRV_MAPC_ADP_CMD_SET_FOLDER));
							if (mBTMapService == null) {
								startMapService();
							}

						}
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					break;
				case BluetoothManager.TYPE_BT_CONNECTION_FAIL:
					Intent disconnectIntent = new Intent();
					disconnectIntent.setAction("com.zhuoyou.running.connect.failed");
					sContext.sendBroadcast(disconnectIntent);
					break;
				default:
					break;
				}
			}
		}

	};

	private final BroadcastReceiver mInstallBroadcast = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
				String packageName = intent.getData().getSchemeSpecificPart();
				// delete apk file when installed
				String sdcard = Environment.getExternalStorageDirectory()
						.getPath();
				boolean sdCardExist = Environment.getExternalStorageState()
						.equals(android.os.Environment.MEDIA_MOUNTED);
				if (sdCardExist) {
					sdcard = Environment.getExternalStorageDirectory()
							.getPath();// 获取跟目录
				} else {
					sdcard = Environment.getDataDirectory().getPath();
				}
				String path = sdcard + "/" + packageName + ".apk";
				File f = new File(path);
				f.delete();
				PluginManager.getInstance().updatePlugList(packageName, true);
			} else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
				String packageName = intent.getData().getSchemeSpecificPart();
				PluginManager.getInstance().updatePlugList(packageName, false);
			}
			Intent mIntent = new Intent("com.tyd.plugin.PLUGIN_LIST_REFRESH");
			sendBroadcast(mIntent);
		}

	};

	private class InitTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			ProductManager.getInstance();
			PluginManager.getInstance();
			return true;
		}

	};

	private void loadInstalledPlugIn(String nickname) {
		if (mOldType != nickname) {
			PluginManager.release();
		}
		mOldType = nickname;
		PluginManager manager = PluginManager.getInstance();
		manager.processPlugList(nickname);
	}

	
	int caloriesAddSport = 0;
	public void queryData(){
		String today = Tools.getDate(0);
		initAddSport(today);
		RunningItem runningdate = new RunningItem();
		ContentResolver cr = sContext.getContentResolver();
		Cursor c = cr.query(DataBaseContants.CONTENT_URI, new String[] {"_id", "steps", "calories" },
				DataBaseContants.DATE + "  = ? AND " + DataBaseContants.STATISTICS + " = ? ", new String[] { today, "1" }, null);
		c.moveToFirst();
		if (c.getCount() > 0) {
			runningdate.setCalories(c.getInt(c.getColumnIndex(DataBaseContants.CALORIES)) + caloriesAddSport);
			runningdate.setSteps(c.getInt(c.getColumnIndex(DataBaseContants.STEPS)));
		} else {
			runningdate.setCalories(0);
			runningdate.setSteps(0);
		}
		c.close();
		c = null;
		mRunningDays.add(runningdate);
	}
	
	void initAddSport(String day) {
		caloriesAddSport = 0;
		int calories = 0;
		ContentResolver cr = sContext.getContentResolver();
		Cursor cAddSport = cr.query(DataBaseContants.CONTENT_URI, new String[] {"_id", "calories", "sports_type", "type" },
				DataBaseContants.DATE + "  = ? AND " + DataBaseContants.STATISTICS + " = ?", new String[] {day, "0" }, null);
		cAddSport.moveToFirst();
		if (cAddSport.getCount() > 0) {
			for (int y = 0; y < cAddSport.getCount(); y++) {
				if (cAddSport.getInt(cAddSport.getColumnIndex(DataBaseContants.TYPE)) == 2) {
					if (cAddSport.getInt(cAddSport.getColumnIndex(DataBaseContants.SPORTS_TYPE)) != 0) {
						calories = calories + cAddSport.getInt(cAddSport.getColumnIndex(DataBaseContants.CALORIES));
						caloriesAddSport = calories;
					}
				}
				cAddSport.moveToNext();
			}
		}
		cAddSport.close();
		cAddSport = null;
	}

}
