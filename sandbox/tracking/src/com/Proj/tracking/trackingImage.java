package com.Proj.tracking;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.*;

import android.os.Bundle;
import android.util.Log;

import org.opencv.core.Rect;

public class trackingImage {
	////////////////////////////////////////////
	private static final int GRID_SIZE = 4;
    private static final int GRID_AREA = GRID_SIZE * GRID_SIZE;
    private static final int GRID_EMPTY_INDEX = GRID_AREA - 1;
    private static final Scalar GRID_EMPTY_COLOR = new Scalar(0x33, 0x33, 0x33, 0xFF);
    ///////////////////////////////////////////
	protected static final String TAG1 = "alon";
	private int FRAME_HEIGHT, FRAME_WIDTH;
	private int FrontX, FrontY, BeckX, BeckY, x ,y;
	private int FrontColor, BeckColor;
	private double iLowH, iLowS, iLowV, iHighH, iHighS, iHighV;
	private Mat mRgba15;
	
	public trackingImage(int fronColor, int beckColor) {
		FrontColor = fronColor;
		BeckColor = beckColor;
	}
	
	public int TrackingImg(Mat imgOriginal){
		Log.i(TAG1, "starting TrackingImg");
		Boolean foundRobo;
		FRAME_HEIGHT = imgOriginal.rows();
		Log.i(TAG1, "in TrackingImg: FRAME_HEIGHT : " + FRAME_HEIGHT);
		FRAME_WIDTH = imgOriginal.cols();
		Log.i(TAG1, "in TrackingImg: FRAME_WIDTH : " + FRAME_WIDTH);
		// Track Front
		Log.i(TAG1, "in TrackingImg: calling TrackingFront(imgOriginal)");
		foundRobo = TrackingFront(imgOriginal);
		Log.i(TAG1, "in TrackingImg: finished TrackingFront(imgOriginal) = " + foundRobo);
		if (foundRobo) {
			SetXY();
			Log.i(TAG1, "in TrackingImg: FrontX = " + FrontX + ", FrontY = " + FrontY);
			return 1;
		}
		return 0;
	}
	
	private void SetXY() {
		x = (FrontX + BeckX) / 2;
		y = (FrontY + BeckY) / 2;
		
	}
	
	public void DoErode(Mat imgThresholded, Boolean enable){
		Log.i(TAG1, "starting DoErode");
		if (enable) {
			Imgproc.erode(imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));
			Imgproc.dilate(imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));

