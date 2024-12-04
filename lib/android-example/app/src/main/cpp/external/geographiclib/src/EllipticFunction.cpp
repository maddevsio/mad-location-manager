/**
 * \file EllipticFunction.cpp
 * \brief Implementation for GeographicLib::EllipticFunction class
 *
 * Copyright (c) Charles Karney (2008-2024) <karney@alum.mit.edu> and licensed
 * under the MIT/X11 License.  For more information, see
 * https://geographiclib.sourceforge.io/
 **********************************************************************/

#include <GeographicLib/EllipticFunction.hpp>

namespace GeographicLib {

  using namespace std;

  /*
   * Implementation of methods given in
   *
   *   B. C. Carlson
   *   Computation of elliptic integrals
   *   Numerical Algorithms 10, 13-26 (1995)
   */

  Math::real EllipticFunction::RF(real x, real y, real z) {
    // Carlson, eqs 2.2 - 2.7
    static const real tolRF =
      pow(3 * numeric_limits<real>::epsilon() * real(0.01), 1/real(8));
    real
      A0 = (x + y + z)/3,
      An = A0,
      Q = fmax(fmax(fabs(A0-x), fabs(A0-y)), fabs(A0-z)) / tolRF,
      x0 = x,
      y0 = y,
      z0 = z,
      mul = 1;
    while (Q >= mul * fabs(An)) {
      // Max 6 trips
      real lam = sqrt(x0)*sqrt(y0) + sqrt(y0)*sqrt(z0) + sqrt(z0)*sqrt(x0);
      An = (An + lam)/4;
      x0 = (x0 + lam)/4;
      y0 = (y0 + lam)/4;
      z0 = (z0 + lam)/4;
      mul *= 4;
    }
    real
      X = (A0 - x) / (mul * An),
      Y = (A0 - y) / (mul * An),
      Z = - (X + Y),
      E2 = X*Y - Z*Z,
      E3 = X*Y*Z;
    // https://dlmf.nist.gov/19.36.E1
    // Polynomial is
    // (1 - E2/10 + E3/14 + E2^2/24 - 3*E2*E3/44
    //    - 5*E2^3/208 + 3*E3^2/104 + E2^2*E3/16)
    // convert to Horner form...
    return (E3 * (6930 * E3 + E2 * (15015 * E2 - 16380) + 17160) +
            E2 * ((10010 - 5775 * E2) * E2 - 24024) + 240240) /
      (240240 * sqrt(An));
  }

  Math::real EllipticFunction::RF(real x, real y) {
    // Carlson, eqs 2.36 - 2.38
    static const real tolRG0 =
      real(2.7) * sqrt((numeric_limits<real>::epsilon() * real(0.01)));
    real xn = sqrt(x), yn = sqrt(y);
    if (xn < yn) swap(xn, yn);
    while (fabs(xn-yn) > tolRG0 * xn) {
      // Max 4 trips
      real t = (xn + yn) /2;
      yn = sqrt(xn * yn);
      xn = t;
    }
    return Math::pi() / (xn + yn);
  }

  Math::real EllipticFunction::RC(real x, real y) {
    // Defined only for y != 0 and x >= 0.
    return ( !(x >= y) ?        // x < y  and catch nans
             // https://dlmf.nist.gov/19.2.E18
             atan(sqrt((y - x) / x)) / sqrt(y - x) :
             ( x == y ? 1 / sqrt(y) :
               asinh( y > 0 ?
                      // https://dlmf.nist.gov/19.2.E19
                      // atanh(sqrt((x - y) / x))
                      sqrt((x - y) / y) :
                      // https://dlmf.nist.gov/19.2.E20
                      // atanh(sqrt(x / (x - y)))
                      sqrt(-x / y) ) / sqrt(x - y) ) );
  }

  Math::real EllipticFunction::RG(real x, real y, real z) {
    return (x == 0 ? RG(y, z) :
            (y == 0 ? RG(z, x) :
             (z == 0 ? RG(x, y) :
              // Carlson, eq 1.7
              (z * RF(x, y, z) - (x-z) * (y-z) * RD(x, y, z) / 3
               + sqrt(x * y / z)) / 2 )));
  }

