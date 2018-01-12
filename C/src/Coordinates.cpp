#include <QDebug>
#include <QFile>
#include <vector>
#include <QRegExp>
#include <math.h>

#include "Coordinates.h"
#include "Geohash.h"
#include "Commons.h"
#include "SensorController.h"


std::vector<geopoint_t> CoordGetFromFile(const QString& filePath) {
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
    if (sd.gpsLat == 0.0 || sd.gpsLon == 0.0)
      continue;
    lstResult.push_back(geopoint_t(sd.gpsLat, sd.gpsLon));
  }

  f.close();
  return lstResult;
}
//////////////////////////////////////////////////////////////////////////

std::vector<geopoint_t>
CoordFilterByGeoHash(std::vector<geopoint_t> &lstSrc,
                int precision,
                int minPointCount) {
  struct cindex {
    int index;
    int count;
    double lon, lat;
  };
  static char buff[GEOHASH_MAX_PRECISION+1] = {0};

  std::vector<geopoint_t> lstRes;
  std::map<std::string, cindex> dctHashCount;
  typedef std::map<std::string, cindex>::iterator dctIter;

  int idx = 0;
  for (auto ci = lstSrc.begin(); ci != lstSrc.end(); ++ci) {
    GeohashEncode(ci->Latitude, ci->Longitude, buff, precision);
    std::string geohash(buff, precision);
    dctIter it = dctHashCount.find(geohash);
    if (it == dctHashCount.end()) {
      cindex ni;
      ni.count = 0;
      ni.lat = 0.0;
      ni.lon = 0.0;
      ni.index = -1;
      auto ir = dctHashCount.insert(std::pair<std::string, cindex>(geohash, ni));
      it = ir.first;
    }
    if (++it->second.count == minPointCount)
      it->second.index = idx++;
    it->second.lat += ci->Latitude;
    it->second.lon += ci->Longitude;
  }

  lstRes.reserve(idx);
  lstRes.resize(idx);

  for (auto it = dctHashCount.begin(); it != dctHashCount.end(); ++it) {
    if (it->second.index == -1) continue;
    geopoint_t np;
    np.Latitude = it->second.lat / it->second.count;
    np.Longitude = it->second.lon / it->second.count;
    lstRes[it->second.index] = np;
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

double CoordLongitudeToMeters(double lon) {
  double distance = geoDistanceMeters(lon, 0.0, 0.0, 0.0);
  return distance * (lon < 0.0 ? -1.0 : 1.0);
}
//////////////////////////////////////////////////////////////////////////

double CoordLatitudeToMeters(double lat) {
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

geopoint_t CoordMetersToGeopoint(double lonMeters,
                            double latMeters) {
  geopoint_t point = {0.0, 0.0};
  geopoint_t pointEast = pointPlusDistanceEast(point, lonMeters);
  geopoint_t pointNorthEast = pointPlusDistanceNorth(pointEast, latMeters);
  return pointNorthEast;
}
//////////////////////////////////////////////////////////////////////////

double CoordDistanceBetweenPointsMeters(double lat1, double lon1,
                                        double lat2, double lon2) {
  return geoDistanceMeters(lon1, lat1, lon2, lat2);
}
//////////////////////////////////////////////////////////////////////////

double CoordGetDistance(const std::vector<geopoint_t> &lst, int precision) {
  double distance = 0.0;
  double llon, llat;

  if (lst.empty() || lst.size() == 1)
    return 0.0;

  llon = lst[0].Longitude;
  llat = lst[0].Latitude;

  for (auto pp = lst.begin()+1; pp != lst.end(); ++pp) {

    if (GeohashComparePoints(llon, llat, pp->Longitude, pp->Latitude, precision) == 0)
      continue;

    distance += CoordDistanceBetweenPointsMeters(llat, llon,
                                                 pp->Latitude, pp->Longitude);
    llat = pp->Latitude;
    llon = pp->Longitude;
  }
  return distance;
}
//////////////////////////////////////////////////////////////////////////

double CoordGetDistanceWithGeohash(const std::vector<geopoint_t> &lst,
                                   int precision,
                                   int minPoints) {
  return 0.0;
}
//////////////////////////////////////////////////////////////////////////
