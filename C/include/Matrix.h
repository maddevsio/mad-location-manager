#ifndef MATRIX_H
#define MATRIX_H

#include <stdint.h>
#include <stdbool.h>
#include "Commons.h"

typedef struct matrix {
  double **data;

  uint32_t rows;
  uint32_t cols;
  //seems aligned, right?
} matrix_t;

matrix_t* MatrixAlloc(uint32_t rows, uint32_t cols);
void MatrixFree(matrix_t *m);

/* Set values of a matrix, row by row. */
void MatrixSet(matrix_t *m, ...);

/* Copy a matrix. */
void MatrixCopy(matrix_t *src,
                matrix_t *dst);

/* Pretty-print a matrix. */
void MatrixPrint(matrix_t *m);

/* Add matrices a and b and put the result in c. */
void MatrixAdd(matrix_t *ma,
               matrix_t *mb,
               matrix_t *mc);

/* Subtract matrices a and b and put the result in c. */
void MatrixSubtract(matrix_t *ma,
                    matrix_t *mb,
                    matrix_t *mc);

/* Subtract from the identity matrix in place. */
void MatrixSubtractFromIdentity(matrix_t *m);

/* Multiply matrices a and b and put the result in c. */
void MatrixMultiply(restrict matrix_t *ma,
                    restrict matrix_t *mb,
                    restrict matrix_t *mc);

/* Multiply matrix a by b-transpose and put the result in c. */
void MatrixMultiplyByTranspose(matrix_t *ma,
                               matrix_t *mb,
                               matrix_t *mc);
/* Transpose matrix*/
void MatrixTranspose(matrix_t *mtxin,
                     matrix_t *mtxout);

/* Whether two matrices are approximately equal. */
bool MatrixEq(matrix_t *a,
              matrix_t *b,
              double tolerance);

/* Multiply a matrix by a scalar. */
void MatrixScale(matrix_t *m, double scalar);

/* Invert a square matrix.
   Returns whether the matrix is invertible.
   input is mutated as well by this routine. */
bool MatrixDestructiveInvert(matrix_t *input,
                             matrix_t *output);

/* Turn m into an identity matrix. */
void MatrixSetIdentity(matrix_t *m);

void MatrixSetIdentityDiag(matrix_t *m);
#endif // MATRIX_H
