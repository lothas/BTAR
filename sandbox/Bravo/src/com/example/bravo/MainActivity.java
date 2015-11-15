package com.example.bravo;

import org.projectproto.btjoystick.BluetoothRfcommClient;

import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {
	private static final String TAG = null;
	public BravoJS joystick;
	public BravoBT bluetooth;
	@Override
	protected void onCreate (Bundle savedInstanceState)  {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		PreferenceManager.setDefaultValues(this, R.layout.preferences, false);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        Long mUpdatePeriod = Long.parseLong(prefs.getString( "updates_interval", "200" ));
		joystick = new BravoJS(this);
		bluetooth = new BravoBT(this);
		joystick.main();
    }
	
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.main, menu);
    	return (super.onCreateOptionsMenu(menu));
    }
    
    public static class SettingsFragment extends PreferenceFragment {
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.layout.preferences);
            Log.d(TAG,"1");
        }
    }
    public boolean onOptionsItemSelected(MenuItem item) {
    	int id = item.getItemId();
    	Log.d(TAG,"2");
    	if (id == R.id.options){
    		Log.d(TAG,"3");
    		getFragmentManager().beginTransaction()
            .replace(android.R.id.content, new SettingsFragment())
            .commit();
    	}
    	/*if ( item == mItemConnect ) {
    		Intent serverIntent = new Intent(this, DeviceListActivity.class);
        	startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    	} else if ( item == mItemOptions ) {
    		startActivity( new Intent(this, OptionsActivity.class) );
    	} else if ( item == mItemAbout ) {
    		//TODO: fill as we please
    	}*/
    	return super.onOptionsItemSelected(item);
    }
	
    public synchronized void onResume() {
    	super.onResume();
    	if (bluetooth.mRfcommClient != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (bluetooth.mRfcommClient.getState() == BluetoothRfcommClient.STATE_NONE) {
              // Start the Bluetooth  RFCOMM services
            	bluetooth.mRfcommClient.start();
            }
        }    	
    }
    
    public void onBackPressed() {
    	new AlertDialog.Builder(this)
    	.setTitle("Bluetooth Joystick")
    	.setMessage("Close this controller?")
    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();				
			}
		})
		.setNegativeButton("No", null)
		.show();
    }
    
    public void onDestroy() {
    	joystick.mUpdateTimer.cancel();
    	if (bluetooth.mRfcommClient != null) bluetooth.mRfcommClient.stop();
        super.onDestroy();
    }
}
