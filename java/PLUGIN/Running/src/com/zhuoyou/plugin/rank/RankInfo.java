package com.zhuoyou.plugin.rank;

import android.graphics.drawable.Drawable;

public class RankInfo {

	private int mRank;
	private Drawable mImg;
	private String accountId;
	private String mName;
	private String mSteps;
	
	public RankInfo(){
		
	}

	public RankInfo(int rank, Drawable img,String accountId, String name, String steps) {
		this.mRank = rank;
		this.mImg = img;
		this.accountId=accountId;
		this.mName = name;
		this.mSteps = steps;
	}
	public int getRank() {
		return mRank;
	}
	public Drawable getImg() {
		return mImg;
	}
	public String getName() {
		return mName;
	}

	public void setRank(int mRank) {
		this.mRank = mRank;
	}

	public void setImg(Drawable mImg) {
		this.mImg = mImg;
	}
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public String getmSteps() {
		return mSteps;
	}

	public void setmSteps(String mSteps) {
		this.mSteps = mSteps;
	}

}
