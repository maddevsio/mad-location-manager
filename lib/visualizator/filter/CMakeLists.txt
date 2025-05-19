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
file(GLOB_RECURSE sources_lib src/*.cpp inc/*h)
file(GLOB_RECURSE sources_test tests/*.cpp)

# find_program(CMAKE_CXX_CPPCHECK NAMES cppcheck)
# if (CMAKE_CXX_CPPCHECK)
#     list(
#         APPEND CMAKE_CXX_CPPCHECK 
#             "--enable=warning"
#             "--inconclusive"
#             "--force" 
#             "--inline-suppr"
#             "--suppressions-list=${CMAKE_SOURCE_DIR}/CppCheckSuppressions.txt"
#     )
# endif()

###############################################################################
## target definitions #########################################################
###############################################################################

find_package(PkgConfig REQUIRED)
find_package(Git REQUIRED)

# Eigen 
find_path(EIGENLIB "eigen" ${CMAKE_CURRENT_SOURCE_DIR}/external)
if (NOT EIGENLIB) 
  message("unable to find eigen sources")
  execute_process(COMMAND git submodule update --init -- external/eigen
                  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR})
  set(EIGENLIB ${CMAKE_CURRENT_SOURCE_DIR}/external)
endif()
set(EIGENLIB ${EIGENLIB}/eigen)

# GeographicLib
find_path(GEOGRAPHICLIB "geographiclib" ${CMAKE_CURRENT_SOURCE_DIR}/external)
if (NOT GEOGRAPHICLIB) 
  message("unable to find geographiclib sources")
  execute_process(COMMAND git submodule update --init -- external/geographiclib
                  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR})
  set(GEOGRAPHICLIB ${CMAKE_CURRENT_SOURCE_DIR}/external)
endif()
set(GEOGRAPHICLIB ${GEOGRAPHICLIB}/geographiclib)


add_library(mlm_filter SHARED ${sources_lib})

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
add_subdirectory(${GEOGRAPHICLIB})
unset(BUILD_SHARED_LIBS)
unset(BUILD_DOCUMENTATION)
set(CMAKE_CXX_FLAGS ${CMAKE_CXX_FLAGS_BACK})

add_subdirectory(${EIGENLIB})

target_link_libraries(mlm_filter 
  PUBLIC Eigen3::Eigen
  PUBLIC GeographicLib
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

###############################################################################
## dependencies ###############################################################
###############################################################################


###############################################################################
## testing ####################################################################
###############################################################################

# This is for our testing framework,
# we don't add REQUIRED because it's just for testing.
# People who might want to build the project to use it should not be required
# to install testing dependencies.
find_package(GTest)

if(GTEST_FOUND)
  add_executable(filter_unit_tests ${sources_test})
  target_include_directories(filter_unit_tests PUBLIC inc)

  # This define is added to prevent collision with the main.
  # It might be better solved by not adding the source with the main to the
  # testing target.
  target_compile_definitions(filter_unit_tests PUBLIC _UNIT_TESTS_)

  # This allows us to use the executable as a link library, and inherit all 
  # linker options and library dependencies from it, by simply adding it as dependency.
  set_target_properties(mlm_filter PROPERTIES ENABLE_EXPORTS on)

  target_link_libraries(filter_unit_tests PUBLIC
    ${GTEST_BOTH_LIBRARIES}
    mlm_filter
  )
  target_link_libraries(filter_unit_tests PUBLIC Eigen3::Eigen)

  target_include_directories(filter_unit_tests PUBLIC
    ${GTEST_INCLUDE_DIRS} # doesn't do anything on linux
  )
  
endif()

###############################################################################
## packaging ##################################################################
###############################################################################

# All install commands get the same destination. this allows us to use paths
# relative to the executable.
# This is basically a repeat of the file copy instruction that copies the
# resources in the build directory, but here we tell cmake that we want it
# in the package.
install(TARGETS mlm_filter DESTINATION mad_location_manager_destination)
install(DIRECTORY resources DESTINATION mad_location_manager_destination)

# Now comes everything we need, to create a package
# there are a lot more variables you can set, and some
# you need to set for some package types, but we want to
# be minimal here.
set(CPACK_PACKAGE_NAME "mlm_filter")
set(CPACK_PACKAGE_VERSION "0.1.0")
# We don't want to split our program up into several incomplete pieces.
set(CPACK_MONOLITHIC_INSTALL 1)

# This must be last
include(CPack)
