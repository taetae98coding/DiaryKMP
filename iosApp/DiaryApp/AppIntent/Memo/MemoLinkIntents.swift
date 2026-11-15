import AppIntents
import DiaryIos

// 데스크톱 연결과 같이 연결·해제는 바뀐 개수 대신 완료 여부(done)를 돌려준다.

struct LinkTagToMemoIntent: AppIntent {
    static let title = LocalizedStringResource("Link Tag to Memo", table: "AppIntentMemoLink")
    static let description = IntentDescription(LocalizedStringResource("Links a tag to a memo.", table: "AppIntentMemoLink"))

    @Parameter(title: LocalizedStringResource("Memo", table: "AppIntentMemo"))
    var memo: MemoEntity

    @Parameter(title: LocalizedStringResource("Tag to Link", table: "AppIntentMemoLink"))
    var tag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Link \(\.$tag) to \(\.$memo)", table: "AppIntentMemoLink")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Bool> & ProvidesDialog {
        _ = try await MemoEntityQuery.require(memoId: memo.id)
        _ = try await TagEntityQuery.require(tagId: tag.id)
        let done = try await appAction { try await DiaryMemoLinkAppAction.shared.tagAdd(memoId: memo.id, tagId: tag.id) }
        return .result(value: done, dialog: IntentDialog(LocalizedStringResource("Linked.", table: "AppIntentCore")))
    }
}

struct UnlinkTagFromMemoIntent: AppIntent {
    static let title = LocalizedStringResource("Unlink Tag from Memo", table: "AppIntentMemoLink")
    static let description = IntentDescription(LocalizedStringResource("Unlinks a tag from a memo. If it was the primary tag, the primary tag is cleared too.", table: "AppIntentMemoLink"))

    @Parameter(title: LocalizedStringResource("Memo", table: "AppIntentMemo"))
    var memo: MemoEntity

    @Parameter(title: LocalizedStringResource("Tag to Unlink", table: "AppIntentMemoLink"))
    var tag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Unlink \(\.$tag) from \(\.$memo)", table: "AppIntentMemoLink")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Bool> & ProvidesDialog {
        _ = try await MemoEntityQuery.require(memoId: memo.id)
        _ = try await TagEntityQuery.require(tagId: tag.id)
        let done = try await appAction { try await DiaryMemoLinkAppAction.shared.tagRemove(memoId: memo.id, tagId: tag.id) }
        return .result(value: done, dialog: IntentDialog(LocalizedStringResource("Unlinked.", table: "AppIntentCore")))
    }
}

struct SetMemoPrimaryTagIntent: AppIntent {
    static let title = LocalizedStringResource("Set Memo Primary Tag", table: "AppIntentMemoLink")
    static let description = IntentDescription(LocalizedStringResource("Sets the primary tag of a memo. The tag is linked too if it isn't linked yet.", table: "AppIntentMemoLink"))

    @Parameter(title: LocalizedStringResource("Memo", table: "AppIntentMemo"))
    var memo: MemoEntity

    @Parameter(title: LocalizedStringResource("Primary Tag", table: "AppIntentCore"))
    var tag: TagEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Set \(\.$tag) as primary tag of \(\.$memo)", table: "AppIntentMemoLink")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Bool> & ProvidesDialog {
        _ = try await MemoEntityQuery.require(memoId: memo.id)
        _ = try await TagEntityQuery.require(tagId: tag.id)
        let done = try await appAction { try await DiaryMemoLinkAppAction.shared.primaryTagSet(memoId: memo.id, tagId: tag.id) }
        return .result(value: done, dialog: IntentDialog(LocalizedStringResource("Set the primary tag.", table: "AppIntentMemoLink")))
    }
}

struct ClearMemoPrimaryTagIntent: AppIntent {
    static let title = LocalizedStringResource("Clear Memo Primary Tag", table: "AppIntentMemoLink")
    static let description = IntentDescription(LocalizedStringResource("Clears the primary tag of a memo. The tag stays linked.", table: "AppIntentMemoLink"))

    @Parameter(title: LocalizedStringResource("Memo", table: "AppIntentMemo"))
    var memo: MemoEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Clear primary tag of \(\.$memo)", table: "AppIntentMemoLink")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Bool> & ProvidesDialog {
        _ = try await MemoEntityQuery.require(memoId: memo.id)
        let done = try await appAction { try await DiaryMemoLinkAppAction.shared.primaryTagUnset(memoId: memo.id) }
        return .result(value: done, dialog: IntentDialog(LocalizedStringResource("Cleared the primary tag.", table: "AppIntentMemoLink")))
    }
}

struct LinkPlaceToMemoIntent: AppIntent {
    static let title = LocalizedStringResource("Link Place to Memo", table: "AppIntentMemoLink")
    static let description = IntentDescription(LocalizedStringResource("Links a place to a memo.", table: "AppIntentMemoLink"))

    @Parameter(title: LocalizedStringResource("Memo", table: "AppIntentMemo"))
    var memo: MemoEntity

    @Parameter(title: LocalizedStringResource("Place to Link", table: "AppIntentMemoLink"))
    var place: PlaceEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Link \(\.$place) to \(\.$memo)", table: "AppIntentMemoLink")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Bool> & ProvidesDialog {
        _ = try await MemoEntityQuery.require(memoId: memo.id)
        _ = try await PlaceEntityQuery.require(placeId: place.id)
        let done = try await appAction { try await DiaryMemoLinkAppAction.shared.placeAdd(memoId: memo.id, placeId: place.id) }
        return .result(value: done, dialog: IntentDialog(LocalizedStringResource("Linked.", table: "AppIntentCore")))
    }
}

