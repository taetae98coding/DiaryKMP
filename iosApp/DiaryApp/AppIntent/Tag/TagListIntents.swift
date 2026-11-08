import AppIntents
import DiaryIos

struct TagListIntent: AppIntent {
    static let title = LocalizedStringResource("Get In-Progress Tags", table: "AppIntentTag")
    static let description = IntentDescription(
        LocalizedStringResource("Gets the tags shown in the tag list that aren't finished. The filters chosen in the app apply.", table: "AppIntentTag")
    )

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
        Summary("Get In-Progress Tags", table: "AppIntentTag") {
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[TagEntity]> & ProvidesDialog {
        let tagPage = try await appAction {
            try await DiaryTagAppAction.shared.list(sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        return .result(value: tagPage.items.map(TagEntity.init(tag:)), dialog: tagPage.dialog)
    }
}

struct TagFinishedListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Finished Tags", table: "AppIntentTag")
    static let description = IntentDescription(LocalizedStringResource("Gets finished tags.", table: "AppIntentTag"))

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
        Summary("Get Finished Tags", table: "AppIntentTag") {
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[TagEntity]> & ProvidesDialog {
        let tagPage = try await appAction {
            try await DiaryTagAppAction.shared.finishedList(sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        return .result(value: tagPage.items.map(TagEntity.init(tag:)), dialog: tagPage.dialog)
    }
}

struct TagDetailIntent: AppIntent {
    static let title = LocalizedStringResource("Get Tag Details", table: "AppIntentTag")
    static let description = IntentDescription(LocalizedStringResource("Gets a tag with the child tags it links to.", table: "AppIntentTag"))

    @Parameter(title: LocalizedStringResource("Tag", table: "AppIntentTag"))
    var tag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$tag) Details", table: "AppIntentTag")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<TagEntity> {
        .result(value: try await TagEntityQuery.require(tagId: tag.id))
    }
}

private extension DiaryAppActionTagPage {
    nonisolated var dialog: IntentDialog {
        AppIntentResultDialog.list(
            count: items.count,
            nextPageNumber: nextPageNumber,
            found: { count in LocalizedStringResource("Found \(count) tags.", table: "AppIntentTag") },
            foundWithNextPage: { count, nextPage in
                LocalizedStringResource("Found \(count) tags. There's more on page \(nextPage).", table: "AppIntentTag")
            }
        )
    }
}
