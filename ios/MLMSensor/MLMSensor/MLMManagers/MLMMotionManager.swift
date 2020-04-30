//
//  MLMMotionManager.swift
//  MLM
//
//  Created by Haze on 27.04.2020.
//  Copyright © 2020 Mad devs. All rights reserved.
//

import Foundation
import CoreMotion

var mlmUnixTimeNow: Double {
    return Date().timeIntervalSince1970
}

enum MLMSensorDataType: Int {
    case GPS = 0
    case Accelerometer = 1
    case Gyro = 2
    case Magnetometer = 3
    case Barometer = 4
    case Speed = 5
}

class MLMMotionManager {

    private var motionManager: CMMotionManager!
    private var altimeter: MLMAltimeter!

    fileprivate var accelerometerOperation: OperationQueue!
    fileprivate var gyroOperation: OperationQueue!
    fileprivate var magnetometerOperation: OperationQueue!
    fileprivate var altimeterOperation: OperationQueue!

    fileprivate var fileManager: MLMFileManager!

    internal init(_ fileManager: MLMFileManager) {

        self.fileManager = fileManager
        motionManager = CMMotionManager()
        altimeter = MLMAltimeter()

        motionManager.accelerometerUpdateInterval = 1.0 / 100.0 //100 Hz
        accelerometerOperation = OperationQueue()
        accelerometerOperation.qualityOfService = .background

        motionManager.gyroUpdateInterval = 1.0 / 100.0 //100 Hz
        gyroOperation = OperationQueue()
        gyroOperation.qualityOfService = .background

        motionManager.magnetometerUpdateInterval = 1.0 / 100.0 //100 Hz
        magnetometerOperation = OperationQueue()
        magnetometerOperation.qualityOfService = .background

        altimeterOperation = OperationQueue()
        altimeterOperation.qualityOfService = .background
    }

    deinit {
        stopAccelerometerUpdate()
        stopGyroUpdate()
        stopMagnetometerUpdate()
        stopAltimeterUpdate()
        print("DEINIT - MLMMotionManager")
    }

}

// MARK: Altimeter
extension MLMMotionManager {
    internal func startAltimeterUpdate() {
        altimeter.startRelativeAltitudeUpdates(to: altimeterOperation) { [unowned self] (altimeterData, error) in
            guard let altimeterData = altimeterData else { return }
            let altitudeInfo = "\n\(MLMSensorDataType.Barometer.rawValue) \(mlmUnixTimeNow) BAR: z=\(altimeterData.relativeAltitude)"
            self.fileManager.writeQueueText(altitudeInfo, queue: self.fileManager.barQeueue)
        }
    }

    internal func stopAltimeterUpdate() {
        altimeter.stopRelativeAltitudeUpdates()
    }

    internal var altimeterIsActive: Bool {
        return altimeter.isActive
    }
}

// MARK: Accelerometer
extension MLMMotionManager {
    internal func startAccelerometerUpdate() {
        motionManager.startAccelerometerUpdates(to: accelerometerOperation) { [unowned self] (accelerometerData, error) in
            guard let accelerometerData = accelerometerData else { return }
            let accelerometerInfo = "\n\(MLMSensorDataType.Accelerometer.rawValue) \(mlmUnixTimeNow) ACC: x=\(accelerometerData.acceleration.x) y=\(accelerometerData.acceleration.y) z=\(accelerometerData.acceleration.z)"
            self.fileManager.writeQueueText(accelerometerInfo, queue: self.fileManager.accQueue)
        }
    }

    internal func stopAccelerometerUpdate() {
        motionManager.stopAccelerometerUpdates()
    }

    internal var accelerometerAvailable: Bool {
        return motionManager.isAccelerometerAvailable
    }

    internal var accelerometerActive: Bool {
        return motionManager.isAccelerometerActive
    }

}

// MARK: Gyro operation
extension MLMMotionManager {
    internal func startGyroUpdate() {
        motionManager.startGyroUpdates(to: gyroOperation) { [unowned self] (gyroData, error) in
            guard let gyroData = gyroData else { return }
            let gyroInfo = "\n\(MLMSensorDataType.Gyro.rawValue) \(mlmUnixTimeNow) GYR: x=\(gyroData.rotationRate.x) y=\(gyroData.rotationRate.y) z=\(gyroData.rotationRate.z)"
            self.fileManager.writeQueueText(gyroInfo, queue: self.fileManager.gyroQueue)
        }
    }

    internal func stopGyroUpdate() {
        motionManager.stopGyroUpdates()
    }

    internal var gyroAvailable: Bool {
        return motionManager.isGyroAvailable
    }

    internal var gyroActive: Bool {
        return motionManager.isGyroActive
    }
}

// MARK: Magnetometer
extension MLMMotionManager {
    internal func startMagnetometerUpdate() {
        motionManager.startMagnetometerUpdates(to: magnetometerOperation) { [unowned self] (magnetometerData, erro) in
            guard let magnetometerData = magnetometerData else { return }
            let magnetometerInfo = "\n\(MLMSensorDataType.Magnetometer.rawValue) \(mlmUnixTimeNow) MAG: x=\(magnetometerData.magneticField.x) y=\(magnetometerData.magneticField.y) z=\(magnetometerData.magneticField.z)"
            self.fileManager.writeQueueText(magnetometerInfo, queue: self.fileManager.magQueue)
        }
    }

    internal func stopMagnetometerUpdate() {
        motionManager.stopMagnetometerUpdates()
    }

    internal var magnetometerAvailable: Bool {
        return motionManager.isMagnetometerAvailable
    }

    internal var magnetometerActive: Bool {
        return motionManager.isMagnetometerActive
    }
}
