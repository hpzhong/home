package com.zhuoyou.plugin.gps.ilistener;

import java.util.ArrayList;
import java.util.List;

import com.zhuoyou.plugin.gps.GuidePointModel;

public class MonitorWatcher implements IGPSWatcher {
	
	private static MonitorWatcher watcher;
	// 存放观察者
	private List<IGPSPointListener> list;
	private MonitorWatcher(){
		list = new ArrayList<IGPSPointListener>();
	}
	
	public static MonitorWatcher getInstance(){
		if(watcher == null){
			watcher = new MonitorWatcher();
		}
		return watcher;
	}

	@Override
	public void addWatcher(IGPSPointListener watcher) {
		list.add(watcher);
	}

	@Override
	public void removeWatcher(IGPSPointListener watcher) {
		list.remove(watcher);
	}

	@Override
	public void notifyWatchers(GuidePointModel point) {
		// 自动调用实际上是主题进行调用的
		for (IGPSPointListener watcher : list) {
			watcher.update(point);
		}
	}

	@Override
	public void notifySumDistance(double distance) {
		for (IGPSPointListener watcher : list) {
			watcher.sumDisChanged(distance);
		}
	}
}