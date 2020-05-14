#include <math.h>
#include <assert.h>

#include "Coordinates.h"
#include "Geohash.h"
#include "Commons.h"

#define EARTH_RADIUS (6371.0 * 1000.0) // meters
#define ACTUAL_GRAVITY 9.80665

#define majAxis 6378137.0   // meter
#define ellipseCoefficient (1.0 / 298.257223563)
#define minAxis ((1 - ellipseCoefficient) * majAxis) // meter

//https://en.wikipedia.org/wiki/Great-circle_distance
static double geoDistanceMeters(double lon1, double lat1,
                                double lon2, double lat2);
static double geoDistanceMetersHQ(double lon1, double lat1,
                                  double lon2, double lat2);
static geopoint_t getPointAhead(geopoint_t point,
                                double distance,
                                double azimuthDegrees);
static geopoint_t getPointAheadHQ(geopoint_t point,
                                  double distance,
                                  double azimuthDegrees);
static geopoint_t pointPlusDistanceEast(geopoint_t point, double distance);
static geopoint_t pointPlusDistanceNorth(geopoint_t point, double distance);
static geopoint_t pointPlusDistanceEastHQ(geopoint_t point, double distance);
static geopoint_t pointPlusDistanceNorthHQ(geopoint_t point, double distance);

double
geoDistanceMeters(double lon1, double lat1,
                  double lon2, double lat2) {
  double deltaLon = degree_to_rad(lon2 - lon1);
  double deltaLat = degree_to_rad(lat2 - lat1);
  double a = pow(sin(deltaLat / 2.0), 2.0) +
             cos(degree_to_rad(lat1))*
             cos(degree_to_rad(lat2))*
             pow(sin(deltaLon / 2.0), 2.0);
  double c = 2.0 * atan2(sqrt(a), sqrt(1.0-a));
  return EARTH_RADIUS * c;
}
//////////////////////////////////////////////////////////////////////////

double
geoDistanceMetersHQ(double lon1, double lat1,
                    double lon2, double lat2) {
  double f1 = degree_to_rad(lat1), l1 = degree_to_rad(lon1);
  double f2 = degree_to_rad(lat2), l2 = degree_to_rad(lon2);
  double L = l2 - l1;
  double tanU1 = (1-ellipseCoefficient) * tan(f1);
  double cosU1 = 1 / sqrt((1.0 + tanU1*tanU1));
  double sinU1 = tanU1 * cosU1;
  double tanU2 = (1-ellipseCoefficient) * tan(f2);
  double cosU2 = 1 / sqrt((1.0 + tanU2*tanU2));
  double sinU2 = tanU2 * cosU2;

  double sinl, cosl;
  double sinSqs, sins=0.0, coss=0.0, sig=0.0, sina, cosSqa=0.0, cos2sigM=0.0, C;
  double l = L, ll;
  int iterations = 1000;

  do {
    sinl = sin(l);
    cosl = cos(l);
    sinSqs = (cosU2*sinl) * (cosU2*sinl) + (cosU1*sinU2-sinU1*cosU2*cosl) * (cosU1*sinU2-sinU1*cosU2*cosl);
    if (sinSqs == 0.0)
      break; // co-incident points
    sins = sqrt(sinSqs);
    coss = sinU1*sinU2 + cosU1*cosU2*cosl;
    sig = atan2(sins, coss);
    sina = cosU1 * cosU2 * sinl / sins;
    cosSqa = 1 - sina*sina;
    cos2sigM = (cosSqa != 0.0) ? (coss - 2*sinU1*sinU2/cosSqa) : 0.0; // equatorial line: cosSqα=0 (§6)
    C = ellipseCoefficient/16*cosSqa*(4+ellipseCoefficient*(4-3*cosSqa));
    ll = l;
    l = L + (1-C) * ellipseCoefficient * sina * (sig + C*sins*(cos2sigM+C*coss*(-1+2*cos2sigM*cos2sigM)));
    if (fabs(l) > M_PI)
      return 0.0;
  } while (fabs(l-ll) > 1e-12 && iterations--);

  if (!iterations)
    return 0.0;

  double uSq = cosSqa * (majAxis*majAxis - minAxis*minAxis) / (minAxis*minAxis);
  double A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)));
  double B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));
  double dsig = B*sins*(cos2sigM+B/4*(coss*(-1+2*cos2sigM*cos2sigM)-
                                      B/6*cos2sigM*(-3+4*sins*sins)*(-3+4*cos2sigM*cos2sigM)));

  double s = minAxis*A*(sig-dsig);
  return s;
}
///////////////////////////////////////////////////////

double
coord_longitude_to_meters(double lon) {
  double distance = geoDistanceMeters(lon, 0.0, 0.0, 0.0);
  return distance * (lon < 0.0 ? -1.0 : 1.0);
}

double
coord_longitude_to_meters_hq(double lon) {
  double distance = geoDistanceMetersHQ(lon, 0.0, 0.0, 0.0);
  return distance * (lon < 0.0 ? -1.0 : 1.0);
}
//////////////////////////////////////////////////////////////////////////

double
coord_latitude_to_meters(double lat) {
  double distance = geoDistanceMeters(0.0, lat, 0.0, 0.0);
  return distance * (lat < 0.0 ? -1.0 : 1.0);
}


double
coord_latitude_to_meters_hq(double lat) {
  double distance = geoDistanceMetersHQ(0.0, lat, 0.0, 0.0);
  return distance * (lat < 0.0 ? -1.0 : 1.0);
}
//////////////////////////////////////////////////////////////////////////

