#ifndef VECTOR3D_H
#define VECTOR3D_H

#include "Matrix.hpp"

class Vector3D {
private:
  double x_, y_, z_, len_;

public:
  Vector3D();
  Vector3D(const Vector3D &v) = default;
  Vector3D(double x,
           double y,
           double z);
  ~Vector3D() = default;

  void normalize();
  static double rotation_sign(const Vector3D &a,
                              const Vector3D &b);
  static double flat_cos(const Vector3D &a,
                         const Vector3D &b);

  double x() const {return x_;}
  double y() const {return y_;}
  double z() const {return z_;}
  double len() const {return len_;}
};


#endif // VECTOR3D_H
