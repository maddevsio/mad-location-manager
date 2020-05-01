//
//  MLMViewController.swift
//  MLM
//
//  Created by Haze on 27.04.2020.
//  Copyright © 2020 Mad devs. All rights reserved.
//

import UIKit

public class MLMViewController: UIViewController {

    fileprivate lazy var accelerometerButton: UIButton = {
        $0.setTitle("Enable Accelerometer", for: .normal)
        $0.addTarget(self, action: #selector(accelerometerAction(_:)), for: .touchUpInside)
        $0.isUserInteractionEnabled = true
        return $0
    }(UIButton(type: .system))

    fileprivate lazy var gyroButton: UIButton = {
        $0.setTitle("Enable Gyro", for: .normal)
        $0.addTarget(self, action: #selector(gyroAction(_:)), for: .touchUpInside)
        $0.isUserInteractionEnabled = true
        return $0
    }(UIButton(type: .system))

    fileprivate lazy var magnetometerButton: UIButton = {
        $0.setTitle("Enable Magnetometer", for: .normal)
        $0.addTarget(self, action: #selector(magnetometerAction(_:)), for: .touchUpInside)
        $0.isUserInteractionEnabled = true
        return $0
    }(UIButton(type: .system))

    fileprivate lazy var locationButton: UIButton = {
        $0.setTitle("Enable Location", for: .normal)
        $0.addTarget(self, action: #selector(locationAction(_:)), for: .touchUpInside)
        $0.isUserInteractionEnabled = true
        return $0
    }(UIButton(type: .system))

    fileprivate lazy var barometerButton: UIButton = {
        $0.setTitle("Enable Barometer", for: .normal)
        $0.addTarget(self, action: #selector(barometerAction(_:)), for: .touchUpInside)
        $0.isUserInteractionEnabled = true
        return $0
    }(UIButton(type: .system))

    fileprivate lazy var shareButton: UIButton = {
        $0.setTitle("Share file", for: .normal)
        $0.addTarget(self, action: #selector(shareAction), for: .touchUpInside)
        $0.isUserInteractionEnabled = true
        return $0
    }(UIButton(type: .system))

    fileprivate let fileManager = MLMFileManager()
    fileprivate var locationManager: MLMLocationManager!
    fileprivate var motionManager: MLMMotionManager!

    deinit {
        print("DEINIT - MLMViewController")
    }

    public override func viewDidLoad() {
        super.viewDidLoad()

        locationManager = MLMLocationManager(fileManager)
        motionManager = MLMMotionManager(fileManager)

        locationManager.requestAuthorization()

        setupView()
    }

    fileprivate func setupView() {
        self.accelerometerButton.translatesAutoresizingMaskIntoConstraints = false
        self.gyroButton.translatesAutoresizingMaskIntoConstraints = false
        self.magnetometerButton.translatesAutoresizingMaskIntoConstraints = false
        self.locationButton.translatesAutoresizingMaskIntoConstraints = false
        self.barometerButton.translatesAutoresizingMaskIntoConstraints = false
        self.shareButton.translatesAutoresizingMaskIntoConstraints = false

        self.view.backgroundColor = .white

        self.view.addSubview(accelerometerButton)
        self.view.addSubview(gyroButton)
        self.view.addSubview(magnetometerButton)
        self.view.addSubview(locationButton)
        self.view.addSubview(barometerButton)
        self.view.addSubview(shareButton)

        accelerometerButton.topAnchor.constraint(equalTo: self.view.topAnchor, constant: 32.0).isActive = true
        accelerometerButton.leadingAnchor.constraint(equalTo: self.view.leadingAnchor, constant: 32.0).isActive = true
        accelerometerButton.trailingAnchor.constraint(equalTo: self.view.trailingAnchor, constant: -32.0).isActive = true
        accelerometerButton.heightAnchor.constraint(equalToConstant: 60.0).isActive = true

        gyroButton.topAnchor.constraint(equalTo: accelerometerButton.bottomAnchor, constant: 32.0).isActive = true
        gyroButton.leadingAnchor.constraint(equalTo: self.view.leadingAnchor, constant: 32.0).isActive = true
        gyroButton.trailingAnchor.constraint(equalTo: self.view.trailingAnchor, constant: -32.0).isActive = true
        gyroButton.heightAnchor.constraint(equalToConstant: 60.0).isActive = true

        magnetometerButton.topAnchor.constraint(equalTo: gyroButton.bottomAnchor, constant: 32.0).isActive = true
        magnetometerButton.leadingAnchor.constraint(equalTo: self.view.leadingAnchor, constant: 32.0).isActive = true
        magnetometerButton.trailingAnchor.constraint(equalTo: self.view.trailingAnchor, constant: -32.0).isActive = true
        magnetometerButton.heightAnchor.constraint(equalToConstant: 60.0).isActive = true

        locationButton.topAnchor.constraint(equalTo: magnetometerButton.bottomAnchor, constant: 32.0).isActive = true
        locationButton.leadingAnchor.constraint(equalTo: self.view.leadingAnchor, constant: 32.0).isActive = true
        locationButton.trailingAnchor.constraint(equalTo: self.view.trailingAnchor, constant: -32.0).isActive = true
        locationButton.heightAnchor.constraint(equalToConstant: 60.0).isActive = true

        barometerButton.topAnchor.constraint(equalTo: locationButton.bottomAnchor, constant: 32.0).isActive = true
        barometerButton.leadingAnchor.constraint(equalTo: self.view.leadingAnchor, constant: 32.0).isActive = true
        barometerButton.trailingAnchor.constraint(equalTo: self.view.trailingAnchor, constant: -32.0).isActive = true
        barometerButton.heightAnchor.constraint(equalToConstant: 60.0).isActive = true

        shareButton.bottomAnchor.constraint(equalTo: self.view.bottomAnchor, constant: -32.0).isActive = true
        shareButton.leadingAnchor.constraint(equalTo: self.view.leadingAnchor, constant: 32.0).isActive = true
        shareButton.trailingAnchor.constraint(equalTo: self.view.trailingAnchor, constant: -32.0).isActive = true
        shareButton.heightAnchor.constraint(equalToConstant: 60.0).isActive = true
    }

    fileprivate func stopAllSensors() {
        accelerometerButton.setTitle("Enable Accelerometer", for: .normal)
        motionManager.stopAccelerometerUpdate()

        gyroButton.setTitle("Enable Gyro", for: .normal)
        motionManager.stopGyroUpdate()

        magnetometerButton.setTitle("Enable Magnetometer", for: .normal)
        motionManager.stopMagnetometerUpdate()

        locationButton.setTitle("Enable Location", for: .normal)
        locationManager.stopLocationServices()
        locationManager.stopUpdatingHeading()

        barometerButton.setTitle("Enable Barometer", for: .normal)
        motionManager.stopAltimeterUpdate()
    }
}

// MARK: Manager Actions
extension MLMViewController {
    @objc fileprivate func accelerometerAction(_ sender: UIButton) {
        if motionManager.accelerometerActive {
            sender.setTitle("Enable Accelerometer", for: .normal)
            motionManager.stopAccelerometerUpdate()
        } else {
            sender.setTitle("Disable Accelerometer", for: .normal)
            motionManager.startAccelerometerUpdate()
        }
    }

    @objc fileprivate func gyroAction(_ sender: UIButton) {
        if motionManager.gyroActive {
            sender.setTitle("Enable Gyro", for: .normal)
            motionManager.stopGyroUpdate()
        } else {
            sender.setTitle("Disable Gyro", for: .normal)
            motionManager.startGyroUpdate()
        }
    }

    @objc fileprivate func magnetometerAction(_ sender: UIButton) {
        if motionManager.magnetometerActive {
            sender.setTitle("Enable Magnetometer", for: .normal)
            motionManager.stopMagnetometerUpdate()
        } else {
            sender.setTitle("Disable Magnetometer", for: .normal)
            motionManager.startMagnetometerUpdate()
        }
    }

    @objc fileprivate func locationAction(_ sender: UIButton) {
        if locationManager.locationManagerIsActive {
            sender.setTitle("Enable Location", for: .normal)
            locationManager.stopLocationServices()
            locationManager.stopUpdatingHeading()
        } else {
            sender.setTitle("Disable Location", for: .normal)
            locationManager.startLocationServices()
            locationManager.startUpdatingHeading(forOrientation: .portrait)
        }
    }

    @objc fileprivate func barometerAction(_ sender: UIButton) {
        if motionManager.altimeterIsActive {
            sender.setTitle("Enable Barometer", for: .normal)
            motionManager.stopAltimeterUpdate()
        } else {
            sender.setTitle("Disable Barometer", for: .normal)
            motionManager.startAltimeterUpdate()
        }
    }

    @objc fileprivate func shareAction() {
        stopAllSensors()

        let fileURL = fileManager.fileUrl

        var filesToShare = [Any]()

        filesToShare.append(fileURL)

        let activityViewController = UIActivityViewController(activityItems: filesToShare, applicationActivities: nil)
        activityViewController.popoverPresentationController?.sourceView = self.view
        activityViewController.excludedActivityTypes = [.airDrop, .mail]

        activityViewController.completionWithItemsHandler = { (activityType: UIActivity.ActivityType?, completed: Bool, returnedItems: [Any]?, error: Error?) -> Void in
            if completed {
                self.fileManager.removeFile()
            }
        }

        self.present(activityViewController, animated: true, completion: nil)
    }
}

