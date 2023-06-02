#ifndef MATRIX_H
#define MATRIX_H

#include <algorithm>
#include <array>
#include <concepts>
#include <cstddef>
#include <cstdint>
#include <cstring>
#include <iostream>

template <typename T>
concept arithmetic = std::integral<T> or std::floating_point<T>;

template <typename T, size_t rows, size_t cols>
  requires arithmetic<T>
class Matrix {
private:
  static const size_t N = rows * cols;
  std::array<T, N> m_data;

  void swap_rows(size_t r1, size_t r2) {
    T(*mtx)[cols] = reinterpret_cast<T(*)[cols]>(m_data.data());
    for (size_t c = 0; c < cols; ++c) {
      std::swap(mtx[r1][c], mtx[r2][c]);
    }
  }

  void scale_row(size_t ri, double scalar) {
    for (size_t c = 0; c < cols; ++c)
      (*this)(ri, c) *= scalar;
  };

  void shear_row(size_t r1, size_t r2, double scalar) {
    T(*mtx)[cols] = reinterpret_cast<T(*)[cols]>(m_data.data());
    for (size_t c = 0; c < cols; ++c)
      mtx[r1][c] += mtx[r2][c] * scalar;
  }

public:
  Matrix() : m_data({}){}; // to init matrix with zeroes

  explicit Matrix(const std::array<T, rows * cols> &arr) : m_data(arr) {}
  explicit Matrix(std::array<T, rows * cols> &&arr) : m_data(arr) {}

  Matrix(const Matrix &) = default;
  Matrix(Matrix &&) = default;
  Matrix &operator=(const Matrix &mtx) = default;

  ~Matrix() = default;

  static Matrix Identity(); // todo init identity matrix in compile time

  Matrix<T, cols, rows> transpose() const;
  Matrix<T, rows, cols> invert() const;

  void set(std::array<T, rows * cols> &&arr) { m_data = arr; }

  const T &operator()(size_t row, size_t col) const;
  T &operator()(size_t row, size_t col);

  friend inline bool operator==(const Matrix<T, rows, cols> &l,
                                const Matrix<T, rows, cols> &r) {
    return l.m_data == r.m_data;
  };
  friend inline bool operator!=(const Matrix<T, rows, cols> &l,
                                const Matrix<T, rows, cols> &r) {
    return !(l == r);
  }

  Matrix &operator+=(const Matrix &mtx);
  Matrix &operator-=(const Matrix &mtx);

  Matrix &operator*=(T scalar);
  Matrix &operator/=(T scalar);

  // AUX!

  template <typename U, size_t N, size_t M>
  friend std::ostream &operator<<(std::ostream &out,
                                  const Matrix<U, N, M> &mtx);
};
//////////////////////////////////////////////////////////////

template <typename T, size_t rows, size_t cols>
  requires arithmetic<T>
Matrix<T, rows, cols> Matrix<T, rows, cols>::Identity() {
  Matrix<T, rows, cols> res;
  for (size_t rc = 0; rc < std::min<size_t>(rows, cols); ++rc)
    res(rc, rc) = 1;
  return res;
}
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
/// ELEMENT ACCESS FUNCTIONS
//////////////////////////////////////////////////////////////
template <typename T, size_t rows, size_t cols>
  requires arithmetic<T>
const T &Matrix<T, rows, cols>::operator()(size_t row, size_t col) const {
  return m_data[row * cols + col];
}
//////////////////////////////////////////////////////////////

template <typename T, size_t rows, size_t cols>
  requires arithmetic<T>
T &Matrix<T, rows, cols>::operator()(size_t row, size_t col) {
  return m_data[row * cols + col];
}
//////////////////////////////////////////////////////////////
/// !ELEMENT ACCESS FUNCTIONS
//////////////////////////////////////////////////////////////

template <typename T, size_t rows, size_t cols>
  requires arithmetic<T>
Matrix<T, cols, rows> Matrix<T, rows, cols>::transpose() const {
  Matrix<T, cols, rows> res;
  for (size_t r = 0; r < rows; ++r) {
    for (size_t c = 0; c < cols; ++c) {
      res(c, r) = (*this)(r, c);
    }
  }
  return res;
}
//////////////////////////////////////////////////////////////

template <typename T, size_t rows, size_t cols>
  requires arithmetic<T>
