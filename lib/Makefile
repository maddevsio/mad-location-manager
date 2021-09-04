CC=gcc
CXX=g++
LINK=$(CXX)

SRC_DIR = src
TST_DIR = tests

BUILD_DIR=build
OBJ_DIR=$(BUILD_DIR)/obj
BIN_DIR=$(BUILD_DIR)/bin

LIBS := 
DEFS := -D_USE_MATH_DEFINES 

TARGET = mlm
INCLUDES := -Iinc

WARN_LEVEL = -Wall -Wextra -Wnon-virtual-dtor -pedantic
CXXFLAGS := -std=c++20 $(INCLUDES) $(DEFS) $(WARN_LEVEL) -fPIC 

LIBS :=
TARGET_LIB := lib$(PRG).so.1.0.0
LDFLAGS := $(LIBS) -shared 

debug: CXXFLAGS += -O0 -g3
debug: all
release: CXXFLAGS += -O2
release: all

SRC := $(wildcard $(SRC_DIR)/*.cpp)
debug: SRC += $(wildcard $(TST_DIR)/*.cpp)

OBJECTS := $(SRC:%.cpp=$(OBJ_DIR)/%.o)

all: directories $(TARGET_LIB)

$(TARGET_LIB): $(BIN_DIR)/$(TARGET_LIB)

$(BIN_DIR)/$(TARGET_LIB): $(OBJECTS)
	@mkdir -p $(@D)
	$(LINK) -o $(BIN_DIR)/$(TARGET_LIB) $^ $(LDFLAGS) $(LIBS)

$(OBJ_DIR)/%.o: %.cpp
	@mkdir -p $(@D)
	$(CXX) $(CXXFLAGS) -o $@ -c $<

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
