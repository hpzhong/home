/**
* mCube Pedometer Listener interface.
* @file
*
* @brief This file defines the pedometer listener interface for receiving notifications from the Pedometer Library when
pedometer events occur.*/
package com.mcube.lib.ped;
/**
* Used for receiving notifications from the Pedometer Library when
* pedometer events occur.
*/
public interface PedListener
{
/**
* Called when pedometer detects a step.
* @param stepCount steps counted.
*/
public void onStepDetected(int stepCount);
/**
* Called when pedometer state change for details on possible state.
*
* @param state the new state.
*/
public void onStateChange(int state);
}