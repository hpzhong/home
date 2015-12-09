package com.zhuoyou.plugin.bluetooth.data;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.util.Xml;

import com.zhuoyou.plugin.bluetooth.service.BTMapService;
import com.zhuoyou.plugin.bluetooth.service.BluetoothService;

public class SmsController {
    
    private final String TAG                = "MAP-SmsController";
    private final String MESSAGE_STATUS_SENT_ACTION = 
            "com.mtk.map.SmsController.action.SENT_RESULT";
    private final String MESSAGE_STATUS_DELIVERED_ACTION = 
            "com.mtk.map.SmsController.action.DELIVERED_RESULT";
    private final String EXTRA_MESSAGE_ID =
            "com.mtk.map.SmsController.action.SENT_MESSAGE_ID";
    public static final String MESSAGE_STATUS_SEND_ACTION =
            "com.mtk.map.SmsController.action.SEND_MESSAGE";

    private final int SMS_READ_STATUS           = 1;
    private final int SMS_UNREAD_STATUS         = 0;

    //for delete folder
    private final int MESSAGE_TYPE_DELETE       = 100;
        
    // Didnot use android static field,just use constant string
    private static final String[] DEFAULT_PROJECTION = new String[] {
        MapConstants._ID,            
        MapConstants.SUBJECT,
        MapConstants.DATE,   
        MapConstants.ADDRESS,
        MapConstants.STATUS,
        MapConstants.READ,
        MapConstants.PERSON,
        MapConstants.BODY,
        MapConstants.THREAD_ID,
        MapConstants.TYPE,
        MapConstants.READ // size attribute is the same to mms 
    };
   
    private static final int ID_COLUMN = 0;
   // private static final int SUBJECT_COLUMN = 1;
    private static final int DATE_COLUMN = 2;
    private static final int ADDRESS_COLUMN = 3;
    private static final int STATUS_COLUMN = 4;
    private static final int READ_COLUMN = 5;
   // private static final int PERSON_COLUMN = 6;
    private static final int BODY_COLUMN = 7;
    //private static final int THREAD_ID_COLUMN = 8;
    private static final int TYPE_COLUMN = 9;
    //private static final int SIZE_COLUMN = 10;

    private static final int INVALID_VALUE_ID = -1;
    public static String  mAddress = null;
    public static String  mPerson = null;
    private final Context                     mContext;
    private final ContentResolver             mContentResolver;
    private final HashMap<Long, Integer>      mDeleteFolder = new HashMap<Long, Integer>();

