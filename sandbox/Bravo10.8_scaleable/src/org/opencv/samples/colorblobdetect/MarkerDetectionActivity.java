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
import bravo.game.manager.GameManager;

public class MarkerDetectionActivity implements OnTouchListener, CvCameraViewListener2 {
    //-------------------------debug TAGs--------------------------//
	private static final String  TAG              = "OCVSample::Activity";
	private static final String  Draw3Dobject     = "draw3Dobject";
    //-------------------------Static----------------------------//
    private static final int     NO_COLOR = -1;
	private static final int     BLUE_COLOR = 1;
	private static final int     GREEN_COLOR = 2;
	private static final int     RED_COLOR = 3;
	
	private static final int 	 NUM_OF_COLORS = 3;
	//-------------------------Fields--------------------------//
	private Mat                  mRgba;
	public MarkerDetector       mDetectorArry[];
    public MarkerDetector		myRobot;
    private int     NUM_OF_TOWERS;
    
    public CameraBridgeViewBase  mOpenCvCameraView;
	private insertColor          insertColorBlue;
	private insertColor          insertColorRed;
	private insertColor          insertColorGreen;
	private int                  colorToCalibrate, colorToView;
	private boolean              updatedLastColor = false;
	private boolean              colorsAreCalibrated = false;
	private boolean              viewCalibration = false;	
	
