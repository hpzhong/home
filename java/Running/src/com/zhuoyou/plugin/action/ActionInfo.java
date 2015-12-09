package com.zhuoyou.plugin.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ActionInfo implements Serializable{

	
	//	pannels	List	内容板块
	private List<ActionPannelItemInfo> mPannelContent;
	//	ranking	List	排名
	private List<ActionRankingItemInfo> mRankList;
	// for user rank
	private  ActionRankingItemInfo myRank;
	//as miaowenzhi description, 0:join;1:unjoin!
	private  int is_join = 0;
	// add for cache file name,if no adtionid,we not cache it
	private  int mActionId = -1;
	
	
	public ActionInfo(){
		mPannelContent = new ArrayList<ActionPannelItemInfo>();
		mRankList = new ArrayList<ActionRankingItemInfo>();
		myRank = new ActionRankingItemInfo();
	}
	
	public void AddPannel(ActionPannelItemInfo actionpannelitem){
		mPannelContent.add(actionpannelitem);
	}
	
	public  List<ActionPannelItemInfo> getPannel(){
		return mPannelContent;
	}
	
	public List<ActionRankingItemInfo> getRankList(){
		return mRankList;
	}
	public void SetPannelList(List<ActionPannelItemInfo> mm){
		mPannelContent = mm;
	}
	
	
	public void AddRank(ActionRankingItemInfo mrankinfo){
		mRankList.add(mrankinfo);
	}
	
	public void SetRankList(List<ActionRankingItemInfo> mrankinfolist){
		mRankList = mrankinfolist;
	}
	
	public void SetMyRankInfo(ActionRankingItemInfo mrank){
		myRank.SetRank(mrank.GetCount(),
				mrank.GetAccountId(),
				mrank.GetSteps(),
				mrank.GetHeadImgId(),
				mrank.GetName(),
				mrank.GetHeadImgUrl());
		
	}
	
	public ActionRankingItemInfo getMyRankInfo(){
		return myRank;
	}
	public void SetMyRankInfo(int count,String accountId,int steps,int headimgId,String name,String headImgUrl){
		myRank.SetRank(count,accountId,steps,headimgId,name,headImgUrl);
	}
	
	public void SetActionId(int id){
		mActionId = id;
	}
	
	public int GetActionId(){
		return mActionId;
	}
	
	public void SetJoinFlag(int flag){
		is_join = flag;
	}
	
	public int GetIsJoin(){
		return is_join;
	}
	
}
