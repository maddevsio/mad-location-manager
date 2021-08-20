#include <math.h>
#include <stdint.h>
#include <assert.h>
#include <stdlib.h>

#include "MadgwickAHRS.hpp"

static double inv_sqrt(double x);

MadgwickFilter::MadgwickFilter(double beta,
                               double sample_freq_HZ) :
  m_beta(beta),
  m_sample_freq(sample_freq_HZ),
  q(1.0, 0.0, 0.0, 0.0)
{
}
//////////////////////////////////////////////////////////////

void
MadgwickFilter::AHRS_update(double gx, double gy, double gz,
                            double ax, double ay, double az,
                            double mx, double my, double mz) {
  double recipNorm;
  double s0, s1, s2, s3;
  double qDot1, qDot2, qDot3, qDot4;
  double hx, hy;
  double _2q0mx, _2q0my, _2q0mz, _2q1mx,
      _2bx, _2bz, _4bx, _4bz,
      _8bx, _8bz, _2q0, _2q1,
      _2q2, _2q3, q0q0, q0q1,
      q0q2, q0q3, q1q1, q1q2,
      q1q3, q2q2, q2q3, q3q3;

  // Use IMU algorithm if magnetometer measurement invalid (avoids NaN in magnetometer normalisation)
  if((mx == 0.0) && (my == 0.0) && (mz == 0.0)) {
    return AHRS_update_IMU(gx, gy, gz, ax, ay, az);
    return;
  }

  // Rate of change of quaternion from gyroscope
  qDot1 = 0.5 * (-q.x() * gx - q.y() * gy - q.z() * gz);
  qDot2 = 0.5 * (q.w() * gx + q.y() * gz - q.z() * gy);
  qDot3 = 0.5 * (q.w() * gy - q.x() * gz + q.z() * gx);
  qDot4 = 0.5 * (q.w() * gz + q.x() * gy - q.y() * gx);

  // Compute feedback only if accelerometer measurement valid (avoids NaN in accelerometer normalisation)
  if(!((ax == 0.0) && (ay == 0.0) && (az == 0.0))) {

    // Normalise accelerometer measurement
    recipNorm = inv_sqrt(ax * ax + ay * ay + az * az);
    ax *= recipNorm;
    ay *= recipNorm;
    az *= recipNorm;

    // Normalise magnetometer measurement
    recipNorm = inv_sqrt(mx * mx + my * my + mz * mz);
    mx *= recipNorm;
    my *= recipNorm;
    mz *= recipNorm;

    // Auxiliary variables to avoid repeated arithmetic
    _2q0mx = 2.0 * q.w() * mx;
    _2q0my = 2.0 * q.w() * my;
    _2q0mz = 2.0 * q.w() * mz;
    _2q1mx = 2.0 * q.x() * mx;
    _2q0 = 2.0 * q.w();
    _2q1 = 2.0 * q.x();
    _2q2 = 2.0 * q.y();
    _2q3 = 2.0 * q.z();
    q0q0 = q.w() * q.w();
    q0q1 = q.w() * q.x();
    q0q2 = q.w() * q.y();
    q0q3 = q.w() * q.z();
    q1q1 = q.x() * q.x();
    q1q2 = q.x() * q.y();
    q1q3 = q.x() * q.z();
    q2q2 = q.y() * q.y();
    q2q3 = q.y() * q.z();
    q3q3 = q.z() * q.z();

    // Reference direction of Earth's magnetic field
    hx = mx * q0q0 - _2q0my * q.z() + _2q0mz * q.y() + mx * q1q1 +
        _2q1 * my * q.y() + _2q1 * mz * q.z() - mx * q2q2 - mx * q3q3;

    hy = _2q0mx * q.z() + my * q0q0 - _2q0mz * q.x() + _2q1mx * q.y() -
        my * q1q1 + my * q2q2 + _2q2 * mz * q.z() - my * q3q3;

    _2bx = sqrt(hx * hx + hy * hy);
    _2bz = -_2q0mx * q.y() + _2q0my * q.x() + mz * q0q0 + _2q1mx * q.z() -
        mz * q1q1 + _2q2 * my * q.z() - mz * q2q2 + mz * q3q3;

    _4bx = 2.0 * _2bx;
    _4bz = 2.0 * _2bz;
    _8bx = 2.0 * _4bx;
    _8bz = 2.0 * _4bz;

    // Gradient decent algorithm corrective step
    s0= -_2q2*(2.0*(q1q3 - q0q2) - ax) + _2q1*(2.0*(q0q1 + q2q3) - ay) +
        -_4bz*q.y()*(_4bx*(0.5 - q2q2 - q3q3) + _4bz*(q1q3 - q0q2) - mx) +
        (-_4bx*q.z()+_4bz*q.x())*(_4bx*(q1q2 - q0q3) + _4bz*(q0q1 + q2q3) - my) +
        _4bx*q.y()*(_4bx*(q0q2 + q1q3) + _4bz*(0.5 - q1q1 - q2q2) - mz);

    s1= _2q3*(2.0*(q1q3 - q0q2) - ax) + _2q0*(2.0*(q0q1 + q2q3) - ay) +
        -4.0*q.x()*(2.0*(0.5 - q1q1 - q2q2) - az) +
        _4bz*q.z()*(_4bx*(0.5 - q2q2 - q3q3) + _4bz*(q1q3 - q0q2) - mx) +
        (_4bx*q.y()+_4bz*q.w())*(_4bx*(q1q2 - q0q3) + _4bz*(q0q1 + q2q3) - my) +
        (_4bx*q.z()-_8bz*q.x())*(_4bx*(q0q2 + q1q3) + _4bz*(0.5 - q1q1 - q2q2) - mz);

    s2= -_2q0*(2.0*(q1q3 - q0q2) - ax) +
        _2q3*(2.0*(q0q1 + q2q3) - ay) +
        (-4.0*q.y())*(2.0*(0.5 - q1q1 - q2q2) - az) +
        (-_8bx*q.y()-_4bz*q.w())*
        (_4bx*(0.5 - q2q2 - q3q3) + _4bz*(q1q3 - q0q2) - mx) +
        (_4bx*q.x()+_4bz*q.z())*(_4bx*(q1q2 - q0q3) + _4bz*(q0q1 + q2q3) - my) +
        (_4bx*q.w()-_8bz*q.y())*(_4bx*(q0q2 + q1q3) + _4bz*(0.5 - q1q1 - q2q2) - mz);

    s3= _2q1*(2.0*(q1q3 - q0q2) - ax) +
        _2q2*(2.0*(q0q1 + q2q3) - ay) +
        (-_8bx*q.z()+_4bz*q.x())*(_4bx*(0.5 - q2q2 - q3q3) + _4bz*(q1q3 - q0q2) - mx) +
        (-_4bx*q.w()+_4bz*q.y())*(_4bx*(q1q2 - q0q3) + _4bz*(q0q1 + q2q3) - my) +
        (_4bx*q.x())*(_4bx*(q0q2 + q1q3) + _4bz*(0.5 - q1q1 - q2q2) - mz);

    recipNorm = inv_sqrt(s0 * s0 + s1 * s1 + s2 * s2 + s3 * s3); // normalise step magnitude
    s0 *= recipNorm;
    s1 *= recipNorm;
    s2 *= recipNorm;
    s3 *= recipNorm;

    // Apply feedback step
    qDot1 -= m_beta * s0;
    qDot2 -= m_beta * s1;
    qDot3 -= m_beta * s2;
    qDot4 -= m_beta * s3;
  }

  // Integrate rate of change of quaternion to yield quaternion
  q.set_w(q.w() + qDot1 * (1.0 / m_sample_freq));
  q.set_x(q.x() + qDot2 * (1.0 / m_sample_freq));
  q.set_y(q.y() + qDot3 * (1.0 / m_sample_freq));
  q.set_z(q.z() + qDot4 * (1.0 / m_sample_freq));
  q.normalize();
//  recipNorm = inv_sqrt(q.w * q.w + q.x * q.x + q.y * q.y + q.z * q.z);
  //  q *= recipNorm;
}
//////////////////////////////////////////////////////////////

