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

class IGPSAccFusionFilter {
protected:
  FusionFilterState m_current_state;
public:
  virtual void predict(double xAcc, double yAcc) = 0;
  virtual void update(const FusionFilterState &state,
                      double pos_deviation) = 0;
  const FusionFilterState& current_state() const {return m_current_state;}
};
//////////////////////////////////////////////////////////////

class GPSAccFusionFilterGPSSpeed : public KalmanFilter<4,2,4>, IGPSAccFusionFilter {
private:
public:

};

class GPSAccFusionFilterNoGPSSpeed : public KalmanFilter<4,2,2>, IGPSAccFusionFilter {
private:
public:
};
//////////////////////////////////////////////////////////////

#endif // GPSACCKALMAN_H
