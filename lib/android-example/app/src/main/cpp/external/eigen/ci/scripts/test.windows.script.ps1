# Change to build directory.
$rootdir = Get-Location
cd $EIGEN_CI_BUILDDIR

# Determine number of processors for parallel tests.
$NPROC=${Env:NUMBER_OF_PROCESSORS}

# Set target based on regex or label.
$target = ""
if (${EIGEN_CI_TEST_REGEX}) {
  $target = "-R","${EIGEN_CI_TEST_REGEX}"
} elseif (${EIGEN_CI_TEST_LABEL}) {
  $target = "-L","${EIGEN_CI_TEST_LABEL}"
}

# Repeat tests up to three times to ignore flakes.  Do not re-run with -T test,
# otherwise we lose test results for those that passed.
# Note: starting with CMake 3.17, we can use --repeat until-pass:3, but we have
#       no way of easily installing this on ppc64le.
ctest -j$NPROC --output-on-failure --no-compress-output --build-no-clean -T test $target || `
  ctest -j$NPROC --output-on-failure --no-compress-output --rerun-failed || `
  ctest -j$NPROC --output-on-failure --no-compress-output --rerun-failed

$success = $LASTEXITCODE

# Return to root directory.
cd ${rootdir}

# Explicitly propagate exit code to indicate pass/failure of test command.
if($success -ne 0) { Exit $success }
