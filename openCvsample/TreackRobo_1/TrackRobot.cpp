#include "TrackRobot.h"
#include <iostream>
#include <math.h>
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
using namespace cv;
using namespace std;



TrackRobot::TrackRobot(int FColor, int BColor)
{
	FrontColor = FColor;
	BeckColor = BColor;
}


TrackRobot::~TrackRobot()
{
}

int TrackRobot::TrackingImg(Mat imgOriginal)
{
	FRAME_HEIGHT = imgOriginal.rows;
	FRAME_WIDTH = imgOriginal.cols;
	Mat imgThresholded, imgHSV;
	// Treck Front
	TrackingFront(imgOriginal);
	SetXY();
	printf("FrontX = %d, FrontY =%d", FrontX, FrontY);
	drawObject(BeckX, BeckY, imgOriginal, Scalar(0, 0, 255));
	drawObject(FrontX, FrontY, imgOriginal, Scalar(0, 255, 0));
	drawObject(x , y , imgOriginal, Scalar(255, 0, 0));
	return 1;
}

void TrackRobot::TrackingFront(Mat imgOriginal)
{
	printf("alon1");
	int i = 0, LeftX = 0, UpY = 0, wide = FRAME_WIDTH / 5, high = FRAME_HEIGHT / 3;
	double ContourSize = 0;
	Mat imgFrontArea;
	Scalar ColorObj;
	ColorObj = SetColorToTrack(FrontColor);
	Mat imgThresholded, imgHSV;
	cvtColor(imgOriginal, imgHSV, COLOR_BGR2HSV); //Convert the captured frame from BGR to HSV
	inRange(imgHSV, Scalar(iLowH, iLowS, iLowV), Scalar(iHighH, iHighS, iHighV), imgThresholded);
	DoErode(imgThresholded, true);
	printf("alon2");
	vector< vector<Point> > contours;
	//vector<Vec4i> hierarchy;
	//find contours of filtered image using openCV findContours function
	findContours(imgThresholded, contours, CV_RETR_CCOMP, CV_CHAIN_APPROX_NONE);
	printf("alon3");
	if (contours.size() > 0) {
		for (i = 0; i < contours.size(); i++) {
			ContourSize = contourArea(contours[i]);
			Moments moment = moments((cv::Mat)contours[i]);
			if ((moment.m00 < 10) | (moment.m00 > 4000)) { continue; }
			FrontX = moment.m10 / moment.m00;
			FrontY = moment.m01 / moment.m00;
			UpY = FrontY - high / 2;
			LeftX = FrontX - wide / 2;
			if (UpY < 0) { UpY = 0; }
			if (LeftX < 0) { LeftX = 0; }
			if (UpY + high > FRAME_HEIGHT) { UpY = FRAME_HEIGHT - high; }
			if (LeftX + wide > FRAME_WIDTH) { LeftX = FRAME_WIDTH - wide; }
			Rect FrontArea(LeftX, UpY, wide, high);
			// Crop the full image to that image contained by the rectangle myROI
			// Note that this doesn't copy the data
			Mat croppedRef(imgOriginal, FrontArea);
			Mat imgFrontArea;
			// Copy the data into new matrix
			croppedRef.copyTo(imgFrontArea);
			if (i == 0) {
			imshow("imgFrontArea1", imgFrontArea);
			}
			if (waitKey(100000) == 27) //wait for 'esc' key press for 30ms. If 'esc' key is pressed, break loop
			{
				cout << "esc key is pressed by user" << endl;
			}
			vector<Point2f>center(1);
			vector<float>radius(1);
			minEnclosingCircle((Mat)contours[i], center[0], radius[0]);
			double Circlity = ((arcLength(contours[i],true)/(2*PI))/ (radius[0]));
			printf("FrontX = %d, FrontY =%d contourArea=%lf Circlity=%lf\n", FrontX, FrontY, ContourSize, Circlity);
			if (TrackingBeck(imgFrontArea, ContourSize , Circlity)){
				BeckX = BeckX + LeftX;
				BeckY = BeckY + UpY;
				break;
			}
		}
	}

}

