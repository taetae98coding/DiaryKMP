import AppIntents
import DiaryIos

enum ContactSortAppEnum: String, AppEnum {
    case name
    case recentlyUpdated

    static let typeDisplayRepresentation = TypeDisplayRepresentation(name: LocalizedStringResource("Sort", table: "AppIntentCore"))
    static let caseDisplayRepresentations: [ContactSortAppEnum: DisplayRepresentation] = [
        .name: DisplayRepresentation(title: LocalizedStringResource("Name", table: "AppIntentSort")),
        .recentlyUpdated: DisplayRepresentation(title: LocalizedStringResource("Recently updated", table: "AppIntentSort")),
    ]

    nonisolated var kotlin: DiaryAppActionSort {
        switch self {
        case .name: .NAME
        case .recentlyUpdated: .RECENTLY_UPDATED
        }
    }
}

enum ContactBirthdayCalendarAppEnum: String, AppEnum {
    case solar
    case lunar

    static let typeDisplayRepresentation = TypeDisplayRepresentation(name: LocalizedStringResource("Birthday Calendar", table: "AppIntentCore"))
    static let caseDisplayRepresentations: [ContactBirthdayCalendarAppEnum: DisplayRepresentation] = [
        .solar: DisplayRepresentation(title: LocalizedStringResource("Solar", table: "AppIntentContact")),
        .lunar: DisplayRepresentation(title: LocalizedStringResource("Lunar", table: "AppIntentContact")),
    ]

    nonisolated init(kotlin: DiaryAppActionContactBirthdayCalendar) {
        switch kotlin {
        case .SOLAR: self = .solar
        case .LUNAR: self = .lunar
        }
    }

    nonisolated var kotlin: DiaryAppActionContactBirthdayCalendar {
        switch self {
        case .solar: .SOLAR
        case .lunar: .LUNAR
        }
    }
}
