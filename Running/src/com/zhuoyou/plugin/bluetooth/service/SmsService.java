package com.zhuoyou.plugin.bluetooth.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.zhuoyou.plugin.ble.BleManagerService;
import com.zhuoyou.plugin.bluetooth.connection.CustomCmd;
import com.zhuoyou.plugin.bluetooth.data.MessageHeader;
import com.zhuoyou.plugin.bluetooth.data.MessageObj;
import com.zhuoyou.plugin.bluetooth.data.PreferenceData;
import com.zhuoyou.plugin.bluetooth.data.SmsMessageBody;
import com.zhuoyou.plugin.bluetooth.data.Util;

public class SmsService extends BroadcastReceiver {

    private static final String LOG_TAG = "SmsService";
    private static final String SMS_RECEIVED ="com.mtk.btnotification.SMS_RECEIVED";
    public static final String SMS_ACTION = "SenderSMSFromeFP";
    private static String preID = null;
    // Received parameters
    private Context mContext = null;
    
    public SmsService() {
        Log.i(LOG_TAG, "SmsReceiver(), SmsReceiver created!");
    }

	@Override
	public void onReceive(Context context, Intent intent) {
        Log.i(LOG_TAG, "onReceive()");

        boolean isServiceEnabled = PreferenceData.isSmsServiceEnable();
        boolean needForward = PreferenceData.isNeedPush();        
        if (isServiceEnabled && needForward) {
            mContext = context;
            if (intent.getAction().equals(SMS_RECEIVED)) {
                sendSms();
                noticeBleNewSMS();
            } else if (intent.getAction().equals("com.tyd.btsecretary.SMS_UNREAD_TO_READ")) {
				long key = intent.getLongExtra("read_id", -1);
				if (key == -1) {
					Log.i("gchk", "ID获取不对.发送已读指令失败");
				} else {
					Log.i("gchk", "开始发送");
					char[] c_tag = new char[4];
					c_tag[0] = (char) (key + 0x20);
					c_tag[1] = 0xFF;
					c_tag[2] = 0xFF;
					c_tag[3] = 0xFF;
					CustomCmd.sendCustomCmd(CustomCmd.CMD_REMOTE_READ_SMS, "Hi~Sao nian!", c_tag);
				}
				noticeBleSMSReaded();
            }
        }
	}

    void sendSms() {
        String msgbody;
        String address;
        String ID;
        
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, "_id desc"); 
            
            if (cursor !=null) { 
            while (cursor.moveToNext()) {
                msgbody = cursor.getString(cursor.getColumnIndex("body"));
                address = cursor.getString(cursor.getColumnIndex("address"));
                ID = cursor.getString(cursor.getColumnIndex("_id"));
                if (ID.equals(preID)) {
                    break;
                }
                else {
                    preID = ID;
					Log.i("gchk", "新短信 preID = " + preID);
                    if ( (msgbody != null) && (address != null)) {
                        sendSmsMessage(msgbody, address);
                        break;
                    }
                }
                
                
            }
            }
        }
        catch(Exception e) {
            
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
            
        }
    }

    private void sendSmsMessage(String msgbody, String address) {
        // Get all messages
        // Create message data object
        MessageObj smsMessageData = new MessageObj();
        smsMessageData.setDataHeader(createSmsHeader());
        smsMessageData.setDataBody(createSmsBody(address, msgbody));
        
        // Call main service to send data
        Log.i(LOG_TAG, "sendSmsMessage(), smsMessageData=" + smsMessageData);        
        BluetoothService service = BluetoothService.getInstance();
        if (service != null) {
            service.sendSmsMessage(smsMessageData);
        } 
    }

    private void noticeBleNewSMS() {
    	Log.i(LOG_TAG, "noticeBleNewSMS");
    	Intent intent = new Intent(BleManagerService.ACTION_NOTICE_NEW_SMS);
		mContext.sendBroadcast(intent);
    }    

    private void noticeBleSMSReaded() {
    	Log.i(LOG_TAG, "noticeBleSMSReaded");
    	Intent intent = new Intent(BleManagerService.ACTION_NOTICE_READ_SMS);
		mContext.sendBroadcast(intent);
    }   
    
    private MessageHeader createSmsHeader() {
        // Fill message header
        MessageHeader header = new MessageHeader();
        header.setCategory(MessageObj.CATEGORY_NOTI);
        header.setSubType(MessageObj.SUBTYPE_SMS);
        header.setMsgId(Util.genMessageId());
        header.setAction(MessageObj.ACTION_ADD);

        Log.i(LOG_TAG, "createSmsHeader(), header=" + header);
        return header;
    }

    private SmsMessageBody createSmsBody(String address, String msgbody) {
        // Get message body content
        String phoneNum = address;
        String sender = Util.getContactName(mContext, phoneNum);  //getSenderName(phoneNum);
        String content = msgbody;
        int timestamp = Util.getUtcTime(System.currentTimeMillis());        
        
        // Fill message body
        SmsMessageBody body = new SmsMessageBody();
        body.setSender(sender);
        body.setNumber(phoneNum);
        body.setContent(content);
        body.setTimestamp(timestamp);
		// gchk add
		body.setID(preID);

        Log.i(LOG_TAG, "createSmsBody(), body=" + body);
        return body;
    }

}
