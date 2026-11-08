import AppIntents
import DiaryIos

struct ContactListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Contacts", table: "AppIntentContact")
    static let description = IntentDescription(
        LocalizedStringResource("Gets contacts. Favorite contacts always come first.", table: "AppIntentContact")
    )

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
        Summary("Get Contacts", table: "AppIntentContact") {
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[ContactEntity]> & ProvidesDialog {
        let contactPage = try await appAction {
            try await DiaryContactAppAction.shared.list(sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        return .result(
            value: contactPage.items.map(ContactEntity.init(contact:)),
            dialog: AppIntentResultDialog.list(
                count: contactPage.items.count,
                nextPageNumber: contactPage.nextPageNumber,
                found: { count in LocalizedStringResource("Found \(count) contacts.", table: "AppIntentContact") },
                foundWithNextPage: { count, nextPage in
                    LocalizedStringResource("Found \(count) contacts. There's more on page \(nextPage).", table: "AppIntentContact")
                }
            )
        )
    }
}

struct ContactDetailIntent: AppIntent {
    static let title = LocalizedStringResource("Get Contact Details", table: "AppIntentContact")
    static let description = IntentDescription(LocalizedStringResource("Gets all details of a contact.", table: "AppIntentContact"))

    @Parameter(title: LocalizedStringResource("Contact", table: "AppIntentContact"))
    var contact: ContactEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Get \(\.$contact) Details", table: "AppIntentContact")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<ContactEntity> {
        .result(value: try await ContactEntityQuery.require(contactId: contact.id))
    }
}

struct ContactMemoListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Contact's In-Progress Memos", table: "AppIntentContact")
    static let description = IntentDescription(
        LocalizedStringResource("Gets memos linked to a contact that aren't finished.", table: "AppIntentContact")
    )

    @Parameter(title: LocalizedStringResource("Contact", table: "AppIntentContact"))
    var contact: ContactEntity

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
        Summary("Get \(\.$contact)'s In-Progress Memos", table: "AppIntentContact") {
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[MemoEntity]> & ProvidesDialog {
        let memoPage = try await appAction {
            try await DiaryContactAppAction.shared.memoList(contactId: contact.id, sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        return .result(value: memoPage.items.map(MemoEntity.init(memo:)), dialog: memoPage.dialog)
    }
}

struct ContactMemoFinishedListIntent: AppIntent {
    static let title = LocalizedStringResource("Get Contact's Finished Memos", table: "AppIntentContact")
    static let description = IntentDescription(LocalizedStringResource("Gets finished memos linked to a contact.", table: "AppIntentContact"))

    @Parameter(title: LocalizedStringResource("Contact", table: "AppIntentContact"))
    var contact: ContactEntity

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
        Summary("Get \(\.$contact)'s Finished Memos", table: "AppIntentContact") {
            \.$sort
            \.$page
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<[MemoEntity]> & ProvidesDialog {
        let memoPage = try await appAction {
            try await DiaryContactAppAction.shared.memoFinishedList(contactId: contact.id, sort: sort?.kotlin, pageNumber: AppIntentPage.number(page))
        }
        return .result(value: memoPage.items.map(MemoEntity.init(memo:)), dialog: memoPage.dialog)
    }
}

private extension DiaryAppActionContactMemoPage {
    nonisolated var dialog: IntentDialog {
        AppIntentResultDialog.list(
            count: items.count,
            nextPageNumber: nextPageNumber,
            found: { count in LocalizedStringResource("Found \(count) memos.", table: "AppIntentContact") },
            foundWithNextPage: { count, nextPage in
                LocalizedStringResource("Found \(count) memos. There's more on page \(nextPage).", table: "AppIntentContact")
            }
        )
    }
}
