package com.zhuoyou.plugin.bluetooth.service;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.CallLog.Calls;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.zhuoyou.plugin.ble.BleManagerService;
import com.zhuoyou.plugin.bluetooth.data.CallMessageBody;
import com.zhuoyou.plugin.bluetooth.data.MessageHeader;
import com.zhuoyou.plugin.bluetooth.data.MessageObj;
import com.zhuoyou.plugin.bluetooth.data.PreferenceData;
import com.zhuoyou.plugin.bluetooth.data.Util;
import com.zhuoyou.plugin.running.R;

public class CallService extends PhoneStateListener {

    // Debugging
    private static final String LOG_TAG = "CallService";  
    
    private Context mContext = null;
    private int mLastState = TelephonyManager.CALL_STATE_IDLE;
    private String mIncomingNumber = null;
    private Timer mTimer = null;
    private boolean mNeedWaiting = false;
    
    
    public CallService(Context context) {
        Log.i(LOG_TAG, "CallService(), CallService created!");
        mContext = context;
    }

    public void runMissedCallTimer() {
    	if (mTimer != null) {
    		mTimer.cancel();
    		mTimer = null;
    	}
    	TimerTask mTask = new TimerTask() {
			
			@Override
			public void run() {
				if (mTimer != null) {
					mTimer.cancel();
					mTimer = null;
				}
				sendCallMessage();
			}
		};
		
		if (mTimer == null) {
        	mTimer = new Timer();
        }
        mTimer.schedule(mTask,2000);
    }
    
    public void onCallStateChanged(int state, String incomingNumber) {
        Log.i(LOG_TAG, "onCallStateChanged(), state:incomingNumber" + state + ","+ incomingNumber);
        
        if (state == TelephonyManager.CALL_STATE_RINGING) {
            noticeBleNewCall(incomingNumber);
        }
        
        if(state == TelephonyManager.CALL_STATE_OFFHOOK){
        	noticeBleCallEnd();
        }
        
        // If current state is idle and last state is ringing, then it is a miss call. 
        if ((mLastState == TelephonyManager.CALL_STATE_RINGING) && (state == TelephonyManager.CALL_STATE_IDLE)) {
            mIncomingNumber = incomingNumber;
            noticeBleCallEnd();
            
            boolean isServiceEnabled = PreferenceData.isCallServiceEnable();
            boolean needForward = PreferenceData.isNeedPush();                       
            if (isServiceEnabled && needForward) {
                // Process miss call
            	mNeedWaiting = true;
            	runMissedCallTimer();
            }
            
            
        }
        
        // If current state is IDLE and last state is OFFHOOK, then it is hang a call. 
        if ((mLastState == TelephonyManager.CALL_STATE_RINGING) && (state == TelephonyManager.CALL_STATE_IDLE)) {          
            boolean isServiceEnabled = PreferenceData.isCallServiceEnable();
            boolean needForward = PreferenceData.isNeedPush();                       
            if (isServiceEnabled && needForward) {
            	noticeBleCallEnd();
            }
        }
        
        // Save phone state
        mLastState = state;
    }
    
    private void sendCallMessage() {
        // Create message data object
    	int missCallCount = getMissedCallCount();
    	if (missCallCount == 0 && mNeedWaiting) {
    		Log.i(LOG_TAG, "sendCallMessage(), callnumber==0"); 
    		mNeedWaiting = false;
    		runMissedCallTimer();
    		return;
    	}
        MessageObj callMessageData = new MessageObj();
        callMessageData.setDataHeader(createCallHeader());
        callMessageData.setDataBody(createCallBody());
        
        // Call main service to send data
        Log.i(LOG_TAG, "sendCallMessage(), callMessageData=" + callMessageData);        
        BluetoothService service = BluetoothService.getInstance();
        if (service != null) {
            service.sendCallMessage(callMessageData);
        } 
        noticeBleMissCall();
    }

    private MessageHeader createCallHeader() {
        // Fill message header
        MessageHeader header = new MessageHeader();     
        header.setCategory(MessageObj.CATEGORY_CALL);
        header.setSubType(MessageObj.SUBTYPE_MISSED_CALL);        
        header.setMsgId(Util.genMessageId());
        header.setAction(MessageObj.ACTION_ADD);

        Log.i(LOG_TAG, "createCallHeader(), header=" + header);
        return header;
    }

    private CallMessageBody createCallBody() {
        // Get message body content
        String phoneNum = mIncomingNumber;
        String sender = Util.getContactName(mContext, phoneNum);
        String content = getMessageContent(sender);
        int timestamp = Util.getUtcTime(System.currentTimeMillis());
        int missedCallCount = getMissedCallCount();         // Add this missed call
        
        // Fill message body
        CallMessageBody body = new CallMessageBody();
        body.setSender(sender);
        body.setNumber(phoneNum);
        body.setContent(content);
        body.setMissedCallCount(missedCallCount);
        body.setTimestamp(timestamp);

        Log.i(LOG_TAG, "createCallBody(), body=" + body);
        return body;
    }

    private String getMessageContent(String sender) {
        StringBuilder content = new StringBuilder();
        content.append(mContext.getText(R.string.missed_call));
        content.append(": ");
        content.append(sender);
        
        // TODO: Only for test
        content.append("\r\n");
        content.append("Missed Call Count:");
        content.append(getMissedCallCount());
        
        Log.i(LOG_TAG, "getMessageContent(), content=" + content);
        return content.toString();
    }
    
    private int getMissedCallCount() {
        // setup query spec, look for all Missed calls that are new.
        StringBuilder queryStr = new StringBuilder("type = ");
        queryStr.append(Calls.MISSED_TYPE);
        queryStr.append(" AND new = 1");
        Log.i(LOG_TAG, "getMissedCallCount(), query string=" + queryStr);

        // start the query
        int missedCallCount = 0;
        Cursor cur = null;
        cur = mContext.getContentResolver().query(Calls.CONTENT_URI, new String[] { Calls._ID }, queryStr.toString(), null, 
                Calls.DEFAULT_SORT_ORDER);
        if (cur != null) {
            missedCallCount = cur.getCount();
            cur.close();
        }
        
        Log.i(LOG_TAG, "getMissedCallCount(), missed call count=" + missedCallCount);
        return missedCallCount;
    }
    
    private void noticeBleNewCall(String inPhoneNum) {
    	Log.i(LOG_TAG, "noticeBleNewCall");
    	Intent intent = new Intent(BleManagerService.ACTION_NOTICE_NEW_CALL);
    	intent.putExtra("incomingNumber", inPhoneNum);
		mContext.sendBroadcast(intent);
    }    
    
    private void noticeBleCallEnd() {
    	Log.i(LOG_TAG, "noticeBleCallEnd");
    	Intent intent = new Intent(BleManagerService.ACTION_NOTICE_CALL_END);
		mContext.sendBroadcast(intent);
    }
    
    private void noticeBleMissCall() {
    	Log.i(LOG_TAG, "noticeBleMissCall");
    	Intent intent = new Intent(BleManagerService.ACTION_NOTICE_MISS_CALL);
		mContext.sendBroadcast(intent);
    }
}
