package com.zhuoyou.plugin.rank;

public class ClickUtils {
	private static long lastClickTime;  
    public static boolean isFastDoubleClick() {  
        long time = System.currentTimeMillis();  
        long timeD = time - lastClickTime;  
        if ( 0 < timeD && timeD < 1000) {     
            return true;     
        }     
        lastClickTime = time;     
        return false;     
    }
    
    public static boolean isMultClick() {  
        long time = System.currentTimeMillis();  
        long timeD = time - lastClickTime;  
        if ( 0 < timeD && timeD < 10000) {     
            return true;     
        }     
        lastClickTime = time;     
        return false;     
    }
}
