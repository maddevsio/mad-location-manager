#include <math.h>
#include "Quaternion.h"

float
vector3d_t::rotation_sign(const vector3d_t &a,
                          const vector3d_t &b) {
  float sign = a.m_x*b.m_y - a.m_y*b.m_x;
  return sign >= 0.0f ? 1.0f : -1.0f;
}
///////////////////////////////////////////////////////

float
vector3d_t::flat_cos(const vector3d_t &a,
                     const vector3d_t &b) {
  float num = a.m_x * b.m_x + a.m_y * b.m_y;
  float denom = sqrtf(a.m_x*a.m_x + a.m_y*a.m_y) * sqrtf(b.m_x*b.m_x + b.m_y*b.m_y);
  return num / denom;
}
///////////////////////////////////////////////////////

vector3d_t::vector3d_t() :
  m_x(0.0f),
  m_y(0.0f),
  m_z(0.0f),
  m_len(0.0) {}

vector3d_t::vector3d_t(float x,
                       float y,
                       float z) :
  m_x(x),
  m_y(y),
  m_z(z) {
  m_len = sqrtf(x*x + y*y + z*z);
}

vector3d_t::~vector3d_t() {
  //do nothing
}
///////////////////////////////////////////////////////

void vector3d_t::normalize() {
  m_x /= m_len;
  m_y /= m_len;
  m_z /= m_len;
  m_len = 1.0f; //normalized
}
///////////////////////////////////////////////////////


vector3d_t quaternion_transform_vec(const quaternion_t &q,
                                    const vector3d_t &v) {  
  quaternion_t t = q * v;
  quaternion_t qi = q.invert();
  t *= qi;
  return vector3d_t(t.x, t.y, t.z);
}
///////////////////////////////////////////////////////
///////////////////////////////////////////////////////

quaternion_t::quaternion_t() :
  w(0.0f),
  x(0.0f),
  y(0.0f),
  z(0.0f) {
}

quaternion_t::quaternion_t(float w,
                           float x,
                           float y,
                           float z) :
  w(w),
  x(x),
  y(y),
  z(z) {
}

quaternion_t::quaternion_t(const vector3d_t &vec,
                           float angle_rads) {
  vector3d_t rotate_vector(vec);
  rotate_vector.normalize();
  w = cosf(angle_rads / 2.0f);
  x = rotate_vector.x() * sinf(angle_rads/ 2.0f);
  y = rotate_vector.y() * sinf(angle_rads / 2.0f);
  z = rotate_vector.z() * sinf(angle_rads / 2.0f);
}

quaternion_t::~quaternion_t() {
  //do nothing
}
///////////////////////////////////////////////////////

quaternion_t&
quaternion_t::operator*=(const quaternion_t &b) {
  w = w * b.w - x * b.x - y * b.y - z * b.z;
  x = w * b.x + x * b.w + y * b.z - z * b.y;
  y = w * b.y - x * b.z + y * b.w + z * b.x;
  z = w * b.z + x * b.y - y * b.x + z * b.w;
  return *this;
}

quaternion_t
operator*(quaternion_t lhs, const quaternion_t &rhs) {
  return lhs *= rhs;
}
///////////////////////////////////////////////////////

void
quaternion_t::scale(float f) {
  w *= f; x *= f;
  y *= f; z *= f;
}

void
quaternion_t::normalize() {
  this->scale(this->len());
}
///////////////////////////////////////////////////////

float
quaternion_t::len() const {
  return sqrtf(w*w + x*x + y*y + z*z);
}
///////////////////////////////////////////////////////

quaternion_t
quaternion_t::invert() const {
  quaternion_t r(w, -x, -y, -z);
  r.normalize();
  return r;
}
///////////////////////////////////////////////////////

float
quaternion_t::pitch() const {
  return asinf(-2.0f * (x*z - w*y));
}
///////////////////////////////////////////////////////

float
quaternion_t::roll() const {
  return atan2f(w*x + y*z, 0.5f - x*x - y*y);
}
///////////////////////////////////////////////////////

float
quaternion_t::yaw() const {
  return atan2f(x*y + w*z, 0.5f - y*y - z*z);
}
///////////////////////////////////////////////////////

quaternion_t
operator*(const vector3d_t &v, const quaternion_t &q) {
  return q*v;
}

quaternion_t
operator*(const quaternion_t &q, const vector3d_t &v) {
  quaternion_t r;
  r.w = -q.x * v.x() - q.y * v.y() - q.z * v.z();
  r.x = q.w * v.x() + q.y * v.z() - q.z * v.y();
  r.y = q.w * v.y() - q.x * v.z() + q.z * v.x();
  r.z = q.w * v.z() + q.x * v.y() - q.y * v.x();
  return r;
}
