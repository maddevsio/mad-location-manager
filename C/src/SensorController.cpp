#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include "SensorController.h"
#include "GPSAccKalman.h"
#include "Coordinates.h"
#include <QDebug>

bool SensorControllerParseDataString(const char *str, SensorData_t *sd) {
  static const char* inFormat = "{\"timestamp\":%lf,"
                                "\"gps_lat\":%lf,"
                                "\"gps_lon\":%lf,"
                                "\"gps_alt\":%lf,"
                                "\"pitch\":%lf,"
                                "\"yaw\":%lf,"
                                "\"roll\":%lf,"
                                "\"abs_north_acc\":%lf,"
                                "\"abs_east_acc\":%lf,"
                                "\"abs_up_acc\":%lf,"
                                "\"vel_north\":%lf,"
                                "\"vel_east\":%lf,"
                                "\"vel_down\":%lf,"
                                "\"vel_error\":%lf,"
                                "\"altitude_error\":%lf}";
  int result = sscanf(str, inFormat,
                      &sd->timestamp,
                      &sd->gpsLat,
                      &sd->gpsLon,
                      &sd->gpsAlt,
                      &sd->pitch,
                      &sd->yaw,
                      &sd->roll,
                      &sd->absNorthAcc,
                      &sd->absEastAcc,
                      &sd->absUpAcc,
                      &sd->velNorth,
                      &sd->velEast,
                      &sd->velDown,
                      &sd->velError,
                      &sd->altitudeError);
  return result == 15;
}
//////////////////////////////////////////////////////////////////////////

bool sensorDataToFile(FILE *fout,
                      const SensorData_t *sd,
                      double predictedAlt,
                      double predictedLon,
                      double predictedLat,
                      double resultantMph) {

  static const char* outFormat = "{\n"
                                 "    \"timestamp\": %.16f,\n"
                                 "    \"gps_alt\": %.16f,\n"
                                 "    \"pitch\": %.16f,\n"
                                 "    \"yaw\": %.16f,\n"
                                 "    \"roll\": %.16f,\n"
                                 "    \"abs_north_acc\": %.16f,\n"
                                 "    \"abs_east_acc\": %.16f,\n"
                                 "    \"abs_up_acc\": %.16f,\n"
                                 "    \"vel_north\": %.16f,\n"
                                 "    \"vel_east\": %.16f,\n"
                                 "    \"vel_down\": %.16f,\n"
                                 "    \"vel_error\": %.16f,\n"
                                 "    \"altitude_error\": %.16f,\n"
                                 "    \"predicted_lat\": %.16f,\n"
                                 "    \"predicted_lon\": %.16f,\n"
                                 "    \"predicted_alt\": %.16f,\n"
                                 "    \"resultant_mph\": %.16f,\n"
                                 "    \"gps_lat\": %.16f,\n"
                                 "    \"gps_lon\": %.16f\n"
                                 "},\n";
  return fprintf(fout, outFormat,
                 sd->timestamp,
                 sd->gpsAlt,
                 sd->pitch,
                 sd->yaw,
                 sd->roll,
                 sd->absNorthAcc,
                 sd->absEastAcc,
                 sd->absUpAcc,
                 sd->velNorth,
                 sd->velEast,
                 sd->velDown,
                 sd->velError,
                 sd->altitudeError,
                 predictedLat,
                 predictedLon,
                 predictedAlt,
                 resultantMph,
                 sd->gpsLat,
                 sd->gpsLon) >= 0;
}
//////////////////////////////////////////////////////////////////////////

