import AppIntents
import DiaryIos

struct RoutineRecordListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Routine Records", table: "AppIntentRoutine")
    static let description = IntentDescription(LocalizedStringResource("Gets the records of a routine, latest date first.", table: "AppIntentRoutine"))

    @Parameter(title: LocalizedStringResource("Routine", table: "AppIntentRoutine"))
    var routine: RoutineEntity

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$routine) Records", table: "AppIntentRoutine") {
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[RoutineRecordEntity]> & ProvidesDialog {
        _ = try await RoutineEntityQuery.require(routineId: routine.id)
        let recordPage = try await appAction {
            try await DiaryRoutineRecordAppAction.shared.list(routineId: routine.id, pageNumber: AppIntentPage.number(page))
        }
        let dialog = AppIntentResultDialog.list(
            count: recordPage.items.count,
            nextPageNumber: recordPage.nextPageNumber,
            found: { count in LocalizedStringResource("Found \(count) routine records.", table: "AppIntentRoutine") },
            foundWithNextPage: { count, nextPage in
                LocalizedStringResource("Found \(count) routine records. There's more on page \(nextPage).", table: "AppIntentRoutine")
            }
        )
        return .result(value: recordPage.items.map(RoutineRecordEntity.init(record:)), dialog: dialog)
    }
}

struct AddRoutineRecordIntent: AppIntent {
    static let title = LocalizedStringResource("Add Routine Record", table: "AppIntentRoutine")
    static let description = IntentDescription(
        LocalizedStringResource(
            "Adds a record to a routine. Today is used if you don't choose a date, and the routine's amount per record if you don't enter an amount.",
            table: "AppIntentRoutine"
        )
    )

    @Parameter(title: LocalizedStringResource("Routine", table: "AppIntentRoutine"))
    var routine: RoutineEntity

    @Parameter(
        title: LocalizedStringResource("Date", table: "AppIntentRoutine"),
        description: LocalizedStringResource("Must be on or after the routine's start date. Leave it empty to use today.", table: "AppIntentRoutine"),
        kind: .date
    )
    var date: Date?

    @Parameter(
        title: LocalizedStringResource("Amount", table: "AppIntentRoutine"),
        description: LocalizedStringResource(
            "A whole number from 1 to 9999999. Leave it empty to use the routine's amount per record.",
            table: "AppIntentRoutine"
        )
    )
    var amount: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Add record to \(\.$routine)", table: "AppIntentRoutine") {
            \.$date
            \.$amount
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await RoutineEntityQuery.require(routineId: routine.id)
        let changedCount = try await appAction {
            try await DiaryRoutineRecordAppAction.shared.add(
                routineId: routine.id,
                dateEpochMillis: date?.epochMillis,
                amount: amount.map(Int32.init(clamping:))
            )
        }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Added the record.", table: "AppIntentRoutine"))
        )
    }
}

struct RemoveRoutineRecordIntent: AppIntent {
    static let title = LocalizedStringResource("Remove Routine Record", table: "AppIntentRoutine")
    static let description = IntentDescription(
        LocalizedStringResource(
            "Removes a record from its routine. This can't be undone; add it again with the same date and amount if needed.",
            table: "AppIntentRoutine"
        )
    )

    @Parameter(title: LocalizedStringResource("Routine Record", table: "AppIntentRoutine"))
    var record: RoutineRecordEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Remove \(\.$record)", table: "AppIntentRoutine")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        try await RoutineRecordEntityQuery.require(record: record)
        let changedCount = try await appAction { try await DiaryRoutineRecordAppAction.shared.remove(recordId: record.recordId) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Removed the record.", table: "AppIntentRoutine"))
        )
    }
}
