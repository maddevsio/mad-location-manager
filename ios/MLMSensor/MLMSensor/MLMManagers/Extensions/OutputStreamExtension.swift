//
//  OutputStreamExtension.swift
//  MLM
//
//  Created by Haze on 30.04.2020.
//  Copyright © 2020 Mad devs. All rights reserved.
//

import Foundation

extension OutputStream {
  internal func write(data: Data) -> Int {
    return data.withUnsafeBytes {
      write($0.bindMemory(to: UInt8.self).baseAddress!, maxLength: data.count)
    }
  }
}
