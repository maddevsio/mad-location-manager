# mad-location-manager 
This is library for GPS and Accelerometer data "fusion" with Kalman filter. 
Project consists of 2 parts: GpsAccelerationKalmanFusion (AAR module) and 2 helper applications. Main thing here is GpsAccelerationKalmanFusion module.

[Blog (english version)](https://blog.maddevs.io/reduce-gps-data-error-on-android-with-kalman-filter-and-accelerometer-43594faed19c)

[Blog (russian version)](https://blog.maddevs.io/ru-reduce-gps-data-error-on-android-with-kalman-filter-and-accelerometer-b81f1026e06c)

[Our site](https://gps.maddevs.io/en/)


[![](https://jitpack.io/v/maddevsio/mad-location-manager.svg)](https://jitpack.io/#maddevsio/mad-location-manager)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

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
- [ ] Improve UI. Need to use some controls for coefficient/noise changes

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
- [x] Add library to some public repository

## Theory

Kalman filtering, also known as linear quadratic estimation (LQE), is an algorithm that uses a series of measurements observed over time, containing statistical noise and other inaccuracies, and produces estimates of unknown variables that tend to be more accurate than those based on a single measurement alone, by estimating a joint probability distribution over the variables for each timeframe.

You can get more details about the filter [here](https://en.wikipedia.org/wiki/Kalman_filter).

The filter is a de-facto standard solution in navigation systems. The project simply defines the given data and implements some math.

The project uses 2 data sources: GPS and accelerometer. GPS coordinates are not very accurate, but each of them doesn't depend on previous values. So, there is no accumulation error in this case. On the other hand, the accelerometer has very accurate readings, but it accumulates error related to noise and integration error. Therefore, it is necessary to "fuse" these two sources. Kalman is the best solution here.

So first - we need to define matrices and do some math with them. And second - we need to get real acceleration (not in device orientation) . First one is described in current project's wiki. But second one is little bit more complex thing called "sensor fusion". There is a lot information about this in internet. For real acceleration we need to know 2 things : device orientation and "linear acceleration". Linear acceleration is acceleration along each device axis excluding force of gravity. It could be calculated by high pass filter or with more complex algorithms. Device orientation could be calculated in many ways :

- Using accelerometer + magnetometer
- Using accelerometer + magnetometer + gyroscope
- Using [Madgwick filter](http://x-io.co.uk/open-source-imu-and-ahrs-algorithms/)
- Using virtual "rotation vector" sensor. 

Best results show Madgwick filter and ROTATION_VECTOR sensor, but Madgwick filter should be used when we know sensor frequency. Android doesn't provide such information. We can set minimum frequency, but it could be much higher then specified. Also we need to provide gain coefficient for each device. So best solution here is to use virtual ROTATION_VECTOR sensor. You can get more details from current project's wiki.

## Issues

Feel free to send pull requests. Also feel free to create issues.

## License

MIT License

Copyright (c) 2017 Mad Devs

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
