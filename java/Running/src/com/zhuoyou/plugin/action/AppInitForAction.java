/*
 * 
 * this class write for tranlate net date which is down when app start by user;
 * and store date in dir as /data/data/Running/cache/tmp
 *  
 */
package com.zhuoyou.plugin.action;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AppInitForAction implements Serializable{
	
	private ActionWelcomeInfo mWelcomeData;
	private List<MessageInfo> mMsgList;
	private List<ActionListItemInfo> mActionList;
	

	
	public AppInitForAction(){
		mWelcomeData = null;
		mMsgList = new ArrayList<MessageInfo>();
		mActionList = new ArrayList<ActionListItemInfo>();
	}
	
	public void SetWelcomeInfo(ActionWelcomeInfo welcomedata){
		mWelcomeData = welcomedata;
	}
	
	public ActionWelcomeInfo GetWelcomeInfo(){
		return mWelcomeData;
	}
	
	public void SetMsgList(List<MessageInfo> msglist){
		mMsgList = msglist;
	}
	
	public void AddMsgItem(MessageInfo msg){
		mMsgList.add(msg);
	}
	
	
	public List<MessageInfo> GetMsgList(){
		return mMsgList;
	}

	
	public void SetActionListItem(List<ActionListItemInfo> mlist){
		mActionList = mlist;
	}
	
	public void AddActionListItem(ActionListItemInfo item){
		mActionList.add(item);
	}
	
	
	public List<ActionListItemInfo>  GetActionList(){
		return  mActionList;
	}
	
	//cache data 
	public void CacheDate(){
		
	}
	
	
}
