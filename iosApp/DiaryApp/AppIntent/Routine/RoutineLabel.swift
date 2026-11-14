import Foundation

// 카드 문구와 양 문구는 앱 화면과 같은 표기를 쓰며, 숫자에 자리 구분 기호를 넣지 않는다.
nonisolated enum RoutineLabel {
    static func card(isFinished: Bool, status: RoutineStatusAppEnum, cycle: RoutineCycleAppEnum, unit: String, cycleAmount: Int?, goalAmount: Int?) -> String {
        if isFinished {
            return String(localized: LocalizedStringResource("Finished", table: "AppIntentRoutine"))
        }
        switch status {
        case .notStarted:
            return String(localized: LocalizedStringResource("Not started", table: "AppIntentRoutine"))
        case .inProgress:
            let total = cycleAmount ?? 0
            if let goalAmount {
                return "\(cycleName(cycle)) \(String(total)) / \(amount(goalAmount, unit: unit))"
            }
            return "\(cycleName(cycle)) \(amount(total, unit: unit))"
        }
    }

    static func cardWithRate(card: String, achievementRate: Int?) -> String {
        guard let achievementRate else { return card }
        return "\(card) · \(String(achievementRate))%"
    }

    static func amount(_ value: Int, unit: String) -> String {
        "\(String(value))\(unit)"
    }

    private static func cycleName(_ cycle: RoutineCycleAppEnum) -> String {
        switch cycle {
        case .daily, .weekdays, .weekends:
            return String(localized: LocalizedStringResource("Today", table: "AppIntentRoutine"))
        case .weekly:
            return String(localized: LocalizedStringResource("This week", table: "AppIntentRoutine"))
        case .monthly:
            return String(localized: LocalizedStringResource("This month", table: "AppIntentRoutine"))
        }
    }
}
