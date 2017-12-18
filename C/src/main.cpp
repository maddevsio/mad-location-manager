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

static quaternion_t quaternionMul(const quaternion_t *a,
                                  const quaternion_t *b) {
  quaternion_t r;
  r.w = a->w*b->w - a->x*b->x - a->y*b->y - a->z*b->z;
  r.x = a->w*b->x + a->x*b->w + a->y*b->z - a->z*b->y;
  r.y = a->w*b->y - a->x*b->z + a->y*b->w + a->z*b->x;
  r.z = a->w*b->z + a->x*b->y - a->y*b->x + a->z*b->w;
  return r;
}
//////////////////////////////////////////////////////////////////////////
#include <math.h>
int main(int argc, char *argv[]) {

  quaternion_t a = {1.0f, 1.0f, 1.0f, 1.0f};
  quaternion_t b = {0.0f, -0.001f, -0.002f, -0.002f};
  quaternion_t c = quaternionMul(&a, &b);

  qDebug() << c.w << " " << c.x << " " << c.y << " " << c.z;
  qDebug() << sqrt(3.0 / 4.0) * -0.005;

  launchTests();
  QCoreApplication::setAttribute(Qt::AA_EnableHighDpiScaling);
  QApplication app(argc, argv);
  QString pathToCoordFile = argc > 1 ? argv[1] : "/home/lezh1k/gps_test_data/log1";
  MainWindow mainWindow(pathToCoordFile);
  mainWindow.show();
  return app.exec();
}
//////////////////////////////////////////////////////////////////////////
