//=====================================================================================================
// MadgwickAHRS.h
//=====================================================================================================
//
// Implementation of Madgwick's IMU and AHRS algorithms.
// See: http://www.x-io.co.uk/node/8#open_source_ahrs_and_imu_algorithms
//
// Date			Author          Notes
// 29/09/2011	SOH Madgwick    Initial release
// 02/10/2011	SOH Madgwick	Optimised for reduced CPU load
//
//=====================================================================================================
#ifndef MadgwickAHRS_h
#define MadgwickAHRS_h

#include "Quaternion.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef struct madgwick_filter {
  float beta;             //algorithm gain
  float sample_freq;
  quaternion_t q;
} madgwick_filter_t;

//---------------------------------------------------------------------------------------------------
// Function declarations

madgwick_filter_t* madgwick_filter_alloc(float beta,
                                         float sampleFreqHZ);
void madgwick_filter_free(madgwick_filter_t *f);

void madgwick_filter_AHRS_update(madgwick_filter_t *f,
                                 float gx, float gy, float gz,
                                 float ax, float ay, float az,
                                 float mx, float my, float mz);

void madgwick_filter_AHRS_update_IMU( madgwick_filter_t *f,
                                      float gx, float gy, float gz,
                                      float ax, float ay, float az);

#ifdef __cplusplus
}
#endif // extern "C"
#endif // MadgwickAHRS_h
