package com.zhuoyou.plugin.bluetooth.product;

import java.util.ArrayList;
import java.util.List;

public class Grandpa {
	/**
	 * @ father information
	 */
	private List<Father> mFathers = new ArrayList<Father>();

	/**
	 * @ grandpa modify time
	 */
	private String mModifyTime;

	public List<Father> getFather() {
		return mFathers;
	}

	public void addFather(Father father) {
		this.mFathers.add(father);
	}

	public String getModifyTime() {
		return mModifyTime;
	}

	public void setModifyTime(String mModifyTime) {
		this.mModifyTime = mModifyTime;
	}

}
