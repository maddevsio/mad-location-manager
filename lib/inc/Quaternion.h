#ifndef QUATERNION_H
#define QUATERNION_H

#include <stdint.h>
#include "Matrix.h"
#include "Vector3d.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef struct quaternion {
  float w, x, y, z;
} quaternion_t;

quaternion_t quaternion_new_wxyz(float w, float x, float y, float z);
quaternion_t quaternion_new_vec(const vector3d_t *rv, float angleRads);
vector3d_t quaternion_transform_vec(const quaternion_t *q, const vector3d_t *v);

matrix_t *quaternion_to_matrix(const quaternion_t *q);

float quaternion_pitch(const quaternion_t *q);
float quaternion_roll(const quaternion_t *q);
float quaternion_yaw(const quaternion_t *q);


#ifdef __cplusplus
}
#endif // extern "C"
#endif // QUATERNION_H