  Math::real EllipticFunction::RG(real x, real y) {
    // Carlson, eqs 2.36 - 2.39
    static const real tolRG0 =
      real(2.7) * sqrt((numeric_limits<real>::epsilon() * real(0.01)));
    real
      x0 = sqrt(fmax(x, y)),
      y0 = sqrt(fmin(x, y)),
      xn = x0,
      yn = y0,
      s = 0,
      mul = real(0.25);
    while (fabs(xn-yn) > tolRG0 * xn) {
      // Max 4 trips
      real t = (xn + yn) /2;
      yn = sqrt(xn * yn);
      xn = t;
      mul *= 2;
      t = xn - yn;
      s += mul * t * t;
    }
    return (Math::sq( (x0 + y0)/2 ) - s) * Math::pi() / (2 * (xn + yn));
  }

  Math::real EllipticFunction::RJ(real x, real y, real z, real p) {
    // Carlson, eqs 2.17 - 2.25
    static const real
      tolRD = pow(real(0.2) * (numeric_limits<real>::epsilon() * real(0.01)),
                  1/real(8));
    real
      A0 = (x + y + z + 2*p)/5,
      An = A0,
      delta = (p-x) * (p-y) * (p-z),
      Q = fmax(fmax(fabs(A0-x), fabs(A0-y)),
               fmax(fabs(A0-z), fabs(A0-p))) / tolRD,
      x0 = x,
      y0 = y,
      z0 = z,
      p0 = p,
      mul = 1,
      mul3 = 1,
      s = 0;
    while (Q >= mul * fabs(An)) {
      // Max 7 trips
      real
        lam = sqrt(x0)*sqrt(y0) + sqrt(y0)*sqrt(z0) + sqrt(z0)*sqrt(x0),
        d0 = (sqrt(p0)+sqrt(x0)) * (sqrt(p0)+sqrt(y0)) * (sqrt(p0)+sqrt(z0)),
        e0 = delta/(mul3 * Math::sq(d0));
      s += RC(1, 1 + e0)/(mul * d0);
      An = (An + lam)/4;
      x0 = (x0 + lam)/4;
      y0 = (y0 + lam)/4;
      z0 = (z0 + lam)/4;
      p0 = (p0 + lam)/4;
      mul *= 4;
      mul3 *= 64;
    }
    real
      X = (A0 - x) / (mul * An),
      Y = (A0 - y) / (mul * An),
      Z = (A0 - z) / (mul * An),
      P = -(X + Y + Z) / 2,
      E2 = X*Y + X*Z + Y*Z - 3*P*P,
      E3 = X*Y*Z + 2*P * (E2 + 2*P*P),
      E4 = (2*X*Y*Z + P * (E2 + 3*P*P)) * P,
      E5 = X*Y*Z*P*P;
    // https://dlmf.nist.gov/19.36.E2
    // Polynomial is
    // (1 - 3*E2/14 + E3/6 + 9*E2^2/88 - 3*E4/22 - 9*E2*E3/52 + 3*E5/26
    //    - E2^3/16 + 3*E3^2/40 + 3*E2*E4/20 + 45*E2^2*E3/272
    //    - 9*(E3*E4+E2*E5)/68)
    return ((471240 - 540540 * E2) * E5 +
            (612612 * E2 - 540540 * E3 - 556920) * E4 +
            E3 * (306306 * E3 + E2 * (675675 * E2 - 706860) + 680680) +
            E2 * ((417690 - 255255 * E2) * E2 - 875160) + 4084080) /
      (4084080 * mul * An * sqrt(An)) + 6 * s;
  }

