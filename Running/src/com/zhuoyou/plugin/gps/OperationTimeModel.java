package com.zhuoyou.plugin.gps;

import java.io.Serializable;

public class OperationTimeModel implements Serializable {

	public static final long serialVersionUID = -1912165521494046890L;
	public static final int  BEGIN_GPS_GUIDE=1;   //开始gps运动
	public static final int  STOP_GPS_GUIDE=2;    //暂停gps运动
	public static final int  CONTINUE_GPS_GUIDE=3;  //继续gps运动
	public static final int  COMPLETE_GPS_GUIDE=4;   //完成gps运动
	public static final int  SERVICE_IS_STOP=5;     //service异常终止
	public static final int  NO_LAOCTION_SINGAL=6;   //无法获得gps信号
	public static final int  OTHER_REASON=7;    //其他原因
	
	
	public long operatId;
	public long operationtime;//获取进行该项操作的时间
	public int  operationState;//记录操作类型
	public long operationSystime;//获取操作的系统时间，单位ms，主要用于时间差计算
	public int syncState;   //云同步状态


	public OperationTimeModel(){
		
	}


	public long getOperationtime() {
		return operationtime;
	}


	public void setOperationtime(long operationtime) {
		this.operationtime = operationtime;
	}


	public int getOperationState() {
		return operationState;
	}


	public void setOperationState(int operationState) {
		this.operationState = operationState;
	}


	public long getOperationSystime() {
		return operationSystime;
	}


	public void setOperationSystime(long operationSystime) {
		this.operationSystime = operationSystime;
	}
	
	public int getSyncState() {
		return syncState;
	}


	public void setSyncState(int syncState) {
		this.syncState = syncState;
	}


	public long getOperatId() {
		return operatId;
	}


	public void setOperatId(long operatId) {
		this.operatId = operatId;
	}
	
	
}
