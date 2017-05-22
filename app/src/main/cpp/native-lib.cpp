//
// Created by ryuji on 2017-05-22.
//

#include <opencv2/opencv.hpp>
#include <jni.h>
#include <android/log.h>
#include <vector>

#define TAG "native-lib"

using namespace std;
using namespace cv;

extern "C"{

class iconRecog {
private:
    int background = 0;
public:
    void squarize(Mat &origin);
    void cropping(Mat &origin);
};



JNIEXPORT void JNICALL
Java_com_rosie_accessibilityservice_Reflection_preprocessing(JNIEnv *env, jobject, jlong inputAddr){

    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "native-lib");
    try{
        iconRecog miconRecog;

        Mat &input = *(Mat *) inputAddr;
        cvtColor(input, input, CV_RGB2GRAY);

        miconRecog.cropping(input);
        miconRecog.squarize(input);

    }catch (Exception e){
        env-> ExceptionDescribe();
        env-> ExceptionClear();
    }

}


void iconRecog::squarize(Mat &originMat) {

    int col = originMat.cols;
    int row = originMat.rows;

    if (col == row) {
        return;
    }

    int move1, move2;
    int margin = 10;
    Mat stride1, stride2;

    if (col > row) {

        move1 = (int)((col - row + margin) / 2);
        move2 = (int)margin / 2;
        stride1 = (Mat_<float>(2, 3) << 1, 0, 0, 0, 1, move1); // move to down
        stride2 = (Mat_<float>(2, 3) << 1, 0, move2, 0, 1, 0); // move to right

        originMat = Mat::zeros(Size(col+margin, col+margin), originMat.type());

    }else if (row > col) {

        move1 = (int)((row - col + margin) / 2);
        move2 = (int)margin / 2;

        stride1 = (Mat_<float>(2, 3) << 1, 0, move1, 0, 1, 0); // move to right
        stride2 = (Mat_<float>(2, 3) << 1, 0, 0, 0, 1, move2); // move to down

        originMat = Mat::zeros(Size(row+margin, row+margin), originMat.type());

    }

    warpAffine(originMat, originMat, stride1, originMat.size(), INTER_LANCZOS4, BORDER_CONSTANT, cv::Scalar(background));
    warpAffine(originMat, originMat, stride2, originMat.size(), INTER_LANCZOS4, BORDER_CONSTANT, cv::Scalar(background));

    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "square complete");

 }

void iconRecog::cropping(Mat &originMat){

    // originMat is grayScale
    uchar *data = originMat.data;
    vector<pair<int, int> > firstIndex;

    int row = originMat.rows;
    int col = originMat.cols;
    int threshold = 50;

    int current, before;

    //////////// left to right scanning ///////////
    for (int i = 0; i < row; i++) {

        for (int j = 0; j < col; j++) {
            if (j == 0) {
                before = (int)data[i*col + j];
            }
            current = (int) data[i*col + j];

            if (abs(current - before) > threshold) {
                background = before;
                int r = i;
                int c = j;
                pair<int, int> pil(r,c);
                firstIndex.push_back(pil);

                break;
            }

            before = current;

        }

    }

    // find min col index
    int minCol = col;
    int minRow;

    for (int i = 0; i < firstIndex.size(); i++) {

        int c = firstIndex.at(i).second;

        if (c < minCol) {
            minCol = c;
        }
    }

    int leftCol = minCol;

    firstIndex.clear();

    ///////////// top to bottom scanning ////////////

    for (int i = 0; i < col; i++) {

        for (int j = 0; j < row; j++) {

            if (j == 0) {
                before = (int)data[j*col + i];
            }

            current = (int) data[j*col + i];

            if (abs(current - before) > threshold) {

                int r = (int) (j*col + i) / col;
                int c = (int)(j*col + i) % col;
                pair<int, int> pil(r,c);
                firstIndex.push_back(pil);
                break;
            }

            before = current;
        }

    }

    // find min row index

    minRow = row;

    for (int i = 0; i < firstIndex.size(); i++) {

        int r = firstIndex.at(i).first;

        if (r < minRow) {
            minRow = r;
        }

    }

    int leftRow = minRow;

    firstIndex.clear();

    ////////////// right to left scanning /////////////

    for (int i = 0; i < row; i++) {

        for (int j = col-1; j >= 0 ; j--) {
            if (j == col-1) {
                before = (int)data[i*col + j];
            }
            current = (int) data[i*col + j];

            if (abs(current - before) > threshold) {

                int r = (int)(i*col + j) / col;
                int c = (int)(i*col + j) % col;
                pair<int, int> pil(r,c);
                firstIndex.push_back(pil);

                break;
            }

            before = current;
        }

    }

    // find max col index

    int maxCol = 0;
    int maxRow = 0;

    for (int i = 0; i < firstIndex.size(); i++) {

        int c = firstIndex.at(i).second;

        if (maxCol < c) {
            maxCol = c;
        }
    }

    int rightCol = maxCol;

    firstIndex.clear();

    ////////////// bottom to up scannig ////////////

    for (int i = 0; i < col; i++) {


        for (int j = row-1; j >= 0 ; j--) {
            if (j == row-1) {
                before = (int)data[col*j + i];
            }

            current = (int) data[col*j + i];

            if (abs(current - before) > threshold) {

                int r = (int)(col*j + i) / col;
                int c = (int)(col*j + i) % col;
                pair<int, int> pil(r,c);
                firstIndex.push_back(pil);
                break;
            }

            before = current;
        }

    }

    // find max row index

    maxRow = 0;

    for (int i = 0; i < firstIndex.size(); i++) {

        int r = firstIndex.at(i).first;

        if (maxRow < r) {
            maxRow = r;
        }
    }

    int rightRow = maxRow;

    // 관심영역 설정 (set ROI (X, Y, W, H)).

    if (leftCol == col || leftRow == row || rightCol - leftCol <= 0 || rightRow - leftRow <= 0 ) {
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "cannot crop");
        return;
    }

    Rect rect;
    rect = Rect(leftCol, leftRow, rightCol - leftCol, rightRow - leftRow);

    originMat = originMat(rect);
    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "crop complete");
}



}
