import AppIntents
import DiaryIos

struct AddRoutineIntent: AppIntent {
    static let title = LocalizedStringResource("Add Routine", table: "AppIntentRoutine")
    static let description = IntentDescription(
        LocalizedStringResource(
            "Adds a new routine. Today is used if you don't choose a start date, and the color is chosen at random.",
            table: "AppIntentRoutine"
        )
    )

    @Parameter(
        title: LocalizedStringResource("Title", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, leave it empty to keep the current title.", table: "AppIntentCore"),
        requestValueDialog: IntentDialog(LocalizedStringResource("What's the routine title?", table: "AppIntentRoutine"))
    )
    var title: String

    @Parameter(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var routineDescription: String?

    @Parameter(
        title: LocalizedStringResource("Start Date", table: "AppIntentRoutine"),
        description: LocalizedStringResource(
            "When adding, leave it empty to use today. When editing, leave it empty to keep the current start date.",
            table: "AppIntentRoutine"
        ),
        kind: .date
    )
    var startDate: Date?

    @Parameter(
        title: LocalizedStringResource("Repeat", table: "AppIntentRoutine"),
        description: LocalizedStringResource("If not chosen when adding, Daily.", table: "AppIntentRoutine")
    )
    var cycle: RoutineCycleAppEnum?

    @Parameter(
        title: LocalizedStringResource("Unit", table: "AppIntentRoutine"),
        description: LocalizedStringResource(
            "Up to 10 characters on one line, like times, ml or pages. Leave it empty to add a routine without a unit. When editing, leave it empty to keep the current unit.",
            table: "AppIntentRoutine"
        )
    )
    var unit: String?

    @Parameter(
        title: LocalizedStringResource("Goal", table: "AppIntentRoutine"),
        description: LocalizedStringResource(
            "The amount to reach in each period, a whole number from 1 to 9999999. Leave it empty to add a routine without a goal.",
            table: "AppIntentRoutine"
        )
    )
    var goalAmount: Int?

    @Parameter(
        title: LocalizedStringResource("Amount per Record", table: "AppIntentRoutine"),
        description: LocalizedStringResource("A whole number from 1 to 9999999. If not entered when adding, 1.", table: "AppIntentRoutine")
    )
    var amountPerRecord: Int?

    @Parameter(title: LocalizedStringResource("Tags to Link", table: "AppIntentCore"))
    var tags: [TagEntity]?

    static var parameterSummary: some ParameterSummary {
        Summary("Add Routine \(\.$title)", table: "AppIntentRoutine") {
            \.$routineDescription
            \.$startDate
            \.$cycle
            \.$unit
            \.$goalAmount
            \.$amountPerRecord
            \.$tags
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<RoutineEntity> & ProvidesDialog {
        let routineId = try await appAction {
            try await DiaryRoutineAppAction.shared.add(
                title: title,
                routineDescription: routineDescription,
                startDateEpochMillis: startDate?.epochMillis,
                cycle: cycle?.kotlin,
                unit: unit,
                goalAmount: goalAmount.map(Int32.init(clamping:)),
                amountPerRecord: amountPerRecord.map(Int32.init(clamping:)),
                tagIds: (tags ?? []).map(\.id)
            )
        }
        return .result(
            value: try await RoutineEntityQuery.require(routineId: routineId),
            dialog: IntentDialog(LocalizedStringResource("Added the routine.", table: "AppIntentRoutine"))
        )
    }
}

struct EditRoutineIntent: AppIntent {
    static let title = LocalizedStringResource("Edit Routine", table: "AppIntentRoutine")
    static let description = IntentDescription(
        LocalizedStringResource(
            "Changes the title, description, color, start date, repeat, unit, goal or amount per record of a routine. Values left empty stay the same.",
            table: "AppIntentRoutine"
        )
    )

    @Parameter(title: LocalizedStringResource("Routine", table: "AppIntentRoutine"))
    var routine: RoutineEntity

    @Parameter(
        title: LocalizedStringResource("Title", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, leave it empty to keep the current title.", table: "AppIntentCore")
    )
    var title: String?

    @Parameter(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var routineDescription: String?

    @Parameter(
        title: LocalizedStringResource("Color", table: "AppIntentCore"),
        description: LocalizedStringResource("Enter a hex color like #4285F4.", table: "AppIntentCore")
    )
    var color: String?

    @Parameter(
        title: LocalizedStringResource("Start Date", table: "AppIntentRoutine"),
        description: LocalizedStringResource(
            "When adding, leave it empty to use today. When editing, leave it empty to keep the current start date.",
            table: "AppIntentRoutine"
        ),
        kind: .date
    )
    var startDate: Date?

    @Parameter(
        title: LocalizedStringResource("Repeat", table: "AppIntentRoutine"),
        description: LocalizedStringResource("If not chosen when adding, Daily.", table: "AppIntentRoutine")
    )
    var cycle: RoutineCycleAppEnum?

    @Parameter(
        title: LocalizedStringResource("Unit", table: "AppIntentRoutine"),
        description: LocalizedStringResource(
            "Up to 10 characters on one line, like times, ml or pages. Leave it empty to add a routine without a unit. When editing, leave it empty to keep the current unit.",
            table: "AppIntentRoutine"
        )
    )
    var unit: String?

    @Parameter(
        title: LocalizedStringResource("Remove Unit", table: "AppIntentRoutine"),
        description: LocalizedStringResource("Turn on to remove the unit.", table: "AppIntentRoutine")
    )
    var clearUnit: Bool?

    @Parameter(
        title: LocalizedStringResource("Goal", table: "AppIntentRoutine"),
        description: LocalizedStringResource(
            "The amount to reach in each period, a whole number from 1 to 9999999. Leave it empty to add a routine without a goal.",
            table: "AppIntentRoutine"
        )
    )
    var goalAmount: Int?

    @Parameter(
        title: LocalizedStringResource("Remove Goal", table: "AppIntentRoutine"),
        description: LocalizedStringResource("Turn on to remove the goal.", table: "AppIntentRoutine")
    )
    var clearGoalAmount: Bool?

    @Parameter(
        title: LocalizedStringResource("Amount per Record", table: "AppIntentRoutine"),
        description: LocalizedStringResource("A whole number from 1 to 9999999. If not entered when adding, 1.", table: "AppIntentRoutine")
    )
    var amountPerRecord: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Edit \(\.$routine)", table: "AppIntentRoutine") {
            \.$title
            \.$routineDescription
            \.$color
            \.$startDate
            \.$cycle
            \.$unit
            \.$clearUnit
            \.$goalAmount
            \.$clearGoalAmount
            \.$amountPerRecord
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await RoutineEntityQuery.require(routineId: routine.id)
        let changedCount = try await appAction {
            try await DiaryRoutineAppAction.shared.update(
                routineId: routine.id,
                title: title,
                routineDescription: routineDescription,
                color: color,
                startDateEpochMillis: startDate?.epochMillis,
                cycle: cycle?.kotlin,
                unit: clearUnit == true ? "" : unit,
                goalAmount: goalAmount.map(Int32.init(clamping:)),
                clearGoalAmount: clearGoalAmount,
                amountPerRecord: amountPerRecord.map(Int32.init(clamping:))
            )
        }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Updated the routine.", table: "AppIntentRoutine"))
        )
    }
}
