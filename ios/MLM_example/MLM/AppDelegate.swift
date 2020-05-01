//
//  AppDelegate.swift
//  MLM
//
//  Created by Haze on 27.04.2020.
//  Copyright © 2020 Mad devs. All rights reserved.
//

import UIKit
import MLMSensor

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.

        window = UIWindow(frame: UIScreen.main.bounds)
        window?.rootViewController = MLMViewController()
        window?.makeKeyAndVisible()

        return true
    }
}
