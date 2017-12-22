#ifndef SENSORCONTROLLER_H
#define SENSORCONTROLLER_H

#include <stdint.h>
#include <stdbool.h>

typedef struct SensorData {
  double timestamp;
  double gpsLat;
  double gpsLon;
  double gpsAlt;
  double pitch;
  double yaw;
  double roll;
  double absNorthAcc;
  double absEastAcc;
  double absUpAcc;
  double velNorth;
  double velEast;
  double velDown;
  double velError;
  double altitudeError;
} SensorData_t;

bool SensorControllerParseDataString(const char *str, SensorData_t *sd);
bool FilterInputFile(const char *inputFile, const char *outputFile, const char *outputFile2);

#endif // SENSORCONTROLLER_H
