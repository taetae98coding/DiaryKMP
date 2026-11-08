import AppIntents
import DiaryIos

enum ChecklistItemSortAppEnum: String, AppEnum {
    case added
    case uncheckedFirst

    static let typeDisplayRepresentation = TypeDisplayRepresentation(name: LocalizedStringResource("Item Sort", table: "AppIntentCore"))
    static let caseDisplayRepresentations: [ChecklistItemSortAppEnum: DisplayRepresentation] = [
        .added: DisplayRepresentation(title: LocalizedStringResource("Added order", table: "AppIntentChecklist")),
        .uncheckedFirst: DisplayRepresentation(title: LocalizedStringResource("Unchecked first", table: "AppIntentChecklist")),
    ]

    nonisolated var kotlin: DiaryAppActionChecklistItemSort {
        switch self {
        case .added: .ADDED
        case .uncheckedFirst: .UNCHECKED_FIRST
        }
    }
}
