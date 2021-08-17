//#include <math.h>
//#include "Quaternion.hpp"

//static quaternion_t quaternion_new(void);
//static void quaternion_scale(quaternion_t *q, float f);
//static float quaternion_len(const quaternion_t *q);
//static void quaternion_normalize(quaternion_t *q);

//static quaternion_t quaternion_inv(const quaternion_t *q);
//static quaternion_t quaternion_mul_qq(const quaternion_t *a, const quaternion_t *b);
//static quaternion_t quaternion_mul_qv(const quaternion_t *a, const vector3d_t *b);

//quaternion_t quaternion_new() {
//  quaternion_t r = {.w = 0.0f, .x = 0.0f, .y = 0.0f, .z = 0.0f};
//  return r;
//}
/////////////////////////////////////////////////////////

//quaternion_t quaternion_new_wxyz(float w,
//                                 float x,
//                                 float y,
//                                 float z) {
//  quaternion_t r = {.w = w, .x = x, .y = y, .z = z};
//  return r;
//}
/////////////////////////////////////////////////////////

//quaternion_t quaternion_new_vec(const vector3d_t *rv,
//                                float angleRads) {
//  quaternion_t r;
//  vector3d_t rotate_vector = vector_new(rv->x, rv->y, rv->z);
//  vector3d_normalize(&rotate_vector);
//  r.w = cosf(angleRads / 2.0f);
//  r.x = rotate_vector.x * sinf(angleRads / 2.0f);
//  r.y = rotate_vector.y * sinf(angleRads / 2.0f);
//  r.z = rotate_vector.z * sinf(angleRads / 2.0f);
//  return r;
//}
/////////////////////////////////////////////////////////

//void quaternion_scale(quaternion_t *q, float f) {
//  q->w *= f; q->x *= f;
//  q->y *= f; q->z *= f;
//}
/////////////////////////////////////////////////////////

//float quaternion_len(const quaternion_t *q) {
//  return sqrtf(q->w*q->w + q->x*q->x + q->y*q->y + q->z*q->z);
//}
/////////////////////////////////////////////////////////

//void quaternion_normalize(quaternion_t *q) {
//  float len = quaternion_len(q);
//  quaternion_scale(q, len);
//}
/////////////////////////////////////////////////////////

//quaternion_t quaternion_inv(const quaternion_t *q) {
//  quaternion_t r = quaternion_new_wxyz(q->w, -q->x, -q->y, -q->z);
//  quaternion_normalize(&r);
//  return r;
//}
/////////////////////////////////////////////////////////

//quaternion_t quaternion_mul_qq(const quaternion_t *a,
//                               const quaternion_t *b) {
//  quaternion_t r = quaternion_new();
//  r.w = a->w * b->w - a->x * b->x - a->y * b->y - a->z * b->z;
//  r.x = a->w * b->x + a->x * b->w + a->y * b->z - a->z * b->y;
//  r.y = a->w * b->y - a->x * b->z + a->y * b->w + a->z * b->x;
//  r.z = a->w * b->z + a->x * b->y - a->y * b->x + a->z * b->w;
//  return r;
//}
/////////////////////////////////////////////////////////

//quaternion_t quaternion_mul_qv(const quaternion_t *a,
//                               const vector3d_t *b) {
//  quaternion_t r = quaternion_new();
//  r.w = -a->x * b->x - a->y * b->y - a->z * b->z;
//  r.x = a->w * b->x + a->y * b->z - a->z * b->y;
//  r.y = a->w * b->y - a->x * b->z + a->z * b->x;
//  r.z = a->w * b->z + a->x * b->y - a->y * b->x;
//  return r;
//}
/////////////////////////////////////////////////////////

//vector3d_t quaternion_transform_vec(const quaternion_t *q,
//                                    const vector3d_t *v) {
//  quaternion_t t = quaternion_mul_qv(q, v);
//  quaternion_t qi = quaternion_inv(q);
//  quaternion_t qmq = quaternion_mul_qq(&t, &qi);
//  return vector_new(qmq.x, qmq.y, qmq.z);
//}
/////////////////////////////////////////////////////////

//float quaternion_pitch(const quaternion_t *q) {
//  return asinf(-2.0f * (q->x*q->z - q->w*q->y));
//}
/////////////////////////////////////////////////////////

//float quaternion_roll(const quaternion_t *q) {
//  return atan2f(q->w*q->x + q->y*q->z, 0.5f - q->x*q->x - q->y*q->y);
//}
/////////////////////////////////////////////////////////

//float quaternion_yaw(const quaternion_t *q) {
//  return atan2f(q->x*q->y + q->w*q->z, 0.5f - q->y*q->y - q->z*q->z);
//}
/////////////////////////////////////////////////////////

//matrix_t *
//quaternion_to_matrix(const quaternion_t *q) {
//  matrix_t *mtx = matrix_alloc(3, 3);
//  mtx->data[0][0] = (double) (1.0f - 2.0f*q->y*q->y - 2.0f*q->z*q->z);
//  mtx->data[0][1] = (double) (2.0f*q->x*q->y - 2.0f*q->z*q->w);
//  mtx->data[0][2] = (double) (2.0f*q->x*q->z + 2.0f*q->y*q->w);
//  mtx->data[1][0] = (double) (2.0f*q->x*q->y + 2.0f*q->z*q->w);
//  mtx->data[1][1] = (double) (1.0f - 2.0f*q->x*q->x - 2.0f*q->z*q->z);
//  mtx->data[1][2] = (double) (2.0f*q->y*q->z - 2.0f*q->x*q->w);
//  mtx->data[2][0] = (double) (2.0f*q->x*q->z - 2.0f*q->y*q->w);
//  mtx->data[2][1] = (double) (2.0f*q->y*q->z + 2.0f*q->x*q->w);
//  mtx->data[2][2] = (double) (1.0f - 2.0f*q->x*q->x - 2.0f*q->y*q->y);
//  return mtx;
//}
/////////////////////////////////////////////////////////
