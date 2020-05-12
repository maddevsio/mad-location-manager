#ifndef VECTOR3D_H
#define VECTOR3D_H

#ifdef __cplusplus
extern "C" {
#endif

typedef struct vector3d {
  float x, y, z, len;
} vector3d_t;

vector3d_t vector_new(float x, float y, float z);
void vector3d_normalize(vector3d_t *v3d);

float vector3d_rotation_sign(const vector3d_t *a, const vector3d_t *b);
float vector3d_flat_cos(const vector3d_t *a, const vector3d_t *b);

#ifdef __cplusplus
}
#endif // __cplusplus

#endif // VECTOR3D_H
