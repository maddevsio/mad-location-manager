TEMPLATE = app

QT += webenginewidgets

INCLUDEPATH += include \
               tests/include

HEADERS += \
    include/mainwindow.h \
    include/Coordinates.h \
    include/Commons.h \
    include/Matrix.h \
    include/Geohash.h \
    tests/include/GeohashTest.h \
    tests/include/MatrixTest.h \
    include/SensorController.h \
    tests/include/SensorControllerTest.h \
    tests/include/CoordinatesTest.h \
    include/Kalman.h \
    include/MadgwickAHRS.h \
    include/GPSAccKalman.h \
    include/MeanFilter.h

SOURCES += src/main.cpp \
    src/mainwindow.cpp \
    src/Coordinates.cpp \
    src/Matrix.cpp \
    src/Geohash.cpp \
    tests/src/GeohashTest.cpp \
    tests/src/MatrixTest.cpp \
    src/SensorController.cpp \
    tests/src/SensorControllerTest.cpp \
    tests/src/CoordinatesTest.cpp \
    src/Kalman.cpp \
    src/MadgwickAHRS.cpp \
    src/GPSAccKalman.cpp \
    src/MeanFilter.cpp

target.path = $$[QT_INSTALL_EXAMPLES]/webenginewidgets/maps
INSTALLS += target
