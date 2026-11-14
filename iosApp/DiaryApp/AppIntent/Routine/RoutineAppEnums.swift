import AppIntents
import DiaryIos

enum RoutineStatusAppEnum: String, AppEnum {
    case notStarted
    case inProgress

    static let typeDisplayRepresentation = TypeDisplayRepresentation(name: LocalizedStringResource("Status", table: "AppIntentRoutine"))
    static let caseDisplayRepresentations: [RoutineStatusAppEnum: DisplayRepresentation] = [
        .notStarted: DisplayRepresentation(title: LocalizedStringResource("Not started", table: "AppIntentRoutine")),
        .inProgress: DisplayRepresentation(title: LocalizedStringResource("In progress", table: "AppIntentRoutine")),
    ]

    nonisolated init(kotlin: DiaryAppActionRoutineStatus) {
        switch kotlin {
        case .NOT_STARTED: self = .notStarted
        case .IN_PROGRESS: self = .inProgress
        }
    }
}

enum RoutineCycleAppEnum: String, AppEnum {
    case daily
    case weekdays
    case weekends
    case weekly
    case monthly

    static let typeDisplayRepresentation = TypeDisplayRepresentation(name: LocalizedStringResource("Repeat", table: "AppIntentRoutine"))
    static let caseDisplayRepresentations: [RoutineCycleAppEnum: DisplayRepresentation] = [
        .daily: DisplayRepresentation(title: LocalizedStringResource("Daily", table: "AppIntentRoutine")),
        .weekdays: DisplayRepresentation(title: LocalizedStringResource("Weekdays only", table: "AppIntentRoutine")),
        .weekends: DisplayRepresentation(title: LocalizedStringResource("Weekends only", table: "AppIntentRoutine")),
        .weekly: DisplayRepresentation(title: LocalizedStringResource("Weekly", table: "AppIntentRoutine")),
        .monthly: DisplayRepresentation(title: LocalizedStringResource("Monthly", table: "AppIntentRoutine")),
    ]

    nonisolated init(kotlin: DiaryAppActionRoutineCycle) {
        switch kotlin {
        case .DAILY: self = .daily
        case .WEEKDAYS: self = .weekdays
        case .WEEKENDS: self = .weekends
        case .WEEKLY: self = .weekly
        case .MONTHLY: self = .monthly
        }
    }

    nonisolated var kotlin: DiaryAppActionRoutineCycle {
        switch self {
        case .daily: .DAILY
        case .weekdays: .WEEKDAYS
        case .weekends: .WEEKENDS
        case .weekly: .WEEKLY
        case .monthly: .MONTHLY
        }
    }
}
