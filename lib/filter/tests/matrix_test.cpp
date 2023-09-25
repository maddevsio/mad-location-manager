#include <gtest/gtest.h>

#include <Eigen/Eigen>
using namespace Eigen;

TEST(matrix, matrix_default_constructor)
{
  static const int rows = 3;
  static const int cols = 2;
  Matrix<int, rows, cols> ma;
  ma.setZero();
  for (size_t r = 0; r < rows; ++r) {
    for (size_t c = 0; c < cols; ++c) {
      ASSERT_EQ(ma(r, c), 0);
    }
  }
}
//////////////////////////////////////////////////////////////

TEST(matrix, matrix_parametrized_constructor)
{
  Matrix<int, 2, 2> ma({
      {1, 2},
      {3, 4}
  });
  ASSERT_EQ(ma(0, 0), 1);
  ASSERT_EQ(ma(0, 1), 2);
  ASSERT_EQ(ma(1, 0), 3);
  ASSERT_EQ(ma(1, 1), 4);
}
//////////////////////////////////////////////////////////////

TEST(matrix, matrix_equality)
{
  Matrix<int, 2, 2> ma({
      {1, 2},
      {3, 4}
  });
  Matrix<int, 2, 2> mb({
      {1, 2},
      {3, 4}
  });
  Matrix<int, 2, 2> mc({
      {1, 2},
      {3, 3}
  });

  ASSERT_EQ(ma, mb);
  ASSERT_NE(mb, mc);
}
//////////////////////////////////////////////////////////////

TEST(matrix, matrix_scale)
{
  Matrix<int, 2, 2> ma({
      {1, 2},
      {3, 4}
  });
  Matrix<int, 2, 2> mb({
      {2, 4},
      {6, 8}
  });
  Matrix<int, 2, 2> mc(ma);

  ma *= 2;
  ASSERT_EQ(ma, mb);
  mb /= 2;
  ASSERT_EQ(mb, mc);
}
//////////////////////////////////////////////////////////////

TEST(matrix, matrix_sub)
{
  Matrix<int, 2, 2> ma({
      {1, 2},
      {3, 4}
  });
  Matrix<int, 2, 2> mb({
      {2, 4},
      {6, 8}
  });
  Matrix<int, 2, 2> mc = mb - ma;

  ASSERT_EQ(ma, mc);
}
//////////////////////////////////////////////////////////////

TEST(matrix, matrix_sum)
{
  Matrix<int, 2, 2> ma({
      {1, 2},
      {3, 4}
  });
  Matrix<int, 2, 2> mb({
      {2, 4},
      {6, 8}
  });
  Matrix<int, 2, 2> mc = ma + ma;

  ASSERT_EQ(mb, mc);
}
//////////////////////////////////////////////////////////////

TEST(matrix, matrix_multiply)
{
  Matrix<int, 1, 4> ma({1, 2, 3, 4});
  Matrix<int, 4, 1> mb({4, 3, 2, 1});
  Matrix<int, 1, 1> mc = ma * mb;

  ASSERT_EQ(mc(0, 0), 20);
}
//////////////////////////////////////////////////////////////

TEST(matrix, matrix_identity)
{
  static const size_t rows = 4;
  static const size_t cols = 5;
  auto I = Matrix<int, rows, cols>::Identity();

  for (size_t rc = 0; rc < std::min(rows, cols); ++rc)
    ASSERT_EQ(I(rc, rc), 1);
}
//////////////////////////////////////////////////////////////

TEST(matrix, matrix_invert)
{
  Matrix<double, 2, 2> mtx({
      {1., 2.},
      {3., 4.}
  });
  Matrix<double, 2, 2> exp({
      {-2.,   1.},
      {1.5, -0.5}
  });
  Matrix<double, 2, 2> res = mtx.inverse();
  ASSERT_EQ(exp, res);
}
//////////////////////////////////////////////////////////////
