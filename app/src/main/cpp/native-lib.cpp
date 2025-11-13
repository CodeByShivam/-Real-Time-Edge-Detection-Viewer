#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include <opencv2/opencv.hpp>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"NativeLib",__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,"NativeLib",__VA_ARGS__)

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_edgeviewer_NativeBridge_processFrame(JNIEnv* env, jclass, jbyteArray input_, jint width, jint height) {
    // Convert jbyteArray -> std::vector<uint8_t>
    jsize inLen = env->GetArrayLength(input_);
    std::vector<uchar> inputBuf(inLen);
    env->GetByteArrayRegion(input_, 0, inLen, reinterpret_cast<jbyte*>(inputBuf.data()));

    // Create cv::Mat from ARGB_8888 byte array (Android stores as 4 bytes per pixel)
    // ARGB_8888 ordering: [A, R, G, B] per pixel. OpenCV prefers RGBA or BGRA.
    // We'll construct RGBA Mat and then convert to grayscale for Canny.

    cv::Mat rgba(height, width, CV_8UC4);
    // Android's Bitmap.copyPixelsToBuffer writes pixels as ARGB (little endian dependent),
    // but for simplicity assume order A,R,G,B per pixel in sequence.
    memcpy(rgba.data, inputBuf.data(), inLen);

    // Convert to grayscale
    cv::Mat gray;
    cv::cvtColor(rgba, gray, cv::COLOR_RGBA2GRAY);

    // Apply Gaussian blur to reduce noise then Canny
    cv::Mat blurred;
    cv::GaussianBlur(gray, blurred, cv::Size(5,5), 1.5);
    cv::Mat edges;
    cv::Canny(blurred, edges, 50, 150);

    // Convert edges (single channel) back to RGBA for display (white edges on black)
    cv::Mat edgesRGBA;
    cv::cvtColor(edges, edgesRGBA, cv::COLOR_GRAY2RGBA);

    // Optionally preserve alpha from original
    std::vector<uchar> outBuf;
    outBuf.assign(edgesRGBA.data, edgesRGBA.data + edgesRGBA.total() * edgesRGBA.elemSize());

    // create jbyteArray and return
    jbyteArray out = env->NewByteArray(static_cast<jsize>(outBuf.size()));
    env->SetByteArrayRegion(out, 0, static_cast<jsize>(outBuf.size()), reinterpret_cast<jbyte*>(outBuf.data()));

    return out;
}
