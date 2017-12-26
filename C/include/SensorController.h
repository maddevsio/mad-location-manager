#ifndef SENSORCONTROLLER_H
#define SENSORCONTROLLER_H

#include <stdint.h>
#include <stdbool.h>
#include <QString>

typedef struct SensorData {
  double timestamp;
  double gpsLat;
  double gpsLon;
  double gpsAlt;
  double absNorthAcc;
  double absEastAcc;
  double absUpAcc;
  double speed;
  double course;
  double posErr;
} SensorData_t;

bool SensorControllerParseDataString(const char *str, SensorData_t *sd);
bool FilterInputFile(const QString &inputFile, const QString &outputFile);

#endif // SENSORCONTROLLER_H
