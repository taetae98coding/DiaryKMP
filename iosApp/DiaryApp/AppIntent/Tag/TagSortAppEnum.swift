import AppIntents
import DiaryIos

enum TagSortAppEnum: String, AppEnum {
    case title
    case recentlyUpdated

    static let typeDisplayRepresentation = TypeDisplayRepresentation(name: LocalizedStringResource("Sort", table: "AppIntentCore"))
    static let caseDisplayRepresentations: [TagSortAppEnum: DisplayRepresentation] = [
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

enum TagScopeAppEnum: String, AppEnum {
    case this
    case child
    case descendant

    static let typeDisplayRepresentation = TypeDisplayRepresentation(name: LocalizedStringResource("Tags to Include", table: "AppIntentCore"))
    static let caseDisplayRepresentations: [TagScopeAppEnum: DisplayRepresentation] = [
        .this: DisplayRepresentation(title: LocalizedStringResource("This tag only", table: "AppIntentTag")),
        .child: DisplayRepresentation(title: LocalizedStringResource("Directly linked tags", table: "AppIntentTag")),
        .descendant: DisplayRepresentation(title: LocalizedStringResource("All linked tags", table: "AppIntentTag")),
    ]

    nonisolated var kotlin: DiaryAppActionTagScope {
        switch self {
        case .this: .SELF
        case .child: .CHILD
        case .descendant: .DESCENDANT
        }
    }
}
