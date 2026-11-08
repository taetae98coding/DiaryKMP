import AppIntents
import DiaryIos

enum MemoSortAppEnum: String, AppEnum {
    case `default`
    case title
    case recentlyUpdated

    static let typeDisplayRepresentation = TypeDisplayRepresentation(name: LocalizedStringResource("Sort", table: "AppIntentCore"))
    static let caseDisplayRepresentations: [MemoSortAppEnum: DisplayRepresentation] = [
        .default: DisplayRepresentation(title: LocalizedStringResource("Default", table: "AppIntentSort")),
        .title: DisplayRepresentation(title: LocalizedStringResource("Title", table: "AppIntentSort")),
        .recentlyUpdated: DisplayRepresentation(title: LocalizedStringResource("Recently updated", table: "AppIntentSort")),
    ]

    nonisolated var kotlin: DiaryAppActionSort {
        switch self {
        case .default: .DEFAULT
        case .title: .TITLE
        case .recentlyUpdated: .RECENTLY_UPDATED
        }
    }
}
