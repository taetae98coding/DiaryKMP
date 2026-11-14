import AppIntents
import DiaryIos

struct FinishRoutineIntent: AppIntent {
    static let title = LocalizedStringResource("Finish Routine", table: "AppIntentRoutine")
    static let description = IntentDescription(LocalizedStringResource("Marks a routine as finished. Its records stay.", table: "AppIntentRoutine"))

    @Parameter(title: LocalizedStringResource("Routine", table: "AppIntentRoutine"))
    var routine: RoutineEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Finish \(\.$routine)", table: "AppIntentRoutine")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await RoutineEntityQuery.require(routineId: routine.id)
        let changedCount = try await appAction { try await DiaryRoutineAppAction.shared.finish(routineId: routine.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Finished the routine.", table: "AppIntentRoutine"))
        )
    }
}

struct RestartRoutineIntent: AppIntent {
    static let title = LocalizedStringResource("Restart Routine", table: "AppIntentRoutine")
    static let description = IntentDescription(LocalizedStringResource("Marks a finished routine as not finished.", table: "AppIntentRoutine"))

    @Parameter(title: LocalizedStringResource("Routine", table: "AppIntentRoutine"))
    var routine: RoutineEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Restart \(\.$routine)", table: "AppIntentRoutine")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await RoutineEntityQuery.require(routineId: routine.id)
        let changedCount = try await appAction { try await DiaryRoutineAppAction.shared.restart(routineId: routine.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Restarted the routine.", table: "AppIntentRoutine"))
        )
    }
}

struct DeleteRoutineIntent: AppIntent {
    static let title = LocalizedStringResource("Delete Routine", table: "AppIntentRoutine")
    static let description = IntentDescription(
        LocalizedStringResource(
            "Deletes a routine. Its records aren't removed. Run Restore Deleted Routine with the same routine to bring it back with its records.",
            table: "AppIntentRoutine"
        )
    )

    @Parameter(title: LocalizedStringResource("Routine", table: "AppIntentRoutine"))
    var routine: RoutineEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Delete \(\.$routine)", table: "AppIntentRoutine")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await RoutineEntityQuery.require(routineId: routine.id)
        let changedCount = try await appAction { try await DiaryRoutineAppAction.shared.delete(routineId: routine.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Deleted the routine.", table: "AppIntentRoutine"))
        )
    }
}

struct RestoreRoutineIntent: AppIntent {
    static let title = LocalizedStringResource("Restore Deleted Routine", table: "AppIntentRoutine")
    static let description = IntentDescription(LocalizedStringResource("Restores a deleted routine.", table: "AppIntentRoutine"))

    @Parameter(title: LocalizedStringResource("Routine to Restore", table: "AppIntentRoutine"))
    var routine: RoutineEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Restore \(\.$routine)", table: "AppIntentRoutine")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        let changedCount = try await appAction { try await DiaryRoutineAppAction.shared.restore(routineId: routine.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Restored the routine.", table: "AppIntentRoutine"))
        )
    }
}

struct LinkTagToRoutineIntent: AppIntent {
    static let title = LocalizedStringResource("Link Tag to Routine", table: "AppIntentRoutine")
    static let description = IntentDescription(LocalizedStringResource("Links a tag to a routine.", table: "AppIntentRoutine"))

    @Parameter(title: LocalizedStringResource("Routine", table: "AppIntentRoutine"))
    var routine: RoutineEntity

    @Parameter(title: LocalizedStringResource("Tag to Link", table: "AppIntentRoutine"))
    var tag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Link \(\.$tag) to \(\.$routine)", table: "AppIntentRoutine")
    }

    func perform() async throws -> some IntentResult & ProvidesDialog {
        _ = try await RoutineEntityQuery.require(routineId: routine.id)
        _ = try await TagEntityQuery.require(tagId: tag.id)
        _ = try await appAction { try await DiaryEntityTagAppAction.shared.add(kind: .ROUTINE, entityId: routine.id, tagId: tag.id) }
        return .result(dialog: IntentDialog(LocalizedStringResource("Linked.", table: "AppIntentCore")))
    }
}

struct UnlinkTagFromRoutineIntent: AppIntent {
    static let title = LocalizedStringResource("Unlink Tag from Routine", table: "AppIntentRoutine")
    static let description = IntentDescription(LocalizedStringResource("Unlinks a tag from a routine.", table: "AppIntentRoutine"))

    @Parameter(title: LocalizedStringResource("Routine", table: "AppIntentRoutine"))
    var routine: RoutineEntity

    @Parameter(title: LocalizedStringResource("Tag to Unlink", table: "AppIntentRoutine"))
    var tag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Unlink \(\.$tag) from \(\.$routine)", table: "AppIntentRoutine")
    }

    func perform() async throws -> some IntentResult & ProvidesDialog {
        _ = try await RoutineEntityQuery.require(routineId: routine.id)
        _ = try await TagEntityQuery.require(tagId: tag.id)
        _ = try await appAction { try await DiaryEntityTagAppAction.shared.remove(kind: .ROUTINE, entityId: routine.id, tagId: tag.id) }
        return .result(dialog: IntentDialog(LocalizedStringResource("Unlinked.", table: "AppIntentCore")))
    }
}
