package org.opencv.samples.colorblobdetect;

import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import com.example.bravo.MainActivity;
import com.example.bravo.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;

public class MarkerDetectionActivity implements OnTouchListener, CvCameraViewListener2 {
    //-------------------------debug TAGs--------------------------//
	private static final String  TAG              = "OCVSample::Activity";
	private static final String  Draw3Dobject     = "draw3Dobject";
    //-------------------------Static----------------------------//
    private static final int     NO_COLOR = -1;
	private static final int     BLUE_COLOR = 1;
	private static final int     GREEN_COLOR = 2;
	private static final int     RED_COLOR = 3;
	//-------------------------Fields--------------------------//
	private Mat                  mRgba;
    private MarkerDetector       mDetector_BlueRed;
    private MarkerDetector       mDetector_GreenRed;
    private MarkerDetector       mDetector_GreenBlue;
    public CameraBridgeViewBase  mOpenCvCameraView;
	private insertColor          insertColorBlue;
	private insertColor          insertColorRed;
	private insertColor          insertColorGreen;
	private int                  colorToCalibrate, colorToView;
	private boolean              updatedLastColor = false;
	private boolean              colorsAreCalibrated = false;
	private boolean              viewCalibration = false;	
	//-------------------------Activity------------------------//
	MainActivity                 mMainActivity;	
	
	//-------------------------Methods-------------------------//
	
