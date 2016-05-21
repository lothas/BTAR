package org.opencv.samples.colorblobdetect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.example.bravo.MainActivity;

import android.util.Log;
import bravo.game.manager.GameManager;

public class MarkerDetector {
    
	
    //-------------------------debug TAGs----------------------//
    protected static final String TAG = "FP";
    protected static final String TAG1 = "FP1";
    protected static final String TAGB = "FPB";
    protected static final String TAGF = "FPF";
    protected static final String NEWTAG = "NEWTAG";
    //-------------------------Static--------------------------//
    private static final int NO_COLOR = -1;
	private static final int BLUE_COLOR = 1;
	private static final int GREEN_COLOR = 2;
	private static final int RED_COLOR = 3;
	//-------------------------Fields--------------------------//
	private boolean flagFound = false;
	private boolean flag = true;
	private boolean flagBack = true;
	private int FrontColor, BackColor;
	private int frameHeight, frameWidth;
	private int FrontX, FrontY, BackX, BackY, MiddleX ,MiddleY;
    private double iLowH, iLowS, iLowV, iHighH, iHighS, iHighV;
	private double[] blueHSV; //0=iLowH, 1=iLowS, 2=iLowV, 3=iHighH, 4=iHighS, 5=iHighV
	private double[] greenHSV; //0=iLowH, 1=iLowS, 2=iLowV, 3=iHighH, 4=iHighS, 5=iHighV
	private double[] redHSV; //0=iLowH, 1=iLowS, 2=iLowV, 3=iHighH, 4=iHighS, 5=iHighV
	private long endTime, startTime; //for performance check purpose 
	private int resizeFactor;
	//----------------------coinFilds--------------------------//
	private double mCoinAngleFromXAxis;
	private boolean mHasCoin;
	private double mRadiosFromMiddle;
	//----------------------randomGenarator--------------------//
	private Random mRandomGenerator;
	
	//--------------------------Mats---------------------------//
	Mat mPyrDownMat = new Mat();
    Mat mHsvMat = new Mat();
    Mat mMask = new Mat();
    Mat mDilatedMask = new Mat();
    Mat mHierarchy = new Mat();
	
	Mat mPyrDownMatBack = new Mat();
    Mat mHsvMatBack = new Mat();
    Mat mMaskBack = new Mat();
    Mat mDilatedMaskBack = new Mat();
    Mat mHierarchyBack = new Mat();
	//-------------------------Methods-------------------------//

    
    public boolean trackObject(List<MatOfPoint> frontContours, List<MatOfPoint> backContours){
     	
    	//------------------------------------------------------
        double maxArea = frameHeight*frameWidth / (resizeFactor * resizeFactor);
        double MinContourArea = 0.001; //TODO find optimal MinContourArea and MaxContourArea
        double MaxContourArea = 0.1;
        //------------------------------------------------------
    	Iterator<MatOfPoint> frontCounterIterator = frontContours.iterator();
        while (frontCounterIterator.hasNext()) {
            MatOfPoint contour = frontCounterIterator.next();
            double counterSize = Imgproc.contourArea(contour);
            if ((counterSize > MinContourArea*maxArea) && (counterSize < MaxContourArea*maxArea)) { 
            	//--------------------------------------------
            	float[] radius = new float[1]; 
				Point center = new Point();
				Imgproc.minEnclosingCircle(new MatOfPoint2f(contour.toArray()), center, radius);
				FrontX = ((int) center.x);
				FrontY = ((int) center.y);
				//--------------------------------------------
				//Log.i(NEWTAG1, "radios = " + Math.sqrt(radius[0]*radius[0] + radius[1]*radius[1]));
				if (trackObjectBack(radius[0], backContours, counterSize) == true){
					SetXYFactor(resizeFactor);
					SetXY();
					flagFound = true;
					return true;
				}
            }
        
        }
        flagFound = false;
		return false;    	
    }
    
