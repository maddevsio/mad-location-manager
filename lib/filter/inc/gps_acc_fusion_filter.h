#ifndef GPSACCKALMAN_H
#define GPSACCKALMAN_H

#include <cstdint>

#include "kalman.h"

struct FusionFilterState {
  double x;  // longitude in meters
  double y;  // latitude in meters
  double x_vel;
  double y_vel;

  FusionFilterState() : x(0.0), y(0.0), x_vel(0.0), y_vel(0.0) {}
  FusionFilterState(double x_, double y_, double x_vel_, double y_vel_)
      : x(x_), y(y_), x_vel(x_vel_), y_vel(y_vel_)
  {
  }

  friend std::ostream& operator<<(std::ostream& os,
                                  const FusionFilterState& obj);
};

//////////////////////////////////////////////////////////////

// state dim = 4
// measure dim = 4
// control dim = 2
class GPSAccFusionFilter : public KalmanFilter<4, 4, 2>
{
 private:
  double m_last_predict_sec;
  double m_acc_deviation;     // accelerometer sigma
  uint32_t m_predicts_count;  // should be replaced with delta time

  void rebuild_F(double dt_sec);
  void rebuild_U(double xAcc, double yAcc);
  void rebuild_B(double dt_sec);
  void rebuild_Q(double acc_deviation);
  void rebuild_R(double pos_sigma, double vel_sigma);

 public:
  GPSAccFusionFilter();

  void reset(double x,  // longitude in meters
             double y,  // latitude in meters
             double x_vel,
             double y_vel,
             double acc_deviation,
             double pos_deviation);

  void predict(double xAcc, double yAcc, double time_sec);
  void update(const FusionFilterState& state,
              double pos_deviation,
              double vel_deviation = 0.0);

  const FusionFilterState current_state() const;
};
//////////////////////////////////////////////////////////////

#endif  // GPSACCKALMAN_H
