import AppIntents
import DiaryIos

struct TagMemoListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Tag's In-Progress Memos", table: "AppIntentTag")
    static let description = IntentDescription(LocalizedStringResource("Gets memos linked to a tag that aren't finished.", table: "AppIntentTag"))

    @Parameter(title: LocalizedStringResource("Tag", table: "AppIntentTag"))
    var tag: TagEntity

    @Parameter(
        title: LocalizedStringResource("Tags to Include", table: "AppIntentCore"),
        description: LocalizedStringResource("If not chosen, this tag only.", table: "AppIntentCore")
    )
    var scope: TagScopeAppEnum?

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
        Summary("Get \(\.$tag)'s In-Progress Memos", table: "AppIntentTag") {
            \.$scope
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[MemoEntity]> & ProvidesDialog {
        let memoPage = try await appAction {
            try await DiaryTagItemAppAction.shared.memoList(tagId: tag.id, scope: scope?.kotlin, sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        return .result(value: memoPage.items.map(MemoEntity.init(memo:)), dialog: memoPage.dialog)
    }
}

struct TagMemoFinishedListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Tag's Finished Memos", table: "AppIntentTag")
    static let description = IntentDescription(LocalizedStringResource("Gets finished memos linked to a tag.", table: "AppIntentTag"))

    @Parameter(title: LocalizedStringResource("Tag", table: "AppIntentTag"))
    var tag: TagEntity

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
        Summary("Get \(\.$tag)'s Finished Memos", table: "AppIntentTag") {
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[MemoEntity]> & ProvidesDialog {
        let memoPage = try await appAction {
            try await DiaryTagItemAppAction.shared.memoFinishedList(tagId: tag.id, sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        return .result(value: memoPage.items.map(MemoEntity.init(memo:)), dialog: memoPage.dialog)
    }
}

struct TagWebListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Tag's Webs", table: "AppIntentTag")
    static let description = IntentDescription(LocalizedStringResource("Gets webs linked to a tag.", table: "AppIntentTag"))

    @Parameter(title: LocalizedStringResource("Tag", table: "AppIntentTag"))
    var tag: TagEntity

    @Parameter(
        title: LocalizedStringResource("Tags to Include", table: "AppIntentCore"),
        description: LocalizedStringResource("If not chosen, this tag only.", table: "AppIntentCore")
    )
    var scope: TagScopeAppEnum?

    @Parameter(
        title: LocalizedStringResource("Sort", table: "AppIntentCore"),
        description: LocalizedStringResource("If not chosen, the list's initial order is used.", table: "AppIntentCore")
    )
    var sort: TagSortAppEnum?

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$tag)'s Webs", table: "AppIntentTag") {
            \.$scope
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[WebEntity]> & ProvidesDialog {
        let webPage = try await appAction {
            try await DiaryTagItemAppAction.shared.webList(tagId: tag.id, scope: scope?.kotlin, sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        let dialog = AppIntentResultDialog.list(
            count: webPage.items.count,
            nextPageNumber: webPage.nextPageNumber,
            found: { count in LocalizedStringResource("Found \(count) webs.", table: "AppIntentTag") },
            foundWithNextPage: { count, nextPage in
                LocalizedStringResource("Found \(count) webs. There's more on page \(nextPage).", table: "AppIntentTag")
            }
        )
        return .result(value: webPage.items.map(WebEntity.init(web:)), dialog: dialog)
    }
}

struct TagPlaceListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Tag's Places", table: "AppIntentTag")
    static let description = IntentDescription(LocalizedStringResource("Gets places linked to a tag.", table: "AppIntentTag"))

    @Parameter(title: LocalizedStringResource("Tag", table: "AppIntentTag"))
    var tag: TagEntity

    @Parameter(
        title: LocalizedStringResource("Tags to Include", table: "AppIntentCore"),
        description: LocalizedStringResource("If not chosen, this tag only.", table: "AppIntentCore")
    )
    var scope: TagScopeAppEnum?

    @Parameter(
        title: LocalizedStringResource("Sort", table: "AppIntentCore"),
        description: LocalizedStringResource("If not chosen, the list's initial order is used.", table: "AppIntentCore")
    )
    var sort: TagSortAppEnum?

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$tag)'s Places", table: "AppIntentTag") {
            \.$scope
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[PlaceEntity]> & ProvidesDialog {
        let placePage = try await appAction {
            try await DiaryTagItemAppAction.shared.placeList(tagId: tag.id, scope: scope?.kotlin, sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        let dialog = AppIntentResultDialog.list(
            count: placePage.items.count,
            nextPageNumber: placePage.nextPageNumber,
            found: { count in LocalizedStringResource("Found \(count) places.", table: "AppIntentTag") },
            foundWithNextPage: { count, nextPage in
                LocalizedStringResource("Found \(count) places. There's more on page \(nextPage).", table: "AppIntentTag")
            }
        )
        return .result(value: placePage.items.map(PlaceEntity.init(place:)), dialog: dialog)
    }
}

private extension DiaryAppActionTagMemoPage {
    nonisolated var dialog: IntentDialog {
        AppIntentResultDialog.list(
            count: items.count,
            nextPageNumber: nextPageNumber,
            found: { count in LocalizedStringResource("Found \(count) memos.", table: "AppIntentTag") },
            foundWithNextPage: { count, nextPage in
                LocalizedStringResource("Found \(count) memos. There's more on page \(nextPage).", table: "AppIntentTag")
            }
        )
    }
}
