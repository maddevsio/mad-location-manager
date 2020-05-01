//
//  MadLocationManager.swift
//  MLM
//
//  Created by Haze on 30.04.2020.
//  Copyright © 2020 Mad devs. All rights reserved.
//

import Foundation
import CoreLocation

// MARK: Location Manager extension for checking active state
class MadLocationManager: CLLocationManager {

    private var _isActive: Bool = false
    internal var isActive: Bool { return _isActive }

    internal override func startUpdatingLocation() {
        super.startUpdatingLocation()
        _isActive = true
    }

    internal override func stopUpdatingLocation() {
        super.stopUpdatingLocation()
        _isActive = false
    }
}
