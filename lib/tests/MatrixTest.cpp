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
  return matrix_unique_ptr_t(MatrixAlloc(cols, rows), MatrixFree);
}

TEST(matrix, matrixSet) {
  auto m1 = create_matrix_uptr(3, 3);
  MatrixSet(m1.get(),
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
  MatrixSet(m2.get(), 0.0, 1.1, 2.2);
  ASSERT_EQ(m2->data[0][0], 0.0);
  ASSERT_EQ(m2->data[1][0], 1.1);
  ASSERT_EQ(m2->data[2][0], 2.2);  
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixCopy) {  
  auto m1 = create_matrix_uptr(3, 3);
  auto m2 = create_matrix_uptr(3, 3);
  MatrixSet(m1.get(),
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0,
            7.0, 8.0, 9.0);

  MatrixCopy(m1.get(), m2.get());
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

  MatrixSet(ma.get(),
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0);
  MatrixSet(mb.get(),
            7.0, 8.0, 9.0,
            10.0, 11.0, 12.0);
  MatrixAdd(ma.get(), mb.get(), mc.get());
  ASSERT_EQ(mc->data[0][0], 8.0);
  ASSERT_EQ(mc->data[0][1], 10.0);
  ASSERT_EQ(mc->data[0][2], 12.0);
  ASSERT_EQ(mc->data[1][0], 14.0);
  ASSERT_EQ(mc->data[1][1], 16.0);
  ASSERT_EQ(mc->data[1][2], 18.0);

  MatrixAdd(ma.get(), mc.get(), ma.get()); //inplace test
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

  MatrixSet(ma.get(),
            12.0, 11.0, 10.0,
            9.0,  8.0,  7.0);
  MatrixSet(mb.get(),
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0);

  MatrixSubtract(ma.get(), mb.get(), mc.get());
  ASSERT_EQ(mc->data[0][0], 11.0);
  ASSERT_EQ(mc->data[0][1], 9.0);
  ASSERT_EQ(mc->data[0][2], 7.0);
  ASSERT_EQ(mc->data[1][0], 5.0);
  ASSERT_EQ(mc->data[1][1], 3.0);
  ASSERT_EQ(mc->data[1][2], 1.0);

  MatrixSubtract(ma.get(), mc.get(), ma.get()); //inplace test
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
  MatrixSet(m.get(),
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0,
            7.0, 8.0, 9.0);
  MatrixSubtractFromIdentity(m.get());

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

  MatrixSet(ma.get(),
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0,
            7.0, 8.0, 9.0);

  MatrixSet(mb.get(),
            10.0, 11.0, 12.0,
            13.0, 14.0, 15.0,
            16.0, 17.0, 18.0);

  MatrixSet(mc.get(),
            84.0, 90.0, 96.0,
            201.0, 216.0, 231.0,
            318.0, 342.0, 366.0);
  MatrixMultiply(ma.get(), mb.get(), md.get());
  ASSERT_TRUE(MatrixEq(md.get(), mc.get(), 1e-06));
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixMultiplyByTranspose) {  
  auto ma = create_matrix_uptr(3, 3);
  auto mb = create_matrix_uptr(3, 3);
  auto mc = create_matrix_uptr(3, 3);
  auto md = create_matrix_uptr(3, 3);

  MatrixSet(ma.get(),
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0,
            7.0, 8.0, 9.0);

  MatrixSet(mb.get(),
            10.0, 11.0, 12.0,
            13.0, 14.0, 15.0,
            16.0, 17.0, 18.0);

  MatrixSet(mc.get(),
            68.0, 86.0, 104.0,
            167.0, 212.0, 257.0,
            266.0, 338.0, 410.0);

  MatrixMultiplyByTranspose(ma.get(), mb.get(), md.get());
  ASSERT_TRUE(MatrixEq(mc.get(), md.get(), 1e-06));
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixTranspose) {  
  auto ma = create_matrix_uptr(3, 1);
  auto mb = create_matrix_uptr(1, 3);
  auto mc = create_matrix_uptr(1, 3);
  MatrixSet(ma.get(), 1.0, 2.0, 3.0);
  MatrixSet(mb.get(), 1.0, 2.0, 3.0);
  MatrixTranspose(ma.get(), mc.get());
  ASSERT_TRUE(MatrixEq(mb.get(), mc.get(), 1e-06));
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
  ASSERT_TRUE(MatrixEq(m1.get(), m2.get(), 1e-06));
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixScale) {  
  double scalar = 2.5;
  auto ma = create_matrix_uptr(2, 2);
  auto mb = create_matrix_uptr(2, 2);

  MatrixSet(ma.get(),
            1.0, 2.0,
            3.0, 4.0);
  MatrixSet(mb.get(),
            2.5, 5.0,
            7.5, 10.0);
  MatrixScale(ma.get(), scalar);
  ASSERT_TRUE(MatrixEq(ma.get(), mb.get(), 1e-06));
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixDestructiveInvert) {  
  auto ma = create_matrix_uptr(3, 3);
  auto mb = create_matrix_uptr(3, 3);
  auto mc = create_matrix_uptr(3, 3);

  MatrixSet(ma.get(),
            5.0, 2.0, 3.0,
            8.0, 12.0, 22.0,
            39.0, 3.0, 11.0);

  MatrixSet(mc.get(),
            33.0 / 269.0, -13.0 / 538.0, 4.0 / 269.0,
            385.0 / 269.0, -31.0 / 269.0, -43.0 / 269.0,
            -222.0 / 269.0, 63.0 / 538.0, 22.0 / 269.0);

  MatrixDestructiveInvert(ma.get(), mb.get());
  ASSERT_TRUE(MatrixEq(mb.get(), mc.get(), 1e-06));
}
//////////////////////////////////////////////////////////////////////////
