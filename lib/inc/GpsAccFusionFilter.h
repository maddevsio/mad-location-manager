#ifndef GPSACCKALMAN_H
#define GPSACCKALMAN_H

#include <stdint.h>
#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef double timestamp_t;

typedef struct kalman_filter kalman_filter_t;
typedef struct gps_accelerometer_fusion_filter gps_accelerometer_fusion_filter_t;

typedef void (*pf_rebuildR)(gps_accelerometer_fusion_filter_t *f,
                            double posSigma);

typedef struct gps_accelerometer_fusion_filter {
  timestamp_t last_predict_ms;
  double acc_deviation; //accelerometer sigma
  uint32_t predicts_count; //aux value for Q matrix
  pf_rebuildR rebuildR; //pointer to function
  kalman_filter_t *kf;
} gps_accelerometer_fusion_filter_t;

typedef struct gps_accelerometer_fusion_filter_state {
  double x; //longitude in meters
  double y; //latitude in meters (see coordinates.h for aux functions)
  double x_vel;
  double y_vel;
} gaff_state_t;
///////////////////////////////////////////////////////

gps_accelerometer_fusion_filter_t* gps_accelerometer_fusion_filter_alloc(
    gaff_state_t initial_state,
    bool use_gps_speed,
    double acc_dev,
    double pos_dev,
    timestamp_t time_stamp_ms);

void gps_accelerometer_fusion_filter_free(gps_accelerometer_fusion_filter_t *filter);

void gps_accelerometer_fusion_filter_predict(gps_accelerometer_fusion_filter_t *filter,
                                             timestamp_t time_now_ms,
                                             double xAcc,
                                             double yAcc);

void gps_accelerometer_fusion_filter_update(gps_accelerometer_fusion_filter_t *filter,
                                            gaff_state_t state,
                                            double pos_dev);

gaff_state_t gps_accelerometer_fusion_filter_current_state(const gps_accelerometer_fusion_filter_t *filter);

#ifdef __cplusplus
}
#endif // extern "C"
#endif // GPSACCKALMAN_H
