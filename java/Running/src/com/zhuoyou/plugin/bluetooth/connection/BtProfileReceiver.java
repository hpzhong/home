package com.zhuoyou.plugin.bluetooth.connection;

import java.util.Timer;
import java.util.TimerTask;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BtProfileReceiver extends BroadcastReceiver {
	private static BluetoothDevice currRemoteDevice = null;
	private Context mContext = null;
	public  static Timer mTimer = new Timer(true);
	private final int LESS_CONNECT_TIME = 3;
	private static int mConnectTime = 0;	
	public static final String NEED_CONNECT_ACTION_STRING = "com.mtk.btconnection.needconnected";

	public BtProfileReceiver (Context context) {
		mContext = context;
	}
	public static BluetoothDevice getRemoteDevice() {
		return currRemoteDevice;
	}

	public static void stopAutoConnect() {
		if (mTimer != null)
		{
			mTimer.cancel();
			mTimer = null;
		}
	}

    private void runningSyncTimer() {
    	
        TimerTask task = new TimerTask(){  
            public void run() {  
                
                this.cancel();
                mTimer = null;
                
                Intent broadcastIntent = new Intent();
		        broadcastIntent.setAction(NEED_CONNECT_ACTION_STRING);
		        
		        if (currRemoteDevice != null && 
			        (BluetoothManager.GetSppConnectState() != BluetoothConnection.STATE_CONNECTING &&
					BluetoothManager.GetSppConnectState() != BluetoothConnection.STATE_CONNECTED)) {
			        	mContext.sendBroadcast(broadcastIntent);			        	
			        }
		        mConnectTime--;
		        if (mConnectTime > 0) {
		        	runningSyncTimer();
		        }
              }  
          }; 
        if (mTimer != null) {
        	mTimer.cancel();
        	mTimer = null;
        }
        mTimer = new Timer();
        mTimer.schedule(task,3000);
    }

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		if (action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)) {
			int connState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 0);							
			if (connState == BluetoothProfile.STATE_CONNECTING || connState == BluetoothProfile.STATE_CONNECTED) {
				if (currRemoteDevice == null || 
					(BluetoothManager.GetSppConnectState() != BluetoothConnection.STATE_CONNECTING &&
					BluetoothManager.GetSppConnectState() != BluetoothConnection.STATE_CONNECTED)) {
					currRemoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				}
				
				Intent broadcastIntent = new Intent();
		        broadcastIntent.setAction(NEED_CONNECT_ACTION_STRING);
		        if (currRemoteDevice != null && 
		        	(BluetoothManager.GetSppConnectState() != BluetoothConnection.STATE_CONNECTING &&
					BluetoothManager.GetSppConnectState() != BluetoothConnection.STATE_CONNECTED)) {
		        	//mContext.sendBroadcast(broadcastIntent);
		        	mConnectTime = LESS_CONNECT_TIME;
		        	runningSyncTimer();
		        }
			}
			return;
		}
		else if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {			
			int connState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 0);			
			if (connState == BluetoothProfile.STATE_CONNECTING || connState == BluetoothProfile.STATE_CONNECTED) {
				if (currRemoteDevice == null || 
				    (BluetoothManager.GetSppConnectState() != BluetoothConnection.STATE_CONNECTING &&
					BluetoothManager.GetSppConnectState() != BluetoothConnection.STATE_CONNECTED)) {
						currRemoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				}
				
				Intent broadcastIntent = new Intent();
		        broadcastIntent.setAction(NEED_CONNECT_ACTION_STRING);
		        if (currRemoteDevice != null && 
		        	(BluetoothManager.GetSppConnectState() != BluetoothConnection.STATE_CONNECTING &&
					BluetoothManager.GetSppConnectState() != BluetoothConnection.STATE_CONNECTED)) {
	        		mConnectTime = LESS_CONNECT_TIME;
	        		runningSyncTimer();
		        }
			}
		}
	}
	
	public BluetoothDevice getCurrRemoteDevice () {
		return currRemoteDevice;
	}
	
	public String getRemoteDeviceName() {
		return currRemoteDevice.getName();
	}
	
	public void setRemoteDevice(BluetoothDevice remoteDevice) {
		currRemoteDevice = remoteDevice;
	}
	public static int getLessTime() {
		// TODO Auto-generated method stub
		return mConnectTime;
	}

}
