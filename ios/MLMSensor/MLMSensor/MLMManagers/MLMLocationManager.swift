//
//  MLMLocationManager.swift
//  MLM
//
//  Created by Haze on 27.04.2020.
//  Copyright © 2020 Mad devs. All rights reserved.
//

import Foundation
import CoreLocation

class MLMLocationManager: NSObject {

    private var locationManager: MadLocationManager!

    internal var authorizationStatus: CLAuthorizationStatus
    internal var usersLocation: CLLocation?

    fileprivate var headingAngle: Double = 0.0

    fileprivate var fileManager: MLMFileManager!

    internal init(_ fileManager: MLMFileManager!) {

        self.fileManager = fileManager
        authorizationStatus = CLLocationManager.authorizationStatus()

        super.init()

        locationManager = MadLocationManager()

        locationManager.delegate = self
        locationManager.allowsBackgroundLocationUpdates = true
        locationManager.pausesLocationUpdatesAutomatically = false
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.distanceFilter = kCLDistanceFilterNone
        locationManager.headingFilter = kCLHeadingFilterNone
        locationManager.activityType = .fitness
    }

    deinit {
        stopUpdatingHeading()
        stopLocationServices()
        print("DEINIT - MLMLocationManager")
    }

    // MARK: Authorization
    internal func requestAuthorization() {

        switch CLLocationManager.authorizationStatus() {
        case .authorizedAlways, .authorizedWhenInUse:
            print("Location services are already authorized.")
        case .notDetermined:
            print("Requesting location services authorization.")
        case .denied:
            print("User needs to allow location services in iPhone Settings app.")
        case .restricted:
            print("Location services are restricted and may be unavailable.")
        @unknown default:
            print("Location services Unknown default status")
        }

        locationManager.requestWhenInUseAuthorization()
    }

    // MARK: Start and End Location Services
    internal func startLocationServices() {
        locationManager.startUpdatingLocation()
    }

    internal func stopLocationServices() {
        locationManager.stopUpdatingLocation()
    }

    // MARK: Start and Stop Heading Updates
    internal func startUpdatingHeading(forOrientation headingOrientation: CLDeviceOrientation = .portrait) {
        locationManager.headingOrientation = headingOrientation
        locationManager.startUpdatingHeading()
    }

    internal func stopUpdatingHeading() {
        locationManager.stopUpdatingHeading()
    }
}

// MARK: Location Manager Delegate
extension MLMLocationManager: CLLocationManagerDelegate {
    internal func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        authorizationStatus = status
    }

    internal func locationManager(_ manager: CLLocationManager, didUpdateLocations newLocations: [CLLocation]) {
        guard let usersLocation = manager.location else { return }
        self.usersLocation = usersLocation

        let coordinatesInfo = "\n\(MLMSensorDataType.GPS.rawValue) \(mlmUnixTimeNow) GPSC: lat=\(usersLocation.coordinate.latitude) lon=\(usersLocation.coordinate.longitude) alt=\(usersLocation.altitude)"
        let speedInfo = "\n\(MLMSensorDataType.Speed.rawValue) \(mlmUnixTimeNow) GPSS: speed=\(usersLocation.speed) north_angle=\(self.headingAngle)"

        self.fileManager.writeQueueText(coordinatesInfo, queue: self.fileManager.gpsQueue)
        self.fileManager.writeQueueText(speedInfo, queue: self.fileManager.speedQueue)
    }

    internal func locationManagerShouldDisplayHeadingCalibration(_ manager: CLLocationManager) -> Bool {
        return true
    }

    internal func locationManager(_ manager: CLLocationManager, didUpdateHeading newHeading: CLHeading) {
        self.headingAngle = newHeading.trueHeading
    }

    internal var locationManagerIsActive: Bool {
        return locationManager.isActive
    }
}
