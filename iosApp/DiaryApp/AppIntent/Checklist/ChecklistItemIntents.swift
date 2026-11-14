import AppIntents
import DiaryIos

struct AddChecklistItemIntent: AppIntent {
    static let title = LocalizedStringResource("Add Checklist Item", table: "AppIntentChecklist")
    static let description = IntentDescription(
        LocalizedStringResource("Adds an unchecked item to the end of a checklist.", table: "AppIntentChecklist")
    )

    @Parameter(title: LocalizedStringResource("Checklist", table: "AppIntentChecklist"))
    var checklist: ChecklistEntity

    @Parameter(
        title: LocalizedStringResource("Content", table: "AppIntentCore"),
        description: LocalizedStringResource("One line of item content.", table: "AppIntentCore")
    )
    var content: String

    static var parameterSummary: some ParameterSummary {
        Summary("Add checklist item \(\.$checklist) \(\.$content)", table: "AppIntentChecklist")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await ChecklistEntityQuery.require(checklistId: checklist.id)
        let changedCount = try await appAction { try await DiaryChecklistItemAppAction.shared.add(checklistId: checklist.id, content: content) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Added the checklist item.", table: "AppIntentChecklist"))
        )
    }
}

struct CheckChecklistItemIntent: AppIntent {
    static let title = LocalizedStringResource("Check Checklist Item", table: "AppIntentChecklist")
    static let description = IntentDescription(LocalizedStringResource("Checks a checklist item.", table: "AppIntentChecklist"))

    @Parameter(title: LocalizedStringResource("Checklist Item", table: "AppIntentChecklist"))
    var item: ChecklistItemEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Check \(\.$item)", table: "AppIntentChecklist")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        try await ChecklistEntityQuery.requireChecklist(of: item)
        let changedCount = try await appAction { try await DiaryChecklistItemAppAction.shared.check(itemId: item.itemId) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Checked.", table: "AppIntentChecklist"))
        )
    }
}

struct UncheckChecklistItemIntent: AppIntent {
    static let title = LocalizedStringResource("Uncheck Checklist Item", table: "AppIntentChecklist")
    static let description = IntentDescription(LocalizedStringResource("Unchecks a checklist item.", table: "AppIntentChecklist"))

    @Parameter(title: LocalizedStringResource("Checklist Item", table: "AppIntentChecklist"))
    var item: ChecklistItemEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Uncheck \(\.$item)", table: "AppIntentChecklist")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        try await ChecklistEntityQuery.requireChecklist(of: item)
        let changedCount = try await appAction { try await DiaryChecklistItemAppAction.shared.uncheck(itemId: item.itemId) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Unchecked.", table: "AppIntentChecklist"))
        )
    }
}

struct DeleteChecklistItemIntent: AppIntent {
    static let title = LocalizedStringResource("Delete Checklist Item", table: "AppIntentChecklist")
    static let description = IntentDescription(
        LocalizedStringResource("Removes an item from its checklist. This can't be undone; add the content again if needed.", table: "AppIntentChecklist")
    )

    @Parameter(title: LocalizedStringResource("Checklist Item", table: "AppIntentChecklist"))
    var item: ChecklistItemEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Delete \(\.$item)", table: "AppIntentChecklist")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        try await ChecklistEntityQuery.requireChecklist(of: item)
        let changedCount = try await appAction { try await DiaryChecklistItemAppAction.shared.delete(itemId: item.itemId) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Deleted the checklist item.", table: "AppIntentChecklist"))
        )
    }
}
