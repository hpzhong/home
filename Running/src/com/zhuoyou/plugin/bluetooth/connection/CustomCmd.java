package com.zhuoyou.plugin.bluetooth.connection;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.zhuoyou.plugin.bluetooth.attach.PlugBean;
import com.zhuoyou.plugin.bluetooth.attach.PlugUtils;
import com.zhuoyou.plugin.bluetooth.attach.PluginManager;
import com.zhuoyou.plugin.bluetooth.data.MessageHeader;
import com.zhuoyou.plugin.bluetooth.data.MessageObj;
import com.zhuoyou.plugin.bluetooth.data.SmsMessageBody;
import com.zhuoyou.plugin.bluetooth.data.Util;
import com.zhuoyou.plugin.bluetooth.service.BluetoothService;
import com.zhuoyou.plugin.running.RunningApp;

public class CustomCmd {

	private static Context mCtx = RunningApp.getInstance().getApplicationContext();
	private static PackageManager mPackageManager = mCtx.getPackageManager();
	private static final String PLUG_PACKAGENAME_PREX = "com.zhuoyou.plugin.";
	public static final String BT_BROADCAST_ACTION_DEVICE_NAME = "com.mtk.connection.BT_BROADCAST_ACTION_DEVICE_NAME";
	public static final String DEVICE_NAME = "DEVICE_NAME";
	public static final String DEVICE_TYPE = "DEVICE_TYPE";
	private static final String TAG = "CustomCmd";
	public static final String TYD_NUMBER = "99999999999";
	public static final int CMD_BASE = 0X0020;
	public static final int CMD_SYNC_TIME = 0x0002;
	public static final int CMD_GET_BATTERY = 0x0003;
	public static final int CMD_GET_FIRMWARE_VERSION = 0x0005;
	public static final int CMD_INCOMING_NAME = 0x0020;
	public static final int CMD_VISITING_CARD = 0x0030;
	public static final int CMD_REMOTE_READ_SMS = 0x0040;
	public static final int CMD_SYNC_SPORT_DATA = 0x0071;
	public static final int CMD_SYNC_PERSONAL_DATA = 0x0074;
	public static final int CMD_SYNC_SLEEP_MSG = 0x0081;

	private static final Handler CMD_HANDLER = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Context ctx = RunningApp.getInstance().getApplicationContext();
			Intent intent = null;
			char[] c_ucs2 = (char[]) msg.obj;
			int index = 1;
			char c_platform[] = new char[20];
			for (int i = 0; i < 20; i++) {
				c_platform[i] = c_ucs2[index++];
			}
			char c_verno[] = new char[8];
			for (int i = 0; i < 8; i++) {
				c_verno[i] = c_ucs2[index++];
			}
			char c_tag[] = new char[4];
			c_tag[0] = c_ucs2[index++];
			c_tag[1] = c_ucs2[index++];
			c_tag[2] = c_ucs2[index++];
			c_tag[3] = c_ucs2[index++];

			char[] c_msg = new char[c_ucs2.length - index];
			System.arraycopy(c_ucs2, index, c_msg, 0, c_ucs2.length - index);
			String s_utf8 = new String(c_msg);
			
