#ifndef MATRIX_H
#define MATRIX_H

#include <stdint.h>
#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct matrix {
  double **data; //todo make 1D arr.
  uint32_t rows;
  uint32_t cols;
} matrix_t;

matrix_t* matrix_alloc(uint32_t rows, uint32_t cols);
void matrix_free(matrix_t *m);

/* Set values of a matrix, row by row. */
void matrix_set(matrix_t *m, ...);

/* Copy a matrix. */
void matrix_copy(const matrix_t *src,
                 matrix_t *dst);

/* Pretty-print a matrix. */
void matrix_print(const matrix_t *m);

/* Add matrices a and b and put the result in c. */
void matrix_add(const matrix_t *ma,
                const matrix_t *mb,
                matrix_t *mc);

/* Subtract matrices a and b and put the result in c. */
void matrix_subtract(const matrix_t *ma,
                     const matrix_t *mb,
                     matrix_t *mc);

/* Subtract from the identity matrix in place. */
void matrix_subtract_from_identity(matrix_t *m);

/* Multiply matrices a and b and put the result in c. */
void matrix_multiply(const matrix_t *ma,
                     const matrix_t *mb,
                     matrix_t *mc);

/* Multiply matrix a by b-transpose and put the result in c. */
void matrix_multiply_by_transpose(const matrix_t *ma,
                                  const matrix_t *mb,
                                  matrix_t *mc);
/* Transpose matrix*/
void matrix_transpose(const matrix_t *mtxin,
                      matrix_t *mtxout);

/* Whether two matrices are approximately equal. */
bool matrix_eq(const matrix_t *a,
               const matrix_t *b,
               double tolerance);

/* Multiply a matrix by a scalar. */
void matrix_scale(matrix_t *m, double scalar);

/* Invert a square matrix.
   Returns whether the matrix is invertible.
   input is mutated as well by this routine. */
bool matrix_destructive_invert(matrix_t *mtxin,
                               matrix_t *mtxout);

/* Turn m into an identity matrix. */
void matrix_set_identity(matrix_t *m);
void matrix_set_identity_diag(matrix_t *m);

#ifdef __cplusplus
}
#endif // extern "C"
#endif // MATRIX_H
