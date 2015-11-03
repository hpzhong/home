package com.zhuoyou.plugin.selfupdate;

import java.util.HashMap;

public interface DataCodec 
{
	public HashMap<String, Object> splitMySelfData(String result);
}
