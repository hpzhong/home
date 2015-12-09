package com.zhuoyou.plugin.bluetooth.product;


public class ProductCategory {
	public boolean mEnable = false;
	public String mNickName = null;
	public String mRemoteName = "";

	/******************************************************************
	 * 定义我们自己的设备名字
	 * 
	 * @param type
	 */

	public ProductCategory(String nickname) {
		mNickName = nickname;
	}

	public void enableProductItem(boolean enable) {
		mEnable = enable;
	}

}
