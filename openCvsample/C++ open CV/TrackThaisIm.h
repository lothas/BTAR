#pragma once
#include <iostream>
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
using namespace cv;
using namespace std;

class TrackThaisIm
{
public:
	TrackThaisIm();
	~TrackThaisIm();
	Mat TrackingImg(Mat imgOriginal);
	void TrackingObj(int Color, Mat imgOriginal);
	Scalar SetColorToTrack(int Color);
	void drawObject(int x, int y, Mat &frame, Scalar ColorObj);
	void DoErode(Mat imgThresholded, bool enable);
	string intToString(int number);
private:
	//frame size
	const int FRAME_WIDTH = 640;
	const int FRAME_HEIGHT = 480;

	//Threshold valus
	int iLowH = 50;
	int iHighH = 93;
	int iLowS = 47;
	int iHighS = 169;
	int iLowV = 101;
	int iHighV = 235;

};

