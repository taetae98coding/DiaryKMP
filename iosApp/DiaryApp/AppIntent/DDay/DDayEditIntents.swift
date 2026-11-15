import AppIntents
import DiaryIos

struct AddDDayIntent: AppIntent {
    static let title = LocalizedStringResource("Add D-Day", table: "AppIntentDDay")
    static let description = IntentDescription(
        LocalizedStringResource(
            "Adds a new D-Day. Today is used if you don't choose a start date, and a random color if you don't choose one.",
            table: "AppIntentDDay"
        )
    )

    @Parameter(
        title: LocalizedStringResource("Title", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, leave it empty to keep the current title.", table: "AppIntentCore"),
        requestValueDialog: IntentDialog(LocalizedStringResource("What's the D-Day title?", table: "AppIntentDDay"))
    )
    var title: String

    @Parameter(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var ddayDescription: String?

    @Parameter(
        title: LocalizedStringResource("Color", table: "AppIntentCore"),
        description: LocalizedStringResource("Enter a hex color like #4285F4.", table: "AppIntentCore")
    )
    var color: String?

    @Parameter(
        title: LocalizedStringResource("Start Date", table: "AppIntentDDay"),
        description: LocalizedStringResource(
            "When adding, leave it empty to use today. When editing, leave it empty to keep the current start date.",
            table: "AppIntentDDay"
        ),
        kind: .date
    )
    var baseDate: Date?

    @Parameter(
        title: LocalizedStringResource("Count Start Date as Day 1", table: "AppIntentDDay"),
        description: LocalizedStringResource("On counts the start date as day 1. When adding, it's on if not chosen.", table: "AppIntentDDay")
    )
    var countsBaseDay: Bool?

    @Parameter(
        title: LocalizedStringResource("Repeat Intervals", table: "AppIntentDDay"),
        description: LocalizedStringResource(
            "Enter each interval like P100D (100 days), P3M (3 months) or P1Y (1 year). One interval can't mix days with months. When editing, they replace all current intervals; an empty list removes them all.",
            table: "AppIntentDDay"
        )
    )
    var repeats: [String]?

    @Parameter(
        title: LocalizedStringResource("End Date", table: "AppIntentDDay"),
        description: LocalizedStringResource("Anniversaries are shown only up to this date.", table: "AppIntentDDay"),
        kind: .date
    )
    var endDate: Date?

    @Parameter(
        title: LocalizedStringResource("Missing Dates", table: "AppIntentDDay"),
        description: LocalizedStringResource(
            "What to do when a monthly anniversary falls on a date the month doesn't have. If not chosen when adding, Use last day of month.",
            table: "AppIntentDDay"
        )
    )
    var overflowPolicy: DDayOverflowPolicyAppEnum?

    @Parameter(title: LocalizedStringResource("Tags to Link", table: "AppIntentCore"))
    var tags: [TagEntity]?

    static var parameterSummary: some ParameterSummary {
        Summary("Add D-Day \(\.$title)", table: "AppIntentDDay") {
            \.$ddayDescription
            \.$color
            \.$baseDate
            \.$countsBaseDay
            \.$repeats
            \.$endDate
            \.$overflowPolicy
            \.$tags
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<DDayEntity> & ProvidesDialog {
        let ddayId = try await appAction {
            try await DiaryDDayAppAction.shared.add(
                title: title,
                ddayDescription: ddayDescription,
                color: color,
                baseDateEpochMillis: baseDate?.epochMillis,
                countsBaseDay: countsBaseDay,
                repeats: repeats ?? [],
                endDateEpochMillis: endDate?.epochMillis,
                overflowPolicy: overflowPolicy?.kotlin,
                tagIds: (tags ?? []).map(\.id)
            )
        }
        return .result(
            value: try await DDayEntityQuery.require(ddayId: ddayId),
            dialog: IntentDialog(LocalizedStringResource("Added the D-Day.", table: "AppIntentDDay"))
        )
    }
}

struct EditDDayIntent: AppIntent {
    static let title = LocalizedStringResource("Edit D-Day", table: "AppIntentDDay")
    static let description = IntentDescription(
        LocalizedStringResource(
            "Changes the title, description, color, start date, repeat intervals, end date or other details of a D-Day. Values left empty stay the same.",
            table: "AppIntentDDay"
        )
    )

    @Parameter(title: LocalizedStringResource("D-Day", table: "AppIntentDDay"))
    var dday: DDayEntity

    @Parameter(
        title: LocalizedStringResource("Title", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, leave it empty to keep the current title.", table: "AppIntentCore")
    )
    var title: String?

    @Parameter(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var ddayDescription: String?

    @Parameter(
        title: LocalizedStringResource("Color", table: "AppIntentCore"),
        description: LocalizedStringResource("Enter a hex color like #4285F4.", table: "AppIntentCore")
    )
    var color: String?

    @Parameter(
        title: LocalizedStringResource("Start Date", table: "AppIntentDDay"),
        description: LocalizedStringResource(
            "When adding, leave it empty to use today. When editing, leave it empty to keep the current start date.",
            table: "AppIntentDDay"
        ),
        kind: .date
    )
    var baseDate: Date?

    @Parameter(
        title: LocalizedStringResource("Count Start Date as Day 1", table: "AppIntentDDay"),
        description: LocalizedStringResource("On counts the start date as day 1. When adding, it's on if not chosen.", table: "AppIntentDDay")
    )
    var countsBaseDay: Bool?

    @Parameter(
        title: LocalizedStringResource("Repeat Intervals", table: "AppIntentDDay"),
        description: LocalizedStringResource(
            "Enter each interval like P100D (100 days), P3M (3 months) or P1Y (1 year). One interval can't mix days with months. When editing, they replace all current intervals; an empty list removes them all.",
            table: "AppIntentDDay"
        )
    )
    var repeats: [String]?

    @Parameter(
        title: LocalizedStringResource("End Date", table: "AppIntentDDay"),
        description: LocalizedStringResource("Anniversaries are shown only up to this date.", table: "AppIntentDDay"),
        kind: .date
    )
    var endDate: Date?

    @Parameter(
        title: LocalizedStringResource("Remove End Date", table: "AppIntentDDay"),
        description: LocalizedStringResource("Turn on to remove the end date.", table: "AppIntentDDay")
    )
    var clearEndDate: Bool?

    @Parameter(
        title: LocalizedStringResource("Missing Dates", table: "AppIntentDDay"),
        description: LocalizedStringResource(
            "What to do when a monthly anniversary falls on a date the month doesn't have. If not chosen when adding, Use last day of month.",
            table: "AppIntentDDay"
        )
    )
    var overflowPolicy: DDayOverflowPolicyAppEnum?

    static var parameterSummary: some ParameterSummary {
        Summary("Edit \(\.$dday)", table: "AppIntentDDay") {
            \.$title
            \.$ddayDescription
            \.$color
            \.$baseDate
            \.$countsBaseDay
            \.$repeats
            \.$endDate
            \.$clearEndDate
            \.$overflowPolicy
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await DDayEntityQuery.require(ddayId: dday.id)
        let changedCount = try await appAction {
            try await DiaryDDayAppAction.shared.update(
                ddayId: dday.id,
                title: title,
                ddayDescription: ddayDescription,
                color: color,
                baseDateEpochMillis: baseDate?.epochMillis,
                countsBaseDay: countsBaseDay,
                repeats: repeats,
                endDateEpochMillis: endDate?.epochMillis,
                clearEndDate: clearEndDate,
                overflowPolicy: overflowPolicy?.kotlin
            )
        }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Updated the D-Day.", table: "AppIntentDDay"))
        )
    }
}
