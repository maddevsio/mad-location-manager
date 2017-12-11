/****************************************************************************
**
** Copyright (C) 2017 The Qt Company Ltd.
** Contact: https://www.qt.io/licensing/
**
** This file is part of the examples of the Qt Toolkit.
**
** $QT_BEGIN_LICENSE:BSD$
** Commercial License Usage
** Licensees holding valid commercial Qt licenses may use this file in
** accordance with the commercial license agreement provided with the
** Software or, alternatively, in accordance with the terms contained in
** a written agreement between you and The Qt Company. For licensing terms
** and conditions see https://www.qt.io/terms-conditions. For further
** information use the contact form at https://www.qt.io/contact-us.
**
** BSD License Usage
** Alternatively, you may use this file under the terms of the BSD license
** as follows:
**
** "Redistribution and use in source and binary forms, with or without
** modification, are permitted provided that the following conditions are
** met:
**   * Redistributions of source code must retain the above copyright
**     notice, this list of conditions and the following disclaimer.
**   * Redistributions in binary form must reproduce the above copyright
**     notice, this list of conditions and the following disclaimer in
**     the documentation and/or other materials provided with the
**     distribution.
**   * Neither the name of The Qt Company Ltd nor the names of its
**     contributors may be used to endorse or promote products derived
**     from this software without specific prior written permission.
**
**
** THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
** "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
** LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
** A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
** OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
** SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
** LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
** DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
** THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
** (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
** OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE."
**
** $QT_END_LICENSE$
**
****************************************************************************/

#include <assert.h>

#include "mainwindow.h"
#include <QApplication>

#include "GeohashTest.h"
#include "MatrixTest.h"
#include "SensorControllerTest.h"
#include "CoordinatesTest.h"
#include "SensorController.h"
#include "MadgwickAHRS.h"

static void launchTests() {
  TestGeohash();
  TestMatrices();
  TestSensorController();
  TestCoordinates();

  assert(FilterInputFile("/home/lezh1k/gps_test_data/pos_final.json",
                  "/home/lezh1k/gps_test_data/test.json"));
}
//////////////////////////////////////////////////////////////////////////
#include "Matrix.h"
int main(int argc, char *argv[])
{
  for (int i = 0; i < 4; ++i)
    MadgwickAHRSupdate(0.0f, 0.0f, 0.0f, //g
                     0.0f, 9.809999f, 0.0f, //a
                     30.2493f, 10.3607f, 36.7975f); //m

  float w, x, y, z;
  w = q0; x = q1; y = q2; z = q3;

  matrix_t *rotMatrix = MatrixAlloc(3, 3);
  matrix_t *currAcc = MatrixAlloc(3, 1);
  matrix_t *rotMatrixI = MatrixAlloc(3, 3);

  rotMatrix->data[0][0] = 1.0f - 2.0f*y*y - 2.0f*z*z;
  rotMatrix->data[0][1] = 2.0f*x*y - 2.0f*z*w;
  rotMatrix->data[0][2] = 2.0f*x*z + 2.0f*y*w;
  rotMatrix->data[1][0] = 2.0f*x*y + 2.0f*z*w;
  rotMatrix->data[1][1] = 1.0f - 2.0f*x*x - 2.0f*z*z;
  rotMatrix->data[1][2] = 2.0f*y*z - 2.0f*x*w;
  rotMatrix->data[2][0] = 2.0f*x*z - 2.0f*y*w;
  rotMatrix->data[2][1] = 2.0f*y*z + 2.0f*x*w;
  rotMatrix->data[2][2] = 1.0f - 2.0f*x*x - 2.0f*y*y;

  MatrixTranspose(rotMatrix, rotMatrixI);
  matrix_t *neuAcc = MatrixAlloc(3, 1);
  MatrixSet(currAcc, 0.0, 0.0, 9.809999);
  MatrixPrint(rotMatrix);
  MatrixPrint(currAcc);
  MatrixMultiply(rotMatrix, currAcc, neuAcc);
  MatrixPrint(neuAcc);

  launchTests();
  QCoreApplication::setAttribute(Qt::AA_EnableHighDpiScaling);
  QApplication app(argc, argv);
  QString pathToCoordFile = argc > 1 ? argv[1] : "/home/lezh1k/gps_test_data/log1";
  MainWindow mainWindow(pathToCoordFile);
  mainWindow.show();
  return app.exec();
}
//////////////////////////////////////////////////////////////////////////
