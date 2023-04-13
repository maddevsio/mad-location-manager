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

LIBS := -lm -lpthread
DEFS := -D_USE_MATH_DEFINES
WARN_LEVEL = -Wall -Wextra -pedantic

PRG = mlm_data_generator
INCLUDES := -Iinc

SRC_C := $(wildcard *.cpp) $(wildcard src/*.cpp)
CXXFLAGS := -std=gnu++2a -pipe $(WARN_LEVEL)
# warning. 
LDFLAGS :=

debug: CXXFLAGS += -O0 -ggdb
debug: all

release: CXXFLAGS += -O2
release: all

test: DEFS += -D_UNIT_TESTS_
test: CXXFLAGS += -O0 -ggdb $(GTEST_FLAGS)
test: LIBS += $(GTEST_LIBS)
test: TST_SRC = $(wildcard $(TST_DIR)/*.cpp)
test: TST_OBJ = $(TST_SRC:%.cpp=$(OBJ_DIR)/%.o)
test: OBJECTS += $(TST_OBJ)
test: all

OBJECTS := $(SRC_C:%.cpp=$(OBJ_DIR)/%.o)
LDFLAGS += $(LIBS)

all: directories $(PRG)

.PHONY: run
run: all
	$(BIN_DIR)/$(PRG).elf

$(PRG): $(BIN_DIR)/$(PRG)

$(OBJ_DIR)/%.o: %.cpp
	@mkdir -p $(@D)
	$(CC) $(CXXFLAGS) $(INCLUDES) $(DEFS) -o $@ -c $<

$(BIN_DIR)/$(PRG): $(OBJECTS)
	@mkdir -p $(@D)
	$(LINK) -o $(BIN_DIR)/$(PRG).elf $^ $(LDFLAGS)

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
