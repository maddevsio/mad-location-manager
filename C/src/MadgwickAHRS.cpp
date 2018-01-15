//=====================================================================================================
// MadgwickAHRS.c
//=====================================================================================================
//
// Implementation of Madgwick's IMU and AHRS algorithms.
// See: http://www.x-io.co.uk/node/8#open_source_ahrs_and_imu_algorithms
//
// Date			Author          Notes
// 29/09/2011	SOH Madgwick    Initial release
// 02/10/2011	SOH Madgwick	Optimised for reduced CPU load
// 19/02/2012	SOH Madgwick	Magnetometer measurement is normalised
//
//=====================================================================================================

//---------------------------------------------------------------------------------------------------
// Header files

#include "MadgwickAHRS.h"
#include <stdint.h>
#include <math.h>
#include <assert.h>
//---------------------------------------------------------------------------------------------------
// Definitions

//#define sampleFreq	512.0f		// sample frequency in Hz
//#define betaDef		0.001f		// 2 * proportional gain

//---------------------------------------------------------------------------------------------------
// Variable definitions


//---------------------------------------------------------------------------------------------------
// Function declarations

static float invSqrt(float x);

//====================================================================================================
// Functions


MadgwickFilter_t *MadgwickFilterAlloc(float beta, float sampleFreqHZ) {
  MadgwickFilter_t *f = (MadgwickFilter_t*) malloc(sizeof(MadgwickFilter_t));
  assert(f);
  f->beta = beta;
  f->q0 = 1.0f;
  f->q1 = 0.0f;
  f->q2 = 0.0f;
  f->q3 = 0.0f;
  f->sampleFreq = sampleFreqHZ;
  return f;
}
//////////////////////////////////////////////////////////////////////////

void MadgwickFilterFree(MadgwickFilter_t *f) {
  if (f) free(f);
}

//---------------------------------------------------------------------------------------------------
// AHRS algorithm update

