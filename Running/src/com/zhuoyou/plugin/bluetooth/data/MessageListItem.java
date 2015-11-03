package com.zhuoyou.plugin.bluetooth.data;

import java.io.UnsupportedEncodingException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;

import android.text.format.Time;

public class MessageListItem {
    private long    MsgHandle;
    private String Subject;
    private String DateTime;
    private String SenderName;
    private String SenderAddr;
    private String RecipientName;
    private String RecipientAddr;
    private int OrignalMsgSize;
    private boolean bText;
    private int RecipientStatus;
    private int AttachSize;
    private boolean bPriority;
    private int read;
    private boolean bProtected;
    
    private ArrayList<String> mMessageItemFeildList = null;

    public MessageListItem(){
        
    }

    public ArrayList<String> getMessageItem() {
        if (mMessageItemFeildList != null) {
            return mMessageItemFeildList;
        }
        else {
            mMessageItemFeildList = new ArrayList<String>();
            mMessageItemFeildList.add(MapConstants.MessageItemField.MsgHandle, String.valueOf(MsgHandle));
            mMessageItemFeildList.add(MapConstants.MessageItemField.Subject, Subject);
            mMessageItemFeildList.add(MapConstants.MessageItemField.DateTime, DateTime);
            mMessageItemFeildList.add(MapConstants.MessageItemField.SenderName, SenderName);
            mMessageItemFeildList.add(MapConstants.MessageItemField.SenderAddr, SenderAddr);
            mMessageItemFeildList.add(MapConstants.MessageItemField.RecipientName, RecipientName);
            mMessageItemFeildList.add(MapConstants.MessageItemField.RecipientAddr, RecipientAddr);
            mMessageItemFeildList.add(MapConstants.MessageItemField.MsgType, "SMS_GSM");
            mMessageItemFeildList.add(MapConstants.MessageItemField.OrignalMsgSize, String.valueOf(OrignalMsgSize));
            mMessageItemFeildList.add(MapConstants.MessageItemField.bText, String.valueOf(bText));
            mMessageItemFeildList.add(MapConstants.MessageItemField.RecipientStatus, String.valueOf(RecipientStatus));
            mMessageItemFeildList.add(MapConstants.MessageItemField.AttachSize, String.valueOf(AttachSize));
            mMessageItemFeildList.add(MapConstants.MessageItemField.bPriority, String.valueOf(bPriority));
            mMessageItemFeildList.add(MapConstants.MessageItemField.read, String.valueOf(read));
            boolean bSent = true;
            mMessageItemFeildList.add(MapConstants.MessageItemField.bSent, String.valueOf(bSent));
            mMessageItemFeildList.add(MapConstants.MessageItemField.bProtected, String.valueOf(bProtected));
            
            return mMessageItemFeildList;
        }
    }
    public synchronized void set( String subject,
                     long time, String senderAddr,
                     String sendName, String reply,
                     String recepientName, String recepientAddr,
                     int msgType, int origSize,
                     boolean bText, int recepientStatus,
                     int AttachSize, int read, boolean protect) {
        setSubject(subject);
        setDatetime(time);
        SenderAddr = senderAddr;
        SenderName = sendName;
        //ReplyToAddr = reply;
        RecipientName = recepientName;
        RecipientAddr = recepientAddr; 
        RecipientStatus = recepientStatus; // to do 
        //MsgType = msgType;        // todo 
        OrignalMsgSize = origSize;
        this.bText = bText;
        bPriority = false;
        this.read = read;
        bProtected = protect;
        
    }
    public void setSubject(String sub) {
        if(sub == null) {
            return;
        }
        sub = encode(sub);
        byte[] databytes = sub.getBytes();
        int length = databytes.length; 
        if (length > MapConstants.MAX_SUBJECT_LEN) {
            try {
                Subject = new String(databytes, 0, (MapConstants.MAX_SUBJECT_LEN-1), "utf-8");
            } catch (UnsupportedEncodingException e) {
            }
        } else {
            Subject = sub;
        }
    }
    public void setHandle(long handle){
        MsgHandle = handle;
    }
    public void setDatetime(long millis) {
        DateTime = convertMillisToUtc(millis);
    }
    public void setSenderName(String name){
        SenderName = encode(name);
    }
    public void setSenderAddr(String addr) {
        SenderAddr = addr;
    }
    public void setRecipientName(String name) {
        RecipientName = encode(name);
    }
    public void setRecipientAddr(String addr) {
        RecipientAddr= addr;
    }
    public void setRecipientStatus(int status) {
        RecipientStatus = status;
    }
    public void setMsgType() {
        //MsgType = type;
    }
    public void setSize(int size) {
        OrignalMsgSize = size;
    }
    public void setText(boolean text) {
        bText= text;
    }
    public void setAttachSize() {
        AttachSize = 0;
    }
    public void setPriority() {
        bPriority = false;
    }
    public void setReadStatus(int read) {
        this.read = read;
    }
    public void setProtected() {
        bProtected = false;
    }
    private String convertMillisToUtc(long millis) {
        Time mTime = new Time();
        mTime.set(millis);
        return mTime.toString().substring(0,15);
    }
    /* Xml 1.0 predefined escape characters*/
    /* description              Character   name    Unicode*/
    /* double quotion mark      "           quot        U+022(34)*/
    /* ampersand                &           amp     U+026(38)*/
    /* apostroghe           '           apos        U+027(39)*/
    /* less-than sign           <           lt      U+03C(60)*/
    /* great-than sign          >           gt      U+03E(62)*/
    private String encode (String rawData) {
	    if (rawData == null) {
	        return null;
	    }       
	    StringBuilder result = new StringBuilder();
	    final StringCharacterIterator iterator = new StringCharacterIterator(rawData);
	    char character =  iterator.current();
	    while (character != CharacterIterator.DONE ){
	        if (character == '<') {
	            result.append("&lt;");
	        }
	        else if (character == '>') {
	            result.append("&gt;");
	        }
	        else if (character == '\"') {
	            result.append("&quot;");
	        }
	        else if (character == '\'') {
	            result.append("&apos;");
	        }
	        else if (character == '&') {
	            result.append("&amp;");
	        }
	        else {
	            // The char is not a special one, add it to the result as is
	            result.append(character);
	        }
	        character = iterator.next();
	    }
	    return result.toString();
	}
}
