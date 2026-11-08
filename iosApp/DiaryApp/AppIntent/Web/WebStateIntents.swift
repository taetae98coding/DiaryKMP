import AppIntents
import DiaryIos

struct DeleteWebIntent: AppIntent {
    static let title = LocalizedStringResource("Delete Web", table: "AppIntentWeb")
    static let description = IntentDescription(
        LocalizedStringResource("Deletes a web. Links to memos stay. Run Restore Deleted Web with the same web to undo.", table: "AppIntentWeb")
    )

    @Parameter(title: LocalizedStringResource("Web", table: "AppIntentWeb"))
    var web: WebEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Delete \(\.$web)", table: "AppIntentWeb")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        let changedCount = try await appAction { try await DiaryWebAppAction.shared.delete(webId: web.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Deleted the web.", table: "AppIntentWeb"))
        )
    }
}

struct RestoreWebIntent: AppIntent {
    static let title = LocalizedStringResource("Restore Deleted Web", table: "AppIntentWeb")
    static let description = IntentDescription(LocalizedStringResource("Restores a deleted web.", table: "AppIntentWeb"))

    @Parameter(title: LocalizedStringResource("Web to Restore", table: "AppIntentWeb"))
    var web: WebEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Restore \(\.$web)", table: "AppIntentWeb")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        let changedCount = try await appAction { try await DiaryWebAppAction.shared.restore(webId: web.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Restored the web.", table: "AppIntentWeb"))
        )
    }
}

struct LinkTagToWebIntent: AppIntent {
    static let title = LocalizedStringResource("Link Tag to Web", table: "AppIntentWeb")
    static let description = IntentDescription(LocalizedStringResource("Links a tag to a web.", table: "AppIntentWeb"))

    @Parameter(title: LocalizedStringResource("Web", table: "AppIntentWeb"))
    var web: WebEntity

    @Parameter(title: LocalizedStringResource("Tag to Link", table: "AppIntentWeb"))
    var tag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Link \(\.$tag) to \(\.$web)", table: "AppIntentWeb")
    }

    func perform() async throws -> some IntentResult & ProvidesDialog {
        _ = try await appAction { try await DiaryWebAppAction.shared.tagAdd(webId: web.id, tagId: tag.id) }
        return .result(dialog: IntentDialog(LocalizedStringResource("Linked.", table: "AppIntentCore")))
    }
}

struct UnlinkTagFromWebIntent: AppIntent {
    static let title = LocalizedStringResource("Unlink Tag from Web", table: "AppIntentWeb")
    static let description = IntentDescription(LocalizedStringResource("Unlinks a tag from a web.", table: "AppIntentWeb"))

    @Parameter(title: LocalizedStringResource("Web", table: "AppIntentWeb"))
    var web: WebEntity

    @Parameter(title: LocalizedStringResource("Tag to Unlink", table: "AppIntentWeb"))
    var tag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Unlink \(\.$tag) from \(\.$web)", table: "AppIntentWeb")
    }

    func perform() async throws -> some IntentResult & ProvidesDialog {
        _ = try await appAction { try await DiaryWebAppAction.shared.tagRemove(webId: web.id, tagId: tag.id) }
        return .result(dialog: IntentDialog(LocalizedStringResource("Unlinked.", table: "AppIntentCore")))
    }
}
