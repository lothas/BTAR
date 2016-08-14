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
	private static final int GOUST_COUNTER = 3;
	//-------------------------Fields--------------------------//
	private boolean mLaserSwitch = false, mLaserWarningSwitch = false;
	private boolean flagFound = false;
	private boolean flag = true;
	private boolean flagBack = true;
	private int FrontColor, BackColor;
	private int frameHeight, frameWidth;
	private int tempFrontX, tempFrontY, tempBackX, tempBackY; //every iteration we find temp points and in end we filter them to get the wanted points
	private int FrontX, FrontY, BackX, BackY, MiddleX ,MiddleY;
	private int frontGoustX, frontGoustY, backGoustX, backGoustY, goustCounter;
    private double iLowH, iLowS, iLowV, iHighH, iHighS, iHighV;
	private double[] blueHSV; //0=iLowH, 1=iLowS, 2=iLowV, 3=iHighH, 4=iHighS, 5=iHighV
	private double[] greenHSV; //0=iLowH, 1=iLowS, 2=iLowV, 3=iHighH, 4=iHighS, 5=iHighV
	private double[] redHSV; //0=iLowH, 1=iLowS, 2=iLowV, 3=iHighH, 4=iHighS, 5=iHighV
	private long endTime, startTime; //for performance check purpose 
	private int resizeFactor;
	//----------------------filterFilds------------------------//
	//we use a filter on the back and front of the object so the object will change more smoothly
	//it will use the average of FILTER_LENGTH - 1 last places the object was plus the new data
	private double[] frontFilterX, frontFilterY;
	private double[] backFilterX, backFilterY;
	private static final int FILTER_LENGTH = 3; //Here we set the filter level
	
	//----------------------coinFilds--------------------------//
	private double mCoinAngleFromXAxis;
	private boolean mHasCoin;
	private double mRadiosFromMiddle;
	private float mCoinAngleInDegAroundItSelf;
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

    
    public boolean trackObject(List<MatOfPoint> frontContours, List<MatOfPoint> backContoursLeft, List<MatOfPoint> backContoursRight){
     	
    	//------------------------------------------------------
        double maxArea = frameHeight*frameWidth / (resizeFactor * resizeFactor);
        double MinContourArea = 0.0001; //TODO find optimal MinContourArea and MaxContourArea
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
				tempFrontX = ((int) center.x);
				tempFrontY = ((int) center.y);
				//--------------------------------------------
				//Log.i(NEWTAG1, "radios = " + Math.sqrt(radius[0]*radius[0] + radius[1]*radius[1]));
				Point centerLeft = new Point(-1,-1);
				Point centerRight = new Point(-1,-1);
				centerLeft = trackObjectBack(radius[0], backContoursLeft, counterSize,centerLeft);
				if ((centerLeft.x == -1) && (centerLeft.y == -1)){
					continue;
				}
				centerRight = trackObjectBack(radius[0], backContoursRight, counterSize,centerLeft);
				if ((centerRight.x == -1) && (centerRight.y == -1)){
					continue;
				}
				if (BackdistanceCheck(radius[0],centerLeft, centerRight) == true) {
					//set filtered front and back
					filterXY(centerLeft, centerRight);//filter the centers and sets XY
					flagFound = true;
					return true;
				}
            }
        
        }
        //set filtered front back also set flagFound
        Point trmpPoint = new Point(-1,-1);
        tempFrontX = -1; //Because we did not find the object; 
        tempFrontY = -1;
        flagFound = filterXY(trmpPoint,trmpPoint);
		return flagFound;    	
    }
    
	public Point trackObjectBack(float frontRadius, List<MatOfPoint> backContours, double frontCounterSize, Point otherCenter){
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
				if ((center.x == otherCenter.x) && (center.y == otherCenter.y)){ //if center == otherCenter its the same point so lock for another 
					continue;
				}
				float distanceFromFront = getdistanceFromTempFront(center); // FrontX, FrontY  
				//--------------------------------------------
				double allowedSistanceFactor = 2.5; //TODO faind optimal
            	if (distanceFromFront < allowedSistanceFactor * frontRadius){
            		return center;
            	}               
            }
    	}
    	Point falsePoint = new Point(-1,-1);
    	return falsePoint;
    }
	
	private float getdistanceFromTempFront(Point center) {
    	return (float)Math.sqrt((double)((tempFrontX - (int)center.x)*(tempFrontX - (int)center.x) + (tempFrontY - (int)center.y)*(tempFrontY - (int)center.y)));
	}
    
    //private float getdistanceFromFront(Point center) {
    //	return (float)Math.sqrt((double)((FrontX - (int)center.x)*(FrontX - (int)center.x) + (FrontY - (int)center.y)*(FrontY - (int)center.y)));
	//}
    
    private boolean BackdistanceCheck(float frontRadius, Point centerLeft, Point centerRight) {
    	float distanceBack = (float)Math.sqrt((double)((centerRight.x - (int)centerLeft.x)*(centerRight.x - (int)centerLeft.x) 
    			+ (centerRight.y - (int)centerLeft.y)*(centerRight.y - (int)centerLeft.y)));
    	double allowedSistanceFactor = 2.5; //TODO faind optimal
    	if (distanceBack < allowedSistanceFactor * frontRadius){
    		return true;
    	}  
		return false;
	}
    
    private void SetTempBackXY(Point centerLeft, Point centerRight) {
    	tempBackX = (int)((centerLeft.x + centerRight.x) / 2);
    	tempBackY = (int)((centerLeft.y + centerRight.y) / 2);
    }
    
    //private void SetBackXY(Point centerLeft, Point centerRight) {
    //	BackX = (int)((centerLeft.x + centerRight.x) / 2);
    //	BackY = (int)((centerLeft.y + centerRight.y) / 2);
    //}

	private void SetTempXYFactor(int factor) {
		tempFrontX = tempFrontX * factor;
		tempFrontY = tempFrontY * factor;
		tempBackX  = tempBackX  * factor;
		tempBackY  = tempBackY  * factor;
		if(tempFrontX < 0){
			tempFrontX = -1;
			tempFrontY = -1;
			tempBackX  = -1;
			tempBackY  = -1;
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
    	goustCounter = 0;
    	prepareFilter();
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
    
    //----------------------filterMethods------------------------//
    private boolean filterXY(Point centerLeft, Point centerRight){
    	SetTempBackXY(centerLeft, centerRight);
		SetTempXYFactor(resizeFactor);
		if(SetFilterdXY() == false){//we did not find the object in all the last iterations
			return tryGoust();
		}
		SetXY();
		setGoust();
    	return true;
    }
    
    private void setGoust() {
    	goustCounter = GOUST_COUNTER;
    	frontGoustX = FrontX;
    	backGoustX  = BackX;
    	frontGoustY = FrontY;
    	backGoustY  = BackY;
		
	}

	private boolean tryGoust() {
    	if (goustCounter == 0){
    		FrontX =  -1;
        	BackX  =  -1;
        	FrontY =  -1;
        	BackX  =  -1;
    		return false;
    	}
    	goustCounter--;
    	FrontX = frontGoustX;
    	BackX  =  backGoustX;
    	FrontY = frontGoustY;
    	BackY  =  backGoustY;
    	SetXY(); 
    	return true;
	}

	private boolean SetFrontAndBack(){
    	FrontX = (int)arryAvrage(frontFilterX,FILTER_LENGTH);
    	FrontY = (int)arryAvrage(frontFilterY,FILTER_LENGTH);
    	BackX = (int)arryAvrage(backFilterX,FILTER_LENGTH);
    	BackY = (int)arryAvrage(backFilterY,FILTER_LENGTH);
    	if(FrontX == -1){
    		return false;
    	}
    	return true;
    }
    
    private boolean SetFilterdXY(){
    	shiftArryAndAddNewData(frontFilterX,tempFrontX,FILTER_LENGTH);
    	shiftArryAndAddNewData(frontFilterY,tempFrontY,FILTER_LENGTH);
    	shiftArryAndAddNewData(backFilterX,tempBackX,FILTER_LENGTH);
    	shiftArryAndAddNewData(backFilterY,tempBackY,FILTER_LENGTH);
    	return SetFrontAndBack();
    }
    
    public void shiftArryAndAddNewData(double arry[],double newData, int arrySize){
    	for(int i = 0; i < arrySize - 1; i++){
    		arry[arrySize - 1 - i] = arry[arrySize - 2 - i];
    	}
    	arry[0] = newData;
    }
    
    public double arryAvrage(double arry[], int arrySize){
    	double sumOfElements = 0;
    	int numOfElements = 0; 
    	for(int i = 0; i < arrySize; i++){
    		if(arry[i] != -1){
    			sumOfElements = sumOfElements + arry[i];
    			numOfElements++;
    		}
    	}
    	if(numOfElements == 0){
    		return -1;
    	}
    	return (sumOfElements/numOfElements);
    }
    
    public void prepareFilter(){
    	frontFilterX = new double[FILTER_LENGTH];
    	frontFilterY = new double[FILTER_LENGTH];
    	backFilterX = new double[FILTER_LENGTH];
    	backFilterY = new double[FILTER_LENGTH];
    	for(int i = 0; i < FILTER_LENGTH; i++){
    		frontFilterX[i] = -1; //-1 means we didn't find the center of the object (at the beginning we don't have an object)
    		frontFilterY[i] = -1;
    		backFilterX[i]  = -1;
    		backFilterY[i]  = -1;
    	}
    }
    //---------------------------------------------------------//
    
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
	
	public float getFront2BackLengthInRealWorld(double rollAngle) {
		rollAngle = findRealAngle(rollAngle, MiddleY);
		double lengthX = (double)(FrontX - BackX);
		if (rollAngle > 65){
			rollAngle = 65;
		}
		double lengthY = ((double)(FrontY - BackY)) / (Math.abs(Math.cos(Math.toRadians(rollAngle))));  
		return (float)Math.sqrt((double)(lengthX*lengthX + lengthY*lengthY));
	}
	
	private double findRealAngle(double rollAngle, int middleY2) {
		int yFactor = ( middleY2 - frameHeight/2)/ frameHeight;
		rollAngle = rollAngle - 1.1*rollAngle*yFactor;
		return rollAngle;
		
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
        //Imgproc.dilate(mMask, mDilatedMask, new Mat()); //closes black holes in the mMask (binary image)
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>(); //setting up contours list
        //Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE); //finding the contours 
        Imgproc.findContours(mMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE); //finding the contours 
        //TODO check which method is the quickest (Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
        //TODO check mDilatedMask Imgproc.dilate(mInput, mInput, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));
    	
    	return contours;
    	
    }
	
	public void setCoin(boolean setCoin){
		if (setCoin == false){
			mHasCoin = false;
			return;
		}
		mHasCoin = true;
		mCoinAngleInDegAroundItSelf = 0;
		mCoinAngleFromXAxis = 2*Math.PI*mRandomGenerator.nextDouble();
	}
	
	public boolean hasCoin(){
		return mHasCoin;
	}
	
	public int getCoinX(){
		mRadiosFromMiddle = 4; //TODO randomize and set only 1 getAngleFromXAxis
		return (int) (mRadiosFromMiddle*getFront2BackLength()*Math.sin(mCoinAngleFromXAxis + getAngleFromXAxisInRadians())) + MiddleX;
	}
	
	public int getCoinY(){
		mRadiosFromMiddle = 4; //TODO randomize and set only 1
		return (int) (mRadiosFromMiddle*getFront2BackLength()*Math.cos(mCoinAngleFromXAxis + + getAngleFromXAxisInRadians())) + MiddleY;
	}
	
	public void updateCoinAngle(){
		mCoinAngleInDegAroundItSelf++;
	}
	
	public float getCoinAngleInDegAroundItSelf(){
		return mCoinAngleInDegAroundItSelf;
	}
	
	public void setLaser(boolean hasLaser){
		mLaserSwitch = hasLaser;
	}
	
	public boolean hasLaser(){
		return mLaserSwitch;
	}
	
	public void setLaserWarning(boolean hasLaserWarning){
		mLaserWarningSwitch = hasLaserWarning;
	}
	
	public boolean hasLaserWarning(){
		return mLaserWarningSwitch;
	}
	
	public void drawLaserLine(Mat frame, int xminus,int yminus,int xplus,int yplus){//debug
		Imgproc.line(frame, new Point(xminus, yminus), new Point(xplus, yplus), new Scalar(0,255,100), 2);
	}
	
	public void drawHitCirclel(Mat frame, Double rollAngle, double radiusFactor){
		Imgproc.circle(frame, new Point(MiddleX, MiddleY), (int)(radiusFactor*getFront2BackLengthInRealWorld(rollAngle)), new Scalar(255, 0, 0), 5);
	}
	
}
