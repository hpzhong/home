package com.zhuoyou.plugin.bluetooth.service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RemoteViews;

import com.zhuoyou.plugin.ble.BleManagerService;
import com.zhuoyou.plugin.bluetooth.data.AppList;
import com.zhuoyou.plugin.bluetooth.data.IgnoreList;
import com.zhuoyou.plugin.bluetooth.data.MessageHeader;
import com.zhuoyou.plugin.bluetooth.data.MessageObj;
import com.zhuoyou.plugin.bluetooth.data.NoDataException;
import com.zhuoyou.plugin.bluetooth.data.NotificationMessageBody;
import com.zhuoyou.plugin.bluetooth.data.PreferenceData;
import com.zhuoyou.plugin.bluetooth.data.Util;

public class NotificationService extends AccessibilityService {
    // For get tile and content of notification
    private static final int NOTIFICATION_TITLE_TYPE = 9;
    private static final int NOTIFICATION_CONTENT_TYPE = 10;

    // Avoid propagating events to the client too frequently
    private static final long EVENT_NOTIFICATION_TIMEOUT_MILLIS = 0L;
    private Handler NotificationHandler;
    // Received event
    private AccessibilityEvent mAccessibilityEvent = null;
    
    // Received notification
    private Notification mNotification = null;
    
    private SendNotficationThread mSNThread = null;
    
