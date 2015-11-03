package com.zhuoyou.plugin.gps.ilistener;

import java.util.ArrayList;
import java.util.List;

public class StepWatcher implements IStepWatcher {
	
	private static StepWatcher watcher;
	// 存放观察者
	private List<IStepListener> list;
	private StepWatcher(){
		list = new ArrayList<IStepListener>();
	}
	
	public static StepWatcher getInstance(){
		if(watcher == null){
			watcher = new StepWatcher();
		}
		return watcher;
	}

	@Override
	public void addWatcher(IStepListener watcher) {
		list.add(watcher);
	}

	@Override
	public void removeWatcher(IStepListener watcher) {
		list.remove(watcher);
	}


	@Override
	public void notifyStepCount(int stepCount) {
		// 自动调用实际上是主题进行调用的
		for (IStepListener watcher : list) {
			watcher.onStepCount(stepCount);
		}
	}

	@Override
	public void notifyStateChanged(int newState) {
		// 自动调用实际上是主题进行调用的
		for (IStepListener watcher : list) {
			watcher.onStateChanged(newState);
		}
	}

	@Override
	public void notifyHadRunStep(int hadRunStep) {
		for (IStepListener watcher : list) {
			watcher.onHadRunStep(hadRunStep);
		}
	}
}