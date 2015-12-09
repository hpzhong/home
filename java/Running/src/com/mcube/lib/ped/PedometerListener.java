/**
* mCube pedometer service listener interface. 
* @file
*
* @brief   This file defines the pedometer listener interface for receiving notifications from the pedometer service when pedometer events occur.
*
*/
package com.mcube.lib.ped;

public interface PedometerListener {
	 /* **************************************************************************
	    * Pedometer methods
	    **************************************************************************/
	   /** callback function to notify listener of step detection */
	   public void onStepCount(int stepCount);

	   /** callback function to notify listener of pedometer state changes */
	   public void onStateChanged(int newState);

}
