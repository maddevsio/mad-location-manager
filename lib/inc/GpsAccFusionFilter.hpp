#ifndef GPSACCKALMAN_H
#define GPSACCKALMAN_H

#include <cstdint>
#include "Kalman.hpp"

struct FusionFilterState {
  double x; // longitude in meters
  double y; // latitude in meters
  double x_vel;
  double y_vel;

  FusionFilterState() : x(0.0), y(0.0), x_vel(0.0), y_vel(0.0) {}
  FusionFilterState(double x_, double y_,
                    double x_vel_, double y_vel_) :
    x(x_), y(y_), x_vel(x_vel_), y_vel(y_vel_) {}
};
//////////////////////////////////////////////////////////////

// state dim = 4
// measure dim = 4
// control dim = 2
// todo make some interface with predict/update methods
class GPSAccFusionFilter : public KalmanFilter<4,4,2> {
private:
  static const size_t state_dim = 4;
  static const size_t measure_dim = 4;
  static const size_t control_dim = 2;

  double m_last_predict_ms;
  double m_acc_deviation; // accelerometer sigma
  FusionFilterState m_current_state;
  uint32_t m_predicts_count; // should be replaced with delta time

  void rebuild_F(double dt_ms);
  void rebuild_U(double xAcc,
                 double yAcc);
  void rebuild_B(double dt_ms);
  void rebuild_Q(double acc_deviation);
  void rebuild_R(double pos_sigma,
                 double vel_sigma);

public:
  GPSAccFusionFilter() = delete;
  GPSAccFusionFilter(const FusionFilterState &init_state,
                     double acc_deviation,
                     double pos_deviation,
                     double last_predict_ms);

  void predict(double xAcc,
               double yAcc,
               double time_ms);
  void update(const FusionFilterState &state,
              double pos_deviation,
              double vel_deviation = 0.0);

  const FusionFilterState& current_state() const {
    return m_current_state;
  }
};
//////////////////////////////////////////////////////////////

#endif // GPSACCKALMAN_H