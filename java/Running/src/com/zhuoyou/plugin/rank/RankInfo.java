package com.zhuoyou.plugin.rank;

import java.io.IOException;
import java.io.Serializable;

public class RankInfo  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4024181216006849270L;
	private int mRank;
	private int mImgId;
	private String accountId;
	private String mName;
	private String mSteps;
	private String mHeadUrl;
	
	public RankInfo(){
		
	}

	public RankInfo(int rank, int imgId,String accountId, String name, String steps, String headUrl) {
		this.mRank = rank;
		this.mImgId = imgId;
		this.accountId=accountId;
		this.mName = name;
		this.mSteps = steps;
		this.mHeadUrl = headUrl;
	}
	public int getRank() {
		return mRank;
	}
	
	public String getName() {
		return mName;
	}

	public void setRank(int mRank) {
		this.mRank = mRank;
	}

	public int getmImgId() {
		return mImgId;
	}

	public void setmImgId(int mImgId) {
		this.mImgId = mImgId;
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

	public String getHeadUrl() {
		return mHeadUrl;
	}

	public void setHeadUrl(String mHeadUrl) {
		this.mHeadUrl = mHeadUrl;
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeInt(mRank);
		out.writeInt(mImgId);
		out.writeUTF(accountId);
		out.writeUTF(mName);
		out.writeUTF(mSteps);
		out.writeUTF(mHeadUrl);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		mRank = in.readInt();
		mImgId = in.readInt();
		accountId = in.readUTF();
		mName = in.readUTF();
		mSteps = in.readUTF();
		mHeadUrl = in.readUTF();
	}
}