    private class ThreadNotfication {
    	public String[] textList;
    	public CharSequence packageName;
    	public CharSequence tickerText;
    	public String appID;
    	public long when;
    }
    public NotificationService() {
    	mSNThread = new SendNotficationThread();
        mSNThread.start();
        NotificationHandler = mSNThread.getHandler();
        
    }

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            return;
        }

        // If notification is null, will not forward it
        mAccessibilityEvent = event;
        mNotification = (Notification) mAccessibilityEvent.getParcelableData();
        if (mNotification == null) {
            return;
        }
		
        boolean isServiceEnabled = PreferenceData.isNotificationServiceEnable();
        boolean needForward = PreferenceData.isNeedPush();   
        if (isServiceEnabled && needForward) {

            // Filter notification according to ignore list and exclusion list
        	CharSequence packagenameString;
            HashSet<String> ignoreList = IgnoreList.getInstance().getIgnoreList();
            HashSet<String> exclusionList = IgnoreList.getInstance().getExclusionList();
            
            packagenameString = event.getPackageName();
           
            if (ignoreList.contains(event.getPackageName()) && !exclusionList.contains(event.getPackageName())) {
            	Message message = new Message();
            	message.what = SendNotficationThread.MESSAGE_SEND_NOTIFICATION;
            	ThreadNotfication threadNotfication = new ThreadNotfication();
            	threadNotfication.textList = getNotificationText();
            	
            	threadNotfication.packageName =  mAccessibilityEvent.getPackageName();
            	threadNotfication.appID = Util.getKeyFromValue(threadNotfication.packageName);
            	threadNotfication.tickerText =  mNotification.tickerText;
            	threadNotfication.when = mNotification.when;
            	
            	message.obj = (Object)threadNotfication;
            	if (NotificationHandler == null) {
            		NotificationHandler = mSNThread.getHandler();
            	}
            	if (NotificationHandler != null) {
            		NotificationHandler.sendMessage(message);
            	}
            } else {
                Log.i("caixinxin", "Notice: This notification received!, package name=" + mAccessibilityEvent.getPackageName());
            }
        }
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void onServiceConnected() {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion < 14) {
            setAccessibilityServiceInfo();
        }
        
        BluetoothService.setNotificationService(this);
    }

    private void setAccessibilityServiceInfo() {
        AccessibilityServiceInfo accessibilityServiceInfo = new AccessibilityServiceInfo();
        accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        accessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        accessibilityServiceInfo.notificationTimeout = EVENT_NOTIFICATION_TIMEOUT_MILLIS;        
        setServiceInfo(accessibilityServiceInfo);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        BluetoothService.clearNotificationService();
        
        return false;
    }

    @SuppressLint("UseSparseArrays")
    @SuppressWarnings("unchecked")
    private String[] getNotificationText() {
        RemoteViews remoteViews = mNotification.contentView;
        Class<? extends RemoteViews> remoteViewsClass = remoteViews.getClass();
        HashMap<Integer, String> text = new HashMap<Integer, String>();

        try {
            Field[] outerFields = remoteViewsClass.getDeclaredFields();
            Field actionField = null;
            for (Field outerField : outerFields) {
            	 if (outerField.getName().equals("mActions")) {
            		 actionField = outerField;
            		 break;
                 }
            }
            if (actionField == null) {
            	return null;
            }
            actionField.setAccessible(true);
            ArrayList<Object> actions = (ArrayList<Object>) actionField.get(remoteViews);
            int viewId = 0;
            for (Object action : actions) {
                /*
                 * Get notification tile and content
                 */
                Field[] innerFields = action.getClass().getDeclaredFields();
                
                //RemoteViews curr_action = (RemoteViews)action;
                Object value = null;
                Integer type = null;
                for (Field field : innerFields) {
                    field.setAccessible(true);
                    if (field.getName().equals("value")) {
                        value = field.get(action);
                    } else if (field.getName().equals("type")) {
                        type = field.getInt(action);
                    } else if (field.getName().equals("methodName")) {
                    	String method = (String)field.get(action);
                    	if (method.equals("setProgress")) {
                    		return null;
                    	}
                    }
                }

                // If this notification filed is title or content, save it to text list
                if ((type != null) && ((type == NOTIFICATION_TITLE_TYPE) || (type == NOTIFICATION_CONTENT_TYPE))) {
                    if (value != null) {
                    	viewId++;
                        text.put(viewId, value.toString());
                        if (viewId == 2) {
                        	break;
                        }
                    }
                }
            } 
        }catch (Exception e) {
            e.printStackTrace();
        }

        String[] textArray = text.values().toArray(new String[0]);
        return textArray;
    }
    
    private class SendNotficationThread extends Thread {
    	ThreadNotfication mThreadNotfication = null;
    	 public static final int MESSAGE_SEND_NOTIFICATION = 1;
    	 @SuppressLint("HandlerLeak")
		private Handler mHandler;
    	 
    	 @Override
         public void run() { 
    		 Looper.prepare();
    		 mHandler = new Handler() {
        		 public void handleMessage(Message msg) {
        			 switch (msg.what) {
	    				case MESSAGE_SEND_NOTIFICATION:
	    					mThreadNotfication = (ThreadNotfication)msg.obj;
	    					if (mThreadNotfication != null) {
	    		    			 sendNotfications(mThreadNotfication);
	    		    			 
	    		    			 mThreadNotfication = null;
	    		    			 
	    		    		 }
	    					break;
	
	    				default:
	    					break;
    				}
        		 }
        	 };
        	 
    		 Looper.loop();
    		 
    		
    	 }
    	 
    	 public Handler getHandler() {
    		 return mHandler;
    	 }
    	
    	 private MessageHeader createNotificationHeader() {
    	        // Fill message header
    	        MessageHeader header = new MessageHeader();
    	        header.setCategory(MessageObj.CATEGORY_NOTI);
    	        header.setSubType(MessageObj.SUBTYPE_NOTI);
    	        header.setMsgId(Util.genMessageId());
    	        header.setAction(MessageObj.ACTION_ADD);

    	        return header;
    	    }

    	    private NotificationMessageBody createNotificationBody(ThreadNotfication threadNotfication) {     

    	        // Get message body content
    	        ApplicationInfo appinfo = Util.getAppInfo(getBaseContext(), threadNotfication.packageName);
    	        String appName = Util.getAppName(getBaseContext(), appinfo);
    	        Bitmap sendIcon = Util.getMessageIcon(getBaseContext(), appinfo);
    	        int timestamp = 0;
    	        if ( (System.currentTimeMillis() - threadNotfication.when) > 1000*60*60) {
    	            timestamp = Util.getUtcTime(System.currentTimeMillis());
    	        }
    	        else {
    	            timestamp = Util.getUtcTime(threadNotfication.when);
    	        }
    	        
    	        // Use the Content to Add Applist
    	        Map<Object,Object> applist = AppList.getInstance().getAppList();
    	        if (!applist.containsValue(threadNotfication.packageName)) {
    	            int max = Integer.parseInt(applist.get(AppList.MAX_APP).toString());
    	            applist.remove(AppList.MAX_APP);
    	            max = max +1;
    	            applist.put(AppList.MAX_APP, max);
    	            applist.put(max, threadNotfication.packageName);
    	            AppList.getInstance().saveAppList(applist);
    	        }
    	        
    	        
    	        // Get notification title and content.
    	        String title = "";
    	        String content = "";
    	        String[] textList = threadNotfication.textList;     
    	        if (textList != null) {
    	            if ((textList.length > 0) && (textList[0] != null)) {
    	                title = textList[0];
    	            }            
    	            if ((textList.length > 1) && (textList[1] != null)) {
    	                content = textList[1];
    	            }

    	            // Adjust text length, no longer than TEXT_MAX_LENGH
    	            if (title.length() > Util.TITLE_TEXT_MAX_LENGH) {
    	                title = title.substring(0, Util.TITLE_TEXT_MAX_LENGH) + Util.TEXT_POSTFIX;
    	            }                        
    	            if (content.length() > Util.TEXT_MAX_LENGH) {
    	                content = content.substring(0, Util.TEXT_MAX_LENGH) + Util.TEXT_POSTFIX;
    	            }
    	        }
    	        
    	        // Get notification ticker text.
    	        String tickerText = "";
    	        if (threadNotfication.tickerText != null && content.length() == 0) {
    	            tickerText = threadNotfication.tickerText.toString();
    	        }
    	        if (tickerText.length() > Util.TICKER_TEXT_MAX_LENGH) {
    	            tickerText = tickerText.substring(0, Util.TICKER_TEXT_MAX_LENGH) + Util.TEXT_POSTFIX;
    	        }
    	        if (tickerText.length() > 0) {
    	            String leftBracket = "[";
    	            String rightBracket = "]";
    	            tickerText = leftBracket.concat(tickerText).concat(rightBracket);
    	        }
    	        
    	        // Fill message body
    	        applist = AppList.getInstance().getAppList();
    	        String appID = Util.getKeyFromValue(threadNotfication.packageName);
    	        
    	        NotificationMessageBody body = new NotificationMessageBody();
    	        body.setSender(appName);
    	        body.setAppID(appID);
    	        body.setTitle(title);
    	        body.setContent(content);
    	        body.setTickerText(tickerText);
    	        body.setTimestamp(timestamp);
    	        body.setIcon(sendIcon);

    	        return body;
    	    }
    	public void sendNotfications(ThreadNotfication threadNotfication) {
            try {
                MessageObj notificationMessage = new MessageObj();
                notificationMessage.setDataHeader(createNotificationHeader());
                notificationMessage.setDataBody(createNotificationBody(threadNotfication));
               
                // Test whether the message is valid
                String msgContent = notificationMessage.getDataBody().getContent();

                if (msgContent.length() == 0) {
                	return;
                }
                
                byte[] data = null;
                data = genBytesFromObject(notificationMessage);
                // Call main service to send data
                BluetoothService service = BluetoothService.getInstance();
                if (service != null) {
                    service.sendNotiMessageByData(data);
                }
                noticeBleNewWeChat();
            } catch (Exception e) {
            	if (e != null) {
            		e.printStackTrace();
            		Log.w("Exception during write", e);
            	}
            }
        }
    	
    	public byte[] genBytesFromObject(MessageObj dataObj) {
            if (dataObj == null) {  // No content
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
    	
    	private void noticeBleNewWeChat() {
        	Log.i("NotificationService", "noticeBleNewWeChat");
        	Intent intent = new Intent(BleManagerService.ACTION_NOTICE_NEW_WECHAT_MSG);
    		sendBroadcast(intent);
    	}
    }    
}
