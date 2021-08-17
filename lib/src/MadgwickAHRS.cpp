//#include <math.h>
//#include <stdint.h>
//#include <assert.h>
//#include <stdlib.h>

//#include "MadgwickAHRS.hpp"

//static float inv_sqrt(float x);

//madgwick_filter_t*
//madgwick_filter_alloc(float beta, float sampleFreqHZ) {
//  madgwick_filter_t *f = (madgwick_filter_t*) malloc(sizeof(madgwick_filter_t));
//  assert(f);
//  f->beta = beta;
//  f->q.w = 1.0f;
//  f->q.x = 0.0f;
//  f->q.y = 0.0f;
//  f->q.z = 0.0f;
//  f->sample_freq = sampleFreqHZ;
//  return f;
//}
////////////////////////////////////////////////////////////////////////////

//void
//madgwick_filter_free(madgwick_filter_t *f) {
//  free(f);
//}

//void
//madgwick_filter_AHRS_update(madgwick_filter_t *f,
//                            float gx, float gy, float gz,
//                            float ax, float ay, float az,
//                            float mx, float my, float mz) {
//  float recipNorm;
//  float s0, s1, s2, s3;
//  float qDot1, qDot2, qDot3, qDot4;
//  float hx, hy;
//  float _2q0mx, _2q0my, _2q0mz, _2q1mx, _2bx, _2bz, _4bx, _4bz, _8bx, _8bz, _2q0, _2q1, _2q2, _2q3,
//      q0q0, q0q1, q0q2, q0q3, q1q1, q1q2, q1q3, q2q2, q2q3, q3q3;

//  // Use IMU algorithm if magnetometer measurement invalid (avoids NaN in magnetometer normalisation)
//  if((mx == 0.0f) && (my == 0.0f) && (mz == 0.0f)) {
//    madgwick_filter_AHRS_update_IMU(f, gx, gy, gz, ax, ay, az);
//    return;
//  }

//  // Rate of change of quaternion from gyroscope
//  qDot1 = 0.5f * (-f->q.x * gx - f->q.y * gy - f->q.z * gz);
//  qDot2 = 0.5f * (f->q.w * gx + f->q.y * gz - f->q.z * gy);
//  qDot3 = 0.5f * (f->q.w * gy - f->q.x * gz + f->q.z * gx);
//  qDot4 = 0.5f * (f->q.w * gz + f->q.x * gy - f->q.y * gx);

//  // Compute feedback only if accelerometer measurement valid (avoids NaN in accelerometer normalisation)
//  if(!((ax == 0.0f) && (ay == 0.0f) && (az == 0.0f))) {

//    // Normalise accelerometer measurement
//    recipNorm = inv_sqrt(ax * ax + ay * ay + az * az);
//    ax *= recipNorm;
//    ay *= recipNorm;
//    az *= recipNorm;

//    // Normalise magnetometer measurement
//    recipNorm = inv_sqrt(mx * mx + my * my + mz * mz);
//    mx *= recipNorm;
//    my *= recipNorm;
//    mz *= recipNorm;

//    // Auxiliary variables to avoid repeated arithmetic
//    _2q0mx = 2.0f * f->q.w * mx;
//    _2q0my = 2.0f * f->q.w * my;
//    _2q0mz = 2.0f * f->q.w * mz;
//    _2q1mx = 2.0f * f->q.x * mx;
//    _2q0 = 2.0f * f->q.w;
//    _2q1 = 2.0f * f->q.x;
//    _2q2 = 2.0f * f->q.y;
//    _2q3 = 2.0f * f->q.z;
//    q0q0 = f->q.w * f->q.w;
//    q0q1 = f->q.w * f->q.x;
//    q0q2 = f->q.w * f->q.y;
//    q0q3 = f->q.w * f->q.z;
//    q1q1 = f->q.x * f->q.x;
//    q1q2 = f->q.x * f->q.y;
//    q1q3 = f->q.x * f->q.z;
//    q2q2 = f->q.y * f->q.y;
//    q2q3 = f->q.y * f->q.z;
//    q3q3 = f->q.z * f->q.z;

