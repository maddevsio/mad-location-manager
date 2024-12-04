#include <jni.h>
#include "inc/mlm.h"

static const double acc_sigma_2 = 0.01;
static const double loc_sigma_2 = 15.0;
static const double vel_sigma_2 = 0.10;
static MLM mlm(acc_sigma_2, loc_sigma_2, vel_sigma_2);

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_mlmexample_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "MLM example";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jdouble JNICALL
Java_com_example_mlmexample_MainActivity_processAcc(
        JNIEnv* env,
        jobject /* this */,
        jdouble acc_x,
        jdouble acc_y,
        jdouble acc_z,
        jdouble ts) {
    abs_accelerometer acc(acc_x, acc_y, acc_z);
    mlm.process_acc_data(acc, ts);
    gps_coordinate pc = mlm.predicted_coordinate();
    return pc.location.latitude;
}

extern "C" JNIEXPORT jdouble JNICALL
Java_com_example_mlmexample_MainActivity_processGPS(
        JNIEnv* env,
        jobject /* this */,
        jdouble latitude,
        jdouble longitude) {
    gps_coordinate gps;
    gps.location.latitude = latitude;
    gps.location.longitude = longitude;
    // todo initialize speed

    mlm.process_gps_data(gps);
    gps_coordinate pc = mlm.predicted_coordinate();
    return pc.location.longitude;
}