geopoint_t
getPointAhead(geopoint_t point,
              double distance,
              double azimuthDegrees) {
  geopoint_t res;
  double radiusFraction = distance / EARTH_RADIUS;
  double bearing = degree_to_rad(azimuthDegrees);
  double lat1 = degree_to_rad(point.Latitude);
  double lng1 = degree_to_rad(point.Longitude);

  double lat2_part1 = sin(lat1) * cos(radiusFraction);
  double lat2_part2 = cos(lat1) * sin(radiusFraction) * cos(bearing);
  double lat2 = asin(lat2_part1 + lat2_part2);

  double lng2_part1 = sin(bearing) * sin(radiusFraction) * cos(lat1);
  double lng2_part2 = cos(radiusFraction) - sin(lat1) * sin(lat2);
  double lng2 = lng1 + atan2(lng2_part1, lng2_part2);
  lng2 = fmod(lng2 + 3.0*M_PI, 2.0*M_PI) - M_PI;

  res.Latitude = rad_to_degree(lat2);
  res.Longitude = rad_to_degree(lng2);
  return res;
}
//////////////////////////////////////////////////////////////////////////

geopoint_t
getPointAheadHQ(geopoint_t point,
                double distance,
                double azimuthDegrees) {
  double t1 = degree_to_rad(point.Latitude);
  double l1 = degree_to_rad(point.Longitude);
  double a1 = degree_to_rad(azimuthDegrees);
  double sina1 = sin(a1);
  double cosa1 = cos(a1);

  double tanU1 = (1-ellipseCoefficient) * tan(t1);
  double cosU1 = 1 / sqrt((1 + tanU1*tanU1));
  double sinU1 = tanU1 * cosU1;
  double sig1 = atan2(tanU1, cosa1);
  double sina = cosU1 * sina1;
  double cosSqa = 1 - sina*sina;
  double uSq = cosSqa * (majAxis*majAxis - minAxis*minAxis) / (minAxis*minAxis);
  double A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)));
  double B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));
  double cos2sigM, sinSig, cosSig, dsig;
  double sig = distance / (minAxis*A);
  double ddsig;

  int iterations = 100;

  do {
    cos2sigM = cos(2*sig1 + sig);
    sinSig = sin(sig);
    cosSig = cos(sig);
    dsig = B*sinSig*(cos2sigM+B/4*(cosSig*(-1+2*cos2sigM*cos2sigM)-
                                   B/6*cos2sigM*(-3+4*sinSig*sinSig)*(-3+4*cos2sigM*cos2sigM)));
    ddsig = sig;
    sig = distance / (minAxis*A) + dsig;
  } while (fabs(sig-ddsig) > 1e-12 && iterations--);

  assert(iterations); //hack!

  double x = sinU1*sinSig - cosU1*cosSig*cosa1;
  double l = atan2(sinSig*sina1, cosU1*cosSig - sinU1*sinSig*cosa1);
  double C = ellipseCoefficient/16*cosSqa*(4+ellipseCoefficient*(4-3*cosSqa));
  double L = l - (1-C) * ellipseCoefficient * sina *
             (sig + C*sinSig*(cos2sigM+C*cosSig*(-1+2*cos2sigM*cos2sigM)));
  double l2 = fmod((l1+L+3*M_PI) , (2*M_PI)) - M_PI;  // normalise to -180..+180

  geopoint_t res;
  res.Latitude = rad_to_degree(atan2(sinU1*cosSig + cosU1*sinSig*cosa1,
                                  (1-ellipseCoefficient)*sqrt(sina*sina + x*x)));
  res.Longitude = rad_to_degree(l2);
  return res;
}
///////////////////////////////////////////////////////

geopoint_t
pointPlusDistanceEast(geopoint_t point, double distance) {
  return getPointAhead(point, distance, 90.0);
}

geopoint_t
pointPlusDistanceNorth(geopoint_t point, double distance) {
  return getPointAhead(point, distance, 0.0);
}
//////////////////////////////////////////////////////////////////////////

geopoint_t
pointPlusDistanceEastHQ(geopoint_t point, double distance) {
  return getPointAheadHQ(point, distance, 90.0);
}

geopoint_t
pointPlusDistanceNorthHQ(geopoint_t point, double distance) {
  return getPointAheadHQ(point, distance, 0.0);
}
//////////////////////////////////////////////////////////////////////////

geopoint_t
coord_meters_to_geopoint(double lonMeters,
                      double latMeters) {
  geopoint_t point = {.Latitude = 0.0, .Longitude = 0.0};
  geopoint_t pointEast = pointPlusDistanceEast(point, lonMeters);
  geopoint_t pointNorthEast = pointPlusDistanceNorth(pointEast, latMeters);
  return pointNorthEast;
}

geopoint_t
coord_meters_to_geopoint_hq(double lonMeters,
                        double latMeters) {
  geopoint_t point = {.Latitude = 0.0, .Longitude = 0.0};
  geopoint_t pointEast = pointPlusDistanceEastHQ(point, lonMeters);
  geopoint_t pointNorthEast = pointPlusDistanceNorthHQ(pointEast, latMeters);
  return pointNorthEast;
}
//////////////////////////////////////////////////////////////////////////

double
coord_distance_between_points_meters(double lat1, double lon1,
                                 double lat2, double lon2) {
  return geoDistanceMeters(lon1, lat1, lon2, lat2);
}

double
coord_distance_between_points_meters_hq(double lat1, double lon1,
                                   double lat2, double lon2) {
  return geoDistanceMetersHQ(lon1, lat1, lon2, lat2);
}
///////////////////////////////////////////////////////
