// Example of using the GeographicLib::MagneticModel class
// This requires that the wmm2010 magnetic model be installed; see
// https://geographiclib.sourceforge.io/C++/doc/magnetic.html#magneticinst

#include <iostream>
#include <exception>
#include <GeographicLib/MagneticModel.hpp>

using namespace std;
using namespace GeographicLib;

int main() {
  try {
    MagneticModel mag("wmm2010");
    double lat = 27.99, lon = 86.93, h = 8820, t = 2012; // Mt Everest
    double Bx, By, Bz;
    mag(t, lat,lon, h, Bx, By, Bz);
    double H, F, D, I;
    MagneticModel::FieldComponents(Bx, By, Bz, H, F, D, I);
    cout << H << " " << F << " " << D << " " << I << "\n";
  }
  catch (const exception& e) {
    cerr << "Caught exception: " << e.what() << "\n";
    return 1;
  }
}
