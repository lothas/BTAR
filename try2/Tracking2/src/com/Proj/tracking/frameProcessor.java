package com.Proj.tracking;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.util.Log;

public class frameProcessor {
	//-------------------------Fields--------------------------//
	protected static final String TAG = "FP";
	protected static final String TAG_Time = "FPtime";
	protected static final String TAG_Now = "Now";
	protected static final String TAGtrackingFront = "tFront";
	
	
	private static final int NO_COLOR = -1;
	private static final int BLUE_COLOR = 1;
	private static final int GREEN_COLOR = 2;
	private static final int RED_COLOR = 3;
	
	private Mat mRgba15;
	
	private Mat mPyrDownMat;
	private Mat mHsvMat;
    private Mat mMask;
    private Mat mDilatedMask;
    private Mat mHierarchy;
	
    private int frameHeight, frameWidth;
	private int FrontX, FrontY, BeckX, BeckY, MiddleX ,MiddleY;
	private int FrontColor, BeckColor;
	private int resizeFactor, maxArea;
	private int LowV = 50, LowS = 50, HighV = 50, HighS = 50;
	private double iLowH, iLowS, iLowV, iHighH, iHighS, iHighV;
	private double[] blueHSV; //0=iLowH, 1=iLowS, 2=iLowV, 3=iHighH, 4=iHighS, 5=iHighV
	private double[] greenHSV; //0=iLowH, 1=iLowS, 2=iLowV, 3=iHighH, 4=iHighS, 5=iHighV
	private double[] redHSV; //0=iLowH, 1=iLowS, 2=iLowV, 3=iHighH, 4=iHighS, 5=iHighV
	
	
    //Mat mPyrDownMat = new Mat();
    //Mat mHsvMat = new Mat();
    //Mat mMask = new Mat();
    //Mat mDilatedMask = new Mat();
    //Mat mHierarchy = new Mat();
	
	// for timing checks
	private Long drawObjectStart, drawObjectEnd, TrackingImgStart, TrackingImgEnd, updateColorsStart, updateColorsEnd;
	private Long TrackingFrontStart, TrackingFrontEnd, TrackingBeckStart, TrackingBeckEnd, start, end;
	//-------------------------Methods-------------------------//
	

	public frameProcessor(int frontC, int backC) {
		FrontColor = frontC;
		BeckColor = backC;
		blueHSV = new double[6];
		greenHSV = new double[6];
		redHSV = new double[6];
		maxArea = 100;
		FrontX = 100;
		FrontY = 100;
		
    }

