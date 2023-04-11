#include "commons.h"
#include "coordinate.h"
#include "sensor_data.h"
#include <assert.h>
#include <math.h>

using namespace coordinate_consts;

static double geo_distance_meters(double lon1, double lat1, double lon2,
                                  double lat2);
static gps_location_t point_ahead(gps_location_t point, double distance,
                                  double azimuth_degrees);

static gps_location_t point_plus_distance_east(gps_location_t point,
                                               double distance);
static gps_location_t point_plus_distance_north(gps_location_t point,
                                                double distance);

static double coord_distance_between_points_meters(double lat1, double lon1,
                                                   double lat2, double lon2);
static double coord_longitude_to_meters(double lon);

static double coord_latitude_to_meters(double lat);

static gps_location_t coord_meters_to_geopoint(double lon_meters,
                                               double lat_meters);

//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////

double geo_distance_meters(double lon1, double lat1, double lon2, double lat2) {
  double f1 = degree_to_rad(lat1), l1 = degree_to_rad(lon1);
  double f2 = degree_to_rad(lat2), l2 = degree_to_rad(lon2);
  double L = l2 - l1;
  double tanU1 = (1 - ellipse_coeff) * tan(f1);
  double cosU1 = 1 / sqrt((1.0 + tanU1 * tanU1));
  double sinU1 = tanU1 * cosU1;
  double tanU2 = (1 - ellipse_coeff) * tan(f2);
  double cosU2 = 1 / sqrt((1.0 + tanU2 * tanU2));
  double sinU2 = tanU2 * cosU2;

  double l = L, ll;
  double cosSqa = 0.0, cos2sigM = 0.0, sins = 0.0, coss = 0.0, sig = 0.0;

  int iterations = 1000;
  do {
    double sinl, cosl, C;
    double sinSqs, sina;
    sinl = sin(l);
    cosl = cos(l);
    sinSqs = (cosU2 * sinl) * (cosU2 * sinl) +
             (cosU1 * sinU2 - sinU1 * cosU2 * cosl) *
                 (cosU1 * sinU2 - sinU1 * cosU2 * cosl);
    if (sinSqs == 0.0)
      break; // co-incident points
    sins = sqrt(sinSqs);
    coss = sinU1 * sinU2 + cosU1 * cosU2 * cosl;
    sig = atan2(sins, coss);
    sina = cosU1 * cosU2 * sinl / sins;
    cosSqa = 1 - sina * sina;
    cos2sigM = (cosSqa != 0.0) ? (coss - 2 * sinU1 * sinU2 / cosSqa)
                               : 0.0; // equatorial line: cosSqα=0 (§6)
    C = ellipse_coeff / 16 * cosSqa * (4 + ellipse_coeff * (4 - 3 * cosSqa));
    ll = l;
    l = L +
        (1 - C) * ellipse_coeff * sina *
            (sig +
             C * sins * (cos2sigM + C * coss * (-1 + 2 * cos2sigM * cos2sigM)));
    if (fabs(l) > M_PI)
      return 0.0;
  } while (fabs(l - ll) > 1e-12 && iterations--);

  if (!iterations)
    return 0.0;

  double uSq =
      cosSqa * (majAxis * majAxis - minAxis * minAxis) / (minAxis * minAxis);
  double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
  double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
  double dsig = B * sins *
                (cos2sigM + B / 4 *
                                (coss * (-1 + 2 * cos2sigM * cos2sigM) -
                                 B / 6 * cos2sigM * (-3 + 4 * sins * sins) *
                                     (-3 + 4 * cos2sigM * cos2sigM)));

  double s = minAxis * A * (sig - dsig);
  return s;
}
///////////////////////////////////////////////////////

