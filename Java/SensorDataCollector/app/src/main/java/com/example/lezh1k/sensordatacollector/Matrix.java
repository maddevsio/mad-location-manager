package com.example.lezh1k.sensordatacollector;

/**
 * Created by lezh1k on 12/6/17.
 */

public class Matrix {
    private int rows;
    private int cols;
    double data[][];

    public Matrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        data = new double[rows][cols];
    }

    public void Set(double... args) {
        assert(args.length == rows * cols);
        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < cols; ++c) {
                data[r][c] = args[r*cols + c];
            }
        }
    }

    public void Set(float... args) {
        assert(args.length == rows * cols);
        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < cols; ++c) {
                data[r][c] = (double)args[r*cols + c];
            }
        }
    }

    private void setIdentity() {
        assert(rows == cols);
        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < cols; ++c) {
                data[r][c] = 0.0;
            }
            data[r][r] = 1.0;
        }
    }

    static void MatrixAdd(Matrix ma,
                          Matrix mb,
                          Matrix mc) {
        assert(ma != null);
        assert(mb != null);
        assert(mc != null);
        assert(ma.cols == mb.cols && mb.cols == mc.cols);
        assert(ma.rows == mb.rows && mb.rows == mc.rows);

        for (int r = 0; r < ma.rows; ++r) {
            for (int c = 0; c < ma.cols; ++c) {
                mc.data[r][c] = ma.data[r][c] + mb.data[r][c];
            }
        }
    }

    static void MatrixSubstract(Matrix ma,
                                Matrix mb,
                                Matrix mc) {
        assert(ma != null);
        assert(mb != null);
        assert(mc != null);
        assert(ma.cols == mb.cols && mb.cols == mc.cols);
        assert(ma.rows == mb.rows && mb.rows == mc.rows);

        for (int r = 0; r < ma.rows; ++r) {
            for (int c = 0; c < ma.cols; ++c) {
                mc.data[r][c] = ma.data[r][c] - mb.data[r][c];
            }
        }
    }

    void SubtractFromIdentity() {
        int r, c;
        for (r = 0; r < rows; ++r) {
            for (c = 0; c < r; ++c)
                data[r][c] = -data[r][c];
            data[r][r] = 1.0 - data[r][r];
            for (c = r+1; c < cols; ++c)
                data[r][c] = -data[r][c];
        }
    }

    static void MatrixMultiply(Matrix ma,
                               Matrix mb,
                               Matrix mc) {
        assert(ma != null);
        assert(mb != null);
        assert(mc != null);
        assert(ma.cols == mb.rows);
        assert(ma.rows == mc.rows);
        assert(mb.cols == mc.cols);
        int r, c, rc;

        for (r = 0; r < mc.rows; ++r) {
            for (c = 0; c < mc.cols; ++c) {
                mc.data[r][c] = 0.0;
                for (rc = 0; rc < ma.cols; ++rc) {
                    mc.data[r][c] += ma.data[r][rc]*mb.data[rc][c];
                }
            } //for col
        } //for row
    }

    static void MatrixMultiplyByTranspose(Matrix ma,
                                          Matrix mb,
                                          Matrix mc) {
        assert(ma != null);
        assert(mb != null);
        assert(mc != null);
        assert(ma.cols == mb.cols);
        assert(ma.rows == mc.rows);
        assert(mb.rows == mc.cols);
        int r, c, rc;
        for (r = 0; r < mc.rows; ++r) {
            for (c = 0; c < mc.cols; ++c) {
                mc.data[r][c] = 0.0;
                for (rc = 0; rc < ma.cols; ++rc) {
                    mc.data[r][c] += ma.data[r][rc] * mb.data[c][rc];
                }
            } //for col
        } //for row
    }

    static void MatrixTranspose(Matrix mtxin,
                         Matrix mtxout) {
        assert(mtxin != null);
        assert(mtxout != null);
        assert(mtxin.rows == mtxout.cols);
        assert(mtxin.cols == mtxout.rows);
        int r, c;
        for (r = 0; r < mtxin.rows; ++r) {
            for (c = 0; c < mtxin.cols; ++c) {
                mtxout.data[c][r] = mtxin.data[r][c];
            } //for col
        } //for row
    }

    static boolean MatrixEq(Matrix ma,
                            Matrix mb,
                            double eps) {
        assert(ma != null);
        assert(mb != null);
        int r, c;
        if (ma.rows != mb.rows || ma.cols != mb.cols)
            return false;
        for (r = 0; r < ma.rows; ++r) {
            for (c = 0; c < ma.cols; ++c) {
                if (Math.abs(ma.data[r][c] - mb.data[r][c]) <= eps)
                    continue;
                return false;
            }
        }
        return true;
    }

    void Scale(double scalar) {
        int r, c;
        for (r = 0; r < rows; ++r) {
            for (c = 0; c < cols; ++c) {
                data[r][c] *= scalar;
            }
        }
    }

    private void swapRows(int r1, int r2) {
        assert(r1 != r2);
        double tmp[] = data[r1];
        data[r1] = data[r2];
        data[r2] = tmp;
    }

    private void scaleRow(int r, double scalar) {
        assert(r < rows);
        int c;
        for (c = 0; c < cols; ++c) {
            data[r][c] *= scalar;
        }
    }

    void shearRow(int r1,
                  int r2,
                  double scalar) {
        assert(r1 != r2);
        assert(r1 < rows && r2 < rows);
        int c;
        for (c = 0; c < cols; ++c)
            data[r1][c] += data[r2][c] * scalar;
    }

    static boolean MatrixDestructiveInvert(Matrix mtxin,
                                 Matrix mtxout) {
        assert(mtxin != null);
        assert(mtxout != null);
        assert(mtxin.cols == mtxin.rows);
        assert(mtxout.cols == mtxin.cols);
        assert(mtxout.rows == mtxin.rows);
        int r, ri;
        double scalar;
        mtxout.setIdentity();

        for (r = 0; r < mtxin.rows; ++r) {
            if (mtxin.data[r][r] == 0.0) { //we have to swap rows here to make nonzero diagonal
                for (ri = r; ri < mtxin.rows; ++ri) {
                    if (mtxin.data[ri][ri] != 0.0)
                        break;
                }

                if (ri == mtxin.rows)
                    return false;  //can't get inverse matrix

                mtxin.swapRows(r, ri);
                mtxout.swapRows(r, ri);
            } //if mtxin.data[r][r] == 0.0

            scalar = 1.0 / mtxin.data[r][r];
            mtxin.scaleRow(r, scalar);
            mtxout.scaleRow(r, scalar);

            for (ri = 0; ri < r; ++ri) {
                scalar = -mtxin.data[ri][r];
                mtxin.shearRow(ri, r, scalar);
                mtxout.shearRow(ri, r, scalar);
            }

            for (ri = r + 1; ri < mtxin.rows; ++ri) {
                scalar = -mtxin.data[ri][r];
                mtxin.shearRow(ri, r, scalar);
                mtxout.shearRow(ri, r, scalar);
            }
        } //for r < mtxin.rows
        return true;
    }
}
