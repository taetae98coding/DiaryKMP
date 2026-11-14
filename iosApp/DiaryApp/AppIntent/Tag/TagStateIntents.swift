import AppIntents
import DiaryIos

struct FinishTagIntent: AppIntent {
    static let title = LocalizedStringResource("Finish Tag", table: "AppIntentTag")
    static let description = IntentDescription(LocalizedStringResource("Marks a tag as finished.", table: "AppIntentTag"))

    @Parameter(title: LocalizedStringResource("Tag", table: "AppIntentTag"))
    var tag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Finish \(\.$tag)", table: "AppIntentTag")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await TagEntityQuery.require(tagId: tag.id)
        let changedCount = try await appAction { try await DiaryTagAppAction.shared.finish(tagId: tag.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Finished the tag.", table: "AppIntentTag"))
        )
    }
}

struct RestartTagIntent: AppIntent {
    static let title = LocalizedStringResource("Restart Tag", table: "AppIntentTag")
    static let description = IntentDescription(LocalizedStringResource("Marks a finished tag as not finished.", table: "AppIntentTag"))

    @Parameter(title: LocalizedStringResource("Tag", table: "AppIntentTag"))
    var tag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Restart \(\.$tag)", table: "AppIntentTag")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await TagEntityQuery.require(tagId: tag.id)
        let changedCount = try await appAction { try await DiaryTagAppAction.shared.restart(tagId: tag.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Restarted the tag.", table: "AppIntentTag"))
        )
    }
}

struct DeleteTagIntent: AppIntent {
    static let title = LocalizedStringResource("Delete Tag", table: "AppIntentTag")
    static let description = IntentDescription(
        LocalizedStringResource("Deletes a tag. Run Restore Deleted Tag with the same tag to undo.", table: "AppIntentTag")
    )

    @Parameter(title: LocalizedStringResource("Tag", table: "AppIntentTag"))
    var tag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Delete \(\.$tag)", table: "AppIntentTag")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await TagEntityQuery.require(tagId: tag.id)
        let changedCount = try await appAction { try await DiaryTagAppAction.shared.delete(tagId: tag.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Deleted the tag.", table: "AppIntentTag"))
        )
    }
}

struct RestoreTagIntent: AppIntent {
    static let title = LocalizedStringResource("Restore Deleted Tag", table: "AppIntentTag")
    static let description = IntentDescription(LocalizedStringResource("Restores a deleted tag.", table: "AppIntentTag"))

    @Parameter(title: LocalizedStringResource("Tag to Restore", table: "AppIntentTag"))
    var tag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Restore \(\.$tag)", table: "AppIntentTag")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        let changedCount = try await appAction { try await DiaryTagAppAction.shared.restore(tagId: tag.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Restored the tag.", table: "AppIntentTag"))
        )
    }
}
