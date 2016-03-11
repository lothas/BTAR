package com.Proj.tracking;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

public class MainTrackingActivity extends Activity implements CvCameraViewListener, View.OnTouchListener {

	protected static final String TAG = "Main";
	
	private static final int NO_COLOR = -1;
	private static final int BLUE_COLOR = 1;
	private static final int GREEN_COLOR = 2;
	private static final int RED_COLOR = 3;

	private CameraBridgeViewBase mOpenCvCameraView;
	private frameProcessor frameProcessorRobo;
	private frameProcessor frameProcessorRobo1;
	private insertColor insertColorBlue;
	private insertColor insertColorRed;
	private insertColor insertColorGreen;

	private int mGameWidth;
	private int mGameHeight;
	
	private int resizeFactor = 1;
	
	private int insertColorStatus;

	private boolean colorsNotUpdated = true;
	private boolean showCount = false;
	

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");

				/* Now enable camera view to start receiving frames */
				mOpenCvCameraView.setOnTouchListener(MainTrackingActivity.this);
				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Log.d(TAG, "Creating and setting view");
		mOpenCvCameraView = (CameraBridgeViewBase) new JavaCameraView(this, -1);
		Log.d(TAG, "Creating and setting view 2");
		setContentView(mOpenCvCameraView);
		Log.d(TAG, "Creating and setting view 3");
		mOpenCvCameraView.setCvCameraViewListener(this);
		Log.d(TAG, "Creating and setting view 4");
		insertColorBlue = new insertColor(BLUE_COLOR);
		Log.d(TAG, "Creating and setting view 5");
		insertColorGreen = new insertColor(GREEN_COLOR);
		Log.d(TAG, "Creating and setting view 6");
		insertColorRed = new insertColor(RED_COLOR);
		Log.d(TAG, "Creating and setting view 7");
		frameProcessorRobo = new frameProcessor(BLUE_COLOR, GREEN_COLOR);
		Log.d(TAG, "Creating and setting view 8");
		frameProcessorRobo1 = new frameProcessor(GREEN_COLOR, RED_COLOR);
		Log.d(TAG, "exiting onCreate");
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {
		Log.d(TAG, "onCameraViewStarted");
		insertColorStatus = BLUE_COLOR;
		mGameWidth = width;
		mGameHeight = height;
		frameProcessorRobo.prepareGameSize(width, height);
		frameProcessorRobo1.prepareGameSize(width, height);
		insertColorBlue.prepareGameSize(width, height);
		insertColorGreen.prepareGameSize(width, height);
		insertColorRed.prepareGameSize(width, height);
		Log.d(TAG, "Exiting onCameraViewStarted");
	}

	public void onCameraViewStopped() {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_tracking, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!OpenCVLoader.initDebug()) {
			Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
		} else {
			Log.d(TAG, "OpenCV library found inside package. Using it!");
			mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		}
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		Log.d(TAG, "onTouch");
		int xpos, ypos;
		if ((showCount == false) && (colorsNotUpdated == true)) {
			xpos = (int) event.getX();
			ypos = (int) event.getY();
			switch (insertColorStatus) {
			case (BLUE_COLOR):
				Log.d(TAG, "onTouch: insertColorStatus = " + insertColorStatus);
				insertColorBlue.deliverTouchEvent(xpos, ypos);
				insertColorStatus = GREEN_COLOR;
				showCount = true;
				break;

			case (GREEN_COLOR):
				Log.d(TAG, "onTouch: insertColorStatus = " + insertColorStatus);
				insertColorGreen.deliverTouchEvent(xpos, ypos);
				insertColorStatus = RED_COLOR;
				showCount = true;
				break;

			case (RED_COLOR):
				Log.d(TAG, "onTouch: insertColorStatus = " + insertColorStatus);
				insertColorRed.deliverTouchEvent(xpos, ypos);
				insertColorStatus = NO_COLOR;
				showCount = true;
				break;
			}
		}else {
			showCount = false;
			if (insertColorStatus == NO_COLOR){
				colorsNotUpdated = false;
				Log.d(TAG, "onTouch: frameProcessorRobo.updateColors");
				frameProcessorRobo.updateColors(BLUE_COLOR, insertColorBlue.getHSVarr());
				frameProcessorRobo.updateColors(GREEN_COLOR, insertColorGreen.getHSVarr());
				frameProcessorRobo.updateColors(RED_COLOR, insertColorRed.getHSVarr());
				frameProcessorRobo1.updateColors(BLUE_COLOR, insertColorBlue.getHSVarr());
				frameProcessorRobo1.updateColors(GREEN_COLOR, insertColorGreen.getHSVarr());
				frameProcessorRobo1.updateColors(RED_COLOR, insertColorRed.getHSVarr());
			}
		}
		Log.d(TAG, "exiting onTouch with:" + false);
		return false;
	}

	@Override
	public Mat onCameraFrame(Mat inputFrame) {
		Long onCameraFrameStart = System.currentTimeMillis();
		Log.d(TAG, "onCameraFrame");
		if (colorsNotUpdated) {
			switch (insertColorStatus) {
			case (BLUE_COLOR):
				insertColorBlue.updateFrame(inputFrame);
				break;

			case (GREEN_COLOR):
				insertColorGreen.updateFrame(inputFrame);
				break;

			case (RED_COLOR):
				insertColorRed.updateFrame(inputFrame);
				break;
			}
			if (showCount) {
				switch (insertColorStatus) {
				case (GREEN_COLOR):
					Log.i(TAG, "onCameraFrame: insertColorBlue");
					return insertColorBlue.getMat();
				case (RED_COLOR):
					Log.i(TAG, "onCameraFrame: insertColorGreen");
					return insertColorGreen.getMat();
				case (NO_COLOR):
					Log.i(TAG, "onCameraFrame: insertColorRed");
					return insertColorRed.getMat();
				}
			}

		} else {
			Log.i(TAG, "onCameraFrame: frameProcessorRobo");
			Mat smallMat = new Mat(mGameWidth/resizeFactor,mGameHeight/resizeFactor,CvType.CV_8UC4);
			Imgproc.resize(inputFrame, smallMat, smallMat.size(), 0, 0, Imgproc.INTER_CUBIC  );
			Long startFrameProcessorRobo = System.currentTimeMillis();
			frameProcessorRobo.TrackingImg(inputFrame,resizeFactor);
			frameProcessorRobo1.TrackingImg(inputFrame,resizeFactor);
			frameProcessorRobo.drawObject(frameProcessorRobo.getFrontX(),frameProcessorRobo.getFrontY(),inputFrame, new Scalar(0,0,255));
			frameProcessorRobo1.drawObject(frameProcessorRobo1.getFrontX(),frameProcessorRobo1.getFrontY(),inputFrame, new Scalar(0,255,0));
			Long endFrameProcessorRobo = System.currentTimeMillis();
			Log.i(TAG, "delta frameProcessorRobo: " + (endFrameProcessorRobo - startFrameProcessorRobo));
		}
		Long onCameraFrameEnd = System.currentTimeMillis();
		Log.i(TAG, "delta onCameraFrame: " + (onCameraFrameEnd - onCameraFrameStart));
		return inputFrame;
	}
}
