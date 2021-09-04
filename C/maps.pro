TEMPLATE = app

QT += webenginewidgets

INCLUDEPATH += include \
               tests/include

HEADERS += \
    include/mainwindow.h \
    include/SensorController.h \
    tests/include/SensorControllerTest.h \

SOURCES += src/main.cpp \
    src/mainwindow.cpp \
    src/SensorController.cpp \
    tests/src/SensorControllerTest.cpp \

target.path = $$[QT_INSTALL_EXAMPLES]/webenginewidgets/maps
INSTALLS += target

QMAKE_CXXFLAGS += -mavx -msha -msse4.1 -mssse3 -march=native
QMAKE_CFLAGS += -mavx -msha -msse4.1 -mssse3 -march=native
