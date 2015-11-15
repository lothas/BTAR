package com.example.bravo;

import java.util.Timer;
import java.util.TimerTask;
import android.util.Log;
import android.widget.TextView;
import com.MobileAnarchy.Android.Widgets.Joystick.JoystickMovedListener;
import com.MobileAnarchy.Android.Widgets.Joystick.JoystickView;

public class BravoJS {
	private static final String TAG = BravoJS.class.getSimpleName();
	private static final String TAG_blue = null;
	private final boolean D = false;
	// Layout View
	JoystickView mJoystick;
	private TextView mTxtData;
	// polar coordinates
	public double mRadius = 0;
	public double mAngle = 0;
	private boolean mCenter = true;
	private int mDataFormat;
	// timer task
	private int mTimeoutCounter = 0;
	private int mMaxTimeoutCount = 20; // actual timeout = count * updateperiod
	public Timer mUpdateTimer;
	private long mUpdatePeriod;
	
	private MainActivity mMainActivity;

	public BravoJS(MainActivity activity) {
		mMainActivity = activity;
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
		public void OnMoved(int pan, int tilt) {
			mRadius = Math.sqrt((pan * pan) + (tilt * tilt));
			// mAngle = Math.atan2(pan, tilt);
			mAngle = Math.atan2(-pan, -tilt);
			mTxtData.setText(String.format("( r%.0f, %.0f\u00B0 )",
					Math.min(mRadius, 10), mAngle * 180 / Math.PI));
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

	public void UpdateMethod() {
		// if either of the joysticks is not on the center, or timeout occurred
		if (!mCenter || (mTimeoutCounter >= mMaxTimeoutCount && mMaxTimeoutCount > -1)) {
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
			/*
			 * if( mDataFormat==4 ) { // raw 4 bytes sendMessage( new String(new
			 * byte[] { radiusL, angleL, radiusR, angleR } ) ); }else if(
			 * mDataFormat==5 ) { // start with 0x55 sendMessage( new String(new
			 * byte[] { 0x55, radiusL, angleL, radiusR, angleR } ) ); }else if(
			 * mDataFormat==6 ) { // use STX & ETX sendMessage( new String(new
			 * byte[] { //TODO: modify this!! bitch! 0x02, radiusL, angleL,
			 * radiusR, angleR, 0x03 } ) ); }
			 */

			mTimeoutCounter = 0;
		} else {
			if (mMaxTimeoutCount > -1)
				mTimeoutCounter++;
		}
	}
}
