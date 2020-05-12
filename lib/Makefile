desktop: CXX=g++
desktop: LINK=$(CXX)

iOS_armv7: TARGET_ARCH=armv7 
iOS_armv7: TARGET_PLATFORM=iPhoneOS
iOS_armv7s: TARGET_ARCH=armv7s 
iOS_armv7s: TARGET_PLATFORM=iPhoneOS
iOS_arm64: TARGET_ARCH=arm64 
iOS_arm64: TARGET_PLATFORM=iPhoneOS
iOS_i386: TARGET_ARCH=i386 
iOS_i386: TARGET_PLATFORM=iPhoneSimulator
iOS_x86_64: TARGET_ARCH=x86_64 
iOS_x86_64: TARGET_PLATFORM=iPhoneSimulator

iOS: PLATFORMPATH=/Applications/Xcode.app/Contents/Developer/Platforms
iOS: TOOLSPATH=/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/bin
iOS: IPHONEOS_DEPLOYMENT_TARGET=8.0
iOS: SDKVERSION=8.0

iOS: CXX=$(shell xcrun -sdk iphoneos -find clang++)
iOS: LINK=$(CXX)
iOS: CXXFLAGS += -arch ${TARGET_ARCH} -isysroot $(PLATFORMPATH)/$(TARGET_PLATFORM).platform/Developer/SDKs/$(TARGET_PLATFORM).sdk -miphoneos-version-min=$(SDKVERSION)
iOS: LDFLAGS += -arch $(TARGET_ARCH) -isysroot $(PLATFORMPATH)/$(TARGET_PLATFORM).platform/Developer/SDKs/$(TARGET_PLATFORM).sdk

BUILD_DIR=build
OBJ_DIR=$(BUILD_DIR)/obj
BIN_DIR=bin

LIBS := 
DEFS := 
WARN_LEVEL = -Wall -pedantic

PRG = mlm

INCLUDES := -Iinc
CXXFLAGS := $(INCLUDES) $(DEFS) $(WARN_LEVEL) -fPIC -std=c++11
LIBS :=
TARGET_LIB := lib$(PRG).so.1.0.0
LDFLAGS := $(LIBS) -shared 

debug: CXXFLAGS += -O0 -g3
debug: all
release: CXXFLAGS += -O2
release: all

SRC := $(wildcard src/*.cpp) $(wildcard src/*/*.cpp)
OBJECTS := $(SRC:%.cpp=$(OBJ_DIR)/%.o)

all: directories $(TARGET_LIB)
desktop: directories $(TARGET_LIB)
iOS: directories $(TARGET_LIB)

iOS_armv7: iOS 
iOS_armv7s: iOS 
iOS_arm64: iOS 
iOS_i386: iOS 
iOS_x86_64: iOS

$(TARGET_LIB): $(BIN_DIR)/$(TARGET_LIB)
$(OBJ_DIR)/%.o: %.cpp
	@mkdir -p $(@D)
	$(CXX) $(CXXFLAGS) -o $@ -c $<

$(BIN_DIR)/$(TARGET_LIB): $(OBJECTS)
	@mkdir -p $(@D)
	$(LINK) -o $(BIN_DIR)/$(TARGET_LIB) $^ $(LDFLAGS) $(LIBS)

directories:
	@mkdir -p $(BUILD_DIR)
	@mkdir -p $(BIN_DIR)

clean:
	@rm -rf $(BUILD_DIR)/*
	@rm -rf $(BIN_DIR)/*

mrproper:
	@rm -rf $(BUILD_DIR)
	@rm -rf $(BIN_DIR)
