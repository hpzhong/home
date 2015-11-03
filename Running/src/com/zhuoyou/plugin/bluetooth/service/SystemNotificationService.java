package com.zhuoyou.plugin.bluetooth.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.os.BatteryManager;
import android.util.Log;

import com.zhuoyou.plugin.bluetooth.data.AppList;
import com.zhuoyou.plugin.bluetooth.data.MessageHeader;
import com.zhuoyou.plugin.bluetooth.data.MessageObj;
import com.zhuoyou.plugin.bluetooth.data.NotificationMessageBody;
import com.zhuoyou.plugin.bluetooth.data.Util;
import com.zhuoyou.plugin.running.R;

public class SystemNotificationService extends BroadcastReceiver {
    // Debugging
    private static final String LOG_TAG = "SystemNotificationService";
    
    // Received parameters
    private Context mContext = null;
    private static float mBettryCapacity = 0;
    private static float mLastBettryCapacity = 0;
    
    public SystemNotificationService() {
        Log.i(LOG_TAG, "SystemNotificationService(), SystemNotificationService created!");
    }

	@Override
	public void onReceive(Context context, Intent intent) {
        mContext = context;
        String intentAction = intent.getAction();
        if (Intent.ACTION_BATTERY_LOW.equalsIgnoreCase(intentAction)) {
            if (mLastBettryCapacity == 0) {
                Log.i(LOG_TAG, "mLastBettryCapacity = 0");
                sendLowBatteryMessage(String.valueOf(mBettryCapacity*100));
                mLastBettryCapacity = mBettryCapacity;
            }
            else {
                if (mLastBettryCapacity != mBettryCapacity) {
                    sendLowBatteryMessage(String.valueOf((int)(mBettryCapacity*100)));
                    mLastBettryCapacity = mBettryCapacity;
                }
            }

        }
        else if (Intent.ACTION_BATTERY_CHANGED.equalsIgnoreCase(intentAction)) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1); 
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);     
            float batteryPct = level / (float)scale; 
            mBettryCapacity = batteryPct;
        }
        else if (Intent.ACTION_POWER_CONNECTED.equalsIgnoreCase(intentAction)) {
            mLastBettryCapacity = 0;
        }
        else if (SmsService.SMS_ACTION.equals(intentAction)) {
            int resultCode = getResultCode();
            if(resultCode==Activity.RESULT_OK){
                sendSMSSuccessMessage();
            }else{
                sendSMSFailMessage();
            }
        }
	}

    private void sendLowBatteryMessage(String value) {
        String titile = mContext.getResources().getString(R.string.batterylow);
        String content = mContext.getResources().getString(R.string.pleaseconnectcharger);
        content += ":" + value + "%";
        MessageObj smsMessageData = new MessageObj();
        smsMessageData.setDataHeader(createNotificationHeader());
        smsMessageData.setDataBody(createNotificationBody(titile, content));
        
        // Call main service to send data
        Log.i(LOG_TAG, "sendSmsMessage(), smsMessageData=" + smsMessageData);        
       
        BluetoothService service = BluetoothService.getInstance();
        if (service != null) {
            service.sendSystemNotiMessage(smsMessageData);
        } 
    }

    private MessageHeader createNotificationHeader() {
        // Fill message header
        MessageHeader header = new MessageHeader();
        header.setCategory(MessageObj.CATEGORY_NOTI);
        header.setSubType(MessageObj.SUBTYPE_NOTI);
        header.setMsgId(Util.genMessageId());
        header.setAction(MessageObj.ACTION_ADD);

        Log.i(LOG_TAG, "createSmsHeader(), header=" + header);
        return header;
    }

    private NotificationMessageBody createNotificationBody(String title, String content) {
        // Get message body content
        ApplicationInfo appinfo = mContext.getApplicationInfo();
        String appName = Util.getAppName(mContext, appinfo);
        Bitmap sendIcon = Util.getMessageIcon(mContext, appinfo);
        int timestamp = Util.getUtcTime(System.currentTimeMillis()); 
        String tickerText = "";
        NotificationMessageBody body = new NotificationMessageBody();
        
        if (title == mContext.getResources().getString(R.string.batterylow)) {
            String appID = Util.getKeyFromValue(AppList.BETTRYLOW_APPID);
            body.setAppID(appID);
        }
        else if (title == mContext.getResources().getString(R.string.sms_send)) {
            String appID = Util.getKeyFromValue(AppList.SMSRESULT_APPID);
            body.setAppID(appID);
        }
        // Fill message body

        body.setSender(appName);
        body.setTitle(title);
        body.setContent(content);
        body.setTickerText(tickerText);
        body.setTimestamp(timestamp);
        body.setIcon(sendIcon);
        
        Log.i(LOG_TAG, "createLowBatteryBody(), body=" + body.toString().substring(0,20));
        return body;
    }

    private void sendSMSSuccessMessage() {
        // Get all messages
        
        String titile = mContext.getResources().getString(R.string.sms_send);
        String content = mContext.getResources().getString(R.string.sms_send_success);
        
        Log.i(LOG_TAG, "sendSMSSuccessMessage()"  + titile + content);
        
        MessageObj sendSMSSuccessMessageData = new MessageObj();
        sendSMSSuccessMessageData.setDataHeader(createNotificationHeader());
        sendSMSSuccessMessageData.setDataBody(createNotificationBody(titile, content));

        BluetoothService service = BluetoothService.getInstance();
        if (service != null) {
            service.sendSystemNotiMessage(sendSMSSuccessMessageData);
        } 
    }
    private void sendSMSFailMessage() {
        
        String titile = mContext.getResources().getString(R.string.sms_send);
        String content = mContext.getResources().getString(R.string.sms_send_fail);
        
        Log.i(LOG_TAG, "sendSMSFailMessage()" + titile + content);
        
        MessageObj sendSMSSuccessMessageData = new MessageObj();
        sendSMSSuccessMessageData.setDataHeader(createNotificationHeader());
        sendSMSSuccessMessageData.setDataBody(createNotificationBody(titile, content));

        BluetoothService service = BluetoothService.getInstance();
        if (service != null) {
            service.sendSystemNotiMessage(sendSMSSuccessMessageData);
        } 
    }
}
