#include <assert.h>
#include <math.h>

#include <exception>
#include <iostream>
#include <limits>
#include <stdexcept>

#include "commons.h"
#include "coordinate.h"
#include "sensor_data.h"

using namespace coordinate_consts;

struct vincenty_result {
  double distance;
  double a1;
  double a2;

  vincenty_result() : distance(0.), a1(0.), a2(0.0) {}
  vincenty_result(double distance, double a1, double a2)
      : distance(distance), a1(a1), a2(a2) {}
};

static vincenty_result vincenty_inverse(double lat1, double lon1, double lat2,
                                        double lon2);
//////////////////////////////////////////////////////////////

static double geo_distance_meters(double lat1, double lon1, double lat2,
                                  double lon2);
static double azimuth_between_points(double lat1, double lon1, double lat2,
                                     double lon2);

// vincent_direct problem
static geopoint point_ahead(geopoint point, double distance,
                            double azimuth_degrees);

static geopoint point_plus_distance_east(geopoint point, double distance);
static geopoint point_plus_distance_north(geopoint point, double distance);

static double coord_longitude_to_meters(double lon);
static double coord_latitude_to_meters(double lat);
static geopoint coord_meters_to_geopoint(double lon_meters, double lat_meters);

//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////

vincenty_result vincenty_inverse(double lat1, double lon1, double lat2,
                                 double lon2) {
  // https://en.wikipedia.org/wiki/Azimuth
  // https://en.wikipedia.org/wiki/Vincenty%27s_formulae#Inverse_problem
  double a = major_axis;
  double f = inv_flattening;
  double b = minor_axis;
  // L, U1 and U2 in radians
  double L = degree_to_rad(lon2 - lon1);
  double tanU1 = (1 - f) * tan(degree_to_rad(lat1));
  double tanU2 = (1 - f) * tan(degree_to_rad(lat2));
  double U1 = atan(tanU1);
  double U2 = atan(tanU2);

  double sinU1 = sin(U1);
  double sinU2 = sin(U2);
  double cosU1 = cos(U1);
  double cosU2 = cos(U2);

  double l = L, lp = 2 * M_PI;

  double sinL = 0., cosL = 0.;
  double sinS = 0., cosS = 0., sig = 0.;
  double sinA = 0., cosSqA = 0.;
  double cos2SM = 0.;
  int iters = 100;
  do {
    // iteration
    sinL = sin(l);
    cosL = cos(l);
    sinS = sqrt((cosU2 * sinL) * (cosU2 * sinL) +
                (cosU1 * sinU2 - sinU1 * cosU2 * cosL) *
                    (cosU1 * sinU2 - sinU1 * cosU2 * cosL));

    // if (sinSigma == 0.0) { }
    if (fabs(sinS) < std::numeric_limits<double>::epsilon()) {
      return vincenty_result(0.0, 0.0, 0.0);  // co-incident points
    }

    cosS = sinU1 * sinU2 + cosU1 * cosU2 * cosL;
    sig = atan2(sinS, cosS);
    sinA = cosU1 * cosU2 * sinL / sinS;

    cosSqA = 1.0 - sinA * sinA;
    cos2SM = (cosSqA != 0.0) ? (cosS - 2.0 * sinU1 * sinU2 / cosSqA) : 0.0;

    double C = f / 16.0 * cosSqA * (4.0 + f * (4.0 - 3.0 * cosSqA));

    lp = l;
    l = L +
        (1.0 - C) * f * sinA *
            (sig +
             C * sinS * (cos2SM + C * cosS * (-1.0 + 2.0 * cos2SM * cos2SM)));

  } while ((fabs(l - lp) > 1e-12) && --iters);

  if (!iters) {
    throw std::invalid_argument("equation does not converge");
  }

  double uSq = cosSqA * (a * a - b * b) / (b * b);
  /* https://en.wikipedia.org/wiki/Vincenty%27s_formulae#Vincenty's_modification
   */
  double k1 = (sqrt(1 + uSq) - 1) / (sqrt(1 + uSq) + 1);
  double A = (1 + 0.25 * k1 * k1) / (1 - k1);
  double B = k1 * (1. - 3. / 8. * k1 * k1);
  /* double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 *
   * uSq))); */
  /* double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq))); */
  double ds = B * sinS *
              (cos2SM + B / 4. *
                            (cosS * (-1. + 2. * cos2SM * cos2SM) -
                             B / 6. * cos2SM * (-3. + 4. * sinS * sinS) *
                                 (-3. + 4. * cos2SM * cos2SM)));
  double s = b * A * (sig - ds);
  double a1 = atan2(cosU2 * sinL, cosU1 * sinU2 - sinU1 * cosU2 * cosL);
  double a2 = atan2(cosU1 * sinL, -sinU1 * cosU2 + cosU1 * sinU2 * cosL);
  return vincenty_result(s, a1, a2);
}
//////////////////////////////////////////////////////////////

double geo_distance_meters(double lat1, double lon1, double lat2, double lon2) {
  vincenty_result vr = vincenty_inverse(lat1, lon1, lat2, lon2);
  return vr.distance;
}
///////////////////////////////////////////////////////

