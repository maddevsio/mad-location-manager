# mad-location-manager 
This is library for GPS and Accelerometer data "fusion" with Kalman filter. 
Project consists of 2 parts: GpsAccelerationKalmanFusion (AAR module) and 2 helper applications. Main thing here is GpsAccelerationKalmanFusion module.

## How can mad-location-manager help you to get location more accurately

This module helps to increase GPS coordinates accuracy and smooth "jumps" from track. 

## How to install

### Gradle

1. Add it in your root build.gradle at the end of repositories:

```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

2. Add the dependency: 

```
dependencies {
        compile 'com.github.maddevsio:mad-location-manager:0.1.0'
}
```

### Maven

1. Add the JitPack repository to your build file:


```
<repositories>
	<repository>
	    <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
</repositories>
```

2. Add the dependency:

```
<dependency>
    <groupId>com.github.maddevsio</groupId>
    <artifactId>mad-location-manager</artifactId>
    <version>0.1.0</version>
</dependency>
```

###

## The roadmap
### Visualizer 

- [x] Implement some route visualizer for desktop application
- [x] Implement Kalman filter and test all settings
- [x] Implement noise generator for logged data

### Filter 

- [x] Implement GeoHash function
- [x] Get device orientation
	- [x] Get device orientation using magnetometer and accelerometer + android sensor manager
	- [x] Get device orientation using magnetometer, accelerometer and gyroscope + Madgwick AHRS
	- [x] Get device orientation using rotation vector virtual sensor
- [x] Compare result device orientation and choose most stable one
- [x] Get linear acceleration of device (acceleration without gravity force)
- [x] Convert relative linear acceleration axis to absolute coordinate system (east/north/up)
- [x] Implement Kalman filter core
- [x] Implement Kalman filter for accelerometer and gps data "fusion"
- [x] Logger for pure GPS data, acceleration data and filtered GPS data.
- [ ] Restore route if gps connection is lost

### Library

- [x] Separate test android application and library
- [ ] Add library to maven repository

## Issues

Feel free to send pull requests. Also feel free to create issues.

## License

MIT License

Copyright (c) 2017 Mad Devs

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
