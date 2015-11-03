package com.zhuoyou.plugin.bluetooth.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.zhuoyou.plugin.bluetooth.data.Util;

public class BluetoothConnection {
	// Debugging
	public static final String LOG_TAG = "BluetoothConnection";

	// BT connection state
	public static final int STATE_NONE = 0; // doing nothing
	public static final int STATE_LISTEN = 1; // listening for incoming
												// connections
	public static final int STATE_CONNECTING = 2; // initiating an outgoing
													// connection
	public static final int STATE_CONNECTED = 3; // connected to a remote device
	public static final int STATE_CONNECT_LOST = 4; // BT connection lost

	// Name for the SDP record when creating server socket
	private static final String NAME = "BTNotification";

	// Unique UUID of this application for Bluetooth spp connection
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// Member fields
	private final BluetoothAdapter mAdapter; // local bluetooth device
	private final Handler mMessageHandler; // received from bt manager, use it
											// to communicate
	private ServerThread mServerThread; // server thread
	private ClientThread mClientThread; // client thread
	private WorkThread mWorkThread; // data transform thread
	private int mConnectState; // indicate the current connection status

	/**
	 * Constructor. Prepares a new BluetoothConnection session.
	 * 
	 * @param handler
	 *            A Handler to send messages back to the UI Activity
	 */
	public BluetoothConnection(Handler handler) {
		Log.i(LOG_TAG, "BluetoothConnection(), BluetoothConnection created!");

		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mConnectState = STATE_NONE;
		mMessageHandler = handler;
	}

	/**
	 * Set the current state of the bluetooth connection
	 * 
	 * @param state
	 *            An integer defining the current connection state
	 */
	private synchronized void setState(int state) {
		Log.i(LOG_TAG, "setState(), state=" + state);

		mConnectState = state;

		// Give the new state to the Handler to notify bt manager
		mMessageHandler.obtainMessage(BluetoothManager.MESSAGE_STATE_CHANGE,
				state, -1).sendToTarget();
	}

	/**
	 * Return the current connection state.
	 */
	public synchronized int getState() {
		Log.i(LOG_TAG, "getState(), mConnectState=" + mConnectState);

		return mConnectState;
	}

	/**
	 * Start the bluetooth connection. Specifically start ServerThread to begin
	 * a session in listening (server) mode.
	 */
	public synchronized void startAccept() {
		Log.i(LOG_TAG, "startAccept()");

		// Cancel any thread attempting to make a connection
		if (mClientThread != null) {
			mClientThread.cancel();
			mClientThread = null;
		}

		// Cancel any thread currently running a connection
		if (mWorkThread != null) {
			mWorkThread.cancel();
			mWorkThread = null;
		}

		// Start the thread to listen on a BluetoothServerSocket
		if (mServerThread == null) {
			mServerThread = new ServerThread();
			mServerThread.start();
		}
		setState(STATE_LISTEN);
	}

	public synchronized void disconnectRemoteDevice() {
		if ((mConnectState == STATE_CONNECTING) && (mClientThread != null)) {
			mClientThread.cancel();
			mClientThread = null;
		}

		if (mWorkThread != null) {
			mWorkThread.cancel();
			mWorkThread = null;
		}
	}

	/**
	 * Start the ClientThread to initiate a connection to a remote device.
	 * 
	 * @param remoteDevice
	 *            The BluetoothDevice to connect
	 */
	public synchronized void connectRemoteDevice(BluetoothDevice remoteDevice) {
		Log.i(LOG_TAG, "connectRemoteDevice(), device=" + remoteDevice);

		// Cancel any thread attempting to make a connection
		if ((mConnectState == STATE_CONNECTING) && (mClientThread != null)) {
			mClientThread.cancel();
			mClientThread = null;
		}

		// Cancel any thread currently running a connection
		if (mWorkThread != null) {
			mWorkThread.cancel();
			mWorkThread = null;
		}

		// Start the thread to connect with the given device
		mClientThread = new ClientThread(remoteDevice);
		mClientThread.start();
		setState(STATE_CONNECTING);
	}

