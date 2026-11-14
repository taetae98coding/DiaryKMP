import AppIntents
import DiaryIos

struct RoutineListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Routines", table: "AppIntentRoutine")
    static let description = IntentDescription(
        LocalizedStringResource(
            "Gets the routines that aren't finished, in progress first, then not started, with this period's total and progress. The tag filter chosen in the app applies, and routines that aren't for today are included.",
            table: "AppIntentRoutine"
        )
    )

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Get Routines", table: "AppIntentRoutine") {
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[RoutineEntity]> & ProvidesDialog {
        let routinePage = try await appAction {
            try await DiaryRoutineAppAction.shared.list(pageNumber: AppIntentPage.number(page))
        }
        return .result(value: routinePage.items.map(RoutineEntity.init(routine:)), dialog: RoutineListDialog.make(page: routinePage))
    }
}

struct RoutineFinishedListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Finished Routines", table: "AppIntentRoutine")
    static let description = IntentDescription(LocalizedStringResource("Gets finished routines.", table: "AppIntentRoutine"))

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Get Finished Routines", table: "AppIntentRoutine") {
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[RoutineEntity]> & ProvidesDialog {
        let routinePage = try await appAction {
            try await DiaryRoutineAppAction.shared.finishedList(pageNumber: AppIntentPage.number(page))
        }
        return .result(value: routinePage.items.map(RoutineEntity.init(routine:)), dialog: RoutineListDialog.make(page: routinePage))
    }
}

struct RoutineDetailIntent: AppIntent {
    static let title = LocalizedStringResource("Get Routine Details", table: "AppIntentRoutine")
    static let description = IntentDescription(LocalizedStringResource("Gets a routine with its linked tags.", table: "AppIntentRoutine"))

    @Parameter(title: LocalizedStringResource("Routine", table: "AppIntentRoutine"))
    var routine: RoutineEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$routine) Details", table: "AppIntentRoutine")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<RoutineEntity> {
        .result(value: try await RoutineEntityQuery.require(routineId: routine.id))
    }
}

nonisolated enum RoutineListDialog {
    static func make(page: DiaryAppActionRoutinePage) -> IntentDialog {
        AppIntentResultDialog.list(
            count: page.items.count,
            nextPageNumber: page.nextPageNumber,
            found: { count in LocalizedStringResource("Found \(count) routines.", table: "AppIntentRoutine") },
            foundWithNextPage: { count, nextPage in
                LocalizedStringResource("Found \(count) routines. There's more on page \(nextPage).", table: "AppIntentRoutine")
            }
        )
    }
}
