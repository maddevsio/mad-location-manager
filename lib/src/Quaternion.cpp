#include <math.h>
#include "Quaternion.hpp"

Quaternion operator*(const Quaternion &a,
                     const Quaternion &b) {
  double w = a.w_*b.w_ - a.x_*b.x_ - a.y_*b.y_ - a.z_*b.z_;
  double x = a.w_*b.x_ + a.x_*b.w_ + a.y_*b.z_ - a.z_*b.y_;
  double y = a.w_*b.y_ - a.x_*b.z_ + a.y_*b.w_ + a.z_*b.x_;
  double z = a.w_*b.z_ + a.x_*b.y_ - a.y_*b.x_ + a.z_*b.w_;
  return Quaternion(w,x,y,z);
}
//////////////////////////////////////////////////////////////

Quaternion operator*(const Quaternion &a,
                     const Vector3D &b) {
  double w = -a.x_*b.x() - a.y_*b.y() - a.z_*b.z();
  double x = a.w_*b.x() + a.y_*b.z() - a.z_*b.y();
  double y = a.w_*b.y() - a.x_*b.z() + a.z_*b.x();
  double z = a.w_*b.z() + a.x_*b.y() - a.y_*b.x();
  return Quaternion(w,x,y,z);
}
///////////////////////////////////////////////////////

void
Quaternion::normalize() {
  (*this) /= len();
}
//////////////////////////////////////////////////////////////

double
Quaternion::len() const {
  return sqrt(w_*w_ + x_*x_ + y_*y_ + z_*z_);
}
//////////////////////////////////////////////////////////////

Quaternion
Quaternion::invert() const {
  Quaternion res(w_, -x_, -y_, -z_);
  res.normalize();
  return res;
}
//////////////////////////////////////////////////////////////

Quaternion::Quaternion() :
  w_(0.0),
  x_(0.0),
  y_(0.0),
  z_(0.0) {
}
//////////////////////////////////////////////////////////////

Quaternion::Quaternion(double w, double x,
                       double y, double z) :
  w_(w), x_(x),
  y_(y), z_(z) {
}
//////////////////////////////////////////////////////////////

Quaternion::Quaternion(const Vector3D &rv,
                       double angle_rads) {
  Vector3D rvn(rv);
  rvn.normalize();
  w_ = cos(angle_rads / 2.0);
  x_ = rvn.x() * sin(angle_rads / 2.0);
  y_ = rvn.y() * sin(angle_rads / 2.0);
  z_ = rvn.z() * sin(angle_rads / 2.0);
}
//////////////////////////////////////////////////////////////

Quaternion& Quaternion::operator*=(double f) {
  w_ *= f; x_ *= f;
  y_ *= f; z_ *= f;
  return *this;
}
//////////////////////////////////////////////////////////////

Quaternion &Quaternion::operator/=(double f) {
  w_ /= f; x_ /= f;
  y_ /= f; z_ /= f;
  return *this;
}
//////////////////////////////////////////////////////////////

Vector3D
Quaternion::transform_vec(const Vector3D &v) const {
  Quaternion t = (*this) * v;
  Quaternion qmq = t * this->invert();
  return Vector3D(qmq.x_, qmq.y_, qmq.z_);
}
//////////////////////////////////////////////////////////////

double
Quaternion::pitch() const {
  return asin(-2.0 * (x_*z_ - w_*y_));
}
//////////////////////////////////////////////////////////////

double
Quaternion::roll() const {
  return atan2(w_*x_ + y_*z_, 0.5 - x_*x_ - y_*y_);
}
//////////////////////////////////////////////////////////////

double
Quaternion::yaw() const {
  return atan2(x_*y_ + w_*z_, 0.5f - y_*y_ - z_*z_);
}
//////////////////////////////////////////////////////////////
