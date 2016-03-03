package com.Proj.tracking;

import java.util.ArrayList;
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
	protected static final String PrefTAG = "Prefdebug";
	protected static final String TAG = "frameProcessor: debug";
	private static final int NO_COLOR = -1;
	private static final int BLUE_COLOR = 1;
	private static final int GREEN_COLOR = 2;
	private static final int RED_COLOR = 3;
	private Mat mRgba15;
	private int frameHeight, frameWidth;
	private int FrontX, FrontY, BeckX, BeckY, MiddleX ,MiddleY;
	private int FrontColor, BeckColor;
	private double iLowH, iLowS, iLowV, iHighH, iHighS, iHighV;
	private double[] blueHSV; //0=iLowH, 1=iLowS, 2=iLowV, 3=iHighH, 4=iHighS, 5=iHighV
	private double[] greenHSV; //0=iLowH, 1=iLowS, 2=iLowV, 3=iHighH, 4=iHighS, 5=iHighV
	private double[] redHSV; //0=iLowH, 1=iLowS, 2=iLowV, 3=iHighH, 4=iHighS, 5=iHighV
	//-------------------------Methods-------------------------//
	

	public frameProcessor(int frontC, int backC) {
		FrontColor = frontC;
		BeckColor =backC;
		blueHSV = new double[6];
		greenHSV = new double[6];
		redHSV = new double[6];
    }
	
	public synchronized void prepareGameSize(int width, int height) {
		Log.i(TAG, "starting prepareGameSize");
        mRgba15 = new Mat(height, width, CvType.CV_8UC4);
        frameWidth = width;
        frameHeight = height;
        Log.i(TAG, "prepareGameSize: frameWidth = " + frameWidth + ", frameHeight = " + frameHeight);
    }
	
	public Mat drawObject(int X, int Y, Mat frame, Scalar ColorObj) {
		Log.i(TAG, "starting drawObject: x = " + X + ", y = " + Y);
		Log.i(TAG, "drawObject: drawing circle");
		Imgproc.circle(frame, new Point(X, Y), 20, ColorObj, 2);
		Log.i(TAG, "drawObject: drawing line");
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
		return frame;
		
	}

	public synchronized boolean TrackingImg(Mat imgOriginal){
		Log.i(TAG, "starting TrackingImg");
		Boolean foundRobo;
		// Track Front
		Log.i(TAG, "TrackingImg: calling TrackingFront(imgOriginal)");
		foundRobo = TrackingFront(imgOriginal);
		Log.i(TAG, "TrackingImg: finished TrackingFront(imgOriginal) = " + foundRobo);
		if (foundRobo) {
			SetXY();
			//drawObject(MiddleX,MiddleY,imgOriginal, new Scalar(0,255,255,0));
			drawObject(FrontX,FrontY,imgOriginal, new Scalar(0,255,255,0));
			Log.i(TAG, "in TrackingImg: FrontX = " + FrontX + ", FrontY = " + FrontY);
			return true;
		}
		return false;
	}
	
	public boolean updateColors(int color, double[] HSVtresholds){
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
		return true;
	}
	
	private synchronized boolean TrackingFront(Mat imgOriginal){
		Log.i(TAG, "starting TrackingFront");
		int i = 0, LeftX = 0, UpY = 0, wide = frameWidth / 5, high = frameHeight / 3;
		double ContourSize = 0;
		Scalar ColorObj;
		Log.i(TAG, "TrackingFront: calling SetColorToTrack");
		ColorObj = SetColorToTrack(FrontColor);
		Mat imgThresholded = new Mat();
		Mat imgHSV  = new Mat();
		Mat imgFrontArea = new Mat();
		Log.i(TAG, "TrackingFront: calling Imgproc.cvtColor");
		Imgproc.cvtColor(imgOriginal, imgHSV, Imgproc.COLOR_BGR2HSV); //Convert the captured frame from BGR to HSV
		Log.i(TAG, "TrackingFront: calling Core.inRange");
		Core.inRange(imgHSV, new Scalar(iLowH, iLowS, iLowV), new Scalar(iHighH, iHighS, iHighV), imgThresholded);
		Log.i(TAG, "TrackingFront: calling DoErode");
		DoErode(imgThresholded, true);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Log.i(TAG, "TrackingFront: calling Imgproc.findContours");
		Imgproc.findContours(imgThresholded, contours, new Mat(),Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);
		Log.i(PrefTAG, "TrackingFront: finished findContours, contours.size() = " + contours.size());
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
				if ((Imgproc.contourArea(contours.get(i)) < 500)) {
					continue;
				}
				FrontX = (int) center.x;
				FrontY = (int) center.y;
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
					return true;
				//}
			}
		}
		return false;
	}

	private synchronized boolean TrackingBeck(Mat imgFrontArea, double frontContourSize, double frontCirclity) {
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
						return true;
					}
				}
			}
		}
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

	private void SetXY() {
		MiddleX = (FrontX + BeckX) / 2;
		MiddleY = (FrontY + BeckY) / 2;

	}

	



}
