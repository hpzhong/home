package com.zhuoyou.plugin.component;

import java.io.Serializable;
import java.text.DecimalFormat;

public class AlarmBean implements Serializable{

	private static final long serialVersionUID = 6967510102698932010L;
	
	private int id;
	private int hour;
	private int min;
	private boolean isOpen;
	private boolean isBrain;
	
	/** 0为只响一次，1为每天，2为周一至周五，3为自定义 */
	private int openType;
	/** 自定义时间	<br>
	 * 用 1111111 共7位组成表示，依次代表周日、周六、周五.... 周二、周一 <br>
	 * 例如 只有 周天和周一 数值代表 1000001 <br>
	 * 默认是周一至周五
	 */
	private int customData = 11111;
	
	
	public int getId() {
		return id;
	}
	public int getHour() {
		return hour;
	}
	public int getMin() {
		return min;
	}
	public boolean isOpen() {
		return isOpen;
	}
	public boolean isBrain() {
		return isBrain;
	}
	public int getOpenType() {
		return openType;
	}
	public int getCustomData() {
		return customData;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setHour(int hour) {
		this.hour = hour;
	}
	public void setMin(int min) {
		this.min = min;
	}
	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}
	public void setBrain(boolean isBrain) {
		this.isBrain = isBrain;
	}
	public void setOpenType(int openType) {
		this.openType = openType;
	}
	public void setCustomData(int customData) {
		this.customData = customData;
	}
	
	/** 根据与蓝牙端确定time|num|isBrain|state|type|custom  */
	@Override
	public final String toString(){
		DecimalFormat intFormat = new DecimalFormat("#00");
		DecimalFormat byteFormat = new DecimalFormat("#0000000");
		String res = intFormat.format(hour) + intFormat.format(min) + "|" + id +
					+ (isBrain?1:0) +
					+ (isOpen?1:0) + "|" 
					+ openType + "|" + byteFormat.format(customData) + "|";
		return res;
	}
	
	
	public final String saveShareP(){
		DecimalFormat intFormat = new DecimalFormat("#00");
		DecimalFormat byteFormat = new DecimalFormat("#0000000");
		String res = intFormat.format(hour) + intFormat.format(min) + "|" + id + "|" 
					+ (isBrain?1:0) + "|" 
					+ (isOpen?1:0) + "|" 
					+ openType + "|" + byteFormat.format(customData) + "|";
		return res;
	}
}