  Math::real EllipticFunction::RD(real x, real y, real z) {
    // Carlson, eqs 2.28 - 2.34
    static const real
      tolRD = pow(real(0.2) * (numeric_limits<real>::epsilon() * real(0.01)),
                  1/real(8));
    real
      A0 = (x + y + 3*z)/5,
      An = A0,
      Q = fmax(fmax(fabs(A0-x), fabs(A0-y)), fabs(A0-z)) / tolRD,
      x0 = x,
      y0 = y,
      z0 = z,
      mul = 1,
      s = 0;
    while (Q >= mul * fabs(An)) {
      // Max 7 trips
      real lam = sqrt(x0)*sqrt(y0) + sqrt(y0)*sqrt(z0) + sqrt(z0)*sqrt(x0);
      s += 1/(mul * sqrt(z0) * (z0 + lam));
      An = (An + lam)/4;
      x0 = (x0 + lam)/4;
      y0 = (y0 + lam)/4;
      z0 = (z0 + lam)/4;
      mul *= 4;
    }
    real
      X = (A0 - x) / (mul * An),
      Y = (A0 - y) / (mul * An),
      Z = -(X + Y) / 3,
      E2 = X*Y - 6*Z*Z,
      E3 = (3*X*Y - 8*Z*Z)*Z,
      E4 = 3 * (X*Y - Z*Z) * Z*Z,
      E5 = X*Y*Z*Z*Z;
    // https://dlmf.nist.gov/19.36.E2
    // Polynomial is
    // (1 - 3*E2/14 + E3/6 + 9*E2^2/88 - 3*E4/22 - 9*E2*E3/52 + 3*E5/26
    //    - E2^3/16 + 3*E3^2/40 + 3*E2*E4/20 + 45*E2^2*E3/272
    //    - 9*(E3*E4+E2*E5)/68)
    return ((471240 - 540540 * E2) * E5 +
            (612612 * E2 - 540540 * E3 - 556920) * E4 +
            E3 * (306306 * E3 + E2 * (675675 * E2 - 706860) + 680680) +
            E2 * ((417690 - 255255 * E2) * E2 - 875160) + 4084080) /
      (4084080 * mul * An * sqrt(An)) + 3 * s;
  }

  void EllipticFunction::Reset(real k2, real alpha2,
                               real kp2, real alphap2) {
    // Accept nans here (needed for GeodesicExact)
    if (k2 > 1)
      throw GeographicErr("Parameter k2 is not in (-inf, 1]");
    if (alpha2 > 1)
      throw GeographicErr("Parameter alpha2 is not in (-inf, 1]");
    if (kp2 < 0)
      throw GeographicErr("Parameter kp2 is not in [0, inf)");
    if (alphap2 < 0)
      throw GeographicErr("Parameter alphap2 is not in [0, inf)");
    _k2 = k2;
    _kp2 = kp2;
    _alpha2 = alpha2;
    _alphap2 = alphap2;
    _eps = _k2/Math::sq(sqrt(_kp2) + 1);
    // Values of complete elliptic integrals for k = 0,1 and alpha = 0,1
    //         K     E     D
    // k = 0:  pi/2  pi/2  pi/4
    // k = 1:  inf   1     inf
    //                    Pi    G     H
    // k = 0, alpha = 0:  pi/2  pi/2  pi/4
    // k = 1, alpha = 0:  inf   1     1
    // k = 0, alpha = 1:  inf   inf   pi/2
    // k = 1, alpha = 1:  inf   inf   inf
    //
    // Pi(0, k) = K(k)
    // G(0, k) = E(k)
    // H(0, k) = K(k) - D(k)
    // Pi(0, k) = K(k)
    // G(0, k) = E(k)
    // H(0, k) = K(k) - D(k)
    // Pi(alpha2, 0) = pi/(2*sqrt(1-alpha2))
    // G(alpha2, 0) = pi/(2*sqrt(1-alpha2))
    // H(alpha2, 0) = pi/(2*(1 + sqrt(1-alpha2)))
    // Pi(alpha2, 1) = inf
    // H(1, k) = K(k)
    // G(alpha2, 1) = H(alpha2, 1) = RC(1, alphap2)
    if (_k2 != 0) {
      // Complete elliptic integral K(k), Carlson eq. 4.1
      // https://dlmf.nist.gov/19.25.E1
      _kKc = _kp2 != 0 ? RF(_kp2, 1) : Math::infinity();
      // Complete elliptic integral E(k), Carlson eq. 4.2
      // https://dlmf.nist.gov/19.25.E1
      _eEc = _kp2 != 0 ? 2 * RG(_kp2, 1) : 1;
      // D(k) = (K(k) - E(k))/k^2, Carlson eq.4.3
      // https://dlmf.nist.gov/19.25.E1
      _dDc = _kp2 != 0 ? RD(0, _kp2, 1) / 3 : Math::infinity();
    } else {
      _kKc = _eEc = Math::pi()/2; _dDc = _kKc/2;
    }
    if (_alpha2 != 0) {
      // https://dlmf.nist.gov/19.25.E2
      real rj = (_kp2 != 0 && _alphap2 != 0) ? RJ(0, _kp2, 1, _alphap2) :
        Math::infinity(),
        // Only use rc if _kp2 = 0.
        rc = _kp2 != 0 ? 0 :
        (_alphap2 != 0 ? RC(1, _alphap2) : Math::infinity());
      // Pi(alpha^2, k)
      _pPic = _kp2 != 0 ? _kKc + _alpha2 * rj / 3 : Math::infinity();
      // G(alpha^2, k)
      _gGc = _kp2 != 0 ? _kKc + (_alpha2 - _k2) * rj / 3 :  rc;
      // H(alpha^2, k)
      _hHc = _kp2 != 0 ? _kKc - (_alphap2 != 0 ? _alphap2 * rj : 0) / 3 : rc;
    } else {
      _pPic = _kKc; _gGc = _eEc;
      // Hc = Kc - Dc but this involves large cancellations if k2 is close to
      // 1.  So write (for alpha2 = 0)
      //   Hc = int(cos(phi)^2/sqrt(1-k2*sin(phi)^2),phi,0,pi/2)
      //      = 1/sqrt(1-k2) * int(sin(phi)^2/sqrt(1-k2/kp2*sin(phi)^2,...)
      //      = 1/kp * D(i*k/kp)
      // and use D(k) = RD(0, kp2, 1) / 3
      // so Hc = 1/kp * RD(0, 1/kp2, 1) / 3
      //       = kp2 * RD(0, 1, kp2) / 3
      // using https://dlmf.nist.gov/19.20.E18
      // Equivalently
      //   RF(x, 1) - RD(0, x, 1)/3 = x * RD(0, 1, x)/3 for x > 0
      // For k2 = 0 and alpha2 = 0, we have
      //   Hc = int(cos(phi)^2,...) = pi/4
      // For k2 = 1 and alpha2 = 0, we have
      //   Hc = int(cos(phi),...) = 1
      _hHc = _kp2 == 1 ? Math::pi()/4 :
        (_kp2 == 0 ? 1 : _kp2 * RD(0, 1, _kp2) / 3);
    }
  }