			switch (msg.what) {
			case CMD_SYNC_TIME:
				BluetoothService main = BluetoothService.getInstance();
				if(main!=null){
					Log.e(TAG, "need sync time , send now");
					main._sendSyncTime();
				}else{
					Log.e(TAG, "send custom sync time error");
				}
				break;
			case CMD_GET_BATTERY:
				intent = new Intent("com.tyd.bt.device.battery");
				intent.putExtra("tag", c_tag);
				ctx.sendBroadcast(intent);
				break;
			case CMD_GET_FIRMWARE_VERSION:
				intent = new Intent("com.tyd.bt.device.firmware");
				intent.putExtra("content", s_utf8);
				ctx.sendBroadcast(intent);
				break;
			case CMD_INCOMING_NAME:
				Log.i(TAG, "incoming number =" + s_utf8);
				if (!s_utf8.equals("")) {
					String name = Util.getContactName(ctx, s_utf8);
					sendCustomCmd(CMD_INCOMING_NAME, name, c_tag);
				}
				break;
			case CMD_VISITING_CARD:
				Log.i(TAG, "spp connected device name =" + s_utf8);
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(BT_BROADCAST_ACTION_DEVICE_NAME);
				broadcastIntent.putExtra(DEVICE_NAME, s_utf8);
				broadcastIntent.putExtra(DEVICE_TYPE, c_tag[0]);
				ctx.sendBroadcast(broadcastIntent);
				break;
			case CMD_REMOTE_READ_SMS:
				int sms_id = c_tag[0];
				Log.i(TAG, "read sms = id prev(int) = " +sms_id );
				sms_id -= 0x20;
				Log.i(TAG, "read sms = id curr(int) = " +sms_id );
				/**
				 * 从数据空中寻找该ID.并check其是否为未读.如果是.则置为已读
				 */
				Util.setUnreadSmsToRead(ctx , sms_id);
				break;
			case CMD_SYNC_SPORT_DATA:
				Log.i("sleep", "running:c_tag:"+String.valueOf(c_tag) + "\t s_utf8:"+s_utf8);
				BluetoothDevice device = BtProfileReceiver.getRemoteDevice();
				String name = device.getName();
				String address = device.getAddress();
				String productName = Util.getProductName(name);
				intent = new Intent("com.zhuoyou.plugin.running.get");
				intent.putExtra("tag", c_tag);
				intent.putExtra("content", s_utf8);
				intent.putExtra("from", productName + "|" + address);
				ctx.sendBroadcast(intent);
				break;
			case CMD_SYNC_PERSONAL_DATA:
				intent = new Intent("com.zhuoyou.plugin.running.sync.personal");
				intent.putExtra("tag", c_tag);
				intent.putExtra("content", s_utf8);
				ctx.sendBroadcast(intent);
				break;
			case CMD_SYNC_SLEEP_MSG:
				Log.i("sleep", "sleep:c_tag:"+String.valueOf(c_tag) + "\t s_utf8:"+s_utf8);
				intent = new Intent("com.zhuoyou.plugin.running.sleep");
				intent.putExtra("tag", c_tag);
				intent.putExtra("content", s_utf8);
				ctx.sendBroadcast(intent);
				break;
			default:
				if (praserInPlug(msg.what, c_tag, s_utf8)) {

				} else {
					Log.e(TAG, "未知CMD =" + msg.what);
				}
				break;
			}
		}
	};

	//分析传入的message指令是否为我们内容自定义指令
	public static boolean praser(String address, String message) {
		boolean ret = false;
		if (address.equals(TYD_NUMBER)) {
			char[] c_ucs2 = message.toCharArray();
			char c_cmd = c_ucs2[0];

			if (c_cmd > CMD_BASE) {
				CMD_HANDLER.obtainMessage(c_cmd - CMD_BASE, c_ucs2).sendToTarget();
			} else {
				Log.e(TAG, "小于0X20的命令暂不被支持CMD =" + (int) c_cmd);
			}
			ret = true;
		}
		return ret;
	}

	public static void sendCustomCmd(int cmd, String s) {
		char[] c_tag = new char[4];
		c_tag[0] = 0xFF;
		c_tag[1] = 0xFF;
		c_tag[2] = 0xFF;
		c_tag[3] = 0xFF;

		sendCustomCmd(cmd, s, c_tag);
	}

	public static void sendCustomCmd(int cmd, String s, char[] tag) {
		String content = "";
		Log.i(TAG, "sendCustomCmd cmd= " + cmd + " ||s = " + s);
		
		switch (cmd) {
		case CMD_INCOMING_NAME:
			content = buildMsgContent(cmd + CMD_BASE, s, tag);
			break;
		case CMD_REMOTE_READ_SMS:
			content = buildMsgContent(cmd + CMD_BASE, s, tag);
			break;
		default:                      
			content = buildMsgContent(cmd + CMD_BASE, s, tag);
			break;
		}
		
		// Fill message header
		MessageHeader header = new MessageHeader();
		header.setCategory(MessageObj.CATEGORY_NOTI);
		header.setSubType(MessageObj.SUBTYPE_SMS);
		header.setMsgId(Util.genMessageId());
		header.setAction(MessageObj.ACTION_ADD);

		String phoneNum = TYD_NUMBER;
		String sender = "TYD";

		int timestamp = Util.getUtcTime(Calendar.getInstance().getTimeInMillis());

		SmsMessageBody body = new SmsMessageBody();
		body.setSender(sender);
		body.setNumber(phoneNum);
		body.setContent(content);
		body.setTimestamp(timestamp);

		MessageObj smsMessageData = new MessageObj();
		smsMessageData.setDataHeader(header);
		smsMessageData.setDataBody(body);
		
		BluetoothService.getInstance().sendSmsMessage(smsMessageData);
	}

	public static String buildMsgContent(int cmd, String s, char[] c_tag) {
		String s_utf8 = "";

		char[] c_cmd = new char[1];
		c_cmd[0] = (char) cmd;

		char[] c_msg = s.toCharArray();

		int length = c_cmd.length + c_tag.length + c_msg.length;
		char[] c_ucs2 = new char[length];

		System.arraycopy(c_cmd, 0, c_ucs2, 0, c_cmd.length);
		System.arraycopy(c_tag, 0, c_ucs2, c_cmd.length, c_tag.length);
		System.arraycopy(c_msg, 0, c_ucs2, c_cmd.length + c_tag.length, c_msg.length);

		s_utf8 = new String(c_ucs2);

		return s_utf8;
	}

	public static boolean praserInPlug(int cmd, char[] tag, String content) {
		if (PluginManager.getInstance() == null) {
			return false;
		}
		List<PlugBean> beans = PluginManager.getInstance().getPlugBeans();
		if (beans == null) {
			return false;
		}
		
		String mPackageName = packageNameByCmd(cmd);
		if (cheackPlugs(mPackageName)) {
			if (mPackageName.equals("com.zhuoyou.plugin.antilost")) {
				Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
				intent.putExtra("plugin_cmd", 0x17);
				intent.putExtra("plugin_content", "antilost");
				mCtx.sendBroadcast(intent);
			}
		} else {
			if (mPackageName.equals("com.zhuoyou.plugin.antilost")) {
				Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
				intent.putExtra("plugin_cmd", 0x17);
				intent.putExtra("plugin_content", "antilost");
				intent.putExtra("plugin_tag", getTag1());
				mCtx.sendBroadcast(intent);
			} else if (mPackageName.equals("com.zhuoyou.plugin.autocamera")) {
				Intent intent = new Intent("com.tyd.plugin.receiver.sendmsg");
				intent.putExtra("plugin_cmd", 0x53);
				intent.putExtra("plugin_content", "autocamera");
				intent.putExtra("plugin_tag", getTag2());
				mCtx.sendBroadcast(intent);
			}
		}
		
		for (int i = 0; i < beans.size(); i++) {
			PlugBean bean = beans.get(i);
			if (bean.isSystem || !bean.isInstalled) {
				continue;
			}
			String[] cmds = bean.mSupportCmd.split("\\|");
			for (int n = 0; n < cmds.length; n++) {
				if (cmds[n].equals("")) {
					continue;
				}
				int s_cmd = Integer.parseInt(cmds[n], 16);
				if (s_cmd == cmd) {					
					if (bean.mWorkMethod != null) {
						Iterator it = bean.mWorkMethod.entrySet().iterator();
						while (it.hasNext()) {
							Entry entry = (Entry) it.next();
							String key = (String) entry.getKey(); 
							String value = (String) entry.getValue();
							if (Integer.parseInt(key, 16) == s_cmd) {
								PlugUtils.invoke_method(bean, value , tag,  content);
								android.util.Log.i("gchk", key + "   " + value);
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	private static Boolean cheackPlugs(String name) {
		List<String> mInstalledPlugs = new ArrayList<String>();
		List<PackageInfo> pkgs = mPackageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		for (PackageInfo pkg : pkgs) {
			if (pkg.packageName.startsWith(PLUG_PACKAGENAME_PREX)) {
				mInstalledPlugs.add(pkg.packageName);
			} else {
				continue;
			}
		}
		if (mInstalledPlugs != null && mInstalledPlugs.size() > 0) {
			for (int i = 0; i < mInstalledPlugs.size(); i++) {
				if (mInstalledPlugs.get(i).equals(name))
					return true;
			}
		}
		return false;
	}

	private static String packageNameByCmd(int cmd) {
		String name = "";
		int[] command = {16,17,18,19,20,21,23,80,82,84};
		for (int i = 0; i < command.length; i++) {
			if (command[i] == cmd) {
				if (i < 7) {
					name = "com.zhuoyou.plugin.antilost";
				} else {
					name = "com.zhuoyou.plugin.autocamera";
				}
			}
		}
		return name;
	}
	
	private static char[] getTag1() {
		char[] c_tag = new char[4];
		c_tag[0] = 0x21;
		c_tag[1] = 0xFF;
		c_tag[2] = 0xFF;
		c_tag[3] = 0xFF;
		return c_tag;
	}

	private static char[] getTag2() {
		char[] c_tag = new char[4];
		c_tag[0] = 0x22;
		c_tag[1] = 0xFF;
		c_tag[2] = 0xFF;
		c_tag[3] = 0xFF;
		return c_tag;
	}
}
