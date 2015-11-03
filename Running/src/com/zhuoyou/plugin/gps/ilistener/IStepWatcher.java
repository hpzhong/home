package com.zhuoyou.plugin.gps.ilistener;

//抽象主题角色，watched：被观察
public interface IStepWatcher {
	
	public void addWatcher(IStepListener listener);

	public void removeWatcher(IStepListener listener);

	public void notifyStepCount(int stepCount);
	
	public void notifyStateChanged(int newState);
	
	public void notifyHadRunStep(int hadRunStep);

}