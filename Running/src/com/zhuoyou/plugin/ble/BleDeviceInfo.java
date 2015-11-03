package com.zhuoyou.plugin.ble;

import android.bluetooth.BluetoothDevice;

public class BleDeviceInfo {

  private BluetoothDevice mBtDevice;
  private int mRssi;

  public BleDeviceInfo(BluetoothDevice device, int rssi) {
    mBtDevice = device;
    mRssi = rssi;
  }

  public BluetoothDevice getBluetoothDevice() {
    return mBtDevice;
  }

  public int getRssi() {
    return mRssi;
  }

  public void updateRssi(int rssiValue) {
    mRssi = rssiValue;
  }

}
