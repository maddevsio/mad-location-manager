TEMPLATE = app

QT += webenginewidgets

INCLUDEPATH += include \
    ../lib/inc/

HEADERS += \
    include/mainwindow.h \
    include/SensorController.h \

SOURCES += src/main.cpp \
    src/mainwindow.cpp \
    src/SensorController.cpp \

target.path = $$[QT_INSTALL_EXAMPLES]/webenginewidgets/maps
INSTALLS += target

QMAKE_CXXFLAGS += -mavx -msha -msse4.1 -mssse3 -march=native
QMAKE_CFLAGS += -mavx -msha -msse4.1 -mssse3 -march=native
