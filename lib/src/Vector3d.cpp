#include <math.h>
#include "Vector3d.hpp"

vector3d_t vector_new(float x, float y, float z) {
  vector3d_t r = {.x = x, .y = y, .z = z};
  r.len = sqrtf(x*x + y*y + z*z);
  return r;
}
///////////////////////////////////////////////////////

void vector3d_normalize(vector3d_t *v3d) {
  v3d->x /= v3d->len;
  v3d->y /= v3d->len;
  v3d->z /= v3d->len;
  v3d->len = 1.0f; //normalized
}
///////////////////////////////////////////////////////

float vector3d_rotation_sign(const vector3d_t *a,
                             const vector3d_t *b) {
  float sign = a->x * b->y - a->y * b->x;
  return sign >= 0.0f ? 1.0f : -1.0f;
}
///////////////////////////////////////////////////////

float vector3d_flat_cos(const vector3d_t *a,
                        const vector3d_t *b) {
  float num = a->x * b->x + a->y * b->y;
  float denom = sqrtf(a->x*a->x + a->y*a->y) * sqrtf(b->x*b->x + b->y*b->y);
  return num / denom;
}
