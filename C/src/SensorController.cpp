#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <QFile>
#include "SensorController.h"
#include "GPSAccKalman.h"
#include "GPSAccKalman2.h"
#include "Coordinates.h"

#include <QDebug>

static const char* NMEA = "NMEA";
static const char* NMEA_POS = "NMEA POS: ";
static const char* NMEA_SPEED = "NMEA SPEED: ";
static const char* NMEA_COURSE = "NMEA COURSE: ";
static const char* GPS  = "GPS ";

bool
SensorControllerParseDataString(const char *str, SensorData_t *sd) {
  int tt;
  sd->gpsAlt = sd->gpsLat = sd->gpsLon = 0.0;
  if (str[0] == ' ') {
    tt = sscanf(str+1, "%lf abs acc: %lf %lf %lf",
                &sd->timestamp,
                &sd->absEastAcc,
                &sd->absNorthAcc,
                &sd->absUpAcc);
    return tt == 4;
  } else {
    if (strstr(str, GPS)) {
      /*String.format("%d GPS : pos lat=%f, lon=%f, alt=%f, hdop=%f, speed=%f, bearing=%f"*/
      tt = sscanf(str, "%lf GPS : pos lat=%lf, lon=%lf, alt=%lf, hdop=%lf, speed=%lf, bearing=%lf",
                  &sd->timestamp,
                  &sd->gpsLat,
                  &sd->gpsLon,
                  &sd->gpsAlt,
                  &sd->posErr,
                  &sd->speed,
                  &sd->course);
      return tt == 7;
    } //GPS

    if (strstr(str, NMEA)) {
      const char *sub;
      if ((tt=sscanf(str, "%lf NMEA", &sd->timestamp)) != 1)
        return false;

      if ((sub = strstr(str, NMEA_POS))) {
        if ((tt=sscanf(sub, "NMEA POS: lat=%lf, lon=%lf, alt=%lf",
                       &sd->gpsLat, &sd->gpsLon, &sd->gpsAlt)) != 3)
          return false;
      }

      if ((sub = strstr(str, NMEA_SPEED))) {
        if ((tt=sscanf(sub, "NMEA SPEED: %lf", &sd->speed)) != 1)
          return false;
      }

      if ((sub = strstr(str, NMEA_COURSE))) {
        if ((tt=sscanf(sub, "NMEA COURSE: %lf", &sd->course)) != 1)
          return false;
      }
      return false; //we don't want to use nmea right now.
//      return true;
    } //NMEA

  }
  return false;
}
//////////////////////////////////////////////////////////////////////////

bool sensorDataToFile(QFile &fOut,
                      const SensorData_t *sd) {
  /*1514274617887 GPS : pos lat=42.879098, lon=74.617890, alt=702.000000, speed=0.000000, hdop=22.000000*/
  QString data = QString("%1 GPS : pos lat=%2, lon=%3, alt=%4, hdop=%5, speed=%6, bearing=%7\n").
                 arg(sd->timestamp).arg(sd->gpsLat).arg(sd->gpsLon).
                 arg(sd->gpsAlt).arg(sd->posErr).arg(sd->speed).arg(sd->course);
  fOut.write(data.toUtf8());
  return true;
}
//////////////////////////////////////////////////////////////////////////

static void patchSdWithNoise(SensorData *sd) {
  double noiseX = RandomBetween2Vals(800, 1300) / 1000000.0;
  double noiseY = RandomBetween2Vals(800, 1300) / 1000000.0;

  noiseX *= rand() & 0x01 ? -1.0 : 1.0;
  noiseY *= rand() & 0x01 ? -1.0 : 1.0;

  sd->posErr += CoordDistanceBetweenPointsMeters(sd->gpsLat, sd->gpsLon,
                                                sd->gpsLat+noiseX, sd->gpsLon+noiseY) / 0.68;
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
      if (!SensorControllerParseDataString(line.toStdString().c_str(), &sd))
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
    GPSAccKalmanFilter2_t *kf2 = GPSAccKalman2Alloc(
                                   CoordLongitudeToMeters(sd.gpsLon),
                                   CoordLatitudeToMeters(sd.gpsLat),
                                   xVel,
                                   yVel,
                                   accDev,
                                   sd.posErr,
                                   sd.timestamp);

    while (!fIn.atEnd()) {
      QString line = fIn.readLine();
      if (!SensorControllerParseDataString(line.toStdString().c_str(), &sd))
        continue;      

      if (sd.gpsLat == 0.0 && sd.gpsLon == 0.0) {
        GPSAccKalman2Predict(kf2,
                             sd.timestamp,
                             sd.absEastAcc,
                             sd.absNorthAcc);
      } else {
        if (--gps_count)
          continue;        
        gps_count = GPS_COUNT;

//        patchSdWithNoise(&sd);
        xVel = sd.speed * cos(sd.course);
        yVel = sd.speed * sin(sd.course);

        GPSAccKalman2Update(kf2,
                            sd.timestamp,
                            CoordLongitudeToMeters(sd.gpsLon),
                            CoordLatitudeToMeters(sd.gpsLat),
                            xVel,
                            yVel,
                            sd.posErr,
                            sd.posErr*0.1);

        geopoint_t pp = CoordMetersToGeopoint(
                          kf2->kf->Xk_k->data[0][0], kf2->kf->Xk_k->data[1][0]);

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
