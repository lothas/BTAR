package com.example.bravo;

import java.util.Timer;
import java.util.TimerTask;
import android.util.Log;
import android.widget.TextView;
import com.MobileAnarchy.Android.Widgets.Joystick.JoystickMovedListener;
import com.MobileAnarchy.Android.Widgets.Joystick.JoystickView;

public class BravoJS {
	private static final String TAG = BravoJS.class.getSimpleName();
	private static final String TAG_blue = "alon";
	private final boolean D = false;
	
	//--------------------------------------------
	private static final int slowDownFactor =5; //slowDownFactor is how much we want the robot to slow down (1 is no slow down)
	//--------------------------------------------
	
	// Layout View
	JoystickView mJoystick;
	private TextView mTxtData;
	// polar coordinates
	public double mRadius = 0;
	public double mAngle = 0;
	private boolean mCenter = true;
	public int mDataFormat;
	// x y coordinates
	private double x_axeL;
	private double y_axeL;
	// timer task
	private int mTimeoutCounter = 0;
	public int mMaxTimeoutCount = 20; // actual timeout = count * updateperiod
	public Timer mUpdateTimer;
	public long mUpdatePeriod;
	
	private MainActivity mMainActivity;

	public BravoJS(MainActivity activity) {
		mMainActivity = activity;
		x_axeL = 0;
	 	y_axeL = 0;
		mJoystick = (JoystickView) mMainActivity.findViewById(R.id.joystickView);
		mTxtData = (TextView) mMainActivity.findViewById(R.id.TextData);
		mJoystick.setOnJostickMovedListener(listener);
	}

	public void main() {
		mUpdateTimer = new Timer();
		mUpdateTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				UpdateMethod();
			}
		}, 2000, 200);
	}

	public JoystickMovedListener listener = new JoystickMovedListener() {
		public void OnMoved(double pan, double tilt) {
			// x y coordinates
		 	x_axeL = 10*pan;  
		 	y_axeL = -10*tilt;
			mRadius = Math.sqrt((pan * pan) + (tilt * tilt));
			// mAngle = Math.atan2(pan, tilt);
			mAngle = Math.atan2(-pan, -tilt);
			mTxtData.setText(String.format("( r%.0f, %.0f\u00B0 x = %.0f, y = %.0f)",Math.min(mRadius, 10), mAngle * 180 / Math.PI,x_axeL,y_axeL));
			mCenter = false;
		}

		public void OnReleased() {
			//
		}

		public void OnReturnedToCenter() {
			mRadius = mAngle = 0;
			UpdateMethod();
			mCenter = true;
		}
	};
	
    private byte[] Updating_x_y_for_sending(double val) {
    	byte[] message = new byte[3]; 
    	if (val >= 0){
    		val = ( Math.min( val, 100.0 ) );
    		message[0] = 50;
    	}else{
    		message[0] = 49;
    		val = 100 + ( Math.max( val, -100.0 ) );
    	}
    	message[1] =(byte)((int)(val/10)+48);
    	message[2] = (byte)((val % 10)+48);
    	
    	return message;
    }
    
    public void UpdateMethod() {
    	
    	// if either of the joysticks is not on the center, or timeout occurred
    	if(!mCenter || (mTimeoutCounter>=mMaxTimeoutCount && mMaxTimeoutCount>-1) ) {
    		// limit to {0..100}
    		
    		byte[] x_message = new byte[3];
	    	byte[] y_message = new byte[3];
    		
    		x_message = Updating_x_y_for_sending(x_axeL/slowDownFactor); // slowDownFactor is how much we want the robot to slow down
	    	y_message = Updating_x_y_for_sending(y_axeL/slowDownFactor);
    		
	    	// limit to {0..10}
	    	byte radius = (byte) (Math.min(mRadius, 10.0));
	    	// scale to {0..35}
	    	byte angle = (byte) (mAngle * 18.0 / Math.PI + 36.0 + 0.5);
	    	if (angle >= 36){
	    		angle = (byte) (angle - 36);
	    	}
	    	Log.v(TAG, String.format("%d, %d", radius, angle));
	    	if (D) {
	    		Log.d(TAG, String.format("%d, %d", radius, angle));
	    	}
	    	
	    	/*if( mDataFormat==4 ) {
	    		// raw 4 bytes
	    		//mMainActivity.BravoBT.sendMessage( new String(new byte[] { radiusL, angleL, radiusR, angleR } ) );
	    	}else if( mDataFormat==5 ) {
	    		// start with 0x55
	    		//mMainActivity.BravoBT.sendMessage( new String(new byte[] {0x55, radiusL, angleL, radiusR, angleR } ) );
	    	}else if( mDataFormat==6 ) {*/
	    		// use STX & ETX
	    		mMainActivity.bluetooth.sendMessage( new String(new byte[] {
		    			0x02, x_message[0], x_message[1], x_message[2], y_message[0], y_message[1], y_message[2], 0x03 } ) );
	    	//}
	    	
	    	mTimeoutCounter = 0;
    	}
    	else{
    		if( mMaxTimeoutCount>-1 )
    			mTimeoutCounter++;
    	}	
    }
	
}
