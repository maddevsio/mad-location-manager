release:TEMPLATE = lib
debug:TEMPLATE = app

CONFIG += c++2a
CONFIG -= app_bundle
CONFIG -= qt

DEFINES += _USE_MATH_DEFINES
INCLUDEPATH += inc/

debug: LIBS += -lgtest

SOURCES += \
    src/Commons.cpp \
    src/Coordinates.cpp \
    src/DataGenerator.cpp \
    src/Geohash.cpp \
    src/GpsAccFusionFilter.cpp \
    src/MadgwickAHRS.cpp \
    src/Quaternion.cpp \
    src/Vector3d.cpp \

debug:SOURCES += \
    tests/main.cpp \
    tests/MatrixTest.cpp \
    tests/GeohashTest.cpp \
    tests/CoordinatesTest.cpp

HEADERS += \
    inc/Commons.hpp \
    inc/Coordinates.hpp \
    inc/DataGenerator.hpp \
    inc/Geohash.hpp \
    inc/GpsAccFusionFilter.hpp \
    inc/Kalman.hpp \
    inc/MadgwickAHRS.hpp \
    inc/Matrix.hpp \
    inc/Quaternion.hpp \
    inc/Vector3d.hpp
