#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <QFile>

#include "SensorController.h"
#include "GPSAccKalman.h"
#include "Coordinates.h"
#include <QDebug>

static void patchSdWithNoise(SensorData *sd) __attribute_used__ ;

static bool parseAbsAccData(const char *str, SensorData_t *sd);
static bool parseGpsData(const char *str, SensorData_t *sd);
static bool parseKalmanAllocData(const char *str, SensorData_t *sd);
static bool parseKalmanPredict(const char *str, SensorData_t *sd);
static bool parseKalmanUpdate(const char *str, SensorData_t *sd);
static bool parseFilteredGpsData(const char *str, SensorData_t *sd);
static bool parseFinalDistance(const char *str, SensorData_t *sd);
typedef bool (*pf_parser)(const char*, SensorData_t*);

//don't change order here. it's related to LogMessageType
static pf_parser parsers[] = {
  parseKalmanAllocData,
  parseKalmanPredict,
  parseKalmanUpdate,
  parseGpsData,
  parseAbsAccData,
  parseFilteredGpsData,
  parseFinalDistance,
};

bool parseAbsAccData(const char *str, SensorData_t *sd) {
  int tt;
  tt = sscanf(str, "%lf abs acc: %lf %lf %lf",
              &sd->timestamp,
              &sd->absEastAcc,
              &sd->absNorthAcc,
              &sd->absUpAcc);
  return tt == 4;
}

bool parseGpsData(const char *str, SensorData_t *sd) {
  int tt;
  tt = sscanf(str, "%lf GPS : pos lat=%lf, lon=%lf, alt=%lf, hdop=%lf, speed=%lf, bearing=%lf",
              &sd->timestamp,
              &sd->gpsLat,
              &sd->gpsLon,
              &sd->gpsAlt,
              &sd->posErr,
              &sd->speed,
              &sd->course);
  return tt == 7;
}

bool parseKalmanAllocData(const char *str, SensorData_t *sd) {
  int tt;
  tt = sscanf(str, "%lf KalmanAlloc : lon=%lf, lat=%lf, speed=%lf, course=%lf, accDev=%lf, posDev=%lf",
              &sd->timestamp,
              &sd->gpsLon,
              &sd->gpsLat,
              &sd->speed,
              &sd->course,
              &sd->accDev,
              &sd->posErr);
  return tt == 7;
}

bool parseKalmanPredict(const char *str, SensorData_t *sd) {
  int tt;
  tt = sscanf(str, "%lf KalmanPredict : accX=%lf, accY=%lf",
              &sd->timestamp,
              &sd->absEastAcc,
              &sd->absNorthAcc);
 return tt == 3;
}

bool parseKalmanUpdate(const char *str, SensorData_t *sd) {
  int tt;
  tt = sscanf(str, "%lf KalmanUpdate : pos lon=%lf, lat=%lf, "
                   "xVel=%lf, yVel=%lf, posErr=%lf, velErr=%lf",
              &sd->timestamp,
              &sd->gpsLon,
              &sd->gpsLat,
              &sd->xVel,
              &sd->yVel,
              &sd->posErr,
              &sd->velErr);
  return tt == 7;
}

bool parseFilteredGpsData(const char *str, SensorData_t *sd) {
  int tt;
  tt = sscanf(str, "%lf FKS : lat=%lf, lon=%lf, alt=%lf",
              &sd->timestamp,
              &sd->gpsLat,
              &sd->gpsLon,
              &sd->gpsAlt);
  return tt == 4;
}

bool parseFinalDistance(const char *str, SensorData_t *sd) {
  int tt;
  tt = sscanf(str, "%lf Distance as is : %lf\n"
                   "Distance as is HP : %lf\n"
                   "Distance(geo) : %lf\n"
                   "Distance(geo) HP : %lf",
              &sd->timestamp,
              &sd->distanceAsIs,
              &sd->distanceAsIsHP,
              &sd->distanceGeo,
              &sd->distanceGeoHP);
  return tt == 5;
}
//////////////////////////////////////////////////////////////////////////

