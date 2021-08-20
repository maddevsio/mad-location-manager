TEMPLATE = app
CONFIG += c++2a
CONFIG -= app_bundle
CONFIG -= qt

DEFINES += _USE_MATH_DEFINES
INCLUDEPATH += inc/
LIBS += -lgtest -lgcov

#QMAKE_CXXFLAGS += --coverage
#QMAKE_LFLAGS += --coverage

SOURCES += \
    src/Commons.cpp \
    src/Coordinates.cpp \
    src/Geohash.cpp \
    src/GpsAccFusionFilter.cpp \
    src/MadgwickAHRS.cpp \
    src/Mlm.cpp \
    src/Quaternion.cpp \
    src/Vector3d.cpp \
    tests/CoordinatesTest.cpp \
    tests/GeohashTest.cpp \
    tests/MatrixTest.cpp \
    tests/main.cpp

HEADERS += \
    inc/Commons.hpp \
    inc/Coordinates.hpp \
    inc/Geohash.hpp \
    inc/GpsAccFusionFilter.hpp \
    inc/Kalman.hpp \
    inc/MadgwickAHRS.hpp \
    inc/Matrix.hpp \
    inc/Mlm.hpp \
    inc/Quaternion.hpp \
    inc/Vector3d.hpp
