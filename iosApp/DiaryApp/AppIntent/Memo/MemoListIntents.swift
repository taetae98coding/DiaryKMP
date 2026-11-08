import AppIntents
import DiaryIos

struct MemoListIntent: AppIntent {
    static let title = LocalizedStringResource("Get In-Progress Memos", table: "AppIntentMemo")
    static let description = IntentDescription(
        LocalizedStringResource("Gets the memos shown in the memo list that aren't finished. The filters chosen in the app apply.", table: "AppIntentMemo")
    )

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
        Summary("Get In-Progress Memos", table: "AppIntentMemo") {
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[MemoEntity]> & ProvidesDialog {
        let memoPage = try await appAction {
            try await DiaryMemoAppAction.shared.list(sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        return .result(value: memoPage.items.map(MemoEntity.init(memo:)), dialog: memoPage.dialog)
    }
}

struct MemoFinishedListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Finished Memos", table: "AppIntentMemo")
    static let description = IntentDescription(LocalizedStringResource("Gets finished memos.", table: "AppIntentMemo"))

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
        Summary("Get Finished Memos", table: "AppIntentMemo") {
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[MemoEntity]> & ProvidesDialog {
        let memoPage = try await appAction {
            try await DiaryMemoAppAction.shared.finishedList(sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        return .result(value: memoPage.items.map(MemoEntity.init(memo:)), dialog: memoPage.dialog)
    }
}

struct MemoDetailIntent: AppIntent {
    static let title = LocalizedStringResource("Get Memo Details", table: "AppIntentMemo")
    static let description = IntentDescription(
        LocalizedStringResource("Gets a memo with its linked tags, places, webs and contacts.", table: "AppIntentMemo")
    )

    @Parameter(title: LocalizedStringResource("Memo", table: "AppIntentMemo"))
    var memo: MemoEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$memo) Details", table: "AppIntentMemo")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<MemoEntity> {
        .result(value: try await MemoEntityQuery.require(memoId: memo.id))
    }
}

private extension DiaryAppActionMemoPage {
    nonisolated var dialog: IntentDialog {
        AppIntentResultDialog.list(
            count: items.count,
            nextPageNumber: nextPageNumber,
            found: { count in LocalizedStringResource("Found \(count) memos.", table: "AppIntentMemo") },
            foundWithNextPage: { count, nextPage in
                LocalizedStringResource("Found \(count) memos. There's more on page \(nextPage).", table: "AppIntentMemo")
            }
        )
    }
}
