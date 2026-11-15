import AppIntents
import DiaryIos

struct ChecklistListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Checklists", table: "AppIntentChecklist")
    static let description = IntentDescription(
        LocalizedStringResource("Gets checklists by title, with how many items are checked.", table: "AppIntentChecklist")
    )

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Get Checklists", table: "AppIntentChecklist") {
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[ChecklistEntity]> & ProvidesDialog {
        let checklistPage = try await appAction {
            try await DiaryChecklistAppAction.shared.list(pageNumber: AppIntentPage.number(page))
        }
        return .result(
            value: checklistPage.items.map(ChecklistEntity.init(summary:)),
            dialog: AppIntentResultDialog.list(
                count: checklistPage.items.count,
                nextPageNumber: checklistPage.nextPageNumber,
                found: { count in LocalizedStringResource("Found \(count) checklists.", table: "AppIntentChecklist") },
                foundWithNextPage: { count, nextPage in
                    LocalizedStringResource("Found \(count) checklists. There's more on page \(nextPage).", table: "AppIntentChecklist")
                }
            )
        )
    }
}

struct ChecklistDetailIntent: AppIntent {
    static let title = LocalizedStringResource("Get Checklist Details", table: "AppIntentChecklist")
    static let description = IntentDescription(LocalizedStringResource("Gets a checklist with all its items and linked tags.", table: "AppIntentChecklist"))

    @Parameter(title: LocalizedStringResource("Checklist", table: "AppIntentChecklist"))
    var checklist: ChecklistEntity

    @Parameter(title: LocalizedStringResource("Item Sort", table: "AppIntentCore"))
    var itemSort: ChecklistItemSortAppEnum?

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$checklist) Details", table: "AppIntentChecklist") {
            \.$itemSort
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<ChecklistEntity> {
        .result(value: try await ChecklistEntityQuery.require(checklistId: checklist.id, itemSort: itemSort?.kotlin))
    }
}

struct AddChecklistIntent: AppIntent {
    static let title = LocalizedStringResource("Add Checklist", table: "AppIntentChecklist")
    static let description = IntentDescription(
        LocalizedStringResource("Adds a new checklist. You can add items too; empty items are skipped.", table: "AppIntentChecklist")
    )

