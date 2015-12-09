package com.zhuoyou.plugin.gps.ilistener;

public interface IStepListener {
	
	public void onStepCount(int stepCount);
	
	public void onStateChanged(int newState);
	
	public void onHadRunStep(int hadRunStep);

}