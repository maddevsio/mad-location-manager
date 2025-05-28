#ifndef LOW_PASS_H
#define LOW_PASS_H

#include <math.h>

#include <cstddef>
#include <cstring>

template <class T, size_t N>
class LowPassFilter
{
 private:
  double fc;  // cutoff [Hz]
  T y[N];
  bool has_init;
  double last_ts;

 public:
  LowPassFilter(double cutoff_hz) : fc(cutoff_hz), has_init(false), last_ts(0.0)
  {
    for (size_t i = 0; i < N; ++i) {
      y[i] = T();
    }
  }

  T* filter(const T (&src)[N], double ts)
  {
    if (!has_init) {
      std::memcpy(y, src, sizeof(T) * N);
      has_init = true;
    } else {
      double dt = ts - last_ts;
      double alpha = dt / ((1.0 / (2.0 * M_PI * fc)) + dt);

      for (size_t i = 0; i < N; ++i) {
        y[i] += alpha * (src[i] - y[i]);
      }
    }
    last_ts = ts;
    return y;
  }
};

#endif
