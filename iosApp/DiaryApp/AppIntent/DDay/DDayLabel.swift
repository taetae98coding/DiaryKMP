import Foundation

// 카드 문구와 캘린더 디데이의 날짜 문구는 앱 화면과 같은 표기를 쓰며, 숫자에 자리 구분 기호를 넣지 않는다.
nonisolated enum DDayLabel {
    static let dDay = "D-Day"

    static func card(status: DDayStatusAppEnum, remainingDays: Int?, passedDays: Int?, years: Int?, months: Int?) -> String {
        switch status {
        case .today:
            return dDay
        case .upcoming:
            return withPeriod("D-\(remainingDays ?? 0)", years: years, months: months)
        case .passed:
            return withPeriod(days(passedDays ?? 0), years: years, months: months)
        }
    }

    static func days(_ count: Int) -> String {
        String(localized: LocalizedStringResource("\(count) days", table: "AppIntentDDay"))
    }

    static func months(_ count: Int) -> String {
        String(localized: LocalizedStringResource("\(count) months", table: "AppIntentDDay"))
    }

    static func anniversaryYears(_ count: Int) -> String {
        String(localized: LocalizedStringResource("\(count)-year anniversary", table: "AppIntentDDay"))
    }

    private static func withPeriod(_ text: String, years: Int?, months: Int?) -> String {
        var periodList: [String] = []
        if let years, years > 0 {
            periodList.append(String(localized: LocalizedStringResource("\(years) years", table: "AppIntentDDay")))
        }
        if let months, months > 0 {
            periodList.append(self.months(months))
        }
        if periodList.isEmpty {
            return text
        }
        let period = periodList.joined(separator: " ")
        return String(localized: LocalizedStringResource("\(text) (\(period))", table: "AppIntentDDay"))
    }
}