    public boolean trackObjectBack(float frontRadius, List<MatOfPoint> backContours, double frontCounterSize){
    	//--------------------------------------------------------------------------------
        double MinContourArea = 0.2; //TODO find optimal MinContourArea and MaxContourArea
        double MaxContourArea = 5;
        //--------------------------------------------------------------------------------
    	Iterator<MatOfPoint> backCounterIterator = backContours.iterator();
    	while (backCounterIterator.hasNext()) {
            MatOfPoint contour = backCounterIterator.next();
            double counterSize = Imgproc.contourArea(contour);
            if ((counterSize > MinContourArea*frontCounterSize) && (counterSize < MaxContourArea*frontCounterSize)) {
            	//--------------------------------------------
        		float[] radius = new float[1];
				Point center = new Point();
				Imgproc.minEnclosingCircle(new MatOfPoint2f(contour.toArray()), center, radius);
				BackX = ((int) center.x);
				BackY = ((int) center.y);
				float distanceFromFront = getFront2BackLength(); //uses BackX, BackY, FrontX, FrontY  
				//--------------------------------------------
				int allowedSistanceFactor = 3; //TODO faind optimal
            	if (distanceFromFront < allowedSistanceFactor * frontRadius){
            		return true;
            	}               
            }
    	}
    	return false;
    }
    
    private void SetXYFactor(int factor) {
    	FrontX = FrontX * factor;
		FrontY = FrontY * factor;
		BackX  = BackX  * factor;
		BackY  = BackY  * factor;
	}
    