    @Parameter(
        title: LocalizedStringResource("Title", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, leave it empty to keep the current title.", table: "AppIntentCore"),
        requestValueDialog: IntentDialog(LocalizedStringResource("What's the checklist title?", table: "AppIntentChecklist"))
    )
    var title: String

    @Parameter(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var checklistDescription: String?

    @Parameter(
        title: LocalizedStringResource("Items", table: "AppIntentCore"),
        description: LocalizedStringResource("Added in the order given.", table: "AppIntentCore")
    )
    var items: [String]?

    @Parameter(title: LocalizedStringResource("Tags to Link", table: "AppIntentCore"))
    var tags: [TagEntity]?

    static var parameterSummary: some ParameterSummary {
        Summary("Add checklist \(\.$title)", table: "AppIntentChecklist") {
            \.$checklistDescription
            \.$items
            \.$tags
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<ChecklistEntity> & ProvidesDialog {
        let checklistId = try await appAction {
            try await DiaryChecklistAppAction.shared.add(
                title: title,
                checklistDescription: checklistDescription,
                items: items ?? [],
                tagIds: (tags ?? []).map(\.id)
            )
        }
        return .result(
            value: try await ChecklistEntityQuery.require(checklistId: checklistId),
            dialog: IntentDialog(LocalizedStringResource("Added the checklist.", table: "AppIntentChecklist"))
        )
    }
}

struct EditChecklistIntent: AppIntent {
    static let title = LocalizedStringResource("Edit Checklist", table: "AppIntentChecklist")
    static let description = IntentDescription(
        LocalizedStringResource("Changes the title or description of a checklist. Values left empty stay the same.", table: "AppIntentChecklist")
    )

    @Parameter(title: LocalizedStringResource("Checklist", table: "AppIntentChecklist"))
    var checklist: ChecklistEntity

    @Parameter(
        title: LocalizedStringResource("Title", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, leave it empty to keep the current title.", table: "AppIntentCore")
    )
    var title: String?

    @Parameter(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var checklistDescription: String?

    static var parameterSummary: some ParameterSummary {
        Summary("Edit \(\.$checklist)", table: "AppIntentChecklist") {
            \.$title
            \.$checklistDescription
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await ChecklistEntityQuery.require(checklistId: checklist.id)
        let changedCount = try await appAction {
            try await DiaryChecklistAppAction.shared.update(checklistId: checklist.id, title: title, checklistDescription: checklistDescription)
        }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Updated the checklist.", table: "AppIntentChecklist"))
        )
    }
}

struct ResetChecklistIntent: AppIntent {
    static let title = LocalizedStringResource("Reset Checklist", table: "AppIntentChecklist")
    static let description = IntentDescription(
        LocalizedStringResource("Unchecks all items of a checklist at once. The items stay.", table: "AppIntentChecklist")
    )

    @Parameter(title: LocalizedStringResource("Checklist", table: "AppIntentChecklist"))
    var checklist: ChecklistEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Reset \(\.$checklist)", table: "AppIntentChecklist")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await ChecklistEntityQuery.require(checklistId: checklist.id)
        let changedCount = try await appAction { try await DiaryChecklistAppAction.shared.reset(checklistId: checklist.id) }
        let count = Int(changedCount)
        return .result(
            value: count,
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Unchecked \(count) items.", table: "AppIntentChecklist"))
        )
    }
}

struct DeleteChecklistIntent: AppIntent {
    static let title = LocalizedStringResource("Delete Checklist", table: "AppIntentChecklist")
    static let description = IntentDescription(
        LocalizedStringResource("Deletes a checklist. Run Restore Deleted Checklist with the same checklist to undo.", table: "AppIntentChecklist")
    )

    @Parameter(title: LocalizedStringResource("Checklist", table: "AppIntentChecklist"))
    var checklist: ChecklistEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Delete \(\.$checklist)", table: "AppIntentChecklist")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await ChecklistEntityQuery.require(checklistId: checklist.id)
        let changedCount = try await appAction { try await DiaryChecklistAppAction.shared.delete(checklistId: checklist.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Deleted the checklist.", table: "AppIntentChecklist"))
        )
    }
}

struct RestoreChecklistIntent: AppIntent {
    static let title = LocalizedStringResource("Restore Deleted Checklist", table: "AppIntentChecklist")
    static let description = IntentDescription(LocalizedStringResource("Restores a deleted checklist.", table: "AppIntentChecklist"))

    @Parameter(title: LocalizedStringResource("Checklist to Restore", table: "AppIntentChecklist"))
    var checklist: ChecklistEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Restore \(\.$checklist)", table: "AppIntentChecklist")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        let changedCount = try await appAction { try await DiaryChecklistAppAction.shared.restore(checklistId: checklist.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Restored the checklist.", table: "AppIntentChecklist"))
        )
    }
}

struct LinkTagToChecklistIntent: AppIntent {
    static let title = LocalizedStringResource("Link Tag to Checklist", table: "AppIntentChecklist")
    static let description = IntentDescription(LocalizedStringResource("Links a tag to a checklist.", table: "AppIntentChecklist"))

    @Parameter(title: LocalizedStringResource("Checklist", table: "AppIntentChecklist"))
    var checklist: ChecklistEntity

    @Parameter(title: LocalizedStringResource("Tag to Link", table: "AppIntentChecklist"))
    var tag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Link \(\.$tag) to \(\.$checklist)", table: "AppIntentChecklist")
    }

    func perform() async throws -> some IntentResult & ProvidesDialog {
        _ = try await ChecklistEntityQuery.require(checklistId: checklist.id)
        _ = try await TagEntityQuery.require(tagId: tag.id)
        _ = try await appAction { try await DiaryEntityTagAppAction.shared.add(kind: .CHECKLIST, entityId: checklist.id, tagId: tag.id) }
        return .result(dialog: IntentDialog(LocalizedStringResource("Linked.", table: "AppIntentCore")))
    }
}

struct UnlinkTagFromChecklistIntent: AppIntent {
    static let title = LocalizedStringResource("Unlink Tag from Checklist", table: "AppIntentChecklist")
    static let description = IntentDescription(LocalizedStringResource("Unlinks a tag from a checklist.", table: "AppIntentChecklist"))

    @Parameter(title: LocalizedStringResource("Checklist", table: "AppIntentChecklist"))
    var checklist: ChecklistEntity

    @Parameter(title: LocalizedStringResource("Tag to Unlink", table: "AppIntentChecklist"))
    var tag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Unlink \(\.$tag) from \(\.$checklist)", table: "AppIntentChecklist")
    }

    func perform() async throws -> some IntentResult & ProvidesDialog {
        _ = try await ChecklistEntityQuery.require(checklistId: checklist.id)
        _ = try await TagEntityQuery.require(tagId: tag.id)
        _ = try await appAction { try await DiaryEntityTagAppAction.shared.remove(kind: .CHECKLIST, entityId: checklist.id, tagId: tag.id) }
        return .result(dialog: IntentDialog(LocalizedStringResource("Unlinked.", table: "AppIntentCore")))
    }
}
