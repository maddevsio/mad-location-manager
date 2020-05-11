#TEMPLATE = lib
TEMPLATE = app
CONFIG += c++11
CONFIG -= app_bundle
CONFIG -= qt

INCLUDEPATH += inc/
LIBS += -lgtest

SOURCES += \
    src/Coordinates.cpp \
    src/GPSAccKalman.cpp \
    src/Geohash.cpp \
    src/Kalman.cpp \
    src/MadgwickAHRS.cpp \
    src/Matrix.cpp \
    src/Quaternion.cpp \
    tests/CoordinatesTest.cpp \
    tests/GeohashTest.cpp \
    tests/MatrixTest.cpp \
    tests/main.cpp

HEADERS += \
    inc/Commons.h \
    inc/Coordinates.h \
    inc/GPSAccKalman.h \
    inc/Geohash.h \
    inc/Kalman.h \
    inc/MadgwickAHRS.h \
    inc/Matrix.h \
    inc/Quaternion.h
