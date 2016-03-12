package org.opencv.samples.colorblobdetect;

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
import org.opencv.imgproc.Imgproc;

import android.util.Log;

public class ColorBlobDetector {
    // Lower and Upper bounds for range checking in HSV color space
    private Scalar mLowerBound = new Scalar(0);
    private Scalar mUpperBound = new Scalar(0);
    // Minimum contour area in percent for contours filtering
    private static double mMinContourArea = 0.1;
    // Color radius for range checking in HSV color space
    private Scalar mColorRadius = new Scalar(25,50,50,0);
    private Mat mSpectrum = new Mat();
    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();

    // Cache
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
    
    //------------------------------------------
    protected static final String TAG = "FP";
    protected static final String TAG1 = "FP1";
    protected static final String TAGB = "FPB";
    protected static final String TAGF = "FPF";
    
    private static final int NO_COLOR = -1;
	private static final int BLUE_COLOR = 1;
	private static final int GREEN_COLOR = 2;
	private static final int RED_COLOR = 3;
    
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
    //------------------------------------------

    public void setColorRadius(Scalar radius) {
        mColorRadius = radius;
    }

    public void setHsvColor(Scalar hsvColor) {
        double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0]-mColorRadius.val[0] : 0;
        double maxH = (hsvColor.val[0]+mColorRadius.val[0] <= 255) ? hsvColor.val[0]+mColorRadius.val[0] : 255;

        mLowerBound.val[0] = minH;
        mUpperBound.val[0] = maxH;

        mLowerBound.val[1] = hsvColor.val[1] - mColorRadius.val[1];
        mUpperBound.val[1] = hsvColor.val[1] + mColorRadius.val[1];

        mLowerBound.val[2] = hsvColor.val[2] - mColorRadius.val[2];
        mUpperBound.val[2] = hsvColor.val[2] + mColorRadius.val[2];

        mLowerBound.val[3] = 0;
        mUpperBound.val[3] = 255;

        Mat spectrumHsv = new Mat(1, (int)(maxH-minH), CvType.CV_8UC3);

        for (int j = 0; j < maxH-minH; j++) {
            byte[] tmp = {(byte)(minH+j), (byte)255, (byte)255};
            spectrumHsv.put(0, j, tmp);
        }

        Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
    }

    public Mat getSpectrum() {
        return mSpectrum;
    }

    public void setMinContourArea(double area) {
        mMinContourArea = area;
    }

    public void process(Mat rgbaImage) {
        Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);

        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);
        

        if (flag == true){
        	flag = false;
        	Log.i(TAG, "iLowH = " + mLowerBound.val[0] + " iHighH = " + mUpperBound.val[0]);
        	Log.i(TAG, "iLowS = " + mLowerBound.val[1] + " iHighS = " + mUpperBound.val[1]);
        	Log.i(TAG, "iLowV = " + mLowerBound.val[2] + " iHighV = " + mUpperBound.val[2]);
        }
        
        Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);
        Imgproc.dilate(mMask, mDilatedMask, new Mat());

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea)
                maxArea = area;
        }

        // Filter contours by area and resize to fit the original image size
        mContours.clear();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea*maxArea) {
                Core.multiply(contour, new Scalar(4,4), contour);
                mContours.add(contour);
                
                
                float[] radius = new float[1];
                Point center = new Point();
        		Imgproc.minEnclosingCircle(new MatOfPoint2f(contour.toArray()), center, radius);
        		FrontX = ((int) center.x);
        		FrontY = ((int) center.y);
            }
        }
    }
    
    public boolean trackFront(Mat rgbaImage) {
    	int high, wide, UpY, LeftX;
        //Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        //Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);
        //int PyrFrameHight = mPyrDownMat.rows() - 4;
        //int PyrFrameWight = mPyrDownMat.cols() - 4;
    	int PyrFrameHight = rgbaImage.rows() - 4;
        int PyrFrameWight = rgbaImage.cols() - 4;
        SetColorToTrack(FrontColor);
        Imgproc.cvtColor(rgbaImage, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);
        //Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

        if (flag == true){
        	flag = false;
        	Log.i(TAG, "iLowH = " + iLowH + " iHighH = " + iHighH);
        	Log.i(TAG, "iLowS = " + iLowS + " iHighS = " + iHighS);
        	Log.i(TAG, "iLowV = " + iLowV + " iHighV = " + iHighV);
        }
        Core.inRange(mHsvMat, new Scalar(iLowH, iLowS, iLowV), new Scalar(iHighH, iHighS, iHighV), mMask);
        Imgproc.dilate(mMask, mDilatedMask, new Mat());

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea)
                maxArea = area;
        }

        // Filter contours by area and resize to fit the original image size
        mContours.clear();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea*maxArea) {
            	
            	float[] radius = new float[1];
				Point center = new Point();
				Imgproc.minEnclosingCircle(new MatOfPoint2f(contour.toArray()), center, radius);
				FrontX = ((int) center.x);
				FrontY = ((int) center.y);
				
                Core.multiply(contour, new Scalar(4,4), contour);
				mContours.add(contour);

				//------------------
				
				
				high = (int) (4 * radius[0]);
				wide = (int) (4 * radius[0]);
				UpY = FrontY - high / 2;
				LeftX = FrontX - wide / 2;
				FrontX = FrontX * 4;
				FrontY = FrontY * 4;
				if (UpY < 0) {
					UpY = 0;
				}
				if (LeftX < 0) {
					LeftX = 0;
				}
				if (UpY + high > PyrFrameHight) {
					high = PyrFrameHight - UpY;
				}
				if (LeftX + wide > PyrFrameWight) {
					wide = PyrFrameWight - LeftX;
				}
				Log.i(TAG, "UpY = " + UpY + ", LeftX = " + LeftX);
				Log.i(TAG, "high = " + high + ", wide = " + wide);
				Rect FrontArea = new Rect(LeftX, UpY, wide, high);
				//Mat croppedRef = new Mat(mPyrDownMat, FrontArea);
				Mat croppedRef = new Mat(rgbaImage, FrontArea);
				//------------------
				
				if (trackBack(croppedRef) == true) {
					BackX = (BackX + LeftX) * 4;
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
    
    public boolean trackBack(Mat rgbaImage) {
    	//Log.i(TAG, "trackBack");
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

        // Find max contour area
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
			double area = Imgproc.contourArea(contour);
			if (area > 5){
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

    public List<MatOfPoint> getContours() {
        return mContours;
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
    
    public synchronized void prepareGame(int width, int height, int frontCol, int beckCol) {
        frameWidth = width;
        frameHeight = height;
        FrontColor =  frontCol;
        BackColor = beckCol;
        blueHSV = new double[6];
        greenHSV = new double[6];
        redHSV = new double[6];
      
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
	
	private void SetXY() {
		MiddleX = (FrontX + BackX) / 2;
		MiddleY = (FrontY + BackY) / 2;

	}
}
