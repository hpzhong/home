package com.zhuoyou.plugin.bluetooth.attach;

import java.util.ArrayList;
import java.util.List;

public class PreInstallBean {

	private String mName = null;
	private String mCategory = null;
	private List<String> mPlugPackageNames = new ArrayList<String>();
	private List<String> mPlugNames = new ArrayList<String>();

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

	public List<String> getPlugPackageNames() {
		return mPlugPackageNames;
	}

	public void addPlugPackageName(String PlugPackageName) {
		this.mPlugPackageNames.add(PlugPackageName);
	}

	public List<String> getPlugNames() {
		return mPlugNames;
	}

	public void addPlugName(String mPlugName) {
		this.mPlugNames.add(mPlugName);
	}

}