void MadgwickAHRSupdate(MadgwickFilter_t *f,
                        float gx, float gy, float gz,
                        float ax, float ay, float az,
                        float mx, float my, float mz) {
	float recipNorm;
	float s0, s1, s2, s3;
	float qDot1, qDot2, qDot3, qDot4;
	float hx, hy;
  float _2q0mx, _2q0my, _2q0mz, _2q1mx, _2bx, _2bz, _4bx, _4bz, _8bx, _8bz, _2q0, _2q1, _2q2, _2q3,
     q0q0, q0q1, q0q2, q0q3, q1q1, q1q2, q1q3, q2q2, q2q3, q3q3;

	// Use IMU algorithm if magnetometer measurement invalid (avoids NaN in magnetometer normalisation)
	if((mx == 0.0f) && (my == 0.0f) && (mz == 0.0f)) {
    MadgwickAHRSupdateIMU(f, gx, gy, gz, ax, ay, az);
		return;
	}

	// Rate of change of quaternion from gyroscope
  qDot1 = 0.5f * (-f->q1 * gx - f->q2 * gy - f->q3 * gz);
  qDot2 = 0.5f * (f->q0 * gx + f->q2 * gz - f->q3 * gy);
  qDot3 = 0.5f * (f->q0 * gy - f->q1 * gz + f->q3 * gx);
  qDot4 = 0.5f * (f->q0 * gz + f->q1 * gy - f->q2 * gx);

	// Compute feedback only if accelerometer measurement valid (avoids NaN in accelerometer normalisation)
	if(!((ax == 0.0f) && (ay == 0.0f) && (az == 0.0f))) {

		// Normalise accelerometer measurement
		recipNorm = invSqrt(ax * ax + ay * ay + az * az);
		ax *= recipNorm;
		ay *= recipNorm;
		az *= recipNorm;   

		// Normalise magnetometer measurement
		recipNorm = invSqrt(mx * mx + my * my + mz * mz);
		mx *= recipNorm;
		my *= recipNorm;
		mz *= recipNorm;

		// Auxiliary variables to avoid repeated arithmetic
    _2q0mx = 2.0f * f->q0 * mx;
    _2q0my = 2.0f * f->q0 * my;
    _2q0mz = 2.0f * f->q0 * mz;
    _2q1mx = 2.0f * f->q1 * mx;
    _2q0 = 2.0f * f->q0;
    _2q1 = 2.0f * f->q1;
    _2q2 = 2.0f * f->q2;
    _2q3 = 2.0f * f->q3;
    q0q0 = f->q0 * f->q0;
    q0q1 = f->q0 * f->q1;
    q0q2 = f->q0 * f->q2;
    q0q3 = f->q0 * f->q3;
    q1q1 = f->q1 * f->q1;
    q1q2 = f->q1 * f->q2;
    q1q3 = f->q1 * f->q3;
    q2q2 = f->q2 * f->q2;
    q2q3 = f->q2 * f->q3;
    q3q3 = f->q3 * f->q3;

    // Reference direction of Earth's magnetic field
    hx = mx * q0q0 - _2q0my * f->q3 + _2q0mz * f->q2 + mx * q1q1 + _2q1 * my * f->q2 + _2q1 * mz * f->q3 - mx * q2q2 - mx * q3q3;
    hy = _2q0mx * f->q3 + my * q0q0 - _2q0mz * f->q1 + _2q1mx * f->q2 - my * q1q1 + my * q2q2 + _2q2 * mz * f->q3 - my * q3q3;
    _2bx = sqrt(hx * hx + hy * hy);
    _2bz = -_2q0mx * f->q2 + _2q0my * f->q1 + mz * q0q0 + _2q1mx * f->q3 - mz * q1q1 + _2q2 * my * f->q3 - mz * q2q2 + mz * q3q3;
    _4bx = 2.0f * _2bx;
    _4bz = 2.0f * _2bz;
    _8bx = 2.0f * _4bx;
    _8bz = 2.0f * _4bz;

    // Gradient decent algorithm corrective step
    s0= -_2q2*(2.0f*(q1q3 - q0q2) - ax) + _2q1*(2.0f*(q0q1 + q2q3) - ay) + -_4bz*f->q2*(_4bx*(0.5f - q2q2 - q3q3) + _4bz*(q1q3 - q0q2) - mx)   +   (-_4bx*f->q3+_4bz*f->q1)*(_4bx*(q1q2 - q0q3) + _4bz*(q0q1 + q2q3) - my)    +   _4bx*f->q2*(_4bx*(q0q2 + q1q3) + _4bz*(0.5f - q1q1 - q2q2) - mz);
    s1= _2q3*(2.0f*(q1q3 - q0q2) - ax) + _2q0*(2.0f*(q0q1 + q2q3) - ay) + -4.0f*f->q1*(2.0f*(0.5f - q1q1 - q2q2) - az)    +   _4bz*f->q3*(_4bx*(0.5f - q2q2 - q3q3) + _4bz*(q1q3 - q0q2) - mx)   + (_4bx*f->q2+_4bz*f->q0)*(_4bx*(q1q2 - q0q3) + _4bz*(q0q1 + q2q3) - my)   +   (_4bx*f->q3-_8bz*f->q1)*(_4bx*(q0q2 + q1q3) + _4bz*(0.5f - q1q1 - q2q2) - mz);
    s2= -_2q0*(2.0f*(q1q3 - q0q2) - ax)    +     _2q3*(2.0f*(q0q1 + q2q3) - ay)   +   (-4.0f*f->q2)*(2.0f*(0.5f - q1q1 - q2q2) - az) +   (-_8bx*f->q2-_4bz*f->q0)*(_4bx*(0.5f - q2q2 - q3q3) + _4bz*(q1q3 - q0q2) - mx)+(_4bx*f->q1+_4bz*f->q3)*(_4bx*(q1q2 - q0q3) + _4bz*(q0q1 + q2q3) - my)+(_4bx*f->q0-_8bz*f->q2)*(_4bx*(q0q2 + q1q3) + _4bz*(0.5f - q1q1 - q2q2) - mz);
    s3= _2q1*(2.0f*(q1q3 - q0q2) - ax) +   _2q2*(2.0f*(q0q1 + q2q3) - ay)+(-_8bx*f->q3+_4bz*f->q1)*(_4bx*(0.5f - q2q2 - q3q3) + _4bz*(q1q3 - q0q2) - mx)+(-_4bx*f->q0+_4bz*f->q2)*(_4bx*(q1q2 - q0q3) + _4bz*(q0q1 + q2q3) - my)+(_4bx*f->q1)*(_4bx*(q0q2 + q1q3) + _4bz*(0.5f - q1q1 - q2q2) - mz);

    recipNorm = invSqrt(s0 * s0 + s1 * s1 + s2 * s2 + s3 * s3); // normalise step magnitude
		s0 *= recipNorm;
		s1 *= recipNorm;
		s2 *= recipNorm;
		s3 *= recipNorm;

		// Apply feedback step
    qDot1 -= f->beta * s0;
    qDot2 -= f->beta * s1;
    qDot3 -= f->beta * s2;
    qDot4 -= f->beta * s3;
	}

	// Integrate rate of change of quaternion to yield quaternion
  f->q0 += qDot1 * (1.0f / f->sampleFreq);
  f->q1 += qDot2 * (1.0f / f->sampleFreq);
  f->q2 += qDot3 * (1.0f / f->sampleFreq);
  f->q3 += qDot4 * (1.0f / f->sampleFreq);

	// Normalise quaternion
  recipNorm = invSqrt(f->q0 * f->q0 + f->q1 * f->q1 + f->q2 * f->q2 + f->q3 * f->q3);
  f->q0 *= recipNorm;
  f->q1 *= recipNorm;
  f->q2 *= recipNorm;
  f->q3 *= recipNorm;
}

