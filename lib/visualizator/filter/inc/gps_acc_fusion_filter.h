#ifndef GPSACCKALMAN_H
#define GPSACCKALMAN_H

#include "kalman.h"

struct FusionFilterState {
  double x;      // EAST -> longitude in meters
  double y;      // NORTH -> latitude in meters
  double x_vel;  // velocity pointing east
  double y_vel;  // velocity pointing north

  FusionFilterState() : x(0.0), y(0.0), x_vel(0.0), y_vel(0.0) {}
  FusionFilterState(double x_, double y_, double x_vel_, double y_vel_)
      : x(x_), y(y_), x_vel(x_vel_), y_vel(y_vel_)
  {
  }

  friend std::ostream& operator<<(std::ostream& os,
                                  const FusionFilterState& obj);
};

//////////////////////////////////////////////////////////////

// template <size_t state_dim, size_t measure_dim, size_t control_dim>
// state dim = 4
// measure dim = 4
// control dim = 2
class GPSAccFusionFilter : public KalmanFilter<4, 4, 2>
{
 private:
  double m_last_predict_sec;
  double m_acc_sigma_2;  // accelerometer sigma^2
  double m_pos_sigma_2;  // gps position sigma^2

  void rebuild_F(double dt_sec);
  void rebuild_U(double xAcc, double yAcc);
  void rebuild_B(double dt_sec);
  void rebuild_Q(double dt_sec);
  void rebuild_R(double pos_sigma_2, double vel_sigma_2);

 public:
  GPSAccFusionFilter();

  void reset(double x,   // EAST - longitude in meters
             double y,   // NORTH - latitude in meters
             double ts,  // last update (GPS coordinate) time
             double x_vel,
             double y_vel,
             double acc_sigma_2,
             double pos_sigma_2);

  void predict(double x_acc, double y_acc, double ts_sec);
  void update(const FusionFilterState& state,
              double pos_sigma_2,
              double vel_sigma_2 = 1e-6);

  const FusionFilterState current_state() const;
};
//////////////////////////////////////////////////////////////

#endif  // GPSACCKALMAN_H
