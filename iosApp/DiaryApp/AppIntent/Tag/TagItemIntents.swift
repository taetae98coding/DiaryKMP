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
        _ = try await TagEntityQuery.require(tagId: tag.id)
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
        _ = try await TagEntityQuery.require(tagId: tag.id)
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
        _ = try await TagEntityQuery.require(tagId: tag.id)
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
        _ = try await TagEntityQuery.require(tagId: tag.id)
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

struct TagContactListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Tag's Contacts", table: "AppIntentTag")
    static let description = IntentDescription(LocalizedStringResource("Gets contacts linked to a tag.", table: "AppIntentTag"))

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
    var sort: ContactSortAppEnum?

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$tag)'s Contacts", table: "AppIntentTag") {
            \.$scope
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[ContactEntity]> & ProvidesDialog {
        _ = try await TagEntityQuery.require(tagId: tag.id)
        let itemPage = try await appAction {
            try await DiaryTagItemAppAction.shared.contactList(tagId: tag.id, scope: scope?.kotlin, sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        let dialog = AppIntentResultDialog.list(
            count: itemPage.items.count,
            nextPageNumber: itemPage.nextPageNumber,
            found: { count in LocalizedStringResource("Found \(count) contacts.", table: "AppIntentTag") },
            foundWithNextPage: { count, nextPage in
                LocalizedStringResource("Found \(count) contacts. There's more on page \(nextPage).", table: "AppIntentTag")
            }
        )
        return .result(value: itemPage.items.map(ContactEntity.init(contact:)), dialog: dialog)
    }
}

struct TagChecklistListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Tag's Checklists", table: "AppIntentTag")
    static let description = IntentDescription(LocalizedStringResource("Gets checklists linked to a tag.", table: "AppIntentTag"))

    @Parameter(title: LocalizedStringResource("Tag", table: "AppIntentTag"))
    var tag: TagEntity

    @Parameter(
        title: LocalizedStringResource("Tags to Include", table: "AppIntentCore"),
        description: LocalizedStringResource("If not chosen, this tag only.", table: "AppIntentCore")
    )
    var scope: TagScopeAppEnum?

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$tag)'s Checklists", table: "AppIntentTag") {
            \.$scope
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[ChecklistEntity]> & ProvidesDialog {
        _ = try await TagEntityQuery.require(tagId: tag.id)
        let itemPage = try await appAction {
            try await DiaryTagItemAppAction.shared.checklistList(tagId: tag.id, scope: scope?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        let dialog = AppIntentResultDialog.list(
            count: itemPage.items.count,
            nextPageNumber: itemPage.nextPageNumber,
            found: { count in LocalizedStringResource("Found \(count) checklists.", table: "AppIntentTag") },
            foundWithNextPage: { count, nextPage in
                LocalizedStringResource("Found \(count) checklists. There's more on page \(nextPage).", table: "AppIntentTag")
            }
        )
        return .result(value: itemPage.items.map(ChecklistEntity.init(summary:)), dialog: dialog)
    }
}

struct TagMusicListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Tag's Songs", table: "AppIntentTag")
    static let description = IntentDescription(LocalizedStringResource("Gets songs linked to a tag.", table: "AppIntentTag"))

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
    var sort: MusicSortAppEnum?

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$tag)'s Songs", table: "AppIntentTag") {
            \.$scope
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[MusicEntity]> & ProvidesDialog {
        _ = try await TagEntityQuery.require(tagId: tag.id)
        let itemPage = try await appAction {
            try await DiaryTagItemAppAction.shared.musicList(tagId: tag.id, scope: scope?.kotlin, sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        let dialog = AppIntentResultDialog.list(
            count: itemPage.items.count,
            nextPageNumber: itemPage.nextPageNumber,
            found: { count in LocalizedStringResource("Found \(count) songs.", table: "AppIntentTag") },
            foundWithNextPage: { count, nextPage in
                LocalizedStringResource("Found \(count) songs. There's more on page \(nextPage).", table: "AppIntentTag")
            }
        )
        return .result(value: itemPage.items.map(MusicEntity.init(music:)), dialog: dialog)
    }
}

struct TagQrListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Tag's QR Codes", table: "AppIntentTag")
    static let description = IntentDescription(LocalizedStringResource("Gets QR codes linked to a tag.", table: "AppIntentTag"))

    @Parameter(title: LocalizedStringResource("Tag", table: "AppIntentTag"))
    var tag: TagEntity

    @Parameter(
        title: LocalizedStringResource("Tags to Include", table: "AppIntentCore"),
        description: LocalizedStringResource("If not chosen, this tag only.", table: "AppIntentCore")
    )
    var scope: TagScopeAppEnum?

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$tag)'s QR Codes", table: "AppIntentTag") {
            \.$scope
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[QrEntity]> & ProvidesDialog {
        _ = try await TagEntityQuery.require(tagId: tag.id)
        let itemPage = try await appAction {
            try await DiaryTagItemAppAction.shared.qrList(tagId: tag.id, scope: scope?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        let dialog = AppIntentResultDialog.list(
            count: itemPage.items.count,
            nextPageNumber: itemPage.nextPageNumber,
            found: { count in LocalizedStringResource("Found \(count) QR codes.", table: "AppIntentTag") },
            foundWithNextPage: { count, nextPage in
                LocalizedStringResource("Found \(count) QR codes. There's more on page \(nextPage).", table: "AppIntentTag")
            }
        )
        return .result(value: itemPage.items.map(QrEntity.init(qr:)), dialog: dialog)
    }
}

struct TagDDayListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Tag's D-Days", table: "AppIntentTag")
    static let description = IntentDescription(LocalizedStringResource("Gets D-Days linked to a tag.", table: "AppIntentTag"))

    @Parameter(title: LocalizedStringResource("Tag", table: "AppIntentTag"))
    var tag: TagEntity

    @Parameter(
        title: LocalizedStringResource("Tags to Include", table: "AppIntentCore"),
        description: LocalizedStringResource("If not chosen, this tag only.", table: "AppIntentCore")
    )
    var scope: TagScopeAppEnum?

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$tag)'s D-Days", table: "AppIntentTag") {
            \.$scope
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[DDayEntity]> & ProvidesDialog {
        _ = try await TagEntityQuery.require(tagId: tag.id)
        let itemPage = try await appAction {
            try await DiaryTagItemAppAction.shared.ddayList(tagId: tag.id, scope: scope?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        let dialog = AppIntentResultDialog.list(
            count: itemPage.items.count,
            nextPageNumber: itemPage.nextPageNumber,
            found: { count in LocalizedStringResource("Found \(count) D-Days.", table: "AppIntentTag") },
            foundWithNextPage: { count, nextPage in
                LocalizedStringResource("Found \(count) D-Days. There's more on page \(nextPage).", table: "AppIntentTag")
            }
        )
        return .result(value: itemPage.items.map(DDayEntity.init(dday:)), dialog: dialog)
    }
}

struct TagRoutineListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Tag's Routines", table: "AppIntentTag")
    static let description = IntentDescription(LocalizedStringResource("Gets routines linked to a tag that aren't finished, in progress first, then not started.", table: "AppIntentTag"))

    @Parameter(title: LocalizedStringResource("Tag", table: "AppIntentTag"))
    var tag: TagEntity

    @Parameter(
        title: LocalizedStringResource("Tags to Include", table: "AppIntentCore"),
        description: LocalizedStringResource("If not chosen, this tag only.", table: "AppIntentCore")
    )
    var scope: TagScopeAppEnum?

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$tag)'s Routines", table: "AppIntentTag") {
            \.$scope
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[RoutineEntity]> & ProvidesDialog {
        _ = try await TagEntityQuery.require(tagId: tag.id)
        let itemPage = try await appAction {
            try await DiaryTagItemAppAction.shared.routineList(tagId: tag.id, scope: scope?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        let dialog = AppIntentResultDialog.list(
            count: itemPage.items.count,
            nextPageNumber: itemPage.nextPageNumber,
            found: { count in LocalizedStringResource("Found \(count) routines.", table: "AppIntentTag") },
            foundWithNextPage: { count, nextPage in
                LocalizedStringResource("Found \(count) routines. There's more on page \(nextPage).", table: "AppIntentTag")
            }
        )
        return .result(value: itemPage.items.map(RoutineEntity.init(routine:)), dialog: dialog)
    }
}

struct TagRoutineFinishedListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Tag's Finished Routines", table: "AppIntentTag")
    static let description = IntentDescription(LocalizedStringResource("Gets finished routines linked to a tag.", table: "AppIntentTag"))

    @Parameter(title: LocalizedStringResource("Tag", table: "AppIntentTag"))
    var tag: TagEntity

    @Parameter(
        title: LocalizedStringResource("Page", table: "AppIntentCore"),
        description: LocalizedStringResource("Page number starting at 1. Leave empty for the first page.", table: "AppIntentCore")
    )
    var page: Int?

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$tag)'s Finished Routines", table: "AppIntentTag") {
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[RoutineEntity]> & ProvidesDialog {
        _ = try await TagEntityQuery.require(tagId: tag.id)
        let itemPage = try await appAction {
            try await DiaryTagItemAppAction.shared.routineFinishedList(tagId: tag.id, pageNumber: AppIntentPage.number(page))
        }
        let dialog = AppIntentResultDialog.list(
            count: itemPage.items.count,
            nextPageNumber: itemPage.nextPageNumber,
            found: { count in LocalizedStringResource("Found \(count) routines.", table: "AppIntentTag") },
            foundWithNextPage: { count, nextPage in
                LocalizedStringResource("Found \(count) routines. There's more on page \(nextPage).", table: "AppIntentTag")
            }
        )
        return .result(value: itemPage.items.map(RoutineEntity.init(routine:)), dialog: dialog)
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
