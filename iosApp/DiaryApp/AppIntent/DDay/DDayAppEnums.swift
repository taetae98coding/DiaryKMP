import AppIntents
import DiaryIos

enum DDayStatusAppEnum: String, AppEnum {
    case upcoming
    case today
    case passed

    static let typeDisplayRepresentation = TypeDisplayRepresentation(name: LocalizedStringResource("Status", table: "AppIntentDDay"))
    static let caseDisplayRepresentations: [DDayStatusAppEnum: DisplayRepresentation] = [
        .upcoming: DisplayRepresentation(title: LocalizedStringResource("Upcoming", table: "AppIntentDDay")),
        .today: DisplayRepresentation(title: LocalizedStringResource("Today", table: "AppIntentDDay")),
        .passed: DisplayRepresentation(title: LocalizedStringResource("Passed", table: "AppIntentDDay")),
    ]

    nonisolated init(kotlin: DiaryAppActionDDayStatus) {
        switch kotlin {
        case .UPCOMING: self = .upcoming
        case .TODAY: self = .today
        case .PASSED: self = .passed
        }
    }
}

enum DDayOverflowPolicyAppEnum: String, AppEnum {
    case clampToMonthEnd
    case skip
    case nextMonthFirstDay

    static let typeDisplayRepresentation = TypeDisplayRepresentation(name: LocalizedStringResource("Missing Dates", table: "AppIntentDDay"))
    static let caseDisplayRepresentations: [DDayOverflowPolicyAppEnum: DisplayRepresentation] = [
        .clampToMonthEnd: DisplayRepresentation(title: LocalizedStringResource("Use last day of month", table: "AppIntentDDay")),
        .skip: DisplayRepresentation(title: LocalizedStringResource("Skip", table: "AppIntentDDay")),
        .nextMonthFirstDay: DisplayRepresentation(title: LocalizedStringResource("Use 1st of next month", table: "AppIntentDDay")),
    ]

    nonisolated init(kotlin: DiaryAppActionDDayOverflowPolicy) {
        switch kotlin {
        case .CLAMP_TO_MONTH_END: self = .clampToMonthEnd
        case .SKIP: self = .skip
        case .NEXT_MONTH_FIRST_DAY: self = .nextMonthFirstDay
        }
    }

    nonisolated var kotlin: DiaryAppActionDDayOverflowPolicy {
        switch self {
        case .clampToMonthEnd: .CLAMP_TO_MONTH_END
        case .skip: .SKIP
        case .nextMonthFirstDay: .NEXT_MONTH_FIRST_DAY
        }
    }
}
