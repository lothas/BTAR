#pragma once
#include <iostream>
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
using namespace cv;
using namespace std;

class TrackRobot
{
public:
	TrackRobot(int FColor, int BColor);
	~TrackRobot();
	int TrackingImg(Mat imgOriginal);
	Scalar SetColorToTrack(int Color);
	void drawObject(int X,int Y, Mat &frame, Scalar ColorObj);
	void DoErode(Mat imgThresholded, bool enable);
	string intToString(int number);
	int GetX();
	int GetY();
	void TrackingFront(Mat imgOriginal);
	bool TrackingBeck(Mat imgFrontArea, double front_contourArea, double CirclityFront);
	void SetXY();
private:
	//Coordinates
	int FrontX;
	int FrontY;
	int BeckX;
	int BeckY;
	int x;
	int y;

	//frame size
	int FRAME_WIDTH = 640;
	int FRAME_HEIGHT = 480;

	//Threshold valus
	int iLowH = 50;
	int iHighH = 93;
	int iLowS = 47;
	int iHighS = 169;
	int iLowV = 101;
	int iHighV = 235;

	//Robot Treck Colors
	int FrontColor;
	int BeckColor;

	//PI
	double PI = 3.141592653589;

};

