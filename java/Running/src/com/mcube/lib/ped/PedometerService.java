/**
* Tendegrees pedometer service. 
* @file
*
* @brief   This file defines Tendegrees pedometer service which runs pedometer library and provides step 
* information to clients binded to the service.
*
*/
package  com.mcube.lib.ped;

import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.mcube.lib.ped.PedLibrary;
import com.mcube.lib.ped.PedListener;

/**
 * Pedometer service
 */


public class PedometerService extends Service {
	private final String TAG = "PedometerService";
	
	/** storage for listeners */
	private ArrayList<PedometerListener> mListeners = new ArrayList<PedometerListener>();
	
	//Kevin private LocalBinder mBinder = new LocalBinder();
	private final IBinder mBinder = new LocalBinder();
	
	/** object to do step detection*/
	private PedLibrary mLibrary = new PedLibrary();
	
	/** object to do pedometer control like start/stop/clear*/
	private PedometerController mController = new PedometerController();
	

    /** listener to receive notifications from the PedLibrary when pedometer events occur*/
	private PedListenerImp mPedListener = new PedListenerImp();
	private SensorManager mSensorManager       = null;
	
	/** listener to receive accelerometer sensor event */
	private AccelListener mAccelListener = new AccelListener();

	 /** A variable describing whether accelerometer listener has been registered or not.*/
	boolean AccleListenerRegisted = false;
	
	private long mPrevisouTime = 0L;
	private PowerManager mPowerManager;
	//private WindowManager mWindowManager;
	
	private static int mPwrState = 0;   //Kevin
	private static final float MeanTime = 30000000.0f; //30ms
	private static final float NS2MS = 1.0f / 1000000.0f;
	//private float timestamp;
	private int stepCountOffset =0;		// SL
	private int currentStepCount =0;	// SL
	@Override
	public IBinder onBind(Intent intent) {		
		return mBinder;
	}
	
	
	/** 
	 * Binder class to realize pedometer service
	 * */
	public class LocalBinder extends Binder{
				
		/**
	    * Register a pedometer event listener.
	    * @param listener pedometer event listener to add.
	    */
		public void registerListeners(PedometerListener listener)
		{
			Log.d(TAG,"registerListeners()");
			mController.registerListeners(listener);
		}
		
		/**
		* Unregister a pedometer event listener.
		* @param listener previously registered pedometer event listener to remove
		*/
		public void unRegisterListeners(PedometerListener listener)
		{
			Log.d(TAG,"unRegisterListeners()");
			mController.unRegisterListeners(listener);
		}
		
		/**
		* Start pedometer service to do step detection		
		*/
		public void startPedometer()
		{
			Log.d(TAG,"startPedometer()");
			mController.startPedometer();
		}
		
		/**
		* Stop step detection		
		*/
		public void stopPedometer()
		{
			Log.d(TAG,"stopPedometer()");
			mController.stopPedometer();
		}		
		/**
		* Clear pedometer step readings to zero.
		*/
		public void clearPedometerStepCount() 
		{
			Log.d(TAG,"clearPedometerStepCount()");
			mController.clearPedometerStepCount();	
		}
	}
	
	/** 
	 * class to realize pedometer control like start/stop/clear
	 */
	class PedometerController
	{
		/**
		* Register a pedometer event listener.
		* @param listener pedometer event listener to add.
		*/
		public void registerListeners(PedometerListener listener)
		{
			Log.d("PedometerController","PedometerController.registerListeners()");
			if (!mListeners.contains(listener))
			{
				mListeners.add(listener);				
			}						
		};
		/**
		* Unregister a pedometer event listener.
		* @param listener previously registered pedometer event listener to remove
		*/
		public void unRegisterListeners(PedometerListener listener)
		{
			Log.d("PedometerController","PedometerController.unRegisterListeners()");
			if (mListeners.contains(listener))
				mListeners.remove(listener);
			if (mListeners.size() == 0)
			{
				mSensorManager.unregisterListener(mAccelListener);
			}
		}		
		/**
		* Start pedometer service to do step detection		
		*/
		public void startPedometer()
		{
			Log.d("PedometerController","PedometerController.startPedometer()");
			if (mListeners.size()> 0)
			{
				Sensor mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				if(null != mAccel)
		            {
		                if (true == mSensorManager.registerListener(mAccelListener, mAccel, SensorManager.SENSOR_DELAY_FASTEST))
		                {
		                	AccleListenerRegisted = true;
		                };
		            }
			}
			if (mLibrary.IsOpened())
			{	
				mLibrary.Close();
				Log.d(TAG,"mLibrary.Close()");
			}
		    //mLibrary.Open(PedLibrary.STEP_SENSITIVTY_MEDIUM);
			if(mLibrary.Open(PedLibrary.STEP_SENSITIVTY_MEDIUM))
			{	
				Log.d(TAG,"==================================Open PedLibrary Success");
			}
			mLibrary.registerListener(mPedListener);
			Log.d(TAG,"==================================mLibrary.registerListener(mPedListener)");
			mLibrary.PollVersion();
			Log.d(TAG,"==================================mLibrary.PollVersion()");
			
			
			//mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		}
		
		/**
		* Stop step detection		
		*/
		public void stopPedometer()
		{
			Log.d("PedometerController","PedometerController.stopPedometer()");
			if (true == AccleListenerRegisted)
			{
				 mSensorManager.unregisterListener(mAccelListener);
				 AccleListenerRegisted = false;				
			}
			if (mLibrary.IsOpened())
			{
				mLibrary.unregisterListener(mPedListener);
				mLibrary.Close();
				stepCountOffset += currentStepCount;	// SL
				Log.d(TAG,"stepCountOffset=" + stepCountOffset);
			}		
		}
		/**
		* Clear pedometer step readings to zero.
		*/
		public void clearPedometerStepCount() 
		{
			Log.d("PedometerController","PedometerController.clearPedometerStepCount()");
			mPedListener.clearStepCount();				
		}
	}
	
