import AppIntents
import DiaryIos

struct DDayListIntent: AppIntent {
    static let title = LocalizedStringResource("Get D-Days", table: "AppIntentDDay")
    static let description = IntentDescription(
        LocalizedStringResource(
            "Gets the D-Days shown in the D-Day list by title, with the days left or passed. The filters chosen in the app apply.",
            table: "AppIntentDDay"
        )
    )

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Get D-Days", table: "AppIntentDDay") {
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[DDayEntity]> & ProvidesDialog {
        let ddayPage = try await appAction {
            try await DiaryDDayAppAction.shared.list(pageNumber: AppIntentPage.number(page))
        }
        return .result(value: ddayPage.items.map(DDayEntity.init(dday:)), dialog: DDayListDialog.make(page: ddayPage))
    }
}

struct DDayDetailIntent: AppIntent {
    static let title = LocalizedStringResource("Get D-Day Details", table: "AppIntentDDay")
    static let description = IntentDescription(LocalizedStringResource("Gets a D-Day with its linked tags.", table: "AppIntentDDay"))

    @Parameter(title: LocalizedStringResource("D-Day", table: "AppIntentDDay"))
    var dday: DDayEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$dday) Details", table: "AppIntentDDay")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<DDayEntity> {
        .result(value: try await DDayEntityQuery.require(ddayId: dday.id))
    }
}

nonisolated enum DDayListDialog {
    static func make(page: DiaryAppActionDDayPage) -> IntentDialog {
        AppIntentResultDialog.list(
            count: page.items.count,
            nextPageNumber: page.nextPageNumber,
            found: { count in LocalizedStringResource("Found \(count) D-Days.", table: "AppIntentDDay") },
            foundWithNextPage: { count, nextPage in
                LocalizedStringResource("Found \(count) D-Days. There's more on page \(nextPage).", table: "AppIntentDDay")
            }
        )
    }
}
