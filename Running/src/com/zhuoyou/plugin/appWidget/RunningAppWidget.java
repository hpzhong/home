package com.zhuoyou.plugin.appWidget;

import java.text.NumberFormat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import com.zhuoyou.plugin.bluetooth.data.Util;
import com.zhuoyou.plugin.database.DataBaseContants;
import com.zhuoyou.plugin.running.FirstActivity;
import com.zhuoyou.plugin.running.PersonalGoal;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;
import com.zhuoyou.plugin.running.RunningItem;
import com.zhuoyou.plugin.running.Tools;
  
  public class RunningAppWidget extends AppWidgetProvider {
      public static int Tag;
      public int max;
      public int current;
  	  private Context mCtx = RunningApp.getInstance();
  	  private float num = 0;
  	  private PersonalGoal personal;
  	  private Context sContext = RunningApp.getInstance().getApplicationContext();
      @Override
      public void onEnabled(Context context) {
          // TODO Auto-generated method stub
  
          super.onEnabled(context);
      }
    private static int PHONE_STEP = 0;
    private static int PHONE_CALORIES = 0;
  	private void readPhoneStep(String day) {
  		ContentResolver cr = mCtx.getContentResolver();
  		Cursor c = cr.query(DataBaseContants.CONTENT_URI, new String[] { "_id", "steps","calories" },
  				DataBaseContants.DATE   + "  = ? AND " + DataBaseContants.STATISTICS + " = ?", new String[] {day,  "1" }, null);
  		
  		c.moveToFirst();
  		PHONE_STEP = 0;
  		PHONE_CALORIES = 0;
  		if (c.getCount() > 0) {
  			for (int y = 0; y < c.getCount(); y++) {
  				PHONE_STEP = c.getInt(c.getColumnIndex(DataBaseContants.STEPS));
  				PHONE_CALORIES = c.getInt(c.getColumnIndex(DataBaseContants.CALORIES));
  				c.moveToNext();
  			}
  		}
  		c.close();
  		c = null;
  	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		super.onReceive(context, intent);
		
		String day = Tools.getDate(0);
		readPhoneStep(day);
		
		personal = Tools.getPersonalGoal();
		num = 0;
		if (PHONE_STEP != 0) {
				num = (float) PHONE_STEP / personal.mGoalSteps;
			
		}
		ComponentName thisWidget = new ComponentName(context,
				RunningAppWidget.class);
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.running_app_widget);
		views.setTextViewText(R.id.tv_steps, String.valueOf(PHONE_STEP));
		views.setTextViewText(R.id.tv_cal, String.valueOf(PHONE_CALORIES));
		views.setTextViewText(R.id.tv_completion,String.valueOf((int)(num*100/1)));
		views.setImageViewBitmap(R.id.im_arc_percent, updateNotificationRemoteViews(sContext, num));
		Intent FirstActivityIntent = new Intent(context, FirstActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, FirstActivityIntent, 0);
        views.setOnClickPendingIntent(R.id.running_app_widget, pendingIntent);
        Log.i("111", "qqqq");
		String action = intent.getAction();
		if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				int[] appWidgetIds = extras
						.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
				if (appWidgetIds != null && appWidgetIds.length > 0) {
					this.onUpdate(context,
							AppWidgetManager.getInstance(context), appWidgetIds);
				}
			}
		} else if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
			Bundle extras = intent.getExtras();
			if (extras != null
					&& extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
				final int appWidgetId = extras
						.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
				this.onDeleted(context, new int[] { appWidgetId });
			}
		} else if (AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(action)) {
			this.onEnabled(context);
		} else if (AppWidgetManager.ACTION_APPWIDGET_DISABLED.equals(action)) {
			this.onDisabled(context);
		}
		AppWidgetManager appmanager = AppWidgetManager.getInstance(context);
		appmanager.updateAppWidget(thisWidget, views);

	}
  
      

	@Override
      public void onUpdate(Context context, AppWidgetManager appWidgetManager,
              int[] appWidgetIds) {
          // TODO Auto-generated method stub
          final int N = appWidgetIds.length;
          for (int i = 0; i < N; i++) {
              int appWidgetId = appWidgetIds[i];
              RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.running_app_widget);
             /* Intent FirstActivityIntent = new Intent(context, FirstActivity.class);
              PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, FirstActivityIntent, 0);
              views.setOnClickPendingIntent(R.id.running_app_widget, pendingIntent);*/
              AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);   
              Intent updateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);  
              int requestCode = 0;   
              PendingIntent pendIntent = PendingIntent.getBroadcast(context, requestCode, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);   
              // 5秒后发送广播，然后每隔10秒重复发广播。广播都是直接发到AlarmReceiver的   
              long triggerAtTime = SystemClock.elapsedRealtime() + 5 * 1000;   
              int interval = 10 * 1000;   
              alarmMgr.setRepeating(AlarmManager.RTC, triggerAtTime, interval, pendIntent);  
              appWidgetManager.updateAppWidget(appWidgetId, views);
          }
      }
  
      static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,int appWidgetId){
          Log.d("app", "update---->id" + appWidgetId);
          RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.running_app_widget);
          appWidgetManager.updateAppWidget(appWidgetId, views);
          
      }
  	public Bitmap updateNotificationRemoteViews(Context paramContext, float num) {
		double d = num;
		if (num > 0) {
			if (num < 0.01) {
				d = 0.01d;
			}
			if (num >= 1.0d) {
				d = 1.0d;
			}
		}
		NumberFormat localNumberFormat = NumberFormat.getPercentInstance();
		localNumberFormat.setMinimumFractionDigits(0);
		Bitmap localBitmap = Bitmap.createBitmap(Util.dip2pixel(paramContext, 53.0F), Util.dip2pixel(paramContext, 53.0F), Bitmap.Config.ARGB_8888);
		Canvas localCanvas = new Canvas(localBitmap);
		Paint localPaint = new Paint();
		localPaint.setAntiAlias(true);
		localPaint.setColor(Color.rgb(236, 122, 35));
		localPaint.setStyle(Paint.Style.STROKE);
		localPaint.setStrokeWidth(Util.dip2pixel(paramContext, 6.0F));
		RectF localRectF = new RectF(Util.dip2pixel(paramContext, 3.0F), Util.dip2pixel(paramContext, 3.0F), Util.dip2pixel(paramContext, 49.0F), Util.dip2pixel(paramContext, 49.0F));
		int j = (int) (360.0D * d);
		localCanvas.drawArc(localRectF, -90, j, false, localPaint);
		return localBitmap;
	}
 }
  
  
  