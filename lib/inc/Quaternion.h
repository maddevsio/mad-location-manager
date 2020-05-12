#ifndef QUATERNION_H
#define QUATERNION_H

#include <cstdlib>

class vector3d_t {
private:
  float m_x, m_y, m_z, m_len;
public:
  static float rotation_sign(const vector3d_t &a, const vector3d_t &b);
  static float flat_cos(const vector3d_t &a, const vector3d_t &b);

  vector3d_t();
  vector3d_t(float m_x, float m_y, float m_z);
  vector3d_t(const vector3d_t &) = default;
  ~vector3d_t();

  void normalize();

  float x() const {return m_x;}
  float y() const {return m_y;}
  float z() const {return m_z;}
  float len() const {return m_len;}
};
///////////////////////////////////////////////////////

class quaternion_t {
public:
  float w, x, y, z;
  quaternion_t();
  quaternion_t(float w, float x, float y, float z);
  explicit quaternion_t(const vector3d_t &vec, float angle_rads);
  ~quaternion_t();

  quaternion_t& operator*=(const quaternion_t& rhs);

  void scale(float f);
  void normalize();
  float len() const;

  quaternion_t invert() const;

  // do we really need this?
  float pitch() const;
  float roll() const;
  float yaw() const;
};
///////////////////////////////////////////////////////

quaternion_t operator*(quaternion_t lhs, const quaternion_t& rhs);
quaternion_t operator*(const quaternion_t &q, const vector3d_t &v);
quaternion_t operator*(const vector3d_t &v, const quaternion_t &q);

vector3d_t quaternion_transform_vec(const quaternion_t &q,
                                    const vector3d_t &v);


#endif // QUATERNION_H
