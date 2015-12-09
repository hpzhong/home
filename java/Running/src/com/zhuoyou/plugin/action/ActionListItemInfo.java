package com.zhuoyou.plugin.action;

import java.io.Serializable;

public class ActionListItemInfo implements Serializable{
	
	//	--actId	int	活动id
	private int mActId;
	//	--title	String	标题
	private String mTitle;
	//	--startTime	String	开始时间2015-02-08 12:00:00
	private String mStartTime;
	//	--endTime	String	结束时间2015-02-09 12:00:00
	private String mEndTime;
	//	--curTime	String	当前系统时间 2015-02-08 11:20:12
	private String mCurTime;
	//	--num	String 	报名人数
	private String mNum;
	//	--flag	String 	当前用户是否已报名 0：未报名  1：已报名 
	private boolean mFlag;
	//	--top	String	是否置顶显示 0：否  1：是
	private boolean mTop;
	//	--imgUrl	String	缩略图地址
	private String mImgUrl;
	
	public ActionListItemInfo(int actid,String title,String startTime,String endTime,String curTime,String num,int flag,int top,String imgUrl){
		mActId = actid;
		mTitle = title;
		mStartTime = startTime;
		mEndTime = endTime;
		mCurTime = curTime;
		mNum = num;
		mFlag = (flag == 0)?true:false;
		mTop = (top == 0)?true:false;
		mImgUrl = imgUrl;
		
	}
	
	/*
	 * get action id;
	 */
	public int GetActivtyId(){
		return mActId;
	}
	
	/*
	 * get action title;
	 */
	public String GetActivtyTitle(){
		return mTitle;
	}
	
	/*
	 * get action starttime;
	 */
	public String GetActivtyStartTime(){
		return mStartTime;
	}
	
	/*
	 * get action endtime;
	 */
	public String GetActivtyEndTime(){
		return mEndTime;
	}

	/*
	 * get action curtime;
	 */
	public String GetActivtyCurTime(){
		return mCurTime;
	}
	
	/*
	 * get action attached people num;
	 */
	public String GetActivtyNum(){
		return mNum;
	}

	/*
	 * get user is attached to action;
	 */
	public boolean GetActiviyFlag(){
		return mFlag;
	}
	
	/*
	 * get is Action top show;
	 */
	public boolean GetActiviyTop(){
		return mTop;
	}
	
	/*
	 * get action Img url;
	 */	
	public String GetActiviyImgUrl(){
		return mImgUrl;
	}
	

}