struct UnlinkPlaceFromMemoIntent: AppIntent {
    static let title = LocalizedStringResource("Unlink Place from Memo", table: "AppIntentMemoLink")
    static let description = IntentDescription(LocalizedStringResource("Unlinks a place from a memo.", table: "AppIntentMemoLink"))

    @Parameter(title: LocalizedStringResource("Memo", table: "AppIntentMemo"))
    var memo: MemoEntity

    @Parameter(title: LocalizedStringResource("Place to Unlink", table: "AppIntentMemoLink"))
    var place: PlaceEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Unlink \(\.$place) from \(\.$memo)", table: "AppIntentMemoLink")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Bool> & ProvidesDialog {
        _ = try await MemoEntityQuery.require(memoId: memo.id)
        _ = try await PlaceEntityQuery.require(placeId: place.id)
        let done = try await appAction { try await DiaryMemoLinkAppAction.shared.placeRemove(memoId: memo.id, placeId: place.id) }
        return .result(value: done, dialog: IntentDialog(LocalizedStringResource("Unlinked.", table: "AppIntentCore")))
    }
}

struct LinkWebToMemoIntent: AppIntent {
    static let title = LocalizedStringResource("Link Web to Memo", table: "AppIntentMemoLink")
    static let description = IntentDescription(LocalizedStringResource("Links a web to a memo.", table: "AppIntentMemoLink"))

    @Parameter(title: LocalizedStringResource("Memo", table: "AppIntentMemo"))
    var memo: MemoEntity

    @Parameter(title: LocalizedStringResource("Web to Link", table: "AppIntentMemoLink"))
    var web: WebEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Link \(\.$web) to \(\.$memo)", table: "AppIntentMemoLink")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Bool> & ProvidesDialog {
        _ = try await MemoEntityQuery.require(memoId: memo.id)
        _ = try await WebEntityQuery.require(webId: web.id)
        let done = try await appAction { try await DiaryMemoLinkAppAction.shared.webAdd(memoId: memo.id, webId: web.id) }
        return .result(value: done, dialog: IntentDialog(LocalizedStringResource("Linked.", table: "AppIntentCore")))
    }
}

struct UnlinkWebFromMemoIntent: AppIntent {
    static let title = LocalizedStringResource("Unlink Web from Memo", table: "AppIntentMemoLink")
    static let description = IntentDescription(LocalizedStringResource("Unlinks a web from a memo.", table: "AppIntentMemoLink"))

    @Parameter(title: LocalizedStringResource("Memo", table: "AppIntentMemo"))
    var memo: MemoEntity

    @Parameter(title: LocalizedStringResource("Web to Unlink", table: "AppIntentMemoLink"))
    var web: WebEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Unlink \(\.$web) from \(\.$memo)", table: "AppIntentMemoLink")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Bool> & ProvidesDialog {
        _ = try await MemoEntityQuery.require(memoId: memo.id)
        _ = try await WebEntityQuery.require(webId: web.id)
        let done = try await appAction { try await DiaryMemoLinkAppAction.shared.webRemove(memoId: memo.id, webId: web.id) }
        return .result(value: done, dialog: IntentDialog(LocalizedStringResource("Unlinked.", table: "AppIntentCore")))
    }
}

struct LinkContactToMemoIntent: AppIntent {
    static let title = LocalizedStringResource("Link Contact to Memo", table: "AppIntentMemoLink")
    static let description = IntentDescription(LocalizedStringResource("Links a contact to a memo.", table: "AppIntentMemoLink"))

    @Parameter(title: LocalizedStringResource("Memo", table: "AppIntentMemo"))
    var memo: MemoEntity

    @Parameter(title: LocalizedStringResource("Contact to Link", table: "AppIntentMemoLink"))
    var contact: ContactEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Link \(\.$contact) to \(\.$memo)", table: "AppIntentMemoLink")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Bool> & ProvidesDialog {
        _ = try await MemoEntityQuery.require(memoId: memo.id)
        _ = try await ContactEntityQuery.require(contactId: contact.id)
        let done = try await appAction { try await DiaryMemoLinkAppAction.shared.contactAdd(memoId: memo.id, contactId: contact.id) }
        return .result(value: done, dialog: IntentDialog(LocalizedStringResource("Linked.", table: "AppIntentCore")))
    }
}

struct UnlinkContactFromMemoIntent: AppIntent {
    static let title = LocalizedStringResource("Unlink Contact from Memo", table: "AppIntentMemoLink")
    static let description = IntentDescription(LocalizedStringResource("Unlinks a contact from a memo.", table: "AppIntentMemoLink"))

    @Parameter(title: LocalizedStringResource("Memo", table: "AppIntentMemo"))
    var memo: MemoEntity

    @Parameter(title: LocalizedStringResource("Contact to Unlink", table: "AppIntentMemoLink"))
    var contact: ContactEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Unlink \(\.$contact) from \(\.$memo)", table: "AppIntentMemoLink")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Bool> & ProvidesDialog {
        _ = try await MemoEntityQuery.require(memoId: memo.id)
        _ = try await ContactEntityQuery.require(contactId: contact.id)
        let done = try await appAction { try await DiaryMemoLinkAppAction.shared.contactRemove(memoId: memo.id, contactId: contact.id) }
        return .result(value: done, dialog: IntentDialog(LocalizedStringResource("Unlinked.", table: "AppIntentCore")))
    }
}
