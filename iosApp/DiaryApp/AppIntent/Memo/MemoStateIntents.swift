import AppIntents
import DiaryIos

struct FinishMemoIntent: AppIntent {
    static let title = LocalizedStringResource("Finish Memo", table: "AppIntentMemo")
    static let description = IntentDescription(LocalizedStringResource("Marks a memo as finished.", table: "AppIntentMemo"))

    @Parameter(title: LocalizedStringResource("Memo", table: "AppIntentMemo"))
    var memo: MemoEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Finish \(\.$memo)", table: "AppIntentMemo")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        let changedCount = try await appAction { try await DiaryMemoAppAction.shared.finish(memoId: memo.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Finished the memo.", table: "AppIntentMemo"))
        )
    }
}

struct RestartMemoIntent: AppIntent {
    static let title = LocalizedStringResource("Restart Memo", table: "AppIntentMemo")
    static let description = IntentDescription(LocalizedStringResource("Marks a finished memo as not finished.", table: "AppIntentMemo"))

    @Parameter(title: LocalizedStringResource("Memo", table: "AppIntentMemo"))
    var memo: MemoEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Restart \(\.$memo)", table: "AppIntentMemo")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        let changedCount = try await appAction { try await DiaryMemoAppAction.shared.restart(memoId: memo.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Restarted the memo.", table: "AppIntentMemo"))
        )
    }
}

struct DeleteMemoIntent: AppIntent {
    static let title = LocalizedStringResource("Delete Memo", table: "AppIntentMemo")
    static let description = IntentDescription(
        LocalizedStringResource("Deletes a memo. Run Restore Deleted Memo with the same memo to undo.", table: "AppIntentMemo")
    )

    @Parameter(title: LocalizedStringResource("Memo", table: "AppIntentMemo"))
    var memo: MemoEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Delete \(\.$memo)", table: "AppIntentMemo")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        let changedCount = try await appAction { try await DiaryMemoAppAction.shared.delete(memoId: memo.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Deleted the memo.", table: "AppIntentMemo"))
        )
    }
}

struct RestoreMemoIntent: AppIntent {
    static let title = LocalizedStringResource("Restore Deleted Memo", table: "AppIntentMemo")
    static let description = IntentDescription(LocalizedStringResource("Restores a deleted memo.", table: "AppIntentMemo"))

    @Parameter(title: LocalizedStringResource("Memo to Restore", table: "AppIntentMemo"))
    var memo: MemoEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Restore \(\.$memo)", table: "AppIntentMemo")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        let changedCount = try await appAction { try await DiaryMemoAppAction.shared.restore(memoId: memo.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Restored the memo.", table: "AppIntentMemo"))
        )
    }
}