	/** listener class to receive accelerometer sensor event */
	class AccelListener implements SensorEventListener
	{
		PedometerController mPedometerController = new PedometerController();
		@Override
		public void onSensorChanged(SensorEvent event) {	
			final float dT = (event.timestamp - mPrevisouTime) * NS2MS;
			//Log.d(TAG,"mPrevisouTime=" + mPrevisouTime +", EventTime=" + event.timestamp +", X=" + event.values[0] +", Y=" + event.values[1] + ", Z=" + event.values[2] );
			Log.d(TAG,"DeltaTime=" + dT +", X=" + event.values[0] +", Y=" + event.values[1] + ", Z=" + event.values[2] );
			
			if(!mPowerManager.isScreenOn())
			{	
			    Log.d(TAG,"Display OFF, mPwrStart = " + mPwrState);
			    if(mPwrState == 0)
			    {	
			    	mPwrState = 1;
					Log.d(TAG,"Set mPwrState = 1");
			    }
			   			    
			    if((event.timestamp - mPrevisouTime) <  MeanTime) 
			    {
					mLibrary.ProcessData(event.timestamp, event.values[0], event.values[1], event.values[2]);
			    	//Log.d(TAG,"AccelListener.ProcessData()");
					Log.d(TAG,"(event.timestamp - mPrevisouTime) <  MeanTime ==> AccelListener.ProcessData()");
			    }
			    else
			    {
			    	 mLibrary.ProcessData( (long) (mPrevisouTime + MeanTime) , event.values[0], event.values[1], event.values[2]);
			    	 Log.d(TAG,"(event.timestamp - mPrevisouTime) >=  MeanTime ==> AccelListener.ProcessData()");
			    }
			}
			else if(mPowerManager.isScreenOn())
			{
				Log.d(TAG,"Display ON : mPwrState = " + mPwrState);
				if(mPwrState == 0)
				{
					mLibrary.ProcessData(event.timestamp, event.values[0], event.values[1], event.values[2]);
				}
				else if (mPwrState == 1)
				{
					Log.d(TAG,"restart Pedometer : mPwrState = 1");
					this.mPedometerController.stopPedometer();
					try 
					{
						Thread.sleep(50);
					} catch (InterruptedException e) 
					{
					   e.printStackTrace();
					}
					this.mPedometerController.startPedometer();
					mPwrState = 0;
				}
			}
			
			/*
			if(!mPowerManager.isScreenOn())
			{	
			    Log.d(TAG,"Display OFF");
			    mLibrary.ProcessData(event.timestamp, event.values[0], event.values[1], event.values[2]);
		    	Log.d(TAG,"AccelListener.ProcessData()");
			}
			else if(mPowerManager.isScreenOn())
			{
				Log.d(TAG,"Display ON");
				mLibrary.ProcessData(event.timestamp, event.values[0], event.values[1], event.values[2]);
			}
			*/
			mPrevisouTime = event.timestamp;
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {		
		}
		
	}
	

	
	/** listener class to receive notifications from the PedLibrary when pedometer events occur*/
	class PedListenerImp implements PedListener{
		//private int stepCountOffset =0;		// SL
		//private int currentStepCount =0;		// SL
	
		public void clearStepCount()
		{
			Log.d(TAG,"PedListener.clearStepCount()");
			// SL
			//stepCountOffset = currentStepCount;
			//for (PedometerListener listener : mListeners)
				//listener.onStepCount(currentStepCount-stepCountOffset);
			for (PedometerListener listener : mListeners)
				listener.onStepCount(0);
			stepCountOffset = 0;
		}

		@Override
		public void onStepDetected(int stepCount) {	
			
			Log.d(TAG,"PedListener.onStepDetected()");
			//currentStepCount++;	// SL
			currentStepCount = stepCount;
			for (PedometerListener listener : mListeners)
				{
					// SL
					//listener.onStepCount(currentStepCount-stepCountOffset);
					listener.onStepCount(currentStepCount+stepCountOffset);
				}						
		}

		@Override
		public void onStateChange(int state) {
			Log.d(TAG,"PedListener.onStateChange()");
			for (PedometerListener listener : mListeners)
				listener.onStateChanged(state);
		}};
	

	@Override
	public void onCreate() {
		super.onCreate();			
		mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG,"++ onDestroy()++");
		mPrevisouTime = 0L;
		super.onDestroy();
	}
}