	/**
	 * Start the WorkThread to begin managing a Bluetooth connection
	 * 
	 * @param socket
	 *            The BluetoothSocket on which the connection was made
	 * @param device
	 *            The BluetoothDevice that has been connected
	 */
	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device) {
		Log.i(LOG_TAG, "connected(), socket=" + socket + ", device=" + device);

		String deviceName = device.getName();
		if (deviceName == null || deviceName.equals(""))
			return;
		int count = Util.filterNames.length;
		for (int i = 0; i < count; i++) {
			if (Util.filterNames[i].equals(deviceName)) {
				// Cancel the thread that completed the connection
				if (mClientThread != null) {
					mClientThread.cancel();
					mClientThread = null;
				}

				// Cancel any thread currently running a connection
				if (mWorkThread != null) {
					mWorkThread.cancel();
					mWorkThread = null;
				}

				// Cancel the accept thread because we only want to connect to
				// one device
				if (mServerThread != null) {
					mServerThread.cancel();
					mServerThread = null;
				}

				// Start the thread to manage the connection and perform
				// transmissions
				mWorkThread = new WorkThread(socket);
				mWorkThread.start();

				// Send the name of the connected device back to bt manager
				BtProfileReceiver.stopAutoConnect();
				Message msg = mMessageHandler
						.obtainMessage(BluetoothManager.MESSAGE_DEVICE_NAME);
				Bundle bundle = new Bundle();
				bundle.putParcelable(BluetoothManager.DEVICE_NAME, device);
				msg.setData(bundle);
				mMessageHandler.sendMessage(msg);

				setState(STATE_CONNECTED);
				write("mtk".getBytes());
				break;
			}
		}
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		Log.i(LOG_TAG, "stop()");

		if (mClientThread != null) {
			mClientThread.cancel();
			mClientThread = null;
		}
		if (mWorkThread != null) {
			mWorkThread.cancel();
			mWorkThread = null;
		}
		if (mServerThread != null) {
			mServerThread.cancel();
			mServerThread = null;
		}
		setState(STATE_NONE);
	}

	/**
	 * Write to the WorkThread in an unsynchronized manner
	 * 
	 * @param out
	 *            The bytes to write
	 * @see com.mtk.btconnection.BluetoothConnection.WorkThread#write(byte[])
	 */
	public void write(byte[] out) {
		// Create temporary object
		WorkThread r;
		// Synchronize a copy of the WorkThread
		synchronized (this) {
			if (mConnectState != STATE_CONNECTED) {
				return;
			}
			r = mWorkThread;
		}
		// Perform the write unsynchronized
		r.write(out);
	}

	/**
	 * Indicate that the connection attempt failed and notify the bt manager.
	 */
	private void connectionFailed() {
		Log.i(LOG_TAG, "connectionFailed()");

		setState(STATE_LISTEN);

		// Send a failure message back to the Activity
		Message msg = mMessageHandler
				.obtainMessage(BluetoothManager.MESSAGE_CONNECT_FAIL);
		Bundle bundle = new Bundle();
		bundle.putString(BluetoothManager.TOAST, "Unable to connect device");
		msg.setData(bundle);
		mMessageHandler.sendMessage(msg);

		BluetoothConnection.this.startAccept();
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost() {
		Log.i(LOG_TAG, "connectionLost()");

		setState(STATE_CONNECT_LOST);

		// restart ServerThread when local bluetooth is enabled.

		BluetoothConnection.this.startAccept();

		Log.i(LOG_TAG, "connectionLost(), ServerThread restart!");

	}

	public boolean isEnable() {
		return mAdapter.isEnabled();
	}

	/**
	 * This thread runs while listening for incoming connections. It behaves
	 * like a server-side client. It runs until a connection is accepted (or
	 * until cancelled).
	 */
	private class ServerThread extends Thread {
		// The local server socket
		private final BluetoothServerSocket mServerSocket;

		public ServerThread() {
			BluetoothServerSocket tmp = null;

			// Create a new listening server socket
			try {
				tmp = mAdapter
						.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
			} catch (IOException e) {
				Log.w("ServerThread listen() failed", e);
			}
			mServerSocket = tmp;
		}

		@Override
		public void run() {
			Log.i(LOG_TAG, "ServerThread BEGIN" + this);

			setName("ServerThread");
			BluetoothSocket socket = null;

			// Listen to the server socket if we're not connected
			while (mConnectState != STATE_CONNECTED) {
				try {
					// This is a blocking call and will only return on a
					// successful connection or an exception
					socket = mServerSocket.accept();
				} catch (IOException e) {
					Log.w("ServerThread accept() failed", e);
					break;
				} catch (Exception e) {
					Log.w("mServerSocket is exception", e);
					break;
				}

				// If a connection was accepted
				if (socket != null) {
					synchronized (BluetoothConnection.this) {
						switch (mConnectState) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							// Situation normal. Start the connected thread.
							connected(socket, socket.getRemoteDevice());
							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							// Either not ready or already connected. Terminate
							// new socket.
							try {
								socket.close();
							} catch (IOException e) {
								Log.w("ServerThread Could not close unwanted socket",
										e);
							}
							break;
						default:
							break;
						}
					}
				}
			}

			Log.i(LOG_TAG, "ServerThread END");
		}

		public void cancel() {
			Log.i(LOG_TAG, "cancel(),  ServerThread is canceled");

			try {
				if (mServerSocket != null)
					mServerSocket.close();
			} catch (IOException e) {
				Log.w("close server socket failed", e);
			} catch (Exception e) {
				Log.w("mServerSocket is exception", e);
			}
		}
	}

	/**
	 * This thread runs while attempting to make an outgoing connection with a
	 * device. It runs straight through; the connection either succeeds or
	 * fails.
	 */
	private class ClientThread extends Thread {
		private final BluetoothSocket mClientSocket;
		private final BluetoothDevice mRemoteDevice;

		public ClientThread(BluetoothDevice remoteDevice) {
			mRemoteDevice = remoteDevice;
			BluetoothSocket tmp = null;

			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try { 
				if (remoteDevice != null) {
					tmp = remoteDevice.createRfcommSocketToServiceRecord(MY_UUID);
				}
			} catch (IOException e) {
				Log.w("create client socket failed", e);
			}
			mClientSocket = tmp;
		}

		@Override
		public void run() {
			Log.i(LOG_TAG, "ClientThread BEGIN");
			setName("ClientThread");

			// Always cancel discovery because it will slow down a connection
			mAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				mClientSocket.connect();
			} catch (Exception e) {
				String errorString = e.toString();
				if (errorString == null) {
					errorString = "connect error";
				}
				Log.w(LOG_TAG, errorString);
				connectionFailed();
				// Close the socket
				try {
					mClientSocket.close();
				} catch (Exception e2) {
					Log.w("unable to close socket during connection failure",
							e2);
				}
				return;
			}

			// Reset the ClientThread because we're done
			synchronized (BluetoothConnection.this) {
				mClientThread = null;
			}

			// Start the connected thread
			connected(mClientSocket, mRemoteDevice);
		}

		public void cancel() {
			Log.i(LOG_TAG, "cancel(), ClientThread is canceled");

			try {
				if (mClientSocket != null)
					mClientSocket.close();
			} catch (IOException e) {
				Log.w("close connect socket failed", e);
			} catch (Exception e) {
				Log.e(LOG_TAG, e.toString());
			}
		}
	}

	/**
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions.
	 */
	private class WorkThread extends Thread {
		private final BluetoothSocket mSocket;
		private final InputStream mInStream;
		private final OutputStream mOutStream;

		public WorkThread(BluetoothSocket socket) {
			Log.i(LOG_TAG, "WorkThread(), create WorkThread");
			mSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.w("temp sockets not created", e);
			}

			mInStream = tmpIn;
			mOutStream = tmpOut;
		}

		@Override
		public void run() {
			Log.i(LOG_TAG, "WorkThread BEGIN");

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					byte[] buffer = new byte[1024 * 5];
					int bytes = 0;
					bytes = mInStream.read(buffer);
					// Send the obtained bytes to the manager
					Log.i(LOG_TAG,
							"read data frome smart client, the lenth is "
									+ bytes);
					if (bytes > 0) {
						mMessageHandler.obtainMessage(
								BluetoothManager.MESSAGE_READ, bytes, -1,
								buffer).sendToTarget();
					}
				} catch (IOException e) {
					Log.w("disconnected", e);
					connectionLost();
					break;
				}
			}
		}

		/**
		 * Write to the connected OutStream.
		 * 
		 * @param buffer
		 *            The bytes to write
		 */
		public void write(byte[] buffer) {
			try {
				mOutStream.write(buffer);
				Log.i(LOG_TAG, "Write to Feature Phone SPP" + buffer.length);
				// Share the sent message back to the bt manager
				mMessageHandler.obtainMessage(BluetoothManager.MESSAGE_WRITE,
						-1, -1, buffer).sendToTarget();
			} catch (Exception e) {
				e.printStackTrace();
				Log.w("Exception during write", e);
				connectionLost();
			}
		}

		public void cancel() {
			Log.i(LOG_TAG, "cancel(),  WorkThread is canceled");

			try {
				mSocket.close();
			} catch (IOException e) {
				Log.w("close connected socket failed", e);
			}
		}
	}
}
