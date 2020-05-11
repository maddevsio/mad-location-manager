#include <stdint.h>
#include <assert.h>
#include <time.h>
#include <stdlib.h>
#include <stddef.h>
#include <gtest/gtest.h>
#include "Matrix.h"

TEST(matrix, matrixSet) {
  matrix_t *m1, *m2;
  m1 = MatrixAlloc(3, 3);
  MatrixSet(m1,
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

  m2 = MatrixAlloc(3, 1);
  MatrixSet(m2, 0.0, 1.1, 2.2);
  ASSERT_EQ(m2->data[0][0], 0.0);
  ASSERT_EQ(m2->data[1][0], 1.1);
  ASSERT_EQ(m2->data[2][0], 2.2);

  MatrixFree(m1);
  MatrixFree(m2);
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixCopy) {
  matrix_t *m1, *m2;
  m1 = MatrixAlloc(3, 3);
  m2 = MatrixAlloc(3, 3);
  MatrixSet(m1,
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0,
            7.0, 8.0, 9.0);

  MatrixCopy(m1, m2);
  ASSERT_EQ(m2->data[0][0], 1.0);
  ASSERT_EQ(m2->data[0][1], 2.0);
  ASSERT_EQ(m2->data[0][2], 3.0);
  ASSERT_EQ(m2->data[1][0], 4.0);
  ASSERT_EQ(m2->data[1][1], 5.0);
  ASSERT_EQ(m2->data[1][2], 6.0);
  ASSERT_EQ(m2->data[2][0], 7.0);
  ASSERT_EQ(m2->data[2][1], 8.0);
  ASSERT_EQ(m2->data[2][2], 9.0);
  MatrixFree(m1);
  MatrixFree(m2);
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixAdd) {
  matrix_t *ma, *mb, *mc;
  ma = MatrixAlloc(2, 3);
  mb = MatrixAlloc(2, 3);
  mc = MatrixAlloc(2, 3);

  MatrixSet(ma,
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0);
  MatrixSet(mb,
            7.0, 8.0, 9.0,
            10.0, 11.0, 12.0);
  MatrixAdd(ma, mb, mc);
  ASSERT_EQ(mc->data[0][0], 8.0);
  ASSERT_EQ(mc->data[0][1], 10.0);
  ASSERT_EQ(mc->data[0][2], 12.0);
  ASSERT_EQ(mc->data[1][0], 14.0);
  ASSERT_EQ(mc->data[1][1], 16.0);
  ASSERT_EQ(mc->data[1][2], 18.0);

  MatrixAdd(ma, mc, ma); //inplace test
  ASSERT_EQ(ma->data[0][0], 9.0);
  ASSERT_EQ(ma->data[0][1], 12.0);
  ASSERT_EQ(ma->data[0][2], 15.0);
  ASSERT_EQ(ma->data[1][0], 18.0);
  ASSERT_EQ(ma->data[1][1], 21.0);
  ASSERT_EQ(ma->data[1][2], 24.0);

  MatrixFree(ma);
  MatrixFree(mb);
  MatrixFree(mc);
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixSubstract) {
  matrix_t *ma, *mb, *mc;
  ma = MatrixAlloc(2, 3);
  mb = MatrixAlloc(2, 3);
  mc = MatrixAlloc(2, 3);

  MatrixSet(ma,
            12.0, 11.0, 10.0,
            9.0,  8.0,  7.0);
  MatrixSet(mb,
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0);

  MatrixSubtract(ma, mb, mc);
  ASSERT_EQ(mc->data[0][0], 11.0);
  ASSERT_EQ(mc->data[0][1], 9.0);
  ASSERT_EQ(mc->data[0][2], 7.0);
  ASSERT_EQ(mc->data[1][0], 5.0);
  ASSERT_EQ(mc->data[1][1], 3.0);
  ASSERT_EQ(mc->data[1][2], 1.0);

  MatrixSubtract(ma, mc, ma); //inplace test
  ASSERT_EQ(ma->data[0][0], 1.0);
  ASSERT_EQ(ma->data[0][1], 2.0);
  ASSERT_EQ(ma->data[0][2], 3.0);
  ASSERT_EQ(ma->data[1][0], 4.0);
  ASSERT_EQ(ma->data[1][1], 5.0);
  ASSERT_EQ(ma->data[1][2], 6.0);

  MatrixFree(ma);
  MatrixFree(mb);
  MatrixFree(mc);
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixSubstractFromIdentity) {
  matrix_t *m;
  m = MatrixAlloc(3, 3);
  MatrixSet(m,
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0,
            7.0, 8.0, 9.0);
  MatrixSubtractFromIdentity(m);

  ASSERT_EQ(m->data[0][0], 0.0);
  ASSERT_EQ(m->data[0][1], -2.0);
  ASSERT_EQ(m->data[0][2], -3.0);
  ASSERT_EQ(m->data[1][0], -4.0);
  ASSERT_EQ(m->data[1][1], -4.0);
  ASSERT_EQ(m->data[1][2], -6.0);
  ASSERT_EQ(m->data[2][0], -7.0);
  ASSERT_EQ(m->data[2][1], -8.0);
  ASSERT_EQ(m->data[2][2], -8.0);
  MatrixFree(m);
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixMultiply) {
  matrix_t *ma, *mb, *mc, *md;
  ma = MatrixAlloc(3, 3);
  mb = MatrixAlloc(3, 3);
  mc = MatrixAlloc(3, 3);
  md = MatrixAlloc(3, 3);

  MatrixSet(ma,
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0,
            7.0, 8.0, 9.0);

  MatrixSet(mb,
            10.0, 11.0, 12.0,
            13.0, 14.0, 15.0,
            16.0, 17.0, 18.0);

  MatrixSet(mc,
            84.0, 90.0, 96.0,
            201.0, 216.0, 231.0,
            318.0, 342.0, 366.0);
  MatrixMultiply(ma, mb, md);
  ASSERT_TRUE(MatrixEq(md, mc, 1e-06));

  MatrixFree(ma);
  MatrixFree(mb);
  MatrixFree(mc);
  MatrixFree(md);
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixMultiplyByTranspose) {
  matrix_t *ma, *mb, *mc, *md;
  ma = MatrixAlloc(3, 3);
  mb = MatrixAlloc(3, 3);
  mc = MatrixAlloc(3, 3);
  md = MatrixAlloc(3, 3);

  MatrixSet(ma,
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0,
            7.0, 8.0, 9.0);

  MatrixSet(mb,
            10.0, 11.0, 12.0,
            13.0, 14.0, 15.0,
            16.0, 17.0, 18.0);

  MatrixSet(mc,
            68.0, 86.0, 104.0,
            167.0, 212.0, 257.0,
            266.0, 338.0, 410.0);

  MatrixMultiplyByTranspose(ma, mb, md);
  ASSERT_TRUE(MatrixEq(mc, md, 1e-06));

  MatrixFree(ma);
  MatrixFree(mb);
  MatrixFree(mc);
  MatrixFree(md);
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixTranspose) {
  matrix_t *ma, *mb, *mc;
  ma = MatrixAlloc(3, 1);
  mb = MatrixAlloc(1, 3);
  mc = MatrixAlloc(1, 3);
  MatrixSet(ma, 1, 2, 3);
  MatrixSet(mb, 1, 2, 3);
  MatrixTranspose(ma, mc);
  ASSERT_TRUE(MatrixEq(mb, mc, 1e-06));
  MatrixFree(ma);
  MatrixFree(mb);
  MatrixFree(mc);
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixEq) {
  matrix_t *m1, *m2;
  uint32_t r, c;
  m1 = MatrixAlloc(3, 3);
  m2 = MatrixAlloc(3, 3);
  for (r = 0; r < 3; ++r) {
    for (c = 0; c < 3; ++c) {
      m1->data[r][c] = m2->data[r][c] = rand();
    }
  }
  ASSERT_TRUE(MatrixEq(m1, m2, 1e-06));
  MatrixFree(m1);
  MatrixFree(m2);
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixScale) {
  matrix_t *ma, *mb;
  double scalar = 2.5;
  ma = MatrixAlloc(2, 2);
  mb = MatrixAlloc(2, 2);

  MatrixSet(ma,
            1.0, 2.0,
            3.0, 4.0);
  MatrixSet(mb,
            2.5, 5.0,
            7.5, 10.0);
  MatrixScale(ma, scalar);
  ASSERT_TRUE(MatrixEq(ma, mb, 1e-06));
  MatrixFree(ma);
  MatrixFree(mb);
}
//////////////////////////////////////////////////////////////////////////

TEST(matrix, matrixDestructiveInvert) {
  matrix_t *ma, *mb, *mc;
  ma = MatrixAlloc(3, 3);
  mb = MatrixAlloc(3, 3);
  mc = MatrixAlloc(3, 3);

  MatrixSet(ma,
            5.0, 2.0, 3.0,
            8.0, 12.0, 22.0,
            39.0, 3.0, 11.0);

  MatrixSet(mc,
            33.0 / 269.0, -13.0 / 538.0, 4.0 / 269.0,
            385.0 / 269.0, -31.0 / 269.0, -43.0 / 269.0,
            -222.0 / 269.0, 63.0 / 538.0, 22.0 / 269.0);

  MatrixDestructiveInvert(ma, mb);
  ASSERT_TRUE(MatrixEq(mb, mc, 1e-06));

  MatrixFree(ma);
  MatrixFree(mb);
  MatrixFree(mc);
}
//////////////////////////////////////////////////////////////////////////
