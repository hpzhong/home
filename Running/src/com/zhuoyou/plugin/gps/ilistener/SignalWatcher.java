package com.zhuoyou.plugin.gps.ilistener;

import java.util.ArrayList;
import java.util.List;

public class SignalWatcher implements GPSSignalWatcher {
	
	private static SignalWatcher watcher;
	// 存放观察者
	private List<GPSSignalListener> list;
	private SignalWatcher(){
		list = new ArrayList<GPSSignalListener>();
	}
	
	public static SignalWatcher getInstance(){
		if(watcher == null){
			watcher = new SignalWatcher();
		}
		return watcher;
	}

	@Override
	public void addWatcher(GPSSignalListener watcher) {
		list.add(watcher);
	}

	@Override
	public void removeWatcher(GPSSignalListener watcher) {
		list.remove(watcher);
	}

	@Override
	public void notifyWatchers(int gpsState) {
		// 自动调用实际上是主题进行调用的
		for (GPSSignalListener watcher : list) {
			watcher.update(gpsState);
		}
	}
}