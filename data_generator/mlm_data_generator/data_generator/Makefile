CC = g++
#CC = clang++
AS = as
LD = ld
LINK = $(CC)

BUILD_DIR = build
OBJ_DIR = $(BUILD_DIR)/obj
BIN_DIR = bin
TST_DIR = tests

GTEST_LIBS = $(shell pkg-config --libs gtest)
GTEST_FLAGS = $(shell pkg-config --cflags gtest)

PRG = mlm_data_generator
PRG_UT = mlm_data_generator_unit_tests

LIBS := -lm -lpthread
LIBS_UT := -lm -lpthread $(GTEST_LIBS)

DEFS := -D_USE_MATH_DEFINES

WARN_LEVEL = -Wall -Wextra -pedantic

INCLUDES := -Iinc
SRC_CPP := $(wildcard *.cpp) $(wildcard src/*.cpp)
SRC_UT := $(SRC_CPP) $(wildcard tests/*.cpp)

OBJECTS := $(SRC_CPP:%.cpp=$(OBJ_DIR)/%.o)
OBJECTS_UT := $(SRC_UT:%.cpp=$(OBJ_DIR)/%.o)

CXXFLAGS := -std=gnu++2a -pipe $(WARN_LEVEL)
LDFLAGS := $(LIBS)

debug: CXXFLAGS += -O0 -ggdb
debug: all

release: CXXFLAGS += -O2
release: all

unit_test: CXXFLAGS += -O0 -ggdb $(GTEST_FLAGS)
unit_test: LDFLAGS += $(GTEST_LIBS)
unit_test: DEFS += -D_UNIT_TESTS_
unit_test: all_ut

all: directories clean_main_o $(PRG)
all_ut: directories clean_main_o $(PRG_UT)

.PHONY: clean_main_o
clean_main_o:
	@rm -rf $(OBJ_DIR)/src/main.o

.PHONY: run
run: all
	$(BIN_DIR)/$(PRG).elf

.PHONY: run_ut
run_ut: unit_test
	$(BIN_DIR)/$(PRG_UT).elf

$(PRG): $(BIN_DIR)/$(PRG)
$(PRG_UT): $(BIN_DIR)/$(PRG_UT)

$(OBJ_DIR)/%.o: %.cpp
	@mkdir -p $(@D)
	$(CC) $(CXXFLAGS) $(INCLUDES) $(DEFS) -o $@ -c $<

$(BIN_DIR)/$(PRG): $(OBJECTS)
	@mkdir -p $(@D)
	$(LINK) -o $@.elf $^ $(LDFLAGS)

$(BIN_DIR)/$(PRG_UT): $(OBJECTS_UT)
	@mkdir -p $(@D)
	$(LINK) -o $@.elf $^ $(LDFLAGS)

.PHONY: directories
directories:
	@mkdir -p $(BUILD_DIR)
	@mkdir -p $(BIN_DIR)

.PHONY: clean
clean:
	@rm -rf $(BUILD_DIR)/*
	@rm -rf $(BIN_DIR)/*

.PHONY: mrproper
mrproper:
	@rm -rf $(BUILD_DIR)
	@rm -rf $(BIN_DIR)
