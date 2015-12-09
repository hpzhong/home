package com.zhuoyou.plugin.action;

import java.io.Serializable;

public class ActionWelcomeInfo implements Serializable{

	//	--imgUrl	string	图片url(多个活动，只随机返回1个)
	private String mImgUrl;
	//	--id	int	活动id
	private int mId;
	
	public ActionWelcomeInfo(String imgurl,int id){
		mImgUrl = imgurl;
		mId = id;
	}
	
	public String GetImgUrl(){
		return mImgUrl;
	}
	
	public int GetID(){
		return mId;
	}
	
	
	
}
