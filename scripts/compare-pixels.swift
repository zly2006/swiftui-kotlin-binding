import AppKit
import Foundation
import CryptoKit

func fail(_ message: String) -> Never {
    FileHandle.standardError.write(Data((message + "\n").utf8)); exit(2)
}
let args = CommandLine.arguments
 guard args.count >= 3 else { fail("Usage: compare-pixels reference.png candidate.png [report.json]") }
func decode(_ file: String) -> (Data, NSBitmapImageRep) {
    guard let data = try? Data(contentsOf: URL(fileURLWithPath: file)), let image = NSBitmapImageRep(data: data) else { fail("Cannot decode PNG: \(file)") }
    guard !image.isPlanar, image.bitsPerSample == 8, image.bitmapData != nil else { fail("Unsupported pixel representation: \(file)") }
    return (data, image)
}
let (referenceData, reference) = decode(args[1])
let (candidateData, candidate) = decode(args[2])
 guard reference.pixelsWide == candidate.pixelsWide, reference.pixelsHigh == candidate.pixelsHigh else { fail("Image dimensions differ") }
 guard reference.samplesPerPixel == candidate.samplesPerPixel, reference.bitsPerPixel == candidate.bitsPerPixel, reference.bitmapFormat == candidate.bitmapFormat, reference.colorSpaceName == candidate.colorSpaceName, reference.colorSpace.iccProfileData == candidate.colorSpace.iccProfileData else { fail("Pixel format or color profile differs; normalization is not permitted") }
let width = reference.pixelsWide, height = reference.pixelsHigh, channels = reference.samplesPerPixel
let referencePixels = reference.bitmapData!, candidatePixels = candidate.bitmapData!
var different = 0, maximumDifference = 0
var minX = width, minY = height, maxX = -1, maxY = -1
for y in 0..<height {
    for x in 0..<width {
        var changed = false
        for channel in 0..<channels {
            let a = Int(referencePixels[y * reference.bytesPerRow + x * channels + channel])
            let b = Int(candidatePixels[y * candidate.bytesPerRow + x * channels + channel])
            let difference = abs(a - b)
            maximumDifference = max(maximumDifference, difference)
            changed = changed || difference != 0
        }
        if changed { different += 1; minX = min(minX, x); minY = min(minY, y); maxX = max(maxX, x); maxY = max(maxY, y) }
    }
}
let result: [String: Any] = [
    "width": width, "height": height, "channels": channels,
    "differentPixels": different, "totalPixels": width * height,
    "maxChannelDifference": maximumDifference, "exactMatch": different == 0,
    "differenceBounds": different == 0 ? [] : [minX, minY, maxX + 1, maxY + 1],
    "referenceSHA256": SHA256.hash(data: referenceData).map { String(format: "%02x", $0) }.joined(),
    "candidateSHA256": SHA256.hash(data: candidateData).map { String(format: "%02x", $0) }.joined(),
]
let report = try JSONSerialization.data(withJSONObject: result, options: [.prettyPrinted, .sortedKeys])
if args.count > 3 { try report.write(to: URL(fileURLWithPath: args[3])) }
FileHandle.standardOutput.write(report); print("")
exit(different == 0 ? 0 : 1)
