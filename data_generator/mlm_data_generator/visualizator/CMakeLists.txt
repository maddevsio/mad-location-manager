cmake_minimum_required(VERSION 3.16)

project(CodewarsCPP)            # this sets the project name

###############################################################################
## file globbing ##############################################################
###############################################################################

set(CMAKE_EXPORT_COMPILE_COMMANDS ON)
# These instructions search the directory tree when cmake is
# invoked and put all files that match the pattern in the variables 
# `sources` and `data`.
file(GLOB_RECURSE sources src/*.cpp inc/*.h resources/*.h resources/*.cpp)
file(GLOB_RECURSE sources_test tests/*.cpp)
# You can use set(sources src/main.cpp) etc if you don't want to
# use globbing to find files automatically.

###############################################################################
## target definitions #########################################################
###############################################################################

find_package(PkgConfig REQUIRED)

# GTK4
pkg_check_modules(GTK REQUIRED gtk4)
include_directories(${GTK_INCLUDE_DIRS})
link_directories(${GTK_LIBRARY_DIRS})

# LIBSHUMATE-1.0
pkg_check_modules(SHUMATE REQUIRED shumate-1.0) 
include_directories(${SHUMATE_INCLUDE_DIRS})
link_directories(${SHUMATE_LIBRARY_DIRS})

add_executable(mlm_visualizer ${sources})
target_link_libraries(mlm_visualizer ${GTK_LIBRARIES})
target_link_libraries(mlm_visualizer ${SHUMATE_LIBRARIES})

# This allows to include files relative to the root of the src directory with a <> pair
# target_include_directories(mlm_visualizer PRIVATE inc resources)
target_include_directories(mlm_visualizer PUBLIC inc resources)
# Just for mlm_visualizer add some compiler flags.

set(warning_level -Wall -Wextra -pedantic)
set(cxxflags -pipe -O0 -g -std=gnu++2a)
target_compile_options(mlm_visualizer PUBLIC ${warning_level} ${cxxflags})

# This copies all resource files in the build directory.
# We need this, because we want to work with paths relative to the executable.
file(COPY ${data} DESTINATION resources)

###############################################################################
## dependencies ###############################################################
###############################################################################

# This defines the variables Boost_LIBRARIES that containts all library names
# that we need to link into the program.
# find_package(Boost 1.36.0 COMPONENTS filesystem system REQUIRED)

# target_link_libraries(mlm_visualizer PUBLIC
#   ${Boost_LIBRARIES}
#   # here you can add any library dependencies
# )

###############################################################################
## testing ####################################################################
###############################################################################

# This is for our testing framework,
# we don't add REQUIRED because it's just for testing.
# People who might want to build the project to use it should not be required
# to install testing dependencies.
find_package(GTest)

if(GTEST_FOUND)
  add_executable(unit_tests ${sources_test} ${sources})
  target_include_directories(unit_tests PRIVATE inc)

  # This define is added to prevent collision with the main.
  # It might be better solved by not adding the source with the main to the
  # testing target.
  target_compile_definitions(unit_tests PUBLIC _UNIT_TESTS_)

  # This allows us to use the executable as a link library, and inherit all 
  # linker options and library dependencies from it, by simply adding it as dependency.
  set_target_properties(mlm_visualizer PROPERTIES ENABLE_EXPORTS on)

  target_link_libraries(unit_tests PUBLIC
    ${GTEST_BOTH_LIBRARIES}
    mlm_visualizer
  )

  target_include_directories(unit_tests PUBLIC
    ${GTEST_INCLUDE_DIRS} # doesn't do anything on linux
  )
  
endif()

###############################################################################
## packaging ##################################################################
###############################################################################

# All install commands get the same destination. this allows us to use paths
# relative to the executable.
install(TARGETS mlm_visualizer DESTINATION example_destination)

# This is basically a repeat of the file copy instruction that copies the
# resources in the build directory, but here we tell cmake that we want it
# in the package.
install(DIRECTORY resources DESTINATION mlm_visualizer_destination)

# Now comes everything we need, to create a package
# there are a lot more variables you can set, and some
# you need to set for some package types, but we want to
# be minimal here.
set(CPACK_PACKAGE_NAME "MyExample")
set(CPACK_PACKAGE_VERSION "1.0.0")
# We don't want to split our program up into several incomplete pieces.
set(CPACK_MONOLITHIC_INSTALL 1)

# This must be last
include(CPack)
