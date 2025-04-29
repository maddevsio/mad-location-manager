cmake_minimum_required(VERSION 3.16)

project(mlm_visualizer)            # this sets the project name

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

if(CMAKE_BUILD_TYPE MATCHES Debug)
  add_custom_target(valgrind_memcheck valgrind --tool=memcheck --num-callers=50 --leak-check=full --suppressions=/usr/share/gtk-4.0/valgrind/gtk.supp --suppressions=/usr/share/glib-2.0/valgrind/glib.supp --leak-resolution=high --show-leak-kinds=definite --log-file=vgdump ${CMAKE_CURRENT_BINARY_DIR}/${PROJECT_NAME})
endif()


# These instructions search the directory tree when cmake is
# invoked and put all files that match the pattern in the variables 
# `sources` and `data`.
file(GLOB_RECURSE sources src/*.cpp inc/*.h)
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
# GTK4
pkg_check_modules(GTK REQUIRED gtk4)
include_directories(${GTK_INCLUDE_DIRS})
link_directories(${GTK_LIBRARY_DIRS})

# LIBSHUMATE-1.0
pkg_check_modules(SHUMATE REQUIRED shumate-1.0) 
include_directories(${SHUMATE_INCLUDE_DIRS})
link_directories(${SHUMATE_LIBRARY_DIRS})

# mlm filter
add_subdirectory(filter)
include_directories(filter/inc)

add_executable(${PROJECT_NAME} ${sources})
target_link_libraries(${PROJECT_NAME} 
  ${GTK_LIBRARIES}
  ${SHUMATE_LIBRARIES}
  mlm_filter
)

# This allows to include files relative to the root of the src directory with a <> pair
target_include_directories(${PROJECT_NAME} PUBLIC 
  ${PROJECT_SOURCE_DIR}/inc 
  ${PROJECT_SOURCE_DIR}/filter/inc)
# Just for ${PROJECT_NAME} add some compiler flags.

set(warning_level -Wall -Wextra -pedantic)
set(CMAKE_CXX_FLAGS_RELEASE "-O2")
set(CMAKE_CXX_FLAGS_DEBUG "-O0 -g")
target_compile_options(${PROJECT_NAME} PUBLIC ${warning_level})

# This copies all resource files in the build directory.
# We need this, because we want to work with paths relative to the executable.
file(COPY ${PROJECT_SOURCE_DIR}/ui DESTINATION ${PROJECT_BINARY_DIR})

###############################################################################
## testing ####################################################################
###############################################################################

# This is for our testing framework,
# we don't add REQUIRED because it's just for testing.
# People who might want to build the project to use it should not be required
# to install testing dependencies.
find_package(GTest)

if(GTEST_FOUND)
  add_executable(unit_tests_${PROJECT_NAME} ${sources_test} ${sources})
  target_include_directories(unit_tests_${PROJECT_NAME} PRIVATE inc)

  # This define is added to prevent collision with the main.
  # It might be better solved by not adding the source with the main to the
  # testing target.
  target_compile_definitions(unit_tests_${PROJECT_NAME} PUBLIC _UNIT_TESTS_)

  # This allows us to use the executable as a link library, and inherit all 
  # linker options and library dependencies from it, by simply adding it as dependency.
  set_target_properties(${PROJECT_NAME} PROPERTIES ENABLE_EXPORTS on)

  target_link_libraries(unit_tests_${PROJECT_NAME} PUBLIC
    ${GTEST_BOTH_LIBRARIES}
    ${PROJECT_NAME}
  )

  target_include_directories(unit_tests_${PROJECT_NAME} PUBLIC
    ${GTEST_INCLUDE_DIRS} # doesn't do anything on linux
  )
  
endif()



###############################################################################
## packaging ##################################################################
###############################################################################

# All install commands get the same destination. this allows us to use paths
# relative to the executable.
install(TARGETS ${PROJECT_NAME} DESTINATION ${PROJECT_NAME}_destination)

# This is basically a repeat of the file copy instruction that copies the
# resources in the build directory, but here we tell cmake that we want it
# in the package.
install(DIRECTORY resources DESTINATION ${PROJECT_NAME}_destination)

# Now comes everything we need, to create a package
# there are a lot more variables you can set, and some
# you need to set for some package types, but we want to
# be minimal here.
set(CPACK_PACKAGE_NAME "${PROJECT_NAME}")
set(CPACK_PACKAGE_VERSION "0.1.0")
# We don't want to split our program up into several incomplete pieces.
set(CPACK_MONOLITHIC_INSTALL 1)

# This must be last
include(CPack)
