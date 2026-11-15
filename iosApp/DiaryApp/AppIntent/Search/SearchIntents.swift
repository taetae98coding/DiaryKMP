import AppIntents
import DiaryIos

struct SearchMemoIntent: AppIntent {
    static let title = LocalizedStringResource("Search Memos", table: "AppIntentSearch")
    static let description = IntentDescription(LocalizedStringResource("Searches memos, including finished ones, like the memo results of the search screen.", table: "AppIntentSearch"))

    @Parameter(
        title: LocalizedStringResource("Search Term", table: "AppIntentCore"),
        requestValueDialog: IntentDialog(LocalizedStringResource("What memo are you looking for?", table: "AppIntentSearch"))
    )
    var query: String

    @Parameter(
        title: LocalizedStringResource("Sort", table: "AppIntentCore"),
        description: LocalizedStringResource("If not chosen, the list's initial order is used.", table: "AppIntentCore")
    )
    var sort: SearchSortAppEnum?

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Search memos for \(\.$query)", table: "AppIntentSearch") {
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[MemoEntity]> & ProvidesDialog {
        let searchPage = try await appAction {
            try await DiarySearchAppAction.shared.memo(query: query, sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        return .result(
            value: searchPage.items.map(MemoEntity.init(memo:)),
            dialog: AppIntentResultDialog.list(
                count: searchPage.items.count,
                nextPageNumber: searchPage.nextPageNumber,
                found: { count in LocalizedStringResource("Found \(count) memos.", table: "AppIntentSearch") },
                foundWithNextPage: { count, nextPageNumber in LocalizedStringResource("Found \(count) memos. There's more on page \(nextPageNumber).", table: "AppIntentSearch") }
            )
        )
    }
}

struct SearchTagIntent: AppIntent {
    static let title = LocalizedStringResource("Search Tags", table: "AppIntentSearch")
    static let description = IntentDescription(LocalizedStringResource("Searches tags like the tag results of the search screen.", table: "AppIntentSearch"))

    @Parameter(
        title: LocalizedStringResource("Search Term", table: "AppIntentCore")
    )
    var query: String

    @Parameter(
        title: LocalizedStringResource("Sort", table: "AppIntentCore"),
        description: LocalizedStringResource("If not chosen, the list's initial order is used.", table: "AppIntentCore")
    )
    var sort: SearchSortAppEnum?

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Search tags for \(\.$query)", table: "AppIntentSearch") {
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[TagEntity]> & ProvidesDialog {
        let searchPage = try await appAction {
            try await DiarySearchAppAction.shared.tag(query: query, sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        return .result(
            value: searchPage.items.map(TagEntity.init(tag:)),
            dialog: AppIntentResultDialog.list(
                count: searchPage.items.count,
                nextPageNumber: searchPage.nextPageNumber,
                found: { count in LocalizedStringResource("Found \(count) tags.", table: "AppIntentSearch") },
                foundWithNextPage: { count, nextPageNumber in LocalizedStringResource("Found \(count) tags. There's more on page \(nextPageNumber).", table: "AppIntentSearch") }
            )
        )
    }
}

struct SearchPlaceIntent: AppIntent {
    static let title = LocalizedStringResource("Search Saved Places", table: "AppIntentSearch")
    static let description = IntentDescription(LocalizedStringResource("Searches places saved in the app. Places outside the app aren't searched.", table: "AppIntentSearch"))

    @Parameter(
        title: LocalizedStringResource("Search Term", table: "AppIntentCore"),
        requestValueDialog: IntentDialog(LocalizedStringResource("What place are you looking for?", table: "AppIntentSearch"))
    )
    var query: String

    @Parameter(
        title: LocalizedStringResource("Sort", table: "AppIntentCore"),
        description: LocalizedStringResource("If not chosen, the list's initial order is used.", table: "AppIntentCore")
    )
    var sort: SearchSortAppEnum?

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Search saved places for \(\.$query)", table: "AppIntentSearch") {
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[PlaceEntity]> & ProvidesDialog {
        let searchPage = try await appAction {
            try await DiarySearchAppAction.shared.place(query: query, sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        return .result(
            value: searchPage.items.map(PlaceEntity.init(place:)),
            dialog: AppIntentResultDialog.list(
                count: searchPage.items.count,
                nextPageNumber: searchPage.nextPageNumber,
                found: { count in LocalizedStringResource("Found \(count) places.", table: "AppIntentSearch") },
                foundWithNextPage: { count, nextPageNumber in LocalizedStringResource("Found \(count) places. There's more on page \(nextPageNumber).", table: "AppIntentSearch") }
            )
        )
    }
}

struct SearchWebIntent: AppIntent {
    static let title = LocalizedStringResource("Search Webs", table: "AppIntentSearch")
    static let description = IntentDescription(LocalizedStringResource("Searches webs saved in the app.", table: "AppIntentSearch"))

    @Parameter(
        title: LocalizedStringResource("Search Term", table: "AppIntentCore")
    )
    var query: String

    @Parameter(
        title: LocalizedStringResource("Sort", table: "AppIntentCore"),
        description: LocalizedStringResource("If not chosen, the list's initial order is used.", table: "AppIntentCore")
    )
    var sort: SearchSortAppEnum?

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Search webs for \(\.$query)", table: "AppIntentSearch") {
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[WebEntity]> & ProvidesDialog {
        let searchPage = try await appAction {
            try await DiarySearchAppAction.shared.web(query: query, sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        return .result(
            value: searchPage.items.map(WebEntity.init(web:)),
            dialog: AppIntentResultDialog.list(
                count: searchPage.items.count,
                nextPageNumber: searchPage.nextPageNumber,
                found: { count in LocalizedStringResource("Found \(count) webs.", table: "AppIntentSearch") },
                foundWithNextPage: { count, nextPageNumber in LocalizedStringResource("Found \(count) webs. There's more on page \(nextPageNumber).", table: "AppIntentSearch") }
            )
        )
    }
}

struct SearchDDayIntent: AppIntent {
    static let title = LocalizedStringResource("Search D-Days", table: "AppIntentSearch")
    static let description = IntentDescription(LocalizedStringResource("Searches D-Days saved in the app, with the days left or passed.", table: "AppIntentSearch"))

    @Parameter(
        title: LocalizedStringResource("Search Term", table: "AppIntentCore")
    )
    var query: String

    @Parameter(
        title: LocalizedStringResource("Sort", table: "AppIntentCore"),
        description: LocalizedStringResource("If not chosen, the list's initial order is used.", table: "AppIntentCore")
    )
    var sort: SearchSortAppEnum?

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Search D-Days for \(\.$query)", table: "AppIntentSearch") {
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[DDayEntity]> & ProvidesDialog {
        let searchPage = try await appAction {
            try await DiarySearchAppAction.shared.dday(query: query, sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        return .result(
            value: searchPage.items.map(DDayEntity.init(dday:)),
            dialog: AppIntentResultDialog.list(
                count: searchPage.items.count,
                nextPageNumber: searchPage.nextPageNumber,
                found: { count in LocalizedStringResource("Found \(count) D-Days.", table: "AppIntentSearch") },
                foundWithNextPage: { count, nextPageNumber in LocalizedStringResource("Found \(count) D-Days. There's more on page \(nextPageNumber).", table: "AppIntentSearch") }
            )
        )
    }
}

struct SearchRoutineIntent: AppIntent {
    static let title = LocalizedStringResource("Search Routines", table: "AppIntentSearch")
    static let description = IntentDescription(LocalizedStringResource("Searches routines saved in the app, including finished ones, with this period's total and progress.", table: "AppIntentSearch"))

    @Parameter(
        title: LocalizedStringResource("Search Term", table: "AppIntentCore")
    )
    var query: String

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Search Routines for \(\.$query)", table: "AppIntentSearch") {
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[RoutineEntity]> & ProvidesDialog {
        let searchPage = try await appAction {
            try await DiarySearchAppAction.shared.routine(query: query, pageNumber: AppIntentPage.number(page))
        }
        return .result(
            value: searchPage.items.map(RoutineEntity.init(routine:)),
            dialog: AppIntentResultDialog.list(
                count: searchPage.items.count,
                nextPageNumber: searchPage.nextPageNumber,
                found: { count in LocalizedStringResource("Found \(count) routines.", table: "AppIntentSearch") },
                foundWithNextPage: { count, nextPageNumber in LocalizedStringResource("Found \(count) routines. There's more on page \(nextPageNumber).", table: "AppIntentSearch") }
            )
        )
    }
}
