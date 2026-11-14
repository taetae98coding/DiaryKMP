import AppIntents
import DiaryIos

struct WebListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Webs", table: "AppIntentWeb")
    static let description = IntentDescription(LocalizedStringResource("Gets saved webs in the order of the web list.", table: "AppIntentWeb"))

    @Parameter(
        title: LocalizedStringResource("Sort", table: "AppIntentCore"),
        description: LocalizedStringResource("If not chosen, the list's initial order is used.", table: "AppIntentCore")
    )
    var sort: WebSortAppEnum?

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Get Webs", table: "AppIntentWeb") {
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[WebEntity]> & ProvidesDialog {
        let webPage = try await appAction {
            try await DiaryWebAppAction.shared.list(sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        return .result(
            value: webPage.items.map { web in WebEntity(web: web) },
            dialog: AppIntentResultDialog.list(
                count: webPage.items.count,
                nextPageNumber: webPage.nextPageNumber,
                found: { count in LocalizedStringResource("Found \(count) webs.", table: "AppIntentWeb") },
                foundWithNextPage: { count, nextPage in
                    LocalizedStringResource("Found \(count) webs. There's more on page \(nextPage).", table: "AppIntentWeb")
                }
            )
        )
    }
}

struct WebDetailIntent: AppIntent {
    static let title = LocalizedStringResource("Get Web Details", table: "AppIntentWeb")
    static let description = IntentDescription(LocalizedStringResource("Gets a web with its request headers and linked tags.", table: "AppIntentWeb"))

    @Parameter(title: LocalizedStringResource("Web", table: "AppIntentWeb"))
    var web: WebEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$web) Details", table: "AppIntentWeb")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<WebEntity> {
        .result(value: try await WebEntityQuery.require(webId: web.id))
    }
}

struct WebMemoListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Web's In-Progress Memos", table: "AppIntentWeb")
    static let description = IntentDescription(LocalizedStringResource("Gets memos linked to a web that aren't finished.", table: "AppIntentWeb"))

    @Parameter(title: LocalizedStringResource("Web", table: "AppIntentWeb"))
    var web: WebEntity

    @Parameter(
        title: LocalizedStringResource("Sort", table: "AppIntentCore"),
        description: LocalizedStringResource("If not chosen, the list's initial order is used.", table: "AppIntentCore")
    )
    var sort: MemoSortAppEnum?

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$web)'s In-Progress Memos", table: "AppIntentWeb") {
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[MemoEntity]> & ProvidesDialog {
        _ = try await WebEntityQuery.require(webId: web.id)
        let memoPage = try await appAction {
            try await DiaryMemoListAppAction.shared.webList(webId: web.id, sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        return .result(value: memoPage.items.map(MemoEntity.init(memo:)), dialog: LinkedMemoListDialog.make(page: memoPage))
    }
}

struct WebFinishedMemoListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Web's Finished Memos", table: "AppIntentWeb")
    static let description = IntentDescription(LocalizedStringResource("Gets finished memos linked to a web.", table: "AppIntentWeb"))

    @Parameter(title: LocalizedStringResource("Web", table: "AppIntentWeb"))
    var web: WebEntity

    @Parameter(
        title: LocalizedStringResource("Sort", table: "AppIntentCore"),
        description: LocalizedStringResource("If not chosen, the list's initial order is used.", table: "AppIntentCore")
    )
    var sort: MemoSortAppEnum?

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$web)'s Finished Memos", table: "AppIntentWeb") {
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[MemoEntity]> & ProvidesDialog {
        _ = try await WebEntityQuery.require(webId: web.id)
        let memoPage = try await appAction {
            try await DiaryMemoListAppAction.shared.webFinishedList(webId: web.id, sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        return .result(value: memoPage.items.map(MemoEntity.init(memo:)), dialog: LinkedMemoListDialog.make(page: memoPage))
    }
}
