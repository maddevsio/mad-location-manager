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

GTK_LIBS = $(shell pkg-config --libs gtk4)
GKT_FLAGS = $(shell pkg-config --cflags gtk4)

SHUMATE_LIBS = $(shell pkg-config --libs shumate-1.0)
SHUMATE_FLAGS = $(shell pkg-config --cflags shumate-1.0)

PRG = mlm_data_generator
PRG_UT = mlm_data_generator_unit_tests

LIBS := -lm -lpthread $(SHUMATE_LIBS) $(GTK_LIBS)
LIBS_UT := $(LIBS) $(GTEST_LIBS)

DEFS := -D_USE_MATH_DEFINES

WARN_LEVEL = -Wall -Wextra -pedantic

INCLUDES := -Iinc
SRC_CPP := $(wildcard *.cpp) $(wildcard src/*.cpp)
SRC_UT := $(SRC_CPP) $(wildcard tests/*.cpp)

OBJECTS := $(SRC_CPP:%.cpp=$(OBJ_DIR)/%.o)
OBJECTS_UT := $(SRC_UT:%.cpp=$(OBJ_DIR)/%.o)

CXXFLAGS := -std=gnu++11 -pipe $(WARN_LEVEL) $(SHUMATE_FLAGS) $(GTKMM_FLAGS)
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

.PHONY: valgrind_memcheck
valgrind_memcheck: debug
	@valgrind --tool=memcheck --num-callers=50 --leak-check=full --suppressions=/usr/share/gtk-4.0/valgrind/gtk.supp \
	--suppressions=/usr/share/glib-2.0/valgrind/glib.supp --leak-resolution=high \
	--show-leak-kinds=definite --log-file=vgdump $(BIN_DIR)/$(PRG).elf
	# @valgrind --tool=memcheck --num-callers=50 --leak-check=full --suppressions=/usr/share/gtk-4.0/valgrind/gtk.supp \
	# --suppressions=/usr/share/glib-2.0/valgrind/glib.supp --leak-resolution=high \
	# --show-leak-kinds=definite,possible --log-file=vgdump $(BIN_DIR)/$(PRG).elf
