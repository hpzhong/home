package com.zhuoyou.plugin.action;

public class ActionRankingItemInfo {

	//	--count	Int	名次
	private int mCount;

	//	--accountId	String	账号id
	private String mAccountId;

	//	--steps	Int	步数
	private int mSteps;

	//	--headimgId	String	头像id
	private int mHeadImgId;

	//	--name	String	昵称
	private String mName;

	//	--headImgUrl	String	头像地址
	private String mHeadImgUrl;
	
	
	public ActionRankingItemInfo(){
		
	}
	
	public ActionRankingItemInfo(int count,String accountId,int steps,int headimgId,String name,String headImgUrl){

		mCount = count;
		mAccountId = accountId;
		mSteps = steps;
		mHeadImgId = headimgId;
		mName = name;
		mHeadImgUrl = headImgUrl;
	}
	
	/*
	 * get rank count
	 */
	public int GetCount(){
		return mCount;
	}
	
	/*
	 * get Accountid
	 */
	public String GetAccountId(){
		return mAccountId;
	}
	
	/*
	 * get steps
	 */
	public int GetSteps(){
		return mSteps;
	}
	
	/*
	 * get user headimg id;
	 */
	public int GetHeadImgId(){
		return mHeadImgId;
	}
	
	/*
	 * get User name
	 */
	public String GetName(){
		return mName;
	}
	
	/*
	 * get user img url
	 */
	public String GetHeadImgUrl(){
		return mHeadImgUrl;
	}
	
	public void SetRank(int count,String accountId,int steps,int headimgId,String name,String headImgUrl){
		mCount = count;
		mAccountId = accountId;
		mSteps = steps;
		mHeadImgId = headimgId;
		mName = name;
		mHeadImgUrl = headImgUrl;
	}
	
	
}
