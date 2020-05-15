# desktop definitions
desktop: CC=gcc
desktop: LINK=$(CC)

iOS: PLATFORMPATH=/Applications/Xcode.app/Contents/Developer/Platforms
iOS: TOOLSPATH=/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/bin
iOS: IPHONEOS_DEPLOYMENT_TARGET=8.0
iOS: SDKVERSION=8.0

iOS: CC=$(shell xcrun -sdk iphoneos -find clang)
iOS: LINK=$(CC)
iOS: CFLAGS += -arch ${TARGET_ARCH} -isysroot $(PLATFORMPATH)/$(TARGET_PLATFORM).platform/Developer/SDKs/$(TARGET_PLATFORM).sdk -miphoneos-version-min=$(IPHONEOS_DEPLOYMENT_TARGET)
iOS: LDFLAGS += -arch $(TARGET_ARCH) -isysroot $(PLATFORMPATH)/$(TARGET_PLATFORM).platform/Developer/SDKs/$(TARGET_PLATFORM).sdk
# android definitions

###################################
# PLATFORM SPECIFIC DEFINITIONS END
###################################

BUILD_DIR=build
OBJ_DIR=$(BUILD_DIR)/obj

BIN_DIR=bin
ifneq '$(BIN_DIR_ENV)' ''
BIN_DIR = $(BIN_DIR_ENV)
endif

LIBS := 
DEFS := -D_USE_MATH_DEFINES 
WARN_LEVEL = -Wall -pedantic

PRG = mlm
INCLUDES := -Iinc
CFLAGS := $(INCLUDES) $(DEFS) $(WARN_LEVEL) -fPIC 
LIBS :=
TARGET_LIB := lib$(PRG).so.1.0.0
LDFLAGS := $(LIBS) -shared 

debug: CFLAGS += -O0 -g3
debug: all
release: CFLAGS += -O2
release: all

SRC := $(wildcard src/*.c) $(wildcard src/*/*.c)
OBJECTS := $(SRC:%.c=$(OBJ_DIR)/%.o)

all: directories $(TARGET_LIB)
desktop: directories $(TARGET_LIB)
iOS: directories $(TARGET_LIB)

printbindir:
	@echo $(BIN_DIR)
$(TARGET_LIB): $(BIN_DIR)/$(TARGET_LIB)

$(BIN_DIR)/$(TARGET_LIB): $(OBJECTS)
	@mkdir -p $(@D)
	$(LINK) -o $(BIN_DIR)/$(TARGET_LIB) $^ $(LDFLAGS) $(LIBS)

$(OBJ_DIR)/%.o: %.c
	@mkdir -p $(@D)
	$(CC) $(CFLAGS) -o $@ -c $<

.PHONY: directories
directories:
	@mkdir -p $(BUILD_DIR)
	@mkdir -p $(BIN_DIR)

.PHONY: clean
clean:
	@rm -rf $(BUILD_DIR)/*

.PHONY: mrproper
mrproper:
	@rm -rf $(BUILD_DIR)
	@rm -rf $(BIN_DIR)