	private boolean trackingFront(Mat rgbaImage) {
		Log.i(TAGtrackingFront, "trackingFront: Start");
		
		Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);
        
        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);
        
        Core.inRange(mHsvMat, new Scalar(iLowH, iLowS, iLowV), new Scalar(iHighH, iHighS, iHighV), mMask);
        Imgproc.dilate(mMask, mDilatedMask, new Mat());
        
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        
        Iterator<MatOfPoint> each = contours.iterator();
        Log.i(TAGtrackingFront, "trackingFront: each.hasNext() = " + each.hasNext());
        Log.i(TAGtrackingFront, "trackingFront: contours.size() = " + contours.size());
        while (each.hasNext()) {
        	
        	MatOfPoint contour = each.next();
            double area = Imgproc.contourArea(contour);
            
            //Log.i(TAGtrackingFront, "trackingFront: area = " + area + " maxArea = " + maxArea / resizeFactor);
            if (area > maxArea / resizeFactor){
              
            	float[] radius = new float[1];
    			Point center = new Point();
    			Imgproc.minEnclosingCircle(new MatOfPoint2f(contour.toArray()), center, radius);
    			FrontX = ((int) center.x);
				FrontY = ((int) center.y);
				return true;
				
            }
        }
		
		
		return false;
	}
	public synchronized void prepareGameSize(int width, int height) {
		Log.i(TAG, "starting prepareGameSize");
        mRgba15 = new Mat(height, width, CvType.CV_8UC4);
        frameWidth = width;
        frameHeight = height;
        mPyrDownMat = new Mat();
	    mHsvMat = new Mat();
	    mMask = new Mat();
	    mDilatedMask = new Mat();
	    mHierarchy = new Mat();
        Log.i(TAG, "prepareGameSize: frameWidth = " + frameWidth + ", frameHeight = " + frameHeight);
    }
	
	public Mat drawObject(int X, int Y, Mat frame, Scalar ColorObj) {
		Log.i(TAG, "starting drawObject: x = " + X + ", y = " + Y);
		Log.i(TAG, "drawObject: drawing circle");
		Imgproc.circle(frame, new Point(X, Y), 20, ColorObj, 2);
		//Imgproc.line(frame, new Point(BeckX, BeckY), new Point(FrontX, FrontY), new Scalar(0,0,255), 2);
		//Imgproc.circle(frame, new Point(FrontX, FrontY), 5, new Scalar(0, 0, 255), 2);
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

		Imgproc.putText(frame, X + "," + Y, new Point(X, Y + 30), 1, 1, ColorObj, 2);
		Log.i(TAG, "Exiting drawObject");
		return frame;
		
	}

	public synchronized boolean TrackingImg(Mat imgOriginal, int resizeFact){
		resizeFactor = resizeFact;
		Log.i(TAG, "starting TrackingImg");
		Boolean foundRobo;
		// Track Front
		
		Log.i(TAG, "TrackingImg: calling TrackingFront(imgOriginal)");
		TrackingFrontStart = System.currentTimeMillis();
		//foundRobo = TrackingFront(imgOriginal);
		foundRobo = trackingFront(imgOriginal);
		TrackingFrontEnd = System.currentTimeMillis();
		Log.i(TAG_Time, "delta TrackingFront: " + (TrackingFrontEnd - TrackingFrontStart));
		Log.i(TAG, "TrackingImg: finished TrackingFront(imgOriginal) = " + foundRobo);
		if (foundRobo) {
			SetXY();
			Log.i(TAG_Time, "delta drawObject: " + (drawObjectEnd - drawObjectStart));
			Log.i(TAG_Now, "in TrackingImg: FrontX = " + FrontX + ", FrontY = " + FrontY);
			return true;
		}
		return false;
	}
	
	public boolean updateColors(int color, double[] HSVtresholds){
		updateColorsStart = System.currentTimeMillis();
		Log.i(TAG, "starting updateColors");
		int i;
		switch (color) {
		case (BLUE_COLOR):
			for(i=0;i<6;i++){
				blueHSV[i] = HSVtresholds[i];
			}
			break;
		case (GREEN_COLOR):
			for(i=0;i<6;i++){
				greenHSV[i] = HSVtresholds[i];
			}
			break;	
		case (RED_COLOR):
			for(i=0;i<6;i++){
				redHSV[i] = HSVtresholds[i];
			}
			break;
		}
		updateColorsEnd = System.currentTimeMillis();
		Log.i(TAG_Time, "delta updateColors: " + (updateColorsEnd - updateColorsStart));
		return true;
	}
	
	private synchronized boolean TrackingFront(Mat imgOriginal){
		Log.i(TAG, "starting TrackingFront");
		int i = 0, LeftX = 0, UpY = 0, wide = frameWidth / 5, high = frameHeight / 3;
		double ContourSize = 0;
		Scalar ColorObj;
		Log.i(TAG, "TrackingFront: calling SetColorToTrack");
		start = System.currentTimeMillis();
		ColorObj = SetColorToTrack(FrontColor);
		end = System.currentTimeMillis();
		Log.i(TAG_Time, "delta SetColorToTrack: " + (end - start));
		Mat imgThresholded = new Mat();
		Mat imgHSV  = new Mat();
		Mat imgFrontArea = new Mat();
		Log.i(TAG, "TrackingFront: calling Imgproc.cvtColor");
		start = System.currentTimeMillis();
		Imgproc.cvtColor(imgOriginal, imgHSV, Imgproc.COLOR_BGR2HSV); //Convert the captured frame from BGR to HSV
		end = System.currentTimeMillis();
		Log.i(TAG_Time, "delta cvtColor: " + (end - start));
		start = System.currentTimeMillis();
		Core.inRange(imgHSV, new Scalar(iLowH, iLowS, iLowV), new Scalar(iHighH, iHighS, iHighV), imgThresholded);
		end = System.currentTimeMillis();
		Log.i(TAG_Time, "delta inRange: " + (end - start));
		start = System.currentTimeMillis();
		DoErode(imgThresholded, true);
		end = System.currentTimeMillis();
		Log.i(TAG_Time, "delta DoErode: " + (end - start));
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Log.i(TAG, "TrackingFront: calling findContours");
		start = System.currentTimeMillis();
		Imgproc.findContours(imgThresholded, contours, new Mat(),Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);			
		end = System.currentTimeMillis();
		Log.i(TAG_Time, "delta findContours: " + (end - start));
		Log.i(TAG, "TrackingFront: finished findContours, contours.size() = " + contours.size());
		Long forStart = System.currentTimeMillis();
		Long forEnd;
		for (i = 0; i < contours.size(); i++) {
			Log.i(TAG, "TrackingFront: in for(i = 0; i < contours.size(); i++): i = " + i);
			ContourSize = Imgproc.contourArea(contours.get(i));
			Log.i(TAG, "TrackingFront: ContourSize = " + ContourSize);
			if (ContourSize < 10) {
				continue;
			};
			float[] radius = new float[1];
			Point center = new Point();
			Log.i(TAG,"TrackingFront: Calling  Imgproc.minEnclosingCircle");
			Imgproc.minEnclosingCircle(new MatOfPoint2f(contours.get(i).toArray()), center, radius);
			Log.i(TAG, "TrackingFront: finished minEnclosingCircle");
			double temp = Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true);
			Log.i(TAG, "TrackingFront: finished Imgproc.arcLength = " + temp);
			double Circlity = ((Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true) / (2 * Math.PI))	/ (radius[0]));
			Log.i(TAG, "TrackingFront: Circlity = " + Circlity);
			if ((Circlity < 1.2) & (Circlity > 0.8)) {
				if ((Imgproc.contourArea(contours.get(i)) < 500/resizeFactor)) {
					continue;
				}
				FrontX = ((int) center.x);
				FrontY = ((int) center.y);
				Log.i(TAG, "TrackingFront: FrontX = " + FrontX + ", FrontY = " + FrontX);
				high = (int) (8 * radius[0]);
				wide = (int) (8 * radius[0]);
				UpY = FrontY - high / 2;
				LeftX = FrontX - wide / 2;
				if (UpY < 0) {
					UpY = 0;
				}
				if (LeftX < 0) {
					LeftX = 0;
				}
				if (UpY + high > frameHeight) {
					UpY = frameHeight - high;
				}
				if (LeftX + wide > frameWidth) {
					LeftX = frameWidth - wide;
				}
				Log.i(TAG, "TrackingFront: Calling Rect");
				Rect FrontArea = new Rect(LeftX, UpY, wide, high);
				// Crop the full image to that image contained by the rectangle
				// myROI
				// Note that this doesn't copy the data
				Mat croppedRef = new Mat(imgOriginal, FrontArea);
				// Copy the data into new matrix
				croppedRef.copyTo(imgFrontArea);
				Log.i(TAG, "in TrackingFront: Calling TrackingBeck(imgFrontArea, ContourSize, Circlity)");
				
				//if (TrackingBeck(imgFrontArea, ContourSize, Circlity)) {
					BeckX = BeckX + LeftX;
					BeckY = BeckY + UpY;
					forEnd = System.currentTimeMillis();
					Log.i(TAG_Time, "delta for if true: " + (forEnd - forStart));
					FrontX = FrontX*resizeFactor;
					FrontY = FrontY*resizeFactor;
					return true;
				//}
			}
		}
		forEnd = System.currentTimeMillis();
		Log.i(TAG_Time, "delta for if false: " + (forEnd - forStart));
		return false;
	}

	private synchronized boolean TrackingBeck(Mat imgFrontArea, double frontContourSize, double frontCirclity) {
		TrackingBeckStart = System.currentTimeMillis();
		int i = 0;
		double ContourSize = 0;
		Scalar ColorObj;
		ColorObj = SetColorToTrack(BeckColor);
		Mat imgThresholded = new Mat(); 
		Mat imgHSV = new Mat();
		Imgproc.cvtColor(imgFrontArea, imgHSV, Imgproc.COLOR_BGR2HSV); //Convert the captured frame from BGR to HSV
		Core.inRange(imgHSV, new Scalar(iLowH, iLowS, iLowV), new Scalar(iHighH, iHighS, iHighV), imgThresholded);
		DoErode(imgThresholded, true);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Log.i(TAG, "TrackingBeck: calling Imgproc.findContours");
		Imgproc.findContours(imgThresholded, contours, new Mat(),Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);
		if (contours.size() > 0) {
			for (i = 0; i < contours.size(); i++) {
				ContourSize = Imgproc.contourArea(contours.get(i));
				Log.i(TAG, "TrackingBeck: ContourSize = " + ContourSize);
				if (ContourSize < 10) {
					continue;
				};
				
				//Moments moment = moments((cv::Mat)contours[i]);
				if ((ContourSize > frontContourSize - 100) & (ContourSize < frontContourSize + 100)) {
					float[] radius = new float[1];
					Point center = new Point();
					
					Log.i(TAG,"TrackingBeck: Calling  Imgproc.minEnclosingCircle");
					Imgproc.minEnclosingCircle(new MatOfPoint2f(contours.get(i).toArray()), center, radius);
					double Circlity = ((Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true) / (2 * Math.PI))	/ (radius[0]));
										
					if ((Circlity < frontCirclity + 0.1) & (Circlity > frontCirclity - 0.1)) {
						BeckX = (int) center.x;
						BeckY = (int) center.y;
						Log.i(TAG, "TrackingBeck: BeckX = " + BeckX + ", BeckY = " + BeckY);
						TrackingBeckEnd = System.currentTimeMillis();
						Log.i(TAG_Time, "delta TrackingBeck if true: " + (TrackingBeckEnd - TrackingBeckStart));
						return true;
					}
				}
			}
		}
		TrackingBeckEnd = System.currentTimeMillis();
		Log.i(TAG_Time, "delta TrackingBeck if false: " + (TrackingBeckEnd - TrackingBeckStart));
		return false; 
	}

	private synchronized void DoErode(Mat imgThresholded, boolean enable) {
		Log.i(TAG, "starting DoErode");
		if (enable) {
			Imgproc.erode(imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(8, 8)));
			Imgproc.dilate(imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(8, 8)));

			//morphological closing (fill small holes in the foreground)
			Imgproc.dilate(imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));
			Imgproc.erode(imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));
		}
		Log.i(TAG, "Exiting DoErode");
	}

	private Scalar SetColorToTrack(int Color) {
		Scalar ColorObj = new Scalar(0, 255, 0);
		switch (Color) {
			//Blue
		case BLUE_COLOR:
			iLowH = blueHSV[0];
			iLowS = blueHSV[1] - LowS;
			iLowV = blueHSV[2] - LowV;
			iHighH = blueHSV[3];
			iHighS = blueHSV[4] + HighS;
			iHighV = blueHSV[5] + HighV;
			ColorObj = new Scalar(255, 0, 0);
			break;
			//Green
		case GREEN_COLOR:
			iLowH = greenHSV[0];
			iLowS = greenHSV[1] - LowS;
			iLowV = greenHSV[2] - LowV;
			iHighH = greenHSV[3];
			iHighS = greenHSV[4] + HighS;
			iHighV = greenHSV[5] + HighV;
			ColorObj = new Scalar(0, 255, 0);
			break;
			//Red
		case RED_COLOR:
			iLowH = redHSV[0];
			iLowS = redHSV[1] - LowS;
			iLowV = redHSV[2] - LowV;
			iHighH = redHSV[3];
			iHighS = redHSV[4] + HighS;
			iHighV = redHSV[5] + HighV;
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

	private void SetXY() {
		MiddleX = (FrontX + BeckX) / 2;
		MiddleY = (FrontY + BeckY) / 2;

	}
	
	public int getFrontX(){
		return FrontX;
	}
	
	public int getFrontY(){
		return FrontY;
	}

	



}
