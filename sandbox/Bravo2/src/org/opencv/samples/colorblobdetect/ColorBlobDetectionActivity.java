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

public class ColorBlobDetectionActivity implements OnTouchListener, CvCameraViewListener2 {
    private static final String  TAG              = "OCVSample::Activity";

    private boolean              mIsColorSelected = false;
    private Mat                  mRgba;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector_BlueRed;
    private ColorBlobDetector    mDetector_GreenRed;
    private ColorBlobDetector    mDetector_GreenBlue;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;

    public CameraBridgeViewBase mOpenCvCameraView;
    
    
    //----------------------------------
    private static final int NO_COLOR = -1;
	private static final int BLUE_COLOR = 1;
	private static final int GREEN_COLOR = 2;
	private static final int RED_COLOR = 3;

	private insertColor insertColorBlue;
	private insertColor insertColorRed;
	private insertColor insertColorGreen;
	
	
	private int colorToCalibrate, colorToView;
	private boolean             updatedLastColor = false;
	private boolean             colorsAreCalibrated = false;
	private boolean             viewCalibration = false;
	//-----------------------------------
	MainActivity mMainActivity;
	
	public BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(mMainActivity) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ColorBlobDetectionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    public ColorBlobDetectionActivity(MainActivity activity) {
		mMainActivity = activity;
    	Log.i(TAG, "called colorblob");
    	
		mOpenCvCameraView = (CameraBridgeViewBase) new JavaCameraView(mMainActivity, -1);
		FrameLayout preview = (FrameLayout) mMainActivity.findViewById(R.id.camera_preview);
		preview.addView(mOpenCvCameraView);
		//mMainActivity.setContentView(mOpenCvCameraView);
		mOpenCvCameraView.setCvCameraViewListener(this);

    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector_BlueRed = new ColorBlobDetector();
        mDetector_GreenRed = new ColorBlobDetector();
        mDetector_GreenBlue = new ColorBlobDetector();
        
        
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
        
        // added
        colorToCalibrate = BLUE_COLOR;
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
    	
    	
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;

        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

        mDetector_BlueRed.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector_BlueRed.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

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
			
        
        if (mIsColorSelected) {
        	if (colorsAreCalibrated == true){
        		Mat mPyrDownMat = new Mat();
        		Imgproc.pyrDown(mRgba, mPyrDownMat);
                Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);
                mDetector_BlueRed.trackFront(mPyrDownMat);
        		mDetector_GreenRed.trackFront(mPyrDownMat);
        		mDetector_GreenBlue.trackFront(mPyrDownMat);
        		//mDetector_BlueRed.trackFront(mRgba);
        		//mDetector_GreenRed.trackFront(mRgba);
        		//mDetector_GreenBlue.trackFront(mRgba);
        		//mDetector.trackBack(mRgba);
        	} else {
        		mDetector_BlueRed.process(mRgba);
        	}
            List<MatOfPoint> contours = mDetector_BlueRed.getContours();
            
            
            
            Log.e(TAG, "Contours count: " + contours.size());
            Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

            //mRgba = mDetector.drawObject(mDetector.getFrontX(), mDetector.getFrontY(), mRgba, new Scalar(0,255,255));
            //mRgba = mDetector.drawObject(mDetector.getBackX(), mDetector.getBackY(), mRgba, new Scalar(255,255,0));
            mRgba = mDetector_BlueRed.drawObject(mDetector_BlueRed.getMiddleX(), mDetector_BlueRed.getMiddleY(), mRgba, new Scalar(255,0,0));
            mRgba = mDetector_GreenRed.drawObject(mDetector_GreenRed.getMiddleX(), mDetector_GreenRed.getMiddleY(), mRgba, new Scalar(255,0,0));
            mRgba = mDetector_GreenBlue.drawObject(mDetector_GreenBlue.getMiddleX(), mDetector_GreenBlue.getMiddleY(), mRgba, new Scalar(255,0,0));
            
            
            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
            colorLabel.setTo(mBlobColorRgba);

            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
            mSpectrum.copyTo(spectrumLabel);
        }

        return mRgba;
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }
}