  /*
   * Implementation of methods given in
   *
   *   R. Bulirsch
   *   Numerical Calculation of Elliptic Integrals and Elliptic Functions
   *   Numericshe Mathematik 7, 78-90 (1965)
   */

  void EllipticFunction::sncndn(real x, real& sn, real& cn, real& dn) const {
    // Bulirsch's sncndn routine, p 89.
    static const real tolJAC =
      sqrt(numeric_limits<real>::epsilon() * real(0.01));
    if (_kp2 != 0) {
      real mc = _kp2, d = 0;
      if (signbit(_kp2)) {
        // This implements DLMF Eqs 22.17.2 - 22.17.4.  But this only
        // accomodates kp2 < 0 or k2 > 1 and these are outside the advertized
        // ranges for the contructor for this class.
        d = 1 - mc;
        mc /= -d;
        d = sqrt(d);
        x *= d;
      }
      real c = 0;           // To suppress warning about uninitialized variable
      real m[num_], n[num_];
      unsigned l = 0;
      for (real a = 1;
           l < num_ ||
             GEOGRAPHICLIB_PANIC("Convergence failure in EllipticFunction");
           ++l) {
        // This converges quadratically.  Max 5 trips
        m[l] = a;
        n[l] = mc = sqrt(mc);
        c = (a + mc) / 2;
        if (!(fabs(a - mc) > tolJAC * a)) {
          ++l;
          break;
        }
        mc *= a;
        a = c;
      }
      x *= c;
      sn = sin(x);
      cn = cos(x);
      dn = 1;
      if (sn != 0) {
        real a = cn / sn;
        c *= a;
        while (l--) {
          real b = m[l];
          a *= c;
          c *= dn;
          dn = (n[l] + a) / (b + a);
          a = c / b;
        }
        a = 1 / sqrt(c*c + 1);
        sn = signbit(sn) ? -a : a;
        cn = c * sn;
        if (signbit(_kp2)) {
          // See DLMF Eqs 22.17.2 - 22.17.4
          swap(cn, dn);
          sn /= d;
        }
      }
    } else {
      sn = tanh(x);
      dn = cn = 1 / cosh(x);
    }
  }

