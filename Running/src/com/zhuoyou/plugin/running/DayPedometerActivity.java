package com.zhuoyou.plugin.running;

import java.util.List;

import com.mcube.lib.ped.PedBackgroundService;
import com.zhuoyou.plugin.ble.BleManagerService;
import com.zhuoyou.plugin.bluetooth.connection.BtProfileReceiver;
import com.zhuoyou.plugin.bluetooth.data.Util;
import com.zhuoyou.plugin.custom.CustomAlertDialog;
import com.zhuoyou.plugin.gps.FirstGpsActivity;
import com.zhuoyou.plugin.mainFrame.MineFragment;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DayPedometerActivity extends Activity {

	private RelativeLayout im_back;
	private TextView mTitle;
	private ImageView mEnable;

	private Boolean isOpen;
	private LinearLayout mTestPed;

	private String preDeviceAddress; // latest device address
	private BluetoothDevice currentBleDevice = null;
	private BluetoothDevice currentClassicDevice = null;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.day_pedometer_layout);

		mTestPed = (LinearLayout) findViewById(R.id.test_ped);
		mTestPed.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(DayPedometerActivity.this,
						FirstGpsActivity.class);
				intent.putExtra("from", "DayPedometerActivity");
				startActivity(intent);
			}

		});
		mEnable = (ImageView) findViewById(R.id.pedometer_enable);
		isOpen = Tools.getPhonePedState();
		setImageSource(isOpen);

		mTitle = (TextView) findViewById(R.id.title);
		mTitle.setText(R.string.day_pedometer);
		im_back = (RelativeLayout) this.findViewById(R.id.back);
		im_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mEnable.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				isOpen = !isOpen;
				setImageSource(isOpen);
				Tools.setPhonePedState(isOpen);
				phoneStepsServiceSwitch();
			}

		});
	}

	private void setImageSource(Boolean isOpen) {
		// TODO Auto-generated method stub
		Tools.setPhonePedState(isOpen);
		if (isOpen) {
			mEnable.setImageResource(R.drawable.warn_on);
			Intent phoneStepsIntent = new Intent(getApplicationContext(),
					PedBackgroundService.class);
			startService(phoneStepsIntent);

		} else {
			mEnable.setImageResource(R.drawable.warn_off);
			Intent phoneStepsIntent = new Intent(getApplicationContext(),
					PedBackgroundService.class);
			stopService(phoneStepsIntent);
		}
	}

	private void phoneStepsServiceSwitch() {
		if (isOpen) {
			if (preDeviceAddress != null) {
				preDeviceAddress = Util.getLatestConnectDeviceAddress(mContext);
				List<BluetoothDevice> gattConnectedDeviceList = BleManagerService
						.getInstance().getGattCurrentDevice();
				if (gattConnectedDeviceList != null) {
					for (int i = 0; i < gattConnectedDeviceList.size(); i++) {
						if (preDeviceAddress.equals(gattConnectedDeviceList.get(i)
								.getAddress())) {
							currentBleDevice = gattConnectedDeviceList.get(i);
						}
					}
				}
			}
			currentClassicDevice = BtProfileReceiver.getRemoteDevice();

		/*	if (currentBleDevice != null || currentClassicDevice != null) {
				CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(
						this);
				builder.setTitle(R.string.alert_title);
				builder.setMessage(R.string.open_phone_steps);
				builder.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent phoneStepsIntent = new Intent(
										getApplicationContext(),
										PedBackgroundService.class);
								startService(phoneStepsIntent);
								isOpen = Tools.getPhonePedState();
								dialog.dismiss();
								// finish();
							}
						});
				builder.setNegativeButton(R.string.cancle,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								mEnable.setImageResource(R.drawable.warn_off);
								isOpen = !isOpen;
								Tools.setPhonePedState(isOpen);
							}
						});
				builder.setCancelable(false);
				builder.create().show();
			} else*/ {
				Intent phoneStepsIntent = new Intent(getApplicationContext(),
						PedBackgroundService.class);
				startService(phoneStepsIntent);
			}
		}
	}

}
