#include <gtest/gtest.h>
#include <iostream>

#include "commons.h"

#ifdef _UNIT_TESTS_
int main_tests(int argc, char *argv[]) {
  testing::InitGoogleTest(&argc, argv);
  return RUN_ALL_TESTS();
}
#endif
//////////////////////////////////////////////////////////////

int main(int argc, char *argv[], char **env) {
#ifdef _UNIT_TESTS_
  UNUSED(env);
  return main_tests(argc, argv);
#else
  UNUSED(argc);
  UNUSED(argv);
  UNUSED(env);

  std::cout << "filter\n";
  return 0;
#endif
}
/////////////////////////////////////////////////////////////
