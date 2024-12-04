// This file is part of Eigen, a lightweight C++ template library
// for linear algebra.
//
// Copyright (C) 2024 Charles Schlosser <cs.schlosser@gmail.com>
//
// This Source Code Form is subject to the terms of the Mozilla
// Public License v. 2.0. If a copy of the MPL was not distributed
// with this file, You can obtain one at http://mozilla.org/MPL/2.0/.

#ifndef EIGEN_FILL_H
#define EIGEN_FILL_H

// IWYU pragma: private
#include "./InternalHeaderCheck.h"

namespace Eigen {

namespace internal {

template <typename Xpr>
struct eigen_fill_helper : std::false_type {};

template <typename Scalar, int Rows, int Cols, int Options, int MaxRows, int MaxCols>
struct eigen_fill_helper<Matrix<Scalar, Rows, Cols, Options, MaxRows, MaxCols>> : std::true_type {};

template <typename Scalar, int Rows, int Cols, int Options, int MaxRows, int MaxCols>
struct eigen_fill_helper<Array<Scalar, Rows, Cols, Options, MaxRows, MaxCols>> : std::true_type {};

template <typename Xpr, int BlockRows, int BlockCols>
struct eigen_fill_helper<Block<Xpr, BlockRows, BlockCols, /*InnerPanel*/ true>> : eigen_fill_helper<Xpr> {};

template <typename Xpr, int BlockRows, int BlockCols>
struct eigen_fill_helper<Block<Xpr, BlockRows, BlockCols, /*InnerPanel*/ false>>
    : std::integral_constant<bool, eigen_fill_helper<Xpr>::value &&
                                       (Xpr::IsRowMajor ? (BlockRows == 1) : (BlockCols == 1))> {};

template <typename Xpr, int Options>
struct eigen_fill_helper<Map<Xpr, Options, Stride<0, 0>>> : eigen_fill_helper<Xpr> {};

template <typename Xpr, int Options, int OuterStride_>
struct eigen_fill_helper<Map<Xpr, Options, Stride<OuterStride_, 0>>>
    : std::integral_constant<bool, eigen_fill_helper<Xpr>::value &&
                                       enum_eq_not_dynamic(OuterStride_, Xpr::InnerSizeAtCompileTime)> {};

template <typename Xpr, int Options, int OuterStride_>
struct eigen_fill_helper<Map<Xpr, Options, Stride<OuterStride_, 1>>>
    : eigen_fill_helper<Map<Xpr, Options, Stride<OuterStride_, 0>>> {};

template <typename Xpr, int Options, int InnerStride_>
struct eigen_fill_helper<Map<Xpr, Options, InnerStride<InnerStride_>>>
    : eigen_fill_helper<Map<Xpr, Options, Stride<0, InnerStride_>>> {};

template <typename Xpr, int Options, int OuterStride_>
struct eigen_fill_helper<Map<Xpr, Options, OuterStride<OuterStride_>>>
    : eigen_fill_helper<Map<Xpr, Options, Stride<OuterStride_, 0>>> {};

template <typename Xpr, bool use_fill = eigen_fill_helper<Xpr>::value>
struct eigen_fill_impl {
  using Scalar = typename Xpr::Scalar;
  using Func = scalar_constant_op<Scalar>;
  using PlainObject = typename Xpr::PlainObject;
  using Constant = CwiseNullaryOp<Func, PlainObject>;
  static EIGEN_DEVICE_FUNC EIGEN_STRONG_INLINE void run(Xpr& dst, const Scalar& val) {
    dst = Constant(dst.rows(), dst.cols(), Func(val));
  }
};

#if !EIGEN_COMP_MSVC
#ifndef EIGEN_GPU_COMPILE_PHASE
template <typename Xpr>
struct eigen_fill_impl<Xpr, /*use_fill*/ true> {
  using Scalar = typename Xpr::Scalar;
  static EIGEN_DEVICE_FUNC EIGEN_STRONG_INLINE void run(Xpr& dst, const Scalar& val) {
    EIGEN_USING_STD(fill_n);
    fill_n(dst.data(), dst.size(), val);
  }
};
#endif
#endif

template <typename Xpr>
struct eigen_memset_helper {
  static constexpr bool value = std::is_trivial<typename Xpr::Scalar>::value && eigen_fill_helper<Xpr>::value;
};

template <typename Xpr, bool use_memset = eigen_memset_helper<Xpr>::value>
struct eigen_zero_impl {
  using Scalar = typename Xpr::Scalar;
  static EIGEN_DEVICE_FUNC EIGEN_STRONG_INLINE void run(Xpr& dst) { eigen_fill_impl<Xpr, false>::run(dst, Scalar(0)); }
};

template <typename Xpr>
struct eigen_zero_impl<Xpr, /*use_memset*/ true> {
  using Scalar = typename Xpr::Scalar;
  static constexpr size_t max_bytes = (std::numeric_limits<std::ptrdiff_t>::max)();
  static EIGEN_DEVICE_FUNC EIGEN_STRONG_INLINE void run(Xpr& dst) {
    const size_t num_bytes = dst.size() * sizeof(Scalar);
    if (num_bytes == 0) return;
    void* dst_ptr = static_cast<void*>(dst.data());
#ifndef EIGEN_NO_DEBUG
    if (num_bytes > max_bytes) throw_std_bad_alloc();
    eigen_assert((dst_ptr != nullptr) && "null pointer dereference error!");
#endif
    EIGEN_USING_STD(memset);
    memset(dst_ptr, 0, num_bytes);
  }
};

}  // namespace internal
}  // namespace Eigen

#endif  // EIGEN_FILL_H