//    // Reference direction of Earth's magnetic field
//    hx = mx * q0q0 - _2q0my * f->q.z + _2q0mz * f->q.y + mx * q1q1 + _2q1 * my * f->q.y + _2q1 * mz * f->q.z - mx * q2q2 - mx * q3q3;
//    hy = _2q0mx * f->q.z + my * q0q0 - _2q0mz * f->q.x + _2q1mx * f->q.y - my * q1q1 + my * q2q2 + _2q2 * mz * f->q.z - my * q3q3;
//    _2bx = sqrtf(hx * hx + hy * hy);
//    _2bz = -_2q0mx * f->q.y + _2q0my * f->q.x + mz * q0q0 + _2q1mx * f->q.z - mz * q1q1 + _2q2 * my * f->q.z - mz * q2q2 + mz * q3q3;
//    _4bx = 2.0f * _2bx;
//    _4bz = 2.0f * _2bz;
//    _8bx = 2.0f * _4bx;
//    _8bz = 2.0f * _4bz;

//    // Gradient decent algorithm corrective step
//    s0= -_2q2*(2.0f*(q1q3 - q0q2) - ax) + _2q1*(2.0f*(q0q1 + q2q3) - ay) + -_4bz*f->q.y*(_4bx*(0.5f - q2q2 - q3q3) + _4bz*(q1q3 - q0q2) - mx)   +   (-_4bx*f->q.z+_4bz*f->q.x)*(_4bx*(q1q2 - q0q3) + _4bz*(q0q1 + q2q3) - my)    +   _4bx*f->q.y*(_4bx*(q0q2 + q1q3) + _4bz*(0.5f - q1q1 - q2q2) - mz);
//    s1= _2q3*(2.0f*(q1q3 - q0q2) - ax) + _2q0*(2.0f*(q0q1 + q2q3) - ay) + -4.0f*f->q.x*(2.0f*(0.5f - q1q1 - q2q2) - az)    +   _4bz*f->q.z*(_4bx*(0.5f - q2q2 - q3q3) + _4bz*(q1q3 - q0q2) - mx)   + (_4bx*f->q.y+_4bz*f->q.w)*(_4bx*(q1q2 - q0q3) + _4bz*(q0q1 + q2q3) - my)   +   (_4bx*f->q.z-_8bz*f->q.x)*(_4bx*(q0q2 + q1q3) + _4bz*(0.5f - q1q1 - q2q2) - mz);
//    s2= -_2q0*(2.0f*(q1q3 - q0q2) - ax)    +     _2q3*(2.0f*(q0q1 + q2q3) - ay)   +   (-4.0f*f->q.y)*(2.0f*(0.5f - q1q1 - q2q2) - az) +   (-_8bx*f->q.y-_4bz*f->q.w)*(_4bx*(0.5f - q2q2 - q3q3) + _4bz*(q1q3 - q0q2) - mx)+(_4bx*f->q.x+_4bz*f->q.z)*(_4bx*(q1q2 - q0q3) + _4bz*(q0q1 + q2q3) - my)+(_4bx*f->q.w-_8bz*f->q.y)*(_4bx*(q0q2 + q1q3) + _4bz*(0.5f - q1q1 - q2q2) - mz);
//    s3= _2q1*(2.0f*(q1q3 - q0q2) - ax) +   _2q2*(2.0f*(q0q1 + q2q3) - ay)+(-_8bx*f->q.z+_4bz*f->q.x)*(_4bx*(0.5f - q2q2 - q3q3) + _4bz*(q1q3 - q0q2) - mx)+(-_4bx*f->q.w+_4bz*f->q.y)*(_4bx*(q1q2 - q0q3) + _4bz*(q0q1 + q2q3) - my)+(_4bx*f->q.x)*(_4bx*(q0q2 + q1q3) + _4bz*(0.5f - q1q1 - q2q2) - mz);

//    recipNorm = inv_sqrt(s0 * s0 + s1 * s1 + s2 * s2 + s3 * s3); // normalise step magnitude
//    s0 *= recipNorm;
//    s1 *= recipNorm;
//    s2 *= recipNorm;
//    s3 *= recipNorm;

//    // Apply feedback step
//    qDot1 -= f->beta * s0;
//    qDot2 -= f->beta * s1;
//    qDot3 -= f->beta * s2;
//    qDot4 -= f->beta * s3;
//  }

//  // Integrate rate of change of quaternion to yield quaternion
//  f->q.w += qDot1 * (1.0f / f->sample_freq);
//  f->q.x += qDot2 * (1.0f / f->sample_freq);
//  f->q.y += qDot3 * (1.0f / f->sample_freq);
//  f->q.z += qDot4 * (1.0f / f->sample_freq);

//  // Normalise quaternion
//  recipNorm = inv_sqrt(f->q.w * f->q.w + f->q.x * f->q.x + f->q.y * f->q.y + f->q.z * f->q.z);
//  f->q.w *= recipNorm;
//  f->q.x *= recipNorm;
//  f->q.y *= recipNorm;
//  f->q.z *= recipNorm;
//}
/////////////////////////////////////////////////////////

