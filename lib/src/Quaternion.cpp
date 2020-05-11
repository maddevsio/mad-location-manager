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

static quaternion_t quaternion_inv(const quaternion_t &q);
static quaternion_t quaternion_mul_qv(const quaternion_t &a, const vector3d_t &b);

quaternion_t quaternion_inv(const quaternion_t &q) {
  quaternion_t r(q.w, -q.x, -q.y, -q.z);
  r.normalize();
  return r;
}
///////////////////////////////////////////////////////

quaternion_t quaternion_mul_qv(const quaternion_t &a,
                               const vector3d_t &b) {
  quaternion_t r;
  r.w = -a.x * b.x() - a.y * b.y() - a.z * b.z();
  r.x = a.w * b.x() + a.y * b.z() - a.z * b.y();
  r.y = a.w * b.y() - a.x * b.z() + a.z * b.x();
  r.z = a.w * b.z() + a.x * b.y() - a.y * b.x();
  return r;
}
///////////////////////////////////////////////////////

vector3d_t quaternion_transform_vec(const quaternion_t &q,
                                    const vector3d_t &v) {
  /*Quaternion t = quatMulVector(q, v);
        Quaternion qi = invert(q);
        Quaternion qmq = quatMulQuat(t, qi);
        return new Vector3D(qmq.x, qmq.y, qmq.z);*/
  quaternion_t t = quaternion_mul_qv(q, v);
  quaternion_t qi = quaternion_inv(q);
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

float quaternion_t::len() const {
  return sqrtf(w*w + x*x + y*y + z*z);
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