LogMessageType
SensorControllerParseDataString(const char *str, SensorData_t *sd) {
  int pi = -1;
  sd->gpsAlt = sd->gpsLat = sd->gpsLon = 0.0;
  pi = str[0] - '0';
  if (pi < 0 || pi > LMT_UNKNOWN)
    return LMT_UNKNOWN;
  return parsers[pi](str+1, sd) ? (LogMessageType)pi : LMT_UNKNOWN;
}
//////////////////////////////////////////////////////////////////////////

bool sensorDataToFile(QFile &fOut,
                      const SensorData_t *sd) {
  /*%d%lf FKS : lat=%lf, lon=%lf, alt=%lf*/
  QString data = QString("%1%2 FKS : lat=%3, lon=%4, alt=%5\n").
                 arg(LMT_FILTERED_GPS_DATA).
                 arg(sd->timestamp).arg(sd->gpsLat).arg(sd->gpsLon).arg(sd->gpsAlt);
  fOut.write(data.toUtf8());
  return true;
}
//////////////////////////////////////////////////////////////////////////

void patchSdWithNoise(SensorData *sd) {
  double noiseX = RandomBetween2Vals(800, 1500) / 1000000.0;
  double noiseY = RandomBetween2Vals(800, 1500) / 1000000.0;
  double dd = 0.0;

  noiseX *= rand() & 0x01 ? -1.0 : 1.0;
  noiseY *= rand() & 0x01 ? -1.0 : 1.0;

  dd = CoordDistanceBetweenPointsMeters(sd->gpsLat, sd->gpsLon,
                                        sd->gpsLat+noiseX, sd->gpsLon+noiseY) / 0.68;
//  qDebug() << dd;
  sd->posErr += dd;
  sd->gpsLat += noiseX;
  sd->gpsLon += noiseY;
}

bool
FilterInputFile(const QString &inputFile,
                const QString &outputFile) {
  QFile fIn(inputFile);
  QFile fOut(outputFile);
  SensorData_t sd;
  bool initialData = false;
  bool result = false;

  do {
    if (!fIn.open(QFile::ReadOnly)) {
      qDebug() << "Couldn't open input file";
      return false;
    }

    if (!fOut.open(QFile::WriteOnly)) {
      qDebug() << "Couldn't open out file";
      return false;
    }

    while (!fIn.atEnd()) {
      QString line = fIn.readLine();
      int pi = SensorControllerParseDataString(line.toStdString().c_str(), &sd);
      if (pi != LMT_ABS_ACC_DATA && pi != LMT_GPS_DATA)
        continue;

      if (sd.gpsLat == 0.0 && sd.gpsLon == 0.0)
        continue;

      initialData = true;
      break;
    }

    if (!initialData)
      break;

    static const int GPS_COUNT = 1;
    int gps_count = GPS_COUNT;
    static const double accDev = 1.0;

    double xVel = sd.speed * cos(sd.course);
    double yVel = sd.speed * sin(sd.course);
//    patchSdWithNoise(&sd);
    GPSAccKalmanFilter_t *kf2 = GPSAccKalmanAlloc(
                                  CoordLongitudeToMeters(sd.gpsLon),
                                  CoordLatitudeToMeters(sd.gpsLat),
                                  xVel,
                                  yVel,
                                  accDev,
                                  sd.posErr,
                                  sd.timestamp);
    sensorDataToFile(fOut, &sd);

    double tnow = sd.timestamp;
    double x, y, vx, vy;
    x = y = 0.0;
    vx = sd.speed * cos(sd.course);
    vy = sd.speed * sin(sd.course);
    vx = vy = 0.0;
    double dtmax = 0.0;
    double dxmax = 0.0;
    double dymax = 0.0;
    while (!fIn.atEnd()) {
      QString line = fIn.readLine();
      int pi = SensorControllerParseDataString(line.toStdString().c_str(), &sd);
      if (pi != LMT_ABS_ACC_DATA && pi != LMT_GPS_DATA)
        continue;

      if (sd.gpsLat == 0.0 && sd.gpsLon == 0.0) {
        double dt = (sd.timestamp - tnow) / 1000.0;
        double dt2 = 0.5*dt*dt;
        double dx = vx*dt + dt2*sd.absEastAcc;
        double dy = vy*dt + dt2*sd.absNorthAcc;
        tnow = sd.timestamp;
        x += dx;
        y += dy;
        vx += sd.absEastAcc*dt;
        vy += sd.absNorthAcc*dt;
        dtmax = std::max(dtmax, dt);
        dxmax = std::max(abs(dxmax), abs(dx));
        dymax = std::max(abs(dymax), abs(dy));
        GPSAccKalmanPredict(kf2, sd.timestamp,
                            sd.absEastAcc, sd.absNorthAcc);
      } else {
        if (--gps_count)
          continue;
        gps_count = GPS_COUNT;

//        patchSdWithNoise(&sd);
        xVel = sd.speed * cos(sd.course);
        yVel = sd.speed * sin(sd.course);
        GPSAccKalmanUpdate(kf2,
                           sd.timestamp,
                           CoordLongitudeToMeters(sd.gpsLon),
                           CoordLatitudeToMeters(sd.gpsLat),
                           xVel,
                           yVel,
                           sd.posErr);
        geopoint_t pp = CoordMetersToGeopoint(GPSAccKalmanGetX(kf2),
                                              GPSAccKalmanGetY(kf2));
        sd.gpsLat = pp.Latitude;
        sd.gpsLon = pp.Longitude;
        sensorDataToFile(fOut, &sd);
      }
    } //while (!fIn.atEnd())
    qDebug() << "x : " << x;
    qDebug() << "y : " << y;
    qDebug() << "vx : " << vx;
    qDebug() << "vy : " << vy;

    qDebug() << "dtmax : " << dtmax;
    qDebug() << "dxmax : " << dxmax;
    qDebug() << "dymax : " << dymax;
    result = true;
  } while (0);

  if (fIn.isOpen())
    fIn.close();
  if (fOut.isOpen())
    fOut.close();
  return result;
}
//////////////////////////////////////////////////////////////////////////