  Math::real EllipticFunction::am(real x) const {
    // This implements DLMF Sec 22.20(ii).
    // See also Sala (1989), https://doi.org/10.1137/0520100, Sec 5.
    static const real tolJAC =
      pow(numeric_limits<real>::epsilon(), real(0.75));
    real k2 = _k2, kp2 = _kp2;
    if (_k2 == 0)
      return x;
    else if (_kp2 == 0) {
      return atan(sinh(x));     // gd(x)
    } else if (_k2 < 0) {
      // Sala Eq. 5.8
      k2 = -_k2 / _kp2; kp2 = 1 / _kp2;
      x *= sqrt(_kp2);
    }
    real a[num_], b, c[num_];
    a[0] = 1; b = sqrt(kp2); c[0] = sqrt(k2);
    int l = 1;
    for (; l < num_ ||
           GEOGRAPHICLIB_PANIC("Convergence failure in EllipticFunction");) {
      a[l] = (a[l-1] + b) / 2;
      c[l] = (a[l-1] - b) / 2;
      b = sqrt(a[l-1] * b);
      if (!(c[l] > tolJAC * a[l])) break;
      ++l;
    }
    // Now a[l] = pi/(2*K)
    // Need to initialize phi1 to stop Visual Studio complaining
    real phi = a[l] * x * real(1 << l), phi1 = 0;
    for (; l > 0; --l) {
      phi1 = phi;
      phi = (phi + asin(c[l] * sin(phi) / a[l])) / 2;
    }
    // For k2 < 0, see Sala Eq. 5.8
    return _k2 < 0 ? phi1 - phi : phi;
  }

  Math::real EllipticFunction::am(real x, real& sn, real& cn, real& dn) const {
    real phi = am(x);
    if (_kp2 == 0) {
      // Could rely on sin(gd(x)) = tanh(x) and cos(gd(x)) = 1 / cosh(x).  But
      // this is more accurate for large |x|.
      sn = tanh(x); cn = dn = 1 / cosh(x);
    } else {
      sn = sin(phi); cn = cos(phi);
      // See comment following DLMF Eq. 22.20.5
      // dn = cn / cos(phi1 - phi)
      dn = Delta(sn, cn);
    }
    return phi;
  }

  Math::real EllipticFunction::F(real sn, real cn, real dn) const {
    // Carlson, eq. 4.5 and
    // https://dlmf.nist.gov/19.25.E5
    real cn2 = cn*cn, dn2 = dn*dn,
      fi = cn2 != 0 ? fabs(sn) * RF(cn2, dn2, 1) : K();
    // Enforce usual trig-like symmetries
    if (signbit(cn))
      fi = 2 * K() - fi;
    return copysign(fi, sn);
  }

  Math::real EllipticFunction::E(real sn, real cn, real dn) const {
    real
      cn2 = cn*cn, dn2 = dn*dn, sn2 = sn*sn,
      ei = cn2 != 0 ?
      fabs(sn) * ( _k2 <= 0 ?
                   // Carlson, eq. 4.6 and
                   // https://dlmf.nist.gov/19.25.E9
                   RF(cn2, dn2, 1) - _k2 * sn2 * RD(cn2, dn2, 1) / 3 :
                   ( _kp2 >= 0 ?
                     // https://dlmf.nist.gov/19.25.E10
                     _kp2 * RF(cn2, dn2, 1) +
                     _k2 * _kp2 * sn2 * RD(cn2, 1, dn2) / 3 +
                     _k2 * fabs(cn) / dn :
                     // https://dlmf.nist.gov/19.25.E11
                     - _kp2 * sn2 * RD(dn2, 1, cn2) / 3 +
                     dn / fabs(cn) ) ) :
      E();
    // Enforce usual trig-like symmetries
    if (signbit(cn))
      ei = 2 * E() - ei;
    return copysign(ei, sn);
  }

