package com.zhuoyou.plugin.gps.ilistener;

import com.zhuoyou.plugin.gps.GuidePointModel;

//抽象主题角色，watched：被观察
public interface IGPSWatcher {
	
	public void addWatcher(IGPSPointListener listener);

	public void removeWatcher(IGPSPointListener listener);

	public void notifyWatchers(GuidePointModel point);
	
	public void notifySumDistance(double distance);

}