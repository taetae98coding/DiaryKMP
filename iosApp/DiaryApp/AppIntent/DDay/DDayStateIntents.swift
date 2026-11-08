import AppIntents
import DiaryIos

struct DeleteDDayIntent: AppIntent {
    static let title = LocalizedStringResource("Delete D-Day", table: "AppIntentDDay")
    static let description = IntentDescription(
        LocalizedStringResource("Deletes a D-Day. Run Restore Deleted D-Day with the same D-Day to undo.", table: "AppIntentDDay")
    )

    @Parameter(title: LocalizedStringResource("D-Day", table: "AppIntentDDay"))
    var dday: DDayEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Delete \(\.$dday)", table: "AppIntentDDay")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        let changedCount = try await appAction { try await DiaryDDayAppAction.shared.delete(ddayId: dday.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Deleted the D-Day.", table: "AppIntentDDay"))
        )
    }
}

struct RestoreDDayIntent: AppIntent {
    static let title = LocalizedStringResource("Restore Deleted D-Day", table: "AppIntentDDay")
    static let description = IntentDescription(LocalizedStringResource("Restores a deleted D-Day.", table: "AppIntentDDay"))

    @Parameter(title: LocalizedStringResource("D-Day to Restore", table: "AppIntentDDay"))
    var dday: DDayEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Restore \(\.$dday)", table: "AppIntentDDay")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        let changedCount = try await appAction { try await DiaryDDayAppAction.shared.restore(ddayId: dday.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Restored the D-Day.", table: "AppIntentDDay"))
        )
    }
}

struct LinkTagToDDayIntent: AppIntent {
    static let title = LocalizedStringResource("Link Tag to D-Day", table: "AppIntentDDay")
    static let description = IntentDescription(LocalizedStringResource("Links a tag to a D-Day.", table: "AppIntentDDay"))

    @Parameter(title: LocalizedStringResource("D-Day", table: "AppIntentDDay"))
    var dday: DDayEntity

    @Parameter(title: LocalizedStringResource("Tag to Link", table: "AppIntentDDay"))
    var tag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Link \(\.$tag) to \(\.$dday)", table: "AppIntentDDay")
    }

    func perform() async throws -> some IntentResult & ProvidesDialog {
        _ = try await appAction { try await DiaryDDayAppAction.shared.tagAdd(ddayId: dday.id, tagId: tag.id) }
        return .result(dialog: IntentDialog(LocalizedStringResource("Linked.", table: "AppIntentCore")))
    }
}

struct UnlinkTagFromDDayIntent: AppIntent {
    static let title = LocalizedStringResource("Unlink Tag from D-Day", table: "AppIntentDDay")
    static let description = IntentDescription(LocalizedStringResource("Unlinks a tag from a D-Day.", table: "AppIntentDDay"))

    @Parameter(title: LocalizedStringResource("D-Day", table: "AppIntentDDay"))
    var dday: DDayEntity

    @Parameter(title: LocalizedStringResource("Tag to Unlink", table: "AppIntentDDay"))
    var tag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Unlink \(\.$tag) from \(\.$dday)", table: "AppIntentDDay")
    }

    func perform() async throws -> some IntentResult & ProvidesDialog {
        _ = try await appAction { try await DiaryDDayAppAction.shared.tagRemove(ddayId: dday.id, tagId: tag.id) }
        return .result(dialog: IntentDialog(LocalizedStringResource("Unlinked.", table: "AppIntentCore")))
    }
}
