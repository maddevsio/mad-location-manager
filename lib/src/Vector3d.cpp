#include "Vector3d.hpp"
#include <math.h>

Vector3D::Vector3D() : x_(0.0), y_(0.0), z_(0.0), len_(0.0) {}
//////////////////////////////////////////////////////////////

Vector3D::Vector3D(double x, double y, double z)
    : x_(x), y_(y), z_(z),
    len_(sqrt(x * x + y * y + z * z)) {}
//////////////////////////////////////////////////////////////

void Vector3D::normalize() {
  x_ /= len_;
  y_ /= len_;
  z_ /= len_;
  len_ = 1.0; // normalized
}
//////////////////////////////////////////////////////////////

double Vector3D::rotation_sign(const Vector3D &a, const Vector3D &b) {
  double sign = a.x_ * b.y_ - a.y_ * b.x_;
  return sign >= 0.0 ? 1.0 : -1.0;
}
//////////////////////////////////////////////////////////////

double Vector3D::flat_cos(const Vector3D &a, const Vector3D &b) {
  double num = a.x_ * b.x_ + a.y_ * b.y_;
  double denom =
      sqrt(a.x_ * a.x_ + a.y_ * a.y_) * sqrt(b.x_ * b.x_ + b.y_ * b.y_);
  return num / denom;
}
//////////////////////////////////////////////////////////////
