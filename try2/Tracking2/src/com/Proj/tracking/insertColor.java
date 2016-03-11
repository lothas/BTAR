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
import org.opencv.imgproc.Imgproc;

import android.util.Log;

public class insertColor {
	//-------------------------Fields--------------------------//
	protected static final String TAG = "IC";
	protected static final String SPEED_TAG = "speedInsertColor: debug";
	protected static final String TIMING_TAG = "Timing";
	
	private Mat mRgba15, imgHSV, imgThresholded;
	private double hMax, sMax, vMax, hMin, sMin, vMin;
	private int color, frameWidth, frameHeight;
	
	//-------------------------Methods-------------------------//
	public insertColor(int Color) {
		Log.i(TAG, "constarcing insertColor with: " + Color);
		hMax = 0;
		sMax = 0;
		vMax = 0;
		hMin = 255;
		sMin = 255;
		vMin = 255;
		color = Color;
		Log.i(TAG, "exiting insertColor Constractot");
	}
	
	public synchronized void prepareGameSize(int width, int height) {
		Log.i(TAG, "starting prepareGameSize");
        mRgba15 = new Mat(height, width, CvType.CV_8UC4);
        imgHSV = new Mat(height, width, CvType.CV_8UC4);
        imgThresholded = new Mat(height, width, CvType.CV_8UC4);
        frameWidth = width;
        frameHeight = height;
        Log.i(TAG, "prepareGameSize: frameWidth = " + frameWidth + ", frameHeight = " + frameHeight);
    }
	
	public void updateFrame(Mat frame){
		Log.i(SPEED_TAG, "starting updateFrame");
		frame.copyTo(mRgba15);
		Imgproc.cvtColor(mRgba15, imgHSV, Imgproc.COLOR_BGR2HSV);
		Log.i(SPEED_TAG, "exiting updateFrame");
	}
	
	public boolean deliverTouchEvent(int x, int y) {
		Log.i(TAG, "starting deliverTouchEvent");
		int i, j;
		for (i = x - 25; i < x + 25; i++){
			for (j = y - 25; j < y + 25; j++){
				if (imgHSV.get(j,i)[0] > hMax){
					hMax = (imgHSV.get(j,i)[0] + 15 < 255 ? imgHSV.get(j,i)[0] + 15 : 255);
				}
				if (imgHSV.get(j,i)[1] > sMax){
					sMax = (imgHSV.get(j,i)[1] + 15 < 255 ? imgHSV.get(j,i)[1] + 15 : 255);
				}
				if (imgHSV.get(j,i)[2] > vMax){
					vMax = (imgHSV.get(j,i)[2] + 15 < 255 ? imgHSV.get(j,i)[2] + 15 : 255);
				}
				if (imgHSV.get(j,i)[0] < hMin){
					hMin = (imgHSV.get(j,i)[0] - 15 > 0 ? imgHSV.get(j,i)[0] - 15 : 0);
				}
				if (imgHSV.get(j,i)[1] < sMin){
					sMin = (imgHSV.get(j,i)[1] - 15 > 0 ? imgHSV.get(j,i)[1] - 15 : 0);
				}
				if (imgHSV.get(j,i)[2] < vMin){
					vMin = (imgHSV.get(j,i)[2] - 15 > 0 ? imgHSV.get(j,i)[2] - 15 : 0);
				}
				Log.i(TAG, "deliverTouchEvent: ");
				Log.i(TAG, "hMax : " + hMax);
				Log.i(TAG, "sMax : " + sMax);
				Log.i(TAG, "vMax : " + vMax);
				Log.i(TAG, "hMin : " + hMin);
				Log.i(TAG, "sMin : " + sMin);
				Log.i(TAG, "vMin : " + vMin);
			}
		}
		drawCounter(x, y);
		Log.i(TAG, "exiting deliverTouchEvent");
		return true;
	}

	private void drawCounter(int x, int y) {
		Long tsLong0 = System.currentTimeMillis();
		String ts0 = tsLong0.toString();
		Log.i(TIMING_TAG, "start drawCounter, time: " + ts0);
		int i;
		//Mat imgThresholded = new Mat();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Core.inRange(imgHSV, new Scalar(hMin, sMin, vMin), new Scalar(hMax, sMax, vMax), imgThresholded);
		//Core.inRange(imgHSV, new Scalar(0, 0, 0), new Scalar(50, 255, 255), imgThresholded);
		Long findContoursStart = System.currentTimeMillis();
		Imgproc.findContours(imgThresholded, contours, new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
		Long findContoursEnd = System.currentTimeMillis();
		Log.i(TIMING_TAG, "findContours: delta " + (findContoursEnd-findContoursStart));
		for (i = 0; i < contours.size(); i++){
			if (Imgproc.pointPolygonTest(new MatOfPoint2f(contours.get(i).toArray()),new Point(x,y), false) == 1){
				Imgproc.drawContours(mRgba15, contours, i, new Scalar(0, 0, 255), -1);
			}
		}
		Long tsLong1 = System.currentTimeMillis();
		ts0 = tsLong1.toString();
		Log.i(TIMING_TAG, "exiting drawCounter, time: " + ts0);
		tsLong1 = tsLong1 - tsLong0;
		ts0 = tsLong1.toString();
		Log.i(TIMING_TAG, "drawCounter: delta " + ts0);
	}
	
	public Mat getMat(){
		Log.i(SPEED_TAG, "getMat");
		return mRgba15;
		//return imgHSV;
		//return imgThresholded;
	}

	public double[] getHSVarr(){
		double[] HSVarr = new double[6];
		HSVarr[0]= hMin;
		HSVarr[1]= sMin;
		HSVarr[2]= vMin;
		HSVarr[3]= hMax;
		HSVarr[4]= sMax;
		HSVarr[5]= vMax;
		return HSVarr;
		
	}
}
