//
//  MLMFileManager.swift
//  MLM
//
//  Created by Haze on 28.04.2020.
//  Copyright © 2020 Mad devs. All rights reserved.
//

import Foundation

class MLMFileManager {

    internal let gpsQueue = DispatchQueue(label: "mlm.write.gps.queue", qos: .background, attributes: .concurrent, autoreleaseFrequency: .inherit, target: .global(qos: .default))
    internal let accQueue = DispatchQueue(label: "mlm.write.acc.queue", qos: .background, attributes: .concurrent, autoreleaseFrequency: .inherit, target: .global(qos: .default))
    internal let gyroQueue = DispatchQueue(label: "mlm.write.gyro.queue", qos: .background, attributes: .concurrent, autoreleaseFrequency: .inherit, target: .global(qos: .default))
    internal let magQueue = DispatchQueue(label: "mlm.write.mag.queue", qos: .background, attributes: .concurrent, autoreleaseFrequency: .inherit, target: .global(qos: .default))
    internal let barQeueue = DispatchQueue(label: "mlm.write.bar.queue", qos: .background, attributes: .concurrent, autoreleaseFrequency: .inherit, target: .global(qos: .default))
    internal let speedQueue = DispatchQueue(label: "mlm.write.speed.queue", qos: .background, attributes: .concurrent, autoreleaseFrequency: .inherit, target: .global(qos: .default))

    fileprivate let folderPath = "MLMFolder"
    fileprivate let folderTempDirectory = FileManager.default.temporaryDirectory
    fileprivate let fileName: String = "MLMFile.txt"

    fileprivate var folderUrl: URL {
        return folderTempDirectory.appendingPathComponent(folderPath)
    }

    internal init() {
        removeFile()
        setupFolder()
    }

    deinit {
        removeFile()
        print("DEINIT - MLMFileManager")
    }

    internal var fileUrl: URL {
        return folderUrl.appendingPathComponent(fileName)
    }

    fileprivate func setupFolder() {
        guard !FileManager.default.fileExists(atPath: folderUrl.path) else { return }

        do {
            try FileManager.default.createDirectory(at: folderUrl, withIntermediateDirectories: true, attributes: nil)
        } catch {
            print("setup folder error \(error)")
        }
    }

    internal func removeFile() {
        do {
            try FileManager.default.removeItem(at: fileUrl)
        } catch {
            print("remove file error \(error)")
        }
    }
}

// MARK: Write
extension MLMFileManager {
    internal func writeQueueText(_ text: String, queue: DispatchQueue) {
        queue.sync(flags: .barrier) {
            if !FileManager.default.fileExists(atPath: fileUrl.path) {
                writeAndCreate(text)
            } else {
                streamWrite(text)
            }
        }
    }

    fileprivate func writeAndCreate(_ text: String) {
        do {
            try text.write(to: fileUrl, atomically: true, encoding: .utf8)
        }
        catch {
            print("write and create file error \(error)")
        }
    }

    fileprivate func streamWrite(_ text: String) {
        if let streamOut = OutputStream(toFileAtPath: fileUrl.path, append: true) {
            let textData = text.data(using: .utf8)!
            streamOut.open()
            _ = streamOut.write(data: textData)
            print(text)
            streamOut.close()
        }
    }
}

// MARK: Read
extension MLMFileManager {
    internal func readText() -> String {
        var writedText = ""
        do {
            writedText = try String(contentsOf: fileUrl, encoding: .utf8)
        }
        catch {
            print("read text error \(error)")
        }
        return writedText
    }
}
