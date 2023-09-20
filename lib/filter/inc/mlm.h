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

 public:
  MLM(void);
  ~MLM(void);
  // predict
  void process_acc_data(const abs_accelerometer &acc, double time_ms);
  // update
  void process_gps_data(const gps_coordinate &gps,
                        double pos_deviation,
                        double vel_deviation);

  gps_coordinate predicted_coordinate() const;
};

#endif
