package com.zhuoyou.plugin.bluetooth.product;

import java.util.ArrayList;
import java.util.List;

public class Father {
	
	/**
	 * @ his child information;
	 */
	private List<Son> mSons = new ArrayList<Son>();
	
	/**
	 * @ owner name
	 */
	private String mName;
	
	/**
	 * @which type is?
	 * 
	 * @earphone
	 * 
	 * @galss
	 * 
	 * @wristwatch
	 * 
	 * @bracelet
	 * 
	 * @and so on
	 */
	private String mCategory;

	public List<Son> getSons() {
		return mSons;
	}

	public void addSon(Son son) {
		this.mSons.add(son);
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public String getCategory() {
		return mCategory;
	}

	public void setCategory(String mCategory) {
		this.mCategory = mCategory;
	}

}
