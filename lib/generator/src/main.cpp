#include <gtest/gtest.h>

#include "main_generator.h"

#ifdef _UNIT_TESTS_
int main_tests(int argc, char *argv[])
{
  testing::InitGoogleTest(&argc, argv);
  return RUN_ALL_TESTS();
}
#endif
//////////////////////////////////////////////////////////////

int main(int argc, char *argv[], char **env)
{
#ifdef _UNIT_TESTS_
  return main_tests(argc, argv);
#else
  return generator_entry_point(argc, argv, env);
#endif
}
/////////////////////////////////////////////////////////////
