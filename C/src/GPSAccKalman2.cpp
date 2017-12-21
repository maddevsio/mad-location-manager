#include <assert.h>

#include "GPSAccKalman2.h"
#include "Matrix.h"

static void rebuildStateTransitions(GPSAccKalmanFilter2_t *k, double deltaT);

GPSAccKalmanFilter2_t *GPSAccKalman2Alloc(
    double x, double y,
    double xvel, double yvel,
    double xacc, double yacc,
    double timeStamp) {
  GPSAccKalmanFilter2_t *f = (GPSAccKalmanFilter2_t*) malloc(sizeof(GPSAccKalmanFilter2_t));
  assert(f);
  f->kf = KalmanFilterCreate(6, 4, 1);
  assert(f->kf);

  f->timeStamp = timeStamp;
  MatrixSet(f->kf->currentState,
            x, y, xvel, yvel, xacc, yacc);

  MatrixSet(f->kf->measurementModel,
            1.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 1.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 1.0);

  MatrixSet(f->kf->controlMatrix,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0); //we don't have control matrix
  MatrixSet(f->kf->controlVector,
            0.0); //we don't have control vector




}
//////////////////////////////////////////////////////////////////////////

void rebuildStateTransitions(GPSAccKalmanFilter2_t *k, double deltaT) {
  MatrixSet(k->kf->stateTransitionMatrix,
            1.0,    0.0,    deltaT,   0.0,      0.5*deltaT*deltaT,  0.0,
            0.0,    1.0,    0.0,      deltaT,   0.0,                0.5*deltaT*deltaT,
            0.0,    0.0,    1.0,      0.0,      deltaT,             0.0,
            0.0,    0.0,    0.0,      1.0,      0.0,                deltaT,
            0.0,    0.0,    0.0,      0.0,      1.0,                0.0,
            0.0,    0.0,    0.0,      0.0,      0.0,                1.0);

}
//////////////////////////////////////////////////////////////////////////
