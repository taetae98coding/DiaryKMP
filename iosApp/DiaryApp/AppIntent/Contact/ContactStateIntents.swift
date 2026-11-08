import AppIntents
import DiaryIos

struct FavoriteContactIntent: AppIntent {
    static let title = LocalizedStringResource("Favorite Contact", table: "AppIntentContact")
    static let description = IntentDescription(LocalizedStringResource("Marks a contact as a favorite.", table: "AppIntentContact"))

    @Parameter(title: LocalizedStringResource("Contact", table: "AppIntentContact"))
    var contact: ContactEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Favorite \(\.$contact)", table: "AppIntentContact")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        let changedCount = try await appAction { try await DiaryContactAppAction.shared.favorite(contactId: contact.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Added to favorites.", table: "AppIntentContact"))
        )
    }
}

struct UnfavoriteContactIntent: AppIntent {
    static let title = LocalizedStringResource("Unfavorite Contact", table: "AppIntentContact")
    static let description = IntentDescription(LocalizedStringResource("Removes the favorite mark from a contact.", table: "AppIntentContact"))

    @Parameter(title: LocalizedStringResource("Contact", table: "AppIntentContact"))
    var contact: ContactEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Unfavorite \(\.$contact)", table: "AppIntentContact")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        let changedCount = try await appAction { try await DiaryContactAppAction.shared.unfavorite(contactId: contact.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Removed from favorites.", table: "AppIntentContact"))
        )
    }
}

struct DeleteContactIntent: AppIntent {
    static let title = LocalizedStringResource("Delete Contact", table: "AppIntentContact")
    static let description = IntentDescription(
        LocalizedStringResource("Deletes a contact. Run Restore Deleted Contact with the same contact to undo.", table: "AppIntentContact")
    )

    @Parameter(title: LocalizedStringResource("Contact", table: "AppIntentContact"))
    var contact: ContactEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Delete \(\.$contact)", table: "AppIntentContact")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        let changedCount = try await appAction { try await DiaryContactAppAction.shared.delete(contactId: contact.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Deleted the contact.", table: "AppIntentContact"))
        )
    }
}

struct RestoreContactIntent: AppIntent {
    static let title = LocalizedStringResource("Restore Deleted Contact", table: "AppIntentContact")
    static let description = IntentDescription(LocalizedStringResource("Restores a deleted contact.", table: "AppIntentContact"))

    @Parameter(title: LocalizedStringResource("Contact to Restore", table: "AppIntentContact"))
    var contact: ContactEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Restore \(\.$contact)", table: "AppIntentContact")
    }

    func perform() async throws -> some IntentResult & ReturnsValue<Int> & ProvidesDialog {
        let changedCount = try await appAction { try await DiaryContactAppAction.shared.restore(contactId: contact.id) }
        return .result(
            value: Int(changedCount),
            dialog: AppIntentResultDialog.changed(count: changedCount, changed: LocalizedStringResource("Restored the contact.", table: "AppIntentContact"))
        )
    }
}
