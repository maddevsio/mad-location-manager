# mad-location-manager library
This is a C++ library for GPS and Accelerometer data "fusion" with Kalman filter.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Developed by Mad Devs](https://maddevs.io/badge-light.svg)](https://maddevs.io)

## Requirements

- cmake
- git
- pkg-config
- gkt4
- libshumate
- eigen 

## How to build

This is a CMake base project. To build use following command (or see .nvim.lua file):

```
cmake -B build
cmake --build build -j12
```

Then run `build/mlm_visualizer` application

## How to run tests

```
cmake -B build
cmake --build build -j12
```

Then run `build/filter/filter_unit_tests`

## How to use visualizer

## The roadmap

### Visualizer 

- [x] Implement some route visualizer as desktop application
- [ ] Maybe it's better to rewrite main window using gtkmm (C++)

## License

MIT License

Copyright (c) 2017 Mad Devs

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