double azimuth_between_points(double lat1, double lon1, double lat2,
                              double lon2) {
  vincenty_result vr = vincenty_inverse(lat1, lon1, lat2, lon2);
  return vr.a1;
}
//////////////////////////////////////////////////////////////

geopoint point_ahead(geopoint point, double distance, double azimuth_degrees) {
  // https://en.wikipedia.org/wiki/Azimuth
  // https://en.wikipedia.org/wiki/Vincenty%27s_formulae#Direct_problem
  double a = major_axis;
  double f = inv_flattening;
  double b = minor_axis;

  double a1 = degree_to_rad(azimuth_degrees);
  double cosa1 = cos(a1);
  double sina1 = sin(a1);

  double tanU1 = (1 - f) * tan(degree_to_rad(point.latitude));
  double U1 = atan(tanU1);
  double cosU1 = cos(U1);
  double sinU1 = sin(U1);
  double sig1 = atan2(tanU1, cosa1);
  double sina = cosU1 * sina1;

  double uSq = (1 - sina * sina) * (a * a - b * b) / (b * b);
  /* https://en.wikipedia.org/wiki/Vincenty%27s_formulae#Vincenty's_modification
   */
  double k1 = (sqrt(1 + uSq) - 1) / (sqrt(1 + uSq) + 1);
  double A = (1 + 0.25 * k1 * k1) / (1 - k1);
  double B = k1 * (1. - 3. / 8. * k1 * k1);
  /* double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 *
   * uSq))); */
  /* double B = (uSq / 1024) * (256 + uSq * (-128 + uSq * (74 - 47 * uSq))); */

  double sinSig = 0., cosSig = 0.;
  double cos2sigM = 0.;
  double sig = distance / (b * A);
  double sigp;  // prev
  int iters = 100;
  do {
    cos2sigM = cos(2 * sig1 + sig);
    sinSig = sin(sig);
    cosSig = cos(sig);
    double dsig =
        B * sinSig *
        (cos2sigM + B / 4. *
                        (cosSig * (-1. + 2. * cos2sigM * cos2sigM) -
                         B / 6. * cos2sigM * (-3. + 4. * sinSig * sinSig) *
                             (-3. + 4. * cos2sigM * cos2sigM)));
    sigp = sig;
    sig = distance / (b * A) + dsig;
  } while ((fabs(sig - sigp) > 1e-12) && --iters);

  if (!iters) {
    throw std::invalid_argument("equation does not converge");
  }

  double x = sinU1 * sinSig - cosU1 * cosSig * cosa1;
  double lat2 = rad_to_degree(atan2(sinU1 * cosSig + cosU1 * sinSig * cosa1,
                                    (1. - f) * sqrt(sina * sina + x * x)));
  double l = atan2(sinSig * sina1, cosU1 * cosSig - sinU1 * sinSig * cosa1);
  double cosSqa = 1. - sina * sina;
  double C = f / 16. * cosSqa * (4. + f * (4. - 3. * cosSqa));
  double L =
      l - (1. - C) * f * sina *
              (sig +
               C * sinSig *
                   (cos2sigM + C * cosSig * (-1. + 2. * cos2sigM * cos2sigM)));
  double l1 = degree_to_rad(point.longitude);
  // normalize to -180..+180
  double lon2 = fmod(L + l1 + 3 * M_PI, 2 * M_PI) - M_PI;
  lon2 = rad_to_degree(lon2);
  /* double a2 = atan2(sina, -sinU1 * sinSig + cosU1 * cosSig * cosa1); */
  return geopoint(lat2, lon2);
}
///////////////////////////////////////////////////////

geopoint point_plus_distance_east(geopoint point, double distance) {
  return point_ahead(point, distance, 90.0);
}
//////////////////////////////////////////////////////////////

geopoint point_plus_distance_north(geopoint point, double distance) {
  return point_ahead(point, distance, 0.0);
}
//////////////////////////////////////////////////////////////

double coord_longitude_to_meters(double lon) {
  double distance = geo_distance_meters(0.0, lon, 0.0, 0.0);
  return distance * (lon < 0.0 ? -1.0 : 1.0);
}
//////////////////////////////////////////////////////////////

double coord_latitude_to_meters(double lat) {
  double distance = geo_distance_meters(lat, 0.0, 0.0, 0.0);
  return distance * (lat < 0.0 ? -1.0 : 1.0);
}
//////////////////////////////////////////////////////////////

geopoint coord_meters_to_geopoint(double lon_meters, double lat_meters) {
  geopoint point(0.0, 0.0);  // = {.latitude = 0.0, .longitude = 0.0};
  geopoint point_east = point_plus_distance_east(point, lon_meters);
  geopoint point_north_east = point_plus_distance_north(point_east, lat_meters);
  return point_north_east;
}
//////////////////////////////////////////////////////////////

coordinates_vptr coord_vptr_hq(void) {
  coordinates_vptr res = {
      .distance_between_points = geo_distance_meters,
      .longitude_to_meters = coord_longitude_to_meters,
      .latitude_to_meters = coord_latitude_to_meters,
      .meters_to_geopoint = coord_meters_to_geopoint,
      .point_ahead = point_ahead,
      .azimuth_between_points = azimuth_between_points,
  };
  return res;
}
//////////////////////////////////////////////////////////////
