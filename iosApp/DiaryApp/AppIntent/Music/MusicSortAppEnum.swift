import AppIntents
import DiaryIos

enum MusicSortAppEnum: String, AppEnum {
    case title
    case recentlyUpdated

    static let typeDisplayRepresentation = TypeDisplayRepresentation(name: LocalizedStringResource("Sort", table: "AppIntentCore"))
    static let caseDisplayRepresentations: [MusicSortAppEnum: DisplayRepresentation] = [
        .title: DisplayRepresentation(title: LocalizedStringResource("Title", table: "AppIntentSort")),
        .recentlyUpdated: DisplayRepresentation(title: LocalizedStringResource("Recently updated", table: "AppIntentSort")),
    ]

    nonisolated var kotlin: DiaryAppActionSort {
        switch self {
        case .title: .TITLE
        case .recentlyUpdated: .RECENTLY_UPDATED
        }
    }
}