//---------------------------------------------------------------------------------------------------
// IMU algorithm update

void MadgwickAHRSupdateIMU(MadgwickFilter_t *f,
                           float gx, float gy, float gz,
                           float ax, float ay, float az) {
	float recipNorm;
	float s0, s1, s2, s3;
	float qDot1, qDot2, qDot3, qDot4;
	float _2q0, _2q1, _2q2, _2q3, _4q0, _4q1, _4q2 ,_8q1, _8q2, q0q0, q1q1, q2q2, q3q3;

	// Rate of change of quaternion from gyroscope
  qDot1 = 0.5f * (-f->q1 * gx - f->q2 * gy - f->q3 * gz);
  qDot2 = 0.5f * (f->q0 * gx + f->q2 * gz - f->q3 * gy);
  qDot3 = 0.5f * (f->q0 * gy - f->q1 * gz + f->q3 * gx);
  qDot4 = 0.5f * (f->q0 * gz + f->q1 * gy - f->q2 * gx);

	// Compute feedback only if accelerometer measurement valid (avoids NaN in accelerometer normalisation)
	if(!((ax == 0.0f) && (ay == 0.0f) && (az == 0.0f))) {

		// Normalise accelerometer measurement
		recipNorm = invSqrt(ax * ax + ay * ay + az * az);
		ax *= recipNorm;
		ay *= recipNorm;
		az *= recipNorm;   

		// Auxiliary variables to avoid repeated arithmetic
    _2q0 = 2.0f * f->q0;
    _2q1 = 2.0f * f->q1;
    _2q2 = 2.0f * f->q2;
    _2q3 = 2.0f * f->q3;
    _4q0 = 4.0f * f->q0;
    _4q1 = 4.0f * f->q1;
    _4q2 = 4.0f * f->q2;
    _8q1 = 8.0f * f->q1;
    _8q2 = 8.0f * f->q2;
    q0q0 = f->q0 * f->q0;
    q1q1 = f->q1 * f->q1;
    q2q2 = f->q2 * f->q2;
    q3q3 = f->q3 * f->q3;

		// Gradient decent algorithm corrective step
    s0 = _4q0*q2q2 + _2q2*ax + _4q0*q1q1 - _2q1*ay;
    s1 = _4q1*q3q3 - _2q3*ax + 4.0f*q0q0*f->q1 - _2q0*ay - _4q1 + _8q1*q1q1 + _8q1*q2q2 + _4q1*az;
    s2 = 4.0f*q0q0*f->q2 + _2q0*ax + _4q2*q3q3 - _2q3*ay - _4q2 + _8q2*q1q1 + _8q2*q2q2 + _4q2*az;
    s3 = 4.0f*q1q1*f->q3 - _2q1*ax + 4.0f*q2q2*f->q3 - _2q2*ay;
		recipNorm = invSqrt(s0 * s0 + s1 * s1 + s2 * s2 + s3 * s3); // normalise step magnitude
		s0 *= recipNorm;
		s1 *= recipNorm;
		s2 *= recipNorm;
		s3 *= recipNorm;

		// Apply feedback step
    qDot1 -= f->beta * s0;
    qDot2 -= f->beta * s1;
    qDot3 -= f->beta * s2;
    qDot4 -= f->beta * s3;
	}

	// Integrate rate of change of quaternion to yield quaternion
  f->q0 += qDot1 * (1.0f / f->sampleFreq);
  f->q1 += qDot2 * (1.0f / f->sampleFreq);
  f->q2 += qDot3 * (1.0f / f->sampleFreq);
  f->q3 += qDot4 * (1.0f / f->sampleFreq);

	// Normalise quaternion
  recipNorm = invSqrt(f->q0*f->q0 + f->q1*f->q1 + f->q2*f->q2 + f->q3*f->q3);
  f->q0 *= recipNorm;
  f->q1 *= recipNorm;
  f->q2 *= recipNorm;
  f->q3 *= recipNorm;
}

