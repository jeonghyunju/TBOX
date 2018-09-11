#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>

using namespace cv;
using namespace std;

extern "C" {
    int processToNegative(Mat img_inputs, Mat &img_output) {
        cvtColor(img_inputs, img_output, CV_RGB2GRAY);
        Mat srcImage = img_output;

        Mat_<uchar>images(srcImage);
        Mat_<uchar>destImage(srcImage.size());
        for(int y=0 ; y<images.rows ; y++) {
            for(int x=0 ; x<images.cols ; x++) {
                uchar u = images(y, x);
                destImage(y, x) = 255-u;
            }
        }
        img_output = destImage.clone();
        return (0);
    }

    JNIEXPORT void JNICALL
    Java_com_example_hyunjujung_tbox_login_1join_1activity_ProfileFilter_loadImage(JNIEnv *env,
                                                                                   jobject instance,
                                                                                   jstring imageFileName,
                                                                                   jlong img) {
        Mat &img_input = *(Mat *) img;
        const char *nativeFileName = env->GetStringUTFChars(imageFileName, 0);
        img_input = imread(nativeFileName, IMREAD_COLOR);
    }

    JNIEXPORT void JNICALL
    Java_com_example_hyunjujung_tbox_login_1join_1activity_ProfileFilter_imageProcessing(JNIEnv *env,
                                                                                         jobject instance,
                                                                                         jlong inputImage,
                                                                                         jlong outputGray,
                                                                                         jlong outputSketch) {

        Mat &input_gray = *(Mat *) inputImage;
        Mat &input_sketch = *(Mat *) inputImage;
        Mat &output_gray = *(Mat *) outputGray;
        Mat &output_sketch = *(Mat *) outputSketch;

        cvtColor(input_gray, output_gray, CV_RGB2GRAY);
        cvtColor(input_sketch, output_sketch, CV_RGB2GRAY);
        blur(output_sketch, output_sketch, Size(5, 5));
        Canny(output_sketch, output_sketch, 50, 150, 5);

    }

    JNIEXPORT void JNICALL
    Java_com_example_hyunjujung_tbox_login_1join_1activity_ProfileFilter_loadImageSketch(JNIEnv *env,
                                                                                         jobject instance,
                                                                                         jstring imageFileName_,
                                                                                         jlong imgs) {
        Mat &input_sketch = *(Mat *) imgs;
        const char *imageFileName = env->GetStringUTFChars(imageFileName_, 0);
        input_sketch = imread(imageFileName, IMREAD_COLOR);
    }

    JNIEXPORT jint JNICALL
    Java_com_example_hyunjujung_tbox_login_1join_1activity_ProfileFilter_reverseProcessing(JNIEnv *env,
                                                                                           jobject instance,
                                                                                           jlong inputimages,
                                                                                           jlong outreverse) {

        Mat &input_reverse = *(Mat *) inputimages;
        Mat &output_reverse = *(Mat *) outreverse;

        int conv = processToNegative(input_reverse, output_reverse);
        int ret = (jint)conv;
        return ret;
    }

}
