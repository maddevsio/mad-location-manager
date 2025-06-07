#ifndef KALMAN_H
#define KALMAN_H

#include <Eigen/Eigen>
#include <cstddef>
#include <iostream>

template <size_t state_d, size_t measure_d, size_t control_d>
class KalmanFilter
{
#define mtx_t Eigen::Matrix
 protected:
  /*these matrices should be provided by user*/
  mtx_t<double, state_d, state_d> F;      // state transition model
  mtx_t<double, measure_d, state_d> H;    // observation model
  mtx_t<double, state_d, control_d> B;    // control matrix
  mtx_t<double, state_d, state_d> Q;      // process noise covariance
  mtx_t<double, measure_d, measure_d> R;  // observation noise covariance
  mtx_t<double, control_d, 1> Uk;         // control vector
  mtx_t<double, measure_d, 1> Zk;         // actual measured values vector

  /*these matrices will be calculated*/
  mtx_t<double, state_d, 1> Xk_km1;        // predicted state estimate
  mtx_t<double, state_d, state_d> Pk_km1;  // predicted estimate covariance
  mtx_t<double, measure_d, 1> Yk;          // measurement innovation
  mtx_t<double, measure_d, measure_d> Sk;  // innovation covariance
  mtx_t<double, state_d, measure_d> K;     // Kalman gain
  mtx_t<double, state_d, 1> Xk_k;          // updated (current) state
  mtx_t<double, measure_d, 1> Yk_k;        // post fit residual
  mtx_t<double, state_d, state_d> Pk_k;    // updated estimate covariance

  /*auxiliary matrices*/
  mtx_t<double, state_d, state_d> I;  // (I - Kk*Hk)
  KalmanFilter() : I(mtx_t<double, state_d, state_d>::Identity()) {}
  //////////////////////////////////////////////////////////////

  // predict
  void estimate()
  {
    // Xk|k-1 = Fk*Xk-1|k-1 + Bk*Uk
    Xk_km1 = F * Xk_k + B * Uk;
    // Pk|k-1 = Fk*Pk-1|k-1*Fk(t) + Qk
    Pk_km1 = F * Pk_k * F.transpose() + Q;
  }

  // update
  void correct()
  {
    // Yk = Zk - Hk*Xk|k-1
    Yk = Zk - H * Xk_km1;
    // Sk = Rk + Hk*Pk|k-1*Hk(t)
    Sk = R + H * Pk_km1 * H.transpose();

    // Kk = Pk|k-1*Hk(t)*Sk(inv)
    K = Pk_km1 * H.transpose() * Sk.inverse();
    // xk|k = xk|k-1 + Kk*Yk
    Xk_k = Xk_km1 + K * Yk;

    // Pk|k = (I - Kk*Hk) * Pk|k-1 * (I - Kk*Hk).t() + (K * R * K.t())
    Pk_k = (I - K * H) * Pk_km1 * (I - K * H).transpose() +
           (K * R * K.transpose());

    // Yk|k = Zk - Hk*Xk|k
    Yk_k = Zk - H * Xk_k;
  }

#undef mtx_t
};
//////////////////////////////////////////////////////////////

#endif  // KALMAN_H
