package com.zhuoyou.plugin.bluetooth.connection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.mtk.btconnection.LoadJniFunction;
import com.zhuoyou.plugin.bluetooth.data.MapConstants;
import com.zhuoyou.plugin.bluetooth.data.Util;
import com.zhuoyou.plugin.bluetooth.service.BluetoothService;

public class BluetoothManager {

    private static final String LOG_TAG = "BluetoothManager";
    private static boolean isHandshake = false; 
    private static boolean isOlderThanVersionTow = true;

    public static final String BT_BROADCAST_ACTION = "com.mtk.connection.BT_CONNECTION_CHANGED";
    public static final String EXTRA_TYPE = "EXTRA_TYPE";
    public static final String EXTRA_DATA = "EXTRA_DATA";

    // Broadcast extra type enum
    public static final int TYPE_BT_CONNECTED = 0x01;
    public static final int TYPE_BT_CONNECTION_LOST = 0x02;
    public static final int TYPE_DATA_SENT = 0x03;
    public static final int TYPE_DATA_ARRIVE = 0x04;
    public static final int TYPE_MAPCMD_ARRIVE = 0x05;
    public static final int TYPE_BT_CONNECTION_FAIL = 0x06;

    // Message types sent from the BluetoothConnection Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_CONNECT_FAIL = 6;

    //State of ReadBuffer
    public static final int READ_IDLE = 0;
    public static final int READ_PRE = 1;
    public static final int READ_CMD = 2;
    public static final int READ_DATA = 3;
    public int READBUFFERSTATE = READ_IDLE;
    public int CMD_TYPE = LoadJniFunction.CMD_1;

    // Error code
    public static final int BLOCKED = 1;
    public static final int SUCCESS = 0;
    public static final int FAILED = -1;

    public static final int BLUETOOTH_CONNECT_SUCCESS = 0;
    public static final int BLUETOOTH_NOT_SUPPORT = -1;
    public static final int BLUETOOTH_NOT_ENABLE = -2;
    public static final int BLUETOOTH_NOT_CONNECT = -3;

    // Key names received from the BluetoothConnection Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    
    //Buffer of cmd and data
    public static byte[] commandBuffer = null;
    public static byte[] dataBuffer = null;
    public static final byte[] reciveBuffer = new byte[50 * 1024];
    public static int reciveBufferLenth = 0;
    public static int cmdBufferLenth = 0;
    public static int dataBufferLenth = 0;
    
    private static int sppConnectState = 0;
    
    // Name of the connected device
    private BluetoothDevice mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothConnection mBluetoothConnection = null;
    private MessageHandler mHandler = null;
    private Context mContext = null;
    private MessageDataList mMessageDataList = null;
    private LoadJniFunction mLoadJniFunction = null;
    
    private BtProfileReceiver mProfileReceiver = null;
    public  Timer mTimer = new Timer(true);
	private static WeakReference<BluetoothManager> mBluetoothManager;

	public BluetoothManager(Context context) {
        Log.i(LOG_TAG, "BluetoothManager(), BluetoothManager created!");

        mHandler = new MessageHandler(this, context);
        mContext = context;
        mLoadJniFunction =  new LoadJniFunction();

        // Register for broadcasts when local Bluetooth turn on or turn off
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(mBTReceiver, filter);

        // Create message list to temporary storage message data
        mMessageDataList = new MessageDataList(mContext);
        
        mProfileReceiver = new BtProfileReceiver(mContext);
        IntentFilter connectFilter = new IntentFilter(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        connectFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        mContext.registerReceiver(mProfileReceiver, connectFilter);
	}
	
    public static int GetSppConnectState() {
    	return sppConnectState;
    }

	public int setupConnection() {
        Log.d(LOG_TAG, "setupConnection()");

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            return BLUETOOTH_NOT_SUPPORT;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            return BLUETOOTH_NOT_ENABLE;
        }

        // Initialize the BluetoothConnection to perform bluetooth connections
        mBluetoothConnection = new BluetoothConnection(mHandler);
        mBluetoothConnection.startAccept();
        // mBluetoothConnection.connectRemoteDevice(getRemoteDevice());

        Log.d(LOG_TAG, "setupConnection(), setupConnection successfully!");
        return BLUETOOTH_CONNECT_SUCCESS;
	}
	