  Math::real EllipticFunction::D(real sn, real cn, real dn) const {
    // Carlson, eq. 4.8 and
    // https://dlmf.nist.gov/19.25.E13
    real
      cn2 = cn*cn, dn2 = dn*dn, sn2 = sn*sn,
      di = cn2 != 0 ? fabs(sn) * sn2 * RD(cn2, dn2, 1) / 3 : D();
    // Enforce usual trig-like symmetries
    if (signbit(cn))
      di = 2 * D() - di;
    return copysign(di, sn);
  }

  Math::real EllipticFunction::Pi(real sn, real cn, real dn) const {
    // Carlson, eq. 4.7 and
    // https://dlmf.nist.gov/19.25.E14
    real
      cn2 = cn*cn, dn2 = dn*dn, sn2 = sn*sn,
      pii = cn2 != 0 ? fabs(sn) * (RF(cn2, dn2, 1) +
                                   _alpha2 * sn2 *
                                   RJ(cn2, dn2, 1, cn2 + _alphap2 * sn2) / 3) :
      Pi();
    // Enforce usual trig-like symmetries
    if (signbit(cn))
      pii = 2 * Pi() - pii;
    return copysign(pii, sn);
  }

  Math::real EllipticFunction::G(real sn, real cn, real dn) const {
    real
      cn2 = cn*cn, dn2 = dn*dn, sn2 = sn*sn,
      gi = cn2 != 0 ? fabs(sn) * (RF(cn2, dn2, 1) +
                                  (_alpha2 - _k2) * sn2 *
                                  RJ(cn2, dn2, 1, cn2 + _alphap2 * sn2) / 3) :
      G();
    // Enforce usual trig-like symmetries
    if (signbit(cn))
      gi = 2 * G() - gi;
    return copysign(gi, sn);
  }

  Math::real EllipticFunction::H(real sn, real cn, real dn) const {
    real
      cn2 = cn*cn, dn2 = dn*dn, sn2 = sn*sn,
      // WARNING: large cancellation if k2 = 1, alpha2 = 0, and phi near pi/2
      hi = cn2 != 0 ? fabs(sn) * (RF(cn2, dn2, 1) -
                                  _alphap2 * sn2 *
                                  RJ(cn2, dn2, 1, cn2 + _alphap2 * sn2) / 3) :
      H();
    // Enforce usual trig-like symmetries
    if (signbit(cn))
      hi = 2 * H() - hi;
    return copysign(hi, sn);
  }

  Math::real EllipticFunction::deltaF(real sn, real cn, real dn) const {
    // Function is periodic with period pi
    if (signbit(cn)) { cn = -cn; sn = -sn; }
    return F(sn, cn, dn) * (Math::pi()/2) / K() - atan2(sn, cn);
  }

  Math::real EllipticFunction::deltaE(real sn, real cn, real dn) const {
    // Function is periodic with period pi
    if (signbit(cn)) { cn = -cn; sn = -sn; }
    return E(sn, cn, dn) * (Math::pi()/2) / E() - atan2(sn, cn);
  }

  Math::real EllipticFunction::deltaPi(real sn, real cn, real dn) const {
    // Function is periodic with period pi
    if (signbit(cn)) { cn = -cn; sn = -sn; }
    return Pi(sn, cn, dn) * (Math::pi()/2) / Pi() - atan2(sn, cn);
  }

  Math::real EllipticFunction::deltaD(real sn, real cn, real dn) const {
    // Function is periodic with period pi
    if (signbit(cn)) { cn = -cn; sn = -sn; }
    return D(sn, cn, dn) * (Math::pi()/2) / D() - atan2(sn, cn);
  }

  Math::real EllipticFunction::deltaG(real sn, real cn, real dn) const {
    // Function is periodic with period pi
    if (signbit(cn)) { cn = -cn; sn = -sn; }
    return G(sn, cn, dn) * (Math::pi()/2) / G() - atan2(sn, cn);
  }

  Math::real EllipticFunction::deltaH(real sn, real cn, real dn) const {
    // Function is periodic with period pi
    if (signbit(cn)) { cn = -cn; sn = -sn; }
    return H(sn, cn, dn) * (Math::pi()/2) / H() - atan2(sn, cn);
  }

