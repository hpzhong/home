package com.zhuoyou.plugin.running;
import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

public class SleepItem implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private long id;
	private Calendar startCal;
	private Calendar endCal;
	private String mDate = "";
	private String mImgUri;

	/** 睡眠时间 eg:3600*7 s */
	private int mSleepT;
	
	/** 深睡时间 eg:3600*4 s */
	private int mDSleepT;
	
	/** 浅睡时间 eg:3600*3 s */
	private int mWSleepT;
	
	/** 入睡时间 eg:23:00 */
	private String mStartT;
	
	/** 醒来时间 eg:07:00 */
	private String mEndT;
	private List<SleepBean> datas;
	
	public SleepItem(){}
	
	public SleepItem(int mSleepT, int mDSleepT, int mWSleepT, String mStartT,String mEndT) {
		this.mSleepT = mSleepT;
		this.mDSleepT = mDSleepT;
		this.mWSleepT = mWSleepT;
		this.mStartT = mStartT;
		this.mEndT = mEndT;
	}
	public SleepItem(int mSleepT, int mDSleepT, int mWSleepT, String mStartT, String mEndT, List<SleepBean> data) {
		this.mSleepT = mSleepT;
		this.mDSleepT = mDSleepT;
		this.mWSleepT = mWSleepT;
		this.mStartT = mStartT;
		this.mEndT = mEndT;
		this.datas = data;
	}
	
	public int getmSleepT() {
		return mSleepT;
	}
	
	public int getmDSleepT() {
		return mDSleepT;
	}
	public int getmWSleepT() {
		return mWSleepT;
	}
	public String getmStartT() {
		return mStartT;
	}
	public String getmEndT() {
		return mEndT;
	}
	public List<SleepBean> getData() {
		return datas;
	}
	public void setmSleepT(int mSleepT) {
		this.mSleepT = mSleepT;
	}
	public void setmDSleepT(int mDSleepT) {
		this.mDSleepT = mDSleepT;
	}
	public void setmWSleepT(int mWSleepT) {
		this.mWSleepT = mWSleepT;
	}
	public void setmStartT(String mStartT) {
		this.mStartT = mStartT;
	}
	public void setmEndT(String mEndT) {
		this.mEndT = mEndT;
	}
	public void setData(List<SleepBean> data) {
		this.datas = data;
	}

	public Calendar getStartCal() {
		return startCal;
	}

	public void setStartCal(Calendar startCal) {
		this.startCal = startCal;
	}

	public Calendar getEndCal() {
		return endCal;
	}

	public void setEndCal(Calendar endCal) {
		this.endCal = endCal;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getmImgUri() {
		// TODO Auto-generated method stub
		return mImgUri;
	}

	public String getDate() {
		// TODO Auto-generated method stub
		return mDate;
	}
}