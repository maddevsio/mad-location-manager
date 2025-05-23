#ifndef MLM_HEADER
#define MLM_HEADER

#include <GeographicLib/LocalCartesian.hpp>

#include "gps_acc_fusion_filter.h"
#include "sensor_data.h"

class MLM
{
 private:
  GPSAccFusionFilter m_fk;
  GeographicLib::LocalCartesian m_lc;
  bool m_got_start_point;

  // accelerometer sigma^2
  double m_acc_sigma_2;
  // location sigma^2
  double m_loc_sigma_2;
  // velocity sigma^2
  double m_vel_sigma_2;

 public:
  MLM(void);
  MLM(double acc_sigma_2, double loc_sigma_2, double vel_sigma_2);
  ~MLM(void);

  // predict
  bool process_acc_data(const enu_accelerometer &acc, double time_sec);
  // update
  void process_gps_data(const gps_coordinate &gps, double time_sec);
  // current state
  gps_coordinate predicted_coordinate() const;
};

#endif