	public BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(mMainActivity) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(MarkerDetectionActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MarkerDetectionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    public MarkerDetectionActivity(MainActivity activity) {
		mMainActivity = activity;
    	Log.i(TAG, "called colorblob"); 	
		mOpenCvCameraView = (CameraBridgeViewBase) new JavaCameraView(mMainActivity, -1);
		FrameLayout preview = (FrameLayout) mMainActivity.findViewById(R.id.camera_preview);
		preview.addView(mOpenCvCameraView);
		mOpenCvCameraView.setCvCameraViewListener(this);
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector_BlueRed = new MarkerDetector();
        mDetector_GreenRed = new MarkerDetector();
        mDetector_GreenBlue = new MarkerDetector();
   
        colorToCalibrate = BLUE_COLOR; // first color to Calibrate 
        viewCalibration = false; 
        
        mDetector_BlueRed.prepareGame(width, height, BLUE_COLOR, RED_COLOR);
        mDetector_GreenRed.prepareGame(width, height, GREEN_COLOR, RED_COLOR);
        mDetector_GreenBlue.prepareGame(width, height, GREEN_COLOR, BLUE_COLOR);
        
        insertColorBlue = new insertColor(BLUE_COLOR);
		insertColorGreen = new insertColor(GREEN_COLOR);
		insertColorRed = new insertColor(RED_COLOR);
		
		insertColorBlue.prepareGameSize(width, height);
		insertColorGreen.prepareGameSize(width, height);
		insertColorRed.prepareGameSize(width, height);
		
		mMainActivity.mGLView.mRenderer.setScreenWidthHeight(width, height);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public synchronized boolean onTouch(View v, MotionEvent event) {
    	if ((colorsAreCalibrated == false) && (viewCalibration == false)){
    		int xpos = (int) event.getX();
			int ypos = (int) event.getY();
    		switch (colorToCalibrate){
			case (BLUE_COLOR):
				Log.d(TAG, "onTouch: colorToCalibrate = " + colorToCalibrate);
				insertColorBlue.deliverTouchEvent(xpos, ypos);
				colorToCalibrate = GREEN_COLOR;
				colorToView = BLUE_COLOR;
				viewCalibration = true;
				break;

			case (GREEN_COLOR):
				Log.d(TAG, "onTouch: colorToCalibrate = " + colorToCalibrate);
				insertColorGreen.deliverTouchEvent(xpos, ypos);
				colorToCalibrate = RED_COLOR;
				colorToView = GREEN_COLOR;
				viewCalibration = true;
				break;

			case (RED_COLOR):
				Log.d(TAG, "onTouch: colorToCalibrate = " + colorToCalibrate);
				insertColorRed.deliverTouchEvent(xpos, ypos);
				colorToCalibrate = NO_COLOR;
				colorToView = RED_COLOR;
				viewCalibration = true;
				updatedLastColor = true;
				break;
			}
    	}else {
    		viewCalibration =false;
    		if(updatedLastColor == true){
    			mDetector_BlueRed.updateColors(BLUE_COLOR, insertColorBlue.getHSVarr());
    			mDetector_BlueRed.updateColors(GREEN_COLOR, insertColorGreen.getHSVarr());
    			mDetector_BlueRed.updateColors(RED_COLOR, insertColorRed.getHSVarr());
    			
    			mDetector_GreenRed.updateColors(BLUE_COLOR, insertColorBlue.getHSVarr());
    			mDetector_GreenRed.updateColors(GREEN_COLOR, insertColorGreen.getHSVarr());
    			mDetector_GreenRed.updateColors(RED_COLOR, insertColorRed.getHSVarr());
    			
    			mDetector_GreenBlue.updateColors(BLUE_COLOR, insertColorBlue.getHSVarr());
    			mDetector_GreenBlue.updateColors(GREEN_COLOR, insertColorGreen.getHSVarr());
    			mDetector_GreenBlue.updateColors(RED_COLOR, insertColorRed.getHSVarr());
    			colorsAreCalibrated = true;
    		}
    	}
        return false; // don't need subsequent touch events
    }

    public synchronized Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        
		if (colorsAreCalibrated == false){
			switch (colorToCalibrate) {
			case (BLUE_COLOR):
				insertColorBlue.updateFrame(mRgba);
				break;
			case (GREEN_COLOR):
				insertColorGreen.updateFrame(mRgba);
				break;
			case (RED_COLOR):
				insertColorRed.updateFrame(mRgba);
				break;
			}			
			if (viewCalibration == true) {
				switch (colorToView) {
				case (BLUE_COLOR):
					Log.i(TAG, "onCameraFrame: insertColorBlue");
					return insertColorBlue.getMat();
				case (GREEN_COLOR):
					Log.i(TAG, "onCameraFrame: insertColorGreen");
					return insertColorGreen.getMat();
				case (RED_COLOR):
					Log.i(TAG, "onCameraFrame: insertColorRed");
					return insertColorRed.getMat();
				}
			}			
		} 
			
		if (colorsAreCalibrated == true) {
			Mat mPyrDownMat = new Mat();
			Imgproc.pyrDown(mRgba, mPyrDownMat);
			Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);
			mDetector_BlueRed.trackFront(mPyrDownMat);
			mDetector_GreenRed.trackFront(mPyrDownMat);
			mDetector_GreenBlue.trackFront(mPyrDownMat);

			//mRgba = mDetector_BlueRed.drawObject(mDetector_BlueRed.getMiddleX(), mDetector_BlueRed.getMiddleY(), mRgba, new Scalar(255, 0, 0));
			mRgba = mDetector_GreenRed.drawObject(mDetector_GreenRed.getMiddleX(), mDetector_GreenRed.getMiddleY(), mRgba, new Scalar(255, 0, 0));
			mRgba = mDetector_GreenBlue.drawObject(mDetector_GreenBlue.getMiddleX(), mDetector_GreenBlue.getMiddleY(), mRgba, new Scalar(255, 0, 0));
			
			draw3Dobject(mDetector_BlueRed);
		}

		return mRgba;
	}

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }
    
    private void draw3Dobject(MarkerDetector mDetector){
    	float x = 2*((float)mDetector.getMiddleX()) / mDetector.getFrameWidth() - 1;
    	float y = 1 - 2*((float)mDetector.getMiddleY()) / mDetector.getFrameHeight();
    	Log.i(Draw3Dobject, "draw3Dobject: x = " + x + " y = " + y);
    	mMainActivity.mGLView.mRenderer.setCordinates(x, y);
    	mMainActivity.mGLView.mRenderer.setTrackObjects(mDetector);
    	mMainActivity.mGLView.requestRender();
    }
}