bool JavaFilter(const QString &inputFile,
                const QString &outputFile) {
  QFile fIn(inputFile);
  QFile fOut(outputFile);
  SensorData_t sd;
  bool initialData = false;
  bool result = false;

  do {
    if (!fIn.open(QFile::ReadOnly)) {
      qDebug() << "Couldn't open input file";
      return false;
    }

    if (!fOut.open(QFile::WriteOnly)) {
      qDebug() << "Couldn't open out file";
      return false;
    }

    while (!fIn.atEnd()) {
      QString line = fIn.readLine();
      int pi = SensorControllerParseDataString(line.toStdString().c_str(), &sd);
      if (pi != LMT_KALMAN_ALLOC)
        continue;
      initialData = true;
      break;
    }

    if (!initialData)
      break;

    GPSAccKalmanFilter_t *kf2 = GPSAccKalmanAlloc(
                                  CoordLongitudeToMeters(sd.gpsLon),
                                  CoordLatitudeToMeters(sd.gpsLat),
                                  sd.xVel,
                                  sd.yVel,
                                  sd.accDev,
                                  sd.posErr,
                                  sd.timestamp);

    while (!fIn.atEnd()) {
      QString line = fIn.readLine();
      int pi = SensorControllerParseDataString(line.toStdString().c_str(), &sd);

      if (pi == LMT_KALMAN_PREDICT) {
        GPSAccKalmanPredict(kf2, sd.timestamp,
                            sd.absEastAcc, sd.absNorthAcc);
      } else if (pi == LMT_KALMAN_UPDATE) {
        GPSAccKalmanUpdate(kf2,
                           sd.timestamp,
                           CoordLongitudeToMeters(sd.gpsLon),
                           CoordLatitudeToMeters(sd.gpsLat),
                           sd.xVel,
                           sd.yVel,
                           sd.posErr);
        geopoint_t pp = CoordMetersToGeopoint(GPSAccKalmanGetX(kf2),
                                              GPSAccKalmanGetY(kf2));
        sd.gpsLat = pp.Latitude;
        sd.gpsLon = pp.Longitude;
        sensorDataToFile(fOut, &sd);
      }
    } //while (!fIn.atEnd())
    result = true;
  } while (0);

  if (fIn.isOpen())
    fIn.close();
  if (fOut.isOpen())
    fOut.close();
  return result;
}
