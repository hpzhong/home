package com.zhuoyou.plugin.bluetooth.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.zhuoyou.plugin.running.RunningApp;

public class SmsContentObserver extends ContentObserver {
	   private static final String TAG = "MessageObserver";
	   //message id(long)  <-->  message type(int)
	   private static final Context sContext = RunningApp.getInstance().getApplicationContext();
	   private HashMap<Long, MsgItem> previousMessage;
	   private SmsController mSmsController = null;
	   private final String HEADER = "telecom/msg/";
	   public SmsContentObserver(SmsController smsController) {
	       super(new Handler());
	       mSmsController = smsController;
	       previousMessage = new HashMap<Long, MsgItem>();
	       new DatabaseMonitor(DatabaseMonitor.MONITER_TYPE_ONLY_QUERY).start();       
	   }
	   @Override
	   public void onChange(boolean onSelf) {  
	       super.onChange(onSelf);
	       Log.i(TAG,"DataBase State Changed");
	       new DatabaseMonitor(DatabaseMonitor.MONITER_TYPE_QUERY_AND_NOTIFY).start();         
	   }
	   public class DatabaseMonitor extends Thread {
	       public final static int MONITER_TYPE_ONLY_QUERY = 0;
	       public final static int MONITER_TYPE_QUERY_AND_NOTIFY = 1;

	       private int mQueryType = 0;

	       public DatabaseMonitor(int type) {
	           mQueryType = type;
	       }

	       public void run(){
	           if (MONITER_TYPE_ONLY_QUERY == mQueryType) {
	               query();
	           } else if (MONITER_TYPE_QUERY_AND_NOTIFY == mQueryType) {
	               queryAndNotify();
	           } else {
	               //do nothing
	               Log.i(TAG,"invalid monitor type:"+mQueryType);
	           }
	       }
	       
