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

//----------------------------------------------------------------------------------------------------
// Variable declaration

typedef struct quaternion {
  float w, x, y, z;
} quaternion_t;

typedef struct MadgwickFilterAlloc {
  float beta;             //algorithm gain
  float sampleFreq;
  float q0, q1, q2, q3;   //quaternion of sensor frame relative to auxiliary frame
} MadgwickFilter_t;

//---------------------------------------------------------------------------------------------------
// Function declarations

MadgwickFilter_t* MadgwickFilterAlloc(float beta, float sampleFreqHZ);
void MadgwickFilterFree(MadgwickFilter_t *f);
void MadgwickAHRSupdate(MadgwickFilter_t *f,
                        float gx, float gy, float gz,
                        float ax, float ay, float az,
                        float mx, float my, float mz);

void MadgwickAHRSupdateIMU( MadgwickFilter_t *f,
                            float gx, float gy, float gz,
                            float ax, float ay, float az);

void MadgwickRotationMatrix (MadgwickFilter_t *mf,
                              float *mtx);

#endif
//=====================================================================================================
// End of file
//=====================================================================================================
