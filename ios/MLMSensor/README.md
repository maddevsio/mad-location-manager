# MLM iOS Sensors
This is library for GPS, Accelerometer, Gyroscope, Barometer, Magnetometer data colletor. 
Project consists of 3 main parts: MLMLocationManager, MLMMotionManager, MLMFileManager.

## How to build framework

1. Open project MLMSensor.xcodeproj
2. Clean build folder of project and Build project ```Command + Shift + K```, ```Command + B```
3. Generate .xcframework file, follwed commands:

```

xcodebuild archive -scheme MLMSensor -archivePath "./build/MLMios.xcarchive" -sdk iphoneos SKIP_INSTALL=NO
xcodebuild archive -scheme MLMSensor -archivePath "./build/MLMios_sim.xcarchive" -sdk iphonesimulator SKIP_INSTALL=NO
xcodebuild -create-xcframework -framework "./build/MLMios.xcarchive/Products/Library/Frameworks/MLMSensor.framework" -framework "./build/MLMios_sim.xcarchive/Products/Library/Frameworks/MLMSensor.framework" -output "./build/MLMSensor.xcframework"

```

## How to install

Install with xcframework:
1. Drag and Drop MLMSensor.xcframework, to the project.
2. Into Build Phases add New Copy File Phase, setup into Destinatio: Framework
3. Press "+" and choose MLMSensor.xcframework.

Install by files:
1. Drag and Drop folder MLMManagers
2. Use :)

## How to use

1. import MLMSensor
2. Call MLMViewController
3. Enable all sensors collect data
4. Stop sensors collect data
5. Share file with collected data

### NOTE:
All sensors data collect only on real device.
