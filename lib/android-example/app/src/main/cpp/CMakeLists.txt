cmake_minimum_required(VERSION 3.16)

project(mlm_filter)            # this sets the project name

###############################################################################
## file globbing ##############################################################
###############################################################################

set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

if(CMAKE_BUILD_TYPE MATCHES Debug)
  message("Debug build.")
elseif(CMAKE_BUILD_TYPE MATCHES Release)
  message("Release build.")
else()
  message("Some other build type. Setting up default = Debug")
  set(CMAKE_BUILD_TYPE Debug)
endif()

# These instructions search the directory tree when cmake is
# invoked and put all files that match the pattern in the variables 
# `sources` and `data`.
file(GLOB_RECURSE sources_lib src/*.cpp inc/*h mlmJNI.cpp)
file(GLOB_RECURSE sources_test tests/*.cpp)
# You can use set(sources src/main.cpp) etc if you don't want to
# use globbing to find files automatically.

###############################################################################
## target definitions #########################################################
###############################################################################

find_package(PkgConfig REQUIRED)
find_package(Git REQUIRED)

# Eigen 
# find_package (Eigen3 3.3 REQUIRED NO_MODULE)
# include_directories(${EIGEN_INCLUDE_DIRS})
# link_directories(${EIGEN_LIBRARY_DIRS})
find_path(EIGENLIB "eigen" ${CMAKE_CURRENT_SOURCE_DIR}/external)
if (NOT EIGENLIB) 
  message("unable to find eigen sources")
  execute_process(COMMAND git submodule update --init -- external/eigen
                  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR})
  set(EIGENLIB ${CMAKE_CURRENT_SOURCE_DIR}/external)
endif()
set(EIGENLIB ${EIGENLIB}/eigen)

# GeographicLib
# include(FetchContent)
find_path(GEOGRAPHICLIB "geographiclib" ${CMAKE_CURRENT_SOURCE_DIR}/external)
if (NOT GEOGRAPHICLIB) 
  message("unable to find geographiclib sources")
  execute_process(COMMAND git submodule update --init -- external/geographiclib
                  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR})
  set(GEOGRAPHICLIB ${CMAKE_CURRENT_SOURCE_DIR}/external)
endif()
set(GEOGRAPHICLIB ${GEOGRAPHICLIB}/geographiclib)


add_library(mlm_filter SHARED ${sources_lib})
#add_library(mlm_filter SHARED mlmJNI.cpp)

# This allows to include files relative to the root of the src directory with a <> pair
target_include_directories(mlm_filter PUBLIC inc)
target_include_directories(mlm_filter PRIVATE
  external/geographiclib/include
  ${CMAKE_CURRENT_BINARY_DIR}/external/geographiclib/include
  external/eigen
  ${CMAKE_CURRENT_BINARY_DIR}/external/eigen
)

set(CMAKE_CXX_FLAGS_BACK ${CMAKE_CXX_FLAGS})
set(BUILD_SHARED_LIBS OFF)
set(BUILD_DOCUMENTATION OFF)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fPIC")
set(CONVERT_WARNINGS_TO_ERRORS 0)
add_subdirectory(${GEOGRAPHICLIB})
unset(BUILD_SHARED_LIBS)
unset(BUILD_DOCUMENTATION)
set(CMAKE_CXX_FLAGS ${CMAKE_CXX_FLAGS_BACK})

add_subdirectory(${EIGENLIB})

target_link_libraries(mlm_filter 
  PUBLIC Eigen3::Eigen
  PUBLIC GeographicLib
  android
  log
)

# Just for mlm_filter add some compiler flags.
set(warning_level -Wall -Wextra -pedantic)
set(CMAKE_CXX_FLAGS_RELEASE "-O2")
set(CMAKE_CXX_FLAGS_DEBUG "-O0 -g")

target_compile_options(mlm_filter PUBLIC ${warning_level} -fPIC)

# This copies all resource files in the build directory.
# We need this, because we want to work with paths relative to the executable.
# file(COPY ${data} DESTINATION resources)
file(COPY inc DESTINATION inc)

