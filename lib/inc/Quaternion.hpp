#ifndef QUATERNION_H
#define QUATERNION_H

#include <stdint.h>
#include "Matrix.hpp"
#include "Vector3d.hpp"

class Quaternion {
private:
  double w_, x_, y_, z_;

  double len() const;
  Quaternion invert() const;

public:
  Quaternion();
  Quaternion(double w, double x, double y, double z);
  Quaternion(const Vector3D &rv, double angle_rads);
  ~Quaternion() = default;

  Quaternion& operator*=(double f);
  Quaternion& operator/=(double f); // todo check if we need this or we can use *= only
  friend Quaternion operator*(const Quaternion &a, const Quaternion &b);
  friend Quaternion operator*(const Quaternion &q, const Vector3D &v);
  Vector3D transform_vec(const Vector3D &v) const;

  double w() const {return w_;}
  double x() const {return x_;}
  double y() const {return y_;}
  double z() const {return z_;}

  void set_w(double w) { w_ = w;}
  void set_x(double x) { x_ = x;}
  void set_y(double y) { y_ = y;}
  void set_z(double z) { z_ = z;}

  void normalize();

  double pitch() const;
  double roll() const;
  double yaw() const;
};

#endif // QUATERNION_H