    public int removeConnection() {
        Log.i(LOG_TAG, "removeConnection(), Bluetooth connection is removed!");

        if (mBluetoothConnection != null) {
            mBluetoothConnection.stop();
        }

        return BLUETOOTH_CONNECT_SUCCESS;
    }

    public String GetRemoteDeviceName() {
    	
    	return mProfileReceiver.getRemoteDeviceName();
    }

    public String getConnectedDeviceName() {
        Log.i(LOG_TAG, "getConnectedDeviceName(), mConnectedDeviceName=" + mConnectedDeviceName);

        return mConnectedDeviceName.getName();
    }

    public void setConnectedDeviceName(BluetoothDevice connectedDeviceName) {
        Log.i(LOG_TAG, "setConnectedDeviceName(), deviceName=" + mConnectedDeviceName);

        mConnectedDeviceName = connectedDeviceName;
        mProfileReceiver.setRemoteDevice(mConnectedDeviceName);
    }

    public void connectToRemoteDevice() {
    	if (mBluetoothConnection != null) {
    		mBluetoothConnection.connectRemoteDevice(mProfileReceiver.getCurrRemoteDevice());
    	}
    }

    public void connectToAppointedDevice(BluetoothDevice remoteDevice) {
    	if (mBluetoothConnection != null) {
    		mBluetoothConnection.connectRemoteDevice(remoteDevice);
    	}
    }
    
    public void disconnectRemoteDevice() {
    	if (mBluetoothConnection != null) {
    		mBluetoothConnection.disconnectRemoteDevice();
    	}
    }

    public void saveData() {
        Log.i(LOG_TAG, "saveData()");

        mMessageDataList.saveMessageDataList();
    }
    
