desktop: CXX=g++
desktop: AS=as
desktop: LD=ld
desktop: LINK=$(CXX)

iOS: CXX=clang++
iOS: AS=llvm-as
iOS: LD=llvm-ld
iOS: LINK=$(CXX)

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
TARGET_LIB = lib$(PRG).so.1.0.0
LDFLAGS = $(LIBS) -shared #-Wl,-soname,lib$(PRG).so.1

debug: CXXFLAGS += -O0 -g3
debug: all
release: CXXFLAGS += -O2
release: all

SRC := $(wildcard src/*.cpp) $(wildcard src/*/*.cpp)
OBJECTS := $(SRC:%.cpp=$(OBJ_DIR)/%.o)

all: directories $(TARGET_LIB)
desktop: directories $(TARGET_LIB)
iOS: directories $(TARGET_LIB)

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
