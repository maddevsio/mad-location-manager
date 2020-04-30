//
//  MLMAltitude.swift
//  MLM
//
//  Created by Haze on 30.04.2020.
//  Copyright © 2020 Mad devs. All rights reserved.
//

import Foundation
import CoreMotion

// MARK: Altimeter extension for checking active state
class MLMAltimeter: CMAltimeter {

    private var _isActive: Bool = false
    internal var isActive: Bool { return _isActive }

    internal override func startRelativeAltitudeUpdates(to queue: OperationQueue, withHandler handler: @escaping CMAltitudeHandler) {
        super.startRelativeAltitudeUpdates(to: queue, withHandler: handler)
        _isActive = true
    }

    internal override func stopRelativeAltitudeUpdates() {
        super.stopRelativeAltitudeUpdates()
        _isActive = false
    }
}