    public void sendData(final byte[] data) {
        if ((data != null) && isBTConnected()) {
            sendDataToRemote(LoadJniFunction.CMD_1,data);
            if (mMessageDataList.getMessageDataList().size() > 0) {
                sendDataFromFile();
            }
        } else {
        	Util.autoConnect(mContext);
			new Thread() {
	            public void run() {
	            	Looper.prepare();
	                try {
	                    Thread.sleep(2000);
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	                if ((data != null) && isBTConnected()) {
	                    sendDataToRemote(LoadJniFunction.CMD_1,data);
	                    if (mMessageDataList.getMessageDataList().size() > 0) {
	                        sendDataFromFile();
	                    }
	                } else {
	                    mMessageDataList.saveMessageData(data);
	                }
	    			Looper.loop();
	            }			        
	        }.start();
        }
    }

    public boolean isBTConnected() {
        boolean isConnected = ((mBluetoothConnection != null) && isHandshake 
                && (mBluetoothConnection.getState() == BluetoothConnection.STATE_CONNECTED));

		Log.i(LOG_TAG, "isBTConnected(), isConnected=" + isConnected);
		return isConnected;
    }

    public boolean isEnable() {
    	return mBluetoothConnection.isEnable();
    }

    public void sendMapResult(String result) {
        if (isBTConnected()) {
            sendCommandToRemote(LoadJniFunction.CMD_5,result);
        }
    }
    
    public void sendMapDResult(String result) {
        if (isBTConnected()) {
            sendCommandToRemote(LoadJniFunction.CMD_6,result);
        }
    }
    
    public boolean sendMAPData(byte[] data) {
        if (isBTConnected()) {
            mBluetoothConnection.write(data);
            Log.i(LOG_TAG, "sendMAPData(), isDataSent=" + true);
            return true;
        }
        Log.i(LOG_TAG, "sendMAPData(), isDataSent=" + false);
        return false;
    }
    
    public boolean sendDataFromFile() {
        List<byte[]> messageList = mMessageDataList.getMessageDataList();
        Log.i(LOG_TAG, "sendDataFromFile(), message count=" + messageList.size());

        if (messageList.size() > 0) {
            final int messageCount = messageList.size();
            for (int index = 0; index < messageCount; index++) {
                if ((messageList.get(0) != null) && isBTConnected()) {
                    sendDataToRemote(LoadJniFunction.CMD_1,messageList.get(0));
                    messageList.remove(0);

                    Log.i(LOG_TAG, "sendDataFromFile(), message index=" + index);
                } else {
                    break;
                }
            }
        }

        return false;
    }

    private void sendDataToRemote(int i, byte[] data){
        
        Log.i(LOG_TAG, "sendDataToRemote cmd and data()" + getCmdBuffer(i,String.valueOf(data.length)));
        mBluetoothConnection.write(getCmdBuffer(i,String.valueOf(data.length)));
        mBluetoothConnection.write(data);
    }

    private void sendCommandToRemote(int i, String command) {
        Log.i(LOG_TAG, "Send Command to Remote: " + command);
        mBluetoothConnection.write(getCmdBuffer(i, command));
    }

    private byte[] getCmdBuffer(int i,String bufferString) {
        byte[] result = mLoadJniFunction.getDataCmd(i,bufferString);

        return result; 
        
    }

    private void sendPureDatToRemote(byte[] data) {
    	Log.i(LOG_TAG, "sendPureDatToRemote() " + String.valueOf(data));
    	if (isBTConnected()) {
    		mBluetoothConnection.write(data);
    	}
    }

    private void sendBroadcast(int extraType, byte[] extraData) {
        Log.i(LOG_TAG, "sendBroadcast(), extraType=" + extraType);

        // Fill action and extra type
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(BT_BROADCAST_ACTION);
        broadcastIntent.putExtra(EXTRA_TYPE, extraType);

        // Fill extra data, it is optional
        if (extraData != null) {
            broadcastIntent.putExtra(EXTRA_DATA, extraData);
        }

        // Send broadcast
        mContext.sendBroadcast(broadcastIntent);
    }

    public void _sendSyncTime(){
    	try {
			sendSyncTime(false);
		} catch (IOException e) {
			Log.e("gchk", "need sync time , _sendSyncTime e=" + e.getMessage());
			e.printStackTrace();
		}
    }

    private void sendSyncTime(boolean useNewFormat) throws IOException {
        
        long curr_system_time = System.currentTimeMillis();
        int timestamp = Util.getUtcTime(curr_system_time);
        int timezone = Util.getUtcTimeZone(curr_system_time);
        
        if (useNewFormat) {
        	int datalen = String.valueOf(timestamp).length() + 1 + String.valueOf(timezone).length();
	        String snycTime_header = "bnsrv_time mtk_bnapk 0 0 " + String.valueOf(datalen) + " ";
	        String snycTime_data = String.valueOf(timestamp) + " " + String.valueOf(timezone);
	        sendCommandToRemote(LoadJniFunction.CMD_9, snycTime_header);
	        sendPureDatToRemote(snycTime_data.getBytes());
        } else {
        	String snycTime = String.valueOf(timestamp) + " " + String.valueOf(timezone);
	        sendCommandToRemote(LoadJniFunction.CMD_2, snycTime);
        }
        
        Log.i(LOG_TAG, "sendSyncTime()");
    }

    public void sendAlarmTime() throws IOException {

        Date date = Util.getAlarmTime(mContext);
        if (date == null) {
            return;
        }

        int datalen = 2 + String.valueOf(date.getDay()).length() + 1 + 
        		String.valueOf(date.getHours()).length() + 1 + 
        		String.valueOf(date.getMinutes()).length();
        
        String alarmTime_header = "bnsrv_alarm mtk_bnapk 0 0 " + 
        		String.valueOf(datalen) + " ";
        String alarmTime_data = "0 " + 
        		String.valueOf(date.getDay()) + " " + 
        		String.valueOf(date.getHours()) + " " + 
        		String.valueOf(date.getMinutes());

        sendCommandToRemote(LoadJniFunction.CMD_9, alarmTime_header);
        sendPureDatToRemote(alarmTime_data.getBytes());
        Log.i(LOG_TAG, "sendAlarmTime()");
    }

    private void handShakeDone() {
   	 if (isOlderThanVersionTow) {
            isHandshake = true;
            SharedPreferences prefs = mContext.getSharedPreferences("installprefs", mContext.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isConnected", true);
            sendBroadcast(TYPE_BT_CONNECTED, null);
            sendDataFromFile();
            Log.i(LOG_TAG, "mTimer is canceled verstion is old");
        }
        else {
            try {
                sendSyncTime(false);                       
                Log.i(LOG_TAG, "mTimer is canceled verstion is new");
            } catch (UnsupportedEncodingException e) {  
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
   }

   private void runningSyncTimer() {
       
       TimerTask task = new TimerTask(){  
           public void run() {  
               
               Log.i(LOG_TAG, "Timer Task Run ... isHandshake = " + isHandshake);
               this.cancel();
               mTimer = null;
               handShakeDone();
               
             }  
         }; 
       if (mTimer == null) {
       	mTimer = new Timer();
       }
       mTimer.schedule(task,6000);
   }
   
   private void runningReadFSM() {
       Log.i(LOG_TAG, "runningReadFSM() READBUFFERSTATE = " + READBUFFERSTATE);
       //1. Get the Length of Command.
       switch (READBUFFERSTATE)  {
       case READ_IDLE:
           getCommandLenth();
           break;
       case READ_PRE:
           getCmdAndDataLenth();
           break;
       case READ_CMD:
           getData();
           break;
       default:
           break;
       }
   }

   private void getCommandLenth() {
       if (READBUFFERSTATE != READ_IDLE) {
           return;
       }
       int cmdpos = -1 ;
       if (reciveBufferLenth < Util.NOTIFYMINIHEADERLENTH) {
           Log.i(LOG_TAG, "getCommandLenth(): reciveBufferLenth < Constants.NOTIFYMINIHEADERLENTH");
           return;
       }
       else {
           int i;
           for(i = 0; i < reciveBufferLenth - Util.NOTIFYSYNCLENTH ; i ++) {
               if ((reciveBuffer[i] == (byte)(0xF0)) && (reciveBuffer[i + 1] == (byte)(0xF0)) && (reciveBuffer[i + 2] == (byte)(0xF0)) && (reciveBuffer[i + 3] == (byte)(0xF1))) {
                   cmdpos = i;
                   Log.i(LOG_TAG, "getCommandLenth(): Get F0F0F0F1 Success");
                   break;
               }
           }
           if (cmdpos != -1) {
               cmdBufferLenth = reciveBuffer[i + 4] << 24 | reciveBuffer[i + 5] << 16 | reciveBuffer[i + 6] << 8 | reciveBuffer[i + 7];
               System.arraycopy(reciveBuffer, Util.NOTIFYMINIHEADERLENTH, reciveBuffer, 0, reciveBufferLenth - Util.NOTIFYMINIHEADERLENTH);
               reciveBufferLenth = reciveBufferLenth - Util.NOTIFYMINIHEADERLENTH;
               READBUFFERSTATE = READ_PRE;
               Log.i(LOG_TAG, "getCommandLenth(): Get cmdBufferLenth Success " + "cmdBufferLenth is " + cmdBufferLenth + "reciveBufferLenth is " + reciveBufferLenth);
               runningReadFSM();
           }
           else {        
               System.arraycopy(reciveBuffer, Util.NOTIFYMINIHEADERLENTH, reciveBuffer, 0, reciveBufferLenth - Util.NOTIFYMINIHEADERLENTH);
               reciveBufferLenth = reciveBufferLenth - Util.NOTIFYMINIHEADERLENTH;
               READBUFFERSTATE = READ_IDLE;
               Log.i(LOG_TAG, "getCommandLenth(): Get cmdBufferLenth Success " + "cmdBufferLenth is " + cmdBufferLenth + "reciveBufferLenth is " + reciveBufferLenth);
               runningReadFSM();
           }
       }
   }

   private void getCmdAndDataLenth() {
       if (reciveBufferLenth < cmdBufferLenth) {
           Log.i(LOG_TAG, "getDataLenth():reciveBufferLenth < cmdBufferLenth");
           return;
       }
       else {
           commandBuffer = new byte[cmdBufferLenth];
           System.arraycopy(reciveBuffer, 0, commandBuffer, 0, cmdBufferLenth);
           System.arraycopy(reciveBuffer, cmdBufferLenth, reciveBuffer, 0, reciveBufferLenth - cmdBufferLenth);
           reciveBuffer[reciveBufferLenth - cmdBufferLenth] = 0;
           reciveBufferLenth = reciveBufferLenth - cmdBufferLenth;
           if (reciveBufferLenth == 39) {
           	reciveBufferLenth = reciveBufferLenth + 1 - 1;
           }
           Log.i(LOG_TAG, "getDataLenth() :Get cmdBuffer Success " + "cmdBufferLenth is " + cmdBufferLenth + "reciveBufferLenth is " + reciveBufferLenth);
           CMD_TYPE = mLoadJniFunction.getCmdType(commandBuffer,cmdBufferLenth);
           Log.i(LOG_TAG, "Get data Success and the CMD_TYPE is " + CMD_TYPE);

           //noinspection PointlessBooleanExpression
           if (!isBTConnected()) {
               if (mLoadJniFunction.getCmdType(commandBuffer,cmdBufferLenth) == 3){
                   isHandshake = true;
                   Log.i(LOG_TAG,"isHandshake = true");
                   //add by caixinxin for compatible bt4.0 @20150127 start
                   reciveBufferLenth = 0;
                   for (int i = 0; i < reciveBuffer.length; i++) {
                	   reciveBuffer[i] = 0;
                   }
                   //add by caixinxin for compatible bt4.0 @20150127 end
                   sendBroadcast(TYPE_BT_CONNECTED, null);
                   sendDataFromFile();
                   READBUFFERSTATE = READ_IDLE;
                   runningReadFSM();
                   return;
               }
               else if (mLoadJniFunction.getCmdType(commandBuffer,cmdBufferLenth) == 4) {
                   reciveBuffer[0] = 0;
                   reciveBufferLenth = 0;
                   isOlderThanVersionTow = false;
                   READBUFFERSTATE = READ_IDLE;
                   mTimer.cancel();
                   mTimer = null;
                   handShakeDone();
                   Log.i(LOG_TAG, "getDataLenth():Get the Version Success");
                   return;
               }
               else {
                   READBUFFERSTATE = READ_IDLE;
                   return;
               }
           }
          
           if (CMD_TYPE == LoadJniFunction.CMD_1 || CMD_TYPE == LoadJniFunction.CMD_5
                   || CMD_TYPE == LoadJniFunction.CMD_6 || CMD_TYPE == LoadJniFunction.CMD_7
                   || CMD_TYPE == LoadJniFunction.CMD_8 || CMD_TYPE == LoadJniFunction.CMD_9) {
               dataBufferLenth = mLoadJniFunction.getDataLenth(commandBuffer,cmdBufferLenth);
               Log.i(LOG_TAG, "getDataLenth():Get dataBufferLenth Success " + "dataBufferLenth is " + dataBufferLenth);
               if (dataBufferLenth == -1) {
                   READBUFFERSTATE = READ_IDLE;
                   return;
               } 
           }
           else {
               READBUFFERSTATE = READ_IDLE;
               return;
           }
           READBUFFERSTATE = READ_CMD;
           runningReadFSM();
       }
   } 

   private void getData() {
       if (dataBufferLenth <= reciveBufferLenth) {
           dataBuffer = new byte[dataBufferLenth];
           System.arraycopy(reciveBuffer, 0, dataBuffer, 0, dataBufferLenth);
           System.arraycopy(reciveBuffer, dataBufferLenth, reciveBuffer, 0, reciveBufferLenth - dataBufferLenth);
           reciveBuffer[reciveBufferLenth - dataBufferLenth] = 0;
           reciveBufferLenth = reciveBufferLenth - dataBufferLenth;
           READBUFFERSTATE = READ_IDLE;
           //reset dataBufferLenth and cmdBufferLenth
           dataBufferLenth = cmdBufferLenth = 0;
           
           if (CMD_TYPE == LoadJniFunction.CMD_1) {
               sendBroadcast(TYPE_DATA_ARRIVE, dataBuffer);
           } else if (CMD_TYPE == LoadJniFunction.CMD_5 || CMD_TYPE == LoadJniFunction.CMD_6) {
               Log.i(LOG_TAG, "sendBroadcast of MAPX OR MAPD :" + CMD_TYPE);
               Log.i(LOG_TAG, "mIsNeedStartBTMapService is :" + BluetoothService.mIsNeedStartBTMapService);
               if (BluetoothService.mIsNeedStartBTMapService) {
                   sendBroadcasetToMapService(dataBuffer);
               } else {
                   sendBroadcast(TYPE_MAPCMD_ARRIVE, dataBuffer);
               }
           } else if (CMD_TYPE == LoadJniFunction.CMD_9) {
        	   String commmand = new String(dataBuffer);
               String[] commands = commmand.split(" ");
               try {
	                if (commands[1].equals("mtk_bnapk")) {
	                	if (commands[0].equals("bnsrv_time")) {
	                		sendSyncTime(true);
	                	} else if (commands[0].equals("bnsrv_alarm")) {              	
	                		sendAlarmTime();
	                	}
	                	else {
	                		Intent broadcastIntent = new Intent();
	                        broadcastIntent.setAction(commands[1]);
	                        // Fill extra data, it is optional
	                        if (dataBuffer != null) {
	                            broadcastIntent.putExtra(EXTRA_DATA, dataBuffer);
	                        }
	                        mContext.sendBroadcast(broadcastIntent);
	                	}
	                }
               } catch (UnsupportedEncodingException e) {
               	
               } catch (IOException e) {
                   e.printStackTrace();
               }     	
           }
           // construct a string from the valid bytes in the buffer
           Log.i(LOG_TAG, "reciveBufferLenth is " + reciveBufferLenth);
           
           if (reciveBufferLenth == 0) {
               return;
           } else {
               runningReadFSM();
           }
       } else {
           return;
       }
   }

   public void sendBroadcasetToMapService(byte[] dataBuffer) {
       Intent broadcastIntent = new Intent();
       broadcastIntent.setAction(MapConstants.BT_MAP_BROADCAST_ACTION);
       // Fill extra data, it is optional
       if (dataBuffer != null) {
           broadcastIntent.putExtra(MapConstants.EXTRA_DATA, dataBuffer);
       }
       mContext.sendBroadcast(broadcastIntent);
   }

   private static final class MessageHandler extends Handler {
		private Context mContext = null;
		public MessageHandler(BluetoothManager bluetoothManager, Context context) {
		    mBluetoothManager = new WeakReference<BluetoothManager>(bluetoothManager);
		    mContext = context;
		}
		
		@Override
		public void handleMessage(Message msg) {
		    Log.i(LOG_TAG, "handleMessage(), msg.what=" + msg.what);
            BluetoothManager bluetoothManager = mBluetoothManager.get();
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                Log.i(LOG_TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothConnection.STATE_NONE:
                	sppConnectState = BluetoothConnection.STATE_NONE;
                    break;
                case BluetoothConnection.STATE_CONNECTING:
                	sppConnectState = BluetoothConnection.STATE_CONNECTING;
                    break;
                case BluetoothConnection.STATE_LISTEN:
					sppConnectState = BluetoothConnection.STATE_NONE;
                    break;
                case BluetoothConnection.STATE_CONNECTED:
                    bluetoothManager.runningSyncTimer();
                    BtProfileReceiver.stopAutoConnect();
                    sppConnectState = BluetoothConnection.STATE_CONNECTED;
                    break;
                case BluetoothConnection.STATE_CONNECT_LOST:                
                    bluetoothManager.sendBroadcast(TYPE_BT_CONNECTION_LOST, null);
                    dataBufferLenth = reciveBufferLenth = cmdBufferLenth = 0;
                    isHandshake = false;
                    isOlderThanVersionTow = true;
                    sppConnectState = BluetoothConnection.STATE_NONE;
                    //caixinxin add
                    bluetoothManager.setConnectedDeviceName(null);
                    break;
                default:
                    break;
                }
                break;
            case MESSAGE_CONNECT_FAIL:
            	bluetoothManager.sendBroadcast(TYPE_BT_CONNECTION_FAIL, null);
                dataBufferLenth = reciveBufferLenth = cmdBufferLenth = 0;
                isHandshake = false;
                isOlderThanVersionTow = true;
                sppConnectState = BluetoothConnection.STATE_NONE;
                bluetoothManager.setConnectedDeviceName(null);
                break;
            case MESSAGE_TOAST:
            	break;
            case MESSAGE_WRITE:
                bluetoothManager.sendBroadcast(TYPE_DATA_SENT, null);
                break;
            case MESSAGE_READ:
                //1. merge readBuf to reciveBuf
                byte[] readBuf = (byte[]) msg.obj;
                int bytes = msg.arg1;
                System.arraycopy(readBuf, 0, reciveBuffer, reciveBufferLenth, bytes);
                reciveBufferLenth = reciveBufferLenth + bytes;
                Log.i(LOG_TAG, "reciveBufferLenth is " + reciveBufferLenth);
                bluetoothManager.runningReadFSM();
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
            	BluetoothDevice device = (BluetoothDevice) msg.getData().getParcelable(DEVICE_NAME);
                bluetoothManager.setConnectedDeviceName(device);
                break;         
            default:
                break;
            }
		}
    }
    
    private final BroadcastReceiver mBTReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothManager bluetoothManager = mBluetoothManager.get();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int connectionState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                Log.i(LOG_TAG, "onReceive(), action=" + intent.getAction());

                // Setup or remove connection according to bt connection state
                if (connectionState == BluetoothAdapter.STATE_ON) {
                    setupConnection();
                } else if (connectionState == BluetoothAdapter.STATE_OFF) {
                    removeConnection();
                } else {
                    return;
                }
            }
        }
    };
}
