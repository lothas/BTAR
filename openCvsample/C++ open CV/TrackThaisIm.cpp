#include "TrackThaisIm.h"



TrackThaisIm::TrackThaisIm()
{
}


TrackThaisIm::~TrackThaisIm()
{
}

Mat TrackThaisIm::TrackingImg(Mat imgOriginal)
{
	int i = 0, x = 0, y = 0;
	Mat imgThresholded, imgHSV;
	TrackingObj(0, imgOriginal);
	TrackingObj(1, imgOriginal);
	TrackingObj(2, imgOriginal);
	
	return imgOriginal;
}

Scalar TrackThaisIm::SetColorToTrack(int Color) {
	Scalar ColorObj;
	switch (Color) {
	case 0:
		iLowH = 30;
		iHighH = 46;
		iLowS = 135;
		iHighS = 214;
		iLowV = 62;
		iHighV = 80;
		ColorObj = Scalar(0, 255, 0);
		break;
	case 1:
		iLowH = 0;
		iHighH = 200;
		iLowS = 0;
		iHighS = 254;
		iLowV = 0;
		iHighV = 254;
		ColorObj = Scalar(255, 0, 0);
		break;
	case 2:
		iLowH = 170;
		iHighH = 180;
		iLowS = 110;
		iHighS = 180;
		iLowV = 60;
		iHighV = 100;
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

void TrackThaisIm::drawObject(int x, int y, Mat &frame, Scalar ColorObj) {

	//use some of the openCV drawing functions to draw crosshairs
	//on your tracked image!

	//UPDATE:JUNE 18TH, 2013
	//added 'if' and 'else' statements to prevent
	//memory errors from writing off the screen (ie. (-25,-25) is not within the window!)


	circle(frame, Point(x, y), 20, ColorObj, 2);
	if (y - 25>0)
		line(frame, Point(x, y), Point(x, y - 25), ColorObj, 2);
	else line(frame, Point(x, y), Point(x, 0), ColorObj, 2);
	if (y + 25<FRAME_HEIGHT)
		line(frame, Point(x, y), Point(x, y + 25), ColorObj, 2);
	else line(frame, Point(x, y), Point(x, FRAME_HEIGHT), ColorObj, 2);
	if (x - 25>0)
		line(frame, Point(x, y), Point(x - 25, y), ColorObj, 2);
	else line(frame, Point(x, y), Point(0, y), ColorObj, 2);
	if (x + 25<FRAME_WIDTH)
		line(frame, Point(x, y), Point(x + 25, y), ColorObj, 2);
	else line(frame, Point(x, y), Point(FRAME_WIDTH, y), ColorObj, 2);

	putText(frame, intToString(x) + "," + intToString(y), Point(x, y + 30), 1, 1, ColorObj, 2);

}

void TrackThaisIm::DoErode(Mat imgThresholded, bool enable)
{
	if (enable) {
		erode(imgThresholded, imgThresholded, getStructuringElement(MORPH_ELLIPSE, Size(5, 5)));
		dilate(imgThresholded, imgThresholded, getStructuringElement(MORPH_ELLIPSE, Size(5, 5)));

		//morphological closing (fill small holes in the foreground)
		dilate(imgThresholded, imgThresholded, getStructuringElement(MORPH_ELLIPSE, Size(5, 5)));
		erode(imgThresholded, imgThresholded, getStructuringElement(MORPH_ELLIPSE, Size(5, 5)));
	}

}

void TrackThaisIm::TrackingObj(int Color, Mat imgOriginal)
{
	int i = 0, x = 0, y = 0;
	Scalar ColorObj;
	ColorObj = SetColorToTrack(Color);
	Mat imgThresholded, imgHSV;
	cvtColor(imgOriginal, imgHSV, COLOR_BGR2HSV); //Convert the captured frame from BGR to HSV
	inRange(imgHSV, Scalar(iLowH, iLowS, iLowV), Scalar(iHighH, iHighS, iHighV), imgThresholded);
	DoErode(imgThresholded, true);
	vector< vector<Point> > contours;
	//vector<Vec4i> hierarchy;
	//find contours of filtered image using openCV findContours function
	findContours(imgThresholded, contours, CV_RETR_LIST, CV_CHAIN_APPROX_NONE);
	if (contours.size() > 0) {
		for (i = 0; i < contours.size(); i++) {
			Moments moment = moments((cv::Mat)contours[i]);
			if (moment.m00 < 10) { continue; }
			x = moment.m10 / moment.m00;
			y = moment.m01 / moment.m00;
			printf("x=%d y=%d \n", x, y);
			drawObject(x, y, imgOriginal, ColorObj);
		}
	}

}

string TrackThaisIm::intToString(int number)
{
	std::stringstream ss;
	ss << number;
	return ss.str();
}

