#include <iostream>
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "TrackRobot.h"

using namespace cv;
using namespace std;

int main(int argc, char** argv)
{
	Mat imgOriginal;
	
	imgOriginal = imread("check4.jpg", CV_LOAD_IMAGE_COLOR);

    
	int rows = imgOriginal.rows;
	int cols = imgOriginal.cols;
	printf("===================%d %d==================/n", rows, cols);
	Size s = imgOriginal.size();
	rows = s.height;
	cols = s.width;
	printf("===================%d %d==================/n", rows, cols);
	TrackRobot Robot(0,1);
	Robot.TrackingImg(imgOriginal);
	imshow("Original", imgOriginal);
	if (waitKey(100000) == 27) //wait for 'esc' key press for 30ms. If 'esc' key is pressed, break loop
	{
		cout << "esc key is pressed by user" << endl;
	}
	return 0;

}