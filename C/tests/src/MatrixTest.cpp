#include <stdint.h>
#include <assert.h>
#include <time.h>
#include <stdlib.h>
#include <stddef.h>
#include "Matrix.h"

static void matrixSetTest();

/* Copy a matrix. */
static void matrixCopyTest();
/* Add matrices a and b and put the result in c. */
static void matrixAddTest();
/* Subtract matrices a and b and put the result in c. */
static void matrixSubstractTest();
/* Subtract from the identity matrix in place. */
static void matrixSubstractFromIdentityTest();
/* Multiply matrices a and b and put the result in c. */
static void matrixMultiplyTest(/*matrix_t *ma, matrix_t *mb, matrix_t *mc*/);
/* Multiply matrix a by b-transpose and put the result in c. */
static void matrixMultiplyByTransposeTest();
/* Transpose matrix */
static void matrixTransposeTest();
/* Whether two matrices are approximately equal. */
static void matrixEqTest();
/* Multiply a matrix by a scalar. */
static void matrixScaleTest();
/* Invert a square matrix.
   Returns whether the matrix is invertible.
   input is mutated as well by this routine. */
static void matrixDestructiveInvertTest();

static void matrixSetTest() {
  matrix_t *m1, *m2;
  m1 = MatrixAlloc(3, 3);
  MatrixSet(m1,
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0,
            7.0, 8.0, 9.0);
  assert(m1->data[0][0] == 1.0);
  assert(m1->data[0][1] == 2.0);
  assert(m1->data[0][2] == 3.0);
  assert(m1->data[1][0] == 4.0);
  assert(m1->data[1][1] == 5.0);
  assert(m1->data[1][2] == 6.0);
  assert(m1->data[2][0] == 7.0);
  assert(m1->data[2][1] == 8.0);
  assert(m1->data[2][2] == 9.0);

  m2 = MatrixAlloc(3, 1);
  MatrixSet(m2, 0.0, 1.1, 2.2);
  assert(m2->data[0][0] == 0.0);
  assert(m2->data[1][0] == 1.1);
  assert(m2->data[2][0] == 2.2);

  MatrixFree(m1);
  MatrixFree(m2);
}
//////////////////////////////////////////////////////////////////////////

static void matrixCopyTest() {
  matrix_t *m1, *m2;
  m1 = MatrixAlloc(3, 3);
  m2 = MatrixAlloc(3, 3);
  MatrixSet(m1,
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0,
            7.0, 8.0, 9.0);

  MatrixCopy(m1, m2);
  assert(m2->data[0][0] == 1.0);
  assert(m2->data[0][1] == 2.0);
  assert(m2->data[0][2] == 3.0);
  assert(m2->data[1][0] == 4.0);
  assert(m2->data[1][1] == 5.0);
  assert(m2->data[1][2] == 6.0);
  assert(m2->data[2][0] == 7.0);
  assert(m2->data[2][1] == 8.0);
  assert(m2->data[2][2] == 9.0);
  MatrixFree(m1);
  MatrixFree(m2);
}
//////////////////////////////////////////////////////////////////////////

static void matrixAddTest() {
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
  assert(mc->data[0][0] == 8.0);
  assert(mc->data[0][1] == 10.0);
  assert(mc->data[0][2] == 12.0);
  assert(mc->data[1][0] == 14.0);
  assert(mc->data[1][1] == 16.0);
  assert(mc->data[1][2] == 18.0);

  MatrixAdd(ma, mc, ma); //inplace test
  assert(ma->data[0][0] == 9.0);
  assert(ma->data[0][1] == 12.0);
  assert(ma->data[0][2] == 15.0);
  assert(ma->data[1][0] == 18.0);
  assert(ma->data[1][1] == 21.0);
  assert(ma->data[1][2] == 24.0);

  MatrixFree(ma);
  MatrixFree(mb);
  MatrixFree(mc);
}
//////////////////////////////////////////////////////////////////////////