void
MadgwickFilter::AHRS_update_IMU(double gx, double gy, double gz,
                                double ax, double ay, double az) {
  double recipNorm;
  double s0, s1, s2, s3;
  double qDot1, qDot2, qDot3, qDot4;
  double _2q0, _2q1, _2q2, _2q3,
      _4q0, _4q1, _4q2 ,
      _8q1, _8q2,
      q0q0, q1q1, q2q2, q3q3;

  // Rate of change of quaternion from gyroscope
  qDot1 = 0.5 * (-q.x() * gx - q.y() * gy - q.z() * gz);
  qDot2 = 0.5 * (q.w() * gx + q.y() * gz - q.z() * gy);
  qDot3 = 0.5 * (q.w() * gy - q.x() * gz + q.z() * gx);
  qDot4 = 0.5 * (q.w() * gz + q.x() * gy - q.y() * gx);

  // Compute feedback only if accelerometer measurement valid (avoids NaN in accelerometer normalisation)
  if(!((ax == 0.0) && (ay == 0.0) && (az == 0.0))) {

    // Normalise accelerometer measurement
    recipNorm = inv_sqrt(ax * ax + ay * ay + az * az);
    ax *= recipNorm;
    ay *= recipNorm;
    az *= recipNorm;

    // Auxiliary variables to avoid repeated arithmetic
    _2q0 = 2.0 * q.w();
    _2q1 = 2.0 * q.x();
    _2q2 = 2.0 * q.y();
    _2q3 = 2.0 * q.z();
    _4q0 = 4.0 * q.w();
    _4q1 = 4.0 * q.x();
    _4q2 = 4.0 * q.y();
    _8q1 = 8.0 * q.x();
    _8q2 = 8.0 * q.y();
    q0q0 = q.w() * q.w();
    q1q1 = q.x() * q.x();
    q2q2 = q.y() * q.y();
    q3q3 = q.z() * q.z();

    // Gradient decent algorithm corrective step
    s0 = _4q0*q2q2 + _2q2*ax + _4q0*q1q1 - _2q1*ay;

    s1 = _4q1*q3q3 - _2q3*ax + 4.0*q0q0*q.x() -
        _2q0*ay - _4q1 + _8q1*q1q1 + _8q1*q2q2 + _4q1*az;

    s2 = 4.0*q0q0*q.y() + _2q0*ax + _4q2*q3q3 - _2q3*ay -
        _4q2 + _8q2*q1q1 + _8q2*q2q2 + _4q2*az;

    s3 = 4.0*q1q1*q.z() - _2q1*ax + 4.0*q2q2*q.z() - _2q2*ay;

    recipNorm = inv_sqrt(s0 * s0 + s1 * s1 + s2 * s2 + s3 * s3); // normalise step magnitude
    s0 *= recipNorm;
    s1 *= recipNorm;
    s2 *= recipNorm;
    s3 *= recipNorm;

    // Apply feedback step
    qDot1 -= m_beta * s0;
    qDot2 -= m_beta * s1;
    qDot3 -= m_beta * s2;
    qDot4 -= m_beta * s3;
  }

  // Integrate rate of change of quaternion to yield quaternion
  q.set_w(q.w() + qDot1 * (1.0 / m_sample_freq));
  q.set_x(q.x() + qDot2 * (1.0 / m_sample_freq));
  q.set_y(q.y() + qDot3 * (1.0 / m_sample_freq));
  q.set_z(q.z() + qDot4 * (1.0 / m_sample_freq));

  // Normalise quaternion
  q.normalize();
}
//////////////////////////////////////////////////////////////

double inv_sqrt(double x) {
  // Fast inverse square-root
  // See: http://en.wikipedia.org/wiki/Fast_inverse_square_root
  return 1.0 / sqrt(x); // fast enough :-D
}
