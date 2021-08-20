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
// Date         Author          Notes
// 19/08/2021   Lezh1k          Reimplemented using double precision numbers + C++
//=====================================================================================================
#ifndef MadgwickAHRS_h
#define MadgwickAHRS_h

#include "Quaternion.hpp"

class MadgwickFilter {
private:
  double m_beta;
  double m_sample_freq;
  Quaternion q;

public:
  MadgwickFilter() = delete;
  MadgwickFilter(double beta, double sample_freq_HZ);
  ~MadgwickFilter() = default;

  void AHRS_update(double gx, double gy, double gz,
                   double ax, double ay, double az,
                   double mx, double my, double mz);
  void AHRS_update_IMU(double gx, double gy, double gz,
                       double ax, double ay, double az);
};

#endif // MadgwickAHRS_h