static void matrixSubstractTest() {
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
  assert(mc->data[0][0] == 11.0);
  assert(mc->data[0][1] == 9.0);
  assert(mc->data[0][2] == 7.0);
  assert(mc->data[1][0] == 5.0);
  assert(mc->data[1][1] == 3.0);
  assert(mc->data[1][2] == 1.0);

  MatrixSubtract(ma, mc, ma); //inplace test
  assert(ma->data[0][0] == 1.0);
  assert(ma->data[0][1] == 2.0);
  assert(ma->data[0][2] == 3.0);
  assert(ma->data[1][0] == 4.0);
  assert(ma->data[1][1] == 5.0);
  assert(ma->data[1][2] == 6.0);

  MatrixFree(ma);
  MatrixFree(mb);
  MatrixFree(mc);
}
//////////////////////////////////////////////////////////////////////////

void matrixSubstractFromIdentityTest() {
  matrix_t *m;
  m = MatrixAlloc(3, 3);
  MatrixSet(m,
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0,
            7.0, 8.0, 9.0);
  MatrixSubtractFromIdentity(m);

  assert(m->data[0][0] == 0.0);
  assert(m->data[0][1] == -2.0);
  assert(m->data[0][2] == -3.0);
  assert(m->data[1][0] == -4.0);
  assert(m->data[1][1] == -4.0);
  assert(m->data[1][2] == -6.0);
  assert(m->data[2][0] == -7.0);
  assert(m->data[2][1] == -8.0);
  assert(m->data[2][2] == -8.0);
  MatrixFree(m);
}
//////////////////////////////////////////////////////////////////////////

void matrixMultiplyTest() {
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
  assert(MatrixEq(md, mc, 1e-06));

  MatrixFree(ma);
  MatrixFree(mb);
  MatrixFree(mc);
  MatrixFree(md);
}
//////////////////////////////////////////////////////////////////////////

void matrixMultiplyByTransposeTest() {
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
  assert(MatrixEq(mc, md, 1e-06));

  MatrixFree(ma);
  MatrixFree(mb);
  MatrixFree(mc);
  MatrixFree(md);
}
//////////////////////////////////////////////////////////////////////////

void matrixTransposeTest() {
  matrix_t *ma, *mb, *mc;
  ma = MatrixAlloc(3, 1);
  mb = MatrixAlloc(1, 3);
  mc = MatrixAlloc(1, 3);
  MatrixSet(ma, 1, 2, 3);
  MatrixSet(mb, 1, 2, 3);
  MatrixTranspose(ma, mc);
  assert(MatrixEq(mb, mc, 1e-06));
  MatrixFree(ma);
  MatrixFree(mb);
  MatrixFree(mc);
}
//////////////////////////////////////////////////////////////////////////

void matrixEqTest() {
  matrix_t *m1, *m2;
  uint32_t r, c;
  m1 = MatrixAlloc(3, 3);
  m2 = MatrixAlloc(3, 3);
  for (r = 0; r < 3; ++r) {
    for (c = 0; c < 3; ++c) {
      m1->data[r][c] = m2->data[r][c] = rand();
    }
  }
  assert(MatrixEq(m1, m2, 1e-06));
  MatrixFree(m1);
  MatrixFree(m2);
}
//////////////////////////////////////////////////////////////////////////

void matrixScaleTest() {
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
  assert(MatrixEq(ma, mb, 1e-06));
  MatrixFree(ma);
  MatrixFree(mb);
}
//////////////////////////////////////////////////////////////////////////

void matrixDestructiveInvertTest() {
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
  assert(MatrixEq(mb, mc, 1e-06));

  MatrixFree(ma);
  MatrixFree(mb);
  MatrixFree(mc);
}
//////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////

void TestMatrices() {
  srand(time(NULL));
  matrixSetTest();
  matrixEqTest();
  matrixCopyTest();
  matrixAddTest();
  matrixSubstractTest();
  matrixSubstractFromIdentityTest();
  matrixMultiplyTest();
  matrixMultiplyByTransposeTest();
  matrixTransposeTest();
  matrixScaleTest();
  matrixDestructiveInvertTest();
}
//////////////////////////////////////////////////////////////////////////
