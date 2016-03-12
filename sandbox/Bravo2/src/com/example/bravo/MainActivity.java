package com.example.bravo;

import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.projectproto.btjoystick.BluetoothRfcommClient;
import org.opencv.samples.colorblobdetect.ColorBlobDetectionActivity;

import com.example.bravo.DeviceListActivity;

import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "alon";
	public BravoJS joystick;
	public BravoBT bluetooth;
	public MainActivityMisc mainActivityMisc;
	public ColorBlobDetectionActivity tracking;
	
	protected void onCreate (Bundle savedInstanceState)  {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		joystick = new BravoJS(this);
		bluetooth = new BravoBT(this);
		tracking = new ColorBlobDetectionActivity(this);
		mainActivityMisc = new MainActivityMisc(this);
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //prefs.registerOnSharedPreferenceChangeListener(this);

		joystick.main();
    }
	
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.main, menu);
    	return (super.onCreateOptionsMenu(menu));
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	mainActivityMisc.onOptionsItemSelected(item);
    	return super.onOptionsItemSelected(item);
    }
    
	public void onPause() {
		super.onPause();
		if (tracking.mOpenCvCameraView != null)
			tracking.mOpenCvCameraView.disableView();
	}
	
    public synchronized void onResume() {
    	super.onResume();
		mainActivityMisc.onResume();
		if (!OpenCVLoader.initDebug()) {
			Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, tracking.mLoaderCallback);
		} else {
			Log.d(TAG, "OpenCV library found inside package. Using it!");
			tracking.mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		}
    }
    
    public void onBackPressed() {
    	mainActivityMisc.onBackPressed();
    }
    
    public void onDestroy() {
    	joystick.mUpdateTimer.cancel();
    	if (bluetooth.mRfcommClient != null) bluetooth.mRfcommClient.stop();
		if (tracking.mOpenCvCameraView != null)
			tracking.mOpenCvCameraView.disableView();
        super.onDestroy();
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	mainActivityMisc.onActivityResult(requestCode, resultCode, data);
    }
}
