package com.example.bravo;

import org.projectproto.btjoystick.BluetoothRfcommClient;
import com.example.bravo.DeviceListActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivityMisc {
	// Intent request codes
    public static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 2;
	private static final String TAG = "alon";
	public MainActivity mMainActivity;

	public MainActivityMisc (MainActivity activity){
		mMainActivity = activity;
	}
	public void onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.connect) {
			Intent serverIntent = new Intent(mMainActivity,DeviceListActivity.class);
			mMainActivity.startActivityForResult(serverIntent,REQUEST_CONNECT_DEVICE);
			Log.v(TAG, "13");
		}
		/*if (id == R.id.options) {
			mMainActivity.startActivity(new Intent(mMainActivity,OptionsActivity.class));
		}*/
		if (id == R.id.about) {
			// TODO: fill as we please
		}
	}

	public void onResume() {
    	if (mMainActivity.bluetooth.mRfcommClient != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mMainActivity.bluetooth.mRfcommClient.getState() == BluetoothRfcommClient.STATE_NONE) {
              // Start the Bluetooth  RFCOMM services
            	mMainActivity.bluetooth.mRfcommClient.start();
            }
        }  
	}

	public void onBackPressed() {
		new AlertDialog.Builder(mMainActivity)
				.setTitle("Bluetooth Joystick")
				.setMessage("Close this controller?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								mMainActivity.finish();
							}
						}).setNegativeButton("No", null).show();
	}

/*	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if (key.equals("updates_interval")) {
			// reschedule task
			mMainActivity.joystick.mUpdateTimer.cancel();
			mMainActivity.joystick.mUpdateTimer.purge();
			mMainActivity.joystick.mUpdatePeriod = Long.parseLong(prefs.getString("updates_interval",
					"200"));
			mMainActivity.joystick.mUpdateTimer = new Timer();
			mMainActivity.joystick.mUpdateTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					mMainActivity.joystick.UpdateMethod();
				}
			}, mMainActivity.joystick.mUpdatePeriod, mMainActivity.joystick.mUpdatePeriod);
		} else if (key.equals("maxtimeout_count")) {
			mMainActivity.joystick.mMaxTimeoutCount = Integer.parseInt(prefs.getString(
					"maxtimeout_count", "20"));
		} else if (key.equals("data_format")) {
			mMainActivity.joystick.mDataFormat = Integer.parseInt(prefs.getString("data_format", "5"));
		}
	}*/

   public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode){
    	case REQUEST_CONNECT_DEVICE:
    		// When DeviceListActivity returns with a device to connect
    		if (resultCode == Activity.RESULT_OK) {
    			// Get the device MAC address
    			String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
    			// Get the BLuetoothDevice object
                BluetoothDevice device = mMainActivity.bluetooth.mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mMainActivity.bluetooth.mRfcommClient.connect(device);
    		}
    		break;
    	case REQUEST_ENABLE_BT:
    		// When the request to enable Bluetooth returns
    		if (resultCode != Activity.RESULT_OK) {
            	// User did not enable Bluetooth or an error occurred
    			Toast.makeText(mMainActivity, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                mMainActivity.finish();
            }
    		break;
    	}
    }
}
