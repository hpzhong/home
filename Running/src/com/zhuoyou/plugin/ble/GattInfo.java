package com.zhuoyou.plugin.ble;

import java.util.UUID;


public class GattInfo {
	// Bluetooth SIG identifiers
	public static final long leastSigBits = 0x800000805f9b34fbL;
	public static final UUID HEART_RATE_SERVICE = new UUID((0x180DL << 32) | 0x1000, leastSigBits);
	public static final UUID HEART_RATE_MEASUREMENT = new UUID((0x2A37L << 32) | 0x1000, leastSigBits);
	public static final UUID HEART_RATE_NOTICEFATION_ENABLE = new UUID((0x2902L << 32) | 0x1000, leastSigBits);

	public static final UUID STEPS_SERVICE = new UUID((0x1817L << 32) | 0x1000, leastSigBits);
	public static final UUID SEGMENT_STEPS_MEASUREMENT = UUID.fromString("6e400004-b5a3-f393-e0a9-e50e24dcca9e");
	public static final UUID TOTAL_STEPS_MEASUREMENT = UUID.fromString("6e400005-b5a3-f393-e0a9-e50e24dcca9e");
	public static final UUID SLEEP_INFO_CHAR = UUID.fromString("6e400006-b5a3-f393-e0a9-e50e24dcca9e");
	public static final UUID STEPS_NOTICEFATION_ENABLE = new UUID((0x2902L << 32) | 0x1000, leastSigBits);
	
	public static final UUID ALARM_SERVICE = new UUID((0x1820L << 32) | 0x1000, leastSigBits);
	public static final UUID ALARM_MEASUREMENT = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
	
	public static final UUID VIBRATION_REMIND = new UUID((0xFFA8L << 32) | 0x1000, leastSigBits);
	public static final UUID DEVICE_NAME = new UUID((0x2A00L << 32) | 0x1000, leastSigBits);
	public static final UUID BATTERY_LEVEL = new UUID((0x2A19L << 32) | 0x1000, leastSigBits);
	public static final UUID TIME_SYNC = new UUID((0x2A61L << 32) | 0x1000, leastSigBits);
	public static final UUID TIME_AND_ALARM_INFO = new UUID((0x2A60L << 32) | 0x1000, leastSigBits);
	//20150602 Heph add
	public static final UUID BATTERY_SERVICE = new UUID((0x180FL << 32) | 0x1000, leastSigBits);
	public static final UUID OTA = new UUID((0x2A5FL << 32) | 0x1000, leastSigBits);
	
	public static final UUID DEVICE_NAME_SERVICE = new UUID((0x1800L << 32) | 0x1000, leastSigBits);
	public static final UUID DEVICE_NAME_CHAR = new UUID((0x2A00L << 32) | 0x1000, leastSigBits);
	
	//�̼���
	public static final UUID OAD_SERVICE_UUID = UUID.fromString("f000ffc0-0451-4000-b000-000000000000");
	public static final UUID OAD_ENABLE_UUID = new UUID((0x2902L << 32) | 0x1000, leastSigBits);
	
	public static final UUID FIND_PHONE_SERVICE = new UUID((0x1817L << 32) | 0x1000, leastSigBits);
	public static final UUID FIND_PHONE_MEASUREMENT = UUID.fromString("6e400007-b5a3-f393-e0a9-e50e24dcca9e");
	public static final UUID FIND_PHONE_NOTIFY_ENABLE = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
	
	
	public static final UUID DEVICEINFO_SERVICE = new UUID((0x180AL << 32) | 0x1000, leastSigBits);
	public static final UUID DEVICEINFO_MEASUMENT = new UUID((0x2A26L << 32) | 0x1000, leastSigBits);
	public static final UUID FIRMWARE_READY_SERVICE = UUID.fromString("00001817-0000-1000-8000-00805f9b34fb");
	public static final UUID FIRMWARE_READY_MEASUMENT = UUID.fromString("00002a5f-0000-1000-8000-00805f9b34fb");

	public GattInfo() {

	}
	
}
