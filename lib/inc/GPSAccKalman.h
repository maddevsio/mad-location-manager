#ifndef GPSACCKALMAN_H
#define GPSACCKALMAN_H

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct kalman_filter kalman_filter_t;
typedef struct gps_accelerometer_fusion_filter {
  double predictTime;
  double updateTime;
  double accDeviation;
  uint32_t predictCount;
  kalman_filter_t *kf;
} gps_accelerometer_fusion_filter_t;

gps_accelerometer_fusion_filter_t* gps_accelerometer_fusion_filter_alloc(
    double x, double y,
    double xVel, double yVel,
    double accDev, double posDev,
    double timeStamp);

void gps_accelerometer_fusion_filter_free(gps_accelerometer_fusion_filter_t *k);

void gps_accelerometer_fusion_filter_predict(gps_accelerometer_fusion_filter_t *k,
                                             double timeNow,
                                             double xAcc,
                                             double yAcc);

void gps_accelerometer_fusion_filter_update(gps_accelerometer_fusion_filter_t *k,
                                            double timeStamp,
                                            double x,
                                            double y,
                                            double xVel,
                                            double yVel,
                                            double posDev);

double gps_accelerometer_fusion_filter_get_x(const gps_accelerometer_fusion_filter_t *k);
double gps_accelerometer_fusion_filter_get_y(const gps_accelerometer_fusion_filter_t *k);
double gps_accelerometer_fusion_filter_get_vel_x(const gps_accelerometer_fusion_filter_t *k);
double gps_accelerometer_fusion_filter_get_vel_y(const gps_accelerometer_fusion_filter_t *k);

#ifdef __cplusplus
}
#endif // extern "C"
#endif // GPSACCKALMAN_H