Matrix<T, rows, cols> Matrix<T, rows, cols>::invert() const {
  static_assert(rows == cols,
                "matrix invert is possible only for square matrices");
  Matrix<T, rows, rows> mtxin(
      *this); // copy! because this algorithm is distructive
  Matrix<T, rows, cols> mtxout = Matrix<T, rows, cols>::Identity();

  size_t r, ri;
  for (r = 0; r < rows; ++r) {
    if (mtxin(r, r) == 0) {
      for (ri = r; ri < rows && mtxin(ri, ri) == 0; ++ri) {
        // do nothing
      }
      if (ri == rows) {
        throw std::invalid_argument("can't get inverse matrix");
      }

      mtxin.swap_rows(r, ri);
      mtxout.swap_rows(r, ri);
    }

    double scalar = 1.0 / mtxin(r, r);
    mtxin.scale_row(r, scalar);
    mtxout.scale_row(r, scalar);

    for (ri = 0; ri < r; ++ri) {
      scalar = -mtxin(ri, r);
      mtxin.shear_row(ri, r, scalar);
      mtxout.shear_row(ri, r, scalar);
    }

    for (ri = r + 1; ri < rows; ++ri) {
      scalar = -mtxin(ri, r);
      mtxin.shear_row(ri, r, scalar);
      mtxout.shear_row(ri, r, scalar);
    } // for ri = r+1; ri < rows
  }   // for r < rows
  return mtxout;
}
//////////////////////////////////////////////////////////////

template <typename T, size_t l, size_t m, size_t n>
  requires arithmetic<T>
Matrix<T, l, n> operator*(const Matrix<T, l, m> &mtx_a,
                          const Matrix<T, m, n> &mtx_b) {
  Matrix<T, l, n> res;
  for (size_t r = 0; r < l; ++r) {
    for (size_t c = 0; c < n; ++c) {
      for (size_t rc = 0; rc < m; ++rc) {
        res(r, c) += mtx_a(r, rc) * mtx_b(rc, c);
      }
    }
  }
  return res;
}
//////////////////////////////////////////////////////////////

template <typename T, size_t rows, size_t cols>
  requires arithmetic<T>
Matrix<T, rows, cols> operator+(const Matrix<T, rows, cols> &l,
                                const Matrix<T, rows, cols> &r) {
  Matrix<T, rows, cols> res(l);
  res += r;
  return res;
}
//////////////////////////////////////////////////////////////

template <typename T, size_t rows, size_t cols>
  requires arithmetic<T>
Matrix<T, rows, cols> operator-(const Matrix<T, rows, cols> &l,
                                const Matrix<T, rows, cols> &r) {
  Matrix<T, rows, cols> res(l);
  res -= r;
  return res;
}
//////////////////////////////////////////////////////////////

template <typename T, size_t rows, size_t cols>
  requires arithmetic<T>
Matrix<T, rows, cols> &
Matrix<T, rows, cols>::operator-=(const Matrix<T, rows, cols> &mtx) {
  std::transform(m_data.begin(), m_data.end(), mtx.m_data.begin(),
                 m_data.begin(), std::minus<T>());
  return *this;
}
//////////////////////////////////////////////////////////////

template <typename T, size_t rows, size_t cols>
  requires arithmetic<T>
Matrix<T, rows, cols> &
Matrix<T, rows, cols>::operator+=(const Matrix<T, rows, cols> &mtx) {
  std::transform(m_data.begin(), m_data.end(), mtx.m_data.begin(),
                 m_data.begin(), std::plus<T>());
  return *this;
}
//////////////////////////////////////////////////////////////

template <typename T, size_t rows, size_t cols>
  requires arithmetic<T>
Matrix<T, rows, cols> &Matrix<T, rows, cols>::operator/=(T scalar) {
  std::for_each(m_data.begin(), m_data.end(), [scalar](T &x) { x /= scalar; });
  return *this;
}
//////////////////////////////////////////////////////////////

template <typename T, size_t rows, size_t cols>
  requires arithmetic<T>
Matrix<T, rows, cols> &Matrix<T, rows, cols>::operator*=(T scalar) {
  std::for_each(m_data.begin(), m_data.end(), [scalar](T &x) { x *= scalar; });
  return *this;
}
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
/// AUX FUNCTIONS
//////////////////////////////////////////////////////////////

template <typename T, size_t rows, size_t cols>
  requires arithmetic<T>
std::ostream &operator<<(std::ostream &out, const Matrix<T, rows, cols> &mtx) {
  for (size_t r = 0; r < rows; ++r) {
    for (size_t c = 0; c < cols; ++c) {
      out << mtx(r, c) << "\t";
    }
    out << std::endl;
  }
  return out;
}
//////////////////////////////////////////////////////////////
/// !AUX FUNCTIONS
//////////////////////////////////////////////////////////////

#endif // MATRIX_H