    public SmsController(Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MESSAGE_STATUS_SENT_ACTION);
        filter.addAction(MESSAGE_STATUS_DELIVERED_ACTION);
        filter.addAction(MESSAGE_STATUS_SEND_ACTION);
        BroadcastReceiver mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action == null) {
                    return;
                }
                int resultCode = getResultCode();
                if (action.equals(MESSAGE_STATUS_SENT_ACTION)) {
                    handleSentResult(intent, resultCode);
                } else if (action.equals(MESSAGE_STATUS_DELIVERED_ACTION)) {
                    handleDeliverResult(intent, resultCode);
                }
                if (action.equals(MESSAGE_STATUS_SEND_ACTION)) {
                    String address = intent.getStringExtra("ADDRESS");
                    String message = intent.getStringExtra("MESSAGE");
                    pushMessage(address, message);
                }
            }
        };
        mContext.registerReceiver(mReceiver, filter);
        SmsContentObserver mSmsContentObserver = new SmsContentObserver(this);
        mContentResolver.registerContentObserver(Uri.parse(MapConstants.SMS_CONTENT_URI), false, mSmsContentObserver);
        mContentResolver.registerContentObserver(Uri.parse(MapConstants.CONVERSATION), false, mSmsContentObserver);
    }
    
    public void onStop() {
        clearDeletedMessage();
    }
    public MessageList getMessageList(int listSize, int maxSubjectLen,String folder) {
        int mailbox = convertMailboxType(folder);
        String orignator = null;
        String recipient = null;
        String orignatortAddrList = null;
        String recipientAddrList = null;
        int index = 0;  
        Uri mailboxUri;         
        StringBuilder selection = new StringBuilder();
        ArrayList<String> selectionArgs = new ArrayList<String>();

        String[] projection = DEFAULT_PROJECTION;       
    
        Cursor messageCursor;
        String from = null;
        String to = null;

        if (mailbox != MESSAGE_TYPE_DELETE){
            mailboxUri = getMailboxUri(mailbox);
            if (mailboxUri == null) {
                return null;
            }
        } else {
            mailboxUri = Uri.parse(MapConstants.SMS_CONTENT_URI);
        }
 
        try{
            messageCursor = mContentResolver.query(mailboxUri,projection,
                                selection.toString(),
                                selectionArgs.toArray(new String[selectionArgs.size()]),
                                MapConstants.DEFAULT_SORT_ORDER);                
        } catch (SQLiteException e) {
                e.printStackTrace();
                return null;
        }

        if (messageCursor == null) {
            return null;
        }
        
        MessageList list = new MessageList();
        

        boolean newMessageFlag = false;
        while(messageCursor.moveToNext() && (listSize  == 0 || list.getCurrentSize() < listSize)) {
            if(messageCursor.getInt(READ_COLUMN) == SMS_READ_STATUS) {
                newMessageFlag = true;
            }           
            
            String address = messageCursor.getString(ADDRESS_COLUMN);
            if (mailbox == MapConstants.MESSAGE_TYPE_INBOX) {
                from = address;
            } else {
                to = address;
            }
            
            //recipient filter: we focus on box except Inbox
            if (recipient != null && recipient.length() > 0 ) { 
                if (doesPhoneNumberMatch(normalizeString(to), recipientAddrList, recipient)) {
                    continue;
                }
            }
            
            //orignator filter
            if (orignator!= null && orignator.length() > 0 && (mailbox == MapConstants.MESSAGE_TYPE_INBOX)) {
                if (doesPhoneNumberMatch(normalizeString(from), orignatortAddrList, orignator)) {
                    continue;
                }
            }
                
            if (listSize > 0 ) {
                list.addMessageItem(composeMessageItem(messageCursor, mailbox, maxSubjectLen));
            }

            index ++;
            list.addSize(1);
                
        }

        messageCursor.close();

        if (newMessageFlag) {
            list.setNewMessage();
        }
        list.addSize(index);    
        return list;
    }

    /*notes: for SMS deliver PDU(that is, messages in inbox), encoding is not used*/
    /*but the charset is UTF-8*/
    /*so we have to confirm the charset of text in provider */
    public  BMessage getMessage(long id) {         
        id = id & MapConstants.MESSAGE_HANDLE_MASK;
        Uri uri =ContentUris.withAppendedId(Uri.parse(MapConstants.SMS_CONTENT_URI), id);
        
        Cursor messageCursor = mContentResolver.query(uri, DEFAULT_PROJECTION,
                            null,
                            null,
                            null);

        if(messageCursor == null || !messageCursor.moveToFirst()) {
            return null;
        }

        String text = messageCursor.getString(BODY_COLUMN);
        String address = messageCursor.getString(ADDRESS_COLUMN);
    
        int mailbox = messageCursor.getInt(TYPE_COLUMN);

        String orignator = new String();
        String recipient = new String();

        if (mailbox == MapConstants.MESSAGE_TYPE_INBOX ){
            orignator = address;
            normalizeString(orignator);
            
        } else {
            recipient = address;
            normalizeString(recipient);
        }
        
        BMessage bMessage = new BMessage();
        bMessage.reset();

        // orignator
        VCard vCard = new VCard();
        vCard.setTelephone(orignator);
        
        bMessage.setOrignator(vCard.toString());
        bMessage.setContent(text);
        vCard.reset();
        vCard.setTelephone(recipient);
        bMessage.addRecipient(vCard.toString());
        
        bMessage.setReadStatus(revertReadStatus(messageCursor.getInt(READ_COLUMN)));

        messageCursor.close();
        return bMessage;
            
    }
    
    
    public boolean pushMessage(String telephone, String text) {
    	try {
    		Log.i(TAG, "Start to Push message, the telephone is:" + telephone + " and the text is:" + text);
		} catch (Exception e) {
			String errorString = e.toString();
            if (errorString == null) {
         	   errorString = "push error";
            }
            Log.w(TAG, errorString);
            BluetoothService.getInstance().sendMapResult(String.valueOf(MapConstants.SRV_MAPC_ADP_CMD_PUSH_MSG));
		}
        
        String recipient = null;
        long messageId = -1;
        boolean isSave;
        int read ;
        isSave = true;

        if ( (text != null) && !text.equals("\n") ){
            text = text.trim();
        }  
        recipient = normalizeString(telephone);
        //save message 
        if (isSave) {
            ContentValues cv = new ContentValues();
            
            cv.put(MapConstants.TYPE, MapConstants.MESSAGE_TYPE_OUTBOX);
            cv.put(MapConstants.DATE, System.currentTimeMillis());
            cv.put(MapConstants.ADDRESS, recipient);
            

            read = MapConstants.READ_STATUS;
            cv.put(MapConstants.READ,read);
            cv.put(MapConstants.BODY,text);
            cv.put(MapConstants.STATUS,MapConstants.STATUS_PENDING);
            
            cv.put(MapConstants.SEEN, 0);
  
            Uri uri = mContentResolver.insert(Uri.parse(MapConstants.OUTBOX),cv);
            if (uri != null) {
                Cursor cs = mContentResolver.query(uri, new String[]{MapConstants._ID},null,null,null);
                if (cs != null && cs.moveToFirst()) {
                    messageId = cs.getLong(0);  
                    cs.close();
                }
            }
        } else {
            //if we donot save the message, we have to assign a unique handle for the message
            messageId = INVALID_VALUE_ID;
        }

        if (recipient != null){
            //send message:
            SmsManager manager = SmsManager.getDefault();       
            ArrayList<String> messages;
            if (text == null) return false;
            messages = manager.divideMessage(text);

            ArrayList<PendingIntent> deliveryIntents =  new ArrayList<PendingIntent>(messages.size());
            ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>(messages.size());
            for (int i = 0; i < messages.size(); i++) {
                //deliver intent and sendIntent
                Intent sendIntent  = new Intent(MESSAGE_STATUS_SENT_ACTION);
                Intent deliveryIntent  = new Intent(MESSAGE_STATUS_DELIVERED_ACTION);
                sendIntent.putExtra(EXTRA_MESSAGE_ID, messageId);
                
                deliveryIntent.putExtra(EXTRA_MESSAGE_ID, messageId);
                if (i == messages.size() -1) {
                    String EXTRA_FINAL_MESSAGE = "com.mtk.map.SmsController.action.FINAL_MESSAGE";
                    sendIntent.putExtra(EXTRA_FINAL_MESSAGE, true);
                    deliveryIntent.putExtra(EXTRA_FINAL_MESSAGE, true);
                }
                deliveryIntents.add(PendingIntent.getBroadcast(
                        mContext, 0,
                        deliveryIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT));
                sentIntents.add(PendingIntent.getBroadcast(
                        mContext, i, 
                        sendIntent, PendingIntent.FLAG_CANCEL_CURRENT));
            }
            manager.sendMultipartTextMessage(recipient, null, messages, sentIntents, deliveryIntents);
            
        }
        return true;  
    }
    
    public boolean setMessageStatus(long id, int state) {
    	BluetoothService service = BluetoothService.getInstance();
        Uri uri =ContentUris.withAppendedId(Uri.parse(MapConstants.SMS_CONTENT_URI), id);
        String[] projection = new String[]{
            MapConstants.READ};
        int newState = state;

        if (newState == -1) {
            return false;
        }
        Cursor cs =  mContentResolver.query(uri, projection, null, null, null);
        if(cs != null && cs.moveToFirst()) {
            
            if (cs.getInt(0) == newState) {
                service.sendMapResult(String.valueOf(-MapConstants.SRV_MAPC_ADP_CMD_SET_STATUS));
            } else {
                ContentValues cv = new ContentValues();
                cv.put(MapConstants.READ, Integer.valueOf(newState));
                mContentResolver.update(uri, cv, null, null);
                service.sendMapResult(String.valueOf(MapConstants.SRV_MAPC_ADP_CMD_SET_STATUS));
            }
            cs.close();
        }
        else {
            service.sendMapResult(String.valueOf(-MapConstants.SRV_MAPC_ADP_CMD_SET_STATUS));
        }
        return true;
        
    }
    
    public  boolean deleteMessage(long id) {
        boolean flag;
        BluetoothService service = BluetoothService.getInstance();
        Uri uri =ContentUris.withAppendedId(Uri.parse(MapConstants.SMS_CONTENT_URI), id);
        String[] projection = new String[]{MapConstants.TYPE};

        Cursor cs =  mContentResolver.query(uri, projection, null, null, null);
        if(cs != null && cs.moveToFirst()) {
            int mailbox = cs.getInt(0);
            if (mailbox == MESSAGE_TYPE_DELETE) {
                mContentResolver.delete(uri,null,null);
                mDeleteFolder.remove(Long.valueOf(id));
            } else {
                ContentValues cv = new ContentValues();
                cv.put(MapConstants.TYPE, Integer.valueOf(MESSAGE_TYPE_DELETE));
                mContentResolver.update(uri, cv, null, null);
                mDeleteFolder.put(Long.valueOf(id), Integer.valueOf(mailbox));
            }
            service.sendMapResult(String.valueOf(MapConstants.SRV_MAPC_ADP_CMD_SET_STATUS));
            flag = true;
            cs.close();
        } else {
            service.sendMapResult(String.valueOf(-MapConstants.SRV_MAPC_ADP_CMD_SET_STATUS));
            flag = false;
        }
        //clearDeletedMessage();
        return flag;
    }
    public void clearDeletedMessage(){
        Uri uri;
        Long id;
        String[] projection = new String[]{MapConstants.TYPE};
        int mailbox;

        uri = Uri.parse(MapConstants.SMS_CONTENT_URI);
        
        Iterator<?> iterator= mDeleteFolder.entrySet().iterator();
        while (iterator.hasNext()) {
                @SuppressWarnings("rawtypes")
                Map.Entry entry = (Map.Entry)iterator.next();
                id = (Long)entry.getKey();  
                uri =ContentUris.withAppendedId(Uri.parse(MapConstants.SMS_CONTENT_URI), id);

                //we have to confirm the message is truly in deleted folder
                Cursor cs = mContentResolver.query(uri,projection,null,null,null);
                if (cs != null && cs.moveToFirst()) {
                    mailbox = cs.getInt(0);
                    if (mailbox == MESSAGE_TYPE_DELETE) {
                        //maybe IllegalArgumentException will be thrown when delete message from ICC
                        try {
                            mContentResolver.delete(uri,null,null);
                        } catch (IllegalArgumentException e) {
                        }
                    }
                }
                if (cs != null){
                    cs.close();
                }
        }
        mDeleteFolder.clear();      
    }
    
    public void onMessageEvent(Long key, String oldFolder, int type) {
        if (MapConstants.EVENT_DELETE_S.equals(getEventType(type))) {
            BTMapService.mKeys.add(key);
        }
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter stringWriter = new StringWriter();
        try { 
            serializer.setOutput(stringWriter);
            serializer.startDocument(MessageObj.CHARSET, false); 
            serializer.startTag(null, "MAP-event-report");
            serializer.attribute(null, "version", "1.0");
            serializer.startTag(null, "event"); 
            serializer.attribute(null, "type", getEventType(type));
            serializer.attribute(null, "handle", String.valueOf(key | MapConstants.SMS_GSM_HANDLE_BASE));
            serializer.attribute(null, "folder", oldFolder);
            serializer.attribute(null, "msg_type", MapConstants.MESSAGE_TYPE_SMS_GSM);
            serializer.endTag(null, "event"); 
            serializer.endTag(null, "MAP-event-report"); 
            serializer.endDocument(); 
            serializer.flush(); 
        } catch (Exception e) { 
        }
        BluetoothService service = BluetoothService.getInstance();
        if (stringWriter != null) {
            byte[] dataOfEventReport;
            try {
                dataOfEventReport = stringWriter.toString().getBytes(MessageObj.CHARSET);
                String cmdOfList = String.valueOf(MapConstants.SRV_MAPC_ADP_EVENT_REPORT) + MapConstants.MAPD_WITH_XML + String.valueOf(dataOfEventReport.length) + " ";
                service.sendMapDResult(cmdOfList);
                service.sendMapData(dataOfEventReport);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
        
    }
 
    private String getEventType(int type) {
        switch(type) {
        case MapConstants.EVENT_DELETE:
            return MapConstants.EVENT_DELETE_S;
        case MapConstants.EVENT_NEW:
            return MapConstants.EVENT_NEW_S;
        case MapConstants.EVENT_SHIFT:
            return MapConstants.EVENT_SHIFT_S;
        default:
            return null;
        }
    }
    private Uri getMailboxUri(int mailbox) {
        //ignore other type
        switch(mailbox) {
            case MapConstants.MESSAGE_TYPE_INBOX:
                return Uri.parse(MapConstants.INBOX);
            case MapConstants.MESSAGE_TYPE_OUTBOX:
                return Uri.parse(MapConstants.OUTBOX);
            case MapConstants.MESSAGE_TYPE_SENT:
                return Uri.parse(MapConstants.SENT);
            case MapConstants.MESSAGE_TYPE_DRAFT:
                return Uri.parse(MapConstants.DRAFT);
            case MapConstants.MESSAGE_TYPE_FAILED:
                return Uri.parse(MapConstants.FAILED);
            default:
                return null;
        }
    }   
    
    private String normalizeString (String text){   
        if(text == null || text.length() == 0){
            return null;
        }
        text = text.replaceAll(" ", "");
        text = text.replaceAll("-", "");
        return text;
    }
    
    private boolean doesPhoneNumberMatch(String[] targetArray, String[] templateArray) {
        if (targetArray == null || templateArray == null ||
            targetArray.length == 0 || templateArray.length == 0) {
            return false;
        }
         
        for(String template:templateArray) {
            for (String target:targetArray) {
                if ((target.indexOf(template) != 0) || (template.contains(target))) {
                    return true;
                }
            }
        }
        return false;
    }

    /* return */
    private boolean doesPhoneNumberMatch (String target, String template1, String template2) {
        //boolean match = false;
        boolean isTemplateEmpty = (template1 == null && template2 == null);
       
        if (target == null) {
            return false;
        }
        if (isTemplateEmpty) {
            return true;
        }
        
        if (template1 != null) {
            String[] targetArray = target.split(";");
            String[] templateArray = template1.split(";");
            if (doesPhoneNumberMatch(targetArray, templateArray)) {
                return true;
            }
        } 
        if (template2 != null && isPhoneNumber(template2)) {
            return target.contains(template2);
        } 
        return false;
        
    }
    private boolean isPhoneNumber(String number) {
        int numDigits = 0;
        int len = number.length();
        for (int i = 0; i < len; i++) {
            char c = number.charAt(i);
            if (Character.isDigit(c)) {
                numDigits ++;
            } else if (c == '*' || c == '#' || c == 'N' || c == '.' || c == ';'
                    || c == '-' || c == '(' || c == ')' || c == ' ') {
                // carry on
            } else if (c == '+' && numDigits == 0) {
                // plus before any digits is ok
            } else {
                return false; // not a phone number
            }
        }
        return (numDigits > 0);
    }
    private MessageListItem composeMessageItem(Cursor cs, int mailbox, int maxSubjextLen){
        MessageListItem msg = new MessageListItem();
        int recipientStatus;    
        boolean isText;
        int readStatus;

        recipientStatus = revertLoadStatus(cs.getInt(STATUS_COLUMN));
        //if the message has been deleted, return directly
        if (recipientStatus == -1) {
            return null; 
        }

        readStatus = revertReadStatus(cs.getInt(READ_COLUMN));
        //if the message has been deleted, return directly
        if (readStatus == -1) {
            return null;
        }

        isText = true;
        isText = cs.getString(BODY_COLUMN) != null;

        String data = cs.getString(DATE_COLUMN);
        msg.setHandle((cs.getLong(ID_COLUMN) | MapConstants.SMS_GSM_HANDLE_BASE));
        msg.setSubject(null);
        msg.setDatetime(Long.valueOf(data));
        String address = cs.getString(ADDRESS_COLUMN);
        if ( mAddress == null || !mAddress.equals(address)) {
            mAddress = address;
            mPerson = Util.getContactName(mContext, address);
        }
        
        
        if (mailbox == MapConstants.MESSAGE_TYPE_INBOX ) {               
            msg.setSenderAddr(address);
            msg.setSenderName(mPerson);
        } else {
            msg.setRecipientAddr(address);
            msg.setRecipientName(mPerson);
        }  
        msg.setMsgType();
        if (cs.getString(BODY_COLUMN) != null) {
            msg.setSize(cs.getString(BODY_COLUMN).length());
        }
        else {
            msg.setSize(0);
        }
        msg.setText(isText);
        msg.setRecipientStatus(recipientStatus);
        msg.setAttachSize();
        msg.setReadStatus(readStatus);
        msg.setProtected();
        msg.setPriority();
        return msg;
    }
 
    private int revertReadStatus(int smsReadStatus){
        switch(smsReadStatus) {
            case SMS_UNREAD_STATUS:
                return MapConstants.UNREAD_STATUS;
            case SMS_READ_STATUS:
                return MapConstants.READ_STATUS;
            default:
                return -1;
        }
    }
    private int convertMailboxType(String mapMailboxType) {
        if (mapMailboxType == null) {
            return -1;
        }
        if (mapMailboxType.equals(MapConstants.Mailbox.INBOX)) {
            return MapConstants.MESSAGE_TYPE_INBOX;
        } else if (mapMailboxType.equals(MapConstants.Mailbox.OUTBOX)) {
            return MapConstants.MESSAGE_TYPE_OUTBOX;
        } else if (mapMailboxType.equals(MapConstants.Mailbox.FAILED)) {
            return MapConstants.MESSAGE_TYPE_FAILED;
        } else if (mapMailboxType.equals(MapConstants.Mailbox.SENT)) {
            return MapConstants.MESSAGE_TYPE_SENT;
        } else if (mapMailboxType.equals(MapConstants.Mailbox.DRAFT)) {
            return MapConstants.MESSAGE_TYPE_DRAFT;
        } else if(mapMailboxType.equals(MapConstants.Mailbox.DELETED)) {
            return MESSAGE_TYPE_DELETE;
        }
        return -1;
    }

    private int revertLoadStatus(int SmsStatus) {
        return MapConstants.RECEPIENT_STATUS_COMPLETE;

    }
    private void moveMessageToFolder(Context context,
            Uri uri, int folder, int error, int status) {

        boolean markAsUnread = false;
        boolean markAsRead = false;
        switch(folder) {
        case MapConstants.MESSAGE_TYPE_INBOX:
        case MapConstants.MESSAGE_TYPE_DRAFT:
             break;
        case MapConstants.MESSAGE_TYPE_OUTBOX:
        case MapConstants.MESSAGE_TYPE_SENT:
             markAsRead = true;
             break;
        case MapConstants.MESSAGE_TYPE_FAILED:
        case MapConstants.MESSAGE_TYPE_QUEUED:
             markAsUnread = true;
             break;
             default:
             return ;
        }

        ContentValues values = new ContentValues(3);
        values.put(MapConstants.TYPE, folder);
        values.put(MapConstants.STATUS, status);
        if (markAsUnread) {
                  values.put(MapConstants.READ, Integer.valueOf(0));
        } else if (markAsRead) {
            values.put(MapConstants.READ, Integer.valueOf(1));
        }
        //values.put(MapConstants.ERROR_CODE, error);
        mContentResolver.update(uri, values, null, null);
    }
    private void handleSentResult(Intent intent, int resultCode){

       
        int error = intent.getIntExtra("errorCode", 0);
        //boolean isfinal = intent.getBooleanExtra(EXTRA_FINAL_MESSAGE, false);
        long id = intent.getLongExtra(EXTRA_MESSAGE_ID, -1);            
        String[] projection = new String[]{MapConstants.TYPE};
        Uri uri = ContentUris.withAppendedId(Uri.parse(MapConstants.SMS_CONTENT_URI), id);
        Cursor cs = mContentResolver.query(uri, projection, null ,null,null);
        if (cs == null) {
            return;
        } else if (!cs.moveToFirst()) {
            cs.close();
            return;
        }           
            
        if(resultCode == Activity.RESULT_OK) {
            int mailbox = cs.getInt(0);
            if (mailbox == MapConstants.MESSAGE_TYPE_OUTBOX){
                moveMessageToFolder(mContext, uri, MapConstants.MESSAGE_TYPE_SENT, error, -1);
            }       
        } else {
            moveMessageToFolder(mContext, uri, MapConstants.MESSAGE_TYPE_FAILED, error, 128);
        }
        cs.close();
        
    }   
    private void handleDeliverResult(Intent intent, int resultCode){
            byte[] pdu = (byte[]) intent.getExtras().get("pdu");
            long id = intent.getLongExtra(EXTRA_MESSAGE_ID, INVALID_VALUE_ID);
            Uri uri = ContentUris.withAppendedId(Uri.parse(MapConstants.SMS_CONTENT_URI), id);
            String[] projection = new String[]{MapConstants._ID};
            if (pdu == null || resultCode != Activity.RESULT_OK){
                return;
            }
            SmsMessage message = SmsMessage.createFromPdu(pdu);
            if (message == null) {
                return;
            }
            Cursor cs = mContentResolver.query(uri, projection, null ,null,null);
            if (cs != null && cs.moveToFirst()) {
                ContentValues cv = new ContentValues();
                try {
                	cv.put(MapConstants.STATUS, message.getStatus());
                	mContentResolver.update(uri, cv, null, null); 
                } catch (Exception e) {
                	String errorString = e.toString();
                    if (errorString == null) {
                 	   errorString = "querry error";
                    }
                    Log.w(TAG, errorString);
                }
                  
            }
           
            if (cs != null){
                cs.close();
            }
    }
}
