import AppIntents
import DiaryIos

struct LunarListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Lunar Dates", table: "AppIntentLunar")
    static let description = IntentDescription(
        LocalizedStringResource("Gets the lunar dates of solar dates in the range. Dates without downloaded lunar data are missing.", table: "AppIntentLunar")
    )

    @Parameter(title: LocalizedStringResource("Start Date", table: "AppIntentCore"), kind: .date)
    var startDate: Date

    @Parameter(
        title: LocalizedStringResource("End Date", table: "AppIntentCore"),
        description: LocalizedStringResource("The end date is included.", table: "AppIntentCore"),
        kind: .date
    )
    var endDate: Date

    static var parameterSummary: some ParameterSummary {
        Summary("Lunar dates from \(\.$startDate) to \(\.$endDate)", table: "AppIntentLunar")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[LunarDateEntity]> & ProvidesDialog {
        let lunarDateList = try await appAction {
            try await DiaryLunarAppAction.shared.list(startDateEpochMillis: startDate.epochMillis, endDateEpochMillis: endDate.epochMillis)
        }.map(LunarDateEntity.init(lunarDate:))
        return .result(
            value: lunarDateList,
            dialog: AppIntentResultDialog.list(
                count: lunarDateList.count,
                nextPageNumber: nil,
                found: { count in LocalizedStringResource("Found \(count) lunar dates.", table: "AppIntentLunar") },
                foundWithNextPage: { count, _ in LocalizedStringResource("Found \(count) lunar dates.", table: "AppIntentLunar") }
            )
        )
    }
}
