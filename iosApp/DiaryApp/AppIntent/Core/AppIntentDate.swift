import Foundation

nonisolated extension Date {
    init(epochMillis: Int64) {
        self.init(timeIntervalSince1970: TimeInterval(epochMillis) / 1000)
    }

    var epochMillis: Int64 {
        Int64((timeIntervalSince1970 * 1000).rounded(.down))
    }
}

nonisolated enum AppIntentPeriod {
    // DateIntervalFormatter는 시작과 끝이 같은 날이면 날짜를 한 번만 쓴다.
    static func text(allDay: Bool, start: Date, end: Date) -> String {
        let formatter = DateIntervalFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = allDay ? .none : .short
        return formatter.string(from: start, to: end)
    }
}