bool
FilterInputFile(const char *inputFile,
                const char *outputFile) {
  int i;
  char line[1024];
  SensorData_t sd;
  FILE *fin, *fout;
  double latLonStandardDeviation = 2.0; // +/- 1m, increased for safety
  double altitudeStandardDeviation = 3.518522417151836;
  // got this value by getting standard deviation from accelerometer, assuming that mean SHOULD be 0
  double accelerometerEastStandardDeviation = ACTUAL_GRAVITY * 0.033436506994600976;
  double accelerometerNorthStandardDeviation = ACTUAL_GRAVITY * 0.05355371135598354;
  double accelerometerUpStandardDeviation = ACTUAL_GRAVITY * 0.2088683796078286;

  GPSAccKalmanFilter_t *kfLat, *kfLon, *kfAlt;
  bool initialData = false;
  bool result = false;
  geopoint_t predictedPoint;
  double predictedVE, predictedVN; //velocity east, north
  double resultantV;
  matrix_t *currentStateLat, *currentStateLon, *currentStateAlt;
  int predictCount = 0;

  fin= fopen(inputFile, "r");
  if (fin == NULL)
    return result = false;

  do {
    fout = fopen(outputFile, "w");
    if (fout == NULL)
      break;

    while (!feof(fin)) {
      for (i = 0; i < 1024 && !feof(fin); ++i) {
        line[i] = getc(fin);
        if (line[i] == '\n') break;
      }

      if (!SensorControllerParseDataString(line, &sd)) {
        continue;
      }

      initialData = true;
      break;
    }

    if (!initialData)
      break;

    kfLon = GPSAccKalmanAlloc(LongitudeToMeters(sd.gpsLon),
                              sd.velEast,
                              latLonStandardDeviation,
                              accelerometerEastStandardDeviation,
                              sd.timestamp);

    kfLat = GPSAccKalmanAlloc(LatitudeToMeters(sd.gpsLat),
                              sd.velNorth,
                              latLonStandardDeviation,
                              accelerometerNorthStandardDeviation,
                              sd.timestamp);

    kfAlt = GPSAccKalmanAlloc(sd.gpsAlt,
                              -sd.velDown,
                              altitudeStandardDeviation,
                              accelerometerUpStandardDeviation,
                              sd.timestamp);

    while (!feof(fin)) {
      for (int i = 0; i < 1024 && !feof(fin); ++i) {
        line[i] = getc(fin);
        if (line[i] == '\n') break;
      }

      if (!SensorControllerParseDataString(line, &sd)) {
        continue;
      }

      GPSAccKalmanPredict(kfLon, sd.timestamp, sd.absEastAcc*ACTUAL_GRAVITY);
      GPSAccKalmanPredict(kfLat, sd.timestamp, sd.absNorthAcc*ACTUAL_GRAVITY);
      GPSAccKalmanPredict(kfAlt, sd.timestamp, sd.absUpAcc*ACTUAL_GRAVITY);

      currentStateAlt = kfAlt->kf->predictedState;
      currentStateLat = kfLat->kf->predictedState;
      currentStateLon = kfLon->kf->predictedState;


      if (sd.gpsLat != 0.0) {
        ++predictCount;
        GPSAccKalmanUpdate(kfLon,
                           LongitudeToMeters(sd.gpsLon),
                           sd.velEast,
                           NULL,
                           sd.velError);
        GPSAccKalmanUpdate(kfLat,
                           LatitudeToMeters(sd.gpsLat),
                           sd.velNorth,
                           NULL,
                           sd.velError);
        GPSAccKalmanUpdate(kfAlt,
                           sd.gpsAlt,
                           -sd.velDown,
                           &sd.altitudeError,
                           sd.velError);

        currentStateAlt = kfAlt->kf->currentState;
        currentStateLat = kfLat->kf->currentState;
        currentStateLon = kfLon->kf->currentState;
      }
      predictedPoint = MetersToGeopoint(
                          currentStateLon->data[0][0],
                          currentStateLat->data[0][0]);
      predictedVE = currentStateLon->data[1][0];
      predictedVN = currentStateLat->data[1][0];
      resultantV = sqrt(pow(predictedVE, 2.0) + pow(predictedVN, 2.0));

      sensorDataToFile( fout,
                        &sd,
                        currentStateAlt->data[0][0],
                        predictedPoint.Longitude,
                        predictedPoint.Latitude,
                        MilesPerHour2MeterPerSecond(resultantV));
    }

    result = true;
  } while (0);

  fclose(fin);
  fclose(fout);
  return result;
}