gps_location_t point_ahead(gps_location_t point, double distance,
                           double azimuth_degrees) {
  double t1 = degree_to_rad(point.latitude);
  double l1 = degree_to_rad(point.longitude);
  double a1 = degree_to_rad(azimuth_degrees);
  double sina1 = sin(a1);
  double cosa1 = cos(a1);

  double tanU1 = (1 - ellipse_coeff) * tan(t1);
  double cosU1 = 1 / sqrt((1 + tanU1 * tanU1));
  double sinU1 = tanU1 * cosU1;
  double sig1 = atan2(tanU1, cosa1);
  double sina = cosU1 * sina1;
  double cosSqa = 1 - sina * sina;
  double uSq =
      cosSqa * (majAxis * majAxis - minAxis * minAxis) / (minAxis * minAxis);
  double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
  double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
  double cos2sigM, sinSig, cosSig;
  double sig = distance / (minAxis * A);
  double ddsig;

  int iterations = 100;
  do {
    cos2sigM = cos(2 * sig1 + sig);
    sinSig = sin(sig);
    cosSig = cos(sig);
    double dsig =
        B * sinSig *
        (cos2sigM + B / 4 *
                        (cosSig * (-1 + 2 * cos2sigM * cos2sigM) -
                         B / 6 * cos2sigM * (-3 + 4 * sinSig * sinSig) *
                             (-3 + 4 * cos2sigM * cos2sigM)));
    ddsig = sig;
    sig = distance / (minAxis * A) + dsig;
  } while (fabs(sig - ddsig) > 1e-12 && iterations--);

  assert(iterations); // hack!

  double x = sinU1 * sinSig - cosU1 * cosSig * cosa1;
  double l = atan2(sinSig * sina1, cosU1 * cosSig - sinU1 * sinSig * cosa1);
  double C =
      ellipse_coeff / 16 * cosSqa * (4 + ellipse_coeff * (4 - 3 * cosSqa));
  double L =
      l -
      (1 - C) * ellipse_coeff * sina *
          (sig + C * sinSig *
                     (cos2sigM + C * cosSig * (-1 + 2 * cos2sigM * cos2sigM)));
  double l2 =
      fmod((l1 + L + 3 * M_PI), (2 * M_PI)) - M_PI; // normalise to -180..+180

  gps_location_t res;
  res.latitude =
      rad_to_degree(atan2(sinU1 * cosSig + cosU1 * sinSig * cosa1,
                          (1 - ellipse_coeff) * sqrt(sina * sina + x * x)));
  res.longitude = rad_to_degree(l2);
  return res;
}
///////////////////////////////////////////////////////

gps_location_t point_plus_distance_east(gps_location_t point, double distance) {
  return point_ahead(point, distance, 90.0);
}
//////////////////////////////////////////////////////////////

gps_location_t point_plus_distance_north(gps_location_t point,
                                         double distance) {
  return point_ahead(point, distance, 0.0);
}
//////////////////////////////////////////////////////////////

double coord_distance_between_points_meters(double lat1, double lon1,
                                            double lat2, double lon2) {
  return geo_distance_meters(lon1, lat1, lon2, lat2);
}
//////////////////////////////////////////////////////////////

double coord_longitude_to_meters(double lon) {
  double distance = geo_distance_meters(lon, 0.0, 0.0, 0.0);
  return distance * (lon < 0.0 ? -1.0 : 1.0);
}
//////////////////////////////////////////////////////////////

double coord_latitude_to_meters(double lat) {
  double distance = geo_distance_meters(0.0, lat, 0.0, 0.0);
  return distance * (lat < 0.0 ? -1.0 : 1.0);
}
//////////////////////////////////////////////////////////////

gps_location_t coord_meters_to_geopoint(double lon_meters, double lat_meters) {
  gps_location_t point; // = {.latitude = 0.0, .longitude = 0.0};
  gps_location_t point_east = point_plus_distance_east(point, lon_meters);
  gps_location_t point_north_east =
      point_plus_distance_north(point_east, lat_meters);
  return point_north_east;
}
//////////////////////////////////////////////////////////////

coordinates_vptr coord_vptr_hq(void) {
  coordinates_vptr res = {
      .distance_between_points = coord_distance_between_points_meters,
      .longitude_to_meters = coord_longitude_to_meters,
      .latitude_to_meters = coord_latitude_to_meters,
      .meters_to_geopoint = coord_meters_to_geopoint,
      .point_ahead = point_ahead,
  };
  return res;
}
//////////////////////////////////////////////////////////////
