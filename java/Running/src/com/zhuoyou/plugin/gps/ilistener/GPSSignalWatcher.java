package com.zhuoyou.plugin.gps.ilistener;


//抽象主题角色，watched：被观察
public interface GPSSignalWatcher {
	
	public void addWatcher(GPSSignalListener listener);

	public void removeWatcher(GPSSignalListener listener);

	public void notifyWatchers(int gpsState);

}