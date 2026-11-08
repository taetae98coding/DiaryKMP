import AppIntents
import UIKit

nonisolated enum AppIntentItemImage {
    // 항목 자체의 색이 없는 항목은 Spotlight와 같은 앱의 주 색으로 칠한다.
    static let primaryHexColor = "#6750A4"

    static func symbol(_ systemName: String, hexColor: String? = nil) -> DisplayRepresentation.Image? {
        DisplayRepresentation.Image(systemName: systemName, tintColor: hexColor.flatMap(UIColor.init(appIntentHex:)))
    }
}

nonisolated extension UIColor {
    convenience init?(appIntentHex hex: String) {
        let value = hex.hasPrefix("#") ? String(hex.dropFirst()) : hex
        guard value.count == 6, let rgb = UInt32(value, radix: 16) else { return nil }
        self.init(
            red: CGFloat((rgb >> 16) & 0xFF) / 255,
            green: CGFloat((rgb >> 8) & 0xFF) / 255,
            blue: CGFloat(rgb & 0xFF) / 255,
            alpha: 1
        )
    }
}