	       private synchronized void query() {
	           queryMessage(previousMessage);
	           Log.i(TAG,"query: size->"+previousMessage.size());
	       }
	      // @SuppressWarnings("unused")
	       private synchronized void queryAndNotify() {
	           HashMap<Long, MsgItem> currentMessage = new HashMap<Long, MsgItem>();
	           @SuppressWarnings("rawtypes")
	           Iterator iterator;
	           String newFolder;
	           String oldFolder; 
	           queryMessage(currentMessage);

	           Log.i(TAG,"database has been changed, mType is " + " previous size is "+previousMessage.size()+
	                   "current size is "+currentMessage.size());
	       
	           
	       
	           //if previous message is smaller than current, new message is received
				/**
				 * 用上次的个数跟本次比较.如果小的话,说明有新短信
				 */
	           if (previousMessage.size() < currentMessage.size()){
	               //find the new message
	               iterator = currentMessage.entrySet().iterator();
	               while (iterator.hasNext()) {
	                   @SuppressWarnings("rawtypes")
	                   Map.Entry entry = (Map.Entry)iterator.next();
	                   Long key = (Long)entry.getKey();
	                   //messasge is not in previous messages and the type is 
	       
	                   String folder = revertMailboxType(currentMessage.get(key).mType);
						/**
						 * 如果当前的这项在前一次数据中没找到的话,说明这条ID是新的.发送到远端
						 */
	                   if (!previousMessage.containsKey(key) && 
	                       folder != null &&
	                       folder.equals(MapConstants.Mailbox.INBOX)){
	                       mSmsController.onMessageEvent(key,HEADER + folder,MapConstants.EVENT_NEW);
	                       Intent newSMSIntent = new Intent();
	                       newSMSIntent.setAction("com.mtk.btnotification.SMS_RECEIVED");
	                       sContext.sendBroadcast(newSMSIntent);
	                       Log.i("gchk", "检测到新短信.准备发送数据了");
	                   }
	               }
	           }
				/**
				 * 如果上一次数据跟本次个数是一样的.或者上一次的个数大于本次的. prev >= curr
				 */
	           else {
	               iterator = previousMessage.entrySet().iterator();
	               while (iterator.hasNext()) {
	                   @SuppressWarnings("rawtypes")
	                   Map.Entry entry = (Map.Entry)iterator.next();
	                   Long key = (Long)entry.getKey();
	                   //messasge is not in previous messages and the type is 
						/**
						 * 如果当前的列表中没找到以前列表中的那个key.则说明删掉了
						 */
	                   if (!currentMessage.containsKey(key)){
	                	   try {
	                		   oldFolder = revertMailboxType((previousMessage.get(key).mType));
	                		   mSmsController.onMessageEvent(key, HEADER + oldFolder,MapConstants.EVENT_DELETE);   
	                	   }
	                	   catch (Exception e) {
	                		   String errorString = e.toString();
		                       if (errorString == null) {
		                    	   errorString = "querry error";
		                       }
		                       Log.w(TAG, errorString);
	                	   }
	                   } 
	                   else {
	                       oldFolder = revertMailboxType(((MsgItem) entry.getValue()).mType);
	                       newFolder = revertMailboxType((currentMessage.get(key).mType));
	                                               
	                   //  log("id " + key +"oldFolder is " + oldFolder + "new folder is " + newFolder);
	                       
	                       if(newFolder == null || oldFolder == null || oldFolder.equals(newFolder)) {                    	   
	                    	   /**
								 * gchk add
								 * TODO:已读和未读都个数不变.而且都在收件箱中.所以应该在这里做处理.......
								 */
	                    	   if (oldFolder.equals(MapConstants.Mailbox.INBOX) && previousMessage.size() == currentMessage.size()) {
	                    		   int oldRead = ((MsgItem) entry.getValue()).mRead;
	                    		   int newRead = currentMessage.get(key).mRead;
	                    		   if (oldRead == 0 && newRead == 1) {
	                    			   Log.i("gchk", "手机端有读了新短信.数据已经置为已读 ID = " + key);
	                    			   // 吧ID发送出去
	                    			   Intent newSMSIntent = new Intent();
	                    			   newSMSIntent.setAction("com.tyd.btsecretary.SMS_UNREAD_TO_READ");
	                    			   newSMSIntent.putExtra("read_id", key);
	                    			   sContext.sendBroadcast(newSMSIntent);
	                    			   Log.i("gchk", "检测到短信由未读设置成已读.准备发送数据了 KEY=" + key);
	                    		   }
	                    	   }
	                    	   continue;
	                       }
	                       //check to determine message to be deleted or shifted
	                       
	                       if(newFolder.equals(MapConstants.Mailbox.DELETED)) {
	                           //mSmsController.onMessageEvent(key, HEADER + oldFolder,MapConstants.EVENT_SHIFT);
	                       } else {
	                           mSmsController.onMessageEvent(key, HEADER + oldFolder,MapConstants.EVENT_SHIFT);
	                       }                       
	                   }
	               }
	           }
	           previousMessage = currentMessage;   
	       }
	       
	       private void queryMessage(HashMap<Long, MsgItem> info){
	           
	           Cursor messageCursor = null;
	           try {
	               messageCursor =  sContext.getContentResolver().query(Uri.parse(MapConstants.SMS_CONTENT_URI), 
	                                       new String[]{MapConstants._ID, MapConstants.TYPE, MapConstants.READ},
	                                       null,   
	                                       null, 
	                                       null);
	               if (messageCursor != null) {


	                   while(messageCursor.moveToNext()){
	                	   MsgItem item = new MsgItem();
	                	   item.mType = messageCursor.getInt(1);
	                	   item.mRead = messageCursor.getInt(2);
	                       info.put(messageCursor.getLong(0), item);
	                   }
	               }
	           }
	           catch(Exception e) {
	               if (messageCursor != null) {
	                   messageCursor.close();
	               }
	           }
	           finally {
	               if (messageCursor != null) {
	                   messageCursor.close();
	               }

	           }
	          
	       }
	       
	       private String revertMailboxType(int smsMailboxType) {
	           switch(smsMailboxType) {
	               case MapConstants.MESSAGE_TYPE_INBOX:
	                   return MapConstants.Mailbox.INBOX;
	               case MapConstants.MESSAGE_TYPE_OUTBOX:
	                   return MapConstants.Mailbox.OUTBOX;
	               case MapConstants.MESSAGE_TYPE_SENT:
	                   return MapConstants.Mailbox.SENT;
	               case MapConstants.MESSAGE_TYPE_DRAFT:
	                   return MapConstants.Mailbox.DRAFT;
	               default:
	                   return MapConstants.Mailbox.DELETED;
	           }
	       }
	   }
}
