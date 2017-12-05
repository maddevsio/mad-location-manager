#include <QDebug>
#include <QFile>
#include <vector>
#include <QRegExp>
#include <math.h>

#include "Coordinates.h"
#include "Geohash.h"
#include "Commons.h"
#include "SensorController.h"

std::vector<geopoint_t> GetCoordsFromFile(const QString& filePath) {
  std::vector<geopoint_t> lstResult;
  QFile f(filePath);
  SensorData_t sd;
  if (!f.open(QFile::ReadOnly)) {
    qDebug() << "Open file " << filePath << " error : " << f.errorString();
    return lstResult;
  }

  while (!f.atEnd()) {
    QString line = f.readLine();
    if (!SensorControllerParseDataString(line.toStdString().c_str(), &sd))
      continue;
    lstResult.push_back(geopoint_t(sd.gpsLat, sd.gpsLon));
  }

  f.close();
  return lstResult;
}
//////////////////////////////////////////////////////////////////////////

std::vector<geopoint_t>
FilterByGeoHash(std::vector<geopoint_t> &lstSrc,
                int precision,
                int minPointCount) {
  struct cindex {
    size_t index;
    int count;
  };
  static char buff[GEOHASH_MAX_PRECISION+1] = {0};

  std::vector<geopoint_t> lstRes;
  std::map<std::string, cindex> dctHashCount;

  for (geopoint_t coord : lstSrc) {
    GeohashEncode(coord.Latitude, coord.Longitude, buff, precision);
    std::string geohash(buff, precision);

    if (dctHashCount.find(geohash) == dctHashCount.end())
      dctHashCount[geohash].count = 0;

    if (++dctHashCount[geohash].count == minPointCount) {
      lstRes.push_back(coord);
      dctHashCount[geohash].index = lstRes.size()-1;
      continue;
    }

    if (dctHashCount[geohash].count > minPointCount) {
      lstRes[dctHashCount[geohash].index].Latitude += coord.Latitude;
      lstRes[dctHashCount[geohash].index].Latitude /= 2.0;
      lstRes[dctHashCount[geohash].index].Longitude += coord.Longitude;
      lstRes[dctHashCount[geohash].index].Longitude /= 2.0;
    }
  }

  return lstRes;
}
//////////////////////////////////////////////////////////////////////////

//https://en.wikipedia.org/wiki/Great-circle_distance
static double geoDistanceMeters(double lon1, double lat1,
                                double lon2, double lat2) {
  double deltaLon = Degree2Rad(lon2 - lon1);
  double deltaLat = Degree2Rad(lat2 - lat1);
  double a = pow(sin(deltaLat / 2.0), 2.0) +
             cos(Degree2Rad(lat1))*
             cos(Degree2Rad(lat2))*
             pow(sin(deltaLon/2.0), 2.0);
  double c = 2.0 * atan2(sqrt(a), sqrt(1.0-a));
  return EARTH_RADIUS * c;
}
//////////////////////////////////////////////////////////////////////////

double LongitudeToMeters(double lon) {
  double distance = geoDistanceMeters(lon, 0.0, 0.0, 0.0);
  return distance * (lon < 0.0 ? -1.0 : 1.0);
}
//////////////////////////////////////////////////////////////////////////

double LatitudeToMeters(double lat) {
  double distance = geoDistanceMeters(0.0, lat, 0.0, 0.0);
  return distance * (lat < 0.0 ? -1.0 : 1.0);
}
//////////////////////////////////////////////////////////////////////////

static geopoint_t getPointAhead(geopoint_t point,
                                double distance,
                                double azimuthDegrees) {
  geopoint_t res;
  double radiusFraction = distance / EARTH_RADIUS;
  double bearing = Degree2Rad(azimuthDegrees);
  double lat1 = Degree2Rad(point.Latitude);
  double lng1 = Degree2Rad(point.Longitude);

  double lat2_part1 = sin(lat1) * cos(radiusFraction);
  double lat2_part2 = cos(lat1) * sin(radiusFraction) * cos(bearing);
  double lat2 = asin(lat2_part1 + lat2_part2);

  double lng2_part1 = sin(bearing) * sin(radiusFraction) * cos(lat1);
  double lng2_part2 = cos(radiusFraction) - sin(lat1) * sin(lat2);
  double lng2 = lng1 + atan2(lng2_part1, lng2_part2);
  lng2 = fmod(lng2 + 3.0*M_PI, 2.0*M_PI) - M_PI;

  res.Latitude = Rad2Degree(lat2);
  res.Longitude = Rad2Degree(lng2);
  return res;
}
//////////////////////////////////////////////////////////////////////////

static geopoint_t pointPlusDistanceEast(geopoint_t point, double distance) {
  return getPointAhead(point, distance, 90.0);
}

static geopoint_t pointPlusDistanceNorth(geopoint_t point, double distance) {
  return getPointAhead(point, distance, 0.0);
}
//////////////////////////////////////////////////////////////////////////

geopoint_t MetersToGeopoint(double lonMeters,
                            double latMeters) {
  geopoint_t point = {0.0, 0.0};
  geopoint_t pointEast = pointPlusDistanceEast(point, lonMeters);
  geopoint_t pointNorthEast = pointPlusDistanceNorth(pointEast, latMeters);
  return pointNorthEast;
}
//////////////////////////////////////////////////////////////////////////
