import AppIntents
import DiaryIos

struct AddMemoIntent: AppIntent {
    static let title = LocalizedStringResource("Add Memo", table: "AppIntentMemo")
    static let description = IntentDescription(
        LocalizedStringResource("Adds a new memo. A random color is used if you don't choose one.", table: "AppIntentMemo")
    )

    @Parameter(
        title: LocalizedStringResource("Title", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, leave it empty to keep the current title.", table: "AppIntentCore"),
        requestValueDialog: IntentDialog(LocalizedStringResource("What's the memo title?", table: "AppIntentMemo"))
    )
    var title: String

    @Parameter(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var memoDescription: String?

    @Parameter(
        title: LocalizedStringResource("Color", table: "AppIntentCore"),
        description: LocalizedStringResource("Enter a hex color like #4285F4.", table: "AppIntentCore")
    )
    var color: String?

    @Parameter(
        title: LocalizedStringResource("All Day", table: "AppIntentCore"),
        description: LocalizedStringResource("On uses dates only; off uses date and time.", table: "AppIntentCore")
    )
    var allDay: Bool?

    @Parameter(title: LocalizedStringResource("Start", table: "AppIntentCore"))
    var start: Date?

    @Parameter(
        title: LocalizedStringResource("End", table: "AppIntentCore"),
        description: LocalizedStringResource("The end is included.", table: "AppIntentCore")
    )
    var end: Date?

    @Parameter(title: LocalizedStringResource("Primary Tag", table: "AppIntentCore"))
    var primaryTag: TagEntity?

    @Parameter(title: LocalizedStringResource("Tags to Link", table: "AppIntentCore"))
    var tags: [TagEntity]?

    @Parameter(title: LocalizedStringResource("Places to Link", table: "AppIntentCore"))
    var places: [PlaceEntity]?

    @Parameter(title: LocalizedStringResource("Webs to Link", table: "AppIntentCore"))
    var webs: [WebEntity]?

    @Parameter(title: LocalizedStringResource("Contacts to Link", table: "AppIntentCore"))
    var contacts: [ContactEntity]?

    static var parameterSummary: some ParameterSummary {
        Summary("Add memo \(\.$title)", table: "AppIntentMemo") {
            \.$memoDescription
            \.$color
            \.$allDay
            \.$start
            \.$end
            \.$primaryTag
            \.$tags
            \.$places
            \.$webs
            \.$contacts
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<MemoEntity> & ProvidesDialog {
        let memoId = try await appAction {
            try await DiaryMemoAppAction.shared.add(
                title: title,
                memoDescription: memoDescription,
                color: color,
                allDay: allDay,
                startEpochMillis: start?.epochMillis,
                endInclusiveEpochMillis: end?.epochMillis,
                primaryTagId: primaryTag?.id,
                tagIds: (tags ?? []).map(\.id),
                placeIds: (places ?? []).map(\.id),
                webIds: (webs ?? []).map(\.id),
                contactIds: (contacts ?? []).map(\.id)
            )
        }
        return .result(
            value: try await MemoEntityQuery.require(memoId: memoId),
            dialog: IntentDialog(LocalizedStringResource("Added the memo.", table: "AppIntentMemo"))
        )
    }
}

struct EditMemoIntent: AppIntent {
    static let title = LocalizedStringResource("Edit Memo", table: "AppIntentMemo")
    static let description = IntentDescription(
        LocalizedStringResource("Changes the title, description, color or period of a memo. Values left empty stay the same.", table: "AppIntentMemo")
    )

    @Parameter(title: LocalizedStringResource("Memo", table: "AppIntentMemo"))
    var memo: MemoEntity

    @Parameter(
        title: LocalizedStringResource("Title", table: "AppIntentCore"),
        description: LocalizedStringResource("When editing, leave it empty to keep the current title.", table: "AppIntentCore")
    )
    var title: String?

    @Parameter(title: LocalizedStringResource("Description", table: "AppIntentCore"))
    var memoDescription: String?

    @Parameter(
        title: LocalizedStringResource("Color", table: "AppIntentCore"),
        description: LocalizedStringResource("Enter a hex color like #4285F4.", table: "AppIntentCore")
    )
    var color: String?

    @Parameter(
        title: LocalizedStringResource("All Day", table: "AppIntentCore"),
        description: LocalizedStringResource("On uses dates only; off uses date and time.", table: "AppIntentCore")
    )
    var allDay: Bool?

    @Parameter(title: LocalizedStringResource("Start", table: "AppIntentCore"))
    var start: Date?

    @Parameter(
        title: LocalizedStringResource("End", table: "AppIntentCore"),
        description: LocalizedStringResource("The end is included.", table: "AppIntentCore")
    )
    var end: Date?

    @Parameter(
        title: LocalizedStringResource("Remove Period", table: "AppIntentCore"),
        description: LocalizedStringResource("Turn on to remove the period.", table: "AppIntentCore")
    )
    var clearDateTime: Bool?

    static var parameterSummary: some ParameterSummary {
        Summary("Edit \(\.$memo)", table: "AppIntentMemo") {
            \.$title
            \.$memoDescription
            \.$color
            \.$allDay
            \.$start
            \.$end
            \.$clearDateTime
        }
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        _ = try await MemoEntityQuery.require(memoId: memo.id)
        let changedCount = try await appAction {
            try await DiaryMemoAppAction.shared.update(
                memoId: memo.id,
                title: title,
                memoDescription: memoDescription,
                color: color,
                allDay: allDay,
                startEpochMillis: start?.epochMillis,
                endInclusiveEpochMillis: end?.epochMillis,
                clearDateTime: clearDateTime
            )
        }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Updated the memo.", table: "AppIntentMemo"))
        )
    }
}

struct CopyMemoIntent: AppIntent {
    static let title = LocalizedStringResource("Copy Memo", table: "AppIntentMemo")
    static let description = IntentDescription(
        LocalizedStringResource("Copies a memo with its links into a new memo that isn't finished.", table: "AppIntentMemo")
    )

    @Parameter(title: LocalizedStringResource("Memo", table: "AppIntentMemo"))
    var memo: MemoEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Copy \(\.$memo)", table: "AppIntentMemo")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<MemoEntity> & ProvidesDialog {
        _ = try await MemoEntityQuery.require(memoId: memo.id)
        let memoId = try await appAction { try await DiaryMemoAppAction.shared.copy(memoId: memo.id) }
        return .result(
            value: try await MemoEntityQuery.require(memoId: memoId),
            dialog: IntentDialog(LocalizedStringResource("Copied the memo.", table: "AppIntentMemo"))
        )
    }
}
