package org.opencv.samples.colorblobdetect;

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
	protected static final String TAG = "insertColor";
	
	private Mat mRgba15, imgHSV, imgThresholded;
	private double hMax, sMax, vMax, hMin, sMin, vMin;
	private int color, frameWidth, frameHeight;	
	//-------------------------Methods-------------------------//
	public insertColor(int Color) {
		hMax = 0;
		sMax = 0;
		vMax = 0;
		hMin = 255;
		sMin = 255;
		vMin = 255;
		color = Color;
	}
	
	public void prepareGameSize(int width, int height) {
        mRgba15 = new Mat(height, width, CvType.CV_8UC4);
        imgHSV = new Mat(height, width, CvType.CV_8UC4);
        imgThresholded = new Mat(height, width, CvType.CV_8UC4);
        frameWidth = width;
        frameHeight = height;
    }
	
	public void updateFrame(Mat frame){
		frame.copyTo(mRgba15);
	}
	
	public boolean deliverTouchEvent(int x, int y) {
		Imgproc.cvtColor(mRgba15, imgHSV, Imgproc.COLOR_RGB2HSV_FULL);
		int i, j;
		for (i = x - 25; i < x + 25; i++){
			for (j = y - 25; j < y + 25; j++){
				if (imgHSV.get(j,i)[0] > hMax){
					hMax = (imgHSV.get(j,i)[0] + 15 < 255 ? imgHSV.get(j,i)[0] + 15 : 255);
				}
				if (imgHSV.get(j,i)[1] > sMax){
					sMax = (imgHSV.get(j,i)[1] + 70 < 255 ? imgHSV.get(j,i)[1] + 70 : 255);
				}
				if (imgHSV.get(j,i)[2] > vMax){
					vMax = (imgHSV.get(j,i)[2] + 60 < 255 ? imgHSV.get(j,i)[2] + 60 : 255);
					vMax = 255; // lighet dosent meeter, TODO reviw
				}
				if (imgHSV.get(j,i)[0] < hMin){
					hMin = (imgHSV.get(j,i)[0] - 15 > 0 ? imgHSV.get(j,i)[0] - 15 : 0);
				}
				if (imgHSV.get(j,i)[1] < sMin){
					sMin = (imgHSV.get(j,i)[1] - 70 > 0 ? imgHSV.get(j,i)[1] - 70 : 0);
				}
				if (imgHSV.get(j,i)[2] < vMin){
					vMin = (imgHSV.get(j,i)[2] - 60 > 0 ? imgHSV.get(j,i)[2] - 60 : 0);
					vMin = 0; // lighet dosent meeter, TODO reviw
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
		return true;
	}

	private void drawCounter(int x, int y) {
		int i;	
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Core.inRange(imgHSV, new Scalar(hMin, sMin, vMin), new Scalar(hMax, sMax, vMax), imgThresholded);		
		Imgproc.findContours(imgThresholded, contours, new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
		for (i = 0; i < contours.size(); i++){
			if (Imgproc.pointPolygonTest(new MatOfPoint2f(contours.get(i).toArray()),new Point(x,y), false) == 1){ // find the nireest counter to chosen Point
				Imgproc.drawContours(mRgba15, contours, i, new Scalar(0, 0, 255), -1); // fill the chosen counter with color (daffined by Scalar) 
				break;
			}
		}
	}
	
	public Mat getMat(){
		return mRgba15;	
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