  Math::real EllipticFunction::F(real phi) const {
    if (_k2 == 0)
      return phi;
    else if (_kp2 == 0)
      return asinh(tan(phi));
    real sn = sin(phi), cn = cos(phi), dn = Delta(sn, cn);
    return fabs(phi) < Math::pi() ? F(sn, cn, dn) :
      (deltaF(sn, cn, dn) + phi) * K() / (Math::pi()/2);
  }

  Math::real EllipticFunction::E(real phi) const {
    if (_k2 == 0)
      return phi;
    // else if (_kp2 == 0)
    // Despite DLMF Eq 19.6.9 this is probably wrong, since
    // sqrt(1 - k^2*sin(phi)^2) -> abs(cos(phi)) in the limit k -> 1.
    //      return sin(phi);
    real sn = sin(phi), cn = cos(phi), dn = Delta(sn, cn);
    return fabs(phi) < Math::pi() ? E(sn, cn, dn) :
      (deltaE(sn, cn, dn) + phi) * E() / (Math::pi()/2);
  }

  Math::real EllipticFunction::Ed(real ang) const {
    // ang - Math::AngNormalize(ang) is (nearly) an exact multiple of 360
    real n = round((ang - Math::AngNormalize(ang))/Math::td);
    real sn, cn;
    Math::sincosd(ang, sn, cn);
    return E(sn, cn, Delta(sn, cn)) + 4 * E() * n;
  }

  Math::real EllipticFunction::Pi(real phi) const {
    real sn = sin(phi), cn = cos(phi), dn = Delta(sn, cn);
    return fabs(phi) < Math::pi() ? Pi(sn, cn, dn) :
      (deltaPi(sn, cn, dn) + phi) * Pi() / (Math::pi()/2);
  }

  Math::real EllipticFunction::D(real phi) const {
    real sn = sin(phi), cn = cos(phi), dn = Delta(sn, cn);
    return fabs(phi) < Math::pi() ? D(sn, cn, dn) :
      (deltaD(sn, cn, dn) + phi) * D() / (Math::pi()/2);
  }

  Math::real EllipticFunction::G(real phi) const {
    real sn = sin(phi), cn = cos(phi), dn = Delta(sn, cn);
    return fabs(phi) < Math::pi() ? G(sn, cn, dn) :
      (deltaG(sn, cn, dn) + phi) * G() / (Math::pi()/2);
  }

  Math::real EllipticFunction::H(real phi) const {
    real sn = sin(phi), cn = cos(phi), dn = Delta(sn, cn);
    return fabs(phi) < Math::pi() ? H(sn, cn, dn) :
      (deltaH(sn, cn, dn) + phi) * H() / (Math::pi()/2);
  }

  Math::real EllipticFunction::Einv(real x) const {
    static const real tolJAC =
      sqrt(numeric_limits<real>::epsilon() * real(0.01));
    real n = floor(x / (2 * _eEc) + real(0.5));
    x -= 2 * _eEc * n;                      // x now in [-ec, ec)
    // Linear approximation
    real phi = Math::pi() * x / (2 * _eEc); // phi in [-pi/2, pi/2)
    // First order correction
    phi -= _eps * sin(2 * phi) / 2;
    // For kp2 close to zero use asin(x/_eEc) or
    // J. P. Boyd, Applied Math. and Computation 218, 7005-7013 (2012)
    // https://doi.org/10.1016/j.amc.2011.12.021
    for (int i = 0;
         i < num_ ||
           GEOGRAPHICLIB_PANIC("Convergence failure in EllipticFunction");
         ++i) {
      real
        sn = sin(phi),
        cn = cos(phi),
        dn = Delta(sn, cn),
        err = (E(sn, cn, dn) - x)/dn;
      phi -= err;
      if (!(fabs(err) > tolJAC))
        break;
    }
    return n * Math::pi() + phi;
  }

  Math::real EllipticFunction::deltaEinv(real stau, real ctau) const {
    // Function is periodic with period pi
    if (signbit(ctau)) { ctau = -ctau; stau = -stau; }
    real tau = atan2(stau, ctau);
    return Einv( tau * E() / (Math::pi()/2) ) - tau;
  }

} // namespace GeographicLib
