#include <stdint.h>
#include <assert.h>
#include <time.h>
#include <stdlib.h>
#include <stddef.h>
#include <memory>
#include <functional>
#include <gtest/gtest.h>
#include "Matrix.h"

typedef std::unique_ptr<matrix_t, std::function<void(matrix_t*)> > matrix_unique_ptr_t;

static matrix_unique_ptr_t create_matrix_uptr(uint32_t cols, uint32_t rows) {
  return matrix_unique_ptr_t(matrix_alloc(cols, rows), matrix_free);
}

TEST(matrix, matrixSet) {
  auto m1 = create_matrix_uptr(3, 3);
  matrix_set(m1.get(),
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0,
            7.0, 8.0, 9.0);
  ASSERT_EQ(m1->data[0][0], 1.0);
  ASSERT_EQ(m1->data[0][1], 2.0);
  ASSERT_EQ(m1->data[0][2], 3.0);
  ASSERT_EQ(m1->data[1][0], 4.0);
  ASSERT_EQ(m1->data[1][1], 5.0);
  ASSERT_EQ(m1->data[1][2], 6.0);
  ASSERT_EQ(m1->data[2][0], 7.0);
  ASSERT_EQ(m1->data[2][1], 8.0);
  ASSERT_EQ(m1->data[2][2], 9.0);

  auto m2 = create_matrix_uptr(3, 1);
  matrix_set(m2.get(), 0.0, 1.1, 2.2);
  ASSERT_EQ(m2->data[0][0], 0.0);
  ASSERT_EQ(m2->data[1][0], 1.1);
  ASSERT_EQ(m2->data[2][0], 2.2);  
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixCopy) {  
  auto m1 = create_matrix_uptr(3, 3);
  auto m2 = create_matrix_uptr(3, 3);
  matrix_set(m1.get(),
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0,
            7.0, 8.0, 9.0);

  matrix_copy(m1.get(), m2.get());
  ASSERT_EQ(m2->data[0][0], 1.0);
  ASSERT_EQ(m2->data[0][1], 2.0);
  ASSERT_EQ(m2->data[0][2], 3.0);
  ASSERT_EQ(m2->data[1][0], 4.0);
  ASSERT_EQ(m2->data[1][1], 5.0);
  ASSERT_EQ(m2->data[1][2], 6.0);
  ASSERT_EQ(m2->data[2][0], 7.0);
  ASSERT_EQ(m2->data[2][1], 8.0);
  ASSERT_EQ(m2->data[2][2], 9.0);
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixAdd) {
  auto ma = create_matrix_uptr(2, 3);
  auto mb = create_matrix_uptr(2, 3);
  auto mc = create_matrix_uptr(2, 3);

  matrix_set(ma.get(),
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0);
  matrix_set(mb.get(),
            7.0, 8.0, 9.0,
            10.0, 11.0, 12.0);
  matrix_add(ma.get(), mb.get(), mc.get());
  ASSERT_EQ(mc->data[0][0], 8.0);
  ASSERT_EQ(mc->data[0][1], 10.0);
  ASSERT_EQ(mc->data[0][2], 12.0);
  ASSERT_EQ(mc->data[1][0], 14.0);
  ASSERT_EQ(mc->data[1][1], 16.0);
  ASSERT_EQ(mc->data[1][2], 18.0);

  matrix_add(ma.get(), mc.get(), ma.get()); //inplace test
  ASSERT_EQ(ma->data[0][0], 9.0);
  ASSERT_EQ(ma->data[0][1], 12.0);
  ASSERT_EQ(ma->data[0][2], 15.0);
  ASSERT_EQ(ma->data[1][0], 18.0);
  ASSERT_EQ(ma->data[1][1], 21.0);
  ASSERT_EQ(ma->data[1][2], 24.0);
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixSubstract) {  
  auto ma = create_matrix_uptr(2, 3);
  auto mb = create_matrix_uptr(2, 3);
  auto mc = create_matrix_uptr(2, 3);

  matrix_set(ma.get(),
            12.0, 11.0, 10.0,
            9.0,  8.0,  7.0);
  matrix_set(mb.get(),
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0);

  matrix_subtract(ma.get(), mb.get(), mc.get());
  ASSERT_EQ(mc->data[0][0], 11.0);
  ASSERT_EQ(mc->data[0][1], 9.0);
  ASSERT_EQ(mc->data[0][2], 7.0);
  ASSERT_EQ(mc->data[1][0], 5.0);
  ASSERT_EQ(mc->data[1][1], 3.0);
  ASSERT_EQ(mc->data[1][2], 1.0);

  matrix_subtract(ma.get(), mc.get(), ma.get()); //inplace test
  ASSERT_EQ(ma->data[0][0], 1.0);
  ASSERT_EQ(ma->data[0][1], 2.0);
  ASSERT_EQ(ma->data[0][2], 3.0);
  ASSERT_EQ(ma->data[1][0], 4.0);
  ASSERT_EQ(ma->data[1][1], 5.0);
  ASSERT_EQ(ma->data[1][2], 6.0);
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixSubstractFromIdentity) {  
  auto m = create_matrix_uptr(3, 3);
  matrix_set(m.get(),
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0,
            7.0, 8.0, 9.0);
  matrix_subtract_from_identity(m.get());

  ASSERT_EQ(m->data[0][0], 0.0);
  ASSERT_EQ(m->data[0][1], -2.0);
  ASSERT_EQ(m->data[0][2], -3.0);
  ASSERT_EQ(m->data[1][0], -4.0);
  ASSERT_EQ(m->data[1][1], -4.0);
  ASSERT_EQ(m->data[1][2], -6.0);
  ASSERT_EQ(m->data[2][0], -7.0);
  ASSERT_EQ(m->data[2][1], -8.0);
  ASSERT_EQ(m->data[2][2], -8.0);
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixMultiply) {  
  auto ma = create_matrix_uptr(3, 3);
  auto mb = create_matrix_uptr(3, 3);
  auto mc = create_matrix_uptr(3, 3);
  auto md = create_matrix_uptr(3, 3);

  matrix_set(ma.get(),
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0,
            7.0, 8.0, 9.0);

  matrix_set(mb.get(),
            10.0, 11.0, 12.0,
            13.0, 14.0, 15.0,
            16.0, 17.0, 18.0);

  matrix_set(mc.get(),
            84.0, 90.0, 96.0,
            201.0, 216.0, 231.0,
            318.0, 342.0, 366.0);
  matrix_multiply(ma.get(), mb.get(), md.get());
  ASSERT_TRUE(matrix_eq(md.get(), mc.get(), 1e-06));
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixMultiplyByTranspose) {  
  auto ma = create_matrix_uptr(3, 3);
  auto mb = create_matrix_uptr(3, 3);
  auto mc = create_matrix_uptr(3, 3);
  auto md = create_matrix_uptr(3, 3);

  matrix_set(ma.get(),
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0,
            7.0, 8.0, 9.0);

  matrix_set(mb.get(),
            10.0, 11.0, 12.0,
            13.0, 14.0, 15.0,
            16.0, 17.0, 18.0);

  matrix_set(mc.get(),
            68.0, 86.0, 104.0,
            167.0, 212.0, 257.0,
            266.0, 338.0, 410.0);

  matrix_multiply_by_transpose(ma.get(), mb.get(), md.get());
  ASSERT_TRUE(matrix_eq(mc.get(), md.get(), 1e-06));
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixTranspose) {  
  auto ma = create_matrix_uptr(3, 1);
  auto mb = create_matrix_uptr(1, 3);
  auto mc = create_matrix_uptr(1, 3);
  matrix_set(ma.get(), 1.0, 2.0, 3.0);
  matrix_set(mb.get(), 1.0, 2.0, 3.0);
  matrix_transpose(ma.get(), mc.get());
  ASSERT_TRUE(matrix_eq(mb.get(), mc.get(), 1e-06));
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixEq) {  
  auto m1 = create_matrix_uptr(3, 3);
  auto m2 = create_matrix_uptr(3, 3);
  for (uint32_t r = 0; r < 3; ++r) {
    for (uint32_t c = 0; c < 3; ++c) {
      m1->data[r][c] = m2->data[r][c] = rand();
    }
  }
  ASSERT_TRUE(matrix_eq(m1.get(), m2.get(), 1e-06));
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixScale) {  
  double scalar = 2.5;
  auto ma = create_matrix_uptr(2, 2);
  auto mb = create_matrix_uptr(2, 2);

  matrix_set(ma.get(),
            1.0, 2.0,
            3.0, 4.0);
  matrix_set(mb.get(),
            2.5, 5.0,
            7.5, 10.0);
  matrix_scale(ma.get(), scalar);
  ASSERT_TRUE(matrix_eq(ma.get(), mb.get(), 1e-06));
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixDestructiveInvert) {  
  auto ma = create_matrix_uptr(3, 3);
  auto mb = create_matrix_uptr(3, 3);
  auto mc = create_matrix_uptr(3, 3);

  matrix_set(ma.get(),
            5.0, 2.0, 3.0,
            8.0, 12.0, 22.0,
            39.0, 3.0, 11.0);

  matrix_set(mc.get(),
            33.0 / 269.0, -13.0 / 538.0, 4.0 / 269.0,
            385.0 / 269.0, -31.0 / 269.0, -43.0 / 269.0,
            -222.0 / 269.0, 63.0 / 538.0, 22.0 / 269.0);

  matrix_destructive_invert(ma.get(), mb.get());
  ASSERT_TRUE(matrix_eq(mb.get(), mc.get(), 1e-06));
}
//////////////////////////////////////////////////////////////////////////
