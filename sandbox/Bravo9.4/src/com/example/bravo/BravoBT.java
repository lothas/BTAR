package com.example.bravo;

import org.projectproto.btjoystick.BluetoothRfcommClient;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class BravoBT {
	// Message types sent from the BluetoothRfcommClient Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothRfcommClient Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
	// Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
	private static final String TAG = "alon";
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    public BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the RFCOMM services
	private TextView mTxtStatus;
    public BluetoothRfcommClient mRfcommClient = null;
    private MainActivity mMainActivity;
    
	public BravoBT (MainActivity activity){
		mMainActivity = activity;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mTxtStatus = (TextView) mMainActivity.findViewById(R.id.TextStatus);
        if (mBluetoothAdapter == null) {
            Toast.makeText(mMainActivity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            mMainActivity.finish();
            return;
        }
    	if (!mBluetoothAdapter.isEnabled()){
    		Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    		mMainActivity.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    	}
    	mRfcommClient = new BluetoothRfcommClient(mMainActivity, mHandler);
	}
	
	public void sendMessage(String message){
    	// Check that we're actually connected before trying anything
    	if (mRfcommClient.getState() != BluetoothRfcommClient.STATE_CONNECTED) {
    		// Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
			Log.v(TAG, "NOT CONNECTED");
    		return;
    	}
    	// Check that there's actually something to send
    	if (message.length() > 0) {
    		// Get the message bytes and tell the BluetoothRfcommClient to write
			Log.v(TAG, message);
    		byte[] send = message.getBytes();
    		mRfcommClient.write(send);
    	}
    }
	
	private final Handler mHandler = new Handler() {
    	@Override
        public void handleMessage(Message msg) {
    		switch (msg.what) {
    		case MESSAGE_STATE_CHANGE:
    			switch (msg.arg1) {
    			case BluetoothRfcommClient.STATE_CONNECTED:
    				mTxtStatus.setText(R.string.title_connected_to);
    				mTxtStatus.append(" " + mConnectedDeviceName);
    				break;
    			case BluetoothRfcommClient.STATE_CONNECTING:
    				mTxtStatus.setText(R.string.title_connecting);
    				break;
    			case BluetoothRfcommClient.STATE_NONE:
    				mTxtStatus.setText(R.string.title_not_connected);
    				break;
    			}
    			break;
    		case MESSAGE_READ:
    			// byte[] readBuf = (byte[]) msg.obj;
    			// int data_length = msg.arg1;
    			break;
    		case MESSAGE_DEVICE_NAME:
    			// save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(mMainActivity.getApplicationContext(), "Connected to "
                        + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
    			break;
    		case MESSAGE_TOAST:
    			Toast.makeText(mMainActivity.getApplicationContext(), msg.getData().getString(TOAST),
                        Toast.LENGTH_SHORT).show();
    			break;
    		}
    	}
    };
}