bool TrackRobot::TrackingBeck(Mat imgFrontArea, double front_contourArea, double CirclityFront)
{
	int i = 0;
	Scalar ColorObj;
	ColorObj = SetColorToTrack(BeckColor);
	Mat imgThresholded, imgHSV;
	cvtColor(imgFrontArea, imgHSV, COLOR_BGR2HSV); //Convert the captured frame from BGR to HSV
	inRange(imgHSV, Scalar(iLowH, iLowS, iLowV), Scalar(iHighH, iHighS, iHighV), imgThresholded);
	DoErode(imgThresholded, true);
	vector< vector<Point> > contours;
	//vector<Vec4i> hierarchy;
	//find contours of filtered image using openCV findContours function
	findContours(imgThresholded, contours, CV_RETR_CCOMP, CV_CHAIN_APPROX_NONE);
	if (contours.size() > 0) {
		for (i = 0; i < contours.size(); i++) {
			double contour_size = contourArea(contours[i]);
			printf("i = %d contourArea=%lf\n",i ,contourArea(contours[i]));
			Moments moment = moments((cv::Mat)contours[i]);
			if ((contour_size > front_contourArea - 200) & (contour_size < front_contourArea + 200)) {
				vector<Point2f>center(1);
				vector<float>radius(1);
				minEnclosingCircle((Mat)contours[i], center[0], radius[0]);
				double Circlity = ((arcLength(contours[i], true) / (2 * PI)) / (radius[0]));
				printf("Beck Circlity=%lf\n", Circlity);
				if ((Circlity < CirclityFront + 0.1) & (Circlity > CirclityFront - 0.1)) {
					BeckX = moment.m10 / moment.m00;
					BeckY = moment.m01 / moment.m00;
					drawObject(BeckX, BeckY, imgFrontArea, Scalar(0, 0, 255));
					imshow("imgFrontArea", imgFrontArea);
					if (waitKey(100000) == 27) //wait for 'esc' key press for 30ms. If 'esc' key is pressed, break loop
					{
						cout << "esc key is pressed by user" << endl;
					}
					return true;
				}
			}
		}
	}
	return false; 
}

Scalar TrackRobot::SetColorToTrack(int Color) {
	Scalar ColorObj;
	switch (Color) {
	case 0:
		iLowH = 20;
		iHighH = 60;
		iLowS = 0;
		iHighS = 244;
		iLowV = 0;
		iHighV = 244;
		ColorObj = Scalar(0, 255, 0);
		break;
	case 1:
		iLowH = 95;
		iHighH = 125;
		iLowS = 0;
		iHighS = 244;
		iLowV = 0;
		iHighV = 244;
		ColorObj = Scalar(255, 0, 0);
		break;
	case 2:
		iLowH = 170;
		iHighH = 180;
		iLowS = 0;
		iHighS = 244;
		iLowV = 0;
		iHighV = 244;
		ColorObj = Scalar(0, 0, 255);
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

void TrackRobot::drawObject(int X, int Y ,Mat &frame, Scalar ColorObj) {

	//use some of the openCV drawing functions to draw crosshairs
	//on your tracked image!

	//UPDATE:JUNE 18TH, 2013
	//added 'if' and 'else' statements to prevent
	//memory errors from writing off the screen (ie. (-25,-25) is not within the window!)


	circle(frame, Point(X, Y), 20, ColorObj, 2);
	if (Y - 25>0)
		line(frame, Point(X, Y), Point(X, Y - 25), ColorObj, 2);
	else line(frame, Point(X, Y), Point(X, 0), ColorObj, 2);
	if (Y + 25<FRAME_HEIGHT)
		line(frame, Point(X, Y), Point(X, Y + 25), ColorObj, 2);
	else line(frame, Point(X, Y), Point(X, FRAME_HEIGHT), ColorObj, 2);
	if (X - 25>0)
		line(frame, Point(X, Y), Point(X - 25, Y), ColorObj, 2);
	else line(frame, Point(X, Y), Point(0, Y), ColorObj, 2);
	if (X + 25<FRAME_WIDTH)
		line(frame, Point(X, Y), Point(X + 25, Y), ColorObj, 2);
	else line(frame, Point(X, Y), Point(FRAME_WIDTH, Y), ColorObj, 2);

	putText(frame, intToString(X) + "," + intToString(Y), Point(X, Y + 30), 1, 1, ColorObj, 2);

}

void TrackRobot::DoErode(Mat imgThresholded, bool enable)
{
	if (enable) {
		erode(imgThresholded, imgThresholded, getStructuringElement(MORPH_ELLIPSE, Size(5, 5)));
		dilate(imgThresholded, imgThresholded, getStructuringElement(MORPH_ELLIPSE, Size(5, 5)));

		//morphological closing (fill small holes in the foreground)
		dilate(imgThresholded, imgThresholded, getStructuringElement(MORPH_ELLIPSE, Size(5, 5)));
		erode(imgThresholded, imgThresholded, getStructuringElement(MORPH_ELLIPSE, Size(5, 5)));
	}

}

string TrackRobot::intToString(int number)
{
	std::stringstream ss;
	ss << number;
	return ss.str();
}

int TrackRobot::GetX()
{
	return x;
}

int TrackRobot::GetY()
{
	return y;
}

void TrackRobot::SetXY()
{
	x = (FrontX + BeckX) / 2;
	y = (FrontY + BeckY) / 2;
}
