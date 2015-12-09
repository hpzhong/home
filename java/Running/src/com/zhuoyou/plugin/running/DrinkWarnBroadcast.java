package com.zhuoyou.plugin.running;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DrinkWarnBroadcast extends BroadcastReceiver {
	private SharedPreferences mSharedPres;
	Boolean phoneState = true;
	@Override
	public void onReceive(Context context, Intent arg1) {
		// TODO Auto-generated method stub
		
		mSharedPres = context.getSharedPreferences("TestResult", Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor editor  = mSharedPres.edit();
		
		// TODO Auto-generated method stub
		if(arg1.getAction().equals("Drink_Water_Warn")){
			Log.v("renjing", "Drink_Water_Warn");
			
			 long current = System.currentTimeMillis();
			if(WaterIntakeUtils.isInWarnTime(context.getApplicationContext(), current)){
				
			Intent intent=new Intent(context,WarnDialogActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
			}
			
		}else if(arg1.getAction().equals(Intent.ACTION_DATE_CHANGED)){
			editor.putInt("water_num", 0);
			editor.commit();
		}
		
	}

}