//void madgwick_filter_AHRS_update_IMU(madgwick_filter_t *f,
//                                     float gx, float gy, float gz,
//                                     float ax, float ay, float az) {
//  float recipNorm;
//  float s0, s1, s2, s3;
//  float qDot1, qDot2, qDot3, qDot4;
//  float _2q0, _2q1, _2q2, _2q3, _4q0, _4q1, _4q2 ,_8q1, _8q2, q0q0, q1q1, q2q2, q3q3;

//  // Rate of change of quaternion from gyroscope
//  qDot1 = 0.5f * (-f->q.x * gx - f->q.y * gy - f->q.z * gz);
//  qDot2 = 0.5f * (f->q.w * gx + f->q.y * gz - f->q.z * gy);
//  qDot3 = 0.5f * (f->q.w * gy - f->q.x * gz + f->q.z * gx);
//  qDot4 = 0.5f * (f->q.w * gz + f->q.x * gy - f->q.y * gx);

//  // Compute feedback only if accelerometer measurement valid (avoids NaN in accelerometer normalisation)
//  if(!((ax == 0.0f) && (ay == 0.0f) && (az == 0.0f))) {

//    // Normalise accelerometer measurement
//    recipNorm = inv_sqrt(ax * ax + ay * ay + az * az);
//    ax *= recipNorm;
//    ay *= recipNorm;
//    az *= recipNorm;

//    // Auxiliary variables to avoid repeated arithmetic
//    _2q0 = 2.0f * f->q.w;
//    _2q1 = 2.0f * f->q.x;
//    _2q2 = 2.0f * f->q.y;
//    _2q3 = 2.0f * f->q.z;
//    _4q0 = 4.0f * f->q.w;
//    _4q1 = 4.0f * f->q.x;
//    _4q2 = 4.0f * f->q.y;
//    _8q1 = 8.0f * f->q.x;
//    _8q2 = 8.0f * f->q.y;
//    q0q0 = f->q.w * f->q.w;
//    q1q1 = f->q.x * f->q.x;
//    q2q2 = f->q.y * f->q.y;
//    q3q3 = f->q.z * f->q.z;

//    // Gradient decent algorithm corrective step
//    s0 = _4q0*q2q2 + _2q2*ax + _4q0*q1q1 - _2q1*ay;
//    s1 = _4q1*q3q3 - _2q3*ax + 4.0f*q0q0*f->q.x - _2q0*ay - _4q1 + _8q1*q1q1 + _8q1*q2q2 + _4q1*az;
//    s2 = 4.0f*q0q0*f->q.y + _2q0*ax + _4q2*q3q3 - _2q3*ay - _4q2 + _8q2*q1q1 + _8q2*q2q2 + _4q2*az;
//    s3 = 4.0f*q1q1*f->q.z - _2q1*ax + 4.0f*q2q2*f->q.z - _2q2*ay;
//    recipNorm = inv_sqrt(s0 * s0 + s1 * s1 + s2 * s2 + s3 * s3); // normalise step magnitude
//    s0 *= recipNorm;
//    s1 *= recipNorm;
//    s2 *= recipNorm;
//    s3 *= recipNorm;

//    // Apply feedback step
//    qDot1 -= f->beta * s0;
//    qDot2 -= f->beta * s1;
//    qDot3 -= f->beta * s2;
//    qDot4 -= f->beta * s3;
//  }

//  // Integrate rate of change of quaternion to yield quaternion
//  f->q.w += qDot1 * (1.0f / f->sample_freq);
//  f->q.x += qDot2 * (1.0f / f->sample_freq);
//  f->q.y += qDot3 * (1.0f / f->sample_freq);
//  f->q.z += qDot4 * (1.0f / f->sample_freq);

//  // Normalise quaternion
//  recipNorm = inv_sqrt(f->q.w*f->q.w + f->q.x*f->q.x + f->q.y*f->q.y + f->q.z*f->q.z);
//  f->q.w *= recipNorm;
//  f->q.x *= recipNorm;
//  f->q.y *= recipNorm;
//  f->q.z *= recipNorm;
//}
/////////////////////////////////////////////////////////

//float inv_sqrt(float x) {
//  // Fast inverse square-root
//  // See: http://en.wikipedia.org/wiki/Fast_inverse_square_root
//  return 1.0f / sqrtf(x);
//}
