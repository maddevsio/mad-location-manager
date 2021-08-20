#ifndef KALMAN_H
#define KALMAN_H

#include "Matrix.hpp"

template <size_t state_dim, size_t measure_dim, size_t control_dim>
class KalmanFilter {
protected:
  /*these matrices should be provided by user*/
  Matrix<double, state_dim, state_dim>      F; // state transition model
  Matrix<double, measure_dim, state_dim>    H; // observation model
  Matrix<double, state_dim, control_dim>    B; // control matrix
  Matrix<double, state_dim, state_dim>      Q; // process noise covariance
  Matrix<double, measure_dim, measure_dim>  R; // observation noise covariance

  /*these matrices will be updated by user*/
  Matrix<double, control_dim, 1> Uk;            // control vector
  Matrix<double, measure_dim, 1> Zk;            // actual measured values vector
  Matrix<double, state_dim, 1> Xk_km1;          // predicted state estimate
  Matrix<double, state_dim, state_dim> Pk_km1;  // predicted estimate covariance
  Matrix<double, measure_dim, 1> Yk;            // measurement innovation

  Matrix<double, measure_dim, measure_dim> Sk;    // innovation covariance
  Matrix<double, measure_dim, measure_dim> SkInv; // innovation covariance inverse

  Matrix<double, state_dim, measure_dim> K;   // Kalman gain
  Matrix<double, state_dim, 1> Xk_k;          // updated (current) state
  Matrix<double, state_dim, state_dim> Pk_k;  // updated estimate covariance
  Matrix<double, measure_dim, 1> Yk_k;        //post fit residual

  /*auxiliary matrices*/
  Matrix<double, state_dim, state_dim> I; //(I - Kk*Hk)

public:

  KalmanFilter() :
    I(Matrix<double, state_dim, state_dim>::Identity()) {
  }
  //////////////////////////////////////////////////////////////

  void predict() {
    //Xk|k-1 = Fk*Xk-1|k-1 + Bk*Uk
    Xk_km1 = F*Xk_k + B*Uk;
    //Pk|k-1 = Fk*Pk-1|k-1*Fk(t) + Qk
    Pk_km1 = F*Pk_k*F.transpose() + Q;
  }

  void update() {
    //Yk = Zk - Hk*Xk|k-1
    Yk = Zk - H*Xk_km1;
    //Sk = Rk + Hk*Pk|k-1*Hk(t)
    Sk = R + H * Pk_km1 * H.transpose();
    //Kk = Pk|k-1*Hk(t)*Sk(inv)
    K = Pk_km1 * H.transpose() * Sk.invert();
    //xk|k = xk|k-1 + Kk*Yk
    Xk_k = Xk_km1 + K * Yk;
    //Pk|k = (I - Kk*Hk) * Pk|k-1 - SEE WIKI!!!
    Pk_k = (I - K * H) * Pk_km1;
    //Yk|k = Zk - Hk*Xk|k
    Yk_k = Zk - H * Xk_k;
  }
};
//////////////////////////////////////////////////////////////

#endif // KALMAN_H
