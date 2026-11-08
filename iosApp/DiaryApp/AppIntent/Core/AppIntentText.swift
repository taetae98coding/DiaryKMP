import Foundation

nonisolated extension String {
    var appIntentNonEmpty: String? {
        isEmpty ? nil : self
    }
}