	private GameManager           gameManager;
	private long                  lastEndFouncTime;
	private int                   resizeFactor;
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
    	resizeFactor = 4;
		mMainActivity = activity;
    	Log.i(TAG, "called colorblob"); 	
		mOpenCvCameraView = (CameraBridgeViewBase) new JavaCameraView(mMainActivity, -1);
		FrameLayout preview = (FrameLayout) mMainActivity.findViewById(R.id.camera_preview);
		preview.addView(mOpenCvCameraView);
		mOpenCvCameraView.setCvCameraViewListener(this);
		gameManager = mMainActivity.mGameManager;
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);        
        setMarkerDetectors(width, height);
        
        colorToCalibrate = BLUE_COLOR; // first color to Calibrate 
        viewCalibration = false; 
                
        insertColorBlue = new insertColor(BLUE_COLOR);
		insertColorGreen = new insertColor(GREEN_COLOR);
		insertColorRed = new insertColor(RED_COLOR);
		
		insertColorBlue.prepareGameSize(width, height);
		insertColorGreen.prepareGameSize(width, height);
		insertColorRed.prepareGameSize(width, height);
		
		mMainActivity.mGLView.mRenderer.setScreenWidthHeight(width, height);
    }
    
    public void setMarkerDetectors(int width, int height){
    	int i = 0;
    	myRobot = new MarkerDetector();
    	myRobot.prepareGame(width, height, BLUE_COLOR, RED_COLOR, resizeFactor);
    	NUM_OF_TOWERS = mMainActivity.mGameManager.NUM_OF_TOWERS;
    	mDetectorArry = new MarkerDetector[NUM_OF_TOWERS];
    	for(i = 0; i < NUM_OF_TOWERS; i++){
    		mDetectorArry[i] = new MarkerDetector();
    		mDetectorArry[i].prepareGame(width, height, BLUE_COLOR, RED_COLOR, resizeFactor);
    	}
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
    	if (colorsAreCalibrated == true){
    		return false;
    	}
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
    			gameManager.GameStarted();
    			setMarkerDetectorsColors();
    			colorsAreCalibrated = true;
    		}
    	}
        return false; // don't need subsequent touch events
    }
    
    private void setMarkerDetectorsColors(){
    	myRobot.updateColors(BLUE_COLOR, insertColorBlue.getHSVarr());
    	myRobot.updateColors(GREEN_COLOR, insertColorGreen.getHSVarr());
    	myRobot.updateColors(RED_COLOR, insertColorRed.getHSVarr());
    	int i = 0;
    	for (i = 0; i < NUM_OF_TOWERS; i++){
    		mDetectorArry[i].updateColors(BLUE_COLOR, insertColorBlue.getHSVarr());
    		mDetectorArry[i].updateColors(GREEN_COLOR, insertColorGreen.getHSVarr());
    		mDetectorArry[i].updateColors(RED_COLOR, insertColorRed.getHSVarr());
    	}
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	long  onCameraFrameStartTime = System.currentTimeMillis();//start of the func
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
				    double[] arr1 = insertColorBlue.getHSVarr();
				    double[] arr2 = insertColorGreen.getHSVarr();
				    double[] arr3 = insertColorRed.getHSVarr();
					gameManager.printTopLeft("H: " + arr1[0] + "-" + arr1[3] + " S: " + arr1[1] + "-" + arr1[4] + " V: " + arr1[2] + "-" + arr1[5] + "\n" +
							                 "H: " + arr2[0] + "-" + arr2[3] + " S: " + arr2[1] + "-" + arr2[4] + " V: " + arr2[2] + "-" + arr2[5] + "\n" +
							                 "H: " + arr3[0] + "-" + arr3[3] + " S: " + arr3[1] + "-" + arr3[4] + " V: " + arr3[2] + "-" + arr3[5]);
					return insertColorRed.getMat();
				}
			}			
		} 
			
		if (colorsAreCalibrated == true) {
			Mat mPyrDownMat = new Mat();
			long  PyrDownStart = System.currentTimeMillis();//start of the func
			Imgproc.resize(mRgba, mPyrDownMat, new Size(), 1/((double)resizeFactor), 1/((double)resizeFactor), Imgproc.INTER_NEAREST );
			long  PyrDownEnd = System.currentTimeMillis();//start of the func
			String PyrDownText = "\nresize timr: " + (PyrDownEnd - PyrDownStart);
			//----------------------------------------------
			//long  PyrDownStart1 = System.currentTimeMillis();//start of the func
			//Imgproc.pyrDown(mRgba, mPyrDownMat);
			//long  PyrDownStart2 = System.currentTimeMillis();//start of the func
			//Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);
			//long  PyrDownEnd = System.currentTimeMillis();//start of the func
			//String PyrDownText = "\npyrDown1: " + (PyrDownStart2 - PyrDownStart1) +"\npyrDown2: " + (PyrDownEnd - PyrDownStart2) + 
			//		"\ntotal: " + (PyrDownEnd - PyrDownStart1);
			//----------------------------------------------
			long  startB = System.currentTimeMillis();//start of the func
			List<MatOfPoint> blueContours = myRobot.getContours(mPyrDownMat, BLUE_COLOR);
			long  startG = System.currentTimeMillis();//start of the func
			List<MatOfPoint> GreenContours = myRobot.getContours(mPyrDownMat, GREEN_COLOR);
			long  startR = System.currentTimeMillis();//start of the func
			List<MatOfPoint> redContours = myRobot.getContours(mPyrDownMat, RED_COLOR);
			long  endR = System.currentTimeMillis();//start of the func
			String debugText = "blueContours: " + blueContours.size() +"\nGreenContours: " + GreenContours.size() + "\nredContours: " + redContours.size() +
					"\nblue time:  " + (startG - startB) + "\ngreen time: " + (startR - startG) + "\nred time:   " + (endR - startR);
			//gameManager.printTopRight(debugText + PyrDownText);
			
			//----------------------------------------------
			long  detactonStartOld = System.currentTimeMillis();//time before draw3Dobject
			
			setTrackingColor(blueContours,GreenContours,redContours);			
			
			long  detactonEndOld = System.currentTimeMillis();//time before draw3Dobject
			
			mRgba = myRobot.drawObject(myRobot.getMiddleX(), myRobot.getMiddleY(), mRgba, new Scalar(255, 0, 0));
			for (int k = 0; k < NUM_OF_TOWERS; k++){
				mRgba = mDetectorArry[k].drawObject(mDetectorArry[k].getMiddleX(), mDetectorArry[k].getMiddleY(), mRgba, new Scalar(255, 0, 0));
			}
			int num = 0;
			for (int k = 0; k < NUM_OF_TOWERS; k++){
				if (mDetectorArry[k].isDetected()) {
					num++;
				}
			}
			//gameManager.printTopRight("num of detacted obj : " + num);
			mMainActivity.mGameManager.objCheck();
			
			long  onCameraFrameBeforeDraw3DobjectTime = System.currentTimeMillis();//time before draw3Dobject
			draw3Dobject(); //prints the 3d objects
			long  onCameraFrameEndTime = System.currentTimeMillis();//end of the func
			//printFouncTimeForDebug(onCameraFrameStartTime,onCameraFrameBeforeDraw3DobjectTime,onCameraFrameEndTime);
			
		}
		
		lastEndFouncTime = System.currentTimeMillis();//End of the func for use in the next start of founc
		return mRgba;
	}

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }
    
    private void draw3Dobject(){
    	mMainActivity.mGLView.mRenderer.setTrackObjects(mDetectorArry);
    	mMainActivity.mGLView.requestRender();
    }
    
    private void printFouncTimeForDebug(long start, long mid, long end){
    	long sM = mid-start;
    	long sE = end-start;
    	long mE = end-mid;
    	long lastEtoS = start-lastEndFouncTime;
    	long lastEtoE = end-lastEndFouncTime;
    	// need to add time from end to next start
    	String debugText = "onCameraFrame time: " + sE + "\ndetection time: " + sM + "\nDraw3Dobject time: " + mE 
    			+ "\nbetween time calls: " + lastEtoS + "\nfull cicle time: " + lastEtoE;
    	gameManager.printTopLeft(debugText);
    }
    
    public void setCoinDistribution(int newCoin){
    	int i = 0;
    	for (i = 0; i < NUM_OF_TOWERS; i++){
    		mDetectorArry[i].setCoin(false);
    	}
    	mDetectorArry[newCoin].setCoin(true);
    }
    
    public void setTrackingColor(List<MatOfPoint> blueContours, List<MatOfPoint> greenContours, List<MatOfPoint> redContours){
    	myRobot.trackObject(blueContours, greenContours, redContours);
		mDetectorArry[0].trackObject(blueContours, redContours, redContours);
		mDetectorArry[1].trackObject(greenContours, redContours, redContours);
		mDetectorArry[2].trackObject(redContours, blueContours, blueContours);
		mDetectorArry[3].trackObject(blueContours, greenContours, greenContours);
		mDetectorArry[4].trackObject(greenContours, blueContours, blueContours);
		mDetectorArry[5].trackObject(redContours, greenContours, greenContours);
    }
}