//---------------------------------------------------------------------------------------------------
// Fast inverse square-root
// See: http://en.wikipedia.org/wiki/Fast_inverse_square_root

float invSqrt(float x) {
  //DOESN'T WORK ON 64-bit MACHINE
//  float halfx = 0.5f * x;
//  float y = x;
//  int32_t i = *((int32_t*)&y);
//  i = 0x5f3759df - (i>>1);
//  y = *(int32_t*)&i;
//  y = y * (1.5f - (halfx * y * y));
//  return y;
  return 1.0f / sqrt(x);
}

void
MadgwickRotationMatrix(MadgwickFilter_t *mf, float *mtx) {
  mtx[0] = 1.0f - 2.0f*mf->q2*mf->q2 - 2.0f*mf->q3*mf->q3;
  mtx[1] = 2.0f*mf->q1*mf->q2 - 2.0f*mf->q3*mf->q0;
  mtx[2] = 2.0f*mf->q1*mf->q3 + 2.0f*mf->q2*mf->q0;
  mtx[3] = 2.0f*mf->q1*mf->q2 + 2.0f*mf->q3*mf->q0;
  mtx[4] = 1.0f - 2.0f*mf->q1*mf->q1 - 2.0f*mf->q3*mf->q3;
  mtx[5] = 2.0f*mf->q2*mf->q3 - 2.0f*mf->q1*mf->q0;
  mtx[6] = 2.0f*mf->q1*mf->q3 - 2.0f*mf->q2*mf->q0;
  mtx[7] = 2.0f*mf->q2*mf->q3 + 2.0f*mf->q1*mf->q0;
  mtx[8] = 1.0f - 2.0f*mf->q1*mf->q1 - 2.0f*mf->q2*mf->q2;
}

//====================================================================================================
// END OF CODE
//====================================================================================================
