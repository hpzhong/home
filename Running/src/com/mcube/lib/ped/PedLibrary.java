package com.mcube.lib.ped;

import java.util.ArrayList;
import java.util.List;

/**
 * Java wrapper for the native pedometer library
 */
public class PedLibrary {
	/** A constant describing the low step sensitivity when calling */
	public static final int STEP_SENSITIVTY_LOW = 0;
	/** A constant describing the medium step sensitivity when calling */
	public static final int STEP_SENSITIVTY_MEDIUM = 1;
	/** A constant describing the high step sensitivity when calling */
	public static final int STEP_SENSITIVTY_HIGH = 2;
	/**
	 * A constant describing the stationary pedometer state. Passed into
	 * PedListener onStateChange
	 */
	public static final int PED_STATE_STATIONARY = 0;
	/**
	 * A constant describing the walking pedometer state. Passed
	 * into.PedListener onStateChange
	 */
	public static final int PED_STATE_WALKING = 1;
	/**
	 * A constant describing the running pedometer state. Passed into
	 * PedListener onStateChange
	 */
	public static final int PED_STATE_RUNNING = 2;
	/** storage for listeners */
	private List<PedListener> mListeners;

	/** constructor */
	public PedLibrary() {
		mListeners = new ArrayList<PedListener>();
	}

	/**
	 * Register a pedometer event listener.
	 * 
	 * @param listener
	 *            pedometer event listener to add.
	 */
	public void registerListener(PedListener listener) {
		synchronized (mListeners) {
			if (!mListeners.contains(listener))
				mListeners.add(listener);
		}
	}

	/**
	 * Unregister a pedometer event listener.
	 * 
	 * @param listener
	 *            previously registered pedometer event listener to remove
	 */
	public void unregisterListener(PedListener listener) {
		synchronized (mListeners) {
			if (mListeners.contains(listener))
				mListeners.remove(listener);
		}
	}

	/* Native function declarations */
	/**
	 * Opens the Pedometer library
	 * 
	 * @param stepSensitivity
	 *            step sensitivity. Must be one of: STEP_SENSITIVTY_LOW,
	 *            STEP_SENSITIVTY_MEDIUM, STEP_SENSITIVTY_HIGH
	 * @return true if library open succeeded, false otherwise.
	 */
	public native boolean Open(int stepSensitivity);

	/**
	 * Determines if the pedometer library has been opened.
	 * 
	 * @return true if successfully opened, false otherwise.
	 */
	public native boolean IsOpened();

	/**
	 * Close the Pedometer library
	 * 
	 * @return true if library close succeeded, false otherwise.
	 */
	public native boolean Close();

	/**
	 * Process one accelerometer data set through the pedometer library. This
	 * may trigger the onStepDetected() or onStateChange() functions.
	 * 
	 * @param timestampNs
	 *            timestamp of the sensor data in nanoseconds.
	 * @param accelXMps2
	 *            accelerometer data X axis in m/s^2
	 * @param accelYMps2
	 *            accelerometer data Y axis in m/s^2
	 * @param accelZMps2
	 *            accelerometer data Z axis in m/s^2
	 * @return true if data processing succeeded, false otherwise.
	 */
	public native boolean ProcessData(long timestampNs, float accelXMps2,
			float accelYMps2, float accelZMps2);

	/**
	 * Provides the pedometer library version information as an integer.
	 * 
	 * @return the version number.
	 */
	public native int PollVersion();

	/**
	 * Internal step detection handler
	 */
	private void onStepDetected(int stepCount) {
		synchronized (mListeners) {
			for (PedListener listener : mListeners) {
				listener.onStepDetected(stepCount);
			}
		}
	}

	/**
	 * Internal state change handler
	 */
	private void onStateChange(int newState) {
		synchronized (mListeners) {
			for (PedListener listener : mListeners) {
				listener.onStateChange(newState);
			}
		}
	}

	static {
		System.loadLibrary("mcube_lib_ped-jni");
	}
}