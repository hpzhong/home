package com.zhuoyou.plugin.gps.ilistener;

import com.zhuoyou.plugin.gps.GuidePointModel;

public interface IGPSPointListener {
	public void update(GuidePointModel point);
	
	public void sumDisChanged(double distance);

}