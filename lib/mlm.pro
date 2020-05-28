TEMPLATE = app
CONFIG += c++11
CONFIG -= app_bundle
CONFIG -= qt

DEFINES += _USE_MATH_DEFINES
INCLUDEPATH += inc/
LIBS += -lgtest

SOURCES += \
    src/Commons.c \
    src/Coordinates.c \
    src/Geohash.c \
    src/GpsAccFusionFilter.c \
    src/Kalman.c \
    src/MadgwickAHRS.c \
    src/Matrix.c \
    src/Mlm.c \
    src/Quaternion.c \
    src/Vector3d.c \
    tests/CoordinatesTest.cpp \
    tests/GeohashTest.cpp \
    tests/MatrixTest.cpp \
    tests/main.cpp

HEADERS += \
    inc/Commons.h \
    inc/Coordinates.h \
    inc/Geohash.h \
    inc/GpsAccFusionFilter.h \
    inc/Kalman.h \
    inc/MadgwickAHRS.h \
    inc/Matrix.h \
    inc/Mlm.h \
    inc/Quaternion.h \
    inc/Vector3d.h