    public boolean trackFront(Mat rgbaImage) {
    	int high, wide, UpY, LeftX;
    	int PyrFrameHight = rgbaImage.rows() - 4; 
        int PyrFrameWight = rgbaImage.cols() - 4;
        SetColorToTrack(FrontColor);
        Imgproc.cvtColor(rgbaImage, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);
        
        //---------------debug prints--------------
        if (flag == true){
        	flag = false;
        	Log.i(TAG, "iLowH = " + iLowH + " iHighH = " + iHighH);
        	Log.i(TAG, "iLowS = " + iLowS + " iHighS = " + iHighS);
        	Log.i(TAG, "iLowV = " + iLowV + " iHighV = " + iHighV);
        }
        //-----------------------------------------
        Core.inRange(mHsvMat, new Scalar(iLowH, iLowS, iLowV), new Scalar(iHighH, iHighS, iHighV), mMask);
        Imgproc.dilate(mMask, mDilatedMask, new Mat());//closes black holes in the mMask (binary image)
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        //------------------------------------------------------
        double maxArea = PyrFrameHight*PyrFrameWight;
        double MinContourArea = 0.001; //TODO find optimal MinContourArea and MaxContourArea
        double MaxContourArea = 0.1;
        //------------------------------------------------------
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if ((Imgproc.contourArea(contour) > MinContourArea*maxArea) && (Imgproc.contourArea(contour) < MaxContourArea*maxArea)) {         	
            	float[] radius = new float[1];
				Point center = new Point();
				Imgproc.minEnclosingCircle(new MatOfPoint2f(contour.toArray()), center, radius);
				FrontX = ((int) center.x);
				FrontY = ((int) center.y);						
				//configure up'er left point of Rectangle around (FrontX,FrontY) so it wont be out of bound 
				high = (int) (4 * radius[0]); //high, wide are setting the Size of the Rectangle
				wide = (int) (4 * radius[0]);
				UpY = FrontY - high / 2;
				LeftX = FrontX - wide / 2;
				FrontX = FrontX * 4; //we multiply X Y by 4 because the image was pyrDown by factor of 4
				FrontY = FrontY * 4;
				if (LeftX < 0)                    {LeftX = 0;}
				if (LeftX + wide > PyrFrameWight) {wide = PyrFrameWight - LeftX;}
				if (UpY < 0)                      {UpY = 0;}
				if (UpY + high > PyrFrameHight)   {high = PyrFrameHight - UpY;}
				Log.i(TAG, "UpY = " + UpY + ", LeftX = " + LeftX);
				Log.i(TAG, "high = " + high + ", wide = " + wide);
				//-------------------------------------------------------------------------------------------
				
				Rect FrontArea = new Rect(LeftX, UpY, wide, high);
				Mat croppedRef = new Mat(rgbaImage, FrontArea);
				
				if (trackBack(croppedRef, Imgproc.contourArea(contour)) == true) {
					BackX = (BackX + LeftX) * 4; //we multiply X Y by 4 because the image was pyrDown by factor of 4
					BackY = (BackY + UpY) * 4;
					Log.i(TAGF, "BackY = " + BackY + " BackX = " + BackX);
					Log.i(TAGF, "trackFront exiting with true");
					flagFound = true;
					SetXY();
					return true;
				}
			}
        }
        flagFound = false;
        Log.i(TAGF, "trackFront exiting with false");
        return false;
    }
    
    public boolean trackBack(Mat rgbaImage, double frontCounterSize) {
        SetColorToTrack(BackColor);
        Imgproc.cvtColor(rgbaImage, mHsvMatBack, Imgproc.COLOR_RGB2HSV_FULL);

        if (flagBack == true){
        	flagBack = false;
        	Log.i(TAG, "iLowH = " + iLowH + " iHighH = " + iHighH);
        	Log.i(TAG, "iLowS = " + iLowS + " iHighS = " + iHighS);
        	Log.i(TAG, "iLowV = " + iLowV + " iHighV = " + iHighV);
        }
        Core.inRange(mHsvMatBack, new Scalar(iLowH, iLowS, iLowV), new Scalar(iHighH, iHighS, iHighV), mMaskBack);
        Imgproc.dilate(mMaskBack, mDilatedMaskBack, new Mat());

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(mDilatedMaskBack, contours, mHierarchyBack, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        //--------------------------------------------------------------------------------
        double MinContourArea = 0.2; //TODO find optimal MinContourArea and MaxContourArea
        double MaxContourArea = 5;
        //--------------------------------------------------------------------------------
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
			double area = Imgproc.contourArea(contour);
			if ((area > MinContourArea*frontCounterSize) && (area <  MaxContourArea*frontCounterSize)){// possibale sulotion
			//if (area > 5){
				float[] radius = new float[1];
				Point center = new Point();
				Imgproc.minEnclosingCircle(new MatOfPoint2f(contour.toArray()), center, radius);
				BackX = ((int) center.x);
				BackY = ((int) center.y);
				Log.i(TAG, "trackBack exiting with true");
				return true;
			}
		}  
        return false;
    }
    
    private Scalar SetColorToTrack(int Color) {
		Scalar ColorObj = new Scalar(0, 255, 0);
		switch (Color) {
			//Blue
		case BLUE_COLOR:
			iLowH = blueHSV[0];
			iLowS = blueHSV[1];
			iLowV = blueHSV[2];
			iHighH = blueHSV[3];
			iHighS = blueHSV[4];
			iHighV = blueHSV[5];
			ColorObj = new Scalar(255, 0, 0);
			break;
			//Green
		case GREEN_COLOR:
			iLowH = greenHSV[0];
			iLowS = greenHSV[1];
			iLowV = greenHSV[2];
			iHighH = greenHSV[3];
			iHighS = greenHSV[4];
			iHighV = greenHSV[5];
			ColorObj = new Scalar(0, 255, 0);
			break;
			//Red
		case RED_COLOR:
			iLowH = redHSV[0];
			iLowS = redHSV[1];
			iLowV = redHSV[2];
			iHighH = redHSV[3];
			iHighS = redHSV[4];
			iHighV = redHSV[5];
			ColorObj = new Scalar(0, 0, 255);
			break;
		default:
			iLowH = 1;
			iHighH = 0;
			iLowS = 1;
			iHighS = 0;
			iLowV = 1;
			iHighV = 0;
		}
		return ColorObj;
	}
    
    public boolean updateColors(int color, double[] HSVtresholds){
		int i;
		switch (color) {
		case (BLUE_COLOR):
			Log.i(TAG1, "updateColors : BLUE_COLOR");
			Log.i(TAG, "iLowH = " + HSVtresholds[0] + " iHighH = " + HSVtresholds[3]);
			Log.i(TAG, "iLowS = " + HSVtresholds[1] + " iHighS = " + HSVtresholds[4]);
			Log.i(TAG, "iLowV = " + HSVtresholds[2] + " iHighV = " + HSVtresholds[5]);
			for (i = 0; i < 6; i++) {
				blueHSV[i] = HSVtresholds[i];
			}
			break;
		case (GREEN_COLOR):
			Log.i(TAG1, "updateColors : GREEN_COLOR");
			Log.i(TAG, "iLowH = " + HSVtresholds[0] + " iHighH = " + HSVtresholds[3]);
			Log.i(TAG, "iLowS = " + HSVtresholds[1] + " iHighS = " + HSVtresholds[4]);
			Log.i(TAG, "iLowV = " + HSVtresholds[2] + " iHighV = " + HSVtresholds[5]);
			for (i = 0; i < 6; i++) {
				greenHSV[i] = HSVtresholds[i];
			}
			break;
		case (RED_COLOR):
			Log.i(TAG1, "updateColors : RED_COLOR");
			Log.i(TAG, "iLowH = " + HSVtresholds[0] + " iHighH = " + HSVtresholds[3]);
			Log.i(TAG, "iLowS = " + HSVtresholds[1] + " iHighS = " + HSVtresholds[4]);
			Log.i(TAG, "iLowV = " + HSVtresholds[2] + " iHighV = " + HSVtresholds[5]);
			for (i = 0; i < 6; i++) {
				redHSV[i] = HSVtresholds[i];
			}
			break;
		}
		return true;
	}
    
    public Mat drawObject(int X, int Y, Mat frame, Scalar ColorObj) {
    	if (flagFound == false){
    		return frame;
    	}
		Imgproc.circle(frame, new Point(X, Y), 20, ColorObj, 2);
		Imgproc.line(frame, new Point(BackX, BackY), new Point(FrontX, FrontY), new Scalar(0,255,100), 2);
		Imgproc.circle(frame, new Point(FrontX, FrontY), 5, new Scalar(0, 255, 100), 2);
		if (Y - 25>0)
			Imgproc.line(frame, new Point(X, Y), new Point(X, Y - 25), ColorObj, 2);
		else Imgproc.line(frame, new Point(X, Y), new Point(X, 0), ColorObj, 2);
		if (Y + 25<frameHeight)
			Imgproc.line(frame, new Point(X, Y), new Point(X, Y + 25), ColorObj, 2);
		else Imgproc.line(frame, new Point(X, Y), new Point(X, frameHeight), ColorObj, 2);
		if (X - 25>0)
			Imgproc.line(frame, new Point(X, Y), new Point(X - 25, Y), ColorObj, 2);
		else Imgproc.line(frame, new Point(X, Y), new Point(0, Y), ColorObj, 2);
		if (X + 25<frameWidth)
			Imgproc.line(frame, new Point(X, Y), new Point(X + 25, Y), ColorObj, 2);
		else Imgproc.line(frame, new Point(X, Y), new Point(frameWidth, Y), ColorObj, 2);
		return frame;
		
	}
    
    public void prepareGame(int width, int height, int frontCol, int beckCol, int factor) {
    	mHasCoin = false;
    	mRandomGenerator = new Random();
        frameWidth = width;
        frameHeight = height;
        FrontColor =  frontCol;
        BackColor = beckCol;
        blueHSV = new double[6];
        greenHSV = new double[6];
        redHSV = new double[6];
        resizeFactor = factor;      
    }
    
    public int getFrontX(){
		return FrontX;
	}
	
	public int getFrontY(){
		return FrontY;
	}
	
	public int getBackX() {
		return BackX;
	}

	public int getBackY() {
		return BackY;
	}
	
	public int getMiddleX() {
		return MiddleX;
	}

	public int getMiddleY() {
		return MiddleY;
	}
	
	public int getFrameHeight() {
		return frameHeight;
	}

	public int getFrameWidth() {
		return frameWidth;
	}
	
	public boolean isDetected() {
		return flagFound;
	}
	
	public float getFront2BackLength() {
		return (float)Math.sqrt((double)((FrontX - BackX)*(FrontX - BackX) + (FrontY - BackY)*(FrontY - BackY)));
	}
	
	public long timeOfTrackBack() {
		return endTime - startTime;
	}
	
	public float getAngleFromXAxis() {
		double c = getFront2BackLength();
		double a = (double)(FrontX - BackX);
		if (FrontY < BackY){ //upper half
			if (a > 0){
				return ((float)Math.toDegrees(Math.acos(a/c)));
			}
			return (180 - (float)Math.toDegrees(Math.acos(-a/c)));
		}else{
			if (a > 0){
				return (360 - (float)Math.toDegrees(Math.acos(a/c)));
			}
			return (180 + (float)Math.toDegrees(Math.acos(-a/c)));
		}
	}
	
	public double getAngleFromXAxisInRadians() {
		double c = getFront2BackLength();
		double a = (double)(FrontX - BackX);
		if (FrontY < BackY){ //upper half
			if (a > 0){
				return (Math.acos(a/c));
			}
			return (Math.PI - (Math.acos(-a/c)));
		}else{
			if (a > 0){
				return (2*Math.PI - (Math.acos(a/c)));
			}
			return (Math.PI + (Math.acos(-a/c)));
		}
	}
	
	private void SetXY() {
		MiddleX = (FrontX + BackX) / 2;
		MiddleY = (FrontY + BackY) / 2;

	}

	private int setLeftX(int LeftX, int wide,int PyrFrameWight){
		if (LeftX < 0) {
			LeftX = 0;
		}
		if (LeftX + wide > PyrFrameWight) {
			wide = PyrFrameWight - LeftX;
		}
		return LeftX;
	}
	
	private int setUpY(int UpY, int high, int PyrFrameHight){
		if (UpY < 0) {
			UpY = 0;
		}
		if (UpY + high > PyrFrameHight) {
			high = PyrFrameHight - UpY;
		}
		return UpY;
	}
	
	public List<MatOfPoint> getContours(Mat rgbMat, int color){
		SetColorToTrack(color); // sets the range of the color we want to Mask
        Imgproc.cvtColor(rgbMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL); // makes HSV image from rbg Mat
        Core.inRange(mHsvMat, new Scalar(iLowH, iLowS, iLowV), new Scalar(iHighH, iHighS, iHighV), mMask); // makes the Mask of the color we want 
        Imgproc.dilate(mMask, mDilatedMask, new Mat()); //closes black holes in the mMask (binary image)
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>(); //setting up contours list
        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE); //finding the contours 
        //TODO check which method is the quickest (Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
    	
    	return contours;
    	
    }
	
	public void setCoin(boolean setCoin){
		if (setCoin == false){
			mHasCoin = false;
			return;
		}
		mHasCoin = true;
		mCoinAngleFromXAxis = 2*Math.PI*mRandomGenerator.nextDouble();
	}
	
	public boolean hasCoin(){
		return mHasCoin;
	}
	
	public int getCoinX(){
		mRadiosFromMiddle = 3; //TODO randomize and set only 1 getAngleFromXAxis
		return (int) (mRadiosFromMiddle*getFront2BackLength()*Math.sin(mCoinAngleFromXAxis + getAngleFromXAxisInRadians())) + MiddleX;
	}
	
	public int getCoinY(){
		mRadiosFromMiddle = 3; //TODO randomize and set only 1
		return (int) (mRadiosFromMiddle*getFront2BackLength()*Math.cos(mCoinAngleFromXAxis + + getAngleFromXAxisInRadians())) + MiddleY;
	}
}
