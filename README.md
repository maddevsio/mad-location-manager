# mad-location-manager 
This is library for GPS and Accelerometer data "fusion" with Kalman filter. 
Project consists of 2 parts: gpsacckalmanfusion (AAR module) and 2 helper applications. Main thing here is gpsacckalmanfusion module.

## How can mad-location-manager help you to get location more accurately

### Problem
On some phones we have gps receiver with bad quality. This leads to positioning error and big mistake in distance calculation. So there are some solutions described below.

#### Kalman filter

Kalman filtering, also known as linear quadratic estimation (LQE), is an algorithm that uses a series of measurements observed over time, containing statistical noise and other inaccuracies, and produces estimates of unknown variables that tend to be more accurate than those based on a single measurement alone, by estimating a joint probability distribution over the variables for each timeframe. 

More details about filter are [here](https://en.wikipedia.org/wiki/Kalman_filter) 

This filter is de-facto standard solution in navigation systems. In our case we just need to define what do we have and implement some math. 

We will use 2 data sources : GPS and accelerometer. GPS readings are not very accurate, but each reading doesn't depend on previous values. So we don't have error accumulation here. In opposite side accelerometer has very accurate readings, but it accumulates error related to noise + integration error. So we need to "fuse" this two sources. Kalman is the best solution here. There are a lot of articles about it. 

More details about accelerometer is in sensor fusion section. Let's assume that we can get acceleration axis related to some coordinate system (East/North/Up in our case). 

So our model will have 4 dimensions: x (longitude), y (latitude), x' (velocity x), y' (velocity y). We can measure all of them via GPS. Also we can get accuracy. We can use NMEA data for that (for example). All we have to do is to describe our process.

Let's define matrices : 
1. State transition matrix (F):
[1.0, 0.0, dt, 0.0,
 0.0, 1.0, 0.0, dt,
 0.0, 0.0, 1.0, 0.0,
 0.0, 0.0, 0.0, 1.0] 
dt here is period between two PREDICT steps. 

2. Observation model (H): 
Identity matrix 4x4 . We can get x, y, x' and y' from GPS receiver. 

3. Control matrix (B) : 
[ dt^2 / 2.0, 0.0,
  0.0, dt^2 / 2.0,
  dt, 0.0,
  0.0, dt ]
dt here is period between two PREDICT steps

4. Control vector (U) : 
[x'', y''].T - we can get x acceleration (x'') and y acceleration (y'') with accelerometer and rotation vector.

5. Process noise (Q) : 
Q.setIdentity();
Q.scale(accSigma * dt);
Looks after that like this : 
[ accSigma*dt, 0.0, 0.0, 0.0,
  0.0, accSigma*dt, 0.0, 0.0,
  0.0, 0.0, accSigma*dt, 0.0,
  0.0, 0.0, 0.0, accSigma*dt ]
dt here is period between two UPDATE steps. So here we are increasing prediction error with time. It helps to compensate integration error.

6. Measurement noise (R) : 
R.setIdentity();
R.scale(posSigma);
After that it will looks like :
[ posSigma, 0.0, 0.0, 0.0,
  0.0, posSigma, 0.0, 0.0,
  0.0, 0.0, posSigma, 0.0,
  0.0, 0.0, 0.0, posSigma ]
posSigma - value we got from HDOP or from method Location.getAccuracy. 

All formulas got from movement law : x = x0 + x'*dt + x''*dt^2 / 2 . 

#### Sensor fusion
To get phone orientation and acceleration axis related to absolute coordinate system we have to use different sensors. Accelerometer "knows" where is Earth. Magnetometer "knows" where is North. Gyroscope can give angle accelerations with high accuracy. We can use all off them with something called "sensor fusion"
It's a really complex thing. There are a lot of solutions: complementary filter, Kalman filter, Madgwick filter etc... Thank to android developers - they did almost all work. At least for our problem we can use their virtual devices LINEAR_ACCELEROMETER and ROTATION_VECTOR with enough precision. Madgwick gives good results too and works faster, but it's difficult to find good gain coefficient for that filter + it has initialization time.  
There is extended kalman filter in core of these sensors. More details [here](https://android.googlesource.com/platform/frameworks/native/+/master/services/sensorservice/SensorFusion.cpp)

With ROTATION_VECTOR we can get quaternion. From quaternion we can get rotation matrix and after that we just need to multiply our linear acceleration vector by rotation matrix and get our acceleration axis as result. Also we need to rotate our East/North axis by [magnetic declination](https://www.ngdc.noaa.gov/geomag/declination.shtml) angle. 

So after those operations we have acceleration vector related to absolute coordinates. 
Don't forget, that LINEAR_ACCELEROMETER has "drift" and we have to do calibration step before using LINEAR_ACCELEROMETER. But value of this "drift" is not big. About 0.004m/s^2. 

#### Geohash 

[Geohash](https://en.wikipedia.org/wiki/Geohash) is cool thing that helps us to join several points into one (with some restrictions of course). We can choose precision and calculate this geohash really fast (algorithm looks like binary search). In our solution we use it for "thinning" coordinates set. For bad signal (+- 300m) it's recommended to use precision 7. Also need to sum count of points with same geohash. If this sum < 3 (for example) then remove all points related to that geohash.

#### Problems

1. Huge integration error related to accelerometer noise. When accelerometer frequency ~50Hz and GPS frequncy ~0.5Hz it's ok and nothing to do. When GPS frequency lower we are getting really bad precision and wrong position as result. We increase process noise variance when period between GPS readings becomes big, but it doesn't seem to be really helpful. It helps when phone is in rest state. We can reset filter if this period >= 15sec. 
2. Accelerometer "drift". When phone is in rest state accelerometer shows some acceleration. About 0.002m/s^2, but it gives significant error. It's recommended to make some calibration step.
3. Process noise covariance doesn't seem to be really precise. We get it by empiric method. Some info could be found [here](http://campar.in.tum.de/Chair/KalmanFilter) and Wikipedia, but result isn't really good with that noise covariance. 

## The roadmap
### Visualizer 
### Filter 
### Web inteface

## Issues

Feel free to send pull requests. Also feel free to create issues.

## License

MIT License

Copyright (c) 2017 Mad Devs

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
