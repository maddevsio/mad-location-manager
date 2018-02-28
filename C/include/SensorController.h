#ifndef SENSORCONTROLLER_H
#define SENSORCONTROLLER_H

#include <stdint.h>
#include <stdbool.h>
#include <QString>

//see java code
enum LogMessageType {
  LMT_KALMAN_ALLOC = 0,
  LMT_KALMAN_PREDICT,
  LMT_KALMAN_UPDATE,
  LMT_GPS_DATA,
  LMT_ABS_ACC_DATA,
  LMT_FILTERED_GPS_DATA,
  LMT_OLD_FILTER,
  LMT_UNKNOWN
};
//////////////////////////////////////////////////////////////////////////

typedef struct SensorData {
  double timestamp;
  double gpsLat;
  double gpsLon;
  double gpsAlt;
  double absNorthAcc;
  double absEastAcc;
  double absUpAcc;
  double accDev;
  double speed;
  double course;
  double xVel;
  double yVel;
  double velErr;
  double posErr;

  double distanceAsIs;
  double distanceAsIsHP;
  double distanceGeo;
  double distanceGeoHP;
} SensorData_t;

LogMessageType SensorControllerParseDataString(const char *str, SensorData_t *sd);
bool FilterInputFile(const QString &inputFile, const QString &outputFile);
bool JavaFilter(const QString &inputFile, const QString &outputFile);

#endif // SENSORCONTROLLER_H
