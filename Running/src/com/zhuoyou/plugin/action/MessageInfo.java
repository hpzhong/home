package com.zhuoyou.plugin.action;

import java.io.Serializable;

public class MessageInfo implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//	--content	String	消息内容
	private String mContent;
	//	--activityId	Int	活动Id
	private int mMsgId;
	
	private int id;
	private int mMsgType;
	private String mMsgTime;
	private int mState;
	private int activityId;

	public MessageInfo(int msgid, String content,int activityid, int type){
		mContent = content;
		mMsgId = msgid;
		mMsgType = type;
		activityId = activityid;
	}
	
	public MessageInfo(int _id, int msgid, String content, int type, String time, int state){
		id = _id;
		mContent = content;
		mMsgId = msgid;
		mMsgType = type;
		mMsgTime = time;
		mState = state;
	}

	/*
	 * get msg content
	 */
	public String GetMsgContent(){
		return mContent;
	}

	/*
	 * get activity id;
	 */
	public int GetMsgId(){
		return mMsgId;
	}

	public int getId() {
		return id;
	}
	
	public String getmMsgTime() {
		return mMsgTime;
	}

	public int getmMsgType() {
		return mMsgType;
	}

	public int getmState() {
		return mState;
	}

	public int getActivityId() {
		return activityId;
	}	
	
}