			//morphological closing (fill small holes in the foreground)
			Imgproc.dilate(imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));
			Imgproc.erode(imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));
		}
	}
	
	public Boolean TrackingFront(Mat imgOriginal){
		Log.i(TAG1, "starting TrackingFront");
		int i = 0, LeftX = 0, UpY = 0, wide = FRAME_WIDTH / 5, high = FRAME_HEIGHT / 3;
		double ContourSize = 0;
		Scalar ColorObj;
		Log.i(TAG1, "in TrackingFront: calling SetColorToTrack");
		ColorObj = SetColorToTrack(FrontColor);
		Mat imgThresholded = new Mat();
		Mat imgHSV  = new Mat();
		Mat imgFrontArea = new Mat();
		Log.i(TAG1, "in TrackingFront: calling Imgproc.cvtColor(imgOriginal, imgHSV, Imgproc.COLOR_BGR2HSV)");
		Imgproc.cvtColor(imgOriginal, imgHSV, Imgproc.COLOR_BGR2HSV); //Convert the captured frame from BGR to HSV
		Log.i(TAG1, "TrackingFront: calling Core.inRange(imgHSV, new Scalar(iLowH, iLowS, iLowV), new Scalar(iHighH, iHighS, iHighV), imgThresholded)");
		Core.inRange(imgHSV, new Scalar(iLowH, iLowS, iLowV), new Scalar(iHighH, iHighS, iHighV), imgThresholded);
		Log.i(TAG1, "in TrackingFront: calling DoErode(imgThresholded, true)");
		DoErode(imgThresholded, true);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Log.i(TAG1, "in TrackingFront: calling Imgproc.findContours(imgThresholded, contours, new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE)");
		Imgproc.findContours(imgThresholded, contours, new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
		Log.i(TAG1, "in TrackingFront: finished findContours, contours.size() = " + contours.size());
		if (contours.size() > 0) {
			for (i = 0; i < contours.size(); i++) {
				Log.i(TAG1, "in TrackingFront: in for(i = 0; i < contours.size(); i++): i = " + i);
				ContourSize = Imgproc.contourArea(contours.get(i));
				Log.i(TAG1, "in TrackingFront: ContourSize = " + ContourSize);
				if (ContourSize < 10) {continue;};
				float[] radius = new float[1];
				Point center = new Point();
				Log.i(TAG1, "in TrackingFront: Calling  Imgproc.minEnclosingCircle(new MatOfPoint2f(contours.get(i).toArray()), center, radius)");
				Imgproc.minEnclosingCircle(new MatOfPoint2f(contours.get(i).toArray()), center, radius);
				Log.i(TAG1, "in TrackingFront: finished minEnclosingCircle");
				double temp = Imgproc.arcLength(new MatOfPoint2f(contours.get(i)), true);	
				Log.i(TAG1, "in TrackingFront: finished Imgproc.arcLength = " + temp);
				double Circlity = ((Imgproc.arcLength(new MatOfPoint2f(contours.get(i)), true) / (2 * Math.PI)) / (radius[0]));
				Log.i(TAG1, "in TrackingFront: Circlity = " + Circlity);
				//printf("FrontX = %d, FrontY =%d contourArea=%lf Circlity=%lf\n", FrontX, FrontY, ContourSize, Circlity);
				if ((Circlity < 1.2) & (Circlity > 0.8)) {
					if ((Imgproc.contourArea(contours.get(i)) < 100)) { continue; }
					FrontX = (int) center.x;
					FrontY = (int) center.y;
					Log.i(TAG1, "in TrackingFront: FrontX = " + FrontX + ", FrontY = " + FrontX);
					high = (int) (8 * radius[0]);
					wide = (int) (8 * radius[0]);
					UpY = FrontY - high / 2;
					LeftX = FrontX - wide / 2;
					if (UpY < 0) { UpY = 0; }
					if (LeftX < 0) { LeftX = 0; }
					if (UpY + high > FRAME_HEIGHT) { UpY = FRAME_HEIGHT - high; }
					if (LeftX + wide > FRAME_WIDTH) { LeftX = FRAME_WIDTH - wide; }
					Log.i(TAG1, "in TrackingFront: Calling Rect(LeftX, UpY, wide, high)");
					Rect FrontArea = new Rect(LeftX, UpY, wide, high);
					// Crop the full image to that image contained by the rectangle myROI
					// Note that this doesn't copy the data
					Mat croppedRef = new Mat(imgOriginal, FrontArea);
					// Copy the data into new matrix
					croppedRef.copyTo(imgFrontArea);
					//debug = to_string(i+100);
					//imshow(debug, imgFrontArea);
					Log.i(TAG1, "in TrackingFront: Calling TrackingBeck(imgFrontArea, ContourSize, Circlity)");

					if (TrackingBeck(imgFrontArea, ContourSize, Circlity)) {
						BeckX = BeckX + LeftX;
						BeckY = BeckY + UpY;
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private Scalar SetColorToTrack(int Color) {
		Scalar ColorObj = new Scalar(0, 255, 0);
		switch (Color) {
			//Green
		case 0:
			//iLowH = HSV_G[0];
			//iHighH = HSV_G[1];
			iLowH = 44;
			iHighH = 66;
			iLowS = 0;
			iHighS = 109;
			iLowV = 48;
			iHighV = 120;
			ColorObj = new Scalar(0, 255, 0);
			break;
			//Blue
		case 1:
			//iLowH = HSV_B[0];
			//iHighH = HSV_B[1];
			iLowH = 90;
			iHighH = 125;
			//iLowS = HSV_B[2];
			//iHighS = HSV_B[3];
			iLowS = 90;
			iHighS = 250;
			iLowV = 0;
			iHighV = 250;
			ColorObj = new Scalar(255, 0, 0);
			break;
			//Red
		case 2:
			//iLowH = HSV_R[0];
			//iHighH = HSV_R[1];
			//iLowS = HSV_R[2];
			//iHighS = HSV_R[3];
			//iLowV = HSV_R[4];
			//iHighV = HSV_R[5];
			iLowH = 150;
			iHighH = 180;
			iLowS = 150;
			iHighS = 256;
			iLowV = 0;
			iHighV = 256;
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
	
	public Boolean TrackingBeck(Mat imgFrontArea, double front_contourArea, double CirclityFront){
		Log.i(TAG1, "starting TrackingBeck");
		int i = 0;
		Scalar ColorObj;
		ColorObj = SetColorToTrack(BeckColor);
		Mat imgThresholded = new Mat();
		Mat imgHSV  = new Mat();
		Imgproc.cvtColor(imgFrontArea, imgHSV, Imgproc.COLOR_BGR2HSV); //Convert the captured frame from BGR to HSV
		Core.inRange(imgHSV, new Scalar(iLowH, iLowS, iLowV), new Scalar(iHighH, iHighS, iHighV), imgThresholded);
		DoErode(imgThresholded, true);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(imgThresholded, contours, new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
		if (contours.size() > 0) {
			for (i = 0; i < contours.size(); i++) {
				double contour_size = Imgproc.contourArea(contours.get(i));
				//printf("i = %d contourArea=%lf\n",i ,Imgproc.contourArea(contours.get(i)));
				if ((contour_size > front_contourArea - 100) & (contour_size < front_contourArea + 100)) {
					float[] radius = new float[1];
					Point center = new Point();
					Imgproc.minEnclosingCircle(new MatOfPoint2f(contours.get(i).toArray()), center, radius);
					double Circlity = ((Imgproc.arcLength(new MatOfPoint2f(contours.get(i)), true) / (2 * Math.PI)) / (radius[0]));
					//printf("Beck Circlity=%lf\n", Circlity);
					if ((Circlity < CirclityFront + 0.1) & (Circlity > CirclityFront - 0.1)) {
						BeckX = (int) center.x;
						BeckY = (int) center.y;
						drawObject(BeckX, BeckY, imgFrontArea, new Scalar(0, 0, 255));
						return true;
					}
				}
			}
		}
		return false; 
	}
	
	public Mat drawObject(int X, int Y, Mat frame, Scalar ColorObj) {
		Log.i(TAG1, "starting drawObject: x = " + X + ", y = " + Y);
		Log.i(TAG1, "in drawObject: drawing circle");
		Imgproc.circle(frame, new Point(X, Y), 20, ColorObj, 2);
		Log.i(TAG1, "in drawObject: drawing line");
		//Imgproc.line(frame, new Point(BeckX, BeckY), new Point(FrontX, FrontY), new Scalar(0,0,255), 2);
		//Imgproc.circle(frame, new Point(FrontX, FrontY), 5, new Scalar(0, 0, 255), 2);
		if (Y - 25>0)
			Imgproc.line(frame, new Point(X, Y), new Point(X, Y - 25), ColorObj, 2);
		else Imgproc.line(frame, new Point(X, Y), new Point(X, 0), ColorObj, 2);
		if (Y + 25<FRAME_HEIGHT)
			Imgproc.line(frame, new Point(X, Y), new Point(X, Y + 25), ColorObj, 2);
		else Imgproc.line(frame, new Point(X, Y), new Point(X, FRAME_HEIGHT), ColorObj, 2);
		if (X - 25>0)
			Imgproc.line(frame, new Point(X, Y), new Point(X - 25, Y), ColorObj, 2);
		else Imgproc.line(frame, new Point(X, Y), new Point(0, Y), ColorObj, 2);
		if (X + 25<FRAME_WIDTH)
			Imgproc.line(frame, new Point(X, Y), new Point(X + 25, Y), ColorObj, 2);
		else Imgproc.line(frame, new Point(X, Y), new Point(FRAME_WIDTH, Y), ColorObj, 2);

		Imgproc.putText(frame, X + "," + Y, new Point(X, Y + 30), 1, 1, ColorObj, 2);
		return frame;
		
	}
	
	public Mat getDrawObject(Mat frame) {
		Log.i(TAG1, "in getDrawObject: calling TrackingImg(frame)");
		if (TrackingImg(frame) == 0) { 
			drawObject(FRAME_WIDTH/2,FRAME_HEIGHT/2,frame,new Scalar(0,0,255));
			return frame;
		};
		Log.i(TAG1, "in getDrawObject: drawObject(x,y,frame,new Scalar(0,0,255))");
		drawObject(x,y,frame,new Scalar(0,0,255));
		return frame;
		
	}
	
	public synchronized void prepareGameSize(int width, int height) {
        mRgba15 = new Mat(height, width, CvType.CV_8UC4);
    }
	
	public Mat draw(Mat drawMat) {      
		Imgproc.line(drawMat, new Point(10, 10), new Point(20,20), new Scalar(0, 255, 0, 255), 3);
		Imgproc.line(drawMat, new Point(20, 10), new Point(10,20), new Scalar(0, 255, 0, 255), 3);
		return drawMat;
	}
}
