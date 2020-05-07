TEMPLATE = lib
CONFIG += c++11
CONFIG -= app_bundle
CONFIG -= qt

INCLUDEPATH += inc/

SOURCES += \
    src/Coordinates.cpp \
    src/GPSAccKalman.cpp \
    src/Geohash.cpp \
    src/Kalman.cpp \
    src/MadgwickAHRS.cpp \
    src/Matrix.cpp

HEADERS += \
    inc/Commons.h \
    inc/Coordinates.h \
    inc/GPSAccKalman.h \
    inc/Geohash.h \
    inc/Kalman.h \
    inc/MadgwickAHRS.h \
    inc/Matrix.h
